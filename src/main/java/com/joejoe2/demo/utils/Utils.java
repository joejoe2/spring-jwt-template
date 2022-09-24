package com.joejoe2.demo.utils;

import java.util.Random;

public class Utils {
    public static String randomNumericCode(int length){
        if (length<=0) throw new IllegalArgumentException("length range must be > 0 !");

        StringBuilder buffer = new StringBuilder(length);
        for (int d:new Random().ints(length, 0, 10).toArray()){
            buffer.append(d);
        }
        return buffer.toString();
    }
}
