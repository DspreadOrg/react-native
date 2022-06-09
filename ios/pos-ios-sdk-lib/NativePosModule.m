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

RCT_EXPORT_METHOD(setCardTradeMode:(CardTradeMode)aCardTMode){
  [self.pos setCardTradeMode:aCardTMode];
}

RCT_EXPORT_METHOD(doTrade){
  [self.pos doTrade];
}

RCT_EXPORT_METHOD(doTrade:(NSInteger)keyIndex delays:(NSInteger)timeout){
  [self.pos doTrade:keyIndex delays:timeout];
}

RCT_EXPORT_METHOD(doCheckCard:(NSInteger) timeout keyIndex:(NSInteger) mKeyIndex){
  [self.pos doCheckCard:timeout keyIndex:mKeyIndex];
}

RCT_EXPORT_METHOD(doEmvApp:(EmvOption)aemvOption){
  [self.pos doEmvApp:aemvOption];
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
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"doUpdateIPEKOperation",@"result":@(isSuccess)}];
  }];
}

RCT_EXPORT_METHOD(updateEMVConfigByXml:(NSString *)xmlStr){
  [self.pos updateEMVConfigByXml:xmlStr];
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
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onBluetoothName2Mode",@"result":bluetoothName}];
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
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestQposConnected",@"result":@""}];
}

//connect bluetooh fail
-(void) onRequestQposDisconnected{
//    NSLog(@"onRequestQposDisconnected");
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestQposDisconnected",@"result":@""}];
}

//No Qpos Detected
-(void) onRequestNoQposDetected{
//    NSLog(@"onRequestNoQposDetected");
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestNoQposDetected",@"result":@""}];
}

// Prompt user to insert/swipe/tap card
-(void) onRequestWaitingUser{
//    NSLog(@"onRequestWaitingUser");
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestWaitingUser",@"result":@""}];
}

//input transaction amount
-(void) onRequestSetAmount{
//    NSLog(@"onRequestSetAmount");
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestSetAmount",@"result":@""}];
}

//callback of input pin on phone
-(void) onRequestPinEntry{
//    NSLog(@"onRequestPinEntry");
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestPinEntry",@"result":@""}];
}

