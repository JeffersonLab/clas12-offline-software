/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.clas.tracking.utilities;

import org.jlab.clas.clas.math.FastMath;
import static org.jlab.clas.tracking.trackrep.Helix.LIGHTVEL;

/**
 *
 * @author ziegler
 * Jacobian calculations by Luca Marsicano and Mylene Caudron
 */
public class CovMatUtil {

    /**
     * 
     * @param signedSolenoidMag
     * @param unit
     * @param charge
     * @param pt
     * @param d0
     * @param phi0
     * @param Z0
     * @param tandip
     * @param hCov  covariance matrix in helical representation
     * @return  covariance matrix in cartesian format
     */
    public static double[][] getCartesianCovMat(double signedSolenoidMag, Units unit, 
            int charge, double pt, double d0, double phi0, double Z0, double tandip, 
            double[][] hCov) {
        double[][] tCov = new double[6][6];
        
        //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
        // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
        // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
        // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature     0            0             |
        // | 0                              0                             0                    d_Z0*d_Z0                     |
        // | 0                              0                             0                       0        d_tandip*d_tandip |
        
        double rho = getRhoFromPt(signedSolenoidMag, unit, charge, pt); 
        
        double delxdeld0 = -Math.sin(phi0);
        double delxdelphi0 = -d0*Math.cos(phi0);
        double delydeld0 = Math.cos(phi0);
        double delydelphi0 = -d0*Math.sin(phi0);
        
        //double delzdelz0 = 1;
        
        double delpxdelphi0 = -pt*Math.sin(phi0);   
        double delpxdelrho = -pt*Math.cos(phi0)/rho;    
        double delpydelphi0 = pt*Math.cos(phi0);
        double delpydelrho = -pt*Math.sin(phi0)/rho;    
        
        double delpzdelrho = -pt*tandip/rho;    
        
        double delpzdeltandip = pt;
        
        tCov[0][0] = (hCov[0][0]*delxdeld0+hCov[0][1]*delxdelphi0)*delxdeld0
                    +(hCov[1][0]*delxdeld0+hCov[1][1]*delxdelphi0)*delxdelphi0;
        tCov[0][1] = (hCov[0][0]*delydeld0+hCov[0][1]*delydelphi0)*delxdeld0
                    +(hCov[1][0]*delydeld0+hCov[1][1]*delydelphi0)*delxdelphi0;
        tCov[0][2] =  hCov[0][3]*delxdeld0+hCov[1][3]*delxdelphi0;
        tCov[0][3] = (hCov[0][1]*delpxdelphi0+hCov[0][2]*delpxdelrho)*delxdeld0
                    +(hCov[1][1]*delpxdelphi0+hCov[1][2]*delpxdelrho)*delxdelphi0;
        tCov[0][4] = (hCov[0][1]*delpydelphi0+hCov[0][2]*delpydelrho)*delxdeld0
                    +(hCov[1][1]*delpydelphi0+hCov[1][2]*delpydelrho)*delxdelphi0;
        tCov[0][5] = (hCov[0][2]*delpzdelrho+hCov[0][4]*delpzdeltandip)*delxdeld0
                    +(hCov[1][2]*delpzdelrho+hCov[1][4]*delpzdeltandip)*delxdelphi0;
        
        
        tCov[1][0] = (hCov[0][0]*delxdeld0+hCov[0][1]*delxdelphi0)*delydeld0
                    +(hCov[1][0]*delxdeld0+hCov[1][1]*delxdelphi0)*delydelphi0;
        tCov[1][1] = (hCov[0][0]*delydeld0+hCov[0][1]*delydelphi0)*delydeld0
                    +(hCov[1][0]*delydeld0+hCov[1][1]*delydelphi0)*delydelphi0;
        tCov[1][2] = (hCov[0][3]*delydeld0+hCov[1][3]*delydelphi0);
        tCov[1][3] = (hCov[0][1]*delpxdelphi0+hCov[0][2]*delpxdelrho)*delydeld0
                    +(hCov[1][1]*delpxdelphi0+hCov[1][2]*delpxdelrho)*delydelphi0;
        tCov[1][4] = (hCov[0][1]*delpydelphi0+hCov[0][2]*delpydelrho)*delydeld0
                    +(hCov[1][1]*delpydelphi0+hCov[1][2]*delpydelrho)*delydelphi0;
        tCov[1][5] = (hCov[0][2]*delpzdelrho+hCov[0][4]*delpzdeltandip)*delydeld0
                    +(hCov[1][2]*delpzdelrho+hCov[1][4]*delpzdeltandip)*delydelphi0;
        
        
        tCov[2][0] = (hCov[3][0]*delxdeld0+hCov[3][1]*delxdelphi0);
        tCov[2][1] = (hCov[3][0]*delydeld0+hCov[3][1]*delydelphi0);
        tCov[2][2] =  hCov[3][3];
        tCov[2][3] = (hCov[3][1]*delpxdelphi0+hCov[3][2]*delpxdelrho);
        tCov[2][4] = (hCov[3][1]*delpydelphi0+hCov[3][2]*delpydelrho);
        tCov[2][5] = (hCov[3][2]*delpzdelrho+hCov[3][4]*delpzdeltandip);


        tCov[3][0] = (hCov[1][0]*delxdeld0+hCov[1][1]*delxdelphi0)*delpxdelphi0
                    +(hCov[2][0]*delxdeld0+hCov[2][1]*delxdelphi0)*delpxdelrho;
        tCov[3][1] = (hCov[1][0]*delydeld0+hCov[1][1]*delydelphi0)*delpxdelphi0
                    +(hCov[2][0]*delydeld0+hCov[2][1]*delydelphi0)*delpxdelrho;
        tCov[3][2] =  hCov[1][3]*delpxdelphi0+hCov[2][3]*delpxdelrho;
        tCov[3][3] = (hCov[1][1]*delpxdelphi0+hCov[1][2]*delpxdelrho)*delpxdelphi0
                    +(hCov[2][1]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpxdelrho;
        tCov[3][4] = (hCov[1][1]*delpydelphi0+hCov[1][2]*delpydelrho)*delpxdelphi0
                    +(hCov[2][1]*delpydelphi0+hCov[2][2]*delpydelrho)*delpxdelrho;
        tCov[3][5] = (hCov[1][2]*delpzdelrho+hCov[1][4]*delpzdeltandip)*delpxdelphi0
                    +(hCov[2][2]*delpzdelrho+hCov[2][4]*delpzdeltandip)*delpxdelrho;
                
        
        tCov[4][0] = (hCov[1][0]*delxdeld0+hCov[1][1]*delxdelphi0)*delpydelphi0
                    +(hCov[2][0]*delxdeld0+hCov[2][1]*delxdelphi0)*delpydelrho;
        tCov[4][1] = (hCov[1][0]*delydeld0+hCov[1][1]*delydelphi0)*delpydelphi0
                    +(hCov[2][0]*delydeld0+hCov[2][1]*delydelphi0)*delpydelrho;
        tCov[4][2] =  hCov[1][3]*delpydelphi0+hCov[2][3]*delpydelrho;
        tCov[4][3] = (hCov[1][1]*delpxdelphi0+hCov[1][2]*delpxdelrho)*delpydelphi0
                    +(hCov[2][1]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpydelrho;
        tCov[4][4] = (hCov[1][1]*delpydelphi0+hCov[1][2]*delpydelrho)*delpydelphi0
                    +(hCov[2][1]*delpydelphi0+hCov[2][2]*delpydelrho)*delpydelrho;
        tCov[4][5] = (hCov[1][2]*delpzdelrho+hCov[1][4]*delpzdeltandip)*delpydelphi0
                    +(hCov[2][2]*delpzdelrho+hCov[2][4]*delpzdeltandip)*delpydelrho;
              
        
        tCov[5][0] = (hCov[2][0]*delxdeld0+hCov[2][1]*delxdelphi0)*delpzdelrho
                    +(hCov[4][0]*delxdeld0+hCov[4][1]*delxdelphi0)*delpzdeltandip;
        tCov[5][1] = (hCov[2][0]*delydeld0+hCov[2][1]*delydelphi0)*delpzdelrho
                    +(hCov[4][0]*delydeld0+hCov[4][1]*delydelphi0)*delpzdeltandip;        
        tCov[5][2] =  hCov[2][3]*delpzdelrho+hCov[4][3]*delpzdeltandip;        
        tCov[5][3] = (hCov[2][1]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpzdelrho
                    +(hCov[4][1]*delpxdelphi0+hCov[4][2]*delpxdelrho)*delpzdeltandip;        
        tCov[5][4] = (hCov[2][1]*delpydelphi0+hCov[2][2]*delpydelrho)*delpzdelrho
                    +(hCov[4][1]*delpydelphi0+hCov[4][2]*delpydelrho)*delpzdeltandip;
        tCov[5][5] = (hCov[2][2]*delpzdelrho+hCov[2][4]*delpzdeltandip)*delpzdelrho
                    +(hCov[4][2]*delpzdelrho+hCov[4][4]*delpzdeltandip)*delpzdeltandip;
        
        //for (int k = 0; k < 6; k++) {
        //    System.out.println(tCov[k][0]+"	"+tCov[k][1]+"	"+tCov[k][2]+"	"+tCov[k][3]+"	"+tCov[k][4]+"	"+tCov[k][5]);
        //}
        //System.out.println("    ");
        
        return tCov;
    }
    /**
     * 
     * @param signedSolenoidMag
     * @param unit (CM or MM)
     * @param charge
     * @param pt
     * @return track curvature (signed quantity in Units)
     */
    private static double getRhoFromPt(double signedSolenoidMag, Units unit, int charge, double pt) {
        double c = LIGHTVEL*unit.value();
        double rho = (double)charge*c*signedSolenoidMag/pt;
        
        return rho;
    }
    
    /**
     * 
     * @param sector
     * @param charge
     * @param x
     * @param y
     * @param z
     * @param px
     * @param py
     * @param pz
     * @param TCSCov the track covariance matrix in the Tilted Coordinate System at the vertex
     * @return the track covariance matrix in the lab frame in cartesian coordimates (x,y,z,px,py,pz)
     */
    public static double[][] getCartesianCovMat(int sector, int charge, 
            double x, double y, double z, double px, double py, double pz, 
            double[][] TCSCov) {
        double[] q = new double[6];
        q[0]=x;
        q[1]=y;
        q[2]=z;
        q[3]=px;
        q[4]=py;
        q[5]=pz;
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