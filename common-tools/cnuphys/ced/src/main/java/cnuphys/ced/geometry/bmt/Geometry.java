package cnuphys.ced.geometry.bmt;

import java.util.Random;

public class Geometry {

	public Geometry() {
		
	}
	
	// Comments on the Geometry of the BMT 
	//------------------------------------
	// The BMT geometry consists of 3 cylindical double layers of MicroMegas. 
	// The inner (i.e. closest to beam in double layer) layer contain longitudinal strips oriented in the Z direction. 
	// This is called the Z layer. 
	// The outer layer contain arched strips at a non-constant pitch in Z. This is called the C layer.
	// The cylinder phi profile in divided into 3 sectors, A, B, C.
	// 
	/**
	 * 
	 * @param sector
	 * @return detector index A (=0), B (=1), C (=2)
	 */
	public int getDetectorIndex(int sector) {
		//sector 1 corresponds to detector B, 2 to A,  3 to C 
		// A is detIdx 0, B 1, C 2.
		int DetIdx = -1;
	/*	if(sector == 1)
			DetIdx = 2;
		if(sector == 2)
			DetIdx = 1;
		if(sector == 3)
			DetIdx = 0;
		*/
		DetIdx = sector-1;
		return DetIdx;
		
	}

	/**
	 * 
	 * @param sector the sector in CLAS12 1...3
	 * @param layer the layer 1...6
	 * @param strip the strip number (starts at 1)
	 * @return the angle to localize the  center of strip in radians
	 */
	public double CRZStrip_GetPhi(int sector, int layer, int strip){

	    // Sector = num_detector + 1;	
	    // num_detector = 0 (region A), 1 (region B), 2, (region C)
	    //For CRZ, this function returns the angle to localize the  center of strip "num_strip" for the "num_detector"
		int num_detector = this.getDetectorIndex(sector); 				// index of the detector (0...2)
		//int num_strip = strip - 1;     									// index of the strip (starts at 0)
		int num_region = (layer+1)/2 - 1; 						// region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		
		//double angle=Constants.getCRZEDGE1()[num_region][num_detector]+(Constants.getCRZXPOS()[num_region]+(Constants.getCRZWIDTH()[num_region]/2.+num_strip*(Constants.getCRZWIDTH()[num_region]+Constants.getCRZSPACING()[num_region])))/Constants.getCRZRADIUS()[num_region];
		
		double Delta_Phi = Constants.getCRZWIDTH()[num_region]/Constants.getCRZRADIUS()[num_region];
		double angle=Constants.getCRZEDGE1()[num_region][num_detector]+(0.5+strip)*Math.abs(Delta_Phi);
		return angle; //in rad 
	}

	
	/**
	 * 
	 * @param layer the layer 1...6
	 * @param angle the position angle of the hit in the Z detector
	 * @return the Z strip as a function of azimuthal angle
	 */
	public int getZStrip(int layer, double angle) { // the angle is the Lorentz uncorrected angle
		
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		int num_detector =isInDetector( layer,  angle) ;
		if(num_detector==-1)
			return -1;
		
		if(angle<0)
			angle+=2*Math.PI; // from 0 to 2Pi
		
		if(num_detector==1) {
			double angle_f=Constants.getCRCEDGE1()[num_region][1]+(Constants.getCRCXPOS()[num_region]+Constants.getCRCLENGTH()[num_region])/Constants.getCRCRADIUS()[num_region] - 2*Math.PI;
			if(angle>=0 && angle<=angle_f)
				angle+=2*Math.PI;
		}
		double strip_calc = ( (angle-Constants.getCRZEDGE1()[num_region][num_detector])*Constants.getCRZRADIUS()[num_region]-Constants.getCRZXPOS()[num_region]-Constants.getCRZWIDTH()[num_region]/2.)/(Constants.getCRZWIDTH()[num_region]+Constants.getCRZSPACING()[num_region]);
			
		strip_calc = (int)(Math.round(strip_calc * 1d) / 1d);
		int strip_num = (int) Math.floor(strip_calc);
		
		int value = strip_num + 1;
		
		if(value<1 || value>Constants.getCRZNSTRIPS()[num_region])
			value = -1;
		
		return value;
	}
	
