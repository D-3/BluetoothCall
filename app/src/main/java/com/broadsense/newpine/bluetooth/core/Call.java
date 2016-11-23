package com.broadsense.newpine.bluetooth.core;

import android.view.View;
import android.widget.TextView;

/**
 * Created by DeW on 2016/11/15.
 */

public class Call {
    /**
     呼出还是呼入

     0.呼出
     1.呼入
     */
    public int callType;

    /**
     第几路通话

     1.第1路通话
     2.第2路通话
     */
    public int index;

    public String phoneNumber;

    public String location;

    /**
     通话状态

     0.已接通
     1.保持
     2.拨号中
     3.来电
     4.响铃
     5.等待
     */
    public int state = -1;

    /*
    通话保持前接通时间
     */
    public long preTotalTime = 0;

    /*
    通话开始、恢复时间
     */
    public long callResumeTime = 0;

    public View callInfoContainer;
    public TextView newCallTipView;
    public TextView callNumberView;
    public TextView locationView;
    public TextView callTypeView;


    @Override
    public String toString() {
        return super.toString() + " callType=" + callType
                + " index=" + index
                + " phoneNumber=" + phoneNumber
                + " state=" + state
                + " preTotalTime=" + preTotalTime
                + " callResumeTime=" + callResumeTime;
    }
}
