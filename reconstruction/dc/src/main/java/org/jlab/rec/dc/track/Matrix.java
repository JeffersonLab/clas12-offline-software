package org.jlab.rec.dc.track;

/**
 * @author gavalian
 * @author benkel
 */
public class Matrix {
    protected double M[] = new double[25];

    public Matrix() {}

    public double get(int i, int j){
        return M[5*i + j];
    }

    public double get(int i) {
        return M[i];
    }

    public void set(int i, int j, double v) {
        M[5*i + j] = v;
    }

    public void reset() {
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                M[5*i + j] = 0.0;
            }
        }
    }

    public void set(double A[][]) {
       for(int i = 0; i < 5; i++) {
           for(int j = 0; j < 5; j++) {
               M[5*i + j] = A[i][j];
           }
       }
    }

    public void set(double... values) {
        if (values.length < 25) {
            System.out.println("[jnp::matrix] error *** number of arguments = " + values.length
                               + " , expected 25" );
        }
        else {
            int count = 0;
            for(int j = 0; j < 5; j++){
                for(int i = 0; i < 5; i++){
                    M[5*i + j] = values[count]; count++;
                }
            }
        }
    }
}
