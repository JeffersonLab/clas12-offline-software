package org.jlab.rec.dc.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.GeometryLoader;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.trajectory.DCSwimmer;

import Jama.Matrix;

/**
 * The Kalman Filter algorithm makes use of the equations derived in the paper entitled
 * Optimized Integration of the Equations of Motion of a Particle in the HERA-B Magnet
 * by Alexander Spiridonov DESY Zeuthen / ITEP Moscow 
 *
 * @author ziegler
 *
 */

public class KalFit {

	// Speed of light in cm/ns
	public static final double C = 0.002997924580;


	public double GainScaleFac = 1.0; // to be optimized...

	
	/**
	 *  Field instantiated using the torus and the solenoid
	*/
	public DCSwimmer dcSwim = new DCSwimmer();
	/**
	 * The state vector is a 5-vector (x,y,px/pz,py/pz, q/p) evalutated at a constant z in the tilted sector coordinate system.	
	 */
	public double[] stateVec; 
	
	private double[][] stateJac; // the state vector Jacobian
	
	public Matrix covMat; // the track covariance matrix
	
	private Matrix noiseMat; // the noise matrix
	
	private double resVal; // the track residuals at the measurement sites
	
	
	private double[][] measVecs; // the array of measurements (1-D for fits to the wires, 4-D for fits to the crosses)
	private double[][] measVecErrs; // the errors on the measuremnts
	
	private int swimDir; // swim Direction
	public double chi2; // fit chi^2
	public int nbOfIterations = 1; // only one iteration is working at them moment 
	
	// the momentum after the Kalman fit
	public double KF_p = Double.NaN;
	public int KF_q = 0;
	public boolean KalFitFail = false;
	/**
	 * The constructor
	 * @param trkcand the track candidate
	 * @param fitsTo the fit method = fits to the wires is the default and only working method at the moment
	 */
	public KalFit(Track trkcand, String fitsTo) {
		
		if(trkcand==null)
			return;
		
		setMeasVecs(trkcand, fitsTo);
		
		double[] VecAtFirstMeasSite =null;
		
		if(trkcand.get_StateVecAtReg1MiddlePlane()!=null ) {
			dcSwim.SetSwimParameters(-1, trkcand.get_StateVecAtReg1MiddlePlane().x(),trkcand.get_StateVecAtReg1MiddlePlane().y(),trkcand.get(0).get_Point().z(),
					trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX(),trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY(),trkcand.get_P(),
					 trkcand.get_Q()); 
			KF_q = trkcand.get_Q();			
			
			VecAtFirstMeasSite = dcSwim.SwimToPlane(measVecs[0][0]);
		}
		
		
		if(VecAtFirstMeasSite==null) {
			KalFitFail = true;
			
			return;		
		}
		
		setStateVecFromTrackCand(trkcand.get_Q(), trkcand.get_P(), 
				VecAtFirstMeasSite[0],VecAtFirstMeasSite[1],
				VecAtFirstMeasSite[3]/VecAtFirstMeasSite[5],VecAtFirstMeasSite[4]/VecAtFirstMeasSite[5]);		
		
		/*setcovMat(VecAtFirstMeasSite[0],VecAtFirstMeasSite[1],VecAtFirstMeasSite[2], 
				-VecAtFirstMeasSite[3],-VecAtFirstMeasSite[4],-VecAtFirstMeasSite[5]); */
		//setcovMat(trkcand.get(0).get_PointErr().x(), trkcand.get(0).get_PointErr().y(), trkcand.get(0).get_DirErr().x(), trkcand.get(0).get_DirErr().y(), trkcand.get(0).get_DirErr().z(), trkcand.get_P());
		
		double val_sl1 = trkcand.get(0).get_Segment1().get_fittedCluster().get_clusterLineFitSlopeErr();
		double val_sl2 = trkcand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitSlopeErr();
		double wy_over_wx = (Math.cos(Math.toRadians(6.))/Math.sin(Math.toRadians(6.)));
		
		double eux = 0.5*Math.sqrt(val_sl1*val_sl1+val_sl2*val_sl2);
		double euy = 0.5*wy_over_wx*Math.sqrt(val_sl1*val_sl1+val_sl2*val_sl2);
		
		setcovMat(trkcand.get(0).get_PointErr().x(), trkcand.get(0).get_PointErr().y(), 
				eux, euy, 0.001*trkcand.get_P()*trkcand.get_P());
	}
	
