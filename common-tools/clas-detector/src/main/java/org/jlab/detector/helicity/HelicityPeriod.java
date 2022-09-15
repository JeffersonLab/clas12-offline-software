package org.jlab.detector.helicity;

/**
 *
 * @author baltzell
 */
public enum HelicityPeriod {

    TSETTLE,
    TSTABLE,
    UDF;

    /**
     * Determine whether the clock looks more like tsettle or tstable periods.
     * All three just need to be in the same units.
     * @param clock
     * @param tsettle
     * @param tstable
     * @return the type of helicity period 
     */
    public static HelicityPeriod create(double clock, double tsettle, double tstable) {
        final double tdiff = tsettle - tstable;
        if (Math.abs(clock - tsettle) < tdiff/4) {
            return TSETTLE;
        }
        else if (Math.abs(clock - tstable) < tdiff/4) {
            return TSTABLE;
        }
        else {
            return UDF;
        }
    }

}

