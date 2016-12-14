package com.broadsense.newpine.bluetooth.control;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;

import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.core.BluetoothDevice;
import com.broadsense.newpine.bluetooth.core.BluetoothEvent;
import com.broadsense.newpine.bluetooth.core.BluetoothManager;
import com.broadsense.newpine.bluetooth.ui.activity.InCallActivity;
import com.broadsense.newpine.launcher.IWorkService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";

    private static Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        bindLauncher();

        LogUtils.e("registerReceiver   EXTERNAL_BLUETOOTH");
        registerReceiver(mReceiver, new IntentFilter("com.android.EXTERNAL_BLUETOOTH"));

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(mReceiver);

        EventBus.getDefault().unregister(this);
    }

    public static final String COMMA = ",";

    @Subscribe
    public void onEvent(BluetoothEvent event){
        switch (event.getEvent()){
            case BluetoothEvent.EVENT_DISCONNET:
                lighthandler.sendMessage(Message.obtain(lighthandler, ISSTOPED));
                break;
            case BluetoothEvent.EVENT_CONNECTED:
                lighthandler.sendMessage(Message.obtain(lighthandler, ISWORKING));
                Toast.makeText(this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                break;
            case BluetoothEvent.EVENT_PAIR_FAIL:
                Toast.makeText(this, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getStringExtra("result") != null){
                String result = intent.getStringExtra("result");
                LogUtils.e("receiver:" + result);

                if(result != null){

                    if(result.contains("AT-B PAIR")) {
                        String pairResult = result.substring(10, 11);
                        String[] data = result.split(COMMA);
                        //配对成功
                        if(pairResult.equals("0")){
                            BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
                            if (bluetoothDevice == null) {
                                LogUtils.d(TAG, "Get bluetoothDevice is null, create one!");
                                bluetoothDevice = new BluetoothDevice(null, data[1]);
                                BluetoothManager.getInstance().addDevice(bluetoothDevice);
                            }
                            bluetoothDevice.connect();
                            //获取设备名
//                        ExtBtApp.getmExternalBluetoothManager().sendCommand("GRDN " + data[1]);
                        }

                        //配对失败
                        else{
                            if(BluetoothManager.getInstance().getPendingConnectDevice() != null
                                    && BluetoothManager.getInstance().getPendingConnectDevice().getAddress().equals(data[1])){
                                BluetoothManager.getInstance().setPendingConnectDevice(null);

                                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_PAIR_FAIL));
                            }
                        }

                    }

                    //更新设备名称
                    else if(result.contains("AT-B GRDN")){
                        String data[] = result.split(COMMA);
                        BluetoothManager.getInstance().updateDevice(data[1], data[2]);
                    }

                    //当接下列广播时，若当前设备改协议没有建立连接，则调整设备以及协议状态
                    else if(result.contains("AT-B A2DPCONN 0")){
                        String[] data = result.split(COMMA);
                        BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
                        if (bluetoothDevice == null) {
                            bluetoothDevice = new BluetoothDevice(null, data[1]);
                            BluetoothManager.getInstance().addDevice(bluetoothDevice);
                            bluetoothDevice.changeA2DPtoConnectedState();
                        }else if(!bluetoothDevice.isA2DPConnected()){
                            bluetoothDevice.changeA2DPtoConnectedState();
                        }
                    }

                    else if(result.contains("AT-B AVRCPCONN 0")){
                        String[] data = result.split(COMMA);
                        BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
                        if (bluetoothDevice == null) {
                            bluetoothDevice = new BluetoothDevice("", data[1]);
                            BluetoothManager.getInstance().addDevice(bluetoothDevice);
                            bluetoothDevice.changeAVRCPtoConnectedState();
                        }else if(!bluetoothDevice.isAVRCPConnected()){
                            bluetoothDevice.changeAVRCPtoConnectedState();
                        }
                    }

                    else if(result.contains("AT-B HFCONN")){
                        String[] data = result.split(COMMA);
                        String hfpResult = result.substring(12, 13);

                        LogUtils.d(TAG, "hf连接 设备:" + data[1]  + " result=" + result
                                +"  待连设备:" + BluetoothManager.getInstance().getPendingConnectDevice());

                        if(hfpResult.equals("0")){
                            BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
                            if (bluetoothDevice == null) {
                                bluetoothDevice = new BluetoothDevice(null, data[1]);
                                BluetoothManager.getInstance().addDevice(bluetoothDevice);
                                bluetoothDevice.changeHFPtoConnectedState();
                            }else if(!bluetoothDevice.isHFPConnected()){
                                bluetoothDevice.changeHFPtoConnectedState();
                            }
                        }else{
                            //连接失败，一般出现在手机蓝牙未打开情况下，配对不上
                            if(BluetoothManager.getInstance().getPendingConnectDevice() != null
                                    && BluetoothManager.getInstance().getPendingConnectDevice().getAddress().equals(data[1])){
                                BluetoothManager.getInstance().setPendingConnectDevice(null);

                                EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_PAIR_FAIL));
                            }
                        }
                    }
                    else if(result.contains("AT-B PBCCONN 0")){
                        String[] data = result.split(COMMA);
                        BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getDevice(data[1]);
                        if (bluetoothDevice == null) {
                            bluetoothDevice = new BluetoothDevice("", data[1]);
                            BluetoothManager.getInstance().addDevice(bluetoothDevice);
                            bluetoothDevice.changePBAPtoConnectedState();
                        }else if(!bluetoothDevice.isPBAPConnected()){
                            bluetoothDevice.changePBAPtoConnectedState();
                        }
                    }
                    else if(result.contains("AT-B DPRD 0")){
                        String[] data = result.split(COMMA);
                        BluetoothManager.getInstance().deleteDevice(data[1]);
                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_SUCCESS));
                    }
