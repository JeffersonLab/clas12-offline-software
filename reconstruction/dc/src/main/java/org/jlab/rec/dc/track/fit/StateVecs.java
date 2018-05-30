package org.jlab.rec.dc.track.fit;

import Jama.Matrix;
import java.util.HashMap;
import java.util.Map;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.trajectory.DCSwimmer;

public class StateVecs {
    private double Bmax = 2.366498; // averaged
    
    final double speedLight = 0.002997924580;
    public double[] Z;
   // public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    private double stepSize = 0.2; // step size 
    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;
    private final double[] A = new double[2];
    private final double[] dA = new double[4];
    private final float[] bf = new float[3];
    private DCSwimmer dcSwim;
    
    /**
     * State vector representing the track in the sector coordinate system at the measurement layer
     */
    public StateVecs(DCSwimmer dcSwimmer) {
        dcSwim = dcSwimmer;
        //Max Field Location: (phi, rho, z) = (29.50000, 44.00000, 436.00000)
        // get the maximum value of the B field
        double phi = Math.toRadians(29.5);
        double rho = 44.0;
        double z = 436.0;
        Bmax = dcSwim.BfieldLab(rho*Math.cos(phi), rho*Math.sin(phi), z).toVector3D().mag() *(2.366498/4.322871999651699); // scales according to torus scale by reading the map and averaging the value
    }
    
    /**
     * 
     * @param i initial state index
     * @param f final state index
     * @param iVec state vector at the initial state index
     * @return state vector at the final state index
     */
    public StateVec f(int i, int f, StateVec iVec) {

        double x = iVec.x;
        double y = iVec.y;
        double tx = iVec.tx;
        double ty = iVec.ty;
        double Q = iVec.Q;

        int nSteps = (int) (Math.abs((Z[i] - Z[f]) / stepSize) + 1);

        double s = Math.signum(Z[f] - Z[i]) * stepSize;

        double z = Z[i];

        for (int j = 0; j < nSteps; j++) {

            if (j == nSteps - 1) {
                s = Math.signum(Z[f] - Z[i]) * Math.abs(z - Z[f]);
            }

           // propagate the state vector a next step
            dcSwim.Bfield(x, y, z, bf);
            A(tx, ty, bf[0], bf[1], bf[2], A);
            // transport stateVec
            x += tx * s + 0.5 * Q * speedLight * A[0] * s * s;
            y += ty * s + 0.5 * Q * speedLight * A[1] * s * s;
            tx += Q * speedLight * A[0] * s;
            ty += Q * speedLight * A[1] * s;

            z += s;
        }

        StateVec fVec = new StateVec(f);
        fVec.z = Z[f];
        fVec.x = x;
        fVec.y = y;
        fVec.tx = tx;
        fVec.ty = ty;
        fVec.Q = Q;

        return fVec;

    }
    
