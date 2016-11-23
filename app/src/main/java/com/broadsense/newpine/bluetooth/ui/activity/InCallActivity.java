package com.broadsense.newpine.bluetooth.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.CallTimer;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;
import com.broadsense.newpine.bluetooth.control.BluetoothService;
import com.broadsense.newpine.bluetooth.core.Call;

public class InCallActivity extends AppCompatActivity {
    private static final String TAG = "InCallActivity";

    Context mContext;

    private View mPrimaryCallInfo;
    private TextView mPrimaryNewCallTip;
    private TextView mPrimaryCallNumber;
    private TextView mPrimaryLocation;
    private TextView mPrimaryCallType;

    private View mSecondCallInfo;
    private TextView mSecondNewCallTip;
    private TextView mSecondCallNumber;
    private TextView mSecondLocation;
    private TextView mSecondCallType;

    private ImageButton mHangupButton;
    private ImageButton mAnswerButton;

    private Call[] mCalls = new Call[2];

//    private CallTimer mCallTimer;
    private CallTimer mPrimaryCallTimer;
    private CallTimer mSecondCallTimer;

    private String[] mPreHFState = new String[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onDestroy");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null || intent.getStringExtra("data") == null) {
            finish();
        }

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().setAttributes(lp);

        mContext = this;
        setContentView(R.layout.activity_in_call);

        mPrimaryCallInfo = findViewById(R.id.primary_call_info);
        mPrimaryNewCallTip = (TextView) findViewById(R.id.new_call_tip);
        mPrimaryCallNumber = (TextView) findViewById(R.id.primary_call_number);
        mPrimaryLocation = (TextView) findViewById(R.id.primary_location);
        mPrimaryCallType = (TextView) findViewById(R.id.primary_call_type);

        mSecondCallInfo = findViewById(R.id.second_call_info);
        mSecondNewCallTip = (TextView) findViewById(R.id.new_call_tip_2);
        mSecondCallNumber = (TextView) findViewById(R.id.second_call_number);
        mSecondLocation = (TextView) findViewById(R.id.second_location);
        mSecondCallType = (TextView) findViewById(R.id.second_call_type);

        mHangupButton = (ImageButton) findViewById(R.id.btn_decline);
        mHangupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //挂断电话
                ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFCHUP);
            }
        });
        mAnswerButton = (ImageButton) findViewById(R.id.btn_anwser);
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //接听电话
                ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFANSW);
            }
        });


        String callInfo = intent.getStringExtra("data");
        LogUtils.d(TAG, "data=" + callInfo);

        String info[] = callInfo.split(BluetoothService.COMMA);
        Call call_1 = new Call();
        call_1.index = 1;
        call_1.callType = Integer.parseInt(info[2]);
        call_1.phoneNumber = info[6];
        call_1.state = Integer.parseInt(info[0].substring(12 , 13));
        call_1.callInfoContainer = mPrimaryCallInfo;
        call_1.newCallTipView = mPrimaryNewCallTip;
        call_1.callNumberView = mPrimaryCallNumber;
        call_1.locationView = mPrimaryLocation;
        call_1.callTypeView = mPrimaryCallType;
        mPrimaryCallInfo.setTag(call_1);

        mCalls[0] = call_1;


        mAnswerButton.setVisibility(mCalls[0].callType == 1 ?  View.VISIBLE : View.GONE);
        mHangupButton.setVisibility(View.VISIBLE);
        mCalls[0].newCallTipView.setVisibility(View.GONE);
        mCalls[0].callTypeView.setText(mCalls[0].callType == 0 ? "正在拨号" : "来电");
        mCalls[0].callNumberView.setText(mCalls[0].phoneNumber);
        mCalls[0].callInfoContainer.setVisibility(View.VISIBLE);

        ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFSVGM + "15");
        ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFSVGS + "15");

        registerReceiver(mReceiver, new IntentFilter("com.android.EXTERNAL_BLUETOOTH"));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        //停止计时器
