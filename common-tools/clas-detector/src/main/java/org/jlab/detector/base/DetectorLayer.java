package org.jlab.detector.base;

public class DetectorLayer {

    public static final byte CND_INNER=1;
    public static final byte CND_MIDDLE=2;
    public static final byte CND_OUTER=3;

    public static final byte PCAL_U=1; 
    public static final byte PCAL_V=2; 
    public static final byte PCAL_W=3;
    public static final byte PCAL_Z=9; // layer number used to define the longitudinal coordinate of the cluster

    public static final byte EC_INNER_U=4; 
    public static final byte EC_INNER_V=5; 
    public static final byte EC_INNER_W=6; 
    public static final byte EC_INNER_Z=9; // layer number used to define the longitudinal coordinate of the cluster 
    
    public static final byte EC_OUTER_U=7; 
    public static final byte EC_OUTER_V=8; 
    public static final byte EC_OUTER_W=9;
    public static final byte EC_OUTER_Z=9; // layer number used to define the longitudinal coordinate of the cluster

    public static final byte PCAL=PCAL_U;
    public static final byte EC_INNER=EC_INNER_U;
    public static final byte EC_OUTER=EC_OUTER_U;

    public static final byte FTOF1A=1;
    public static final byte FTOF1B=2;
    public static final byte FTOF2=3;

    public static final byte TARGET_CENTER=1;
    public static final byte TARGET_DOWNSTREAM=2;
    public static final byte TARGET_UPSTREAM=3;
    
    public static final byte FTTRK_MODULE1=1;
    public static final byte FTTRK_MODULE2=2;
    public static final byte FTTRK_LAYER1=1;
    public static final byte FTTRK_LAYER2=2;
    public static final byte FTTRK_LAYER3=3;
    public static final byte FTTRK_LAYER4=4;
    
}

