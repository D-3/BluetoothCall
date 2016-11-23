package com.broadsense.newpine.bluetooth.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;

public class CustomDialog extends Dialog {
    private static final String TAG = "CustomDialog";

    private Context context;
    private String info;
    private String confirmButtonText;
    private String cacelButtonText;
    private ClickListenerInterface clickListenerInterface;

    public interface ClickListenerInterface {
        public void doConfirm();

        public void doCancel();
    }

    public CustomDialog(Context context, String info, String confirmButtonText, String cacelButtonText) {
        super(context, R.style.CustomDialog);
        this.context = context;
        this.info = info;
        this.confirmButtonText = confirmButtonText;
        this.cacelButtonText = cacelButtonText;
    }


    public CustomDialog(Context context, boolean cancelable, OnCancelListener cancelListener, ClickListenerInterface clickListenerInterface) {
        super(context, cancelable, cancelListener);
        this.clickListenerInterface = clickListenerInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(TAG, "onBackPressed");
        super.onBackPressed();
    }

    public void init() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_dialog, null);
        setContentView(view);

        TextView tvTitle = (TextView) view.findViewById(R.id.info_text);
        TextView tvConfirm = (TextView) view.findViewById(R.id.dialog_ok_button);
        TextView tvCancel = (TextView) view.findViewById(R.id.dialog_cancel_button);

        tvTitle.setText(info);
        tvConfirm.setText(confirmButtonText);
        tvCancel.setText(cacelButtonText);

        tvConfirm.setOnClickListener(new clickListener());
        tvCancel.setOnClickListener(new clickListener());
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
//            return true;
//        }
//        else if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return false;
//        }
//        else {
//            return false;
//        }
//    }

    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        this.clickListenerInterface = clickListenerInterface;
    }

    private class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int id = v.getId();
            switch (id) {
                case R.id.dialog_ok_button:
                    clickListenerInterface.doConfirm();
                    break;
                case R.id.dialog_cancel_button:
                    clickListenerInterface.doCancel();
                    break;
            }
        }
    }

}