//        cancelTimer(mCalls[0]);
        cancelPrimaryCallTimer();
        cancelSecondCallTimer();

        unregisterReceiver(mReceiver);
    }

    private void recordPre3HFState(String HFstate){
        mPreHFState[0] = mPreHFState[1];
        mPreHFState[1] = mPreHFState[2];
        mPreHFState[2] = HFstate;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (context == null) return;

            String result = intent.getStringExtra("result");
            LogUtils.i(TAG, "receiver:" + result);

            String data[] = result.split(BluetoothService.COMMA);
            if(data[0].contains("AT-B HFSTAT")){
                recordPre3HFState(data[0]);
            }

            /*
            连接断了
             */
            if (result.equals("AT-B HFSTAT 1")) {
                finish();
            }

            /*
             回到已连接状态，说明已挂断电话
             */
            else if (result.contains("AT-B HFSTAT 3")) {
                finish();
            }

            else if (result.contains("AT-B HFSTAT 4")) {
                //来电状态  4
            }

            /*
             已接听来电  6
             */
            else if (result.contains("AT-B HFSTAT 6")) {

                if(mPreHFState[0] != null && mPreHFState[0].equals("AT-B HFSTAT 7") &&
                        mPreHFState[1] != null && mPreHFState[1].equals("AT-B HFSTAT 4")){
                    LogUtils.d(TAG, "结束第 1 路，接通第 2 路");

                    stopPrimaryCallTimer();
                    mHangupButton.setVisibility(View.VISIBLE);
                    mCalls[0].callInfoContainer.setVisibility(View.GONE);
                    mCalls[0] = mCalls[1];
                    mCalls[1] = null;
                    updatePrimaryCallTime();
                }

                else if(mPreHFState[0] != null && mPreHFState[0].equals("AT-B HFSTAT 6") &&
                        mPreHFState[1] != null && mPreHFState[1].equals("AT-B HFSTAT 7")){
                    LogUtils.d(TAG, "挂断第 2 路来电");

                    //恢复显示第1路通话
                    mCalls[1].callInfoContainer.setVisibility(View.GONE);
                    mCalls[0].callInfoContainer.setVisibility(View.VISIBLE);
                    mCalls[1] = null;

                }

                //保留通话被挂断出现了两种情况
                else if(mPreHFState[0] != null && mPreHFState[0].equals("AT-B HFSTAT 8") &&
                        mPreHFState[1] != null && mPreHFState[1].equals("AT-B HFSTAT 8")){
                    LogUtils.d(TAG, "第 2 路通话中，保留通话被挂断");

                    stopPrimaryCallTimer();
                    transitionCall2ToCenter(mCalls[1].callInfoContainer);
                    scaleToCall1ToDisappear(mCalls[0].callInfoContainer);
                    mCalls[0] = mCalls[1];
                    mCalls[1] = null;

                }

                else if(mPreHFState[0] != null && mPreHFState[0].equals("AT-B HFSTAT 6") &&
                        mPreHFState[1] != null && mPreHFState[1].equals("AT-B HFSTAT 8")){
                    LogUtils.d(TAG, "第 2 路通话中，保留通话被挂断");

                    stopPrimaryCallTimer();
                    transitionCall2ToCenter(mCalls[1].callInfoContainer);
                    scaleToCall1ToDisappear(mCalls[0].callInfoContainer);
                    mCalls[0] = mCalls[1];
                    mCalls[1] = null;

                }

                else{
                    //正常接听第1路来电
                    mCalls[0].callResumeTime = System.currentTimeMillis();
                    updatePrimaryCallTime();
                    mAnswerButton.setVisibility(View.GONE);
                    mHangupButton.setVisibility(View.VISIBLE);
                }

            }

            //通话中，有来电  7 (这里不做处理,放在 HFCCIN 中)
//            else if (result.contains("AT-B HFSTAT 7")) {
//
//            }

            /*
             保留当前通话，接听来电  8
             */
            else if (result.contains("AT-B HFSTAT 8")) {
                LogUtils.d(TAG, "保留第 1 路，接通第 2 路");

                //初始化第2路接通时间戳
                mCalls[1].callResumeTime = System.currentTimeMillis();
                updateSecondCallTime();
                mCalls[1].newCallTipView.setVisibility(View.GONE);
                mCalls[1].callInfoContainer.setBackgroundResource(R.drawable.call_info_bg);
                mHangupButton.setVisibility(View.VISIBLE);

                //停止第1路计时器
                stopPrimaryCallTimer();
                mCalls[0].callTypeView.setText("保持");
                mCalls[0].callInfoContainer.setVisibility(View.VISIBLE);
                mCalls[0].callInfoContainer.setBackgroundResource(R.drawable.call_info_bg);
                transitionCall1ToRight(mCalls[0].callInfoContainer);
                transitionCall2ToLeft(mCalls[1].callInfoContainer);
            }

            /*
             通话中，挂断一路通话  10
             */
            else if (result.contains("AT-B HFSTAT 10")) {
                LogUtils.d(TAG, "挂断第 2 路，恢复第 1 路");

                //第二路通话挂断
//                mSecondCall = null;
//                updateSecondCallView();
                updatePrimaryCallTime();
                scaleToCall2ToDisappear(mCalls[1].callInfoContainer);
                transitionCall1ToCenter(mCalls[0].callInfoContainer);
                mCalls[1] = null;
            }

            else if (result.contains("AT-B HFCCIN")) {

//                //第一路通话
//                if(data[1] == "1"){
//                    if(mPrimaryCall != null && !mPrimaryCall.phoneNumber.equals(data[6])){
//                        mPrimaryCall = new Call();
//                        mPrimaryCall.phoneNumber = data[6];
//                        mPrimaryCall.callType = Integer.parseInt(data[2]);
//                        mPrimaryCall.state = Integer.parseInt(data[0].substring(12 , 13));
//                        updatePrimaryCallView();
//                    }
//                }

//                //第二路通话
//                if(data[1].equals("2")){

                    if(mCalls[1] == null && !data[6].equals(mCalls[0].phoneNumber)){
                        LogUtils.d(TAG, "来了一个新的电话");
                        Call call_2 = new Call();
                        call_2.index = 2;
                        call_2.callType = Integer.parseInt(data[2]);
                        call_2.phoneNumber = data[6];
                        call_2.state = Integer.parseInt(data[0].substring(12 , 13));
                        call_2.preTotalTime = 0;

                        if(mCalls[0].callInfoContainer != mPrimaryCallInfo){
                            call_2.callInfoContainer = mPrimaryCallInfo;
                            call_2.newCallTipView = mPrimaryNewCallTip;
                            call_2.callNumberView = mPrimaryCallNumber;
                            call_2.locationView = mPrimaryLocation;
                            call_2.callTypeView = mPrimaryCallType;
                        }else{
                            call_2.callInfoContainer = mSecondCallInfo;
                            call_2.newCallTipView = mSecondNewCallTip;
                            call_2.callNumberView = mSecondCallNumber;
                            call_2.locationView = mSecondLocation;
                            call_2.callTypeView = mSecondCallType;
                        }
                        mCalls[1] = call_2;

                        LogUtils.d(TAG, "call_1_view=" + mCalls[0].callInfoContainer + "call_2_view=" + mCalls[1].callInfoContainer);

                        mAnswerButton.setVisibility(View.GONE);
                        mHangupButton.setVisibility(View.GONE);
                        mCalls[1].newCallTipView.setVisibility(View.VISIBLE);
                        mCalls[1].callTypeView.setText(mCalls[1].callType == 0 ? "正在拨号" : "来电");
                        mCalls[1].callNumberView.setText(mCalls[1].phoneNumber);
                        mCalls[1].callInfoContainer.setVisibility(View.VISIBLE);

                        mCalls[0].callInfoContainer.setVisibility(View.GONE);
                    }

//                }
            }
        }
    };

    private static long TIMER_INTERVAL = 1000;

    private void updatePrimaryCallTime(){
        if(mPrimaryCallTimer == null){
            mPrimaryCallTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                    updateCallTime(mCalls[0]);
                }
            });
        }
        mPrimaryCallTimer.start(TIMER_INTERVAL);
    }

    private void updateSecondCallTime(){
        if(mSecondCallTimer == null){
            mSecondCallTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                    updateCallTime(mCalls[1]);
                }
            });
        }
        mSecondCallTimer.start(TIMER_INTERVAL);
    }

    private void stopPrimaryCallTimer(){
        if(mPrimaryCallTimer != null){
            mPrimaryCallTimer.stop();
        }
        if(mCalls[0] != null){
            mCalls[0].preTotalTime = System.currentTimeMillis() - mCalls[0].callResumeTime;
        }
    }

    private void stopSecondCallTimer(){
        if(mPrimaryCallTimer != null){
            mPrimaryCallTimer.stop();
        }
        if(mCalls[1] != null){
            mCalls[1].preTotalTime = System.currentTimeMillis() - mCalls[1].callResumeTime;
        }
    }

    private void cancelPrimaryCallTimer(){
        if(mPrimaryCallTimer != null){
            mPrimaryCallTimer.cancel();
        }
        if(mCalls[0] != null){
            mCalls[0].preTotalTime = System.currentTimeMillis() - mCalls[0].callResumeTime;
        }
    }

    private void cancelSecondCallTimer(){
        if(mSecondCallTimer != null){
            mSecondCallTimer.cancel();
        }
        if(mCalls[1] != null){
            mCalls[1].preTotalTime = System.currentTimeMillis() - mCalls[1].callResumeTime;
        }
    }

    private void updateCallTime(Call call){
        if(call == null) return;
        call.callTypeView.setText(DateUtils.formatElapsedTime((call.preTotalTime + System.currentTimeMillis() - call.callResumeTime)/1000));
    }

