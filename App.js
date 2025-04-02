import React, { Component } from 'react';
import { Text, TextInput, View, NativeEventEmitter,NativeModules,Button, StyleSheet,DeviceEventEmitter,FlatList, TouchableOpacity, string, ScrollView, Title, Alert, prompt} from 'react-native';
var pos = NativeModules.NativePosModule;
import { Colors } from 'react-native/Libraries/NewAppScreen';
const NativePosEmitter = new NativeEventEmitter(pos);

/**
     * Transaction type.
     */
 const TransactionType = {};
    TransactionType.GOODS = "GOODS"; // 货物 GOODS
    TransactionType.SERVICES = "SERVICES"; // 服务 service
    TransactionType.CASH = "CASH"; // 现金 cash
    TransactionType.CASHBACK = "CASHBACK"; //  返现
    TransactionType.INQUIRY = "INQUIRY"; // 查询
    TransactionType.TRANSFER = "TRANSFER"; // 转账
    TransactionType.ADMIN = "ADMIN"; // 管理
    TransactionType.CASHDEPOSIT = "CASHDEPOSIT"; // 存款
    TransactionType.PAYMENT = "PAYMENT"; // 付款 支付

    TransactionType.PBOCLOG = "PBOCLOG"; // 0x0A /*PBOC日志(电子现金日志)*/
    TransactionType.SALE = "SALE"; // 0x0B /*消费*/
    TransactionType.PREAUTH = "PREAUTH"; // 0x0C /*预授权*/

    TransactionType.ECQ_DESIGNATED_LOAD = "ECQ_DESIGNATED_LOAD"; // 0x10 /*电子现金Q指定账户圈存*/
    TransactionType.ECQ_UNDESIGNATED_LOAD = "ECQ_UNDESIGNATED_LOAD"; // 0x11 /*电子现金费非指定账户圈存*/
    TransactionType.ECQ_CASH_LOAD = "ECQ_UNDESIGNATED_LOAD"; // 0x12 /*电子现金费现金圈存*/
    TransactionType.ECQ_CASH_LOAD_VOID = "ECQ_CASH_LOAD_VOID"; // 0x13 /*电子现金圈存撤销*/
    TransactionType.ECQ_INQUIRE_LOG = "ECQ_INQUIRE_LOG"; // 0x0A /*电子现金日志(和PBOC日志一样)*/
    TransactionType.REFUND = "REFUND";//退款
    TransactionType.UPDATE_PIN = "UPDATE_PIN";    //修改密码
    TransactionType.SALES_NEW = "SALES_NEW";
    TransactionType.NON_LEGACY_MONEY_ADD = "NON_LEGACY_MONEY_ADD"; /* 0x17*/
    TransactionType.LEGACY_MONEY_ADD = "LEGACY_MONEY_ADD";  /*0x16*/
    TransactionType.BALANCE_UPDATE = "BALANCE_UPDATE"; /*0x18*/

const communicationMode = [
  'BLUETOOTH',
  'BLUETOOTH_BLE',
  'UART',
  'USB_OTG_CDC_ACM',
  'AUDIO',
   ];

const EMVOperation = {
    EMVOperation_clear : 0,
    EMVOperation_add : 1,
    EMVOperation_delete : 2,
    EMVOperation_getList : 3,
    EMVOperation_update : 4,
    EMVOperation_quickemv : 5,
};   

const EmvOption = {
    EmvOption_START : 0,
    EmvOption_START_WITH_FORCE_ONLINE : 1,
    EmvOption_START_WITH_FORCE_PIN : 2,
    EmvOption_START_WITH_FORCE_ONLINE_FORCE_PIN : 3,
}

const EncryptType = {
  EncryptType_plaintext : 0,
  EncryptType_encrypted : 1,
}

const CHECKVALUE_KEYTYPE = {
   MKSK_TMK : 0,
   MKSK_PIK : 1,
   MKSK_TDK : 2,
   MKSK_MCK : 3,
   TCK : 4,
   MAGK : 5,
   DUKPT_TRK_IPEK : 6,
   DUKPT_EMV_IPEK : 7,
   DUKPT_PIN_IPEK : 8,
   DUKPT_TRK_KSN : 9,
   DUKPT_EMV_KSN : 10,
   DUKPT_PIN_KSN : 11,
   DUKPT_MKSK_ALLTYPE : 12,
}

