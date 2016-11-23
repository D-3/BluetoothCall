package com.broadsense.newpine.bluetooth.core;

import android.text.TextUtils;

import com.broadsense.newpine.bluetooth.Utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import static com.android.internal.app.IntentForwarderActivity.TAG;

/**
 * Created by DeW on 2016/11/7.
 */

public class BluetoothManager {

    private ArrayList<BluetoothDevice> mPairedBluetoothDevices;
    private BluetoothDevice mPendingConnectDevice;
//    private ArrayList<BluetoothDevice> mPendingDelDevices;
//    private boolean delDeviceSuccess = true;

    private static BluetoothManager S_BLUETOOTH_MANAGER = new BluetoothManager();

    public static synchronized BluetoothManager getInstance(){
        return S_BLUETOOTH_MANAGER;
    }

    private BluetoothManager(){
        mPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
//        mPendingDelDevices = new ArrayList<BluetoothDevice>();
    }

    public BluetoothDevice getConnectedDevice(){
        for (int i=0; i<mPairedBluetoothDevices.size(); i++){
            BluetoothDevice bluetoothDevice = mPairedBluetoothDevices.get(i);
            if(bluetoothDevice.isConnected()){
                return bluetoothDevice;
            }
        }
        return null;
    }

    public ArrayList<BluetoothDevice> getParedDevices(){
        return mPairedBluetoothDevices;
    }

    public BluetoothDevice getDevice(String address){

        for (int i=0; i<mPairedBluetoothDevices.size(); i++){
            BluetoothDevice bluetoothDevice = mPairedBluetoothDevices.get(i);
            if(bluetoothDevice.getAddress().equals(address)){
                return bluetoothDevice;
            }
        }
        return null;
    }

    public void refreshDevice(BluetoothDevice device){
        mPairedBluetoothDevices.remove(device);
        mPairedBluetoothDevices.add(0, device);
    }

    public void addDevice(BluetoothDevice device){
        if(device != null)
            mPairedBluetoothDevices.add(0, device);
    }

    public synchronized void deleteDevice(String address){
        for (int i=0; i<mPairedBluetoothDevices.size(); i++){
            BluetoothDevice bluetoothDevice = mPairedBluetoothDevices.get(i);
            if(bluetoothDevice.getAddress().equals(address)){
                if(bluetoothDevice.isConnected()){
                    bluetoothDevice.disconnect();
                }
                mPairedBluetoothDevices.remove(bluetoothDevice);
            }
            if(mPendingConnectDevice != null &&
                    (bluetoothDevice.getAddress().equals(mPendingConnectDevice.getAddress()))){
                mPendingConnectDevice = null;
            }
        }
    }

//    public void downloadContact(){
//        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
//        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CONTACT));
//    }
//
//    public void downloadCallLog(){
//        ExtBtApp.getmExternalBluetoothManager().sendCommand("PBCPULLPB 1,1,0,0");
//        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DOWNLOADING_CALLLOG));
//    }

//    private void removePairdDevice(String address){
//        for (BluetoothDevice bluetoothDevice : mPairedBluetoothDevices){
//            if(bluetoothDevice.getAddress().equals(address)){
//                mPendingDelDevices.remove(bluetoothDevice);
//            }
//        }
//    }

//    public void deleteDevice(ArrayList<BluetoothDevice> devices){
//        mPendingDelDevices.addAll(devices);
//        delDeviceSuccess = true;
//        if(mPendingDelDevices.size() > 0){
//            if(isConnetedDevInPendingDelList()){
//                getConnectedDevice().disconnect();
//            }else{
//                startDeleteDevice();
//            }
//        }
//    }

//    public void onDeleteDeviceSuccess(String address){
//        removePendingDelDevice(address);
//        removePairdDevice(address);
//        if(mPendingDelDevices.size() > 0){
//            startDeleteDevice();
//        }else{
//            if(delDeviceSuccess){
//                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_SUCCESS));
//            }else{
//                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_FAIL));
//            }
//        }
//    }

//    private void removePendingDelDevice(String address){
//        for (BluetoothDevice bluetoothDevice: mPendingDelDevices){
//            if(bluetoothDevice.getAddress().equals(address)){
//                mPendingDelDevices.remove(bluetoothDevice);
//            }
//        }
//    }

//    public void onDeleteDevice(boolean success, String address){
//        if(success){
//            removePairdDevice(address);
//        }
//        delDeviceSuccess = success && delDeviceSuccess;
//
//        removePendingDelDevice(address);
//        if(mPendingDelDevices.size() > 0){
//            startDeleteDevice();
//        }else{
//            if(delDeviceSuccess){
//                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_SUCCESS));
//            }else{
//                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_FAIL));
//            }
//        }
//    }

//    public void startDeleteDevice(){
//        if(mPendingDelDevices.size() > 0){
//            ExtBtApp.getmExternalBluetoothManager().sendCommand("DPRD " + mPendingDelDevices.get(0).getAddress());
//        }
//    }

//    private boolean isConnetedDevInPendingDelList(){
//        for(BluetoothDevice bluetoothDevice : mPendingDelDevices){
//            if (bluetoothDevice.isConnected()){
//                return true;
//            }
//        }
//        return false;
//    }

//    public void disconnectDevice(BluetoothDevice bluetoothDevice){
//        if(bluetoothDevice != null){
//            bluetoothDevice.disconnect();
//        }
//    }

//    public void connectDevice(BluetoothDevice bluetoothDevice){
//        if(bluetoothDevice != null){
//            bluetoothDevice.connect();
//        }
//    }

    public void updateDevice(String address, String name){
        LogUtils.d(TAG, "updateDevice address=" + address + " name=" + name);
        BluetoothDevice device = getDevice(address);
        if(device != null && TextUtils.isEmpty(device.getName())) {
            device.setName(name);
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_UPDATE_DEVICE_INFO));
        }

    }

    public void setPendingConnectDevice(BluetoothDevice device){
        mPendingConnectDevice = device;
    }

    public BluetoothDevice getPendingConnectDevice(){
        return mPendingConnectDevice;
    }

//    public ArrayList<BluetoothDevice> getPendingDelDevices(){
//        return mPendingDelDevices;
//    }

}
