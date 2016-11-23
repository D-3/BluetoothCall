package com.broadsense.newpine.bluetooth.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.core.BluetoothDevice;
import com.broadsense.newpine.bluetooth.core.BluetoothEvent;
import com.broadsense.newpine.bluetooth.core.BluetoothManager;
import com.broadsense.newpine.bluetooth.ui.widget.CustomDialog;
import com.broadsense.newpine.bluetooth.ui.widget.WaveView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";

    private Context mContext;
    private RelativeLayout mContainer;

    private View mNoDeviceView;
    private Button mBtnAddDevice;

    private View mDeviceView;
    private ListView mDeviceListView;
    private DevicesListAdapter mDevicesListAdapter;
    private Button mBtnDelDevie;
    private Button mBtnCancelDel;
    private Button mBtnAddMore;
    private CustomDialog mConfirmDiscnctDialog;


    private View mSelfInfoView;
    private Button mBtnBack;
    private WaveView mWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_setting);

        mContainer = (RelativeLayout) findViewById(R.id.container);

        initViews();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "onBackPressed  ConfirmDiscnctDialog=" + mConfirmDiscnctDialog);
        if(BluetoothManager.getInstance().getConnectedDevice() == null){
            Intent close = new Intent("com.broadsense.newpine.closeapp");
            sendBroadcast(close);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWaveView != null){
            mWaveView.stopImmediately();
        }
        EventBus.getDefault().unregister(this);
    }

    private void initViews() {

        BluetoothManager bluetoothManager = BluetoothManager.getInstance();
        if (bluetoothManager.getParedDevices() == null || bluetoothManager.getParedDevices().size() == 0) {
            initNoDeviceView();
        } else {
            initDeviceView();
        }
    }

    private void initDeviceView(){
        mDeviceView = LayoutInflater.from(this).inflate(R.layout.paired_device_view, null);
        mDeviceListView = (ListView) mDeviceView.findViewById(R.id.paird_device_list);
        ArrayList<BluetoothDevice> paredDevices = BluetoothManager.getInstance().getParedDevices();
        mDevicesListAdapter = new DevicesListAdapter(this, paredDevices);
        mDeviceListView.setAdapter(mDevicesListAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice clickedDevice = (BluetoothDevice) mDevicesListAdapter.getItem(position);

                //正在连接时防止多次点触
                BluetoothDevice pendingConnectDevice = BluetoothManager.getInstance().getPendingConnectDevice();
                if(pendingConnectDevice != null){
                    return;
//                    if(pendingConnectDevice == clickedDevice){
//                        return;
//                    }else {
//                    }
                }

                BluetoothDevice connectedDevice = BluetoothManager.getInstance().getConnectedDevice();
                if(!clickedDevice.isConnected()){
                    BluetoothManager.getInstance().setPendingConnectDevice(clickedDevice);
                    if (connectedDevice != null) {
                        connectedDevice.disconnect();
                        //在此Activity和ReceiverService内接收断开后异步结果，再链接新的设备
                    } else {
                        //开始配对
                        BluetoothManager.getInstance().setPendingConnectDevice(clickedDevice);
                        ExtBtApp.getmExternalBluetoothManager().sendCommand("PAIR " + clickedDevice.getAddress());
                    }
                    BluetoothManager.getInstance().refreshDevice(clickedDevice);
                    mDevicesListAdapter.notifyDataSetChanged();
                }

            }
        });

        mBtnAddMore = (Button) mDeviceView.findViewById(R.id.btn_add_more);
        mBtnAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDisconnectConfirmDialog();
            }
        });

        mBtnDelDevie = (Button) mDeviceView.findViewById(R.id.btn_del_device);
        mBtnDelDevie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mDevicesListAdapter.isDelMode()){
                    mDevicesListAdapter.enterDelMode();
                    mBtnCancelDel.setVisibility(View.VISIBLE);
                    mBtnAddMore.setVisibility(View.GONE);
                }else{
                    if(mDevicesListAdapter.getSelectDevices().size()>0){
                        //删除选中设备
                        for (String address : mDevicesListAdapter.getSelectDevices()){
                            BluetoothManager.getInstance().deleteDevice(address);
                        }
//                        //显示正在删除
//                        if(mDeletingDialog == null){
//                            mDeletingDialog = new ProgressDialog(mContext);
//                            mDeletingDialog.setTitle("正在删除设备");
//                        }
//                        mDeletingDialog.show();
//                        //退出删除模式
//                        mDevicesListAdapter.exitDelMode();
//                        if(BluetoothManager.getInstance().getParedDevices().size() == 0 ){
//                            mContainer.removeAllViews();
//                            if(mNoDeviceView == null){
//                                initNoDeviceView();
//                            }else{
//                                mContainer.addView(mNoDeviceView);
//                            }
//                        }

                        //退出删除模式
                        mDevicesListAdapter.exitDelMode();
                        mBtnAddMore.setVisibility(View.VISIBLE);
                        mBtnCancelDel.setVisibility(View.GONE);
                        if (BluetoothManager.getInstance().getParedDevices().size() == 0) {
                            if (mDeviceView != null && mDeviceView.getParent() != null) {
                                mContainer.removeAllViews();
                            }
                            if (mNoDeviceView == null) {
                                initNoDeviceView();
                            } else {
                                mContainer.addView(mNoDeviceView);
                            }
                        } else {
                            if(mDeviceView != null){
                                mDevicesListAdapter.changeData(BluetoothManager.getInstance().getParedDevices());
                            }
                        }

                    }
                }
            }
        });

        mBtnCancelDel = (Button) mDeviceView.findViewById(R.id.btn_cancel);
        mBtnCancelDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevicesListAdapter.exitDelMode();
                mDevicesListAdapter.notifyDataSetChanged();
                mBtnCancelDel.setVisibility(View.GONE);
                mBtnAddMore.setVisibility(View.VISIBLE);
            }
        });

        mContainer.addView(mDeviceView);
        mDeviceView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void initNoDeviceView() {
        mNoDeviceView = LayoutInflater.from(this).inflate(R.layout.empty_device_view, null);
        mNoDeviceView.findViewById(R.id.btn_add_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelfInfoView == null) {
                    initSelfInfoView();
                }
                //TODO 这断代码好像没用
                else{
                    mContainer.removeAllViews();
                    mContainer.addView(mSelfInfoView);
                }
                //设置可见
                ExtBtApp.getmExternalBluetoothManager().sendCommand("SCAN 3");
            }
        });
