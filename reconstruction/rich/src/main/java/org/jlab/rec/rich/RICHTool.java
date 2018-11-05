/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.rich;

/**
 *
 * @author mcontalb
 */
public class RICHTool{

    //------------------------------
    public void RICHTool(){
    //------------------------------

    } 

    public int debugMode = 0;

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


    private static int NROW=25;
    private static int NCOL=56;
    private static int NPMT=397;

    private static int pfirst[] = {1, 7,14,22,31,41,52,64,77, 91,106,122,139,157,176,196,217,239,262,286,311,337,364,392,395};
    private static int plast[]  = {6,13,21,30,40,51,63,76,90,105,121,138,156,175,195,216,238,261,285,310,336,363,391,394,397};

    private int xp[] = new int[NPMT]; // X coordinate of pixel 1 of each mapmt
    private int yp[] = new int[NPMT]; // Y coordinate of pixel 1 of each mapmt
    private int nxp[] = new int[NPMT]; // X coordinate of pixel 1 of each mapmt
    private int nyp[] = new int[NPMT]; // Y coordinate of pixel 1 of each mapmt


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
    public int Globalidx(int pmt, int anode) {
    //------------------------------

        // return global idx on the RICH plane
	//System.out.println("gdX pmt "+pmt+" anode "+anode+" --> "+nxp[pmt-1]+" "+Anode2idx(anode));
	if(pmt>391)return nxp[pmt-1]-(Anode2idx(anode)-1);
        return nxp[pmt-1]+(Anode2idx(anode)-1);
    }


    //------------------------------
    public int Globalidy(int pmt, int anode) {
    //------------------------------

        // return global idy on the RICH plane
	//System.out.println("gdY pmt "+pmt+" anode "+anode+" -->  "+nyp[pmt-1]+" "+Anode2idy(anode));
	if(pmt>391)return nyp[pmt-1]-(Anode2idy(anode)-1);
        return nyp[pmt-1]+(Anode2idy(anode)-1);
    }


    //------------------------------
    public void NewGeo(){
    //------------------------------

	if(debugMode>=1) System.out.println("Sto in newGeo");
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
    public void InitGeo(){
    //------------------------------
        int i,ccrow,ccol;
        int[] yrow = new int[NROW];    // set of yp
        int[] xcol = new int[NCOL];    // set of xp

	yrow[0]=16; //arbitrary
        for(i=1;i<NROW;i++) yrow[i]=yrow[i-1]+8;

	xcol[0]=9; //arbitrary
        for(i=1;i<NCOL;i++) xcol[i]=xcol[i-1]+4;

	if(debugMode>=1) System.out.println("Sto in initGeo");
        for(int crow=0; crow<NROW; crow++){ // loop on rows

            for(int ipmt=pfirst[crow];ipmt<=plast[crow];ipmt++){ // loop on pmts

                // first we do the pixel 1 coordinate
		if(crow<23){
	           ccrow = crow;
                   ccol = (NCOL-23+crow)-2*(ipmt-pfirst[crow]); // current half-pmt column
		}else{
	           ccol = 6+2*(ipmt-pfirst[crow]);  // tracking has opposite PMT orientation
	           ccrow = crow-20;
	           //ccol = 24+2*(ipmt-pfirst[crow]);  // tracking has opposite PMT orientation
	           //ccrow = 13+crow;
		}

                yp[ipmt-1]= yrow[ccrow];
                xp[ipmt-1]= xcol[ccol];
		if(debugMode>=1) System.out.println("PMT "+ipmt+" COL "+ccol+" "+xcol[ccol]+" ROW "+(ccrow+1)+" "+yrow[ccrow]);

	    }
	}
     }


      /*
     //------------------------------
     void  GenCoord(int x1, int y1){
     //------------------------------

        int j;
        int rw; // row
        int cm; // column
        int x[64];
        int y[64];

        for(j=0;j<64;j++) x[j]=0;
            for(j=0;j<64;j++) y[j]=0;

            for(j=0;j<64;j++){
                rw=j/8;
                cm=j%8;
                if(cm==0)x[j]=x1; else x[j]=x[j-1]+1;
                y[j]=y1-rw;
                // printf("Pixel %2d X %3d Y %3d\n",j+1,x[j],y[j]);
		if(debugMode>=1)  System.out.println("Pixel"+(j+1)+" "+x[j]+" "+y[j]);
            }
        }
	*/


}
