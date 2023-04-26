package org.jlab.detector.geom.RICH;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import org.jlab.geom.prim.Point3D;

public class RICHGeoParameters {

    // Default values of RICH Reconstruction parameters to be re-loaded from CCDB or TxT

    public int       DO_ALIGNMENT                         =   1;        // if 1 apply alignment
    public int       ALIGN_PMT_PIVOT                      =   1;        // if 1 use MAPMT barycenter for rotations
    public int       APPLY_SURVEY                         =   0;        // if 1 apply the survey data for misalignment

    public double    ALIGN_SHIFT_SCALE                    =   1.0;      // Scale factor for misalignment shifts
    public double    ALIGN_ANGLE_SCALE                    =   1.0;      // Scale factor for misalignment angles

    public double    MIN_RAY_STEP                         =  0.0001;    // Min distance for the next tracing step
    public double    MAX_SPHE_DIST                        =   200.;     // Max distance to approximate the spherical mirror with triangles
    public double    MAPMT_EXTEND                         =   0.;      // MAPMT plane extension to prevent ray-tracing losses at the edge (cm)

    public double    XC_SPHE_MIR1                         =  -45.868;   // Nominal x coord of the RICH1 spherical mirror center      
    public double    YC_SPHE_MIR1                         =  0.0;       // Nominal y coord of the RICH1 spherical mirror center
    public double    ZC_SPHE_MIR1                         =  391.977;   // Nominal z coord of the RICH1 spherical mirror center        
    public double    RADIUS_SPHE_MIR1                     =  270.0;     // Nominal radius of the RICH1 spherical mirror center
    
    public double    XC_SPHE_MIR2                         =  -45.868;   // Nominal x coord of the RICH2 spherical mirror center      
    public double    YC_SPHE_MIR2                         =  0.0;       // Nominal y coord of the RICH2 spherical mirror center
    public double    ZC_SPHE_MIR2                         =  391.977;   // Nominal z coord of the RICH2 spherical mirror center        
    public double    RADIUS_SPHE_MIR2                     =  270.0;     // Nominal radius of the RICH2 spherical mirror center
    
    public int       DEBUG_GEO_PARAMS                     =   0;        // if 1 activate debug of geometry parameters
    public int       DEBUG_GEO_CONSTS                     =   0;        // if 1 activate debug of geometry calibration

    public Point3D   CENTER_SPHE_MIR1 = new Point3D(XC_SPHE_MIR1,YC_SPHE_MIR1,ZC_SPHE_MIR1);
    public Point3D   CENTER_SPHE_MIR2 = new Point3D(XC_SPHE_MIR2,YC_SPHE_MIR2,ZC_SPHE_MIR2);


    // ----------------
    public RICHGeoParameters() {
    // ----------------
    }


    //------------------------------
    public void load_CCDB(ConstantsManager manager, int run, int ncalls, boolean engineDebug){
    //------------------------------

        int debugMode = 0;

        init_ParametersCCDB( manager.getConstants(run, "/geometry/rich/geo_parameter"), engineDebug );

        if((debugMode>=1 || DEBUG_GEO_PARAMS>=1) && ncalls<Math.max(1,DEBUG_GEO_PARAMS)) {
            System.out.format("------------------------------------------------------------- \n");
            System.out.format("RICH: Load GEO parameters from CCDB for run %6d \n", run);
            System.out.format("------------------------------------------------------------- \n");
            System.out.format("Banks \n /geometry/rich/geo_parameter\n");
 
            dump_Parameters();
        }

        if(RICHGeoConstants.GEOPAR_FROM_FILE==1){

            init_ParametersTxT();

            if((debugMode>=1 || DEBUG_GEO_PARAMS>=1) && ncalls<Math.max(1,DEBUG_GEO_PARAMS)) {
                System.out.format("------------------------------------------------------------- \n");
                System.out.format("RICH: Load GEO parameters from local TxT file for run %6d \n", run);
                System.out.format("------------------------------------------------------------- \n");

                dump_Parameters();
            }
        }

    }

