//
//  CDVWechat.m
//  cordova-plugin-wxpay
//
//  Created by xu.li on 12/23/13.
//
//

#import "CDVWechat.h"

static int const MAX_THUMBNAIL_SIZE = 320;

@implementation CDVWXPay

#pragma mark "API"
- (void)pluginInitialize {
    NSString* appId = [[self.commandDelegate settings] objectForKey:@"wechatappid"];
    if (appId){
        self.wechatAppId = appId;
        [WXApi registerApp: appId];
    }

    NSLog(@"cordova-plugin-wxpay has been initialized. WXPay SDK Version: %@. APP_ID: %@.", [WXApi getApiVersion], appId);
}

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[WXApi isWXAppInstalled]];

    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)requestPayment:(CDVInvokedUrlCommand *)command
{

     // if not installed
    if (![WXApi isWXAppInstalled])
    {
        [self failWithCallbackID:command.callbackId withMessage:@"未安装微信"];
        return ;
    } else {
        // check arguments
        NSDictionary *params = [command.arguments objectAtIndex:0];
        if (!params)
        {
            [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
            return ;
        }

        // check required parameters
        NSArray *requiredParams;
        if ([params objectForKey:@"mchId"])
        {
            requiredParams = @[@"mchId", @"prepayId", @"timeStamp", @"nonce", @"sign"];
        }
        else
        {
            requiredParams = @[@"partnerId", @"prepayId", @"timeStamp", @"nonceStr", @"sign"];
        }

        for (NSString *key in requiredParams)
        {
            if (![params objectForKey:key])
            {
                [self failWithCallbackID:command.callbackId withMessage:@"参数格式错误"];
                return ;
            }
        }

        PayReq *req = [[PayReq alloc] init];
        req.partnerId = [params objectForKey:requiredParams[0]];
        req.prepayId = [params objectForKey:requiredParams[1]];
        req.timeStamp = [[params objectForKey:requiredParams[2]] intValue];
        req.nonceStr = [params objectForKey:requiredParams[3]];
        req.package = @"Sign=WXPay";
        req.sign = [params objectForKey:requiredParams[4]];

        if ([WXApi sendReq:req])
        {
            // save the callback id
            self.currentCallbackId = command.callbackId;
        }
        else
        {
            [self failWithCallbackID:command.callbackId withMessage:@"发送请求失败"];
        }
    }
}


#pragma mark "WXApiDelegate"

/**
 * Not implemented
 */
- (void)onReq:(BaseReq *)req
{
    NSLog(@"%@", req);
}

- (void)onResp:(BaseResp *)resp
{
    BOOL success = NO;
    NSString *message = @"Unknown";
    NSDictionary *response = nil;

    switch (resp.errCode)
    {
        case WXSuccess:
            success = YES;
            break;

        case WXErrCodeCommon:
            message = @"普通错误";
            break;

        case WXErrCodeUserCancel:
            message = @"用户点击取消并返回";
            break;

        case WXErrCodeSentFail:
            message = @"发送失败";
            break;

        case WXErrCodeAuthDeny:
            message = @"授权失败";
            break;

        case WXErrCodeUnsupport:
            message = @"微信不支持";
            break;

        default:
            message = @"未知错误";
    }

    if (success)
    {
        if ([resp isKindOfClass:[SendAuthResp class]])
        {
            // fix issue that lang and country could be nil for iPhone 6 which caused crash.
            SendAuthResp* authResp = (SendAuthResp*)resp;
            response = @{
                         @"code": authResp.code != nil ? authResp.code : @"",
                         @"state": authResp.state != nil ? authResp.state : @"",
                         @"lang": authResp.lang != nil ? authResp.lang : @"",
                         @"country": authResp.country != nil ? authResp.country : @"",
                         };

            CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];

            [self.commandDelegate sendPluginResult:commandResult callbackId:self.currentCallbackId];
        }
        else
        {
            [self successWithCallbackID:self.currentCallbackId];
        }
    }
    else
    {
        [self failWithCallbackID:self.currentCallbackId withMessage:message];
    }

    self.currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"


- (void)successWithCallbackID:(NSString *)callbackID
{
    [self successWithCallbackID:callbackID withMessage:@"OK"];
}

- (void)successWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID withError:(NSError *)error
{
    [self failWithCallbackID:callbackID withMessage:[error localizedDescription]];
}

- (void)failWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

@end
