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
public class Trap implements Primitive {

    private final PropertyStorage properties = new PropertyStorage();
    private double pDz, pTheta, pPhi, pDy1, pDx1, pDx2, pAlp1, pDy2, pDx3, pDx4, pAlp2;
    private double pTthetaCphi, pTthetaSphi, pTalpha1, pTalpha2;

    public Trap(double pDz, double pTheta, double pPhi,
            double pDy1, double pDx1, double pDx2, double pAlp1,
            double pDy2, double pDx3, double pDx4, double pAlp2) {

        if (pDz <= 0 || pDy1 <= 0 || pDx1 <= 0
                || pDx2 <= 0 || pDy2 <= 0 || pDx3 <= 0 || pDx4 <= 0 || pAlp1 != pAlp2) {
            throw new IllegalArgumentException("Illegal arguments for Trap Primitive!");
        }

        double tTh = Math.tan(pTheta);
        pTthetaCphi = tTh * Math.cos(pPhi);
        pTthetaSphi = tTh * Math.sin(pPhi);

        pTalpha1 = Math.tan(pAlp1);
        pTalpha2 = Math.tan(pAlp2);

        this.pDz = pDz;
        this.pTheta = pTheta;
        this.pPhi = pPhi;
        this.pDy1 = pDy1;
        this.pDx1 = pDx1;
        this.pDx2 = pDx2;
        this.pAlp1 = pAlp1;
        this.pDy2 = pDy2;
        this.pDx3 = pDx3;
        this.pDx4 = pDx4;
        this.pAlp2 = pAlp2;
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
            new Vector3d(-pDz * pTthetaCphi - pDy1 * pTalpha1 - pDx1,
            -pDz * pTthetaSphi - pDy1, -pDz),
            new Vector3d(-pDz * pTthetaCphi - pDy1 * pTalpha1 + pDx1,
            -pDz * pTthetaSphi - pDy1, -pDz),
            new Vector3d(-pDz * pTthetaCphi + pDy1 * pTalpha1 - pDx2,
            -pDz * pTthetaSphi + pDy1, -pDz),
            new Vector3d(-pDz * pTthetaCphi + pDy1 * pTalpha1 + pDx2,
            -pDz * pTthetaSphi + pDy1, -pDz),
            new Vector3d(+pDz * pTthetaCphi - pDy2 * pTalpha2 - pDx3,
            +pDz * pTthetaSphi - pDy2, +pDz),
            new Vector3d(+pDz * pTthetaCphi - pDy2 * pTalpha2 + pDx3,
            +pDz * pTthetaSphi - pDy2, +pDz),
            new Vector3d(+pDz * pTthetaCphi + pDy2 * pTalpha2 - pDx4,
            +pDz * pTthetaSphi + pDy2, +pDz),
            new Vector3d(+pDz * pTthetaCphi + pDy2 * pTalpha2 + pDx4,
            +pDz * pTthetaSphi + pDy2, +pDz)};

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
