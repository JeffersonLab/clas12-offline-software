package cnuphys.ced.geometry.bmt;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

public class ConstantsLoader {

	public ConstantsLoader() {
		// TODO Auto-generated constructor stub
	}

	static boolean CSTLOADED = false;
	
	public static boolean DEBUG = true;

	// static FTOFGeant4Factory geometry ;

	private static DatabaseConstantProvider DB;
	static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(
			10, "default");

	public static final synchronized void Load(int runNb) {
		// initialize the constants
		//Z detector characteristics
		int NREGIONS = 3;
		double[] CRZRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
		int[] CRZNSTRIPS = new int[NREGIONS]; 			// the number of strips
		double[] CRZSPACING = new double[NREGIONS]; 	// the strip spacing in mm
		double[] CRZWIDTH = new double[NREGIONS]; 		// the strip width in mm
		double[] CRZLENGTH = new double[NREGIONS]; 		// the strip length in mm
		double[] CRZZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
		double[] CRZZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
//		double[] CRZOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
		double[][] CRZEDGE1 = new double[NREGIONS][3]; 	// the angle of the first edge of each PCB detector A, B, C
		double[][] CRZEDGE2 = new double[NREGIONS][3]; 	// the angle of the second edge of each PCB detector A, B, C
//		double[] CRZXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

		//C detector characteristics
		double[] CRCRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
		int[] CRCNSTRIPS = new int[NREGIONS]; 			// the number of strips
		double[] CRCSPACING = new double[NREGIONS]; 	// the strip spacing in mm
		double[] CRCLENGTH = new double[NREGIONS]; 		// the strip length in mm
		double[] CRCZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
		double[] CRCZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
	//	double[] CRCOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
		int[][] CRCGROUP = new int[NREGIONS][100]; 		// Number of strips with same width
		double[][] CRCWIDTH = new double[NREGIONS][100];	// the width of the corresponding group of strips 
		double[][] CRCEDGE1 = new double[NREGIONS][3]; 	// the angle of the first edge of each PCB detector A, B, C
		double[][] CRCEDGE2 = new double[NREGIONS][3]; 	// the angle of the second edge of each PCB detector A, B, C
//		double[] CRCXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

		// Load the tables
		dbprovider = new DatabaseConstantProvider(runNb, "default"); // reset
																		// using
																		// the
																		// new
																		// run
		// load the geometry tables
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_layer");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L1");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L2");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L3");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L4");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L5");
		dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L6");
		dbprovider.disconnect();

		// dbprovider.show();

		// Getting the Constants
		// 1) Layer info 
		for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_layer/Layer"); i++) {

			int layer = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Layer", i);
			int region = (layer+1)/2;
			
			double radius = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Radius", i);
			double Zmin = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Zmin", i);
			double Zmax = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Zmax", i);
			double spacing = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Interstrip", i);
			int axis = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Axis", i);
			int Nstrips = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer/Nstrip", i);
			
			double Phi_min = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Phi_min", i);
			double Phi_max = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer/Phi_max", i);
			
			//double[] EDGE1	=	new double[]{Math.toRadians(Phi_min),Math.toRadians(Phi_min+240),Math.toRadians(Phi_min+120)};
			//double[] EDGE2	=	new double[]{Math.toRadians(Phi_max),Math.toRadians(Phi_max-120),Math.toRadians(Phi_max+120)};
			//sector clocking fix
			double[] EDGE1	=	new double[]{Math.toRadians(Phi_min+120),Math.toRadians(Phi_min),Math.toRadians(Phi_min+240)};
			double[] EDGE2	=	new double[]{Math.toRadians(Phi_max+120),Math.toRadians(Phi_max),Math.toRadians(Phi_max-120)};
			spacing =0;
			if(axis == 1) { //Z-detector
				CRZRADIUS[region-1] = radius;
				CRZNSTRIPS[region-1] = Nstrips;
				CRZZMIN[region-1] = Zmin;
				CRZZMAX[region-1] = Zmax;
				CRZLENGTH[region-1] = Zmax-Zmin;
				CRZSPACING[region-1] = spacing;
				CRZEDGE1[region-1]	=	EDGE1;
				CRZEDGE2[region-1]	=	EDGE2;
			}
			if(axis == 0) { //C-detector
				CRCRADIUS[region-1] = radius;
				CRCNSTRIPS[region-1] = Nstrips;
				CRCZMIN[region-1] = Zmin;
				CRCZMAX[region-1] = Zmax;
				CRCLENGTH[region-1] = Zmax-Zmin;
				CRCSPACING[region-1] = spacing;
				CRCEDGE1[region-1]	=	EDGE1;
				CRCEDGE2[region-1]	=	EDGE2;
			}
			

		}
		
		// Getting the Constants
		// 2) pitch info 
		for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_strip_L1/Group_size"); i++) {
			CRCGROUP[0][i] = dbprovider.getInteger("/geometry/cvt/mvt/bmt_strip_L1/Group_size", i);
			CRCWIDTH[0][i] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L1/Pitch", i);
		}
		for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_strip_L4/Group_size"); i++) {
			CRCGROUP[1][i] = dbprovider.getInteger("/geometry/cvt/mvt/bmt_strip_L4/Group_size", i);
			CRCWIDTH[1][i] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L4/Pitch", i);
		}
		for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_strip_L6/Group_size"); i++) {
			CRCGROUP[2][i] = dbprovider.getInteger("/geometry/cvt/mvt/bmt_strip_L6/Group_size", i);
			CRCWIDTH[2][i] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L6/Pitch", i);
		}
	 
		CRZWIDTH[0] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L2/Pitch", 0);
		CRZWIDTH[1] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L3/Pitch", 0);
		CRZWIDTH[2] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L5/Pitch", 0);
		
		Constants.setCRCRADIUS(CRCRADIUS);
		Constants.setCRZRADIUS(CRZRADIUS);
		
		if (DEBUG) {
			System.out.println("-- [CSTRIPS] --");
			for (int i = 0; i < CRCRADIUS.length; i++) {
				System.out.println("[DEBUG] CRCRADIUS[" + i + "] = " + CRCRADIUS[i]);
			}
			System.out.println("----");
			for (int i = 0; i < CRZRADIUS.length; i++) {
				System.out.println("[DEBUG] CRZRADIUS[" + i + "] = " + CRZRADIUS[i]);
			}
		}
		
		Constants.setCRZNSTRIPS(CRZNSTRIPS);
		Constants.setCRZZMIN(CRZZMIN);
		Constants.setCRZZMAX(CRZZMAX);
		Constants.setCRZLENGTH(CRZLENGTH);
		Constants.setCRZSPACING(CRZSPACING);
		Constants.setCRZEDGE1(CRZEDGE1);
		Constants.setCRZEDGE2(CRZEDGE2);
		Constants.setCRCRADIUS(CRCRADIUS);
		Constants.setCRCNSTRIPS(CRCNSTRIPS);
		Constants.setCRCZMIN(CRCZMIN);
		Constants.setCRCZMAX(CRCZMAX);
		Constants.setCRCLENGTH(CRCLENGTH);
		Constants.setCRCSPACING(CRCSPACING);
		Constants.setCRCEDGE1(CRCEDGE1);
		Constants.setCRCEDGE2(CRCEDGE2);
		Constants.setCRCGROUP(CRCGROUP);
		Constants.setCRCWIDTH(CRCWIDTH);
		Constants.setCRZWIDTH(CRZWIDTH);
		
		CSTLOADED = true;
		System.out
				.println("SUCCESSFULLY LOADED BMT CONSTANTS....");
		setDB(dbprovider);
	}

	public static final synchronized DatabaseConstantProvider getDB() {
		return DB;
	}

	public static final synchronized void setDB(DatabaseConstantProvider dB) {
		DB = dB;
	}
}
