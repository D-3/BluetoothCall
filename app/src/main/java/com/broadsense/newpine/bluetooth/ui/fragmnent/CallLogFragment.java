package com.broadsense.newpine.bluetooth.ui.fragmnent;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.core.BluetoothDevice;
import com.broadsense.newpine.bluetooth.core.BluetoothEvent;
import com.broadsense.newpine.bluetooth.core.BluetoothManager;
import com.broadsense.newpine.bluetooth.db.CallLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CallLogFragment extends Fragment {
    private static final String TAG = "CallLogFragment";

    private ListView mCallLogListView;
    private CallLogListAdapter mCallLogListAdapter;
    private LinearLayout mEmptyView;
    private TextView mCallLogsSyncStatus;
    private Button mRetryBtn;

    public CallLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calllogs, null);
        mCallLogListView = (ListView) rootView.findViewById(R.id.call_log_list);
        mCallLogListAdapter = new CallLogListAdapter(getActivity(), null);
        mCallLogListView.setAdapter(mCallLogListAdapter);
        mCallLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CallLog call = (CallLog) mCallLogListAdapter.getItem(position);
                if(call != null){
                    ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFDIAL + "0," + call.number);
                }
            }
        });

        mEmptyView = (LinearLayout)rootView.findViewById(R.id.calllog_sync_status);
        mCallLogsSyncStatus = (TextView) rootView.findViewById(R.id.tv_calllog_sync_status);
        mRetryBtn = (Button) rootView.findViewById(R.id.btn_retry);
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getConnectedDevice();
                if(bluetoothDevice != null){
                    bluetoothDevice.downloadCallLog();
                }
            }
        });
        mCallLogListView.setEmptyView(mEmptyView);

        updateViews();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        getActivity().registerReceiver(mLogChangeReceiver, new IntentFilter("com.android.action.call_log_change"));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event){
        LogUtils.d(TAG, "onEvent " + event);
        switch (event.getEvent()){
            case BluetoothEvent.EVENT_CONNECTED:
//                mCallLogsSyncStatus.setText("正在通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showLoadingCallLog();
                break;
            case BluetoothEvent.EVENT_DISCONNET:
//                mCallLogsSyncStatus.setText("没有通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showNoCallLog();
                mCallLogListAdapter.setData(null);
                mCallLogListAdapter.notifyDataSetChanged();
                break;
            case BluetoothEvent.EVENT_DOWNLOADING_CALLLOG:
//                mCallLogsSyncStatus.setText("正在同步通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showLoadingCallLog();
                break;
            case BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_SUCCESS:
                updateViews();
                break;
            case BluetoothEvent.EVENT_DOWNLOAD_CALLLOG_FAIL:
//                mCallLogsSyncStatus.setText("通话记录同步失败，请重新同步");
//                mRetryBtn.setVisibility(View.VISIBLE);
                showDownloadCallLogFail();
                mCallLogListAdapter.setData(null);
                mCallLogListAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    private void showLoadingCallLog(){
        mCallLogsSyncStatus.setText("正在同步通话记录");
        mRetryBtn.setVisibility(View.GONE);
    }

    private void showNoCallLog(){
        mCallLogsSyncStatus.setText("没有通话记录");
        mRetryBtn.setVisibility(View.GONE);
    }

    private void showDownloadCallLogFail(){
        mCallLogsSyncStatus.setText("没有通话记录");
        mRetryBtn.setVisibility(View.GONE);
    }

    private void updateViews() {
        BluetoothDevice device = BluetoothManager.getInstance().getConnectedDevice();

        if(device != null){
            ArrayList<CallLog> callLogs = device.getCallLogs();
            if(device.isDownloadingCallLogs() || device.isDownloadingContacts()){
                LogUtils.d(TAG, "updateViews 正在同步通话记录");
//                mCallLogsSyncStatus.setText("正在同步通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showLoadingCallLog();
            }
            else if(!device.isCallLogDownload()){
                LogUtils.d(TAG, "updateViews " + (device.isCallLogDownload() ? "calllog downloaded" : "calllog download fail") );
//                mCallLogsSyncStatus.setText("通话记录同步失败，请重新同步");
//                mRetryBtn.setVisibility(View.VISIBLE);
                showDownloadCallLogFail();
                mCallLogListAdapter.setData(null);
                mCallLogListAdapter.notifyDataSetChanged();
            }
            else if(callLogs != null && callLogs.size() > 0){
                LogUtils.d(TAG, "updateViews 有通话记录");
                mCallLogListAdapter.setData(callLogs);
                mCallLogListAdapter.notifyDataSetChanged();
//                mCallLogsSyncStatus.setText("没有通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showNoCallLog();
            }
            else if(callLogs == null || callLogs.size() == 0){
                LogUtils.d(TAG, "updateViews 没有通话记录");
                mCallLogListAdapter.setData(null);
                mCallLogListAdapter.notifyDataSetChanged();
//                mCallLogsSyncStatus.setText("没有通话记录");
//                mRetryBtn.setVisibility(View.GONE);
                showNoCallLog();
            }
        }else {
            LogUtils.d(TAG, "updateViews 无设备");
            mCallLogListAdapter.setData(null);
            mCallLogListAdapter.notifyDataSetChanged();
//            mCallLogsSyncStatus.setText("没有通话记录");
//            mRetryBtn.setVisibility(View.GONE);
            showNoCallLog();
        }


    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    class CallLogListAdapter extends BaseAdapter {

        public LayoutInflater inflater;
        public Context mContext;
        private List<CallLog> mCallLogs;

        public CallLogListAdapter(Context context, List<CallLog> callLogs) {
            inflater = LayoutInflater.from(context);
            this.mCallLogs = callLogs;
        }

        public void setData(List<CallLog> callLogs){
            mCallLogs = callLogs;
        }

        @Override
        public int getCount() {
            return mCallLogs == null ? 0 : mCallLogs.size();
        }

        @Override
        public Object getItem(int i) {
            return mCallLogs == null ? null : mCallLogs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = inflater.inflate(R.layout.calllog_list_item, null);
                holder.phonename = (TextView) view.findViewById(R.id.call_log_item_number);
                holder.time = (TextView) view.findViewById(R.id.call_log_item_time);
                holder.type = (ImageView) view.findViewById(R.id.call_log_status_Image);
                holder.number = (ImageView)view.findViewById(R.id.call_log_item_call);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            CallLog callLog = mCallLogs.get(i);

            holder.phonename.setText(callLog.name);
            holder.time.setText(formatTime(callLog.time));
            if (callLog.type == CallLog.TYPE_INCOMING) {
                holder.type.setBackgroundResource(R.drawable.ico_incoming_call);
            } else if (callLog.type == CallLog.TYPE_OUTGOING) {
                holder.type.setBackgroundResource(R.drawable.ico_outgoing_call);
            } else if (callLog.type == CallLog.TYPE_MISSED) {
                holder.type.setBackgroundResource(R.drawable.ico_incoming_call_cancelled);
            }

            final String callnum = callLog.number;
//            mCallLogListView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFDIAL + "0," + callnum);
////                    Intent intent = new Intent(ExtBtApp.getContext(), CallActivity.class);
////                    intent.putExtra("num", callnum);
////                    startActivity(intent);
//                }
//            });
            return view;
        }

        class ViewHolder {
            TextView phonename;
            TextView time;
            ImageView type;
            ImageView number;
        }
    }

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf2 = new SimpleDateFormat("MM月dd日 HH:mm");
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    private String formatTime(long time) {
        String formatted;
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int today = now.get(Calendar.DAY_OF_YEAR);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(time);
        calendar.setTime(date);
        int timeYear = calendar.get(Calendar.YEAR);
        int timeDay = calendar.get(Calendar.DAY_OF_YEAR);
        if (year == timeYear && today == timeDay) {
            formatted = sdf1.format(date);
        } else if (year == timeYear && today == timeDay + 1) {
            formatted = "昨天" + sdf1.format(date);
        } else if (year == timeYear) {
            formatted = sdf2.format(date);
        } else {
            formatted = sdf3.format(date);
        }
        return formatted;
    }

}
