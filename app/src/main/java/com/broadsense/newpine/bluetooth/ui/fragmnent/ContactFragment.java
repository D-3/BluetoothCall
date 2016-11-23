package com.broadsense.newpine.bluetooth.ui.fragmnent;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.broadsense.newpine.bluetooth.db.Contact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {
    private static final String TAG = "ContactFragment";

    private ListView mContactsListView;
    private ContactListAdapter mContactListAdapter;
    private TextView mContactsSyncStatus;
    private LinearLayout mContactsSyncStatusView;
    private Button mRetryBtn;

    List<Contact> list;

    public ContactFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_contacts, null);
        mContactsListView = (ListView) rootView.findViewById(R.id.contact_list);
        mContactListAdapter = new ContactListAdapter(getActivity(), null);
        mContactsListView.setAdapter(mContactListAdapter);
        mContactsSyncStatus = (TextView) rootView.findViewById(R.id.tv_contact_sync_status);
        mContactsSyncStatusView = (LinearLayout) rootView.findViewById(R.id.contact_sync_status);
        mRetryBtn = (Button) rootView.findViewById(R.id.btn_retry);
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice bluetoothDevice = BluetoothManager.getInstance().getConnectedDevice();
                if(bluetoothDevice != null){
                    bluetoothDevice.downloadContact();
                }
            }
        });
        mContactsListView.setEmptyView(mContactsSyncStatusView);

        updateViews();

        return rootView;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BluetoothEvent event){
        LogUtils.d(TAG, "onEvent " + event);
        switch (event.getEvent()){
            case BluetoothEvent.EVENT_CONNECTED:
                mContactsSyncStatus.setText("正在同步联系人");
                mRetryBtn.setVisibility(View.GONE);
                break;
            case BluetoothEvent.EVENT_DISCONNET:
                mContactsSyncStatus.setText("没有联系人");
                mRetryBtn.setVisibility(View.GONE);
                mContactListAdapter.setData(null);
                mContactListAdapter.notifyDataSetChanged();
                break;
            case BluetoothEvent.EVENT_DOWNLOADING_CONTACT:
                mContactsSyncStatus.setText("正在同步联系人");
                mRetryBtn.setVisibility(View.GONE);
                break;
            case BluetoothEvent.EVENT_DOWNLOAD_CONTACT_SUCCESS:
                updateViews();
                break;
            case BluetoothEvent.EVENT_DOWNLOAD_CONTACT_FAIL:
                mContactsSyncStatus.setText("联系人同步失败，请重新同步");
                mRetryBtn.setVisibility(View.VISIBLE);
                mContactListAdapter.setData(null);
                mContactListAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }
    }

    private void updateViews() {
        BluetoothDevice device = BluetoothManager.getInstance().getConnectedDevice();

        if(device != null){
            if(device.isDownloadingContacts()){
                LogUtils.d(TAG, "updateViews 正在同步联系人");
                mContactsSyncStatus.setText("正在同步联系人");
                mRetryBtn.setVisibility(View.GONE);
            }
            else if(!device.isContactDownload()){
                LogUtils.d(TAG, "updateViews " + (device.isContactDownload() ? "contact downloaded" : "contact download fail") );
                mContactsSyncStatus.setText("联系人同步失败，请重新同步");
                mRetryBtn.setVisibility(View.VISIBLE);
                mContactListAdapter.setData(null);
                mContactListAdapter.notifyDataSetChanged();
            }
            else if(device.getContacts() != null && device.getContacts().size() > 0){
                LogUtils.d(TAG, "updateViews 有联系人");
                LogUtils.d(TAG, "contact size=" + device.getContacts().size());
                mContactListAdapter.setData(device.getContacts());
                mContactListAdapter.notifyDataSetChanged();
            }else{
                LogUtils.d(TAG, "updateViews 无联系人");
                mContactsSyncStatus.setText("没有联系人");
                mRetryBtn.setVisibility(View.GONE);
                mContactListAdapter.setData(null);
                mContactListAdapter.notifyDataSetChanged();
            }
        }else {
            LogUtils.d(TAG, "updateViews 无设备");
            mContactListAdapter.setData(null);
            mContactListAdapter.notifyDataSetChanged();
            mContactsSyncStatus.setText("没有联系人");
            mRetryBtn.setVisibility(View.GONE);
        }

    }


    class ContactListAdapter extends BaseAdapter {

        public LayoutInflater inflater;
        private List<Contact> mContacts;

        public ContactListAdapter(Context context, List<Contact> list) {
            inflater = LayoutInflater.from(context);
            this.mContacts = list;
        }

        public void setData(List<Contact> contacts){
            mContacts = contacts;
        }

        @Override
        public int getCount() {
            return mContacts == null ? 0 : mContacts.size();
        }

        @Override
        public Object getItem(int i) {
            return mContacts == null ? null : mContacts.get(i);
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
                view = inflater.inflate(R.layout.contact_list_item, null);
                holder.phonename = (TextView) view.findViewById(R.id.contact_item_number);
                holder.call = (ImageView)view.findViewById(R.id.contact_item_call);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            Contact contact = mContacts.get(i);

            String name = contact.name;
            if (!name.equals("")) {
                holder.phonename.setText(contact.name + " " + contact.number);
            } else {
                holder.phonename.setText(contact.number);
            }
            final String callnum = contact.number;
            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFDIAL + "0," + callnum);
                }
            });
            return view;
        }

        class ViewHolder {
            TextView phonename;
            ImageView call;
        }
    }

}