	public double GetStartAngle(int sector, int layer) {
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		int num_detector = this.getDetectorIndex(sector); 
		
		return Constants.getCRCEDGE1()[num_region][num_detector];
	}
	
	public double GetEndAngle(int sector, int layer) {
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		int num_detector = this.getDetectorIndex(sector); 
		
		return Constants.getCRCEDGE2()[num_region][num_detector];
	}

	
	/**
	 * 
	 * @param layer the layer 1...6
	 * @return the Z position of the strip center
	 */
	private double CRZ_GetZStrip(int layer){
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		//For CRZ, this function returns the Z position of the strip center
		double zc=Constants.getCRZZMIN()[num_region]+Constants.getCRZOFFSET()[num_region]+Constants.getCRZLENGTH()[num_region]/2.;
		return zc; //in mm
	}
	
	/**
	 * 
	 * @param sector the sector in CLAS12 1...3
	 * @param layer the layer 1...6
	 * @return the angle to localize the beginning of the strips
	 */
	public double CRC_GetBeginStrip(int sector, int layer){
		// Sector = num_detector + 1;	
	    // num_detector = 0 (region A), 1 (region B), 2, (region C)
	    
		int num_detector = this.getDetectorIndex(sector); 			// index of the detector (0...2)
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		
		//For CRC, this function returns the angle to localize the beginning of the strips
		double angle=Constants.getCRCEDGE1()[num_region][num_detector]+Constants.getCRCXPOS()[num_region]/Constants.getCRCRADIUS()[num_region];
		if (angle>2*Math.PI) angle-=2*Math.PI;
		return angle; //in rad
	}

	/**
	 * 
	 * @param sector the sector in CLAS12 1...3
	 * @param layer the layer 1...6
	 * @return the angle to localize the end of the strips
	 */
	public double CRC_GetEndStrip(int sector, int layer){
		// Sector = num_detector + 1;	
	    // num_detector = 0 (region A), 1 (region B), 2, (region C)
	    
		int num_detector = this.getDetectorIndex(sector); 			// index of the detector (0...2)
		int num_region = (layer+1)/2 - 1; 					// region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		
		//For CRC, this function returns the angle to localize the end of the strips
		double angle=Constants.getCRCEDGE1()[num_region][num_detector]+(Constants.getCRCXPOS()[num_region]+Constants.getCRCLENGTH()[num_region])/Constants.getCRCRADIUS()[num_region];
		if (angle>2*Math.PI) angle-=2*Math.PI;
		return angle; //in rad
	}

	/**
	 * 
	 * @param layer the hit layer
	 * @param strip the hit strip
	 * @return the z position in mm for the C-detectors
	 */
	public double CRCStrip_GetZ(int layer, int strip){
		
		int num_strip = strip - 1;     			// index of the strip (starts at 0)
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6

		//For CR6C, this function returns the Z position of the strip center
		int group=0;
		int limit=Constants.getCRCGROUP()[num_region][group];
		double zc=Constants.getCRCZMIN()[num_region]+Constants.getCRCOFFSET()[num_region] + Constants.getCRCWIDTH()[num_region][group]/2.;
		  
		if (num_strip>0){
		  for (int j=1;j<num_strip+1;j++){
		    zc+=Constants.getCRCWIDTH()[num_region][group]/2.;
		    if (j>=limit) { //test if we change the width
		    	group++;
		    	limit+=Constants.getCRCGROUP()[num_region][group];
		    } 
		    zc+=Constants.getCRCWIDTH()[num_region][group]/2.+Constants.getCRCSPACING()[num_region];
		  }
		}
		  
		return zc; //in mm
	}

