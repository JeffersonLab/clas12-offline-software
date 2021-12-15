package org.jlab.rec.fmt.track.fit;

import java.util.ArrayList;
import org.jlab.clas.swimtools.Swim;

/**
 *
 * @author ziegler
 */
public class RungeKutta {

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

    public RungeKutta() {
        this.k1 = new ArrayList<>(4);
        this.k2 = new ArrayList<>(4);
        this.k3 = new ArrayList<>(4);
        this.k4 = new ArrayList<>(4);
        this.jk1 = new ArrayList<>(12);
        this.jk2 = new ArrayList<>(12);
        this.jk3 = new ArrayList<>(12);
        this.jk4 = new ArrayList<>(12);
    }

    public void SwimToZ(int sector, StateVecs.StateVec fVec, Swim dcSwim, double z0, float[] bf){

        double stepSize = 0.5;
        dcSwim.BfieldLab(fVec.x, fVec.y, fVec.z, bf);

        fVec.B = Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2]);
        double s  = fVec.B;
        double z = fVec.z;
        final double Zi = fVec.z;
        double BatMeas = 0;

        while(Math.signum(z0 - Zi) *z<Math.signum(z0 - Zi) *z0) {
            //System.out.println(" RK step num "+(j+1)+" = "+(float)s+" nSteps = "+nSteps);
            double x =  fVec.x;
            double y =  fVec.y;
            z = fVec.z;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q =  fVec.Q;
            double dPath = fVec.deltaPath;

            s= Math.signum(z0 - Zi) * stepSize;
            if(Math.signum(z0 - Zi) *(z+s)>Math.signum(z0 - Zi) *z0)
                s=Math.signum(z0 - Zi) *Math.abs(z0-z);

            this.RK4transport( sector, Q, x, y, z, tx, ty, s, dcSwim,
                        dPath, fVec);

            BatMeas = fVec.B;
        }

    }

    void RK4transport(int sector, double q, double x0, double y0, double z0, double tx0, double ty0, double h, Swim swimmer,
            double dPath, StateVecs.StateVec fVec) { // lab system = 1, TSC =0

        swimmer.BfieldLab(x0, y0, z0, _b);
        double x1 = tx0;
        double y1 = ty0;
        double tx1=q*v*Ax(tx0, ty0, _b[0], _b[1], _b[2]);
        double ty1=q*v*Ay(tx0, ty0, _b[0], _b[1], _b[2]);

        swimmer.BfieldLab(x0+0.5*h*x1, y0+0.5*h*y1, z0+0.5*h, _b);
        double x2 = tx0+0.5*h*tx1;
        double y2 = ty0+0.5*h*ty1;
        double tx2=q*v*Ax((tx0+0.5*h*tx1), (ty0+0.5*h*ty1), _b[0], _b[1], _b[2]);
        double ty2=q*v*Ay((tx0+0.5*h*tx1), (ty0+0.5*h*ty1), _b[0], _b[1], _b[2]);

        swimmer.BfieldLab(x0+0.5*h*x2, y0+0.5*h*y2, z0+0.5*h, _b);
        double x3 = tx0+0.5*h*tx2;
        double y3 = ty0+0.5*h*ty2;
        double tx3=q*v*Ax((tx0+0.5*h*tx2), (ty0+0.5*h*ty2), _b[0], _b[1], _b[2]);
        double ty3=q*v*Ay((tx0+0.5*h*tx2), (ty0+0.5*h*ty2), _b[0], _b[1], _b[2]);

        swimmer.BfieldLab(x0+h*x3, y0+h*y3, z0+h, _b);
        double x4 = tx0+h*tx3;
        double y4 = ty0+h*ty3;
        double tx4=q*v*Ax((tx0+h*tx3), (ty0+h*ty3), _b[0], _b[1], _b[2]);
        double ty4=q*v*Ay((tx0+h*tx3), (ty0+h*ty3), _b[0], _b[1], _b[2]);


        double x = x0 + this.RK4(x1, x2, x3, x4, h);
        double y = y0 + this.RK4(y1, y2, y3, y4, h);
        double tx = tx0 + this.RK4(tx1, tx2, tx3, tx4, h);
        double ty = ty0 + this.RK4(ty1, ty2, ty3, ty4, h);
        double z = z0+h;


        fVec.x = x;
        fVec.y  = y ;
        fVec.z = z0+h;
        fVec.tx = tx;
        fVec.ty = ty;
        fVec.Q = q;
        fVec.B = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        fVec.deltaPath = Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y)+h*h)+dPath;



    }
    //
    void RK4transport(int sector, double q, double x0, double y0, double z0, double tx0, double ty0, double h, Swim swimmer,
            StateVecs.CovMat covMat, StateVecs.StateVec fVec, StateVecs.CovMat fCov, double dPath) {
        // Jacobian:
        double[][] u = new double[5][5];
        double[][] C = new double[5][5];
        double deltx_deltx0_0 =1;
        double delty_deltx0_0 =0;
        double deltx_delty0_0 =0;
        double delty_delty0_0 =1;
        double deltx_delq0_0 =0;
        double delty_delq0_0 =0;
        //System.out.println("RK0 "+x0+","+y0+","+z0+";"+tx0+","+ty0+","+" z0 "+z0+" h "+h);
        //State
        swimmer.BfieldLab(x0, y0, z0, _b);
        double x1 = tx0;
        double y1 = ty0;
        double tx1=q*v*Ax(tx0, ty0, _b[0], _b[1], _b[2]);
        double ty1=q*v*Ay(tx0, ty0, _b[0], _b[1], _b[2]);

        // Jacobian:
        double delx_deltx0_1 = deltx_deltx0_0;
        double dely_deltx0_1 = delty_deltx0_0;
        double delx_delty0_1 = deltx_delty0_0;
        double dely_delty0_1 = delty_delty0_0;

        double deltx_deltx0_1 = q*v*(delAx_deltx(tx0,ty0,_b[0],_b[1],_b[2])*deltx_deltx0_0
                + delAx_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_deltx0_0);
        double delty_deltx0_1 = q*v*(delAy_deltx(tx0,ty0,_b[0],_b[1],_b[2])*deltx_deltx0_0
                + delAy_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_deltx0_0);
        double deltx_delty0_1 = q*v*(delAx_delty(tx0,ty0,_b[0],_b[1],_b[2])*deltx_delty0_0
                + delAx_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_delty0_0);
        double delty_delty0_1 = q*v*(delAy_delty(tx0,ty0,_b[0],_b[1],_b[2])*deltx_delty0_0
                + delAy_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_delty0_0);

        double delx_delq0_1 = deltx_delq0_0;
        double dely_delq0_1 = delty_delq0_0;

        double deltx_delq0_1 = v*Ax(tx0, ty0, _b[0], _b[1], _b[2])
                + q*v*(delAx_deltx(tx0,ty0,_b[0],_b[1],_b[2])*deltx_delq0_0
                    + delAx_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_delq0_0);
        double delty_delq0_1 = v*Ay(tx0, ty0, _b[0], _b[1], _b[2])
                + q*v*(delAy_deltx(tx0,ty0,_b[0],_b[1],_b[2])*deltx_delq0_0
                    + delAy_delty(tx0,ty0,_b[0],_b[1],_b[2])*delty_delq0_0);


        swimmer.BfieldLab(x0+0.5*h*x1, y0+0.5*h*y1, z0+0.5*h, _b);
        double x2 = tx0+0.5*h*tx1;
        double y2 = ty0+0.5*h*ty1;
        double tx2=q*v*Ax((tx0+0.5*h*tx1), (ty0+0.5*h*ty1), _b[0], _b[1], _b[2]);
        double ty2=q*v*Ay((tx0+0.5*h*tx1), (ty0+0.5*h*ty1), _b[0], _b[1], _b[2]);

        // Jacobian:
        double delx_deltx0_2 = deltx_deltx0_0+0.5*h*deltx_deltx0_1;
        double dely_deltx0_2 = delty_deltx0_0+0.5*h*delty_deltx0_1;
        double delx_delty0_2 = deltx_delty0_0+0.5*h*deltx_delty0_1;
        double dely_delty0_2 = delty_delty0_0+0.5*h*delty_delty0_1;

        double deltx_deltx0_2 = this.deltx_deltx0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_deltx0_0+0.5*h*deltx_deltx0_1,delty_deltx0_0+0.5*h*delty_deltx0_1);
        double delty_deltx0_2 = this.delty_deltx0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_deltx0_0+0.5*h*deltx_deltx0_1,delty_deltx0_0+0.5*h*delty_deltx0_1);
        double deltx_delty0_2 = this.deltx_delty0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_delty0_0+0.5*h*deltx_delty0_1,delty_delty0_0+0.5*h*delty_delty0_1);
        double delty_delty0_2 = this.delty_delty0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_delty0_0+0.5*h*deltx_delty0_1,delty_delty0_0+0.5*h*delty_delty0_1);

        double delx_delq0_2 = deltx_delq0_0+0.5*h*deltx_delq0_1;
        double dely_delq0_2 = delty_delq0_0+0.5*h*delty_delq0_1;

        double deltx_delq0_2 = this.deltx_delq0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_delq0_0+0.5*h*deltx_delq0_1,delty_delq0_0+0.5*h*delty_delq0_1);
        double delty_delq0_2 = this.delty_delq0_next(q,v,tx0+0.5*h*tx1,ty0+0.5*h*ty1,_b[0],_b[1],_b[2],
                deltx_delq0_0+0.5*h*deltx_delq0_1,delty_delq0_0+0.5*h*delty_delq0_1);

        swimmer.BfieldLab(x0+0.5*h*x2, y0+0.5*h*y2, z0+0.5*h, _b);
        double x3 = tx0+0.5*h*tx2;
        double y3 = ty0+0.5*h*ty2;
        double tx3=q*v*Ax((tx0+0.5*h*tx2), (ty0+0.5*h*ty2), _b[0], _b[1], _b[2]);
        double ty3=q*v*Ay((tx0+0.5*h*tx2), (ty0+0.5*h*ty2), _b[0], _b[1], _b[2]);

        // Jacobian:
        double delx_deltx0_3 = deltx_deltx0_0+0.5*h*deltx_deltx0_2;
        double dely_deltx0_3 = delty_deltx0_0+0.5*h*delty_deltx0_2;
        double delx_delty0_3 = deltx_delty0_0+0.5*h*deltx_delty0_2;
        double dely_delty0_3 = delty_delty0_0+0.5*h*delty_delty0_2;

        double deltx_deltx0_3 = this.deltx_deltx0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_deltx0_0+0.5*h*deltx_deltx0_2,delty_deltx0_0+0.5*h*delty_deltx0_2);
        double delty_deltx0_3 = this.delty_deltx0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_deltx0_0+0.5*h*deltx_deltx0_2,delty_deltx0_0+0.5*h*delty_deltx0_2);
        double deltx_delty0_3 = this.deltx_delty0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_delty0_0+0.5*h*deltx_delty0_2,delty_delty0_0+0.5*h*delty_delty0_2);
        double delty_delty0_3 = this.delty_delty0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_delty0_0+0.5*h*deltx_delty0_2,delty_delty0_0+0.5*h*delty_delty0_2);

        double delx_delq0_3 = deltx_delq0_0+0.5*h*deltx_delq0_2;
        double dely_delq0_3 = delty_delq0_0+0.5*h*delty_delq0_2;

        double deltx_delq0_3 = this.deltx_delq0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_delq0_0+0.5*h*deltx_delq0_2,delty_delq0_0+0.5*h*delty_delq0_2);
        double delty_delq0_3 = this.delty_delq0_next(q,v,tx0+0.5*h*tx2,ty0+0.5*h*ty2,_b[0],_b[1],_b[2],
                deltx_delq0_0+0.5*h*deltx_delq0_2,delty_delq0_0+0.5*h*delty_delq0_2);

        swimmer.BfieldLab(x0+h*x3, y0+h*y3, z0+h, _b);
        double x4 = tx0+h*tx3;
        double y4 = ty0+h*ty3;
        double tx4=q*v*Ax((tx0+h*tx3), (ty0+h*ty3), _b[0], _b[1], _b[2]);
        double ty4=q*v*Ay((tx0+h*tx3), (ty0+h*ty3), _b[0], _b[1], _b[2]);

         // Jacobian:
        double delx_deltx0_4 = deltx_deltx0_0+h*deltx_deltx0_3;
        double dely_deltx0_4 = delty_deltx0_0+h*delty_deltx0_3;
        double delx_delty0_4 = deltx_delty0_0+h*deltx_delty0_3;
        double dely_delty0_4 = delty_delty0_0+h*delty_delty0_3;

        double deltx_deltx0_4 = this.deltx_deltx0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_deltx0_0+h*deltx_deltx0_3,delty_deltx0_0+h*delty_deltx0_3);
        double delty_deltx0_4 = this.delty_deltx0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_deltx0_0+h*deltx_deltx0_3,delty_deltx0_0+h*delty_deltx0_3);
        double deltx_delty0_4 = this.deltx_delty0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_delty0_0+h*deltx_delty0_3,delty_delty0_0+h*delty_delty0_3);
        double delty_delty0_4 = this.delty_delty0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_delty0_0+h*deltx_delty0_3,delty_delty0_0+h*delty_delty0_3);

        double delx_delq0_4 = deltx_delq0_0+h*deltx_delq0_3;
        double dely_delq0_4 = delty_delq0_0+h*delty_delq0_3;

        double deltx_delq0_4 = this.deltx_delq0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_delq0_0+h*deltx_delq0_3,delty_delq0_0+h*delty_delq0_3);
        double delty_delq0_4 = this.delty_delq0_next(q,v,tx0+h*tx3,ty0+h*ty3,_b[0],_b[1],_b[2],
                deltx_delq0_0+h*deltx_delq0_3,delty_delq0_0+h*delty_delq0_3);

        double x = x0 + this.RK4(x1, x2, x3, x4, h);
        double y = y0 + this.RK4(y1, y2, y3, y4, h);
        double tx = tx0 + this.RK4(tx1, tx2, tx3, tx4, h);
        double ty = ty0 + this.RK4(ty1, ty2, ty3, ty4, h);
        double z = z0+h;
        //System.out.println("RK "+x+","+y+","+z+";"+tx+","+ty+","+" z0 "+z0);
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

