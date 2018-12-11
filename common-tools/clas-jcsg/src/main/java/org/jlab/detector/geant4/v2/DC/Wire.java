/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2.DC;

import eu.mihosoft.vrl.v3d.Vector3d;

/**
 *
 * @author kenjo
 */

public final class Wire {

    private final int ireg;
    private final DCdatabase dbref = DCdatabase.getInstance();

    private final Vector3d midpoint;
    private final Vector3d center;
    private final Vector3d direction;
    private Vector3d leftend;
    private Vector3d rightend;

    public Wire translate(Vector3d vshift) {
        midpoint.add(vshift);
        center.add(vshift);
        leftend.add(vshift);
        rightend.add(vshift);

        return this;
    }

    public Wire rotateX(double rotX) {
        midpoint.rotateX(rotX);
        center.rotateX(rotX);
        direction.rotateX(rotX);
        leftend.rotateX(rotX);
        rightend.rotateX(rotX);

        return this;
    }

    public Wire rotateY(double rotY) {
        midpoint.rotateY(rotY);
        center.rotateY(rotY);
        direction.rotateY(rotY);
        leftend.rotateY(rotY);
        rightend.rotateY(rotY);

        return this;
    }

    public Wire rotateZ(double rotZ) {
        midpoint.rotateZ(rotZ);
        center.rotateZ(rotZ);
        direction.rotateZ(rotZ);
        leftend.rotateZ(rotZ);
        rightend.rotateZ(rotZ);

        return this;
    }

    private void findEnds() {
        // define vector from wire midpoint to chamber tip (z is wrong!!)
        Vector3d vnum = new Vector3d(0, dbref.xdist(ireg), 0);
        vnum.sub(midpoint);

        double copen = Math.cos(dbref.thopen(ireg) / 2.0);
        double sopen = Math.sin(dbref.thopen(ireg) / 2.0);

        // define unit vector normal to the sides of the chamber and pointing inside
        Vector3d rnorm = new Vector3d(copen, sopen, 0);
        Vector3d lnorm = new Vector3d(-copen, sopen, 0);

        double wlenl = vnum.dot(lnorm) / direction.dot(lnorm);
        leftend = direction.times(wlenl).add(midpoint);

        double wlenr = vnum.dot(rnorm) / direction.dot(rnorm);
        rightend = direction.times(wlenr).add(midpoint);
    }

    public Wire(int isuper, int ilayer, int iwire) {
        this.ireg = isuper / 2;

        // calculate first-wire distance from target
        double w2tgt = dbref.dist2tgt(ireg);
        if (isuper % 2 > 0) {
            w2tgt += dbref.superwidth(isuper - 1) + dbref.midgap(ireg);
        }
        w2tgt /= Math.cos(dbref.thtilt(ireg) - dbref.thmin(isuper));

        // y0 and z0 in the lab for the first wire of the layer
        double y0mid = w2tgt * Math.sin(dbref.thmin(isuper));
        double z0mid = w2tgt * Math.cos(dbref.thmin(isuper));

        double cster = Math.cos(dbref.thster(isuper));
        double ctilt = Math.cos(dbref.thtilt(ireg));
        double stilt = Math.sin(dbref.thtilt(ireg));

        double dw = 4 * Math.cos(Math.toRadians(30)) * dbref.wpdist(isuper);
        double dw2 = dw / cster;

        // hh: wire distance in the wire plane
        double hh = (iwire-1 + ((double)(ilayer % 2)) / 2.0) * dw2;
        if(ireg==2 && isSensitiveWire(isuper, ilayer, iwire) && dbref.getMinistaggerStatus())
                hh += ((ilayer%2)*2-1)*0.03;

        // ll: layer distance
        double tt = dbref.cellthickness(isuper) * dbref.wpdist(isuper);
        double ll = ilayer * tt;

        // wire x=0 coordinates in the lab
        double ym = y0mid + ll * stilt + hh * ctilt;
        double zm = z0mid + ll * ctilt - hh * stilt;

        // wire midpoint in the lab
        midpoint = new Vector3d(0, ym, zm);
        direction = new Vector3d(1, 0, 0);
        direction.rotateZ(dbref.thster(isuper));
        direction.rotateX(-dbref.thtilt(ireg));
        findEnds();
        center = leftend.plus(rightend).dividedBy(2.0);
    }

    private boolean isSensitiveWire(int isuper, int ilayer, int iwire) {
        return iwire>0 && iwire<=dbref.nsensewires() &&
                ilayer>0 && ilayer<=dbref.nsenselayers(isuper);
    }

    public Vector3d mid() {
        return new Vector3d(midpoint);
    }

    public Vector3d left() {
        return new Vector3d(leftend);
    }

    public Vector3d right() {
        return new Vector3d(rightend);
    }

    public Vector3d dir() {
        return new Vector3d(direction);
    }

    public Vector3d top() {
        if (leftend.y < rightend.y) {
            return new Vector3d(rightend);
        }
        return new Vector3d(leftend);
    }

    public Vector3d bottom() {
        if (leftend.y < rightend.y) {
            return new Vector3d(leftend);
        }
        return new Vector3d(rightend);
    }

    public double length() {
        return leftend.minus(rightend).magnitude();
    }

    public Vector3d center() {
        return new Vector3d(center);
    }
}
