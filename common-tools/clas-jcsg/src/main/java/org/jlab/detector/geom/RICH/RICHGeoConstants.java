package org.jlab.detector.geom.RICH;

import org.jlab.geom.prim.Vector3D;
import org.jlab.geom.prim.Point3D;

public class RICHGeoConstants {

    // -----------------
    public RICHGeoConstants() {
    // -----------------
    }

    // RICH Reconstruction static constants

    // -----------------
    // Static
    // -----------------

    public static final int    NSEC                       =   6;        // number of sectors of RICH
    public static final int    NLAY                       =   13;       // number of layers of RICH
    public static final int    NROW                       =   24;       // number of PMT rows in RICH                 
    public static final int    NCOL                       =   56;       // number of PMT coloumns in RICH
    public static final int    NPMT                       =   391;      // number of PMTs in RICH
    public static final int    NPIX                       =   64;       // number of Pixels in one PMT
    public static final int    NCOMPO                     =   10;       // max number of components per layer

    public static final double MRAD                       =   1000.;
    public static final double RAD                        =   180./Math.PI;
    public static final double CM                         =   0.1;

    public static final double RICH_AEROGEL_INDEX         =   1.05;      // Aerogel refracting index (not used)
    public static final double AERO_REF_THICKNESS         =   20.;       // Aerogel thickness in mm (to define emission point)
    public static final double AERO_REF_DIMENSION         =   200.;      // Aerogel lateral size in mm 
    public static final double AERO_CUT_DIMENSION         =   165.;      // Aerogel heigth for the first two shaped tiles
    public static final double RICH_AIR_INDEX             =   1.000273;  // AIR n used in Mirazita's code (da CCDB)

    public static final double RICH_TABLE_FROM_FILE       =   0;         // read rich module configuration
    public static final double ALIGN_TABLE_FROM_FILE      =   0;         // read alignment calibration values from local txt files
    public static final double AERO_OPTICS_FROM_FILE      =   0;         // read aerogel nominal optics values from local txt files
    public static final double GEOPAR_FROM_FILE           =   0;         // read geometry parameters from file


    public static final int    NALIMAX                    =   126;       // maximum number of potential alignments (with all aerogel tiles)
    public static final int    NALIGN                     =   24;        // number of components with indipendent alignment


    public static final   Vector3D vfront   = new Vector3D(-0.42,   0.00,   0.91);
    public static final   Vector3D vleft    = new Vector3D(-0.50,  -0.87,   0.00);
    public static final   Vector3D vright   = new Vector3D(-0.50,   0.87,   0.00);
    public static final   Vector3D vbottom  = new Vector3D(-1.00,   0.00,   0.00);
    public static final   Vector3D vback    = new Vector3D( 0.42,   0.00,  -0.91);
    public static final   Vector3D vsphere  = new Vector3D( 0.76,   0.00,  -0.65);


    public static final int    anode_map[] = { 60,58,59,57,52,50,51,49,44,42,43,41,36,34,35,
                                               33,28,26,27,25,20,18,19,17,12,10,11,9,4,2,3,1,5,7,6,8,13,15,14,16,21,
                                               23,22,24,29,31,30,32,37,39,38,40,45,47,46,48,53,55,54,56,61,63,62,64};

    public static final int    NWALK       = 4;                          // number of time walk parameters
    public static final int    NAERLAY     = 4;                          // number of aerogel layers
    public static final int    NAERMAX     = 31;                         // maximum number of tiles per aerogel layer
    public static final int    NAERCO[]    = { 16,22,NAERMAX,NAERMAX };  // number of tiles per aerogel layer
    //public static final int    ATHICK[]    = { 10, 10, 15, 15};          // thickness per aerogel layer

    public static final double ROTANG[]    = {180./RAD,120./RAD,60./RAD,0./RAD,-60./RAD,-120./RAD}; // rotation angle between RICH geo and sector (degree)

    public static final int    tile2pmt[][] = {{   1,   2,   3},  
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


     /*public Point3D[] get_NominalPlane(int ila, int ico){

         Point3D vtx[] = new Point3D[4];
         if(ila==RICHLayerType.AEROGEL_2CM_B1.id() && ico==0){
            vtx[0] = new Point3D(-40.015, 10.697, 589.906);
            vtx[1] = new Point3D(-40.015, -10.697, 589.906);
            vtx[2] = new Point3D(-110.821, 51.573, 556.901);
            vtx[3] = new Point3D(-110.821, -51.573, 556.901);
        }
        return vtx[];

     }*/

}
