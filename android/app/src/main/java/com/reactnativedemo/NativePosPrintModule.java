package com.reactnativedemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.action.printerservice.PrintStyle;
import com.action.printerservice.barcode.Barcode1D;
import com.action.printerservice.barcode.Barcode2D;
import com.alibaba.fastjson.JSONObject;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativedemo.utils.TRACE;

public class NativePosPrintModule extends ReactContextBaseJavaModule {
    protected PrinterDevice mPrinter;
    public ReactApplicationContext reactApplicationContext;
    public Context context;
    public NativePosPrintModule(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
       this.reactApplicationContext = reactContext;
       context = reactContext;
    }
    @Override
    public boolean canOverrideExistingModule() {
        return true;
    }
    @NonNull
    @Override
    public String getName() {
        return "NativePosPrintModule";
    }

    @ReactMethod
    public void initPrint() {
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        if (mPrinter == null) {
            TRACE.d("Printer initialization failed");
            return;
        }
        TRACE.d("Printer initialization failed--2=="+mPrinter);
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
        mPrinter.setFooter(50);//unit is px
        if ("D30".equalsIgnoreCase(Build.MODEL) || isAppInstalled(context, UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
            TRACE.i("init printer with callkback==");
            mPrinter.initPrinter(context, new PrinterInitListener() {
                @Override
                public void connected() {
                    TRACE.i("init printer with callkback success==");
                    mPrinter.setPrinterTerminatedState(PrinterDevice.PrintTerminationState.PRINT_STOP);
                }
                @Override
                public void disconnected() {
                }
            });
        } else {
            TRACE.i("init printer ==");
            mPrinter.initPrinter(context);
        }

    }
    @ReactMethod
    class MyPrinterListener implements PrintListener {
        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            TRACE.d("printResult:" + b + "--status--" + s + "--resultType---" + resultType.getValue());
            if (!b && resultType.getValue() == -9) {
                TRACE.d("Battery level is below 10%, printing prohibit, please charge quickly! ");
            }
            sendMsg("printResult", "printResult: " +b + " status: " + s,resultType.toString());
            mPrinter.close();
        }
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
    @ReactMethod
    public void printTicket() throws RemoteException {
        TRACE.d("Printer initialization failed--3=="+mPrinter);
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 16));
        mPrinter.addText("Testing");
        mPrinter.addText("POS Signing of purchase orders");
        mPrinter.addText("MERCHANT COPY");
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.CENTER, 14));
        mPrinter.addText("- - - - - - - - - - - - - -");
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 14));
        mPrinter.addText("ISSUER Agricultural Bank of China");
        mPrinter.addText("ACQ 48873110");
        mPrinter.addText("CARD number.");
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 14));
        mPrinter.addText("6228 48******8 116 S");
        mPrinter.addText("TYPE of transaction(TXN TYPE)");
        mPrinter.addText("SALE");
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.CENTER, 14));
        mPrinter.addText("- - - - - - - - - - - - - -");
        mPrinter.addTexts(new String[]{"BATCH NO", "000043"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"VOUCHER NO", "000509"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"AUTH NO", "000786"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"DATE/TIME", "2010/12/07 16:15:17"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"REF NO", "000001595276"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"2014/12/07 16:12:17", ""}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addTexts(new String[]{"AMOUNT:", ""}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
        mPrinter.addText("RMB:249.00");
        mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.CENTER, 12));
        mPrinter.addText("- - - - - - - - - - - - - -");
        mPrinter.addText("Please scan the QRCode for getting more information: ");
        mPrinter.addBarCode(context, Barcode1D.CODE_128.name(), 400, 100, "123456", PrintLine.CENTER);
        mPrinter.addText("Please scan the QRCode for getting more information:");
        mPrinter.addQRCode(300, Barcode2D.QR_CODE.name(), "123456", PrintLine.CENTER);
        mPrinter.setFooter(150);
        mPrinter.print(context);

    }
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            TRACE.d("[PrinterManager] isAppInstalled ");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            TRACE.d("not found pacakge == " + e.toString());
            return false;
        }
    }
    public static final String UART_AIDL_SERVICE_APP_PACKAGE_NAME = "com.dspread.sdkservice";//新架构的service包名
}