	/**
	 *
	 * @param layer
	 * @param trk_z the track z position of intersection with the C-detector
	 * @return the C-strip
	 */
	public int getCStrip(int layer, double trk_z) { 
		
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		int strip_group = 0;
		int ClosestStrip =-1;
		// get group
		int len = Constants.getCRCGROUP()[num_region].length;
		double[] Z_lowBound = new double[len];
		double[] Z_uppBound = new double[len];
		int[] NStrips = new int[len];
		
		double zi= Constants.getCRCZMIN()[num_region]+Constants.getCRCOFFSET()[num_region];
		double z = trk_z - zi;
		
		Z_lowBound[0] = Constants.getCRCWIDTH()[num_region][0]/2.; // the lower bound is the zMin+theOffset with half the width
		Z_uppBound[0] = Z_lowBound[0]
						   + (Constants.getCRCGROUP()[num_region][0]-1)*(Constants.getCRCWIDTH()[num_region][0]+ Constants.getCRCSPACING()[num_region]);
		NStrips[0] = Constants.getCRCGROUP()[num_region][0];
		for(int i =1; i< len; i++)
		{
			Z_lowBound[i] = Z_uppBound[i-1] + Constants.getCRCWIDTH()[num_region][i-1]/2. + Constants.getCRCSPACING()[num_region] + Constants.getCRCWIDTH()[num_region][i]/2.;
			Z_uppBound[i] = Z_lowBound[i] + (Constants.getCRCGROUP()[num_region][i]-1)*(Constants.getCRCWIDTH()[num_region][i] + Constants.getCRCSPACING()[num_region]);
			
			NStrips[i] = NStrips[i-1] + Constants.getCRCGROUP()[num_region][i];
			
			if(z>=Z_lowBound[i] && z<=Z_uppBound[i]) {
				strip_group = i;
				ClosestStrip = 1 + (int) (Math.round(((z-Z_lowBound[strip_group])/(Constants.getCRCWIDTH()[num_region][strip_group] + Constants.getCRCSPACING()[num_region]))))+NStrips[i-1];

				len =i;
			} 
		}
		return ClosestStrip;
	}
	
	/**
	 * 
	 * @param layer
	 * @param x x-coordinate of the hit in the lab frame
	 * @param y y-coordinate of the hit in the lab frame
	 * @return the sigma along the beam direction (longitudinal)
	 */
	public double getSigmaLongit(int layer, double x, double y) { // sigma for C-detector
			
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		double sigma = Constants.SigmaMax*Math.sqrt((Math.sqrt(x*x+y*y)-Constants.getCRCRADIUS()[num_region]+Constants.hStrip2Det)/Constants.hDrift);
		
		return sigma;
			
	}
	
	/**
	 * 
	 * @param layer
	 * @param x x-coordinate of the hit in the lab frame
	 * @param y y-coordinate of the hit in the lab frame
	 * @return the sigma along in the azimuth direction taking the Lorentz angle into account
	 */
	public double getSigmaAzimuth(int layer, double x, double y) { // sigma for Z-detectors
		
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6double Z0=0;
		double sigma = Constants.SigmaMax*Math.sqrt((Math.sqrt(x*x+y*y)-Constants.getCRZRADIUS()[num_region]+Constants.hStrip2Det)/Constants.hDrift/Math.cos(Constants.getThetaL()));
		
		return sigma;
			
	}
	
	/**
	 * 
	 * @param layer
	 * @param x x-coordinate of the hit in the lab frame
	 * @param y y-coordinate of the hit in the lab frame
	 * @param z z-coordinate of the hit in the lab frame
	 * @return X[] = the smeared position x = X[0], y = X[1], z = X[2] taking the Lorentz angle into account
	 */
	public double[] smearedPosition(int layer, double x, double y, double z) {
		
		double[] newPos = new double[3];
        Random rand = new Random();
        
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6;

		double sigma =0;
		if(layer%2==0)  {// C layer
			sigma = getSigmaLongit(layer, x, y); //  longitudinal shower profile
			// changes z
			z = this.randomGaus(z, sigma, rand);
		}
		
		if(layer%2==1)  {// Z layer
			sigma = getSigmaAzimuth(layer, x, y); //  azimuth shower profile taking into account the Lorentz angle
			// changes phi
			double phicorr = (this.randomGaus(0, sigma, rand)/Math.cos(Constants.getThetaL())
					-(Math.sqrt(x*x+y*y)-Constants.getCRZRADIUS()[num_region]+
							Constants.hStrip2Det)*Math.tan(Constants.getThetaL()))/Constants.getCRZRADIUS()[num_region];
			double phi = Math.atan2(y, x); 
			phi+=phicorr;
			
			x = Math.sqrt(x*x+y*y)*Math.cos(phi);
			y = Math.sqrt(x*x+y*y)*Math.sin(phi);
		}
		newPos[0] = x;
		newPos[1] = y;
		newPos[2] = z;
		
		return newPos;
	}
	
	
	
