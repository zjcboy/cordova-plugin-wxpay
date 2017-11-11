package __PACKAGE_NAME__;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import chinfan.cordova.wxpay.WXPay;;

public class EntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IWXAPI api = WXPay.getWxAPI(this);

        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        IWXAPI api = WXPay.getWxAPI(this);
        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(intent, this);
        }

    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(WXPay.TAG, resp.toString());

        CallbackContext ctx = WXPay.getCurrentCallbackContext();

        if (ctx == null) {
            startMainActivity();
            return ;
        }

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                switch (resp.getType()) {
                    case ConstantsAPI.COMMAND_PAY_BY_WX:
                    default:
                        ctx.success();
                        break;
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ctx.error(WXPay.ERROR_WECHAT_RESPONSE_USER_CANCEL);
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                ctx.error(WXPay.ERROR_WECHAT_RESPONSE_SENT_FAILED);
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                ctx.error(WXPay.ERROR_WECHAT_RESPONSE_UNSUPPORT);
                break;
            case BaseResp.ErrCode.ERR_COMM:
                ctx.error(WXPay.ERROR_WECHAT_RESPONSE_COMMON);
                break;
            default:
                ctx.error(WXPay.ERROR_WECHAT_RESPONSE_UNKNOWN);
                break;
        }

        finish();
    }

    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    protected void startMainActivity() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(intent);
    }
}
