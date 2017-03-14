package org.jlab.geom.component;

import java.util.Arrays;
import org.jlab.geom.prim.Point3D;

/**
 * This class provides a basic implementation of the
 * {@link org.jlab.geom.base.Component} interface where the component's
 * {@link #getVolumeShape() volume} is defined by two rectangles such that the
 * edges of each rectangle are connected to form trapezoidal sides.
 * <p>
 * For more information see {@link org.jlab.geom.component.PrismaticComponent}.
 * 
 * @author jnhankins
 */
public class RectangularComponent extends PrismaticComponent {
    /**
     * Initializes a {@code RectangularComponent} centered on the origin with
     * the direction vector parallel with the y-axis, the midpoint at the
     * origin, the plane on the upstream face with the normal anti-parallel to
     * the z-axis, and the line passing through the midpoints of the top and
     * bottom faces.
     * @param componentId   the id of the component
     * @param width         the length along the x-axis
     * @param length        the length along the y-axis
     * @param thickness     the length along the z-axis
     */
    public RectangularComponent(int componentId, double width, double length, double thickness) {
        this(componentId,
            new Point3D(-width*0.5, -length*0.5, -thickness*0.5),
            new Point3D( width*0.5, -length*0.5, -thickness*0.5),
            new Point3D( width*0.5, -length*0.5,  thickness*0.5),
            new Point3D(-width*0.5, -length*0.5,  thickness*0.5),
            new Point3D(-width*0.5,  length*0.5, -thickness*0.5),
            new Point3D( width*0.5,  length*0.5, -thickness*0.5),
            new Point3D( width*0.5,  length*0.5,  thickness*0.5),
            new Point3D(-width*0.5,  length*0.5,  thickness*0.5));
    }

    /**
     * Initializes a {@code RectangularComponent} from the given points.
     * The line will be from the middle of the bottom face to the middle of the
     * top face. The midpoint will be at the midpoint of the line. The direction
     * vector will parallel the line.
     * @param componentId the id of the component
     * @param p0 1st point of the bottom face
     * @param p1 2nd point of the bottom face
     * @param p2 3rd point of the bottom face
     * @param p3 4th point of the bottom face
     * @param p4 1st point of the top face
     * @param p5 2nd point of the top face
     * @param p6 3rd point of the top face
     * @param p7 4th point of the top face
     */
    public RectangularComponent(int componentId,
            Point3D p0, Point3D p1, Point3D p2, Point3D p3,
            Point3D p4, Point3D p5, Point3D p6, Point3D p7) {
        super(componentId, Arrays.asList(p0, p1, p2, p3), Arrays.asList(p4, p5, p6, p7));
    }

    /**
     * Returns "Rectangular Component".
     * @return "Rectangular Component"
     */
    @Override
    public String getType() {
        return "Rectangular Component";
    }
}
