package org.jlab.geom.prim;

import java.util.List;

/**
 * A 3D triangle represented by three points.
 * <p>
 * Since any three points in 3D space that are not collinear define a plane,
 * a triangle can be converted into a plane via {@link #plane()}.
 * <p>
 * The normal of the surface of a triangle is oriented such that when looking
 * antiparallel to the normal towards the triangle the triangle's points wound
 * counterclockwise. Conversely, when looking in a direction parallel to the
 * normal, the points are wound in a clockwise fashion.
 * <p>
 * The intersection of a line with a triangle can be calculated using the
 * intersection methods:
 * {@link #intersection(org.jlab.geom.prim.Line3D, List) intersection(...)}, 
 * {@link #intersectionRay(org.jlab.geom.prim.Line3D, List) intersectionRay(...)}, 
 * {@link #intersectionSegment(org.jlab.geom.prim.Line3D, List) intersectionSegment(...)}.
 *
 * @author gavalian
 */
public final class Triangle3D implements Face3D {
    private final Point3D point0 = new Point3D(); // the first point
    private final Point3D point1 = new Point3D(); // the second point
    private final Point3D point2 = new Point3D(); // the third point

    /**
     * Constructs a new {@code Triangle3D} with all three points at the origin.
     */
    public Triangle3D() {
        // nothing to do
    }

    /**
     * Constructs a new {@code Triangle3D} with the points at the specified
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
     */
    public Triangle3D(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2) {
        set(x0, y0, z0, x1, y1, z1, x2, y2, z2);
    }

    /**
     * Constructs a new {@code Triangle3D} from the given points.
     *
     * @param point0 the first point
     * @param point1 the second point
     * @param point2 the third point
     */
    public Triangle3D(Point3D point0, Point3D point1, Point3D point2) {
        set(point0, point1, point2);
    }

    /**
     * Constructs a new {@code Triangle3D} with its points coinciding with the
     * points of the given triangle.
     *
     * @param triangle the triangle to copy
     */
    public Triangle3D(Triangle3D triangle) {
        copy(triangle);
    }

    /**
     * Sets the points of this {@code Triangle3D} to coincide with points of the
     * given triangle.
     *
     * @param triangle the triangle to copy
     */
    public void copy(Triangle3D triangle) {
        set(triangle.point0, triangle.point1, triangle.point2);
    }

    /**
     * Sets the points of the triangle to coincide with the given coordinates.
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
     */
    public void set(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2) {
        point0.set(x0, y0, z0);
        point1.set(x1, y1, z1);
        point2.set(x2, y2, z2);
    }

    /**
     * Sets the points of this {@code Triangle3D} to coincide with the given points.
     *
     * @param point0 the first point
     * @param point1 the second point
     * @param point2 the third point
     */
    public void set(Point3D point0, Point3D point1, Point3D point2) {
        this.point0.copy(point0);
        this.point1.copy(point1);
        this.point2.copy(point2);
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
        }
        System.err.println("Warning: Triangle3D point(int index): invalid index=" + index);
        return null;
    }

    /**
     * Constructs a new {@code Point3D} at the geometric mean of the three
     * points in this {@code Triangle3D}.
     * @return the center of this {@code Triangle3D}
     */
    public Point3D center() {
        return Point3D.average(point0, point1, point2);
    }

    public Vector3D normal() {
        Vector3D vec1 = point0.vectorTo(point1);
        Vector3D vec2 = point0.vectorTo(point2);
        return vec1.cross(vec2);
    }
    
    public Plane3D plane() {
        return new Plane3D(center(), normal());
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
    }

    @Override
    public void rotateX(double angle) {
        point0.rotateX(angle);
        point1.rotateX(angle);
        point2.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        point0.rotateY(angle);
        point1.rotateY(angle);
        point2.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        point0.rotateZ(angle);
        point1.rotateZ(angle);
        point2.rotateZ(angle);
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
        return String.format("Triangle3D:\t(%12.5f %12.5f %12.5f) (%12.5f %12.5f %12.5f) (%12.5f %12.5f %12.5f)",
                point0.x(), point0.y(), point0.z(),
                point1.x(), point1.y(), point1.z(),
                point2.x(), point2.y(), point2.z());
    }
}
