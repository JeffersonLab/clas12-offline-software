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
        double de = (Constants.emax - Constants.emin) / (Constants.Ne - 1);
        double db = (Constants.bmax - Constants.bmin) / (Constants.Nb - 1);

        if (xe < Constants.emin) {
            xe = Constants.emin;
            System.err.println("Warning: E out of grid... setting it to Emin");
        }
        if (xe >= Constants.emax) {
            xe = Constants.emax;
            System.err.println("Warning: E out of grid... setting it to Emax");
        }
        if (xb > Constants.bmax) {
            xb = Constants.bmax;
            //System.err.println("Warning: B field out of grid... setting it to Bmax = "+Constants.bmax);
        }

        int i11 = getBin(xe, xb);
        int i12 = getBin(xe, xb + db);
        int i21 = getBin(xe + de, xb);
        int i22 = getBin(xe + de, xb + db);

        double Q11 = 0;
        double Q12 = 0;
        double Q21 = 0;
        double Q22 = 0;
        double e1 = Constants.emin;
        double e2 = Constants.emax;
        double b1 = 0; // RDV check if it should be 0 or bmin
        double b2 = Constants.bmax;
        if (i11 >= 0) {
            Q11 = Constants.ThetaL_grid[i11];
            e1 = Constants.E_grid[i11];
            b1 = Constants.B_grid[i11];
        }
        if (i12 >= 0) {
            Q12 = Constants.ThetaL_grid[i12];
        }
        if (xb >= Constants.bmin) {
            Q21 = Constants.ThetaL_grid[i21];
        }
        if (xb < Constants.bmin) {
            Q21 = 0;
        }
        if (i22 >= 0) {
            Q22 = Constants.ThetaL_grid[i22];
            e2 = Constants.E_grid[i22];
            b2 = Constants.B_grid[i22];
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
        double de = (Constants.emax - Constants.emin) / (Constants.Ne - 1);
        double db = (Constants.bmax - Constants.bmin) / (Constants.Nb - 1);

        int ie = (int) Math.floor((e - Constants.emin) / de);
        int ib = (int) Math.floor((b - Constants.bmin) / db);

        return ib + Constants.Nb * ie;
    }
}
