package org.jlab.detector.geant4.v2.URWELL;


import org.jlab.detector.calib.utils.DatabaseConstantProvider; // coatjava-3.0



public class URWellConstants {

    private static String ccdbPath = "/geometry/urwell/";
    
    public static double x_enlargement = 0.5;
    public static double y_enlargement = 1.;
    public static double z_enlargement = 0.1;
   
    public static int nSectors = 6;    //number of sectors
    public static int nChambers = 3;              //number of chambers

    // Sector geometrical parameters
    public static double th_open = 54.; // opening angle between endplate planes
    public static double th_tilt = 25; // theta tilt
    public static double th_min = 4.694; // polar angle to the base of first chamber
    public static double sector_height = 146.21;  //height of each sector
    public static double dx0_chamber0 = 5.197;    // halfbase of chamber 1 
  
    // Chamber volumes  and materials 
    public static double[] chamber_volumes_thickness = {0.0025, 0.0005, 0.3,        // window
        0.0025, 0.0005,0.4,                                                        // cathode
        0.0005, 0.005, 0.0005,                                                      //uRWell + DlC
        0.0005, 0.005, 0.0005,                                                      // Capacitive sharing layer1
        0.0005, 0.005, 0.0005,                                                      // Capacitive sharing layer2
        0.005,  0.0005,0.005, 0.005,  0.0005,0.005, 0.005,                           // Readout
        0.0127, 0.3, 0.0125};                                                       // support
    public static String[] chamber_volumes_string = {"window_kapton", "window_Al", "window_gas",
           "cathode_kapton", "cathode_Al", "cathode_gas",
           "muRwell_Cu", "muRwell_kapton", "muRwell_dlc", 
           "capa_sharing_layer1_glue","capa_sharing_layer1_Cr","capa_sharing_layer1_kapton",
           "capa_sharing_layer2_glue","capa_sharing_layer2_Cr","capa_sharing_layer2_kapton",
           "readout1_glue", "readout1_Cu", "readout1_kapton", "readout2_glue", "readout2_Cu", "readout2_kapton", "readout3_glue",
           "support_skin1_g10", "support_honeycomb_nomex", "support_skin2_g10"};

    // URWELL position in the CLAS12 frame 
    public static double distance_tgt_2_dc0 = 228.078;
    public static double distance_urwell2dc0 = 2;
    public static double dist_2_tgt = (distance_tgt_2_dc0-distance_urwell2dc0);
    public static double  w2tgt = dist_2_tgt/Math.cos(Math.toRadians(th_tilt-th_min));
    public static double  y_min = w2tgt*Math.sin(Math.toRadians(th_min)); // distance from the base chamber1 and beamline
    public static double  z_min = w2tgt*Math.cos(Math.toRadians(th_min)); 

    
    public static double strip_pitch = 0.1 ; 
    public static double strip_stereo_angle = 10;
    
         /*
	 * @return String a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
	public static String getCcdbPath()
	{
		return ccdbPath;
	}
	
	
	/**
	 * Sets the path to the database directory that contains the core parameters and constants for the uRWELL
	 * 
	 * @param aPath a path to a directory in CCDB of the format {@code "/geometry/detector/"}
	 */
        public static void setCcdbPath( String aPath )
	{
		ccdbPath = aPath;
	}

         /**
	 * Loads the the necessary tables for the URWELL geometry for a given DatabaseConstantProvider.
	 * 
	 * @return DatabaseConstantProvider the same thing
	 */
	public static DatabaseConstantProvider connect( DatabaseConstantProvider cp )
	{
              //  cp.loadTable( ccdbPath +"RWELL");
  
		load( cp );
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
          //      NREGIONS = cp.getInteger( ccdbPath+"svt/nRegions", 0 );

        }
}