//    private void startTimer(final Call call){
//        mCallTimer = new CallTimer(new Runnable() {
//            @Override
//            public void run() {
//                updateTimerView(call);
//            }
//        });
//        mCallTimer.start(TIMER_INTERVAL);
//    }
//
//    private void updateTimerView(Call call){
//        if(call == null || call.callTypeView == null) return;
//        call.callTypeView.setText(DateUtils.formatElapsedTime((call.preTotalTime + System.currentTimeMillis() - call.callResumeTime)/1000));
//    }
//
//    private void cancelTimer(Call call){
//        if(mCallTimer != null){
//            mCallTimer.cancel();
//        }
//        if(call != null){
//            call.preTotalTime = System.currentTimeMillis() - call.callResumeTime;
//        }
//    }


    private void transitionCall2ToLeft(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 0F, -185F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", 0F, -10F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 1.06F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 1.06F);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3 , pvh4)
                .setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setBackgroundResource(R.drawable.call_info_bg);
                view.setElevation(10);
            }
        });
        animator.start();
    }

    private void transitionCall2ToCenter(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", -185F, 0F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", -10F, 0F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1.06F, 1F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1.06F, 1F);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3, pvh4)
                .setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setBackgroundColor(android.R.color.transparent);
                view.setElevation(0);
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    private void scaleToCall2ToDisappear(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1.1F, 0.8F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1.1F, 0.8F);
//        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
//                "alpha", 1.0F, 0F);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvh3, pvh4)
                .setDuration(400);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
                view.setBackgroundColor(android.R.color.transparent);
            }
        });
        objectAnimator.start();
    }

    private void transitionCall1ToRight(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 0F, 185F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", 0F, -10F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 0.9F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 0.9F);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3 , pvh4)
                .setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setBackgroundResource(R.drawable.call_info_bg);
                view.setElevation(10);
            }
        });
        animator.start();
    }

    private void transitionCall1ToCenter(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 185F, 0F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", -10F, 0F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 0.9F, 1F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 0.9F, 1F);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3, pvh4)
                .setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setBackgroundColor(android.R.color.transparent);
                view.setElevation(0);
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    private void scaleToCall1ToDisappear(final View view){
        Log.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 0.9F, 0.6F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 0.9F, 0.6F);
//        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
//                "alpha", 1F, 0F);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvh3, pvh4)
                .setDuration(400);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
                view.setBackgroundColor(android.R.color.transparent);
            }
        });
        objectAnimator.start();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
