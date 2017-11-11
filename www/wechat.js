var exec = require('cordova/exec');

module.exports = {
    isInstalled: function (onSuccess, onError) {
        exec(onSuccess, onError, "WXPay", "isWXAppInstalled", []);
    },

    /**
     * Send a payment request
     *
     * @link https://pay.weixin.qq.com/wiki/doc/api/app.php?chapter=9_1
     * @example
     * <code>
     * var params = {
     *     mchId: '10000100', // merchantId
     *     prepayId: 'wx201411101639507cbf6ffd8b0779950874', // prepayId returned from server
     *     nonce: '1add1a30ac87aa2db72f57a2375d8fec', // nonce string returned from server
     *     timeStamp: '1439531364', // timeStamp
     *     sign: '0CB01533B8C1EF103065174F50BCA001', // signed string
     * };
     * WXPay.requestPayment(params, function () {
     *     alert("Success");
     * }, function (reason) {
     *     alert("Failed: " + reason);
     * });
     * </code>
     */
    requestPayment: function (params, onSuccess, onError) {
        exec(onSuccess, onError, "WXPay", "requestPayment", [params]);
    }
};
