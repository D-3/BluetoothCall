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

    private static BluetoothManager S_BLUETOOTH_MANAGER = new BluetoothManager();

    public static synchronized BluetoothManager getInstance(){
        return S_BLUETOOTH_MANAGER;
    }

    private BluetoothManager(){
        mPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
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

}
