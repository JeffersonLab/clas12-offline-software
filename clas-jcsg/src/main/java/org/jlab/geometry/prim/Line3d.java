/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

/**
 *
 * @author kenjo
 */
public class Line3d extends Straight {

    public Line3d(Vector3d origin, Vector3d end) {
        super(origin, end);
    }

    public Line3d(Straight line) {
        super(line);
    }

    @Override
    public boolean contains(double parametricT) {
        return true;
    }

    public Line3d transformed(Transform trans) {
        transform(trans);
        return this;
    }
}