//        // Q  process noise matrix estimate
//        double p = Math.abs(1. / q);
//
//        double X0 = fVec.getX0(z0+h/2);
//        double t_ov_X0 = Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y)+h*h) / X0;//path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm
//
//        double beta = p / Math.sqrt(p * p + mass * mass);  //use particle momentum
//        beta = 1;
//
//        double sctRMS = ((0.0136)/(beta*p))*Math.sqrt(t_ov_X0*Math.sqrt(1 + tx0 * tx0 + ty0 * ty0))*
//                (1 + 0.038 * Math.log(t_ov_X0*Math.sqrt(1 + tx0 * tx0 + ty0 * ty0)));
//
//        double cov_txtx = (1 + tx0 * tx0) * (1 + tx0 * tx0 + ty0 * ty0) * sctRMS * sctRMS;
//        double cov_tyty = (1 + ty0 * ty0) * (1 + tx0 * tx0 + ty0 * ty0) * sctRMS * sctRMS;
//        double cov_txty = tx0 * ty0 * (1 + tx0 * tx0 + ty0 * ty0) * sctRMS * sctRMS;
//
//        if (h > 0) {
//            C[2][2] += cov_txtx;
//            C[2][3] += cov_txty;
//            C[3][2] += cov_txty;
//            C[3][3] += cov_tyty;
//        }

        fVec.x = x;
        fVec.y  = y ;
        fVec.z = z0+h;
        fVec.tx = tx;
        fVec.ty = ty;
        fVec.Q = q;
        fVec.B = Math.sqrt(_b[0]*_b[0]+_b[1]*_b[1]+_b[2]*_b[2]);
        fVec.deltaPath = Math.sqrt((x0-x)*(x0-x)+(y0-y)*(y0-y)+h*h)+dPath;
        fCov.covMat.set(C);
        //System.out.println("Transported matrix");
        //Matrix5x5.show(fCov.covMat);
    }


    private double RK4(double k1, double k2, double k3, double k4, double h) {
        return h/6*(k1 + 2*k2 +2*k3 + k4);
    }

    private double Ax(double tx, double ty, double Bx, double By, double Bz) {
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        return C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
    }
    private double Ay(double tx, double ty, double Bx, double By, double Bz) {
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        return C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);
    }

    private double delAx_deltx(double tx, double ty, double Bx, double By, double Bz) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        return tx * Ax / C2 + C * (ty * Bx - 2 * tx * By); //delAx_deltx
    }
    private double delAx_delty(double tx, double ty, double Bx, double By, double Bz) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        return ty * Ax / C2 + C * (tx * Bx + Bz); //delAx_delty
    }
    private double delAy_deltx(double tx, double ty, double Bx, double By, double Bz) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        return tx * Ay / C2 + C * (-ty * By - Bz); //delAy_deltx
    }
    private double delAy_delty(double tx, double ty, double Bx, double By, double Bz) {
        double C2 = 1 + tx * tx + ty * ty;
        double C = Math.sqrt(1 + tx * tx + ty * ty);
        double Ax = C * (ty * (tx * Bx + Bz) - (1 + tx * tx) * By);
        double Ay = C * (-tx * (ty * By + Bz) + (1 + ty * ty) * Bx);

        return ty * Ay / C2 + C * (-tx * By + 2 * ty * Bx); //delAy_delty
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

    private double deltx_deltx0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_deltx0_1, double delty_deltx0_1) {
        return q*v*(delAx_deltx(tx1,ty1,b0,b1,b2)*(deltx_deltx0_1)
                + delAx_delty(tx1,ty1,b0,b1,b2)*(delty_deltx0_1));
    }

    private double delty_deltx0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_deltx0_1, double delty_deltx0_1) {
        return q*v*(delAy_deltx(tx1,ty1,b0,b1,b2)*(deltx_deltx0_1)
                + delAy_delty(tx1,ty1,b0,b1,b2)*(delty_deltx0_1));
    }

    private double deltx_delty0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_delty0_1, double delty_delty0_1) {
        return q*v*(delAx_delty(tx1,ty1,b0,b1,b2)*(deltx_delty0_1)
                + delAx_delty(tx1,ty1,b0,b1,b2)*(delty_delty0_1));
    }

    private double delty_delty0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_delty0_1, double delty_delty0_1) {
        return q*v*(delAy_delty(tx1,ty1,b0,b1,b2)*(deltx_delty0_1)
                + delAy_delty(tx1,ty1,b0,b1,b2)*(delty_delty0_1));
    }

    private double deltx_delq0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ax(tx1, ty1, b0, b1, b2)
                + q*v*(delAx_deltx(tx1,ty1,b0,b1,b2)*(deltx_delq0_1)
                    + delAx_delty(tx1,ty1,b0,b1,b2)*(delty_delq0_1));
    }

    private double delty_delq0_next(double q, double v, double tx1, double ty1, float b0, float b1, float b2, double deltx_delq0_1, double delty_delq0_1) {
        return v*Ay(tx1, ty1, b0, b1, b2)
                + q*v*(delAy_deltx(tx1, ty1,b0,b1,b2)*(deltx_delq0_1)
                    + delAy_delty(tx1, ty1,b0,b1,b2)*(delty_delq0_1));
    }

    private void getRKn(int sector, ArrayList<Double> k1, ArrayList<Double> k2, double d, double x0, double y0, double z0, double tx0, double ty0, double q, float[] b) {

        double tx1  = k1.get(2);
        double ty1  = k1.get(3);

        double x2 = tx0+d*tx1;
        double y2 = ty0+d*ty1;
        double tx2=q*v*Ax((tx0+d*tx1), (ty0+d*ty1), b[0], b[1], b[2]);
        double ty2=q*v*Ay((tx0+d*tx1), (ty0+d*ty1), b[0], b[1], b[2]);

        k2.add(0, x2);
        k2.add(1, y2);
        k2.add(2, tx2);
        k2.add(3, ty2);
    }

    private void getjRKn(int sector, ArrayList<Double> k1, ArrayList<Double> jk1, ArrayList<Double> jk2, double d, double x0, double y0, double z0, double tx0, double ty0, double q, float[] _b,
            double deltx_deltx0_0, double delty_deltx0_0, double deltx_delty0_0, double delty_delty0_0, double deltx_delq0_0, double delty_delq0_0) {

        double tx1  = k1.get(2);
        double ty1  = k1.get(3);

        double delx_deltx0_1 = jk1.get(0);
        double dely_deltx0_1 = jk1.get(1);
        double delx_delty0_1 = jk1.get(2);
        double dely_delty0_1 = jk1.get(3);

        double deltx_deltx0_1 = jk1.get(4);
        double delty_deltx0_1 = jk1.get(5);
        double deltx_delty0_1 = jk1.get(6);
        double delty_delty0_1 = jk1.get(7);

        double delx_delq0_1 = jk1.get(8);
        double dely_delq0_1 = jk1.get(9);

        double deltx_delq0_1 = jk1.get(10);
        double delty_delq0_1 = jk1.get(11);

        double delx_deltx0_2 = deltx_deltx0_0+d*deltx_deltx0_1;
        double dely_deltx0_2 = delty_deltx0_0+d*delty_deltx0_1;
        double delx_delty0_2 = deltx_delty0_0+d*deltx_delty0_1;
        double dely_delty0_2 = delty_delty0_0+d*delty_delty0_1;

        double deltx_deltx0_2 = this.deltx_deltx0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_deltx0_0+d*deltx_deltx0_1,delty_deltx0_0+d*delty_deltx0_1);
        double delty_deltx0_2 = this.delty_deltx0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_deltx0_0+d*deltx_deltx0_1,delty_deltx0_0+d*delty_deltx0_1);
        double deltx_delty0_2 = this.deltx_delty0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_delty0_0+d*deltx_delty0_1,delty_delty0_0+d*delty_delty0_1);
        double delty_delty0_2 = this.delty_delty0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_delty0_0+d*deltx_delty0_1,delty_delty0_0+d*delty_delty0_1);

        double delx_delq0_2 = deltx_delq0_0+d*deltx_delq0_1;
        double dely_delq0_2 = delty_delq0_0+d*delty_delq0_1;

        double deltx_delq0_2 = this.deltx_delq0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_delq0_0+d*deltx_delq0_1,delty_delq0_0+d*delty_delq0_1);
        double delty_delq0_2 = this.delty_delq0_next(q,v,tx0+d*tx1,ty0+d*ty1,_b[0],_b[1],_b[2],
                deltx_delq0_0+d*deltx_delq0_1,delty_delq0_0+d*delty_delq0_1);

        jk2.add(0, delx_deltx0_2 );
        jk2.add(1, dely_deltx0_2 );
        jk2.add(2, delx_delty0_2 );
        jk2.add(3, dely_delty0_2 );

        jk2.add(4, deltx_deltx0_2 );
        jk2.add(5, delty_deltx0_2 );
        jk2.add(6, deltx_delty0_2 );
        jk2.add(7, delty_delty0_2 );

        jk2.add(8, delx_delq0_2 );
        jk2.add(9, dely_delq0_2 );

        jk2.add(10, deltx_delq0_2 );
        jk2.add(11, delty_delq0_2 );
    }
}
