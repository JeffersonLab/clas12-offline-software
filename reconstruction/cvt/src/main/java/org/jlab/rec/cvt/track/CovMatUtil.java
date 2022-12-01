/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jlab.rec.cvt.track;

import org.jlab.clas.clas.math.FastMath;
import org.jlab.rec.cvt.Constants;

/**
 *
 * @author ziegler
 * Jacobian calculations by Luca Marsicano and Mylene Caudron
 */
public class CovMatUtil {

    /**
     * 
     * @param solenoidMag
     * @param charge
     * @param pt
     * @param d0
     * @param phi0
     * @param Z0
     * @param tandip
     * @param hCov
     * @return covariance matrix in cartesian format
     */
    public static double[][] getCartesianCovMat(double solenoidMag, int charge, double pt, double d0, double phi0, double Z0, double tandip, 
            double[][] hCov) {
        double[][] tCov = new double[6][6];
        
        //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
        // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
        // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
        // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature     0            0             |
        // | 0                              0                             0                    d_Z0*d_Z0                     |
        // | 0                              0                             0                       0        d_tandip*d_tandip |
        // (double pt, double d0, double phi0, double Z0, double tandip, int q, double solenoidMag) {
        double c = Constants.LIGHTVEL;
        double rho = (double)charge*c*solenoidMag/pt * Math.signum(Constants.getSolenoidScale());
        //double Bz = 1./Math.abs(rho);
        
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