package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.track.Track;
//import org.jlab.rec.dc.trajectory.DCSwimmer;
import org.jlab.clas.swimtools.Swim;

import org.jlab.rec.dc.cross.Cross;

import Jama.Matrix;

public class GStateVecs {

	final double speedLight = 0.002997924580;
	public double[] Z;
	public List<B> bfieldPoints = new ArrayList<B>();;
	public Map<Integer, GStateVec> trackTraj = new HashMap<Integer, GStateVec>();
	public Map<Integer,CovMat> trackCov = new HashMap<Integer, CovMat>();

	public double stepSize = 0.2; // step size 
	public GStateVec GStateVec;
	public CovMat CovMat;
	public Matrix F;
	
	public GStateVec f(int i, int f, GStateVec iVec) {
		
		double x 	= iVec.x;
		double y 	= iVec.y;
		double tx 	= iVec.tx;
		double ty 	= iVec.ty;
		double Q 	= iVec.Q;

		int nSteps = (int) (Math.abs((Z[i] - Z[f])/stepSize) + 1 );
		
		double s = Math.signum(Z[f]-Z[i])*stepSize;
		
		double z 	= Z[i];
		 
		for(int j =0; j< nSteps; j++) {
		
			if(j==nSteps-1) {
				s = Math.signum(Z[f]-Z[i])*Math.abs(z - Z[f]);
			}
			
			//B bf = new B(sector, i, z, x, y, tx, ty, s);
			B bf = new B(1, i, z, x, y, tx, ty, s); // MO: MUST add sector here
			
			double[] A = A(tx,ty,bf.Bx,bf.By,bf.Bz);
		 // transport stateVec
 			x += tx*s + Q*speedLight*A[0]*s*s/2;
 			y += ty*s + Q*speedLight*A[1]*s*s/2; 
 			tx += Q*speedLight*A[0]*s;
 			ty += Q*speedLight*A[1]*s;
 			
			z+= s;
		}
		
		GStateVec fVec = new GStateVec(f);
		fVec.z = Z[f];
		fVec.x = x;
		fVec.y = y;
		fVec.tx = tx;
		fVec.ty = ty;
		fVec.Q = Q;
		
		return fVec;
		
	}
	
