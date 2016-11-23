package com.squareup.leakcanary;

import android.support.v4.app.Fragment;

/**
 * @创建者 luhaisheng
 * @创建时间 2016/11/15 14:49
 * @描述 ${TOO}
 * @更新者 $AUTHOR$
 * @创建时间 2016/11/15 14:49
 * @描述 ${TOO}
 */
public class LeakCanaryFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        LeakCanaryApplication.getRefWatcher().watch(this);
    }
}
