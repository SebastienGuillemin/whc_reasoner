package com.sebastienguillemin.whcreasoner.core.util;

import java.util.TreeSet;

public class Util {
    public static <T> T pop(TreeSet<T> set) {
        T e = set.first();
        set.remove(e);

        return e;
    }
}
