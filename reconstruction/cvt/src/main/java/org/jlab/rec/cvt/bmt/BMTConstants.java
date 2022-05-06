package org.jlab.rec.cvt.bmt;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;

public class BMTConstants {

    private BMTConstants() {

    }
    
    /*
     * The algorithm to describe the geometry of the Barrel Micromegas is provided by Franck Sabatie and implemented into the Java framework.
     * This version is for the last region of the BMT only.
       CRC and CRZ characteristics localize strips in the cylindrical coordinate system. The target center is at the origin. The Z-axis is along the beam axis. 
       The angles are defined with theZ-axis oriented from the accelerator to the beam dump.
     */
    //CUTS
    public static double MAXCLUSSIZE = 5;
    public static int MAXBMTHITS = 700;
    //public static final int STARTINGLAYR = 5;						// current configuration is 3 SVT + 3BMT (outermost BST ring)
    public static double ETOTCUT = 0.0;
    // THE GEOMETRY CONSTANTS
    public static final int NREGIONS = 3;						// 3 regions of MM 
    public static final int NLAYERS   = NREGIONS*2;					// 6 layer
    public static final int NSECTORS  = 3;						// 3 sectors or tiles per layer
    //Z detector characteristics
    private static double[] CRZRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
    private static int[] CRZNSTRIPS = new int[NREGIONS]; 			// the number of strips
    private static double[] CRZSPACING = new double[NREGIONS]; 		// the strip spacing in mm
    private static double[] CRZWIDTH = new double[NREGIONS]; 		// the strip width in mm
    private static double[] CRZLENGTH = new double[NREGIONS]; 		// the strip length in mm
    private static double[] CRZZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
    private static double[] CRZZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
    private static double[] CRZOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
    private static double[][] CRZPHI = new double[NREGIONS][3]; 	// the central phi of each PCB detector A, B, C
    private static double[][] CRZDPHI = new double[NREGIONS][3]; 	// the half phi width of each PCB detector A, B, C
    private static double[][] CRZEDGE1 = new double[NREGIONS][3]; 	// the angle of the first edge of each PCB detector A, B, C
    private static double[][] CRZEDGE2 = new double[NREGIONS][3]; 	// the angle of the second edge of each PCB detector A, B, C
    private static double[] CRZXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

    //C detector characteristics
    private static double[] CRCRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
    private static int[] CRCNSTRIPS = new int[NREGIONS]; 			// the number of strips
    private static double[] CRCSPACING = new double[NREGIONS]; 		// the strip spacing in mm
    private static double[] CRCLENGTH = new double[NREGIONS]; 		// the strip length in mm
    private static double[] CRCZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
    private static double[] CRCZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
    private static double[] CRCOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
    private static int[][] CRCGROUP = new int[NREGIONS][];		// Number of strips with same width
    private static double[][] CRCWIDTH = new double[NREGIONS][];	// the width of the corresponding group of strips 
    private static int[][] CRCGRPNMIN = new int[NREGIONS][];		// the group min strip number
    private static int[][] CRCGRPNMAX = new int[NREGIONS][];		// the group max strip number
    private static double[][] CRCGRPZMIN = new double[NREGIONS][];      // the group minimum z
    private static double[][] CRCGRPZMAX = new double[NREGIONS][];      // the group maximum z
    private static double[][] CRCPHI = new double[NREGIONS][3]; 	// the central phi of each PCB detector A, B, C
    private static double[][] CRCDPHI = new double[NREGIONS][3]; 	// the half phi width of each PCB detector A, B, C
    private static double[][] CRCEDGE1 = new double[NREGIONS][3]; 	// the angle of the first edge of each PCB detector A, B, C
    private static double[][] CRCEDGE2 = new double[NREGIONS][3]; 	// the angle of the second edge of each PCB detector A, B, C
    private static double[] CRCXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

    // map of material properties for dE/dx and mult. scat
    // the array contains thickness density in g/mm3, Z/A, X0, I (eV)
    private static Map<String, double[]> MATERIALPROPERTIES = new LinkedHashMap<>();

  
    // THE RECONSTRUCTION CONSTANTS
    public static final double SIGMADRIFT = 0.036; 				// Max transverse diffusion value (GEMC value)
    
    public static final double HDRIFT = 3.0; 					// Size of the drift gap
    
    public static Point3D[][]  SHIFTS    = new Point3D[NLAYERS][NSECTORS];  // detector alignment SHIFTS
    public static Vector3D[][] ROTATIONS = new Vector3D[NLAYERS][NSECTORS]; // detector alignment ROTATIONS
    public static Line3D[][]   AXES = new Line3D[NLAYERS][NSECTORS];        // detector AXES
    public static Transformation3D[][] TOLOCAL  = new Transformation3D[NLAYERS][NSECTORS];
    public static Transformation3D[][] TOGLOBAL = new Transformation3D[NLAYERS][NSECTORS];
    public static double[] ThetaL_grid = new double[405];    //Lorentz angle grid
    public static double[] E_grid = new double[405];         //Electric field value of the grid
    public static double[] B_grid = new double[405];        //Magnetic field value of the grid
    public static double ThetaL = 0; 						// the Lorentz angle for 5-T B-field
    public static double emin=Double.MAX_VALUE;           //Emin of the grid
    public static double emax=0;                          //Emax of the grid
    public static double bmax=0;                          //Bmax of the grid
    public static double bmin=Double.MAX_VALUE;           //Bmin of the grid
    public static int Ne=0;                               //Number of step for the electric field
    public static int Nb=0;                               //Number of step for the magnetic field
  
    
// THE HV CONSTANT
    public static double[][] E_DRIFT_FF = new double[2*NREGIONS][3]; 
    public static double[][] E_DRIFT_MF = new double[2*NREGIONS][3]; 