    /**
     * 
     * @param i initial state vector index
     * @param f final state vector index
     * @param iVec state vector at the initial index
     * @param covMat state covariance matrix at the initial index
     */
    public void transport(int i, int f, StateVec iVec, CovMat covMat) { // s = signed step-size
        if(iVec==null)
            return;
        //StateVec iVec = trackTraj.get(i);
        //bfieldPoints = new ArrayList<B>();
       // CovMat covMat = icovMat;
     //   double[] A = new double[5];
      //  double[] dA = new double[5];
        double[][] u = new double[5][5];       
        double[][] C = new double[5][5];
       // Matrix Cpropagated = null;
        //double[][] transpStateJacobian = null;

        double x = iVec.x;
        double y = iVec.y;
        double tx = iVec.tx;
        double ty = iVec.ty;
        double Q = iVec.Q;

        // B-field components at state vector coordinates
        dcSwim.Bfield(x, y, Z[i], bf);
        
       // if (bfieldPoints.size() > 0) {
        //    double B = new Vector3D(bfieldPoints.get(bfieldPoints.size() - 1).Bx, bfieldPoints.get(bfieldPoints.size() - 1).By, bfieldPoints.get(bfieldPoints.size() - 1).Bz).mag();
        if (bf!=null) { // get the step size used in swimming as a function of the field intensity in the region traversed
            double B = Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2]); 
            if (B / Bmax > 0.01) {
                stepSize = 0.15*4;
            }
            if (B / Bmax > 0.02) {
                stepSize = 0.1*3;
            }
            if (B / Bmax > 0.05) {
                stepSize = 0.075*2;
            }
            if (B / Bmax > 0.1) {
                stepSize = 0.05*2;
            }
            if (B / Bmax > 0.5) {
                stepSize = 0.02;
            }
            if (B / Bmax > 0.75) {
                stepSize = 0.01;
            }
        }
        
        int nSteps = (int) (Math.abs((Z[i] - Z[f]) / stepSize) + 1);

        double s  = (Z[f] - Z[i]) / (double) nSteps;
        double z = Z[i];

       
        for (int j = 0; j < nSteps; j++) {
            // get the sign of the step
            if (j == nSteps - 1) {
                s = Math.signum(Z[f] - Z[i]) * Math.abs(z - Z[f]);
            }

            //B bf = new B(i, z, x, y, tx, ty, s);
            //bfieldPoints.add(bf);
            dcSwim.Bfield(x, y, z, bf);
            
            A(tx, ty, bf[0], bf[1], bf[2], A);
            delA_delt(tx, ty, bf[0], bf[1], bf[2], dA);

            // transport covMat
            double delx_deltx0 = s;
            double dely_deltx0 = 0.5 * Q * speedLight * s * s * dA[2];
            double deltx_delty0 = Q * speedLight * s * dA[1];
            double delx_delQ = 0.5 * speedLight * s * s * A[0];
            double deltx_delQ = speedLight * s * A[0];
            double delx_delty0 = 0.5 * Q * speedLight * s * s * dA[1];
            double dely_delty0 = s;
            double delty_deltx0 = Q * speedLight * s * dA[2];
            double dely_delQ = 0.5 * speedLight * s * s * A[1];
            double delty_delQ = speedLight * s * A[1];

            
            //double transpStateJacobian00=1; 
            //double transpStateJacobian01=0; 
            double transpStateJacobian02=delx_deltx0; 
            double transpStateJacobian03=delx_delty0; 
            double transpStateJacobian04=delx_delQ;
            //double transpStateJacobian10=0; 
            //double transpStateJacobian11=1; 
            double transpStateJacobian12=dely_deltx0; 
            double transpStateJacobian13=dely_delty0; 
            double transpStateJacobian14=dely_delQ;
            //double transpStateJacobian20=0; 
            //double transpStateJacobian21=0; 
            //double transpStateJacobian22=1; 
            double transpStateJacobian23=deltx_delty0; 
            double transpStateJacobian24=deltx_delQ;
            //double transpStateJacobian30=0; 
            //double transpStateJacobian31=0; 
            double transpStateJacobian32=delty_deltx0; 
            //double transpStateJacobian33=1; 
            double transpStateJacobian34=delty_delQ;
            //double transpStateJacobian40=0; 
            //double transpStateJacobian41=0; 
            //double transpStateJacobian42=0; 
            //double transpStateJacobian43=0; 
            //double transpStateJacobian44=1;
            

            //covMat = FCF^T; u = FC;
            for (int j1 = 0; j1 < 5; j1++) {
                u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * transpStateJacobian02 + covMat.covMat.get(3,j1)* transpStateJacobian03 + covMat.covMat.get(4,j1) * transpStateJacobian04;
                u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * transpStateJacobian12 + covMat.covMat.get(3,j1) * transpStateJacobian13 + covMat.covMat.get(4,j1) * transpStateJacobian14;
                u[2][j1] = covMat.covMat.get(2,j1) + covMat.covMat.get(3,j1) * transpStateJacobian23 + covMat.covMat.get(4,j1) * transpStateJacobian24;
                u[3][j1] = covMat.covMat.get(2,j1) * transpStateJacobian32 + covMat.covMat.get(3,j1) + covMat.covMat.get(4,j1) * transpStateJacobian34;
                u[4][j1] = covMat.covMat.get(4,j1);
            }

            for (int i1 = 0; i1 < 5; i1++) {
                C[i1][0] = u[i1][0] + u[i1][2] * transpStateJacobian02 + u[i1][3] * transpStateJacobian03 + u[i1][4] * transpStateJacobian04;
                C[i1][1] = u[i1][1] + u[i1][2] * transpStateJacobian12 + u[i1][3] * transpStateJacobian13 + u[i1][4] * transpStateJacobian14;
                C[i1][2] = u[i1][2] + u[i1][3] * transpStateJacobian23 + u[i1][4] * transpStateJacobian24;
                C[i1][3] = u[i1][2] * transpStateJacobian32 + u[i1][3] + u[i1][4] * transpStateJacobian34;
                C[i1][4] = u[i1][4];
            }

            // Q  process noise matrix estimate	
            double p = Math.abs(1. / Q);
            double pz = p / Math.sqrt(1 + tx * tx + ty * ty);
            double px = tx * pz;
            double py = ty * pz;

            double t_ov_X0 = Math.signum(Z[f] - Z[i]) * s / Constants.ARGONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            //double mass = this.MassHypothesis(this.massHypo); // assume given mass hypothesis
            double mass = 0.000510998; // assume given mass hypothesis
            if (Q > 0) {
                mass = 0.938272029;
            }

            double beta = p / Math.sqrt(p * p + mass * mass); // use particle momentum
            double cosEntranceAngle = Math.abs((x * px + y * py + z * pz) / (Math.sqrt(x * x + y * y + z * z) * p));
            double pathLength = t_ov_X0 / cosEntranceAngle;

            double sctRMS = (0.0136 / (beta * p)) * Math.sqrt(pathLength) * (1 + 0.038 * Math.log(pathLength)); // Highland-Lynch-Dahl formula

            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;

            if (s > 0) {
                C[2][2] += cov_txtx;
                C[2][3] += cov_txty;
                C[3][2] += cov_txty;
                C[3][3] += cov_tyty;
            }

           
            covMat.covMat = new Matrix(C);
            // transport stateVec
            x += tx * s + 0.5 * Q * speedLight * A[0] * s * s;
            y += ty * s + 0.5 * Q * speedLight * A[1] * s * s;
            tx += Q * speedLight * A[0] * s;
            ty += Q * speedLight * A[1] * s;

            z += s;
        }

        StateVec fVec = new StateVec(f);
        fVec.z = Z[f];
        fVec.x = x;
        fVec.y = y;
        fVec.tx = tx;
        fVec.ty = ty;
        fVec.Q = Q;

        //StateVec = fVec;
        this.trackTraj.put(f, fVec);

        //if(transpStateJacobian!=null) {
        //	F = new Matrix(transpStateJacobian); 
        //} 
        if (covMat.covMat != null) {
            CovMat fCov = new CovMat(f);
            fCov.covMat = covMat.covMat;
            //CovMat = fCov;
            this.trackCov.put(f, fCov);
        }
    }


    /*
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

        B(int k, double z, double x, double y, double tx, double ty, double s) {
            this.k = k;
            this.z = z;
            this.x = x;
            this.y = y;
            this.tx = tx;
            this.ty = ty;
            this.s = s;

            float[] bf = new float[3];
            dcSwim.Bfield(x, y, z, bf);
            this.Bx = bf[0];
            this.By = bf[1];
            this.Bz = bf[2];
        }
    }
*/
    
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

    
     /**
        1 piMass = 0.13957018;
        2 KMass = 0.493677;
        3 muMass = 0.105658369;
        0 eMass = 0.000510998;
        4 pMass = 0.938272029;
        **/
    private void setMass(int hypo, double mass) {
          
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
     * @param z0 the value at which the state vector needs to be reinitialized
     * @param kf the final state measurement index
     */
    public void rinit(double z0, int kf) {
        if (this.trackTraj.get(kf) != null) {
            double x = this.trackTraj.get(kf).x;
            double y = this.trackTraj.get(kf).y;
            double z = this.trackTraj.get(kf).z;
            double tx = this.trackTraj.get(kf).tx;
            double ty = this.trackTraj.get(kf).ty;
            double p = 1. / Math.abs(this.trackTraj.get(kf).Q);
            int q = (int) Math.signum(this.trackTraj.get(kf).Q);

            dcSwim.SetSwimParameters(-1, x, y, z, tx, ty, p, q);
            double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
            StateVec initSV = new StateVec(0);
            if(VecAtFirstMeasSite==null) {
                return;
            }
            initSV.x = VecAtFirstMeasSite[0];
            initSV.y = VecAtFirstMeasSite[1];
            initSV.z = VecAtFirstMeasSite[2];
            initSV.tx = VecAtFirstMeasSite[3] / VecAtFirstMeasSite[5];
            initSV.ty = VecAtFirstMeasSite[4] / VecAtFirstMeasSite[5];
            initSV.Q = this.trackTraj.get(kf).Q;
            this.trackTraj.put(0, initSV);
        } else {
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
            dcSwim.SetSwimParameters(-1, trkcand.get_StateVecAtReg1MiddlePlane().x(), trkcand.get_StateVecAtReg1MiddlePlane().y(), trkcand.get(0).get_Point().z(),
                    trkcand.get_StateVecAtReg1MiddlePlane().tanThetaX(), trkcand.get_StateVecAtReg1MiddlePlane().tanThetaY(), trkcand.get_P(),
                    trkcand.get_Q());

            double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
            StateVec initSV = new StateVec(0);
            initSV.x = VecAtFirstMeasSite[0];
            initSV.y = VecAtFirstMeasSite[1];
            initSV.z = VecAtFirstMeasSite[2];
            initSV.tx = VecAtFirstMeasSite[3] / VecAtFirstMeasSite[5];
            initSV.ty = VecAtFirstMeasSite[4] / VecAtFirstMeasSite[5];
            initSV.Q = trkcand.get_Q() / trkcand.get_P();
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
        double wy_over_wx = (Math.cos(Math.toRadians(6.)) / Math.sin(Math.toRadians(6.)));

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

    public void printMatrix(Matrix C) {
        for (int k = 0; k < 5; k++) {
            for (int j = 0; j < 5; j++) {
                System.out.println("C["+j+"]["+k+"] = "+C.get(j, k));
            }
        }
    }

    void initFromHB(Track trkcand, double z0, KFitter kf) { 
        if (trkcand != null && trkcand.get_CovMat()!=null) {
            dcSwim.SetSwimParameters(trkcand.get_Vtx0().x(), trkcand.get_Vtx0().y(), trkcand.get_Vtx0().z(), 
                    trkcand.get_pAtOrig().x(), trkcand.get_pAtOrig().y(), trkcand.get_pAtOrig().z(), trkcand.get_Q());
            double[] VecInDCVolume = dcSwim.SwimToPlaneLab(175.);
            // rotate to TCS
            Cross C = new Cross(trkcand.get(0).get_Sector(), trkcand.get(0).get_Region(), -1);
        
            Point3D trkR1X = C.getCoordsInTiltedSector(VecInDCVolume[0],VecInDCVolume[1],VecInDCVolume[2]);
            Point3D trkR1P = C.getCoordsInTiltedSector(VecInDCVolume[3],VecInDCVolume[4],VecInDCVolume[5]);
            
            dcSwim.SetSwimParameters(trkR1X.x(), trkR1X.y(), trkR1X.z(), 
                    trkR1P.x(), trkR1P.y(), trkR1P.z(), trkcand.get_Q());
            
            double[] VecAtFirstMeasSite = dcSwim.SwimToPlane(z0);
            StateVec initSV = new StateVec(0);
            initSV.x = VecAtFirstMeasSite[0];
            initSV.y = VecAtFirstMeasSite[1];
            initSV.z = VecAtFirstMeasSite[2];
            initSV.tx = VecAtFirstMeasSite[3] / VecAtFirstMeasSite[5];
            initSV.ty = VecAtFirstMeasSite[4] / VecAtFirstMeasSite[5];
            initSV.Q = trkcand.get_Q() / trkcand.get_pAtOrig().mag(); 
            this.trackTraj.put(0, initSV); 
            
            CovMat initCM = new CovMat(0);
            initCM.covMat = trkcand.get_CovMat(); 
            this.trackCov.put(0, initCM); 
        } else {
            kf.setFitFailed = true;
            return;
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
        
        StateVec(int k) {
            this.k = k;
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