//        mContainer.setLayoutParams(mLayoutParams);
        mContainer.addView(mNoDeviceView);
        mNoDeviceView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void initSelfInfoView(){
        mSelfInfoView = LayoutInflater.from(this).inflate(R.layout.self_info_view, null);
        mBtnBack = (Button) mSelfInfoView.findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BluetoothManager.getInstance().getParedDevices() != null
                        && BluetoothManager.getInstance().getParedDevices().size() > 0){
                    mContainer.removeAllViews();
                    if(mDeviceView == null){
                        initDeviceView();
                    }else{
                        mContainer.addView(mDeviceView);
                    }
                }else{
                    mContainer.removeAllViews();
                    mContainer.addView(mNoDeviceView);
                }

            }
        });
        mWaveView = (WaveView) mSelfInfoView.findViewById(R.id.v_wave);
//        mSelfInfoView.setLayoutParams(mLayoutParams);
        mContainer.addView(mSelfInfoView);
        mSelfInfoView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
    }

    private void showDisconnectConfirmDialog(){
        if(mConfirmDiscnctDialog == null){
            mConfirmDiscnctDialog = new CustomDialog(SettingActivity.this, getString(R.string.set_connecting_insert_new_devices_tip),
                    getString(R.string.btn_comfirm), getString(R.string.btn_cancel));
            mConfirmDiscnctDialog.setClicklistener(new CustomDialog.ClickListenerInterface() {
                @Override
                public void doConfirm() {

                    if(mSelfInfoView == null){
                        mContainer.removeAllViews();
                        initSelfInfoView();
                    }else{
                        mContainer.removeAllViews();
                        mContainer.addView(mSelfInfoView);
                    }
                    if(BluetoothManager.getInstance().getConnectedDevice() != null){
                        BluetoothManager.getInstance().getConnectedDevice().disconnect();
                    }else{
                        ExtBtApp.getmExternalBluetoothManager().sendCommand("SCAN 3");
                    }

                    mConfirmDiscnctDialog.dismiss();
                }

                @Override
                public void doCancel() {
                    mConfirmDiscnctDialog.dismiss();
                }
            });
//            mConfirmDiscnctDialog.setCanceledOnTouchOutside(true);
        }

        mConfirmDiscnctDialog.setCancelable(true);
        mConfirmDiscnctDialog.setCanceledOnTouchOutside(true);
        mConfirmDiscnctDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event) {
        LogUtils.d(TAG, "onEvent " + event);
        switch (event.getEvent()) {
            case BluetoothEvent.EVENT_PAIR_FAIL:
            case BluetoothEvent.EVENT_UPDATE_DEVICE_INFO:

                if(mDeviceView != null){
                    mDevicesListAdapter.notifyDataSetChanged();
                }
                break;
            case BluetoothEvent.EVENT_CONNECTED:
                mContainer.removeAllViews();
                if(mDeviceView != null){
                    mContainer.addView(mDeviceView);
                    mDevicesListAdapter.notifyDataSetChanged();
                }else {
                    initDeviceView();
                }
                break;

            case BluetoothEvent.EVENT_DISCONNET:
                if(mDeviceView != null){
                    if(mDevicesListAdapter.isDelMode()){
                        mDevicesListAdapter.exitDelMode();
                    }else{
                        mDevicesListAdapter.notifyDataSetChanged();
                    }
                }
                //设置可见
                ExtBtApp.getmExternalBluetoothManager().sendCommand("SCAN 3");
                break;

            case BluetoothEvent.EVENT_DEPAIR_FAIL:
            case BluetoothEvent.EVENT_DEPAIR_SUCCESS:

                if (BluetoothManager.getInstance().getParedDevices() == null
                        || BluetoothManager.getInstance().getParedDevices().size() == 0) {
                    if (mDeviceView != null && mDeviceView.getParent() != null) {
                        mBtnAddMore.setVisibility(View.VISIBLE);
                        mBtnCancelDel.setVisibility(View.GONE);
                        mContainer.removeAllViews();
                    }
                    if (mNoDeviceView == null) {
                        initNoDeviceView();
                    } else {
                        mContainer.addView(mNoDeviceView);
                    }
                } else {
                    if(mDeviceView != null){
                        mDevicesListAdapter.changeData(BluetoothManager.getInstance().getParedDevices());
                    }
                }
                break;
            default:
                break;
        }
    }


    private class DevicesListAdapter extends BaseAdapter {

        private ArrayList<BluetoothDevice> mPairedDevices;
        private LayoutInflater inflater;
        private boolean isDelMode = false;
        private HashSet<String> mSelectedDevices = new HashSet<String>();

        public DevicesListAdapter(Context context, ArrayList<BluetoothDevice> devices) {
            inflater = LayoutInflater.from(context);
            mPairedDevices = devices;
        }

        public void changeData(ArrayList<BluetoothDevice> devices) {
            mPairedDevices = devices;
            notifyDataSetChanged();
        }

        public boolean isDelMode(){
            return isDelMode;
        }

        public int getSelectCount(){
            return mSelectedDevices.size();
        }

        public HashSet<String> getSelectDevices(){
            return mSelectedDevices;
        }

        @Override
        public int getCount() {
            return mPairedDevices == null ? 0 : mPairedDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mPairedDevices == null ? null : mPairedDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.device_list_item, null);
                holder.deviceName = (TextView) view.findViewById(R.id.device_name);
                holder.connecting = (ProgressBar) view.findViewById(R.id.conneting_ind);
                holder.connected = (ImageView) view.findViewById(R.id.connect_ind);
                holder.del_check = (CheckBox) view.findViewById(R.id.cb_del_ind);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            final BluetoothDevice device = mPairedDevices.get(i);
            LogUtils.d(TAG, "getView  device=" + device);
            holder.deviceName.setText(device.getName());
            if (isDelMode) {
                holder.connected.setVisibility(View.GONE);
                holder.connecting.setVisibility(View.GONE);
                holder.del_check.setVisibility(View.VISIBLE);
                if (mSelectedDevices.contains(device.getAddress())) {
                    holder.del_check.setChecked(true);
                } else {
                    holder.del_check.setChecked(false);
                }
                holder.del_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (checked) {
                            mSelectedDevices.add(device.getAddress());
                        } else {
                            mSelectedDevices.remove(device.getAddress());
                        }
                    }
                });
            } else {
                holder.connected.setVisibility(device.isConnected() ? View.VISIBLE : View.GONE);
                if(BluetoothManager.getInstance().getPendingConnectDevice() != null
                        && BluetoothManager.getInstance().getPendingConnectDevice().getAddress().equals(device.getAddress())){
                    holder.connecting.setVisibility(View.VISIBLE);
                }else{
                    holder.connecting.setVisibility(View.GONE);
                }
                holder.del_check.setVisibility(View.GONE);
            }

            return view;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public void enterDelMode() {
            isDelMode = true;
            notifyDataSetChanged();
        }

        public void exitDelMode() {
            isDelMode = false;
            mSelectedDevices.clear();
            notifyDataSetChanged();
        }

        class ViewHolder {
            TextView deviceName;
            ProgressBar connecting;
            ImageView connected;
            CheckBox del_check;
        }
    }

}