	/**
	 * the step size - the default is 1 mm
	 */
	public double stepSize = 0.1;
	/**
	 * Runs the fit
	 */
	public void runKalFit() {
		
		int iterNb = 0;
		int nSteps = 100;			// some default
		if(measVecs[0].length<36-2*6) //we allow for 2 missing layers in each superlayer
			return;
		while(iterNb < nbOfIterations) {
			
			//going forward
			for(int i = 0; i < measVecs[0].length-1; i++) {
				nSteps = (int) (Math.abs(measVecs[0][i] - measVecs[0][i+1])/stepSize);
				
				transPortToPlane(nSteps,  measVecs[0][i], measVecs[0][i+1]);			
				
				filter(i); 						
			}
			
			iterNb++;
		}
		
		//transPortToPlane((int) (measVecs[0][0]/stepSize),  measVecs[0][0], 0);
		// transport the parameters to Region 3 middle plane
		double zR3MP = GeometryLoader.dcDetector.getSector(0).getRegionMiddlePlane(2).point().z();
		
		//transPortToPlane((int) (Math.abs(measVecs[0][0]-zR3MP)/stepSize),  measVecs[0][0], zR3MP);
		transPortToPlane((int) (Math.abs(measVecs[0][measVecs[0].length-1]-zR3MP)/stepSize),  measVecs[0][measVecs[0].length-1], zR3MP);
		//System.out.println(" KF trans R3 ");this.printInfo(stateVec);
		KF_p = 1./Math.abs(stateVec[4]);
		
		if(KF_p < Constants.MINTRKMOM) 
			return;
		KF_q = (int) Math.signum(stateVec[4]);
		
	}
	double thehitChisq = Double.POSITIVE_INFINITY;
	/**
	 * Fitlters the state and associated covariance matrix at measurement site i
	 * @param i the measurement site index
	 */
	public void filter(int i) {
		if(measVecs[0].length>23) { // require at least four hits in a given superlayer
			
			double[] K = new double[5];
			//double V = Constants.CELLRESOL; // 
			
			int i1 = i+1;
			// do this separately for each superlayer
			if(i1>measVecs[0].length-1) 
				i1 = measVecs[0].length-2;
			
			double V = measVecErrs[0][i1] ;
			double[] h =  h(stateVec, (int) measVecs[2][i1]);
			
			double[][] HTGH =  new double[][] {
					{h[0]*h[0]/V,h[0]*h[1]/V,0,0,0},
					{h[1]*h[0]/V,h[1]*h[1]/V,0,0,0},
					{0,0,0,0,0},
					{0,0,0,0,0},
					{0,0,0,0,0}
			}; 
			
			Matrix Ci = null;
			
			if(this.isNonsingular(covMat)==false) {
				//System.out.println("Covariance Matrix is non-invertible - quit filter!");
				return;
			}
			try {
				Ci = covMat.inverse(); 
			} catch (Exception e) {
				
				return;
				
			}
			
			Matrix Ca = null;
			try {
				Ca = Ci.plus(new Matrix(HTGH)); 
			} catch (Exception e) {
				return;
			}
			if(Ca!=null)
				if(this.isNonsingular(Ca)==false) {
					//System.out.println("Covariance Matrix is non-invertible - quit filter!");
					return;
				}
			if(Ca!=null) {
				if(Ca.inverse()!=null) {
				Matrix CaInv = Ca.inverse();
				covMat = CaInv; 
				//System.err.println("Error: e");
				} else {
				return;
				}
			} else {
				return;
			}
			
			for(int j = 0; j < 5; j++) {
				// the gain matrix
				K[j] = this.GainScaleFac*(h[0]*covMat.get(j, 0) + h[1]*covMat.get(j, 1))/V;		
			}
			resVal = measVecs[1][i1] - get_Proj(stateVec, (int) measVecs[2][i1]);
			
			double c2 = ((1 - (h[0]*K[0] + h[1]*K[1]))*(1 - (h[0]*K[0] + h[1]*K[1]))*resVal*resVal/V); 
			if(c2<thehitChisq) {
				thehitChisq = c2; 
				KF_p = 1./Math.abs(stateVec[4]); // temp patch ---> fix this.
			}
			chi2 += c2;
			
			if(chi2<10) {
				for(int j = 0; j < 5; j++) {
					// the filtered state
					stateVec[j]+=K[j]*resVal;
					
				}
			}
		}
	}

