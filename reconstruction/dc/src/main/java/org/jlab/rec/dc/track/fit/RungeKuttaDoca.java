package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import org.jlab.clas.swimtools.Swim;

/**
 * Swims a given state vector to a given Z position using Runge Kutta 4 transport.
 * @author ziegler
 * @author benkel
 */
public class RungeKuttaDoca {
    private final float[] _b = new float[3];
    final double v = 0.0029979245;

    public RungeKuttaDoca() {}

    /** Swim to Z position without updating the covariance matrix. */
    public void SwimToZ(int sector, StateVecsDoca.StateVec fVec, Swim swim, double z0, float[] bf) {
        double stepSize = 1.0;
        swim.Bfield(sector, fVec.x, fVec.y, fVec.z, bf);

        fVec.B = Math.sqrt(bf[0]*bf[0] + bf[1]*bf[1] + bf[2]*bf[2]);
        double s = fVec.B;
        final double travelSign = Math.signum(z0 - fVec.z);
        double BatMeas = 0;

        while(travelSign * fVec.z < travelSign * z0) {
            s = travelSign * stepSize;
            if (travelSign*(fVec.z+s) > travelSign*z0) s = travelSign*Math.abs(z0-fVec.z);

            this.RK4transport(sector, s, swim, fVec);

            if (Math.abs(fVec.B - BatMeas) < 0.0001) stepSize *= 2;
            BatMeas = fVec.B;
        }
    }

    /**
     * Transport using Runge Kutta 4 without updating the covariance matrix. Lab system = 1, TSC = 0.
     * Internal state array (sN) is defined as {x, y, tx, ty}, since q doesn't change.
     */
    private void RK4transport(int sector, double h, Swim swim, StateVecsDoca.StateVec fVec) {
        // Set initial state.
        double qv = fVec.Q*v;
        double[] s0 = {fVec.x, fVec.y, fVec.tx, fVec.ty};
        double[] sNull = {0, 0, 0, 0};

        // Transport.
        double[] s1 = RK4step(sector, fVec.z, 0,     swim, s0, sNull, qv);
        double[] s2 = RK4step(sector, fVec.z, 0.5*h, swim, s0, s1,    qv);
        double[] s3 = RK4step(sector, fVec.z, 0.5*h, swim, s0, s2,    qv);
        double[] s4 = RK4step(sector, fVec.z, h,     swim, s0, s3,    qv);

        // Set final state.
        fVec.z  += h;
        fVec.x  += this.RK4(s1[0], s2[0], s3[0], s4[0], h);
        fVec.y  += this.RK4(s1[1], s2[1], s3[1], s4[1], h);
        fVec.tx += this.RK4(s1[2], s2[2], s3[2], s4[2], h);
        fVec.ty += this.RK4(s1[3], s2[3], s3[3], s4[3], h);

        fVec.B = Math.sqrt(_b[0]*_b[0] + _b[1]*_b[1] + _b[2]*_b[2]);
        fVec.deltaPath += Math.sqrt((s0[0]-fVec.x)*(s0[0]-fVec.x)+(s0[1]-fVec.y)*(s0[1]-fVec.y)+h*h);
    }

    /** Perform a single RK4 step without updating the covariance matrix. */
    private double[] RK4step(int sector, double z0, double h, Swim swim, double[] sInit,
                             double[] sPrev, double qv) {
        swim.Bfield(sector, sInit[0]+h*sPrev[0], sInit[1]+h*sPrev[1], z0+h, _b);
        double[] sNext = {0,0,0,0};
        sNext[0] = sInit[2] + h*sPrev[2];
        sNext[1] = sInit[3] + h*sPrev[3];
        double C = C(sNext[0], sNext[1]);
        sNext[2] = qv * Ax(C, sNext[0], sNext[1]);
        sNext[3] = qv * Ay(C, sNext[0], sNext[1]);
        return sNext;
    }

