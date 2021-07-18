package org.jlab.geom;

/**
 * The unique identifier of a type of detector. {@code DetectorId} is an
 * enumeration to ensure that within the geometry package there are can be no
 * ambiguity about what kind of detector is being referenced by a
 * {@code DetectorId}. {@code DetectorId} also associates an id number and a
 * short {@code String} name with each type of detector. Detector id numbers are
 * loosely based on the CLAS12 Bank Numbering scheme. {@link #UNDEFINED} is used
 * to that there is no such detector could be found via whatever mechanism was
 * being used to retrieve a {@code DetectorId} (e.g.
 * {@link #getId(java.lang.String)}}).
 * @author jnhankins
 */
public enum DetectorId implements Showable {
    UNDEFINED ( 0, "undefined"),
    BST       ( 1, "BST"),
    CND       ( 3, "CND"),
    RICH      ( 6, "RICH"),
    FMT       ( 8, "FMT"),
    FTCAL     ( 9, "FTCAL"),
    CTOF      (11, "CTOF"),
    FTOF      (10, "FTOF"),
    DC        (13, "DC"),
    EC        (15, "EC");
    
    // Detector id numbers should be based on the CLAS12 Bank Numbering scheme
    private final int detectorId;
    private final String detectorName;
    
    DetectorId(int id, String name) {
        detectorId = id;
        detectorName = name;
    }
    
    /**
     * Returns the id number of the detector.
     * @return the id number of the detector
     */
    public int getIdNumber() {
        return detectorId;
    }
    
    /**
     * Returns the name of the detector.
     * @return the name of the detector
     */
    public String getName() {
        return detectorName;
    }
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return String.format("%15s (#%d)", detectorName, detectorId);
    }
    
    /**
     * Returns the DetectorId corresponding to the given name or UNDEFINED if no
     * DetectorId matches the given name.
     * @param name the name of the DetectrId to find
     * @return the corresponding DetectorId or UNDIFIED if not found
     */
    public static DetectorId getId(String name) {
        name = name.trim();
        for(DetectorId id: DetectorId.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    
    /**
     * Returns the DetectorId corresponding to the given id number or UNDEFINED 
     * if no DetectorId matches the given id number.
     * @param number the id number of the the DetectrId to find
     * @return the corresponding DetectorId or UNDIFIED if not found
     */
    public static DetectorId getId(int number) {
        for(DetectorId id: DetectorId.values())
            if (id.getIdNumber() == number)
                return id;
        return UNDEFINED;
    }
}
