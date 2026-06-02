package org.example.util;

public final class TimeUtil {

    private static final double FIXED_DELTA = 1.0 / 60.0;
    private static final double MAX_DELTA = 1.0 / 30.0;

    private TimeUtil() {
    }

    public static double stableDelta(double tpf) {
        if (tpf <= 0 || tpf > MAX_DELTA) {
            return FIXED_DELTA;
        }
        return tpf;
    }
}
