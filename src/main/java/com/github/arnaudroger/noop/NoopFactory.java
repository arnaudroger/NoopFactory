package com.github.arnaudroger.noop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class NoopFactory {

    private static final InvocationHandler NULL_INVOCATION_HANDLER = new ConstantInvocationHandler(null);

    private static final Map<Class<?>, Object> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(boolean.class, false);
        DEFAULT_VALUES.put(byte.class, (byte)0);
        DEFAULT_VALUES.put(char.class, (char)0);
        DEFAULT_VALUES.put(short.class, (short)0);
        DEFAULT_VALUES.put(int.class, (int)0);
        DEFAULT_VALUES.put(long.class, (long)0);
        DEFAULT_VALUES.put(float.class, (float)0);
        DEFAULT_VALUES.put(double.class, (double)0);
    }

    private static final ClassValue<InvocationHandler> PRIMTIVES_INVOCATION_HANDLERS = new ClassValue<InvocationHandler>() {
        @Override
        protected InvocationHandler computeValue(Class<?> type) {
            if (!type.isPrimitive()) {
                throw new IllegalArgumentException("Only support primitives");
            }

            Object value = DEFAULT_VALUES.get(type);

            if (value == null) {
                throw new IllegalStateException("No default value defined for " + type);
            }
            return new ConstantInvocationHandler(value);
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> T noop(Class<?> target) {
        Method m = getFunctionalMethod(target);
        if (m == null) {
            throw new IllegalArgumentException("Expect class to be a functional interface " + target);
        }

        return (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, newInvotionHandler(m));
    }

    static Method getFunctionalMethod(Class<?> target) {
        if (!target.isInterface()) {
            return null;
        }

        Method functionalMethod = null;
        for(Method m : target.getDeclaredMethods()) {
            if (isElegibleMethod(m)) {
                if (functionalMethod != null) return null;
                functionalMethod = m;
            }
        }
        return functionalMethod;
    }

    private static boolean isElegibleMethod(Method m) {
        if (m.isDefault() || m.isSynthetic() || m.isBridge() || Modifier.isStatic(m.getModifiers())) {
            return false;
        }
        if (m.getName().equals("equals")) {
            return !(boolean.class.equals(m.getReturnType()) && m.getParameterCount() == 1 && Object.class.equals(m.getParameterTypes()[0]));
        }
        if (m.getName().equals("hashCode")) {
            return !(int.class.equals(m.getReturnType()) && m.getParameterCount() == 0);
        }
        return true;
    }

    private static InvocationHandler newInvotionHandler(Method target) {
        Class<?> returnType = target.getReturnType();
        if (!returnType.isPrimitive()) {
            return NULL_INVOCATION_HANDLER;
        }

        return PRIMTIVES_INVOCATION_HANDLERS.get(returnType);
    }

    private static class ConstantInvocationHandler implements InvocationHandler {
        private final Object value;

        private ConstantInvocationHandler(Object value) {
            this.value = value;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return value;
        }
    }
}
