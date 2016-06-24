/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.data.detector;

/**
 *
 * @author gavalian
 */
public enum DetectorType {
    
    UNDEFINED ( 0, "undefined"),
    BST       ( 1, "BST"),
    BMT       ( 1, "BMT"),
    CND       ( 3, "CND"),
    CTOF      ( 4, "CTOF"),
    FMT       ( 5,"FMT"),
    FTCAL     ( 9, "FTCAL"),
    FTOF      (10, "FTOF"),
    DC        (13, "DC"),
    EC        (15, "EC");
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
}
