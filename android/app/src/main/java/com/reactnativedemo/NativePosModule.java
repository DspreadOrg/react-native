package com.reactnativedemo;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativedemo.keyboard.keyboard.KeyBoardNumInterface;
import com.reactnativedemo.keyboard.keyboard.KeyboardUtil;
import com.reactnativedemo.keyboard.keyboard.MyKeyboardView;
import com.reactnativedemo.utils.QPOSUtil;
import com.reactnativedemo.utils.TRACE;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NativePosModule extends ReactContextBaseJavaModule {
    public ReactApplicationContext reactApplicationContext;
    public Context context;
    private QPOSService pos;
    MyQposClass listener;
    private KeyboardUtil keyboardUtil;
    private List<String> keyBoardList = new ArrayList<>();

    public NativePosModule(ReactApplicationContext applicationContext) {
        super(applicationContext);
        this.reactApplicationContext = applicationContext;
        context = applicationContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "NativePosModule";
    }

    //表示react native和android有相同module时，返回true表示允许覆盖
    @Override
    public boolean canOverrideExistingModule() {
        return true;
    }

    @ReactMethod
    public void initPos(String mode) {
        Log.w("initPos", "mode==" + mode);
        if ("BLUETOOTH".equals(mode)) {
            bluetoothRelaPer();
            open(QPOSService.CommunicationMode.BLUETOOTH);
        } else if ("UART".equals(mode)) {
            open(QPOSService.CommunicationMode.UART);
            String blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
            // blueTootchAddress = "/dev/ttyHSL1";//tongfang is s1，tianbo is s3
            pos.setDeviceAddress(blueTootchAddress);
            pos.openUart();
        } else if ("USB_OTG_CDC_ACM".equals(mode)) {
            USBClass usb = new USBClass();
            ArrayList<String> deviceList = usb.GetUSBDevices(getCurrentActivity());
            if (deviceList == null) {
                Toast.makeText(context, "No Permission", Toast.LENGTH_SHORT).show();
                return;
            }
            final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
            builder.setTitle("Select a Reader");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    String selectedDevice = (String) items[item];
                    dialog.dismiss();
                    UsbDevice usbDevice = USBClass.getMdevices().get(selectedDevice);
                    open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
                    pos.openUsb(usbDevice);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @ReactMethod
    public void scanQPos2Mode(int time) {
        pos.scanQPos2Mode(context, 20);
        Log.w("scanQPos2Mode", "scanQPos2Mode=" + time);
    }

    @ReactMethod
    public void setCardTradeMode(int cardTradeMode) {
        Log.w("setCardTradeMode: ", QPOSService.CardTradeMode.values()[cardTradeMode].toString());
        pos.setCardTradeMode(QPOSService.CardTradeMode.values()[cardTradeMode]);
    }

    private void open(QPOSService.CommunicationMode mode) {
        TRACE.d("open");
        //pos=null;
        listener = new MyQposClass();
        pos = QPOSService.getInstance(context,mode);
        if (pos == null) {
            Log.w("open", "CommunicationMode unknow");
            return;
        }
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
//        pos.setD20Trade(true);
//        pos.setConext(context);
        //init handler
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);

    }

    @ReactMethod
    public void connectBT(String blueTootchName) {
        String blueTootchAddress = listener.deviceList.get(blueTootchName);
        Log.w("connectBluetoothDevice", "connectBluetoothDevice=22=" + blueTootchAddress);
        pos.connectBluetoothDevice(true, 25, blueTootchAddress);
    }

    @ReactMethod
    public void stopQPos2Mode() {
        pos.stopScanQPos2Mode();
    }

    @ReactMethod
    public void resetPosStatus() {
        pos.resetQPosStatus();
    }

    @ReactMethod
    public void disconnectBT() {
        pos.disconnectBT();
    }
    @ReactMethod
    public void closeUart(){
        pos.closeUart();
    }
    @ReactMethod
    public void doTrade(int keyIdex, int timeOut) {
        pos.doTrade(keyIdex, timeOut);
    }

    @ReactMethod
    public void sendTime(String terminalTime) {
        pos.sendTime(terminalTime);
    }

    @ReactMethod
    public void setAmount(String amount, String cashbackAmount, String currencyCode,int transactionType) {
//        Log.w("setAmount", "goods==" + transactionTypeString);
//        QPOSService.TransactionType transactionType = QPOSService.TransactionType.GOODS;
//        if (transactionTypeString.equals("GOODS")) {
//            transactionType = QPOSService.TransactionType.GOODS;
//        } else if (transactionTypeString.equals("SERVICES")) {
//            transactionType = QPOSService.TransactionType.SERVICES;
//        } else if (transactionTypeString.equals("CASH")) {
//            transactionType = QPOSService.TransactionType.CASH;
//        } else if (transactionTypeString.equals("CASHBACK")) {
//            transactionType = QPOSService.TransactionType.CASHBACK;
//        } else if (transactionTypeString.equals("INQUIRY")) {
//            transactionType = QPOSService.TransactionType.INQUIRY;
//        } else if (transactionTypeString.equals("TRANSFER")) {
//            transactionType = QPOSService.TransactionType.TRANSFER;
//        } else if (transactionTypeString.equals("ADMIN")) {
//            transactionType = QPOSService.TransactionType.ADMIN;
//        } else if (transactionTypeString.equals("CASHDEPOSIT")) {
//            transactionType = QPOSService.TransactionType.CASHDEPOSIT;
//        } else if (transactionTypeString.equals("PAYMENT")) {
//            transactionType = QPOSService.TransactionType.PAYMENT;
//        } else if (transactionTypeString.equals("PBOCLOG||ECQ_INQUIRE_LOG")) {
//            transactionType = QPOSService.TransactionType.PBOCLOG;
//        } else if (transactionTypeString.equals("SALE")) {
//            transactionType = QPOSService.TransactionType.SALE;
//        } else if (transactionTypeString.equals("PREAUTH")) {
//            transactionType = QPOSService.TransactionType.PREAUTH;
//        } else if (transactionTypeString.equals("ECQ_DESIGNATED_LOAD")) {
//            transactionType = QPOSService.TransactionType.ECQ_DESIGNATED_LOAD;
//        } else if (transactionTypeString.equals("ECQ_UNDESIGNATED_LOAD")) {
//            transactionType = QPOSService.TransactionType.ECQ_UNDESIGNATED_LOAD;
//        } else if (transactionTypeString.equals("ECQ_CASH_LOAD")) {
//            transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD;
//        } else if (transactionTypeString.equals("ECQ_CASH_LOAD_VOID")) {
//            transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD_VOID;
//        } else if (transactionTypeString.equals("CHANGE_PIN")) {
//            transactionType = QPOSService.TransactionType.UPDATE_PIN;
//        } else if (transactionTypeString.equals("REFOUND")) {
//            transactionType = QPOSService.TransactionType.REFUND;
//        } else if (transactionTypeString.equals("SALES_NEW")) {
//            transactionType = QPOSService.TransactionType.SALES_NEW;
//        }
        pos.setAmount(amount, cashbackAmount, currencyCode, QPOSService.TransactionType.values()[transactionType]);
    }

    @ReactMethod
    public void sendOnlineProcessResult(String tlv) {
        pos.sendOnlineProcessResult(tlv);
    }

    @ReactMethod
    public void sendPin(String pin) {
        pos.sendPin(pin.getBytes(), false);
    }

    @ReactMethod
    public void getQPosId() {
        pos.getQposId();
    }

    @ReactMethod
    public void getQPosInfo() {
        pos.getQposInfo();
    }

    @ReactMethod
    public void addListener(String eventName) {

    }

    @ReactMethod
    public void removeListeners(Integer count) {

    }

    @ReactMethod
    public Hashtable getICCTag(int encryType, int cardType, int tagCount, String tagArrStr) {
       return pos.getICCTag(QPOSService.EncryptType.values()[encryType],cardType,tagCount,tagArrStr);
    }

    @ReactMethod
    public void selectEmvApp(int applicationIndex) {
        pos.selectEmvApp(applicationIndex);
    }

    @ReactMethod
    public void doEmvApp(int emvOption) {
        pos.doEmvApp(QPOSService.EmvOption.values()[emvOption]);
    }

    @ReactMethod
    public Hashtable<String, String> getNFCBatchData() {
        return pos.getNFCBatchData();
    }

    @ReactMethod
    public void updateEMVConfigByXml(String xmlData) {
        pos.updateEMVConfigByXml(xmlData);
    }

    class MyQposClass extends CQPOSService {

        public Map<String, String> deviceList = new HashMap<String, String>();

        @Override
        public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
            TRACE.d("onRequestWaitingUser()");
            String waitingInfo = context.getString(R.string.waiting_for_card);
            sendMsg("onRequestWaitingUser", waitingInfo);
        }

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            String content = "";
            if (result == QPOSService.DoTradeResult.NONE) {
                content = context.getString(R.string.no_card_detected);
            } else if (result == QPOSService.DoTradeResult.ICC) {
//                pos.doEmvApp(QPOSService.EmvOption.START);
            } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
                content = context.getString(R.string.card_inserted);
            } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
                content = context.getString(R.string.bad_swipe);
            } else if (result == QPOSService.DoTradeResult.CARD_NOT_SUPPORT) {
                content = "GPO NOT SUPPORT";
            } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                content = "PLS SEE PHONE";
            } else if (result == QPOSService.DoTradeResult.MCR) {//磁条卡
                content = context.getString(R.string.card_swiped);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    content += context.getString(R.string.format_id) + " " + formatID + "\n";
                    content += context.getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += context.getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += context.getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                    content += context.getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += context.getString(R.string.pinBlock) + " " + pinblock + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                } else if (formatID.equals("FF")) {
                    String type = decodeData.get("type");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    content += "cardType:" + " " + type + "\n";
                    content += "track_1:" + " " + encTrack1 + "\n";
                    content += "track_2:" + " " + encTrack2 + "\n";
                    content += "track_3:" + " " + encTrack3 + "\n";
                } else {
                    String orderID = decodeData.get("orderId");
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    // TODO
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");
                    if (orderID != null && !"".equals(orderID)) {
                        content += "orderID:" + orderID;
                    }
                    content += context.getString(R.string.format_id) + " " + formatID + "\n";
                    content += context.getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += context.getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += context.getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                    content += context.getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += context.getString(R.string.trackksn) + " " + trackksn + "\n";
                    content += context.getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += context.getString(R.string.track_1_length) + " " + track1Length + "\n";
                    content += context.getString(R.string.track_2_length) + " " + track2Length + "\n";
                    content += context.getString(R.string.track_3_length) + " " + track3Length + "\n";
                    content += context.getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                    content += context.getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                    content += context.getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                    content += context.getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                    content += context.getString(R.string.partial_track) + " " + partialTrack + "\n";
                    content += context.getString(R.string.pinBlock) + " " + pinBlock + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
//                    String realPan = null;
//                    if (!TextUtils.isEmpty(trackksn) && !TextUtils.isEmpty(encTrack2)) {
//                        String clearPan = DUKPK2009_CBC.getDate(trackksn, encTrack2, DUKPK2009_CBC.Enum_key.DATA, DUKPK2009_CBC.Enum_mode.CBC);
//                    content += "encTrack2:" + " " + clearPan + "\n";
//                        realPan = clearPan.substring(0, maskedPAN.length());
//                    content += "realPan:" + " " + realPan + "\n";
//                    }
//                    if (!TextUtils.isEmpty(pinKsn) && !TextUtils.isEmpty(pinBlock) && !TextUtils.isEmpty(realPan)) {
//                        String date = DUKPK2009_CBC.getDate(pinKsn, pinBlock, DUKPK2009_CBC.Enum_key.PIN, DUKPK2009_CBC.Enum_mode.CBC);
//                        String parsCarN = "0000" + realPan.substring(realPan.length() - 13, realPan.length() - 1);
//                        String s = DUKPK2009_CBC.xor(parsCarN, date);
//                    content += "PIN:" + " " + s + "\n";
//                    }
                }
            } else if ((result == QPOSService.DoTradeResult.NFC_ONLINE) || (result == QPOSService.DoTradeResult.NFC_OFFLINE)) {
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40")
                        || formatID.equals("37") || formatID.equals("17")
                        || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");

                    content += context.getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += context.getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += context.getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += context.getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";

                    content += context.getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += context.getString(R.string.pinBlock) + " " + pinblock
                            + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                } else {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");

                    content += context.getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += context.getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += context.getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += context.getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";
                    content += context.getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += context.getString(R.string.trackksn) + " " + trackksn
                            + "\n";
                    content += context.getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += context.getString(R.string.track_1_length) + " "
                            + track1Length + "\n";
                    content += context.getString(R.string.track_2_length) + " "
                            + track2Length + "\n";
                    content += context.getString(R.string.track_3_length) + " "
                            + track3Length + "\n";
                    content += context.getString(R.string.encrypted_tracks) + " "
                            + encTracks + "\n";
                    content += context.getString(R.string.encrypted_track_1) + " "
                            + encTrack1 + "\n";
                    content += context.getString(R.string.encrypted_track_2) + " "
                            + encTrack2 + "\n";
                    content += context.getString(R.string.encrypted_track_3) + " "
                            + encTrack3 + "\n";
                    content += context.getString(R.string.partial_track) + " "
                            + partialTrack + "\n";
                    content += context.getString(R.string.pinBlock) + " " + pinBlock
                            + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber
                            + "\n";
                }
            } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
                content += context.getString(R.string.transaction_declined);
            } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
                content += context.getString(R.string.card_no_response);
            }
            sendMsg("onDoTradeResult", "DoTradeResult_"+result.toString(),JSONObject.toJSONString(decodeData));

        }

        @Override
        public void onQposInfoResult(Hashtable<String, String> posInfoData) {
            TRACE.d("onQposInfoResult" + posInfoData.toString());
            String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
            String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
            String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
            String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
            String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
            String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
            String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
            String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
            String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
                    : posInfoData.get("batteryPercentage");
            String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
            String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
            String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? ""
                    : posInfoData.get("PCI_firmwareVersion");
            String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? ""
                    : posInfoData.get("PCI_hardwareVersion");
            String content = "";
            content += context.getString(R.string.bootloader_version) + bootloaderVersion + "\n";
            content += context.getString(R.string.firmware_version) + firmwareVersion + "\n";
            content += context.getString(R.string.usb) + isUsbConnected + "\n";
            content += context.getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
            content += context.getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
            content += context.getString(R.string.battery_percentage) + batteryPercentage + "\n";
//			}
            content += context.getString(R.string.hardware_version) + hardwareVersion + "\n";
            content += "SUB : " + SUB + "\n";
            content += context.getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
            content += context.getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
            content += context.getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
            content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
            content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
            sendMsg("onQposInfoResult", "",JSONObject.toJSONString(posInfoData));
        }

        /**
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
//            if (transactionResult != QPOSService.TransactionResult.APPROVED) {
                sendMsg("onRequestTransactionResult", "TransactionResult_"+transactionResult.toString());
//            }
        }

        @Override
        public void onRequestBatchData(String tlv) {
            TRACE.d("ICC trade finished");
            TRACE.d("onRequestBatchData(String tlv):" + tlv);
            sendMsg("onRequestBatchData", "",tlv);
        }

        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            super.onQposRequestPinResult(dataList, offlineTime);
            keyBoardList = dataList;
            MyKeyboardView.setKeyBoardListener(new KeyBoardNumInterface() {
                @Override
                public void getNumberValue(String value) {
                    pos.pinMapSync(value, 20);
                }
            });
            keyboardUtil = new KeyboardUtil(getCurrentActivity(), keyBoardList);
        }

        @Override
        public void onReturnGetPinInputResult(int num) {
            super.onReturnGetPinInputResult(num);
            String s = "";
            if (num == -1) {
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
            } else {
                for (int i = 0; i < num; i++) {
                    s += "*";
                }
                KeyboardUtil.pinpadEditText.setText(s);
            }
        }

        @Override
        public void onRequestTransactionLog(String tlv) {
            TRACE.d("onRequestTransactionLog(String tlv):" + tlv);
        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            TRACE.w("onQposIdResult():" + posIdTable.toString());
            String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
            String psamId = posIdTable.get("psamId") == null ? "" : posIdTable
                    .get("psamId");
            String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable
                    .get("nfcID");
            String content = "";
            content += context.getString(R.string.posId) + posId + "\n";
            content += "csn: " + csn + "\n";
            content += "conn: " + pos.getBluetoothState() + "\n";
            content += "psamId: " + psamId + "\n";
            content += "NFCId: " + NFCId + "\n";
            sendMsg("onQposIdResult", "",JSONObject.toJSONString(posIdTable));
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            sendMsg("onRequestSelectEmvApp","",JSONObject.toJSONString(appList));
//            Dialog dialog = new Dialog(getCurrentActivity());
//            dialog.setContentView(R.layout.emv_app_dialog);
//            dialog.setTitle(R.string.please_select_app);
//            String[] appNameList = new String[appList.size()];
//            for (int i = 0; i < appNameList.length; ++i) {
//                appNameList[i] = appList.get(i);
//            }
//            ListView appListView = (ListView) dialog.findViewById(R.id.appList);
//            appListView.setAdapter(new ArrayAdapter<String>(getCurrentActivity(), android.R.layout.simple_list_item_1, appNameList));
//            appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    pos.selectEmvApp(position);
//                    TRACE.d("select emv app position = " + position);
//                    dialog.dismiss();
//                }
//            });
//            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    pos.cancelSelectEmvApp();
//                    dialog.dismiss();
//                }
//            });
//            dialog.show();
        }

        @Override
        public void onRequestSetAmount() {
            TRACE.d("onRequestSetAmount()");
            sendMsg("onRequestSetAmount", "");
        }

        /**
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected()
         */
        @Override
        public void onRequestIsServerConnected() {
            TRACE.d("onRequestIsServerConnected()");
        }

        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess" + tlv);
            sendMsg("onRequestOnlineProcess", "");
        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            sendMsg("onRequestTime", "");
