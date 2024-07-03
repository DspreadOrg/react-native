//
//  NativePosModule.m
//  ReactNativeDemo
//
//  Created by fangzhengwei on 2022/4/13.
//

#import "NativePosModule.h"
#import "QPOSService.h"
#import "QPOSUtil.h"
#import "BTDeviceFinder.h"
BOOL is2ModeBluetooth = YES;
@interface NativePosModule()<RCTBridgeModule,BluetoothDelegate2Mode,QPOSServiceListener>
@property (nonatomic,strong)QPOSService *pos;
@property (nonatomic,strong)BTDeviceFinder *bt;
@end
@implementation NativePosModule
RCT_EXPORT_MODULE();

-(NSArray<NSString *> *)supportedEvents{
  return @[@"NativePosReminder"];
}

RCT_EXPORT_METHOD(scanQPos2Mode:(NSInteger)timeout){
  [self scanBluetooth:timeout];
}

RCT_EXPORT_METHOD(stopQPos2Mode){
  [self.bt stopQPos2Mode];
}

RCT_EXPORT_METHOD(connectBT:(NSString *)bluetoothName){
  if (nil == self.pos) {
      self.pos = [QPOSService sharedInstance];
  }
  [self.pos setDelegate:self];
  [self.pos setQueue:nil];
  [self.pos setPosType:PosType_BLUETOOTH_2mode];
  [self.pos connectBT:bluetoothName];
  [self.pos setBTAutoDetecting:true];
}

RCT_EXPORT_METHOD(disconnectBT){
  [self.pos disconnectBT];
}

RCT_EXPORT_METHOD(setCardTradeMode:(NSInteger)aCardTMode){
  [self.pos setCardTradeMode:aCardTMode];
}

RCT_EXPORT_METHOD(doTrade){
  [self.pos doTrade];
}

RCT_EXPORT_METHOD(doTrade:(NSInteger)keyIndex delays:(NSInteger)timeout){
  [self.pos doTrade:keyIndex delays:timeout];
}

RCT_EXPORT_METHOD(doCheckCard:(NSInteger)timeout keyIndex:(NSInteger) mKeyIndex){
  [self.pos doCheckCard:timeout keyIndex:mKeyIndex];
}

RCT_EXPORT_METHOD(sendTime:(NSString *)aterminalTime){
  [self.pos sendTime:aterminalTime];
}

RCT_EXPORT_METHOD(doEmvApp:(NSInteger)aemvOption){
  [self.pos doEmvApp:aemvOption];
}

RCT_EXPORT_METHOD(selectEmvApp: (NSInteger)index){
  [self.pos selectEmvApp:index];
}

RCT_EXPORT_METHOD(setAmount: (NSString *)aAmount aAmountDescribe:(NSString *)aAmountDescribe currency:(NSString *)currency transactionType:(NSInteger)transactionType){
  [self.pos setAmount:aAmount aAmountDescribe:aAmountDescribe currency:currency transactionType:transactionType];
}

RCT_EXPORT_METHOD(sendPinEntryResult:(NSString *)pin){
  [self.pos sendPinEntryResult:pin];
}

RCT_EXPORT_METHOD(sendOnlineProcessResult:(NSString *)tlv){
  [self.pos sendOnlineProcessResult:tlv];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getNFCBatchData){
  return [self.pos getNFCBatchData];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getICCTag:(NSInteger)encryptType cardType:(NSInteger)cardType tagCount:(NSInteger) mTagCount tagArrStr:(NSString*)mTagArrStr){
  return [self.pos getICCTag:encryptType cardType:cardType tagCount:mTagCount tagArrStr:mTagArrStr];
}

RCT_EXPORT_METHOD(getQPosId){
  [self.pos getQPosId];
}

RCT_EXPORT_METHOD(getQPosInfo){
  [self.pos getQPosInfo];
}

RCT_EXPORT_METHOD(setMasterKey:(NSString *)key checkValue:(NSString *)chkValue){
  [self.pos setMasterKey:key checkValue:chkValue];
}

