package org.jlab.detector.banks;

/**
 * The second (and third) digits of ADC/TDC bank's order variable can be used to
 * encode additional information (as long as the first digit is left unmanipulated).
 * This defines the convention for those higher digits in production data.
 *
 * @author baltzell
 */
public enum RawOrderType {

    UNDEFINED  ( -1, "UDFF"),
    A0         (  0, "0"),    
    BACKGROUND ( 10, "BACKGROUND"),    
    DENOISED   ( 20, "DENOISED"),
    A3         ( 30, "3"),
    A4         ( 40, "4"),
    A5         ( 50, "5"),
    A6         ( 60, "6"),
    A7         ( 70, "7"),
    A8         ( 80, "8"),
    A9         ( 90, "9");

    private final int rawOrderId;
    private final String rawOrderName;

    RawOrderType(){
        rawOrderId = 0;
        rawOrderName = "UNDEFINED";
    }

    RawOrderType(int id, String name){
        rawOrderId = id;
        rawOrderName = name;
    }

    /**
     * Returns the name of the detector.
     * @return the name of the detector
     */
    public String getName() {
        return rawOrderName;
    }

    /**
     * Returns the id number of the detector.
     * @return the id number of the detector
     */
    public int getTypeId() {
        return rawOrderId;
    }

    public static RawOrderType getType(String name) {
        name = name.trim();
        for(RawOrderType id: RawOrderType.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }

    public static RawOrderType getType(Integer orderId) {

        for(RawOrderType id: RawOrderType.values())
            if (id.getTypeId() == orderId) 
                return id;
        return UNDEFINED;
    }

}

