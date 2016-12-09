/**
 * Trap.java
 *
 */
package org.jlab.geometry.prim;

import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.Primitive;
import eu.mihosoft.vrl.v3d.PropertyStorage;
import eu.mihosoft.vrl.v3d.Vector3d;
import eu.mihosoft.vrl.v3d.Vertex;
import java.util.ArrayList;
import java.util.List;

/**
 * An axis-aligned solid trapezoid defined by dimensions, inspired by G4Trap
 *
 * @author Andrey Kim;
 */
public class Box implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();
    private double pDz, pDy, pDx;

    public Box(double pDx, double pDy, double pDz) {

        if (pDx <= 0 || pDy <= 0 || pDz <= 0) {
            throw new IllegalArgumentException("Illegal arguments for Box Primitive!");
        }

        this.pDz = pDz;
        this.pDx = pDx;
        this.pDy = pDy;
    }

    @Override
    public List<Polygon> toPolygons() {

        int[][][] facenorm = {
            // position     // normal
            {{0, 4, 6, 2}, {-1, 0, 0}},
            {{1, 3, 7, 5}, {+1, 0, 0}},
            {{0, 1, 5, 4}, {0, -1, 0}},
            {{2, 6, 7, 3}, {0, +1, 0}},
            {{0, 2, 3, 1}, {0, 0, -1}},
            {{4, 5, 7, 6}, {0, 0, +1}}
        };

        List<Polygon> polygons = new ArrayList<>();
        for (int[][] face : facenorm) {
            List<Vertex> vertices = new ArrayList<>();
            for (int ivert : face[0]) {
                Vector3d vpos = new Vector3d(
                        pDx * (2 * Math.min(1, ivert & 1) - 1),
                        pDy * (2 * Math.min(1, ivert & 2) - 1),
                        pDz * (2 * Math.min(1, ivert & 4) - 1)
                );

                vertices.add(new Vertex(vpos, new Vector3d(
                        (double) face[1][0],
                        (double) face[1][1],
                        (double) face[1][2]
                )));
            }
            polygons.add(new Polygon(vertices, properties));
        }

        return polygons;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }
}
