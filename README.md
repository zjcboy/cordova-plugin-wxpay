# cordova-plugin-wxpay

A cordova plugin, a JS version of Wechat SDK

# Example

See [cordova-plugin-wechat-example](https://github.com/xu-li/cordova-plugin-wechat-example)

# Install

1. ```cordova plugin add cordova-plugin-wechat  --variable wechatappid=YOUR_WECHAT_APPID```, or using [plugman](https://npmjs.org/package/plugman), [phonegap](https://npmjs.org/package/phonegap), [ionic](http://ionicframework.com/)

2. ```cordova build ios``` or ```cordova build android```

3. (iOS only) if your cordova version <5.1.1,check the URL Type using XCode

# Usage

## Check if wechat is installed
```Javascript
Wechat.isInstalled(function (installed) {
    alert("Wechat installed: " + (installed ? "Yes" : "No"));
}, function (reason) {
    alert("Failed: " + reason);
});
```


## Send payment request
```Javascript
var = {
    partnerId: '10000100', // merchant id
    prepayId: 'wx201411101639507cbf6ffd8b0779950874', // prepay id
    nonceStr: '1add1a30ac87aa2db72f57a2375d8fec', // nonce
    timeStamp: '1439531364', // timestamp
    sign: '0CB01533B8C1EF103065174F50BCA001', // signed string
};

Wechat.sendPaymentRequest(params, function () {
    alert("Success");
}, function (reason) {
    alert("Failed: " + reason);
});
```
# cordova-plugin-wechat
# cordova-plugin-wxpay
