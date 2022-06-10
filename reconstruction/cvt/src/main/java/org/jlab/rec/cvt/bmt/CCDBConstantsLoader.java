package org.jlab.rec.cvt.bmt;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ziegler
 *
 */
public class CCDBConstantsLoader {

    public static Logger LOGGER = Logger.getLogger(CCDBConstantsLoader.class.getName());

    public CCDBConstantsLoader() {
        // TODO Auto-generated constructor stub
    }

    static boolean CSTLOADED = false;

   
    //private static DatabaseConstantProvider DB;
    

    public static final synchronized void Load(DatabaseConstantProvider dbprovider) {
        // initialize the constants
        //Z detector characteristics
        int NREGIONS = BMTConstants.NREGIONS;
        int NSECTORS = BMTConstants.NSECTORS;
        int NLAYERS  = BMTConstants.NLAYERS;
        double[] CRZRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
        int[] CRZNSTRIPS = new int[NREGIONS]; 			// the number of strips
        double[] CRZSPACING = new double[NREGIONS]; 	        // the strip spacing in mm
        double[] CRZWIDTH = new double[NREGIONS]; 		// the strip width in mm
        double[] CRZLENGTH = new double[NREGIONS]; 		// the strip length in mm
        double[] CRZZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
        double[] CRZZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
        double[] CRZOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
        double[][] CRZPHI = new double[NREGIONS][NSECTORS]; 	// the the central phi of the PCB
        double[][] CRZDPHI = new double[NREGIONS][NSECTORS]; 	// the the half phi width of the PCB
        double[][] CRZEDGE1 = new double[NREGIONS][NSECTORS]; 	// the angle of the first edge of each PCB detector A, B, C
        double[][] CRZEDGE2 = new double[NREGIONS][NSECTORS]; 	// the angle of the second edge of each PCB detector A, B, C
        double[] CRZXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

        //C detector characteristics
        double[] CRCRADIUS = new double[NREGIONS]; 		// the radius of the Z detector in mm
        int[] CRCNSTRIPS = new int[NREGIONS]; 			// the number of strips
        double[] CRCSPACING = new double[NREGIONS]; 	// the strip spacing in mm
        double[] CRCLENGTH = new double[NREGIONS]; 		// the strip length in mm
        double[] CRCZMIN = new double[NREGIONS]; 		// PCB upstream extremity mm
        double[] CRCZMAX = new double[NREGIONS]; 		// PCB downstream extremity mm
        double[] CRCOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
        int[][] CRCGROUP = new int[NREGIONS][100]; 		// Number of strips with same width
        double[][] CRCWIDTH = new double[NREGIONS][100];	// the width of the corresponding group of strips 
        int[][] CRCGRPNMIN     = new int[NREGIONS][100];        // the group min strip number
        int[][] CRCGRPNMAX     = new int[NREGIONS][100];        // the group max strip number
        double[][] CRCGRPZMIN  = new double[NREGIONS][100];	// the group minimum z
        double[][] CRCGRPZMAX  = new double[NREGIONS][100];	// the group maximum z
        double[][] CRCPHI = new double[NREGIONS][NSECTORS]; 	// the the central phi of the PCB
        double[][] CRCDPHI = new double[NREGIONS][NSECTORS]; 	// the the half phi width of the PCB
        double[][] CRCEDGE1 = new double[NREGIONS][NSECTORS];   // the angle of the first edge of each PCB detector A, B, C
        double[][] CRCEDGE2 = new double[NREGIONS][NSECTORS];   // the angle of the second edge of each PCB detector A, B, C
        double[] CRCXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm
        
         int GRID_SIZE=405;
         double[] THETA_L_grid = new double [GRID_SIZE];
         double[] ELEC_grid = new double [GRID_SIZE];
         double[] MAG_grid = new double [GRID_SIZE];
         
        // Load the tables
        
        // using
        // the
        // new
        // run
        // load the geometry tables
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_layer_noshim");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L1");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L2");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L3");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L4");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L5");
        dbprovider.loadTable("/geometry/cvt/mvt/bmt_strip_L6");
        
        //load material budget:
        dbprovider.loadTable("/geometry/cvt/mvt/material");
        
         //load Lorentz angle table
        dbprovider.loadTable("/calibration/mvt/lorentz");
        
        //load alignment parameters
        dbprovider.loadTable("/geometry/cvt/mvt/alignment");
        dbprovider.loadTable("/geometry/cvt/mvt/position");
        
        dbprovider.disconnect();
        
        // Getting the BMTConstants
        // 1) pitch info 
        int[] C_Layers = {1, 4, 6};
        for (int j = 0; j < NREGIONS; j++) {
            for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_strip_L" + C_Layers[j] + "/Group_size"); i++) {
                CRCGROUP[j][i] = dbprovider.getInteger("/geometry/cvt/mvt/bmt_strip_L" + C_Layers[j] + "/Group_size", i);
                CRCWIDTH[j][i] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L" + C_Layers[j] + "/Pitch", i);
                if(i==0) {
                    CRCGRPNMIN[j][i] = 1;
                    CRCGRPNMAX[j][i] = CRCGROUP[j][i];
                    CRCGRPZMIN[j][i] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Zmin", C_Layers[j]-1);
                    CRCGRPZMAX[j][i] = CRCGRPZMIN[j][i] + CRCGROUP[j][i]*CRCWIDTH[j][i]; 
                }
                else     {
                    CRCGRPNMIN[j][i] = CRCGRPNMAX[j][i-1] + 1;
                    CRCGRPNMAX[j][i] = CRCGRPNMAX[j][i-1] + CRCGROUP[j][i];
                    CRCGRPZMIN[j][i] = CRCGRPZMAX[j][i-1];
                    CRCGRPZMAX[j][i] = CRCGRPZMIN[j][i]   + CRCGROUP[j][i]*CRCWIDTH[j][i]; 
                }
            }
        }

        CRZWIDTH[0] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L2/Pitch", 0);
        CRZWIDTH[1] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L3/Pitch", 0);
        CRZWIDTH[2] = dbprovider.getDouble("/geometry/cvt/mvt/bmt_strip_L5/Pitch", 0);

        // Getting the BMTConstants
        // 2) Layer info 
        for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/bmt_layer_noshim/Layer"); i++) {

            int layer = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer_noshim/Layer", i);
            int region = (int) ((layer + 1) / 2);

            double radius = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Radius", i);
            double Zmin = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Zmin", i);
            double Zmax = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Zmax", i);
            double spacing = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Interstrip", i);
            int axis = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer_noshim/Axis", i);
            int Nstrips = dbprovider.getInteger("/geometry/cvt/mvt/bmt_layer_noshim/Nstrip", i);

            double Phi_min = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Phi_min", i);
            double Phi_max = dbprovider.getDouble("/geometry/cvt/mvt/bmt_layer_noshim/Phi_max", i);

            //sector clocking fix data
            double[] EDGE1 = new double[]{Math.toRadians(Phi_min + 120), Math.toRadians(Phi_min), Math.toRadians(Phi_min + 240)};
            double[] EDGE2 = new double[]{Math.toRadians(Phi_max + 120), Math.toRadians(Phi_max), Math.toRadians(Phi_max - 120)};
            spacing = 0;
            if (axis == 1) { //Z-detector
                CRZRADIUS[region - 1] = radius;
                CRZNSTRIPS[region - 1] = Nstrips;
                CRZZMIN[region - 1] = Zmin;
                CRZZMAX[region - 1] = Zmax;
                CRZLENGTH[region - 1] = Zmax - Zmin;
                CRZSPACING[region - 1] = spacing;
                

                for (int j = 0; j < 3; j++) {
                    if (EDGE2[j] > EDGE1[j]) {
                        double middle = (EDGE1[j] + EDGE2[j]) / 2.;
                        EDGE1[j] = middle - ((double) Nstrips) * (CRZWIDTH[region - 1] / radius) / 2. - 0 * CRZWIDTH[region - 1] / radius; // clock by one strip = -CRZWIDTH[region-1]/radius ?
                        EDGE2[j] = middle + ((double) Nstrips) * (CRZWIDTH[region - 1] / radius) / 2. - 0 * CRZWIDTH[region - 1] / radius;
                    }
                    if (EDGE2[j] < EDGE1[j]) {
                        double middle = (EDGE1[j] + EDGE2[j] + 2 * Math.PI) / 2.;
                        EDGE1[j] = middle - ((double) Nstrips) * (CRZWIDTH[region - 1] / radius) / 2. - 0 * CRZWIDTH[region - 1] / radius;
                        EDGE2[j] = middle + ((double) Nstrips) * (CRZWIDTH[region - 1] / radius) / 2. - 0 * CRZWIDTH[region - 1] / radius;
                        EDGE2[j] -= 2 * Math.PI;
                    }
                    if     (j==0) CRZPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2 + 120);
                    else if(j==1) CRZPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2);
                    else if(j==2) CRZPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2 + 240);
                    CRZDPHI[region-1][j] = Nstrips * CRZWIDTH[region - 1]/radius/2;
                }
                CRZEDGE1[region - 1] = EDGE1;
                CRZEDGE2[region - 1] = EDGE2;
            }
            if (axis == 0) { //C-detector
                CRCRADIUS[region - 1] = radius;
                CRCNSTRIPS[region - 1] = Nstrips;
                CRCZMIN[region - 1] = Zmin;
                CRCZMAX[region - 1] = Zmax;
                CRCLENGTH[region - 1] = Zmax - Zmin;
                CRCSPACING[region - 1] = spacing;
                CRCEDGE1[region - 1] = EDGE1;
                CRCEDGE2[region - 1] = EDGE2;
                for (int j = 0; j < 3; j++) {
                    if     (j==0) CRCPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2 + 120);
                    else if(j==1) CRCPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2);
                    else if(j==2) CRCPHI[region-1][j] = Math.toRadians((Phi_min+Phi_max)/2 + 240);
                    CRCDPHI[region-1][j] = Math.toRadians((Phi_max-Phi_min)/2 );
                }
            }

        }
        
        //material budget
        //===============
        for (int i = 0; i < dbprovider.length("/geometry/cvt/mvt/material" + "/sector"); i++) {
            int layer    = dbprovider.getInteger("/geometry/cvt/mvt/material" + "/layer", i);
            int comp     = dbprovider.getInteger("/geometry/cvt/mvt/material" + "/component", i);
            double[] properties = new double[5];
            String name   = dbprovider.getString("/geometry/cvt/mvt/material" + "/name", i);
            properties[0] = dbprovider.getDouble("/geometry/cvt/mvt/material" + "/thickness", i)/1000.;  // mm
            properties[1] = dbprovider.getDouble("/geometry/cvt/mvt/material" + "/density", i)*1E-3;     // g/mm3
            properties[2] = dbprovider.getDouble("/geometry/cvt/mvt/material" + "/average_Z", i)/
                            dbprovider.getDouble("/geometry/cvt/mvt/material" + "/average_A", i);
            properties[3] =  dbprovider.getDouble("/geometry/cvt/mvt/material" + "/X0", i)*10;           // mm
            properties[4] =  dbprovider.getDouble("/geometry/cvt/mvt/material" + "/I", i);               // eV
            BMTConstants.addMaterial(name, properties);
        }
        
        
        if (GRID_SIZE!=dbprovider.length("/calibration/mvt/lorentz/angle")) {
         LOGGER.log(Level.WARNING,"WARNING... Lorentz angle grid is not the same size as the table in CCDBConstant");}
         for (int i = 0; i < dbprovider.length("/calibration/mvt/lorentz/angle"); i++) {
         	THETA_L_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/angle",i);
         	ELEC_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/Edrift",i);
         	MAG_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/Bfield",i);
        }
        
        // alignment and offsets
        double xpos = dbprovider.getDouble("/geometry/cvt/mvt/position/x", 0 );
        double ypos = dbprovider.getDouble("/geometry/cvt/mvt/position/y", 0 );
        double zpos = dbprovider.getDouble("/geometry/cvt/mvt/position/z", 0 );
        // hardcode gemc rotation: set angles to 0 to null it
        Vector3D  gemcRot = new Vector3D(0,0,0);
        Point3D gemcCenter = new Point3D(0,0,-94.7); // the original BMT Center
        Point3D gemcShift  = new Point3D(gemcCenter);
        gemcCenter.rotateZ(Math.toRadians(gemcRot.z()));
        gemcCenter.rotateY(Math.toRadians(gemcRot.y()));
        gemcCenter.rotateX(Math.toRadians(gemcRot.x()));
        gemcShift.translateXYZ(-gemcCenter.x(), -gemcCenter.y(), -gemcCenter.z());
        for (int row = 0; row<NLAYERS*NSECTORS; row++) {
            int sector = dbprovider.getInteger("/geometry/cvt/mvt/alignment/sector", row);
            int layer  = dbprovider.getInteger("/geometry/cvt/mvt/alignment/layer", row);
            Point3D shift = new Point3D(dbprovider.getDouble("/geometry/cvt/mvt/alignment/deltaX", row),
                                        dbprovider.getDouble("/geometry/cvt/mvt/alignment/deltaY", row),
                                        dbprovider.getDouble("/geometry/cvt/mvt/alignment/deltaZ", row)); 
            shift.translateXYZ(xpos+gemcShift.x(), ypos+gemcShift.y(), zpos+gemcShift.z());
            Vector3D rot = new Vector3D(dbprovider.getDouble("/geometry/cvt/mvt/alignment/rotX", row)+Math.toRadians(gemcRot.x()),
                                        dbprovider.getDouble("/geometry/cvt/mvt/alignment/rotY", row)+Math.toRadians(gemcRot.y()),
                                        dbprovider.getDouble("/geometry/cvt/mvt/alignment/rotZ", row)+Math.toRadians(gemcRot.z())); 

            int region = (int) Math.floor((layer+1)/2);
            double Zmin = CRZZMIN[region - 1];
            double Zmax = CRZZMAX[region - 1];
            Transformation3D transform = new Transformation3D();
            transform.rotateZ(rot.z());
            transform.rotateY(rot.y());
            transform.rotateX(rot.x());
            transform.translateXYZ(shift.x(), shift.y(), shift.z());
            Line3D axis = new Line3D(new Point3D(0,0,Zmin), new Vector3D(0,0,Zmax));
            transform.apply(axis);
            BMTConstants.SHIFTS[layer-1][sector-1]    = shift;
            BMTConstants.ROTATIONS[layer-1][sector-1] = rot;
            BMTConstants.AXES[layer-1][sector-1]      = axis;
            BMTConstants.TOGLOBAL[layer-1][sector-1]  = transform;
            BMTConstants.TOLOCAL[layer-1][sector-1]   = transform.inverse();
        }
        
       
        BMTConstants.setCRCRADIUS(CRCRADIUS);
        BMTConstants.setCRZRADIUS(CRZRADIUS);
        BMTConstants.setCRZNSTRIPS(CRZNSTRIPS);
        BMTConstants.setCRZZMIN(CRZZMIN);
        BMTConstants.setCRZZMAX(CRZZMAX);
        BMTConstants.setCRZLENGTH(CRZLENGTH);
        BMTConstants.setCRZSPACING(CRZSPACING);
        BMTConstants.setCRZPHI(CRZPHI);
        BMTConstants.setCRZDPHI(CRZDPHI);
        BMTConstants.setCRZEDGE1(CRZEDGE1);
        BMTConstants.setCRZEDGE2(CRZEDGE2);
        BMTConstants.setCRCRADIUS(CRCRADIUS);
        BMTConstants.setCRCNSTRIPS(CRCNSTRIPS);
        BMTConstants.setCRCZMIN(CRCZMIN);
        BMTConstants.setCRCZMAX(CRCZMAX);
        BMTConstants.setCRCLENGTH(CRCLENGTH);
        BMTConstants.setCRCSPACING(CRCSPACING);
        BMTConstants.setCRCPHI(CRCPHI);
        BMTConstants.setCRCDPHI(CRCDPHI);
        BMTConstants.setCRCEDGE1(CRCEDGE1);
        BMTConstants.setCRCEDGE2(CRCEDGE2);
        BMTConstants.setCRCGROUP(CRCGROUP);
        BMTConstants.setCRCWIDTH(CRCWIDTH);
        BMTConstants.setCRCGRPZMIN(CRCGRPZMIN);
        BMTConstants.setCRCGRPZMAX(CRCGRPZMAX);
        BMTConstants.setCRCGRPNMIN(CRCGRPNMIN);
        BMTConstants.setCRCGRPNMAX(CRCGRPNMAX);
        BMTConstants.setCRZWIDTH(CRZWIDTH);
        BMTConstants.setTHETAL_grid(THETA_L_grid);
        BMTConstants.setE_grid(ELEC_grid);
        BMTConstants.setB_grid(MAG_grid);
        BMTConstants.setPar_grid();
        dbprovider.disconnect();
        CSTLOADED = true;
    }

}
