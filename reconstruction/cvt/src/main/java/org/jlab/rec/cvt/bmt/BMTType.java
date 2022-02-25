package org.jlab.rec.cvt.bmt;

/**
 *
 * @author devita
 */
public enum BMTType {
      
    UNDEFINED ( 0, "UNDEF"),
    C         ( 1, "C"),    
    Z         ( 2, "Z");
    
    private final int detectorId;
    private final String detectorName;
    
    BMTType(){
        detectorId = 0;
        detectorName = "UNDEFINED";
    }
    
    BMTType(int id, String name){
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
    
    public static BMTType getType(String name) {
        name = name.trim();
        for(BMTType id: BMTType.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    public static BMTType getType(Integer detId) {

        for(BMTType id: BMTType.values())
            if (id.getDetectorId() == detId) 
                return id;
        return UNDEFINED;
    }
}
