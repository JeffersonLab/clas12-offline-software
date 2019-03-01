/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.ft.trk;

/**
 *
 * @author devita
 */
public class FTTRKConstantsLoader {

	FTTRKConstantsLoader() {
	}

        // geometry constants
	public static final int Nlayers = 4;
	public static int Nstrips=768 ;// Number of strips
        public static int Halfstrips ; // In the middle of the FMT, 320 strips are split in two. 
        public static int Sidestrips=312 ;
        public static double[] Zlayer = {175, 176, 177, 178}; //Give z-coordinate of the layer
        public static double[] Alpha = {0,90,0,90}; //Give the rotation angle to apply
	public static double Pitch=0.05; //strip width
	public static double Beamhole=7;//Radius of the hole in the center for the beam.
//	public static double Interstrip ; //inter strip
        public static double YCentral;
	public static double Rmax;
        public static double[][] stripsXloc; //Give the local end-points x-coordinates of the strip segment
        public static double[][] stripsYloc; //Give the local end-points y-coordinates of the strip segment
        public static double[] stripsXlocref; //Give the local ref-points x-coordinates of the strip segment
        public static double[] stripsYlocref; //Give the local ref-points y-coordinates of the strip segment
        public static double[][][] stripsX; //Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
        public static double[][][] stripsY; //Give the  end-points y-coordinates of the strip segment
        public static double[] stripslength; //Give the strip length

        public static synchronized void Load() {

		Sidestrips =  (Nstrips -2*Halfstrips)/2;
		YCentral = (double)Halfstrips*Pitch/2.;
		Rmax = Pitch*(Halfstrips + 2*Sidestrips)/2.;
		stripsXloc = new double[Nstrips][2]; 
                stripsYloc = new double[Nstrips][2];
                stripsXlocref = new double[Nstrips]; 
                stripsYlocref = new double[Nstrips]; 
                stripsX = new double[Nlayers][Nstrips][2];
                stripsY = new double[Nlayers][Nstrips][2]; 
                stripslength = new double[Nstrips]; 
		
                
		for(int i=0;i<Nstrips;i++) { 
			//Give the Y of the middle of the strip
			if (i<512){
				stripsYloc[i][0]=-Rmax+(511-i+0.5)*Pitch;
				stripsYloc[i][1]=-Rmax+(511-i+0.5)*Pitch;
			} else {
				stripsYloc[i][0]=Rmax-(1023-i+0.5)*Pitch;
				stripsYloc[i][1]=Rmax-(1023-i+0.5)*Pitch;
			}
			stripsYlocref[i] = stripsYloc[i][0];
                        
			int localRegion = getLocalRegion(i);
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
			for(int j=0;j<Nlayers;j++) { //X sign flip
				stripsX[j][i][0] = -(stripsXloc[i][0]*Math.cos(Alpha[j]) + stripsYloc[i][0]*Math.sin(Alpha[j]));
				stripsY[j][i][0] = -stripsXloc[i][0]*Math.sin(Alpha[j]) + stripsYloc[i][0]*Math.cos(Alpha[j]);
				stripsX[j][i][1] = -(stripsXloc[i][1]*Math.cos(Alpha[j]) + stripsYloc[i][1]*Math.sin(Alpha[j]));
				stripsY[j][i][1] = -stripsXloc[i][1]*Math.sin(Alpha[j]) + stripsYloc[i][1]*Math.cos(Alpha[j]);
			}
			
		
			//System.out.println(Constants.getLocalRegion(i)+" strip-1 = "+i+" x' "+stripsXloc[i][1]+" y' "+stripsYloc[i][1]+" length "+stripslength[i]+" Beamhole "+Beamhole);
		}
		System.out.println("*****   FMT constants loaded!");
	}
        
	private static int getLocalRegion(int i) {
		// To represent the geometry we divide the barrel micromega disk into 3 regions according to the strip numbering system.
		// Here i = strip_number -1;
		// Region 1 is the region in the negative x part of inner region: the strips range is from   1 to 320  (   0 <= i < 320)
		// Region 2 is the region in the negative y part of outer region: the strips range is from 321 to 512  ( 320 <= i < 512)
		// Region 3 is the region in the positive x part of inner region: the strips range is from 513 to 832  ( 512 <= i < 832)
		// Region 4 is the region in the positive y part of outer region: the strips range is from 833 to 1024 ( 832 <= i < 1024)
		
		int region = 0;
		if(i>=0 && i<320)
			region =1;
		if(i>=320 && i<512)
			region =2;
		if(i>=512 && i<832)
			region =3;
		if(i>=832 && i<1024)
			region =4;
		
		return region;
	}

    
}
