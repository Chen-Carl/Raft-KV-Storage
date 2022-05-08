package com.zoecarl.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ServiceManager {
    private static final Logger logger = LogManager.getLogger(ServiceManager.class);

    private ConcurrentHashMap<String, Class<?>> serviceMap = new ConcurrentHashMap<>();

    void register(Class<?> serviceClass) {
        serviceMap.put(serviceClass.getName(), serviceClass);
    }

    void unregister(Class<?> serviceClass) {
        serviceMap.remove(serviceClass.getName());
    }

    public Object executeService(String serviceClass, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        Object res;
        Class<?> service = serviceMap.get(serviceClass);
        if (service == null) {
            logger.error("service not found, service name: {}", serviceClass + "." + methodName);
            return null;
        }
        try {
            Method method = service.getMethod(methodName, parameterTypes);
            res = method.invoke(service.getDeclaredConstructor().newInstance(), arguments);
        } catch (NoSuchMethodException e) {
            logger.error("method not found, method name: {}", methodName);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("illegal access, method name: {}", methodName);
            return null;
        } catch (Exception e) {
            logger.error("execute service error, method name: {}, {}", methodName, e);
            return null;
        }

        return res;
    }
}