RCT_EXPORT_METHOD(udpateWorkKey:(NSString *)pik pinKeyCheck:(NSString *)pikCheck trackKey:(NSString *)trk trackKeyCheck:(NSString *)trkCheck macKey:(NSString *)mak macKeyCheck:(NSString *)makCheck){
  [self.pos udpateWorkKey:pik pinKeyCheck:pikCheck trackKey:trk trackKeyCheck:trkCheck macKey:mak macKeyCheck:makCheck];
}

RCT_EXPORT_METHOD(doUpdateIPEKOperation:(NSString *)groupKey
                  tracksn:(NSString *)trackksn
                trackipek:(NSString *)trackipek
      trackipekCheckValue:(NSString *)trackipekCheckValue
                   emvksn:(NSString *)emvksn
                  emvipek:(NSString *)emvipek
        emvipekcheckvalue:(NSString *)emvipekcheckvalue
                   pinksn:(NSString *)pinksn
                  pinipek:(NSString *)pinipek
        pinipekcheckValue:(NSString *)pinipekcheckValue){
  [self.pos doUpdateIPEKOperation:groupKey tracksn:trackksn trackipek:trackipek trackipekCheckValue:trackipekCheckValue emvksn:emvksn emvipek:emvipek emvipekcheckvalue:emvipekcheckvalue pinksn:pinksn pinipek:pinipek pinipekcheckValue:pinipekcheckValue block:^(BOOL isSuccess, NSString *stateStr) {
    NSString *result = isSuccess? @"success":@"fail";
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"doUpdateIPEKOperation",@"result":result,@"data":@""}];
  }];
}

RCT_EXPORT_METHOD(updateEMVConfigByXml:(NSString *)xmlStr){
  [self.pos updateEMVConfigByXml:xmlStr];
}

RCT_EXPORT_METHOD(resetPosStatus){
  [self.pos resetPosStatus];
}

RCT_EXPORT_METHOD(cancelTrade:(BOOL)isUserCancel){
  [self.pos cancelTrade:true];
}

-(void)scanBluetooth:(NSInteger)time{
    if (self.bt == nil) {
        self.bt = [BTDeviceFinder new];
    }
    NSInteger delay = 0;
    if(is2ModeBluetooth){
//        NSLog(@"蓝牙状态:%ld",(long)[self.bt getCBCentralManagerState]);
        [self.bt setBluetoothDelegate2Mode:self];
        if ([self.bt getCBCentralManagerState] == CBCentralManagerStateUnknown) {
            while ([self.bt getCBCentralManagerState]!= CBCentralManagerStatePoweredOn) {
//                NSLog(@"Bluetooth state is not power on");
                [self sleepMs:10];
                if(delay++==10){
                    return;
                }
            }
        }else if ([self.bt getCBCentralManagerState] == CBManagerStatePoweredOff){
//            NSLog(@"Bluetooth state is power off");
        }
        [self.bt scanQPos2Mode:time];
    }
}

-(void)onBluetoothName2Mode:(NSString *)bluetoothName{
//    NSLog(@"+++onBluetoothName2Mode %@",bluetoothName);
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onBluetoothName2Mode",@"result":bluetoothName,@"data":@""}];
}

-(void)finishScanQPos2Mode{
    dispatch_async(dispatch_get_main_queue(),  ^{
        [self.bt stopQPos2Mode];
    });
}

-(void)bluetoothIsPowerOff2Mode{
    dispatch_async(dispatch_get_main_queue(),  ^{
//        NSLog(@"+++bluetoothIsPowerOff2Mode");
        //        [bt setBluetoothDelegate2Mode:nil];
        [self.bt stopQPos2Mode];
        //        bt = nil;
    });
}

-(void)bluetoothIsPowerOn2Mode{
    dispatch_async(dispatch_get_main_queue(),  ^{
//        NSLog(@"+++bluetoothIsPowerOn2Mode");
    });
}

//bluetooth connected
-(void) onRequestQposConnected{
//    NSLog(@"onRequestQposConnected");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestQposConnected",@"result":@"",@"data":@""}];
}

//connect bluetooh fail
-(void) onRequestQposDisconnected{
//    NSLog(@"onRequestQposDisconnected");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestQposDisconnected",@"result":@"",@"data":@""}];
}

