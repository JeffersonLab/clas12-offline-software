package org.jlab.detector.geant4.v2.URWELL;


import org.jlab.detector.calib.utils.DatabaseConstantProvider; 
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;


public class URWellConstants {

    private final static String CCDBPATH = "/geometry/urwell/";
    
    public final static int NMAXREGIONS  = 2;    //number of regions 
    public final static int NSECTORS  = 6;    //number of sectors
    public final static int NLAYERS   = 2;    //number of layers
    public final static int NCHAMBERS = 3;    //number of chambers in a sector

    public final static double XENLARGEMENT = 0.5; // cm
    public final static double YENLARGEMENT = 1.;  // cm
    public final static double ZENLARGEMENT = 0.1; // cm
   
    // Sector geometrical parameters
    public final static double THOPEN = 54.;           // opening angle between endplate planes (deg)
    public final static double THTILT = 25;            // theta tilt (deg)
    public final static double THMIN  = 4.694;         // polar angle to the base of first chamber (deg)
    public final static double SECTORHEIGHT = 146.21;  //height of each sector (cm)
    public final static double DX0CHAMBER0  = 5.197;   // halfbase of chamber 1  (cm)
  
    // Chamber volumes  and materials (units are cm)
    public final static double[] CHAMBERVOLUMESTHICKNESS = {0.0025, 0.0005,0.3,                                // window
                                                            0.0025, 0.0005,0.4,                                // cathode
                                                            0.0005, 0.005, 0.0005,                             // uRWell + DlC
                                                            0.0005, 0.005, 0.0005,                             // Capacitive sharing layer1
                                                            0.0005, 0.005, 0.0005,                             // Capacitive sharing layer2
                                                            0.005,  0.0005,0.005, 0.005,  0.0005,0.005, 0.005, // Readout
                                                            0.0127, 0.3, 0.0125};                              // support
    public final static String[] CHAMBERVOLUMESNAME = {"window_kapton", "window_Al", "window_gas",
           "cathode_kapton", "cathode_Al", "cathode_gas",
           "muRwell_Cu", "muRwell_kapton", "muRwell_dlc", 
           "capa_sharing_layer1_glue","capa_sharing_layer1_Cr","capa_sharing_layer1_kapton",
           "capa_sharing_layer2_glue","capa_sharing_layer2_Cr","capa_sharing_layer2_kapton",
           "readout1_glue", "readout1_Cu", "readout1_kapton", "readout2_glue", "readout2_Cu", "readout2_kapton", "readout3_glue",
           "support_skin1_g10", "support_honeycomb_nomex", "support_skin2_g10"};

    // URWELL position in the CLAS12 frame 
    public final static double TGT2DC0    = 228.078; // cm            
   // public final static double URWELL2DC0 = 2;       // cm
    public final static double URWELL2DC0[] = new double[NMAXREGIONS];
    public final static double DIST2TGT[] = new double[NMAXREGIONS];
    public final static double W2TGT[] = new double[NMAXREGIONS];; 
    public final static double YMIN[] = new double[NMAXREGIONS];
    public final static double ZMIN[] = new double[NMAXREGIONS];
    
  //  public final static double DIST2TGT   = (TGT2DC0-URWELL2DC0);
   // public final static double W2TGT = DIST2TGT/Math.cos(Math.toRadians(THTILT-THMIN));
  //  public final static double YMIN = W2TGT*Math.sin(Math.toRadians(THMIN)); // distance from the base chamber1 and beamline
  //  public final static double ZMIN = W2TGT*Math.cos(Math.toRadians(THMIN));   
    public final static double PITCH = 0.1 ;       // cm
    public final static double STEREOANGLE = 10;   // deg
    
    
    
        // URWELL - PROTO information // 
    /*
    public final static double DX0_PROTO = 101.2; //cm
    public final static double DX1_PROTO = 146.2; //cm
    public final static double HEIGHT_PROTO = 50; //cm
   */
    // public final static int THILT_PROTO;            // theta tilt (deg)
    public final static int NREGIONS_PROTO  = 1;    //number of regions 
    public final static int NSECTORS_PROTO  = 1;    //number of sectors
    public final static int NCHAMBERS_PROTO = 1;    //number of chambers in a sector
    
    
        // URWELL-PROTO position in CLAS12 frame
    
    // 4 URWELL-PROTO vertex - sensitive plane
    public static Point3D Apoint = new Point3D(22.9545, -337.005, 465.9); // {X,Y,Z}
    public static Point3D Bpoint = new Point3D(148.906, -264.288, 465.9); // {X,Y,Z}
    public static Point3D Cpoint = new Point3D(152.047, -314.276, 445.0); // {X,Y,Z}
    public static Point3D Dpoint = new Point3D(64.676, -364.719, 445.0); // {X,Y,Z}
    
    /*
     * @return String a path to a directory in CCDB of the format {@code "/geometry/detector/"}
     */
    public static String getCcdbPath()
    {
            return CCDBPATH;
    }



     /**
     * Loads the the necessary tables for the URWELL geometry for a given DatabaseConstantProvider.
     * 
     * @return DatabaseConstantProvider the same thing
     */
    public static DatabaseConstantProvider connect( DatabaseConstantProvider cp )
    {
          //  cp.loadTable( CCDBPATH +"RWELL");

            load(cp  );
            return cp;
    }

    /**
     * Reads all the necessary constants from CCDB into static variables.
     * Please use a DatabaseConstantProvider to access CCDB and load the following tables:
     * @param cp a ConstantProvider that has loaded the necessary tables
     */
    
    public static synchronized void load( DatabaseConstantProvider cp )
    {
            // read constants from svt table
//            NREGIONS = cp.getInteger( CCDBPATH+"svt/nRegions", 0 );

             for (int i=0; i<NMAXREGIONS; i++){
                 
                URWELL2DC0[NMAXREGIONS-i-1] =  2+i*2;
                DIST2TGT[NMAXREGIONS-i-1]   = (TGT2DC0-URWELL2DC0[NMAXREGIONS-i-1]);
                W2TGT[NMAXREGIONS-i-1] = DIST2TGT[NMAXREGIONS-i-1]/Math.cos(Math.toRadians(THTILT-THMIN));
                YMIN[NMAXREGIONS-i-1]= W2TGT[NMAXREGIONS-i-1]*Math.sin(Math.toRadians(THMIN)); // distance from the base chamber1 and beamline
                ZMIN[NMAXREGIONS-i-1] = W2TGT[NMAXREGIONS-i-1]*Math.cos(Math.toRadians(THMIN));  

                }

    
    }


}