	public void transport(int sector, int i, int f, GStateVec iVec, CovMat icovMat) { // s = signed step-size
          if(iVec==null)
            return;
		//StateVec iVec = trackTraj.get(i);
		//bfieldPoints = new ArrayList<B>();
		CovMat covMat;
		if(i>0){
		 covMat = icovMat;
		} else { // initialize transportation matrix when at first layer
		 Matrix initCMatrix = new Matrix( new double[][]{
				{    1,       0,       0,       0,  0},
				{    0,       1,       0,       0,  0},
				{    0,       0,       1,       0,  0},
				{    0,       0,       0,       1,  0},
				{    0,       0,       0,       0,  1}
		});
		 covMat = new CovMat(0);
		 covMat.covMat = initCMatrix;
		 this.trackCov.put(0, covMat);
		}
		CovMat fCov = new CovMat(f); // init final matrix here
		/*for(int rw = 0; rw< covMat.covMat.getRowDimension(); rw++) {
		 for(int cl = 0; cl< covMat.covMat.getColumnDimension(); cl++) {
		  fCov.covMat.set(rw, cl, covMat.covMat.get(rw, cl));
		 }
		}*/
		fCov.covMat = covMat.covMat.copy();
		
		double x = iVec.x;
		double y = iVec.y;
		double tx = iVec.tx;
		double ty = iVec.ty;
		double Q = iVec.Q;
		
//		double Bmax = 2.366498*Math.abs(Constants.getTORSCALE());
		double Bmax = 2.366498;
		if(bfieldPoints.size()>0) {
			double B = new Vector3D(bfieldPoints.get(bfieldPoints.size()-1).Bx,bfieldPoints.get(bfieldPoints.size()-1).By,bfieldPoints.get(bfieldPoints.size()-1).Bz).mag();
			
			if(B/Bmax>0.01)
				stepSize = 0.15;
			if(B/Bmax>0.02)
				stepSize = 0.1;
			if(B/Bmax>0.05)
				stepSize = 0.075;
			if(B/Bmax>0.1)
				stepSize = 0.05;
			if(B/Bmax>0.5)
				stepSize = 0.02;
			if(B/Bmax>0.75)
				stepSize = 0.01;
		}
		
		int nSteps = (int) (Math.abs((Z[i] - Z[f])/stepSize) + 1 );
		
		double s = Math.signum(Z[f]-Z[i])*stepSize;
		s = (Z[f]-Z[i])/(double) nSteps;
		double z 	= Z[i];
		
		Matrix Cpropagated = null;
		double[][] transpStateJacobian = null;
		
		double dPath=0;
		
		for(int j =0; j< nSteps; j++) {
			
			if(j==nSteps-1) {
				s = Math.signum(Z[f]-Z[i])*Math.abs(z - Z[f]);
			}
			
			B bf = new B(sector, i, z, x, y, tx, ty, s) ;
			bfieldPoints.add(bf);
			
			double[] A = A(tx,ty,bf.Bx,bf.By,bf.Bz);
			double[] dA = delA_delt(tx,ty,bf.Bx,bf.By,bf.Bz);
			
			// transport covMat
			//double delx_deltx0 = s;
			//double dely_delty0 = s;
			double delx_deltx0 = s + Q*speedLight*s*s*dA[0]/2; // dA[0]=delAx_deltx, x~Ax
			double dely_delty0 = s + Q*speedLight*s*s*dA[3]/2; // dA[3]=delAy_delty, y~Ay
			double deltx_delQ = speedLight*s*A[0];
			double delty_delQ = speedLight*s*A[1];
			double delx_delQ = speedLight*s*s*A[0]/2;
			double dely_delQ = speedLight*s*s*A[1]/2;
			
			double deltx_delty0 = Q*speedLight*s*dA[1]; // dA[1]=delAx_delty, tx~Ax
			double delty_deltx0 = Q*speedLight*s*dA[2]; // dA[2]=delAy_deltx, ty~Ay
			
			double delx_delty0 = Q*speedLight*s*s*dA[1]/2; // dA[1]=delAx_delty, x~Ax
			double dely_deltx0 = Q*speedLight*s*s*dA[2]/2; // dA[2]=delAy_deltx, y~Ay
			
			
			
			
			transpStateJacobian =  new double[][] {
					{1,0, delx_deltx0, delx_delty0, delx_delQ},
					{0,1, dely_deltx0, dely_delty0, dely_delQ},
					{0,0,           1,deltx_delty0,deltx_delQ},
					{0,0,delty_deltx0,           1,delty_delQ},
					{0,0,           0,           0,         1}
			};
			double[][] u = new double[5][5];
			//double[][] c = new double[covMat.covMat.getRowDimension()][covMat.covMat.getColumnDimension()];
			//double[][] C = new double[covMat.covMat.getRowDimension()][covMat.covMat.getColumnDimension()];
			
			/*for(int rw = 0; rw< covMat.covMat.getRowDimension(); rw++) {
				for(int cl = 0; cl< covMat.covMat.getColumnDimension(); cl++) {
					c[rw][cl] = covMat.covMat.get(rw, cl);
				}
			}*/
			
			 //covMat = u = FC, q_k=u*q_0, where q_0=(x0,y0,tx0,ty0,Q)
			/*for(int j1 = 0; j1 < 5; j1++) {
				u[0][j1] = c[0][j1] + transpStateJacobian[0][2]*c[2][j1] + transpStateJacobian[0][3]*c[3][j1] + transpStateJacobian[0][4]*c[4][j1];
				u[1][j1] = c[1][j1] + transpStateJacobian[1][2]*c[2][j1] + transpStateJacobian[1][3]*c[3][j1] + transpStateJacobian[1][4]*c[4][j1];
				u[2][j1] = c[2][j1] + transpStateJacobian[2][3]*c[3][j1] + transpStateJacobian[2][4]*c[4][j1];
				u[3][j1] = transpStateJacobian[3][2]*c[2][j1] + c[3][j1] + transpStateJacobian[3][4]*c[4][j1];
				u[4][j1] = c[4][j1];
			}*/
        
	// u = FC
        /*for (int j1 = 0; j1 < 5; j1++) {
            u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * delx_deltx0 + covMat.covMat.get(3,j1)* delx_delty0 + covMat.covMat.get(4,j1) * delx_delQ;
            u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * dely_deltx0 + covMat.covMat.get(3,j1) * dely_delty0 + covMat.covMat.get(4,j1) * dely_delQ;
            u[2][j1] = covMat.covMat.get(2,j1) + covMat.covMat.get(3,j1) * deltx_delty0 + covMat.covMat.get(4,j1) * deltx_delQ;
            u[3][j1] = covMat.covMat.get(2,j1) * delty_deltx0 + covMat.covMat.get(3,j1) + covMat.covMat.get(4,j1) * delty_delQ;
            u[4][j1] = covMat.covMat.get(4,j1);
        }*/
        for (int j1 = 0; j1 < 5; j1++) {
            u[0][j1] = fCov.covMat.get(0,j1) + fCov.covMat.get(2,j1) * delx_deltx0 + fCov.covMat.get(3,j1)* delx_delty0 + fCov.covMat.get(4,j1) * delx_delQ;
            u[1][j1] = fCov.covMat.get(1,j1) + fCov.covMat.get(2,j1) * dely_deltx0 + fCov.covMat.get(3,j1) * dely_delty0 + fCov.covMat.get(4,j1) * dely_delQ;
            u[2][j1] = fCov.covMat.get(2,j1) + fCov.covMat.get(3,j1) * deltx_delty0 + fCov.covMat.get(4,j1) * deltx_delQ;
            u[3][j1] = fCov.covMat.get(2,j1) * delty_deltx0 + fCov.covMat.get(3,j1) + fCov.covMat.get(4,j1) * delty_delQ;
            u[4][j1] = fCov.covMat.get(4,j1);
        }
	//covMat = FCF^T
	/*for (int i1 = 0; i1 < 5; i1++) {
            C[i1][0] = u[i1][0] + u[i1][2] * delx_deltx0 + u[i1][3] * delx_delty0 + u[i1][4] * delx_delQ;
            C[i1][1] = u[i1][1] + u[i1][2] * dely_deltx0 + u[i1][3] * dely_delty0 + u[i1][4] * dely_delQ;
            C[i1][2] = u[i1][2] + u[i1][3] * deltx_delty0 + u[i1][4] * deltx_delQ;
            C[i1][3] = u[i1][2] * delty_deltx0 + u[i1][3] + u[i1][4] * delty_delQ;
            C[i1][4] = u[i1][4];
        }*/
	//covMat = CF^T
	/*for (int i1 = 0; i1 < 5; i1++) {
            C[i1][0] = covMat.covMat.get(i1,0) + covMat.covMat.get(i1,2) * delx_deltx0 + covMat.covMat.get(i1,3) * delx_delty0 + covMat.covMat.get(i1,4) * delx_delQ;
            C[i1][1] = covMat.covMat.get(i1,1) + covMat.covMat.get(i1,2) * dely_deltx0 + covMat.covMat.get(i1,3) * dely_delty0 + covMat.covMat.get(i1,4) * dely_delQ;
            C[i1][2] = covMat.covMat.get(i1,2) + covMat.covMat.get(i1,3) * deltx_delty0 + covMat.covMat.get(i1,4) * deltx_delQ;
            C[i1][3] = covMat.covMat.get(i1,2) * delty_deltx0 + covMat.covMat.get(i1,3) + covMat.covMat.get(i1,4) * delty_delQ;
            C[i1][4] = covMat.covMat.get(i1,4);
        }*/
			// Q
		    /*double p = Math.abs(1./Q);
		    double pz = p/Math.sqrt(1 + tx*tx + ty*ty);
		    double px = tx*pz;
		    double py = ty*pz;
		    
		    double t_ov_X0 = Math.signum(Z[f]-Z[i])*s/Constants.ARGONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm
		    
		    //double mass = this.MassHypothesis(this.massHypo); // assume given mass hypothesis
		    double mass = MassHypothesis("electron"); // assume given mass hypothesis
		    if(Q>0)
		    	mass = MassHypothesis("proton");
		    
		    double beta = p/Math.sqrt(p*p+mass*mass); // use particle momentum
		    double cosEntranceAngle = Math.abs((x*px+y*py+z*pz)/(Math.sqrt(x*x+y*y+z*z)*p));
		    double pathLength = t_ov_X0/cosEntranceAngle;  
		   
		    double sctRMS = (0.0136/(beta*p))*Math.sqrt(pathLength)*(1+0.038*Math.log(pathLength)); // Highland-Lynch-Dahl formula
		   
		    double cov_txtx = (1+tx*tx)*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
		    double cov_tyty = (1+ty*ty)*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
		    double cov_txty = tx*ty*(1 + tx*tx + ty*ty)*sctRMS*sctRMS;
		    
		    if(s>0) {
		    	C[2][2]+=cov_txtx;
		    	C[2][3]+=cov_txty;
		    	C[3][2]+=cov_txty;
		    	C[3][3]+=cov_tyty;
		    }*/
		    
		    Cpropagated = new Matrix(u); // transport matrix (not error/covariance matrix)
		    //Cpropagated = new Matrix(C); // error/covariance matrix
		    //covMat.covMat = Cpropagated; // MO: this overwrites the input covMat
		    fCov.covMat = Cpropagated; // MO: using final state covMat
		 // transport stateVec
 			x += tx*s + Q*speedLight*A[0]*s*s/2;
 			y += ty*s + Q*speedLight*A[1]*s*s/2;
 			tx += Q*speedLight*A[0]*s;
 			ty += Q*speedLight*A[1]*s;
 			
			z+= s;
			
			dPath += Math.sqrt(
			 (tx*s+Q*speedLight*A[0]*s*s/2)*(tx*s+Q*speedLight*A[0]*s*s/2)
			+(ty*s+Q*speedLight*A[1]*s*s/2)*(ty*s+Q*speedLight*A[1]*s*s/2)
			+s*s);
		}
		
		GStateVec fVec = new GStateVec(f);
		fVec.z = Z[f];
		fVec.x = x;
		fVec.y = y;
		fVec.tx = tx;
		fVec.ty = ty;
		fVec.Q = Q;
		fVec.deltaPath = dPath;
		
		//StateVec = fVec;
		this.trackTraj.put(f, fVec);
		
		//if(transpStateJacobian!=null) {
		//	F = new Matrix(transpStateJacobian); 
		//} 
		if(Cpropagated!=null) {
			//CovMat fCov = new CovMat(f);
			//fCov.covMat = Cpropagated;
			//CovMat = fCov;
			this.trackCov.put(f, fCov);
		}
	}
	
	
	
