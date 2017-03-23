package org.jlab.rec.cvt.track;

import java.util.Collections;
import java.util.HashMap;

import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.svt.Constants;
import org.jlab.rec.cvt.svt.Geometry;

import Jama.Matrix;

/**
 * The Kalman Filter algorithm to fit straight tracks from cosmic data 
 * @author ziegler
 *
 */

public class KalFitCosmics {

	private static double GainScaleFac = 1;
	int Dir =1;
	
	public int nbMeast ;
	private int[] _k ;
	/**
	 * The state vector is a 4-vector representing the straight track parameters, the yx projection slope and intercept, and the yz projection slop and intercept.	
	 */
	private double[][] stateVec ; // 
	private double[][] X ; 	
	private double[] projVecs ; // the array of measurements projections
	
	private HashMap<Integer, Matrix> covMat = new HashMap<Integer, Matrix>(); // the track covariance matrix
	private double _h;
	private double[][] _H;
	
	// the normal to the measuremnt plane
	private double[][] MeasPlaneNorm ;
	// the measurement is the cluster seed strip
	private double[] Measurement ;
	// the error on the measurement of the centroid
	private double[] MeasurementError;
	// the sector of the measurement
	private int[] Sector ;
	private int[] Layer ;
	
	
	public double chisq; // fit chi^2
	public int nbOfIterations = 1; // only one iteration is working at them moment 
	
	// the momentum after the Kalman fit
	public double KF_chi2 = -1;
	
	public boolean KalFitFail = false;
	
	
	/**
	 * The constructor
	 * @param cand the track candidate
	 * @param fitsTo the fit method = fits to the wires is the default and only working method at the moment
	 */
	public KalFitCosmics(StraightTrack cand, Geometry geo) {
		
		if(cand==null)
			return;

		this.setMeasurements(cand, geo);
		
		init(cand);
		
		if(stateVec[0]==null ) {
			KalFitFail = true;
			System.out.println(" KF initialization error!!!");
			return;		
		}
	}
	
	private void init(StraightTrack trkcand) {
		
		double[] initStateVec = f0(trkcand);
		
		stateVec[0][0] = initStateVec[0];
		stateVec[0][1] = initStateVec[1];
		stateVec[0][2] = initStateVec[2];
		stateVec[0][3] = initStateVec[3];
		
		covMat.put(0, C0(trkcand));   
	}
	
	private void setMeasurements(StraightTrack trkcand, Geometry geo) {
		
		Collections.sort(trkcand);
		nbMeast = trkcand.size()*2;
		
		stateVec = new double[nbMeast][4]; // 
		X = new double[nbMeast][3]; 	
		projVecs  = new double[nbMeast]; // the array of measurements projections		
		
		MeasPlaneNorm = new double[nbMeast][3];
		// the measurement is the cluster seed strip
		Measurement = new double[nbMeast];
		// the error on the measurement of the centroid
		MeasurementError = new double[nbMeast];
		// the sector of the measurement
		Sector = new int[nbMeast];
		Layer = new int[nbMeast];

		_k = new int[trkcand.size()*2];
		
		for(int k =0; k< nbMeast; k++) {
			MeasPlaneNorm[k][0] = Double.NaN;
			MeasPlaneNorm[k][1] = Double.NaN;
			MeasPlaneNorm[k][2] = Double.NaN;
			Measurement[k] = Double.NaN;
		
		}
		
		for(int j = 0; j< trkcand.size(); j++) {
			
			int layer1 =-1;
			int layer2 =-1;
			if(trkcand.get(j).get_Point0().y()<0) {
				 layer1 = (j+1)*2;
				 layer2 = (j+1)*2-1;
			}
			if(trkcand.get(j).get_Point0().y()>0) {
				 layer1 = (j+1)*2-1;
				 layer2 = (j+1)*2;
			}
			int sector = trkcand.get(j).get_Sector();
			_k[layer1-1] = layer1-1;
			_k[layer2-1] = layer2-1;
			
			Measurement[layer1-1] = trkcand.get(j).get_Cluster1().get_Centroid();
			MeasurementError[layer1-1] = Math.sqrt(trkcand.get(j).get_Cluster1().size());
			Sector[layer1-1] = trkcand.get(j).get_Sector();
			Layer[layer1-1] = trkcand.get(j).get_Cluster1().get_Layer();
			Vector3D Plane1ModuleNormal = geo.findBSTPlaneNormal(sector, Layer[layer1-1]);
			MeasPlaneNorm[layer1-1][0] = Plane1ModuleNormal.x();
			MeasPlaneNorm[layer1-1][1] = Plane1ModuleNormal.y();
			MeasPlaneNorm[layer1-1][2] = Plane1ModuleNormal.z();
			
			
			Measurement[layer2-1] = trkcand.get(j).get_Cluster2().get_Centroid();
			MeasurementError[layer2-1] = Math.sqrt(trkcand.get(j).get_Cluster2().size());
			Sector[layer2-1] = trkcand.get(j).get_Sector();
			Layer[layer2-1] = trkcand.get(j).get_Cluster2().get_Layer();
			Vector3D Plane2ModuleNormal = geo.findBSTPlaneNormal(sector, Layer[layer2-1]);
			MeasPlaneNorm[layer2-1][0] = Plane2ModuleNormal.x();
			MeasPlaneNorm[layer2-1][1] = Plane2ModuleNormal.y();
			MeasPlaneNorm[layer2-1][2] = Plane2ModuleNormal.z();
			
			//System.out.println(j+" Clayer1 "+layer1+" Clayer2 "+layer2+" Layer1 "+Layer[layer1-1] +" Layer2 "+Layer[layer2-1]);
		}
		
	}
	
