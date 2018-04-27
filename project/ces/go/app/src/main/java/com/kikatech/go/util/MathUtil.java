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

    public static double distance(int[] pair1, int[] pair2) {
        return distance(pair1[0], pair1[1], pair2[0], pair2[1]);
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
