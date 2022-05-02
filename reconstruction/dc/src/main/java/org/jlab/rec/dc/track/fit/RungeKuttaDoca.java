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
    private final ArrayList<Double> k1;
    private final ArrayList<Double> k2;
    private final ArrayList<Double> k3;
    private final ArrayList<Double> k4;
    private final ArrayList<Double> jk1;
    private final ArrayList<Double> jk2;
    private final ArrayList<Double> jk3;
    private final ArrayList<Double> jk4;

    public RungeKuttaDoca() {
        this.k1 = new ArrayList<>(4);
        this.k2 = new ArrayList<>(4);
        this.k3 = new ArrayList<>(4);
        this.k4 = new ArrayList<>(4);
        this.jk1 = new ArrayList<>(12);
        this.jk2 = new ArrayList<>(12);
        this.jk3 = new ArrayList<>(12);
        this.jk4 = new ArrayList<>(12);
    }

    /** Swim to Z position without updating the covariance matrix. */
    public void SwimToZ(int sector, StateVecsDoca.StateVec fVec, Swim dcSwim, double z0, float[] bf) {
        double stepSize = 1.0;
        dcSwim.Bfield(sector, fVec.x, fVec.y, fVec.z, bf);

        fVec.B   = Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2]);
        double s = fVec.B;
        double z = fVec.z;
        final double Zi = fVec.z;
        double BatMeas = 0;

        while (Math.signum(z0 - Zi) * z < Math.signum(z0 - Zi) * z0) {
            z = fVec.z;

            s = Math.signum(z0 - Zi) * stepSize;
            if (Math.signum(z0 - Zi) *(z+s)>Math.signum(z0 - Zi) *z0)
                s = Math.signum(z0 - Zi) * Math.abs(z0-z);

            this.RK4transport(sector, s, dcSwim, fVec);

            if (Math.abs(fVec.B - BatMeas) < 0.0001) stepSize*=2;

            BatMeas = fVec.B;
        }
    }

    /**
     * Transport using Runge Kutta 4 without updating the covariance matrix. Lab system = 1, TSC = 0.
     * Internal state array (sN) is defined as {x, y, tx, ty}, since q doesn't change.
     */
    private void RK4transport(int sector, double h, Swim swim, StateVecsDoca.StateVec fVec) {
        // Set initial state.
        double z0 = fVec.z;
        double qv = fVec.Q*v;
        double[] s0 = {fVec.x, fVec.y, fVec.tx, fVec.ty};
        double[] sNull = {0, 0, 0, 0};

        // Transport.
        double[] s1 = RK4step(sector, z0, 0,     swim, s0, sNull, qv);
        double[] s2 = RK4step(sector, z0, 0.5*h, swim, s0, s1,    qv);
        double[] s3 = RK4step(sector, z0, 0.5*h, swim, s0, s2,    qv);
        double[] s4 = RK4step(sector, z0, h,     swim, s0, s3,    qv);

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

    /** Transport using Runge Kutta 4, updating the covariance matrix. */
    void RK4transport(int sector, double q, double x0, double y0, double z0, double tx0, double ty0,
                      double h, Swim swimmer, StateVecsDoca.CovMat covMat,
                      StateVecsDoca.StateVec fVec, StateVecsDoca.CovMat fCov, double dPath) {
        // Jacobian:
        double[][] u = new double[5][5];
        double[][] C = new double[5][5];
        double deltx_deltx0_0 =1;
        double delty_deltx0_0 =0;
        double deltx_delty0_0 =0;
        double delty_delty0_0 =1;
        double deltx_delq0_0 =0;
        double delty_delq0_0 =0;

        //State
        swimmer.Bfield(sector, x0, y0, z0, _b);
        double x1 = tx0;
        double y1 = ty0;
        double tx1=q*v*Ax(tx0, ty0);
        double ty1=q*v*Ay(tx0, ty0);

        // Jacobian:
        double delx_deltx0_1 = deltx_deltx0_0;
        double dely_deltx0_1 = delty_deltx0_0;
        double delx_delty0_1 = deltx_delty0_0;
        double dely_delty0_1 = delty_delty0_0;

        double deltx_deltx0_1 = q*v*(delAx_deltx(tx0,ty0) * deltx_deltx0_0
                + delAx_delty(tx0,ty0)*delty_deltx0_0);
        double delty_deltx0_1 = q*v*(delAy_deltx(tx0,ty0)*deltx_deltx0_0
                + delAy_delty(tx0,ty0)*delty_deltx0_0);
        double deltx_delty0_1 = q*v*(delAx_delty(tx0,ty0)*deltx_delty0_0
                + delAx_delty(tx0,ty0)*delty_delty0_0);
        double delty_delty0_1 = q*v*(delAy_delty(tx0,ty0)*deltx_delty0_0
                + delAy_delty(tx0,ty0)*delty_delty0_0);

        double delx_delq0_1 = deltx_delq0_0;
        double dely_delq0_1 = delty_delq0_0;

        double deltx_delq0_1 = v*Ax(tx0, ty0)
                + q*v*(delAx_deltx(tx0,ty0)*deltx_delq0_0
                    + delAx_delty(tx0,ty0)*delty_delq0_0);
        double delty_delq0_1 = v*Ay(tx0, ty0)
                + q*v*(delAy_deltx(tx0,ty0)*deltx_delq0_0
                    + delAy_delty(tx0,ty0)*delty_delq0_0);

        swimmer.Bfield(sector, x0+0.5*h*x1, y0+0.5*h*y1, z0+0.5*h, _b);
        double x2 = tx0+0.5*h*tx1;
        double y2 = ty0+0.5*h*ty1;
        double tx2=q*v*Ax((tx0+0.5*h*tx1), (ty0+0.5*h*ty1));
        double ty2=q*v*Ay((tx0+0.5*h*tx1), (ty0+0.5*h*ty1));

        // Jacobian:
        double delx_deltx0_2 = deltx_deltx0_0+0.5*h*deltx_deltx0_1;
        double dely_deltx0_2 = delty_deltx0_0+0.5*h*delty_deltx0_1;
        double delx_delty0_2 = deltx_delty0_0+0.5*h*deltx_delty0_1;
        double dely_delty0_2 = delty_delty0_0+0.5*h*delty_delty0_1;

        double deltx_deltx0_2 = this.deltx_deltx0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_deltx0_0+0.5*h*deltx_deltx0_1,delty_deltx0_0+0.5*h*delty_deltx0_1);
        double delty_deltx0_2 = this.delty_deltx0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_deltx0_0+0.5*h*deltx_deltx0_1,delty_deltx0_0+0.5*h*delty_deltx0_1);
        double deltx_delty0_2 = this.deltx_delty0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_delty0_0+0.5*h*deltx_delty0_1,delty_delty0_0+0.5*h*delty_delty0_1);
        double delty_delty0_2 = this.delty_delty0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_delty0_0+0.5*h*deltx_delty0_1,delty_delty0_0+0.5*h*delty_delty0_1);

        double delx_delq0_2 = deltx_delq0_0+0.5*h*deltx_delq0_1;
        double dely_delq0_2 = delty_delq0_0+0.5*h*delty_delq0_1;

        double deltx_delq0_2 = this.deltx_delq0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_delq0_0+0.5*h*deltx_delq0_1,delty_delq0_0+0.5*h*delty_delq0_1);
        double delty_delq0_2 = this.delty_delq0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,
                deltx_delq0_0+0.5*h*deltx_delq0_1,delty_delq0_0+0.5*h*delty_delq0_1);

        swimmer.Bfield(sector, x0+0.5*h*x2, y0+0.5*h*y2, z0+0.5*h, _b);
        double x3 = tx0+0.5*h*tx2;
        double y3 = ty0+0.5*h*ty2;
        double tx3=q*v*Ax((tx0+0.5*h*tx2), (ty0+0.5*h*ty2));
        double ty3=q*v*Ay((tx0+0.5*h*tx2), (ty0+0.5*h*ty2));

        // Jacobian:
        double delx_deltx0_3 = deltx_deltx0_0+0.5*h*deltx_deltx0_2;
        double dely_deltx0_3 = delty_deltx0_0+0.5*h*delty_deltx0_2;
        double delx_delty0_3 = deltx_delty0_0+0.5*h*deltx_delty0_2;
        double dely_delty0_3 = delty_delty0_0+0.5*h*delty_delty0_2;

        double deltx_deltx0_3 = this.deltx_deltx0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_deltx0_0+0.5*h*deltx_deltx0_2,delty_deltx0_0+0.5*h*delty_deltx0_2);
        double delty_deltx0_3 = this.delty_deltx0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_deltx0_0+0.5*h*deltx_deltx0_2,delty_deltx0_0+0.5*h*delty_deltx0_2);
        double deltx_delty0_3 = this.deltx_delty0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_delty0_0+0.5*h*deltx_delty0_2,delty_delty0_0+0.5*h*delty_delty0_2);
        double delty_delty0_3 = this.delty_delty0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_delty0_0+0.5*h*deltx_delty0_2,delty_delty0_0+0.5*h*delty_delty0_2);

        double delx_delq0_3 = deltx_delq0_0+0.5*h*deltx_delq0_2;
        double dely_delq0_3 = delty_delq0_0+0.5*h*delty_delq0_2;

        double deltx_delq0_3 = this.deltx_delq0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_delq0_0+0.5*h*deltx_delq0_2,delty_delq0_0+0.5*h*delty_delq0_2);
        double delty_delq0_3 = this.delty_delq0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,
                deltx_delq0_0+0.5*h*deltx_delq0_2,delty_delq0_0+0.5*h*delty_delq0_2);

        swimmer.Bfield(sector, x0+h*x3, y0+h*y3, z0+h, _b);
        double x4 = tx0+h*tx3;
        double y4 = ty0+h*ty3;
        double tx4=q*v*Ax((tx0+h*tx3), (ty0+h*ty3));
        double ty4=q*v*Ay((tx0+h*tx3), (ty0+h*ty3));

         // Jacobian:
        double delx_deltx0_4 = deltx_deltx0_0+h*deltx_deltx0_3;
        double dely_deltx0_4 = delty_deltx0_0+h*delty_deltx0_3;
        double delx_delty0_4 = deltx_delty0_0+h*deltx_delty0_3;
        double dely_delty0_4 = delty_delty0_0+h*delty_delty0_3;

        double deltx_deltx0_4 = this.deltx_deltx0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_deltx0_0+h*deltx_deltx0_3,delty_deltx0_0+h*delty_deltx0_3);
        double delty_deltx0_4 = this.delty_deltx0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_deltx0_0+h*deltx_deltx0_3,delty_deltx0_0+h*delty_deltx0_3);
        double deltx_delty0_4 = this.deltx_delty0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_delty0_0+h*deltx_delty0_3,delty_delty0_0+h*delty_delty0_3);
        double delty_delty0_4 = this.delty_delty0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_delty0_0+h*deltx_delty0_3,delty_delty0_0+h*delty_delty0_3);

        double delx_delq0_4 = deltx_delq0_0+h*deltx_delq0_3;
        double dely_delq0_4 = delty_delq0_0+h*delty_delq0_3;

        double deltx_delq0_4 = this.deltx_delq0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_delq0_0+h*deltx_delq0_3,delty_delq0_0+h*delty_delq0_3);
        double delty_delq0_4 = this.delty_delq0_next(q,v,tx0+h*tx3,ty0+h*ty3,
                deltx_delq0_0+h*deltx_delq0_3,delty_delq0_0+h*delty_delq0_3);

        double x = x0 + this.RK4(x1, x2, x3, x4, h);
        double y = y0 + this.RK4(y1, y2, y3, y4, h);
        double tx = tx0 + this.RK4(tx1, tx2, tx3, tx4, h);
        double ty = ty0 + this.RK4(ty1, ty2, ty3, ty4, h);
        double z = z0+h;

        // Jacobian:
        double delx_deltx0  = this.RK4(delx_deltx0_1, delx_deltx0_2, delx_deltx0_3, delx_deltx0_4, h);
        double deltx_deltx0 = 1 + this.RK4(deltx_deltx0_1, deltx_deltx0_2, deltx_deltx0_3, deltx_deltx0_4, h);
        double dely_deltx0  = this.RK4(dely_deltx0_1, dely_deltx0_2, dely_deltx0_3, dely_deltx0_4, h);
        double delty_deltx0 = this.RK4(delty_deltx0_1, delty_deltx0_2, delty_deltx0_3, delty_deltx0_4, h);

        double delx_delty0  = this.RK4(delx_delty0_1, delx_delty0_2, delx_delty0_3, delx_delty0_4, h);
        double deltx_delty0 = this.RK4(deltx_delty0_1, deltx_delty0_2, deltx_delty0_3, deltx_delty0_4, h);
        double dely_delty0  = this.RK4(dely_delty0_1, dely_delty0_2, dely_delty0_3, dely_delty0_4, h);
        double delty_delty0 = 1 + this.RK4(delty_delty0_1, delty_delty0_2, delty_delty0_3, delty_delty0_4, h);

        double delx_delq0  = this.RK4(delx_delq0_1, delx_delq0_2, delx_delq0_3, delx_delq0_4, h);
        double deltx_delq0 = this.RK4(deltx_delq0_1, deltx_delq0_2, deltx_delq0_3, deltx_delq0_4, h);
        double dely_delq0  = this.RK4(dely_delq0_1, dely_delq0_2, dely_delq0_3, dely_delq0_4, h);
        double delty_delq0 = this.RK4(delty_delq0_1, delty_delq0_2, delty_delq0_3, delty_delq0_4, h);

        //covMat = FCF^T; u = FC;
        for (int j1 = 0; j1 < 5; j1++) {
            u[0][j1] = covMat.covMat.get(0,j1) + covMat.covMat.get(2,j1) * delx_deltx0+ covMat.covMat.get(3,j1)* delx_delty0 + covMat.covMat.get(4,j1) * delx_delq0;
            u[1][j1] = covMat.covMat.get(1,j1) + covMat.covMat.get(2,j1) * dely_deltx0+ covMat.covMat.get(3,j1)* dely_delty0 + covMat.covMat.get(4,j1) * dely_delq0;
            u[2][j1] = covMat.covMat.get(2,j1) * deltx_deltx0+ covMat.covMat.get(3,j1)* deltx_delty0 + covMat.covMat.get(4,j1) * deltx_delq0;
            u[3][j1] = covMat.covMat.get(2,j1) * delty_deltx0+ covMat.covMat.get(3,j1)* delty_delty0 + covMat.covMat.get(4,j1) * delty_delq0;
            u[4][j1] = covMat.covMat.get(4,j1);
        }

        for (int i1 = 0; i1 < 5; i1++) {
            C[i1][0] = u[i1][0] + u[i1][2] * delx_deltx0 + u[i1][3] * delx_delty0 + u[i1][4] * delx_delq0;
            C[i1][1] = u[i1][1] + u[i1][2] * dely_deltx0 + u[i1][3] * dely_delty0 + u[i1][4] * dely_delq0;
            C[i1][2] = u[i1][2] * deltx_deltx0 + u[i1][3] * deltx_delty0 + u[i1][4] * deltx_delq0;
            C[i1][3] = u[i1][2] * delty_deltx0 + u[i1][3] * delty_delty0 + u[i1][4] * delty_delq0;
            C[i1][4] = u[i1][4];
        }

        fVec.x = x;
        fVec.y  = y ;
        fVec.z = z0+h;
        fVec.tx = tx;
        fVec.ty = ty;
        fVec.Q = q;
        fVec.B = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        fVec.deltaPath = Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y)+h*h)+dPath;
        fCov.covMat.set(C);
    }

    private double RK4(double k1, double k2, double k3, double k4, double h) {
        return h/6*(k1 + 2*k2 +2*k3 + k4);
    }

    private double C(double tx, double ty) {
        return Math.sqrt(1 + tx*tx + ty*ty);
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

    private double delAx_deltx(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return tx * Ax / C2 + C * (ty * _b[0] - 2 * tx * _b[1]);
    }
    private double delAx_delty(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return ty * Ax / C2 + C * (tx * _b[0] + _b[2]);
    }
    private double delAy_deltx(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return tx * Ay / C2 + C * (-ty * _b[1] - _b[2]);
    }
    private double delAy_delty(double tx, double ty) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * _b[0] + _b[2]) - (1 + tx * tx) * _b[1]);
        double Ay = C * (-tx * (ty * _b[1] + _b[2]) + (1 + ty * ty) * _b[0]);

        return ty * Ay / C2 + C * (-tx * _b[1] + 2 * ty * _b[0]);
    }

    private double deltx_deltx0_next(double q, double v, double tx1, double ty1, double deltx_deltx0_1, double delty_deltx0_1) {
        return q*v*(delAx_deltx(tx1,ty1)*(deltx_deltx0_1)
                + delAx_delty(tx1,ty1)*(delty_deltx0_1));
    }

    private double delty_deltx0_next(double q, double v, double tx1, double ty1, double deltx_deltx0_1, double delty_deltx0_1) {
        return q*v*(delAy_deltx(tx1,ty1)*(deltx_deltx0_1)
                + delAy_delty(tx1,ty1)*(delty_deltx0_1));
    }

    private double deltx_delty0_next(double q, double v, double tx1, double ty1, double deltx_delty0_1, double delty_delty0_1) {
        return q*v*(delAx_delty(tx1,ty1)*(deltx_delty0_1)
                + delAx_delty(tx1,ty1)*(delty_delty0_1));
    }

    private double delty_delty0_next(double q, double v, double tx1, double ty1, double deltx_delty0_1, double delty_delty0_1) {
        return q*v*(delAy_delty(tx1,ty1)*(deltx_delty0_1)
                + delAy_delty(tx1,ty1)*(delty_delty0_1));
    }

    private double deltx_delq0_next(double q, double v, double tx1, double ty1, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ax(tx1, ty1)
                + q*v*(delAx_deltx(tx1,ty1)*(deltx_delq0_1)
                    + delAx_delty(tx1,ty1)*(delty_delq0_1));
    }

    private double delty_delq0_next(double q, double v, double tx1, double ty1, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ay(tx1, ty1)
                + q*v*(delAy_deltx(tx1, ty1)*(deltx_delq0_1)
                    + delAy_delty(tx1, ty1)*(delty_delq0_1));
    }
}
