package com.broadsense.newpine.bluetooth;

import android.app.Application;
import android.content.Intent;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.control.BluetoothService;


public class ExtBtApp extends Application {

//    private static Context mContext;

    private static ExternalBluetoothManager mExternalBluetoothManager;

    @Override
    public void onCreate() {
        super.onCreate();

//        mContext = getApplicationContext();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(mContext);

        mExternalBluetoothManager = (ExternalBluetoothManager)getSystemService("external_bt_service");
        startService(new Intent(getApplicationContext(), BluetoothService.class));

        mExternalBluetoothManager.sendCommand("SLDN 4G智能后视镜-蓝牙电话");
    }

//    public static Context getContext() {
//        return mContext;
//    }

    public synchronized static ExternalBluetoothManager getmExternalBluetoothManager() {
        return mExternalBluetoothManager;
    }
}