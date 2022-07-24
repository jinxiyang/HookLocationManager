package io.github.jinxiyang.hooklocationmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationListener;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * hook 系统的定位服务LocationManager，监控每一次发起的定位，也可以监控每次获取的定位位置。
 *
 * 在Android P及P以后，需要突破系统限制，访问受限的系统API，否则无法hook LocationManager。
 *
 * 突破系统限制：https://github.com/tiann/FreeReflection/
 *
 * LocationManager的源码地址（Android 8.0）：http://androidxref.com/8.0.0_r4/xref/frameworks/base/location/java/android/location/LocationManager.java
 */
public class HookLocationManager {
    private static final String TAG = "===hook===";

    /**
     * hook Context.getSystemService(Context.LOCATION_SERVICE)方法，得到的每一个LocationManager，我们都hook了它的mService。
     *
     * 在应用的application类的onCreate调用
     */
    public static void hook() {
        try {
            Class<?> systemServiceRegistryClass = Class.forName("android.app.SystemServiceRegistry");
            @SuppressLint("BlockedPrivateApi") Field mapField = systemServiceRegistryClass.getDeclaredField("SYSTEM_SERVICE_FETCHERS");
            mapField.setAccessible(true);
            Object map = mapField.get(null);
            if (map instanceof Map) {
                Object serviceFetcher = ((Map<?, ?>) map).get(Context.LOCATION_SERVICE);
                Class<?> serviceFetcherClass = Class.forName("android.app.SystemServiceRegistry$ServiceFetcher");
                Object proxy = Proxy.newProxyInstance(systemServiceRegistryClass.getClassLoader(), new Class[]{serviceFetcherClass}, new ServiceFetcherProxy(serviceFetcher));
                //用我们的代理对象，替换掉系统的ServiceFetcher，我们可以对生成的LocationManager搞事情
                Object instance = ((Map<String, Object>) map).put(Context.LOCATION_SERVICE, proxy);
                if (serviceFetcher == instance) {
                    Log.i(TAG, "hook [getSystemService(Context.LOCATION_SERVICE)] success");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "hook [getSystemService(Context.LOCATION_SERVICE)] failed");
    }

    /**
     * hook LocationManager的成员变量mService，检测mService的方法执行
     *
     * APP端发起定位，最终会执行mService.requestLocationUpdates()方法
     * @param locationManager
     */
    protected static void hookLocationManager(Object locationManager){
        try {
            //class是：android.location.LocationManager;
            Class<?> clazz = locationManager.getClass();
            @SuppressLint("BlockedPrivateApi") Field mServiceField = clazz.getDeclaredField("mService");
            mServiceField.setAccessible(true);
            //mService类型是：android.location.ILocationManager，实际是android.location.ILocationManager$Stub$Proxy
            //mService是一个代理类，通过Binder，获取系统定位功能
            Object mService = mServiceField.get(locationManager);
            if (mService instanceof IHookedFlag) {
                Log.i(TAG, "LocationManager的成员变量mService 不用重复hook");
                return;
            }

            Class<?> iLocationManagerClass = Class.forName("android.location.ILocationManager");
            //多继承一个接口IHookedFlag，避免重复hook
            Class<?>[] interfaces = {iLocationManagerClass, IHookedFlag.class};
            //代理mService，我们可以监控mService的方法执行，知晓定位的发起，也可以对定位结果回调listener搞事情
            Object proxy = Proxy.newProxyInstance(HookLocationManager.class.getClassLoader(), interfaces, new ILocationManagerProxy(mService));
            mServiceField.set(locationManager, proxy);

            Log.i(TAG, "hook LocationManager的成员变量mService success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "hook LocationManager的成员变量mService failed");
        }
    }


    /**
     * hook LocationListener，监控每一次的定位结果。
     * @param listenerTransport
     */
    protected static void hookLocationListener(Object listenerTransport) {
        try {
            Class<?> listenerTransportClass = Class.forName("android.location.LocationManager$ListenerTransport");
            Field mListenerField = listenerTransportClass.getDeclaredField("mListener");
            mListenerField.setAccessible(true);
            Object mListener = mListenerField.get(listenerTransport);
            if (mListener instanceof IHookedFlag) {
                Log.i(TAG, "LocationListener 不用重复hook");
                return;
            }
            Class<?>[] interfaces = {LocationListener.class, IHookedFlag.class};
            LocationListenerProxy locationListenerProxy = new LocationListenerProxy(mListener);
            Object proxy = Proxy.newProxyInstance(HookLocationManager.class.getClassLoader(), interfaces, locationListenerProxy);
            mListenerField.set(listenerTransport, proxy);
            Log.i(TAG, "hook LocationListener success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "hook LocationListener failed");
        }
    }
}
