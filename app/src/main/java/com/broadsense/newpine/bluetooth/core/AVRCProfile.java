package com.broadsense.newpine.bluetooth.core;

import android.os.Message;
import android.text.TextUtils;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by DeW on 2016/11/8.
 */

public class AVRCProfile extends StateMachine implements Profile {

    private static final String TAG = "AVRCProfile";

    private ReadyState      readyState          = new ReadyState();
    private ConnectingState connectingState     = new ConnectingState();
    private ConnectedState  connectedState      = new ConnectedState();

    private boolean isRegisted = false;

    private BluetoothDevice mBluetoothDevice;

    public AVRCProfile(BluetoothDevice device){
        super("AVCRProfileState");
        mBluetoothDevice = device;

        addState(readyState);
        addState(connectingState);
        addState(connectedState);

        setInitialState(readyState);

        start();
    }

    @Override
    public boolean isConnected() {
        return getCurrentState() == connectedState;
    }

    public void connect(){
        LogUtils.d(TAG, "connect");

        if (mBluetoothDevice == null) {
            return;
        }

        regeisterAtResponseListener();
        transitionTo(connectingState);

        //发送连接命令
//        sendMessage(obtainMessage(0, "AT+B AVRCPCONN " + mBluetoothDevice.getAddress()));
    }

    public void disconnect(){
        //无主动断开指令
    }

    public void reset(){
        unregeisterAtResponseListener();
        transitionTo(readyState);
    }

    public void jump2ConnectedState(){
        regeisterAtResponseListener();
        transitionTo(connectedState);
    }

    private void regeisterAtResponseListener(){
        if(!isRegisted){
            EventBus.getDefault().register(this);
            isRegisted = true;
        }
    }

    private void unregeisterAtResponseListener(){
        if(isRegisted){
            EventBus.getDefault().unregister(this);
            isRegisted = false;
        }
    }

    @Subscribe
    public void onEvent(String response){
        LogUtils.d(TAG, mBluetoothDevice.getName() + " onEvent " + response);
        if (TextUtils.isEmpty(response)) return;
        //断开连接时取消监听
        if (response.contains("AT-B AVRCPSTAT 1")) {
            unregeisterAtResponseListener();
        }
        //转发给状态机处理
        sendMessage(obtainMessage(0, response));
    }


    private class ReadyState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "初始状态");
            mBluetoothDevice.onProfileStateChange();
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            return super.processMessage(msg);
        }
    }

    private class ConnectingState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "连接中");

            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "连接中 " + response);
                if(response.contains("AT-B AVRCPSTAT 3")){
                    transitionTo(connectedState);
                }
                //断开连接
                else if(response.contains("AT-B AVRCPSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    private class ConnectedState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "已连接");
            mBluetoothDevice.onProfileStateChange();
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "已连接 " + response);
                //断开连接
                if(response.contains("AT-B AVRCPSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

}