//            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        }

        @Override
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            String msg = "";
            if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
                msg = "";
            } else if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
//            builder.setTitle("Audio");
//            builder.setMessage("Success,Contine ready");
//            builder.setPositiveButton("Confirm", null);
//            builder.show();
            } else if (displayMsg == QPOSService.Display.PLEASE_WAIT) {
//            msg = getString(R.string.wait);
            } else if (displayMsg == QPOSService.Display.REMOVE_CARD) {
//            msg = getString(R.string.remove_card);
            } else if (displayMsg == QPOSService.Display.TRY_ANOTHER_INTERFACE) {
                msg = context.getString(R.string.try_another_interface);
            } else if (displayMsg == QPOSService.Display.PROCESSING) {
//            msg = getString(R.string.processing);
            } else if (displayMsg == QPOSService.Display.INPUT_PIN_ING) {
                msg = "please input pin on pos";

            } else if (displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == QPOSService.Display.INPUT_LAST_OFFLINE_PIN) {
                msg = "please input offline pin on pos";

            } else if (displayMsg == QPOSService.Display.MAG_TO_ICC_TRADE) {
                msg = "please insert chip card on pos";
            } else if (displayMsg == QPOSService.Display.CARD_REMOVED) {
                msg = "card removed";
            }
            sendMsg("onRequestDisplay", "Display_"+displayMsg.toString());
        }

        @Override
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
        }

        @Override
        public void onRequestNoQposDetected() {
            TRACE.d("onRequestNoQposDetected()");
            sendMsg("onRequestNoQposDetected", "");
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()");
            sendMsg("onRequestQposConnected", "");

        }

        @Override
        public void onRequestQposDisconnected() {
            sendMsg("onRequestQposDisconnected", "");
        }

        @Override
        public void onError(QPOSService.Error errorState) {
            TRACE.d("onError" + errorState.toString());
//            String content = "";
//            if (errorState == QPOSService.Error.CMD_NOT_AVAILABLE) {
//                content = context.getString(R.string.command_not_available);
//            } else if (errorState == QPOSService.Error.TIMEOUT) {
//                content = context.getString(R.string.device_no_response);
//            } else if (errorState == QPOSService.Error.DEVICE_RESET) {
//                content = context.getString(R.string.device_reset);
//            } else if (errorState == QPOSService.Error.UNKNOWN) {
//                content = context.getString(R.string.unknown_error);
//            } else if (errorState == QPOSService.Error.DEVICE_BUSY) {
//                content = context.getString(R.string.device_busy);
//            } else if (errorState == QPOSService.Error.INPUT_OUT_OF_RANGE) {
//                content = context.getString(R.string.out_of_range);
//            } else if (errorState == QPOSService.Error.INPUT_INVALID_FORMAT) {
//                content = context.getString(R.string.invalid_format);
//            } else if (errorState == QPOSService.Error.INPUT_ZERO_VALUES) {
//                content = context.getString(R.string.zero_values);
//            } else if (errorState == QPOSService.Error.INPUT_INVALID) {
//                content = context.getString(R.string.input_invalid);
//            } else if (errorState == QPOSService.Error.CASHBACK_NOT_SUPPORTED) {
//                content = context.getString(R.string.cashback_not_supported);
//            } else if (errorState == QPOSService.Error.CRC_ERROR) {
//                content = context.getString(R.string.crc_error);
//            } else if (errorState == QPOSService.Error.COMM_ERROR) {
//                content = context.getString(R.string.comm_error);
//            } else if (errorState == QPOSService.Error.MAC_ERROR) {
//                content = context.getString(R.string.mac_error);
//            } else if (errorState == QPOSService.Error.APP_SELECT_TIMEOUT) {
//                content = context.getString(R.string.app_select_timeout_error);
//            } else if (errorState == QPOSService.Error.CMD_TIMEOUT) {
//                content = context.getString(R.string.cmd_timeout);
//            } else if (errorState == QPOSService.Error.ICC_ONLINE_TIMEOUT) {
//            }
            sendMsg("onError", "Error_"+errorState.toString());
        }

        @Override
        public void onReturnReversalData(String tlv) {
            TRACE.d("onReturnReversalData(): " + tlv);
            sendMsg("onReturnReversalData", "",tlv);
        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> result) {
            TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
            String pinBlock = result.get("pinBlock");
            String pinKsn = result.get("pinKsn");
            String content = "get pin result\n";
            TRACE.i(content);
            sendMsg("onReturnGetPinResult", "",JSONObject.toJSONString(result));
        }

        @Override
        public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffIccResult(boolean arg0) {
            TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);
        }

        @Override
        public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            if (arg0) {
//            pos.sendApdu("123456");
            }
        }

        @Override
        public void onReturnSetSleepTimeResult(boolean isSuccess) {
            TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);
            String content = "";
            if (isSuccess) {
                content = "set the sleep time success.";
            } else {
                content = "set the sleep time failed.";
            }
        }

        @Override
        public void onGetCardNoResult(String cardNo) {
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
        }

        @Override
        public void onRequestCalculateMac(String calMac) {
            TRACE.d("onRequestCalculateMac(String calMac):" + calMac);
            if (calMac != null && !"".equals(calMac)) {
                calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
            }
            TRACE.d("calMac_result: calMac=> e: " + calMac);
        }

        @Override
        public void onRequestSignatureResult(byte[] arg0) {
            TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
        }

        @Override
        public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