	/**
	 * prints the matrix -- used for debugging
	 * @param C matrix
	 */
	public void printMatrix(Matrix C) {
		System.out.println("------------------------------------------");
		for(int k = 0; k< 5; k++) {
			System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
		}
		System.out.println("------------------------------------------");
	}
	/**
	 * The state projector - it projects the state onto the measurement
	 * @param stateV the state vector
	 * @param s the superlayer of the measurement (0..1) 
	 * @return a double array correspoding to the 2 entries of the projector matrix
	 */
	private double[] h(double[] stateV, int s) {
		double[] hMatrix = new double[2];
		
		hMatrix[0] = 1;
		hMatrix[1] = - Math.tan((Math.toRadians(s*6.)));
		
		return hMatrix;
	}
	
	/**
	 * The projected measurement derived from the stateVector at the measurement site
	 * @param stateV the state vector
	 * @param s the superlayer (0..1)
	 * @return projected measurement
	 */
	private double get_Proj(double[] stateV, int s) {
		
		double val = stateV[0] - Math.tan((Math.toRadians(s*6.)))*stateV[1];
		return val;
	}

	/**
	 * Transport the state and associated Jacobian to the measurement site plane
	 * @param nsteps the number of steps used in swimming the track
	 * @param z0 the starting z 
	 * @param z the ending z
	 */
	public void transPortToPlane(int nsteps, double z0, double z) {
		
		double s = (z - z0)/(double) nsteps;
		
		swimDir = (int) Math.signum(s);
		
		double Z_i = z0;
		
		for(int i = 0; i< nsteps; i++) {

			transStateVec(stateVec[0],stateVec[1],stateVec[2],stateVec[3], stateVec[4], Z_i, s); 
			Q(stateVec[0],stateVec[1],stateVec[2],stateVec[3], stateVec[4], Z_i, s); 
			Z_i +=s;		
			
			propagateCovMat();
			if(swimDir>0)
				covMat.plusEquals(noiseMat);  
		}
	}
	
	
	/**
	 * Propagates the covariance matrix
	 */
	private void propagateCovMat() {
		if(this.getStateJac() == null || covMat == null)
			return;
		
		double[][] u = new double[5][5];
		double[][] c = new double[covMat.getRowDimension()][covMat.getColumnDimension()];		
		double[][] C = new double[covMat.getRowDimension()][covMat.getColumnDimension()];
		
		for(int rw = 0; rw< covMat.getRowDimension(); rw++) {
			for(int cl = 0; cl< covMat.getColumnDimension(); cl++) {
				c[rw][cl] = covMat.get(rw, cl);
			}
			
		}
		
		 //covMat = FCF^T; u = FC;
		for(int j = 0; j < 5; j++) {
			u[0][j] = c[0][j] + c[2][j]*stateJac[0][2] + c[3][j]*stateJac[0][3] + c[4][j]*stateJac[0][4];
			u[1][j] = c[1][j] + c[2][j]*stateJac[1][2] + c[3][j]*stateJac[1][3] + c[4][j]*stateJac[1][4];
			u[2][j] = c[2][j] + c[3][j]*stateJac[2][3] + c[4][j]*stateJac[2][4];
			u[3][j] = c[2][j]*stateJac[3][2] + c[3][j] + c[4][j]*stateJac[3][4];
			u[4][j] = c[4][j];		
		}
		
		for(int i = 0; i < 5; i++) {
			C[i][0] = u[i][0] + u[i][2]*stateJac[0][2] + u[i][3]*stateJac[0][3] + u[i][4]*stateJac[0][4];
			C[i][1] = u[i][1] + u[i][2]*stateJac[1][2] + u[i][3]*stateJac[1][3] + u[i][4]*stateJac[1][4];
			C[i][2] = u[i][2] + u[i][3]*stateJac[2][3] + u[i][4]*stateJac[2][4];
			C[i][3] = u[i][2]*stateJac[3][2] + u[i][3] + u[i][4]*stateJac[3][4];
			C[i][4] = u[i][4];
		}
		
		Matrix Cpropagated = new Matrix(C);	
		covMat = Cpropagated;	
		
	}
	/**
	 * prints the stateVector at a given z - used for debugging
	 * @param stateVec2 the state vector
	 */
	@SuppressWarnings("unused")
	private void printInfo(double[] stateVec2) {
		System.out.println("x^T = (x, y, tx, ty, P) = ("+stateVec2[0]+" ,"+stateVec2[1]+" ,"+stateVec2[2]+" ,"+stateVec2[3]+" ,"+1./stateVec2[4]+");");
	}

