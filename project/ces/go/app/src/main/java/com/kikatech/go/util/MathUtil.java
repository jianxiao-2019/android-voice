package com.kikatech.go.util;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * @author SkeeterWang Created on 2018/4/23.
 */

public class MathUtil {

    public static int min(@NonNull int... numbers) {
        if (numbers.length > 0) {
            final int[] copyData = new int[numbers.length];
            System.arraycopy(numbers, 0, copyData, 0, numbers.length);
            Arrays.sort(copyData);
            return copyData[0];
        }
        return 0;
    }
}
