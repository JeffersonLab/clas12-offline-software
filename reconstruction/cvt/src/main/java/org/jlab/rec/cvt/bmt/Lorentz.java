package org.jlab.rec.cvt.bmt;

/**
 *
 * @author defurne
 */

public class Lorentz {

    public Lorentz() {

    }

    public static double getLorentzAngle(double xe, double xb) {

        if (xe == 0 || xb == 0) {
            return 0;
        }
        double de = (BMTConstants.emax - BMTConstants.emin) / (BMTConstants.Ne - 1);
        double db = (BMTConstants.bmax - BMTConstants.bmin) / (BMTConstants.Nb - 1);

        if (xe < BMTConstants.emin) {
            xe = BMTConstants.emin;
//            System.err.println("Warning: E out of grid... setting it to Emin " + BMTConstants.emin);
        }
        if (xe >= BMTConstants.emax) {
            xe = BMTConstants.emax;
//            System.err.println("Warning: E out of grid... setting it to Emax " + BMTConstants.emax);
        }
        if (xb > BMTConstants.bmax) {
            xb = BMTConstants.bmax;
            //System.err.println("Warning: B field out of grid... setting it to Bmax = "+BMTConstants.bmax);
        }

        int i11 = getBin(xe, xb);
        int i12 = getBin(xe, xb + db);
        int i21 = getBin(xe + de, xb);
        int i22 = getBin(xe + de, xb + db);

        double Q11 = 0;
        double Q12 = 0;
        double Q21 = 0;
        double Q22 = 0;
        double e1 = BMTConstants.emin;
        double e2 = BMTConstants.emax;
        double b1 = 0; // RDV check if it should be 0 or bmin
        double b2 = BMTConstants.bmax;
        if (i11 >= 0) {
            Q11 = BMTConstants.ThetaL_grid[i11];
            e1 = BMTConstants.E_grid[i11];
            b1 = BMTConstants.B_grid[i11];
        }
        if (i12 >= 0) {
            Q12 = BMTConstants.ThetaL_grid[i12];
        }
        if (xb >= BMTConstants.bmin) {
            Q21 = BMTConstants.ThetaL_grid[i21];
        }
        if (xb < BMTConstants.bmin) {
            Q21 = 0;
        }
        if (i22 >= 0) {
            Q22 = BMTConstants.ThetaL_grid[i22];
            e2 = BMTConstants.E_grid[i22];
            b2 = BMTConstants.B_grid[i22];
        }

        double R1 = linInterp(xe, e1, e2, Q11, Q21);
        double R2 = linInterp(xe, e1, e2, Q12, Q22);

        double P = linInterp(xb, b1, b2, R1, R2);

        return P;
    }

    
    public static double linInterp(double x0, double xa, double xb, double ya, double yb) {

        double m = (yb - ya) / (xb - xa);

        // return
        return m * (x0 - xa) + ya;
    }

    
    public static int getBin(double e, double b) {
        double de = (BMTConstants.emax - BMTConstants.emin) / (BMTConstants.Ne - 1);
        double db = (BMTConstants.bmax - BMTConstants.bmin) / (BMTConstants.Nb - 1);

        int ie = (int) Math.floor((e - BMTConstants.emin) / de);
        int ib = (int) Math.floor((b - BMTConstants.bmin) / db);

        return ib + BMTConstants.Nb * ie;
    }
}
