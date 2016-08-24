/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.units;

/**
 *
 * @author kenjo
 */
public final class Measurement {

    public double value;
    public String unit;

    public Measurement(double value, String unit) {
        this.value = value;
        this.unit = unit;
    }
}
