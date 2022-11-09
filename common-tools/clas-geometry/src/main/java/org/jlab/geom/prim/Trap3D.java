package org.jlab.geom.prim;

import java.util.List;

/**
 * A 3D trapezoid represented by four points laying on the same plane.
 * <p>
 * The normal of the surface of a trapezoid is oriented such that when looking
 * antiparallel to the normal towards the trapezoid the Trap's points wound
 * counterclockwise. Conversely, when looking in a direction parallel to the
 * normal, the points are wound in a clockwise fashion.
 * <p>
 * The intersection of a line with a trapezoid can be calculated using the
 * intersection methods:
 * {@link #intersection(org.jlab.geom.prim.Line3D, List) intersection(...)}, 
 * {@link #intersectionRay(org.jlab.geom.prim.Line3D, List) intersectionRay(...)}, 
 * {@link #intersectionSegment(org.jlab.geom.prim.Line3D, List) intersectionSegment(...)}.
 *
 * @author devita
 */
public final class Trap3D implements Face3D {
    private final Point3D point0 = new Point3D(); // the first point
    private final Point3D point1 = new Point3D(); // the second point
    private final Point3D point2 = new Point3D(); // the third point
    private final Point3D point3 = new Point3D(); // the third point
    
    private final static double SMALL_NUMBER = 1E-6;
    
    /**
     * Constructs a new {@code Trap3D} with all three points at the origin.
     */
    public Trap3D() {
        // nothing to do
    }

    /**
     * Constructs a new {@code Trap3D} with the points at the specified
     * coordinates.
     *
     * @param x0 x coordinate of the first point
     * @param y0 y coordinate of the first point
     * @param z0 z coordinate of the first point
     * @param x1 x coordinate of the second point
     * @param y1 y coordinate of the second point
     * @param z1 z coordinate of the second point
     * @param x2 x coordinate of the third point
     * @param y2 y coordinate of the third point
     * @param z2 z coordinate of the third point
     * @param x3 x coordinate of the fourth point
     * @param y3 y coordinate of the fourth point
     * @param z3 z coordinate of the fourth point
     */
    public Trap3D(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3) {
        set(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3);
        if(!isPlanar()) {
            System.err.println("Error: provided points are not coplanar");
        }
    }

    /**
     * Constructs a new {@code Trap3D} from the given points.
     *
     * @param point0 the first point
     * @param point1 the second point
     * @param point2 the third point
     * @param point3 the fourth point
     */
    public Trap3D(Point3D point0, Point3D point1, Point3D point2, Point3D point3) {
        set(point0, point1, point2, point3);
        if(!isPlanar()) {
            System.err.println("Error: provided points are not coplanar");
        }
    }

    /**
     * Constructs a new {@code Trap3D} with its points coinciding with the
     * points of the given Trap.
     *
     * @param Trap the Trap to copy
     */
    public Trap3D(Trap3D Trap) {
        copy(Trap);
    }

    /**
     * Sets the points of this {@code Trap3D} to coincide with points of the
     * given Trap.
     *
     * @param Trap the Trap to copy
     */
    public void copy(Trap3D Trap) {
        set(Trap.point0, Trap.point1, Trap.point2, Trap.point3);
    }

    /**
     * Sets the points of the Trap to coincide with the given coordinates.
     *
     * @param x0 x coordinate of the first point
     * @param y0 y coordinate of the first point
     * @param z0 z coordinate of the first point
     * @param x1 x coordinate of the second point
     * @param y1 y coordinate of the second point
     * @param z1 z coordinate of the second point
     * @param x2 x coordinate of the third point
     * @param y2 y coordinate of the third point
     * @param z2 z coordinate of the third point
     * @param x3 x coordinate of the fourth point
     * @param y3 y coordinate of the fourth point
     * @param z3 z coordinate of the fourth point
     */
    public void set(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3) {
        point0.set(x0, y0, z0);
        point1.set(x1, y1, z1);
        point2.set(x2, y2, z2);
        point3.set(x3, y3, z3);
    }

    /**
     * Sets the points of this {@code Trap3D} to coincide with the given points.
     *
     * @param point0 the first point
     * @param point1 the second point
     * @param point2 the third point
     * @param point3 the fourth point
     */
    public void set(Point3D point0, Point3D point1, Point3D point2, Point3D point3) {
        this.point0.copy(point0);
        this.point1.copy(point1);
        this.point2.copy(point2);
        this.point3.copy(point3);
    }
    
    /**
     * Check trapezoid planarity
     * 
     * @return true if the four points are on a plane, false otherwise 
     */
    public boolean isPlanar() {
        return this.isInPlane(point3);
    }

    @Override
    public Point3D point(int index) {
        switch (index) {
            case 0:
                return point0;
            case 1:
                return point1;
            case 2:
                return point2;
            case 3:
                return point3;
        }
        System.err.println("Warning: Trap3D point(int index): invalid index=" + index);
        return null;
    }

    /**
     * Constructs a new {@code Point3D} at the geometric mean of the four
     * points in this {@code Trap3D}.
     * @return the center of this {@code Trap3D}
     */
    public Point3D center() {
        return Point3D.average(point0, point1, point2, point3);
    }

    /**
     * Constructs the normal vector to the trapezoid plane, defined 
     * from the first three points
     * @return unit vector {@code Trap3D}
     */
    public Vector3D normal() {
        Vector3D vec1 = point0.vectorTo(point1);
        Vector3D vec2 = point0.vectorTo(point2);
        return vec1.cross(vec2).asUnit();
    }
    
