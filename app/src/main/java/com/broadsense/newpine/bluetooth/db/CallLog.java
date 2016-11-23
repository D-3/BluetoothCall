package com.broadsense.newpine.bluetooth.db;

public class CallLog implements Comparable<CallLog>{
    public static final int TYPE_OUTGOING = 2;
    public static final int TYPE_INCOMING = 3;
    public static final int TYPE_MISSED = 4;

    public String name;
    public String number;
    public long time;
    public int type;

    @Override
    public int compareTo(CallLog another) {
        return time - another.time > 0 ? -1 : 1;
    }
}
