package com.squareup.leakcanary;

import android.app.Application;
import android.os.StrictMode;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.GINGERBREAD;

/**
 * @创建者 luhaisheng
 * @创建时间 2016/11/15 10:41
 * @描述 ${TOO}
 * @更新者 $AUTHOR$
 * @创建时间 2016/11/15 10:41
 * @描述 ${TOO}
 */
public class LeakCanaryApplication extends Application {

    //在自己的Application中添加如下代码
    private static RefWatcher refWatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        CanaryLog.initFile(this);

//        enabledStrictMode();
        refWatcher= LeakCanary.install(this);
    }

    private void enabledStrictMode() {
        if (SDK_INT >= GINGERBREAD) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                    .detectAll() //
                    .penaltyLog() //
                    .penaltyDeath() //
                    .build());
        }
    }

    public  static  RefWatcher getRefWatcher() {
        return refWatcher;
    }

}
