package org.jlab.rec.dc.track.fit;

import Jama.Matrix;
import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.clas.math.FastMath;
import org.jlab.clas.swimtools.Swim;
import org.jlab.rec.dc.track.Track;

public class StateVecs {
    private double Bmax = 2.366498; // averaged
    
    final double speedLight = 0.002997924580;
    public double[] Z;
   // public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

   
    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;
    private final double[] A = new double[2];
    private final double[] dA = new double[4];
    private final float[] bf = new float[3];
    private final float[] lbf = new float[3];
    private Swim dcSwim;
    private RungeKutta rk;
    
    /**
     * State vector representing the track in the sector coordinate system at the measurement layer
     */
    public StateVecs(Swim swimmer) {
        //Max Field Location: (phi, rho, z) = (29.50000, 44.00000, 436.00000)
        // get the maximum value of the B field
        dcSwim = swimmer;
        rk = new RungeKutta();
    }
    
    /**
     * 
     * @param i initial state vector index
     * @param f final state vector index
     * @param iVec state vector at the initial index
     * @param covMat state covariance matrix at the initial index
     */
    public void transport(int sector, int i, int f, StateVec iVec, CovMat covMat) { // s = signed step-size
        if(iVec==null)
            return;
        double stepSize = 1.0;
        StateVecs.StateVec fVec = new StateVec(f);
        CovMat fCov = new CovMat(f);
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.ty = iVec.ty;
        fVec.Q = iVec.Q;
        fVec.B = iVec.B;
        fCov.covMat = covMat.covMat;
        
        double s  = 0;
        double z = Z[i];
        double BatMeas = iVec.B;
        
        while(Math.signum(Z[f] - Z[i]) *z<Math.signum(Z[f] - Z[i]) *Z[f]) {
            //System.out.println(" RK step num "+(j+1)+" = "+(float)s+" nSteps = "+nSteps);
            double x =  fVec.x;
            double y =  fVec.y;
            z = fVec.z;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q =  fVec.Q;
            double dPath = fVec.deltaPath;
            covMat.covMat = fCov.covMat; 
            
            s= Math.signum(Z[f] - Z[i]) * stepSize;
           // System.out.println(" from "+(float)Z[i]+" to "+(float)Z[f]+" at "+(float)z+" By is "+bf[1]+" B is "+Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2])/Bmax+" stepSize is "+s);
            if(Math.signum(Z[f] - Z[i]) *(z+s)>Math.signum(Z[f] - Z[i]) *Z[f])
                s=Math.signum(Z[f] - Z[i]) *Math.abs(Z[f]-z);
            
            rk.RK4transport( sector, Q, x, y, z, tx, ty, s, dcSwim,
                        covMat, fVec, fCov, mass, dPath);
            
            if( Math.abs(fVec.B - BatMeas)<0.0001)
                stepSize*=2;
                    
            BatMeas = fVec.B;
        }
        
        this.trackTraj.put(f, fVec);
        this.trackCov.put(f, fCov);
    }
    