	/**
	 * At initialization time, this sets the starting stateVector parameters
	 * @param q the charge
	 * @param p the momentum
	 * @param x x
	 * @param y y
	 * @param tx ux/uz (ux,uy,uz) is the unit cross direction vector obtained from pattern recognition 
	 * @param ty uy/uz (ux,uy,uz) is the unit cross direction vector obtained from pattern recognition 
	 */
	private void setStateVecFromTrackCand(int q, double p, double x, double y, double tx, double ty) {
		stateVec = new double[] {x, y, tx, ty, (double)q/p };
	}

	/**
	 * transports the state vector and associated 
	 * @param x0
	 * @param y0
	 * @param tx0
	 * @param ty0
	 * @param Q
	 * @param z0
	 * @param s
	 */
	private void transStateVec(double x0, double y0, double tx0, double ty0, double Q, double z0, double s) {		
		
		double[] B = getFieldAt(x0,y0,z0); 
		double[] A = A(tx0,ty0,B[0],B[1],B[2]);
		
		double x = x0 + tx0*s + 0.5*Q*C*A[0]*s*s;
		double y = y0 + ty0*s + 0.5*Q*C*A[1]*s*s;
		double tx = tx0 + Q*C*A[0]*s;
		double ty = ty0 + Q*C*A[1]*s;
		
		double[] transpStateVec = new double[] {x,y,tx,ty,Q};	
		
		double[] dA = delA_delt(tx0,ty0,B[0],B[1],B[2]);
		
		double delx_deltx0 = s;
		double dely_deltx0 = 0.5*Q*C*s*s*dA[2];
		double deltx_delty0 = Q*C*s*dA[1];
		double delx_delQ = 0.5*C*s*s*A[0];
		double deltx_delQ = C*s*A[0];
		double delx_delty0 = 0.5*Q*C*s*s*dA[1];
		double dely_delty0 = s;
		double delty_deltx0 = Q*C*s*dA[2];
		double dely_delQ = 0.5*C*s*s*A[1];
		double delty_delQ = C*s*A[1];				
		
		double[][] transpStateJacobian =  new double[][] {
				{1,0,delx_deltx0,delx_delty0,delx_delQ},
				{0,1,dely_deltx0,dely_delty0,dely_delQ},
				{0,0,1,deltx_delty0,deltx_delQ},
				{0,0,delty_deltx0,1,delty_delQ},
				{0,0,0,0,1}
		}; 
		
		stateVec = transpStateVec;
		
		setStateJac(transpStateJacobian);
	}
	
	
	
	
	
