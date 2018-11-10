package org.jlab.rec.dc.track.fit;

//import org.jlab.rec.dc.GeometryLoader;
import org.jlab.geom.prim.Vector3D;

import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.track.Track;

import org.jlab.rec.dc.track.fit.GStateVecs;
import org.jlab.rec.dc.track.fit.GStateVecs.GStateVec;
import org.jlab.rec.dc.track.fit.GMeasVecs;
import org.jlab.rec.dc.track.fit.GMeasVecs.GMeasVec;

import Jama.Matrix;

//import org.jlab.rec.dc.track.fit.StateVecs.StateVec;

public class GFitter {

	public boolean setFitFailed = false;
	
	private GStateVecs sv ;
	private GMeasVecs mv = new GMeasVecs();
	private DCGeant4Factory DcDetector;
	
	public GStateVec finalStateVec;
	
	private Swim dcSwim;
	private double TORSCALE;
	private boolean debug_gfit=false;
	//private boolean debug_gfit=true;
	
	// save track crossing layers
	//public List<org.jlab.rec.dc.trajectory.StateVec> kfStateVecsAlongTrajectory;
	
	public GFitter(int sector, Track trk, DCGeant4Factory DcDetector_init,
                   boolean TimeBasedUsingHBtrack,
                   Swim swimmer, double torus_bfiled_scale) {
		 sv = new GStateVecs(swimmer);
		 dcSwim=swimmer;
		 TORSCALE = torus_bfiled_scale;
		this.init(sector, trk, DcDetector_init);
	}

	public void init(int sector, Track trk, DCGeant4Factory DcDetector_init) {
		DcDetector = DcDetector_init;
		mv.setGMeasVecs(trk,DcDetector);
		sv.Z = new double[mv.measurements.size()];
		
		for(int i =0; i< mv.measurements.size(); i++) {
			//sv.Z[i] = mv.measurements.get(i).z;
			int superlayer=mv.measurements.get(i).isl;
			int layer=mv.measurements.get(i).ilayer;
			int wire=mv.measurements.get(i).iwire;
			sv.Z[i] = DcDetector.getWireMidpoint(sector-1,mv.measurements.get(i).isl-1, mv.measurements.get(i).ilayer-1,  mv.measurements.get(i).iwire-1).z;
			
			// MO: we don't have error yet here at HTB, use cell size
			//mv.measurements.get(i).error=0.8;
			if(i>0) mv.measurements.get(i).error=(sv.Z[i]-sv.Z[i-1])/Math.cos(Math.toRadians(30))/2;
		}
		mv.measurements.get(0).error=(sv.Z[1]-sv.Z[0])/Math.cos(Math.toRadians(30))/2;
		
		sv.init(sector, trk, sv.Z[0], this);
	}
	
	//public int totNumIter = 10;
	public int totNumIter = 5;
	//public int totNumIter = 1; // for test use single iteration (for more need to reset sv.trackCov.get(0).covMat)
	double newChisq = Double.POSITIVE_INFINITY;
	
	double[] pdoca  = new double[5];
	
