package io.github.jinxiyang.hooklocationmanagerdemo;

import android.app.Application;
import android.content.Context;

import io.github.jinxiyang.hooklocationmanager.HookLocationManager;
import me.weishu.reflection.Reflection;

public class DemoApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //突破系统限制，访问受限的系统api
        Reflection.unseal(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //hook LocationManager，监控每一次发起的定位以及定位结果。开发测试时用，APP生产上线去掉
        HookLocationManager.hook();
    }
}
