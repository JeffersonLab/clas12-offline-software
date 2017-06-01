package org.jlab.rec.dc.track.fit.basefit;

import org.jlab.rec.dc.track.fit.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.track.Track;
import org.jlab.rec.dc.trajectory.DCSwimmer;

import Jama.Matrix;

public class StateVecs {

    final double speedLight = 0.002997924580;
    public double[] Z;
    public List<B> bfieldPoints = new ArrayList<B>();
    public Map<Integer, StateVec> trackTraj = new HashMap<Integer, StateVec>();
    public Map<Integer, CovMat> trackCov = new HashMap<Integer, CovMat>();

    public double stepSize = 0.2; // step size 
    public StateVec StateVec;
    public CovMat CovMat;
    public Matrix F;
    private double[] A;
    private double[] dA;
    private double[][] transpStateJacobian;

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

            B bf = new B(i, z, x, y, tx, ty, s);

            double[] A = A(tx, ty, bf.Bx, bf.By, bf.Bz);
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

    public void transport(int i, int f, StateVec iVec, CovMat covMat) { // s = signed step-size
        //StateVec iVec = trackTraj.get(i);
        //bfieldPoints = new ArrayList<B>();
       // CovMat covMat = icovMat;
     //   double[] A = new double[5];
      //  double[] dA = new double[5];
        double[][] u = new double[5][5];       
        double[][] C = new double[covMat.covMat.getRowDimension()][covMat.covMat.getColumnDimension()];
       // Matrix Cpropagated = null;
        //double[][] transpStateJacobian = null;

        double x = iVec.x;
        double y = iVec.y;
        double tx = iVec.tx;
        double ty = iVec.ty;
        double Q = iVec.Q;

        float[] bf = new float[3];
        dcSwim.Bfield(x, y, Z[i], bf);
        double Bmax = 2.366498 * Math.abs(Constants.getTORSCALE());
       // if (bfieldPoints.size() > 0) {
        //    double B = new Vector3D(bfieldPoints.get(bfieldPoints.size() - 1).Bx, bfieldPoints.get(bfieldPoints.size() - 1).By, bfieldPoints.get(bfieldPoints.size() - 1).Bz).mag();
        if (Math.abs(bf[1]) > 0) {
            double B = Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2]);
            if (B / Bmax > 0.01) {
                stepSize = 0.15;
            }
            if (B / Bmax > 0.02) {
                stepSize = 0.1;
            }
            if (B / Bmax > 0.05) {
                stepSize = 0.075;
            }
            if (B / Bmax > 0.1) {
                stepSize = 0.05;
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

            if (j == nSteps - 1) {
                s = Math.signum(Z[f] - Z[i]) * Math.abs(z - Z[f]);
            }

            //B bf = new B(i, z, x, y, tx, ty, s);
            //bfieldPoints.add(bf);
            dcSwim.Bfield(x, y, z, bf);
            A = A(tx, ty, bf[0], bf[1], bf[2]);
            dA = delA_delt(tx, ty, bf[0], bf[1], bf[2]);

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

            transpStateJacobian = new double[][]{
                {1, 0, delx_deltx0, delx_delty0, delx_delQ},
                {0, 1, dely_deltx0, dely_delty0, dely_delQ},
                {0, 0, 1, deltx_delty0, deltx_delQ},
                {0, 0, delty_deltx0, 1, delty_delQ},
                {0, 0, 0, 0, 1}
            };
            
            

            //covMat = FCF^T; u = FC;
            for (int j1 = 0; j1 < 5; j1++) {
                u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * transpStateJacobian[0][2] + covMat.covMat.get(3,j1)* transpStateJacobian[0][3] + covMat.covMat.get(4,j1) * transpStateJacobian[0][4];
                u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * transpStateJacobian[1][2] + covMat.covMat.get(3,j1) * transpStateJacobian[1][3] + covMat.covMat.get(4,j1) * transpStateJacobian[1][4];
                u[2][j1] = covMat.covMat.get(2,j1) + covMat.covMat.get(3,j1) * transpStateJacobian[2][3] + covMat.covMat.get(4,j1) * transpStateJacobian[2][4];
                u[3][j1] = covMat.covMat.get(2,j1) * transpStateJacobian[3][2] + covMat.covMat.get(3,j1) + covMat.covMat.get(4,j1) * transpStateJacobian[3][4];
                u[4][j1] = covMat.covMat.get(4,j1);
            }

            for (int i1 = 0; i1 < 5; i1++) {
                C[i1][0] = u[i1][0] + u[i1][2] * transpStateJacobian[0][2] + u[i1][3] * transpStateJacobian[0][3] + u[i1][4] * transpStateJacobian[0][4];
                C[i1][1] = u[i1][1] + u[i1][2] * transpStateJacobian[1][2] + u[i1][3] * transpStateJacobian[1][3] + u[i1][4] * transpStateJacobian[1][4];
                C[i1][2] = u[i1][2] + u[i1][3] * transpStateJacobian[2][3] + u[i1][4] * transpStateJacobian[2][4];
                C[i1][3] = u[i1][2] * transpStateJacobian[3][2] + u[i1][3] + u[i1][4] * transpStateJacobian[3][4];
                C[i1][4] = u[i1][4];
            }

            // Q	
            double p = Math.abs(1. / Q);
            double pz = p / Math.sqrt(1 + tx * tx + ty * ty);
            double px = tx * pz;
            double py = ty * pz;

            double t_ov_X0 = Math.signum(Z[f] - Z[i]) * s / Constants.ARGONRADLEN; //path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            //double mass = this.MassHypothesis(this.massHypo); // assume given mass hypothesis
            double mass = MassHypothesis("electron"); // assume given mass hypothesis
            if (Q > 0) {
                mass = MassHypothesis("proton");
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

    public class StateVec {

        final int k;
        public double z;
        public double x;
        public double y;
        public double tx;
        public double ty;
        public double Q;

        StateVec(int k) {
            this.k = k;
        }

    }

    public class CovMat {

        final int k;
        public Matrix covMat;

        CovMat(int k) {
            this.k = k;
        }

    }

    DCSwimmer dcSwim = new DCSwimmer();

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

    private double[] A(double tx, double ty, double Bx, double By, double Bz) {

        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        return new double[]{Ax, Ay};
    }

    private double[] delA_delt(double tx, double ty, double Bx, double By, double Bz) {

        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(C2);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        double delAx_deltx = tx * Ax / C2 + C * (ty * Bx - 2 * tx * By);
        double delAx_delty = ty * Ax / C2 + C * (tx * Bx + Bz);
        double delAy_deltx = tx * Ay / C2 + C * (-ty * By - Bz);
        double delAy_delty = ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx);

        return new double[]{delAx_deltx, delAx_delty, delAy_deltx, delAy_delty};
    }

    public String massHypo = "electron";

    public double MassHypothesis(String H) {
        double piMass = 0.13957018;
        double KMass = 0.493677;
        double muMass = 0.105658369;
        double eMass = 0.000510998;
        double pMass = 0.938272029;
        double value = piMass; //default
        if (H.equals("proton")) {
            value = pMass;
        }
        if (H.equals("electron")) {
            value = eMass;
        }
        if (H.equals("pion")) {
            value = piMass;
        }
        if (H.equals("kaon")) {
            value = KMass;
        }
        if (H.equals("muon")) {
            value = muMass;
        }
        return value;
    }

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
            System.out.println(C.get(k, 0) + "	" + C.get(k, 1) + "	" + C.get(k, 2) + "	" + C.get(k, 3) + "	" + C.get(k, 4));
        }
    }
}