//No Qpos Detected
-(void) onRequestNoQposDetected{
//    NSLog(@"onRequestNoQposDetected");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestNoQposDetected",@"result":@"",@"data":@""}];
}

// Prompt user to insert/swipe/tap card
-(void) onRequestWaitingUser{
//    NSLog(@"onRequestWaitingUser");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestWaitingUser",@"result":@"",@"data":@""}];
}

//input transaction amount
-(void) onRequestSetAmount{
//    NSLog(@"onRequestSetAmount");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestSetAmount",@"result":@"",@"data":@""}];
}

//callback of input pin on phone
-(void) onRequestPinEntry{
//    NSLog(@"onRequestPinEntry");
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestPinEntry",@"result":@"",@"data":@""}];
}

//return NFC and swipe card data on this function.
-(void) onDoTradeResult: (DoTradeResult)result DecodeData:(NSDictionary*)decodeData{
    if (result == DoTradeResult_NONE) {
//        NSLog(@"No card detected. Please insert or swipe card again and press check card.");
        [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_NONE",@"data":@"No card detected. Please insert or swipe card again and press check card."}];
    }else if (result==DoTradeResult_ICC) {
//        NSLog(@"ICC Card Inserted");
        //Use this API to activate chip card transactions
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_ICC",@"data":@"ICC Card Inserted"}];
    }else if(result==DoTradeResult_NOT_ICC){
//        NSLog(@"Card Inserted (Not ICC)");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_NOT_ICC",@"data":@"Card Inserted (Not ICC)"}];
    }else if(result==DoTradeResult_MCR){
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_MCR",@"data":[self convertToJsonData:decodeData]}];
    }else if(result==DoTradeResult_NFC_OFFLINE || result == DoTradeResult_NFC_ONLINE){
        NSString *str = @"";
        if(result == DoTradeResult_NFC_ONLINE){
            str = @"DoTradeResult_NFC_ONLINE";
        }else if(result == DoTradeResult_NFC_OFFLINE){
            str = @"DoTradeResult_NFC_OFFLINE";
        }
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":str,@"data":[self convertToJsonData:decodeData]}];
    }else if(result==DoTradeResult_NFC_DECLINED){
//        NSLog(@"Tap Card Declined");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_NFC_DECLINED",@"data":@"Tap Card Declined"}];
    }else if (result==DoTradeResult_NO_RESPONSE){
//        NSLog(@"Check card no response");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_NO_RESPONSE",@"data":@"Check card no response"}];
    }else if(result==DoTradeResult_BAD_SWIPE){
//        NSLog(@"Bad Swipe. \nPlease swipe again and press check card.");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_BAD_SWIPE",@"data":@"Bad Swipe.\nPlease swipe again and press check card."}];
    }else if(result==DoTradeResult_NO_UPDATE_WORK_KEY){
//        NSLog(@"device not update work key");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_NO_UPDATE_WORK_KEY",@"data":@"device not update work key"}];
    }else if(result==DoTradeResult_CARD_NOT_SUPPORT){
//        NSLog(@"card not support");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_CARD_NOT_SUPPORT",@"data":@"card not support"}];
    }else if(result==DoTradeResult_PLS_SEE_PHONE){
//        NSLog(@"pls see phone");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_PLS_SEE_PHONE",@"data":@"pls see phone"}];
    }else if(result==DoTradeResult_TRY_ANOTHER_INTERFACE){
//        NSLog(@"pls try another interface");
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":@"DoTradeResult_TRY_ANOTHER_INTERFACE",@"data":@"pls try another interface"}];
    }else{
      [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDoTradeResult",@"result":[NSString stringWithFormat: @"Not Implemented %ld",(long)result],@"data":@""}];
    }
}

//send current transaction time to pos
-(void) onRequestTime{
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestTime",@"result":@"",@"data":@""}];
}

//Prompt message
-(void) onRequestDisplay: (Display)displayMsg{
    NSString *result = @"";
    if (displayMsg==Display_CLEAR_DISPLAY_MSG) {
      result = @"Display_CLEAR_DISPLAY_MSG";
    }else if(displayMsg==Display_PLEASE_WAIT){
      result = @"Display_PLEASE_WAIT";
    }else if(displayMsg==Display_REMOVE_CARD){
      result = @"Display_REMOVE_CARD";
    }else if (displayMsg==Display_TRY_ANOTHER_INTERFACE){
      result = @"Display_TRY_ANOTHER_INTERFACE";
    }else if (displayMsg == Display_TRANSACTION_TERMINATED){
      result = @"Display_TRANSACTION_TERMINATED";
    }else if (displayMsg == Display_PIN_OK){
      result = @"Display_PIN_OK";
    }else if (displayMsg == Display_INPUT_PIN_ING){
      result = @"Display_INPUT_PIN_ING";
    }else if (displayMsg == Display_MAG_TO_ICC_TRADE){
      result = @"Display_MAG_TO_ICC_TRADE";
    }else if (displayMsg == Display_INPUT_OFFLINE_PIN_ONLY){
      result = @"Display_INPUT_OFFLINE_PIN_ONLY";
    }else if(displayMsg == Display_CARD_REMOVED){
      result = @"Display_CARD_REMOVED";
    }else if (displayMsg == Display_INPUT_LAST_OFFLINE_PIN){
      result = @"Display_INPUT_LAST_OFFLINE_PIN";
    }else if (displayMsg == Display_PROCESSING){
      result = @"Display_PROCESSING";
    }else{
      result = [NSString stringWithFormat: @"Not Implemented %ld",(long)displayMsg];
    }
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestDisplay",@"result":result,@"data":@""}];
}

//Multiple AIDS select
-(void) onRequestSelectEmvApp: (NSArray*)appList{
    //NSLog(@"onRequestSelectEmvApp: %@",appList);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestSelectEmvApp",@"result":@"",@"data":[self arrayToJsonString:appList]}];
}

//return chip card tlv data on this function
-(void) onRequestOnlineProcess: (NSString*) tlv{
//    NSLog(@"onRequestOnlineProcess = %@",[[QPOSService sharedInstance] anlysEmvIccData:tlv]);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestOnlineProcess",@"result":@"",@"data":tlv}];
}

// transaction result callback function
-(void) onRequestTransactionResult: (TransactionResult)transactionResult{
  /*
   TransactionResult_APPROVED,
   TransactionResult_TERMINATED,
   TransactionResult_DECLINED,
   TransactionResult_CANCEL,
   TransactionResult_CAPK_FAIL,
   TransactionResult_NOT_ICC,
   TransactionResult_SELECT_APP_FAIL,
   TransactionResult_DEVICE_ERROR,
   TransactionResult_CARD_NOT_SUPPORTED,
   TransactionResult_MISSING_MANDATORY_DATA,
   TransactionResult_CARD_BLOCKED_OR_NO_EMV_APPS,
   TransactionResult_INVALID_ICC_DATA,
   TransactionResult_FALLBACK,
   TransactionResult_NFC_TERMINATED,
   TransactionResult_TRADE_LOG_FULL,
   TransactionResult_CONTACTLESS_TRANSACTION_NOT_ALLOW,
   TransactionResult_CARD_BLOCKED,
   TransactionResult_TOKEN_INVALID,
   TransactionResult_APP_BLOCKED,
   TransactionResult_MULTIPLE_CARDS,
   */
    NSString *messageTextView = @"";
    if (transactionResult==TransactionResult_APPROVED) {
        messageTextView = @"TransactionResult_APPROVED";
    }else if(transactionResult == TransactionResult_TERMINATED) {
        messageTextView = @"TransactionResult_TERMINATED";
    } else if(transactionResult == TransactionResult_DECLINED) {
        messageTextView = @"TransactionResult_DECLINED";
    } else if(transactionResult == TransactionResult_CANCEL) {
        messageTextView = @"TransactionResult_CANCEL";
    } else if(transactionResult == TransactionResult_CAPK_FAIL) {
        messageTextView = @"TransactionResult_CAPK_FAIL";
    } else if(transactionResult == TransactionResult_NOT_ICC) {
        messageTextView = @"TransactionResult_NOT_ICC";
    } else if(transactionResult == TransactionResult_SELECT_APP_FAIL) {
        messageTextView = @"TransactionResult_SELECT_APP_FAIL";
    } else if(transactionResult == TransactionResult_DEVICE_ERROR) {
        messageTextView = @"TransactionResult_DEVICE_ERROR";
    } else if(transactionResult == TransactionResult_CARD_NOT_SUPPORTED) {
        messageTextView = @"TransactionResult_CARD_NOT_SUPPORTED";
    } else if(transactionResult == TransactionResult_MISSING_MANDATORY_DATA) {
        messageTextView = @"TransactionResult_MISSING_MANDATORY_DATA";
    } else if(transactionResult == TransactionResult_CARD_BLOCKED_OR_NO_EMV_APPS) {
        messageTextView = @"TransactionResult_CARD_BLOCKED_OR_NO_EMV_APPS";
    } else if(transactionResult == TransactionResult_INVALID_ICC_DATA) {
        messageTextView = @"TransactionResult_INVALID_ICC_DATA";
    } else if(transactionResult == TransactionResult_FALLBACK) {
      messageTextView = @"TransactionResult_FALLBACK";
    } else if(transactionResult == TransactionResult_NFC_TERMINATED) {
        messageTextView = @"TransactionResult_NFC_TERMINATED";
    } else if(transactionResult == TransactionResult_TRADE_LOG_FULL) {
      messageTextView = @"TransactionResult_TRADE_LOG_FULL";
    } else if(transactionResult == TransactionResult_CONTACTLESS_TRANSACTION_NOT_ALLOW) {
        messageTextView = @"TransactionResult_CONTACTLESS_TRANSACTION_NOT_ALLOW";
    } else if(transactionResult == TransactionResult_CARD_BLOCKED) {
        messageTextView = @"TransactionResult_CARD_BLOCKED";
    } else if(transactionResult == TransactionResult_TOKEN_INVALID) {
        messageTextView = @"TransactionResult_TOKEN_INVALID";
    } else if(transactionResult == TransactionResult_APP_BLOCKED) {
        messageTextView = @"TransactionResult_APP_BLOCKED";
    } else if(transactionResult == TransactionResult_MULTIPLE_CARDS) {
        messageTextView = @"TransactionResult_MULTIPLE_CARDS";
    }else{
      messageTextView = [NSString stringWithFormat: @"Not Implemented %ld",(long)transactionResult];
    }
//    NSLog(@"onRequestTransactionResult: %@",messageTextView);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestTransactionResult",@"result":messageTextView,@"data":@""}];
}

//return transaction batch data
-(void) onRequestBatchData: (NSString*)tlv{
//    tlv = [@"batch data:\n" stringByAppendingString:tlv];
//    NSLog(@"onBatchData %@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestBatchData",@"result":@"",@"data":tlv}];
}

//return transaction reversal data
-(void) onReturnReversalData: (NSString*)tlv{
//    tlv = [@"reversal data:\n" stringByAppendingString:tlv];
//    NSLog(@"onReversalData %@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onReturnReversalData",@"result":@"",@"data":tlv}];
}

-(void) onEmvICCExceptionData: (NSString*)tlv{
//    NSLog(@"onEmvICCExceptionData:%@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onEmvICCExceptionData",@"result":@"",@"data":tlv}];
}

//Prompt error message in this function
-(void) onDHError: (DHError)errorState{
    NSString *msg = @"";
    if(errorState ==DHError_TIMEOUT) {
        msg = @"DHError_TIMEOUT";
    } else if(errorState == DHError_DEVICE_RESET) {
        msg = @"DHError_DEVICE_RESET";
    } else if(errorState == DHError_UNKNOWN) {
        msg = @"DHError_UNKNOWN";
    } else if(errorState == DHError_DEVICE_BUSY) {
        msg = @"DHError_DEVICE_BUSY";
    } else if(errorState == DHError_INPUT_OUT_OF_RANGE) {
        msg = @"DHError_INPUT_OUT_OF_RANGE";
    } else if(errorState == DHError_INPUT_INVALID_FORMAT) {
        msg = @"DHError_INPUT_INVALID_FORMAT";
    } else if(errorState == DHError_INPUT_ZERO_VALUES) {
        msg = @"DHError_INPUT_ZERO_VALUES";
    } else if(errorState == DHError_INPUT_INVALID) {
        msg = @"DHError_INPUT_INVALID";
    } else if(errorState == DHError_CASHBACK_NOT_SUPPORTED) {
        msg = @"DHError_CASHBACK_NOT_SUPPORTED";
    } else if(errorState == DHError_CRC_ERROR) {
        msg = @"DHError_CRC_ERROR";
    } else if(errorState == DHError_COMM_ERROR) {
        msg = @"DHError_COMM_ERROR";
    }else if(errorState == DHError_MAC_ERROR){
        msg = @"DHError_MAC_ERROR";
    }else if(errorState == DHError_CMD_TIMEOUT){
        msg = @"DHError_CMD_TIMEOUT";
    }else if(errorState == DHError_AMOUNT_OUT_OF_LIMIT){
        msg = @"DHError_AMOUNT_OUT_OF_LIMIT";
    }else if(errorState == DHError_CMD_NOT_AVAILABLE){
        msg = @"DHError_CMD_NOT_AVAILABLE";
    }else{
        msg = [NSString stringWithFormat: @"Not Implemented %ld",(long)errorState];
    }
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onDHError",@"result":msg,@"data":@""}];
}

-(void) onQposIdResult: (NSDictionary*)posId{
    [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onQposIdResult",@"result":@"",@"data":[self convertToJsonData:posId]}];
}

-(void) onQposInfoResult: (NSDictionary*)posInfoData{
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onQposInfoResult",@"result":@"",@"data":[self convertToJsonData:posInfoData]}];
}

-(void)onReturnSetMasterKeyResult: (BOOL)isSuccess{
  NSString *result = isSuccess? @"success":@"fail";
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onReturnSetMasterKeyResult",@"result":result,@"data":@""}];
}

-(void)onRequestUpdateWorkKeyResult:(UpdateInformationResult)updateInformationResult{
    NSString *updateResult = @"";
    if (updateInformationResult==UpdateInformationResult_UPDATE_SUCCESS) {
        updateResult = @"UpdateInformationResult_UPDATE_SUCCESS";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_FAIL){
        updateResult = @"UpdateInformationResult_UPDATE_FAIL";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_PACKET_LEN_ERROR){
        updateResult = @"UpdateInformationResult_UPDATE_PACKET_LEN_ERROR";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_PACKET_VEFIRY_ERROR){
        updateResult = @"UpdateInformationResult_UPDATE_PACKET_VEFIRY_ERROR";
    }
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onRequestUpdateWorkKeyResult",@"result":updateResult,@"data":@""}];
}

- (void)onTradeCancelled{
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onTradeCancelled",@"result":@"success",@"data":@""}];
}


// callback function of updateEmvConfig and updateEMVConfigByXml api.
-(void)onReturnCustomConfigResult:(BOOL)isSuccess config:(NSString*)resutl{
  NSString *result = isSuccess? @"success":@"fail";
  [self sendEventWithName:@"NativePosReminder" body:@{@"method":@"onReturnCustomConfigResult",@"result":result,@"data":resutl}];
}

- (NSString *)convertToJsonData:(NSDictionary *)dict{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    NSString *jsonString;

    if (!jsonData) {
    } else {
        jsonString = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    NSMutableString *mutStr = [NSMutableString stringWithString:jsonString];
    NSRange range = {0,jsonString.length};
    //去掉字符串中的空格
    [mutStr replaceOccurrencesOfString:@" " withString:@"" options:NSLiteralSearch range:range];
    NSRange range2 = {0,mutStr.length};
    //去掉字符串中的换行符
    [mutStr replaceOccurrencesOfString:@"\n" withString:@"" options:NSLiteralSearch range:range2];
    return mutStr;
}

- (NSString *)arrayToJsonString:(NSArray *)array{
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:array options:NSJSONWritingPrettyPrinted error:&error];
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];;
}

-(void)sleepMs: (NSInteger)msec {
    NSTimeInterval sec = (msec / 1000.0f);
    [NSThread sleepForTimeInterval:sec];
}

@end
