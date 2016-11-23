package com.broadsense.newpine.bluetooth.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.core.BluetoothDevice;
import com.broadsense.newpine.bluetooth.core.BluetoothManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataProvider extends ContentProvider {

    private static String CONTACT_URI = "content://com.broadsense.newpine.bluetooth/contact";
    private static String STATUS_URI = "content://com.broadsense.newpine.bluetooth/status";

    public DataProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        String url = uri.toString();
        LogUtils.i("DataProvider url:" + url);
        if (url.equals(CONTACT_URI)) {
            BluetoothDevice device = BluetoothManager.getInstance().getConnectedDevice();
            if(device != null && device.getContacts() != null && device.getContacts().size() > 0){
                ArrayList<Contact> contacts = device.getContacts();
                LogUtils.i("contact size:" + contacts.size());
                try {
                    JSONArray array = new JSONArray();
                    for (int x = 0; x < contacts.size(); x++) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("name", contacts.get(x).name);
                        jsonObject.put("num", contacts.get(x).number);
                        array.put(jsonObject);
                    }
                    LogUtils.i("return contact");
                    return array.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                return "0";
            }

        } else if (url.equals(STATUS_URI)) {
            LogUtils.i("connectbd:" + (BluetoothManager.getInstance().getConnectedDevice() == null));
            return BluetoothManager.getInstance().getConnectedDevice() == null ? "0" : "1";
        }
        return "0";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
