package com.broadsense.newpine.bluetooth.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.core.BluetoothDevice;
import com.broadsense.newpine.bluetooth.core.BluetoothEvent;
import com.broadsense.newpine.bluetooth.core.BluetoothManager;
import com.broadsense.newpine.bluetooth.ui.fragmnent.CallLogFragment;
import com.broadsense.newpine.bluetooth.ui.fragmnent.ContactFragment;
import com.broadsense.newpine.bluetooth.ui.fragmnent.DialFragment;
import com.broadsense.newpine.bluetooth.ui.widget.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

//    /**
//     * The {@link android.support.v4.view.PagerAdapter} that will provide
//     * fragments for each of the sections. We use a
//     * {@link FragmentPagerAdapter} derivative, which will keep every
//     * loaded fragment in memory. If this becomes too memory intensive, it
//     * may be best to switch to a
//     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
//     */
//    private static SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;
    private ImageButton mButtonSettings;
    private TextView mBtDeviceName;
    private CustomDialog mNoDeviceDialog;

    private Context mContext;
    private TabLayout mTabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        setContentView(R.layout.activity_main);

//        // Add for test
//        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, TestActivity.class));
//            }
//        });
//        // Add for test

        initView();

        EventBus.getDefault().register(this);


        //监听系统关闭应用广播
        registerReceiver(closeAppReceiver, new IntentFilter("com.broadsense.newpine.closeapp"));
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView() {

        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.setupWithViewPager(mViewPager);
        mTabs.setTabMode(TabLayout.MODE_FIXED);
        mTabs.getTabAt(0).setIcon(R.drawable.tab_calllog_bg);
        mTabs.getTabAt(1).setIcon(R.drawable.tab_contact_bg);
        mTabs.getTabAt(2).setIcon(R.drawable.tab_dialer_bg);

        mButtonSettings = (ImageButton) findViewById(R.id.setting_button);
        mButtonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
        mBtDeviceName = (TextView) findViewById(R.id.bluetooth_name_text);
        updateDeviceName();

    }

    @Override
    protected void onResume() {
        if (BluetoothManager.getInstance().getConnectedDevice() == null) {
            showNoCnctDeviceDialog();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(closeAppReceiver);
    }

    private void updateDeviceName(){
        BluetoothDevice connectedDevice = BluetoothManager.getInstance().getConnectedDevice();
        LogUtils.d(TAG, "updateDeviceName --> device: " + connectedDevice);
        if(connectedDevice == null){
            mBtDeviceName.setText("");
        }else{
            mBtDeviceName.setText(connectedDevice.getName());
        }
    }

    private void dismissNCnctoDeviceDialog(){
        if(mNoDeviceDialog != null){
            mNoDeviceDialog.dismiss();
        }
    }

    private void showNoCnctDeviceDialog(){
        if(mNoDeviceDialog == null){
            mNoDeviceDialog = new CustomDialog(MainActivity.this, getString(R.string.bluetooth_not_connect),
                    getString(R.string.btn_comfirm), getString(R.string.btn_cancel));
            mNoDeviceDialog.setClicklistener(new CustomDialog.ClickListenerInterface() {
                @Override
                public void doConfirm() {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    mNoDeviceDialog.dismiss();
                }

                @Override
                public void doCancel() {
                    mNoDeviceDialog.cancel();
                }
            });
        }
        mNoDeviceDialog.setCancelable(true);
        mNoDeviceDialog.setCanceledOnTouchOutside(true);
        mNoDeviceDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        mNoDeviceDialog.show();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event){
        switch (event.getEvent()){
            case BluetoothEvent.EVENT_UPDATE_DEVICE_INFO:
                updateDeviceName();
                break;
            case BluetoothEvent.EVENT_CONNECTED:
                updateDeviceName();
                dismissNCnctoDeviceDialog();
                break;
            case BluetoothEvent.EVENT_DISCONNET:
                updateDeviceName();
                showNoCnctDeviceDialog();
                break;
            default:
                break;
        }
    }


    BroadcastReceiver closeAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new CallLogFragment();
                case 1:
                    return new ContactFragment();
                case 2:
                    return new DialFragment();
                default:
                    throw new IllegalArgumentException("position is not applicable!");
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            String pageTitle = "";
//            switch (position) {
//                case 0:
//                    pageTitle = "通话记录";
//                    break;
//                case 1:
//                    pageTitle = "联系人";
//                    break;
//                case 2:
//                    pageTitle = "拨号";
//                    break;
//            }
//            return pageTitle;
//        }

    }
}

