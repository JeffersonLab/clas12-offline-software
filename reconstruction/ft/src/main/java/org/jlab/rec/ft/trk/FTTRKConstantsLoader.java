/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

/**
 *
 * @author devita
 * @author filippi
 */
public class FTTRKConstantsLoader {

	FTTRKConstantsLoader() {
	}

        // geometry constants
	public static final int Nlayers = 4;   // 2 double layers, ordered as FTT1B+FTT1T, FTT3B+FTT3T
        public static int NSupLayers = Nlayers/2;   // bottom+top makes a a SuperLayer
	public static int Nstrips=768 ;// Number of strips
        public static int SideHalfstrips;  
        public static int Longstrips=128;
        public static double[] Zlayer = {175., 176., 177., 178.}; //Give z-coordinate of the layer
//        public static double[] Alpha = {0.,0.5*Math.PI}; //Give the rotation angle to apply
        public static double[] Alpha = {-0.5*Math.PI, 0.}; //Give the rotation angle to apply
	public static double Pitch=0.056; //strip width
	public static double Beamhole=14.086/2.;//Radius of the hole in the center for the beam (mm)
//	public static double Interstrip ; //inter strip
        public static double InnerHole;
	public static double Rmax;
        public static double[][][] stripsXloc; //Give the local end-points x-coordinates of the strip segment, per layer
        public static double[][][] stripsYloc; //Give the local end-points y-coordinates of the strip segment, per layer
        public static double[] stripsXlocref; //Give the local ref-points x-coordinates of the strip segment
        public static double[] stripsYlocref; //Give the local ref-points y-coordinates of the strip segment
        public static double[][][] stripsX; //Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
        public static double[][][] stripsY; //Give the  end-points y-coordinates of the strip segment
        public static double[] stripslength; //Give the strip length
 ///       public static int[] UStrips, IStrips;   
        
      
        public static synchronized void Load() {

		SideHalfstrips =  (Nstrips -2*Longstrips)/4;    // 128
		InnerHole = (double)SideHalfstrips*Pitch;       // 7.168 cm, exceeds Beamhole by 0.125 cm - reference as minimum radius       
		Rmax = Pitch*(SideHalfstrips + Longstrips);     // 21.504
                // 2d arrays: [0] origin, [1] segment endpoint
		stripsXloc = new double[NSupLayers][Nstrips][2]; 
                stripsYloc = new double[NSupLayers][Nstrips][2];
                stripsXlocref = new double[Nstrips]; 
                stripsYlocref = new double[Nstrips];
                stripsX = new double[Nlayers][Nstrips][2];
                stripsY = new double[Nlayers][Nstrips][2]; 
                stripslength = new double[Nstrips]; 
                
                int debug = FTTRKReconstruction.debugMode;
		
                // fill the arrays with strip numbers ordered according to the module region. Start from 0.
                /*
                for(int i=0; i<Longstrips; i++){UStrips[i] = i;}
                for(int i=Longstrips; i<(Longstrips+SideHalfstrips); i++){UStrips[i] = i;}
                for(int i=(Longstrips+SideHalfstrips); i<(Longstrips+2*SideHalfstrips); i++){IStrips[i-Longstrips-SideHalfstrips] = i;}
                for(int i=(Longstrips+2*SideHalfstrips); i<2*(Longstrips+SideHalfstrips); i++){UStrips[i-SideHalfstrips] = i;}    
                */
                
                // just the first two layers are enough, the second two are identical
                for(int j=0; j<Nlayers/2; j++){
                    for(int i=0;i<Nstrips;i++) {
                        //Give the Y of the middle of the strip - it is the same for bottom and top modules (before rotation)
                        // i goes from 0 to 767
                        int localRegionY = getLocalRegionY(i);
                        //System.out.println("strip " + i + " local region Y " + localRegionY);
                        if(localRegionY == -1){ // bottom long strips+right side half strips
                            stripsYloc[j][i][0] = stripsYloc[j][i][1] = -Rmax + (i + 0.5)*Pitch;
                        }else if(localRegionY == -2){ // bottom left side half strips 
//                           stripsYloc[j][i][0] = stripsYloc[j][i][1] = -Rmax + (i - SideHalfstrips - 0.5)*Pitch;
                            stripsYloc[j][i][0] = stripsYloc[j][i][1] = -Rmax - (2*SideHalfstrips -i-0.5)*Pitch;
                        }else if(localRegionY == 1){ // top long strips + left side half strips
                            stripsYloc[j][i][0] = stripsYloc[j][i][1] = Rmax - (Nstrips - i - 0.5)*Pitch;
                        }else if(localRegionY == 2){ // top right side half strips
//                            stripsYloc[j][i][0] = stripsYloc[j][i][1] = Rmax - (i + 0.5)*Pitch;
                            stripsYloc[j][i][0] = stripsYloc[j][i][1] = (i + 0.5 - 2*SideHalfstrips)*Pitch;
                        }else{
                            System.out.println("**** check strip number, Y coordinate not assigned ****");
                        }     
                        stripsYlocref[i] = stripsYloc[j][i][0];
                        
                        /*
			if (i<512){
				stripsYloc[i][0]=-Rmax+(511-i+0.5)*Pitch;
				stripsYloc[i][1]=-Rmax+(511-i+0.5)*Pitch;
			} else {
				stripsYloc[i][0]=Rmax-(1023-i+0.5)*Pitch;
				stripsYloc[i][1]=Rmax-(1023-i+0.5)*Pitch;
			}
			stripsYlocref[i] = stripsYloc[i][0];
                        */
                        
                        // reference geometry: bottom module (top are left/right symmetric
                        // Give the X of the middle of the strip
			int localRegionX = getLocalRegionX(i);
//                        System.out.println("strip " + i + " localRegionX " + localRegionX);
                        if(localRegionX==1 || localRegionX==2 || localRegionX==4 || localRegionX==5){
                            // half strips, hole sides
                            if(j%2 == 0){ // bottom layer
                                if(localRegionX==1 || localRegionX==2){ // left (negative X) side
                                    stripsXloc[j][i][0] = -Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                                    stripsXloc[j][i][1] = -Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                                    stripslength[i] = Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                                    stripsXlocref[i] = -stripslength[i]/2.;                                        
                                }else if(localRegionX==4 || localRegionX==5){ // right (positive X) side
                                    stripsXloc[j][i][0] = Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                                    stripsXloc[j][i][1] = Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                                    stripslength[i] = Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                                    stripsXlocref[i] = +stripslength[i]/2.;                                                                        
                                }
                            }else{ // top layer
                                if(localRegionX==1 || localRegionX==2){ // left (negative X) side
                                    stripsXloc[j][i][0] = Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                                    stripsXloc[j][i][1] = Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                                    stripslength[i] = Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                                    stripsXlocref[i] = stripslength[i]/2.;
                                    double calc = InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1];
//                                    System.out.println("local coordinates strips j, i " + j + " " + i + " " + stripsXloc[j][i][1]);
//                                    System.out.println("argument sqrt " + calc);
                                }else if(localRegionX==4 || localRegionX==5){ // right (positive X) side
                                    stripsXloc[j][i][0] = -Math.sqrt(Rmax*Rmax-stripsYloc[j][i][0]*stripsYloc[j][i][0]);
                                    stripsXloc[j][i][1] = -Math.sqrt(InnerHole*InnerHole-stripsYloc[j][i][1]*stripsYloc[j][i][1]);
                                    stripslength[i] = -Math.abs(stripsXloc[j][i][0] - stripsXloc[j][i][1]);
                                    stripsXlocref[i] = -stripslength[i]/2.;                                                                        
                                }
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
                        
                        /*
			switch(localRegion) {
			case 2: case 4:
				stripslength[i]=2*Rmax*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Rmax));
				stripsXloc[i][0] = -stripslength[i]/2.;
				stripsXloc[i][1] =  stripslength[i]/2.;
                                stripsXlocref[i] = 0;
				break;
			case 1:
				stripslength[i]= Rmax*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Rmax));
				stripsXloc[i][1] = 0;
				stripsXloc[i][0] = -stripslength[i];
                                stripsXlocref[i] = -stripslength[i]/2;
				if(Math.abs(stripsYloc[i][0])/Beamhole<1) {
					stripslength[i]= Rmax*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Rmax))-Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
					stripsXloc[i][1] = -Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
					stripsXloc[i][0] = -stripslength[i];
                                        stripsXlocref[i] = -stripslength[i]/2-Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
				}
				break;
			case 3:
				stripslength[i]= Rmax*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Rmax));
				stripsXloc[i][0] = 0;
				stripsXloc[i][1] = stripslength[i];
                                stripsXlocref[i] = stripslength[i]/2;
				if(Math.abs(stripsYloc[i][0])/Beamhole<1) {
					stripslength[i]= Rmax*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Rmax))-Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
					stripsXloc[i][0] = Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
					stripsXloc[i][1] = stripslength[i];
                                        stripsXlocref[i] = stripslength[i]/2+Beamhole*Math.sin(Math.acos(Math.abs(stripsYloc[i][0])/Beamhole));
				}
				break;
			}
*/ 
                        // flip all x,y local coordinates
//                        for(int k=0; k<1; k++){
//                            stripsXloc[j][i][k] = -stripsXloc[j][i][k];
//                            stripsYloc[j][i][k] = -stripsYloc[j][i][k];
//                        }
                        
                        
                        // with a negative rotation of layer 1-3 the coordinates are matched to the spot on FTCAL
                        // in this way the strip numbering is consistent with the MC geometry
                        stripsX[j][i][0] = (stripsXloc[j][i][0]*Math.cos(Alpha[j]) - stripsYloc[j][i][0]*Math.sin(Alpha[j]));
                        stripsY[j][i][0] = stripsXloc[j][i][0]*Math.sin(Alpha[j]) + stripsYloc[j][i][0]*Math.cos(Alpha[j]);
                        stripsX[j][i][1] = (stripsXloc[j][i][1]*Math.cos(Alpha[j]) - stripsYloc[j][i][1]*Math.sin(Alpha[j]));
                        stripsY[j][i][1] = stripsXloc[j][i][1]*Math.sin(Alpha[j]) + stripsYloc[j][i][1]*Math.cos(Alpha[j]);
                        
                        stripsX[j+2][i][0] = stripsX[j][i][0];
                        stripsY[j+2][i][0] = stripsY[j][i][0];
                        stripsX[j+2][i][1] = stripsX[j][i][1];
                        stripsY[j+2][i][1] = stripsY[j][i][1];
                        
			//if(debug>=1) System.out.println(Constants.getLocalRegion(i)+" strip-1 = "+i+" x' "+stripsXloc[i][1]+" y' "+stripsYloc[i][1]+" length "+stripslength[i]+" Beamhole "+Beamhole);
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
        
        
}