	//delAx_deltx = delA_delt[0]; delAx_delty = delA_delt[1]; delAy_deltx = delA_delt[2]; delAy_delty = delA_delt[3];
	private double[] delA_delt(double tx, double ty, double Bx, double By, double Bz) {
		
		double C2 = 1 + tx*tx + ty*ty;
		double C = Math.sqrt(C2);
		double Ax = C*(ty*(tx*Bx + Bz) - (1+tx*tx)*By);
		double Ay = C*(-tx*(ty*By + Bz) + (1+ty*ty)*Bx);
		
		double delAx_deltx = tx*Ax/C2 +C*(ty*Bx - 2*tx*By);
		double delAx_delty = ty*Ax/C2 +C*(tx*Bx + Bz);
		double delAy_deltx = tx*Ay/C2 +C*(-ty*By - Bz);
		double delAy_delty = ty*Ay/C2 +C*(-tx*By + 2*ty*Bx);
		
		return new double[] {delAx_deltx,delAx_delty,delAy_deltx,delAy_delty};
	}
	
	// Ax = A[0]; Ay = A[1]
	private double[] A(double tx, double ty, double Bx, double By, double Bz) {
		
		double C = Math.sqrt(1 + tx*tx + ty*ty);
		double Ax = C*(ty*(tx*Bx + Bz) - (1+tx*tx)*By);
		double Ay = C*(-tx*(ty*By + Bz) + (1+ty*ty)*Bx);
		return new double[] {Ax, Ay};
	}
	
	private double[] getFieldAt(double x, double y, double z) {
		Point3D bf = dcSwim.Bfield(x,y,z);
	//System.out.println("x "+x+" y "+y+" z "+z+ " B = " +bf.toString());
		return new double[] {bf.x(), bf.y(),bf.z()};
	}

	public double[][] getStateJac() {
		return stateJac;
	}

	public void setStateJac(double[][] stateJac) {
		this.stateJac = stateJac;
	}
	
	
	public Matrix getcovMat() {
		return covMat;
	}

	public void setcovMat(double ex, double ey, double eux, double euy, double perr) { // use the uncertainty in the cross in region 1 to determine the init covMat
		//public void setcovMat(double x, double y, double z, double px, double py, double pz) {
		//double p2 = (px*px+py*py+pz*pz);
		// the error matrix is estimated from the uncertainty in the cross in region 1
        //setcovMat(trkcand.get(0).get_PointErr().x(), trkcand.get(0).get_PointErr().y(), trkcand.get(0).get_DirErr().x(), trkcand.get(0).get_DirErr().y(), trkcand.get(0).get_DirErr().z(), trkcand.get_P());
		// the error matrix is estimated from the uncertainty in the cross in region 1
				
				
				covMat = new Matrix( new double[][]{
						{ex*ex,  			0, 									0,         						  	0,         							 	0},
						{0, 				ey*ey, 								0,         							0,         								0},
						{0, 				0, 		  							eux*eux,							0,										0},
						{0, 				0, 									0, 									euy*euy, 							    0},
						{0, 				0, 									0,         							0,							            perr}
				});
		/*
		double p2 = p*p;
		covMat = new Matrix( new double[][]{
				{ex*ex,  			0, 									0,         						  	0,         							 	0},
				{0, 				ey*ey, 								0,         							0,         								0},
				{0, 				0, 		  							etx*etx,							0,										0},
				{0, 				0, 									0, 									ety*ety, 							    0},
				{0, 				0, 									0,         							0,							           0.25*p2}
		});*/
			/*
			 * covMat = new Matrix( new double[][]{
					{0.0025,  			0, 									0,         						  	0,         						 	0},
					{0, 				0.0625, 							0,         							0,         							0},
					{0, 				0, 		  							0.000004,	//0.0001,							0,									0},
					{0, 				0, 									0, 									0.01, 							    0},
					{0, 				0, 									0,         							0,							        0.001*p2}
			});
			 */
			
	}

	public double[][] getMeasVecs() {
		return measVecs;
	}

	public double[][] getMeasVecErrs() {
		return measVecErrs;
	}
	
   private class HitOnTrack implements Comparable<HitOnTrack> {
	   private double _X;
	   private double _Z;
	   private int _superlayer;
	   public double hitError = Constants.CELLRESOL;
	   
	   public HitOnTrack(int superlayer, double X, double Z) {
		   _X = X;
		   _Z = Z;
		   _superlayer = superlayer;
	   }
        
