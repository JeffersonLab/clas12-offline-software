/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.clas.math;

/**
 * KAPPA  WEBSITE
 * http://www.java-gaming.org/topics/extremely-fast-atan2/36467/msg/346112/view.html#msg346112
 */
public final class Kappa {

    private static final float PI = 3.1415927f;
    private static final float PI_2 = PI / 2f;
    private static final float MINUS_PI_2 = -PI_2;

    public static final float atan2(float y, float x) {
        if (x == 0.0f) {
            if (y > 0.0f) {
                return PI_2;
            }
            if (y == 0.0f) {
                return 0.0f;
            }
            return MINUS_PI_2;
        }

        final float atan;
        final float z = y / x;
        if (Math.abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28f * z * z);
            if (x < 0.0f) {
                return (y < 0.0f) ? atan - PI : atan + PI;
            }
            return atan;
        } else {
            atan = PI_2 - z / (z * z + 0.28f);
            return (y < 0.0f) ? atan - PI : atan;
        }
    }
}