	private void project(int k, Geometry geo) {
		double val = -1;
		double[][] H = new double[][]{{0,0,0,0,0}};
		//System.out.println(" Projecting to meas plane ...........");
		if(!Double.isNaN(MeasPlaneNorm[k][0])) {
			
			// global rotation angle
			double Glob_rangl = ((double) (Sector[k]-1)/(double) Constants.NSECT[Layer[k]-1])*2.*Math.PI + Constants.PHI0[Layer[k]-1];
			// angle to rotate to global frame
			double Loc_to_Glob_rangl = Glob_rangl-Constants.LOCZAXISROTATION;

			double yxs = stateVec[k][0];
			double yxi = stateVec[k][1];
			double yzs = stateVec[k][2];
			double yzi = stateVec[k][3];
			
			double cosA = Math.cos(Loc_to_Glob_rangl);
			double sinA = Math.sin(Loc_to_Glob_rangl);
			
			double R = Constants.MODULERADIUS[Layer[k]-1][0];
			// the intersection of the track with the module plane
			double y = (sinA*yxi + R)/(-sinA*yxs + cosA);
			double x = y*yxs + yxi;
			double z = y*yzs + yzi;
			X[k][0]=x;
			X[k][1]=y;
			X[k][2]=z;
			
			// now get this point in the local frame
			
			double lTx = (Constants.MODULERADIUS[Layer[k]-1][Sector[k]-1])*Math.cos(Glob_rangl);
			double lTy = (Constants.MODULERADIUS[Layer[k]-1][Sector[k]-1])*Math.sin(Glob_rangl); 
			double lTz = Constants.Z0[Layer[k]-1]; 
			
			//rotate and translate in the local module frame
			double cosRotation = Math.cos(Loc_to_Glob_rangl);
			double sinRotation = Math.sin(Loc_to_Glob_rangl);

			double xt=  (x-lTx)*cosRotation +(y-lTy)*sinRotation  + 0.5*Constants.ACTIVESENWIDTH;;
			//double yt= -(x-lTx)*sinRotation +(y-lTy)*cosRotation  ;
			double zt = z - lTz ;
			
			double alphaAng = (double) Constants.STEREOANGLE/(double) (Constants.NSTRIP-1); 
			
			double P = Constants.PITCH;
		
			
			double del_m_del_x =0;
			double del_m_del_y =0;
			double del_m_del_z =0;
			
			if(Layer[k]%2==1) {
				 del_m_del_x = -cosRotation/(alphaAng*zt+P);
				 del_m_del_y = -sinRotation/(alphaAng*zt+P);
				 del_m_del_z = alphaAng*(P+xt-Constants.ACTIVESENWIDTH)/((alphaAng*zt+P)*(alphaAng*zt+P));
				 
			 }
	
			
			if(Layer[k]%2==0) {
				 del_m_del_x = cosRotation/(alphaAng*zt+P);
				 del_m_del_y = sinRotation/(alphaAng*zt+P);
				 del_m_del_z = alphaAng*(P-xt)/((alphaAng*zt+P)*(alphaAng*zt+P));
				 
			 }
			
			
			val = geo.calcNearestStrip(x, y, z, Layer[k],Sector[k]);  
			
			// find H
			
			double dely_delyxi = sinA/(-sinA*yxs + cosA);
			double dely_delyxs = sinA*(sinA*yxi + R)/((-sinA*yxs + cosA)*(-sinA*yxs + cosA));
			double dely_delyzi = 0;
			double dely_delyzs = 0;
			
			double delx_delyxi = 1 + dely_delyxi*yxs;
			double delx_delyxs = y + dely_delyxs*yxs;
			double delx_delyzi = 0;
			double delx_delyzs = 0;
			
			double delz_delyxi = dely_delyxi*yzs;
			double delz_delyxs = dely_delyxs*yzs;
			double delz_delyzi = 1;
			double delz_delyzs = y;
			
			 H =  new double[][] {
						{   del_m_del_x*delx_delyxs + del_m_del_y*dely_delyxs + del_m_del_z*delz_delyxs,
							del_m_del_x*delx_delyxi + del_m_del_y*dely_delyxi + del_m_del_z*delz_delyxi,						    
							del_m_del_x*delx_delyzs + del_m_del_y*dely_delyzs + del_m_del_z*delz_delyzs,
							del_m_del_x*delx_delyzi + del_m_del_y*dely_delyzi + del_m_del_z*delz_delyzi
						}
				};

		}

		this.set_h(val);
		
		this.set_H(H);
	
	}

