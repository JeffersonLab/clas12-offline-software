package org.jlab.detector.base;

/**
 *
 * @author gavalian
 */
public enum DetectorType {
      
    UNDEFINED ( 0, "UNDEF"),
    BMT       ( 1, "BMT"),    
    BST       ( 2, "BST"),
    CND       ( 3, "CND"),
    CTOF      ( 4, "CTOF"),
    CVT       ( 5, "CVT"),
    DC        ( 6, "DC"),
    ECAL      ( 7, "ECAL"),
    FMT       ( 8, "FMT"),
    FT        ( 9, "FT"),
    FTCAL     (10, "FTCAL"),
    FTHODO    (11, "FTHODO"),
    FTOF      (12, "FTOF"),
    FTTRK     (13, "FTTRK"),
    HTCC      (15, "HTCC"),
    LTCC      (16, "LTCC"),
    RF        (17, "RF"),
    RICH      (18, "RICH"),
    RTPC      (19, "RTPC"),
    HEL       (20, "HEL"),
    BAND      (21, "BAND"),
    TARGET    (100, "TARGET"),
    ECIN      (110, "ECIN"),
    ECOUT     (111, "ECOUT"),
    ECTOT     (112, "ECTOT"),
    LAC       (113, "LAC"),
    SC        (114, "SC"),
    CC        (115, "CC");
    
    private final int detectorId;
    private final String detectorName;
    
    DetectorType(){
        detectorId = 0;
        detectorName = "UNDEFINED";
    }
    
    DetectorType(int id, String name){
        detectorId = id;
        detectorName = name;
    }
    
    /**
     * Returns the name of the detector.
     * @return the name of the detector
     */
    public String getName() {
        return detectorName;
    }
    
     /**
     * Returns the id number of the detector.
     * @return the id number of the detector
     */
    public int getDetectorId() {
        return detectorId;
    }
    
    public static DetectorType getType(String name) {
        name = name.trim();
        for(DetectorType id: DetectorType.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    public static DetectorType getType(Integer detId) {

        for(DetectorType id: DetectorType.values())
            if (id.getDetectorId() == detId) 
                return id;
        return UNDEFINED;
    }
}
