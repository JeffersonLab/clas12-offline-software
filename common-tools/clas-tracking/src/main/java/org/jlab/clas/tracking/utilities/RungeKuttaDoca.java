package org.jlab.clas.tracking.utilities;

import org.jlab.jnp.matrix.*;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;;

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
    public void SwimToZ(int sector, StateVec vec, Swim swim, double z0, float[] bf) {
        double stepSize = 1.0;
        swim.Bfield(sector, vec.x, vec.y, vec.z, bf);

        vec.B = Math.sqrt(bf[0]*bf[0] + bf[1]*bf[1] + bf[2]*bf[2]);
        double s = vec.B;
        final double travelSign = Math.signum(z0 - vec.z);
        double BatMeas = 0;

        while(travelSign * vec.z < travelSign * z0) {
            s = travelSign * stepSize;
            if (travelSign*(vec.z+s) > travelSign*z0) s = travelSign*Math.abs(z0-vec.z);

            this.RK4transport(sector, s, swim, vec);

            if (Math.abs(vec.B - BatMeas) < 0.0001) stepSize *= 2;
            BatMeas = vec.B;
        }
    }

    /**
     * Transport using Runge Kutta 4 without updating the covariance matrix. Lab system = 1, TSC = 0.
     * Internal state array (sN) is defined as {x, y, tx, ty}, since q doesn't change.
     */
    private void RK4transport(int sector, double h, Swim swim, StateVec vec) {
        // Set initial state.
        double qv = vec.Q*v;
        double[] s0 = {vec.x, vec.y, vec.tx, vec.ty};
        double[] sNull = {0, 0, 0, 0};
        
        // Transport.
        double[] s1 = RK4step(sector, vec.z, 0,     swim, s0, sNull, qv);
        double[] s2 = RK4step(sector, vec.z, 0.5*h, swim, s0, s1,    qv);
        double[] s3 = RK4step(sector, vec.z, 0.5*h, swim, s0, s2,    qv);
        double[] s4 = RK4step(sector, vec.z, h,     swim, s0, s3,    qv);

        // Set final state.
        vec.z  += h;
        vec.x  += this.RK4(s1[0], s2[0], s3[0], s4[0], h);
        vec.y  += this.RK4(s1[1], s2[1], s3[1], s4[1], h);
        vec.tx += this.RK4(s1[2], s2[2], s3[2], s4[2], h);
        vec.ty += this.RK4(s1[3], s2[3], s3[3], s4[3], h);

        vec.B = Math.sqrt(_b[0]*_b[0] + _b[1]*_b[1] + _b[2]*_b[2]);
        vec.deltaPath += Math.sqrt((s0[0]-vec.x)*(s0[0]-vec.x)+(s0[1]-vec.y)*(s0[1]-vec.y)+h*h);
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
    public void RK4transport(int sector, double h, Swim swim, Matrix cMat, StateVec vec) {
        // Set initial state and Jacobian.
        double qv = vec.Q*v;
        RK4Vec s0 = new RK4Vec(vec);
        RK4Vec sNull = new RK4Vec();

        // Perform steps.
        RK4Vec s1 = RK4step(sector, vec.z, 0,     swim, s0, sNull, qv);
        RK4Vec s2 = RK4step(sector, vec.z, 0.5*h, swim, s0, s1,    qv);
        RK4Vec s3 = RK4step(sector, vec.z, 0.5*h, swim, s0, s2,    qv);
        RK4Vec s4 = RK4step(sector, vec.z, h,     swim, s0, s3,    qv);

        // Compute and set final state and covariance matrix.
        RK4Vec sF = computeFinalState(h, s0, s1, s2, s3, s4);

        vec.x   = sF.x;
        vec.y   = sF.y;
        vec.tx  = sF.tx;
        vec.ty  = sF.ty;
        vec.z  += h;
        vec.B   = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        vec.deltaPath += Math.sqrt((s0.x-vec.x)*(s0.x-vec.x) + (s0.y-vec.y)*(s0.y-vec.y) + h*h);
        vec.CM.set(computeCovMat(cMat, sF));
    }

    /** Perform one RK4 step, updating the covariance matrix. */
    private RK4Vec RK4step(int sector, double z0, double h, Swim swim,
                               RK4Vec sInit, RK4Vec sPrev, double qv) {
        RK4Vec sNext = new RK4Vec();
        swim.Bfield(sector, sInit.x + h*sPrev.x, sInit.y + h*sPrev.y, z0 + h, _b);

        // State.
        sNext.x = sInit.tx + h*sPrev.tx;
        sNext.y = sInit.ty + h*sPrev.ty;

        double Csq  = Csq(sNext.x, sNext.y);
        double C    = C(Csq);
        double Ax   = Ax(C, sNext.x, sNext.y);
        double Ay   = Ay(C, sNext.x, sNext.y);

        sNext.tx = qv * Ax;
        sNext.ty = qv * Ay;

        // Jacobian.
        sNext.dxdtx0 = sInit.dtxdtx0 + h*sPrev.dtxdtx0;
        sNext.dxdty0 = sInit.dtxdty0 + h*sPrev.dtxdty0;
        sNext.dxdq0  = sInit.dtxdq0  + h*sPrev.dtxdq0;
        sNext.dydtx0 = sInit.dtydtx0 + h*sPrev.dtydtx0;
        sNext.dydty0 = sInit.dtydty0 + h*sPrev.dtydty0;
        sNext.dydq0  = sInit.dtydq0  + h*sPrev.dtydq0;

        double dAx_dtx = dAx_dtx(C, Csq, Ax, sNext.x, sNext.y);
        double dAx_dty = dAx_dty(C, Csq, Ax, sNext.x, sNext.y);
        double dAy_dtx = dAy_dtx(C, Csq, Ay, sNext.x, sNext.y);
        double dAy_dty = dAy_dty(C, Csq, Ay, sNext.x, sNext.y);

        sNext.dtxdtx0 = this.dtx_dtx0(qv,     dAx_dtx, dAx_dty, sNext.dxdtx0, sNext.dydtx0);
        sNext.dtxdty0 = this.dtx_dty0(qv,              dAx_dty, sNext.dxdty0, sNext.dydty0);
        sNext.dtxdq0  = this.dtx_dq0( qv, Ax, dAx_dtx, dAx_dty, sNext.dxdq0,  sNext.dydq0);
        sNext.dtydtx0 = this.dty_dtx0(qv,     dAy_dtx, dAy_dty, sNext.dxdtx0, sNext.dydtx0);
        sNext.dtydty0 = this.dty_dty0(qv,              dAy_dty, sNext.dxdty0, sNext.dydty0);
        sNext.dtydq0  = this.dty_dq0( qv, Ay, dAy_dtx, dAy_dty, sNext.dxdq0,  sNext.dydq0);

        return sNext;
    }

    /** Compute the final state for each entry in the internal state matrix. */
    private RK4Vec computeFinalState(double h, RK4Vec s0, RK4Vec s1, RK4Vec s2, RK4Vec s3,
            RK4Vec s4) {
        RK4Vec sF = new RK4Vec();

        sF.x      = s0.x  + this.RK4(s1.x,      s2.x,      s3.x,      s4.x,      h);
        sF.dxdtx0 =         this.RK4(s1.dxdtx0, s2.dxdtx0, s3.dxdtx0, s4.dxdtx0, h);
        sF.dxdty0 =         this.RK4(s1.dxdty0, s2.dxdty0, s3.dxdty0, s4.dxdty0, h);
        sF.dxdq0  =         this.RK4(s1.dxdq0,  s2.dxdq0,  s3.dxdq0,  s4.dxdq0,  h);

        sF.y      = s0.y  + this.RK4(s1.y,      s2.y,      s3.y,      s4.y,      h);
        sF.dydtx0 =         this.RK4(s1.dydtx0, s2.dydtx0, s3.dydtx0, s4.dydtx0, h);
        sF.dydty0 =         this.RK4(s1.dydty0, s2.dydty0, s3.dydty0, s4.dydty0, h);
        sF.dydq0  =         this.RK4(s1.dydq0,  s2.dydq0,  s3.dydq0,  s4.dydq0,  h);

        sF.tx      = s0.tx + this.RK4(s1.tx,      s2.tx,      s3.tx,      s4.tx,      h);
        sF.dtxdtx0 = 1     + this.RK4(s1.dtxdtx0, s2.dtxdtx0, s3.dtxdtx0, s4.dtxdtx0, h);
        sF.dtxdty0 =         this.RK4(s1.dtxdty0, s2.dtxdty0, s3.dtxdty0, s4.dtxdty0, h);
        sF.dtxdq0  =         this.RK4(s1.dtxdq0,  s2.dtxdq0,  s3.dtxdq0,  s4.dtxdq0,  h);

        sF.ty      = s0.ty + this.RK4(s1.ty,      s2.ty,      s3.ty,      s4.ty,      h);
        sF.dtydtx0 =         this.RK4(s1.dtydtx0, s2.dtydtx0, s3.dtydtx0, s4.dtydtx0, h);
        sF.dtydty0 = 1     + this.RK4(s1.dtydty0, s2.dtydty0, s3.dtydty0, s4.dtydty0, h);
        sF.dtydq0  =         this.RK4(s1.dtydq0,  s2.dtydq0,  s3.dtydq0,  s4.dtydq0,  h);

        return sF;
    }

    /** Compute the final covariance matrix. covMat = FCF^T. */
    private double[][] computeCovMat(Matrix C, RK4Vec sF) {
        double[][] cNext = new double[5][5];
        cNext[0][0] = C.get(0,0)+C.get(2,0)*sF.dxdtx0+C.get(3,0)*sF.dxdty0+C.get(4,0)*sF.dxdq0
                    + sF.dxdq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                    + sF.dxdtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                    + sF.dxdty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);

        cNext[0][1] = C.get(0,1)+C.get(2,1)*sF.dxdtx0+C.get(3,1)*sF.dxdty0+C.get(4,1)*sF.dxdq0
                    + sF.dydq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                    + sF.dydtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                    + sF.dydty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);

        cNext[0][2] = sF.dtxdq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                    + sF.dtxdtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                    + sF.dtxdty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);

        cNext[0][3] = sF.dtydq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                    + sF.dtydtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                    + sF.dtydty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);

        cNext[0][4] = C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0;

        cNext[1][0] = C.get(1,0)+C.get(2,0)*sF.dydtx0+C.get(3,0)*sF.dydty0+C.get(4,0)*sF.dydq0
                    + sF.dxdq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                    + sF.dxdtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                    + sF.dxdty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);

        cNext[1][1] = C.get(1,1)+C.get(2,1)*sF.dydtx0+C.get(3,1)*sF.dydty0+C.get(4,1)*sF.dydq0
                    + sF.dydq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                    + sF.dydtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                    + sF.dydty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);

        cNext[1][2] = sF.dtxdq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                    + sF.dtxdtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                    + sF.dtxdty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);

        cNext[1][3] = sF.dtydq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                    + sF.dtydtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                    + sF.dtydty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);

        cNext[1][4] = C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0;

        cNext[2][0] = C.get(2,0)*sF.dtxdtx0+C.get(3,0)*sF.dtxdty0+C.get(4,0)*sF.dtxdq0
                    + sF.dxdq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                    + sF.dxdtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                    + sF.dxdty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);

        cNext[2][1] = C.get(2,1)*sF.dtxdtx0+C.get(3,1)*sF.dtxdty0+C.get(4,1)*sF.dtxdq0
                    + sF.dydq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                    + sF.dydtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                    + sF.dydty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);

        cNext[2][2] = sF.dtxdq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                    + sF.dtxdtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                    + sF.dtxdty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);

        cNext[2][3] = sF.dtydq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                    + sF.dtydtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                    + sF.dtydty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);

        cNext[2][4] = C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0;

        cNext[3][0] = C.get(2,0)*sF.dtydtx0+C.get(3,0)*sF.dtydty0+C.get(4,0)*sF.dtydq0
                    + sF.dxdq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                    + sF.dxdtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                    + sF.dxdty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);

        cNext[3][1] = C.get(2,1)*sF.dtydtx0+C.get(3,1)*sF.dtydty0+C.get(4,1)*sF.dtydq0
                    + sF.dydq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                    + sF.dydtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                    + sF.dydty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);

        cNext[3][2] = sF.dtxdq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                    + sF.dtxdtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                    + sF.dtxdty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);

        cNext[3][3] = sF.dtydq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                    + sF.dtydtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                    + sF.dtydty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);

        cNext[3][4] = C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0;

        cNext[4][0] = C.get(4,0)+C.get(4,2)*sF.dxdtx0+C.get(4,3)*sF.dxdty0+C.get(4,4)*sF.dxdq0;

        cNext[4][1] = C.get(4,1)+C.get(4,2)*sF.dydtx0+C.get(4,3)*sF.dydty0+C.get(4,4)*sF.dydq0;

        cNext[4][2] = C.get(4,2)*sF.dtxdtx0+C.get(4,3)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0;

        cNext[4][3] = C.get(4,2)*sF.dtydtx0+C.get(4,3)*sF.dtydty0+C.get(4,4)*sF.dtydq0;

        cNext[4][4] = C.get(4,4);

        return cNext;
    }

    /** Get the final RK4 estimate. */
    private double RK4(double k1, double k2, double k3, double k4, double h) {
        return (h/6) * (k1 + 2*k2 + 2*k3 + k4);
    }

    // Auxiliary calculations.
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

    private double dAx_dtx(double C, double C2, double Ax, double tx, double ty) {
        return tx * Ax/C2 + C * (ty*_b[0] - 2*tx*_b[1]);
    }
    private double dAx_dty(double C, double C2, double Ax, double tx, double ty) {
        return ty * Ax/C2 + C * (tx*_b[0] + _b[2]);
    }
    private double dAy_dtx(double C, double C2, double Ay, double tx, double ty) {
        return tx * Ay/C2 + C * (-ty*_b[1] - _b[2]);
    }
    private double dAy_dty(double C, double C2, double Ay, double tx, double ty) {
        return ty * Ay/C2 + C * (-tx*_b[1] + 2*ty*_b[0]);
    }

    // Total derivatives.
    private double dtx_dtx0(double qv, double dAx_dtx, double dAx_dty,
                            double dtx_dtx0, double dty_dtx0) {
        return qv * (dAx_dtx*dtx_dtx0 + dAx_dty*dty_dtx0);
    }
    private double dtx_dty0(double qv, double dAx_dty, double dtx_dty0, double dty_dty0) {
        return qv * (dAx_dty*dtx_dty0 + dAx_dty*dty_dty0);
    }
    private double dtx_dq0(double qv, double Ax, double dAx_dtx, double dAx_dty,
                           double dtx_dq0, double dty_dq0) {
        return v*Ax + qv * (dAx_dtx*dtx_dq0 + dAx_dty*dty_dq0);
    }
    private double dty_dtx0(double qv, double dAy_dtx, double dAy_dty,
                            double dtx_dtx0, double dty_dtx0) {
        return qv * (dAy_dtx*dtx_dtx0 + dAy_dty*dty_dtx0);
    }
    private double dty_dty0(double qv, double dAy_dty, double dtx_dty0, double dty_dty0) {
        return qv * (dAy_dty*dtx_dty0 + dAy_dty*dty_dty0);
    }
    private double dty_dq0(double qv, double Ay, double dAy_dtx, double dAy_dty,
                           double dtx_dq0, double dty_dq0) {
        return v*Ay + qv * (dAy_dtx*dtx_dq0 + dAy_dty*dty_dq0);
    }

    /** State vector and its derivatives used internally. */
    private class RK4Vec {
        public double x      = 0;
        public double dxdtx0 = 0;
        public double dxdty0 = 0;
        public double dxdq0  = 0;

        public double y      = 0;
        public double dydtx0 = 0;
        public double dydty0 = 0;
        public double dydq0  = 0;

        public double tx      = 0;
        public double dtxdtx0 = 0;
        public double dtxdty0 = 0;
        public double dtxdq0  = 0;

        public double ty      = 0;
        public double dtydtx0 = 0;
        public double dtydty0 = 0;
        public double dtydq0  = 0;

        RK4Vec() {}
        RK4Vec(StateVec vec) {
            this.x  = vec.x;
            this.y  = vec.y;
            this.tx = vec.tx;
            this.ty = vec.ty;

            this.dtxdtx0 = 1;
            this.dtydty0 = 1;
        }
    }
}