	public void runKalFit(StraightTrack trk, Geometry geo) {
		//boolean passCand = true;
		double bestChi2 = Double.POSITIVE_INFINITY;
		
		for(int nIter =0; nIter<=150; nIter++) {
			
			this.chisq =0;
			for(int j = 0; j< _k.length-1; j++) {
				//System.out.println(j+": _k[j] "+_k[j]+" _k[j+1] "+ _k[j+1]);
				propagate(_k[j], _k[j+1], geo,true);
				filter(_k[j+1], geo);
			}
			for(int j = _k.length-1; j>0; j--) {
				//System.out.println(j+": _k[j] "+_k[j]+" _k[j-1] "+ _k[j-1]);
				propagate(_k[j], _k[j-1], geo,true);	
				filter(_k[j-1], geo);
			}
			
			if(this.chisq<bestChi2) {
				bestChi2 = this.chisq;
				trk.get_ray().set_yxslope(stateVec[0][0]);
				trk.get_ray().set_yxinterc(stateVec[0][1]);
				trk.get_ray().set_yzslope(stateVec[0][2]);
				trk.get_ray().set_yzinterc(stateVec[0][3]);
				trk.set_chi2(this.chisq);
				this.KF_chi2 = this.chisq;
			} else {
				nIter = 151;
			}
			
		}
		
		int ndf = trk.size()*2 - 4;
		trk.set_ndf(ndf);
		
	}
	
	private void propagate(int ki, int kf, Geometry geo, boolean isMeasurementPlane) { // for now run outwards only
		
		Dir = (int)Math.signum(kf-ki);
		// for the state
		//System.out.println( " init_layer-1 "+ki +" final_layer-1 "+kf);
		double[] f_kp = f(ki, kf);
		stateVec[kf] = f_kp;
		//System.out.println( " old state : ");this.printInfo(stateVec[ki]);
		//System.out.println( " new state : ");this.printInfo(f_kp);
		
		//System.out.println( " new state after EL:");this.printInfo(stateVec[kf]);
		Matrix C = covMat.get(ki);
		Matrix F_k = new Matrix(F(ki, kf));
		Matrix F_k_T = F_k.transpose();
		
		
		Matrix C_kp = F_k.times(C).times(F_k_T);
		
		covMat.put(kf, C_kp);
		
		if(isMeasurementPlane) {
			// for the measurement
			this.project(kf, geo);
			double h_kp = this.get_h();
			projVecs[kf] = h_kp;
			//System.out.println("----------> projVec "+h_kp+ "   at "+kf);
		}		
	}
	
