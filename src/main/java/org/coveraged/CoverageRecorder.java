package org.coveraged;

public class CoverageRecorder {
    public static void main() {

    }

    public static <T> T wrap(T val, String where) {
        System.out.println(where);
        return val;
    }
}
