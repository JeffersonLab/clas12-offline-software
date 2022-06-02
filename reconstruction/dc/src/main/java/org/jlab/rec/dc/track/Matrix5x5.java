/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.track;

/**
 *
 * @author gavalian
 */
public class Matrix5x5 {
    public static double determinant(double A[][]){
        double D =
               + A[0][0]*A[1][1]*A[2][2]*A[3][3]*A[4][4] + A[0][0]*A[1][1]*A[2][3]*A[3][4]*A[4][2] + A[0][0]*A[1][1]*A[2][4]*A[3][2]*A[4][3]
- A[0][0]*A[1][1]*A[2][4]*A[3][3]*A[4][2] - A[0][0]*A[1][1]*A[2][3]*A[3][2]*A[4][4] - A[0][0]*A[1][1]*A[2][2]*A[3][4]*A[4][3]
- A[0][0]*A[2][1]*A[1][2]*A[3][3]*A[4][4] - A[0][0]*A[2][1]*A[1][3]*A[3][4]*A[4][2] - A[0][0]*A[2][1]*A[1][4]*A[3][2]*A[4][3]
+ A[0][0]*A[2][1]*A[1][4]*A[3][3]*A[4][2] + A[0][0]*A[2][1]*A[1][3]*A[3][2]*A[4][4] + A[0][0]*A[2][1]*A[1][2]*A[3][4]*A[4][3]
+ A[0][0]*A[3][1]*A[1][2]*A[2][3]*A[4][4] + A[0][0]*A[3][1]*A[1][3]*A[2][4]*A[4][2] + A[0][0]*A[3][1]*A[1][4]*A[2][2]*A[4][3]
- A[0][0]*A[3][1]*A[1][4]*A[2][3]*A[4][2] - A[0][0]*A[3][1]*A[1][3]*A[2][2]*A[4][4] - A[0][0]*A[3][1]*A[1][2]*A[2][4]*A[4][3]
- A[0][0]*A[4][1]*A[1][2]*A[2][3]*A[3][4] - A[0][0]*A[4][1]*A[1][3]*A[2][4]*A[3][2] - A[0][0]*A[4][1]*A[1][4]*A[2][2]*A[3][3]
+ A[0][0]*A[4][1]*A[1][4]*A[2][3]*A[3][2] + A[0][0]*A[4][1]*A[1][3]*A[2][2]*A[3][4] + A[0][0]*A[4][1]*A[1][2]*A[2][4]*A[3][3]

- A[1][0]*A[0][1]*A[2][2]*A[3][3]*A[4][4] - A[1][0]*A[0][1]*A[2][3]*A[3][4]*A[4][2] - A[1][0]*A[0][1]*A[2][4]*A[3][2]*A[4][3]
+ A[1][0]*A[0][1]*A[2][4]*A[3][3]*A[4][2] - A[1][0]*A[0][1]*A[2][3]*A[3][2]*A[4][4] - A[1][0]*A[0][1]*A[2][2]*A[3][4]*A[4][3]
+ A[1][0]*A[2][1]*A[0][2]*A[3][3]*A[4][4] + A[1][0]*A[2][1]*A[0][3]*A[3][4]*A[4][2] + A[1][0]*A[2][1]*A[0][4]*A[3][2]*A[4][3]
- A[1][0]*A[2][1]*A[0][4]*A[3][3]*A[4][2] - A[1][0]*A[2][1]*A[0][3]*A[3][2]*A[4][4] - A[1][0]*A[2][1]*A[0][2]*A[3][4]*A[4][3]
- A[1][0]*A[3][1]*A[0][2]*A[2][3]*A[4][4] - A[1][0]*A[3][1]*A[0][3]*A[2][4]*A[4][2] - A[1][0]*A[3][1]*A[0][4]*A[2][2]*A[4][3]
+ A[1][0]*A[3][1]*A[0][4]*A[2][3]*A[4][2] + A[1][0]*A[3][1]*A[0][3]*A[2][2]*A[4][4] + A[1][0]*A[3][1]*A[0][2]*A[2][4]*A[4][3]
+ A[1][0]*A[4][1]*A[0][2]*A[2][3]*A[3][4] + A[1][0]*A[4][1]*A[0][3]*A[2][4]*A[3][2] + A[1][0]*A[4][1]*A[0][4]*A[2][2]*A[3][3]
- A[1][0]*A[4][1]*A[0][4]*A[2][3]*A[3][2] - A[1][0]*A[4][1]*A[0][3]*A[2][2]*A[3][4] - A[1][0]*A[4][1]*A[0][2]*A[2][4]*A[3][3]

+ A[2][0]*A[0][1]*A[1][2]*A[3][3]*A[4][4] + A[2][0]*A[0][1]*A[1][3]*A[3][4]*A[4][2] + A[2][0]*A[0][1]*A[1][4]*A[3][2]*A[4][3]
- A[2][0]*A[0][1]*A[1][4]*A[3][3]*A[4][2] - A[2][0]*A[0][1]*A[1][3]*A[3][2]*A[4][4] - A[2][0]*A[0][1]*A[1][2]*A[3][4]*A[4][3]
- A[2][0]*A[1][1]*A[0][2]*A[3][3]*A[4][4] - A[2][0]*A[1][1]*A[0][3]*A[3][4]*A[4][2] - A[2][0]*A[1][1]*A[0][4]*A[3][2]*A[4][3]
- A[2][0]*A[1][1]*A[0][4]*A[3][3]*A[4][2] - A[2][0]*A[1][1]*A[0][3]*A[3][2]*A[4][4] - A[2][0]*A[1][1]*A[0][2]*A[3][4]*A[4][3]
+ A[2][0]*A[3][1]*A[0][2]*A[1][3]*A[4][4] + A[2][0]*A[3][1]*A[0][3]*A[1][4]*A[4][2] + A[2][0]*A[3][1]*A[0][4]*A[1][2]*A[4][3]
- A[2][0]*A[3][1]*A[0][4]*A[1][3]*A[4][2] - A[2][0]*A[3][1]*A[0][3]*A[1][2]*A[4][4] - A[2][0]*A[3][1]*A[0][2]*A[1][4]*A[4][3]
- A[2][0]*A[4][1]*A[0][2]*A[1][3]*A[3][4] - A[2][0]*A[4][1]*A[0][3]*A[1][4]*A[3][2] - A[2][0]*A[4][1]*A[0][4]*A[1][2]*A[3][3]
+ A[2][0]*A[4][1]*A[0][4]*A[1][3]*A[3][2] + A[2][0]*A[4][1]*A[0][3]*A[1][2]*A[3][4] + A[2][0]*A[4][1]*A[0][2]*A[1][4]*A[3][3];

        return D;
    }

