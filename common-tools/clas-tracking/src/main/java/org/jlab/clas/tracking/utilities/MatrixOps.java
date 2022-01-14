package org.jlab.clas.tracking.utilities;
 
/**
 *
 * @author ziegler
 */
public class MatrixOps {
    
    public MatrixOps(Libr l) {
        this.libr = l;
    }
    
    public double[][] ConversionToArray(Object obj) {
        double[][] result = null; 
        if(obj instanceof double[][]) {
            result = (double[][]) obj;
            return result;
            } else {
                if(this.libr == Libr.JAMA) {
                    if (!(obj instanceof Jama.Matrix)) { 
                        return null; 
                    } else {
                        Jama.Matrix m = (Jama.Matrix) obj;
                        result = m.getArrayCopy();
                    }
                }
                if(this.libr == Libr.JNP) {
                    if (!(obj instanceof org.jlab.jnp.matrix.Matrix)) { 
                        return null; 
                    } else {
                        result = new double[5][5];
                        org.jlab.jnp.matrix.Matrix m = (org.jlab.jnp.matrix.Matrix) obj;
                        for(int r =0; r<5; r++) {
                            for(int c =0; c<5; c++) {
                                result[r][c] = m.get(r,c); 
                            }
                        }
                    }
                }
                if(this.libr == Libr.APA) {
                    if (!(obj instanceof org.apache.commons.math3.linear.RealMatrix)) { 
                        return null; 
                    } else {
                        org.apache.commons.math3.linear.RealMatrix m = (org.apache.commons.math3.linear.RealMatrix) obj;
                        result = m.getData();
                    }
                }
                if(this.libr == Libr.EJML) {
                    if (!(obj instanceof org.ejml.simple.SimpleMatrix)) { 
                        return null; 
                    } else {
                        org.ejml.simple.SimpleMatrix m = (org.ejml.simple.SimpleMatrix) obj;
                        result = new double[m.numRows()][m.numCols()];
                        for(int r =0; r<m.numRows(); r++) {
                            for(int c =0; c<m.numCols(); c++) {
                                result[r][c] = m.get(r,c);
                            }
                        }
                    }
                }
            return result;
        }
    }
    public double[][] MatrixAddition(Object obj1, Object obj2) {
        double[][] arr1 = this.ConversionToArray(obj1);
        double[][] arr2 = this.ConversionToArray(obj2);
        double[][] result = null;
        if(arr1==null || arr2==null)
            return null;
        if(arr1.length!=arr2.length || arr1[0].length!=arr2[0].length)
            return null;
        result = new double[arr1.length][arr1[0].length];
        for(int r = 0; r< arr1.length; r++) {
            for(int c = 0; c< arr1[0].length; c++) {
                result[r][c] = arr1[r][c]+arr2[r][c];
            }
        }
        arr1 = null;
        arr2 = null;
                
        return result;
    }
    public double[][] MatrixSubtraction(Object obj1, Object obj2) {
        double[][] arr1 = this.ConversionToArray(obj1);
        double[][] arr2 = this.ConversionToArray(obj2);
        double[][] result = null;
        if(arr1==null || arr2==null)
            return null;
        if(arr1.length!=arr2.length || arr1[0].length!=arr2[0].length)
            return null;
        result = new double[arr1.length][arr1[0].length];
        for(int r = 0; r< arr1.length; r++) {
            for(int c = 0; c< arr1[0].length; c++) {
                result[r][c] = arr1[r][c]-arr2[r][c];
            }
        }
        arr1 = null;
        arr2 = null;
                
        return result;
    }
    public double[][] MatrixMultiplication(Object obj1, Object obj2) {
        double[][] arr1 = this.ConversionToArray(obj1);
        double[][] arr2 = this.ConversionToArray(obj2);
       
        if(arr1==null || arr2==null)
            return null;
        int r1 =arr1.length; int c1=arr1[0].length; int c2=arr2[0].length;
        double[][] product = new double[r1][c2];
        for(int i = 0; i < r1; i++) {
            for (int j = 0; j < c2; j++) {
                for (int k = 0; k < c1; k++) {
                    product[i][j] += arr1[i][k] * arr2[k][j];
                }
            }
        }
        arr1 = null;
        arr2 = null;
          
        return product;
    }
    public double[][] MatrixTranspose(Object obj1) {
        double[][] arr1 = this.ConversionToArray(obj1);
        if(arr1==null)
            return null;
        int r =arr1.length; int c=arr1[0].length; 
        double[][] result = new double[c][r];

        for(int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                result[i][j] = arr1[j][i];
            }
        }
        arr1 = null;
          
        return result;
    }
    public double[][] MatrixInversion(double[][] C){
        double[][] result = null;
        
        if(this.libr == Libr.JAMA) 
            result = MatrixInversionJAMA(C);
        if(this.libr == Libr.JNP) 
            result = MatrixInversionJNP(C);
        if(this.libr == Libr.APA) 
            result = MatrixInversionAPA(C);
        if(this.libr == Libr.EJML) 
            result = MatrixInversionEJML(C);
        return result;
    }
    
    public double[][] MatrixInversionJAMA(double[][] Carr){
        Jama.Matrix Ci = null;
        Jama.Matrix C = new Jama.Matrix(Carr);
        if(Math.abs(C.det()) < 1.e-30) {
            return null;
        }
        try {
            Ci = C.inverse();
        } catch (Exception e) {
            return null;
        }
        C = null;
        
        return this.ConversionToArray(Ci);
    }
    public double[][] MatrixInversionJNP(double[][] Carr){
        org.jlab.jnp.matrix.Matrix adj = new org.jlab.jnp.matrix.Matrix();
        org.jlab.jnp.matrix.Matrix Ci = new org.jlab.jnp.matrix.Matrix();
        org.jlab.jnp.matrix.Matrix C = new org.jlab.jnp.matrix.Matrix();
        C.set(Carr);
        double det = org.jlab.jnp.matrix.Matrix5x5.inverse(C, Ci, adj);
        
        if(Math.abs(det)<1.e-30)
            return null;
        adj = null;
        C = null;
        
        return this.ConversionToArray(Ci);
        
    }
    public double[][] MatrixInversionAPA(double[][] C){
        org.apache.commons.math3.linear.RealMatrix m = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(C);
        org.apache.commons.math3.linear.RealMatrix Ci = new org.apache.commons.math3.linear.LUDecomposition(m).getSolver().getInverse();
        m = null;
        
        return this.ConversionToArray(Ci);
        
    }
    public double[][] MatrixInversionEJML(double[][] C){
        if(C==null)
            return null;
        org.ejml.simple.SimpleMatrix m = new org.ejml.simple.SimpleMatrix(C);
        org.ejml.simple.SimpleMatrix Ci = m.invert();
        m = null;
        
        return this.ConversionToArray(Ci);
        
    }
    
    public Libr libr;
    
    public enum Libr {
        UDF(-1), JAMA(0), JNP(1), APA(2), EJML(3);
        private final int value;

        Libr(int value) {
            this.value = value;
        }

        public byte value() {
            return (byte) this.value;
        }
    }
    
    
    /**
     * prints the matrix -- used for debugging
     *
     * @param mat matrix
     * @param message
     */
    public static void printMatrix(double[][] mat, String message) {
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
