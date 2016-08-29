/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Intersection;
import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.geometry.prim.Straight;

/**
 *
 * @author kenjo
 */
public class Triangle3d {

    private final Vector3d[] vertices = new Vector3d[3];
    private final Vector3d side1, side2;

    public final Vector3d normal;

    public Triangle3d(Vector3d vertex0, Vector3d vertex1, Vector3d vertex2) {
        vertices[0] = vertex0.clone();
        vertices[1] = vertex1.clone();
        vertices[2] = vertex2.clone();
        side1 = vertices[1].minus(vertices[0]);
        side2 = vertices[2].minus(vertices[0]);
        normal = side1.cross(side2).normalized();
    }

    public boolean contains(Vector3d point) {
        Vector3d pvec = point.minus(vertices[0]);
        double d00 = side1.dot(side1);
        double d01 = side1.dot(side2);
        double d11 = side2.dot(side2);
        double d20 = pvec.dot(side1);
        double d21 = pvec.dot(side2);
        double denom = d00 * d11 - d01 * d01;
        double vv = (d11 * d20 - d01 * d21) / denom;
        double ww = (d00 * d21 - d01 * d20) / denom;
        return (vv >= 0 && ww >= 0 && (vv + ww) <= 1);
    }

    public Intersection getIntersection(Straight line) {
        Intersection intersect = new Intersection();

        double tt = vertices[0].minus(line.origin()).dot(normal);
        double denom = line.diff().dot(normal);
        if (denom != 0) {
            tt /= denom;
            if (line.contains(tt)) {
                Vector3d p0 = line.origin().plus(line.diff().times(tt));
                if (this.contains(p0)) {
                    intersect.setPosition(p0, tt);
                }
            }

        }
        return intersect;
    }
}
