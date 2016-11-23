package com.broadsense.newpine.bluetooth.core;

import android.os.Message;
import android.text.TextUtils;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by DeW on 2016/11/8.
 */

public class HFProfile extends StateMachine implements Profile {

    private static final String TAG = "HFProfile";

    private ReadyState                          readyState                        = new ReadyState();
    private ConnectingState                     connectingState                   = new ConnectingState();
    private ConnectedState                      connectedState                    = new ConnectedState();
    private IncomingCallState                   incomingCallState                 = new IncomingCallState();
    private OutgoingCallState                   outgoingCallState                 = new OutgoingCallState();
    private ActiveCallState                     activeCallState                   = new ActiveCallState();
    private ActiveCallIncomingCallState         activeCallIncomingCallState       = new ActiveCallIncomingCallState();
    private TWActiveState                       tWActiveState                     = new TWActiveState();
    private TWActiveCallReleaseOneActiveState   tWActiveCallReleaseOneActiveState = new TWActiveCallReleaseOneActiveState();

    private boolean isRegisted = false;
//    private Context mContext;
    private BluetoothDevice mBluetoothDevice;

    public HFProfile(BluetoothDevice device){
        super("HFProfileState");
//        mContext = context;
        mBluetoothDevice = device;

        addState(readyState);
        addState(connectingState);
        addState(connectedState);
        addState(incomingCallState);
        addState(outgoingCallState);
        addState(activeCallState);
        addState(activeCallIncomingCallState);
        addState(tWActiveState);
        addState(tWActiveCallReleaseOneActiveState);

        setInitialState(readyState);

        start();
    }

    public void connect(){
        LogUtils.d(TAG, "connect");

        if (mBluetoothDevice == null) {
            LogUtils.d(TAG, "Fail to connect, BluetoothDevice is null! ");
            return;
        }

        regeisterAtResponseListener();
        transitionTo(connectingState);

        //发送连接命令
//        ExtBtApp.getmExternalBluetoothManager().sendCommand("HFCONN " + mBluetoothDevice.getAddress());
//        sendMessage(obtainMessage(1, "AT+B HFCONN " + mBluetoothDevice.getAddress()));
    }

    public void disconnect(){
        //发送断开命令
        ExtBtApp.getmExternalBluetoothManager().sendCommand("HFDISC");
    }

    public void jump2ConnectedState(){
        regeisterAtResponseListener();
//        mContext.registerReceiver(mATresponseReceiver, new IntentFilter("com.android.EXTERNAL_BLUETOOTH"));
        transitionTo(connectedState);
    }

    @Override
    public boolean isConnected() {
        return getCurrentState() !=  readyState && getCurrentState() != connectingState;
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
        LogUtils.d(TAG,  mBluetoothDevice.getName() + " onEvent " + response);

        if(TextUtils.isEmpty(response)) return;
        //断开连接时取消监听
        if(response.contains("AT-B HFSTAT 1")){
            unregeisterAtResponseListener();
        }
        //转发给状态机处理
        sendMessage(obtainMessage(0, response));

//        if(response.contains("AT-B HFCONN 0")){
//            String[] data = response.split(COMMA);
//            BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
//            if (bluetoothDevice == null) {
//                bluetoothDevice = new BluetoothDevice(null, data[1]);
//                BluetoothManager.getInstance().addDevice(bluetoothDevice);
//                jump2ConnectedState();
//            }else if(!bluetoothDevice.isHFPConnected()){
//                jump2ConnectedState();
//            }
//        }
//        else if(getEvent() != readyState){
//            handleAtResponse(response);
//        }
    }

//    private BroadcastReceiver mATresponseReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if(intent != null && intent.getStringExtra(EXTRA_AT_RESULT) != null ){
//                handleAtResponse(intent.getStringExtra(EXTRA_AT_RESULT));
//            }
//        }
//    };

//    private void handleAtResponse(String response){
//        if(TextUtils.isEmpty(response)) return;
//
//        if(response.contains("AT-B HFSTAT 1")){
//            //取消监听
//            EventBus.getDefault().unregister(HFProfile.this);
//        }
//        sendMessage(obtainMessage(0, response));
//    }

    /**
     * 初始状态
     */
    private class ReadyState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "初始状态");
            mBluetoothDevice.onProfileStateChange();
