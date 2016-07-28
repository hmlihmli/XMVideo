package cn.xm.xmvideoplayer.core;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by 11 on 2016/3/31.
 */
public class MyApp extends Application {

    public static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        //在自己的Application中添加如下代码
        LeakCanary.install(this);
    }

    public static Context getContext() {
        return mAppContext;
    }


}