	public void runFitter(int sector) {
		this.chi2 = 0;
		this.NDF = sv.Z.length-5;

		double[] vx = new double[sv.Z.length];
		double[] weight = new double[sv.Z.length];
		double[][] dtrsp = new double[5][sv.Z.length];


if(debug_gfit) System.out.println(" GFit.initial: " +sector
+" , "+ sv.trackTraj.get(0).x
+" , "+ sv.trackTraj.get(0).y
+" , "+ sv.trackTraj.get(0).z
+" , "+ sv.trackTraj.get(0).tx
+" , "+ sv.trackTraj.get(0).ty
+" , "+ sv.trackTraj.get(0).Q
);

		for(int i =1; i<=totNumIter; i++) {
		  double chi2_before=0;
			if(i>1) sv.transport(sector,sv.Z.length-1, 0, sv.trackTraj.get(sv.Z.length-1), sv.trackCov.get(sv.Z.length-1)); //get new state vec at 1st measurement after propagating back from the last filtered state

// MO: reset propagation matrix
              /*Matrix initCMatrix = new Matrix( new double[][]{
				{    1,       0,       0,       0,  0},
				{    0,       1,       0,       0,  0},
				{    0,       0,       1,       0,  0},
				{    0,       0,       0,       1,  0},
				{    0,       0,       0,       0,  1}
		});
		CovMat initCM = new CovMat(0);
		initCM.covMat = initCMatrix;
		sv.trackCov.put(0, initCM);*/
/*for(int k=0; k<sv.Z.length-1; k++){
 for(int k1=0; k1<sv.Z.length-1; k1++){
  if(k==k1) sv.trackCov.get(0).covMat.set(0,0,1);
  else sv.trackCov.get(0).covMat.set(0,0,0);
 }
}*/
//printMatrix(sv.trackCov.get(0).covMat);


			for(int k=0; k<sv.Z.length; k++) { // transport from layer=0 to last measured
				if(k<sv.Z.length-1) sv.transport(sector, k, k+1, sv.trackTraj.get(k), sv.trackCov.get(k));

//if(k<sv.Z.length-1) System.out.println(" sv.Z: " +k+" , "+sv.Z[k]+" , "+sv.trackTraj.get(k).z+" , "+sv.trackTraj.get(k+1).z);
//printMatrix(sv.trackCov.get(k).covMat);


				double cnorm = Math.sqrt(1 + sv.trackTraj.get(k).tx*sv.trackTraj.get(k).tx + sv.trackTraj.get(k).ty*sv.trackTraj.get(k).ty);
				
				//double x_wire = DcDetector.getWireMidpoint(sector-1, mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x;
				
				//Vector3D wire_pos = new Vector3D(
				//DcDetector.getWireMidpoint(sector-1, mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x,
				//DcDetector.getWireMidpoint(sector-1, mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).y,
				//DcDetector.getWireMidpoint(sector-1, mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).z);
				//int superlayer=mv.measurements.get(k).isl;
			        //int layer=mv.measurements.get(k).ilayer;
			        //int wire=mv.measurements.get(k).iwire;
				//double xw=DcDetector.getWireMidpoint(sector-1,superlayer-1,layer-1,wire-1).x;
				//double xw=DcDetector.Wire(sector-1,superlayer-1,layer-1,wire-1).mid().x();
				
				/*Vector3D t_pos=new Vector3D( // track position at intersection with current layer
				sv.trackTraj.get(k).x,
				sv.trackTraj.get(k).y,
				sv.trackTraj.get(k).z);
				Vector3D t_dir=new Vector3D( // track direction at intersection with current layer
				sv.trackTraj.get(k).tx/cnorm,
				sv.trackTraj.get(k).ty/cnorm,
				1./cnorm);*/
				
				double doca =  this.doca(
				new Vector3D( // wire position in sector tilted frame
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x,
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).y,
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).z),
				new Vector3D( // wire direction in sector tilted frame
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x,
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).y,
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).z),
				DcDetector.getWireDirection(mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x,
				DcDetector.getWireDirection(mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).y,
				DcDetector.getWireDirection(mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).z),
				new Vector3D( // track position at intersection with current layer
				sv.trackTraj.get(k).x,
				sv.trackTraj.get(k).y,
				sv.trackTraj.get(k).z),
				new Vector3D( // track direction at intersection with current layer
				sv.trackTraj.get(k).tx/cnorm,
				sv.trackTraj.get(k).ty/cnorm,
				1./cnorm)
				);
				chi2_before+= (mv.measurements.get(k).drift_dist - doca)*(mv.measurements.get(k).drift_dist - doca)/mv.measurements.get(k).error/mv.measurements.get(k).error;

if(debug_gfit) System.out.println(" GFit.Before(Sec,SL,layer,wire): " +sector
+" , "+ mv.measurements.get(k).isl
+" , "+ mv.measurements.get(k).ilayer
+" , "+ mv.measurements.get(k).iwire
+ " doca = " +doca+ " drift_dist = " + mv.measurements.get(k).drift_dist
+ " error = " +mv.measurements.get(k).error
+ " sv.x = " +sv.trackTraj.get(k).x+ " sv.y = " +sv.trackTraj.get(k).y+ " sv.z = " +sv.trackTraj.get(k).z
+ " mv.x = " +DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).x
+ " mv.y = " +DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).y
+ " mv.z = " +DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k).isl-1, mv.measurements.get(k).ilayer-1,  mv.measurements.get(k).iwire-1).z
);