    /**
     * Transport using Runge Kutta 4, updating the covariance matrix.
     * Internal state matrix (sN) is defined as:
     *     sN = {{00: x,  01: dx/dtx0,  02: dx/dty0,  03: dx/dq0},
     *           {10: y,  11: dy/dtx0,  12: dy/dty0,  13: dy/dq0},
     *           {20: tx, 21: dtx/dtx0, 22: dtx/dty0, 23: dtx/dq0},
     *           {30: ty, 31: dty/dtx0, 32: dty/dty0, 33: dty/dq0}}
     */
    void RK4transport(int sector, double h, Swim swim, StateVecsDoca.CovMat covMat,
                      StateVecsDoca.StateVec fVec, StateVecsDoca.CovMat fCov) {
        // Set initial state and Jacobian.
        double qv = fVec.Q*v;
        double[][] s0 = {{fVec.x,0,0,0}, {fVec.y,0,0,0}, {fVec.tx,1,0,0}, {fVec.ty,0,1,0}};
        double[][] sNull = {{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};

        // Perform steps.
        double[][] s1 = RK4step(sector, fVec.z, 0,     swim, s0, sNull, qv);
        double[][] s2 = RK4step(sector, fVec.z, 0.5*h, swim, s0, s1,    qv);
        double[][] s3 = RK4step(sector, fVec.z, 0.5*h, swim, s0, s2,    qv);
        double[][] s4 = RK4step(sector, fVec.z, h,     swim, s0, s3,    qv);

        // === SET FINAL STATE =====================================================================
        double[][] sFinal = {{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                if (j == 0) sFinal[i][j] += s0[i][j];
                if (i == 2 && j == 1) sFinal[i][j] += 1;
                if (i == 3 && j == 2) sFinal[i][j] += 1;
                sFinal[i][j] += this.RK4(s1[i][j], s2[i][j], s3[i][j], s4[i][j], h);
            }
        }

        // covMat = FCF^T; u = FC.
        double[][] u = new double[5][5];
        for (int j1 = 0; j1 < 5; j1++) {
            u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * sFinal[0][1]+ covMat.covMat.get(3,j1)* sFinal[0][2] + covMat.covMat.get(4,j1) * sFinal[0][3];
            u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * sFinal[1][1]+ covMat.covMat.get(3,j1)* sFinal[1][2] + covMat.covMat.get(4,j1) * sFinal[1][3];
            u[2][j1] = covMat.covMat.get(2,j1) * sFinal[2][1] + covMat.covMat.get(3,j1)* sFinal[2][2]
                     + covMat.covMat.get(4,j1) * sFinal[2][3];
            u[3][j1] = covMat.covMat.get(2,j1) * sFinal[3][1] + covMat.covMat.get(3,j1)* sFinal[3][2]
                     + covMat.covMat.get(4,j1) * sFinal[3][3];
            u[4][j1] = covMat.covMat.get(4,j1);
        }

        double[][] C = new double[5][5];
        for (int i1 = 0; i1 < 5; i1++) {
            C[i1][0] = u[i1][0] + u[i1][2] * sFinal[0][1] + u[i1][3] * sFinal[0][2] + u[i1][4] * sFinal[0][3];
            C[i1][1] = u[i1][1] + u[i1][2] * sFinal[1][1] + u[i1][3] * sFinal[1][2] + u[i1][4] * sFinal[1][3];
            C[i1][2] = u[i1][2] * sFinal[2][1] + u[i1][3] * sFinal[2][2] + u[i1][4] * sFinal[2][3];
            C[i1][3] = u[i1][2] * sFinal[3][1] + u[i1][3] * sFinal[3][2] + u[i1][4] * sFinal[3][3];
            C[i1][4] = u[i1][4];
        }

        fVec.x   = sFinal[0][0];
        fVec.y   = sFinal[1][0];
        fVec.tx  = sFinal[2][0];
        fVec.ty  = sFinal[3][0];
        fVec.z  += h;
        fVec.B   = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        fVec.deltaPath += Math.sqrt((s0[0][0]-fVec.x)*(s0[0][0]-fVec.x)
                                    + (s0[1][0]-fVec.y)*(s0[1][0]-fVec.y) + h*h);
        fCov.covMat.set(C);
    }

    /** Perform one RK4 step, updating the covariance matrix. */
    private double[][] RK4step(int sector, double z0, double h, Swim swim,
                               double[][] sInit, double[][] sPrev, double qv) {
        double[][] sNext = {{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};
        swim.Bfield(sector, sInit[0][0] + h*sPrev[0][0], sInit[1][0] + h*sPrev[1][0], z0 + h, _b);

        // State.
        sNext[0][0] = sInit[2][0] + h*sPrev[2][0];
        sNext[1][0] = sInit[3][0] + h*sPrev[3][0];
        double C2 = C(sNext[0][0], sNext[1][0]);
        sNext[2][0]=qv*Ax(C2, sNext[0][0], sNext[1][0]);
        sNext[3][0]=qv*Ay(C2, sNext[0][0], sNext[1][0]);

        // Jacobian.
        sNext[0][1] = sInit[2][1] + h*sPrev[2][1];
        sNext[0][2] = sInit[2][2] + h*sPrev[2][2];
        sNext[0][3] = sInit[2][3] + h*sPrev[2][3];
        sNext[1][1] = sInit[3][1] + h*sPrev[3][1];
        sNext[1][2] = sInit[3][2] + h*sPrev[3][2];
        sNext[1][3] = sInit[3][3] + h*sPrev[3][3];

        sNext[2][1] = this.dtx_dtx0(qv, sNext[0][0], sNext[1][0], sNext[0][1], sNext[1][1]);
        sNext[2][2] = this.dtx_dty0(qv, sNext[0][0], sNext[1][0], sNext[0][2], sNext[1][2]);
        sNext[2][3] = this.dtx_dq0( qv, sNext[0][0], sNext[1][0], sNext[0][3], sNext[1][3]);
        sNext[3][1] = this.dty_dtx0(qv, sNext[0][0], sNext[1][0], sNext[0][1], sNext[1][1]);
        sNext[3][2] = this.dty_dty0(qv, sNext[0][0], sNext[1][0], sNext[0][2], sNext[1][2]);
        sNext[3][3] = this.dty_dq0( qv, sNext[0][0], sNext[1][0], sNext[0][3], sNext[1][3]);

        return sNext;
    }

    private double RK4(double k1, double k2, double k3, double k4, double h) {
        return (h/6) * (k1 + 2*k2 + 2*k3 + k4);
    }

    private double C(double tx, double ty) {
        return Math.sqrt(1 + tx*tx + ty*ty);
    }
    private double C(double Csq) {
        return Math.sqrt(Csq);
    }
    private double Csq(double tx, double ty) {
        return 1 + tx*tx + ty*ty;
    }

    private double Ax(double C, double tx, double ty) {
        return C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
    }
    private double Ay(double C, double tx, double ty) {
        return C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);
    }

    private double Ax(double tx, double ty) {
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        return C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
    }
    private double Ay(double tx, double ty) {
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        return C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);
    }

