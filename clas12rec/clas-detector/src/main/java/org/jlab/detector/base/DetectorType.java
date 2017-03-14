/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

/**
 *
 * @author gavalian
 */
public enum DetectorType {
      
    UNDEFINED ( 0, "UNDEF"),
    BST       ( 1, "BST"),    
    BMT       ( 2, "BMT"),
    CND       ( 3, "CND"),
    CTOF      ( 4, "CTOF"),
    FMT       ( 5,"FMT"),
    HTCC      ( 6,"HTCC"),
    FTHODO    ( 8,"FTHODO"),
    FTCAL     ( 9, "FTCAL"),
    FTOF1A    (10, "FTOF1A"),
    FTOF1B    (11, "FTOF1B"),
    FTOF2     (12, "FTOF2"),
    DC        (13, "DC"),
    LTCC      (14, "LTCC"),
    PCAL      (15, "PCAL"),
    EC        (16, "EC"),
    FTOF      (17, "FTOF"),
    RF        (21,"RF"),
    ECIN      (110, "ECIN"),
    ECOUT     (111, "ECOUT"),
    ECTOT     (112, "ECTOT"),
    LAC       (113, "LAC"),
    SC        (114, "SC"),
    CC        (115, "CC"),
    SVT       (220, "SVT");
    
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