/*System.out.println(" drvs(k): " +k
+" , "+ this.pdoca[0]
+" , "+ this.pdoca[1]
+" , "+ this.pdoca[2]
+" , "+ this.pdoca[3]
+" , "+ this.pdoca[4]
);*/

				//mv.h(new double[]{sv.trackTraj.get(k).x,sv.trackTraj.get(k).y}, (int) mv.measurements.get(k).tilt);
				
				// here covMat = Product_i=0^i=k (dq_i+1/dq_i) = dq_k+1/dq_0
				
				// TRANSFORMATION MATRIX BETWEEN ERRORS AT k=0 AND k=k+1 = partial derivatives:
				// -d(DOCA_fit_k)/dq_j, where q_j=(x0,y0,tx0,ty0,Q) is parameter vector
				if(k>0){
				  dtrsp[0][k] = this.pdoca[0]*sv.trackCov.get(k).covMat.get(0, 0) //  d(DOCA_fit_k)/dx_k * dx_k/dx_0
				              + this.pdoca[1]*sv.trackCov.get(k).covMat.get(1, 0) //  d(DOCA_fit_k)/dy_k * dy_k/dx_0
				              + this.pdoca[2]*sv.trackCov.get(k).covMat.get(2, 0) //  d(DOCA_fit_k)/dtx_k * dtx_k/dx_0
				              + this.pdoca[3]*sv.trackCov.get(k).covMat.get(3, 0) //  d(DOCA_fit_k)/dty_k * dty_k/dx_0
				              + this.pdoca[4]*sv.trackCov.get(k).covMat.get(4, 0);//  d(DOCA_fit_k)/dQ_k * dQ_k/dx_0
					      
				  dtrsp[1][k] = this.pdoca[0]*sv.trackCov.get(k).covMat.get(0, 1) //  d(DOCA_fit_k)/dx_k * dx_k/dy_0
				              + this.pdoca[1]*sv.trackCov.get(k).covMat.get(1, 1) //  d(DOCA_fit_k)/dy_k * dy_k/dy_0
				              + this.pdoca[2]*sv.trackCov.get(k).covMat.get(2, 1) //  d(DOCA_fit_k)/dtx_k * dtx_k/dy_0
				              + this.pdoca[3]*sv.trackCov.get(k).covMat.get(3, 1) //  d(DOCA_fit_k)/dty_k * dty_k/dy_0
				              + this.pdoca[4]*sv.trackCov.get(k).covMat.get(4, 1);//  d(DOCA_fit_k)/dQ_k * dQ_k/dy_0
				
                                  dtrsp[2][k] = this.pdoca[0]*sv.trackCov.get(k).covMat.get(0, 2) //  d(DOCA_fit_k)/dx_k * dx_k/dtx_0
				              + this.pdoca[1]*sv.trackCov.get(k).covMat.get(1, 2) //  d(DOCA_fit_k)/dy_k * dy_k/dtx_0
				              + this.pdoca[2]*sv.trackCov.get(k).covMat.get(2, 2) //  d(DOCA_fit_k)/dtx_k * dtx_k/dtx_0
				              + this.pdoca[3]*sv.trackCov.get(k).covMat.get(3, 2) //  d(DOCA_fit_k)/dty_k * dty_k/dtx_0
				              + this.pdoca[4]*sv.trackCov.get(k).covMat.get(4, 2);//  d(DOCA_fit_k)/dQ_k * dQ_k/dtx_0
				
                                  dtrsp[3][k] = this.pdoca[0]*sv.trackCov.get(k).covMat.get(0, 3) //  d(DOCA_fit_k)/dx_k * dx_k/dty_0
				              + this.pdoca[1]*sv.trackCov.get(k).covMat.get(1, 3) //  d(DOCA_fit_k)/dy_k * dy_k/dty_0
				              + this.pdoca[2]*sv.trackCov.get(k).covMat.get(2, 3) //  d(DOCA_fit_k)/dtx_k * dtx_k/dty_0
				              + this.pdoca[3]*sv.trackCov.get(k).covMat.get(3, 3) //  d(DOCA_fit_k)/dty_k * dty_k/dty_0
				              + this.pdoca[4]*sv.trackCov.get(k).covMat.get(4, 3);//  d(DOCA_fit_k)/dQ_k * dQ_k/dty_0
				
                                  dtrsp[4][k] = this.pdoca[0]*sv.trackCov.get(k).covMat.get(0, 4) //  d(DOCA_fit_k)/dx_k * dx_k/dQ_0
				              + this.pdoca[1]*sv.trackCov.get(k).covMat.get(1, 4) //  d(DOCA_fit_k)/dy_k * dy_k/dQ_0
				              + this.pdoca[2]*sv.trackCov.get(k).covMat.get(2, 4) //  d(DOCA_fit_k)/dtx_k * dtx_k/dQ_0
				              + this.pdoca[3]*sv.trackCov.get(k).covMat.get(3, 4) //  d(DOCA_fit_k)/dty_k * dty_k/dQ_0
				              + this.pdoca[4]*sv.trackCov.get(k).covMat.get(4, 4);//  d(DOCA_fit_k)/dQ_k * dQ_k/dQ_0
				} else { // at first hit we don't need to transport => matrix is unitary
				 dtrsp[0][k] = this.pdoca[0];
				 dtrsp[1][k] = this.pdoca[1];
				 dtrsp[2][k] = this.pdoca[2];
				 dtrsp[3][k] = this.pdoca[3];
				 dtrsp[4][k] = this.pdoca[4];
				}
