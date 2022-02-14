package org.jlab.clas.tracking.kalmanfilter;

/**
 *
 * @author ziegler
 */
public enum Type {
    UDF(-1), PLANEWITHPOINT(0), PLANEWITHLINE(1), PLANEWITHSTRIP(2),
    CYLINDERWITHPOINT(3), CYLINDERWITHLINE(4), CYLINDERWITHARC(5), 
    CYLINDERWITHSTRIP(6), LINE(7);
    private final int value;

    Type(int value) {
        this.value = value;
    }

    public byte value() {
        return (byte) this.value;
    }

    public static Type create(byte value) {
        for (Type hp : Type.values()) {
            if (hp.value() == value) {
                return hp;
            }
        }
        return UDF;
    }
    
}
