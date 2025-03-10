package com.alibaba.p3c.pmd.lang.java.util;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.p3c.pmd.lang.java.util.namelist.NameListConfig;

/**
 * @author changle.lq
 * @date 2017/04/01
 */
public class SpiLoader {
    private final static ConcurrentHashMap<Class<?>, Object> INSTANCE_CACHE = new ConcurrentHashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> classType) {
        T instance = (T)INSTANCE_CACHE.get(classType);

        if (instance != null) {
            return instance;
        }
        try {
            instance = ServiceLoader.load(classType, NameListConfig.class.getClassLoader()).iterator().next();
            if (instance == null) {
                return null;
            }
            INSTANCE_CACHE.putIfAbsent(classType, instance);
            return instance;
        } catch (Throwable e) {
            return null;
        }
    }
}
