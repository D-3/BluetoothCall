package com.broadsense.newpine.bluetooth.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.control.BluetoothService;

/**
 * Created by DeW on 2016/11/9.
 */

public class TestActivity extends Activity {
    private EditText mLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test);

        startService(new Intent(this, BluetoothService.class));


        findViewById(R.id.btn_set_discover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtBtApp.getmExternalBluetoothManager().sendCommand("SCAN 3");
            }
        });

        final EditText editText = (EditText) findViewById(R.id.et_cmd);

        findViewById(R.id.btn_send_cmd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtBtApp.getmExternalBluetoothManager().sendCommand(editText.getText().toString());
            }
        });

        mLog = (EditText) findViewById(R.id.et_address);

//        findViewById(R.id.btn_disc_hf).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ExtBtApp.getmExternalBluetoothManager().sendCommand("HFDISC");
//            }
//        });
//
//        findViewById(R.id.btn_disc_pbc).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCDISC");
//            }
//        });
//
//        findViewById(R.id.btn_disc_a2dp).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ExtBtApp.getmExternalBluetoothManager().sendCommand("A2DPDISC");
//            }
//        });

        super.onCreate(savedInstanceState);

        registerReceiver(mReceiver, new IntentFilter("com.android.EXTERNAL_BLUETOOTH"));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getStringExtra("result") != null) {
                String result = intent.getStringExtra("result");
                LogUtils.i("receiver:" + result);
                StringBuilder builder = new StringBuilder();
                if(mLog.getText() == null){
                    builder.append(result).append("\n");
                }else{
                    builder.append(mLog.getText()).append(result).append("\n");
                }
                mLog.setText(builder.toString());
            }
        }
    };

}