    public static void transpose(double A[][], double T[][]){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                T[j][i] = A[i][j];
            }
        }
    }

    public static void add(double A[][], double B[][], double C[][]){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                C[i][j] = A[i][j] + B[i][j];
            }
        }
    }

    public static double inverseOLD(double A[][], double I[][]){
        //Matrix5x5.transpose(A, I);
        double det = Matrix5x5.determinant(A);
        if(Math.abs(det)<1e-30) return det;
        double factor = 1.0/det;
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                I[i][j] = A[j][i]*factor;
            }
        }
        return det;
    }

    public static void copy(double A[][], double B[][]){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                B[i][j] = A[i][j];
            }
        }
    }

    public static void copy(Matrix A, Matrix B){
        Matrix5x5.copy(A.M, B.M);
    }

    public static double det(Matrix m){
        return Matrix5x5.determinant(m.M);
    }

    public static void   add(Matrix a, Matrix b, Matrix c){
        Matrix5x5.add(a.M, b.M, c.M);
    }
    public static double inverse(double A[][], double B[][], double adj[][]){

        double det = 0.0;


        adj[0][0] =  Matrix5x5_4x4.determinant__00(A);
        adj[0][1] = -Matrix5x5_4x4.determinant__01(A);
        adj[0][2] =  Matrix5x5_4x4.determinant__02(A);
        adj[0][3] = -Matrix5x5_4x4.determinant__03(A);
        adj[0][4] =  Matrix5x5_4x4.determinant__04(A);

        adj[1][0] = -Matrix5x5_4x4.determinant__10(A);
        adj[1][1] =  Matrix5x5_4x4.determinant__11(A);
        adj[1][2] = -Matrix5x5_4x4.determinant__12(A);
        adj[1][3] =  Matrix5x5_4x4.determinant__13(A);
        adj[1][4] = -Matrix5x5_4x4.determinant__14(A);

        adj[2][0] =  Matrix5x5_4x4.determinant__20(A);
        adj[2][1] = -Matrix5x5_4x4.determinant__21(A);
        adj[2][2] =  Matrix5x5_4x4.determinant__22(A);
        adj[2][3] = -Matrix5x5_4x4.determinant__23(A);
        adj[2][4] =  Matrix5x5_4x4.determinant__24(A);

        adj[3][0] = -Matrix5x5_4x4.determinant__30(A);
        adj[3][1] =  Matrix5x5_4x4.determinant__31(A);
        adj[3][2] = -Matrix5x5_4x4.determinant__32(A);
        adj[3][3] =  Matrix5x5_4x4.determinant__33(A);
        adj[3][4] = -Matrix5x5_4x4.determinant__34(A);

        adj[4][0] =  Matrix5x5_4x4.determinant__40(A);
        adj[4][1] = -Matrix5x5_4x4.determinant__41(A);
        adj[4][2] =  Matrix5x5_4x4.determinant__42(A);
        adj[4][3] = -Matrix5x5_4x4.determinant__43(A);
        adj[4][4] =  Matrix5x5_4x4.determinant__44(A);

        for(int i = 0; i < 5; i++){
            det += A[i][0]*adj[i][0];
        }

        double factor = 1.0/det;

        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                B[i][j] = factor*adj[j][i];
            }
        }
        return det;
    }
    public static double inverse(Matrix source, Matrix inv, Matrix adj){
        double det = Matrix5x5.inverse(source.M, inv.M,adj.M);
        return det;
    }
}
