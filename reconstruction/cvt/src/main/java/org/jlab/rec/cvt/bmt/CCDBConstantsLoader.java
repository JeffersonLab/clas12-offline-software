package org.jlab.rec.cvt.bmt;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author ziegler
 *
 */
public class CCDBConstantsLoader {

    public CCDBConstantsLoader() {
        // TODO Auto-generated constructor stub
    }

    static boolean CSTLOADED = false;

   
    //private static DatabaseConstantProvider DB;
    

    public static final synchronized void Load(DatabaseConstantProvider dbprovider) {
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
        double[] CRZOFFSET = new double[NREGIONS]; 		// Beginning of strips in mm
        double[][] CRZEDGE1 = new double[NREGIONS][3]; 	// the angle of the first edge of each PCB detector A, B, C
        double[][] CRZEDGE2 = new double[NREGIONS][3]; 	// the angle of the second edge of each PCB detector A, B, C
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
        double[][] CRCEDGE1 = new double[NREGIONS][3];          // the angle of the first edge of each PCB detector A, B, C
        double[][] CRCEDGE2 = new double[NREGIONS][3];          // the angle of the second edge of each PCB detector A, B, C
        double[] CRCXPOS = new double[NREGIONS]; 		// Distance on the PCB between the PCB first edge and the edge of the first strip in mm

        double[] EFF_Z_OVER_A = new double[NREGIONS*2];
        double[] T_OVER_X0    = new double[NREGIONS*2];
        
         int GRID_SIZE=405;
         double[] THETA_L_grid = new double [GRID_SIZE];
         double[] ELEC_grid = new double [GRID_SIZE];
         double[] MAG_grid = new double [GRID_SIZE];
         
         // HV settings for Lorentz Angle
         double [][] HV_DRIFT_FF= new double [NREGIONS*2][3];
         double [][] HV_DRIFT_MF= new double [NREGIONS*2][3];
         
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
        dbprovider.loadTable("/test/mvt/bmt_mat_l1");
        dbprovider.loadTable("/test/mvt/bmt_mat_l2");
        dbprovider.loadTable("/test/mvt/bmt_mat_l3");
        dbprovider.loadTable("/test/mvt/bmt_mat_l4");
        dbprovider.loadTable("/test/mvt/bmt_mat_l5");
        dbprovider.loadTable("/test/mvt/bmt_mat_l6");
        
         //load Lorentz angle table
        dbprovider.loadTable("/calibration/mvt/lorentz");
        dbprovider.loadTable("/calibration/mvt/bmt_hv/drift_fullfield");
        dbprovider.loadTable("/calibration/mvt/bmt_hv/drift_midfield");

        //beam offset table
        dbprovider.loadTable("/geometry/beam/position");
        
        //target position table
        dbprovider.loadTable("/geometry/target");
        
        dbprovider.disconnect();
        
        // target position
        double ztarget = dbprovider.getDouble("/geometry/target/position", 0);
        
        System.out.println(" ................READ TARGET SHIFT "+ztarget+" cm......."); 
      //  dbprovider.show();
        // Getting the Constants
        // 1) pitch info 
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

        // Getting the Constants
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
            }

        }
        
        //material budget
        //===============
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l1/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l1/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l1/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l1/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l1/x0", i);
            EFF_Z_OVER_A[0] += thickness*Zeff/Aeff;      
            T_OVER_X0[0]+=thickness/X0;
        }
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l2/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l2/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l2/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l2/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l2/x0", i);
            EFF_Z_OVER_A[1] += thickness*Zeff/Aeff;      
            T_OVER_X0[1]+=thickness/X0;     
        }  
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l3/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l3/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l3/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l3/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l3/x0", i);
            EFF_Z_OVER_A[2] += thickness*Zeff/Aeff;      
            T_OVER_X0[2]+=thickness/X0;      
        }
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l4/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l4/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l4/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l4/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l4/x0", i);
            EFF_Z_OVER_A[3] += thickness*Zeff/Aeff;      
            T_OVER_X0[3]+=thickness/X0;   
        }
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l5/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l5/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l5/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l5/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l5/x0", i);
            EFF_Z_OVER_A[4] += thickness*Zeff/Aeff;      
            T_OVER_X0[4]+=thickness/X0;  
        }
        for (int i = 0; i < dbprovider.length("/test/mvt/bmt_mat_l6/thickness"); i++) {
            double thickness = dbprovider.getDouble("/test/mvt/bmt_mat_l6/thickness", i)/10000.;
            double Zeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l6/average_z", i);
            double Aeff =  dbprovider.getDouble("/test/mvt/bmt_mat_l6/average_a", i);
            double X0 =  dbprovider.getDouble("/test/mvt/bmt_mat_l6/x0", i);
            EFF_Z_OVER_A[5] += thickness*Zeff/Aeff;      
            T_OVER_X0[5]+=thickness/X0;      
        }
        
        if (GRID_SIZE!=dbprovider.length("/calibration/mvt/lorentz/angle")) {
         System.out.println("WARNING... Lorentz angle grid is not the same size as the table in CCDBConstant");}
         for (int i = 0; i < dbprovider.length("/calibration/mvt/lorentz/angle"); i++) {
         	THETA_L_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/angle",i);
         	ELEC_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/Edrift",i);
         	MAG_grid[i]=dbprovider.getDouble("/calibration/mvt/lorentz/Bfield",i);
        }
         
         for (int i = 0; i<2*NREGIONS; i++) {
        	HV_DRIFT_FF[i][0]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_fullfield/Sector_1", i);
    		HV_DRIFT_FF[i][1]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_fullfield/Sector_2", i);
    		HV_DRIFT_FF[i][2]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_fullfield/Sector_3", i);
        	HV_DRIFT_MF[i][0]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_midfield/Sector_1", i);
                HV_DRIFT_MF[i][1]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_midfield/Sector_2", i);
                HV_DRIFT_MF[i][2]=dbprovider.getDouble("/calibration/mvt/bmt_hv/drift_midfield/Sector_3", i);
        	 
        }
         // beam offset
        double xb = dbprovider.getDouble("/geometry/beam/position/x_offset", 0);     
        double yb = dbprovider.getDouble("/geometry/beam/position/y_offset", 0); 
        double exb = dbprovider.getDouble("/geometry/beam/position/x_error", 0);     
        double eyb = dbprovider.getDouble("/geometry/beam/position/y_error", 0); 
        double err = 0;
        if(Math.sqrt(xb*xb+yb*yb)!=0) err = Math.sqrt((Math.pow(xb*exb,2)+Math.pow(yb*eyb,2))/(xb*xb+yb*yb));
        
        org.jlab.rec.cvt.Constants.setXb(xb*10);
        org.jlab.rec.cvt.Constants.setYb(yb*10);
        org.jlab.rec.cvt.Constants.setRbErr(err*10);
        System.out.println(" ................READ BEAM OFFSET PARAMETERS "+xb+" & "+yb+" cm.......");
        
        // target position
        org.jlab.rec.cvt.Constants.setZoffset(ztarget);
        
        Constants.setCRCRADIUS(CRCRADIUS);
        Constants.setCRZRADIUS(CRZRADIUS);
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
        Constants.setEFF_Z_OVER_A(EFF_Z_OVER_A);
        Constants.set_T_OVER_X0(T_OVER_X0);
        Constants.setTHETAL_grid(THETA_L_grid);
        Constants.setE_grid(ELEC_grid);
        Constants.setB_grid(MAG_grid);
        Constants.setPar_grid();
        Constants.setE_drift_FF(HV_DRIFT_FF);
        Constants.setE_drift_MF(HV_DRIFT_MF);
        dbprovider.disconnect();
        CSTLOADED = true;
        System.out
                .println("SUCCESSFULLY LOADED BMT CONSTANTS....");
     //   setDB(dbprovider);
    }

    //public static final synchronized DatabaseConstantProvider getDB() {
    //    return DB;
    //}

    //public static final synchronized void setDB(DatabaseConstantProvider dB) {
    //    DB = dB;
    //}
}
