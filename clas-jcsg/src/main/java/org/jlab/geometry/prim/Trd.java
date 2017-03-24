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
public class Trd implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();
    private double pDz, pDy1, pDy2, pDx1, pDx2;

    public Trd(double pDx1, double pDx2, double pDy1, double pDy2, double pDz) {

        if (pDx1 <= 0 || pDx2 <= 0 || pDy1 <= 0 || pDy2 <= 0 || pDz <= 0) {
            throw new IllegalArgumentException("Illegal arguments for Trd Primitive!");
        }

        this.pDz = pDz;
        this.pDx1 = pDx1;
        this.pDx2 = pDx2;
        this.pDy1 = pDy1;
        this.pDy2 = pDy2;
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
        
        Vector3d[] vpos = {
            new Vector3d(-pDx1, -pDy1, -pDz),
            new Vector3d(pDx1, -pDy1, -pDz),
            new Vector3d(-pDx1, pDy1, -pDz),
            new Vector3d(pDx1, pDy1, -pDz),
            
            new Vector3d(-pDx2, -pDy2, pDz),
            new Vector3d(pDx2, -pDy2, pDz),
            new Vector3d(-pDx2, pDy2, pDz),
            new Vector3d(pDx2, pDy2, pDz)};

        List<Polygon> polygons = new ArrayList<>();
        for (int[][] face : facenorm) {
            List<Vertex> vertices = new ArrayList<>();
            for (int ivert : face[0]) {
                vertices.add(new Vertex(vpos[ivert], new Vector3d(
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
