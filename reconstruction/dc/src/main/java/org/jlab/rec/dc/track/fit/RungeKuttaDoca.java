package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import org.jlab.jnp.matrix.*;
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
    public void SwimToZ(int sector, StateVecsDoca.StateVec vec, Swim swim, double z0, float[] bf) {
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
    private void RK4transport(int sector, double h, Swim swim, StateVecsDoca.StateVec vec) {
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
    void RK4transport(int sector, double h, Swim swim, Matrix C,
                      StateVecsDoca.StateVec vec, StateVecsDoca.CovMat fCov) {
        // Set initial state and Jacobian.
        double qv = vec.Q*v;
        double hh = 0.5*h;
        RK4Vec s0 = new RK4Vec(vec);

        // === FIRST STEP ==========================================================================
        RK4Vec s1 = new RK4Vec();
        swim.Bfield(sector, s0.x, s0.y, vec.z, _b);

        // State.
        s1.x = s0.tx;
        s1.y = s0.ty;

        double Csq1  = Csq(s1.x, s1.y);
        double C1    = C(Csq1);
        double Ax1   = Ax(C1, s1.x, s1.y);
        double Ay1   = Ay(C1, s1.x, s1.y);

        s1.tx = qv * Ax1;
        s1.ty = qv * Ay1;

        // Jacobian.
        s1.dxdtx0 = s0.dtxdtx0;
        s1.dxdty0 = s0.dtxdty0;
        s1.dxdq0  = s0.dtxdq0;
        s1.dydtx0 = s0.dtydtx0;
        s1.dydty0 = s0.dtydty0;
        s1.dydq0  = s0.dtydq0;

        double dAx1_dtx = dAx_dtx(C1, Csq1, Ax1, s1.x, s1.y);
        double dAx1_dty = dAx_dty(C1, Csq1, Ax1, s1.x, s1.y);
        double dAy1_dtx = dAy_dtx(C1, Csq1, Ay1, s1.x, s1.y);
        double dAy1_dty = dAy_dty(C1, Csq1, Ay1, s1.x, s1.y);

        s1.dtxdtx0 = this.dtx_dtx0(qv,      dAx1_dtx, dAx1_dty, s1.dxdtx0, s1.dydtx0);
        s1.dtxdty0 = this.dtx_dty0(qv,                dAx1_dty, s1.dxdty0, s1.dydty0);
        s1.dtxdq0  = this.dtx_dq0( qv, Ax1, dAx1_dtx, dAx1_dty, s1.dxdq0,  s1.dydq0);
        s1.dtydtx0 = this.dty_dtx0(qv,      dAy1_dtx, dAy1_dty, s1.dxdtx0, s1.dydtx0);
        s1.dtydty0 = this.dty_dty0(qv,                dAy1_dty, s1.dxdty0, s1.dydty0);
        s1.dtydq0  = this.dty_dq0( qv, Ay1, dAy1_dtx, dAy1_dty, s1.dxdq0,  s1.dydq0);

        // === SECOND STEP =========================================================================
        RK4Vec s2 = new RK4Vec();
        swim.Bfield(sector, s0.x + hh*s1.x, s0.y + hh*s1.y, vec.z + hh, _b);

        // State.
        s2.x = s0.tx + hh*s1.tx;
        s2.y = s0.ty + hh*s1.ty;

        double Csq2  = Csq(s2.x, s2.y);
        double C2    = C(Csq2);
        double Ax2   = Ax(C2, s2.x, s2.y);
        double Ay2   = Ay(C2, s2.x, s2.y);

        s2.tx = qv * Ax2;
        s2.ty = qv * Ay2;

        // Jacobian.
        s2.dxdtx0 = s0.dtxdtx0 + hh*s1.dtxdtx0;
        s2.dxdty0 = s0.dtxdty0 + hh*s1.dtxdty0;
        s2.dxdq0  = s0.dtxdq0  + hh*s1.dtxdq0;
        s2.dydtx0 = s0.dtydtx0 + hh*s1.dtydtx0;
        s2.dydty0 = s0.dtydty0 + hh*s1.dtydty0;
        s2.dydq0  = s0.dtydq0  + hh*s1.dtydq0;

        double dAx2_dtx = dAx_dtx(C2, Csq2, Ax2, s2.x, s2.y);
        double dAx2_dty = dAx_dty(C2, Csq2, Ax2, s2.x, s2.y);
        double dAy2_dtx = dAy_dtx(C2, Csq2, Ay2, s2.x, s2.y);
        double dAy2_dty = dAy_dty(C2, Csq2, Ay2, s2.x, s2.y);

        s2.dtxdtx0 = this.dtx_dtx0(qv,      dAx2_dtx, dAx2_dty, s2.dxdtx0, s2.dydtx0);
        s2.dtxdty0 = this.dtx_dty0(qv,                dAx2_dty, s2.dxdty0, s2.dydty0);
        s2.dtxdq0  = this.dtx_dq0( qv, Ax2, dAx2_dtx, dAx2_dty, s2.dxdq0,  s2.dydq0);
        s2.dtydtx0 = this.dty_dtx0(qv,      dAy2_dtx, dAy2_dty, s2.dxdtx0, s2.dydtx0);
        s2.dtydty0 = this.dty_dty0(qv,                dAy2_dty, s2.dxdty0, s2.dydty0);
        s2.dtydq0  = this.dty_dq0( qv, Ay2, dAy2_dtx, dAy2_dty, s2.dxdq0,  s2.dydq0);

        // === THIRD STEP ==========================================================================
        RK4Vec s3 = new RK4Vec();
        swim.Bfield(sector, s0.x + hh*s2.x, s0.y + hh*s2.y, vec.z + hh, _b);

        // State.
        s3.x = s0.tx + hh*s2.tx;
        s3.y = s0.ty + hh*s2.ty;

        double Csq3  = Csq(s3.x, s3.y);
        double C3    = C(Csq3);
        double Ax3   = Ax(C3, s3.x, s3.y);
        double Ay3   = Ay(C3, s3.x, s3.y);

        s3.tx = qv * Ax3;
        s3.ty = qv * Ay3;

        // Jacobian.
        s3.dxdtx0 = s0.dtxdtx0 + hh*s2.dtxdtx0;
        s3.dxdty0 = s0.dtxdty0 + hh*s2.dtxdty0;
        s3.dxdq0  = s0.dtxdq0  + hh*s2.dtxdq0;
        s3.dydtx0 = s0.dtydtx0 + hh*s2.dtydtx0;
        s3.dydty0 = s0.dtydty0 + hh*s2.dtydty0;
        s3.dydq0  = s0.dtydq0  + hh*s2.dtydq0;

        double dAx3_dtx = dAx_dtx(C3, Csq3, Ax3, s3.x, s3.y);
        double dAx3_dty = dAx_dty(C3, Csq3, Ax3, s3.x, s3.y);
        double dAy3_dtx = dAy_dtx(C3, Csq3, Ay3, s3.x, s3.y);
        double dAy3_dty = dAy_dty(C3, Csq3, Ay3, s3.x, s3.y);

        s3.dtxdtx0 = this.dtx_dtx0(qv,      dAx3_dtx, dAx3_dty, s3.dxdtx0, s3.dydtx0);
        s3.dtxdty0 = this.dtx_dty0(qv,                dAx3_dty, s3.dxdty0, s3.dydty0);
        s3.dtxdq0  = this.dtx_dq0( qv, Ax3, dAx3_dtx, dAx3_dty, s3.dxdq0,  s3.dydq0);
        s3.dtydtx0 = this.dty_dtx0(qv,      dAy3_dtx, dAy3_dty, s3.dxdtx0, s3.dydtx0);
        s3.dtydty0 = this.dty_dty0(qv,                dAy3_dty, s3.dxdty0, s3.dydty0);
        s3.dtydq0  = this.dty_dq0( qv, Ay3, dAy3_dtx, dAy3_dty, s3.dxdq0,  s3.dydq0);

        // === FOURTH STEP =========================================================================
        RK4Vec s4 = new RK4Vec();
        swim.Bfield(sector, s0.x + h*s3.x, s0.y + h*s3.y, vec.z + h, _b);

        // State.
        s4.x = s0.tx + h*s3.tx;
        s4.y = s0.ty + h*s3.ty;

        double Csq4  = Csq(s4.x, s4.y);
        double C4    = C(Csq4);
        double Ax4   = Ax(C4, s4.x, s4.y);
        double Ay4   = Ay(C4, s4.x, s4.y);

        s4.tx = qv * Ax4;
        s4.ty = qv * Ay4;

        // Jacobian.
        s4.dxdtx0 = s0.dtxdtx0 + h*s3.dtxdtx0;
        s4.dxdty0 = s0.dtxdty0 + h*s3.dtxdty0;
        s4.dxdq0  = s0.dtxdq0  + h*s3.dtxdq0;
        s4.dydtx0 = s0.dtydtx0 + h*s3.dtydtx0;
        s4.dydty0 = s0.dtydty0 + h*s3.dtydty0;
        s4.dydq0  = s0.dtydq0  + h*s3.dtydq0;

        double dAx4_dtx = dAx_dtx(C4, Csq4, Ax4, s4.x, s4.y);
        double dAx4_dty = dAx_dty(C4, Csq4, Ax4, s4.x, s4.y);
        double dAy4_dtx = dAy_dtx(C4, Csq4, Ay4, s4.x, s4.y);
        double dAy4_dty = dAy_dty(C4, Csq4, Ay4, s4.x, s4.y);

        s4.dtxdtx0 = this.dtx_dtx0(qv,      dAx4_dtx, dAx4_dty, s4.dxdtx0, s4.dydtx0);
        s4.dtxdty0 = this.dtx_dty0(qv,                dAx4_dty, s4.dxdty0, s4.dydty0);
        s4.dtxdq0  = this.dtx_dq0( qv, Ax4, dAx4_dtx, dAx4_dty, s4.dxdq0,  s4.dydq0);
        s4.dtydtx0 = this.dty_dtx0(qv,      dAy4_dtx, dAy4_dty, s4.dxdtx0, s4.dydtx0);
        s4.dtydty0 = this.dty_dty0(qv,                dAy4_dty, s4.dxdty0, s4.dydty0);
        s4.dtydq0  = this.dty_dq0( qv, Ay4, dAy4_dtx, dAy4_dty, s4.dxdq0,  s4.dydq0);

        // === FINAL STATE =========================================================================
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

        vec.x   = sF.x;
        vec.y   = sF.y;
        vec.tx  = sF.tx;
        vec.ty  = sF.ty;
        vec.z  += h;
        vec.B   = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        vec.deltaPath += Math.sqrt((s0.x-vec.x)*(s0.x-vec.x) + (s0.y-vec.y)*(s0.y-vec.y) + h*h);

        // === COVARIANCE MATRIX ===================================================================
        double[][] cF = new double[5][5]; // cF = FCF^T
        cF[0][0] = C.get(0,0)+C.get(2,0)*sF.dxdtx0+C.get(3,0)*sF.dxdty0+C.get(4,0)*sF.dxdq0
                + sF.dxdq0  *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                + sF.dxdtx0 *(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                + sF.dxdty0 *(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);
        cF[0][1] = C.get(0,1)+C.get(2,1)*sF.dxdtx0+C.get(3,1)*sF.dxdty0+C.get(4,1)*sF.dxdq0
                + sF.dydq0  *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                + sF.dydtx0 *(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                + sF.dydty0 *(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);
        cF[0][2] =sF.dtxdq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                + sF.dtxdtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                + sF.dtxdty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);
        cF[0][3] =sF.dtydq0 *(C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0)
                + sF.dtydtx0*(C.get(0,2)+C.get(2,2)*sF.dxdtx0+C.get(3,2)*sF.dxdty0+C.get(4,2)*sF.dxdq0)
                + sF.dtydty0*(C.get(0,3)+C.get(2,3)*sF.dxdtx0+C.get(3,3)*sF.dxdty0+C.get(4,3)*sF.dxdq0);
        cF[0][4] = C.get(0,4)+C.get(2,4)*sF.dxdtx0+C.get(3,4)*sF.dxdty0+C.get(4,4)*sF.dxdq0;

        cF[1][0] = C.get(1,0)+C.get(2,0)*sF.dydtx0+C.get(3,0)*sF.dydty0+C.get(4,0)*sF.dydq0
                + sF.dxdq0  *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                + sF.dxdtx0 *(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                + sF.dxdty0 *(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);
        cF[1][1] = C.get(1,1)+C.get(2,1)*sF.dydtx0+C.get(3,1)*sF.dydty0+C.get(4,1)*sF.dydq0
                + sF.dydq0  *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                + sF.dydtx0 *(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                + sF.dydty0 *(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);
        cF[1][2] =sF.dtxdq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                + sF.dtxdtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                + sF.dtxdty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);
        cF[1][3] =sF.dtydq0 *(C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0)
                + sF.dtydtx0*(C.get(1,2)+C.get(2,2)*sF.dydtx0+C.get(3,2)*sF.dydty0+C.get(4,2)*sF.dydq0)
                + sF.dtydty0*(C.get(1,3)+C.get(2,3)*sF.dydtx0+C.get(3,3)*sF.dydty0+C.get(4,3)*sF.dydq0);
        cF[1][4] = C.get(1,4)+C.get(2,4)*sF.dydtx0+C.get(3,4)*sF.dydty0+C.get(4,4)*sF.dydq0;

        cF[2][0] = C.get(2,0)*sF.dtxdtx0+C.get(3,0)*sF.dtxdty0+C.get(4,0)*sF.dtxdq0
                + sF.dxdq0  *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                + sF.dxdtx0 *(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                + sF.dxdty0 *(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);
        cF[2][1] = C.get(2,1)*sF.dtxdtx0+C.get(3,1)*sF.dtxdty0+C.get(4,1)*sF.dtxdq0
                + sF.dydq0  *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                + sF.dydtx0 *(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                + sF.dydty0 *(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);
        cF[2][2] =sF.dtxdq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                + sF.dtxdtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                + sF.dtxdty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);
        cF[2][3] =sF.dtydq0 *(C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0)
                + sF.dtydtx0*(C.get(2,2)*sF.dtxdtx0+C.get(3,2)*sF.dtxdty0+C.get(4,2)*sF.dtxdq0)
                + sF.dtydty0*(C.get(2,3)*sF.dtxdtx0+C.get(3,3)*sF.dtxdty0+C.get(4,3)*sF.dtxdq0);
        cF[2][4] = C.get(2,4)*sF.dtxdtx0+C.get(3,4)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0;

        cF[3][0] = C.get(2,0)*sF.dtydtx0+C.get(3,0)*sF.dtydty0+C.get(4,0)*sF.dtydq0
                + sF.dxdq0  *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                + sF.dxdtx0 *(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                + sF.dxdty0 *(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);
        cF[3][1] = C.get(2,1)*sF.dtydtx0+C.get(3,1)*sF.dtydty0+C.get(4,1)*sF.dtydq0
                + sF.dydq0  *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                + sF.dydtx0 *(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                + sF.dydty0 *(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);
        cF[3][2] =sF.dtxdq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                + sF.dtxdtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                + sF.dtxdty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);
        cF[3][3] =sF.dtydq0 *(C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0)
                + sF.dtydtx0*(C.get(2,2)*sF.dtydtx0+C.get(3,2)*sF.dtydty0+C.get(4,2)*sF.dtydq0)
                + sF.dtydty0*(C.get(2,3)*sF.dtydtx0+C.get(3,3)*sF.dtydty0+C.get(4,3)*sF.dtydq0);
        cF[3][4] = C.get(2,4)*sF.dtydtx0+C.get(3,4)*sF.dtydty0+C.get(4,4)*sF.dtydq0;

        cF[4][0] = C.get(4,0)+C.get(4,2)*sF.dxdtx0+C.get(4,3)*sF.dxdty0+C.get(4,4)*sF.dxdq0;
        cF[4][1] = C.get(4,1)+C.get(4,2)*sF.dydtx0+C.get(4,3)*sF.dydty0+C.get(4,4)*sF.dydq0;
        cF[4][2] = C.get(4,2)*sF.dtxdtx0+C.get(4,3)*sF.dtxdty0+C.get(4,4)*sF.dtxdq0;
        cF[4][3] = C.get(4,2)*sF.dtydtx0+C.get(4,3)*sF.dtydty0+C.get(4,4)*sF.dtydq0;
        cF[4][4] = C.get(4,4);

        fCov.covMat.set(cF);
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
        RK4Vec(StateVecsDoca.StateVec vec) {
            this.x  = vec.x;
            this.y  = vec.y;
            this.tx = vec.tx;
            this.ty = vec.ty;

            this.dtxdtx0 = 1;
            this.dtydty0 = 1;
        }
    }
}