// check transportation matrix
if(debug_gfit) {
if(k>0){
System.out.println(" covMat: " +k
+" ,x= "+ ( sv.trackTraj.get(0).x *sv.trackCov.get(k).covMat.get(0, 0)
           +sv.trackTraj.get(0).y *sv.trackCov.get(k).covMat.get(0, 1)
	   +sv.trackTraj.get(0).tx*sv.trackCov.get(k).covMat.get(0, 2)
	   +sv.trackTraj.get(0).ty*sv.trackCov.get(k).covMat.get(0, 3)
	   +sv.trackTraj.get(0).Q *sv.trackCov.get(k).covMat.get(0, 4)
	   )+" , "+ sv.trackTraj.get(k).x
+" ,y= "+ ( sv.trackTraj.get(0).x *sv.trackCov.get(k).covMat.get(1, 0)
           +sv.trackTraj.get(0).y *sv.trackCov.get(k).covMat.get(1, 1)
	   +sv.trackTraj.get(0).tx*sv.trackCov.get(k).covMat.get(1, 2)
	   +sv.trackTraj.get(0).ty*sv.trackCov.get(k).covMat.get(1, 3)
	   +sv.trackTraj.get(0).Q *sv.trackCov.get(k).covMat.get(1, 4)
	   )+" , "+ sv.trackTraj.get(k).y
+" ,tx= "+ ( sv.trackTraj.get(0).x*sv.trackCov.get(k).covMat.get(2, 0)
           +sv.trackTraj.get(0).y *sv.trackCov.get(k).covMat.get(2, 1)
	   +sv.trackTraj.get(0).tx*sv.trackCov.get(k).covMat.get(2, 2)
	   +sv.trackTraj.get(0).ty*sv.trackCov.get(k).covMat.get(2, 3)
	   +sv.trackTraj.get(0).Q *sv.trackCov.get(k).covMat.get(2, 4)
	   )+" , "+ sv.trackTraj.get(k).tx
+" ,ty= "+ ( sv.trackTraj.get(0).x*sv.trackCov.get(k).covMat.get(3, 0)
           +sv.trackTraj.get(0).y *sv.trackCov.get(k).covMat.get(3, 1)
	   +sv.trackTraj.get(0).tx*sv.trackCov.get(k).covMat.get(3, 2)
	   +sv.trackTraj.get(0).ty*sv.trackCov.get(k).covMat.get(3, 3)
	   +sv.trackTraj.get(0).Q *sv.trackCov.get(k).covMat.get(3, 4)
	   )+" , "+ sv.trackTraj.get(k).ty
//+" ,Q= "+ ( sv.trackTraj.get(0).x *sv.trackCov.get(k).covMat.get(4, 0)
//           +sv.trackTraj.get(0).y *sv.trackCov.get(k).covMat.get(4, 1)
//	   +sv.trackTraj.get(0).tx*sv.trackCov.get(k).covMat.get(4, 2)
//	   +sv.trackTraj.get(0).ty*sv.trackCov.get(k).covMat.get(4, 3)
//	   +sv.trackTraj.get(0).Q *sv.trackCov.get(k).covMat.get(4, 4)
//	   )+" , "+ sv.trackTraj.get(k).Q
);
}
}



				vx[k] = mv.measurements.get(k).drift_dist - doca; // TDC(k)*V_drift - DOCA_fit_k
				//vx[k] = Math.abs(mv.measurements.get(k).drift_dist) - Math.abs(doca); // TDC(k)*V_drift - DOCA_fit_k
				//vx[k] = Math.abs(doca) - Math.abs(mv.measurements.get(k).drift_dist); // TDC(k)*V_drift - DOCA_fit_k
				weight[k] = 1./(mv.measurements.get(k).error*mv.measurements.get(k).error); // V=trk(5,il,ilnk)**2 + dc_Sigma_doca(is)**2
				this.chi2 += weight[k]*vx[k]*vx[k];
				vx[k] *= weight[k]; // for proper Chi2 fit: need to put the weights into beta as well
				
				//sv.trackTraj.add(k, sv.StateVec); 
				//sv.trackCov.add(k, sv.CovMat);
				//System.out.println((k)+"] trans "+sv.trackTraj.get(k).x+","+sv.trackTraj.get(k).y+","+
				//		sv.trackTraj.get(k).z+","+sv.trackTraj.get(k).tx+","+sv.trackTraj.get(k).ty); 
				//this.filter(k);
				
				// dQ_k/dty_0=C(4,3)=0
				//System.out.println(" C(4,3): " +sv.trackCov.get(k).covMat.get(4, 3)
				//+" C(3,4): " +sv.trackCov.get(k).covMat.get(3, 4));
				
			} // next layer

