package com.broadsense.newpine.bluetooth;

import android.app.Application;
import android.content.Intent;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.Utils.CrashHandler;
import com.broadsense.newpine.bluetooth.control.BluetoothService;

public class ExtBtApp extends Application {

    private static ExternalBluetoothManager mExternalBluetoothManager;


    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);

        mExternalBluetoothManager = (ExternalBluetoothManager)getSystemService("external_bt_service");
        mExternalBluetoothManager.sendCommand("SLDN 4G智能后视镜-蓝牙电话");

        startService(new Intent(getApplicationContext(), BluetoothService.class));
    }

    public synchronized static ExternalBluetoothManager getmExternalBluetoothManager() {
        return mExternalBluetoothManager;
    }

}