	private double randomGaus(double mean, double width, Random aRandom){
        if ( width <= 0 ) {
        	return 0;
        }
       
        double smear = width * aRandom.nextGaussian();
        double randomNumber =  mean + smear;   

        return randomNumber;
      }
	

	/**
	 * 
	 * @param sector
	 * @param layer
	 * @param x
	 * @return a boolean indicating is the track hit is in the fiducial detector
	 */
	public boolean isInFiducial(int sector, int layer, double[] x) {
		
		boolean isInFid = false;
		
		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6;
		
		double z_i = CRZ_GetZStrip(layer) - Constants.getCRZLENGTH()[num_region]/2.; // fiducial z-profile lower limit
		double z_f = CRZ_GetZStrip(layer) + Constants.getCRZLENGTH()[num_region]/2.; // fiducial z-profile upper limit
		
		double R_i = 0; // inner radius init
		double R_f = 0; // outer radius init for a C or Z detector
		if(layer%2==1)
			R_i = Constants.getCRZRADIUS()[num_region]; // Z layer
		if(layer%2==0)
			R_i = Constants.getCRCRADIUS()[num_region]; // outer radius
		
		R_f = R_i + Constants.hDrift;
		
		double angle_i = 0; // first angular boundary init
		double angle_f = 0; // second angular boundary for detector A, B, or C init
		double A_i = CRC_GetBeginStrip(sector, layer); 
		double A_f = CRC_GetEndStrip(sector, layer);
		angle_i = A_i;
		angle_f = A_f;
		if(A_i>A_f) { // for B-detector
			angle_f = A_i;	
			angle_i = A_f;	
		}
		// the hit parameters
		double angle = Math.atan2(x[1], x[0]);
		if (angle>2*Math.PI) angle-=2*Math.PI;
		double R = Math.sqrt(x[0]*x[0]+x[1]*x[1]);
		double z = x[2];
		
		if( (angle_i-angle)<(angle_f-angle_i) && (R-R_i)<(R_f-R_i) && (z-z_i)<(z_f-z_i) )
			isInFid = true;
		
		return isInFid;
	}
	
	// in A (index 0), B (index 1), in C (index 2)
	public int isInDetector(int layer, double angle) {

		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		if(angle<0)
			angle+=2*Math.PI; // from 0 to 2Pi
		double angle_pr = angle + 2*Math.PI;
		
		double angle_i = 0; // first angular boundary init
		double angle_f = 0; // second angular boundary for detector A, B, or C init
		int num_detector = -1;

	    for(int i = 0; i<3; i++) {

	    	angle_i=Constants.getCRCEDGE1()[num_region][i]+Constants.getCRCXPOS()[num_region]/Constants.getCRCRADIUS()[num_region];
	    	angle_f=Constants.getCRCEDGE1()[num_region][i]+(Constants.getCRCXPOS()[num_region]+Constants.getCRCLENGTH()[num_region])/Constants.getCRCRADIUS()[num_region];

			if( (angle>=angle_i && angle<=angle_f) || (angle_pr>=angle_i && angle_pr<=angle_f) )
				num_detector=i;
	    }
	    return num_detector ; 
	}

	public int isInSector(int layer, double angle) {

		int value = -1;
		int num_det = this.isInDetector(layer, angle);
		if(num_det == 0)
			value = 2;
		if(num_det ==2)
			value = 3;
		if(num_det == 1)
			value = 1;
		
		return value;
	}

	public double LorentzAngleCorr(double phi, int layer) {

		int num_region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		
		return phi +( Constants.hDrift/2*Math.tan(Constants.getThetaL()) )/Constants.getCRZRADIUS()[num_region];
		
	}
	
