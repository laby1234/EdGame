package org.example.util;

public final class TimeUtil {

    private static final double FIXED_DELTA = 1.0 / 60.0;
    private static final double MIN_DELTA = 1.0 / 144.0;
    private static final double MAX_DELTA = 1.0 / 90.0;

    private TimeUtil() {
    }

    public static double stableDelta(double tpf) {
        if (tpf <= 0 || Double.isNaN(tpf) || Double.isInfinite(tpf)) {
            return FIXED_DELTA;
        }

        return Math.max(MIN_DELTA, Math.min(tpf, MAX_DELTA));
    }
}