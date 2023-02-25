package org.jlab.detector.banks;

/**
 * The second and third digits of ADC/TDC bank's order variable can be used
 * to encode additional information (as long as the first digit is left
 * unmanipulated), and this enum just defines the convention for those higher
 * digits in production data.
 *
 * These order categories are all mutually exclusive, and denoising is
 * categorized into levels, from 0 (least noisy) to 4 (most noisy).
 * 
 * @author baltzell
 */
public enum RawOrderType {

    NOISE0          (  0),  // normal hits retained by denoising level-0
    BGADDED         ( 10),  // hits added by background merging
    BGREMOVED       ( 20),  // hits removed during background merging 
    RESERVED        ( 30),  // reserved for later use
    NOISE1          ( 40),  // normal hits retained by level-1 denoising
    NOISE2          ( 50),  // normal hits retained by level-2 denoising
    NOISE3          ( 60),  // normal hits retained by level-3 denoising
    BGADDED_NOISE1  ( 70),  // background hits retained by level-1 denoising
    BGADDED_NOISE2  ( 80),  // background hits retained by level-2 denoising
    BGADDED_NOISE3  ( 90),  // background hits retained by level-3 denoising
    USER1           (100),
    USER2           (110),
    USER3           (120);

    private final int rawOrderId;

    private RawOrderType(int id){
        rawOrderId = id;
    }

    public int getTypeId() {
        return rawOrderId;
    }

}