//            try{
//                mContext.unregisterReceiver(mATresponseReceiver);
//            }catch (IllegalArgumentException e){
//                LogUtils.e(e);
//            }
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            return super.processMessage(msg);
        }
    }

    /**
     * 连接中
     */
    private class ConnectingState extends State {

        @Override
        public void enter() {
            LogUtils.d(TAG, "连接中");

//            //注册广播接收器接收 AT 指令响应结果
//            if(mContext != null){
//                mContext.registerReceiver(mATresponseReceiver, new IntentFilter("com.android.EXTERNAL_BLUETOOTH"));
//            }
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "连接中 " + response);

                //已连接
                if(response.contains("AT-B HFSTAT 3")){
                    transitionTo(connectedState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }


//                //连接结果
//                else if(response.contains("AT-B HFCONN")){
//                    String status = response.substring(13, 14);
//                    if(status != null){
//                        try{
//                            int status_int = Integer.parseInt(status);
//                            LogUtils.d(TAG, "HFCONN status=" + status);
////                            if(status_int == 0){
////                                //已连接
////                                transitionTo(connectedState);
////                            }else{
////                                transitionTo(readyState);
////                            }
//
//                            if(status_int != 0){
//                                //已连接
//                                transitionTo(readyState);
//                            }
//                        }catch (NumberFormatException ex){
//                            LogUtils.e(ex.toString());
//                        }
//                    }
//
//                }

            }
            return super.processMessage(msg);
        }
    }

    /**
     * 已连接
     */
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

                //来电
                if(response.contains("AT-B HFSTAT 4")){
                    transitionTo(incomingCallState);
                }
                //去电
                else if(response.contains("AT-B HFSTAT 5")){
                    transitionTo(outgoingCallState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }

//                //DTMF拨号，去电
//                else if(response.contains("AT-B HFDIAL 0")){
//                    transitionTo(outgoingCallState);
//                }
//                //断开连接
//                else if(response.contains("AT-B HFDISC 0")){
//                    transitionTo(readyState);
//                }

                //接听电话
    //        else if(response.contains("AT-B HFANSW")){
    //
    //        }
    //        //拒接，挂断电话
    //        else if(response.contains("AT-B HFCHUP")){
    //
    //        }
    //        //DTMF拨号
    //        else if(response.contains("AT-B HFDIAL")){
    //
    //        }
    //        //车机语音切换
    //        else if(response.contains("AT-B HFCTRS")){
    //
    //        }
    //        //查询当前通话号码
    //        else if(response.contains("AT-B HFCLCC")){
    //
    //        }
    //        //设置通话音量
    //        else if(response.contains("AT-B HFSVGS")){
    //
    //        }
    //        //获取当前通话音量
    //        else if(response.contains("AT-B HFGVGS")){
    //
    //        }
    //        //获取当前mic音量
    //        else if(response.contains("AT-B HFGVGM")){
    //
    //        }
    //        //设置当前mic音量
    //        else if(response.contains("AT-B HFSVGM")){
    //
    //        }
    //        //设置静音
    //        else if(response.contains("AT-B HFMUTE")){
    //
    //        }
    //        //hfp基本状态
    //        else if(response.contains("AT-B HFSTAT")){
    //
    //        }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 来电
     */
    private class IncomingCallState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "来电");
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "来电 " + response);

                //通话中
                if(response.contains("AT-B HFSTAT 6")){
                    transitionTo(activeCallState);
                }
                //已连接
                else if(response.contains("AT-B HFSTAT 3")){
                    transitionTo(connectedState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }

//                //接听电话 0表示成功
//                if(response.contains("AT-B HFANSW 0")){
//                    transitionTo(activeCallState);
//                }
//                //断开连接
//                else if(response.contains("AT-B HFDISC 0")){
//                    transitionTo(readyState);
//                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 呼出
     */
    private class OutgoingCallState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "呼出");
            //TODO 通知界面更新
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "呼出 " + response);

                //通话中
                if(response.contains("AT-B HFSTAT 6")){
                    transitionTo(activeCallState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 通话中
     */
    private class ActiveCallState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "通话中");
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "通话中 " + response);

                //通话中，有新的来电
                if(response.contains("AT-B HFSTAT 7")){
                    transitionTo(activeCallIncomingCallState);
                }
                //挂断，返回已连接
                else if(response.contains("AT-B HFSTAT 3")){
                    transitionTo(connectedState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }




                //断开连接
//                if(response.contains("AT-B HFDISC 0")){
//                    transitionTo(readyState);
//                }
                //车机语音切换
                else if(response.contains("AT-B HFCTRS")){

                }
                //查询当前通话号码
                else if(response.contains("AT-B HFCCIN")){
                    //TODO 更新号码
                }
                //设置通话音量
                else if(response.contains("AT-B HFSVGS")){
                    //
                }
                //设置当前mic音量
                else if(response.contains("AT-B HFSVGM")){

                }
                //设置静音
                else if(response.contains("AT-B HFMUTE")){

                }

            }
            return super.processMessage(msg);
        }
    }

    /**
     * 通话中，新的来电
     */
    private class ActiveCallIncomingCallState extends State {
        @Override
        public void enter() {
            //TODO 通知界面有新的来电
            LogUtils.d(TAG, "通话中新来电");
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "通话中新来电 " + response);

                //挂断一路
                if(response.contains("AT-B HFSTAT 10")){
                    transitionTo(tWActiveCallReleaseOneActiveState);
                }
                //接听
                else if(response.contains("AT-B HFSTAT 8")){
                    transitionTo(tWActiveState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 通话中，保留当前通话，接听新的来电
     */
    private class TWActiveState extends State {
        @Override
        public void enter() {
            //TODO 通知界面展示接听界面
            LogUtils.d(TAG, "通话中接听新来电");
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "通话中接听新来电 " + response);

                //挂断一路
                if(response.contains("AT-B HFSTAT 10")){
                    transitionTo(tWActiveCallReleaseOneActiveState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 通话中，且此时保留有一路通话，此时挂掉一路通话
     */
    private class TWActiveCallReleaseOneActiveState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "通话中挂断一路");
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "通话中挂断一路 " + response);

                //返回正在通话
                if(response.contains("AT-B HFSTAT 6")){
                    transitionTo(activeCallState);
                }
                //断开连接
                else if(response.contains("AT-B HFSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

}