    public static final int STARTINGLAYR = 1;

    public static double getThetaL() {
        return ThetaL;
    }

    public static double[] getCRZRADIUS() {
        return CRZRADIUS;
    }

    public static synchronized void setCRZRADIUS(double[] cRZRADIUS) {
        CRZRADIUS = cRZRADIUS;
    }

    public static int[] getCRZNSTRIPS() {
        return CRZNSTRIPS;
    }

    public static synchronized void setCRZNSTRIPS(int[] cRZNSTRIPS) {
        CRZNSTRIPS = cRZNSTRIPS;
    }

    public static double[] getCRZSPACING() {
        return CRZSPACING;
    }

    public static synchronized void setCRZSPACING(double[] cRZSPACING) {
        CRZSPACING = cRZSPACING;
    }

    public static double[] getCRZWIDTH() {
        return CRZWIDTH;
    }

    public static synchronized void setCRZWIDTH(double[] cRZWIDTH) {
        CRZWIDTH = cRZWIDTH;
    }

    public static double[] getCRZLENGTH() {
        return CRZLENGTH;
    }

    public static synchronized void setCRZLENGTH(double[] cRZLENGTH) {
        CRZLENGTH = cRZLENGTH;
    }

    public static double[] getCRZZMIN() {
        return CRZZMIN;
    }

    public static synchronized void setCRZZMIN(double[] cRZZMIN) {
        CRZZMIN = cRZZMIN;
    }

    public static double[] getCRZZMAX() {
        return CRZZMAX;
    }

    public static synchronized void setCRZZMAX(double[] cRZZMAX) {
        CRZZMAX = cRZZMAX;
    }

    public static double[] getCRZOFFSET() {
        return CRZOFFSET;
    }

    public static synchronized void setCRZOFFSET(double[] cRZOFFSET) {
        CRZOFFSET = cRZOFFSET;
    }

    public static double[][] getCRZPHI() {
        return CRZPHI;
    }

    public static void setCRZPHI(double[][] cRZPHI) {
        BMTConstants.CRZPHI = cRZPHI;
    }

    public static double[][] getCRZDPHI() {
        return CRZDPHI;
    }

    public static void setCRZDPHI(double[][] cRZDPHI) {
        BMTConstants.CRZDPHI = cRZDPHI;
    }

    public static double[][] getCRZEDGE1() {
        return CRZEDGE1;
    }

    public static synchronized void setCRZEDGE1(double[][] cRZEDGE1) {
        CRZEDGE1 = cRZEDGE1;
    }

    public static double[][] getCRZEDGE2() {
        return CRZEDGE2;
    }

    public static synchronized void setCRZEDGE2(double[][] cRZEDGE2) {
        CRZEDGE2 = cRZEDGE2;
    }

    public static double[] getCRZXPOS() {
        return CRZXPOS;
    }

    public static synchronized void setCRZXPOS(double[] cRZXPOS) {
        CRZXPOS = cRZXPOS;
    }

    public static double[] getCRCRADIUS() {
        return CRCRADIUS;
    }

    public static synchronized void setCRCRADIUS(double[] cRCRADIUS) {
        CRCRADIUS = cRCRADIUS;
    }

    public static int[] getCRCNSTRIPS() {
        return CRCNSTRIPS;
    }

    public static synchronized void setCRCNSTRIPS(int[] cRCNSTRIPS) {
        CRCNSTRIPS = cRCNSTRIPS;
    }

    public static double[] getCRCSPACING() {
        return CRCSPACING;
    }

    public static synchronized void setCRCSPACING(double[] cRCSPACING) {
        CRCSPACING = cRCSPACING;
    }

    public static double[] getCRCLENGTH() {
        return CRCLENGTH;
    }

    public static synchronized void setCRCLENGTH(double[] cRCLENGTH) {
        CRCLENGTH = cRCLENGTH;
    }

    public static double[] getCRCZMIN() {
        return CRCZMIN;
    }

    public static synchronized void setCRCZMIN(double[] cRCZMIN) {
        CRCZMIN = cRCZMIN;
    }

    public static double[] getCRCZMAX() {
        return CRCZMAX;
    }

    public static synchronized void setCRCZMAX(double[] cRCZMAX) {
        CRCZMAX = cRCZMAX;
    }

    public static double[] getCRCOFFSET() {
        return CRCOFFSET;
    }

