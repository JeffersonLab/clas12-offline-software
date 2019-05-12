package org.jlab.detector.helicity;

/**
 * Just to force a convention and avoid confusion on helicity bit definitions.
 *
 * These numeric values are also what goes in data banks.
 *
 * @author baltzell
 */
public enum HelicityBit {

    DNE   (  9 ),
    UDF   (  0 ),
    PLUS  (  1 ),
    MINUS ( -1 );

    private final int value;

    HelicityBit(int value) {
        this.value=value;
    }

    public byte value() { return (byte)this.value; }

    public static HelicityBit create(byte value) {
        for(HelicityBit hb: HelicityBit.values()) {
            if (hb.value() == value) return hb;
        }
        return UDF;
    }

    public static HelicityBit getFlipped(HelicityBit bit) {
        if (bit==PLUS) return MINUS;
        if (bit==MINUS) return PLUS;
        return UDF;
    }

}