//        if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
//            statusEditText.setText("update work key success");
//        } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
//            statusEditText.setText("update work key fail");
//        } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
//            statusEditText.setText("update work key packet vefiry error");
//        } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
//            statusEditText.setText("update work key packet len error");
//        }
            sendMsg("onReturnGetPinResult", result.toString());
        }

        @Override
        public void onReturnCustomConfigResult(boolean isSuccess, String result) {
            TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + TRACE.NEW_LINE + result);
            sendMsg("onReturnCustomConfigResult", isSuccess? "Success":"Fail");
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
            sendMsg("onRequestSetPin", "");
        }

        @Override
        public void onReturnSetMasterKeyResult(boolean isSuccess) {
            TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
            sendMsg("onReturnSetMasterKeyResult", isSuccess? "Success":"Fail");
        }

        @Override
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
            TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
            StringBuilder sb = new StringBuilder();
            sb.append("APDU Responses: \n");
            for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
                sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
            }
        }

        @Override
        public void onBluetoothBondFailed() {
            TRACE.d("onBluetoothBondFailed()");
            sendMsg("onBluetoothBondFailed","");
        }

        @Override
        public void onBluetoothBondTimeout() {
            TRACE.d("onBluetoothBondTimeout()");
            sendMsg("onBluetoothBondTimeout","");
        }

        @Override
        public void onBluetoothBonded() {
            TRACE.d("onBluetoothBonded()");
            sendMsg("onBluetoothBonded","");
        }

        @Override
        public void onBluetoothBonding() {
            TRACE.d("onBluetoothBonding()");
            sendMsg("onBluetoothBonding","");
        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");
            sendMsg("onReturniccCashBack","",JSONObject.toJSONString(result));
        }

        @Override
        public void onLcdShowCustomDisplay(boolean arg0) {
            TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
            sendMsg("onLcdShowCustomDisplay", arg0? "Success":"Fail");
        }

        @Override
        public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
