package com.broadsense.newpine.bluetooth.core;

import android.os.Message;
import android.text.TextUtils;

import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.db.CallLog;
import com.broadsense.newpine.bluetooth.db.Contact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import static com.broadsense.newpine.bluetooth.control.BluetoothService.COMMA;

/**
 * Created by DeW on 2016/11/8.
 */

public class PBAProfile extends StateMachine implements Profile {

    private static final String TAG = "PBAProfile";

    private ReadyState readyState          = new ReadyState();
    private ConnectingState connectingState     = new ConnectingState();
    private ConnectedState connectedState      = new ConnectedState();
    private DownloadingState downloadingState      = new DownloadingState();
    private DisconnectingState disconnectingState      = new DisconnectingState();

    private boolean isRegisted = false;

    private BluetoothDevice mBluetoothDevice;

    private boolean isQueryContactsIndex = false;
    private boolean isDownloadingContacts = false;
    private boolean isDownloadingIncomingCall = false;
    private boolean isDownloadingOutgoingCall = false;
    private boolean isDownloadingMissedCall = false;
    private boolean isContactDownload = false;
    private boolean isCallLogDownload = false;

    private LinkedBlockingQueue<Task> mPendingDownloadTask = new LinkedBlockingQueue<Task>();

//    private boolean isConnected = false;

    public PBAProfile(BluetoothDevice device){
        super("PBAProfileState");
        mBluetoothDevice = device;

        addState(readyState);
        addState(connectingState);
        addState(connectedState);
        addState(downloadingState);
        addState(disconnectingState);

        setInitialState(readyState);

        start();
    }

    public boolean isDownloadingContacts(){
        return isDownloadingContacts || isQueryContactsIndex;
    }

    public boolean isDownloadingCallLogs(){
        return isDownloadingIncomingCall
                || isDownloadingOutgoingCall
                || isDownloadingMissedCall;
    }

    public boolean isContactDownload(){
        return isContactDownload;
    }

    public boolean isCallLogDownload(){
        return isCallLogDownload;
    }

    @Override
    public boolean isConnected() {
        LogUtils.d(TAG, "current state = " + getCurrentState());
        return getCurrentState() != readyState
                && getCurrentState() != connectingState;
//        return isConnected;
    }

    public void connect(){
        LogUtils.d(TAG, "connect");
        if (mBluetoothDevice == null) {
            return;
        }
        if(getCurrentState() != readyState){
            return;
        }

        regeisterAtResponseListener();
        transitionTo(connectingState);

        //发送连接命令
        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCCONN " + mBluetoothDevice.getAddress());
//        sendMessage(obtainMessage(0, "AT+B PBCCONN " + mBluetoothDevice.getAddress()));
    }

    public void disconnect(){
        //发送断开指令
        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCDISC");
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
    public void onEvent(String response) {
        LogUtils.d(TAG,  mBluetoothDevice.getName() + " onEvent " + response);

        if (TextUtils.isEmpty(response)) return;
        //断开连接时取消监听
        if (response.contains("AT-B PBCSTAT 5")) {
            unregeisterAtResponseListener();
        }
        //转发给状态机处理
        sendMessage(obtainMessage(0, response));
    }

    public void jump2ConnectedState(){
        regeisterAtResponseListener();
        transitionTo(connectedState);
    }

    public void reset(){
        unregeisterAtResponseListener();
        transitionTo(readyState);
    }

    public void downloadContact(){
        if(getCurrentState() != connectedState){
            mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CONTACT));
            if(!isDownloadingContacts && !isQueryContactsIndex){
                sendMessage(obtainMessage(0, "excute_task_now"));
            }
        }

//        if(isDownloadingContacts){
//            //添加到待处理
//            mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CONTACT));
//        }else{
//            ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
//            isDownloadingContacts = true;
//            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CONTACT));
//        }
    }

    public void downloadCallLog(){
        if(getCurrentState() != connectedState){
            mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CALLLOG));
            if(!isDownloadingIncomingCall && !isDownloadingOutgoingCall && !isDownloadingMissedCall){
                sendMessage(obtainMessage(0, "excute_task_now"));
            }
        }

