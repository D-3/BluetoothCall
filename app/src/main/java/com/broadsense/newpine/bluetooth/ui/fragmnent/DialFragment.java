package com.broadsense.newpine.bluetooth.ui.fragmnent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.external.bluetooth.ExternalBluetoothManager;
import com.broadsense.newpine.bluetooth.ExtBtApp;
import com.broadsense.newpine.bluetooth.R;
import com.broadsense.newpine.bluetooth.Utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialFragment extends Fragment {

    EditText numberText;
    Button callButton;
    TextView k1;
    TextView k2;
    TextView k3;
    TextView k4;
    TextView k5;
    TextView k6;
    TextView k7;
    TextView k8;
    TextView k9;
    TextView ka;
    TextView k0;
    TextView ks;
    ImageButton delete;


    public DialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialer, null);
        numberText = (EditText) rootView.findViewById(R.id.number_input_text);
        numberText.setInputType(InputType.TYPE_NULL);
        callButton = (Button) rootView.findViewById(R.id.call_button);

        k1 = (TextView) rootView.findViewById(R.id.keypad_1);
        k2 = (TextView) rootView.findViewById(R.id.keypad_2);
        k3 = (TextView) rootView.findViewById(R.id.keypad_3);
        k4 = (TextView) rootView.findViewById(R.id.keypad_4);
        k5 = (TextView) rootView.findViewById(R.id.keypad_5);
        k6 = (TextView) rootView.findViewById(R.id.keypad_6);
        k7 = (TextView) rootView.findViewById(R.id.keypad_7);
        k8 = (TextView) rootView.findViewById(R.id.keypad_8);
        k9 = (TextView) rootView.findViewById(R.id.keypad_9);
        ka = (TextView) rootView.findViewById(R.id.keypad_asterisk);
        k0 = (TextView) rootView.findViewById(R.id.keypad_0);
        ks = (TextView) rootView.findViewById(R.id.keypad_number_sign);
        delete = (ImageButton) rootView.findViewById(R.id.input_delete_button);

        k1.setOnClickListener(keypadClivk);
        k2.setOnClickListener(keypadClivk);
        k3.setOnClickListener(keypadClivk);
        k4.setOnClickListener(keypadClivk);
        k5.setOnClickListener(keypadClivk);
        k6.setOnClickListener(keypadClivk);
        k7.setOnClickListener(keypadClivk);
        k8.setOnClickListener(keypadClivk);
        k9.setOnClickListener(keypadClivk);
        ka.setOnClickListener(keypadClivk);
        k0.setOnClickListener(keypadClivk);
        ks.setOnClickListener(keypadClivk);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = numberText.getSelectionStart();
                Editable editable = numberText.getText();
                if (index > 0) {
                    editable.delete(index - 1, index);
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String callnum = numberText.getText().toString();
                LogUtils.i("callnum:" + callnum);
                if (callnum.length() > 1) {
                    ExtBtApp.getmExternalBluetoothManager().sendCommand(ExternalBluetoothManager.Command.CMD_HFDIAL + "0," + numberText.getText().toString());
//                    Intent intent = new Intent(getActivity(), CallActivity.class);
//                    intent.putExtra("num", callnum);
//                    startActivity(intent);
                    numberText.setText("");
                }
            }
        });

        return rootView;
    }

    View.OnClickListener keypadClivk = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int index = numberText.getSelectionStart();
            Editable editable = numberText.getText();
            switch (view.getId()) {
                case R.id.keypad_1:
                    editable.insert(index, getString(R.string.keypad_1));
                    break;
                case R.id.keypad_2:
                    editable.insert(index, getString(R.string.keypad_2));
                    break;
                case R.id.keypad_3:
                    editable.insert(index, getString(R.string.keypad_3));
                    break;
                case R.id.keypad_4:
                    editable.insert(index, getString(R.string.keypad_4));
                    break;
                case R.id.keypad_5:
                    editable.insert(index, getString(R.string.keypad_5));
                    break;
                case R.id.keypad_6:
                    editable.insert(index, getString(R.string.keypad_6));
                    break;
                case R.id.keypad_7:
                    editable.insert(index, getString(R.string.keypad_7));
                    break;
                case R.id.keypad_8:
                    editable.insert(index, getString(R.string.keypad_8));
                    break;
                case R.id.keypad_9:
                    editable.insert(index, getString(R.string.keypad_9));
                    break;
                case R.id.keypad_asterisk:
                    editable.insert(index, getString(R.string.keypad_asterisk));
                    break;
                case R.id.keypad_0:
                    editable.insert(index, getString(R.string.keypad_zero));
                    break;
                case R.id.keypad_number_sign:
                    editable.insert(index, getString(R.string.keypad_number_sign));
                    break;
            }
        }
    };
}
