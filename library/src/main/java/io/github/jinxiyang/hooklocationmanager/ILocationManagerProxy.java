package io.github.jinxiyang.hooklocationmanager;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * ILocationManager的代理类，代理LocationManager的成员变量mService，检测mService的方法执行
 */
public class ILocationManagerProxy implements InvocationHandler {
    private static final String TAG = "===hook===";

    private Object iLocationManager;

    public ILocationManagerProxy(Object object) {
        this.iLocationManager = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("requestLocationUpdates".equals(method.getName())) {
            //发起定位，这里可以搞事情，打印调用方法栈，知晓是谁发起的定位。
            //最好是记录LocationListener，定位结果回调类，更容易知晓调用情况
            Log.i(TAG, "requestLocationUpdates");
            if (args != null && args.length > 0) {
                for (Object o : args) {
                    if (o != null && "android.location.LocationManager$ListenerTransport".equals(o.getClass().getName())) {
                        //hook LocationListener，监控每一次的定位结果
                        HookLocationManager.hookLocationListener(o);
                        break;
                    }
                }
            }
        }
        return method.invoke(iLocationManager, args);
    }
}