//                    else if(result.contains("AT-B DPRD")){
//                        String[] data = result.split(COMMA);
//                        int dprdResult = result.charAt(12);
//                        BluetoothManager.getInstance().onDeleteDevice(dprdResult == 0, data[1]);
//
//                        BluetoothManager.getInstance().deleteDevice(data[1]);
//                        EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.EVENT_DEPAIR_SUCCESS));
//                    }

                    else if(result.contains("AT-B HFCCIN")){
//                        String callStatus = result.substring(13, 14);
                        Intent startInCallUi = new Intent(BluetoothService.this, InCallActivity.class);
                        startInCallUi.putExtra("data", result);
                        startInCallUi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startInCallUi);
                    }
                    else{
                        EventBus.getDefault().post(result);
                    }
                }
            }

        }
    };

    IWorkService mIWorkService;
    MyConn conn;

    private static final int ISWORKING = 0;
    private static final int ISSTOPED = 1;

    private void bindLauncher(){
        Intent intent = new Intent();
        intent.setAction("com.service.workservice.REMOTE");
        Intent imintent = createExplicitFromImplicitIntent(BluetoothService.this, intent);
        if (imintent != null) {
            Intent connectintent = new Intent(imintent);
            conn = new MyConn();
            bindService(connectintent, conn, BIND_AUTO_CREATE);
        }
    }

    class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.i(TAG, "绑定服务");
            mIWorkService = IWorkService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIWorkService = null;
        }

    }

    private void work(boolean isWi) {
        try {
            LogUtils.i(TAG, "work" + isWi);
            if(mIWorkService != null){
                mIWorkService.isWork("com.broadsense.newpine.bluetooth", isWi);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }


    private Handler lighthandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            LogUtils.i(TAG, "handleMessage " + msg);
            if (mIWorkService == null) {
                lighthandler.sendEmptyMessageDelayed(msg.what, 1000);
                return;
            }
            switch (msg.what) {
                case ISWORKING:
                    work(true);
                    break;
                case ISSTOPED:
                    work(false);
                    break;
            }
        }
    };

}