	public class GStateVec {
		final int k;
		public double z;
		public double x;
		public double y;
		public double tx;
		public double ty;
		public double Q;
		double B;
                double deltaPath;
		
		GStateVec(int k){
			this.k = k;
		}
		
	}
	
	public class CovMat {
		final int k;
		public Matrix covMat;
		
		CovMat(int k){
			this.k = k;
		}
		
	}
	
//    DCSwimmer dcSwim = new DCSwimmer();
    private final float[] bf = new float[3];
    private Swim dcSwim;
    private RungeKutta rk;
    
    /**
     * State vector representing the track in the sector coordinate system at the measurement layer
     */
    public GStateVecs(Swim swimmer) {
        //Max Field Location: (phi, rho, z) = (29.50000, 44.00000, 436.00000)
        // get the maximum value of the B field
        dcSwim = swimmer;
        rk = new RungeKutta();
        //double phi = Math.toRadians(29.5);
        //double rho = 44.0;
        //double z = 436.0;
        //swimmer.BfieldLab(rho*FastMath.cos(phi), rho*FastMath.sin(phi), z, lbf);
        //Bmax = Math.sqrt(lbf[0]*lbf[0]+lbf[1]*lbf[1]+lbf[2]*lbf[2]) *(2.366498/4.322871999651699); // scales according to torus scale by reading the map and averaging the value
     }

