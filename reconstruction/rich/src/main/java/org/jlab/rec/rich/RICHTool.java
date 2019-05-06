/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rich;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
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


    private static int anode_map[] = {60,58,59,57,52,50,51,49,44,42,43,41,36,34,35,
                     33,28,26,27,25,20,18,19,17,12,10,11,9,4,2,3,1,5,7,6,8,13,15,14,16,21,
                     23,22,24,29,31,30,32,37,39,38,40,45,47,46,48,53,55,54,56,61,63,62,64};


    private static int tile2pmt[][]={{   1,   2,   3},  
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


    private static int NLAY=13;
    private static int NROW=25;
    private static int NCOL=56;
    private static int NPMT=391;
    private static int NPIX=64;

    private static double global_shift[] = {0.0, 0.0, 0.0};
    private static double global_angle[] = {0.0, 0.0, 0.0};
    private static double pmt_timeoff[][] = new double[NPMT][NPIX];
    private static double pmt_timewalk[][] = new double[NPMT][4];
    private static double aero_refi[][] = new double[4][31];
    private static double aero_plan[][] = new double[4][31];

    private Shape3D rich_survey_plane = null;
    private Vector3d rich_misa_centre = null;
    private Vector3d rich_misa_angle  = null;
    private Vector3d rich_misa_shift  = null;
    private static Vector3d layer_misa_angle[] = new Vector3d[NLAY];
    private static Vector3d layer_misa_shift[] = new Vector3d[NLAY];

    private static int pfirst[] = {1, 7,14,22,31,41,52,64,77, 91,106,122,139,157,176,196,217,239,262,286,311,337,364,392,395};
    private static int plast[]  = {6,13,21,30,40,51,63,76,90,105,121,138,156,175,195,216,238,261,285,310,336,363,391,394,397};

    private int nxp[] = new int[397]; // X coordinate of pixel 1 of each mapmt
    private int nyp[] = new int[397]; // Y coordinate of pixel 1 of each mapmt

    public RICHGeant4Factory richfactory = new RICHGeant4Factory();

    private static List<RICHLayer> opticlayers = new ArrayList<RICHLayer>();

    private static RICHPixel MAPMTpixels  = null; 

    private static int NTIME = 10;
    private long RICH_START_TIME = (long) 0;
    private long RICH_LAST_TIME = (long) 0;
    private double richprocess_time[] = new double[NTIME];
    private int richprocess_ntimes[] = new int[NTIME];

    public int FTOF_phase_corr = 0;
    public int DO_MISALIGNMENT = 0;
    public int MISA_RICH_REF   = 0;
    public int MISA_PMT_PIVOT  = 0;
    public int FORCE_DC_MATCH  = 0;

    //------------------------------
    public void init(IndexedTable aeroConstants){
    //------------------------------

        // start processing time
        init_ProcessTime();
    
        // to be moved to CCDB
        if(RICHConstants.READ_FROM_FILES==1){
            init_ConstantsTxT();
        }else{
            init_ConstantsCCDB(aeroConstants);
        }

        // global pixel coordinat indexes
        init_GlobalPixelGeo();

        // RICH survey
        this.rich_misa_angle = Vector3d.ZERO;
        this.rich_misa_shift = Vector3d.ZERO;
        init_Geo_Align();

        // RICH geometry organized on layers of Shape3D area and RICH components 
        init_Geo_Factory();

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
    public void init_ConstantsCCDB(IndexedTable aeroConstants){
    //------------------------------

        int debugMode = 1;
        double mrad = RICHConstants.MRAD;

       /*
        * TIME_OFFSETs
        */

        for(int ipmt=0; ipmt<NPMT; ipmt++){
            for(int ich=0; ich<NPIX; ich++){
                pmt_timeoff[ipmt][ich]=0.0;
            }
        }

        /*
        *  TIME_WALKs
        */

        for(int ipmt=0; ipmt<NPMT; ipmt++){
            for(int ich=0; ich<4; ich++){
                pmt_timewalk[ipmt][ich]=0.0;
            }
        }


        /*
        *  SINGLE COMPONENT MISALIGNMENT
        *  This comes on top of the RICH survey and global transformation
        */

        for (int ilay=0; ilay<NLAY; ilay++){
            layer_misa_shift[ilay] = new Vector3d(0., 0., 0.);
            layer_misa_angle[ilay] = new Vector3d(0., 0., 0.);
        }


        /*
        * AEROGEL OPTCIS
        */

        int nco[] = {16,22,31,31};
        for (int ila=0; ila<4; ila++){
            for (int ico=0; ico<nco[ila]; ico++){
                aero_refi[ila][ico] = (float) aeroConstants.getDoubleValue("n400", 4,201+ila,ico+1);
                aero_plan[ila][ico] = (float) aeroConstants.getDoubleValue("planarity", 4,201+ila,ico+1);
                if(debugMode>=0)System.out.format(" ila %4d  ico %3d  n = %8.5f  pla = %8.2f\n", 201+ila, ico+1, aero_refi[ila][ico], aero_plan[ila][ico]);
            }
        }

    }


    //------------------------------
    public void init_ConstantsTxT(){
    //------------------------------
    // To be moved to CCDB

        int debugMode = 1;
        double mrad = RICHConstants.MRAD;

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
                          System.out.format("Read TOFF pmt %4d (ich=%3d: %8.2f) \n", ipmt, ich, pmt_timeoff[ipmt-1][ich-1]);

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

                if(debugMode>=1)System.out.format("Read WALK pmt %d", ipmt);
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



        /**
        * DC_OFFSETs
        */
        String dcoff_filename = new String("CALIB_DATA/DC_offsets_4013.txt");

        try {

            BufferedReader bf = new BufferedReader(new FileReader(dcoff_filename));
            String currentLine = null;

            while ( (currentLine = bf.readLine()) != null) {    

                String[] array = currentLine.split(" ");
                int idc = Integer.parseInt(array[0]);
                int imatch = Integer.parseInt(array[1]);
                int iref   = Integer.parseInt(array[2]);
                int ipiv   = Integer.parseInt(array[3]);
                
                DO_MISALIGNMENT = idc;
                FORCE_DC_MATCH  = imatch;
                MISA_RICH_REF   = iref;
                MISA_PMT_PIVOT  = ipiv;

                double dx  =  (double) Float.parseFloat(array[4]);
                double dy  =  (double) Float.parseFloat(array[5]);
                double dz  =  (double) Float.parseFloat(array[6]);
                double thx =  (double) Float.parseFloat(array[7]);
                double thy =  (double) Float.parseFloat(array[8]);
                double thz =  (double) Float.parseFloat(array[9]);

                dx=dx/5;
                dy=dy/5;
                dz=dz/2;
                thx=thx/1000;
                thy=thy/1000;
                thz=thz/1000;

                double THE = 25./180.*Math.PI;
                if(MISA_RICH_REF==1){
                    // Transformation as MAPMT normal (pointing to IP) opposite for lab z (and x) axes
                    thx=-thx;
                    thz=-thz;
                    global_shift[0] = (float) (Math.cos(THE)*dx - Math.sin(THE)*dz);
                    global_shift[1] = (float) dy;
                    global_shift[2] = (float) (Math.sin(THE)*dx + Math.cos(THE)*dz);

                    global_angle[0] = (float) (Math.cos(THE)*thx - Math.sin(THE)*thz);
                    global_angle[1] = (float) thy;
                    global_angle[2] = (float) (Math.sin(THE)*thx + Math.cos(THE)*thz);
                }else{
                    global_shift[0] = (float) dx;
                    global_shift[1] = (float) dy;
                    global_shift[2] = (float) dz;

                    global_angle[0] = (float) thx;
                    global_angle[1] = (float) thy;
                    global_angle[2] = (float) thz;
                }
                
                if(debugMode>=1){
                    System.out.format("Read DO_MISALIGNMENT %d \n", idc); 
                    System.out.format("Read FORCE_DC_MATCH  %d \n", imatch); 
                    System.out.format("Read MISA_RICH_REF   %d \n", iref); 
                    System.out.format("Read MISA_PMT_PIVOT  %d \n", ipiv); 
                    System.out.format("GLOBAL OFF %8.2f %8.2f %8.2f \n", global_shift[0], global_shift[1], global_shift[2]); 
                    System.out.format("GLOBAL THE %8.4f %8.4f %8.4f \n", global_angle[0], global_angle[1], global_angle[2]); 
                }
                
            }

        } catch (Exception e) {

            System.err.format("Exception occurred trying to read '%s' \n", dcoff_filename);
            e.printStackTrace();
        }



        /**
        *  SINGLE COMPONENT MISALIGNMENT
        *  This comes on top of the RICH survey and global transformation
        */

        for (int ilay=0; ilay<NLAY; ilay++){
            layer_misa_shift[ilay] = new Vector3d(0., 0., 0.);
            layer_misa_angle[ilay] = new Vector3d(0., 0., 0.);
        }

        String misaco_filename = new String("CALIB_DATA/RICHlayer_misalignment.txt");

        try {

            BufferedReader bf = new BufferedReader(new FileReader(misaco_filename));
            String currentLine = null;

            while ( (currentLine = bf.readLine()) != null) {    

                String[] array = currentLine.split(" ");
                int ila = Integer.parseInt(array[0]);

                float  dx  = Float.parseFloat(array[1]);
                float  dy  = Float.parseFloat(array[2]);
                float  dz  = Float.parseFloat(array[3]);
                float  thx = Float.parseFloat(array[4]);
                float  thy = Float.parseFloat(array[5]);
                float  thz = Float.parseFloat(array[6]);

                // the rotation is assumed to be in the component local ref system
                layer_misa_shift[ila] = new Vector3d(dx/5., dy/5., dz/5.);
                //layer_misa_shift[ila] = new Vector3d(dx/2., dy/2., dz/2.);
                //layer_misa_angle[ila] = new Vector3d(thx/mrad, thy/mrad, thz/mrad);
                layer_misa_angle[ila] = new Vector3d(thx/mrad*2, thy/mrad*2, thz/mrad*2);

                if(debugMode>=0){
                    System.out.format("MISA layer %4d shift %s  angle %s \n", ila, toString(layer_misa_shift[ila]), toString(layer_misa_angle[ila]));
                }
            }

        } catch (Exception e) {

            System.err.format("Exception occurred trying to read '%s' \n", misaco_filename);
            e.printStackTrace();
        }



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

    //------------------------------
    public void init_Geo_Align(){
    //------------------------------

        int debugMode = 0;

        if(debugMode>=1){
            System.out.format("---------------\n");
            System.out.format("Calculate RICH Alignment \n");
            System.out.format("---------------\n");
        }

        /* 
        *  Define nominal plane
        */
        Vector3d RDA = new Vector3d(-300.274,  168.299,  460.327);
        Vector3d RDB = new Vector3d(-300.309,  -168.299, 460.310);
        Vector3d RDC = new Vector3d(-31.102,     0., 585.886);

        Face3D f = new Triangle3D( toPoint3D(RDA), toPoint3D(RDB), toPoint3D(RDC));
        Shape3D nomi_plane = new Shape3D(f);
        Vector3d nomi_n = get_Shape3D_Normal(nomi_plane);
        Vector3d nomi_b = get_Shape3D_Bary(nomi_plane); 

        rich_survey_plane = new Shape3D(f);
        
        /*
        *   Define surveyed plane
        */
        Vector3d mRDA = new Vector3d(-301.211, 168.505, 467.514);
        Vector3d mRDB = new Vector3d(-300.514, -167.929, 465.334);
        Vector3d mRDC = new Vector3d(-31.552, -0.086, 591.329);

        Face3D mf= new Triangle3D( toPoint3D(mRDA), toPoint3D(mRDB), toPoint3D(mRDC));
        Shape3D real_plane = new Shape3D(mf);
        Vector3d real_n = get_Shape3D_Normal(real_plane);
        Vector3d real_b = get_Shape3D_Bary(real_plane); 

        if(debugMode>=1){
            // check possible deformations
            Vector3d check_a = toVector3d(f.point(1)).minus(toVector3d(f.point(0)));
            Vector3d check_b = toVector3d(f.point(2)).minus(toVector3d(f.point(1)));
            Vector3d check_c = toVector3d(f.point(2)).minus(toVector3d(f.point(0)));

            Vector3d checp_a = toVector3d(mf.point(1)).minus(toVector3d(mf.point(0)));
            Vector3d checp_b = toVector3d(mf.point(2)).minus(toVector3d(mf.point(1)));
            Vector3d checp_c = toVector3d(mf.point(2)).minus(toVector3d(mf.point(0)));

            System.out.format("Sides nominal    %8.3f %8.3f %8.3f \n",check_a.magnitude(), check_b.magnitude(), check_c.magnitude());
            System.out.format("Sides real       %8.3f %8.3f %8.3f \n",checp_a.magnitude(), checp_b.magnitude(), checp_c.magnitude());
        }

        // define shift among barycenters
        Vector3d diff_b = real_b.minus(nomi_b);
        rich_misa_centre = nomi_b;

        // define rotation angle and vector
        Vector3d dir = nomi_n.cross(real_n).normalized();
        double ang = Math.acos(nomi_n.dot(real_n));
        Vector3d rota_n = dir.times(ang);

        double mrad = RICHConstants.MRAD;

        Vector3d misa_shift = null;
        Vector3d misa_angle = null;
        if(DO_MISALIGNMENT==1){
            misa_shift = new Vector3d(diff_b);  
            misa_angle = new Vector3d(rota_n);
        }else{
            misa_shift = new Vector3d(0.,0.,0.);
            misa_angle = new Vector3d(0.,0.,0.);
        }

        Vector3d dcrich_shift = new Vector3d(global_shift[0], global_shift[1], global_shift[2]);
        this.rich_misa_shift = new Vector3d(misa_shift.plus(dcrich_shift));
        Vector3d dcrich_angle = new Vector3d(global_angle[0], global_angle[1], global_angle[2]);
        this.rich_misa_angle = new Vector3d(misa_angle.plus(dcrich_angle));

        if(debugMode>=1){
            System.out.format(" -------------------- \n");
            System.out.format(" survey angle %7.2f %7.2f %7.2f \n", rota_n.x*mrad, rota_n.y*mrad, rota_n.z*mrad);
            System.out.format(" survey shift %7.2f %7.2f %7.2f \n", diff_b.x, diff_b.y, diff_b.z);
            System.out.format(" -------------------- \n");
            System.out.format(" misalg angle %7.2f %7.2f %7.2f \n", misa_angle.x*mrad, misa_angle.y*mrad, misa_angle.z*mrad);
            System.out.format(" misalg shift %7.2f %7.2f %7.2f \n", misa_shift.x, misa_shift.y, misa_shift.z);
            System.out.format(" -------------------- \n");
            System.out.format(" extern angle %7.2f %7.2f %7.2f \n", dcrich_angle.x*mrad, dcrich_angle.y*mrad, dcrich_angle.z*mrad);
            System.out.format(" extern shift %7.2f %7.2f %7.2f \n", dcrich_shift.x, dcrich_shift.y, dcrich_shift.z);
            System.out.format(" -------------------- \n");
            System.out.format(" rich   angle %7.2f %7.2f %7.2f \n", this.rich_misa_angle.x*mrad, this.rich_misa_angle.y*mrad, this.rich_misa_angle.z*mrad);
            System.out.format(" rich   shift %7.2f %7.2f %7.2f \n", this.rich_misa_shift.x, this.rich_misa_shift.y, this.rich_misa_shift.z);
            System.out.format(" -------------------- \n");
        
      
            System.out.format(" Check survey plane \n");
            System.out.format(" -------------------- \n");
            double thex = this.rich_misa_angle.dot(Vector3d.X_ONE);
            double they = this.rich_misa_angle.dot(Vector3d.Y_ONE);
            double thez = this.rich_misa_angle.dot(Vector3d.Z_ONE);

            System.out.format("Rot Angles NewRef %7.2f | %7.2f %7.2f %7.2f \n", ang*mrad, thex*mrad, they*mrad, thez*mrad);

            Vector3d new_n = new Vector3d(nomi_n);
            new_n = new_n.rotateZ(thez);
            new_n = new_n.rotateY(they);
            new_n = new_n.rotateX(thex);

            System.out.format("Normal nominal %9.6f %9.6f %9.6f \n", nomi_n.x, nomi_n.y, nomi_n.z);
            System.out.format("Normal real    %9.6f %9.6f %9.6f \n", real_n.x, real_n.y, real_n.z);
            System.out.format("Normal rotated %9.6f %9.6f %9.6f \n", new_n.x, new_n.y, new_n.z);
            System.out.format("\n");
            System.out.format("Baryc  nominal %9.6f %9.6f %9.6f \n", nomi_b.x, nomi_b.y, nomi_b.z);
            System.out.format("Baryc  real    %9.6f %9.6f %9.6f \n", real_b.x, real_b.y, real_b.z);
            System.out.format("Baryc  diff    %9.6f %9.6f %9.6f \n", diff_b.x, diff_b.y, diff_b.z);
            System.out.format("\n");

            show_Shape3D(nomi_plane, null, null);

            show_Shape3D(real_plane, null, null);

            // test misalignment angle and shift
            Face3D at = new Triangle3D( toPoint3D(RDA), toPoint3D(RDB), toPoint3D(RDC));
            Shape3D test_plane = new Shape3D(at);

            //misalign_Global_Plane(test_plane, -1);
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


        }

    }

    //------------------------------
    public void init_Geo_Factory(){
    //------------------------------
    // Take RICHFactory Layers of Geant4 volumes (for GEMC) and convert in coatjava Layers 
    // of RICH components accounting for optical descriptiors plus basic tracking 
    // planes for effective ray tracing
    // to be done: aerogel cromatic dispersion, mirror reflectivity vs wavelength

        int debugMode = 0;
        // relevant directions for the basic tracking planes
        Vector3d front   = new Vector3d(-0.42,   0.00,   0.91);
        Vector3d left    = new Vector3d(-0.50,  -0.87,   0.00);
        Vector3d right   = new Vector3d(-0.50,   0.87,   0.00);
        Vector3d bottom  = new Vector3d(-1.00,   0.00,   0.00);
        Vector3d back    = new Vector3d( 0.42,   0.00,  -0.91);
        Vector3d sphere  = new Vector3d( 0.76,   0.00,  -0.65);

        int factory_lay[] = {201,202,203,204,301,301,301,301,301,301,301,302,401};
        int type_lay[] = {1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 4};
        String strlay[] = {"aerogel_2cm_B1" ,"aerogel_2cm_B2", "aerogel_3cm_L1", "aerogel_3cm_L2", 
                           "mirror_front_B1", "mirror_front_B2", "mirror_left_L1", "mirror_left_L2", "mirror_right_L1", "mirror_right_L2", "mirror_bottom", 
                           "mirror_sphere", "mapmts"};
        Vector3d veclay[] = {front, front, front, back, front, front, left, left, right, right, bottom, sphere, back};

        /*
        * Generate the layers of components
        */
        for (int ilay=0; ilay<NLAY; ilay++){

            int idlayer = factory_lay[ilay];
            int tlayer = type_lay[ilay];
            String slayer = strlay[ilay];
            Vector3d vlayer = veclay[ilay];
            if(debugMode>=1){
                System.out.format("-------------------------\n");
                System.out.format("Create Layer %d id  %d: %s dir %7.2f %7.2f %7.2f \n",ilay, idlayer, slayer, vlayer.x, vlayer.y, vlayer.z);
                System.out.format("-------------------------\n");
            }
            RICHLayer layer = new RICHLayer(idlayer, slayer, vlayer);

            for (int ico=0; ico<get_RICHFactory_Size(idlayer); ico++){
                RICHComponent compo = get_RICHFactory_Component(idlayer, ico);
                compo.set_type(tlayer);
                if(debugMode>=1)System.out.format(" Lay %3d component %3d  bary %s\n", idlayer, ico, get_CSGVol_Bary(compo.get_CSGVol()));

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

                if(debugMode>=3 && (idlayer==201 || idlayer==202)){
                    System.out.format("ECCOLO\n");
                    compo.showComponent();
                    dump_StlComponent(compo.get_CSGVol());
                }

                // regrouping of the planar mirros into planes
                if(idlayer!=301){
                    layer.add(compo);
                }else{
                    if(get_PlaneMirrorSide(compo).equals(slayer)){
                        if(debugMode>=1)System.out.format(" ---> add to Layer %3d id %3d %s \n",ilay,idlayer,slayer);
                        layer.add(compo); 
                    }
                }

            }

            opticlayers.add(layer);
        }

        /*
        * Generate the basic planes  for tracking
        */
        generate_Tracking_Planes();
        show_RICH("Real RICH Geometry", "RR");

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
        Face3D compo_face = get_Layer(ilay).get_Component_Face(ipmt-1, 0);
        Vector3d Vertex = toVector3d( compo_face.point(1) );
        //System.out.format("Misa vtx %8.3f %8.3f %8.3f \n",Vertex.x, Vertex.y, Vertex.z);
        Vector3d VPixel = Vertex.plus(MAPMTpixels.GetPixelCenter(anode));
        return new Vector3d (VPixel.x, -VPixel.y, VPixel.z);

    }

    //------------------------------
    public Shape3D build_Global_Plane_new(Shape3D plane) {
    //------------------------------
        /*
        *  build a global tracking plane from the detailed component surface
        * ATT: assumes planes with vertical (along y) edges 
        */

        int debugMode = 0;
        if(plane==null) return null;

        Vector3d extre1 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre2 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre3 = new Vector3d(0.0, 0.0, 0.0);
        Vector3d extre4 = new Vector3d(0.0, 0.0, 0.0);

        /*
        * look for two diagonals
        */
        int ifound=-1;
        int jfound=-1;
        Line3D diagfirst = null;
        for(int ik=0; ik<2; ik++){

            double distmax = 0.0;
            Line3D diagmax = null;
            for (int ifa=0; ifa<plane.size(); ifa++){
                Face3D fi = plane.face(ifa);
                for (int ipo=0; ipo<3; ipo++){
                    if(ifa*3+ipo==ifound)continue;

                    for (int jfa=0; jfa<plane.size(); jfa++){
                        Face3D fj = plane.face(jfa);
                        for (int jpo=0; jpo<3; jpo++){
                            if(jfa*3+jpo==jfound)continue;

                            double dist = fi.point(ipo).distance (fj.point(jpo));
                            Line3D diag = new Line3D(fi.point(ipo), fj.point(jpo));
                            if(debugMode>=1)System.out.format("  Points %d %d (%s) %d %d (%s) : dist %7.3f \n",ifa,ipo,toString(fi.point(ipo)), jfa,jpo,toString(fj.point(jpo)), dist);
                            if(dist>distmax && (diagfirst==null || diagfirst.direction().angle(diag.direction())>0.6)) {
                                distmax=dist;
                                ifound=ifa*3+ipo;
                                jfound=jfa*3+jpo;
                                diagmax = diag;
                                if(ik==0){
                                    extre1 = toVector3d(fi.point(ipo));
                                    extre2 = toVector3d(fj.point(jpo));
                                }else{
                                    extre3 = toVector3d(fi.point(ipo));
                                    extre4 = toVector3d(fj.point(jpo));
                                }
                                if(debugMode>=1)System.out.format("  taken \n");
                            }
                            
                        }  
                    }
                }
            }  
            diagfirst = diagmax;

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

        Vector3d plane_norm = get_Shape3D_Normal(plane);
        Vector3d guess_norm = get_Shape3D_Normal(guess_one);
        double ang = guess_norm.angle(plane_norm)*RICHConstants.RAD;

        if(debugMode>=1){
            guess_one.show();
            System.out.format("Guess one normal %7.2f %7.2f %7.2f --> %7.2f \n",guess_norm.x, guess_norm.y, guess_norm.z, ang);
            guess_two.show();
            Vector3d other_norm = get_Shape3D_Normal(guess_two);
            double other_ang = other_norm.angle(plane_norm)*RICHConstants.RAD;
            System.out.format("Guess two normal %7.2f %7.2f %7.2f --> %7.2f \n",other_norm.x, other_norm.y, other_norm.z, other_ang);
        }

        if(ang<10){
            return guess_one;
        }else{
            return guess_two;
        }

    }


    //------------------------------
    public Shape3D build_Global_Plane(Shape3D plane) {
    //------------------------------
        /*
        *  build a global tracking plane from the detailed component surface
        * ATT: assumes planes with vertical (along y) edges 
        */

        int debugMode = 0;
        if(plane==null) return null;

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

        Vector3d plane_norm = get_Shape3D_Normal(plane);
        Vector3d guess_norm = get_Shape3D_Normal(guess_one);
        double ang = guess_norm.angle(plane_norm)*RICHConstants.RAD;

        if(debugMode>=1){
            guess_one.show();
            System.out.format("Guess one normal %7.2f %7.2f %7.2f --> %7.2f \n",guess_norm.x, guess_norm.y, guess_norm.z, ang);
            guess_two.show();
            Vector3d other_norm = get_Shape3D_Normal(guess_two);
            double other_ang = other_norm.angle(plane_norm)*RICHConstants.RAD;
            System.out.format("Guess two normal %7.2f %7.2f %7.2f --> %7.2f \n",other_norm.x, other_norm.y, other_norm.z, other_ang);
        }

        if(ang<10){
            return guess_one;
        }else{
            return guess_two;
        }

    }


    //------------------------------
    public Shape3D build_Component_Plane(int ilay, Vector3d orient, List<Integer> icompos){
    //------------------------------
        //build the tracking plane from the components

        int debugMode = 0;
        Shape3D plane = new Shape3D();
       
        RICHLayer layer = opticlayers.get(ilay);
        Vector3d inside = layer.get_Vinside();
 
        if(debugMode>=1){
            System.out.format("generate tracking plane for layer %d \n",ilay);
            System.out.format("inside  vect: %7.2f %7.2f %7.2f\n",inside.x, inside.y, inside.z);
            System.out.format("orient vect: %7.2f %7.2f %7.2f  --> %7.3f\n",orient.x, orient.y, orient.z, orient.angle(inside)*57.3);
        }

        int igo = 0;
        for (int ico=0; ico<layer.get_size(); ico++){

            if(!layer.is_spherical_mirror()){
                /*
                * Build from CSG volumes
                */
                RICHComponent compo = opticlayers.get(ilay).get(ico);

                int ipo = 0;
                for (Polygon pol: compo.get_CSGVol().getPolygons()){
                    Vector3d pol_norm = get_Poly_Normal(pol);
                    if(debugMode>=1){System.out.format("Compo %4d pol %4d  angle %7.2f ", ico, ipo, pol_norm.angle(orient)*57.3); dump_Polygon(pol);}
                    
                    if(pol_norm.angle(orient)<1e-2){  
                        // in case of multiple surfaces (i.e. for glass skin mirrors), take the innermost.
                        Vector3d bary_diff = get_Poly_Bary(pol).minus(layer.get_Barycenter());

                        if(debugMode>=1){
                            System.out.format("Compo %d face %d %d : bary %7.2f %7.2f %7.2f vs lay_bary %7.2f %7.2f %7.2f \n",ico,ipo,igo,  
                                get_Poly_Bary(pol).x, get_Poly_Bary(pol).y, get_Poly_Bary(pol).z, 
                                layer.get_Barycenter().x, layer.get_Barycenter().y, layer.get_Barycenter().z);
                            System.out.format(" diff %7.2f %7.2f %7.2f   normal %7.2f %7.2f %7.2f  dor %7.3f \n",  bary_diff.x, bary_diff.y, bary_diff.z,
                                pol_norm.x, pol_norm.y, pol_norm.z, bary_diff.dot(pol_norm));
                        }

                        if(bary_diff.dot(pol_norm)>0) {
                            List<Triangle3D> trias = toTriangle3D(pol);
                            for(int ifa=0; ifa<trias.size(); ifa++) {plane.addFace(trias.get(ifa)); icompos.add(ico);}
                            if(debugMode>=1)System.out.format("    ---> take this from compo %4d \n",ico);
                        }
                        igo++;
                    }
                    ipo++;
                }

            }else{

                /*
                * Build from Nominal planes
                */
                Shape3D submir = generate_Nominal_Plane(ilay, ico+1, inside);
                for(int ifa=0; ifa<submir.size(); ifa++) {plane.addFace(submir.face(ifa)); icompos.add(ico);}
                 
            }

        }

        if(debugMode>=1 && plane.size()>0){
            plane.show();
            System.out.format("Plane Normal %s \n",toString(get_Shape3D_Normal(plane)));
            for(int j = 0; j< icompos.size(); j++) System.out.format("  --> %4d \n",icompos.get(j));
        }
        return plane;

    }


    //------------------------------
    public Triangle3D toTriangle3D(Face3D face){
    //------------------------------

        return new Triangle3D(face.point(0), face.point(1), face.point(2));

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
    public List<Triangle3D> toTriangle3D(Polygon pol){
    //------------------------------

        List<Triangle3D> trias = new ArrayList<Triangle3D>();

        for (int iv=2; iv<pol.vertices.size(); iv++){
            Triangle3D tri = new Triangle3D(toPoint3D(pol.vertices.get(0)), toPoint3D(pol.vertices.get(iv-1)), toPoint3D(pol.vertices.get(iv)));
            trias.add(tri);
        }
        
        return trias;
    }


    //------------------------------
    public Shape3D generate_PMTplane(){
    //------------------------------
        //define the PMT Tracking Plane assuming fixed PMT order

        int debugMode = 0;

        int ilay=12;
        Vector3d front = new Vector3d(0.42,   0.00,  -0.91);

        // Look for the high-x high-y extreme
        RICHComponent compo = opticlayers.get(ilay).get(0);
        Vector3d extre1 = new Vector3d(0.0, 0.0, 0.0);

        for (Polygon pol: compo.get_CSGVol().getPolygons()){
            //MAPMT front face 
            if(get_Poly_Normal(pol).angle(front)<5.e-3){  
                for (Vertex vert: pol.vertices ){
                    if(debugMode>=1)System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
                    if(vert.pos.x > extre1.x || vert.pos.y > extre1.y) {
                        extre1 = vert.pos;
                        if(debugMode>=1)System.out.format(" ---> ok vert1 \n");
                    }else{
                        if(debugMode>=1)System.out.format(" \n");
                    }
                 }
             }
        }

        // Look for the high-x low-y extreme
        compo = opticlayers.get(ilay).get(5);
        Vector3d extre2 = new Vector3d(0.0, 9999.0, 0.0);

        for (Polygon pol: compo.get_CSGVol().getPolygons()){
            //MAPMT front face 
            if(get_Poly_Normal(pol).angle(front)<5.e-3){  
                for (Vertex vert: pol.vertices ){
                    if(debugMode>=1)System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
                    if(vert.pos.x > extre2.x || vert.pos.y < extre2.y) {
                        extre2 = vert.pos;
                        if(debugMode>=1)System.out.format(" ---> ok vert2 \n");
                    }else{
                        if(debugMode>=1)System.out.format(" \n");
                    }
                 }
             }
        }

        // Look for the low-x high-y extreme
        compo = opticlayers.get(ilay).get(363);
        Vector3d extre3 = new Vector3d(9999.0, 0.0, 0.0);

        for (Polygon pol: compo.get_CSGVol().getPolygons()){
            //MAPMT front face 
            if(get_Poly_Normal(pol).angle(front)<5.e-3){  
                for (Vertex vert: pol.vertices ){
                    if(debugMode>=1)System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
                    if(vert.pos.x < extre3.x || vert.pos.y > extre3.y) {
                        extre3 = vert.pos;
                        if(debugMode>=1)System.out.format(" ---> ok vert3 \n");
                    }else{
                        if(debugMode>=1)System.out.format(" \n");
                    }
                 }
             }
        }

        // Look for the low-x low-y extreme
        compo = opticlayers.get(ilay).get(390);
        Vector3d extre4 = new Vector3d(9999.0, 9999.0, 0.0);

        for (Polygon pol: compo.get_CSGVol().getPolygons()){
            //MAPMT front face 
            if(get_Poly_Normal(pol).angle(front)<5.e-3){  
                for (Vertex vert: pol.vertices ){
                    if(debugMode>=1)System.out.format(" Vtx %7.2f %7.2f %7.2f  ",vert.pos.x,vert.pos.y,vert.pos.z);
                    if(vert.pos.x < extre4.x || vert.pos.y < extre4.y) {
                        extre4 = vert.pos;
                        if(debugMode>=1)System.out.format(" ---> ok vert4 \n");
                    }else{
                        if(debugMode>=1)System.out.format(" \n");
                    }
                 }
             }
        }

        Face3D half1 = new Triangle3D( toPoint3D(extre1), toPoint3D(extre2), toPoint3D(extre3));
        Face3D half2 = new Triangle3D( toPoint3D(extre2), toPoint3D(extre3), toPoint3D(extre4));

        Shape3D PMTplane = new Shape3D();
        PMTplane.addFace(half1);
        PMTplane.addFace(half2);
        if(debugMode>=1){
            System.out.format(" ------------------------\n");
            System.out.format(" Create PMT plane for tracking \n");
            System.out.format(" ------------------------\n");
            PMTplane.show();
        }
        return PMTplane;
   
    }


    //------------------------------
    public void misalign_Global_Plane(Shape3D plane, int ilay){
    //------------------------------

        int debugMode = 0;

        if(debugMode>=1 && ilay==0){
            System.out.format(" ------------------------\n");
            System.out.format(" Misalign global plane\n");
            System.out.format(" ------------------------\n");
            if(ilay!=12)show_Shape3D(plane, null, null);
            System.out.format("Old normal %s (%s mrad) \n",toString(get_Shape3D_Normal(plane, 0), 4), toString( get_angles(get_Shape3D_Normal(plane, 0)),4) );
        }
 
        /*
        *   To account for RICH survey
        */
        Vector3d r_angle = rich_misa_angle;
        Vector3d r_shift = rich_misa_shift;

        if(debugMode>=1)System.out.format("Layer %3d  -->  AsRICH %s %s ",ilay, toString(r_shift), toString(r_angle));
        rotate_AsRICH(plane, r_angle);
        translate_Shape3D(plane, r_shift);

        /*
        *   To account for internal misalignments
        */
        if(ilay>=0 && ilay<=NLAY){
            Vector3d c_angle = layer_misa_angle[ilay];
            Vector3d c_shift = layer_misa_shift[ilay];
            
            if(c_angle.magnitude()>0 || c_shift.magnitude()>0){
                Vector3d bary = get_Layer(ilay).get_Barycenter(); 
                if(debugMode>=1){
                    System.out.format("  -->  AsComponent  %s %s \n", toString(c_shift), toString(c_angle));
                }
                rotate_AsComponent(plane, bary, c_angle);
                translate_AsComponent(plane, c_shift);
            }else{
                if(debugMode>=1)System.out.format("  \n");
            }

            if(debugMode>=1 && ilay==0){
                show_Shape3D(plane, null, null);
                System.out.format("New normal %s (%s mrad) \n",toString(get_Shape3D_Normal(plane, 0), 4), toString( get_angles(get_Shape3D_Normal(plane, 0)),4));
            }
        }

    }


    //------------------------------
    public void generate_Tracking_Planes(){
    //------------------------------

        int debugMode = 0;

        for (int ilay=0; ilay<NLAY; ilay++){

            if(debugMode>=1){
                System.out.format("------------------------\n");
                System.out.format("Generate tracking for Layer %d %s\n", ilay, get_Layer(ilay).get_Name());
                System.out.format("------------------------\n");
            }

            RICHLayer layer = get_Layer(ilay);

            layer.generate_Barycenter();
            Vector3d orient = layer.get_Vinside();

            /*
            *  Nominal plane just for reference 
            */
            layer.set_Nominal_Plane( generate_Nominal_Plane(ilay, 0, orient) );

            if(layer.is_spherical_mirror()){

                /*
                *   define the real mirror spherical surface
                */
                Sphere3D sphere = new Sphere3D(-45.868, 0.0, 391.977, 270.);
                layer.set_Tracking_Sphere(sphere);

                /*
                *  automatize procedure still to be defined
                */
                //layer.set_Global_Plane( layer.get_Nominal_Plane() );

                //System.out.format("Provo col speherical \n");
                //List<Integer> compo_list = new ArrayList<Integer>();
                //Shape3D compo_plane = build_Component_Plane(ilay, orient, compo_list );

            }


                /*
                *  Group component faces with normal and position vs barycenter along orient
                *  Generate a global plane for fast tracking without gaps
                */
                if(debugMode>=1)System.out.format(" build_Component_Plane %d \n",ilay);
                List<Integer> compo_list = new ArrayList<Integer>();
                Shape3D compo_plane = build_Component_Plane(ilay, orient, compo_list );
                
                if(debugMode>=1)System.out.format(" build_Global_Plane %d \n",ilay);
                Shape3D global_plane = null;
                if(layer.is_mirror()){
                    if(layer.is_planar_mirror()) global_plane = build_Component_Plane(ilay, orient, compo_list );
                    if(layer.is_spherical_mirror()) global_plane = generate_Nominal_Plane(ilay, 0, orient);
                }else{
                    global_plane = build_Global_Plane(compo_plane);
                }


                /*
                *  In case of aerogel add the second face 
                */
                if(layer.is_aerogel()){
                    Shape3D other_compo = build_Component_Plane(ilay, orient.negated(), compo_list );
                    Shape3D other_global = build_Global_Plane(other_compo);
                   
                    merge_Shape3D(compo_plane, other_compo);
                    merge_Shape3D(global_plane, other_global);

                }

                layer.set_Component_Surf( compo_plane, compo_list);
                layer.set_Global_Plane( global_plane);

                /*
                *  Take MAPMT plane barycenter as pivot of RICH rotations
                */
                if(MISA_PMT_PIVOT==1 && layer.is_mapmt()) {
                    rich_misa_centre = layer.get_Barycenter();
                }
                if(debugMode>=0 && layer.is_mapmt())System.out.format(" RICH PIVO %s \n",toString(rich_misa_centre));

           

        }

        for (int ilay=0; ilay<NLAY; ilay++){

            if(debugMode>=1){
                System.out.format("------------------------\n");
                System.out.format("Misalign tracking for Layer %d %s\n", ilay, get_Layer(ilay).get_Name());
                System.out.format("------------------------\n");
            }

            RICHLayer layer = get_Layer(ilay);

            if(layer.is_spherical_mirror()){

                // to be defined

            }else{

                /*
                *  Misalign both planes as required
                */
                Shape3D global_misa = layer.get_Global_Plane();
                Shape3D compo_misa = layer.get_Component_Surf();
                List<Integer> compo_list = layer.get_Component_List();

                int do_misa=0;
                if(layer_misa_angle[ilay].magnitude()>0 || layer_misa_shift[ilay].magnitude()>0)do_misa=1;
                
                if(rich_misa_angle.magnitude()>0 || rich_misa_shift.magnitude()>0 || do_misa==1){
                    if(debugMode>=1)System.out.format("MISA LAYER %3d %s %s \n",ilay, toString(layer_misa_shift[ilay]), toString(layer_misa_angle[ilay]));
                    misalign_Global_Plane(global_misa, ilay);
                    misalign_Global_Plane(compo_misa, ilay);
                }

                /*
                *  Generate Pixel map on the misaligned MAPMT plane
                */
                if(layer.is_mapmt()) {
                    if(debugMode>=1)show_Shape3D(compo_misa, null, "CC");
                    generate_Pixel_Map(ilay, 0, compo_misa, compo_list);
                }

            }

        }

        /*
        *  Check misalignment effect on survey plane
        */
        if(debugMode>=1){
            System.out.format("Centre %s\n",toString(rich_misa_centre));
            double mrad = RICHConstants.MRAD;
            System.out.format(" rich   angle %7.2f %7.2f %7.2f \n", this.rich_misa_angle.x*mrad, this.rich_misa_angle.y*mrad, this.rich_misa_angle.z*mrad);
            System.out.format(" rich   shift %7.2f %7.2f %7.2f \n", this.rich_misa_shift.x, this.rich_misa_shift.y, this.rich_misa_shift.z);
            show_Shape3D(rich_survey_plane,"Nominal survey", null);
            misalign_Global_Plane(rich_survey_plane, 0);
            show_Shape3D(rich_survey_plane,"Misalig survey", null);
        }


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
                    if(debugMode>=1)System.out.format("  --> ifa %4d ", ifa);
                    dump_Face( compo_plane.face(ifa) );
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
                    vertex = toVector3d( layer.get_Component_Face(5,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                    vertex = toVector3d( layer.get_Component_Face(363,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                    vertex = toVector3d( layer.get_Component_Face(390,0).point(1) );
                    MAPMTpixels.show_Pixels( vertex );
                }
            }
        }
    }

    //------------------------------
    public Shape3D generate_Nominal_Plane(int ilay, int ico, Vector3d orient){
    //------------------------------

        int debugMode = 0;

        if(ilay<0 || ilay>NLAY) return null;

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
            extre1 = new Point3D(-37.165,  -11.847,  587.781);
            extre2 = new Point3D(-37.165,  11.847,  587.781);
            extre3 = new Point3D(165.136,  -85.728,  528.107);
            extre4 = new Point3D(-165.136,  85.728,  528.107);
        }

        // Left-side mirror
        if((ilay==6 || ilay==7) && ico==0){
            extre1 = new Point3D(39.849,  12.095,  688.630);
            extre2 = new Point3D(-39.849,  12.095,  591.568);
            extre3 = new Point3D(-238.031, 126.515,  526.116);
            extre4 = new Point3D(-229.924, 121.834,  502.935);
        }

        // Right-side mirror
        if((ilay==8 || ilay==9) && ico==0){
            extre1 = new Point3D(39.849,  -12.095,  688.630);
            extre2 = new Point3D(-39.849,  -12.095,  591.568);
            extre3 = new Point3D(-238.031, -126.515,  526.116);
            extre4 = new Point3D(-229.924, -121.834,  502.935);
        }

        // Bottom mirror
        if(ilay==10 && ico==0){
            extre1 = new Point3D(-39.763,  11.500,  591.601);
            extre2 = new Point3D(-39.763,  -11.500,  687.101);
            extre3 = new Point3D(-39.763,  11.500,  687.101);
            extre4 = new Point3D(-39.763,  -11.500,  591.601);
        }

        //  Spherical mirror
        if(ilay==11){
            if(ico==0){
                extre1 = new Point3D(-146.861, 77.9926, 629.86);
                extre2 = new Point3D(-244.481, 134.353, 516.032);
                extre3 = new Point3D(-146.861, -77.9926, 629.86);
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
                extre1 = new Point3D(-239.384,   0.169, 580.210);
                extre2 = new Point3D(-274.838,   0.160, 535.010);
                extre3 = new Point3D(-232.914,  69.398, 573.831);
                extre4 = new Point3D(-267.782,  66.527, 530.594);
            }
            if(ico==7){
                extre1 = new Point3D(-239.384,  -0.169, 580.210);
                extre2 = new Point3D(-274.838,  -0.160, 535.010);
                extre3 = new Point3D(-232.914, -69.398, 573.831);
                extre4 = new Point3D(-267.782, -66.527, 530.594);
            }
            if(ico==8){
                extre1 = new Point3D(-236.785,  42.182, 578.124);
                extre2 = new Point3D(-196.080,  42.185, 612.272);
                extre3 = new Point3D(-219.114, 119.708, 560.905);
                extre4 = new Point3D(-186.891, 101.097, 598.779);
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
        Triangle3D half1 = new Triangle3D( extre1, extre2, extre3);
        Triangle3D half2 = new Triangle3D( extre2, extre4, extre3);
        Shape3D guess_one = new Shape3D(half1, half2);

        Triangle3D half3 = new Triangle3D( extre3, extre2, extre1);
        Triangle3D half4 = new Triangle3D( extre3, extre4, extre2);
        Shape3D guess_two = new Shape3D(half3, half4);

        Vector3d vinside = get_Layer(ilay).get_Vinside().normalized();
        Vector3d guess_norm = toVector3d(half1.normal()).normalized();
        double ang = guess_norm.angle(vinside)*RICHConstants.RAD;

        if(debugMode>=1){
            System.out.format("Look for Nominal plane %3d\n",ilay);
            guess_one.show();
            System.out.format("Guess one normal %7.2f %7.2f %7.2f --> %7.2f \n",guess_norm.x, guess_norm.y, guess_norm.z, ang);
            guess_two.show();
            Vector3d other_norm = toVector3d(half3.normal()).normalized();
            double other_ang = other_norm.angle(vinside)*RICHConstants.RAD;
            System.out.format("Guess two normal %7.2f %7.2f %7.2f --> %7.2f \n",other_norm.x, other_norm.y, other_norm.z, other_ang);
        }

        if(ang<10){
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
    public double get_GlobalShift(int ich){
    //------------------------------
 
        return global_shift[ich-1];

    }

    //------------------------------
    public double get_GlobalAngle(int ich){
    //------------------------------
 
        return global_angle[ich-1];

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
    public RICHLayer get_Layer(int ilay){ 
    //------------------------------
        if(ilay>-1 && ilay<NLAY) return opticlayers.get(ilay);
        return null; 
    }


    //------------------------------
    public RICHComponent get_RICHFactory_Component(int idlay, int ico){ 
    //------------------------------
        //if(idlay==401) return new RICHComponent(ico, idlay, 1, richfactory.GetPMT(ico+1));
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
    public RICHComponent get_Component(int ilay, int ico){ return opticlayers.get(ilay).get(ico);}
    //------------------------------


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
        Line3D lout = new Line3D(toPoint3D(ray.origin()), toPoint3D(ray.end()));
        return lout;
     }


    //------------------------------
    public void translate_Triangle3D(Triangle3D tri, Vector3d shift) {
    //------------------------------

        tri.translateXYZ(shift.x, shift.y, shift.z);

    }


    //------------------------------
    public void translate_Shape3D(Shape3D shape, Vector3d shift) {
    //------------------------------

        shape.translateXYZ(shift.x, shift.y, shift.z);

    }

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
            if(ilay!=14){
                RICHLayer layer = get_Layer(ilay);
                if(!layer.is_spherical_mirror()){
                    show_Shape3D(layer.get_Global_Plane(), null, head);
                }else{
                    show_Shape3D(layer.get_Component_Surf(), null, head);
                }
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



    //------------------------------
    public void translate_AsComponent(Shape3D shape, Vector3d shift) {
    //------------------------------

        int debugMode = 0;
        Vector3d zref = get_Shape3D_Normal(shape);
        Vector3d yref = new Vector3d(0., 1., 0.);
        // in case of lateral mirrors
        if(Math.abs(zref.y)>0.6) yref = new Vector3d(0., 0., 1.);
        Vector3d xref = (yref.cross(zref)).normalized();

        if(debugMode>=1){
            System.out.format("translate_AsComponent\n");
            System.out.format("X ref %s\n",toString(xref));
            System.out.format("Y ref %s\n",toString(yref));
            System.out.format("Z ref %s\n",toString(zref));
            System.out.format("Shift  %s\n",toString(shift));
            System.out.format(" --> Z axis %7.3f \n", shift.z*zref.z + shift.y*yref.z + shift.x*xref.z);
            System.out.format(" --> Y axis %7.3f \n", shift.z*zref.y + shift.y*yref.y + shift.x*xref.y);
            System.out.format(" --> X axis %7.3f \n", shift.z*zref.x + shift.y*yref.x + shift.x*xref.x);
        } 

        // decompose each shift along a ref axis (i.e. shift.x*xref) into three cartesian shifts in the lab system 
        shape.translateXYZ(shift.z*zref.x + shift.y*yref.x + shift.x*xref.x,
                           shift.z*zref.y + shift.y*yref.y + shift.x*xref.y,
                           shift.z*zref.z + shift.y*yref.z + shift.x*xref.z);

    }


    //------------------------------
    public void rotate_AsComponent(Shape3D shape, Vector3d bary, Vector3d angle) {
    //------------------------------

        int debugMode = 0;
        Vector3d zref = get_Shape3D_Normal(shape);
        Vector3d yref = new Vector3d(0., 1., 0.);
        // in case of lateral mirrors
        if(Math.abs(zref.y)>0.6) yref = new Vector3d(0., 0., 1.);
        Vector3d xref = (yref.cross(zref)).normalized();

        if(debugMode>=1){
            System.out.format("rotate_AsComponent\n");
            System.out.format("bary  %s\n",toString(bary));
            System.out.format("X ref %s\n",toString(xref));
            System.out.format("Y ref %s\n",toString(yref));
            System.out.format("Z ref %s\n",toString(zref));
            System.out.format("Angle %s\n",toString(angle));
            System.out.format(" --> Z axis %7.3f \n", angle.z*zref.z + angle.y*yref.z + angle.x*xref.z);
            System.out.format(" --> Y axis %7.3f \n", angle.z*zref.y + angle.y*yref.y + angle.x*xref.y);
            System.out.format(" --> X axis %7.3f \n", angle.z*zref.x + angle.y*yref.x + angle.x*xref.x);
            show_Shape3D(shape, null, null);
            System.out.format("-----------------\n");
        } 

        translate_Shape3D(shape, bary.negated());

        // decompose each rotation along a ref axis (i.e. angle.x*xref) into three cartesian rotation in the lab system 
        shape.rotateZ(angle.z*zref.z + angle.y*yref.z + angle.x*xref.z);
        shape.rotateY(angle.z*zref.y + angle.y*yref.y + angle.x*xref.y);
        shape.rotateX(angle.z*zref.x + angle.y*yref.x + angle.x*xref.x);

        translate_Shape3D(shape, bary);
        if(debugMode>=1)show_Shape3D(shape, null, null);

    }


    //------------------------------
    public void rotate_AsRICH(Triangle3D tri, Vector3d angle) {
    //------------------------------

        translate_Triangle3D(tri, rich_misa_centre.negated());

        tri.rotateZ(angle.z);
        tri.rotateY(angle.y);
        tri.rotateX(angle.x);

        translate_Triangle3D(tri, rich_misa_centre);

    }


    //------------------------------
    public void rotate_AsRICH(Shape3D shape, Vector3d angle) {
    //------------------------------

        translate_Shape3D(shape, rich_misa_centre.negated());

        shape.rotateZ(angle.z);
        shape.rotateY(angle.y);
        shape.rotateX(angle.x);

        translate_Shape3D(shape, rich_misa_centre);

    }

    //------------------------------
    public void rotate_Shape3D(Shape3D shape, Vector3d angle) {
    //------------------------------

        Vector3d bary = get_Shape3D_Bary(shape);

        shape.rotateZ(angle.z);
        shape.rotateY(angle.y);
        shape.rotateX(angle.x);

        Vector3d shift = bary.minus( get_Shape3D_Bary(shape) );
        translate_Shape3D(shape, shift);

    }

     //------------------------------
     public Vector3d get_Shape3D_Center(Shape3D shape) { return toVector3d(shape.center()); }
     //------------------------------

     // ----------------
     public Vector3d get_CSGVol_Bary(CSG CSGVol) {
     // ----------------

        /*
        *   Avoid double counting of points  
        */
        int debugMode = 0;
        List<Vector3d> pts = new ArrayList<Vector3d>();
        if(debugMode>=1)System.out.format(" get_CSGVol_Bary %d \n", CSGVol.getPolygons().size());

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
    public boolean into_Shape3D(Line3d direction, Shape3D shape, int iface) {
    //------------------------------

        //System.out.format("CK %s   %s \n",toString(direction.diff().normalized()), toString(toLine3D(direction).toVector().asUnit())); 
        if(direction.diff().normalized().dot(get_Shape3D_Normal(shape, iface))<0)return true;
        return false;

    }


    //------------------------------
    public boolean out_from_Shape3D(Line3d direction, Shape3D shape, int iface) {
    //------------------------------

        if(direction.diff().normalized().dot(get_Shape3D_Normal(shape, iface))>0)return true;
        return false;

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


    //------------------------------
    public double get_Poly_Distance(Point3D point, Polygon pol) {
    //------------------------------

        Vector3D norm = toVector3D(get_Poly_Normal(pol));
        Point3D  pref = toPoint3D(pol.vertices.get(0).pos);
        Plane3D plane = new Plane3D(pref, norm);
        Line3D line = new Line3D(point, norm);
        Point3D inter = new Point3D(0.,0.,0.);;
        int itype = plane.intersection(line, inter); 
        if(itype>0){
            return point.distance(inter);
        }
        return 999.;

    }


    //------------------------------
    public boolean outfromPoly(Line3d direction, Polygon pol) {
    //------------------------------

        if(direction.diff().normalized().dot(get_Poly_Normal(pol).normalized())>0)return true;
        return false;

    }

    
    //------------------------------
    public boolean intoPoly(Line3d direction, Polygon pol) {
    //------------------------------

        if(direction.diff().normalized().dot(get_Poly_Normal(pol).normalized())<0)return true;
        return false;

    }

    
    //------------------------------
    public boolean insidePoly(Polygon pol, Vector3d pos) {
    //------------------------------

        if(pol.contains(pos)){
            Point3D point = toPoint3D(pos);
            //System.out.println(" UU "+get_Poly_Distance(point, pol));
            if(get_Poly_Distance(point, pol)<RICHConstants.RICH_MATCH_POLYDIST)return true;
        }
        return false;

    }


    //------------------------------
    public int getFTOFphase() {return FTOF_phase_corr;}
    //------------------------------

    //------------------------------
    public void setFTOFphase(int phase) { FTOF_phase_corr = phase; }
    //------------------------------


    // ----------------
    public String get_PlaneMirrorSide(RICHComponent compo) {
    // ----------------

        int debugMode = 0;

        Vector3d front   = new Vector3d(-0.42,   0.00,   0.91);
        Vector3d left    = new Vector3d(-0.50,  -0.87,   0.00);
        Vector3d right   = new Vector3d(-0.50,   0.87,   0.00);
        Vector3d bottom  = new Vector3d(-1.00,   0.00,   0.00);

        Vector3d bary = get_CSGVol_Bary( compo.get_CSGVol() );
        if(debugMode>=1)System.out.format(" compo bary %s \n", toString(bary));

        for (Polygon pol: compo.get_CSGVol().getPolygons()){

            if(debugMode>=1)System.out.format("Test front %7.3f  left %7.3f  right %7.3f  bot %7.3f \n",
                     get_Poly_Normal(pol).angle(front), get_Poly_Normal(pol).angle(left),
                     get_Poly_Normal(pol).angle(right), get_Poly_Normal(pol).angle(bottom));

            if(get_Poly_Normal(pol).angle(front)<5.e-3){
                 if(bary.x > -100){
                     return new String("mirror_front_B1");
                 }else{
                     return new String("mirror_front_B2");
                 }
            }
            if(get_Poly_Normal(pol).angle(left)<5.e-3){
                 if(bary.x > -100){
                     return new String("mirror_left_L1");
                 }else{
                     return new String("mirror_left_L2");
                 }
            }
            if(get_Poly_Normal(pol).angle(right)<5.e-3){
                 if(bary.x > -100){
                     return new String("mirror_right_L1");
                 }else{
                     return new String("mirror_right_L2");
                 }
            }
            if(get_Poly_Normal(pol).angle(bottom)<5.e-3){
                 return new String("mirror_bottom");
            }
            
        }
        return new String("none");
    }


    // ----------------
    public void merge_Shape3D(Shape3D shape, Shape3D other) {
    // ----------------

        for(int ifa=0; ifa<other.size(); ifa++)shape.addFace( other.face(ifa) );

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
        int ilay=11;
        Shape3D mirror = opticlayers.get(ilay).get_Global_Plane();

        List<Point3D> inters = new ArrayList<Point3D>();
        int nint = mirror.intersection(ray, inters);
        if(debugMode>=1)  System.out.format("RICHTool::find_intersection with Mirror %d (%d, %d): %d \n",8,nint);

        if(nint==1)return toVector3d(inters.get(0));
        return null;

    }

    // ----------------
    public Vector3d find_intersection_MAPMT(Line3d ray){
    // ----------------

        int debugMode = 0;
        int ilay=get_LayerNumber("mapmts");

        RICHIntersection inter = find_intersection_Shape3D(ray, ilay, -1, 0, 1);

        if(inter!=null){
            if(debugMode>=1)  System.out.format("RICHTool::find_intersection with MAPMT (%d, %d): %s\n",
                 inter.get_layer(), inter.get_component(), toString(inter.get_pos()));
            return inter.get_pos();
        }

        return null;
    }



    // ----------------
    public RICHIntersection find_intersection_Layer(Line3d ray, int ilay, int ico, int exit, int post){
    // ----------------
        /*
        * Search for the intersection points of ray with the RICH component  
        * exit=0 Take the first (in z) with track pointing inside as Entrance
        * exit=1 Take the last (in z) with track pointing outside as Exit
        * post=0 Take the intersection in backward direction
        * post=1 Take the intersection in forward direction
        */
        //ATT:aggiungere min path

        int debugMode = 0;

        if(ilay<0 || ilay>NLAY) return null;

        RICHLayer layer = opticlayers.get(ilay);
        Shape3D plane = null;
        if(ico>0 || !layer.is_aerogel()){
            if(debugMode>=1)System.out.format("Take global plane \n");
            plane = layer.get_Global_Plane();
        }else{
            if(debugMode>=1)System.out.format("Take compo surf \n");
            plane = layer.get_Component_Surf();
        }

        /*
        *  Take all the intersection with Layer
        */
        RICHIntersection intersection = null;
        List<Point3D> inters = new ArrayList<Point3D>();
        List<Integer> ifaces = new ArrayList<Integer>();

        int nint = plane.intersection_with_faces(toLine3D(ray), inters, ifaces);
        if(debugMode>=1) {
            System.out.format("RICHTool::find_intersection with Shape3D %d (%d, %d): %d \n",ilay,exit,post,nint);
            for (int ii=0; ii<nint; ii++)System.out.format(" %3d %4d \n",ii,ifaces.get(ii));
        }

        float vers = (float) ray.diff().dot(Vector3d.Z_ONE);
        Vector3d point = new Vector3d(0,0,0);
        if((exit==0 && vers>0) || (exit==1 && vers<0))point.z = 999.;
        
        /*
        *  Select the best intersection 
        */
        for (int ii=0; ii<nint; ii++){

            Vector3d G4inter = toVector3d(inters.get(ii));
            int iface = ifaces.get(ii);
            int ifacompo = layer.get_Component_Index(iface);
            if(debugMode>=1)  System.out.format("     Find intersection %3d  %3d  %3d: %s  check poi %7.2f   ori %7.2f   vers %7.2f\n",
                                      ii,iface,ifacompo,toString(G4inter),point.z,ray.origin().z, vers);

            if(post==1){
                if(vers>0 && G4inter.z<ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
                if(vers<0 && G4inter.z>ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
            }else{
                if(vers>0 && G4inter.z>ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
                if(vers<0 && G4inter.z<ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
            }
            if(exit==1){
                if(into_Shape3D(ray, plane, iface)){if(debugMode>=1)System.out.format(" Wrong direction\n"); continue;}
            }else{
                if(out_from_Shape3D(ray, plane, iface)){if(debugMode>=1)System.out.format(" Wrong direction\n"); continue;}
            }

            Vector3d norm = get_Shape3D_Normal(plane, iface);
            if(debugMode>=1 && ilay==0)System.out.format(" AERONormal %s \n",toString(norm, 4));

            if(exit==0 && into_Shape3D(ray, plane, iface)){
                // take the first (in z) entrance point
                if(debugMode>=1)  System.out.format("     --> ok as moving into Shape %7.2f %7.2f \n", G4inter.z, point.z);
                if((vers>0 && G4inter.z<point.z) || (vers<0 && G4inter.z>point.z)){
                    if(debugMode>=1)  System.out.format("         --> taken \n");
                    point = G4inter;
                    int icompo = ico;
                    if(icompo==-1)icompo=ifacompo;
                        
                    if(is_Spherical_Mirror(ilay)){

                        Sphere3D sphere = layer.get_Tracking_Sphere();
                        List<Point3D> crosses = new ArrayList<Point3D>();
                        int ncross = sphere.intersection(toLine3D(ray), crosses);
                        for (int ic=0; ic<ncross; ic++){
                            Vector3d new_point = toVector3d(crosses.get(ic));
                            if(debugMode>=1)System.out.format(" cross with sphere %7.2f %7.2f %7.2f \n",new_point.x,new_point.y,new_point.z);
                            if(new_point.distance(G4inter)<RICHConstants.PHOTON_DISTMIN_SPHERE){

                                Vector3d new_norm = toVector3d( sphere.getNormal(new_point.x, new_point.y, new_point.z) );
                                intersection = new RICHIntersection(ilay, 0, 0, exit, new_point, new_norm);
                             }
                        }
                                
                    }else{
                        intersection = new RICHIntersection(ilay, icompo, iface, exit, point, norm);
                        if(layer.is_aerogel()){ 
                            if(icompo>=0) intersection.set_nout(get_Component(ilay,icompo).get_index());
                            if(debugMode>=1)System.out.format("     -->  save aerogel entrance for ilay %4d icompo %4d (%4d %4d) nindx %7.5f \n",
                                             ilay,icompo,ico,ifacompo,intersection.get_nout());
                        }
                    }
                }
            }

            if(exit==1 && out_from_Shape3D(ray, plane, iface)){
                // take the last (in z) exiting point
                if(debugMode>=1)  System.out.format("     --> ok as moving out of Poly %7.2f %7.2f \n", G4inter.z, point.z);
                if((vers>0 && G4inter.z>point.z) || (vers<0 && G4inter.z<point.z)){
                    if(debugMode>=1)  System.out.format("         --> taken \n");
                    point = G4inter;
                    int icompo = ico;
                    if(icompo==-1)icompo=ifacompo;
                    intersection = new RICHIntersection(ilay, icompo, iface, exit, point, norm);
                    if(layer.is_aerogel()){
                        if(icompo>=0)intersection.set_nin(get_Component(ilay,icompo).get_index());
                        if(debugMode>=1)System.out.format("     -->  save aerogel exit for ilay %4d icompo %4d (%4d %4d) nindx %7.5f \n",
                                              ilay,icompo, ico, ifacompo, intersection.get_nin());
                    }
                }
            }

        }

        /*
        *  Correct for non planar shape
        */
        if(layer.is_aerogel()){
            int icompo = intersection.get_component();
            double radius = layer.get(icompo).get_radius();
            Vector3d center = layer.get_CompoCenter(icompo, toVector3D(intersection.get_normal()));
            Vector3d nnorm = intersection.get_normal().times(radius);
            Vector3d rnorm = new Vector3d (nnorm.x, -nnorm.y, nnorm.z);
            Vector3d ncenter = center.plus(rnorm);
            if(debugMode>=1)System.out.format(" aerogel sphere %s %s r %7.3f --> %s \n",toString(center), toString(rnorm), radius, toString(ncenter));
            Sphere3D sphere = new Sphere3D( ncenter.x, ncenter.y, ncenter.z, radius);
            RICHIntersection new_inter = null;

            List<Point3D> crosses = new ArrayList<Point3D>();
            int ncross = sphere.intersection(toLine3D(ray), crosses);
            for (int ic=0; ic<ncross; ic++){
                Vector3d new_point = toVector3d(crosses.get(ic));
                if(debugMode>=1)System.out.format(" cross with sphere %7.2f %7.2f %7.2f \n",new_point.x,new_point.y,new_point.z);
                if(new_point.distance(intersection.get_pos())<RICHConstants.PHOTON_DISTMIN_SPHERE){

                    Vector3d new_norm = toVector3d( sphere.getNormal(new_point.x, new_point.y, new_point.z) ).negated().normalized();
                    new_inter = new RICHIntersection(ilay, icompo, 0, exit, new_point, new_norm);

                }
            }
            if(new_inter!=null){
                if(debugMode>=1){
                    System.out.format(" reset intersection \n");
                    System.out.format(" pos  %s --> %s \n", toString(intersection.get_pos()), toString(new_inter.get_pos()));
                    System.out.format(" norm %s --> %s \n", toString(intersection.get_normal()), toString(new_inter.get_normal()));
                }
                intersection.set_pos(new_inter.get_pos());
                intersection.set_normal(new_inter.get_normal());
            }
        }

        if(debugMode>=3){
            if(exit==0)System.out.println(" entrance point "+intersection.get_pos()+"   normal "+intersection.get_normal());
            if(exit==1)System.out.println(" exit     point "+intersection.get_pos()+"   normal "+intersection.get_normal());
        }

        return intersection;

    }

    // ----------------
    public RICHIntersection find_intersection_Shape3D(Line3d ray, int ilay, int ico, int exit, int post){
    // ----------------
        /*
        * Search for the intersection points of ray with the RICH component  
        * exit=0 Take the first (in z) with track pointing inside as Entrance
        * exit=1 Take the last (in z) with track pointing outside as Exit
        * post=0 Take the intersection in backward direction
        * post=1 Take the intersection in forward direction
        */
        //ATT:aggiungere min path

        int debugMode = 0;

        if(ilay<0 || ilay>NLAY) return null;

        RICHLayer layer = opticlayers.get(ilay);
        Shape3D plane = null;
        if(ico>0 || !layer.is_aerogel()){
            if(debugMode>=1)System.out.format("Take global plane \n");
            plane = layer.get_Global_Plane();
        }else{
            if(debugMode>=1)System.out.format("Take compo surf \n");
            plane = layer.get_Component_Surf();
        }

        RICHIntersection intersection = null;
        List<Point3D> inters = new ArrayList<Point3D>();
        List<Integer> ifaces = new ArrayList<Integer>();

        int nint = plane.intersection_with_faces(toLine3D(ray), inters, ifaces);
        if(debugMode>=1) {
            System.out.format("RICHTool::find_intersection with Shape3D %d (%d, %d): %d \n",ilay,exit,post,nint);
            for (int ii=0; ii<nint; ii++)System.out.format(" %3d %4d \n",ii,ifaces.get(ii));
        }

        float vers = (float) ray.diff().dot(Vector3d.Z_ONE);
        Vector3d point = new Vector3d(0,0,0);
        if((exit==0 && vers>0) || (exit==1 && vers<0))point.z = 999.;
        
        for (int ii=0; ii<nint; ii++){

            Vector3d G4inter = toVector3d(inters.get(ii));
            int iface = ifaces.get(ii);
            int ifacompo = layer.get_Component_Index(iface);
            if(debugMode>=1)  System.out.format("     Find intersection %3d  %3d  %3d: %s  check poi %7.2f   ori %7.2f   vers %7.2f\n",
                                      ii,iface,ifacompo,toString(G4inter),point.z,ray.origin().z, vers);

            if(post==1){
                if(vers>0 && G4inter.z<ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
                if(vers<0 && G4inter.z>ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
            }else{
                if(vers>0 && G4inter.z>ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
                if(vers<0 && G4inter.z<ray.origin().z){if(debugMode>=1)System.out.format(" Wrong progression \n"); continue;}
            }
            if(exit==1){
                if(into_Shape3D(ray, plane, iface)){if(debugMode>=1)System.out.format(" Wrong direction\n"); continue;}
            }else{
                if(out_from_Shape3D(ray, plane, iface)){if(debugMode>=1)System.out.format(" Wrong direction\n"); continue;}
            }

            Vector3d norm = get_Shape3D_Normal(plane, iface);
            if(debugMode>=1 && ilay==0)System.out.format(" AERONormal %s \n",toString(norm, 4));

            if(exit==0 && into_Shape3D(ray, plane, iface)){
                // take the first (in z) entrance point
                if(debugMode>=1)  System.out.format("     --> ok as moving into Shape %7.2f %7.2f \n", G4inter.z, point.z);
                if((vers>0 && G4inter.z<point.z) || (vers<0 && G4inter.z>point.z)){
                    if(debugMode>=1)  System.out.format("         --> taken \n");
                    point = G4inter;
                    int icompo = ico;
                    if(icompo==-1)icompo=ifacompo;
                        
                    if(is_Spherical_Mirror(ilay)){

                        Sphere3D sphere = layer.get_Tracking_Sphere();
                        List<Point3D> crosses = new ArrayList<Point3D>();
                        int ncross = sphere.intersection(toLine3D(ray), crosses);
                        for (int ic=0; ic<ncross; ic++){
                            Vector3d new_point = toVector3d(crosses.get(ic));
                            if(debugMode>=1)System.out.format(" cross with sphere %7.2f %7.2f %7.2f \n",new_point.x,new_point.y,new_point.z);
                            if(new_point.distance(G4inter)<RICHConstants.PHOTON_DISTMIN_SPHERE){

                                Vector3d new_norm = toVector3d( sphere.getNormal(new_point.x, new_point.y, new_point.z) );
                                intersection = new RICHIntersection(ilay, 0, 0, exit, new_point, new_norm);
                             }
                        }
                                
                    }else{
                        intersection = new RICHIntersection(ilay, icompo, iface, exit, point, norm);
                        if(layer.is_aerogel()){ 
                            if(icompo>=0) intersection.set_nout(get_Component(ilay,icompo).get_index());
                            if(debugMode>=1)System.out.format("     -->  save aerogel entrance for ilay %4d icompo %4d (%4d %4d) nindx %7.5f \n",
                                             ilay,icompo,ico,ifacompo,intersection.get_nout());
                        }
                    }
                }
            }

            if(exit==1 && out_from_Shape3D(ray, plane, iface)){
                // take the last (in z) exiting point
                if(debugMode>=1)  System.out.format("     --> ok as moving out of Poly %7.2f %7.2f \n", G4inter.z, point.z);
                if((vers>0 && G4inter.z>point.z) || (vers<0 && G4inter.z<point.z)){
                    if(debugMode>=1)  System.out.format("         --> taken \n");
                    point = G4inter;
                    int icompo = ico;
                    if(icompo==-1)icompo=ifacompo;
                    intersection = new RICHIntersection(ilay, icompo, iface, exit, point, norm);
                    if(layer.is_aerogel()){
                        if(icompo>=0)intersection.set_nin(get_Component(ilay,icompo).get_index());
                        if(debugMode>=1)System.out.format("     -->  save aerogel exit for ilay %4d icompo %4d (%4d %4d) nindx %7.5f \n",
                                              ilay,icompo, ico, ifacompo, intersection.get_nin());
                    }
                }
            }

        }

        if(debugMode>=3){
            if(exit==0)System.out.println(" entrance point "+intersection.get_pos()+"   normal "+intersection.get_normal());
            if(exit==1)System.out.println(" exit     point "+intersection.get_pos()+"   normal "+intersection.get_normal());
        }

        return intersection;

    }


    // ----------------
    public boolean is_Spherical_Mirror (int ilay){
    // ----------------

        if(opticlayers.get(ilay).get_Name().equals("mirror_sphere"))return true;
        return false;  

    }


    // ----------------
    public Vector3d Reflection(Vector3d vector1, Vector3d normal) {
    // ----------------

        int debugMode = 0;
        Vector3d vin = vector1.normalized();
        Vector3d vnorm = normal.normalized();

        double cosI  =  vin.dot(vnorm); 
        if(debugMode>=1)System.out.println("--Parametro angolo "+ cosI);
        if (cosI > 0) {
            vnorm.set(vnorm.negated());  
        }

        Vector3d vout = vin.minus(vnorm.times(2*(vin.dot(vnorm))));

        if(debugMode>=1){
            System.out.println("La normale sullo specchio vale " + normal);
            System.out.println("The reflected versor is " + vout.normalized());
        }

        return vout.normalized();
    }


    // ----------------
    public Vector3d Transmission(Vector3d vector1, Vector3d normal, double n_1, double n_2) {
    // ----------------

        int debugMode = 0;
        double rn = n_1 / n_2;

        Vector3d vin = vector1.normalized();
        Vector3d vnorm = normal.normalized();
        double cosI  =  vin.dot(vnorm); 
        if (cosI > 0) {
            vnorm.set(vnorm.negated());
        }
        if(debugMode>=1)System.out.format("Vector in %s  vnorm %s cosI %7.3f \n ",toString(vin),toString(vnorm),cosI);

        Vector3d crossP = vnorm.cross(vin);
        Vector3d McrossP = vnorm.negated().cross(vin);

        double sqroot = Math.sqrt(1 - rn*rn* crossP.dot(crossP));

        Vector3d secondquantity = new Vector3d(vnorm.times(sqroot));
        Vector3d firstquantity = new Vector3d(vnorm.cross(McrossP));
        Vector3d vout = firstquantity.times(rn);

        if(debugMode>=1){
            System.out.format(" crossP %s \n", toString(crossP));
            System.out.format("McrossP %s \n", toString(McrossP)); 
            System.out.format("Variables %7.3f %7.3f \n",crossP.dot(crossP), sqroot); 
            System.out.format(" first  %s \n", toString(firstquantity));  
            System.out.format(" secon  %s \n", toString(secondquantity));  
            System.out.format(" vout   %s \n", toString(vout)); 
        }


        vout.sub(secondquantity);

        if(debugMode>=1)System.out.format("The transmitted vector is %7.3f %7.3f %7.3f for rn = %7.3f (%7.3f %7.3f)\n",vout.x,vout.y,vout.z,rn,n_1,n_2);
        return vout.normalized();

    }

    // ----------------
    public Vector3d Transmission2(Vector3d vector1, Vector3d normal, double n_1, double n_2) {
    // ----------------

        int debugMode = 0;
        double rn = n_1 / n_2;

        Vector3d vin = vector1.normalized();
        Vector3d vnorm = normal.normalized();

        if(vnorm.dot(vin)<0){
            vnorm.set(vnorm.negated());
        }

        Vector3d vrot = (vnorm.cross(vin)).normalized();
 
        double angi = Math.acos(vin.dot(vnorm)) ;
        double ango = Math.asin( rn * Math.sin(angi));

        Quaternion q = new Quaternion(ango, vrot);

        Vector3d vout = q.rotate(vnorm);

        if(debugMode>=1){
            System.out.format(" vin   %s \n", toString(vin));
            System.out.format(" vnorm %s \n", toString(vnorm)); 
            System.out.format(" angles %7.3f %7.3f \n",angi*57.3, ango*57.3);
            System.out.format(" vout  %s \n", toString(vout)); 
        }

        return vout;

    }
 
    // ----------------
    public RICHRay OpticalRotation(RICHRay rayin, RICHIntersection intersection) {
    // ----------------

        int debugMode = 0;
        Vector3d vori = rayin.origin();
        Vector3d inVersor = rayin.diff().normalized();
        Vector3d newVersor = new Vector3d(0, 0, 0);
        RICHRay rayout = null;
        int type = 0;
 
        RICHComponent component = opticlayers.get(intersection.get_layer()).get(intersection.get_component());

        if(component.is_optical()==true){
                
            if(debugMode>=1)System.out.format("Ray rotation at Optical STL %3d %3d  xyz %6.2f %6.2f %6.2f \n", intersection.get_layer(), intersection.get_component(), vori.x, vori.y, vori.z);
            //Vector3d vnorm = richfactory.GetNormal_Stl(ivol,vori);
            Vector3d vnorm = intersection.get_normal();
            if(vnorm != null ){
                if(component.is_mirror()==true){
             
                    newVersor = Reflection(inVersor, vnorm );
                    type=1;
                    if(debugMode>=1)System.out.format(" Reflection at mirror surface norm %7.3f %7.3f %7.3f \n", 
                                  vnorm.x, vnorm.y, vnorm.z);

                }else{

                    newVersor = Transmission(inVersor, vnorm, intersection.get_nin(), intersection.get_nout());
                    type=2;
                    if(debugMode>=1){
                        System.out.format(" Refraction at surface boundary norm %7.3f %7.3f %7.3f \n",
                                  vnorm.x, vnorm.y, vnorm.z);
                        System.out.format(" norm in %s %7.4f \n",toString(vnorm), vnorm.angle(Vector3d.Z_ONE));
                        System.out.format(" vers in %s %7.4f \n",toString(inVersor), inVersor.angle(Vector3d.Z_ONE));
                        System.out.format(" vers ou %s %7.4f \n",toString(newVersor), newVersor.angle(Vector3d.Z_ONE));
                    }
                }
            }

            if(debugMode>=1)System.out.format(" Versor in %7.3f %7.3f %7.3f   --> out %7.3f %7.3f %7.3f \n", 
                            inVersor.x, inVersor.y, inVersor.z, newVersor.x, newVersor.y, newVersor.z);
        }

        rayout = new RICHRay(vori, vori.plus(newVersor.times(200)));
        rayout.set_type(type);
        return rayout;

    }


    // ----------------
    public ArrayList<RICHRay> RayTrace(Vector3d emission, int orilay, int orico, Vector3d vlab) {
    // ---------------- 
    // return the hit position on the PMT plane of a photon emitted at emission with direction vlab

        int debugMode = 0;
        boolean detected = false;
        boolean lost = false;
        ArrayList<RICHRay> raytracks = new ArrayList<RICHRay>();
        int n_front_refl = 0;

        RICHRay lastray = new RICHRay(emission, emission.plus(vlab.times(200)));
        if(debugMode>=1) {
            System.out.format(" --------------------------- \n");
            System.out.format("Raytrace photon ori %6.2f %6.2f %6.2f  olay %3d  oco %3d  dir %6.2f %6.2f %6.2f \n",emission.x,emission.y,emission.z,orilay,orico,vlab.x,vlab.y,vlab.z); 
            System.out.format(" --------------------------- \n");
        }

        RICHIntersection first_intersection = find_intersection_Shape3D(lastray, orilay, orico, 1, 1);
        if(first_intersection==null){
            lost=true;
            return null;
        }  
        if(debugMode>=1){
            System.out.format(" first inter : ");
            first_intersection.showIntersection();
        }

        RICHRay oriray = new RICHRay(emission, first_intersection.get_pos());
        oriray.set_refind(get_Component(orilay,orico).get_index());
        raytracks.add(oriray);

        Vector3d new_pos = first_intersection.get_pos();
        RICHRay rayin = new RICHRay(new_pos, new_pos.plus(oriray.diff().times(200)));

        lastray = OpticalRotation(rayin, first_intersection);
        lastray.set_refind(RICHConstants.RICH_AIR_INDEX);
        RICHIntersection last_intersection = first_intersection;

        if(debugMode>=1){
            System.out.format(" add first ray : ");
            oriray.showRay();
            System.out.format(" get rotated ray : ");
            lastray.showRay();
        }

        int jj=1;
        while( detected == false && lost == false && raytracks.size()<10){

            Vector3d last_ori  = lastray.origin();
            Vector3d new_hit = null;
            RICHIntersection new_intersection = null;

            if(debugMode>=1)System.out.format(" ray-tracking step %d \n",jj);
            //int iSTL = 0;
            //double mindist = 999999.0;

            // next intersection starting from aerogel
            if(last_intersection.get_layer()<4){
  
                // planar mirrors
                RICHIntersection test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_bottom"), -1, 0, 1);
                if(test_intersection==null)test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_left_L1"), -1, 0, 1);
                if(test_intersection==null)test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_right_L1"), -1, 0, 1);
                if(test_intersection==null)test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_left_L2"), -1, 0, 1);
                if(test_intersection==null)test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_right_L2"), -1, 0, 1);
                if(test_intersection!=null){
                    if(debugMode>=1){
                        System.out.format(" test bottom (%7.2f, %7.2f) : ",last_ori.z, test_intersection.get_pos().distance(last_ori));
                        test_intersection.showIntersection();
                    }
                    if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection;
                }else{
                    if(debugMode>=1)System.out.format(" no lateral mirror intersection \n");
                }

                // shperical mirrors
                if(lastray.diff().dot(Vector3d.Z_ONE)>0){
                    test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_sphere"), -1, 0, 1);
                    
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test sphere (%7.2f, %7.2f) : ",last_ori.z, test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            if(new_intersection==null || (new_intersection!=null && test_intersection.get_pos().z<new_intersection.get_pos().z)) {
                                new_intersection = test_intersection;
                            }
                        }
                    }else{
                        if(debugMode>=1)System.out.format(" no sphere intersection \n");
                    }

                    //Vector3d test_hit = find_PMT_hit(lastray);
                    RICHIntersection pmt_inter = find_intersection_Shape3D(lastray, get_LayerNumber("mapmts"), -1, 0, 1);
                    if(pmt_inter!=null) {
                        Vector3d test_hit = pmt_inter.get_pos(); 
                        if(test_hit.distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING){
                            new_hit=test_hit;
                            if(debugMode>=1)System.out.format(" test PMT : Hit %7.2f %7.2f %7.2f \n",new_hit.x,new_hit.y,new_hit.z);
                        }else{
                            if(debugMode>=1)System.out.format(" too far PMT plane intersection \n");
                        }
                    }else{
                        if(debugMode>=1)System.out.format(" no PMT plane intersection \n");
                    }
                }else{
                    test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_front_B1"), -1, 0, 1);
                    if(test_intersection==null)test_intersection = find_intersection_Shape3D(lastray, get_LayerNumber("mirror_front_B2"), -1, 0, 1);
                    if(test_intersection!=null){
                        if(debugMode>=1){
                            System.out.format(" test front (%7.2f, %7.2f) : ",last_ori.z, test_intersection.get_pos().distance(last_ori));
                            test_intersection.showIntersection();
                        }
                        if(test_intersection.get_pos().distance(last_ori)>RICHConstants.PHOTON_DISTMIN_TRACING)new_intersection = test_intersection; n_front_refl++;
                    }else{
                        if(debugMode>=1)System.out.format(" no front mirror intersection \n");
                    }
                }

                if((new_hit==null && new_intersection==null) || (n_front_refl>1)) {
                    lost = true;
                    return null;
                }
            }


            int end_trace = 0;
            if(new_hit!=null && new_intersection==null) end_trace=1;
            if(new_hit!=null && new_intersection!=null && new_hit.distance(last_ori) < new_intersection.get_pos().distance(last_ori)) end_trace=1;

            if(end_trace==1){
                detected = true;
                if(debugMode>=1) System.out.format(" found PMT hit %6.2f %6.2f %6.2f  dist %6.2f \n", new_hit.x, new_hit.y, new_hit.z, new_hit.distance(last_ori));

                RICHRay newray = new RICHRay(last_ori, new_hit);
                newray.set_type(lastray.get_type());
                newray.set_refind((float) RICHConstants.RICH_AIR_INDEX);
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
                Vector3d new_end = new_intersection.get_pos().plus(newray.diff().times(200));
                rayin = new RICHRay(new_intersection.get_pos(), new_end);
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
        if(detected==true)return raytracks;
        return null;
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
        if(iphase==0 && interval > RICHConstants.SHOW_PROGRESS_INTERVAL) {

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

    // ----------------
    public Vector3d find_PMT_hit(RICHRay ray){
    // ----------------

        int debugMode = 0;
        Vector3d pmt_hit = new Vector3d(0., 0., 999.);
        for (int ipmt=1; ipmt<=391; ipmt++){

            int ii= 0;
            //for ( Vector3d intersection: richfactory.GetPMT(ipmt).toCSG().getIntersections(ray)) {
            for ( Vector3d intersection: richfactory.GetPhotocatode(ipmt).toCSG().getIntersections(ray)) {

                if(debugMode>=1)  System.out.format(" PMT %d point %d | %8.2f %8.2f %8.2f  dist %8.2f \n", ipmt, ii, 
                                         intersection.x, intersection.y, intersection.z, intersection.distance(ray.origin()));
                ii++;
                if(intersection.z<pmt_hit.z){
                    pmt_hit = intersection;
                    if(debugMode>=1)  System.out.format("    --> hit point \n");
                }
            }
        }

        if(pmt_hit.z==999.)return null;
        return pmt_hit;

    }
}
