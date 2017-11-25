/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.svt.test;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;

/**
 *
 * @author ziegler
 */
public class CCDBConstantsLoader {
    
    public CCDBConstantsLoader() {
        // TODO Auto-generated constructor stub
    }

    static boolean CSTLOADED = false;

    // static FTOFGeant4Factory geometry ;
    private static DatabaseConstantProvider DB;
    static DatabaseConstantProvider dbprovider = new DatabaseConstantProvider(
            10, "default");

    public static final synchronized void Load(int runNb) {
        // SVT GEOMETRY PARAMETERS
	// fundamentals
	int NREGIONS; // number of regions
	int[] NSECTORS; // number of sectors in a region
	int NFIDUCIALS; // number of survey fiducials on a sector module
	int NMODULES; // number of modules in a sector
	int NSENSORS; // number of sensors in a module
	int NSTRIPS; // number of strips in a layer
	int NPADS; // number of pads on High Voltage Rail
	double STRIPOFFSETWID; // offset of first intermediate sensor strip from edge of active zone
	double READOUTPITCH; // distance between start of strips along front of hybrid sensor
	double STEREOANGLE; // total angle swept by sensor strips
	int[] STATUS; // whether a region is used in Reconstruction
	//
	// position and orientation of layers
	double PHI0;
	double SECTOR0;
	double[] REFRADIUS; // outer side of U (inner) module
	double LAYERPOSFAC; // location of strip layer within sensor volume
	double[] Z0ACTIVE; // Cu edge of hybrid sensor's active volume
	// fiducials
	double[] SUPPORTRADIUS; // from MechEng drawings, to inner side of wide copper part
	double FIDCUX;
	double FIDPKX;
	double FIDORIGINZ;
	double FIDCUZ;
	double FIDPKZ0;
	double FIDPKZ1;
	//
	// dimensions of sensors
	double ACTIVESENWID;
	double PHYSSENWID;
	double DEADZNWID;
	//
	double SILICONTHK;
	//
	double PHYSSENLEN;
	double ACTIVESENLEN;
	double DEADZNLEN;
	double MICROGAPLEN; // spacing between sensors
	//
	// dimensions of passive materials
	int NMATERIALS;
	double PASSIVETHK;
	//
	// calculated on load()
	
	double[][] LAYERRADIUS; // radius to strip planes
	double LAYERGAPTHK; // distance between pairs of layers within a sector
	double MODULELEN;    // || DZ |  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  || DZ ||
	double STRIPLENMAX;  //      ||  AZ  | DZ |MG| DZ |  AZ  | DZ |MG| DZ |  AZ  ||
	double MODULEWID; // || DZ | AZ | DZ ||
	double SECTORLEN;
	double[][][] SECTORSHIFTDATA;
        //double
        
        // Load the tables
        dbprovider = new DatabaseConstantProvider(runNb, "default"); // reset
        // using
        // the
        // new
        // run
        // load the geometry tables       
        dbprovider.loadTable("/geometry/cvt/svt/svt");
	dbprovider.loadTable("/geometry/cvt/svt/region");
	dbprovider.loadTable("/geometry/cvt/svt/support");
	dbprovider.loadTable("/geometry/cvt/svt/fiducial");
	dbprovider.loadTable("/geometry/cvt/svt/material/box");
	dbprovider.loadTable("/geometry/cvt/svt/material/tube");
	dbprovider.loadTable("/geometry/cvt/svt/alignment");
        
        // read constants from svt table
        NREGIONS = dbprovider.getInteger("/geometry/cvt/svt/svt/nRegions", 0 );
        NMODULES = dbprovider.getInteger("/geometry/cvt/svt/svt/nModules", 0 );
        NSENSORS = dbprovider.getInteger("/geometry/cvt/svt/svt/nSensors", 0 );
        NSTRIPS = dbprovider.getInteger("/geometry/cvt/svt/svt/nStrips", 0 );
        NFIDUCIALS = dbprovider.getInteger("/geometry/cvt/svt/svt/nFiducials", 0 );
        NPADS = dbprovider.getInteger("/geometry/cvt/svt/svt/nPads", 0 );

        READOUTPITCH = dbprovider.getDouble("/geometry/cvt/svt/svt/readoutPitch", 0 );
        STEREOANGLE = Math.toRadians(dbprovider.getDouble("/geometry/cvt/svt/svt/stereoAngle", 0 ));
        PHI0 = Math.toRadians(dbprovider.getDouble("/geometry/cvt/svt/svt/phiStart", 0 ));
        SECTOR0 = Math.toRadians(dbprovider.getDouble("/geometry/cvt/svt/svt/zRotationStart", 0 ));
        LAYERPOSFAC = dbprovider.getDouble("/geometry/cvt/svt/svt/modulePosFac", 0 );

        SILICONTHK = dbprovider.getDouble("/geometry/cvt/svt/svt/siliconThk", 0 );
        PHYSSENLEN = dbprovider.getDouble("/geometry/cvt/svt/svt/physSenLen", 0 );
        PHYSSENWID = dbprovider.getDouble("/geometry/cvt/svt/svt/physSenWid", 0 );
        ACTIVESENLEN = dbprovider.getDouble("/geometry/cvt/svt/svt/activeSenLen", 0 );
        ACTIVESENWID = dbprovider.getDouble("/geometry/cvt/svt/svt/activeSenWid", 0 );
        DEADZNLEN = dbprovider.getDouble("/geometry/cvt/svt/svt/deadZnLen", 0 );
        DEADZNWID = dbprovider.getDouble("/geometry/cvt/svt/svt/deadZnWid", 0 );
        MICROGAPLEN = dbprovider.getDouble("/geometry/cvt/svt/svt/microGapLen", 0 );

        FIDCUX = dbprovider.getDouble("/geometry/cvt/svt/fiducial/CuX", 0 );
        FIDPKX = dbprovider.getDouble("/geometry/cvt/svt/fiducial/PkX", 0 );
        FIDORIGINZ = dbprovider.getDouble("/geometry/cvt/svt/fiducial/OriginZ", 0 );
        FIDCUZ = dbprovider.getDouble("/geometry/cvt/svt/fiducial/CuZ", 0 );
        FIDPKZ0 = dbprovider.getDouble("/geometry/cvt/svt/fiducial/PkZ0", 0 );
        FIDPKZ1 = dbprovider.getDouble("/geometry/cvt/svt/fiducial/PkZ1", 0 );
        
        // calculate derived constants
        int NLAYERS = NMODULES*NREGIONS;
        MODULELEN = NSENSORS*(ACTIVESENLEN + 2*DEADZNLEN) + (NSENSORS - 1)*MICROGAPLEN;
        STRIPLENMAX = MODULELEN - 2*DEADZNLEN;
        MODULEWID = ACTIVESENWID + 2*DEADZNWID;
        STRIPOFFSETWID = dbprovider.getDouble("/geometry/cvt/svt/svt/stripStart", 0 );
        LAYERGAPTHK = dbprovider.getDouble("/geometry/cvt/svt/svt/layerGapThk", 0 );
        
        // read constants from region and support table
        NSECTORS = new int[NREGIONS];
        STATUS = new int[NREGIONS];
        Z0ACTIVE = new double[NREGIONS];
        REFRADIUS = new double[NREGIONS]; // used to derive LAYERRADIUS
        SUPPORTRADIUS = new double[NREGIONS]; // used to build volumes
        LAYERRADIUS = new double[NREGIONS][NMODULES]; // used to build strips
        
        for( int regionIdx = 0; regionIdx < NREGIONS; regionIdx++ ) {
            NSECTORS[regionIdx] = dbprovider.getInteger("/geometry/cvt/svt/region/nSectors", regionIdx );
            STATUS[regionIdx] = dbprovider.getInteger("/geometry/cvt/svt/region/status", regionIdx );
            Z0ACTIVE[regionIdx] = dbprovider.getDouble("/geometry/cvt/svt/region/zStart", regionIdx ); // Cu edge of hybrid sensor's active volume
            REFRADIUS[regionIdx] = dbprovider.getDouble("/geometry/cvt/svt/region/UlayerOuterRadius", regionIdx); // radius to outer side of U (inner) module
            SUPPORTRADIUS[regionIdx] = dbprovider.getDouble("/geometry/cvt/svt/region/CuSupportInnerRadius", regionIdx); // radius to inner side of heatSinkRidge

            for( int m = 0; m < NMODULES; m++ )
            {
                switch( m ) 
                {
                case 0: // U = lower / inner
                        LAYERRADIUS[regionIdx][m] = REFRADIUS[regionIdx] - LAYERPOSFAC*SILICONTHK;
                        break;
                case 1: // V = upper / outer
                        LAYERRADIUS[regionIdx][m] = REFRADIUS[regionIdx] + LAYERGAPTHK + LAYERPOSFAC*SILICONTHK;
                        break;
                }
                //System.out.println("LAYERRADIUS "+ LAYERRADIUS[region][m]);
            }
        }
	
        SECTORSHIFTDATA = new double[NSECTORS[2]][3][7];
        int alignIdx=0;
        for( int regionIdx = 0; regionIdx < NREGIONS; regionIdx++ ) {
            for(int sectorIdx = 0; sectorIdx<NSECTORS[regionIdx]; sectorIdx++) {
                double tx = dbprovider.getDouble("/geometry/cvt/svt/alignment/tx", alignIdx );
                double ty = dbprovider.getDouble("/geometry/cvt/svt/alignment/ty", alignIdx );
                double tz = dbprovider.getDouble("/geometry/cvt/svt/alignment/tz", alignIdx );
                double rx = dbprovider.getDouble("/geometry/cvt/svt/alignment/rx", alignIdx );
                double ry = dbprovider.getDouble("/geometry/cvt/svt/alignment/ry", alignIdx );
                double rz = dbprovider.getDouble("/geometry/cvt/svt/alignment/rz", alignIdx );
                double ra = dbprovider.getDouble("/geometry/cvt/svt/alignment/ra", alignIdx );
                
                SECTORSHIFTDATA[sectorIdx][regionIdx][0] = tx;
                SECTORSHIFTDATA[sectorIdx][regionIdx][1] = ty;
                SECTORSHIFTDATA[sectorIdx][regionIdx][2] = tz;
                SECTORSHIFTDATA[sectorIdx][regionIdx][3] = rx;
                SECTORSHIFTDATA[sectorIdx][regionIdx][4] = ry;
                SECTORSHIFTDATA[sectorIdx][regionIdx][5] = rz;
                SECTORSHIFTDATA[sectorIdx][regionIdx][6] = ra;
                
                alignIdx++;
            }
        }		 

    }
    
    
}
