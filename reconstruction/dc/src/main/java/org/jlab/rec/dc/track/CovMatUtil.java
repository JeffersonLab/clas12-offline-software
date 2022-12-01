/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.dc.track;

import org.jlab.clas.clas.math.FastMath;

/**
 *
 * @author ziegler
 * Jacobian calculations by Luca Marsicano and Mylene Caudron
 */
public class CovMatUtil {

    /**
     * 
     * @param sector the DC sector of the track
     * @param charge the track charge
     * @param q the track parameters in the lab frame at the vertex
     * @param TCSCov the track covariance matrix in the Tilted Coordinate System at the vertex
     * @return the track covariance matrix in the lab frame in cartesian coordimates (x,y,z,px,py,pz)
     */
    public static double[][] getCartesianCovMat(int sector, int charge, double[] q, 
            double[][] TCSCov) {
        double[][] C = new double[6][6]; // C' = F^T C F
        double[][] CF = new double[5][6];
        //the parameters in the TCS
        double[] t = localFramePars(sector, charge, q);
        
        double Q = (double) charge;
        
        double thetaS = Math.PI/3*(sector -1);
        double thetaT = Math.toDegrees(-25.0);
        
        double cS = Math.cos(thetaS);
        double sS = Math.sin(thetaS);
        double cT = Math.cos(thetaT);
        double sT = Math.sin(thetaT);
        
        double del_xH_del_xT = cS*cT;
        double del_xH_del_yT = - sS;
        double del_xH_del_zT = cS*sT;
        
        double del_yH_del_xT = sS*cT;
        double del_yH_del_yT = cS;
        double del_yH_del_zT = sS*sT;
        
        double del_zH_del_xT = -sT;
        double del_zH_del_zT = cT;
        
        double del_pxH_del_txT = Q/t[5] *
                (cS*cT*t[4]*t[4]+sS*t[3]*t[4]-cS*sT*t[3]+cS*cT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pxH_del_tyT = Q/t[5] *
                (-sS*t[3]*t[3]-cS*cT*t[3]*t[4]-cS*sT*t[4]-sS)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pxH_del_QT = Q/(t[5]*t[5]) *
                (-cS*cT*t[3]+sS*t[4]-cS*sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_txT = Q/t[5] *
                (sS*cT*t[4]*t[4]-cS*t[3]*t[4]-sS*sT*t[3]+sS*cT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_tyT = Q/t[5] *
                (cS*t[3]*t[3]-sS*cT*t[3]*t[4]-sS*sT*t[4]+cS)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pyH_del_QT = Q/(t[5]*t[5]) *
                (-sS*cT*t[3]-cS*t[4]-sS*sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_txT = Q/t[5] *
                (-sT*t[4]*t[4]-cT*t[3]-sT)/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_tyT = Q/t[5] *
                (sT*t[3]*t[4]-cT*t[4])/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5])*(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double del_pzH_del_QT = Q/(t[5]*t[5]) *
                (sT*t[3]-cT*t[4])/
                (Math.sqrt(t[3]*t[3]+t[4]*t[4]+t[5]*t[5]));
        
        double[][] F = new double[][]{
            {del_xH_del_xT, del_yH_del_xT, del_zH_del_xT, 0, 0, 0},
            {del_xH_del_yT, del_yH_del_yT, 0, 0, 0, 0},
            {del_xH_del_zT, del_yH_del_zT, del_zH_del_zT, del_pxH_del_txT, del_pyH_del_txT, del_pzH_del_txT},
            {0, 0, 0, del_pxH_del_tyT, del_pyH_del_tyT, del_pzH_del_tyT},
            {0, 0, 0, del_pxH_del_QT, del_pyH_del_QT, del_pzH_del_QT}};
        
        double[][] FT = new double[6][5];
        for(int i = 0; i<6; i++) {
            for(int j = 0; j<5; j++) {
                FT[i][j] = F[j][i];
            }
        }        
          
        
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i < 6; i++) {
                 for (int j = 0; j < 5; j++) {
                     CF[k][i] += F[j][i] * TCSCov[k][j];
                }
            }
        }
        
        for (int k = 0; k < 6; k++) {
            for (int i = 0; i < 5; i++) {
                 for (int j = 0; j < 6; j++) {
                     C[k][j] += CF[i][k] * FT[j][i]; 
                }
            }
        }
        
        return C;
    }

    private static double[] localFramePars(int sector, int charge, double[] q) {
        double t[] = new double[6];
        double[] xl = getCoordsInLab(sector, q[0],q[1],q[2]);
        double[] pl = getCoordsInLab(sector, q[3],q[4],q[5]);
        t[0] = xl[0];
        t[1] = xl[1];
        t[2] = xl[2];
        t[3] = pl[0]/pl[2];
        t[4] = pl[1]/pl[2];
        t[5] = charge/Math.sqrt(q[3]*q[3]+q[4]*q[4]+q[5]*q[5]);
        
        return q;
    }
    
    static final double cos_tilt = FastMath.cos(Math.toRadians(25.));
    static final double sin_tilt = FastMath.sin(Math.toRadians(25.));
    private static double[] getCoordsInLab(int sector, double x, double y, double z) {
        double[] coordLab = new double[3];
        double sx = x * cos_tilt + z * sin_tilt;
        coordLab[0] = sx * FastMath.cos((sector - 1) * Math.toRadians(60.)) - y * FastMath.sin((sector - 1) * Math.toRadians(60.));
        coordLab[1] = sx * FastMath.sin((sector - 1) * Math.toRadians(60.)) + y * FastMath.cos((sector - 1) * Math.toRadians(60.));
        coordLab[2] = -x * sin_tilt + z * cos_tilt;
        
        return coordLab;
    }
    
    
}