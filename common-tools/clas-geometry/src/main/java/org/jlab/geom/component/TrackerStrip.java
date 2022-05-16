package org.jlab.geom.component;

import org.jlab.geom.prim.Point3D;

/**
 * A tracker strip. 
 * <p>
 * This class is an alias of
 * {@link org.jlab.geom.component.RectangularComponent}.
 * 
 * @author devita
 */
public class TrackerStrip extends RectangularComponent {
    
    private double width;
    private double thickness;

    /**
     * Constructs a new {@code TrackerStrip} from the given points.
     * <p>
     * The line will be from the middle of the bottom face to the middle of the
     * top face. The midpoint will be at the midpoint of the line. The direction
     * vector will parallel the line.
     * @param componentId the id of the component
     * @param origin
     * @param end
     * @param width
     * @param thickness
     */
    public TrackerStrip(int componentId, 
            Point3D origin, Point3D end, double width, double thickness) {
        this(componentId, 
             new Point3D(origin.x(), origin.y()-width/2, origin.z()+thickness/2), 
             new Point3D(origin.x(), origin.y()+width/2, origin.z()+thickness/2), 
             new Point3D(origin.x(), origin.y()+width/2, origin.z()-thickness/2), 
             new Point3D(origin.x(), origin.y()-width/2, origin.z()-thickness/2), 
             new Point3D(end.x(), end.y()-width/2, end.z()+thickness/2), 
             new Point3D(end.x(), end.y()+width/2, end.z()+thickness/2), 
             new Point3D(end.x(), end.y()+width/2, end.z()-thickness/2), 
             new Point3D(end.x(), end.y()-width/2, end.z()-thickness/2));
        this.width = width;
        this.thickness = thickness;
    }

    /**
     * Constructs a new {@code TrackerStrip} from the given points.
     * <p>
     * The line will be from the middle of the bottom face to the middle of the
     * top face. The midpoint will be at the midpoint of the line. The direction
     * vector will parallel the line.
     * @param p0 1st point of the bottom face
     * @param p1 2nd point of the bottom face
     * @param p2 3rd point of the bottom face
     * @param p3 4th point of the bottom face
     * @param p4 1st point of the top face
     * @param p5 2nd point of the top face
     * @param p6 3rd point of the top face
     * @param p7 4th point of the top face
     */
    private TrackerStrip(int componentId, Point3D p0, Point3D p1, Point3D p2, Point3D p3, Point3D p4, Point3D p5, Point3D p6, Point3D p7) {
        super(componentId, p0, p1, p2, p3, p4, p5, p6, p7);
    }
    
    
    /**
     * Returns "Tracker Strip".
     * @return "Tracker Strip"
     */
    @Override
    public String getType() {
        return "Tracker Strip";
    }
    
    /**
     * Returns width of the strip
     * @return width
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Returns thickness of the strip
     * @return thickness
     */
    public double getThickness() {
        return this.thickness;
    }
}