    //------------------------------
    public void init_ParametersCCDB(IndexedTable paraConstants, boolean engineDebug) {
    //------------------------------

        int debugMode = 0;

        DO_ALIGNMENT                =  paraConstants.getIntValue("do_align", 0, 0, 0);
        ALIGN_PMT_PIVOT             =  paraConstants.getIntValue("pmt_pivot", 0, 0, 0);
        APPLY_SURVEY                =  paraConstants.getIntValue("use_survey", 0, 0, 0);

        ALIGN_SHIFT_SCALE           =  paraConstants.getDoubleValue("shift_scale", 0, 0, 0);
        ALIGN_ANGLE_SCALE           =  paraConstants.getDoubleValue("angle_scale", 0, 0, 0);

        MIN_RAY_STEP                =  paraConstants.getDoubleValue("min_ray_step", 0, 0, 0);
        MAX_SPHE_DIST               =  paraConstants.getDoubleValue("max_sphe_dist", 0, 0, 0);
        MAPMT_EXTEND                =  paraConstants.getDoubleValue("mapmt_extend", 0, 0, 0);

        XC_SPHE_MIR1                =  paraConstants.getDoubleValue("xc_sphe_mir1", 0, 0, 0);
        YC_SPHE_MIR1                =  paraConstants.getDoubleValue("yc_sphe_mir1", 0, 0, 0);
        ZC_SPHE_MIR1                =  paraConstants.getDoubleValue("zc_sphe_mir1", 0, 0, 0);
        RADIUS_SPHE_MIR1            =  paraConstants.getDoubleValue("rc_sphe_mir1", 0, 0, 0);
    
        XC_SPHE_MIR2                =  paraConstants.getDoubleValue("xc_sphe_mir2", 0, 0, 0);
        YC_SPHE_MIR2                =  paraConstants.getDoubleValue("yc_sphe_mir2", 0, 0, 0);
        ZC_SPHE_MIR2                =  paraConstants.getDoubleValue("zc_sphe_mir2", 0, 0, 0); 
        RADIUS_SPHE_MIR2            =  paraConstants.getDoubleValue("rc_sphe_mir2", 0, 0, 0);

        DEBUG_GEO_PARAMS            =  paraConstants.getIntValue("debug_par", 0, 0, 0);
        DEBUG_GEO_CONSTS            =  paraConstants.getIntValue("debug_const", 0, 0, 0);

        CENTER_SPHE_MIR1.set(XC_SPHE_MIR1,YC_SPHE_MIR1,ZC_SPHE_MIR1);
        CENTER_SPHE_MIR2.set(XC_SPHE_MIR2,YC_SPHE_MIR2,ZC_SPHE_MIR2);

        if(!engineDebug){
            if(debugMode>=1)System.out.format("RICH GEO  debugging set to OFF \n");
            DEBUG_GEO_PARAMS            =  0;
            DEBUG_GEO_CONSTS            =  0;
        }

    }


    //------------------------------
    public void init_ParametersTxT() {
    //------------------------------

        int debugMode = 0;

        IndexedTable prova = new IndexedTable(3, "first/D:second/I");

        prova.addEntry(1,1,1);

        prova.setDoubleValue(0.455, "first", 1,1,1);
        prova.setIntValue(776, "first", 1,1,1);

        double value = prova.getDoubleValue("first", 1,1,1);
        int ivalue = prova.getIntValue("second", 1,1,1);

        System.out.format(" Value %7.2f %6d \n",value,ivalue);

    }


    //------------------------------
    public void dump_Parameters() {
    //------------------------------

        System.out.format(" \n");
        System.out.format("CCDB RICH PARA    DO_ALIGNMENT                 %9d \n", DO_ALIGNMENT);
        System.out.format("CCDB RICH PARA    ALIGN_PMT_PIVOT              %9d \n", ALIGN_PMT_PIVOT);
        System.out.format("CCDB RICH PARA    APPLY_SURVEY                 %9d \n", APPLY_SURVEY);

        System.out.format("CCDB RICH PARA    ALIGN_SHIFT_SCALE            %9.3f \n",    ALIGN_SHIFT_SCALE);
        System.out.format("CCDB RICH PARA    ALIGN_ANGLE_SCALE            %9.3f \n \n", ALIGN_ANGLE_SCALE);

        System.out.format("CCDB RICH PARA    MIN_RAY_STEP                 %9.3f (e-3) (cm)\n", MIN_RAY_STEP*1e3);
        System.out.format("CCDB RICH PARA    MAX_SPHE_DIST                %9.3f (cm)\n \n", MAX_SPHE_DIST);
        System.out.format("CCDB RICH PARA    MAPMT_EXTEND                 %9.3f (cm)\n \n", MAPMT_EXTEND);

        System.out.format("CCDB RICH PARA    XC_SPHE_MIR1                 %9.3f (cm) \n", CENTER_SPHE_MIR1.x());
        System.out.format("CCDB RICH PARA    YC_SPHE_MIR1                 %9.3f (cm) \n", CENTER_SPHE_MIR1.y());
        System.out.format("CCDB RICH PARA    ZC_SPHE_MIR1                 %9.3f (cm) \n", CENTER_SPHE_MIR1.z());
        System.out.format("CCDB RICH PARA    RADIUS_SPHE_MIR1             %9.3f (cm) \n \n", RADIUS_SPHE_MIR1);

        System.out.format("CCDB RICH PARA    XC_SPHE_MIR2                 %9.3f (cm) \n", CENTER_SPHE_MIR2.x());
        System.out.format("CCDB RICH PARA    YC_SPHE_MIR2                 %9.3f (cm) \n", CENTER_SPHE_MIR2.y());
        System.out.format("CCDB RICH PARA    ZC_SPHE_MIR2                 %9.3f (cm) \n", CENTER_SPHE_MIR2.z());
        System.out.format("CCDB RICH PARA    RADIUS_SPHE_MIR2             %9.3f (cm) \n \n", RADIUS_SPHE_MIR2);

        System.out.format("CCDB RICH PARA    DEBUG_GEO_PARAMS             %9d \n", DEBUG_GEO_PARAMS);
        System.out.format("CCDB RICH PARA    DEBUG_GEO_CONSTS             %9d \n", DEBUG_GEO_CONSTS);

     }

}
