package org.jlab.geom.component;

import org.jlab.geom.prim.Point3D;

/**
 * A silicon strip. 
 * <p>
 * This class is an alias of
 * {@link org.jlab.geom.component.RectangularComponent}.
 * 
 * @author jnhankins
 */
public class SiStrip extends RectangularComponent {
    
    
    /**
     * Constructs a new {@code SiStrip} from the given points.
     * <p>
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
    public SiStrip(int componentId,
            Point3D p0, Point3D p1, Point3D p2, Point3D p3,
            Point3D p4, Point3D p5, Point3D p6, Point3D p7) {
        super(componentId, p0, p1, p2, p3, p4, p5, p6, p7);
    }
    
    /**
     * Returns "Silicon Strip".
     * @return "Silicon Strip"
     */
    @Override
    public String getType() {
        return "Silicon Strip";
    }
}
