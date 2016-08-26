/**
 * Trap.java
 *
 */
package eu.mihosoft.vrl.v3d;

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
            
    public Trap(double pDz, double pTheta, double pPhi,
            double pDy1, double pDx1, double pDx2, double pAlp1,
            double pDy2, double pDx3, double pDx4, double pAlp2) {

        double[] dimensions = {pDz, pTheta, pPhi, pDy1, pDx1, pDx2, pAlp1, pDy2, pDx3, pDx4, pAlp2};
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
        Vector3d[] vertpos = {};
        
        List<Polygon> polygons = new ArrayList<>();
        for (int[][] face : facenorm) {
            List<Vertex> vertices = new ArrayList<>();
            for (int i : face[0]) {
                Vector3d vertpos = new Vector3d(
                        dimensions.x * (1 * Math.min(1, i & 1) - 0.5),
                        dimensions.y * (1 * Math.min(1, i & 2) - 0.5),
                        dimensions.z * (1 * Math.min(1, i & 4) - 0.5)
                );
                vertices.add(new Vertex(pos, new Vector3d(
                        (double) face[1][0],
                        (double) face[1][1],
                        (double) face[1][2]
                )));
            }
            polygons.add(new Polygon(vertices, properties));
        }

        if (!centered) {

            Transform centerTransform = Transform.unity().translate(dimensions.x / 2.0, dimensions.y / 2.0, dimensions.z / 2.0);

            for (Polygon p : polygons) {
                p.transform(centerTransform);
            }
        }

        return polygons;
    }

    @Override
    public PropertyStorage getProperties() {
        return properties;
    }
}
