package org.jlab.geom.prim;

import org.jlab.geom.Showable;

/**
 *
 * @author jnhankins
 */
public final class Helix3D implements Transformable, Showable {
    private final Point3D origin = new Point3D();
    private final Point3D center = new Point3D();
    private final Vector3D normal = new Vector3D();
    private double totalHeight;
    private double loopHeight;
    private boolean clockwise;
    
    /**
     * Constructs a new {@code Helix3D} centered around the origin, with its
     * origin point at (1, 0, 0), looping once clockwise up the y-axis and
     * ending at (1, 1, 0).
     */
    public Helix3D() {
        origin.set(1, 0, 0);
        center.set(0, 0, 0);
        normal.setXYZ(0, 1, 0);
        totalHeight = 1;
        loopHeight = 1;
        clockwise = true;
    }
    
    /**
     * Constructs a helix from the given parameters.
     * @param origin the starting point on the helix
     * @param center a point at the center of the spiral of the helix
     * @param normal the overall direction of the helix
     * @param totalHeight the total height of the helix
     * @param loopHeight the distance between points on the helix after rotating
     * through a full circle
     * @param clockwise true if the helix orbits clockwise around its central 
     * axis
     */
    public Helix3D(Point3D origin, Point3D center, Vector3D normal, 
            double totalHeight, double loopHeight, boolean clockwise) {
        set(origin, center, normal, totalHeight, loopHeight, clockwise);
    }
    
    /**
     * Creates a new {@code Helix3D} identical to the given helix.
     * @param helix the helix to copy
     */
    public Helix3D(Helix3D helix) {
        copy(helix);
    }
    
    /**
     * Copies the parameters of the given helix so this helix is is identical
     * to the given helix.
     * @param helix the helix to copy
     */
    public void copy(Helix3D helix) {
        set(helix.origin, helix.center, helix.normal, 
                helix.totalHeight, helix.loopHeight, helix.clockwise);
    }

    /**
     * Sets the parameters of this helix.
     * @param origin the starting point on the helix
     * @param center a point at the center of the spiral of the helix
     * @param normal the overall direction of the helix
     * @param totalHeight the total height of the helix
     * @param loopHeight the distance between points on the helix after rotating
     * through a full circle
     * @param clockwise true if the helix orbits clockwise around its central 
     * axis
     */
    public void set(Point3D origin, Point3D center, Vector3D normal, 
            double totalHeight, double loopHeight, boolean clockwise) {
        setOrigin(origin);
        setCenter(center);
        setNormal(normal);
        setTotalHeight(totalHeight);
        setLoopHeight(loopHeight);
        setClockwise(clockwise);
    } 

    /**
     * Sets the starting point on the helix.
     * @param origin the origin point
     */
    public void setOrigin(Point3D origin) {
        this.origin.copy(origin);
    }

    /**
     * Sets the center point of the helix.
     * @param center the center point
     */
    public void setCenter(Point3D center) {
        this.center.copy(center);
    }

    /**
     * Sets the orientation of the central axis of the helix.
     * @param normal the normal
     */
    public void setNormal(Vector3D normal) {
        if (normal.isNull()) {
            System.out.println("Warning: Helix3D: setNormal(Vector3D normal): normal is null");
            return;
        }
        this.normal.copy(normal);
    }

    /**
     * Sets the total height of the helix measured along the central axis.
     * @param totalHeight the total height
     */
    public void setTotalHeight(double totalHeight) {
        if (normal.isNull()) {
            System.out.println("Warning: Helix3D: setTotalHeight(double totalHeight): totalHeight is null");
            return;
        }
        this.totalHeight = totalHeight;
    }

    /**
     * Sets the distance between winds of the helix. The distance between some
     * starting point on the helix and a point arrived at by traveling a
     * complete 360 degrees around a loop the helix.
     * @param loopHeight the loop height
     */
    public void setLoopHeight(double loopHeight) {
        if (normal.isNull()) {
            System.out.println("Warning: Helix3D: setLoopHeight(double loopHeight): loopHeight is null");
            return;
        }
        this.loopHeight = loopHeight;
    } 

    /**
     * Sets the clockwisedness of the loop. If true the loop spirals clockwise
     * around the central axis/normal.
     * @param clockwise if true then the helix spirals clockwise
     */
    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }
    
    /**
     * Returns the origin point on the helix.
     * @return the origin point
     */
    public Point3D origin() {
        return origin;
    }
    
    /**
     * Returns the central point of the helix.
     * @return the center point
     */
    public Point3D center() {
        return center;
    }
    
    /**
     * Returns the direction vector of the central axis.
     * @return the normal vector
     */
    public Vector3D normal() {
        return normal;
    }
    
    /**
     * Returns the total height of the helix as measured along the central axis.
     * @return the total height
     */
    public double totalHeight() {
        return totalHeight;
    }
    
    /**
     * Returns the distance between winds of the helix. The distance between some
     * starting point on the helix and a point arrived at by traveling a
     * complete 360 degrees around a loop the helix.
     * @return the loop height
     */
    public double loopHeight() {
        return loopHeight;
    }
    
    /**
     * Returns true if the helix spirals clockwise around its central axis.
     * @return true if the helix spirals clockwise around its central axis
     */
    public boolean clockwise() {
        return clockwise;
    }
    
    /**
     * Returns the point on the helix at the specified height.
     * @param height the height
     * @return the point on the helix at the specified height.
     */
    public Point3D point(double height) {
        if (height < 0 || totalHeight < height) {
            System.err.println("Helix3D: point(double height): height is not in range [0, totalHeight]");
        }
        Vector3D N = new Vector3D(normal);
        N.setMag(height);
        Vector3D P = center.vectorTo(origin);
        double t = clockwise? theta(height) : -theta(height);
        normal.rotate(P, t);
        P.add(N);
        return new Point3D(center, P);
    }
    
    /**
     * Returns the angle of the point on the helix at the specified height
     * relative to the origin point around the central axis.
     * @param height the height
     * @return the angle of the point on the helix at that height
     */
    public double theta(double height) {
        return (height%loopHeight)*Math.PI*2;
    }
    
    /**
     * Returns true if the point is on the helix (within 100nm).
     * @param point the point
     * @return true if the point is on the helix
     */
    public boolean isPointOnHelix(Point3D point) {
        Vector3D N = new Vector3D(normal);
        N.unit();
        Vector3D V = center.vectorTo(point);
        double height = V.dot(N);
        if (height < 0 || totalHeight < height)
            return false;
        double t = N.angle(center.vectorTo(origin), V);
        double th = theta(height);
        return (Math.abs(t - th) < 0.00001);
    }

    @Override
    public void translateXYZ(double dx, double dy, double dz) {
        origin.translateXYZ(dx, dy, dz);
        center.translateXYZ(dx, dy, dz);
    }

    @Override
    public void rotateX(double angle) {
        origin.rotateX(angle);
        center.rotateX(angle);
        normal.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        origin.rotateY(angle);
        center.rotateY(angle);
        normal.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        origin.rotateZ(angle);
        center.rotateZ(angle);
        normal.rotateZ(angle);
    }

    @Override
    public void show() {
        System.out.println(this);
    }
    
    @Override
    public String toString() {
        return new StringBuilder("Helix: ")
                .append("\n\torigin:\t").append(origin)
                .append("\n\tcenter:\t").append(center)
                .append("\n\tnormal:\t").append(normal)
                .append("\n\ttotal height:\t").append(totalHeight)
                .append("\n\tloop height:\t").append(loopHeight)
                .toString();
                
    }
}