if(debug_gfit) System.out.println(" GFit.Chi2.Before: " +sector+" , "+ chi2_before);
			
			
			// Calculation of matrix alfa(5,5)
			double[][] alfa = new double[5][5];
			for(int j=0; j<5; j++) {
			 for(int l=0; l<5; l++) {
			  alfa[j][l] = 0;
			  for(int k=0; k<sv.Z.length; k++) {
			   //if(mv.measurements.get(k).isl==1) // Superlayer 1
			   //if(sv.trackTraj.get(k).isl%2==0) // odd Superlayers only
			   alfa[j][l] += weight[k]*dtrsp[j][k]*dtrsp[l][k]; // proper chi2 fit
			  }
			 }
			}
			

                      // for no magnetic field, p must be an independent variable in the fit.
                      // modify the alfa matrix appropriately:
                        if(Math.abs(TORSCALE) < 0.001) {
			 alfa[4][4] = 1;
                         for(int j=0; j<4; j++) {
                          alfa[4][j] = 0;
                          alfa[j][4] = 0;
                         }
			}
			
			// Calculation of vector BETA(5)=D^T*V, D=dV_i/dq_j, V=(measured-calculated)*W
			double[] beta = new double[5];
			for(int j=0; j<5; j++) {
			 beta[j] = 0;
			 for(int k=0; k<sv.Z.length; k++) {
			 //if(mv.measurements.get(k).isl==1) // Superlayer 1
			 //if(sv.trackTraj.get(k).isl%2==0) // odd Superlayers only
			  beta[j] += dtrsp[j][k]*vx[k];
			 }
			}
			// Invertion of matrix ALFA(5,5)=D^T*W*D, W=1/err_doca^2 - inverse of covariance matrix
                        Matrix alpha = new Matrix(alfa);

// MO: same order in matrix as in array
/*for(int j=0; j<5; j++) {
 for(int l=0; l<5; l++) {
 System.out.println(" a[j][l]: "+j+" , "+l+" , "+ alfa[j][l] +" , "+ alpha.get(j,l));
 }
}*/

			Matrix ialpha = null;
			if(this.isNonsingular(alpha)==false) {
				System.out.println("Covariance Matrix is non-invertible - quit filter!");
				return;
			}
			try {
				ialpha = alpha.inverse();
			} catch (Exception e) {
				return;
			}
                        // Calculation of corrections DX(5)
		       double[] dx = new double[5];
		       for(int j=0; j<5; j++) {
		        dx[j] = 0;
			for(int l=0; l<5; l++) {
			 // dx[j] += ialpha[j][l]*beta[l];
			  dx[j] += ialpha.get(j,l)*beta[l];
			 // dx[j] += ialpha.get(l,j)*beta[l]; // not better
//System.out.println(" ia*b: " + j +" , "+ l +" , "+  ialpha.get(j,l) +" , "+  beta[l] +" , "+ dx[j] );
			}
		       }

			// update track parameters at vertex(layer=0)
			sv.trackTraj.get(0).x += dx[0];
			sv.trackTraj.get(0).y += dx[1];
			sv.trackTraj.get(0).tx += dx[2];
			sv.trackTraj.get(0).ty += dx[3];
			sv.trackTraj.get(0).Q += dx[4];

