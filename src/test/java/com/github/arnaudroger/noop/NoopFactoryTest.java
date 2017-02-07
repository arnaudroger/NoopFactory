package com.github.arnaudroger.noop;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static org.junit.Assert.*;

public class NoopFactoryTest {
    @Test
    public void noop() throws Exception {
        Function<Integer, String> function = NoopFactory.noop(Function.class);
        assertNull(function.apply(3));
    }

    @Test
    public void noopToInt() throws Exception {
        ToIntFunction<Void> function = NoopFactory.noop(ToIntFunction.class);
        assertEquals(0, function.applyAsInt(null));
    }

    @Test
    public void noopToVoid() throws Exception {
        IntFunction<Void> function = NoopFactory.noop(IntFunction.class);
        assertNull(function.apply(1));
    }



    @Test
    public void testIsFunctionalInterface() throws NoSuchMethodException {
        assertEquals(Function.class.getMethod("apply", Object.class), NoopFactory.getFunctionalMethod(Function.class));
        assertEquals(CustomFunction.class.getMethod("doWhat", Void.class), NoopFactory.getFunctionalMethod(CustomFunction.class));
        assertEquals(CustomFunctionWithEquals.class.getMethod("doWhat", Void.class), NoopFactory.getFunctionalMethod(CustomFunctionWithEquals.class));
        assertNull(NoopFactory.getFunctionalMethod(CustomClass.class));
        assertNull(NoopFactory.getFunctionalMethod(Custom2Methods.class));
    }

    private interface CustomFunction {
        void doWhat(Void v);
    }

    private interface CustomFunctionWithEquals {
        void doWhat(Void v);
        boolean equals(Object o);
    }

    private interface Custom2Methods {
        void doWhat(Void v);
        boolean test(Object o);
    }


    private abstract class CustomClass {
        abstract void doWhat(Void v);
    }

}