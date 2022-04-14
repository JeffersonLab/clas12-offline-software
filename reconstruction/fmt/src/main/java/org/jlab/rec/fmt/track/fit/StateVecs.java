package org.jlab.rec.fmt.track.fit;

import org.jlab.jnp.matrix.*;
import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.swimtools.Swim;

/**
 *
 * @author ziegler
 */
public class StateVecs {
    private final double Bmax = 2.366498; // averaged

    final double speedLight = 0.002997924580;
    public double[] Z;
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F = new Matrix();
    private final Matrix fMS = new Matrix();
    private final Matrix copyMatrix = new Matrix();
    private final double[] A = new double[2];
    private final double[] dA = new double[4];
    private final float[] bf = new float[3];
    private final float[] lbf = new float[3];
    private final Swim dcSwim;
    private final RungeKutta rk;

    /**
     * State vector representing the track in the sector coordinate system at the measurement layer
     * @param swimmer
     */
    public StateVecs(Swim swimmer) {
        dcSwim = swimmer;
        rk = new RungeKutta();
    }

    /**
     *
     * @param sector
     * @param i initial state vector index
     * @param Zf
     * @param iVec state vector at the initial index
     * @param covMat state covariance matrix at the initial index
     * @return 
     */
    public Matrix transport(int sector, int i, double Zf, StateVec iVec, CovMat covMat) { // s = signed step-size
        double stepSize = 1.0;
        StateVecs.StateVec fVec = new StateVec(0);
        CovMat fCov = new CovMat(0);
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.ty = iVec.ty;
        fVec.Q = iVec.Q;
        fVec.B = iVec.B;

        Matrix5x5.copy(covMat.covMat, fCov.covMat);
        double s  = 0;
        double z = Z[i];
        double BatMeas = iVec.B;

        while(Math.signum(Zf - Z[i]) *z<Math.signum(Zf - Z[i]) *Zf) {
            double x =  fVec.x;
            double y =  fVec.y;
            z = fVec.z;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q =  fVec.Q;
            double dPath = fVec.deltaPath;

            Matrix5x5.copy(fCov.covMat, covMat.covMat);
            s= Math.signum(Zf - Z[i]) * stepSize;

            if (Math.signum(Zf - Z[i]) *(z+s)>Math.signum(Zf - Z[i]) *Zf)
                s=Math.signum(Zf - Z[i]) *Math.abs(Zf-z);

            rk.RK4transport(sector, Q, x, y, z, tx, ty, s, dcSwim,
                        covMat, fVec, fCov, dPath);

            // Q  process noise matrix estimate

            double p = Math.abs(1. / iVec.Q);

            double X0 = this.getX0(z);
            double t_ov_X0 = Math.abs(s) / X0;//path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            double beta = this.beta;
            if(beta>1.0 || beta<=0)
                beta =1.0;

            double sctRMS = 0;

            if(Math.abs(s)>0)
                sctRMS = ((0.0136)/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
                    (1 + 0.038 * Math.log(t_ov_X0));

            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;

            fMS.set(
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, cov_txtx, cov_txty, 0,
                    0, 0, cov_txty, cov_tyty, 0,
                    0, 0, 0, 0, 0
            );

            Matrix5x5.copy(fCov.covMat, copyMatrix);
            Matrix5x5.add(copyMatrix, fMS, fCov.covMat);

            // end add process noise
            if (Math.abs(fVec.B - BatMeas) < 0.0001) stepSize*=2;
            BatMeas = fVec.B;
        }

        return fCov.covMat;
    }

    /**
     *
     * @param sector
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
        //fCov.covMat = covMat.covMat;
        Matrix5x5.copy(covMat.covMat, fCov.covMat);
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
            //covMat.covMat = fCov.covMat;
            Matrix5x5.copy(fCov.covMat, covMat.covMat);
            s= Math.signum(Z[f] - Z[i]) * stepSize;
           // System.out.println(" from "+(float)Z[i]+" to "+(float)Z[f]+" at "+(float)z+" By is "+bf[1]+" B is "+Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2])/Bmax+" stepSize is "+s);
            if(Math.signum(Z[f] - Z[i]) *(z+s)>Math.signum(Z[f] - Z[i]) *Z[f])
                s=Math.signum(Z[f] - Z[i]) *Math.abs(Z[f]-z);

            rk.RK4transport(sector, Q, x, y, z, tx, ty, s, dcSwim,
                        covMat, fVec, fCov, dPath);

            // Q  process noise matrix estimate

            double p = Math.abs(1. / iVec.Q);

            double X0 = this.getX0(z);
            double t_ov_X0 = Math.abs(s) / X0;//path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            double beta = this.beta;
            if(beta>1.0 || beta<=0)
                beta =1.0;

            double sctRMS = 0;

            if(Math.abs(s)>0)
                sctRMS = ((0.0136)/(beta*PhysicsConstants.speedOfLight()*p))*Math.sqrt(t_ov_X0)*
                    (1 + 0.038 * Math.log(t_ov_X0));


            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;


            //if (Math.signum(Z[f] - Z[i]) > 0) {
                fMS.set(
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, cov_txtx, cov_txty, 0,
                0, 0, cov_txty, cov_tyty, 0,
                0, 0, 0, 0, 0
                );

                Matrix5x5.copy(fCov.covMat, copyMatrix);
                Matrix5x5.add(copyMatrix, fMS, fCov.covMat);

            //}
            // end add process noise

            if( Math.abs(fVec.B - BatMeas)<0.0001)
                stepSize*=2;

            BatMeas = fVec.B;
        }
        this.trackTraj.put(f, fVec);
        this.trackCov.put(f, fCov);
    }
    double AIRRADLEN = 30400; // radiation length in cm
    public double getX0(double z) {

        return AIRRADLEN;
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
        StateVecs.CovMat fCov = new CovMat(f);
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

            rk.RK4transport(sector, Q, x, y, z, tx, ty, s, dcSwim,
                        covMat, fVec, fCov, dPath);

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


    private double beta = 1.0;
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
     * @param sector
     * @param xVtx
     * @param yVtx
     * @param zVtx
     * @param z0 the value in z to which the track is swam back to
     * @param pyVtx
     * @param pzVtx
     * @param q
     * @param kf the final state measurement index
     * @param pxVtx
     * @param c
     */
    public void init(int sector, double xVtx, double yVtx, double zVtx,
            double pxVtx, double pyVtx, double pzVtx,
            int q,
            double z0, KFitter kf, int c) {

        StateVec initSV = new StateVec(0);
        initSV.x = xVtx;
        initSV.y = yVtx;
        initSV.z = zVtx;
        initSV.tx = pxVtx/pzVtx;
        initSV.ty = pyVtx/pzVtx;
        double p = Math.sqrt(pxVtx*pxVtx+pyVtx*pyVtx+pzVtx*pzVtx);
        initSV.Q = (double)q / p;

        rk.SwimToZ(sector, initSV, dcSwim, z0, bf);

        if (initSV != null) {

            this.trackTraj.put(0, initSV);
        } else {
            kf.setFitFailed = true;
            return;
        }

        CovMat initCM = new CovMat(0);
        StateVec rinitSV = new StateVec(0);
        rinitSV.x = xVtx;
        rinitSV.y = yVtx;
        rinitSV.z = zVtx;
        rinitSV.tx = pxVtx/pzVtx;
        rinitSV.ty = pyVtx/pzVtx;
        rinitSV.Q = (double)q / p;
        double[] FTF = new double[25];
        double[] F = this.F(sector, z0, rinitSV);
        for(int i = 0; i<5; i++) {
            FTF[i*5+i]=F[i]*F[i];
        }
        Matrix initCMatrix = new Matrix();
        initCMatrix.set(FTF);
        initCM.covMat = initCMatrix;
        this.trackCov.put(0, initCM);
    }
    private StateVec reset(StateVec SVplus, StateVec stateVec) {
        SVplus = new StateVec(stateVec.k);
        SVplus.x = stateVec.x;
        SVplus.y = stateVec.y;
        SVplus.tx = stateVec.tx;
        SVplus.ty = stateVec.ty;
        SVplus.z = stateVec.z;
        SVplus.Q = stateVec.Q;

        return SVplus;
    }
    private void swimToSite(int sector, double z0,
            StateVec SVplus, StateVec SVminus) {

        rk.SwimToZ(sector, SVplus, dcSwim, z0, bf);
        rk.SwimToZ(sector, SVminus, dcSwim, z0, bf);
    }

    double[] F(int sector, double z0, StateVec stateVec) {
        double[] _F = new double[5];
        StateVec SVplus = null;
        StateVec SVminus = null;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_x = 0.05;
        SVplus.x += delt_x/2.;
        SVminus.x-= delt_x/2.;

        this.swimToSite(sector, z0, SVplus, SVminus);

        _F[0] = (SVplus.x - SVminus.x)/delt_x;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_y = 0.05;
        SVplus.y += delt_y/2.;
        SVminus.y-= delt_y/2.;

        this.swimToSite(sector, z0, SVplus, SVminus);

        _F[1] = (SVplus.y - SVminus.y)/delt_y;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_tx = 0.001;
        SVplus.tx += delt_tx/2.;
        SVminus.tx-= delt_tx/2.;

        this.swimToSite(sector, z0, SVplus, SVminus);

        _F[2] = (SVplus.tx - SVminus.tx)/delt_tx;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);

        double delt_ty = 0.001;
        SVplus.ty += delt_ty/2.;
        SVminus.ty-= delt_ty/2.;

        this.swimToSite(sector, z0, SVplus, SVminus);

        _F[3] = (SVplus.ty - SVminus.ty)/delt_ty;

        SVplus = this.reset(SVplus, stateVec);
        SVminus = this.reset(SVminus, stateVec);


        _F[4] = 0.01/Math.abs(SVplus.Q);

        return _F;

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

        public double getPx() {
            return this.getPz()*tx;
        }

        public double getPy() {
            return this.getPz()*ty;
        }

        public double getPz() {
            double pz = this.getP()/Math.sqrt(tx*tx+ty*ty+1);
            return pz;
        }

        public double getP() {
            return 1./Math.abs(this.Q);
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
        public Matrix covMat = new Matrix();

        CovMat(int k) {
            this.k = k;
        }

    }
}
