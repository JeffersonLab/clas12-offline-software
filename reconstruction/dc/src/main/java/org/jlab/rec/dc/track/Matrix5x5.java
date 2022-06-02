package org.jlab.rec.dc.track;

/**
 * @author gavalian
 * @author benkel
 */
public class Matrix5x5 {
    public static double determinant(Matrix A){
        double D =
    + A.get(0)*A.get(6)*A.get(12)*A.get(18)*A.get(24) + A.get(0)*A.get(6)*A.get(13)*A.get(19)*A.get(22) + A.get(0)*A.get(6)*A.get(14)*A.get(17)*A.get(23)
    - A.get(0)*A.get(6)*A.get(14)*A.get(18)*A.get(22) - A.get(0)*A.get(6)*A.get(13)*A.get(17)*A.get(24) - A.get(0)*A.get(6)*A.get(12)*A.get(19)*A.get(23)
    - A.get(0)*A.get(11)*A.get(7)*A.get(18)*A.get(24) - A.get(0)*A.get(11)*A.get(8)*A.get(19)*A.get(22) - A.get(0)*A.get(11)*A.get(9)*A.get(17)*A.get(23)
    + A.get(0)*A.get(11)*A.get(9)*A.get(18)*A.get(22) + A.get(0)*A.get(11)*A.get(8)*A.get(17)*A.get(24) + A.get(0)*A.get(11)*A.get(7)*A.get(19)*A.get(23)
    + A.get(0)*A.get(16)*A.get(7)*A.get(13)*A.get(24) + A.get(0)*A.get(16)*A.get(8)*A.get(14)*A.get(22) + A.get(0)*A.get(16)*A.get(9)*A.get(12)*A.get(23)
    - A.get(0)*A.get(16)*A.get(9)*A.get(13)*A.get(22) - A.get(0)*A.get(16)*A.get(8)*A.get(12)*A.get(24) - A.get(0)*A.get(16)*A.get(7)*A.get(14)*A.get(23)
    - A.get(0)*A.get(21)*A.get(7)*A.get(13)*A.get(19) - A.get(0)*A.get(21)*A.get(8)*A.get(14)*A.get(17) - A.get(0)*A.get(21)*A.get(9)*A.get(12)*A.get(18)
    + A.get(0)*A.get(21)*A.get(9)*A.get(13)*A.get(17) + A.get(0)*A.get(21)*A.get(8)*A.get(12)*A.get(19) + A.get(0)*A.get(21)*A.get(7)*A.get(14)*A.get(18)

    - A.get(5)*A.get(1)*A.get(12)*A.get(18)*A.get(24) - A.get(5)*A.get(1)*A.get(13)*A.get(19)*A.get(22) - A.get(5)*A.get(1)*A.get(14)*A.get(17)*A.get(23)
    + A.get(5)*A.get(1)*A.get(14)*A.get(18)*A.get(22) - A.get(5)*A.get(1)*A.get(13)*A.get(17)*A.get(24) - A.get(5)*A.get(1)*A.get(12)*A.get(19)*A.get(23)
    + A.get(5)*A.get(11)*A.get(2)*A.get(18)*A.get(24) + A.get(5)*A.get(11)*A.get(3)*A.get(19)*A.get(22) + A.get(5)*A.get(11)*A.get(4)*A.get(17)*A.get(23)
    - A.get(5)*A.get(11)*A.get(4)*A.get(18)*A.get(22) - A.get(5)*A.get(11)*A.get(3)*A.get(17)*A.get(24) - A.get(5)*A.get(11)*A.get(2)*A.get(19)*A.get(23)
    - A.get(5)*A.get(16)*A.get(2)*A.get(13)*A.get(24) - A.get(5)*A.get(16)*A.get(3)*A.get(14)*A.get(22) - A.get(5)*A.get(16)*A.get(4)*A.get(12)*A.get(23)
    + A.get(5)*A.get(16)*A.get(4)*A.get(13)*A.get(22) + A.get(5)*A.get(16)*A.get(3)*A.get(12)*A.get(24) + A.get(5)*A.get(16)*A.get(2)*A.get(14)*A.get(23)
    + A.get(5)*A.get(21)*A.get(2)*A.get(13)*A.get(19) + A.get(5)*A.get(21)*A.get(3)*A.get(14)*A.get(17) + A.get(5)*A.get(21)*A.get(4)*A.get(12)*A.get(18)
    - A.get(5)*A.get(21)*A.get(4)*A.get(13)*A.get(17) - A.get(5)*A.get(21)*A.get(3)*A.get(12)*A.get(19) - A.get(5)*A.get(21)*A.get(2)*A.get(14)*A.get(18)

    + A.get(10)*A.get(1)*A.get(7)*A.get(18)*A.get(24) + A.get(10)*A.get(1)*A.get(8)*A.get(19)*A.get(22) + A.get(10)*A.get(1)*A.get(9)*A.get(17)*A.get(23)
    - A.get(10)*A.get(1)*A.get(9)*A.get(18)*A.get(22) - A.get(10)*A.get(1)*A.get(8)*A.get(17)*A.get(24) - A.get(10)*A.get(1)*A.get(7)*A.get(19)*A.get(23)
    - A.get(10)*A.get(6)*A.get(2)*A.get(18)*A.get(24) - A.get(10)*A.get(6)*A.get(3)*A.get(19)*A.get(22) - A.get(10)*A.get(6)*A.get(4)*A.get(17)*A.get(23)
    - A.get(10)*A.get(6)*A.get(4)*A.get(18)*A.get(22) - A.get(10)*A.get(6)*A.get(3)*A.get(17)*A.get(24) - A.get(10)*A.get(6)*A.get(2)*A.get(19)*A.get(23)
    + A.get(10)*A.get(16)*A.get(2)*A.get(8)*A.get(24) + A.get(10)*A.get(16)*A.get(3)*A.get(9)*A.get(22) + A.get(10)*A.get(16)*A.get(4)*A.get(7)*A.get(23)
    - A.get(10)*A.get(16)*A.get(4)*A.get(8)*A.get(22) - A.get(10)*A.get(16)*A.get(3)*A.get(7)*A.get(24) - A.get(10)*A.get(16)*A.get(2)*A.get(9)*A.get(23)
    - A.get(10)*A.get(21)*A.get(2)*A.get(8)*A.get(19) - A.get(10)*A.get(21)*A.get(3)*A.get(9)*A.get(17) - A.get(10)*A.get(21)*A.get(4)*A.get(7)*A.get(18)
    + A.get(10)*A.get(21)*A.get(4)*A.get(8)*A.get(17) + A.get(10)*A.get(21)*A.get(3)*A.get(7)*A.get(19) + A.get(10)*A.get(21)*A.get(2)*A.get(9)*A.get(18);

        return D;
    }