    public static synchronized void setCRCOFFSET(double[] cRCOFFSET) {
        CRCOFFSET = cRCOFFSET;
    }

    public static int[][] getCRCGROUP() {
        return CRCGROUP;
    }

    public static synchronized void setCRCGROUP(int[][] cRCGROUP) {
        CRCGROUP = cRCGROUP;
    }

    public static double[][] getCRCWIDTH() {
        return CRCWIDTH;
    }

    public static void setCRCGRPZMIN(double[][] cRCGRPZ) {
        CRCGRPZMIN = cRCGRPZ;
    }

    public static double[][] getCRCGRPZMIN() {
        return CRCGRPZMIN;
    }

    public static void setCRCGRPZMAX(double[][] cRCGRPZ) {
        CRCGRPZMAX = cRCGRPZ;
    }

    public static double[][] getCRCGRPZMAX() {
        return CRCGRPZMAX;
    }

    public static void setCRCGRPNMAX(int[][] cRCGRPN) {
        CRCGRPNMAX = cRCGRPN;
    }

    public static int[][] getCRCGRPNMAX() {
        return CRCGRPNMAX;
    }

    public static void setCRCGRPNMIN(int[][] cRCGRPN) {
        CRCGRPNMIN = cRCGRPN;
    }

    public static int[][] getCRCGRPNMIN() {
        return CRCGRPNMIN;
    }

    public static synchronized void setCRCWIDTH(double[][] cRCWIDTH) {
        CRCWIDTH = cRCWIDTH;
    }

    public static double[][] getCRCPHI() {
        return CRCPHI;
    }

    public static void setCRCPHI(double[][] cRCPHI) {
        BMTConstants.CRCPHI = cRCPHI;
    }

    public static double[][] getCRCDPHI() {
        return CRCDPHI;
    }

    public static void setCRCDPHI(double[][] cRCDPHI) {
        BMTConstants.CRCDPHI = cRCDPHI;
    }

    public static double[][] getCRCEDGE1() {
        return CRCEDGE1;
    }

    public static synchronized void setCRCEDGE1(double[][] cRCEDGE1) {
        CRCEDGE1 = cRCEDGE1;
    }

    public static double[][] getCRCEDGE2() {
        return CRCEDGE2;
    }

    public static synchronized void setCRCEDGE2(double[][] cRCEDGE2) {
        CRCEDGE2 = cRCEDGE2;
    }

    public static double[] getCRCXPOS() {
        return CRCXPOS;
    }

    public static synchronized void setCRCXPOS(double[] cRCXPOS) {
        CRCXPOS = cRCXPOS;
    }

    public static Map<String, double[]> getMaterials() {
        return MATERIALPROPERTIES;
    }

    public static synchronized void setMaterials(Map<String, double[]> properties) {
        BMTConstants.MATERIALPROPERTIES = properties;
    }
    
    public static synchronized void addMaterial(String name, double[] properties) {
        BMTConstants.MATERIALPROPERTIES.put(name, properties);
    }
    
    public static synchronized void setTHETAL_grid(double[] cThetaL_grid) {
  	ThetaL_grid  = cThetaL_grid;
   }
   public static synchronized void setE_grid(double[] cE_grid) {
   	E_grid  = cE_grid;
   }
   public static synchronized void setB_grid(double[] cB_grid) {
   	B_grid  = cB_grid;
   }
   public static synchronized void setPar_grid() {
    double pe=0;
    double pb=0;
    Ne=15;
    Nb=27;
    for (int j=0;j<405;j++) {	
            if(Ne==0 && Nb==0 ){
                    emin = E_grid[j];
                    emax = E_grid[j];
                    bmin = B_grid[j];
                    bmax = B_grid[j];
                   
                    pe = E_grid[j];
                    pb = B_grid[j];
                    continue;
            }	
             // check max and minima
           if ( E_grid[j] < emin ) emin = E_grid[j];
           if ( E_grid[j] > emax ) emax = E_grid[j];
           if ( B_grid[j] < bmin ) bmin = B_grid[j];
           if ( B_grid[j] > bmax ) bmax = B_grid[j];

           // count E and B
           //if( Math.abs( pe - E_grid[j] ) > 0.0001 ) Ne++;
           //if ( Ne==1) { if( Math.abs( pb - B_grid[j] ) > 0.0001 ) Nb++; } // only for the first Nb value


           pe = E_grid[j] ;
           pb = B_grid[j] ;
        }
   }
   public static synchronized void setE_drift_FF(double[][] cHV_drift) {
   	for (int i=0; i<2*NREGIONS;i++) {
            for (int j=0; j<3;j++) {	
                    E_DRIFT_FF[i][j] = 10*cHV_drift[i][j]/HDRIFT;
            }	
   	}
  }
   public static synchronized void setE_drift_MF(double[][] cHV_drift) {
   	for (int i=0; i<2*NREGIONS;i++) {
            for (int j=0; j<3;j++) {	
                    E_DRIFT_MF[i][j]  = 10*cHV_drift[i][j]/HDRIFT;
            }	
   	}
  }
   
}