const CardTradeMode = {
  CardTradeMode_ONLY_INSERT_CARD : 0,
  CardTradeMode_ONLY_SWIPE_CARD : 1,
  CardTradeMode_TAP_INSERT_CARD : 2,
  CardTradeMode_TAP_INSERT_CARD_NOTUP : 3,
  CardTradeMode_SWIPE_TAP_INSERT_CARD : 4,
  CardTradeMode_UNALLOWED_LOW_TRADE : 5,
  CardTradeMode_SWIPE_INSERT_CARD : 6,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE : 7,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP_UNALLOWED_LOW_TRADE : 8,
  CardTradeMode_ONLY_TAP_CARD : 9,
  CardTradeMode_ONLY_TAP_CARD_QF : 10,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP : 11,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_DOWN : 12,
  CardTradeMode_SWIPE_INSERT_CARD_UNALLOWED_LOW_TRADE : 13,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE_NEW : 14,
  CardTradeMode_ONLY_INSERT_CARD_NOPIN : 15,
  CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP_DELAY : 16,
}

export default class catComponent extends Component {
 
  render() {
      return (
            <View style = {this.styles.container}>
            <FlatList
            ListHeaderComponent={
                <View>
                    <TouchableOpacity onPress={this.scanBluetooth.bind(this)} style = {this.styles.button}>
                      <Text style={this.styles.text}>Scan Bluetooth</Text>
                    </TouchableOpacity>  
                    <TouchableOpacity onPress={this.doTrade} style = {this.styles.button}>
                      <Text style={this.styles.text}>doTrade</Text>
                    </TouchableOpacity>  
                    <TouchableOpacity onPress={this.disconnect} style = {this.styles.button}>
                      <Text style={this.styles.text}>disconnect</Text>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.getQposId} style = {this.styles.button}>
                      <Text style={this.styles.text}>getQposId</Text>
                    </TouchableOpacity> 
                    <TouchableOpacity onPress={this.getQposInfo} style = {this.styles.button}>
                      <Text style={this.styles.text}>getQposInfo</Text>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.resetPosStatus} style = {this.styles.button}>
                      <Text style={this.styles.text}>resetPosStatus</Text>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={this.updateEMVConfigByXML} style = {this.styles.button}>
                      <Text style={this.styles.text}>updateEMVConfigByXML</Text>
                    </TouchableOpacity>
                    <Text style = {this.styles.textStyle}>Bluetooth Name</Text>        
                 </View>
                }
                
                data={this.state.bluetoothName}
                renderItem={({item}) => (<TouchableOpacity onPress={() => this._onPressItem(item)}>
                        <Text style = {this.styles.textStyle}>{item.key}</Text>
                </TouchableOpacity>)} 
                