    public static void transpose(Matrix A, Matrix T){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                T.set(j, i, A.get(i,j));
            }
        }
    }

    public static void add(Matrix A, Matrix B, Matrix C){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                C.set(i, j, A.get(i,j) + B.get(i,j));
            }
        }
    }

    public static void copy(Matrix A, Matrix B){
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                B.set(i, j, A.get(i,j));
            }
        }
    }

    public static double det(Matrix M){
        return Matrix5x5.determinant(M);
    }

    public static double inverse(Matrix A, Matrix B, Matrix adj){
        double det = 0.0;

        adj.set(0, 0,  Matrix5x5_4x4.determinant__00(A));
        adj.set(0, 1, -Matrix5x5_4x4.determinant__01(A));
        adj.set(0, 2,  Matrix5x5_4x4.determinant__02(A));
        adj.set(0, 3, -Matrix5x5_4x4.determinant__03(A));
        adj.set(0, 4,  Matrix5x5_4x4.determinant__04(A));

        adj.set(1, 0, -Matrix5x5_4x4.determinant__10(A));
        adj.set(1, 1,  Matrix5x5_4x4.determinant__11(A));
        adj.set(1, 2, -Matrix5x5_4x4.determinant__12(A));
        adj.set(1, 3,  Matrix5x5_4x4.determinant__13(A));
        adj.set(1, 4, -Matrix5x5_4x4.determinant__14(A));

        adj.set(2, 0,  Matrix5x5_4x4.determinant__20(A));
        adj.set(2, 1, -Matrix5x5_4x4.determinant__21(A));
        adj.set(2, 2,  Matrix5x5_4x4.determinant__22(A));
        adj.set(2, 3, -Matrix5x5_4x4.determinant__23(A));
        adj.set(2, 4,  Matrix5x5_4x4.determinant__24(A));

        adj.set(3, 0, -Matrix5x5_4x4.determinant__30(A));
        adj.set(3, 1,  Matrix5x5_4x4.determinant__31(A));
        adj.set(3, 2, -Matrix5x5_4x4.determinant__32(A));
        adj.set(3, 3,  Matrix5x5_4x4.determinant__33(A));
        adj.set(3, 4, -Matrix5x5_4x4.determinant__34(A));

        adj.set(4, 0,  Matrix5x5_4x4.determinant__40(A));
        adj.set(4, 1, -Matrix5x5_4x4.determinant__41(A));
        adj.set(4, 2,  Matrix5x5_4x4.determinant__42(A));
        adj.set(4, 3, -Matrix5x5_4x4.determinant__43(A));
        adj.set(4, 4,  Matrix5x5_4x4.determinant__44(A));

        for(int i = 0; i < 5; i++){
            det += A.get(i,0)*adj.get(i,0);
        }

        double factor = 1.0/det;

        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 5; j++){
                B.set(i, j, factor*adj.get(j,i));
            }
        }
        return det;
    }
}