//return NFC and swipe card data on this function.
-(void) onDoTradeResult: (DoTradeResult)result DecodeData:(NSDictionary*)decodeData{
    if (result == DoTradeResult_NONE) {
//        NSLog(@"No card detected. Please insert or swipe card again and press check card.");
        [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"No card detected. Please insert or swipe card again and press check card."}];
    }else if (result==DoTradeResult_ICC) {
//        NSLog(@"ICC Card Inserted");
        //Use this API to activate chip card transactions
        [self.pos doEmvApp:EmvOption_START];
    }else if(result==DoTradeResult_NOT_ICC){
//        NSLog(@"Card Inserted (Not ICC)");
        [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"Card Inserted (Not ICC)"}];
    }else if(result==DoTradeResult_MCR){
        NSString *formatID = [NSString stringWithFormat:@"Format ID: %@\n",decodeData[@"formatID"]] ;
        NSString *maskedPAN = [NSString stringWithFormat:@"Masked PAN: %@\n",decodeData[@"maskedPAN"]];
        NSString *expiryDate = [NSString stringWithFormat:@"Expiry Date: %@\n",decodeData[@"expiryDate"]];
        NSString *cardHolderName = [NSString stringWithFormat:@"Cardholder Name: %@\n",decodeData[@"cardholderName"]];
        NSString *serviceCode = [NSString stringWithFormat:@"Service Code: %@\n",decodeData[@"serviceCode"]];
        NSString *encTrack1 = [NSString stringWithFormat:@"Encrypted Track 1: %@\n",decodeData[@"encTrack1"]];
        NSString *encTrack2 = [NSString stringWithFormat:@"Encrypted Track 2: %@\n",decodeData[@"encTrack2"]];
        NSString *encTrack3 = [NSString stringWithFormat:@"Encrypted Track 3: %@\n",decodeData[@"encTrack3"]];
        NSString *pinKsn = [NSString stringWithFormat:@"PIN KSN: %@\n",decodeData[@"pinKsn"]];
        NSString *trackksn = [NSString stringWithFormat:@"Track KSN: %@\n",decodeData[@"trackksn"]];
        NSString *pinBlock = [NSString stringWithFormat:@"pinBlock: %@\n",decodeData[@"pinblock"]];
        NSString *encPAN = [NSString stringWithFormat:@"encPAN: %@\n",decodeData[@"encPAN"]];
        NSString *msg = [NSString stringWithFormat:@"Card Swiped:\n"];
        msg = [msg stringByAppendingString:formatID];
        msg = [msg stringByAppendingString:maskedPAN];
        msg = [msg stringByAppendingString:expiryDate];
        msg = [msg stringByAppendingString:cardHolderName];
        msg = [msg stringByAppendingString:pinKsn];
        msg = [msg stringByAppendingString:trackksn];
        msg = [msg stringByAppendingString:serviceCode];
        msg = [msg stringByAppendingString:encTrack1];
        msg = [msg stringByAppendingString:encTrack2];
        msg = [msg stringByAppendingString:encTrack3];
        msg = [msg stringByAppendingString:pinBlock];
        msg = [msg stringByAppendingString:encPAN];
        [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":[NSString stringWithFormat:@"MSR||%@",msg]}];
//        NSString *a = [QPOSQPOSUtil byteArray2Hex:[QPOSQPOSUtil stringFormatTAscii:maskedPAN]];
//        [self.pos getPin:1 keyIndex:0 maxLen:6 typeFace:@"Pls Input Pin" cardNo:maskedPAN data:@"" delay:30 withResultBlock:^(BOOL isSuccess, NSDictionary *result) {
//            NSLog(@"result: %@",result);
//            self.textViewLog.backgroundColor = [UIColor greenColor];
//            [self playAudio];
//            AudioServicesPlaySystemSound (kSystemSoundID_Vibrate);
//            self.textViewLog.text = msg;
//            self.lableAmount.text = @"";
//        }];
//
//        self.textViewLog.backgroundColor = [UIColor greenColor];
//        [self playAudio];
//        AudioServicesPlaySystemSound (kSystemSoundID_Vibrate);
//        self.textViewLog.text = msg;
//        self.lableAmount.text = @"";
    }else if(result==DoTradeResult_NFC_OFFLINE || result == DoTradeResult_NFC_ONLINE){
        NSString *formatID = [NSString stringWithFormat:@"Format ID: %@\n",decodeData[@"formatID"]] ;
        NSString *maskedPAN = [NSString stringWithFormat:@"Masked PAN: %@\n",decodeData[@"maskedPAN"]];
        NSString *expiryDate = [NSString stringWithFormat:@"Expiry Date: %@\n",decodeData[@"expiryDate"]];
        NSString *cardHolderName = [NSString stringWithFormat:@"Cardholder Name: %@\n",decodeData[@"cardholderName"]];
        NSString *serviceCode = [NSString stringWithFormat:@"Service Code: %@\n",decodeData[@"serviceCode"]];
        NSString *encTrack1 = [NSString stringWithFormat:@"Encrypted Track 1: %@\n",decodeData[@"encTrack1"]];
        NSString *encTrack2 = [NSString stringWithFormat:@"Encrypted Track 2: %@\n",decodeData[@"encTrack2"]];
        NSString *encTrack3 = [NSString stringWithFormat:@"Encrypted Track 3: %@\n",decodeData[@"encTrack3"]];
        NSString *pinKsn = [NSString stringWithFormat:@"PIN KSN: %@\n",decodeData[@"pinKsn"]];
        NSString *trackksn = [NSString stringWithFormat:@"Track KSN: %@\n",decodeData[@"trackksn"]];
        NSString *pinBlock = [NSString stringWithFormat:@"pinBlock: %@\n",decodeData[@"pinblock"]];
        NSString *encPAN = [NSString stringWithFormat:@"encPAN: %@\n",decodeData[@"encPAN"]];
        NSString *msg = [NSString stringWithFormat:@"Tap Card:\n"];
        msg = [msg stringByAppendingString:formatID];
        msg = [msg stringByAppendingString:maskedPAN];
        msg = [msg stringByAppendingString:expiryDate];
        msg = [msg stringByAppendingString:cardHolderName];
        msg = [msg stringByAppendingString:pinKsn];
        msg = [msg stringByAppendingString:trackksn];
        msg = [msg stringByAppendingString:serviceCode];
        msg = [msg stringByAppendingString:encTrack1];
        msg = [msg stringByAppendingString:encTrack2];
        msg = [msg stringByAppendingString:encTrack3];
        msg = [msg stringByAppendingString:pinBlock];
        msg = [msg stringByAppendingString:encPAN];
        NSString *str = @"";
        if(result == DoTradeResult_NFC_ONLINE){
            str = @"NFC_ONLINE";
        }else if(result == DoTradeResult_NFC_OFFLINE){
            str = @"NFC_OFFLINE";
        }
        [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":[NSString stringWithFormat:@"%@||%@",str,msg]}];
//        dispatch_async(dispatch_get_main_queue(),  ^{
//            NSDictionary *mDic = [self.pos getNFCBatchData];
//            NSString *tlv;
//            if(mDic !=nil){
//                tlv= [NSString stringWithFormat:@"NFCBatchData: %@\n",mDic[@"tlv"]];
//                NSLog(@"--------nfc:tlv%@",tlv);
//            }else{
//                tlv = @"";
//            }
//        });
    }else if(result==DoTradeResult_NFC_DECLINED){
//        NSLog(@"Tap Card Declined");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"Tap Card Declined"}];
    }else if (result==DoTradeResult_NO_RESPONSE){
//        NSLog(@"Check card no response");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"Check card no response"}];
    }else if(result==DoTradeResult_BAD_SWIPE){
//        NSLog(@"Bad Swipe. \nPlease swipe again and press check card.");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"Bad Swipe. \nPlease swipe again and press check card."}];
    }else if(result==DoTradeResult_NO_UPDATE_WORK_KEY){
//        NSLog(@"device not update work key");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"device not update work key"}];
    }else if(result==DoTradeResult_CARD_NOT_SUPPORT){
//        NSLog(@"card not support");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"card not support"}];
    }else if(result==DoTradeResult_PLS_SEE_PHONE){
//        NSLog(@"pls see phone");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"pls see phone"}];
    }else if(result==DoTradeResult_TRY_ANOTHER_INTERFACE){
//        NSLog(@"pls try another interface");
      [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDoTradeResult",@"result":@"pls try another interface"}];
    }
}

//send current transaction time to pos
-(void) onRequestTime{
    NSString *formatStringForHours = [NSDateFormatter dateFormatFromTemplate:@"j" options:0 locale:[NSLocale currentLocale]];
    NSRange containA = [formatStringForHours rangeOfString:@"a"];
    BOOL hasAMPM = containA.location != NSNotFound;
    NSString *terminalTime = @"";
//    when phone time is 12h format, need add this judgement.
    if (hasAMPM) {
        NSDateFormatter *dateFormatter = [NSDateFormatter new];
        [dateFormatter setDateFormat:@"yyyyMMddhhmmss"];
        terminalTime = [dateFormatter stringFromDate:[NSDate date]];
    }else{
        NSDateFormatter *dateFormatter = [NSDateFormatter new];
        [dateFormatter setDateFormat:@"yyyyMMddHHmmss"];
        terminalTime = [dateFormatter stringFromDate:[NSDate date]];
    }
    [self.pos sendTime:terminalTime];
}

//Prompt message
-(void) onRequestDisplay: (Display)displayMsg{
    NSString *msg = @"";
    if (displayMsg==Display_CLEAR_DISPLAY_MSG) {
        msg = @"";
    }else if(displayMsg==Display_PLEASE_WAIT){
        msg = @"Please wait...";
    }else if(displayMsg==Display_REMOVE_CARD){
        msg = @"Please remove card";
    }else if (displayMsg==Display_TRY_ANOTHER_INTERFACE){
        msg = @"Please try another interface";
    }else if (displayMsg == Display_TRANSACTION_TERMINATED){
        msg = @"Terminated";
    }else if (displayMsg == Display_PIN_OK){
        msg = @"Pin ok";
    }else if (displayMsg == Display_INPUT_PIN_ING){
        msg = @"please input pin on pos";
    }else if (displayMsg == Display_MAG_TO_ICC_TRADE){
        msg = @"please insert chip card on pos";
    }else if (displayMsg == Display_INPUT_OFFLINE_PIN_ONLY){
        msg = @"please input offline pin only";
    }else if(displayMsg == Display_CARD_REMOVED){
        msg = @"Card Removed";
    }else if (displayMsg == Display_INPUT_LAST_OFFLINE_PIN){
        msg = @"please input last offline pin";
    }else if (displayMsg == Display_PROCESSING){
        msg = @"processing";
    }
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestDisplay",@"result":msg}];
//    NSLog(@"onRequestDisplay: %@",msg);
}

//Multiple AIDS select
-(void) onRequestSelectEmvApp: (NSArray*)appList{
    //NSLog(@"onRequestSelectEmvApp: %@",appList);
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestSelectEmvApp",@"result":@""}];
}

//return chip card tlv data on this function
-(void) onRequestOnlineProcess: (NSString*) tlv{
//    NSLog(@"onRequestOnlineProcess = %@",[[QPOSService sharedInstance] anlysEmvIccData:tlv]);
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestOnlineProcess",@"result":tlv}];
}

// transaction result callback function
-(void) onRequestTransactionResult: (TransactionResult)transactionResult{
    NSString *messageTextView = @"";
    if (transactionResult==TransactionResult_APPROVED) {
        messageTextView = @"Approved";
    }else if(transactionResult == TransactionResult_TERMINATED) {
        messageTextView = @"Terminated";
    } else if(transactionResult == TransactionResult_DECLINED) {
        messageTextView = @"Declined";
    } else if(transactionResult == TransactionResult_CANCEL) {
        messageTextView = @"Cancel";
    } else if(transactionResult == TransactionResult_CAPK_FAIL) {
        messageTextView = @"Fail (CAPK fail)";
    } else if(transactionResult == TransactionResult_NOT_ICC) {
        messageTextView = @"Fail (Not ICC card)";
    } else if(transactionResult == TransactionResult_SELECT_APP_FAIL) {
        messageTextView = @"Fail (App fail)";
    } else if(transactionResult == TransactionResult_DEVICE_ERROR) {
        messageTextView = @"Pos Error";
    } else if(transactionResult == TransactionResult_CARD_NOT_SUPPORTED) {
        messageTextView = @"Card not support";
    } else if(transactionResult == TransactionResult_MISSING_MANDATORY_DATA) {
        messageTextView = @"Missing mandatory data";
    } else if(transactionResult == TransactionResult_CARD_BLOCKED_OR_NO_EMV_APPS) {
        messageTextView = @"Card blocked or no EMV apps";
    } else if(transactionResult == TransactionResult_INVALID_ICC_DATA) {
        messageTextView = @"Invalid ICC data";
    }else if(transactionResult == TransactionResult_NFC_TERMINATED) {
        messageTextView = @"NFC Terminated";
    }else if(transactionResult == TransactionResult_CONTACTLESS_TRANSACTION_NOT_ALLOW) {
        messageTextView = @"TRANS NOT ALLOW";
    }else if(transactionResult == TransactionResult_CARD_BLOCKED) {
        messageTextView = @"Card Blocked";
    }else if(transactionResult == TransactionResult_TOKEN_INVALID) {
        messageTextView = @"Token Invalid";
    }else if(transactionResult == TransactionResult_APP_BLOCKED) {
        messageTextView = @"APP Blocked";
    }else if(transactionResult == TransactionResult_MULTIPLE_CARDS) {
        messageTextView = @"Multiple Cards";
    }
//    NSLog(@"onRequestTransactionResult: %@",messageTextView);
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestTransactionResult",@"result":messageTextView}];
}

//return transaction batch data
-(void) onRequestBatchData: (NSString*)tlv{
//    tlv = [@"batch data:\n" stringByAppendingString:tlv];
//    NSLog(@"onBatchData %@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestBatchData",@"result":tlv}];
}

//return transaction reversal data
-(void) onReturnReversalData: (NSString*)tlv{
//    tlv = [@"reversal data:\n" stringByAppendingString:tlv];
//    NSLog(@"onReversalData %@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onReturnReversalData",@"result":tlv}];
}

-(void) onEmvICCExceptionData: (NSString*)tlv{
//    NSLog(@"onEmvICCExceptionData:%@",tlv);
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onEmvICCExceptionData",@"result":tlv}];
}

//Prompt error message in this function
-(void) onDHError: (DHError)errorState{
    NSString *msg = @"";
    if(errorState ==DHError_TIMEOUT) {
        msg = @"Pos no response";
    } else if(errorState == DHError_DEVICE_RESET) {
        msg = @"Pos reset";
    } else if(errorState == DHError_UNKNOWN) {
        msg = @"Unknown error";
    } else if(errorState == DHError_DEVICE_BUSY) {
        msg = @"Pos Busy";
    } else if(errorState == DHError_INPUT_OUT_OF_RANGE) {
        msg = @"Input out of range.";
    } else if(errorState == DHError_INPUT_INVALID_FORMAT) {
        msg = @"Input invalid format.";
    } else if(errorState == DHError_INPUT_ZERO_VALUES) {
        msg = @"Input are zero values.";
    } else if(errorState == DHError_INPUT_INVALID) {
        msg = @"Input invalid.";
    } else if(errorState == DHError_CASHBACK_NOT_SUPPORTED) {
        msg = @"Cashback not supported.";
    } else if(errorState == DHError_CRC_ERROR) {
        msg = @"CRC Error.";
    } else if(errorState == DHError_COMM_ERROR) {
        msg = @"Communication Error.";
    }else if(errorState == DHError_MAC_ERROR){
        msg = @"MAC Error.";
    }else if(errorState == DHError_CMD_TIMEOUT){
        msg = @"CMD Timeout.";
    }else if(errorState == DHError_AMOUNT_OUT_OF_LIMIT){
        msg = @"Amount out of limit.";
    }else if(errorState == DHError_CMD_NOT_AVAILABLE){
        msg = @"command not available";
    }
//    NSLog(@"onError = %@",msg);
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onDHError",@"result":msg}];
}

-(void) onQposIdResult: (NSDictionary*)posId{
    NSString *aStr = [@"posId:" stringByAppendingString:posId[@"posId"]];

    NSString *temp = [@"psamId:" stringByAppendingString:posId[@"psamId"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:temp];
    
    temp = [@"merchantId:" stringByAppendingString:posId[@"merchantId"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:temp];
    
    temp = [@"vendorCode:" stringByAppendingString:posId[@"vendorCode"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:temp];
    
    temp = [@"deviceNumber:" stringByAppendingString:posId[@"deviceNumber"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:temp];
    
    temp = [@"psamNo:" stringByAppendingString:posId[@"psamNo"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:temp];
    
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onQposIdResult",@"result":aStr}];
}

-(void) onQposInfoResult: (NSDictionary*)posInfoData{
    NSString *aStr = @"ModelInfo: ";
    aStr = [aStr stringByAppendingString:posInfoData[@"ModelInfo"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"PCIHardwareVersion: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"PCIHardwareVersion"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"SUB: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"SUB"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"bootloaderVersion: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"bootloaderVersion"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Firmware Version: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"firmwareVersion"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Hardware Version: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"hardwareVersion"]];
    
    NSString *batteryPercentage = posInfoData[@"batteryPercentage"];
    if (batteryPercentage==nil || [@"" isEqualToString:batteryPercentage]) {
        aStr = [aStr stringByAppendingString:@"\n"];
        aStr = [aStr stringByAppendingString:@"Battery Level: "];
        aStr = [aStr stringByAppendingString:posInfoData[@"batteryLevel"]];
    }else{
        aStr = [aStr stringByAppendingString:@"\n"];
        aStr = [aStr stringByAppendingString:@"Battery Percentage: "];
        aStr = [aStr stringByAppendingString:posInfoData[@"batteryPercentage"]];
    }
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Charge: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"isCharging"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"USB: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"isUsbConnected"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Track 1 Supported: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"isSupportedTrack1"]];
    
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Track 2 Supported: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"isSupportedTrack2"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"Track 3 Supported: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"isSupportedTrack3"]];
    aStr = [aStr stringByAppendingString:@"\n"];
    aStr = [aStr stringByAppendingString:@"updateWorkKeyFlag: "];
    aStr = [aStr stringByAppendingString:posInfoData[@"updateWorkKeyFlag"]];
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onQposInfoResult",@"result":aStr}];
}

-(void)onReturnSetMasterKeyResult: (BOOL)isSuccess{
   [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onReturnSetMasterKeyResult",@"result":@(isSuccess)}];
}

-(void)onRequestUpdateWorkKeyResult:(UpdateInformationResult)updateInformationResult{
    NSString *updateResult = @"";
    if (updateInformationResult==UpdateInformationResult_UPDATE_SUCCESS) {
        updateResult = @"Success";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_FAIL){
        updateResult = @"Failed";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_PACKET_LEN_ERROR){
        updateResult = @"Packet len error";
    }else if(updateInformationResult==UpdateInformationResult_UPDATE_PACKET_VEFIRY_ERROR){
        updateResult = @"Packer vefiry error";
    }
    [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onRequestUpdateWorkKeyResult",@"result":updateResult}];
}

// callback function of updateEmvConfig and updateEMVConfigByXml api.
-(void)onReturnCustomConfigResult:(BOOL)isSuccess config:(NSString*)resutl{
  [self sendEventWithName:@"NativePosReminder" body:@{@"key":@"onReturnCustomConfigResult",@"result":@(isSuccess)}];
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

-(void)sleepMs: (NSInteger)msec {
    NSTimeInterval sec = (msec / 1000.0f);
    [NSThread sleepForTimeInterval:sec];
}

@end
