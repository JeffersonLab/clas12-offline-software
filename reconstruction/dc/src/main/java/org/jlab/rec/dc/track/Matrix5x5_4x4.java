package org.jlab.rec.dc.track;

/**
 * @author gavalian
 * @author benkel
 */
public class Matrix5x5_4x4 {
 public static double determinant__00(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(6)>1e-30)
            t0 = A.get(6)*(
                + A.get(12)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(22)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(11)>1e-30)
            t1 = -A.get(11)*(
                + A.get(7)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                );
        //if(A.get(16)>1e-30)
            t2 = + A.get(16)*(
                + A.get(7)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        //if(A.get(21)>1e-30)
            t3 = - A.get(21)*(
                + A.get(7)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(17)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__01(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(5)>1e-30)
            t0 = A.get(5)*(
                + A.get(12)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(22)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(7)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(7)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(7)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(17)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__02(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(5)>1e-30)
            t0 = A.get(5)*(
                + A.get(11)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(21)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(6)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(21)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(6)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(11)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(21)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(6)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(11)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(16)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__03(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(5)>1e-30)
            t0 = A.get(5)*(
                + A.get(11)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                + A.get(21)*(A.get(12)*A.get(19)-A.get(14)*A.get(17))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(6)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(19) - A.get(9)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(6)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                - A.get(11)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(14) - A.get(9)*A.get(12))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(6)*(A.get(12)*A.get(19) - A.get(14)*A.get(17))
                - A.get(11)*(A.get(7)*A.get(19) - A.get(9)*A.get(17))
                + A.get(16)*(A.get(7)*A.get(14) - A.get(9)*A.get(12))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__04(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(5)>1e-30)
            t0 = A.get(5)*(
                + A.get(11)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                + A.get(21)*(A.get(12)*A.get(18)-A.get(13)*A.get(17))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(6)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(18) - A.get(8)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(6)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                - A.get(11)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(13) - A.get(8)*A.get(12))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(6)*(A.get(12)*A.get(18) - A.get(13)*A.get(17))
                - A.get(11)*(A.get(7)*A.get(18) - A.get(8)*A.get(17))
                + A.get(16)*(A.get(7)*A.get(13) - A.get(8)*A.get(12))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__10(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(1)>1e-30)
            t0 = A.get(1)*(
                + A.get(12)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(22)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(11)>1e-30)
            t1 = -A.get(11)*(
                + A.get(2)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(16)>1e-30)
            t2 = + A.get(16)*(
                + A.get(2)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(21)>1e-30)
            t3 = - A.get(21)*(
                + A.get(2)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__11(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(12)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(22)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(2)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(2)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(2)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__12(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(11)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                + A.get(21)*(A.get(13)*A.get(19)-A.get(14)*A.get(18))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(1)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(11)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(11)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(16)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__13(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(11)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                + A.get(21)*(A.get(12)*A.get(19)-A.get(14)*A.get(17))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(1)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                - A.get(11)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(12)*A.get(19) - A.get(14)*A.get(17))
                - A.get(11)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__14(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(11)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                + A.get(21)*(A.get(12)*A.get(18)-A.get(13)*A.get(17))
                );
        //if(A.get(10)>1e-30)
            t1 = -A.get(10)*(
                + A.get(1)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                - A.get(11)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(12)*A.get(18) - A.get(13)*A.get(17))
                - A.get(11)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__20(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(1)>1e-30)
            t0 = A.get(1)*(
                + A.get(7)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(19)-A.get(9)*A.get(18))
                );
        //if(A.get(6)>1e-30)
            t1 = -A.get(6)*(
                + A.get(2)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(16)>1e-30)
            t2 = + A.get(16)*(
                + A.get(2)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(7)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(21)>1e-30)
            t3 = - A.get(21)*(
                + A.get(2)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(7)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__21(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(7)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(19)-A.get(9)*A.get(18))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(2)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(17)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(2)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(7)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(2)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(7)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__22(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(21)*(A.get(8)*A.get(19)-A.get(9)*A.get(18))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(18)*A.get(24) - A.get(19)*A.get(23))
                - A.get(16)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(6)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(6)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(16)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__23(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(19)-A.get(9)*A.get(17))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(17)*A.get(24) - A.get(19)*A.get(22))
                - A.get(16)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                - A.get(6)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(7)*A.get(19) - A.get(9)*A.get(17))
                - A.get(6)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__24(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(18)-A.get(8)*A.get(17))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(17)*A.get(23) - A.get(18)*A.get(22))
                - A.get(16)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                );
        //if(A.get(15)>1e-30)
            t2 = + A.get(15)*(
                + A.get(1)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                - A.get(6)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(7)*A.get(18) - A.get(8)*A.get(17))
                - A.get(6)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__30(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(1)>1e-30)
            t0 = A.get(1)*(
                + A.get(7)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(6)>1e-30)
            t1 = -A.get(6)*(
                + A.get(2)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(11)>1e-30)
            t2 = + A.get(11)*(
                + A.get(2)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(7)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(21)>1e-30)
            t3 = - A.get(21)*(
                + A.get(2)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(7)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(12)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__31(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(7)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(22)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(2)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(12)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(2)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(7)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(22)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(2)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(7)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(12)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__32(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(11)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                + A.get(21)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(13)*A.get(24) - A.get(14)*A.get(23))
                - A.get(11)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(8)*A.get(24) - A.get(9)*A.get(23))
                - A.get(6)*(A.get(3)*A.get(24) - A.get(4)*A.get(23))
                + A.get(21)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(6)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(11)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__33(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                - A.get(11)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(14)-A.get(9)*A.get(12))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(12)*A.get(24) - A.get(14)*A.get(22))
                - A.get(11)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(7)*A.get(24) - A.get(9)*A.get(22))
                - A.get(6)*(A.get(2)*A.get(24) - A.get(4)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(7)*A.get(14) - A.get(9)*A.get(12))
                - A.get(6)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                + A.get(11)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__34(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                - A.get(11)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                + A.get(21)*(A.get(7)*A.get(13)-A.get(8)*A.get(12))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(12)*A.get(23) - A.get(13)*A.get(22))
                - A.get(11)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(7)*A.get(23) - A.get(8)*A.get(22))
                - A.get(6)*(A.get(2)*A.get(23) - A.get(3)*A.get(22))
                + A.get(21)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        //if(A.get(20)>1e-30)
            t3 = - A.get(20)*(
                + A.get(1)*(A.get(7)*A.get(13) - A.get(8)*A.get(12))
                - A.get(6)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                + A.get(11)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__40(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(1)>1e-30)
            t0 = A.get(1)*(
                + A.get(7)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(17)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(6)>1e-30)
            t1 = -A.get(6)*(
                + A.get(2)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(11)>1e-30)
            t2 = + A.get(11)*(
                + A.get(2)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(7)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(16)>1e-30)
            t3 = - A.get(16)*(
                + A.get(2)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(7)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(12)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__41(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(7)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(17)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(2)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(12)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(2)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(7)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(17)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(15)>1e-30)
            t3 = - A.get(15)*(
                + A.get(2)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(7)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(12)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__42(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(11)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                + A.get(16)*(A.get(8)*A.get(14)-A.get(9)*A.get(13))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(13)*A.get(19) - A.get(14)*A.get(18))
                - A.get(11)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(16)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(8)*A.get(19) - A.get(9)*A.get(18))
                - A.get(6)*(A.get(3)*A.get(19) - A.get(4)*A.get(18))
                + A.get(16)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        //if(A.get(15)>1e-30)
            t3 = - A.get(15)*(
                + A.get(1)*(A.get(8)*A.get(14) - A.get(9)*A.get(13))
                - A.get(6)*(A.get(3)*A.get(14) - A.get(4)*A.get(13))
                + A.get(11)*(A.get(3)*A.get(9) - A.get(4)*A.get(8))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__43(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(12)*A.get(19) - A.get(14)*A.get(17))
                - A.get(11)*(A.get(7)*A.get(19) - A.get(9)*A.get(17))
                + A.get(16)*(A.get(7)*A.get(14)-A.get(9)*A.get(12))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(12)*A.get(19) - A.get(14)*A.get(17))
                - A.get(11)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(7)*A.get(19) - A.get(9)*A.get(17))
                - A.get(6)*(A.get(2)*A.get(19) - A.get(4)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        //if(A.get(15)>1e-30)
            t3 = - A.get(15)*(
                + A.get(1)*(A.get(7)*A.get(14) - A.get(9)*A.get(12))
                - A.get(6)*(A.get(2)*A.get(14) - A.get(4)*A.get(12))
                + A.get(11)*(A.get(2)*A.get(9) - A.get(4)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
 public static double determinant__44(Matrix A){
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = 0.0;
        double t3 = 0.0;
        //if(A.get(0)>1e-30)
            t0 = A.get(0)*(
                + A.get(6)*(A.get(12)*A.get(18) - A.get(13)*A.get(17))
                - A.get(11)*(A.get(7)*A.get(18) - A.get(8)*A.get(17))
                + A.get(16)*(A.get(7)*A.get(13)-A.get(8)*A.get(12))
                );
        //if(A.get(5)>1e-30)
            t1 = -A.get(5)*(
                + A.get(1)*(A.get(12)*A.get(18) - A.get(13)*A.get(17))
                - A.get(11)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                );
        //if(A.get(10)>1e-30)
            t2 = + A.get(10)*(
                + A.get(1)*(A.get(7)*A.get(18) - A.get(8)*A.get(17))
                - A.get(6)*(A.get(2)*A.get(18) - A.get(3)*A.get(17))
                + A.get(16)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        //if(A.get(15)>1e-30)
            t3 = - A.get(15)*(
                + A.get(1)*(A.get(7)*A.get(13) - A.get(8)*A.get(12))
                - A.get(6)*(A.get(2)*A.get(13) - A.get(3)*A.get(12))
                + A.get(11)*(A.get(2)*A.get(8) - A.get(3)*A.get(7))
                );
        return t0 + t1 + t2 + t3;
    }
}