if(debug_gfit) {
System.out.println(" dx: " + sv.trackTraj.get(0).x +" , "+ dx[0] );
System.out.println(" dy: " + sv.trackTraj.get(0).y +" , "+ dx[1] );
System.out.println(" dtx: " + sv.trackTraj.get(0).tx +" , "+ dx[2] );
System.out.println(" dty: " + sv.trackTraj.get(0).ty +" , "+ dx[3] );
System.out.println(" dq/p: " + sv.trackTraj.get(0).Q +" , "+ dx[4] );
}

			// for no magnetic field, p must be an independent variable in the fit.
                        if(Math.abs(TORSCALE) < 0.001) {
			 sv.trackTraj.get(0).Q =-0.001;
			}
			
			// transport from vertex (layer 0) to other layers
			for(int k=0; k<sv.Z.length-1; k++) {
				sv.transport(sector,k, k+1, sv.trackTraj.get(k), sv.trackCov.get(k));

// test transport method - works
/*double x = sv.trackTraj.get(k).x;
double y = sv.trackTraj.get(k).y;
double z = sv.trackTraj.get(k).z;
double tx = sv.trackTraj.get(k).tx;
double ty = sv.trackTraj.get(k).ty;
double p = 1./Math.abs(sv.trackTraj.get(k).Q);
int q = (int) Math.signum(sv.trackTraj.get(k).Q);
dcSwim.SetSwimParameters(-1, x, y, z, tx, ty, p, q);
//double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
//double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneLab(z0);
double[] VecAtNextHit = dcSwim.SwimToPlaneTiltSecSys(sector, sv.trackTraj.get(k+1).z); // to next Z_hit
System.out.println(" transp: " + k
+" , Z: "+ sv.trackTraj.get(k).z
+" , "+ sv.trackTraj.get(k+1).z
+" , "+ VecAtNextHit[2]
+" , X: "+ sv.trackTraj.get(k).x
+" , "+ sv.trackTraj.get(k+1).x
+" , "+ VecAtNextHit[0]
+" , Y: "+ sv.trackTraj.get(k).y
+" , "+ sv.trackTraj.get(k+1).y
+" , "+ VecAtNextHit[1]
+" , tgX: "+ sv.trackTraj.get(k).tx
+" , "+ sv.trackTraj.get(k+1).tx
+" , "+ VecAtNextHit[3]/VecAtNextHit[5]
+" , tgY: "+ sv.trackTraj.get(k).ty
+" , "+ sv.trackTraj.get(k+1).ty
+" , "+ VecAtNextHit[4]/VecAtNextHit[5]
+" , Q/P: "+ sv.trackTraj.get(k).Q
+" , "+ sv.trackTraj.get(k+1).Q
);*/


			}
			
			
			this.calcFinalChisq(sector);
			if(this.chi2<newChisq) {
				this.finalStateVec = sv.trackTraj.get(sv.Z.length-1); // last measured layer
				newChisq = this.chi2;
			}  else {
				i=totNumIter;
			}
		} // next iteration
		
	}
	
	public double chi2 = 0;
	public int NDF = 0;

	/*private void smooth(int k) {
		this.chi2 =0;
		if(sv.trackTraj.get(k)!=null && sv.trackCov.get(k).covMat!=null) {
			sv.transport(sector,k, 0, sv.trackTraj.get(k), sv.trackCov.get(k)); 
			for(int k1=0; k1<k; k1++) {
				sv.transport(sector,k1, k1+1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
				this.filter(k1+1);				
			}
		}
	}*/

	private void calcFinalChisq(int sector) {
	
	   //kfStateVecsAlongTrajectory = new ArrayList<>();
	   
	   
	    
	   
		int k = sv.Z.length-1;
		//int k = 0;
		this.chi2 =0;
		if(sv.trackTraj.get(k)!=null && sv.trackCov.get(k).covMat!=null) {
			//sv.rinit(sv.Z[0], k);
			sv.rinit(sector, sv.Z[0], k); // do we need this?
			//sv.rinit(sv.Z[0], 0);
			
			
			/*org.jlab.rec.dc.trajectory.StateVec svc =
                    new org.jlab.rec.dc.trajectory.StateVec(sv.trackTraj.get(0).x,
                            sv.trackTraj.get(0).y,
                            sv.trackTraj.get(0).tx,
                            sv.trackTraj.get(0).ty);
            svc.setZ(sv.trackTraj.get(0).z);
            svc.setB(sv.trackTraj.get(0).B);
            path += sv.trackTraj.get(0).deltaPath;
            svc.setPathLength(path);
            double h0 = mv.h(new double[]{sv.trackTraj.get(0).x, sv.trackTraj.get(0).y},
                    mv.measurements.get(0).tilt,
                    mv.measurements.get(0).wireMaxSag,
                    mv.measurements.get(0).wireLen);
            svc.setProjector(h0);
            kfStateVecsAlongTrajectory.add(svc);*/
	    
			
			for(int k1=0; k1<sv.Z.length; k1++) {
				if(k1<sv.Z.length-1) sv.transport(sector,k1, k1+1, sv.trackTraj.get(k1), sv.trackCov.get(k1));
				
				
				
				double V =  mv.measurements.get(k1).error;
				//double h =  mv.h(new double[]{sv.trackTraj.get(k1).x,sv.trackTraj.get(k1).y}, (int) mv.measurements.get(k1).tilt);
                                //chi2+= (mv.measurements.get(k1).x - h)*(mv.measurements.get(k1).x - h)/V;				
                                
				double cnorm = Math.sqrt(1 + sv.trackTraj.get(k1).tx*sv.trackTraj.get(k1).tx + sv.trackTraj.get(k1).ty*sv.trackTraj.get(k1).ty);
				double doca =  this.doca(
				new Vector3D( // wire position
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).x,
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).y,
				DcDetector.getWireMidpoint(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).z),
				new Vector3D( // wire direction
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).x,
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).y,
				//DcDetector.getWireDirection(sector-1,mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).z),
				DcDetector.getWireDirection(mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).x,
				DcDetector.getWireDirection(mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).y,
				DcDetector.getWireDirection(mv.measurements.get(k1).isl-1, mv.measurements.get(k1).ilayer-1,  mv.measurements.get(k1).iwire-1).z),
				new Vector3D( // track position at intersection with current layer
				sv.trackTraj.get(k1).x,sv.trackTraj.get(k1).y,sv.trackTraj.get(k1).z),
				new Vector3D( // track direction at intersection with current layer
				sv.trackTraj.get(k1).tx/cnorm,
				sv.trackTraj.get(k1).ty/cnorm,
				1./cnorm)
				);
				this.chi2+= (mv.measurements.get(k1).drift_dist - doca)*(mv.measurements.get(k1).drift_dist - doca)/V/V;

