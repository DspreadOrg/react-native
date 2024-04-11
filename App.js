import React, { Component } from 'react';
import {AppRegistry, Text, TextInput, View, EventEmitter,NativeEventEmitter,NativeModules,Button, StyleSheet,DeviceEventEmitter,FlatList, TouchableOpacity, string, ScrollView, Title, Alert, prompt} from 'react-native';
var pos = NativeModules.NativePosModule;
import { Colors } from 'react-native/Libraries/NewAppScreen';
const NativePosEmitter = new NativeEventEmitter(pos);

/**
     * Transaction type.
     */
const TransactionType = [
  'GOODS', // 货物 GOODS
  'SERVICES', // 服务 service
  'CASH', // 现金 cash
  'CASHBACK', //  返现
  'INQUIRY', // 查询
  'TRANSFER', // 转账
  'ADMIN', // 管理
  'CASHDEPOSIT', // 存款
  'PAYMENT', // 付款 支付

  'PBOCLOG', // 0x0A /*PBOC日志(电子现金日志)*/
  'SALES_NEW', // 0x0B /*消费*/
  'PREAUTH', // 0x0C /*预授权*/

  'ECQ_DESIGNATED_LOAD', // 0x10 /*电子现金Q指定账户圈存*/
  'ECQ_UNDESIGNATED_LOAD', // 0x11 /*电子现金费非指定账户圈存*/
  'ECQ_CASH_LOAD', // 0x12 /*电子现金费现金圈存*/
  'ECQ_CASH_LOAD_VOID', // 0x13 /*电子现金圈存撤销*/
  'ECQ_INQUIRE_LOG', // 0x0A /*电子现金日志(和PBOC日志一样)*/
  'REFUND',//退款
  'UPDATE_PIN',     //修改密码
  'SALES_NEW',
  'NON_LEGACY_MONEY_ADD', /* 0x17*/
  'LEGACY_MONEY_ADD',  /*0x16*/
  'BALANCE_UPDATE', /*0x18*/
];
const communicationMode = [
  'BLUETOOTH',
  'BLUETOOTH_BLE',
  'UART',
  'USB_OTG_CDC_ACM',
  'AUDIO',
   ];


var test = NativeModules.NativePosModule;
export default class catComponent extends Component {
 
  render() {
      return (
            <View style = {this.styles.container}>
        							
			{/* <Button title="Start Now!" onPress={this.scanBluetooth2android}/> */}
				
        
            <FlatList
            ListHeaderComponent={
                           
                <View>
                   <View style = {this.styles.container}>
                      {/* <TouchableOpacity onPress={this.initPosBluetooth.bind(this)} style = {this.styles.button}>
                      <Text style={this.styles.text}>initPos Bluetooth</Text>                                          
                      </TouchableOpacity>  */}
                      <TouchableOpacity onPress={this.scanBluetooth.bind(this)} style = {this.styles.button}>
                      <Text style={this.styles.text}>Scan Bluetooth</Text>
                      </TouchableOpacity> 
                      <TouchableOpacity onPress={this.disconnect} style = {this.styles.button}>
                      <Text style={this.styles.text}>Disconnect</Text>
                    </TouchableOpacity>    
                   </View>
                     <View style = {this.styles.container}>
                     <TouchableOpacity onPress={this.initPosUART.bind(this)} style = {this.styles.button}>
                      <Text style={this.styles.text}>Open UART</Text>                                            
                    </TouchableOpacity>  
                    <TouchableOpacity onPress={this.closeUart} style = {this.styles.button}>
                      <Text style={this.styles.text}>Close Uart</Text>
                    </TouchableOpacity>                 
                    </View>
                   
                    <TouchableOpacity onPress={this.doTrade} style = {this.styles.buttons}>
                      <Text style={this.styles.text}>DoTrade</Text>
                    </TouchableOpacity>  
                    <View style = {this.styles.container}>
                    <TouchableOpacity onPress={this.getQposId} style = {this.styles.button}>
                      <Text style={this.styles.text}>GetQposId</Text>
                    </TouchableOpacity> 
                    <TouchableOpacity onPress={this.getQposInfo} style = {this.styles.button}>
                      <Text style={this.styles.text}>GetQposInfo</Text>
                    </TouchableOpacity>   
                    </View>
                    {/* <Text style = {this.styles.textStyle}>Bluetooth Name</Text>         */}
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



scanBluetooth2android(){
		//test.jump();
		test.jump
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
// 获取数据列表

  onScanningResult(msg){  
   
    var message = msg.key + "\n" + msg.result;
    console.log("js",message);

    if(msg.key == "onBluetoothName2Mode"){
      blueName = msg.result;
      var pages = this.state.bluetoothName;
      pages.push(
          {key : blueName}
      );
     
      this.setState({
           bluetoothName : pages
          
      });
    }else if(msg.key == "onRequestQposConnected"){
      this.setState({
           bluetoothName: [],
          transactionData: "",
    
          transactionData : message
      });

    }else if(msg.key == "onRequestQposDisconnected"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestNoQposDetected"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onQposIdResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onQposInfoResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestWaitingUser"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestSetAmount"){
      this.setState({
        transactionData : message
      });
      pos.setAmount("123","","0156",TransactionType[0]);
    }else if(msg.key == "onRequestSetPin"){
      this.setState({
        transactionData : message
      });
      pos.sendPin("1234") 
    }else if(msg.key == "onDoTradeResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestDisplay"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestSelectEmvApp"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestOnlineProcess"){
      this.setState({
        transactionData : message
      });
      pos.sendOnlineProcessResult("8A023030");
    }else if(msg.key == "onRequestTransactionResult"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onRequestBatchData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onReturnReversalData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onEmvICCExceptionData"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "onDHError"){
      this.setState({
        transactionData : message
      });

    }else if(msg.key == "doTrade"){
        this.setState({
          transactionData : message
       });
    }else if(msg.key=="onError"){
      this.setState({
        transactionData : message
     });

    }
   
   }
   _onPressItem(item) {
  
    console.log("connectBluetooth: " + item.key);
    pos.connectBT(item.key);
   }
  /**
   * RN调用Native且通过Callback回调 通信方式
   */

  
    // initPosBluetooth(msg){
    //   pos.initPos(communicationMode[0]);
    // }
    scanBluetooth(msg) {
      this.setState({
           bluetoothName : []
      });
      console.log("scanBluetooth");
      pos.initPos(communicationMode[0]);
      pos.scanQPos2Mode(10);
   }
   initPosUART(msg){
    pos.initPos(communicationMode[2]);
  }
   closeUart(msg){
     pos.closeUart();
  }
   doTrade(msg) {
      console.log("doTrade");
      pos.doTrade(0,20);
   }

   disconnect(msg) {
    this.state = ({
      bluetoothName: [],
      transactionData: "",
  });
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

  styles = StyleSheet.create({

   container:{
       // marginTop : 10,
      // marginLeft: 10,
      // marginRight:10,
      flexDirection: 'row', 
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
      padding:5,
      marginBottom : 10
    },

    textBlue:{
      fontSize : 15,
      marginBottom : 10
    },

    button:{
        marginTop:5,
        backgroundColor : "#4CAF50",
        height : 40,
        marginLeft:10,
        marginRight:10,
        marginBottom : 10,
        borderRadius : 8,
        width:'43%'
        // width:140
    },
    buttons:{
      marginTop:5,
      backgroundColor : "#4CAF50",
      height : 40,
      marginLeft:10,
      marginRight:10,
      marginBottom : 10,
      borderRadius : 8,
      // width:140
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
