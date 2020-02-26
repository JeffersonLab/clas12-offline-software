package org.jlab.rec.fmt;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;


/**
 * 
 * @author defurne, ziegler
 *
 */
public class Constants {
	
	public static final int FVT_Nlayers = 6;
	public static int FVT_Nstrips ;// Number of strips
        public static int FVT_Halfstrips ; // In the middle of the FMT, 320 strips are split in two. 
        public static int FVT_Sidestrips ;
        
	// units = cm
	public static int MAX_NB_CROSSES = 30;
	
	public static double FVT_Pitch; //strip width
	public static double FVT_Interstrip ; //inter strip
        public static double FVT_YCentral;
	public static double FVT_Rmax;
        
	public static double FVT_Beamhole;//Radius of the hole in the center for the beam.
        public static double FVT_SigmaS;

        public static double[] FVT_Zlayer; //Give z-coordinate of the layer
        public static double[] FVT_Alpha; //Give the rotation angle to apply

        public static double[][] FVT_stripsXloc; //Give the local end-points x-coordinates of the strip segment
        public static double[][] FVT_stripsYloc; //Give the local end-points y-coordinates of the strip segment
        public static double[] FVT_stripsXlocref; //Give the local ref-points x-coordinates of the strip segment
        public static double[] FVT_stripsYlocref; //Give the local ref-points y-coordinates of the strip segment
        public static double[][][] FVT_stripsX; //Give the  end-points x-coordinates of the strip segment rotated in the correct frame for the layer
        public static double[][][] FVT_stripsY; //Give the  end-points y-coordinates of the strip segment

        public static double[] FVT_stripslength; //Give the strip length

        public static double hDrift;
	
        private static double[] EFF_Z_OVER_A ;      // for ELOSS
        private static double[] _X0 ;         // for M.Scat.
        private static double[] _REL_POS;    // relative postion of the material wrt to strip plane
	public static boolean areConstantsLoaded = false;
	
	// ----- cut based cand select
	public static  double phi12cut = 35.; 
	public static  double phi13cut = 35.; 

	public static  double drdzcut =1.5;
	// ----- end cut based cand select
	
    public static synchronized void Load() {
		if (areConstantsLoaded ) return;

		//CCDBConstantsLoader FVT_CCDB= new CCDBConstantsLoader();
		//FVT_CCDB.Load(runNb);
		FVT_Sidestrips =  (FVT_Nstrips -2*FVT_Halfstrips)/2;
		FVT_YCentral = (double)FVT_Halfstrips*FVT_Pitch/2.;
		FVT_Rmax = FVT_Pitch*(FVT_Halfstrips + 2*FVT_Sidestrips)/2.;
		FVT_SigmaS = FVT_Pitch/Math.sqrt(12);
		FVT_stripsXloc = new double[FVT_Nstrips][2]; 
                FVT_stripsYloc = new double[FVT_Nstrips][2];
                FVT_stripsXlocref = new double[FVT_Nstrips]; 
                FVT_stripsYlocref = new double[FVT_Nstrips]; 
                FVT_stripsX = new double[FVT_Nlayers][FVT_Nstrips][2];
                FVT_stripsY = new double[FVT_Nlayers][FVT_Nstrips][2]; 
                FVT_stripslength = new double[FVT_Nstrips]; 
		
                
		for(int i=0;i<FVT_Nstrips;i++) { 
			//Give the Y of the middle of the strip
			if (i<512){
				FVT_stripsYloc[i][0]=-FVT_Rmax+(511-i+0.5)*FVT_Pitch;
				FVT_stripsYloc[i][1]=-FVT_Rmax+(511-i+0.5)*FVT_Pitch;
			} else {
				FVT_stripsYloc[i][0]=FVT_Rmax-(1023-i+0.5)*FVT_Pitch;
				FVT_stripsYloc[i][1]=FVT_Rmax-(1023-i+0.5)*FVT_Pitch;
			}
			FVT_stripsYlocref[i] = FVT_stripsYloc[i][0];
                        
			int localRegion = getLocalRegion(i);
			switch(localRegion) {
			case 2: case 4:
				FVT_stripslength[i]=2*FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
				FVT_stripsXloc[i][0] = -FVT_stripslength[i]/2.;
				FVT_stripsXloc[i][1] =  FVT_stripslength[i]/2.;
                                FVT_stripsXlocref[i] = 0;
				break;
			case 1:
				FVT_stripslength[i]= FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
				FVT_stripsXloc[i][1] = 0;
				FVT_stripsXloc[i][0] = -FVT_stripslength[i];
                                FVT_stripsXlocref[i] = -FVT_stripslength[i]/2;
				if(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole<1) {
					FVT_stripslength[i]= FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax))-FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
					FVT_stripsXloc[i][1] = -FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
					FVT_stripsXloc[i][0] = -FVT_stripslength[i];
                                        FVT_stripsXlocref[i] = -FVT_stripslength[i]/2-FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
				}
				break;
			case 3:
				FVT_stripslength[i]= FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax));
				FVT_stripsXloc[i][0] = 0;
				FVT_stripsXloc[i][1] = FVT_stripslength[i];
                                FVT_stripsXlocref[i] = FVT_stripslength[i]/2;
				if(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole<1) {
					FVT_stripslength[i]= FVT_Rmax*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Rmax))-FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
					FVT_stripsXloc[i][0] = FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
					FVT_stripsXloc[i][1] = FVT_stripslength[i];
                                        FVT_stripsXlocref[i] = FVT_stripslength[i]/2+FVT_Beamhole*Math.sin(Math.acos(Math.abs(FVT_stripsYloc[i][0])/FVT_Beamhole));
				}
				break;
			}
			for(int j=0;j<FVT_Nlayers;j++) { //X sign flip
				FVT_stripsX[j][i][0] = -(FVT_stripsXloc[i][0]*Math.cos(FVT_Alpha[j]) + FVT_stripsYloc[i][0]*Math.sin(FVT_Alpha[j]));
				FVT_stripsY[j][i][0] = -FVT_stripsXloc[i][0]*Math.sin(FVT_Alpha[j]) + FVT_stripsYloc[i][0]*Math.cos(FVT_Alpha[j]);
				FVT_stripsX[j][i][1] = -(FVT_stripsXloc[i][1]*Math.cos(FVT_Alpha[j]) + FVT_stripsYloc[i][1]*Math.sin(FVT_Alpha[j]));
				FVT_stripsY[j][i][1] = -FVT_stripsXloc[i][1]*Math.sin(FVT_Alpha[j]) + FVT_stripsYloc[i][1]*Math.cos(FVT_Alpha[j]);
			}
			
		
			//System.out.println(Constants.getLocalRegion(i)+" strip-1 = "+i+" x' "+FVT_stripsXloc[i][1]+" y' "+FVT_stripsYloc[i][1]+" length "+FVT_stripslength[i]+" FVT_Beamhole "+FVT_Beamhole);
		}
		areConstantsLoaded = true;
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
	
        public static synchronized double[] getEFF_Z_OVER_A() {
            return EFF_Z_OVER_A;
        }
        public static synchronized void setEFF_Z_OVER_A(double[] eFF_Z_OVER_A) {
            EFF_Z_OVER_A = eFF_Z_OVER_A;
        }

        public static synchronized double[] get_X0() {
            return _X0;
        }
        public static synchronized void set_X0(double[] X0) {
           _X0 = X0;
        }
        
        public static synchronized double[] get_RELPOS() {
            return _REL_POS;
        }
        public static synchronized void set_RELPOS(double[] REL_POS) {
           _REL_POS = REL_POS;
        }

       
	
		
}
