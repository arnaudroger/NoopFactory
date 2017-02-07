package com.github.arnaudroger.noop;

import java.util.function.Function;

import static org.junit.Assert.*;

public class NoopFactoryTest {
    @org.junit.Test
    public void noop() throws Exception {

        Function<Integer, String> function = NoopFactory.noop(Function.class);
        assertNull(function.apply(3));
    }

}