	// Correct strip position before clustering
	public int getLorentzCorrectedZStrip(int sector, int layer, int theMeasuredZStrip) {
		
		 double theMeasuredPhi = this.CRZStrip_GetPhi(sector, layer, theMeasuredZStrip);
		 double theLorentzCorrectedAngle = this.LorentzAngleCorr( theMeasuredPhi, layer);
		 int theLorentzCorrectedStrip = this.getZStrip(5, theLorentzCorrectedAngle);
		 
		 return theLorentzCorrectedStrip;
	}


	public boolean isInFiducial(double x, double y, double z, int layer ) {

		boolean isOK = false;
		
//		int num_region = (int) (layer+1)/2 - 1;
//	
//		int axis = Geometry.getZorC(layer);
//		
//		double R = 0;
//		if(axis==0)
//			R = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[num_region];
//		if(axis==1)
//			R = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region];
//		
//		double CRZLENGTH	=	org.jlab.rec.cvt.bmt.Constants.getCRCLENGTH()[num_region];
//		double CRZZMIN		=	org.jlab.rec.cvt.bmt.Constants.getCRZZMIN()[num_region];
//		double CRZOFFSET	=	org.jlab.rec.cvt.bmt.Constants.getCRZOFFSET()[num_region];
//		
//		double z_min = CRZZMIN+CRZOFFSET;
//		double z_max = z_min + CRZLENGTH;
//		
//
//		double epsilon=1e-1;
//		
//		if(Math.abs(x)<R+epsilon && Math.abs(y)<R+epsilon && z>z_min-epsilon && z<z_max+epsilon ) {
//			isOK = true;
//		}
		return isOK;
	}

	public static final synchronized int getZorC(int layer) {
		int axis = 0; //C
		if(layer==2 || layer==3 || layer==5)
			axis = 1; //Z
		return axis;
	}
	
	public boolean isCLayer(int layer) {
		return !isZLayer(layer);
	}
	
	public boolean isZLayer(int layer) {
		return ((layer == 2) || (layer == 3) || (layer == 5));
	}
	