//        if(isDownloadingIncomingCall || isDownloadingOutgoingCall || isDownloadingMissedCall){
//            //添加到待处理
//            mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CALLLOG));
//        }else{
//            ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,2,500,0");
//            isDownloadingIncomingCall = true;
//            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CALLLOG));
//        }
    }

    private void startDownloadCallLogInner(){
        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,2,500,0");
        isDownloadingIncomingCall = true;
        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CALLLOG));
    }

    private void startDownloadContactsInner(){
        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
        isQueryContactsIndex = true;
//        isDownloadingContacts = true;
        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CONTACT));
    }


    private void excuteDownloadTask(){
        Task task = mPendingDownloadTask.poll();

        LogUtils.d(TAG, "excuteDownloadTask：" + task);
        if(task != null){
            switch (task.getType()){
                case Task.DOWNLOAD_CALLLOG:
//                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,2,500,0");
//                    isDownloadingIncomingCall = true;
//                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CALLLOG));
                    startDownloadCallLogInner();
                    break;
                case Task.DOWNLOAD_CONTACT:
//                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
//                    isDownloadingContacts = true;
//                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CONTACT));
                    startDownloadContactsInner();
                    break;
            }
        }
    }

    private void resetDownloadTasks(){
        mPendingDownloadTask.clear();
        mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CONTACT));
        mPendingDownloadTask.add(new Task(Task.DOWNLOAD_CALLLOG));
    }

    /**
     * 初始状态 1
     */
    private class ReadyState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "初始状态");
            isQueryContactsIndex = false;
            isDownloadingIncomingCall = false;
            isDownloadingOutgoingCall = false;
            isDownloadingMissedCall = false;
            isDownloadingContacts = false;

            isContactDownload = false;
            isCallLogDownload = false;

            resetDownloadTasks();

            mBluetoothDevice.onProfileStateChange();

            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;

            }
            return super.processMessage(msg);
        }
    }

    /**
     * 连接中状态 status=2
     */
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
                //正在断开
                if(response.contains("AT-B PBCSTAT 5")){
                    transitionTo(disconnectingState);
                }
                //断开连接
                else if(response.contains("AT-B PBCSTAT 1")){
                    transitionTo(readyState);
                }
                //连接中
                else if(response.contains("AT-B PBCSTAT 2")){

                }
                //已连接
                else if(response.contains("AT-B PBCSTAT 3")){
                    transitionTo(connectedState);
                }
                //连接失败
                else if(response.contains("AT-B PBCCONN 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 已连接状态 status=3
     */
    private class ConnectedState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "已连接");

            //更新蓝牙设备状态
            mBluetoothDevice.onProfileStateChange();

            //准备同步电话数据，先查询索引
//            ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
//            isDownloadingContacts = true;
            excuteDownloadTask();

            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "已连接 " + response);
                //正在断开
                if(response.contains("AT-B PBCSTAT 5")){
                    if(isQueryContactsIndex){
                        isQueryContactsIndex = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    else if(isDownloadingContacts){
                        isContactDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                    }
                    else if(isDownloadingIncomingCall || isDownloadingOutgoingCall || isDownloadingMissedCall){
                        isCallLogDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    transitionTo(disconnectingState);
                }
                //有时候不会受到PBC断开的指令，为防止PBC不复位的bug，当HFP断开时，同时将PBC置为初始状态。
                //（当HFP断开时，PBC肯定已断开）
                else if(response.contains("AT-B HFSTAT 1")){
                    if(isQueryContactsIndex){
                        isQueryContactsIndex = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    else if(isDownloadingContacts){
                        isContactDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                    }
                    else if(isDownloadingIncomingCall || isDownloadingOutgoingCall || isDownloadingMissedCall){
                        isCallLogDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    transitionTo(readyState);
                }
                //返回初始状态
                else if(response.contains("AT-B PBCSTAT 1")){
                    if(isQueryContactsIndex){
                        isQueryContactsIndex = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    else if(isDownloadingContacts){
                        isContactDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                    }
                    else if(isDownloadingIncomingCall || isDownloadingOutgoingCall || isDownloadingMissedCall){
                        isCallLogDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    transitionTo(readyState);
                }
                //查询失败
                else if(response.contains("AT-B PBCPULLPB 1")){
                    LogUtils.d(TAG, "同步失败");
                    if(isQueryContactsIndex){
                        isQueryContactsIndex = false;
                        isContactDownload = false;
                        isCallLogDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    else if(isDownloadingContacts){
                        isDownloadingContacts = false;
                        isContactDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL));
                    }
                    else if(isDownloadingMissedCall || isDownloadingOutgoingCall || isDownloadingIncomingCall){
                        isDownloadingMissedCall = false;
                        isDownloadingOutgoingCall = false;
                        isDownloadingIncomingCall = false;
                        isCallLogDownload = false;
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL));
                    }
                    //执行下一个任务
                    excuteDownloadTask();
                    //TODO 添加重试机制
                }

                //查询成功
                else if(response.contains("AT-B PBCPARSEDATAIND")){

                    String[] data = response.split(COMMA);
                    LogUtils.i(TAG, "data length=" + data.length);
                    //索引
                    if(data.length == 4){

                        //延迟500毫秒获取联系人，防止查询索引结束标志未及时上报，导致联系同步异常
                        String contactCountStr = data[0].substring(21, data[0].length());
                        sendMessageDelayed(0,0,Integer.parseInt(contactCountStr),
                                "start_sync_contacts", 500);
                    }
                    //联系人
                    else if(data.length == 5
                                && data[0].equals("AT-B PBCPARSEDATAIND 1")){
                        LogUtils.d(TAG, "添加联系人 " + data[2]);

                        //保存联系人数据
                        Contact contact = new Contact();
                        contact.number = data[2];
                        contact.name = data[4];
                        mBluetoothDevice.addContact(contact);
                    }
                    //通话记录
                    else if(data.length == 5
                            && data[0].equals("AT-B PBCPARSEDATAIND 0")){
                        LogUtils.d(TAG, "添加通话记录 " + data[2]);

                        //保存通话记录数据
                        CallLog callLog = new CallLog();
                        callLog.name = data[4];
                        callLog.number = data[2];
                        callLog.time = formatTime(data[3]);
                        mBluetoothDevice.addCallLog(callLog);
                    }
                }

                //上报数据完成
                else if(response.contains("AT-B PBCPULLCMTIND")){
                    LogUtils.d(TAG, "同步完成标记 "
                        + " isQueryContactsIndex=" + isQueryContactsIndex
                        + " downloadingContacts=" + isDownloadingContacts
                        + " downloadingOutgoingCall=" + isDownloadingOutgoingCall
                        + " downloadingMissedCall=" + isDownloadingMissedCall
                        + " downloadingIncomingCall=" + isDownloadingIncomingCall);

                    //查询索引结束
                    if(isQueryContactsIndex){
                        isQueryContactsIndex = false;
                    }
                    //等待2秒后通知同步完成，因为有时候接收到完成命令后仍有数据上报
                    else if(isDownloadingContacts){
                        sendMessageDelayed(0, "sync_contacts_cmt", 1000);
                    }
                    else if(isDownloadingIncomingCall){
                        sendMessageDelayed(0, "sync_in_call_cmt", 1000);
                    }
                    else if(isDownloadingMissedCall){
                        sendMessageDelayed(0, "sync_miss_call_cmt", 1000);
                    }
                    else if(isDownloadingOutgoingCall){
                        sendMessageDelayed(0, "sync_out_call_cmt", 1000);
                    }

                }
                else if(response == "start_sync_contacts"){
                    LogUtils.d(TAG, "开始同步联系人");

                    //TODO 将所有同步数据的业务封装成Task以任务队列的形式统一管理，
                    //TODO 通过sendMessage()统一调度触发，excuteDownloadTask()统一执行
                    //开始同步联系人
                    int contactCount = msg.arg2;
                    ExtBtApp.getmExternalBluetoothManager()
                            .sendCommand("PBCPULLPB 1,1," + contactCount +",0");
                    isDownloadingContacts = true;
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CONTACT));
                }
                else if(response == "sync_contacts_cmt"){
                    LogUtils.d(TAG, "同步联系人完成");

                    // 同步联系人成功
                    isDownloadingContacts = false;
                    isContactDownload = true;
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CONTACT_SUCCESS));