                ListFooterComponent = {
                  <View>
                     <Text style = {this.styles.textStyle}>{this.state.transactionData}</Text>
                  </View>
                }
                />
            </View>
      )
  }

  constructor() {
    super()
    this.state = ({
        bluetoothName: [],
        transactionData: "",
    });
 }
  
  componentDidMount(){
    //组件加载完整
    NativePosEmitter.addListener('NativePosReminder',this.onScanningResult.bind(this));
  }

  componentWillUnmount(){
    NativePosEmitter.removeListener('NativePosReminder',this.onScanningResult);//移除扫描监听
    this.setState = ({
      bluetoothName: [],
      transactionData: "",
     });
  }

  onScanningResult(msg){  
    var message = "method: " + msg.method + " result: " + msg.result + " data: " + msg.data;
    console.log("js",message);

    if(msg.method == "onBluetoothName2Mode"){
      blueName = msg.result;
      var pages = this.state.bluetoothName;
      pages.push(
          {key : blueName}
      );
     
      this.setState({
           bluetoothName : pages
      });
    }else if(msg.method == "onRequestQposConnected"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onRequestQposDisconnected"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onRequestNoQposDetected"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onQposIdResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onQposInfoResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onRequestWaitingUser"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onRequestSetAmount"){
      this.setState({
        transactionData : message
      });
      pos.setAmount("123","","0156",TransactionType.GOODS);
    }else if(msg.method == "onRequestPinEntry"){
      this.setState({
        transactionData : message
      });
      pos.sendPinEntryResult("1234");
    }else if(msg.method == "onDoTradeResult"){
      if(msg.result == "DoTradeResult_ICC"){
         pos.doEmvApp(EmvOption.EmvOption_START);
      }else if(msg.result == "DoTradeResult_NFC_ONLINE" || msg.result == "DoTradeResult_NFC_OFFLINE"){
        this.setState({
          transactionData : message
        });
        let data = pos.getNFCBatchData();
        console.log("js",data);
      }else{
         this.setState({
           transactionData : message
         });
      }
    }else if(msg.method == "onRequestDisplay"){
      this.setState({
        transactionData : message
      });
    }else if(msg.method == "onRequestTime"){
      this.setState({
        transactionData : message
      });
      var time = this.formattedDate();
      console.log("formattedDate: " + time);
      pos.sendTime(this.formattedDate());
    }else if(msg.method == "onRequestSelectEmvApp"){
      this.setState({
        transactionData : message
      });
      pos.selectEmvApp(0);
    }else if(msg.method == "onRequestOnlineProcess"){
      this.setState({
        transactionData : message
      });
      //let data = pos.getICCTag(EncryptType.EncryptType_plaintext,0,2,"9F3495");//get 9F34, 95 tag from terminal
      //console.log("getICCTag: " + data);
      pos.sendOnlineProcessResult("8A023030");
    }else if(msg.method == "onRequestTransactionResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onRequestBatchData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onReturnReversalData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onEmvICCExceptionData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.method == "onError"){
      this.setState({
        transactionData : message
      });
    }else if(msg.method == "onReturnCustomConfigResult"){
      this.setState({
        transactionData : message
      });
    }else if(msg.method == "onReturnGetEMVListResult"){
      this.setState({
        transactionData : message
      });
    }else if(msg.method == "onReturnUpdateEMVResult"){
      this.setState({
        transactionData : message
      });
    }else if(msg.method == "onReturnUpdateEMVRIDResult"){
      this.setState({
        transactionData : message
      });
    }else{
      this.setState({
        transactionData : message
      });
    }
   }
   _onPressItem(item) {
    console.log("connectBluetooth: " + item.key);
    pos.stopQPos2Mode();
    pos.connectBT(item.key);
    this.setState({
      bluetoothName : []
    });
   }
  /**
   * RN调用Native且通过Callback回调 通信方式
   */
   scanBluetooth(msg) {
      this.setState({
           bluetoothName : []
      });
      pos.initPos(communicationMode[0]);
      console.log("scanBluetooth");
      pos.scanQPos2Mode(10);
   }

   doTrade(msg) {
      console.log("doTrade");
      pos.setCardTradeMode(CardTradeMode.CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP);
      pos.doTrade(0,20);
   }

   disconnect(msg) {
      console.log("disconnect");
      pos.disconnectBT();
      this.setState = ({
        bluetoothName: [],
        transactionData: "",
       });
   }

   getQposId(msg) {
      console.log("getQposId");
      pos.getQPosId();
   }

   getQposInfo(msg) {
      console.log("getQposInfo");
      pos.getQPosInfo();
   }
    
   resetPosStatus(msg) {
      console.log("resetPosStatus");
      pos.resetPosStatus();
   }

   updateEMVConfigByXML(msg) {
    console.log("updateEMVConfigByXML");
  // use fetch to read emv xml content
    fetch("https://gitlab.com/dspread/android/-/raw/master/pos_android_studio_demo/pos_android_studio_app/src/main/assets/emv_profile_tlv.xml?ref_type=heads")
    .then(response => response.text())
    .then(text => {
      console.log('XML content:', text);
      pos.updateEMVConfigByXml(text);
    })
    .catch(error => {
      console.error('Error reading XML file:', error);
    });
 }

 updateEMVConfigByTlv(msg) {
  /*
  update contactless cvm limit:
  9F0607A00000000310109F92810E06000000050000 visa
  9F0607A00000000320109F92810E06000000050000 visa
  9F0607A00000000330109F92810E06000000050000 visa
  9F0607A0000000041010DF812606000000050000 mastercard
  9F0606A000000025019F820906000000050000 amex
  9F0607A00000015230109F820906000000050000 discover
  */
  pos.updateEmvAPPByTlv(EMVOperation.EMVOperation_update,"9F0607A00000000310109F92810E060000000500009F0607A00000000320109F92810E060000000500009F0607A00000000330109F92810E060000000500009F0607A0000000041010DF8126060000000500009F0606A000000025019F8209060000000500009F0607A00000015230109F820906000000050000");
}

   formattedDate(){
    const date = new Date();
    const year = date.getFullYear();  
    const month = (date.getMonth() + 1).toString().padStart(2, '0');  
    const day = date.getDate().toString().padStart(2, '0'); 
    const hour = date.getHours().toString().padStart(2, '0');
    const minute = date.getMinutes().toString().padStart(2, '0');
    const second = date.getSeconds().toString().padStart(2, '0');
    const formattedDate = year+month+day+hour+minute+second;
    return formattedDate;  
  }

  styles = StyleSheet.create({

   container:{
      marginTop : 10,
      marginLeft: 10,
      marginRight:10,
    },
    
    text:{
        textAlign:'center',
        textAlignVertical:'center',
        height : 40,
        fontSize : 18
    },

    footView :{
      backgroundColor : "#00FF00"
    },

    textStyle:{
      fontSize : 17,
      marginBottom : 10
    },

    textBlue:{
      fontSize : 15,
      marginBottom : 10
    },

    button:{
        backgroundColor : "#4CAF50",
        height : 40,
        marginBottom : 10,
        borderRadius : 8
    },

    scrollView:{
       marginTop : 10,
       marginBottom : 20
    },

    item:{
        padding:10,
        fontSize:18,
        height:44,
    },
  });
}