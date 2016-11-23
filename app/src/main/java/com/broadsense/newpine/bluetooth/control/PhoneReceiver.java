package com.broadsense.newpine.bluetooth.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneReceiver extends BroadcastReceiver {

    public static String ACTION_CALL_OUTGOING = "android.intent.action.PHONE_CALL_OUTGOING";
    public static String ACTION_CALL_INCOMING_ANSWER = "android.intent.action.PHONE_CALL_INCOMING_ANSWER";
    public static String ACTION_CALL_INCOMING_DROPPED = "android.intent.action.PHONE_CALL_INCOMING_DROPPED";
    public static String ACTION_CALL_INCOMING = "android.intent.action.PHONE_CALL_INCOMING";

    public PhoneReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (context == null) return;
        if (intent.getAction().equals(ACTION_CALL_OUTGOING)){
//            LogUtils.i("call : " + intent.getStringExtra("num"));
//            ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFDIAL + "0," + intent.getStringExtra("num"));
//            Intent call = new Intent(context, CallActivity.class);
//            call.putExtra("num", intent.getStringExtra("num"));
//            call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(call);
        }
    }
}
