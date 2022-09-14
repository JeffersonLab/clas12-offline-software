package org.jlab.detector.helicity;

/**
 *
 * @author baltzell
 */
public class HelicityPeriod {
    
    public enum Period {
        TSETTLE,
        TSTABLE,
        UDF
    };

    /**
     * Determine whether the clock looks more like tsettle or tstable periods.
     * @param clock
     * @param tsettle
     * @param tstable
     * @return the type of helicity period 
     */
    public static Period getHelicityPeriod(double clock, double tsettle, double tstable) {
        final double tdiff = tsettle - tstable;
        if (Math.abs(clock - tsettle) < tdiff/4) {
            return Period.TSETTLE;
        }
        else if (Math.abs(clock - tstable) < tdiff/4) {
            return Period.TSTABLE;
        }
        else {
            return Period.UDF;
        }
    }

}