//            if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
//            updateThread.concelSelf();
//            }
            sendMsg("onUpdatePosFirmwareResult", arg0.toString());
        }

        @Override
        public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
            TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map.toString());
            if (map == null) {
                TRACE.d("MainActivity++++++++++++++map == null");
                return;
            }
            String randomKeyLen = map.get("randomKeyLen");
            String randomKey = map.get("randomKey");
            String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
            String randomKeyCheckValue = map.get("randomKeyCheckValue");
            TRACE.d("randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
        }

        @Override
        public void onGetPosComm(int mod, String amount, String posid) {
            TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
            sendMsg("onGetPosComm", mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
        }

        @Override
        public void onPinKey_TDES_Result(String arg0) {
            TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
        }

        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
            sendMsg("onUpdateMasterKeyResult", arg0? "Success":"Fail",JSONObject.toJSONString(arg1));
        }

        @Override
        public void onEmvICCExceptionData(String arg0) {
            TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);
            sendMsg("onEmvICCExceptionData", "",arg0);
        }

        @Override
        public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
            TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onGetInputAmountResult(boolean arg0, String arg1) {
            TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffNFCResult(boolean arg0) {
            TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        }

        @Override
        public void onCbcMacResult(String result) {
            TRACE.d("onCbcMacResult(String result):" + result);
        }

        @Override
        public void onReadBusinessCardResult(boolean arg0, String arg1) {
            TRACE.d(" onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
        }

        @Override
        public void onWriteBusinessCardResult(boolean arg0) {
            TRACE.d(" onWriteBusinessCardResult(boolean arg0):" + arg0);
        }

        @Override
        public void onConfirmAmountResult(boolean arg0) {
            TRACE.d("onConfirmAmountResult(boolean arg0):" + arg0);
        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
        }

        @Override
        public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onSearchMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String statuString = arg0.get("status");
                String cardTypeString = arg0.get("cardType");
                String cardUidLen = arg0.get("cardUidLen");
                String cardUid = arg0.get("cardUid");
                String cardAtsLen = arg0.get("cardAtsLen");
                String cardAts = arg0.get("cardAts");
                String ATQA = arg0.get("ATQA");
                String SAK = arg0.get("SAK");
            }
        }

        @Override
        public void onBatchReadMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchReadMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onBatchWriteMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchWriteMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onSetBuzzerResult(boolean arg0) {
            TRACE.d("onSetBuzzerResult(boolean arg0):" + arg0);
        }

        @Override
        public void onSetBuzzerTimeResult(boolean b) {
            TRACE.d("onSetBuzzerTimeResult(boolean b):" + b);
        }

        @Override
        public void onSetBuzzerStatusResult(boolean b) {
            TRACE.d("onSetBuzzerStatusResult(boolean b):" + b);
        }

        @Override
        public void onGetBuzzerStatusResult(String s) {
            TRACE.d("onGetBuzzerStatusResult(String s):" + s);
        }

        @Override
        public void onSetManagementKey(boolean arg0) {
            TRACE.d("onSetManagementKey(boolean arg0):" + arg0);
        }

        @Override
        public void onReturnUpdateIPEKResult(boolean arg0) {
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
            sendMsg("onReturnUpdateIPEKResult",arg0? "Success":"Fail");
        }

        @Override
        public void onReturnUpdateEMVRIDResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
            sendMsg("onReturnUpdateEMVRIDResult",arg0? "Success":"Fail");
        }

        @Override
        public void onReturnUpdateEMVResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
            sendMsg("onReturnUpdateEMVResult",arg0? "Success":"Fail");
        }

        @Override
        public void onBluetoothBoardStateResult(boolean arg0) {
            TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDeviceFound(BluetoothDevice arg0) {
            if (arg0 != null && arg0.getName() != null) {
                TRACE.d("onDeviceFound(BluetoothDevice arg0):" + arg0.getName() + ":" + arg0.toString());
                deviceList.put(arg0.getName(), arg0.getAddress());
                sendMsg("onBluetoothName2Mode", arg0.getName());
            }
        }

        @Override
        public void onSetSleepModeTime(boolean arg0) {
            TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);
            sendMsg("onSetSleepModeTime",arg0? "Success":"Fail");
        }

        @Override
        public void onReturnGetEMVListResult(String arg0) {
            TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);
            if (arg0 != null && arg0.length() > 0) {
            }
        }

        @Override
        public void onWaitingforData(String arg0) {
            TRACE.d("onWaitingforData(String arg0):" + arg0);
        }

        @Override
        public void onRequestDeviceScanFinished() {
            TRACE.d("onRequestDeviceScanFinished()");
        }

        @Override
        public void onRequestUpdateKey(String arg0) {
            TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
            sendMsg("onRequestUpdateKey","",arg0);
        }

        @Override
        public void onReturnGetQuickEmvResult(boolean arg0) {
            TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);
        }

        @Override
        public void onQposDoGetTradeLogNum(String arg0) {
            TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);
            int a = Integer.parseInt(arg0, 16);
        }

        @Override
        public void onQposDoTradeLog(boolean arg0) {
            TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);
        }

        @Override
        public void onAddKey(boolean arg0) {
            TRACE.d("onAddKey(boolean arg0) :" + arg0);
        }

        @Override
        public void onQposKsnResult(Hashtable<String, String> arg0) {
            TRACE.d("onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());
            String pinKsn = arg0.get("pinKsn");
            String trackKsn = arg0.get("trackKsn");
            String emvKsn = arg0.get("emvKsn");
            TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);
        }

        @Override
        public void onQposDoGetTradeLog(String arg0, String arg1) {
            TRACE.d("onQposDoGetTradeLog(String arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
            arg1 = QPOSUtil.convertHexToString(arg1);
//        statusEditText.setText("orderId:" + arg1 + "\ntrade log:" + arg0);
        }

        @Override
        public void onRequestDevice() {
        }

        @Override
        public void onGetDevicePubKey(String clearKeys) {
            TRACE.d("onGetDevicePubKey(clearKeys):" + clearKeys);
//        statusEditText.setText(clearKeys);
            String lenStr = clearKeys.substring(0, 4);
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                sum += bit * Math.pow(16, (3 - i));
            }
//        pubModel = clearKeys.substring(4, 4 + sum * 2);
        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            sendMsg("onTradeCancelled","");
        }

        @Override
        public void onReturnSignature(boolean b, String signaturedData) {
            if (b) {
//                BASE64Encoder base64Encoder = new BASE64Encoder();
//                String encode = base64Encoder.encode(signaturedData.getBytes());
            }
        }

        @Override
        public void onReturnConverEncryptedBlockFormat(String result) {
        }

        @Override
        public void onFinishMifareCardResult(boolean arg0) {
            TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
            }
        }

        @Override
        public void getMifareCardVersion(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareCardVersion(Hashtable<String, String> arg0):" + arg0.toString());

                String verLen = arg0.get("versionLen");
                String ver = arg0.get("cardVersion");
            }
        }

        @Override
        public void getMifareFastReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareFastReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String startAddr = arg0.get("startAddr");
                String endAddr = arg0.get("endAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
            }
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String blockAddr = arg0.get("blockAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
            }
        }

        @Override
        public void writeMifareULData(String arg0) {
            if (arg0 != null) {
                TRACE.d("writeMifareULData(String arg0):" + arg0);
            }
        }

        @Override
        public void verifyMifareULData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("verifyMifareULData(Hashtable<String, String> arg0):" + arg0.toString());
                String dataLen = arg0.get("dataLen");
                String pack = arg0.get("pack");
            }
        }

        @Override
        public void onGetSleepModeTime(String arg0) {
            if (arg0 != null) {
                TRACE.d("onGetSleepModeTime(String arg0):" + arg0.toString());

                int time = Integer.parseInt(arg0, 16);
            }
        }

        @Override
        public void onGetShutDownTime(String arg0) {
            if (arg0 != null) {
                TRACE.d("onGetShutDownTime(String arg0):" + arg0.toString());
            }
        }

        @Override
        public void onQposDoSetRsaPublicKey(boolean arg0) {
            TRACE.d("onQposDoSetRsaPublicKey(boolean arg0):" + arg0);
        }

        @Override
        public void onQposGenerateSessionKeysResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onQposGenerateSessionKeysResult(Hashtable<String, String> arg0):" + arg0.toString());
                String rsaFileName = arg0.get("rsaReginString");
                String enPinKeyData = arg0.get("enPinKey");
                String enKcvPinKeyData = arg0.get("enPinKcvKey");
                String enCardKeyData = arg0.get("enDataCardKey");
                String enKcvCardKeyData = arg0.get("enKcvDataCardKey");
            } else {
            }
        }

        @Override
        public void transferMifareData(String arg0) {
            TRACE.d("transferMifareData(String arg0):" + arg0.toString());
        }

        @Override
        public void onReturnRSAResult(String arg0) {
            TRACE.d("onReturnRSAResult(String arg0):" + arg0.toString());
        }

        @Override
        public void onRequestNoQposDetectedUnbond() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestNoQposDetectedUnbond()");
        }
    }



    private void sendMsg(String key, String result) {
        sendMsg(key,result,"");
    }

    private void sendMsg(String key, String result,String data) {
        Log.w("sendMsg", "sendMsg==" + key);
        WritableMap params = Arguments.createMap();
        params.putString("method", key);
        params.putString("result", result);
        params.putString("data", data);
        sendEvent(getReactApplicationContext(), "NativePosReminder", params);
    }

    @ReactMethod
    public void sendEvent(ReactContext reactContext,
                          String eventName,
                          @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);

    }
    private LocationManager lm;//【位置管理】
    private static final int BLUETOOTH_CODE = 100;
    private static final int LOCATION_CODE = 101;
    public void bluetoothRelaPer() {
        android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//if bluetooth is disabled, add one fix
            Intent enabler = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enabler);
        }
        lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//Location service is on
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
                // Request authorization
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE};
                        ActivityCompat.requestPermissions(getCurrentActivity(), list, BLUETOOTH_CODE);

                    }
                } else {
                    ActivityCompat.requestPermissions(getCurrentActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
                }
            } else {
//                Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "System detects that the GPS location service is not turned on", Toast.LENGTH_SHORT).show();

        }
    }
}