	public class B {
		final int k;
		final double z;
		double x;
		double y;
		double tx;
		double ty;
		double s;
		
		public double Bx;
		public double By;
		public double Bz;
		
		B(int sector, int k, double z, double x, double y, double tx, double ty, double s) {
			this.k = k;
			this.z = z;
			this.x = x;
			this.y = y;
			this.tx = tx;
			this.ty = ty;
			this.s = s;
			
//			Point3D bf = dcSwim.Bfield(sector,x,y,z);
			dcSwim.Bfield(sector,x,y,z,bf);
			//dcSwim.Bfield(1,x,y,z,bf); // MO: MUST to add sector dependence here
//			this.Bx = bf.x();
//			this.By = bf.y();
//			this.Bz = bf.z();
			this.Bx = bf[0];
			this.By = bf[1];
			this.Bz = bf[2];
		}
	}
	private double[] A(double tx, double ty, double Bx, double By, double Bz) {
		
		double C = Math.sqrt(1 + tx*tx + ty*ty);
		double Ax = C*( ty*(tx*Bx + Bz) - (1+tx*tx)*By);
		double Ay = C*(-tx*(ty*By + Bz) + (1+ty*ty)*Bx);
		
		return new double[] {Ax, Ay};
	}
	private double[] delA_delt(double tx, double ty, double Bx, double By, double Bz) {
		
		double C2 = 1 + tx*tx + ty*ty;
		double C = Math.sqrt(C2);
		double Ax = C*( ty*(tx*Bx + Bz) - (1+tx*tx)*By);
		double Ay = C*(-tx*(ty*By + Bz) + (1+ty*ty)*Bx);
		
		double delAx_deltx = tx*Ax/C2 +C*(ty*Bx - 2*tx*By);
		double delAx_delty = ty*Ax/C2 +C*(tx*Bx + Bz);
		double delAy_deltx = tx*Ay/C2 +C*(-ty*By - Bz);
		double delAy_delty = ty*Ay/C2 +C*(-tx*By + 2*ty*Bx);
		
		return new double[] {delAx_deltx,delAx_delty,delAy_deltx,delAy_delty};
	}
	
