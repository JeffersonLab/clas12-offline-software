package org.jlab.rec.fmt;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;


/**
 *
 * @author defurne, ziegler, benkel, devita
 */
public class Geometry {

    private static int nLayers;
    private static int nStrips;     // Number of strips: 1024
    private static int nHalfStrips; // In the middle of the FMT, 320 strips are split in two.

    public static  double pitch;      // strip width 525 um
    private static double interStrip; // inter strip
    private static double hDrift; // Thickness of the drift region in the micromegas

    private static double beamHole; // Radius of the hole in the center for the beam.
    public static  double stripSigma;

    private static double[] layerZ;     // Give z-coordinate of the layer
    private static double[] layerAngle; // Give the rotation angle to apply

    private static Point3D[] offset;
    private static Point3D[] rotation;
    
    public static Line3D[]   stripLocal;
    public static Line3D[][] stripGlobal;
    
//    private static double[] EFF_Z_OVER_A; // for ELOSS
//    private static double[] _X0;          // for M.Scat.
//    private static double[] _REL_POS;     // relative postion of the material wrt to strip plane
//    public static boolean areConstantsLoaded = false;

    
    
    
    public static final synchronized void Load(int run, String variation) {

        // Load the tables
        DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(run, variation);

//        // Load material budget:
//        dbprovider.loadTable("/test/mvt/fmt_mat");
//
//        // Material budget
//        // ===============
//        double[] EFF_Z_OVER_A;
//        double[] EFFX0;
//        double[] REL_POS;
//        EFF_Z_OVER_A = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
//        EFFX0 = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
//        REL_POS = new double[dbprovider.length("/test/mvt/fmt_mat/thickness")];
//        for (int i = 0; i < dbprovider.length("/test/mvt/fmt_mat/thickness"); i++) {
//            double RelPos = dbprovider.getDouble("/test/mvt/fmt_mat/relative_position", i);
//            double Zeff =  dbprovider.getDouble("/test/mvt/fmt_mat/average_z", i);
//            double Aeff =  dbprovider.getDouble("/test/mvt/fmt_mat/average_a", i);
//            double X0 =  dbprovider.getDouble("/test/mvt/fmt_mat/x0", i);
//            EFF_Z_OVER_A[i] = Zeff/Aeff;
//            EFFX0[i] = X0;
//            REL_POS[i] = RelPos;
//        }
//        Constants.setEFF_Z_OVER_A(EFF_Z_OVER_A);
//        Constants.set_X0(EFFX0);
//        Constants.set_RELPOS(REL_POS);
        // Get common variables for all disks and apply shifts
        // ===============
        dbprovider.loadTable("/geometry/fmt/fmt_global");
        hDrift      = dbprovider.getDouble("/geometry/fmt/fmt_global/hDrift", 0) / 10.;
        pitch       = dbprovider.getDouble("/geometry/fmt/fmt_global/Pitch", 0) / 10.;
        interStrip  = dbprovider.getDouble("/geometry/fmt/fmt_global/Interstrip", 0) / 10.;
        beamHole    = dbprovider.getDouble("/geometry/fmt/fmt_global/R_min", 0) / 10.;
        nStrips     = dbprovider.getInteger("/geometry/fmt/fmt_global/N_strip", 0);
        nHalfStrips = dbprovider.getInteger("/geometry/fmt/fmt_global/N_halfstr", 0);

        // Position and strip orientation of each disks
        // ===============
        dbprovider.loadTable("/geometry/fmt/fmt_layer_noshim");
        nLayers = dbprovider.length("/geometry/fmt/fmt_layer_noshim/Z");
        layerZ     = new double[nLayers];
        layerAngle = new double[nLayers];
        for (int i = 0; i < dbprovider.length("/geometry/fmt/fmt_layer_noshim/Z"); i++) {
            layerZ[i]     = dbprovider.getDouble("/geometry/fmt/fmt_layer_noshim/Z", i) / 10.;
            layerAngle[i] = Math.toRadians(dbprovider.getDouble("/geometry/fmt/fmt_layer_noshim/Angle", i));
        }

        // Layer alignment constants
        // ===============
        dbprovider.loadTable("/geometry/fmt/alignment");
        offset   = new Point3D[nLayers];
        rotation = new Point3D[nLayers];
        for (int i = 0; i < Geometry.nLayers; i++) {
            double deltaX = dbprovider.getDouble("/geometry/fmt/alignment/deltaX", i) / 10.;
            double deltaY = dbprovider.getDouble("/geometry/fmt/alignment/deltaY", i) / 10.;
            double deltaZ = dbprovider.getDouble("/geometry/fmt/alignment/deltaZ", i) / 10.;
            double rotX   = Math.toRadians(dbprovider.getDouble("/geometry/fmt/alignment/rotX", i));
            double rotY   = Math.toRadians(dbprovider.getDouble("/geometry/fmt/alignment/rotY", i));
            double rotZ   = Math.toRadians(dbprovider.getDouble("/geometry/fmt/alignment/rotZ", i));
            layerZ[i]     += deltaZ + hDrift/2;
            layerAngle[i] += rotZ;
            offset[i]   = new Point3D(deltaX, deltaY, layerZ[i]);
            rotation[i] = new Point3D(rotX, rotY, layerAngle[i]);
        }
        
        dbprovider.disconnect();

        System.out.println("SUCCESSFULLY LOADED FMT CONSTANTS....");
        
        createStrips();
    }


