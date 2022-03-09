/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.tracking.kalmanfilter;

/**
 *
 * @author ziegler
 */
public enum Mass {
    UDF(-1), pi(0.13957018), K(0.493677), mu(0.105658369), e(0.000510998), p(0.938272029);
    private final double value;

    Mass(double value) {
        this.value = value;
    }

    public double value() {
        return (double) this.value;
    }

    public static Mass create(byte value) {
        for (Mass hp : Mass.values()) {
            if (hp.value() == value) {
                return hp;
            }
        }
        return UDF;
    }
}