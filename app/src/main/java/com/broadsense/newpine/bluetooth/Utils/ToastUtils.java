package com.broadsense.newpine.bluetooth.Utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.broadsense.newpine.bluetooth.R;


/**
 * Toast tool class
 * Created by zhouxian on 2016/1/26 0026.
 */
public class ToastUtils {
    private static Toast mToast;

//    private static Handler mHandler = new Handler();
//    private static Runnable r = new Runnable() {
//        public void run() {
//            mToast.cancel();
//            mToast = null;//toast隐藏后，将其置为null
//        }
//    };

    public static void showShortToast(Context context, String message) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_toast, null);//自定义布局
        TextView text = (TextView) view.findViewById(R.id.custom_toast_content_textview);//显示的提示文字
        text.setText(message);
//        mHandler.removeCallbacks(r);
        if (mToast == null) {//只有mToast==null时才重新创建，否则只需更改提示文字
            mToast = new Toast(context);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 80);
            mToast.setView(view);
        } else {
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 80);
            mToast.setView(view);
        }
        //mHandler.postDelayed(r, 2500);
        mToast.show();
    }

//    /**
//     * 显示短时间的Toast
//     */
//    public static void showShortToast(Context context, String text) {
//        if (mToast == null) {
//            mToast = ToastUtils.showShortToast(context, text, Toast.LENGTH_SHORT);
//        } else {
//            mToast.setText(text);
//            mToast.setDuration(Toast.LENGTH_SHORT);
//        }
//        mToast.show();
//    }

    /**
     * 显示长时间的Toast
     */
    public static void showLongToast(Context context, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }
//
//    /**
//     * 获取失败，请连接网络
//     *
//     * @param context
//     */
//    public static void showNoNetToast(Context context) {
//        String error = context.getString(R.string.no_net_toast);
//        showShortToast(context,error);
//    }
//
//    /**
//     * 网络连接失败，请稍后再试
//     *
//     * @param context
//     */
//    public static void showNetErrorToast(Context context) {
//        String error = context.getString(R.string.error_net_toast);
//        showShortToast(context,error);
//    }
}
