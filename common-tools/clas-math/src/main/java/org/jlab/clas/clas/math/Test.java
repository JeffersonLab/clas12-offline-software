/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.clas.math;

/**
 *
 * @author ziegler
 */
public class Test {
    
    public static void main(String[] args) {
        for(int i =0; i<100; i++) {
            double x = -100+(double)(i+1);
            for(int j =0; j<100; j++) {
                double y = -100+(double)(j+1);
                System.out.println("angle "+(float)Math.toDegrees(Math.atan2(y, x))+"  "
                        +"I1-D "+(Icecore.atan2((float)y, (float)x)-org.apache.commons.math3.util.FastMath.atan2(y, x))+"  "
                        +"I2-D "+(Icecore2.atan2((float)y, (float)x)-org.apache.commons.math3.util.FastMath.atan2(y, x))+"  "
                        +"K-D "+(Kappa.atan2((float)y, (float)x)-org.apache.commons.math3.util.FastMath.atan2(y, x))
                );
            }
        }
    }
    
}