    /**
     * 
     * @param i initial state vector index
     * @param f final state vector index
     * @param iVec state vector at the initial index
     * @param covMat state covariance matrix at the initial index
     */
    public void transportFixed(int sector, int i, int f, StateVec iVec, CovMat covMat) { // s = signed step-size
        if(iVec==null)
            return;
        double stepSize = 0.5;
        
        StateVecs.StateVec fVec = new StateVec(f);
        CovMat fCov = new CovMat(f);
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.ty = iVec.ty;
        fVec.Q = iVec.Q;
        fCov.covMat = covMat.covMat;
        int nSteps = (int) (Math.abs((Z[i] - Z[f]) / stepSize) + 1);

        double s  = (Z[f] - Z[i]) / (double) nSteps;
        double z = Z[i];
        
        for (int j = 0; j < nSteps; j++) {
            // get the sign of the step
            if (j == nSteps - 1) {
                s = Math.signum(Z[f] - Z[i]) * Math.abs(z - Z[f]);
            }
            //System.out.println(" RK step num "+(j+1)+" = "+(float)s+" nSteps = "+nSteps);
            double x =  fVec.x;
            double y =  fVec.y;
            z = fVec.z;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q =  fVec.Q;
            double dPath = fVec.deltaPath;
            covMat.covMat = fCov.covMat; 
            
            rk.RK4transport( sector, Q, x, y, z, tx, ty, s, dcSwim,
                        covMat, fVec, fCov, mass, dPath);
            
        }
        
        this.trackTraj.put(f, fVec);
        this.trackCov.put(f, fCov);
    }
    
    
    private void A(double tx, double ty, double Bx, double By, double Bz, double[] a) {

        double C = Math.sqrt(1 + tx * tx + ty * ty);
        a[0] = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        a[1] = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);
    }

    private void delA_delt(double tx, double ty, double Bx, double By, double Bz, double[] dela_delt) {

        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        dela_delt[0] = tx * Ax / C2 + C * (ty * Bx - 2 * tx * By); //delAx_deltx
        dela_delt[1] = ty * Ax / C2 + C * (tx * Bx + Bz); //delAx_delty
        dela_delt[2] = tx * Ay / C2 + C * (-ty * By - Bz); //delAy_deltx
        dela_delt[3] = ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx); //delAy_delty
    }

    
    private double mass = 0.13957018;
    public void setMass(int hypo, double mass) {
          
        switch (hypo) {
            case 0:
                mass = 0.000510998;
                break;
            case 1:
                mass = 0.13957018;
                break;
            case 2:    
                mass = 0.493677;
                break;
            case 3:  
                mass = 0.105658369;
                break;
            case 4:  
                mass = 0.938272029;    
                break;
        }
    }
    
    
    /**
     * 
     * @param trkcand the track candidate
     * @param z0 the value in z to which the track is swam back to
     * @param kf the final state measurement index
     */
    public void init(Track trkcand, double z0, KFitter kf) {
        
        if (trkcand.get_StateVecAtReg1MiddlePlane() != null) {
            
            StateVec initSV = new StateVec(0);
            initSV.x = trkcand.get_StateVecAtReg1MiddlePlane().x();
            initSV.y = trkcand.get_StateVecAtReg1MiddlePlane().y();
            initSV.z = trkcand.get(0).get_Point().z();
            initSV.tx = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX();
            initSV.ty = trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY();
            initSV.Q = trkcand.get_Q() / trkcand.get_P();
            
            rk.SwimToZ(trkcand.get(0).get_Sector(), initSV, dcSwim, z0, bf);
            
            this.trackTraj.put(0, initSV);
        } else {
            kf.setFitFailed = true;
            return;
        }
        //System.out.println((0)+"] init "+this.trackTraj.get(0).x+","+this.trackTraj.get(0).y+","+
        //		this.trackTraj.get(0).z+","+this.trackTraj.get(0).tx+","+this.trackTraj.get(0).ty+" "+1/this.trackTraj.get(0).Q); 
        double err_sl1 = trkcand.get(0).get_Segment1().get_fittedCluster().get_clusterLineFitSlopeErr();
        double err_sl2 = trkcand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitSlopeErr();
        double err_it1 = trkcand.get(0).get_Segment1().get_fittedCluster().get_clusterLineFitInterceptErr();
        double err_it2 = trkcand.get(0).get_Segment2().get_fittedCluster().get_clusterLineFitInterceptErr();
        double wy_over_wx = (FastMath.cos(Math.toRadians(6.)) / FastMath.sin(Math.toRadians(6.)));

        double eux = 0.5 * Math.sqrt(err_sl1 * err_sl1 + err_sl2 * err_sl2);
        double euy = 0.5 * wy_over_wx * Math.sqrt(err_sl1 * err_sl1 + err_sl2 * err_sl2);
        double z = trkcand.get(0).get_Point().z();
        double ex = 0.5 * Math.sqrt(err_it1 * err_it1 + err_it2 * err_it2 + z * z * (err_sl1 * err_sl1 + err_sl2 * err_sl2));
        double ey = 0.5 * wy_over_wx * Math.sqrt(err_it1 * err_it1 + err_it2 * err_it2 + z * z * (err_sl1 * err_sl1 + err_sl2 * err_sl2));
        double epSq = 0.001 * trkcand.get_P() * trkcand.get_P();

        Matrix initCMatrix = new Matrix(new double[][]{
            {ex * ex, 0, 0, 0, 0},
            {0, ey * ey, 0, 0, 0},
            {0, 0, eux * eux, 0, 0},
            {0, 0, 0, euy * euy, 0},
            {0, 0, 0, 0, epSq}
        });

        CovMat initCM = new CovMat(0);
        initCM.covMat = initCMatrix;
        this.trackCov.put(0, initCM);
    }

    
    void initFromHB(Track trkcand, double z0, KFitter kf) { 
        if (trkcand != null && trkcand.getFinalStateVec()!=null 
                && trkcand.get_CovMat()!=null) {
            
            StateVec initSV = new StateVec(0);
            initSV.x = trkcand.getFinalStateVec().x();
            initSV.y = trkcand.getFinalStateVec().y();
            initSV.z = trkcand.getFinalStateVec().getZ();
            initSV.tx = trkcand.getFinalStateVec().tanThetaX();
            initSV.ty = trkcand.getFinalStateVec().tanThetaY();
            initSV.Q = ((double) trkcand.get_Q())/trkcand.get_P();
            
            rk.SwimToZ(trkcand.get(0).get_Sector(), initSV, dcSwim, z0, bf);
            this.trackTraj.put(0, initSV); 
            
            CovMat initCM = new CovMat(0);
            initCM.covMat = trkcand.get_CovMat(); 
            this.trackCov.put(0, initCM); 
        } else {
            kf.setFitFailed = true;
            return;
        }
        
    }
    
    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            for (int j = 0; j < 5; j++) {
                System.out.println("C["+j+"]["+k+"] = "+C.get(j, k));
            }
        }
    }
    
    /**
     * The state vector representing the track at a given measurement site
     */
    public class StateVec {
        
        final int k;        //index
        public double z;    //z (fixed measurement planes)
        public double x;    //track x in the tilted sector coordinate system at z
        public double y;    //track y in the tilted sector coordinate system at z
        public double tx;   //track px/pz in the tilted sector coordinate system at z
        public double ty;   //track py/pz in the tilted sector coordinate system at z
        public double Q;    //track q/p
        double B;
        double deltaPath;
        
        StateVec(int k) {
            this.k = k;
        }

        String printInfo() {
            return this.k+"] = "+(float)this.x+", "+(float)this.y+", "+(float)this.z+", "
                    +(float)this.tx+", "+(float)this.ty+", "+(float)this.Q+" B = "+(float)this.B;
        }
    }
    /**
     * The track covariance matrix
     */
    public class CovMat {
        
        final int k;
        public Matrix covMat;
        
        CovMat(int k) {
            this.k = k;
        }
        
    }
}
