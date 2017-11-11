//
//  CDVWechat.h
//  cordova-plugin-wxpay
//
//  Created by xu.li on 12/23/13.
//
//

#import <Cordova/CDV.h>
#import "WXApi.h"
#import "WXApiObject.h"


@interface CDVWXPay:CDVPlugin <WXApiDelegate>

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *wechatAppId;

- (void)isWxAppInstalled:(CDVInvokedUrlCommand *)command;
- (void)requestPayment:(CDVInvokedUrlCommand *)command;

@end
