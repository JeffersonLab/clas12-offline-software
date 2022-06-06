package org.jlab.rec.ft.trk;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.prim.Line3D;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKConstantsLoader {

	FTTRKConstantsLoader() {
	}
//  read all relevant constants from DB
        
        public static final int Nlayers = 4;        // 2 double layers, ordered as FTT1B+FTT1T, FTT3T+FTT3B, 0-4
        public static int NSupLayers = Nlayers/2;   // bottom+top makes a a SuperLayer
	
        public static int Nstrips;             // Number of strips 
        public static int Longstrips;
        public static int SideHalfstrips; 
        public static double[] Zlayer;   // from MC mean position of gas layers (average z position of MC hits)

        public static double[] Alpha;
        
        public static double Pitch;            //strip width
	public static double Beamhole;     //Radius of the hole in the center for the beam (mm)
        
        public static double InnerHole;
	public static double Rmax;
        public static double[][][] stripsXloc; //Give the local end-points x-coordinates of the strip segment, per layer
        public static double[][][] stripsYloc; //Give the local end-points y-coordinates of the strip segment, per layer
        public static double[] stripsXlocref;  //Give the local ref-points x-coordinates of the strip segment
        public static double[] stripsYlocref;  //Give the local ref-points y-coordinates of the strip segment
        public static double[][][] stripsX;    //Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
        public static double[][][] stripsY;    //Give the  end-points y-coordinates of the strip segment
        public static double[] stripslength;  //Give the strip length   
      
        public static boolean ADJUSTTT = false;
        public static boolean CSTLOADED = false;
        private static DatabaseConstantProvider dbprovider = null;
        
      
        public static synchronized void Load(int run, String var) {

            // get constants from database table
            System.out.println(" LOADING CONSTANTS ");
//	    if(CSTLOADED == true) return null;
            dbprovider = new DatabaseConstantProvider(run, var); // reset using the new variation
				
	    // load table reads entire table and makes an array of variables for each column in the table.
	    dbprovider.loadTable("/geometry/ft/fttrk");
	    dbprovider.disconnect(); 
	    dbprovider.show();          
                       
            Nstrips = dbprovider.getInteger("/geometry/ft/fttrk/nstrips", 0) ;             // Number of strips
            Longstrips = dbprovider.getInteger("/geometry/ft/fttrk/nlongstrips", 0);
            Pitch = dbprovider.getDouble("/geometry/ft/fttrk/pitch", 0);
            Beamhole = dbprovider.getDouble("/geometry/ft/fttrk/innerradius", 0);          // radius of the central hole for beam (mm)

            
            double innerRadiusActive = dbprovider.getDouble("/geometry/ft/fttrk/innerradiusactive", 0);
            Zlayer = new double[Nlayers];
            Alpha = new double[Nlayers];
            for(int lay=0; lay<Nlayers; lay++){
                Zlayer[lay] = (double) dbprovider.getDouble("/geometry/ft/fttrk/z", lay);
                Alpha[lay] = (double) dbprovider.getInteger("/geometry/ft/fttrk/angle", lay);
                
                Alpha[lay] *= Math.PI/180.;     // convert to radians
            }
            
            CSTLOADED = true;
            System.out.println("SUCCESFULLY LOADED FTTRK GEOMETRY CONSTANTS");
            
            SideHalfstrips =  (Nstrips -2*Longstrips)/4;      // 128
            InnerHole = (double)(SideHalfstrips)*Pitch;       // 7.168 cm, exceeds Beamhole by 0.125 cm - reference as minimum radius
            Rmax = Pitch*(SideHalfstrips + Longstrips);       // 14.336
            // 2d arrays: [0] origin, [1] segment endpoint
            stripsXloc = new double[NSupLayers][Nstrips][2]; 
            stripsYloc = new double[NSupLayers][Nstrips][2];
            stripsXlocref = new double[Nstrips]; 
            stripsYlocref = new double[Nstrips];
            stripsX = new double[Nlayers][Nstrips][2];
            stripsY = new double[Nlayers][Nstrips][2]; 
            stripslength = new double[Nstrips]; 

            int debug = FTTRKReconstruction.debugMode;
            double half = 0.5;

            // just the first two layers are enough, the second two are identical
            for(int j=0; j<Nlayers/2; j++){
                for(int i=0;i<Nstrips;i++) {
                    //Give the Y of the middle of the strip - it is the same for bottom and top modules (before rotation)
                    // i goes from 0 to 767
                    int localRegionY = getLocalRegionY(i);
                    if(localRegionY == -1){ // bottom long strips+right side half strips
                        stripsYloc[j][i][0] = stripsYloc[j][i][1] = -Rmax + (i + half)*Pitch;
                    }else if(localRegionY == -2){ // bottom left side half strips 
                        stripsYloc[j][i][0] = stripsYloc[j][i][1] = -Rmax - (2*SideHalfstrips-half -i)*Pitch;
                    }else if(localRegionY == 1){ // top long strips + left side half strips
                        stripsYloc[j][i][0] = stripsYloc[j][i][1] = Rmax - (Nstrips - i -half)*Pitch;
                    }else if(localRegionY == 2){ // top right side half strips
                        stripsYloc[j][i][0] = stripsYloc[j][i][1] = (i + half - 2*SideHalfstrips)*Pitch;
                    }else{
                        System.out.println("**** check strip number, Y coordinate not assigned ****");
                    }     
                    stripsYlocref[i] = stripsYloc[j][i][0];

                   // reference geometry: bottom module (top are left/right symmetric
                    // Give the X of the middle of the strip
                    int localRegionX = getLocalRegionX(i);
                    if(localRegionX==1 || localRegionX==2 || localRegionX==4 || localRegionX==5){
                        if(localRegionX==1 || localRegionX==2){ // left (negative X) side
                            stripsXloc[j][i][0] = Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                            stripsXloc[j][i][1] = Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                            stripslength[i] = Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                            stripsXlocref[i] = stripslength[i]/2.;                                        
                        }else if(localRegionX==4 || localRegionX==5){ // right (positive X) side
                            stripsXloc[j][i][0] = -Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                            stripsXloc[j][i][1] = -Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                            stripslength[i] = Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                            stripsXlocref[i] = -stripslength[i]/2.;                                                                        
                        }
                    }else if(localRegionX==3 || localRegionX==6){
                        // long strips, same for bottom and top modules
                        stripslength[i] = 2.*Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                        stripsXloc[j][i][0] = -stripslength[i]/2.; 
                        stripsXloc[j][i][1] = -stripsXloc[j][i][0];
                        stripsXlocref[i] = 0.;
                    }else{
                        System.out.println("**** check strip number, X coordinate not assigned ****");
                    }

                    stripsX[j][i][0] = (stripsXloc[j][i][0]*Math.cos(Alpha[j]) - stripsYloc[j][i][0]*Math.sin(Alpha[j]));
                    stripsY[j][i][0] = stripsXloc[j][i][0]*Math.sin(Alpha[j]) + stripsYloc[j][i][0]*Math.cos(Alpha[j]);
                    stripsX[j][i][1] = (stripsXloc[j][i][1]*Math.cos(Alpha[j]) - stripsYloc[j][i][1]*Math.sin(Alpha[j]));
                    stripsY[j][i][1] = stripsXloc[j][i][1]*Math.sin(Alpha[j]) + stripsYloc[j][i][1]*Math.cos(Alpha[j]);

                    stripsX[j+2][i][0] = (stripsXloc[j][i][0]*Math.cos(Alpha[j+2]) - stripsYloc[j][i][0]*Math.sin(Alpha[j+2]));
                    stripsY[j+2][i][0] = (stripsXloc[j][i][0]*Math.sin(Alpha[j+2]) + stripsYloc[j][i][0]*Math.cos(Alpha[j+2]));
                    stripsX[j+2][i][1] = (stripsXloc[j][i][1]*Math.cos(Alpha[j+2]) - stripsYloc[j][i][1]*Math.sin(Alpha[j+2]));
                    stripsY[j+2][i][1] = (stripsXloc[j][i][1]*Math.sin(Alpha[j+2]) + stripsYloc[j][i][1]*Math.cos(Alpha[j+2]));

                    // alignment: global translation: x-y coordinates layer 0,1
                    double tX1 = 0.; double tY1 = 0.;
                    double tX2 = 0.; double tY2 = 0.; 

                    stripsX[j][i][0] += tX1;  // cm
                    stripsX[j][i][1] += tX1;  
                    stripsY[j][i][0] += tY1;
                    stripsY[j][i][1] += tY1;

                    stripsX[j+2][i][0] += tX2;  // cm
                    stripsX[j+2][i][1] += tX2;  
                    stripsY[j+2][i][0] += tY2;
                    stripsY[j+2][i][1] += tY2;
                }
            }
            System.out.println("*****    FTTRK constants loaded");
        }
  
        private static int getLocalRegionY(int strip){
            // the strips are read by the db table in the format 1-768
            // divide the geometry in vertical direction in 4 contiguity regions
            int regionY = 0;
            int i = strip+1;
            int stripBlock = Nstrips/6;
            if(i>0 && i<=stripBlock*2){
                regionY = -1;
            }else if(i>stripBlock*3 && i<=stripBlock*4){
                regionY = -2;
            }else if(i>stripBlock*2 && i<=stripBlock*3){
                regionY = 2;
            }else if(i>stripBlock*4 && i<=Nstrips){
                regionY = 1;
            }        
            return regionY;
        }
        
        private static int getLocalRegionX(int strip){
            // the strips are read by the db table in the format 1-768
            // divide the geometry in horizontal direction in 6 contiguity regions
            int regionX = 0;
            int i = strip+1;
            int stripBlock = Nstrips/6;
            if(i>stripBlock*4 && i<=stripBlock*5){        // 513-640
                regionX = 1;
            }else if(i>stripBlock*3 && i<=stripBlock*4){  // 385-512
                regionX = 2;
            }else if(i>0 && i<=stripBlock){               // 1-128
                regionX = 3;
            }else if(i>stripBlock && i<=stripBlock*2){    // 129-256
                regionX = 4;
            }else if(i>stripBlock*2 && i<=stripBlock*3){  // 257-384 
                regionX = 5;
            }else if(i>stripBlock*5 && i<=Nstrips){       // 641-768
                regionX = 6;
            }
            return regionX;
        }
        
        public static Line3D getStripSegmentLab(int layer, int seed){
            Line3D stripSegment = new Line3D(stripsX[layer-1][seed-1][0], stripsY[layer-1][seed-1][0], Zlayer[layer-1], 
                                             stripsX[layer-1][seed-1][1], stripsY[layer-1][seed-1][1], Zlayer[layer-1]);
            //stripSegment.show();
            return stripSegment;
        }
}
