package com.broadsense.newpine.bluetooth.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;

/**
 * Created by DeW on 2016/11/9.
 */

public class TestAnimatorActivity extends Activity {

    private static final String TAG = "TestAnimatorActivity";

    Context mContext;

    private View mPrimaryCallInfo;

    private View mSecondCallInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_anim_test);

        mPrimaryCallInfo = findViewById(R.id.primary_call_info);
        mSecondCallInfo = findViewById(R.id.second_call_info);

        Button button1 = (Button) findViewById(R.id.btn_baochi_jieting);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baochijieting();
            }
        });
        Button button2 = (Button) findViewById(R.id.btn_2_guaduan);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huifu1();
            }
        });
        Button button3 = (Button) findViewById(R.id.btn_1_guaduan);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huifu2();
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    //1.保持通话，接听（1路右移，2路左移）
    private void baochijieting(){
        mPrimaryCallInfo.setVisibility(View.VISIBLE);
        transitionCall1ToRight(mPrimaryCallInfo);
        transitionCall2ToLeft(mSecondCallInfo);
    }

    //2.恢复保持通话（2路从左边消失，1路从右边放大恢复）
    private void huifu1(){
        scaleToCall2ToDisappear(mSecondCallInfo);
        transitionCall1ToCenter(mPrimaryCallInfo);
    }

    //3.保持通话被挂断（2路从左边放大恢复，1路从右边消失）
    private void huifu2(){
        scaleToCall1ToDisappear(mPrimaryCallInfo);
        transitionCall2ToCenter(mSecondCallInfo);
    }

    private void transitionCall2ToLeft(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 0F, -245F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", 0F, -10F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 1.2F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 1.2F);

        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3 , pvh4)
                .setDuration(1000).start();
    }

    private void transitionCall2ToCenter(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", -245F, 0F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", -10F, 0F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1.2F, 1F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1.2F, 1F);
        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3, pvh4)
                .setDuration(1000).start();
    }

    private void transitionCall1ToRight(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 0F, 245F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", 0F, -10F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 0.8F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 0.8F);
        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3, pvh4)
                .setDuration(1000).start();
    }

    private void transitionCall1ToCenter(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "translationX", 245F, 0F);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat(
                "translationY", -10F, 0F);
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 0.8F, 1F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 0.8F, 1F);
        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh2, pvh3, pvh4)
                .setDuration(1000).start();
    }

    private void scaleToCall1ToDisappear(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 0.8F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 0.8F);
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "alpha", 1F, 0F);
        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh3, pvh4)
                .setDuration(1000).start();
    }


    private void scaleToCall2ToDisappear(View view){
        LogUtils.d(TAG, "x=" + view.getX() + "y" + view.getY());
        PropertyValuesHolder pvh3 = PropertyValuesHolder.ofFloat(
                "scaleX", 1F, 0.8F);
        PropertyValuesHolder pvh4 = PropertyValuesHolder.ofFloat(
                "scaleY", 1F, 0.8F);
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat(
                "alpha", 1.0F, 0F);
        ObjectAnimator.ofPropertyValuesHolder(view, pvh1, pvh3, pvh4)
                .setDuration(1000).start();
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
