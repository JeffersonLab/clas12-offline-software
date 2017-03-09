/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

/**
 *
 * @author gavalian
 */
public class EBConstants {
    
    public static final double  PCAL_MATCHING = 15.0; // matching distance in PCAL
    public static final double  ECIN_MATCHING = 15.0; // matching distance in PCAL
    public static final double ECOUT_MATCHING = 15.0; // matching distance in PCAL
    
    public static final double  FTOF_MATCHING_1A = 18.0; // matching for layer 1A of FTOF
    public static final double  FTOF_MATCHING_1B = 18.0; // matching for layer 1A of FTOF
    public static final double  FTOF_MATCHING_2 = 18.0; // matching for layer 1A of FTOF
    
    public static final double ECAL_SAMPLINGFRACTION = 0.24;
    public static final double ECAL_SAMPLINGFRACTION_CUT = 0.218;
    
    public static final double      RF_BUCKET_LENGTH = 2.004;
    public static final double             RF_OFFSET = 0.0;
    public static final int                 RF_SHIFT = 800;   // NOT USED
    public static final int                RF_CYCLES = 80;   
    public static final int                    RF_ID = 1;     // signal ID that will be used in reconstruction
    public static final double           RF_TDC2TIME = 0.0234358;
    
    public static final double        SPEED_OF_LIGHT = 29.9792;
    
    
}
