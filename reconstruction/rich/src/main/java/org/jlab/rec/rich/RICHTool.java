/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rich;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.geant4.v2.RICHGeant4Factory;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4Box;

import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import eu.mihosoft.vrl.v3d.Polygon;   
import eu.mihosoft.vrl.v3d.CSG;   
import org.jlab.geometry.prim.Line3d;   

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Sphere3D;   
import org.jlab.geom.prim.Triangle3D;   
import org.jlab.geom.prim.Face3D;   
import org.jlab.geom.prim.Shape3D;   

import org.jlab.utils.groups.IndexedTable;


/**
 *
 * @author mcontalb
 */
public class RICHTool{

    //------------------------------
    public void RICHTool(){
    //------------------------------

    }


    private final static int anode_map[] = {60,58,59,57,52,50,51,49,44,42,43,41,36,34,35,
                     33,28,26,27,25,20,18,19,17,12,10,11,9,4,2,3,1,5,7,6,8,13,15,14,16,21,
                     23,22,24,29,31,30,32,37,39,38,40,45,47,46,48,53,55,54,56,61,63,62,64};


    private final static int tile2pmt[][]={{   1,   2,   3},  
                        {   4,   5,   6},  
                        {   7,   0,   8},  
                        {   9,  10,  11},  
                        {  12,   0,  13},  
                        {  14,  15,  16},  
                        {  17,   0,  18},  
                        {  19,  20,  21},  
                        {  22,  23,  24},  
                        {  25,  26,  27},  
                        {  28,  29,  30},  
                        {  31,   0,  32},  
                        {  33,  34,  35},  
                        {  36,  37,  38},  
                        {  39,   0,  40},  
                        {  41,  42,  43},  
                        {  44,  45,  46},  
                        {  47,  48,  49},  
                        {  50,   0,  51},  
                        {  52,  53,  54},  
                        {  55,  56,  57},  
                        {  58,  59,  60},  
                        {  61,  62,  63},  
                        {  64,   0,  65},  
                        {  66,  67,  68},  
                        {  69,  70,  71},  
                        {  72,  73,  74},  
                        {  75,   0,  76},  
                        {  77,  78,  79},  
                        {  80,  81,  82},  
                        {  83,  84,  85},  
                        {  86,  87,  88},  
                        {  89,   0,  90},  
                        {  91,  92,  93},  
                        {  94,  95,  96},  
                        {  97,  98,  99},  
                        { 100, 101, 102},  
                        { 103, 104, 105},  
                        { 106,   0, 107},  
                        { 108, 109, 110},  
                        { 111, 112, 113},  
                        { 114, 115, 116},  
                        { 117, 118, 119},  
                        { 120,   0, 121},  
                        { 122, 123, 124},  
                        { 125, 126, 127},  
                        { 128, 129, 130},  
                        { 131, 132, 133},  
                        { 134, 135, 136},  
                        { 137,   0, 138},  
                        { 139, 140, 141},  
                        { 142, 143, 144},  
                        { 145, 146, 147},  
                        { 148, 149, 150},  
                        { 151, 152, 153},  
                        { 154, 155, 156},  
                        { 157,   0, 158},  
                        { 159, 160, 161},  
                        { 162, 163, 164},  
                        { 165, 166, 167},  
                        { 168, 169, 170},  
                        { 171, 172, 173},  
                        { 174,   0, 175},  
                        { 176, 177, 178},  
                        { 179, 180, 181},  
                        { 182, 183, 184},  
                        { 185, 186, 187},  
                        { 188, 189, 190},  
                        { 191, 192, 193},  
                        { 194,   0, 195},  
                        { 196, 197, 198},  
                        { 199, 200, 201},  
                        { 202, 203, 204},  
                        { 205, 206, 207},  
                        { 208, 209, 210},  
                        { 211, 212, 213},  
                        { 214, 215, 216},  
                        { 217,   0, 218},  
                        { 219, 220, 221},  
                        { 222, 223, 224},  
                        { 225, 226, 227},  
                        { 228, 229, 230},  
                        { 231, 232, 233},  
                        { 234, 235, 236},  
                        { 237,   0, 238},  
                        { 239, 240, 241},  
                        { 242, 243, 244},  
                        { 245, 246, 247},  
                        { 248, 249, 250},  
                        { 251, 252, 253},  
                        { 254, 255, 256},  
                        { 257, 258, 259},  
                        { 260,   0, 261},  
                        { 262, 263, 264},  
                        { 265, 266, 267},  
                        { 268, 269, 270},  
                        { 271, 272, 273},  
                        { 274, 275, 276},  
                        { 277, 278, 279},  
                        { 280, 281, 282},  
                        { 283, 284, 285},  
                        { 286,   0, 287},  
                        { 288, 289, 290},  
                        { 291, 292, 293},  
                        { 294, 295, 296},  
                        { 297, 298, 299},  
                        { 300, 301, 302},  
                        { 303, 304, 305},  
                        { 306, 307, 308},  
                        { 309,   0, 310},  
                        { 311, 312, 313},  
                        { 314, 315, 316},  
                        { 317, 318, 319},  
                        { 320, 321, 322},  
                        { 323, 324, 325},  
                        { 326, 327, 328},  
                        { 329, 330, 331},  
                        { 332, 333, 334},  
                        { 335,   0, 336},  
                        { 337, 338, 339},  
                        { 340, 341, 342},  
                        { 343, 344, 345},  
                        { 346, 347, 348},  
                        { 349, 350, 351},  
                        { 352, 353, 354},  
                        { 355, 356, 357},  
                        { 358, 359, 360},  
                        { 361, 362, 363},  
                        { 364,   0, 365},  
                        { 366, 367, 368},  
                        { 369, 370, 371},  
                        { 372, 373, 374},  
                        { 375, 376, 377},  
                        { 378, 379, 380},  
                        { 381, 382, 383},  
                        { 384, 385, 386},  
                        { 387, 388, 389},  
                        { 390,   0, 391},
                        { 392, 393, 394},  //tracking station
                        { 395, 396, 397}};


    private final static int NLAY=13;
    private final static int NROW=25;
    private final static int NCOL=56;
    private final static int NPMT=391;
    private final static int NPIX=64;
    private final static int NCOMPO=10;

    private double pmt_timeoff[][] = new double[NPMT][NPIX];
    private double pmt_timewalk[][] = new double[NPMT][4];
    private double pixel_gain[][] = new double[NPMT][NPIX];
    private double pixel_eff[][] = new double[NPMT][NPIX];
    private int pixel_flag[][] = new int [NPMT][NPIX];            // 0 = dead, 1 = ok, 2= hot
    private int pixel_ntime[][] = new int [NPMT][NPIX];            // 0 = dead, 1 = ok, 2= hot
    private double pixel_mtime[][] = new double [NPMT][NPIX];            // 0 = dead, 1 = ok, 2= hot
    private double pixel_stime[][] = new double [NPMT][NPIX];            // 0 = dead, 1 = ok, 2= hot

    private double aero_refi[][] = new double[4][32];
    private double aero_plan[][] = new double[4][32];
    private double aero_chele_dir[][][] = new double[4][32][225];
    private double aero_chele_lat[][][] = new double[4][32][225];
    private double aero_chele_spe[][][] = new double[4][32][225];
    private double aero_schele_dir[][][] = new double[4][32][225];
    private double aero_schele_lat[][][] = new double[4][32][225];
    private double aero_schele_spe[][][] = new double[4][32][225];

    private Vector3D rich_survey_angle  = new Vector3D();
    private Vector3D rich_survey_shift  = new Vector3D();
    private Vector3D layer_misa_angle[][] = new Vector3D[NLAY+1][NCOMPO+1];
    private Vector3D layer_misa_shift[][] = new Vector3D[NLAY+1][NCOMPO+1];
    private RICHFrame rich_frame = new RICHFrame();
    private RICHFrame survey_frame = new RICHFrame();

    private final static int pfirst[] = {1, 7,14,22,31,41,52,64,77, 91,106,122,139,157,176,196,217,239,262,286,311,337,364,392,395};
    private final static int plast[]  = {6,13,21,30,40,51,63,76,90,105,121,138,156,175,195,216,238,261,285,310,336,363,391,394,397};

    private int nxp[] = new int[397]; // X coordinate of pixel 1 of each mapmt
    private int nyp[] = new int[397]; // Y coordinate of pixel 1 of each mapmt

    public RICHGeant4Factory richfactory = new RICHGeant4Factory();

    private List<RICHLayer> opticlayers = new ArrayList<RICHLayer>();

    private RICHPixel MAPMTpixels  = null; 

    private final static int NTIME = 10;
    private long RICH_START_TIME = (long) 0;
    private long RICH_LAST_TIME = (long) 0;
    private double richprocess_time[] = new double[NTIME];
    private int richprocess_ntimes[] = new int[NTIME];

    private RICHConstants reco_constants = new RICHConstants();

    //------------------------------
    public void init_GeoConstants(int iflag, IndexedTable aeroConstants, IndexedTable misaConstants, IndexedTable paraConstants){
    //------------------------------

        // generate the tracking layers (0 = only Aerogel and MaPMT for trajectory, 1 = all)
        // start processing time
        init_ProcessTime();

        // reset alignment constants
        for (int ila=0; ila<NLAY+1; ila++){
            for (int ico=0; ico<NCOMPO+1; ico++){
                layer_misa_shift[ila][ico] = new Vector3D(0., 0., 0.);
                layer_misa_angle[ila][ico] = new Vector3D(0., 0., 0.);
            }
        }
    
        // load constants
        if(RICHConstants.READ_FROM_FILES==1){
            init_ConstantsTxT(1);
            init_ConstantsTxT(3);
        }else{
            init_GeoConstantsCCDB(aeroConstants, misaConstants, paraConstants);
        }

        if(iflag>0){
            // global pixel coordinat indexes
            init_GlobalPixelGeo();

            // RICH survey
            init_Survey();
        }

        // RICH geometry organized on layers of Shape3D area and RICH components 
        init_RICHLayers(iflag);

    } 

    //------------------------------
    public void init_TimeConstants(IndexedTable timewalkConstants, IndexedTable timeoffConstants, IndexedTable cheleConstants, IndexedTable pixelConstants){
    //------------------------------

        // load constants
        if(RICHConstants.READ_FROM_FILES==1){
            init_ConstantsTxT(2);
        }else{
            init_TimeConstantsCCDB(timewalkConstants, timeoffConstants, cheleConstants, pixelConstants);
        }

    }


    //------------------------------
    public int Maroc2Anode(int channel) {
    //------------------------------

        // return anode from MAROC channel
        return anode_map[(channel)%64];
    }

    //------------------------------
    public int Tile2PMT(int tile, int channel) {
    //------------------------------

        // return anode from MAROC channel

        return tile2pmt[tile-1][(int) (channel-1)/64];
    }


    //------------------------------
    public int Anode2idx(int anode) {
    //------------------------------

        // return anode idx position within the pmt
        return (anode-1)%8+1;
    }


    //------------------------------
    public int Anode2idy(int anode) {
    //------------------------------

        // return anode idy position within the pmt
        return 8-(anode-1)/8;
    }


    
    //------------------------------
    public int get_Globalidx(int pmt, int anode) {
    //------------------------------
    // return global idx on the RICH plane

	if(pmt>391)return nxp[pmt-1]-(Anode2idx(anode)-1); //obsolete for cosmics
        return nxp[pmt-1]+(Anode2idx(anode)-1);
    }


    //------------------------------
    public int get_Globalidy(int pmt, int anode) {
    //------------------------------
    // return global idy on the RICH plane

        if(pmt>391)return nyp[pmt-1]-(Anode2idy(anode)-1);  //obsolete for cosmics
        return nyp[pmt-1]+(Anode2idy(anode)-1);
    }


    //------------------------------
    public RICHConstants get_Constants() {return reco_constants;}
    //------------------------------


