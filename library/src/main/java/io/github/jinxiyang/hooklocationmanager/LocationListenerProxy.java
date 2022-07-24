package io.github.jinxiyang.hooklocationmanager;

import android.location.Location;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 代理LocationListener
 *
 * 监控LocationListener.onLocationChanged()，监控每一次的定位结果
 */
public class LocationListenerProxy implements InvocationHandler {

    private static final String TAG = "===hook===";

    private Object instance;

    public LocationListenerProxy(Object locationListener) {
        this.instance = locationListener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("onLocationChanged".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof Location) {
            //每次定位结果的回调，可以搞事情，记录定位时间、回调类是谁等等。
            Location location = (Location) args[0];
            Log.i(TAG, "onLocationChanged: " + location.getLatitude() + "  " + location.getLongitude());
        }
        return method.invoke(instance, args);
    }
}
