package cnuphys.ced.micromegas;

public class Constants {

    public Constants() {

    }

    /*
     * The algorithm to describe the geometry of the Barrel Micromegas is
     * provided by Franck Sabatie and implemented into the Java framework. This
     * version is for the first region of the BMT only. CRC and CRZ
     * characteristics localize strips in the cylindrical coordinate system. The
     * target center is at the origin. The Z-axis is along the beam axis. The
     * angles are defined with theZ-axis oriented from the accelerator to the
     * beam dump.
     */
    // THE GEOMETRY CONSTANTS
    public static final int NREGIONS = 3; // 3 regions of MM

    // Z detector characteristics
    public static double[] CRZRADIUS = new double[NREGIONS]; // the radius of
							     // the Z detector
							     // in mm
    public static int[] CRZNSTRIPS = new int[NREGIONS]; // the number of strips
    public static double[] CRZSPACING = new double[NREGIONS]; // the strip
							      // spacing in mm
    public static double[] CRZWIDTH = new double[NREGIONS]; // the strip width
							    // in mm
    public static double[] CRZLENGTH = new double[NREGIONS]; // the strip length
							     // in mm
    public static double[] CRZZMIN = new double[NREGIONS]; // PCB upstream
							   // extremity mm
    public static double[] CRZZMAX = new double[NREGIONS]; // PCB downstream
							   // extremity mm
    public static double[] CRZOFFSET = new double[NREGIONS]; // Beginning of
							     // strips in mm
    public static double[][] CRZEDGE1 = new double[NREGIONS][3]; // the angle of
								 // the first
								 // edge of each
								 // PCB detector
								 // A, B, C
    public static double[][] CRZEDGE2 = new double[NREGIONS][3]; // the angle of
								 // the second
								 // edge of each
								 // PCB detector
								 // A, B, C
    public static double[] CRZXPOS = new double[NREGIONS]; // Distance on the
							   // PCB between the
							   // PCB first edge and
							   // the edge of the
							   // first strip in mm

    // C detector characteristics
    public static double[] CRCRADIUS = new double[NREGIONS]; // the radius of
							     // the Z detector
							     // in mm
    public static int[] CRCNSTRIPS = new int[NREGIONS]; // the number of strips
    public static double[] CRCSPACING = new double[NREGIONS]; // the strip
							      // spacing in mm
    public static double[] CRCLENGTH = new double[NREGIONS]; // the strip length
							     // in mm
    public static double[] CRCZMIN = new double[NREGIONS]; // PCB upstream
							   // extremity mm
    public static double[] CRCZMAX = new double[NREGIONS]; // PCB downstream
							   // extremity mm
    public static double[] CRCOFFSET = new double[NREGIONS]; // Beginning of
							     // strips in mm
    public static int[][] CRCGROUP = new int[NREGIONS][]; // Number of strips
							  // with same width
    public static double[][] CRCWIDTH = new double[NREGIONS][]; // the width of
								// the
								// corresponding
								// group of
								// strips
    public static double[][] CRCEDGE1 = new double[NREGIONS][3]; // the angle of
								 // the first
								 // edge of each
								 // PCB detector
								 // A, B, C
    public static double[][] CRCEDGE2 = new double[NREGIONS][3]; // the angle of
								 // the second
								 // edge of each
								 // PCB detector
								 // A, B, C
    public static double[] CRCXPOS = new double[NREGIONS]; // Distance on the
							   // PCB between the
							   // PCB first edge and
							   // the edge of the
							   // first strip in mm

    // THE RECONSTRUCTION CONSTANTS
    public static double SigmaDrift = 0.4; // This is the value from GEMC
    public static double hDrift = 3.0; // ? the distance from the micromesh to
				       // the strips in mm - this is the value
				       // hardcoded in GEMC
    public static double hStrip2Det = hDrift / 2.;
    public static double ThetaL = Math.toRadians(20.); // the Lorentz angle

    public static boolean areConstantsLoaded = false;

    public static synchronized void Load() {
	if (areConstantsLoaded)
	    return;

	CRZRADIUS[2] = 205.8;
	CRZNSTRIPS[2] = 768;
	CRZSPACING[2] = 0.2;
	CRZWIDTH[2] = 0.328;
	CRZLENGTH[2] = 444.88;
	CRZZMIN[2] = -421.75;
	CRZZMAX[2] = 290.25;
	CRZOFFSET[2] = 252.1;
	CRZEDGE1[2] = new double[] { Math.toRadians(30.56),
		Math.toRadians(270.56), Math.toRadians(150.56) };
	CRZEDGE2[2] = new double[] { Math.toRadians(149.44),
		Math.toRadians(29.44), Math.toRadians(269.44) };
	CRZXPOS[2] = 10.547;

	CRCRADIUS[2] = 220.8;
	CRCNSTRIPS[2] = 1152;
	CRCLENGTH[2] = 438.6;
	CRCSPACING[2] = 0.16;
	CRCZMIN[2] = -421.75;
	CRCZMAX[2] = 290.25;
	CRCOFFSET[2] = 252.18;
	CRCGROUP[2] = new int[] { 32, 32, 32, 32, 704, 64, 32, 32, 32, 32, 32,
		32, 32, 32 };
	CRCWIDTH[2] = new double[] { 0.38, 0.32, 0.27, 0.23, 0.17, 0.18, 0.22,
		0.25, 0.29, 0.33, 0.37, 0.41, 0.46, 0.51 };
	CRCEDGE1[2] = new double[] { Math.toRadians(30.52),
		Math.toRadians(270.52), Math.toRadians(150.52) };
	CRCEDGE2[2] = new double[] { Math.toRadians(149.48),
		Math.toRadians(29.48), Math.toRadians(269.48) };
	CRCXPOS[2] = 11.999;

	areConstantsLoaded = true;

	System.out.println(" Barrel Micromegas Geometry constants loaded !!! ");
	if (CRZRADIUS[0] == 0)
	    System.out.println(
		    " CAUTION... Barrel Micromegas Geometry with outermost region only! Ensure you are running on data corresponding to this geometry configuration ...");
    }

}