	public String massHypo = "electron";
	
	public double MassHypothesis(String H) {
    	double piMass = 0.13957018;
   	  	double KMass  = 0.493677;
   	  	double muMass = 0.105658369;
   	  	double eMass  = 0.000510998;
   	  	double pMass  = 0.938272029;
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
	
	
	
	public void rinit(int sector, double z0, int gf) { // gf-is reference layer, Z0-next poin to go
		if(this.trackTraj.get(gf)!=null) {
			double x = this.trackTraj.get(gf).x;
			double y = this.trackTraj.get(gf).y;
			double z = this.trackTraj.get(gf).z;
			double tx = this.trackTraj.get(gf).tx;
			double ty = this.trackTraj.get(gf).ty;
			double p = 1./Math.abs(this.trackTraj.get(gf).Q);
			int q = (int) Math.signum(this.trackTraj.get(gf).Q);

//System.out.println("GVinit.sect: " + this.trackTraj.get(0).get_Sector() +" , "+ sector );
			
			dcSwim.SetSwimParameters(-1, x, y, z, tx, ty, p, q);
			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneLab(z0);
			double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneTiltSecSys(sector, z0); // Z0 must be of first layer
			GStateVec initSV = new GStateVec(0);
			if(VecAtFirstMeasSite!=null) {
			 initSV.x  = VecAtFirstMeasSite[0];
			 initSV.y  = VecAtFirstMeasSite[1];
			 initSV.z  = VecAtFirstMeasSite[2];
			 initSV.tx = VecAtFirstMeasSite[3]/VecAtFirstMeasSite[5];
			 initSV.ty = VecAtFirstMeasSite[4]/VecAtFirstMeasSite[5];
			} else { // dcSwim.SwimToPlaneTiltSecSys returns null is no B-field is present
			 initSV.x  = x + tx*(z0-z);
			 initSV.y  = y + ty*(z0-z);
			 initSV.z  = z0;
			 initSV.tx = tx;
			 initSV.ty = ty;
			}
			initSV.Q = this.trackTraj.get(gf).Q;
			this.trackTraj.put(0, initSV); // start from first layer
		} else {
			return;
		}
	}
	
	public void init(Track trkcand, double z0, GFitter gf) {

//System.out.println("GVinit: " + trkcand.get_StateVecAtReg1MiddlePlane() +" , "+ dcSwim );
//System.out.println("GVinit.sect: " + trkcand.get(0).get_Sector() +" , "+ sector );

		if(trkcand.get_StateVecAtReg1MiddlePlane()!=null ) {
			dcSwim.SetSwimParameters(-1, trkcand.get_StateVecAtReg1MiddlePlane().x(),trkcand.get_StateVecAtReg1MiddlePlane().y(),trkcand.get(0).get_Point().z(),
					trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX(),trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY(),trkcand.get_P(),
					 trkcand.get_Q());

//System.out.println("GVinit: " + trkcand.get_StateVecAtReg1MiddlePlane().x() +" , "+ trkcand.get_Q());

			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneLab(z0);
			double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneTiltSecSys(trkcand.get(0).get_Sector(), z0);
			GStateVec initSV = new GStateVec(0);
			if(VecAtFirstMeasSite!=null) {
			 initSV.x = VecAtFirstMeasSite[0];
			 initSV.y = VecAtFirstMeasSite[1];
			 initSV.z = VecAtFirstMeasSite[2];
			 initSV.tx = VecAtFirstMeasSite[3]/VecAtFirstMeasSite[5];
			 initSV.ty = VecAtFirstMeasSite[4]/VecAtFirstMeasSite[5];
			} else { // dcSwim.SwimToPlaneTiltSecSys returns null is no B-field is present
			 initSV.x  = trkcand.get_StateVecAtReg1MiddlePlane().x() + trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX()*(z0-trkcand.get(0).get_Point().z());
			 initSV.y  = trkcand.get_StateVecAtReg1MiddlePlane().y() + trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY()*(z0-trkcand.get(0).get_Point().z());
			 initSV.z  = z0;
			 initSV.tx = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX();
			 initSV.ty = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY();
			}
			initSV.Q = trkcand.get_Q()/trkcand.get_P();
			this.trackTraj.put(0, initSV);
		} else {
			gf.setFitFailed = true;
			return;
		}
		//System.out.println((0)+"] init "+this.trackTraj.get(0).x+","+this.trackTraj.get(0).y+","+
		//		this.trackTraj.get(0).z+","+this.trackTraj.get(0).tx+","+this.trackTraj.get(0).ty+" "+1/this.trackTraj.get(0).Q); 
		
		
		Matrix initCMatrix = new Matrix( new double[][]{
				{    1,       0,       0,       0,  0},
				{    0,       1,       0,       0,  0},
				{    0,       0,       1,       0,  0},
				{    0,       0,       0,       1,  0},
				{    0,       0,       0,       0,  1}
		});
		
		CovMat initCM = new CovMat(0);
		initCM.covMat = initCMatrix;
		this.trackCov.put(0, initCM);
	}


	public void initFromHB(Track trkcand, double z0, GFitter gf) {

//System.out.println("GVinit: " + trkcand.get_StateVecAtReg1MiddlePlane() +" , "+ dcSwim );

		//if(trkcand.get_StateVecAtReg1MiddlePlane()!=null ) {
		//	dcSwim.SetSwimParameters(-1, trkcand.get_StateVecAtReg1MiddlePlane().x(),trkcand.get_StateVecAtReg1MiddlePlane().y(),trkcand.get(0).get_Point().z(),
		//			trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX(),trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY(),trkcand.get_P(),
		//			 trkcand.get_Q());

        if (trkcand != null && trkcand.get_CovMat()!=null) {
            dcSwim.SetSwimParameters(trkcand.get_Vtx0().x(), trkcand.get_Vtx0().y(), trkcand.get_Vtx0().z(), 
                    trkcand.get_pAtOrig().x(), trkcand.get_pAtOrig().y(), trkcand.get_pAtOrig().z(), trkcand.get_Q());
            double[] VecInDCVolume = dcSwim.SwimToPlaneLab(175.);


/*System.out.println("GSV.init: " + trkcand.get_Vtx0().x() +" , "+ trkcand.get_Vtx0().y() +" , "+ trkcand.get_Vtx0().z()
+" , "+ trkcand.get_pAtOrig().x()
+" , "+ trkcand.get_pAtOrig().y()
+" , "+ trkcand.get_pAtOrig().z()
+" , "+ trkcand.get_Q()
+" , z0= "+ z0
);*/

            if(VecInDCVolume==null){
                gf.setFitFailed = true;
                return;
            }
            // rotate to TCS
            Cross C = new Cross(trkcand.get(0).get_Sector(), trkcand.get(0).get_Region(), -1);
        
            Point3D trkR1X = C.getCoordsInTiltedSector(VecInDCVolume[0],VecInDCVolume[1],VecInDCVolume[2]);
            Point3D trkR1P = C.getCoordsInTiltedSector(VecInDCVolume[3],VecInDCVolume[4],VecInDCVolume[5]);
            
            dcSwim.SetSwimParameters(trkR1X.x(), trkR1X.y(), trkR1X.z(), 
                    trkR1P.x(), trkR1P.y(), trkR1P.z(), trkcand.get_Q());

/*System.out.println("GSV.SetSwimParameters: " + trkR1X.x() +" , "+ trkR1X.y() +" , "+ trkR1X.z()
+" , "+ trkR1P.x()
+" , "+ trkR1P.y()
+" , "+ trkR1P.z()
);*/

            double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneTiltSecSys(trkcand.get(0).get_Sector(), z0);
            if(VecAtFirstMeasSite==null){
                gf.setFitFailed = true;
                return;
            }

/*System.out.println("GSV.VecAtFirstMeasSite: " + VecAtFirstMeasSite[0] +" , "+ VecAtFirstMeasSite[1] +" , "+ VecAtFirstMeasSite[2] +" , "+ z0
+" , "+ VecAtFirstMeasSite[3]
+" , "+ VecAtFirstMeasSite[4]
+" , "+ VecAtFirstMeasSite[5]
);*/

//System.out.println("GVinit: " + trkcand.get_StateVecAtReg1MiddlePlane().x() +" , "+ trkcand.get_Q());

			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneLab(z0);
			//double[] VecAtFirstMeasSite = dcSwim.SwimToPlaneTiltSecSys(sector, z0);
			GStateVec initSV = new GStateVec(0);

// MO: this does not exist anymore (not saved into HB bank)			
/*System.out.println("GSV.get_Trajectory: " + trkcand.get_Trajectory() );

System.out.println("GSV.get_Trajectory.get(0): " + trkcand.get_Trajectory() +" , "+ trkcand.get_Trajectory().get(0));

org.jlab.rec.dc.trajectory.StateVec firstSV = trkcand.get_Trajectory().get(0);

initSV.x = firstSV.x();
initSV.y = firstSV.y();
initSV.z = firstSV.getZ();
initSV.tx = firstSV.tanThetaX();
initSV.ty = firstSV.tanThetaY();*/

			//if(false) {
			if(VecAtFirstMeasSite!=null) {
			 initSV.x = VecAtFirstMeasSite[0];
			 initSV.y = VecAtFirstMeasSite[1];
			 initSV.z = VecAtFirstMeasSite[2];
			 initSV.tx = VecAtFirstMeasSite[3]/VecAtFirstMeasSite[5];
			 initSV.ty = VecAtFirstMeasSite[4]/VecAtFirstMeasSite[5];
			} else { // dcSwim.SwimToPlaneTiltSecSys returns null is no B-field is present
			 initSV.x  = trkcand.get_StateVecAtReg1MiddlePlane().x() + trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX()*(z0-trkcand.get(0).get_Point().z());
			 initSV.y  = trkcand.get_StateVecAtReg1MiddlePlane().y() + trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY()*(z0-trkcand.get(0).get_Point().z());
			 initSV.z  = z0;
			 initSV.tx = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX();
			 initSV.ty = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY();
			}
			//}
			//initSV.Q = trkcand.get_Q()/trkcand.get_P(); // MO:
			double P_r1l1_tilt=Math.sqrt(VecAtFirstMeasSite[3]*VecAtFirstMeasSite[3]
		                                    +VecAtFirstMeasSite[4]*VecAtFirstMeasSite[4]
						    +VecAtFirstMeasSite[5]*VecAtFirstMeasSite[5]);
			if(P_r1l1_tilt>0) initSV.Q = trkcand.get_Q()/P_r1l1_tilt;
			else initSV.Q = -0.001; // MO: momentum not defined
			this.trackTraj.put(0, initSV);
		} else {
			gf.setFitFailed = true;
			return;
		}
		//System.out.println((0)+"] init "+this.trackTraj.get(0).x+","+this.trackTraj.get(0).y+","+
		//		this.trackTraj.get(0).z+","+this.trackTraj.get(0).tx+","+this.trackTraj.get(0).ty+" "+1/this.trackTraj.get(0).Q); 
		
		
		Matrix initCMatrix = new Matrix( new double[][]{
				{    1,       0,       0,       0,  0},
				{    0,       1,       0,       0,  0},
				{    0,       0,       1,       0,  0},
				{    0,       0,       0,       1,  0},
				{    0,       0,       0,       0,  1}
		});
		
		CovMat initCM = new CovMat(0);
		initCM.covMat = initCMatrix;
		this.trackCov.put(0, initCM);
	}


	public void printMatrix(Matrix C) {
		for(int k = 0; k< 5; k++) {
			System.out.println(C.get(k, 0)+"	"+C.get(k, 1)+"	"+C.get(k, 2)+"	"+C.get(k, 3)+"	"+C.get(k, 4));
		}
	}
}