    /**
     * Constructs the plane representing the trapezoid surface
     * @return the plane of this {@code Trap3D}
     */
    public Plane3D plane() {
        return new Plane3D(center(), normal());
    }

    /**
     * Test whether the given point is in the trapezoid plane
     * @param p the given point
     * @return true or false {@code Trap3D}
     */
    public boolean isInPlane(Point3D p) {
        double distance = Math.abs(point0.vectorTo(p).dot(this.normal()));
        return distance<SMALL_NUMBER;
    }

    /**
     * Test whether the given point is inside the area of this {@code Trap3D}.
     * @param p the given point
     * @return true if inside or false if outside
     */
    public boolean isInside(Point3D p) {

        Vector3D n = this.normal();
        int sign = (int) Math.signum(this.point(3).vectorTo(this.point(0)).cross(this.point(3).vectorTo(p)).dot(n));
        for(int i=0; i<3; i++) {
            int signi = (int) Math.signum(this.point(i).vectorTo(this.point(i+1)).cross(this.point(i).vectorTo(p)).dot(n));
            if(signi != sign)
                return false;
        }
        return true;
    }

    /**
     * Computes the minimum distance of the given point from the edges of 
     * this {@code Trap3D}.
     * @param p the given point
     * @return the signed distance to the {@code Trap3D} edges, positive if inside or negative if outside
     */
    public double distanceFromEdge(Point3D p) {
        double distance = new Line3D(this.point(3), this.point(0)).distanceSegment(p).length();
        for (int i = 0; i < 3; i++) {
            double disti = new Line3D(this.point(i), this.point(i+1)).distanceSegment(p).length();
            if (disti < distance) {
                distance = disti;
            }
        }
        if (!this.isInside(p)) {
            distance *= -1;
        }
        return distance;
    }             
        
    @Override
    public int intersection(Line3D line, List<Point3D> intersections) {
        Vector3D v0 = point0.vectorTo(point1);
        Vector3D v1 = point0.vectorTo(point2);
        Vector3D normal = v0.cross(v1);
        Plane3D plane = new Plane3D(point0, normal);
        Point3D intersect = new Point3D();
        if (plane.intersection(line, intersect) != 1) {
            return 0;
        }
        Vector3D v2 = point0.vectorTo(intersect);
        double dot00 = v1.dot(v1);
        double dot01 = v1.dot(v0);
        double dot02 = v1.dot(v2);
        double dot11 = v0.dot(v0);
        double dot12 = v0.dot(v2);
        double invDenom = 1. / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        if (u >= 0 && v >= 0 && ((u + v) <= 1)) {
            intersections.add(intersect);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int intersectionRay(Line3D line, List<Point3D> intersections) {
        Vector3D v0 = point0.vectorTo(point1);
        Vector3D v1 = point0.vectorTo(point2);
        Vector3D normal = v0.cross(v1);
        Plane3D plane = new Plane3D(point0, normal);
        Point3D intersect = new Point3D();
        if (plane.intersectionRay(line, intersect) != 1) {
            return 0;
        }
        Vector3D v2 = point0.vectorTo(intersect);
        double dot00 = v1.dot(v1);
        double dot01 = v1.dot(v0);
        double dot02 = v1.dot(v2);
        double dot11 = v0.dot(v0);
        double dot12 = v0.dot(v2);
        double invDenom = 1. / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        if (u >= 0 && v >= 0 && ((u + v) <= 1)) {
            intersections.add(intersect);
            return 1;
        } else {
            return 0;
        }
    }
    
    @Override
    public int intersectionSegment(Line3D line, List<Point3D> intersections) {
        Vector3D v0 = point0.vectorTo(point1);
        Vector3D v1 = point0.vectorTo(point2);
        Vector3D normal = v0.cross(v1);
        Plane3D plane = new Plane3D(point0, normal);
        Point3D intersect = new Point3D();
        if (plane.intersectionSegment(line, intersect) != 1) {
            return 0;
        }
        Vector3D v2 = point0.vectorTo(intersect);
        double dot00 = v1.dot(v1);
        double dot01 = v1.dot(v0);
        double dot02 = v1.dot(v2);
        double dot11 = v0.dot(v0);
        double dot12 = v0.dot(v2);
        double invDenom = 1. / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        if (u >= 0 && v >= 0 && ((u + v) <= 1)) {
            intersections.add(intersect);
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void translateXYZ(double x, double y, double z) {
        point0.translateXYZ(x, y, z);
        point1.translateXYZ(x, y, z);
        point2.translateXYZ(x, y, z);
        point3.translateXYZ(x, y, z);
    }

    @Override
    public void rotateX(double angle) {
        point0.rotateX(angle);
        point1.rotateX(angle);
        point2.rotateX(angle);
        point3.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        point0.rotateY(angle);
        point1.rotateY(angle);
        point2.rotateY(angle);
        point3.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        point0.rotateZ(angle);
        point1.rotateZ(angle);
        point2.rotateZ(angle);
        point3.rotateZ(angle);
    }

    /**
     * Invokes {@code System.out.println(this)}.
     */
    @Override
    public void show() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("Trap3D:\t(%12.5f %12.5f %12.5f) (%12.5f %12.5f %12.5f) (%12.5f %12.5f %12.5f) (%12.5f %12.5f %12.5f)",
                point0.x(), point0.y(), point0.z(),
                point1.x(), point1.y(), point1.z(),
                point2.x(), point2.y(), point2.z(),
                point3.x(), point3.y(), point3.z());
    }
}
