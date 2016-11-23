package com.broadsense.newpine.bluetooth.core;

/**
 * Created by DeW on 2016/11/11.
 */

public class BluetoothEvent {

    public static final int EVENT_DISCONNET = 0;
    public static final int EVENT_CONNECTED = 1;
    public static final int EVENT_DOWNLOADING_CONTACT = 2;
    public static final int EVENT_DOWNLOADING_CALLLOG = 3;
    public static final int EVENT_DOWNLOAD_CONTACT_SUCCESS = 4;
    public static final int EVENT_DOWNLOAD_CALLLOG_SUCCESS = 5;
    public static final int EVENT_DOWNLOAD_CONTACT_FAIL = 6;
    public static final int EVENT_DOWNLOAD_CALLLOG_FAIL = 7;

//    public static final int EVENT_DOWNLOAD_FAIL = 6;

    public static final int EVENT_DEPAIR_SUCCESS = 8;
    public static final int EVENT_DEPAIR_FAIL = 9;

    public static final int EVENT_PAIR_FAIL = 10;

    public static final int EVENT_UPDATE_DEVICE_INFO = 99;

    private int mEvent;

    public BluetoothEvent(int event){
        mEvent = event;
    }

    public int getEvent(){
        return mEvent;
    }

    @Override
    public String toString() {
        switch (mEvent){
            case EVENT_DISCONNET:
                return super.toString() + "   event=" + "EVENT_DISCONNET";
            case EVENT_CONNECTED:
                return super.toString() + "   event=" + "EVENT_CONNECTED";
            case EVENT_DOWNLOADING_CONTACT:
                return super.toString() + "   event=" + "EVENT_DOWNLOADING_CONTACT";
            case EVENT_DOWNLOADING_CALLLOG:
                return super.toString() + "   event=" + "EVENT_DOWNLOADING_CALLLOG";
            case EVENT_DOWNLOAD_CONTACT_SUCCESS:
                return super.toString() + "   event=" + "EVENT_DOWNLOAD_CONTACT_SUCCESS";
            case EVENT_DOWNLOAD_CALLLOG_SUCCESS:
                return super.toString() + "   event=" + "EVENT_DOWNLOAD_CALLLOG_SUCCESS";
//            case EVENT_DOWNLOAD_FAIL:
//                return this.hashCode() + "   event=" + "EVENT_DOWNLOAD_FAIL";
            case EVENT_DOWNLOAD_CALLLOG_FAIL:
                return super.toString() + "   event=" + "EVENT_DOWNLOAD_CALLLOG_FAIL";
            case EVENT_DOWNLOAD_CONTACT_FAIL:
                return super.toString() + "   event=" + "EVENT_DOWNLOAD_CONTACT_FAIL";

            case EVENT_DEPAIR_SUCCESS:
                return super.toString() + "   event=" + "EVENT_DEPAIR_SUCCESS";
            case EVENT_DEPAIR_FAIL:
                return super.toString() + "   event=" + "EVENT_DEPAIR_FAIL";
            case EVENT_PAIR_FAIL:
                return super.toString() + "   event=" + "EVENT_PAIR_FAIL";
            case EVENT_UPDATE_DEVICE_INFO:
                return super.toString() + "   event=" + "EVENT_UPDATE_DEVICE_INFO";

            default:
                throw new IllegalArgumentException("Unsupported event " + mEvent);
        }

    }
}
