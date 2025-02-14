package com.sebastienguillemin.whcreasoner.core.util;

import java.lang.Math;

public class MathUtil {
    public static float computeDiffPercentage(float x, float y) {
        return 100.0f * (Math.abs(x - y)) / ((x + y) / 2.0f);
    }
}