if(debug_gfit) System.out.println(" GFit.Chi2(SL,layer,wire): " +mv.measurements.get(k1).isl
+" , "+ mv.measurements.get(k1).ilayer
+" , "+ mv.measurements.get(k1).iwire
+ " doca = " +doca+ " drift_dist = " + mv.measurements.get(k1).drift_dist
+ " sv.x = " +sv.trackTraj.get(k1).x+ " sv.y = " +sv.trackTraj.get(k1).y);


			}

if(debug_gfit) System.out.println(" GFit.Chi2= " +chi2+" /N.d.F= "+ (sv.Z.length-5));
this.NDF = sv.Z.length-5;

		}
	}
	

	private double doca(Vector3D wpos, Vector3D wdir, Vector3D tpos, Vector3D tdir) {
	        Vector3D tmp = wdir.cross(tdir); // returns new vector3d, does not affect tpos
                double mtmp = tmp.mag();
		if (mtmp > 0. && Math.abs(tdir.z())>1.e-6 ) { // MO: tracks along wire or along beam exluded
		//Vector3D tdif = new Vector3D();
		//tdif = tpos.sub(wpos); // MO: ATTENTION THIS CHANGES tpos!!! ERROR!!!
		Vector3D tdif = tpos.clone();// returns new vector3d, does not affect tpos
		tdif=tdif.sub(wpos); // modifies tdif, returns modified tdif
		double tdot = tmp.dot(tdif);
		double tx = tdir.x()/tdir.z();
		double ty = tdir.y()/tdir.z();
		//double d = (double)tmp.dot(tpos.subtract(wpos))/mtmp;
		 double d = tdot/mtmp;
		 // Partial Derivatives - assume: 1) CAP at Z_k, 2) straight track around CAP
		 this.pdoca[0]=tmp.x()/mtmp; // d(DOCA_fit_k)/dx_k
		 this.pdoca[1]=tmp.y()/mtmp; // d(DOCA_fit_k)/dy_k
		 this.pdoca[2]=((tpos.y()-wpos.y())*wdir.z()-(tpos.z()-wpos.z())*wdir.y())*tdir.z()/mtmp
		 -((wdir.z()*wdir.z()+wdir.y()*wdir.y())*tx-wdir.x()*wdir.y()*ty-wdir.z()*wdir.x())*d*tdir.z()*tdir.z()/(mtmp*mtmp); // d(DOCA_fit_k)/dtx_k
		 this.pdoca[3]=((tpos.z()-wpos.z())*wdir.x()-(tpos.x()-wpos.x())*wdir.z())*tdir.z()/mtmp
		 -((wdir.z()*wdir.z()+wdir.x()*wdir.x())*ty-wdir.x()*wdir.y()*tx-wdir.z()*wdir.y())*d*tdir.z()*tdir.z()/(mtmp*mtmp); // d(DOCA_fit_k)/dty_k
		 
		 // actually Vx=Drift_Distance-DOCA, hence we must take derivatives of -DOCA
		 /*this.pdoca[0]=-tmp.x()/mtmp; // d(DOCA_fit_k)/dx_k
		 this.pdoca[1]=-tmp.y()/mtmp; // d(DOCA_fit_k)/dy_k
		 this.pdoca[2]=-((tpos.y()-wpos.y())*wdir.z()-(tpos.z()-wpos.z())*wdir.y())*tdir.z()/mtmp
		 +((wdir.z()*wdir.z()+wdir.y()*wdir.y())*tx-wdir.x()*wdir.y()*ty-wdir.z()*wdir.x())*d*tdir.z()*tdir.z()/(mtmp*mtmp); // d(DOCA_fit_k)/dtx_k
		 this.pdoca[3]=-((tpos.z()-wpos.z())*wdir.x()-(tpos.x()-wpos.x())*wdir.z())*tdir.z()/mtmp
		 +((wdir.z()*wdir.z()+wdir.x()*wdir.x())*ty-wdir.x()*wdir.y()*tx-wdir.z()*wdir.y())*d*tdir.z()*tdir.z()/(mtmp*mtmp); // d(DOCA_fit_k)/dty_k
                  */
/*System.out.println(" doca.drv: " +mtmp+" , "+ tdir.z()
+" , "+ wpos.x()+" , "+ wpos.y()+" , "+ wpos.z()
+" , "+ tpos.x()+" , "+ tpos.y()+" , "+ tpos.z()
+" , "+ wdir.x()+" , "+ wdir.y()+" , "+wdir.z()
+" , "+ tdir.x()+" , "+ tdir.y()+" , "+ tdir.z()
+" , "+ (tpos.x()-wpos.x())
+" , "+ (tpos.y()-wpos.y())
+" , "+ (tpos.z()-wpos.z())
+" , "+ ((tpos.y()-wpos.y())*wdir.z()-(tpos.z()-wpos.z())*wdir.y())*tdir.z()/mtmp
+" , "+ ((wdir.z()*wdir.z()+wdir.y()*wdir.y())*tx-wdir.x()*wdir.y()*ty-wdir.z()*wdir.x())*d*tdir.z()*tdir.z()/(mtmp*mtmp)
+" , "+ ((tpos.z()-wpos.z())*wdir.x()-(tpos.x()-wpos.x())*wdir.z())*tdir.z()/mtmp
+" , "+ ((wdir.z()*wdir.z()+wdir.x()*wdir.x())*ty-wdir.x()*wdir.y()*tx-wdir.z()*wdir.y())*d*tdir.z()*tdir.z()/(mtmp*mtmp)
);*/


		 //-------
		 this.pdoca[4]=0; // d(DOCA_fit_k)/dQ_k+1
		 // DOCA
                 return d;
                } else {
                 //fprintf(stderr,"ERROR: wire and track in same direction\n");
                return 1000.;
                 }
	}


	/**
	 * prints the matrix -- used for debugging
	 * @param C matrix
	 */
	public void printMatrix(Matrix C) {
		for(int k = 0; k< 5; k++) {
			System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
		}
	}
	
	private boolean isNonsingular(Matrix mat) {
		
	      for (int j = 0; j < mat.getColumnDimension(); j++) {
	         if (mat.get(j, j) < 0.00000000001)
	            return false;
	      }
	      return true;
	}
	
}