		@Override
		public int compareTo(HitOnTrack o) {
			if(this._Z == o._Z)
				return 0;
			if(this._Z>o._Z) {
				return 1;
			} else {
				return -1;
			}
		}		
    }

	/**
	 * Sets the arrays of measurement vectors using the track candidate trajectory which contains the information on the detector planes on which a signal was recorded.
	 * @param trkcand the track candidate
	 * @param fitsTo the string indicating if the measurements are the individual wires  or the pattern recognition crosses (i.e. "wires" or "crosses" )
	 */
	public void setMeasVecs(Track trkcand, String fitsTo) {
		
		if(fitsTo.equals("wires")) {
			// 36 planes measuring x & z  // allowing for double hits gives a factor of 10*36 in the array
			double[] X = new double[10*36];
			double[] Z = new double[10*36];
			
			FittedHit hitOnTrk;
			
			int index = 0;
			List<HitOnTrack> hOTS = new ArrayList<HitOnTrack>(); // the list of hits on track
			
			// loops over the regions (1 to 3) and the superlayers in a region (1 to 2) and obtains the hits on track
			for(int c = 0; c<3; c++) { 
				for(int s =0; s<2; s++) {
					for(int h =0; h<trkcand.get(c).get(s).size(); h++) {
						
						hitOnTrk = trkcand.get(c).get(s).get(h);
						int slayr = trkcand.get(c).get(s).get(h).get_Superlayer();
						//X[index] = hitOnTrk.get_X();
						Z[index] = hitOnTrk.get_Z(); 
						X[index] = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlope()*Z[index]
								+ trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitIntercept();
						// make the hit on track and add it to the list
						HitOnTrack hot = new HitOnTrack(slayr, X[index], Z[index]);
						//hot.hitError = trkcand.get(c).get(s).get(h).get_DocaErr()/Math.cos(Math.toRadians(6.)); 
						
						hot.hitError = Math.sqrt(trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlopeErr()*Z[index]*trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlopeErr()*Z[index]
								+ trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitInterceptErr()*trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitInterceptErr());
						//hot.hitError = trkcand.get(c).get(s).get(h).get_CellSize()/Math.sqrt(12)/Math.cos(Math.toRadians(6.));
						hOTS.add(hot);
						
						index++;	
					}
				}
			}
			
			int mmShift =0;
			if(trkcand.get_MicroMegasPointsList()!=null) 
				mmShift = trkcand.get_MicroMegasPointsList().size();
								
			
			Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)
			// measVecs is a double array where measVecs[0][i] is the Z component of the coordinate of hit i and measVecs[1][i] is the X component of the coordinate of hit i
			measVecs = new double[2][index+mmShift]; 
			measVecErrs = new double[1][index+mmShift];
			
			// identify double hits and take the average position		
			for(int i = 0; i<hOTS.size(); i++) {
				measVecs[0][i] = hOTS.get(i)._Z;
				measVecs[1][i] = hOTS.get(i)._X;
				measVecErrs[0][i] = hOTS.get(i).hitError;
				if( i > 0 ) {
					if(measVecs[0][i-1] == measVecs[0][i]) {
						//measVecs[1][i-1] = (measVecs[1][i-1] + measVecs[1][i])/2.;
						// uncertainty - weighted average measurement
						measVecs[1][i-1] = (measVecs[1][i-1]/(measVecErrs[0][i-1]*measVecErrs[0][i-1]) + measVecs[1][i]/(measVecErrs[0][i]*measVecErrs[0][i]) )/(1./(measVecErrs[0][i-1]*measVecErrs[0][i-1]) + 1./(measVecErrs[0][i]*measVecErrs[0][i]) );
						//measVecErrs[0][i-1] = Math.sqrt((measVecErrs[0][i-1]*measVecErrs[0][i-1] + measVecErrs[0][i]*measVecErrs[0][i])/2.);
						// uncertainty for measurement that is sigma weighted
						measVecErrs[0][i-1] = 1./Math.sqrt(1./(measVecErrs[0][i-1]*measVecErrs[0][i-1]) + 1./(measVecErrs[0][i]*measVecErrs[0][i]) );
						
						hOTS.remove(i); 
						// rescale the hit error accordingly
						//hOTS.get(hOTS.size()-1).hitError/=Math.sqrt(2.);
					}
				}
			}
			
			// add component 3 for the tilt of the wire corresponding to hit.  This is used to project the state vector onto the measurement plane.
			measVecs = new double[3][hOTS.size()+mmShift]; 
			for(int i = 0; i<hOTS.size(); i++) {
				
				measVecs[0][i+mmShift] = hOTS.get(i)._Z;
				measVecs[1][i+mmShift] = hOTS.get(i)._X;			
				measVecErrs[0][i+mmShift] = hOTS.get(i).hitError;
				// this is the tilt along the wire = +/- 6 degrees
				int SL = hOTS.get(i)._superlayer;
				
				int s = (int) SL%2;
				int tilt = 1;
				if(s == 0 )
					tilt = -1;
				
				measVecs[2][i+mmShift] = tilt;
				
			}
			if(trkcand.get_MicroMegasPointsList()!=null && trkcand.get_MicroMegasPointsList().size()!=0) {
				for(int i = 0; i<mmShift; i++) {
	
					double[] V = new double[2];
					V[0] = trkcand.get_MicroMegasPointsList().get(i).x(); 
					V[1] = trkcand.get_MicroMegasPointsList().get(i).y(); 
					
					measVecs[0][i] = trkcand.get_MicroMegasPointsList().get(i).z();
					measVecs[1][i] = get_Proj(V, 1);
					measVecErrs[0][i] = Constants.CELLRESOL;
					measVecs[2][i] =0;			
				
				}
			}
			
		}
		if(fitsTo.equals("crosses")) {
			measVecs = new double[5][4];
			measVecErrs = new double[4][4];
			
			double[] X = new double[3];
			double[] Y = new double[3];
			double[] tX = new double[3];
			double[] tY = new double[3];
			double[] Z = new double[3];
			
			double[] XErr = new double[3];
			double[] YErr = new double[3];
			double[] tXErr = new double[3];
			double[] tYErr = new double[3];
			
			for(int i = 0; i<3; i++) {
				
				X[i] = trkcand.get(i).get_Point().x();
				Y[i] = trkcand.get(i).get_Point().y();
				tX[i] = trkcand.get(i).get_Dir().x()/trkcand.get(i).get_Dir().z();
				tY[i] = trkcand.get(i).get_Dir().y()/trkcand.get(i).get_Dir().z();
				Z[i] = trkcand.get(i).get_Point().z();
				
				XErr[i] = trkcand.get(i).get_PointErr().x();
				YErr[i] = trkcand.get(i).get_PointErr().y();
				tXErr[i] = propagatedError(trkcand.get(i).get_Dir().x(), trkcand.get(i).get_Dir().z(),
						trkcand.get(i).get_DirErr().x(), trkcand.get(i).get_DirErr().z());
				tYErr[i] = propagatedError(trkcand.get(i).get_Dir().y(), trkcand.get(i).get_Dir().z(),
						trkcand.get(i).get_DirErr().y(), trkcand.get(i).get_DirErr().z());
				
			}
			
			// 3 planes measuring x,y,tx,ty
			for(int i = 0; i < 3; i++) {
				measVecs[0][i+1] = Z[i];
				measVecs[1][i+1] = X[i];
				measVecs[2][i+1] = Y[i];
				measVecs[3][i+1] = tX[i];
				measVecs[4][i+1] = tY[i];
				
				measVecErrs[0][i+1] = 1./(XErr[i]*XErr[i]);
				measVecErrs[1][i+1] = 1./(YErr[i]*YErr[i]);
				measVecErrs[2][i+1] = 1./(tXErr[i]*tXErr[i]);
				measVecErrs[3][i+1] = 1./(tYErr[i]*tYErr[i]);
			}
			
			measVecs[0][0] = 0;
			measVecs[1][0] = 0;
			measVecs[2][0] = 0;
			measVecs[3][0] = X[1]/Z[1];
			measVecs[4][0] = Y[1]/Z[1];
			
			measVecErrs[0][0] = 1./12;
			measVecErrs[1][0] = 1./12;
			measVecErrs[2][0] = 1./(tXErr[1]*tXErr[1]);
			measVecErrs[3][0] = 1./(tYErr[1]*tYErr[1]);
		}
		
	}
	
	/**
	 * The process noise covariance matrix for a given state vector  (x0, y0, tx0, ty0, Q_ov_P).
	 * This method sets the matrix Q at a given measurement site
	 * @param x0
	 * @param y0
	 * @param tx0
	 * @param ty0
	 * @param Q_ov_P
	 * @param z0 the starting z position
	 * @param s the step size
	 */
	public void Q(double x0, double y0, double tx0, double ty0, double Q_ov_P, double z0, double s) { // noise  matrix

	    double p = Math.abs(1./Q_ov_P);
	    double pz = p/Math.sqrt(1 + tx0*tx0 + ty0*ty0);
	    double px = tx0*pz;
	    double py = ty0*pz;
	  
	    double t_ov_X0 = stepSize/Constants.ARGONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm
	    
	    double mass = MassHypothesis("electron"); // assume given mass hypothesis
	    if(Q_ov_P>0)
	    	mass = MassHypothesis("proton");
	    double beta = p/Math.sqrt(p*p+mass*mass); // use particle momentum
	    double cosEntranceAngle = Math.abs((x0*px+y0*py+z0*pz)/(Math.sqrt(x0*x0+y0*y0+z0*z0)*p));
	    double pathLength = t_ov_X0/cosEntranceAngle;  
	   
	    double sctRMS = (0.0136/(beta*p))*Math.sqrt(pathLength)*(1+0.038*Math.log(pathLength)); // Highland-Lynch-Dahl formula
	  
	    double cov_txtx = (1+tx0*tx0)*(1 + tx0*tx0 + ty0*ty0)*sctRMS*sctRMS;
	    double cov_tyty = (1+ty0*ty0)*(1 + tx0*tx0 + ty0*ty0)*sctRMS*sctRMS;
	    double cov_txty = tx0*ty0*(1 + tx0*tx0 + ty0*ty0)*sctRMS*sctRMS;
	   
	    Matrix Q = new Matrix( new double[][]{
				{0,     			0, 									0,   	  	0,        0},
				{0, 				0,  						    	0,        0,        0},
				{0, 				0, 		  							cov_txtx,							cov_txty,							0},
				{0, 				0, 									cov_txty, 							cov_tyty, 							0},
				{0, 				0, 									0,         							0,							        0}
		});
	    noiseMat = Q;
	}
    
	 
	/**
	 *  
	 * @param H a string corresponding to the mass hypothesis - the pion mass hypothesis is the default value
	 * @return the mass value for the given mass hypothesis in GeV/c^2
	 */
    public double MassHypothesis(String H) {
  	   double value = piMass; //default
  	   if(H.equals("proton"))
  		  value = pMass;
  	   if(H.equals("electron"))
  		  value = eMass;
  	   if(H.equals("pion"))
  		  value = piMass;
  	   if(H.equals("kaon"))
  		  value = KMass;
  	   if(H.equals("muon"))
  		  value = muMass;
  	return value;
     }
	
    /**
     * Propagates the error on x/z (x = X or Y coordinate) given the error in x and z
     * @param x
     * @param z
     * @param dx - error in x
     * @param dz - error in z
     * @return error in x/z
     */
	private double propagatedError(double x, double z, double dx, double dz) {
		// error on x/z
		if(z == 0) 
			return Double.POSITIVE_INFINITY;
		
		double term1 = (x/z)*dz;
		double term2 = dx;
		
		return (1/z)*Math.sqrt(term1*term1 + term2*term2);
	}
	
	private boolean isNonsingular(Matrix mat) {
		
	      for (int j = 0; j < mat.getColumnDimension(); j++) {
	         if (mat.get(j, j) == 0)
	            return false;
	      }
	      return true;
	   }
	
	
	 static double piMass = 0.13957018;
	 static double KMass  = 0.493677;
	 static double muMass = 0.105658369;
	 static double eMass  = 0.000510998;
	 static double pMass  = 0.938272029;
}
