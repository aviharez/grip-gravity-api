package com.project.gripgravity.model;

/**
 * V-scale bouldering grades from V0 to V17
 */
public enum Grade {
    V0, V1, V2, V3, V4, V5, V6, V7, V8, V9,
    V10, V11, V12, V13, V14, V15, V16, V17;

    /**
     * Returns the numeric value if this grade
     */
    public int numericValue() {
        return this.ordinal();
    }

    /**
     * Returns the Grade closest to the given numeric value.
     * Clamps to the valid range [0, 17].
     *
     * @param value the numeric value to round (e.g. average of submission)
     * @return the nearest valid grade
     */
    public static Grade fromNumeric(double value) {
        int rounded = (int) Math.round(value);
        int clamped = Math.max(0, Math.min(rounded, values().length - 1));
        return values()[clamped];
    }
}
