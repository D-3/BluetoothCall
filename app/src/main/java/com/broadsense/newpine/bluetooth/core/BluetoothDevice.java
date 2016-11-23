package com.broadsense.newpine.bluetooth.core;

import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.db.CallLog;
import com.broadsense.newpine.bluetooth.db.Contact;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DeW on 2016/11/7.
 */

public class BluetoothDevice {
    private static final String TAG = "BluetoothDevice";

    private ArrayList<Contact> mContacts;
    private ArrayList<CallLog> mCallLogs;
    private HFProfile mHFProfile;
    private A2DProfile mA2DProfile;
    private AVRCProfile mAVRCProfile;
    private PBAProfile mPBAProfile;

    private String mName;
    private String mAddress;

    private boolean isConnected = false;

    public BluetoothDevice(String name, String address){
        mName = name;
        mAddress = address;
        mHFProfile = new HFProfile(this);
        mA2DProfile = new A2DProfile(this);
        mAVRCProfile = new AVRCProfile(this);
        mPBAProfile = new PBAProfile(this);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isHFPConnected(){
        return mHFProfile.isConnected();
    }

    public boolean isA2DPConnected(){
        return mA2DProfile.isConnected();
    }

    public boolean isAVRCPConnected(){
        return mAVRCProfile.isConnected();
    }

    public boolean isPBAPConnected(){
        return mPBAProfile.isConnected();
    }

    public void changeHFPtoConnectedState(){
        LogUtils.d(TAG, "changeHFPtoConnectedState");
        mHFProfile.jump2ConnectedState();
    }

    public void changeA2DPtoConnectedState(){
        LogUtils.d(TAG, "changeA2DPtoConnectedState");
        mA2DProfile.jump2ConnectedState();
    }

    public void changeAVRCPtoConnectedState(){
        LogUtils.d(TAG, "changeAVRCPtoConnectedState");
        mAVRCProfile.jump2ConnectedState();
    }

    public void changePBAPtoConnectedState(){
        LogUtils.d(TAG, "changePBAPtoConnectedState");
        mPBAProfile.jump2ConnectedState();
    }

    public void connect(){
        mHFProfile.connect();
        mA2DProfile.connect();
        mAVRCProfile.connect();
//        mPBAProfile.connect();
    }

    public void disconnect(){
        mHFProfile.disconnect();
        mA2DProfile.disconnect();
//        mAVRCProfile.disconnect();
//        mPBAProfile.disconnect();
    }


    public synchronized void onProfileStateChange(){
        if(mA2DProfile == null || mAVRCProfile == null
                || mPBAProfile == null || mHFProfile == null){
            return;
        }

//        //有时候不会受到PBC断开的指令，为防止PBC不复位的bug，当HFP断开时，同时将PBC置为初始状态。
//        //（当HFP断开时，PBC肯定已断开）
//        if(!mHFProfile.isConnected()){
//            mPBAProfile.reset();
//        }

        LogUtils.d(TAG, "onProfileStateChange    a2dp=" + mA2DProfile.isConnected() + "   avrcp=" + mAVRCProfile.isConnected()
                + "    hfp="+ mHFProfile.isConnected() + "    pbc=" + mPBAProfile.isConnected());

        //有协议连接时，表示设备已连接
        if(!isConnected && (mA2DProfile.isConnected()
                || mAVRCProfile.isConnected()
                || mHFProfile.isConnected()
                || mPBAProfile.isConnected())){
            isConnected = true;

            //清除待连接设备
            if(BluetoothManager.getInstance().getPendingConnectDevice() != null
                && BluetoothManager.getInstance().getPendingConnectDevice().getAddress().equals(mAddress)){
                BluetoothManager.getInstance().setPendingConnectDevice(null);
            }

            //通知设备已链接
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_CONNECTED));
        }

        //链接都断开时，表示设备已断开
        if(!mA2DProfile.isConnected()
                && !mAVRCProfile.isConnected()
                && !mHFProfile.isConnected()
                //TODO 总是获取到mPBAProfile的状态总是连接
//                && !mPBAProfile.isConnected()
                ){
            isConnected = false;

            clearContact();
            clearCallLog();

            //如果有待连接的设备，开始连接
            if(BluetoothManager.getInstance().getPendingConnectDevice() != null){
                    ExtBtApp.getmExternalBluetoothManager().sendCommand("PAIR "
                            + BluetoothManager.getInstance().getPendingConnectDevice().getAddress());
            }

            LogUtils.d(TAG, mName + "已断开，发送设备断开广播");

            //通知设备已断开
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DISCONNET));
        }

        //当其他链接都已建立后，发起PBC链接
        if(mA2DProfile.isConnected()
                && mAVRCProfile.isConnected()
                && mHFProfile.isConnected()
                && !mPBAProfile.isConnected()){
            mPBAProfile.connect();
//            ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCCONN " + mAddress);
        }
    }

    public boolean isDownloadingContacts(){
        return mPBAProfile.isDownloadingContacts();
    }

    public boolean isDownloadingCallLogs(){
        return mPBAProfile.isDownloadingCallLogs();
    }

//    public void onDisconnected(){
//        if(!mHFProfile.isConnected()
//            && !mA2DProfile.isConnected()
////            && !mAVRCProfile.isConnected()
//            && !mPBAProfile.isConnected()){
//            //所有连接都已断开时表示该设备已断开
//            //TODO 通知界面刷新
//        }
//    }

    public void downloadContact(){
        mPBAProfile.downloadContact();
    }

    public void downloadCallLog(){
        mPBAProfile.downloadCallLog();
    }

    public boolean isContactDownload(){
        LogUtils.d(TAG, "isContactDownload" + mPBAProfile.isContactDownload());
        return mPBAProfile.isContactDownload();
    }

    public boolean isCallLogDownload(){
        LogUtils.d(TAG, "isCallLogDownload" + mPBAProfile.isCallLogDownload());
        return mPBAProfile.isCallLogDownload();
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){ return mName; }

    public String getAddress(){
        return mAddress;
    }

    public synchronized ArrayList<Contact> getContacts(){
        return mContacts;
    }

    public synchronized ArrayList<CallLog> getCallLogs(){
        return mCallLogs;
    }

    public synchronized void addContact(Contact contactValue){
        if (mContacts == null){
            mContacts = new ArrayList<Contact>();
        }
        mContacts.add(contactValue);
    }

    public synchronized void addCallLog(CallLog call){
        if (mCallLogs == null){
            mCallLogs = new ArrayList<CallLog>();
        }
        mCallLogs.add(call);
    }

    public void clearContact(){
        if(mContacts != null) mContacts.clear();
    }

    public void clearCallLog(){
        if(mCallLogs != null) mCallLogs.clear();
    }

    public synchronized void sortCallLogs(){
        if(mCallLogs != null){
            Collections.sort(mCallLogs);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "    content={" +
                "\"name:\"" + mName +
                "  \"address:\"" + mAddress +
                "  \"calllogs:\"" + (mCallLogs == null ? "null" : mCallLogs.size()) +
                "  \"contacts:\"" + (mContacts == null ? "null" : mContacts.size()) +
                "  \"isConnected:\"" + isConnected() +
                "  \"HFPconnected:\"" + mHFProfile.isConnected() +
                "  \"PBCconnected:\"" + mPBAProfile.isConnected() +
                "  \"A2DPconnected:\"" + mA2DProfile.isConnected() +
                "  \"AVCRPconnected:\"" + mAVRCProfile.isConnected() +
                "}";
    }
}
