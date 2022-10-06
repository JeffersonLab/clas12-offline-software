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
        switch (bit) {
            case PLUS:
                return MINUS;
            case MINUS:
                return PLUS;
            case DNE:
                return DNE;
            default:
                return UDF;
        }
    }

    public static HelicityBit createFromRawBit(byte bit) {
        switch (bit) {
            case 0:
                return MINUS;
            case 1:
                return PLUS;
            default:
                return UDF;
        }
        
    }
}