//                    // 开始同步通话记录
//                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,2,500,0");
//                    isDownloadingIncomingCall = true;
//                    //通知开始同步通话记录
//                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CALLLOG));

                    //继续下一个同步任务
                    excuteDownloadTask();

                }
                else if(response == "sync_in_call_cmt"){
                    LogUtils.d(TAG, "同步已接电话完成");

                    //TODO 将所有同步数据的业务封装成Task以任务队列的形式统一管理，
                    //TODO 通过sendMessage()统一调度触发，excuteDownloadTask()统一执行
                    //同步外拨电话
                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,3,500,0");
                    isDownloadingOutgoingCall = true;
                    isDownloadingIncomingCall = false;
                }
                else if(response == "sync_out_call_cmt"){
                    LogUtils.d(TAG, "同步外拨电话完成");

                    //TODO 将所有同步数据的业务封装成Task以任务队列的形式统一管理，
                    //TODO 通过sendMessage()统一调度触发，excuteDownloadTask()统一执行
                    //同步未接电话
                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,4,500,0");
                    isDownloadingMissedCall = true;
                    isDownloadingOutgoingCall = false;
                }
                else if(response == "sync_miss_call_cmt"){
                    LogUtils.d(TAG, "同步通话记录完成");
                    isDownloadingMissedCall = false;
                    mBluetoothDevice.sortCallLogs();
                    //通知通话记录同步完成
                    isCallLogDownload = true;
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_SUCCESS));

                    //继续下一个同步任务
                    excuteDownloadTask();
                }
                else if(response == "excute_task_now"){
                    excuteDownloadTask();
                }
                //下载中
                else if(response.contains("AT-B PBCSTAT 4")){
                    transitionTo(downloadingState);
                }

            }
            return super.processMessage(msg);
        }
    }

    private long formatTime(String time){
        String time_ = time.replace("T", "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        try {
            Date date = simpleDateFormat.parse(time_);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 同步状态 status=4
     *
     * ！！！！！       似乎不会进入这种状态         ！！！！！
     *
     * ！！！！！ 所以同步联系人在connected状态中处理 ！！！！！
     *
     */
    private class DownloadingState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "同步中");
            //TODO 通知界面刷新同步状态
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            if(msg != null){
                String response = (String) msg.obj;
                LogUtils.d(TAG, "同步中 " + response);
                //断开
                if(response.contains("AT-B PBCSTAT 5")){
                    transitionTo(disconnectingState);
                }
                //返回已连接
                if(response.contains("AT-B PBCSTAT 3")){
                    transitionTo(connectedState);
                }
                //返回初始
                else if(response.contains("AT-B PBCSTAT 1")){
                    transitionTo(readyState);
                }
            }
            return super.processMessage(msg);
        }
    }

    /**
     * 断开连接状态 status=5
     */
    private class DisconnectingState extends State {
        @Override
        public void enter() {
            LogUtils.d(TAG, "断开");
            //进入此状态后不再改变，所以一到此状态后我们重置该状态机
            transitionTo(readyState);

//            mBluetoothDevice.onProfileStateChange();
            super.enter();
        }

        @Override
        public boolean processMessage(Message msg) {
            return super.processMessage(msg);
        }
    }

    private class Task{
        public static final int DOWNLOAD_CONTACT = 0;
        public static final int DOWNLOAD_CALLLOG = 1;

        Task(int type){
            mType = type;
        }

        private int mType;
        public int getType(){
            return mType;
        }

        @Override
        public String toString() {
            return super.toString() + "  type=" + mType;
        }
    }

}