    private static void createStrips() {

        stripLocal  = new Line3D[nStrips];
        stripGlobal = new Line3D[nLayers][nStrips];
        int nSideStrips = (nStrips - 2*nHalfStrips)/2;              // 192
        double rMax     = pitch * (nHalfStrips + 2*nSideStrips)/2.; // 184.8 mm
        stripSigma      = pitch/Math.sqrt(12);

        for (int i = 0; i < nStrips; i++) {
            // Give the Y of the middle of the strip
            double x1=0;
            double x2=0;
            double y=0;
            if (i < 512){
                y =  -rMax + (511-i+0.5)*pitch;
            } else {
                y = rMax - (1023-i+0.5)*pitch;
            }

            int localRegion = getLocalRegion(i);
            switch(localRegion) {
            case 1:
                x1 = getLayerXEdge(rMax,y);
                x2 = 0;
                if(Math.abs(y)/beamHole<1) {
                    x2 = getLayerXEdge(beamHole,y);
                }
                break;
            case 3:
                x1 =  0;
                x2 = -getLayerXEdge(rMax,y);
                if(Math.abs(y)/beamHole<1) {
                    x1 = -getLayerXEdge(beamHole,y);
                }
                break;
            default:
                x1 =  getLayerXEdge(rMax,y);
                x2 = -getLayerXEdge(rMax,y);
                break;
            }
            stripLocal[i]  = new Line3D(x1,y,0,x2,y,0);    
            for (int j = 0; j < nLayers; j++) { // x sign flipgit s
                stripGlobal[j][i] = new Line3D(stripLocal[i]);
                stripGlobal[j][i].rotateZ(rotation[j].z());
                stripGlobal[j][i].rotateY(rotation[j].y());
                stripGlobal[j][i].rotateX(rotation[j].x());
                stripGlobal[j][i].translateXYZ(offset[j].x(), offset[j].y(), offset[j].z());
            }
        }

        System.out.println("*****   FMT strips created!");
    }

    private static double getLayerXEdge(double radius, double y) {
        return radius*Math.sin(Math.acos(Math.abs(y)/radius));
    }
    
    private static int getLocalRegion(int i) {
            // To represent the geometry we divide the barrel micromega disk into 4 regions according to the strip numbering system.
            // Here i = strip_number -1;
            // Region 1 is the region in the negative x part of inner region: the strips range is from   1 to 320  (   0 <= i < 320)
            // Region 2 is the region in the negative y part of outer region: the strips range is from 321 to 512  ( 320 <= i < 512)
            // Region 3 is the region in the positive x part of inner region: the strips range is from 513 to 832  ( 512 <= i < 832)
            // Region 4 is the region in the positive y part of outer region: the strips range is from 833 to 1024 ( 832 <= i < 1024)
            if (i>=  0 && i< 320) return 1;
            if (i>=320 && i< 512) return 2;
            if (i>=512 && i< 832) return 3;
            if (i>=832 && i<1024) return 4;
            return 0;
    }

    /**
     * Transform a Point3D from global to a FMT layer's local coordinates, applying the x-y alignment
     * shifts in the process.
     * @param glPos: Point3D describing the position to be transformed in lab coordinates.
     * @param layer: Target FMT layer.
      * @return 
     */
    public static Point3D globalToLocal(Point3D glPos, int layer) {
        return globalToLocal(glPos.x(),glPos.y(),glPos.z(),layer);
    }
    
    public static Point3D globalToLocal(double x, double y, double z, int layer) {
        Point3D local = new Point3D(x,y,z);
        local.translateXYZ(-offset[layer-1].x(), -offset[layer-1].y(), -offset[layer-1].z());
        local.rotateX(-rotation[layer-1].x());
        local.rotateY(-rotation[layer-1].y());
        local.rotateZ(-rotation[layer-1].z());
        return local;
    }
        
    public static double getZ(int layer) {
        return layerZ[layer-1];
    }

    public static double getDx(int layer) {
        return -Math.sin(rotation[layer-1].z());
    }

    public static double getDy(int layer) {
        return Math.cos(rotation[layer-1].z());
    }
    
    /**
     *
     * @param other
     * @return a boolean comparing 2 hits based on basic descriptors; returns
     * true if the hits are the same
     */
    public static boolean areClose(int strip1, int strip2) {
        Point3D p0 = stripLocal[strip1-1].midpoint();
        Point3D p1 = stripLocal[strip2-1].midpoint();
        if(Math.round(Math.abs(p0.y()-p1.y())/pitch)==1 && 
           Math.abs(Math.signum(p0.x())-Math.signum(p1.x()))<=1) {
            return true;
        }
        else return false;
    }


//    public static synchronized double[] getEFF_Z_OVER_A() {
//        return EFF_Z_OVER_A;
//    }
//    public static synchronized void setEFF_Z_OVER_A(double[] eFF_Z_OVER_A) {
//        EFF_Z_OVER_A = eFF_Z_OVER_A;
//    }
//
//    public static synchronized double[] get_X0() {
//        return _X0;
//    }
//    public static synchronized void set_X0(double[] X0) {
//       _X0 = X0;
//    }
//
//    public static synchronized double[] get_RELPOS() {
//        return _REL_POS;
//    }
//    public static synchronized void set_RELPOS(double[] REL_POS) {
//       _REL_POS = REL_POS;
//    }
}