	private void filter(int k, Geometry geo) {
		
		Matrix C = covMat.get(k);
		double[] a = stateVec[k];
		
		double measErr = MeasurementError[k];//*geo.getSingleStripResolution(Layer[k], (int) Measurement[k],(X[k][2]-Constants.Z0[Layer[k]-1]));
		
		double V = measErr*measErr; 
		
		Matrix G = new Matrix( new double[][]{
				{(1./V)	}
		});
		Matrix H = new Matrix(this.get_H());
		
		double h = projVecs[k];
		double m = Measurement[k];
		
		//System.out.println(" in filter h = "+projVecs[k]);
		if(this.isNonsingular(C)==false) {
			//System.out.println("Covariance Matrix is non-invertible - quit filter!");
			return ;
		}
		Matrix Cinv = null;
		try {
			Cinv = C.inverse();
		} catch (Exception e) {
			
			return ;
			
		}
		
		Matrix HT = H.transpose();
		Matrix HTG = HT.times(G);
		Matrix HTGH = HTG.times(H);
		
		Matrix L = Cinv.plus(HTGH);
		
		
		if(this.isNonsingular(L)==false) {
			//System.out.println("Covariance Matrix is non-invertible - quit filter!");
			return ;
		}
		Matrix Linv = null;
		try {
			Linv = L.inverse();
        } catch (Exception e)  {
        	System.err.println("Error: " + e); 
			return ;
		}
		covMat.put(k, Linv);
		
		Matrix K = Linv.times(HTG);
		
		double[][] res = new double[1][1];
				
		res[0][0] = m - h;
				
		Matrix R = new Matrix(res);
		
		Matrix filt = K.times(R);
		
		double[] a_filt = new double[5];
		
		
		for(int i =0; i<4; i++) {
			
			a_filt[i] = a[i] + filt.get(i, 0)*KalFitCosmics.getGainscalefac();
			//System.out.println(" filtered part ["+i+"] "+filt.get(i,0));
			System.out.println(" KF ");this.printInfo(a);System.out.println(" KF filt ");this.printInfo(a_filt);
		}
		
		stateVec[k]=a_filt;
		project(k,  geo);
		/*
		// reevaluating site:
		System.out.println(" res "+(m-this.get_h())*(m-this.get_h())/V+" trkchi2 "+this.KF_chi2);
		if(this.KF_chi2!=-1 && (m-this.get_h())*(m-this.get_h())/V>this.KF_chi2*0.5) {
			stateVec[k]=a;
			project(k,  geo);
			return;
		}
		*/
		if(Dir<0)
			this.chisq +=(m-this.get_h())*(m-this.get_h())/V;  // add only for return
		
		
		
		//System.out.println(k+" before filter ");this.printInfo(a);System.out.println(k+" after filter ");this.printInfo(a_filt);
	}
	
	private Matrix C0(StraightTrack trkcand) {
		double components[][] = new double[4][4];
		for (int i = 0; i<4; i++)
		        for (int j = 0; j<4; j++)
		                components[i][j] = 0;

		
		components[0][0] = trkcand.get_ray().get_yxslopeErr()*trkcand.get_ray().get_yxslopeErr();
		components[1][1] = trkcand.get_ray().get_yxintercErr()*trkcand.get_ray().get_yxintercErr();
		components[2][2] = trkcand.get_ray().get_yzslopeErr()*trkcand.get_ray().get_yzslopeErr();
		components[3][3] = trkcand.get_ray().get_yxintercErr()*trkcand.get_ray().get_yxintercErr();
		
		
		return new Matrix(components);
			
	}
	//OK
	private double[] f0(StraightTrack trkcand) {
		 
		double yxs = trkcand.get_ray().get_yxslope();
		double yxi = trkcand.get_ray().get_yxinterc();
		double yzs = trkcand.get_ray().get_yzslope();
		double yzi = trkcand.get_ray().get_yzinterc();
		
		return new double[]{yxs,yxi,yzs,yzi};
	}
	
	//OK
	private double[] f(int ki, int kf) {
		return this.stateVec[ki];
		
	}

	private double[][] F(int ki, int kf) {
		
		double[][] FMat =  new double[][] {
				{1,  0,   0,   0},
				{0,  1,   0,   0},				
				{0,  0,   1,   0},
				{0,  0,   0,   1}
		};
		
		return FMat;
	}
	
	/**
	 * prints the matrix -- used for debugging
	 * @param C matrix
	 */
	public void printMatrix(Matrix C) {
		for(int k = 0; k< C.getRowDimension(); k++) {
			String st = new String();
			for(int k2 = 0; k2<C.getColumnDimension(); k2++) {
				st+=C.get(k, k2);
				st+=" ";
			}
			System.out.println(st);
		}
	}
	
	

	/**
	 * prints the stateVector at a given z - used for debugging
	 * @param stateVec2 the state vector
	 */
	@SuppressWarnings("unused")
	private void printInfo(double[] stateVec2) {
		System.out.println("x^T = (s1,i1,s2,i2) = ("+stateVec2[0]+" ,"+stateVec2[1]+" ,"+stateVec2[2]+" ,"+stateVec2[3]+");");
	}

	
	 private boolean isNonsingular(Matrix mat) {
			
	      for (int j = 0; j < mat.getColumnDimension(); j++) {
	         if (mat.get(j, j) == 0)
	            return false;
	      }
	      return true;
	   }

	public double get_h() {
		return _h;
	}

	public void set_h(double val) {
		this._h = val;
	}

	public double[][] get_H() {
		return _H;
	}

	public void set_H(double[][] _H) {
		this._H = _H;
	}

	public static double getGainscalefac() {
		return GainScaleFac;
	}

}
