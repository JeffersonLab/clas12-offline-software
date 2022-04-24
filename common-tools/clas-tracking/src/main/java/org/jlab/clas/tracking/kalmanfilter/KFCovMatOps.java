package org.jlab.clas.tracking.kalmanfilter;

import org.jlab.clas.tracking.utilities.MatrixOps;
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;

/**
 *
 * @author ziegler
 */
public class KFCovMatOps{
    
    public MatrixOps mo;
    public KFCovMatOps(Libr l) {
        mo = new MatrixOps(l);
    }
    
    public Libr getMatrixLibrary(){
        return mo.libr;
    }
    
    public double[][] filterCovMat(double[] H, double[][] Carr, double V) {
        double[][] HTGH = new double[][]{
                {H[0] * H[0] / V, H[0] * H[1] / V, H[0] * H[2] / V, H[0] * H[3] / V, H[0] * H[4] / V},
                {H[1] * H[0] / V, H[1] * H[1] / V, H[1] * H[2] / V, H[1] * H[3] / V, H[1] * H[4] / V},
                {H[2] * H[0] / V, H[2] * H[1] / V, H[2] * H[2] / V, H[2] * H[3] / V, H[2] * H[4] / V},
                {H[3] * H[0] / V, H[3] * H[1] / V, H[3] * H[2] / V, H[3] * H[3] / V, H[3] * H[4] / V},
                {H[4] * H[0] / V, H[4] * H[1] / V, H[4] * H[2] / V, H[4] * H[3] / V, H[4] * H[4] / V}
            };
        
        double[][] Ci = null;
        try {
            Ci = mo.MatrixInversion(Carr);
        } catch (Exception e) {
            return null;
        }
        double[][] Ca = null;
        try {
            Ca = mo.MatrixAddition(Ci, HTGH);
        } catch (Exception e) {
            return null;
        }
        double[][] Cinv = null;
        try {
            Cinv = mo.MatrixInversion(Ca);
        } catch (Exception e) {
            return null;
        }
        return Cinv;
    }
        
    public double[][] smoothCovMat(double[][] C_n_kp1, double[][] C_k, double[][] A , double[][] C_k_kp1) {
        double[][] At = null;
        try {
            At = mo.MatrixTranspose(A);
        } catch (Exception e) {
            return null;
        }
        double[][] dC = null;
        try {
            dC = mo.MatrixSubtraction(C_n_kp1, C_k_kp1);
        } catch (Exception e) {
            return null;
        }
        double[][] Ca = null;
        try {
            Ca = mo.MatrixMultiplication(dC, At);
        } catch (Exception e) {
            return null;
        }
        double[][] Caa = null;
        try {
            Caa = mo.MatrixMultiplication(A, Ca);
        } catch (Exception e) {
            return null;
        }
        double[][] Cn = null;
        try {
            Cn = mo.MatrixAddition(C_k, Caa);
        } catch (Exception e) {
            return null;
        }
        
        return Cn;
    }
 
    public double[][] smoothingCorr(double[][] C_k, double[][] FMatT , double[][] C_k_kp1) {
        double[][] Ci = null;
        try {
            Ci = mo.MatrixInversion(C_k_kp1);
        } catch (Exception e) {
            return null;
        }
        double[][] Ca = null;
        try {
            Ca = mo.MatrixMultiplication(C_k, FMatT);
        } catch (Exception e) {
            return null;
        }
        double[][] A = null;
        try {
            A = mo.MatrixMultiplication(Ca, Ci);
        } catch (Exception e) {
            return null;
        }
        return A;
    }
    
    public double[][] inverse(double[][] C) {
        double[][] Ci = null;
        try {
            Ci = mo.MatrixInversion(C);
        } catch (Exception e) {
            return null;
        }
        return Ci;
    }
 
    /**
     * prints the matrix -- used for debugging
     *
     * @param mat matrix
     * @param message
     */
    public void printMatrix(double[][] mat, String message) {
        int nrow = mat.length; 
        int ncol = mat[0].length; 

        System.out.println("\t" + message);
        for (int ir = 0; ir < nrow; ir++) {
            for (int ic = 0; ic < ncol; ic++) {
                System.out.print("\t" + mat[ir][ic]);
            }
            System.out.print("\n");
        }
        System.out.println();
    }
}
