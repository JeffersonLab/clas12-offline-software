package org.jlab.detector.helicity;

/**
 * This is the same numbering/naming convention used by CEBAF's EPICS
 * variable HELPATTERNd, the helicity board manual, and in our CCDB
 * table /runcontrol/helicity.
 *
 * @author baltzell
 */
public enum HelicityPattern {
	
    UDF     ( -1 ),
    PAIR    ( 0 ),
    QUARTET ( 1 ),
    OCTET   ( 2 ),
    TOGGLE  ( 3 );

    private final int value;

    HelicityPattern(int value) {
        this.value=value;
    }

    public byte value() {
        return (byte)this.value;
    }

    public static HelicityPattern create(byte value) {
        for(HelicityPattern hp: HelicityPattern.values()) {
            if (hp.value() == value) return hp;
        }
        return UDF;
    }

}

