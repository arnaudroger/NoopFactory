package com.github.arnaudroger.noop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FixedValue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class NoopFactory {

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


    private static final ClassValue<Class<?>> BYTEBUDDIES = new ClassValue<Class<?>>() {
        @Override
        protected Class<?> computeValue(Class<?> type) {
            return createByteBuddyClass(getFunctionalMethod(type));
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> T noop(Class<?> target) {
        Method m = getFunctionalMethod(target);
        if (m == null) {
            throw new IllegalArgumentException("Expect class to be a functional interface " + target);
        }


        try {
            return (T) BYTEBUDDIES.get(target).newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> createByteBuddyClass(Method m) {
        DynamicType.Builder<Object> subclass = new ByteBuddy().subclass(Object.class);

        Class<?> target = m.getDeclaringClass();
        for(TypeVariable<?> tv : target.getTypeParameters()) {
             subclass = subclass.typeVariable(tv.getName(), Object.class);
        }
        DynamicType.Builder.MethodDefinition.ImplementationDefinition<Object> method = subclass.implement(target).define(m);

        DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<Object> definition;
        if (m.getReturnType().isPrimitive()) {
            definition = method.intercept(FixedValue.value(DEFAULT_VALUES.get(m.getReturnType())));
        } else {
            definition = method.intercept(FixedValue.nullValue());
        }

        DynamicType.Unloaded<Object> make = definition.make();
//        try {
//            make.saveIn(new File("target/"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return make.load(target.getClassLoader()).getLoaded();
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
}
