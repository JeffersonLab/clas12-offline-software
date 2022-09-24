package org.jlab.detector.helicity;

/**
 *
 * @author baltzell
 */
public enum HelicityInterval {

    TSETTLE,
    TSTABLE,
    UDF;

    private static final double tolerance = 0.1;

    /**
     * Determine whether the clock looks more like tsettle or tstable intervals.
     * All three just need to be in the same units.
     * @param clock
     * @param tsettle
     * @param tstable
     * @return the type of helicity interval 
     */
    public static HelicityInterval createStrict(double clock, double tsettle, double tstable) {
        if (clock < 0) {
            return UDF;
        }
        if (Math.abs(clock-tsettle)/tsettle < tolerance) {
            return TSETTLE;
        }
        if (Math.abs(clock-tstable)/tstable < tolerance) {
            return TSTABLE;
        }
        return UDF;
    }

    public static HelicityInterval createLoose(double clock, double tsettle, double tstable) {
        if (clock < 0) {
            return UDF;
        }
        if (Math.abs(clock-tsettle)/tsettle < tolerance) {
            return TSETTLE;
        }
        return TSTABLE;
    }
}