    private double dAx_dtx(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return tx * Ax / C2 + C * (ty * _b[0] - 2 * tx * _b[1]);
    }
    private double dAx_dty(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return ty * Ax / C2 + C * (tx * _b[0] + _b[2]);
    }
    private double dAy_dtx(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return tx * Ay / C2 + C * (-ty * _b[1] - _b[2]);
    }
    private double dAy_dty(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return ty * Ay / C2 + C * (-tx * _b[1] + 2 * ty * _b[0]);
    }

    private double dtx_dtx0(double qv, double tx1, double ty1, double deltx_deltx0_1, double delty_deltx0_1) {
        return qv*(dAx_dtx(tx1,ty1)*(deltx_deltx0_1)
                + dAx_dty(tx1,ty1)*(delty_deltx0_1));
    }

    private double dty_dtx0(double qv, double tx1, double ty1, double deltx_deltx0_1, double delty_deltx0_1) {
        return qv*(dAy_dtx(tx1,ty1)*(deltx_deltx0_1)
                + dAy_dty(tx1,ty1)*(delty_deltx0_1));
    }

    private double dtx_dty0(double qv, double tx1, double ty1, double deltx_delty0_1, double delty_delty0_1) {
        return qv*(dAx_dty(tx1,ty1)*(deltx_delty0_1)
                + dAx_dty(tx1,ty1)*(delty_delty0_1));
    }

    private double dty_dty0(double qv, double tx1, double ty1, double deltx_delty0_1, double delty_delty0_1) {
        return qv*(dAy_dty(tx1,ty1)*(deltx_delty0_1)
                + dAy_dty(tx1,ty1)*(delty_delty0_1));
    }

    private double dtx_dq0(double qv, double tx1, double ty1, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ax(tx1, ty1)
                + qv*(dAx_dtx(tx1,ty1)*(deltx_delq0_1)
                    + dAx_dty(tx1,ty1)*(delty_delq0_1));
    }

    private double dty_dq0(double qv, double tx1, double ty1, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ay(tx1, ty1)
                + qv*(dAy_dtx(tx1, ty1)*(deltx_delq0_1)
                    + dAy_dty(tx1, ty1)*(delty_delq0_1));
    }
}
