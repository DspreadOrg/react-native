import React, { Component } from 'react';
import { Text, TextInput, View, NativeEventEmitter,NativeModules,Button, StyleSheet,DeviceEventEmitter,FlatList, TouchableOpacity, string, ScrollView, Title, Alert, prompt} from 'react-native';
var pos = NativeModules.NativePosModule;
import { Colors } from 'react-native/Libraries/NewAppScreen';
const NativePosEmitter = new NativeEventEmitter(pos);

/**
     * Transaction type.
     */
export const MPosTransactionType = {
    TransactionType_GOODS : 0, // 货物
    TransactionType_SERVICES : 1, // 服务
    TransactionType_CASH : 2,//现金
    TransactionType_CASHBACK : 3, // 退货 返现
    TransactionType_INQUIRY : 4, // 查询
    TransactionType_TRANSFER : 5, // 转账
    TransactionType_ADMIN : 6,//管理
    TransactionType_CASHDEPOSIT : 7,//存款
    TransactionType_PAYMENT : 8,// 付款 支付
    TransactionType_PBOCLOG : 9,//        0x0A            /*PBOC日志(电子现金日志)*/
    TransactionType_SALE : 10,//           0x0B            /*消费*/
    TransactionType_PREAUTH : 11,//        0x0C            /*预授权*/
    TransactionType_ECQ_DESIGNATED_LOAD : 12,//        0x10
    TransactionType_ECQ_UNDESIGNATED_LOAD : 13,//    0x11
    TransactionType_ECQ_CASH_LOAD : 14,//    0x12    /*电子现金费现金圈存*/
    TransactionType_ECQ_CASH_LOAD_VOID : 15,//            0x13
    TransactionType_ECQ_INQUIRE_LOG : 16,//    0x0A    /*电子现金日志(和PBOC日志一样)*/
    TransactionType_REFUND : 17,
    TransactionType_UPDATE_PIN : 18,
}

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
    CardTradeMode_SWIPE_INSERT_CARD : 2,
    CardTradeMode_UNALLOWED_LOW_TRADE : 3,
    CardTradeMode_SWIPE_TAP_INSERT_CARD : 4,// add 20150715
    CardTradeMode_SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE : 5,
    CardTradeMode_ONLY_TAP_CARD : 6,
    CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP : 7,
    CardTradeMode_TAP_INSERT_CARD_NOTUP : 8,//无上翻键
    CardTradeMode_TAP_INSERT_CARD_TUP : 9,//有上翻键
    CardTradeMode_SWIPE_TAP_INSERT_CARD_Down : 10,//下翻建
    CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP_UNALLOWED_LOW_TRADE : 11,
    CardTradeMode_SWIPE_INSERT_CARD_UNALLOWED_LOW_TRADE : 12,
    CardTradeMode_SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE_NEW : 13,
    CardTradeMode_SWIPE_TAP_INSERT_CARD_NOTUP_DELAY : 14,
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
      pos.setAmount("123","","0156",MPosTransactionType.TransactionType_GOODS);
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
      //pos.getICCTag(EncryptType_plaintext,0,2,"9F3495");//get 9F34, 95 tag from terminal
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

    }else if(msg.method == "onDHError"){
      this.setState({
        transactionData : message
      });
    }
   }
   _onPressItem(item) {
    pos.stopQPos2Mode();
    console.log("connectBluetooth: " + item.key);
    pos.connectBT(item.key);
   }
  /**
   * RN调用Native且通过Callback回调 通信方式
   */
   scanBluetooth(msg) {
      this.setState({
           bluetoothName : []
      });
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