    //------------------------------
    public void init_TimeConstantsCCDB(IndexedTable timewalkConstants, IndexedTable timeoffConstants, IndexedTable cheleConstants, IndexedTable pixelConstants){
    //------------------------------

        int debugMode = 1;

        /*
        * TIME_OFFSETs
        */

        for(int ipmt=0; ipmt<NPMT; ipmt++){
            for(int ich=0; ich<NPIX; ich++){
                pmt_timeoff[ipmt][ich] = (float) timeoffConstants.getDoubleValue("offset", 4, ipmt+1, ich+1);
            }
            if(debugMode>=1 && reco_constants.RICH_DEBUG>0){
                if(ipmt<10 || ipmt>380)System.out.format("CCDB RICH TOFF    ipmt %4d  %8.3f (ch1)  %8.3f (ch2)  %8.3f (ch63)  %8.3f (ch64) \n", ipmt+1,
                   pmt_timeoff[ipmt][0], pmt_timeoff[ipmt][1], pmt_timeoff[ipmt][62], pmt_timeoff[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB RICH TOFF     ....... \n");
                if(ipmt==390)System.out.format("  \n");
            }
        }

        /*
        *  TIME_WALKs
        */

        // TODO: time_walk bank definition is wrong
        for(int ipmt=0; ipmt<NPMT; ipmt++){
            pmt_timewalk[ipmt][0] = (float) timewalkConstants.getDoubleValue("D0", 4, ipmt+1, 0);
            pmt_timewalk[ipmt][1] = (float) timewalkConstants.getDoubleValue("m1", 4, ipmt+1, 0);
            pmt_timewalk[ipmt][2] = (float) Math.abs(timewalkConstants.getDoubleValue("m2", 4, ipmt+1, 0))*(-1.0);
            pmt_timewalk[ipmt][3] = (float) timewalkConstants.getDoubleValue("T0", 4, ipmt+1, 0);
            if(debugMode>=1 && reco_constants.RICH_DEBUG>0){
                if(ipmt<10 || ipmt>380)System.out.format("CCDB RICH TWALK   ipmt %4d  D0 = %8.3f  T0 = %8.3f  m1 = %8.4f  m2 = %8.4f\n", ipmt+1,
                         pmt_timewalk[ipmt][0], pmt_timewalk[ipmt][1] , pmt_timewalk[ipmt][2], pmt_timewalk[ipmt][3]);
                if(ipmt==10)System.out.format("CCDB RICH TWALK    ....... \n");
                if(ipmt==390)System.out.format("  \n");
            }
        }

        /*
        * AEROGEL CALIBRATED OPTCIS (USING ELECTRON CONTROL SAMPLE)
        */

        int ndo[] = {16,22,32,32};
        double mrad = reco_constants.MRAD;
        for (int ila=0; ila<4; ila++){
            for (int ico=0; ico<ndo[ila]*225; ico++){
                int itil = (int) ico/225;
                int iqua  = (int) ico - itil*225;

                aero_chele_dir[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("ch_dir", 4,201+ila,ico+1);
                aero_chele_lat[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("ch_lat", 4,201+ila,ico+1);
                aero_chele_spe[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("ch_spe", 4,201+ila,ico+1);

                aero_schele_dir[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("s_dir", 4,201+ila,ico+1);
                aero_schele_lat[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("s_lat", 4,201+ila,ico+1);
                aero_schele_spe[ila][itil][iqua] = (float) cheleConstants.getDoubleValue("s_spe", 4,201+ila,ico+1);

                if(debugMode>=1 && reco_constants.RICH_DEBUG>0){
                    if( (itil<2 || itil>ndo[ila]-3) && (iqua==0 || iqua==224)) {
                        System.out.format("CCDB RICH CHELE   ila %4d  itile %3d  iq %4d dir = %7.2f  %7.2f  lat = %7.2f  %7.2f  spe = %7.2f  %7.2f \n", 201+ila, itil+1, iqua+1,
                        aero_chele_dir[ila][itil][iqua]*mrad, aero_schele_dir[ila][itil][iqua]*mrad,
                        aero_chele_lat[ila][itil][iqua]*mrad, aero_schele_lat[ila][itil][iqua]*mrad,
                        aero_chele_spe[ila][itil][iqua]*mrad, aero_schele_spe[ila][itil][iqua]*mrad);
                    }
                    if(ila==3 && ico==ndo[ila]*225-1)System.out.format("  \n");
                }
            }
        }

        /*
        * PIXELS
        */

        for(int ipmt=0; ipmt<NPMT; ipmt++){
            for(int ich=0; ich<NPIX; ich++){
                pixel_gain[ipmt][ich] = (float) pixelConstants.getDoubleValue("gain", 4, ipmt+1, ich+1);
                pixel_eff[ipmt][ich] = (float) pixelConstants.getDoubleValue("efficiency", 4, ipmt+1, ich+1);
                pixel_flag[ipmt][ich] = (int) pixelConstants.getIntValue("status", 4, ipmt+1, ich+1);
                
                pixel_ntime[ipmt][ich] = (int) pixelConstants.getIntValue("N_t", 4, ipmt+1, ich+1);
                pixel_mtime[ipmt][ich] = (float) pixelConstants.getDoubleValue("mean_t", 4, ipmt+1, ich+1);
                pixel_stime[ipmt][ich] = (float) pixelConstants.getDoubleValue("sigma_t", 4, ipmt+1, ich+1);
            }
            if(debugMode>=1 && reco_constants.RICH_DEBUG>0){
                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL GAIN    ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt+1,
                   pixel_gain[ipmt][0], pixel_gain[ipmt][1], pixel_gain[ipmt][62], pixel_gain[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL GAIN     ....... \n");

                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL EFF     ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt+1,
                   pixel_eff[ipmt][0], pixel_eff[ipmt][1], pixel_eff[ipmt][62], pixel_eff[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL EFF      ....... \n");

                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL STATUS  ipmt %4d  %8d (ch1)  %8d (ch2)  %8d (ch63)  %8d (ch64) \n", ipmt+1,
                   pixel_flag[ipmt][0], pixel_flag[ipmt][1], pixel_flag[ipmt][62], pixel_flag[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL STATUS   ....... \n");

                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL NTIME   ipmt %4d  %8d (ch1)  %8d (ch2)  %8d (ch63)  %8d (ch64) \n", ipmt+1,
                   pixel_ntime[ipmt][0], pixel_ntime[ipmt][1], pixel_ntime[ipmt][62], pixel_ntime[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL NTIME    ....... \n");

                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL MTIME   ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt+1,
                   pixel_mtime[ipmt][0], pixel_mtime[ipmt][1], pixel_mtime[ipmt][62], pixel_mtime[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL MTIME    ....... \n");

                if(ipmt<2 || ipmt>388)System.out.format("CCDB PIXEL STIME   ipmt %4d  %8.2f (ch1)  %8.2f (ch2)  %8.2f (ch63)  %8.2f (ch64) \n", ipmt+1,
                   pixel_stime[ipmt][0], pixel_stime[ipmt][1], pixel_stime[ipmt][62], pixel_stime[ipmt][63]);
                if(ipmt==10)System.out.format("CCDB PIXEL STIME    ....... \n");

                if(ipmt==390)System.out.format("  \n");
            }
        }

    }


    //------------------------------
    public void init_GeoConstantsCCDB(IndexedTable paraConstants, IndexedTable aeroConstants, IndexedTable misaConstants){
    //------------------------------

        int debugMode = 1;

        /*
        * RECONSTRUCTION PARAMETERS
        */

        reco_constants.DO_MISALIGNMENT             =  paraConstants.getIntValue("flag1", 4, 0, 0); 
        reco_constants.FORCE_DC_MATCH              =  paraConstants.getIntValue("flag2", 4, 0, 0);
        reco_constants.MISA_RICH_REF               =  paraConstants.getIntValue("flag3", 4, 0, 0);
        reco_constants.MISA_PMT_PIVOT              =  paraConstants.getIntValue("flag4", 4, 0, 0);
        reco_constants.APPLY_SURVEY                =  paraConstants.getIntValue("flag5", 4, 0, 0);

        reco_constants.DO_ANALYTIC                 =  paraConstants.getIntValue("flag6", 4, 0, 0);
        reco_constants.THROW_ELECTRONS             =  paraConstants.getIntValue("flag7", 4, 0, 0);
        reco_constants.THROW_PIONS                 =  paraConstants.getIntValue("flag8", 4, 0, 0);
        reco_constants.THROW_KAONS                 =  paraConstants.getIntValue("flag9", 4, 0, 0);
        reco_constants.THROW_PROTONS               =  paraConstants.getIntValue("flag10", 4, 0, 0);
        reco_constants.THROW_PHOTON_NUMBER         =  paraConstants.getIntValue("flag11", 4, 0, 0);
        reco_constants.TRACE_PHOTONS               =  paraConstants.getIntValue("flag12", 4, 0, 0);

        reco_constants.REDO_RICH_RECO              =  paraConstants.getIntValue("flag13", 4, 0, 0);
        reco_constants.DO_MIRROR_HADS              =  paraConstants.getIntValue("flag14", 4, 0, 0);  
        reco_constants.DO_CURVED_AERO              =  paraConstants.getIntValue("flag15", 4, 0, 0);  

        reco_constants.USE_ELECTRON_ANGLES         =  paraConstants.getIntValue("flag16", 4, 0, 0);
        reco_constants.USE_PIXEL_PROPERTIES        =  paraConstants.getIntValue("flag17", 4, 0, 0);
        reco_constants.SAVE_THROWS                 =  paraConstants.getIntValue("flag18", 4, 0, 0);
        reco_constants.QUADRANT_NUMBER             =  paraConstants.getIntValue("flag19", 4, 0, 0);

        reco_constants.GOODHIT_FRAC                =  paraConstants.getDoubleValue("par1", 4, 0, 0);
        reco_constants.RICH_DCMATCH_CUT            =  paraConstants.getDoubleValue("par2", 4, 0, 0);
        reco_constants.RICH_HITMATCH_RMS           =  paraConstants.getDoubleValue("par3", 4, 0, 0);
        reco_constants.RICH_DIRECT_RMS             =  paraConstants.getDoubleValue("par4", 4, 0, 0) / 1000.;
        reco_constants.SHOW_PROGRESS_INTERVAL      =  paraConstants.getDoubleValue("par5", 4, 0, 0);
        reco_constants.THROW_ASSOCIATION_CUT       =  paraConstants.getDoubleValue("par6", 4, 0, 0);

        reco_constants.RICH_DEBUG                  =  paraConstants.getDoubleValue("par7", 4, 0, 0);
        reco_constants.RICH_TIME_RMS               =  paraConstants.getDoubleValue("par8", 4, 0, 0);
        reco_constants.MISA_SHIFT_SCALE            =  paraConstants.getDoubleValue("par9", 4, 0, 0);
        reco_constants.MISA_ANGLE_SCALE            =  paraConstants.getDoubleValue("par10", 4, 0, 0);
        
        //TODO: check
        //reco_constants.RICH_DEBUG                  =  1.0;
        //reco_constants.QUADRANT_NUMBER             =  5;
        //reco_constants.USE_ELECTRON_ANGLES         =  1;
        //reco_constants.USE_PIXEL_PROPERTIES        =  1;
        if(debugMode>=1 && reco_constants.RICH_DEBUG>0){   //MC
        //if(debugMode>=1){ 

            System.out.format(" \n");
            System.out.format("CCDB RICH PARA    DO_MISALIGNMENT              %9d \n", reco_constants.DO_MISALIGNMENT); 
            System.out.format("CCDB RICH PARA    FORCE_DC_MATCH               %9d \n", reco_constants.FORCE_DC_MATCH); 
            System.out.format("CCDB RICH PARA    MISA_RICH_REF                %9d \n", reco_constants.MISA_RICH_REF); 
            System.out.format("CCDB RICH PARA    MISA_PMT_PIVOT               %9d \n", reco_constants.MISA_PMT_PIVOT); 
            System.out.format("CCDB RICH PARA    APPLY_SURVEY                 %9d \n", reco_constants.APPLY_SURVEY); 

            System.out.format("CCDB RICH PARA    DO_ANALYTIC                  %9d \n", reco_constants.DO_ANALYTIC); 
            System.out.format("CCDB RICH PARA    THROW_ELECTRONS              %9d \n", reco_constants.THROW_ELECTRONS); 
            System.out.format("CCDB RICH PARA    THROW_PIONS                  %9d \n", reco_constants.THROW_PIONS); 
            System.out.format("CCDB RICH PARA    THROW_KAONS                  %9d \n", reco_constants.THROW_KAONS); 
            System.out.format("CCDB RICH PARA    THROW_PROTONS                %9d \n", reco_constants.THROW_PROTONS); 
            System.out.format("CCDB RICH PARA    THROW_PHOTON_NUMBER          %9d \n", reco_constants.THROW_PHOTON_NUMBER); 
            System.out.format("CCDB RICH PARA    TRACE_PHOTONS                %9d \n", reco_constants.TRACE_PHOTONS); 

            System.out.format("CCDB RICH PARA    REDO_RICH_RECO               %9d \n", reco_constants.REDO_RICH_RECO); 
            System.out.format("CCDB RICH PARA    DO_MIRROR_HADS               %9d \n", reco_constants.DO_MIRROR_HADS); 
            System.out.format("CCDB RICH PARA    DO_CURVED_AERO               %9d \n", reco_constants.DO_CURVED_AERO); 

            System.out.format("CCDB RICH PARA    USE_ELECTRON_ANGLES          %9d \n", reco_constants.USE_ELECTRON_ANGLES); 
            System.out.format("CCDB RICH PARA    USE_PIXEL_PROPERTIES         %9d \n", reco_constants.USE_PIXEL_PROPERTIES); 
            System.out.format("CCDB RICH PARA    SAVE_THROWS                  %9d \n", reco_constants.SAVE_THROWS);
            System.out.format("CCDB RICH PARA    QUADRANT_NUMBER              %9d \n \n", reco_constants.QUADRANT_NUMBER);

            System.out.format("CCDB RICH PARA    GOODHIT_FRAC                 %9.4f \n", reco_constants.GOODHIT_FRAC); 
            System.out.format("CCDB RICH PARA    RICH_DCMATCH_CUT             %9.4f \n", reco_constants.RICH_DCMATCH_CUT); 
            System.out.format("CCDB RICH PARA    RICH_HITMATCH_RMS            %9.4f \n", reco_constants.RICH_HITMATCH_RMS); 
            System.out.format("CCDB RICH PARA    RICH_DIRECT_RMS              %9.4f \n", reco_constants.RICH_DIRECT_RMS); 
            System.out.format("CCDB RICH PARA    SHOW_PROGRESS_INTERVAL       %9.4f \n", reco_constants.SHOW_PROGRESS_INTERVAL); 
            System.out.format("CCDB RICH PARA    THROW_ASSOCIATION_CUT        %9.4f \n", reco_constants.THROW_ASSOCIATION_CUT); 

            System.out.format("CCDB RICH PARA    RICH_DEBUG                   %9.4f \n", reco_constants.RICH_DEBUG); 
            System.out.format("CCDB RICH PARA    RICH_TIME_RMS                %9.4f \n", reco_constants.RICH_TIME_RMS); 
            System.out.format("CCDB RICH PARA    MISA_SHIFT_SCALE             %9.4f \n", reco_constants.MISA_SHIFT_SCALE); 
            System.out.format("CCDB RICH PARA    MISA_ANGLE_SCALE             %9.4f \n", reco_constants.MISA_ANGLE_SCALE); 
            System.out.format(" \n");

        }


        /*
        *  SINGLE COMPONENT MISALIGNMENT
        *  This comes on top of the RICH survey and global transformation
        */

        int NMISA = 24;
        int ccdb_ila[] = {0,201,202,203,204,301,301,301,301,301,301,301,302,302,302,302,302,302,302,302,302,302,302,401};
        int ccdb_ico[] = {0,  0,  0,  0,  0,  1,  2,  3,  4,  5,  6,  7,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10,  0};

        //                                   BO  F1  F2  R1  R2   L1  L2   
        int tool_ila[] = {0,  1,  2,  3,  4, 11,  5,  6,  9, 10,  7,  8, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 13};
        int tool_ico[] = {0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10,  0};

        double sscale = reco_constants.MISA_SHIFT_SCALE;
        double ascale = reco_constants.MISA_ANGLE_SCALE / reco_constants.MRAD;  // to convert in rad

        for (int im=0; im<NMISA; im++){

            int lla = ccdb_ila[im];
            int cco = ccdb_ico[im];
            double dx = (double) misaConstants.getDoubleValue("dx", 4, lla, cco);
            double dy = (double) misaConstants.getDoubleValue("dy", 4, lla, cco);
            double dz = (double) misaConstants.getDoubleValue("dz", 4, lla, cco);
            double thx = (double) misaConstants.getDoubleValue("dthx", 4, lla, cco);
            double thy = (double) misaConstants.getDoubleValue("dthy", 4, lla, cco);
            double thz = (double) misaConstants.getDoubleValue("dthz", 4, lla, cco);

            // the rotation is assumed to be in the component local ref system
            int ila = tool_ila[im];
            int ico = tool_ico[im];
            layer_misa_shift[ila][ico].add( new Vector3D( dx*sscale,  dy*sscale,  dz*sscale));
            layer_misa_angle[ila][ico].add( new Vector3D(thx*ascale, thy*ascale, thz*ascale));

            if(debugMode>=1 && reco_constants.RICH_DEBUG>0){
                //System.out.format("QUA QUA %4d %4d  %d %d  %7.2f %7.2f %7.2f  %7.2f %7.2f \n",ila,ico,lla,cco,dx,dy,dz,
                //    layer_misa_shift[ila][ico].mag(),layer_misa_angle[ila][ico].mag()*reco_constants.MRAD);
                if(layer_misa_shift[ila][ico].mag()>0 || layer_misa_angle[ila][ico].mag()>0){
                    System.out.format("CCDB RICH MISA    ila  %4d ico %3d  (%4d %3d)  shift %s  angle %s \n", lla,cco, ila,ico,
                               layer_misa_shift[ila][ico].toStringBrief(3), layer_misa_angle[ila][ico].toStringBrief(3));
                }
                if(im==23)System.out.format("  \n");
            }

        }


        /*
        * AEROGEL NOMINAL OPTCIS
        */

        int nco[] = {16,22,32,32};
        for (int ila=0; ila<4; ila++){
            for (int ico=0; ico<nco[ila]; ico++){
                aero_refi[ila][ico] = (float) aeroConstants.getDoubleValue("n400", 4,201+ila,ico+1);
                aero_plan[ila][ico] = (float) aeroConstants.getDoubleValue("planarity", 4,201+ila, ico+1);
                if(debugMode>=2 && reco_constants.RICH_DEBUG>0)System.out.format("CCDB RICH AERO    ila %4d  ico %3d  n = %8.5f  pla = %8.2f\n", 201+ila, ico+1, aero_refi[ila][ico], aero_plan[ila][ico]);
            }
        }

        if(debugMode>=2){
            int ndo[] = {16,22,32,32};
            for (int ila=0; ila<4; ila++){
                for (int ico=0; ico<ndo[ila]; ico++){
                    for (int iq=0; iq<225; iq++) {
                        int icompo = ico*225+iq+1;
                        System.out.format(" KK 4  %4d %6d  1 312.0 6.0  1 310.0 7.0  1 313.0 10.0 \n", 201+ila, icompo+1);
                    }
                }
            }
        }

    }

    //------------------------------
    public void init_ConstantsTxT(int flag){
    //------------------------------
    // To be moved to CCDB

        int debugMode = 1;

       if(flag==2){
           /**
            * TIME_OFFSETs
            */
            String off_filename = new String("CALIB_DATA/MIRA/richTimeOffsets.out");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(off_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    String[] array = currentLine.split(" ");
                    int ipmt = Integer.parseInt(array[0]);
                    int ich  = Integer.parseInt(array[1]);
                    float off = Float.parseFloat(array[4]);
                    pmt_timeoff[ipmt-1][ich-1] = off;

                    if(debugMode>=1)if(ich==1 || ich==64)
                              System.out.format("TXT RICH TOFF   pmt %4d (ich=%3d: %8.2f) \n", ipmt, ich, pmt_timeoff[ipmt-1][ich-1]);

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", off_filename);
                e.printStackTrace();

            }


            /*
            *  TIME_WALKs
            */
            String walk_filename = new String("CALIB_DATA/MIRA/richTimeWalks.out");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(walk_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    String[] array = currentLine.split(" ");
                    int ipmt = Integer.parseInt(array[0]);

                    if(debugMode>=1)System.out.format("TXT WALK   pmt %d", ipmt);
                    for (int ich=1; ich<5; ich++){
                        float walk = Float.parseFloat(array[1+(ich-1)*2]);
                        if(ich==4 && walk<-1000)walk= (float)-0.100;
                        pmt_timewalk[ipmt-1][ich-1] = walk;
                        if(debugMode>=1)System.out.format(" (%d, %8.4f) ", ich, pmt_timewalk[ipmt-1][ich-1]);
                    }
                    if(debugMode>=1)System.out.format("\n");

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", walk_filename);
                e.printStackTrace();

            }
       }


        if(flag==1){

            /*
            * DC_OFFSETs
            */
            String dcoff_filename = new String("CALIB_DATA/DC_offsets_4013.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(dcoff_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {    

                    String[] array = currentLine.split(" ");
                    int idc    = Integer.parseInt(array[0]);
                    int imatch = Integer.parseInt(array[1]);
                    int iref   = Integer.parseInt(array[2]);
                    int ipiv   = Integer.parseInt(array[3]);
                    int isur   = Integer.parseInt(array[4]);

                    float  ss  = Float.parseFloat(array[5]);
                    float  sa  = Float.parseFloat(array[6]);

                    int inp    = Integer.parseInt(array[7]);
                    
                    float  hr  = Float.parseFloat(array[8]);

                    reco_constants.DO_MISALIGNMENT = idc;
                    reco_constants.FORCE_DC_MATCH  = imatch;
                    reco_constants.MISA_RICH_REF   = iref;
                    reco_constants.MISA_PMT_PIVOT  = ipiv;
                    reco_constants.APPLY_SURVEY    = isur;

                    reco_constants.MISA_SHIFT_SCALE     = (double) ss ;
                    reco_constants.MISA_ANGLE_SCALE     = (double) sa;

                    reco_constants.THROW_PHOTON_NUMBER  = inp;
                    reco_constants.RICH_DIRECT_RMS      = (double) hr / 1000.;

                    if(debugMode>=1){

                        System.out.format("TEXT PARA    DO_MISALIGNMENT              %7d \n", reco_constants.DO_MISALIGNMENT);
                        System.out.format("TEXT PARA    FORCE_DC_MATCH               %7d \n", reco_constants.FORCE_DC_MATCH);
                        System.out.format("TEXT PARA    MISA_RICH_REF                %7d \n", reco_constants.MISA_RICH_REF);
                        System.out.format("TEXT PARA    MISA_PMT_PIVOT               %7d \n", reco_constants.MISA_PMT_PIVOT);
                        System.out.format("TEXT PARA    APPLY_SURVEY                 %7d \n", reco_constants.APPLY_SURVEY);

                        System.out.format("TEXT PARA    MISA_SHIFT_SCALE             %7.3f \n", reco_constants.MISA_SHIFT_SCALE);
                        System.out.format("TEXT PARA    MISA_ANGLE_SCALE             %7.3f \n", reco_constants.MISA_ANGLE_SCALE);

                        System.out.format("TEXT PARA    THROW_PHOTON_NUMBER          %7d \n", reco_constants.THROW_PHOTON_NUMBER);

                        System.out.format("TEXT PARA    RICH_DIRECT_RMS              %7.3f (mrad) \n", reco_constants.RICH_DIRECT_RMS*1000);

                    }

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", dcoff_filename);
                e.printStackTrace();
            }


            double sscale = reco_constants.MISA_SHIFT_SCALE;
            double ascale = reco_constants.MISA_ANGLE_SCALE / reco_constants.MRAD;  // to convert in rad


            /*
            *  SINGLE COMPONENT MISALIGNMENT
            *  This comes on top of the RICH survey and global transformation
            */
            /*for (int ila=0; ila<NLAY+1; ila++){
                for (int ico=0; ico<NCOMPO+1; ico++){
                    layer_misa_shift[ila][ico] = new Vector3D(0., 0., 0.);
                    layer_misa_angle[ila][ico] = new Vector3D(0., 0., 0.);
                }
            }*/

            String misaco_filename = new String("CALIB_DATA/RICHlayer_misalignment.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(misaco_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {    

                    String[] array = currentLine.split(" ");
                    int isec = Integer.parseInt(array[0]);
                    int lla = Integer.parseInt(array[1]);
                    int cco = Integer.parseInt(array[2]);

                    float  dx  = Float.parseFloat(array[3]);
                    float  dy  = Float.parseFloat(array[4]);
                    float  dz  = Float.parseFloat(array[5]);
                    float  thx = Float.parseFloat(array[6]);
                    float  thy = Float.parseFloat(array[7]);
                    float  thz = Float.parseFloat(array[8]);

                    int[] ind = {0,0};
                    if(convert_indexes(lla, cco, ind)){

                        int ila=ind[0];
                        int ico=ind[1];
                        if(debugMode>=0)System.out.format("MISA conversion %4d %3d --> %4d %3d \n",lla,cco,ila,ico);

                        // the rotation is assumed to be in the component local ref system
                        layer_misa_shift[ila][ico].add( new Vector3D( dx*sscale,  dy*sscale,  dz*sscale));
                        layer_misa_angle[ila][ico].add( new Vector3D(thx*ascale, thy*ascale, thz*ascale));

                        if(debugMode>=0){
                            if(layer_misa_shift[ila][ico].mag()>0 || layer_misa_angle[ila][ico].mag()>0){
                                System.out.format("TXT MISA   layer %4d ico %3d  (%4d %3d)  shift %s  angle %s \n", ila,ico,lla,cco, 
                                   layer_misa_shift[ila][ico].toStringBrief(3), layer_misa_angle[ila][ico].toStringBrief(3));
                            }
                        }

                    }else{
                        System.out.format("Unsupported imisalignment for layer %3d %3d \n",lla,cco);
                    }
                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", misaco_filename);
                e.printStackTrace();
            }
        }


        if(flag==3){

            /*
            * AEROGEL OPTCIS
            */

            String aero_filename = new String("CALIB_DATA/aerogel_passports.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(aero_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {    

                    String[] array = currentLine.split(" ");
                    int idlay = Integer.parseInt(array[1]);
                    int iaer = Integer.parseInt(array[2]);
                    
                    if(debugMode>=1)System.out.format("Read optics for AERO lay %3d  compo %3d", idlay, iaer); 
                    float refi = Float.parseFloat(array[5]);
                    float plana = Float.parseFloat(array[11]);
                    aero_refi[idlay-201][iaer-1] = refi;
                    aero_plan[idlay-201][iaer-1] = plana;
                    //aero_refi[idlay-201][iaer-1] = (float) RICHConstants.RICH_AEROGEL_INDEX;
                    if(debugMode>=1)System.out.format(" n = %8.5f   pla = %8.2f \n", aero_refi[idlay-201][iaer-1], aero_plan[idlay-201][iaer-1]);
                    
                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", aero_filename);
                e.printStackTrace();
            }

            if(debugMode>=1)System.out.format("initConstants: DONE \n");
            
        }

        if(flag==4){

           /*
            * AEROGEL CALIBRATED OPTICS
            */

            String chele_filename = new String("CALIB_DATA/aerogel_chele.txt");

            try {

                BufferedReader bf = new BufferedReader(new FileReader(chele_filename));
                String currentLine = null;

                while ( (currentLine = bf.readLine()) != null) {

                    String[] array = currentLine.split(" ");
                    int idlay = Integer.parseInt(array[1]);
                    int iaer  = Integer.parseInt(array[2]);
                    int iqua  = Integer.parseInt(array[3]);

                    if(debugMode>=1)System.out.format("Read chele for AERO lay %3d  compo %3d quadrant  %3d", idlay, iaer, iqua);

                    int ndir     = Integer.parseInt(array[4]);
                    float chdir  = Float.parseFloat(array[5]);
                    float sdir   = Float.parseFloat(array[6]);

                    int nlat     = Integer.parseInt(array[7]);
                    float chlat  = Float.parseFloat(array[8]);
                    float slat   = Float.parseFloat(array[9]);

                    int nspe     = Integer.parseInt(array[10]);
                    float chspe  = Float.parseFloat(array[11]);
                    float sspe   = Float.parseFloat(array[12]);

                    aero_chele_dir[idlay-201][iaer-1][iqua] = chdir;
                    aero_chele_lat[idlay-201][iaer-1][iqua] = chlat;
                    aero_chele_spe[idlay-201][iaer-1][iqua] = chspe;

                    aero_schele_dir[idlay-201][iaer-1][iqua] = sdir;
                    aero_schele_lat[idlay-201][iaer-1][iqua] = slat;
                    aero_schele_spe[idlay-201][iaer-1][iqua] = sspe;

                    //aero_refi[idlay-201][iaer-1] = (float) RICHConstants.RICH_AEROGEL_INDEX;
                    if(debugMode>=1)System.out.format(" n = %8.5f   pla = %8.2f \n", aero_refi[idlay-201][iaer-1], aero_plan[idlay-201][iaer-1]);

                }

            } catch (Exception e) {

                System.err.format("Exception occurred trying to read '%s' \n", chele_filename);
                e.printStackTrace();
            }

            if(debugMode>=1)System.out.format("initConstants: DONE \n");

        }

    }


    //------------------------------
    public boolean convert_indexes(int lla, int cco, int[] ind){
    //------------------------------

        int[] lateral_compo = {11,5,6,9,10,7,8};

        /*
        *  Aerogel
        */
	if(lla==0 && cco==0){ind[0]=lla; ind[1]=cco; return true;}
	if(lla>=201 && lla<=204 && cco==0){ind[0]=lla-200; ind[1]=cco; return true;}
        if(lla==301 && cco>0 && cco<=7) {ind[0]=lateral_compo[cco-1]; ind[1]=0; return true;}
        if(lla==302){
            if(cco==0){ind[0]=12; ind[1]=0; return true;}
            if(cco>0 && cco<11){ind[0]=12; ind[1]=cco; return true;}
        }
        if(lla==401 && cco==0){ind[0]=13; ind[1]=cco; return true;}

        return false;
    }


    //------------------------------
    public void init_Survey(){
    //------------------------------

        int debugMode = 0;

        if(debugMode>=1){
            System.out.format("---------------\n");
            System.out.format("Calculate RICH Alignment from Survey\n");
            System.out.format("---------------\n");
        }

        /* 
        *  Define nominal plane
        */
        Point3D RDA = new Point3D(-300.274,  168.299,  460.327);
        Point3D RDB = new Point3D(-300.309,  -168.299, 460.310);
        Point3D RDC = new Point3D(-31.102,     0., 585.886);

        Triangle3D f = new Triangle3D( RDA, RDB, RDC);
        Vector3D nomi_n = f.normal();
        Point3D  nomi_b = f.center();
        Shape3D  nomi_plane = new Shape3D(f);

        Shape3D rich_survey_plane = new Shape3D(f);
        
        /*
        *   Define surveyed plane
        */
        Point3D mRDA = new Point3D(-301.211, 168.505, 467.514);
        Point3D mRDB = new Point3D(-300.514, -167.929, 465.334);
        Point3D mRDC = new Point3D(-31.552, -0.086, 591.329);

        Triangle3D mf= new Triangle3D( mRDA, mRDB, mRDC);
        Shape3D real_plane = new Shape3D(mf);
        Vector3D real_n = mf.normal();
        Point3D  real_b = mf.center();

        if(debugMode>=1){
            // check possible deformations
            double check_a = f.point(1).distance(f.point(0));
            double check_b = f.point(2).distance(f.point(1));
            double check_c = f.point(2).distance(f.point(0));

            double checp_a = mf.point(1).distance(mf.point(0));
            double checp_b = mf.point(2).distance(mf.point(1));
            double checp_c = mf.point(2).distance(mf.point(0));

            System.out.format("Sides nominal    %8.3f %8.3f %8.3f \n",check_a, check_b, check_c);
            System.out.format("Sides real       %8.3f %8.3f %8.3f \n",checp_a, checp_b, checp_c);
        }

        // define shift among barycenters
        Vector3D diff_b = real_b.vectorFrom(nomi_b);
        //rich_survey_center = nomi_b;

        Vector3D rich_xref = new Vector3D(Math.cos(25/180.*Math.PI),0.,Math.sin(25/180.*Math.PI));
        Vector3D rich_yref = new Vector3D(0.,1.,0.);
        Vector3D rich_zref = new Vector3D(-Math.sin(25/180.*Math.PI),0.,Math.cos(25/180.*Math.PI));
        survey_frame = new RICHFrame (rich_xref, rich_yref, rich_zref, nomi_b.toVector3D());


        // define rotation angle and vector
        Vector3D dir = nomi_n.cross(real_n).asUnit();
        double ang = Math.acos(nomi_n.dot(real_n));
        Vector3D rota_n = dir.multiply(ang);

        double mrad = RICHConstants.MRAD;

        rich_survey_shift = diff_b.clone();  
        rich_survey_angle = rota_n.clone();

        //Vector3d dcrich_shift = new Vector3d(global_shift[0], global_shift[1], global_shift[2]);
        //this.rich_survey_shift = new Vector3d(misa_shift.plus(dcrich_shift));
        //Vector3d dcrich_angle = new Vector3d(global_angle[0], global_angle[1], global_angle[2]);
        //this.rich_survey_angle = new Vector3d(misa_angle.plus(dcrich_angle));

        if(debugMode>=1){
            /*System.out.format(" -------------------- \n");
            System.out.format(" survey angle %s \n", rota_n.multiply(mrad).toStringBrief(2));
            System.out.format(" survey shift %.2f %7.2f \n", diff_b.x, diff_b.y, diff_b.z);
            System.out.format(" -------------------- \n");
            System.out.format(" misalg angle %7.2f %7.2f %7.2f \n", misa_angle.x*mrad, misa_angle.y*mrad, misa_angle.z*mrad);
            System.out.format(" misalg shift %7.2f %7.2f %7.2f \n", misa_shift.x, misa_shift.y, misa_shift.z);
            System.out.format(" -------------------- \n");
            System.out.format(" extern angle %7.2f %7.2f %7.2f \n", dcrich_angle.x*mrad, dcrich_angle.y*mrad, dcrich_angle.z*mrad);
            System.out.format(" extern shift %7.2f %7.2f %7.2f \n", dcrich_shift.x, dcrich_shift.y, dcrich_shift.z);
            System.out.format(" -------------------- \n");*/
            System.out.format(" survey angle %s \n", rich_survey_angle.multiply(mrad).toStringBrief(2));
            System.out.format(" survey shift %s \n", rich_survey_shift.toStringBrief(2));
            System.out.format(" -------------------- \n");
        
            System.out.format(" Check survey plane \n");
            System.out.format(" -------------------- \n");
            double thex = rich_survey_angle.dot(new Vector3D(1.,0.,0.));
            double they = rich_survey_angle.dot(new Vector3D(0.,1.,0.));
            double thez = rich_survey_angle.dot(new Vector3D(0.,0.,1.));

            System.out.format("Rot Angles NewRef %7.2f | %7.2f %7.2f %7.2f \n", ang*mrad, thex*mrad, they*mrad, thez*mrad);

            Vector3D new_n = nomi_n.clone();
            new_n.rotateZ(thez);
            new_n.rotateY(they);
            new_n.rotateX(thex);

            System.out.format("Normal nominal %s \n", nomi_n.toString());
            System.out.format("Normal real    %s \n", real_n.toString());
            System.out.format("Normal rotated %s \n", new_n.toString());
            System.out.format("\n");
            System.out.format("Baryc  nominal %s \n", nomi_b.toString());
            System.out.format("Baryc  real    %s \n", real_b.toString());
            System.out.format("Baryc  diff    %s \n", diff_b.toString());
            System.out.format("\n");

            show_Shape3D(nomi_plane, null, null);

            show_Shape3D(real_plane, null, null);

            /* test misalignment angle and shift
            Face3D at = new Triangle3D( RDA, RDB, RDC);
            Shape3D test_plane = new Shape3D(at);

            //misalign_TrackingPlane(test_plane, -1);
            show_Shape3D(test_plane, null, null);

            double aang = 10./57.3;
            Vector3d ini = new Vector3d(Math.sin(aang), 0., Math.cos(aang));
            Vector3d anor = new Vector3d(0., 0.1, 1.);
            Vector3d nor = anor.normalized();
            Vector3d out = Transmission(ini, nor, 1.10, 1.00);
            System.out.format(" nor %s \n", toString(nor));
            System.out.format(" ini %s \n", toString(ini));
            System.out.format(" out %s \n", toString(out));
            

            double aa = Math.acos(ini.dot(nor)/ini.magnitude());
            double bb = Math.acos(out.dot(nor)/out.magnitude());
            double cc = Math.acos(ini.dot(Vector3d.Z_ONE)/ini.magnitude());
            double dd = Math.acos(out.dot(Vector3d.Z_ONE)/out.magnitude());
            System.out.format(" ini angle vn %8.3f  vz %8.3f \n", aa*57.3, bb*57.3);
            System.out.format(" out angle vn %8.3f  vz %8.3f \n", cc*57.3, dd*57.3);

            Vector3d out2 = Transmission2(ini, nor, 1.10, 1.00);
            aa = Math.acos(ini.dot(nor)/ini.magnitude());
            bb = Math.acos(out2.dot(nor)/out2.magnitude());
            cc = Math.acos(ini.dot(Vector3d.Z_ONE)/ini.magnitude());
            dd = Math.acos(out2.dot(Vector3d.Z_ONE)/out2.magnitude());
            System.out.format(" ini angle vn %8.3f  vz %8.3f \n", aa*57.3, bb*57.3);
            System.out.format(" out angle vn %8.3f  vz %8.3f \n", cc*57.3, dd*57.3);
            */


        }

    }

    //------------------------------
    public void init_RICHLayers(int iflag){
    //------------------------------
    // Take RICHFactory Layers of Geant4 volumes (for GEMC) and convert in coatjava Layers 
    // of RICH components accounting for optical descriptiors plus basic tracking 
    // planes for effective ray tracing
    // ATT: to be done: aerogel cromatic dispersion, mirror reflectivity vs wavelength

        int debugMode = 0;

        /*
        * relevant characterization of the basic tracking planes
        */
        Vector3D front   = new Vector3D(-0.42,   0.00,   0.91);
        Vector3D left    = new Vector3D(-0.50,  -0.87,   0.00);
        Vector3D right   = new Vector3D(-0.50,   0.87,   0.00);
        Vector3D bottom  = new Vector3D(-1.00,   0.00,   0.00);
        Vector3D back    = new Vector3D( 0.42,   0.00,  -0.91);
        Vector3D sphere  = new Vector3D( 0.76,   0.00,  -0.65);

        int factory_lay[] = {201,202,203,204,301,301,301,301,301,301,301,302,401};
        int type_lay[] = {1, 1, 1, 1, 2, 2, 3, 3, 3, 3, 3, 4, 5};
        String name_lay[] = {"aerogel_2cm_B1" ,"aerogel_2cm_B2", "aerogel_3cm_L1", "aerogel_3cm_L2", 
                           "mirror_front_B1", "mirror_front_B2", "mirror_left_L1", "mirror_left_L2", "mirror_right_L1", "mirror_right_L2", "mirror_bottom", 
                           "mirror_sphere", "mapmts"};
        Vector3D view_lay[] = {front, front, front, front, front, front, left, left, right, right, bottom, sphere, back};

        /*
        * Generate the layers of components
        */
        for (int ilay=0; ilay<NLAY; ilay++){

            int idlayer = factory_lay[ilay];
            int tlayer = type_lay[ilay];
            String slayer = name_lay[ilay];
            Vector3D vlayer = view_lay[ilay];
            if(debugMode>=1){
                System.out.format("-------------------------\n");
                System.out.format("Create Layer %d id  %d: %s dir %s \n",ilay, idlayer, slayer, vlayer.toStringBrief(2));
                System.out.format("-------------------------\n");
            }
            RICHLayer layer = new RICHLayer(ilay, slayer, vlayer);

            //if(iflag==0 && (ilay>3 && ilay<12)) continue;
            if(iflag==1 || (ilay<4 || ilay==12)) {

            for (int ico=0; ico<get_RICHFactory_Size(idlayer); ico++){
                RICHComponent compo = get_RICHFactory_Component(idlayer, ico);
                compo.set_type(tlayer);
                if(debugMode>=1)System.out.format(" Lay %3d component %3d  bary %s\n", idlayer, ico, get_CSGBary(compo.get_CSGVol()));

                // define optical properties, so far only aerogel
                if(idlayer<300){  
                    compo.set_index((float) aero_refi[ilay][ico]);
                    compo.set_planarity((float) aero_plan[ilay][ico]/10);  // converted into cm
                }else{
                    compo.set_index((float) 1.000);
                    compo.set_planarity((float) 0.000);
                }

                if(debugMode>=3 && idlayer==301){
                    if(get_PlaneMirrorSide(compo).equals(slayer)){
                        compo.showComponent();
                        dump_StlComponent(compo.get_CSGVol());
                    }
                }

                // regrouping of the planar mirros into planes
                if(idlayer!=301){
                    layer.add(compo);
                }else{
                    if(get_PlaneMirrorSide(compo).equals(slayer)){
                        if(debugMode>=1)System.out.format(" ---> add to Layer %3d %s id (%3d %3d) \n",ilay,slayer,idlayer,ico);
                        layer.add(compo); 
                    }
                }

            }
            }

            if(debugMode>=1)System.out.format("add layer %d \n",ilay);
            opticlayers.add(layer);

        }


        /*
        * Generate and misalign the basic planes for tracking 
        */

        rich_frame = survey_frame.clone();

        for (int ilay=0; ilay<NLAY; ilay++){

            if(iflag==0 && (ilay>3 && ilay<12)) continue;
            if(debugMode>=1)System.out.format("generate surfaces for layer %d \n",ilay);

            generate_TrackingPlane(ilay);

            if(reco_constants.DO_MISALIGNMENT==1)misalign_TrackingPlane(ilay);

            store_TrackingPlane(ilay);

        }

        
        if(iflag>0){
            /*
            *  Generate Pixel map on the misaligned MAPMT plane
            */
            RICHLayer layer = get_Layer("mapmts");
            List<Integer> compo_list = layer.get_CompoList();
            Shape3D compo_misa = layer.get_TrackingSurf();
            generate_Pixel_Map(layer.get_id(), 0, compo_misa, compo_list);

            if(debugMode>=1)show_Shape3D(compo_misa, null, "CC");
            if(debugMode>=1)show_RICH("Real RICH Geometry", "RR");
        }

    }

    // ----------------
    public void testTraj() {
    // ----------------

        Plane3D pl_mapmt = get_MaPMTforTraj();
        pl_mapmt.show();

        Point3D pa[] = new Point3D[3];
        for (int ia=0; ia<3; ia++){
            Plane3D pl_aero = get_AeroforTraj(ia);
            pl_aero.show();
            pa[ia]=pl_aero.point();
            System.out.format("Ref point %s \n",pa[ia].toStringBrief(2));

        }

        Point3D IP = new Point3D(0.,0.,0.);
        for (int ia=0; ia<3; ia++){
            Line3D lin = new Line3D(IP, pa[ia]);
            int iplane = select_AeroforTraj(lin, lin, lin);
            System.out.format("For LIN %d select plane %d \n",ia,iplane);

        }

    }


    //------------------------------
    public Plane3D get_MaPMTforTraj() {
    //------------------------------

        RICHLayer layer = get_Layer("mapmts");
        return layer.get_TrajPlane();

    }


    //------------------------------
    public Plane3D get_AeroforTraj(int iflag) {
    //------------------------------

        RICHLayer layer = get_Layer("aerogel_2cm_B1");
        if(iflag==1) layer = get_Layer("aerogel_2cm_B2");
        if(iflag==2) layer = get_Layer("aerogel_3cm_L1");

        return layer.get_TrajPlane();

    }


    //------------------------------
    public int select_AeroforTraj(Line3D first, Line3D second, Line3D third) {
    //------------------------------

        RICHIntersection entra = get_Layer("aerogel_2cm_B2").find_Entrance(second, -2);
        if(entra!=null) return 1;

        if(entra==null) entra = get_Layer("aerogel_3cm_L1").find_Entrance(third, -2);
        if(entra!=null) return 2;

        // return a solution plane in any case
        return 0;

    }


    //------------------------------
    public Vector3d GetPixelCenter(int ipmt, int anode){
    //------------------------------

        Vector3d Vertex = richfactory.GetPhotocatode(ipmt).getVertex(2);
        Vector3d VPixel = Vertex.plus(MAPMTpixels.GetPixelCenter(anode));
        //System.out.format("Std  vtx %8.3f %8.3f %8.3f \n",Vertex.x, Vertex.y, Vertex.z);
        return new Vector3d (VPixel.x, -VPixel.y, VPixel.z);

    }


    //------------------------------
    public Vector3d get_Pixel_Center(int ipmt, int anode){
    //------------------------------

        int ilay = 12;
        Face3D compo_face = get_Layer(ilay).get_CompoFace(ipmt-1, 0);
        Vector3d Vertex = toVector3d( compo_face.point(1) );
        //System.out.format("Misa vtx %8.3f %8.3f %8.3f \n",Vertex.x, Vertex.y, Vertex.z);
        //System.out.println(MAPMTpixels.GetPixelCenter(anode));
        Vector3d VPixel = Vertex.plus(MAPMTpixels.GetPixelCenter(anode));
        return new Vector3d (VPixel.x, -VPixel.y, VPixel.z);

    }

    //------------------------------
    public Shape3D build_GlobalPlane(Shape3D plane, Vector3D orient) {
    //------------------------------
        /*
        *  build a global tracking plane from the detailed component surface
        * ATT: assumes a plane (with unique normal) with vertical (along y) edges 
        */

        int debugMode = 0;
        if(plane==null) return null;
        if(debugMode>=1)System.out.format("build_GlobalPlane: orient %s \n",orient.toStringBrief(3));

        Vector3d extre1 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre2 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre3 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre4 = new Vector3d(0.0, 0.0, 0.0);

        /*
        * look for the extremes in x
        */
        double xmin = 999.0;
        double xmax = -999.0;
        for (int ifa=0; ifa<plane.size(); ifa++){
            Face3D f = plane.face(ifa);
            if(toTriangle3D(f).normal().angle(orient)>1.e-2)continue;
            for (int ipo=0; ipo<3; ipo++){

                if(f.point(ipo).x() < xmin) xmin=f.point(ipo).x();
                if(f.point(ipo).x() > xmax) xmax=f.point(ipo).x();
            }
        }  
        if(debugMode>=1)System.out.format("  x range: %7.2f %7.2f \n",xmin,xmax);

        /*
        *  look for the points at exreme y for xmin 
        */
        double ymin = 999.0;
        double ymax = -999.0;
        for (int ifa=0; ifa<plane.size(); ifa++){
            Face3D f = plane.face(ifa);
            if(toTriangle3D(f).normal().angle(orient)>1.e-2)continue;
            for (int ipo=0; ipo<3; ipo++){

                if(Math.abs(f.point(ipo).x() - xmin) < 0.5 && f.point(ipo).y() < ymin ) {
                    ymin = f.point(ipo).y();
                    extre1 = toVector3d(f.point(ipo));
                }
                if(Math.abs(f.point(ipo).x() - xmin) < 0.5 && f.point(ipo).y() > ymax ) {
                    ymax = f.point(ipo).y();
                    extre2 = toVector3d(f.point(ipo));
                }
            }
        }

        /*
        * look for the points at exreme y for xmax 
        */
        ymin = 999.0;
        ymax = -999.0;
        for (int ifa=0; ifa<plane.size(); ifa++){
            Face3D f = plane.face(ifa);
            if(toTriangle3D(f).normal().angle(orient)>1.e-2)continue;
            for (int ipo=0; ipo<3; ipo++){

                if(Math.abs(f.point(ipo).x() - xmax) < 0.5 && f.point(ipo).y() < ymin ) {
                    ymin = f.point(ipo).y();
                    extre3 = toVector3d(f.point(ipo));
                }
                if(Math.abs(f.point(ipo).x() - xmax) < 0.5 && f.point(ipo).y() > ymax ) {
                    ymax = f.point(ipo).y();
                    extre4 = toVector3d(f.point(ipo));
                }
            }
        }

        /*
        *  preserve the same normal of the original plane
        */
        Face3D half1 = new Triangle3D( toPoint3D(extre1), toPoint3D(extre2), toPoint3D(extre3));
        Face3D half2 = new Triangle3D( toPoint3D(extre2), toPoint3D(extre4), toPoint3D(extre3));
        Shape3D guess_one = new Shape3D(half1, half2);

        Face3D half3 = new Triangle3D( toPoint3D(extre3), toPoint3D(extre2), toPoint3D(extre1));
        Face3D half4 = new Triangle3D( toPoint3D(extre3), toPoint3D(extre4), toPoint3D(extre2));
        Shape3D guess_two = new Shape3D(half3, half4);

        Vector3D plane_norm = orient;
        Vector3D guess_norm = toVector3D(get_Shape3D_Normal(guess_one));
        double ang = guess_norm.angle(plane_norm)*RICHConstants.RAD;

        if(debugMode>=1){
            guess_one.show();
            System.out.format("Guess one normal %s --> %7.2f \n",guess_norm.toStringBrief(2), ang*57.3);
            guess_two.show();
            Vector3D other_norm = toVector3D(get_Shape3D_Normal(guess_two));
            double other_ang = other_norm.angle(plane_norm)*RICHConstants.RAD;
            System.out.format("Guess two normal %s --> %7.2f \n",other_norm.toStringBrief(2), other_ang*57.3);
        }

        if(ang<10){
            return guess_one;
        }else{
            return guess_two;
        }

    }


    //------------------------------
    public void build_GlobalPlanes(RICHLayer layer, Vector3D orient) {
    //------------------------------
        //build the tracking plane of the component with given orientation

        int debugMode = 0;


        if(debugMode>=2){
            Vector3D inside = layer.get_Vinside();
            System.out.format("build_GlobalPlane: generate global plane for layer %3d \n",layer.get_id());
            System.out.format("inside vect: %s \n",inside.toStringBrief(3));
            System.out.format("orient vect: %s  --> %7.3f\n",orient.toStringBrief(3), orient.angle(inside)*57.3);
        }

        Shape3D global_surf = null;

        if(layer.is_mirror()){

            if(layer.is_planar_mirror()) global_surf = copy_Shape3D(layer.merge_CompoSurfs());
            if(layer.is_spherical_mirror()) global_surf = copy_Shape3D(layer.get_NominalPlane());

        }else{
            global_surf = build_GlobalPlane(layer.merge_CompoSurfs(), orient);

            if(layer.is_aerogel()){
                Shape3D other_global = build_GlobalPlane(layer.merge_CompoSurfs(), orient.multiply(-1.0));
                merge_Shape3D(global_surf, other_global);
            }
        }

        layer.set_GlobalSurf( global_surf);
        if(debugMode>=1 && global_surf.size()>0){
            String head = String.format("GLOB %3d 0 ",layer.get_id());
            System.out.format("Globa %3d Normal %s \n",layer.get_id(),toString(get_Shape3D_Normal(global_surf)));
            for (int ifa=0; ifa<global_surf.size(); ifa++){
                System.out.format("Face %3d Normal %s \n",ifa,toTriangle3D(global_surf.face(ifa)).normal().asUnit().toStringBrief(3));
            }
            show_Shape3D(global_surf, null, head);
        }
 
    }


    //------------------------------
    public void build_CompoSpheres(RICHLayer layer) {
    //------------------------------
        //build the spherical surface of the component 

        int debugMode = 0;

        /*
        *   define the spherical surfaces when needed
        */
        if(layer.is_spherical_mirror()){

            Sphere3D sphere = new Sphere3D(-45.868, 0.0, 391.977, 270.);
            layer.set_TrackingSphere(sphere);
            for (int ico=0; ico<layer.size(); ico++){ 
                layer.set_TrackingSphere( new Sphere3D(-45.868, 0.0, 391.977, 270.), ico); 
            }

        }

        if(layer.is_aerogel()){

            for (int ico=0; ico<layer.size(); ico++){

                double radius = layer.get(ico).get_radius();
                Vector3D normal = layer.get_CompoNormal(ico);
                Vector3D center = layer.get_CompoCenter(ico, normal);

                Sphere3D sphere = new Sphere3D(center.x(), center.y(), center.z(), radius);
                if(debugMode>=1)System.out.format(" AERO lay %3d ico %3d : sphere center %s radius %7.2f \n",layer.get_id(),ico,toString(center), radius);
                layer.set_TrackingSphere(sphere, ico);

            }
        }

    }


    //------------------------------
    public void build_CompoSurfs(RICHLayer layer, Vector3D orient) {
    //------------------------------
        //build the tracking plane of the component with given orientation

        int debugMode = 0;

        Vector3D inside = layer.get_Vinside();

        if(debugMode>=2){
            System.out.format("build_CompoSurfs: generate tracking plane for layer %3d \n",layer.get_id());
            System.out.format("inside vect: %s \n",inside.toStringBrief(3));
            System.out.format("orient vect: %s  --> %7.3f\n",orient.toStringBrief(3), orient.angle(inside)*57.3);
        }

        for (int ico=0; ico<layer.size(); ico++){
            RICHComponent compo = layer.get(ico);
            Shape3D plane = new Shape3D();
            Vector3D cbary = layer.get_CompoCSGBary(ico);

            if(layer.is_spherical_mirror()){

                /*
                * Build from Nominal planes (nominal orientation)
                */
                Shape3D submir = generate_Nominal_Plane(layer.get_id(), ico+1);
                for(int ifa=0; ifa<submir.size(); ifa++) plane.addFace(submir.face(ifa)); 
                     
            }else{

                /*
                * Build from CSG volumes
                */
                int ipo = 0;
                int igo = 0;
                for (Triangle3D tri: toTriangle3D(compo.get_CSGVol().getPolygons()) ){

                    Vector3D tri_norm = tri.normal().asUnit();
                    double norm_ang = tri_norm.angle(orient);
                    double norm_oppang = tri_norm.angle(orient.multiply(-1));
                    Vector3D bary_diff = (tri.center().toVector3D().sub(cbary)).asUnit();
                    double bary_dot = tri_norm.dot(bary_diff);
                    if(debugMode>=2){System.out.format("Compo %4d tri %4d  norm_ang %7.2f : %7.2f bary_dot %7.2f (%s %s)", ico, ipo, 
                                     norm_ang*57.3, norm_oppang*57.3, bary_dot*57.3, toString(tri.center()), toString(cbary));}
                    
                    /*
                    * in case of multiple surfaces (i.e. for glass skin mirrors), take the innermost.
                    */
                    if((norm_ang<1e-2 && bary_dot>0) || (layer.is_aerogel() && norm_oppang<1e-2 && bary_dot>0) ){  

                        plane.addFace(tri); 
                        if(debugMode>=2)System.out.format("    ---> take this face %3d %s\n",igo,tri_norm.toStringBrief(2));
                        igo++;
                    }else{
                        if(debugMode>=2)System.out.format("  \n");
                    }
                    ipo++;
                }
            }

            compo.set_TrackingSurf(plane);
            if(debugMode>=1 && plane.size()>0){
                System.out.format("Compo %3d %3d Normal %s \n",layer.get_id(),ico,toString(get_Shape3D_Normal(plane)));
                String head = String.format("COMP %3d %3d ",layer.get_id(),ico);
                show_Shape3D(plane, null, head);
            }
        }

    }
 
     //------------------------------
     public Vector3d get_angles(Vector3d vec) {
     //------------------------------

        Vector3d vone = vec.normalized();
        Vector3d vang = new Vector3d( Math.acos(vone.dot(Vector3d.X_ONE)), Math.acos(vone.dot(Vector3d.Y_ONE)), Math.acos(vone.dot(Vector3d.Z_ONE)));
        return vang;

     }

    //------------------------------
    public String toString(Vector3d vec, int qua) {
    //------------------------------
        if(qua==2)return String.format("%8.2f %8.2f %8.2f", vec.x, vec.y, vec.z);
        if(qua==3)return String.format("%8.3f %8.3f %8.3f", vec.x, vec.y, vec.z);
        if(qua==4)return String.format("%8.4f %8.4f %8.4f", vec.x, vec.y, vec.z);
        return String.format("%8.1f %8.1f %8.1f", vec.x, vec.y, vec.z);
 
    }

    //------------------------------
    public String toString(Vector3d vec) {
    //------------------------------
        return String.format("%8.3f %8.3f %8.3f", vec.x, vec.y, vec.z);
    }


    //------------------------------
    public String toString(Vector3D vec) {
    //------------------------------
        return String.format("%8.3f %8.3f %8.3f", vec.x(), vec.y(), vec.z());
    }

    //------------------------------
    public String toString(Point3D vec) {
    //------------------------------
        return String.format("%7.2f %7.2f %7.2f", vec.x(), vec.y(), vec.z());
    }


    //------------------------------
    public Triangle3D toTriangle3D(Face3D face){ return new Triangle3D(face.point(0), face.point(1), face.point(2)); }
    //------------------------------

    //------------------------------
    public ArrayList<Triangle3D> toTriangle3D(List<Polygon> pols){
    //------------------------------

        ArrayList<Triangle3D> trias = new ArrayList<Triangle3D>();

        for (Polygon pol: pols){
            for (int iv=2; iv<pol.vertices.size(); iv++){
                Triangle3D tri = new Triangle3D(toPoint3D(pol.vertices.get(0)), toPoint3D(pol.vertices.get(iv-1)), toPoint3D(pol.vertices.get(iv)));
                trias.add(tri);
            }
        }
        
        return trias;
    }

    //------------------------------
    public void misalign_Layer(RICHLayer layer){
    //------------------------------

        int debugMode = 0;

        /*
        *  To account for SURVEY
        */
        if(reco_constants.APPLY_SURVEY==1){
            if(debugMode>=1)System.out.format(" --> SURVEY %s %s \n", toString(rich_survey_shift), toString(rich_survey_angle));

            misalign_Element( layer.get_GlobalSurf(), survey_frame, rich_survey_angle, rich_survey_shift);
            misalign_Element( layer.get_TrackingSphere(), survey_frame, rich_survey_angle, rich_survey_shift);
            for(int ico=0; ico<layer.size(); ico++){
                misalign_Element( layer.get_TrackingSurf(ico), survey_frame, rich_survey_angle, rich_survey_shift);
                misalign_Element( layer.get_TrackingSphere(ico), survey_frame, rich_survey_angle, rich_survey_shift);
            }
        }


        /*
        *  To account for global RICH misalignments
        */
        Vector3D rshift = layer_misa_shift[0][0];
        Vector3D rangle = layer_misa_angle[0][0];
        if(rangle.mag()>0 || rshift.mag()>0){
            if(debugMode>=1)System.out.format(" -->  asRICH %s %s \n", toString(rshift), toString(rangle));

            
            if(debugMode>=1)System.out.format("     --> global \n");
            misalign_Element( layer.get_GlobalSurf(), rich_frame, rangle, rshift);
            misalign_Element( layer.get_TrackingSphere(), rich_frame, rangle, rshift);

            for(int ico=0; ico<layer.size(); ico++){
                if(debugMode>=1)System.out.format("     --> compo %3d \n",ico);
                misalign_Element( layer.get_TrackingSurf(ico), rich_frame, rangle, rshift);
                misalign_Element( layer.get_TrackingSphere(ico), rich_frame, rangle, rshift);
            }
        }


        /*
        *  To account for Layer misalignment 
        */
        int ilay = layer.get_id();
        RICHFrame lframe = layer.generate_LocalRef();
        Vector3D lshift = layer_misa_shift[ilay+1][0];
        Vector3D langle = layer_misa_angle[ilay+1][0];
        if(langle.mag()>0 || lshift.mag()>0){
            if(debugMode>=1){System.out.format("    -->  asLayer  %d  %s %s \n", ilay, toString(lshift), toString(langle)); }

            if(debugMode>=1)System.out.format("     --> global \n");
            misalign_Element( layer.get_GlobalSurf(), lframe, langle, lshift);
            misalign_Element( layer.get_TrackingSphere(), lframe, langle, lshift);

            for(int ico=0; ico<layer.size(); ico++){
                if(debugMode>=1)System.out.format("     --> compo %3d \n",ico);
                misalign_Element( layer.get_TrackingSurf(ico), lframe, langle, lshift);
                misalign_Element( layer.get_TrackingSphere(ico), lframe, langle, lshift);
            }
        }


        /*
        *  To account for single Component misalignment 
        */
        if(layer.is_spherical_mirror()){
            for(int ico=0; ico<layer.size(); ico++){

                RICHFrame cframe = layer.generate_LocalRef(ico);
                Vector3D cangle = layer_misa_angle[ilay+1][ico+1];
                Vector3D cshift = layer_misa_shift[ilay+1][ico+1];

                if(cangle.mag()==0 && cshift.mag()==0)continue;
                if(debugMode==1){System.out.format("       -->  asCompo %3d  %s %s \n", ico, toString(cshift), toString(cangle));}

                misalign_Element( layer.get_TrackingSurf(ico), cframe, cangle, cshift);
                misalign_Element( layer.get_TrackingSphere(ico), cframe, cangle, cshift);
                if(!layer.CheckSphere(ico))System.out.format("Misalignment issue for lay %3d compo %3d \n",ilay,ico);
            }
        }
    }


    //------------------------------
    public void generate_TrackingPlane(int ilay){
    //------------------------------

        int debugMode = 0;

        RICHLayer layer = get_Layer(ilay);
        Vector3D orient = layer.get_Vinside();

        if(debugMode>=1){
            System.out.format("------------------------\n");
            System.out.format("Generate tracking for Layer %d %s view %s \n", ilay, layer.get_Name(), orient.toStringBrief(3));
            System.out.format("------------------------\n");
        }

        /*
        *  Nominal plane just for reference 
        */
        layer.set_NominalPlane( generate_Nominal_Plane(ilay, 0) );


        /*
        *  For each component, group faces with normal and position vs barycenter along orient
        */
        build_CompoSurfs(layer, orient);
        

        /*
        *  Generate a global plane for fast tracking without gaps
        *  In case of aerogel add the second global face 
        */
        build_GlobalPlanes(layer, orient);


        /*
        *  Select the pivot for the RICH rotations
        */
        if(layer.is_mapmt()) {
            if(reco_constants.MISA_PMT_PIVOT==1) rich_frame.set_bref(layer.get_SurfBary());
            if(debugMode>=1)System.out.format("RICH PIVOT %s \n",rich_frame.bref().toStringBrief(2));
        }


        /*
        *   define the spherical surfaces when needed
        */
        build_CompoSpheres(layer);

    }

    //------------------------------
    public void misalign_TrackingPlane(int ilay){
    //------------------------------

        int debugMode = 0;

        /*
        *  Apply misalignment around given PIVOT
        */

        if(debugMode>=1){
            System.out.format("------------------------\n");
            System.out.format("Misalign tracking for Layer %d %s\n", ilay, get_Layer(ilay).get_Name());
            System.out.format("------------------------\n");
        }

        RICHLayer layer = get_Layer(ilay);

        /*
        *  Misalign surfs as required
        */
        misalign_Layer(layer);

        /*
        *  Check misalignment effect on survey plane
        *//*
        if(debugMode>=1){
            System.out.format("Centre %s\n",rich_misa_center.toStringBrief(2));
            double mrad = RICHConstants.MRAD;
            System.out.format(" rich   angle %7.2f %7.2f %7.2f \n", this.rich_misa_angle.x*mrad, this.rich_misa_angle.y*mrad, this.rich_misa_angle.z*mrad);
            System.out.format(" rich   shift %7.2f %7.2f %7.2f \n", this.rich_misa_shift.x, this.rich_misa_shift.y, this.rich_misa_shift.z);
            show_Shape3D(rich_survey_plane,"Nominal survey", null);
            //misalign_TrackingPlane(rich_survey_plane, 0);
            show_Shape3D(rich_survey_plane,"Misalig survey", null);
        }*/

    }


    //------------------------------
    public void store_TrackingPlane(int ilay){
    //------------------------------

        int debugMode = 0;

        /*
        *  Store the composite tracking planes
        */

        if(debugMode>=1){
            System.out.format("------------------------\n");
            System.out.format("Store    tracking for Layer %d %s\n", ilay, get_Layer(ilay).get_Name());
            System.out.format("------------------------\n");
        }

        RICHLayer layer = get_Layer(ilay);

        /* 
        *  Store misalignmed tracking surfaces for fast tracking 
        */
        layer.set_TrackingSurf( layer.merge_CompoSurfs());
        layer.set_CompoList( layer.merge_CompoList());
           
    }
 
    //------------------------------
    public void generate_Pixel_Map(int ilay, int ico, Shape3D compo_plane, List<Integer> compo_list) {
    //------------------------------

        int debugMode = 0;

        RICHLayer layer = get_Layer(ilay);
        if(layer.is_mapmt()){

            if(debugMode>=1){
                System.out.format("------------------------\n");
                System.out.format("Generate pixel map for Layer %d %s\n", ilay, get_Layer(ilay).get_Name());
                System.out.format("------------------------\n");
            }

            int found=0;
            Vector3d downversor   = null;
            Vector3d rightversor  = null;
            Vector3d vertex       = null;
            for(int ifa=0; ifa<compo_plane.size(); ifa++){
                if(compo_list.get(ifa)==ico){
                    if(debugMode>=1){ System.out.format("  --> ifa %4d ", ifa); dump_Face( compo_plane.face(ifa) ); }
                    if(found==0){
                        Vector3d vp0 = toVector3d( compo_plane.face(ifa).point(0) );
                        Vector3d vp1 = toVector3d( compo_plane.face(ifa).point(1) );
                        Vector3d vp2 = toVector3d( compo_plane.face(ifa).point(2) );
                        downversor   = (vp0.minus(vp1)).normalized();
                        rightversor  = (vp2.minus(vp1).normalized());
                        vertex       = new Vector3d(vp1);
                        if(debugMode>=1){
                            System.out.format("MAPMT ico %4d  ifa %4d \n",ico, ifa); 
                            System.out.format("vtx0  %s \n",toString(vp0));  
                            System.out.format("vtx1  %s \n",toString(vp1));  
                            System.out.format("vtx2  %s \n",toString(vp2));  
                            System.out.format("down  %s \n",toString(downversor));  
                            System.out.format("right %s \n",toString(rightversor));  
                        }
                        found++;
                    }
                }
            }

            if(downversor!=null && rightversor!= null) {
                MAPMTpixels = new RICHPixel(new Vector3d(0.,0.,0.), downversor, rightversor);
                if(debugMode>=1){
                    MAPMTpixels.show_Pixels( vertex );
                    vertex = toVector3d( layer.get_CompoFace(5,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                    vertex = toVector3d( layer.get_CompoFace(363,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                    vertex = toVector3d( layer.get_CompoFace(390,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                }
            }
        }
    }

    //------------------------------
    public Shape3D generate_Nominal_Plane(int ilay, int ico){
    //------------------------------

        int debugMode = 0;

        if(ilay<0 || ilay>=NLAY) return null;

        Point3D extre1 = new Point3D(0.0, 0.0, 0.0);
        Point3D extre2 = new Point3D(0.0, 0.0, 0.0);
        Point3D extre3 = new Point3D(0.0, 0.0, 0.0);
        Point3D extre4 = new Point3D(0.0, 0.0, 0.0);

        //  Aerogel 2cm plane within B1 mirror 
        if(ilay==0 && ico==0){
            extre1 = new Point3D(-41.1598, 11.5495, 588.125);
            extre2 = new Point3D(-41.1598, -11.5495, 588.125);
            extre3 = new Point3D(-110.583, 51.6008, 555.752);
            extre4 = new Point3D(-110.583, -51.6008, 555.752);
        }

        //  Aerogel 2cm plane within B2 mirror
        if(ilay==1 && ico==0){
            extre1 = new Point3D(-111.353, 53.6256, 555.393);
            extre2 = new Point3D(-111.353, -53.6256, 555.393);
            extre3 = new Point3D(-165.777, 85.0457, 530.015);
            extre4 = new Point3D(-165.777, -85.0457, 530.015);
        }

        //  Aerogel 6cm plane within CFRP panel
        if((ilay==2 || ilay==3) && ico==0){
            extre1 = new Point3D(-167.565, 85.356, 530.003);
            extre2 = new Point3D(-167.565, -85.356, 530.003);
            extre3 = new Point3D(-240.137, 127.254, 496.162);
            extre4 = new Point3D(-240.137, -127.254, 496.162);
        }

        // Front mirror
        if((ilay==4 || ilay==5) && ico==0){
            extre1 = new Point3D(-37.165,  11.847,  587.781);
            extre2 = new Point3D(-37.165,  -11.847,  587.781);
            extre3 = new Point3D(-165.136,  85.728,  528.107);
            extre4 = new Point3D(-165.136,  -85.728,  528.107);
        }

        // Left-side mirror
        if((ilay==6 || ilay==7) && ico==0){
            extre1 = new Point3D(-39.849,  12.095,  688.630);
            extre2 = new Point3D(-39.849,  12.095,  591.568);
            extre3 = new Point3D(-238.031, 126.515,  526.116);
            extre4 = new Point3D(-229.924, 121.834,  502.935);
        }

        // Right-side mirror
        if((ilay==8 || ilay==9) && ico==0){
            extre1 = new Point3D(-39.849,  -12.095,  688.630);
            extre2 = new Point3D(-39.849,  -12.095,  591.568);
            extre3 = new Point3D(-238.031, -126.515,  526.116);
            extre4 = new Point3D(-229.924, -121.834,  502.935);
        }

        // Bottom mirror
        if(ilay==10 && ico==0){
            extre1 = new Point3D(-39.763,  11.500,  591.601);
            extre2 = new Point3D(-39.763,  -11.500,  591.601);
            extre3 = new Point3D(-39.763,  11.500,  687.101);
            extre4 = new Point3D(-39.763,  -11.500,  687.101);
        }

        //  Spherical mirror
        if(ilay==11){
            if(ico==0){
                extre1 = new Point3D(-146.861, 77.9926, 629.86);
                extre2 = new Point3D(-146.861, -77.9926, 629.86);
                extre3 = new Point3D(-244.481, 134.353, 516.032);
                extre4 = new Point3D(-244.481, -134.353, 516.032);
            }
            if(ico==1){
                extre1 = new Point3D(-186.669, 100.976, 598.990);
                extre2 = new Point3D(-195.823,  42.177, 612.446);
                extre3 = new Point3D(-146.869,  77.996, 629.870);
                extre4 = new Point3D(-150.160,  40.583, 637.633);
            }
            if(ico==2){
                extre1 = new Point3D(-186.670,-100.975, 598.991);
                extre2 = new Point3D(-195.760, -42.186, 612.450);
                extre3 = new Point3D(-146.862, -77.995, 629.860);
                extre4 = new Point3D(-150.160, -40.592, 637.625);
            }
            if(ico==3){
                extre1 = new Point3D(-244.480, 134.356, 516.045);
                extre2 = new Point3D(-219.293, 119.805, 560.660);
                extre3 = new Point3D(-267.718,  66.825, 530.562);
                extre4 = new Point3D(-232.817,  69.694, 573.835);
            }
            if(ico==4){
                extre1 = new Point3D(-244.475,-134.350, 516.040);
                extre2 = new Point3D(-219.291,-119.808, 560.655);
                extre3 = new Point3D(-267.713, -66.825, 530.556);
                extre4 = new Point3D(-232.817, -69.699, 573.836);
            }
            if(ico==5){
                extre1 = new Point3D(-150.187, -40.275, 637.670);
                extre2 = new Point3D(-195.858, -41.878, 612.487);
                extre3 = new Point3D(-150.187,  40.268, 637.668);
                extre4 = new Point3D(-195.842,  41.844, 612.500);
            }
            if(ico==6){
                extre1 = new Point3D(-239.371,   0.150, 580.221);
                extre2 = new Point3D(-274.840,   0.150, 535.006);
                extre3 = new Point3D(-232.873,  69.405, 573.896);
                extre4 = new Point3D(-267.781,  66.526, 530.596);
            }
            if(ico==7){
                extre1 = new Point3D(-239.371,  -0.150, 580.221);
                extre2 = new Point3D(-274.840,  -0.150, 535.010);
                extre3 = new Point3D(-232.873, -69.404, 573.889);
                extre4 = new Point3D(-267.782, -66.530, 530.594);
            }
            if(ico==8){
                extre1 = new Point3D(-236.779,  42.186, 578.135);
                extre2 = new Point3D(-196.078,  42.180, 612.277);
                extre3 = new Point3D(-219.115, 119.693, 560.915);
                extre4 = new Point3D(-186.889, 101.102, 598.779);
            }
            if(ico==9){
                extre1 = new Point3D(-236.810,  41.877, 578.175);
                extre2 = new Point3D(-196.108,  41.835, 612.303);
                extre3 = new Point3D(-236.818, -41.883, 578.157);
                extre4 = new Point3D(-196.105, -41.883, 612.315);
            }
            if(ico==10){
                extre1 = new Point3D(-236.785, -42.182, 578.124);
                extre2 = new Point3D(-196.080, -42.185, 612.272);
                extre3 = new Point3D(-219.114,-119.708, 560.905);
                extre4 = new Point3D(-186.891,-101.097, 598.779);
            }
        }

        // MA-PMTs
        if(ilay==12  && ico==0){
            extre1 = new Point3D(-41.126,  15.850,  694.974);
            extre2 = new Point3D(-41.126,  -15.850,  694.974);
            extre3 = new Point3D(-151.514,  74.150,  643.499);
            extre4 = new Point3D(-151.514,  -74.150,  643.499);
        }

        /*
        *  force the layer orientation 
        */
        Vector3D vinside = get_Layer(ilay).get_Vinside();

        Triangle3D half1 = new Triangle3D( extre1, extre2, extre3);
        Triangle3D half2 = new Triangle3D( extre2, extre4, extre3);
        Vector3D norm1 = half1.normal().asUnit();
        Vector3D norm2 = half2.normal().asUnit();
        Shape3D guess_one = new Shape3D(half1, half2);
        Vector3D norm_one = half1.normal().asUnit();
        double ang_one = norm_one.angle(vinside)*RICHConstants.RAD;

        Triangle3D half3 = new Triangle3D( extre3, extre2, extre1);
        Triangle3D half4 = new Triangle3D( extre3, extre4, extre2);
        Vector3D norm3 = half3.normal().asUnit();
        Vector3D norm4 = half4.normal().asUnit();
        Shape3D guess_two = new Shape3D(half3, half4);
        Vector3D norm_two = half3.normal().asUnit();
        double ang_two = norm_two.angle(vinside)*RICHConstants.RAD;

        if(debugMode>=1){
            System.out.format("Look for Nominal plane %3d ico %3d\n",ilay,ico);
            System.out.format("norm1 %s \n",norm1.toStringBrief(3));
            System.out.format("norm2 %s \n",norm2.toStringBrief(3));
            System.out.format("norm3 %s \n",norm3.toStringBrief(3));
            System.out.format("norm4 %s \n",norm4.toStringBrief(3));
            guess_one.show();
            System.out.format("Guess one normal %s --> %7.2f \n",norm_one.toStringBrief(3), ang_one);
            guess_two.show();
            System.out.format("Guess two normal %s --> %7.2f \n",norm_two.toStringBrief(3), ang_two);
        }

        if(ang_one<30){
            if(debugMode>=1)System.out.format(" --> guess one\n");
            return guess_one;
        }else{
            if(debugMode>=1)System.out.format(" --> guess two\n");
            return guess_two;
        }

    }


    //------------------------------
    public double get_aerorefi(int ila, int ico){
    //------------------------------
 
        return aero_refi[ila][ico];

    }


    //------------------------------
    public double get_sChElectron(int ila, int ico, int iqua, int irefle) {
    //------------------------------

        if(get_Constants().USE_ELECTRON_ANGLES==1){
            if(irefle==0){
                if(aero_schele_dir[ila][ico][iqua]>0){ 
                    return aero_schele_dir[ila][ico][iqua];
                }else{
                    if(aero_schele_lat[ila][ico][iqua]>0){ 
                        return aero_schele_lat[ila][ico][iqua];
                    }else{
                        return aero_schele_spe[ila][ico][iqua];
                    }
                }
            }
            if(irefle==1){
                if(aero_schele_lat[ila][ico][iqua]>0){ 
                    return aero_schele_lat[ila][ico][iqua];
                }else{
                    if(aero_schele_dir[ila][ico][iqua]>0){ 
                        return aero_schele_dir[ila][ico][iqua];
                    }else{
                        return aero_schele_spe[ila][ico][iqua];
                    }
                }
            }
            if(irefle==2){
                if(aero_schele_spe[ila][ico][iqua]>0){ 
                    return aero_schele_spe[ila][ico][iqua];
                }else{
                    if(aero_schele_dir[ila][ico][iqua]>0){ 
                        return aero_schele_dir[ila][ico][iqua];
                    }else{
                        return aero_schele_lat[ila][ico][iqua];
                    }
                }
            }
        }
        return 0.0;
    }
 

    //------------------------------
    public double get_PixelGain(int ipmt, int ich) { return pixel_gain[ipmt][ich]; }
    //------------------------------

    //------------------------------
    public double get_PixelEff(int ipmt, int ich) { return pixel_eff[ipmt][ich]; }
    //------------------------------

    //------------------------------
    public int get_PixelFlag(int ipmt, int ich) { return pixel_flag[ipmt][ich]; }
    //------------------------------

    //------------------------------
    public double get_PixelMtime(int ipmt, int ich) { return pixel_mtime[ipmt][ich]; }
    //------------------------------

    //------------------------------
    public double get_PixelStime(int ipmt, int ich) { return pixel_stime[ipmt][ich]; }
    //------------------------------

    //------------------------------
    public double get_ChElectron(int ila, int ico, int iqua, int irefle) {
    //------------------------------
 
        if(get_Constants().USE_ELECTRON_ANGLES==1){
            if(irefle==0){
                if(aero_chele_dir[ila][ico][iqua]>0){ 
                    return aero_chele_dir[ila][ico][iqua];
                }else{
                    if(aero_chele_lat[ila][ico][iqua]>0){ 
                        return aero_chele_lat[ila][ico][iqua];
                    }else{
                        return aero_chele_spe[ila][ico][iqua];
                    }
                }
            }
            if(irefle==1){
                if(aero_chele_lat[ila][ico][iqua]>0){ 
                    return aero_chele_lat[ila][ico][iqua];
                }else{
                    if(aero_chele_dir[ila][ico][iqua]>0){ 
                        return aero_chele_dir[ila][ico][iqua];
                    }else{
                        return aero_chele_spe[ila][ico][iqua];
                    }
                }
            }
            if(irefle==2){
                if(aero_chele_spe[ila][ico][iqua]>0){ 
                    return aero_chele_spe[ila][ico][iqua];
                }else{
                    if(aero_chele_dir[ila][ico][iqua]>0){ 
                        return aero_chele_dir[ila][ico][iqua];
                    }else{
                        return aero_chele_lat[ila][ico][iqua];
                    }
                }
            }
        }
        return 0.0;

    }


    //------------------------------
    public double getPMTtimeoff(int ipmt, int ich){
    //------------------------------
 
        return -1*pmt_timeoff[ipmt-1][ich-1];

    }

    //------------------------------
    public double getPMTtimewalk(int ipmt, int ich){
    //------------------------------
 
        return pmt_timewalk[ipmt-1][ich-1];

    }


    //------------------------------
    public void init_GlobalPixelGeo(){
    //------------------------------

        int debugMode = 0;
        for(int irow=0; irow<NROW; irow++){ // loop on rows

            for(int ipmt=pfirst[irow];ipmt<=plast[irow];ipmt++){ // loop on pmts

                // pixel 1 coordinate
		if(irow<23){
	           nyp[ipmt-1]=16+irow*8;
		   nxp[ipmt-1]=15+(28+(plast[irow]-pfirst[irow]+1)-(ipmt-pfirst[irow])*2)*4; 
		}else{
                   int yoff = (int) (23*8+(32-20*25.4)/6.5);
	           nyp[ipmt-1]=16+yoff-(irow-24)*8;
		   nxp[ipmt-1]=15+(28-(plast[irow]-pfirst[irow]+1)+(ipmt-pfirst[irow])*2)*4+8; 
		}

		if(debugMode>=1) System.out.println("PMT "+ipmt+" Nx "+nxp[ipmt-1]+" Ny "+nyp[ipmt-1]);

	    }
	}

     }


    //------------------------------
    public int get_LayerNumber(String slay){
    //------------------------------
        int debugMode = 0;
        for (int ila=0; ila<opticlayers.size(); ila++){
            if(opticlayers.get(ila).get_Name().equals(slay)) {
                if(debugMode>=1)System.out.format(" Find layer %s --> %4d \n",slay,ila);
                return ila;
            }
        }  
        return -1;
    }


    //------------------------------
    public RICHLayer get_Layer(String slay){
    //------------------------------
        for (int ila=0; ila<opticlayers.size(); ila++){
            if(opticlayers.get(ila).get_Name().equals(slay)) {
                return opticlayers.get(ila);
            }
        }  
        return null;
    }


    //------------------------------
    public RICHLayer get_Layer(int ilay){ 
    //------------------------------
        if(ilay>-1 && ilay<NLAY) return opticlayers.get(ilay);
        return null; 
    }


    //------------------------------
    public RICHComponent get_RICHFactory_Component(int idlay, int ico){ 
    //------------------------------
        if(idlay==401) return new RICHComponent(ico, idlay, 1, richfactory.GetPhotocatode(ico+1));
        if(idlay==201 || idlay==202 || idlay==203 || idlay==204 || idlay==301 || idlay==302) 
                  return new RICHComponent(ico, idlay, 1, richfactory.getStlComponent(idlay, ico));

        return null;
    }


    //------------------------------
    public int get_RICHFactory_Size(int idlay){ 
    //------------------------------
        if(idlay==401) return 391;
        if(idlay==201 || idlay==202 || idlay==203 || idlay==204 || idlay==301 || idlay==302) 
                  {return richfactory.getStlNumber(idlay);}

        return 0;
    }

    //------------------------------
    public RICHComponent get_Component(int ilay, int ico){ 
    //------------------------------
        return opticlayers.get(ilay).get(ico);
    }


    //------------------------------
    public CSG get_CSGVolume(int ilay, int ico){
    //------------------------------
        return opticlayers.get(ilay).get(ico).get_CSGVol();
    }

     //------------------------------
     public ArrayList<CSG> get_CSGLayerVolumes(int ilay){
     //------------------------------
        ArrayList<CSG> vols = new ArrayList<CSG>();
        RICHLayer layer = opticlayers.get(ilay);
        for (int ico=0; ico<layer.size(); ico++){
            CSG vol = get_CSGVolume(ilay, ico);
            if(vol!=null)vols.add(vol);
        }  
        return vols;
     }

     //------------------------------
     public G4Stl get_StlVolume(int ilay, int ico){
     //------------------------------
        RICHComponent compo = opticlayers.get(ilay).get(ico);
        if(compo.get_voltype()==2) return compo.get_StlVol();
        return null;
     }

     //------------------------------
     public ArrayList<G4Stl> get_StlLayerVolumes(int ilay){
     //------------------------------
        ArrayList<G4Stl> vols = new ArrayList<G4Stl>();
        RICHLayer layer = opticlayers.get(ilay);
        for (int ico=0; ico<layer.size(); ico++){
            G4Stl vol = get_StlVolume(ilay, ico);
            if(vol!=null)vols.add(vol);
        }  
        return vols;
     }

     //------------------------------
     public G4Box get_BoxVolume(int ilay, int ico){
     //------------------------------
        RICHComponent compo = opticlayers.get(ilay).get(ico);
        if(compo.get_voltype()==1) return compo.get_BoxVol();
        return null;
     }

     //------------------------------
     public ArrayList<G4Box> get_BoxLayerVolumes(int ilay){
     //------------------------------
        ArrayList<G4Box> vols = new ArrayList<G4Box>();
        RICHLayer layer = opticlayers.get(ilay);
        for (int ico=0; ico<layer.size(); ico++){
            G4Box vol = get_BoxVolume(ilay, ico);
            if(vol!=null)vols.add(vol);
        }  
        return vols;
     }

     //------------------------------
     public Vector3D toVector3D(Vector3d vin) {
     //------------------------------
        Vector3D vout = new Vector3D(vin.x, vin.y, vin.z); 
	return vout;
     }

     //------------------------------
     public Vector3D toVector3D(Point3D pin) {
     //------------------------------
        Vector3D vout = new Vector3D(pin.x(), pin.y(), pin.z()); 
	return vout;
     }

     //------------------------------
     public Vector3d toVector3d(Vertex ver) {return  new Vector3d(ver.pos.x, ver.pos.y, ver.pos.z); }
     //------------------------------

     //------------------------------
     public Vector3d toVector3d(Vector3D vin) {
     //------------------------------
        Vector3d vout = new Vector3d(vin.x(), vin.y(), vin.z()); 
	return vout;
     }

     //------------------------------
     public Vector3d toVector3d(Point3D pin) {
     //------------------------------
        Vector3d vout = new Vector3d(pin.x(), pin.y(), pin.z()); 
	return vout;
     }

     //------------------------------
     public Point3D toPoint3D(Vertex vin) {
     //------------------------------
        Point3D pout = new Point3D(vin.pos.x, vin.pos.y, vin.pos.z); 
	return pout;
     }

     //------------------------------
     public Point3D toPoint3D(Vector3D vin) {
     //------------------------------
        Point3D pout = new Point3D(vin.x(), vin.y(), vin.z()); 
	return pout;
     }


     //------------------------------
     public Point3D toPoint3D(Vector3d vin) {
     //------------------------------
        if(vin==null) return null;
        Point3D pout = new Point3D(vin.x, vin.y, vin.z); 
	return pout;
     }


     //------------------------------
     public Line3d toLine3d(Line3D lin) {
     //------------------------------
        Line3d lout = new Line3d(toVector3d(lin.origin()), toVector3d(lin.end()));
	return lout;
     }


     //------------------------------
     public Line3D toLine3D(Line3d lin) {
     //------------------------------
        Line3D lout = new Line3D(toPoint3D(lin.origin()), toPoint3D(lin.end()));
        return lout;
     }

     //------------------------------
     public Line3D toLine3D(RICHRay ray) {
     //------------------------------
        Line3D lout = new Line3D(ray.origin(), ray.end());
        return lout;
     }


    //------------------------------
    public void translate_Triangle3D(Triangle3D tri, Vector3d shift) {
    //------------------------------

        tri.translateXYZ(shift.x, shift.y, shift.z);

    }

    //------------------------------
    public void translate_Sphere3D(Sphere3D sphere, Vector3D shift) { sphere.translateXYZ(shift.x(), shift.y(), shift.z()); }
    //------------------------------

    //------------------------------
    public void translate_Shape3D(Shape3D shape, Vector3D shift) { shape.translateXYZ(shift.x(), shift.y(), shift.z()); }
    //------------------------------

    //------------------------------
    public void translate_Sphere3D(Sphere3D sphere, Vector3d shift) { sphere.translateXYZ(shift.x, shift.y, shift.z); }
    //------------------------------

    //------------------------------
    public void translate_Shape3D(Shape3D shape, Vector3d shift) { shape.translateXYZ(shift.x, shift.y, shift.z); }
    //------------------------------

    //------------------------------
    public void rotate_Triangle3D(Triangle3D tri, Vector3d angle) {
    //------------------------------

        Vector3d bary = get_Triangle3D_Bary(tri);

        tri.rotateZ(angle.z);
        tri.rotateY(angle.y);
        tri.rotateX(angle.x);

        Vector3d shift = bary.minus( get_Triangle3D_Bary(tri) );
        translate_Triangle3D(tri, shift);

    }


    // ----------------
    public void show_RICH(String name, String head){
    // ----------------

        System.out.format(" -----------------------\n  %s \n ----------------------- \n", name);

        for (int ilay=0; ilay<NLAY; ilay++){
            String ini = head + " "+ ilay;
            RICHLayer layer = get_Layer(ilay);
            if(layer.is_aerogel() || layer.is_mapmt()){
                show_Shape3D(layer.get_GlobalSurf(), null, ini);
                if(layer.is_aerogel()){
                    show_Shape3D(layer.get_TrackingSurf(), null, "AA");
                    for(int ico=0; ico<layer.size(); ico++) System.out.format("HH %4d %4d %s \n", ilay, ico, layer.get_CompoBary(ico).toStringBrief(2));
                }
                if(layer.is_mapmt())show_Shape3D(layer.get_TrackingSurf(), null, "PP");

            }else{
                if(layer.is_spherical_mirror()) show_Shape3D(layer.get_GlobalSurf(), null, ini);
                show_Shape3D(layer.get_TrackingSurf(), null, ini);
            }
        }
      

    }

    // ----------------
    public void show_Triangle3D(Triangle3D tri, String name){
    // ----------------

        if(name!=null) System.out.format(" %s ----------------------- %s \n", name, toString(get_Triangle3D_Bary(tri)));
        System.out.format(" %s %s %s \n", toString(tri.point(0)), toString(tri.point(1)), toString(tri.point(2)));
    }


    // ----------------
    public void show_Shape3D(Shape3D plane, String name, String head){
    // ----------------

        if(name!=null) System.out.format(" %s ----------------------- %s \n", name, toString(get_Shape3D_Bary(plane)));
        for (int ifa=0; ifa<plane.size(); ifa++){
            Face3D f = plane.face(ifa);
            if(head==null){
                System.out.format(" %s %s %s \n", toString(f.point(0)), toString(f.point(1)), toString(f.point(2)));
            }else{
                System.out.format(" %s %s %s %s \n", head, toString(f.point(0)), toString(f.point(1)), toString(f.point(2)));
            }
        }
    }


    // ----------------
    public void show_Sphere3D(Sphere3D sphere, String name, String head){
    // ----------------

        if(name!=null) System.out.format(" %s ----------------------- \n", name);
        if(head==null){
            System.out.format(" %s %7.2f \n", toString(sphere.getCenter()), sphere.getRadius());
        }else{
            System.out.format(" %s %s %7.2f \n", head, toString(sphere.getCenter()), sphere.getRadius());
        }
    }


    //------------------------------
    public Vector3D into_LabFrame(Vector3D vec, RICHFrame frame) {
    //------------------------------

        return into_LabFrame(vec, frame.xref(), frame.yref(), frame.zref());

    }

    //------------------------------
    public Vector3D into_LabFrame(Vector3D vec, Vector3D xref, Vector3D yref, Vector3D zref) {
    //------------------------------

        // decompose each vector/rotation along a ref axis (i.e. angle.x*xref) into three cartesian rotation in the lab system 
        return new Vector3D( vec.z()*zref.x() + vec.y()*yref.x() + vec.x()*xref.x(),
                             vec.z()*zref.y() + vec.y()*yref.y() + vec.x()*xref.y(),
                             vec.z()*zref.z() + vec.y()*yref.z() + vec.x()*xref.z());
    }


    //------------------------------
    public void misalign_Element(Shape3D shape, RICHFrame frame, Vector3D angle, Vector3D shift) {
    //------------------------------

        int debugMode = 0;

        if(shape!=null){

            if(debugMode>=1)System.out.format(" FRAME %s %s %s %s\n",frame.xref().toStringBrief(2),frame.yref().toStringBrief(2),
                                                     frame.zref().toStringBrief(2),frame.bref().toStringBrief(2));
            if(debugMode>=1)show_Shape3D(shape, "BEFORE", null);

            if(angle.mag()>0){
                translate_Shape3D(shape, frame.bref().multiply(-1.0));

                Vector3D ang_lab = into_LabFrame(angle, frame);
                if(debugMode>=1)System.out.format(" ang_lab %s \n", ang_lab.toStringBrief(2));
                shape.rotateZ(ang_lab.z());
                shape.rotateY(ang_lab.y());
                shape.rotateX(ang_lab.x());

                translate_Shape3D(shape, frame.bref());
            }

            if(shift.mag()>0){
                Vector3D shift_lab = into_LabFrame(shift, frame);
                if(debugMode>=1)System.out.format(" shift_lab %s \n", shift_lab.toStringBrief(2));
                translate_Shape3D(shape, shift_lab);
            }

            if(debugMode>=1)show_Shape3D(shape, "AFTER ", null);
        }

    }


    //------------------------------
    public void misalign_Element(Sphere3D sphere, RICHFrame frame, Vector3D angle, Vector3D shift) {
    //------------------------------

        int debugMode = 0;

        if(sphere!=null){

            if(debugMode>=1) System.out.format(" FRAME %s %s %s %s\n",frame.xref().toStringBrief(2),frame.yref().toStringBrief(2),
                                                     frame.zref().toStringBrief(2),frame.bref().toStringBrief(2));
            if(debugMode>=1)show_Sphere3D(sphere, "BEFORE", null);

            if(angle.mag()>0){
                translate_Sphere3D(sphere, frame.bref().multiply(-1.0));

                Vector3D ang_lab = into_LabFrame(angle, frame);
                if(debugMode>=1)System.out.format(" ang_lab %s \n", ang_lab.toStringBrief(2));
                sphere.rotateZ(ang_lab.z());
                sphere.rotateY(ang_lab.y());
                sphere.rotateX(ang_lab.x());

                translate_Sphere3D(sphere, frame.bref());
            }
            
            if(shift.mag()>0){
                Vector3D shift_lab = into_LabFrame(shift, frame);
                if(debugMode>=1)System.out.format(" shift_lab %s \n", shift_lab.toStringBrief(2));
                translate_Sphere3D(sphere, shift_lab);
            }

            if(debugMode>=1)show_Sphere3D(sphere, "AFTER ", null);
        }

    }

    //------------------------------
    public Shape3D copy_Shape3D(Shape3D shape) { 
    //------------------------------

        Shape3D copy = new Shape3D();
        for (int ifa=0; ifa<shape.size(); ifa++){copy.addFace( toTriangle3D(shape.face(ifa)));}
        return copy;

    }

    // ----------------
    public void merge_Shape3D(Shape3D shape, Shape3D other) {
    // ----------------

        for(int ifa=0; ifa<other.size(); ifa++)shape.addFace( other.face(ifa) );

    }


    //------------------------------
    public Vector3d get_Shape3D_Center(Shape3D shape) { return toVector3d(shape.center()); }
    //------------------------------

    
    // ----------------
    public Vector3d get_CSGBary(CSG CSGVol) {
    // ----------------

        /*
        *   Avoid double counting of points  
        */
        int debugMode = 0;
        List<Vector3d> pts = new ArrayList<Vector3d>();
        if(debugMode>=1)System.out.format(" get_CSGBary %d \n", CSGVol.getPolygons().size());

        double cX=0.0;
        double cY=0.0;
        double cZ=0.0;
        double np=0.0;
        int ii=0;
        for (Polygon pol: CSGVol.getPolygons()){
            if(debugMode>=1)System.out.format(" poli  %4d ",ii);
            for (Vertex vert: pol.vertices ){
                Vector3d p = toVector3d(vert);
                int found = 0;
                for(int i=0; i<pts.size(); i++){
                    if(p.distance(pts.get(i))<1.e-3)found=1;
                }

                if(found==0){
                    if(debugMode>=1)System.out.format(" --> New Vertex %s\n",toString(p));
                    pts.add(p);
                    cX += p.x;
                    cY += p.y;
                    cZ += p.z;
                    np += 1;
                }else{
                    if(debugMode>=1)System.out.format(" --> Old Vertex %s\n",toString(p));
                }
                
            }
            ii++;
        } 

        if(np>0)return new Vector3d(cX/np, cY/np, cZ/np);
        return new Vector3d(0., 0., 0.);
    }


    //------------------------------
    public Vector3d get_Shape3D_Bary(Shape3D shape) { 
    //------------------------------
     
        /*
        *   Avoid double counting of points  
        */
        int debugMode = 0;
        List<Vector3d> pts = new ArrayList<Vector3d>();
        if(debugMode>=1)System.out.format(" get_Shape3D_Bary %d \n", shape.size());

        double cX=0.0;
        double cY=0.0;
        double cZ=0.0;
        double np=0.0;
        for (int ifa=0; ifa<shape.size(); ifa++){
            Face3D f = shape.face(ifa);
            if(debugMode>=1)System.out.format(" --> get_face %d \n",ifa);
            for (int ipo=0; ipo<3; ipo++){

                Vector3d p = toVector3d(f.point(ipo));
                int found = 0;
                for(int i=0; i<pts.size(); i++){
                    if(p.distance(pts.get(i))<1.e-3)found=1;
                }

                if(found==0){
                    if(debugMode>=1)System.out.format(" --> New Vertex %s\n",toString(p));
                    pts.add(p);
                    cX += p.x;
                    cY += p.y;
                    cZ += p.z;
                    np += 1;
                }else{
                    if(debugMode>=1)System.out.format(" --> Old Vertex %s\n",toString(p));
                }
                
            }
        } 

        if(np>0)return new Vector3d(cX/np, cY/np, cZ/np);
        return new Vector3d(0., 0., 0.);
    }


    //------------------------------
    public Vector3d get_Triangle3D_Bary(Triangle3D tri) { return toVector3d(tri.center()); }
    //------------------------------


    //------------------------------
    public Vector3d get_Shape3D_Normal(Shape3D shape, int iface) {
    //------------------------------

        Triangle3D face = new Triangle3D(shape.face(iface).point(0), shape.face(iface).point(1), shape.face(iface).point(2));
        Vector3D normal = face.normal();
        return toVector3d(normal).normalized(); 

    }


    //------------------------------
    public Vector3d get_Shape3D_Normal(Shape3D shape) {
    //------------------------------

        Triangle3D face = new Triangle3D(shape.face(0).point(0), shape.face(0).point(1), shape.face(0).point(2));
        Vector3D normal = face.normal();
        return toVector3d(normal).normalized(); 

    }


    //------------------------------
    public Vector3d get_Poly_Normal(Polygon pol) {
    //------------------------------
        Vector3d a = pol.vertices.get(0).pos;
        Vector3d b = pol.vertices.get(1).pos;
        Vector3d c = pol.vertices.get(2).pos;
        Vector3d n = b.minus(a).cross(c.minus(a)).normalized();
        return n;
    }


    //------------------------------
    public Vector3d get_Poly_Bary(Polygon pol) {
    //------------------------------
        Vector3d a = pol.vertices.get(0).pos;
        Vector3d b = pol.vertices.get(1).pos;
        Vector3d c = pol.vertices.get(2).pos;
        Vector3d bary = a.plus(b);
        bary = bary.add(c);
        return bary.dividedBy(3.);
    }

    //------------------------------
    public double get_Poly_Area(Polygon pol) {
    //------------------------------
        Vector3d a = pol.vertices.get(0).pos;
        Vector3d b = pol.vertices.get(1).pos;
        Vector3d c = pol.vertices.get(2).pos;
        Line3D base = new Line3D(toPoint3D(a), toPoint3D(b));
        Line3D h = base.distance( toPoint3D(c));
        return base.length()*h.length()/2;
    }

    
    // ----------------
    public String get_PlaneMirrorSide(RICHComponent compo) {
    // ----------------

        int debugMode = 0;

        Vector3D front   = new Vector3D(-0.42,   0.00,   0.91);
        Vector3D left    = new Vector3D(-0.50,  -0.87,   0.00);
        Vector3D right   = new Vector3D(-0.50,   0.87,   0.00);
        Vector3D bottom  = new Vector3D(-1.00,   0.00,   0.00);

        //ATT: this is before having set the layer components
        Vector3D bary = toVector3D(get_CSGBary( compo.get_CSGVol() ));
        if(debugMode>=1)System.out.format(" compo bary %s \n", toString(bary));

        for (Triangle3D pol: toTriangle3D(compo.get_CSGVol().getPolygons()) ){

            if(debugMode>=1)System.out.format("Test front %7.3f  left %7.3f  right %7.3f  bot %7.3f \n",
                     pol.normal().angle(front), pol.normal().angle(left),
                     pol.normal().angle(right), pol.normal().angle(bottom));

            if(pol.normal().angle(front)<5.e-3){
                 if(bary.x() > -100){
                     return new String("mirror_front_B1");
                 }else{
                     return new String("mirror_front_B2");
                 }
            }
            if(pol.normal().angle(left)<5.e-3){
                 if(bary.x() > -100){
                     return new String("mirror_left_L1");
                 }else{
                     return new String("mirror_left_L2");
                 }
            }
            if(pol.normal().angle(right)<5.e-3){
                 if(bary.x() > -100){
                     return new String("mirror_right_L1");
                 }else{
                     return new String("mirror_right_L2");
                 }
            }
            if(pol.normal().angle(bottom)<5.e-3){
                 return new String("mirror_bottom");
            }
            
        }
        return new String("none");
    }


    // ----------------
    public void dump_Face(Face3D face) {
    // ----------------

            Vector3d p0 = toVector3d( face.point(0) );
            Vector3d p1 = toVector3d( face.point(1) );
            Vector3d p2 = toVector3d( face.point(2) );
            System.out.format(" %8.3f %8.3f %8.3f   %8.3f %8.3f %8.3f  %8.3f %8.3f %8.3f \n",p0.x,p0.y,p0.z,p1.x,p1.y,p1.z,p2.x,p2.y,p2.z);
    }


    // ----------------
    public void dump_Polygon(Polygon pol) {
    // ----------------
        for (Vertex vert: pol.vertices ){
            System.out.format(" %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
        }
        System.out.format(" Norm %7.2f %7.2f %7.2f A %7.2f \n",get_Poly_Normal(pol).x,get_Poly_Normal(pol).y,get_Poly_Normal(pol).z,get_Poly_Area(pol));
    }

    // ----------------
    public void dump_StlComponent(CSG CSGVol) {
    // ----------------

        System.out.format(" ------------------\n");
        System.out.format(" Dump of Stl \n");
        System.out.format(" ------------------\n");
        int ii=0;
        for (Polygon pol: CSGVol.getPolygons()){
            System.out.format("  %4d ",ii);
            for (Vertex vert: pol.vertices ){
                System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
            }
            System.out.format(" Norm %7.2f %7.2f %7.2f A %7.2f \n",get_Poly_Normal(pol).x,get_Poly_Normal(pol).y,get_Poly_Normal(pol).z,get_Poly_Area(pol));
            ii++;
        }

    }


    // ----------------
    public void dump_StlComponent(int ilay, int ico) {
    // ----------------

        System.out.format(" ------------------\n");
        System.out.format(" Dump of Stl %d in layer %d \n", ico, ilay);
        System.out.format(" ------------------\n");
        int ii=0;
        for (Polygon pol: get_CSGVolume(ilay, ico).getPolygons()){
            System.out.format("  %4d ",ii);
            for (Vertex vert: pol.vertices ){
                System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
            }
            System.out.format(" Norm %7.2f %7.2f %7.2f A %7.2f \n",get_Poly_Normal(pol).x,get_Poly_Normal(pol).y,get_Poly_Normal(pol).z,get_Poly_Area(pol));
            ii++;
        }

    }


    // ----------------
    public Vector3d find_intersection_UpperHalf_RICH(Line3D ray){
    // ----------------

        int debugMode = 0;
        RICHIntersection inter = get_Layer("mirror_sphere").find_Entrance(ray, -1);

        if(inter!=null){
            if(debugMode>=1)  System.out.format("find_intersection with SPHERICAL (%d, %d): %s\n",
                 inter.get_layer(), inter.get_component(), inter.get_pos().toStringBrief(2));
            return toVector3d(inter.get_pos());
        }else{
            if(debugMode>=1)  System.out.format("find NO intersection with SPHERICAL \n");
        }

        return null;

    }

    // ----------------
    public Vector3d find_intersection_MAPMT(Line3D ray){
    // ----------------

        int debugMode = 0;

        RICHIntersection inter = get_Layer("mapmts").find_Entrance(ray, -1);

        if(inter!=null){
            if(debugMode>=1)  System.out.format("find_intersection with MAPMT (%d, %d): %s\n",
                 inter.get_layer(), inter.get_component(), inter.get_pos().toStringBrief(2));
            return toVector3d(inter.get_pos());
        }

        return null;
    }


    // ----------------
    public boolean is_Spherical_Mirror (int ilay){
    // ----------------

        if(opticlayers.get(ilay).get_Name().equals("mirror_sphere"))return true;
        return false;  

    }


    // ----------------
    public Vector3D Reflection(Vector3D vector1, Vector3D normal) {
    // ----------------

        int debugMode = 0;
        Vector3D vin = vector1.asUnit();
        Vector3D vnorm = normal.asUnit();

        double cosI  =  vin.dot(vnorm); 
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);
        if (cosI > 0) {
            if(debugMode>=1)System.out.format("ATT: Mirror normal parallel to impinging ray %7.3f \n",cosI);
            vnorm.scale(-1.0);
        }

        double refle = 2*(vin.dot(vnorm));
        Vector3D vout = vin.sub(vnorm.multiply(refle));

        if(debugMode>=1){
            System.out.format("Mirror normal %s\n",normal.toStringBrief(3));
            System.out.format("Reflected versor %s\n", vout.asUnit().toStringBrief(3));
        }

        return vout.asUnit();
    }

    // ----------------
    public Vector3D Transmission2(Vector3D vector1, Vector3D normal, double n_1, double n_2) {
    // ----------------

        int debugMode = 0;
        double rn = n_1 / n_2;

        Vector3D vin = vector1.asUnit();
        Vector3D vnorm = normal.asUnit();

        double cosI  =  vin.dot(vnorm); 
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);
        if (cosI < 0) {
            if(debugMode>=1)System.out.format("ATT: Mirror normal parallel to impinging ray %7.3f \n",cosI);
            vnorm.scale(-1.0);
        }
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",vin.toStringBrief(3),vnorm.toStringBrief(3),cosI);

        Vector3D vrot = (vnorm.cross(vin)).asUnit();
 
        double angi = Math.acos(vin.dot(vnorm)) ;
        double ango = Math.asin( rn * Math.sin(angi));

        Quaternion q = new Quaternion(ango, toVector3d(vrot));

        Vector3D vout = toVector3D(q.rotate(toVector3d(vnorm)));

        if(debugMode>=1){
            System.out.format(" vin   %s \n", vin.toStringBrief(3));
            System.out.format(" vnorm %s \n", vnorm.toStringBrief(3)); 
            System.out.format(" angles %7.3f %7.3f \n",angi*57.3, ango*57.3);
            System.out.format(" vout  %s \n", vout.toStringBrief(3)); 
        }

        return vout;

    }
 
    // ----------------
    public RICHRay OpticalRotation(RICHRay rayin, RICHIntersection intersection) {
    // ----------------

        int debugMode = 0;
        Point3D vori = rayin.origin();
        Vector3D inVersor = rayin.direction().asUnit();
        Vector3D newVersor = new Vector3D(0.0, 0.0, 0.0);
        RICHRay rayout = null;
        int type = 0;
 
        if(debugMode>=1)System.out.format("Ray for %3d %3d \n",intersection.get_layer(), intersection.get_component());
        //RICHComponent component = opticlayers.get(intersection.get_layer()).get(intersection.get_component());
        RICHLayer layer = opticlayers.get(intersection.get_layer());

        if(layer.is_optical()==true){
                
            if(debugMode>=1)System.out.format("Ray rotation at Optical compo %3d %3d  xyz %s \n", intersection.get_layer(), intersection.get_component(), vori.toStringBrief(2));
            Vector3D vnorm = intersection.get_normal();
            if(vnorm != null ){
                if(layer.is_mirror()==true){
             
                    newVersor = Reflection(inVersor, vnorm);
                    type=10000+intersection.get_layer()*100+intersection.get_component()+1;
                    if(debugMode>=1)System.out.format(" Reflection at mirror surface norm %s \n", vnorm.toStringBrief(3));

                }else{

                    newVersor = Transmission2(inVersor, vnorm, intersection.get_nin(), intersection.get_nout());
                    type=20000+intersection.get_layer()*100+intersection.get_component()+1;
                    if(debugMode>=1){
                        System.out.format(" Refraction at surface boundary norm %s \n", vnorm.toStringBrief(3));
                        System.out.format(" norm in %s %7.4f \n",vnorm.toStringBrief(3), vnorm.costheta());
                        System.out.format(" vers in %s %7.4f \n",inVersor.toStringBrief(3), inVersor.costheta());
                        System.out.format(" vers ou %s %7.4f \n",newVersor.toStringBrief(3), newVersor.costheta());
                    }
                }
            }

            if(debugMode>=1)System.out.format(" Versor in %s   --> out %s \n",inVersor.toStringBrief(3), newVersor.toStringBrief(3)); 
        }

        rayout = new RICHRay(vori, newVersor.multiply(200));
        rayout.set_type(type);
        return rayout;

    }

    // ----------------
    public ArrayList<RICHRay> RayTrace(Vector3d emission, int orilay, int orico, Vector3d vlab) {
    // ---------------- 

        int debugMode = 0;

        RICHLayer layer = get_Layer(orilay);
        if(debugMode>=1)System.out.format("Raytrace gets refractive index from CCDB database %8.5f \n",layer.get(orico).get_index());
        return RayTrace(emission, orilay, orico, vlab, layer.get(orico).get_index());

    }

    // ----------------
    public ArrayList<RICHRay> RayTrace(Vector3d emission, int orilay, int orico, Vector3d vlab, double naero) {
    // ---------------- 
    // return the hit position on the PMT plane of a photon emitted at emission with direction vlab

        int debugMode = 0;
        ArrayList<RICHRay> raytracks = new ArrayList<RICHRay>();

        Point3D emi = toPoint3D(emission);
        Vector3D vdir = toVector3D(vlab);

        RICHRay lastray = new RICHRay(emi, vdir.multiply(200));
        if(debugMode>=1) {
            System.out.format(" --------------------------- \n");
            System.out.format("Raytrace photon ori %s  olay %3d  oco %3d  dir %s \n",emi.toStringBrief(2),orilay,orico,vdir.toStringBrief(3)); 
            System.out.format(" --------------------------- \n");
        }

        RICHLayer layer = get_Layer(orilay);
        if(layer==null)return null;

        RICHIntersection first_intersection = null;
        if(reco_constants.DO_CURVED_AERO==1){
            first_intersection = layer.find_ExitCurved(lastray.asLine3D(), orico);
        }else{
            first_intersection = layer.find_Exit(lastray.asLine3D(), orico);
        }
        if(first_intersection==null)return null;   

        if(debugMode>=1){
            System.out.format(" first inter : ");
            first_intersection.showIntersection();
        }

        Point3D new_pos = first_intersection.get_pos();
        RICHRay oriray = new RICHRay(emi, new_pos);

        /* rewrite the refractive index to be consistent with photon theta
           only valid for initial aerogel
           the rest of components take ref index from CCDB database 
        */
        //oriray.set_refind(layer.get(orico).get_index());
        first_intersection.set_nin((float) naero);
        oriray.set_refind(naero);
        raytracks.add(oriray);

        RICHRay rayin = new RICHRay(new_pos, oriray.direction().multiply(200));
        lastray = OpticalRotation(rayin, first_intersection);
        lastray.set_refind(RICHConstants.RICH_AIR_INDEX);
        RICHIntersection last_intersection = first_intersection;

        if(debugMode>=1){
            System.out.format(" add first ray : ");
            oriray.showRay();
            System.out.format(" get rotated ray : ");
            lastray.showRay();
        }

        int jj = 1;
        int front_nrefl = 0;
        boolean detected = false;
        boolean lost = false;
        while( detected == false && lost == false && raytracks.size()<10){

            Point3D last_ori  = lastray.origin();
            Point3D new_hit = null;
            RICHIntersection new_intersection = null;
            if(debugMode>=1)System.out.format(" ray-tracking step %d \n",jj);

            if(last_intersection.get_layer()<4){
  
                // planar mirrors
                RICHIntersection test_intersection = get_Layer("mirror_bottom").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer("mirror_left_L1").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer("mirror_right_L1").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer("mirror_left_L2").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection==null)test_intersection = get_Layer("mirror_right_L2").find_Entrance(lastray.asLine3D(), -1);
                if(test_intersection!=null){
                    if(debugMode>=1){
                        System.out.format(" test planar (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                        test_intersection.showIntersection();
                    }
                    //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection;
                    new_intersection = test_intersection;
                }else{
                    if(debugMode>=1)System.out.format(" no lateral mirror intersection \n");
                }

                // shperical mirrors
                if(lastray.direction().costheta()>0){
                    test_intersection = get_Layer("mirror_sphere").find_EntranceCurved(lastray.asLine3D(), -1);
                    
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test sphere (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            if(new_intersection==null || (new_intersection!=null && test_intersection.get_pos().z()<new_intersection.get_pos().z())) {
                                new_intersection = test_intersection;
                            }
                        //}
                    }else{
                        if(debugMode>=1)System.out.format(" no sphere intersection \n");
                    }

                    RICHIntersection pmt_inter = get_Layer("mapmts").find_Entrance(lastray.asLine3D(), -1);
                    if(pmt_inter!=null) {
                        Point3D test_hit = pmt_inter.get_pos(); 
                        //if(test_hit.distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            new_hit=test_hit;
                            if(debugMode>=1)System.out.format(" test PMT : Hit %s \n",new_hit.toStringBrief(2));
                        //}else{
                            //if(debugMode>=1)System.out.format(" too far PMT plane intersection \n");
                        //}
                    }else{
                        if(debugMode>=1)System.out.format(" no PMT plane intersection \n");
                    }
                }else{
                    test_intersection = get_Layer("mirror_front_B1").find_Entrance(lastray.asLine3D(), -1);
                    if(test_intersection==null)test_intersection = get_Layer("mirror_front_B2").find_Entrance(lastray.asLine3D(), -1);
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test front (z %7.2f, step %7.2f) : ",last_ori.z(), test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        //if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection; 
                        new_intersection = test_intersection;
                        front_nrefl++;
                    }else{
                        if(debugMode>=1)System.out.format(" no front mirror intersection \n");
                    }
                }

            }

            if(new_hit!=null){
                if(new_intersection==null || new_hit.distance(last_ori) <= new_intersection.get_pos().distance(last_ori)) {
                    detected=true;
                    if(debugMode>=1) System.out.format(" found PMT hit %s  dist %6.2f \n", new_hit.toStringBrief(2), new_hit.distance(last_ori));
                }
            }
            if(front_nrefl>1){
                lost = true; 
                new_hit=new_intersection.get_pos();
                if(debugMode>=1) System.out.format(" double front reflection: stop at front %s \n",toString(new_hit));
            }
            if(new_hit==null && new_intersection==null){
                lost = true; 
                Point3D point = new Point3D(0.0, 0.0, 0.0);;
                new_hit = new Point3D(lastray.end());
                Plane3D plane = toTriangle3D(get_Layer(get_LayerNumber("mapmts")).get_Face(0)).plane();
                if(plane.intersection(lastray.asLine3D(), point)==1){ 
                    double vers = lastray.direction().costheta();
                    double Delta_z = point.z()-lastray.origin().z();
                    if(debugMode>=1) System.out.format(" forced stop at PMT plane: Delta_z %7.3f vers %7.3f \n",Delta_z, vers);
                    if(Delta_z*vers>0){
                        new_hit=point;
                        if(debugMode>=1) System.out.format(" take PMT plane hit %s \n", new_hit.toStringBrief(2));
                    }else{
                        if(debugMode>=1) System.out.format(" no Delta_z on PMT plane: take last ray end %s \n", new_hit.toStringBrief(2));
                    }
                }else{
                    if(debugMode>=1) System.out.format(" no hit on PMT plane: take last ray end %s \n", new_hit.toStringBrief(2));
                }
            }

            if(lost || detected){
                if(debugMode>=1 && lost) System.out.format("LOST! stop ray-tracing \n");
                if(debugMode>=1 && detected) System.out.format("DETECTED! stop ray-tracing \n");

                RICHRay newray = new RICHRay(last_ori, new_hit);
                newray.set_type(lastray.get_type());
                newray.set_refind((float) RICHConstants.RICH_AIR_INDEX);
                if(detected)newray.set_detected();
                raytracks.add(newray);
                if(debugMode>=1){
                    System.out.format(" --> Add last ray (%7.4f) : ", RICHConstants.RICH_AIR_INDEX);
                    newray.showRay();
                }

            }else{

                RICHRay newray = new RICHRay(last_ori, new_intersection.get_pos());
                newray.set_refind(new_intersection.get_nin());
                newray.set_type(lastray.get_type());
                raytracks.add(newray);

                // new ray starting at intersection, to be rotated
                rayin = new RICHRay(new_intersection.get_pos(), newray.direction().multiply(200));
                lastray = OpticalRotation(rayin, new_intersection);
                lastray.set_refind(new_intersection.get_nout());

                if(debugMode>=1){
                    System.out.format(" -->  Add new ray (%7.4f) : ",new_intersection.get_nin());
                    newray.showRay();
                    System.out.format(" -->  Get rotated ray (%7.4f) : ",new_intersection.get_nout());
                    lastray.showRay();
                }

            }
            jj++;

        }

        if(debugMode>=1) System.out.format(" --------------------------- \n");
        //if(detected==true)return raytracks;
        return raytracks;
        //return null;
   }


    // ----------------
    public void init_ProcessTime(){
    // ----------------

       for(int i=0; i<NTIME; i++){richprocess_time[i] = 0.0; richprocess_ntimes[i]=0;}

    }

    // ----------------
    public void start_ProcessTime(){
    // ----------------
       int debugMode = 0;

       RICH_START_TIME = System.nanoTime();
       RICH_LAST_TIME = RICH_START_TIME;

       if(debugMode==1)System.out.format("RICH_START_TIME %d \n",RICH_START_TIME);
    }

    // ----------------
    public void save_ProcessTime(int iphase){
    // ----------------

        int debugMode = 0;

        if(iphase>-1 && iphase<NTIME){

            long nanotime = System.nanoTime()-RICH_START_TIME;
            double dtime = nanotime * 1.0e-6;

            richprocess_time[iphase] += dtime;
            richprocess_ntimes[iphase] += 1;

            if(debugMode==1)System.out.format("Phase %3d: Save time %3d  %10.4f \n", iphase, richprocess_ntimes[iphase], dtime);

        }

        double interval = (System.nanoTime()-RICH_LAST_TIME)*1e-9;   //seconds
        if(iphase==0 && interval > reco_constants.SHOW_PROGRESS_INTERVAL) {

            RICH_LAST_TIME = System.nanoTime();
            dump_ProcessTime();
        }
    }

    // ----------------
    public void dump_ProcessTime(){
    // ----------------

        String str[] = {" RAW-RICH " ," DC-RICH  ", " HADRONS  ", " ANALYTIC ", " TRACED   ", " WRITE    ", " CLOSE    "};

        for(int i=0; i<NTIME; i++){
            double time = 0.0;
            if(richprocess_ntimes[i]>0){
                int found=-1;
                for(int j=i-1; j>-1; j--){
                    if(richprocess_ntimes[j]>0){found=j; break;}
                }
                if(found>-1){
                    time = (richprocess_time[i]/richprocess_ntimes[i]-richprocess_time[found]/richprocess_ntimes[found]);
                }else{
                    time = richprocess_time[i]/richprocess_ntimes[i];
                }
                System.out.format(" PHASE %3d: %s  average over %6d  time %10.4f ms \n", i, str[i], richprocess_ntimes[i], time);
            }
        }

        for(int i=NTIME-1; i>-1; i--){
            double time = 0.0;
            if(richprocess_ntimes[i]>0){
                time = richprocess_time[i]/richprocess_ntimes[i];
                System.out.format(" PHASE %3d:  TOTAL      average over %6d  time %10.4f ms \n", NTIME, richprocess_ntimes[i], time);
                break;
            }
        }
    }

}
