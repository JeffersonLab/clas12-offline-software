package org.jlab.detector.helicity;

/**
 *
 * @author baltzell
 */
public enum HelicityInterval {

    TSETTLE,
    TSTABLE,
    UDF;

    /**
     * Determine whether the clock looks like tsettle or tstable intervals within 
     * a given relative tolerance.  All three times just need to be in the same units.
     * @param tolerance
     * @param clock
     * @param tsettle
     * @param tstable
     * @return the type of helicity interval 
     */
    public static HelicityInterval createStrict(double tolerance, double clock, double tsettle, double tstable) {
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

    /**
     * Determine whether the clock looks more like tsettle within a given 
     * tolerance, else assume it's tstable.  All three times just need to be
     * in the same units.
     * @param tolerance
     * @param clock
     * @param tsettle
     * @param tstable
     * @return the type of helicity interval 
     */
    public static HelicityInterval createLoose(double tolerance, double clock, double tsettle, double tstable) {
        if (clock < 0) {
            return UDF;
        }
        if (Math.abs(clock-tsettle)/tsettle < tolerance) {
            return TSETTLE;
        }
        return TSTABLE;
    }
    
}

