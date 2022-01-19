package org.jlab.rec.cvt.measurement;

import org.jlab.detector.base.DetectorType;

/**
 *
 * @author devita
 */
public enum MLayer {
    UNDEFINED(99,   "Undefined",              0),
    TARGET(0,       "Target",                 0),
    SHIELD(1,       "Tungsten Shield",        0),
    INNERSVTCAGE(2, "Inner SVT Faraday Cage", 0),
    SVTLAYER1(3,    "SVT Layer 1",            1),
    SVTLAYER2(4,    "SVT Layer 2",            2),
    SVTLAYER3(5,    "SVT Layer 3",            3),
    SVTLAYER4(6,    "SVT Layer 4",            4),
    SVTLAYER5(7,    "SVT Layer 5",            5),
    SVTLAYER6(8,    "SVT Layer 6",            6),
    OUTERSVTCAGE(9, "Outer SVT Faraday Cage", 0),
    BMTLAYER1(10,   "BMT Layer 1",            1),
    BMTLAYER2(11,   "BMT Layer 2",            2),
    BMTLAYER3(12,   "BMT Layer 3",            3),
    BMTLAYER4(13,   "BMT Layer 4",            4),
    BMTLAYER5(14,   "BMT Layer 5",            5),
    BMTLAYER6(15,   "BMT Layer 6",            6),
    COSMICPLANE(16, "Cosmic reference plane", 0);

    private final int id;
    private final String name;
    private final int layer;

    MLayer() {
        id = 99;
        name = "UNDEFINED";
        layer = 0;
    }

    MLayer(int id, String name, int layer) {
        this.id = id;
        this.name = name;
        this.layer = layer;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getLayer() {
        return layer;
    }

    public int getIndex() {
        return this.getIndex(0);
    }

    public int getIndex(int hemisphere) {
        switch (hemisphere) {
            case 1:
                return COSMICPLANE.id - id;
            case -1:
                return COSMICPLANE.id + id - 1;
            default:
                return id;
        }
    }

    public static int getId(int index, int hemisphere) {
        switch (hemisphere) {
            case 1:
                return COSMICPLANE.id - index;
            case -1:
                return index - COSMICPLANE.id + 1;
            default:
                return index;
        }
    }

    public static int getHemisphere(int index) {
        if (index < COSMICPLANE.id) {
            return 1;
        } else {
            return -1;
        }
    }

    public static DetectorType getDetectorType(int layId) {
        if (layId >= SVTLAYER1.getId() && layId <= SVTLAYER6.getId()) {
            return DetectorType.BST;
        } else if (layId >= BMTLAYER1.getId() && layId <= BMTLAYER6.getId()) {
            return DetectorType.BMT;
        } else {
            return DetectorType.UNDEFINED;
        }
    }

    public static MLayer getType(String name) {
        name = name.trim();
        for (MLayer id : MLayer.values()) {
            if (id.getName().equalsIgnoreCase(name)) {
                return id;
            }
        }
        return UNDEFINED;
    }

    public static MLayer getType(Integer layId) {

        for (MLayer id : MLayer.values()) {
            if (id.getId() == layId) {
                return id;
            }
        }
        return UNDEFINED;
    }

    public static MLayer getType(DetectorType type, int layer) {
        for (MLayer id : MLayer.values()) {
            if (id.getLayer() == layer && MLayer.getDetectorType(id.getId()) == type) {
                return id;
            }
        }
        return UNDEFINED;
    }

}
