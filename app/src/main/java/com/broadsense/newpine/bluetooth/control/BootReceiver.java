package com.broadsense.newpine.bluetooth.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.ExtBtApp;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

//        ExtBtApp.getmExternalBluetoothManager().openExternalBluetooth();
//        ExtBtApp.getContext().startService(new Intent(ExtBtApp.getContext(), BluetoothService.class));
//        LogUtils.i("ExternalBluetoothEnabled:" + ExtBtApp.getmExternalBluetoothManager().isExternalBluetoothEnabled());
        ExtBtApp.getmExternalBluetoothManager().sendCommand("SCAN 3");

        ExtBtApp.getmExternalBluetoothManager().sendCommand("HFDISC");
        ExtBtApp.getmExternalBluetoothManager().sendCommand("A2DPDISC");
        ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_PBCDISC);

//        BluetoothService.clearPairlist();
        ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_GPRL);

        Intent launchVoicerIntent = new Intent("com.android.dr.mlauncher.LAUNCHER_START");
        launchVoicerIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(launchVoicerIntent);
    }
}
