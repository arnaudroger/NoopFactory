package com.github.arnaudroger.noop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class NoopFactory {

    private static final InvocationHandler NULL_INVOCATION_HANDLER = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    };

    public static <T> T noop(Class<?> target) {
        if (! target.isInterface()) {
            throw new IllegalArgumentException("Expect class to be an interface " + target);
        }

        return (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, newInvotionHandler(target));
    }

    private static InvocationHandler newInvotionHandler(Class<?> target) {
        return NULL_INVOCATION_HANDLER;
    }

}
