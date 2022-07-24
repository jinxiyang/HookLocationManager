package io.github.jinxiyang.hooklocationmanager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceFetcherProxy implements InvocationHandler {
    private Object serviceFetcher;

    public ServiceFetcherProxy(Object serviceFetcher) {
        this.serviceFetcher = serviceFetcher;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(serviceFetcher, args);
        if ("getService".equals(method.getName())) {
            HookLocationManager.hookLocationManager(result);
        }
        return result;
    }
}
