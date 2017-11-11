package chinfan.cordova.wxpay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class WXPay extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.WXPay";

    public static final String PREFS_NAME = "Cordova.Plugin.WXPay";
    public static final String WXAPPID_PROPERTY_KEY = "wechatappid";

    public static final String ERROR_WECHAT_NOT_INSTALLED = "未安装微信";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public static final String ERROR_SEND_REQUEST_FAILED = "发送请求失败";
    public static final String ERROR_WECHAT_RESPONSE_COMMON = "普通错误";
    public static final String ERROR_WECHAT_RESPONSE_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_WECHAT_RESPONSE_SENT_FAILED = "发送失败";
    public static final String ERROR_WECHAT_RESPONSE_UNSUPPORT = "微信不支持";
    public static final String ERROR_WECHAT_RESPONSE_UNKNOWN = "未知错误";

    public static final String EXTERNAL_STORAGE_IMAGE_PREFIX = "external://";


    protected static CallbackContext currentCallbackContext;
    protected static IWXAPI wxAPI;

    protected String appId;

    @Override
    protected void pluginInitialize() {

        super.pluginInitialize();

        String id = getAppId();

        // save app id
        saveAppId(cordova.getActivity(), id);

        // init api
        initWXAPI();

        Log.d(TAG, "plugin initialized.");
    }

    protected void initWXAPI() {
        IWXAPI api = getWxAPI(cordova.getActivity());

        if (api != null) {
            api.registerApp(getAppId());
        }
    }

    /**
     * Get weixin api
     * @param ctx
     * @return
     */
    public static IWXAPI getWxAPI(Context ctx) {
        if (wxAPI == null) {
            String appId = getSavedAppId(ctx);

            if (!appId.isEmpty()) {
                wxAPI = WXAPIFactory.createWXAPI(ctx, appId, true);
            }
        }

        return wxAPI;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        final IWXAPI api = getWxAPI(cordova.getActivity());
        // check if installed
        if (!api.isWXAppInstalled()) {
            callbackContext.error(ERROR_WECHAT_NOT_INSTALLED);
            return true;
        }

        Log.d(TAG, String.format("%s is called. Callback ID: %s.", action, callbackContext.getCallbackId()));

        if (action.equals("requestPayment")) {
            return requestPayment(args, callbackContext);
        } else if (action.equals("isWXAppInstalled")) {
            return isInstalled(callbackContext);
        }

        return false;
    }

    protected boolean requestPayment(CordovaArgs args, CallbackContext callbackContext) {

        final IWXAPI api = getWxAPI(cordova.getActivity());

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        PayReq req = new PayReq();

        try {
            req.appId = getAppId();
            req.partnerId = params.has("mchId") ? params.getString("mchId") : params.getString("partnerId");
            req.prepayId = params.has("prepayId") ? params.getString("prepayId") : params.getString("prepayId");
            req.nonceStr = params.has("nonce") ? params.getString("nonce") : params.getString("nonceStr");
            req.timeStamp = params.getString("timeStamp");
            req.sign = params.getString("sign");
            req.packageValue = "Sign=WXPay";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Payment request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully.");

            // send error
            callbackContext.error(ERROR_SEND_REQUEST_FAILED);
        }

        return true;
    }

    protected boolean isInstalled(CallbackContext callbackContext) {
        final IWXAPI api = getWxAPI(cordova.getActivity());

        if (!api.isWXAppInstalled()) {
            callbackContext.success(0);
        } else {
            callbackContext.success(1);
        }

        return true;
    }

    private String buildTransaction() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String buildTransaction(final String type) {
        return type + System.currentTimeMillis();
    }


    public String getAppId() {
        if (this.appId == null) {
            this.appId = preferences.getString(WXAPPID_PROPERTY_KEY, "");
        }

        return this.appId;
    }

    /**
     * Get saved app id
     * @param ctx
     * @return
     */
    public static String getSavedAppId(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(WXAPPID_PROPERTY_KEY, "");
    }

    /**
     * Save app id into SharedPreferences
     * @param ctx
     * @param id
     */
    public static void saveAppId(Context ctx, String id) {
        if (id.isEmpty()) {
            return;
        }

        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(WXAPPID_PROPERTY_KEY, id);
        editor.commit();
    }

    public static CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
}
