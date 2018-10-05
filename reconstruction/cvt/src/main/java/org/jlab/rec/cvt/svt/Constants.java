package org.jlab.rec.cvt.svt;

import java.util.ArrayList;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Triangle3D;

public class Constants {

    /**
     * Constants used in the reconstruction
     */
    Constants() {
    }

    // THRESHOLDS
    public static int initThresholds = 30;
    public static int deltaThresholds = 15;

    // RECONSTRUCTION CONSTANTS
    public static final double RHOVTXCONSTRAINT = 1. / Math.sqrt(12.);//0.1;
    public static final double ZVTXCONSTRAINT = 50. / Math.sqrt(12);//5cm
    public static double ETOTCUT = 10.0;
    // GEOMETRY PARAMETERS
    public static final int[] NSECT = new int[]{10,10,14,14,18,18};
    public static final int NSLAYR = 2;
    public static final int NLAYR = 6;
    public static final int NREG = 3;
    //public static final int NREG = 4;
    public static final int NSTRIP = 256;

    public static final int MAXNUMSECT = 18;
   
    
    // CONSTANTS USED IN RECONSTRUCTION
    //---------------------------------
    public static double LIGHTVEL = 0.000299792458;       // velocity of light (mm/ns) - conversion factor from radius in mm to momentum in GeV/c 

    // selection cuts for helical tracks
    public static final double MINRADCURV = 200.00; //in cm

    // cut on Edep min;
    public static double edep_min = 0.020; //20keV=0.020
    // cut on strip intersection tolerance
    public static double interTol = 10.0; //10.0 = /1 cm
    // sum of strip numbers for valid intersection:
    //public static int sumStpNumMin = 174;
    //public static int sumStpNumMax = 259;
    public static int sumStpNumMin = 70;
    public static int sumStpNumMax = 350;

    // contants for dEdx 
    //------------------
    //public static double rho = 2.3296; 	// g/cm^3 (Si)
    static double CaThick = 0.5;
    static double RohacellThick = 2.5;
    static double Z_eff_roha = Math.pow((7.84 / 100.) * Math.pow(1, 2.94) + (64.5 / 100.) * Math.pow(6, 2.94) + (8.38 / 100.) * Math.pow(7, 2.94) + (19.12 / 100.) * Math.pow(8, 2.94), (1. / 2.94));

    // empirical scaling factor from MC
    public static final double SILICONRADLEN = 9.36 * 10; //check this - converted to mm
    public static final double SILICONTHICK = 0.320;    // Silicon sensor width
    public static double detMatZ_ov_A_timesThickn = (14. * 2 * SILICONTHICK / 28.0855 + (Z_eff_roha * RohacellThick / 12.0588) + 6 * CaThick / 12.0107);
    //...................
    //Code for identifying BST in making an ID for a bst intersection
    public static int BSTidCode = 1;

    // ----- cut based cand select
    public static double phi12cut = 35.;
    public static double phi13cut = 35.;
    public static double phi14cut = 35.;
    public static double radcut = 100.;
    public static double dzdrcut = 200.;// used to be 150

    //BST misalignments
    public static boolean isRadialMisalignmentTest = false;
    public static final double RadSpecs = 0.750;

    public static final double CIRCLEFIT_MAXCHI2 = 100;

    public static final int BSTTRKINGNUMBERITERATIONS = 3;

    public static final int MAXNUMCROSSES = 50;

    public static final int MAXNUMCROSSESINMODULE = 4;

    // these are the constants for ADC to energy conversion 
    public static final int NBITSADC = 3; // 3bit adc for BST

    public static int EMAXREADOUT = 1;

    // for cosmics
    public static final double COSMICSMINRESIDUAL = 2;
    public static final double COSMICSMINRESIDUALZ = 20;

    public static ArrayList<ArrayList<Shape3D>> MODULEPLANES;

    // track list cut-off
    public static int maxNcands = 200;
    public static boolean hasWidthResolution = false;

    public static boolean ignoreErr = false;

    public static boolean areConstantsLoaded = false;

    public static boolean removeClones = true;

    public static final double MODULEPOSFAC = 0.5; // % wrt top of  module

    public static final double PIDCUTOFF = 2.6;

    public static final double TOLTOMODULEEDGE = 1.0; // Tolerance for track trajectory point at layer to module fiducial edge (mm)

    public static double MAXDISTTOTRAJXY = 5; //max xy dist to cross in cm

    public static int BSTEXCLUDEDFITREGION = 0;
   
    public static boolean LAYEREFFS = false;

    public static synchronized void Load() {
        if (areConstantsLoaded) {
            return;
        } else {
            
            ArrayList<ArrayList<Shape3D>> modules = new ArrayList<ArrayList<Shape3D>>();
            Geometry geo = new Geometry();

            for (int layer = 1; layer <= 8; layer++) {
                ArrayList<Shape3D> layerModules = new ArrayList<Shape3D>();

                for (int sector = 1; sector <= Constants.NSECT[layer - 1]; sector++) {

                    Shape3D module = new Shape3D();

                    Point3D PlaneModuleOrigin = geo.getPlaneModuleOrigin(sector, layer);
                    double x0 = PlaneModuleOrigin.x();
                    double y0 = PlaneModuleOrigin.y();
                    Point3D PlaneModuleEnd = geo.getPlaneModuleEnd(sector, layer);
                    double x1 = PlaneModuleEnd.x();
                    double y1 = PlaneModuleEnd.y();

                    double[] z = new double[6];
                    z[0] = PlaneModuleOrigin.z();
                    z[1] = PlaneModuleOrigin.z() + SVTConstants.ACTIVESENLEN;
                    z[2] = PlaneModuleOrigin.z() + SVTConstants.ACTIVESENLEN + 1 * SVTConstants.DEADZNLEN;
                    z[3] = PlaneModuleOrigin.z() + 2 * SVTConstants.ACTIVESENLEN + 1 * SVTConstants.DEADZNLEN;
                    z[4] = PlaneModuleOrigin.z() + 2 * SVTConstants.ACTIVESENLEN + 2 * SVTConstants.DEADZNLEN;
                    z[5] = PlaneModuleOrigin.z() + 3 * SVTConstants.ACTIVESENLEN + 2 * SVTConstants.DEADZNLEN;

                    for (int i = 0; i < 5; i++) {
                        Point3D ModulePoint1 = new Point3D(x0, y0, z[i]);
                        Point3D ModulePoint2 = new Point3D(x1, y1, z[i]);
                        Point3D ModulePoint3 = new Point3D(x0, y0, z[i + 1]);
                        Point3D ModulePoint4 = new Point3D(x1, y1, z[i + 1]);

                        Triangle3D Module1 = new Triangle3D(ModulePoint1, ModulePoint2, ModulePoint4);
                        Triangle3D Module2 = new Triangle3D(ModulePoint1, ModulePoint3, ModulePoint4);

                        module.addFace(Module1);
                        module.addFace(Module2);
                    }

                    layerModules.add(module);
                }
                modules.add(layer - 1, layerModules);
            }
            MODULEPLANES = modules;
        }

        areConstantsLoaded = true;
        System.out.println(" SVT geometry constants loaded ? " + areConstantsLoaded);
    
    }

}