	public void getCRZEndPoints(int sector, int layer, int strip, float[] coords) {
		int region = (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
		int numStrips =  Constants.getCRZNSTRIPS()[region];
		
		if ((strip < 1) || (strip > numStrips)) {
			for (int i = 0; i < 6; i++) {
				coords[i] = Float.NaN;
			}
			return;
		}
		
		double phi = CRZStrip_GetPhi(sector, layer, strip);
		double r = Constants.getCRZRADIUS()[region] + Constants.LYRTHICKN/2;
		double x = r*Math.cos(phi)/10;
		double y = r*Math.sin(phi)/10;
		double zMin = Constants.getCRZZMIN()[region]/10;
		double zMax = Constants.getCRZZMAX()[region]/10;
		coords[0] =(float)x;
		coords[1] =(float)y;
		coords[2] = (float)zMin;
		coords[3] =(float)x;
		coords[4] =(float)y;
		coords[5] = (float)zMax;

	}
	
	public void getCRCEndPoints(int sector, int layer, int strip, float[] coords) {
//		int region = (int) (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
//		int numStrips =  Constants.getCRZNSTRIPS()[region];
//		
//		if ((strip < 1) || (strip > numStrips)) {
//			for (int i = 0; i < 6; i++) {
//				coords[i] = Float.NaN;
//			}
//			return;
//		}
//		
//		double phi1 = CRC_GetBeginStrip(sector, layer);
//		double phi2 = CRC_GetEndStrip(sector, layer);
//		double r = Constants.getCRCRADIUS()[region] + Constants.LYRTHICKN/2;
//		double x = r*Math.cos(phi)/10;
//		double y = r*Math.sin(phi)/10;
//		double zMin = Constants.getCRZZMIN()[region]/10;
//		double zMax = Constants.getCRZZMAX()[region]/10;
//		coords[0] =(float)x;
//		coords[1] =(float)y;
//		coords[2] = (float)zMin;
//		coords[3] =(float)x;
//		coords[4] =(float)y;
//		coords[5] = (float)zMax;

	}

	
	
public static void main (String arg[])  {
	
	ConstantsLoader.Load(10);
	Constants.Load();		
	Geometry geo = new Geometry();
	
	double trk_z = 0;
	
	int layer = 5;
	System.out.println(geo.CRCStrip_GetZ(6, 267)+" strip "+geo.getCStrip(6, -65.));
	/*
	int num_region = (int) (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6
	int strip_group = 0;
	int ClosestStrip =-1;
	System.out.println((""+1*1+""+1*0+""));
	// get group
	int len = Constants.CRCGROUP[num_region].length;
	double[] Z_lowBound = new double[len];
	double[] Z_uppBound = new double[len];
	int[] NStrips = new int[len];
	
	double zi= Constants.CRCZMIN[num_region]+Constants.CRCOFFSET[num_region];
	double z = trk_z - zi;
	
	Z_lowBound[0] = Constants.CRCWIDTH[num_region][0]/2.; // the lower bound is the zMin+theOffset with half the width
	Z_uppBound[0] = Z_lowBound[0]
					   + (Constants.CRCGROUP[num_region][0]-1)*(Constants.CRCWIDTH[num_region][0]+ Constants.CRCSPACING[num_region]);
	NStrips[0] = Constants.CRCGROUP[num_region][0];
	for(int i =1; i< len; i++)
	{
		Z_lowBound[i] = Z_uppBound[i-1] + Constants.CRCWIDTH[num_region][i-1]/2. + Constants.CRCSPACING[num_region] + Constants.CRCWIDTH[num_region][i]/2.;
		Z_uppBound[i] = Z_lowBound[i] + (Constants.CRCGROUP[num_region][i]-1)*(Constants.CRCWIDTH[num_region][i] + Constants.CRCSPACING[num_region]);
		
		NStrips[i] = NStrips[i-1] + Constants.CRCGROUP[num_region][i];
		
		if(z>=Z_lowBound[i] && z<=Z_uppBound[i]) {
			strip_group = i;
			ClosestStrip = 1 + (int) (Math.round(((z-Z_lowBound[strip_group])/(Constants.CRCWIDTH[num_region][strip_group] + Constants.CRCSPACING[num_region]))))+NStrips[i-1];

			len =i;
		} 
	}
	 double[] X = geo.smearedPosition(5, 0 , Constants.CRZRADIUS[2] , 0);
	 System.out.println(0+", "+(0.3+Constants.CRZRADIUS[2])+" , "+0+"  smeared "+X[0]+", "+X[1]+" , "+X[2]);
	 System.out.println(geo.getZStrip(5, Math.atan2(Constants.CRZRADIUS[2],0 )));
	 System.out.println(geo.getZStrip(5, Math.atan2(X[1],X[0])));
	 System.out.println(Math.toDegrees( geo.CRZStrip_GetPhi(1,6, geo.getZStrip(5, Math.atan2(X[1],X[0]))) ));	
	 int theMeasuredZStrip = geo.getZStrip(5, Math.atan2(X[1],X[0])); // start reco
	 double theMeasuredPhi = geo.CRZStrip_GetPhi(1,6,theMeasuredZStrip);
	 double theLorentzCorrectedAngle = geo.LorentzAngleCorr( theMeasuredPhi, 6);
	 System.out.println(" corrected phi = "+Math.toDegrees(theLorentzCorrectedAngle));
	 int theLorentzCorrectedStrip = geo.getZStrip(5, theLorentzCorrectedAngle);
	 System.out.println(theMeasuredZStrip+" "+theLorentzCorrectedStrip); */
	 /*
	 double phiC = geo.CRZStrip_GetPhi(3,6,216);
	 double x = Constants.CRCRADIUS[2]*Math.cos(phiC);
	 double y = Constants.CRCRADIUS[2]*Math.sin(phiC);
	 int theMeasuredCStrip = geo.getCStrip(6,X[2]);
	 double z = geo.CRCStrip_GetZ(6,309);
	 System.out.println(x+", "+y+", "+z);*/
	//List<double[]> Hits = geo.GEMCBMTHits(layer, sector, -199.89230321711165 , 93.78543124898611 , -164.52000000000007, .1);
	//System.out.println("There are "+Hits.size()+" hits in this cluster");
	//for(int i =0; i<Hits.size(); i++) {
	//	System.out.println(" strip "+(int)Hits.get(i)[0]+" Edep "+Hits.get(i)[1]);
	//}
}

}
