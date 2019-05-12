package org.jlab.geom.prim;

import java.util.Arrays;
import java.util.Collection;
import org.jlab.geom.Showable;

/**
 * A 3D point in space represented by three coordinates coordinates (x, y, z).
 * <p>
 * The distance between two points can be obtained via
 * {@link #distance(org.jlab.geom.prim.Point3D)}. It is also possible to linearly
 * interpolate between one point an another via {@link #lerp(Point3D, double)},
 * and to vector from one point to another via {@link #vectorTo(Point3D) } and
 * {@link #vectorFrom(Point3D) }. Also, any collection of points can be averaged
 * using {@link #average(java.util.Collection)}.
 *
 * @author gavalian
 */
public final class Point3D implements Transformable, Showable {
    private double x; // the x coordinate
    private double y; // the y coordinate
    private double z; // the z coordinate

    /**
     * Constructs a new {@code Point3D} at (0, 0, 0).
     */
    public Point3D() {
        set(0, 0, 0);
    }
    /**
     * Constructs a new {@code Point3D} at (x, y, z).
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Point3D(double x, double y, double z) {
        set(x, y, z);
    }
    /**
     * Constructs a new {@code Point3D} with the given vector.
     * @param v the vector
     */
    public Point3D(Vector3D v) {
        set(v.x(), v.y(), v.z());
    }
    /**
     * Constructs a new {@code Point3D} by adding the given vector to the given
     * point.
     * @param point  the origin point
     * @param vector the direction vector
     */
    public Point3D(Point3D point, Vector3D vector) {
        set(point, vector);
    }
    /**
     * Constructs a new {@code Point3D} by copying the x, y, and z coordinates
     * of the
     * given point.
     * @param point the point to copy
     */
    public Point3D(Point3D point) {
        copy(point);
    }

    /**
     * Sets the components of this point to be equal the components of the
     * given point.
     * @param point the point to copy
     */
    public void copy(Point3D point) {
        set(point.x, point.y, point.z);
    }

    /**
     * Sets this points coordinates by adding the given vector to the given
     * point.
     * @param point  the origin point
     * @param vector the direction vector
     */
    public void set(Point3D point, Vector3D vector) {
        set(point.x + vector.x(), point.y+vector.y(), point.z+vector.z());
    }
    /**
     * Sets the x, y and z coordinates of this point.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public void set(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }
    /**
     * Sets the x coordinate.
     * @param x the x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }
    /**
     * Sets the y coordinate.
     * @param y the y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
    /**
     * Sets the z coordinate.
     * @param z the z coordinate
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the x coordinate.
     * @return the x coordinate
     */
    public double x() {
        return x;
    }
    /**
     * Returns the y coordinate.
     * @return the y coordinate
     */
    public double y() {
        return y;
    }
    /**
     * Returns the z coordinate.
     * @return the z coordinate
     */
    public double z() {
        return z;
    }

    /**
     * Returns the distance between this point and the given point.
     * @param point the point to calculate the distance from this point to
     * @return the distance between the points
     */
    public double distance(Point3D point) {
        return distance(point.x, point.y, point.z);
    }
    /**
     * Returns the distance between this point and the point at the given
     * coordinate.
     * @param x the x coordinate of the given point
     * @param y the y coordinate of the given point
     * @param z the z coordinate of the given point
     * @return the distance between the points
     */
    public double distance(double x, double y, double z) {
        return Math.sqrt(
                (x-this.x)*(x-this.x) +
                (y-this.y)*(y-this.y) +
                (z-this.z)*(z-this.z));
    }

    /**
     * Constructs a new {@code Point3D} at the geometric mean of this point and the given
     * point. This function behaves identically to lerp(point, 0.5).
     * @param point the other point
     * @return a point at the geometric mean of the two given points
     */
    public Point3D midpoint(Point3D point) {
        return lerp(point, 0.5);
    }
    /**
     * Constructs a new {@code Point3D} between this point and the given point
     * by linearly interpolating from this point to the given point by the
     * amount specified.
     * <ul>
     * <li>If t==0, then the returned point is equal to this point.</li>
     * <li>If t==0.5, then the returned point is half way between both points.</li>
     * <li>If t==1, then the returned point is equal to the given point.</li>
     * </ul>
     * @param point the point to interpolate between
     * @param t the interpolation coefficient
     * @return the newly constructed point
     */
    public Point3D lerp(Point3D point, double t) {
        return new Point3D(
                x+(point.x-x)*t,
                y+(point.y-y)*t,
                z+(point.z-z)*t);
    }

    /**
     * Combines this point with the given point. The position of this point will
     * move to the geometric mean (midpoint) of the the two points.
     * @param point the point used to calculate the geometric mean
     */
    public void combine(Point3D point) {
        x = (x + point.x)*0.5;
        y = (y + point.y)*0.5;
        z = (z + point.x)*0.5;
    }

    /**
     * Constructs a new {@code Vector3D} pointing from the given point to this
     * point (equivalent to (this.x-point.x, this.y-point.y, this.z-point.z).
     * @param point the given point
     * @return the direction vector from the given point to this point
     */
    public Vector3D vectorFrom(Point3D point) {
        return Point3D.this.vectorFrom(point.x, point.y, point.z);
    }
    /**
     * Constructs a new {@code Vector3D} point from the point at the given
     * coordinates to this point (this.x-x, this.y-y, this.z-z)
     * @param x the x coordinate of the given point
     * @param y the y coordinate of the given point
     * @param z the z coordinate of the given point
     * @return the direction vector from the given point to this point
     */
    public Vector3D vectorFrom(double x, double y, double z) {
        return new Vector3D(this.x-x, this.y-y, this.z-z);

    }
    /**
     * Constructs a new {@code Vector3D} pointing from this point to the given
     * point (equivalent to (point.x-this.x, point.y-this.y, point.z-this.z)).
     * @param point the given point
     * @return the direction vector from this point to the given
     */
    public Vector3D vectorTo(Point3D point) {
        return vectorTo(point.x, point.y, point.z);
    }
    /**
     * Constructs a new {@code Vector3D} point from this point to the point at
     * the given coordinates (x-this.x, y-this.y, z-this.z)).
     * @param x the x coordinate of the given point
     * @param y the y coordinate of the given point
     * @param z the z coordinate of the given point
     * @return the direction vector from this point to the given
     */
    public Vector3D vectorTo(double x, double y, double z) {
        return new Vector3D(x-this.x, y-this.y, z-this.z);
    }

    /**
     * Constructs a new {@code Vector3D} using this points x, y, and z
     * coordinates.
     * @return a vector representation of this point
     */
    public Vector3D toVector3D() {
        return new Vector3D(x, y, z);
    }

    @Override
    public void translateXYZ(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }
    @Override
    public void rotateX(double angle) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double yy = y;
        y = c*yy - s*z;
        z = s*yy + c*z;
    }
    @Override
    public void rotateY(double angle) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double zz = z;
        z = c*zz - s*x;
        x = s*zz + c*x;
    }
    @Override
    public void rotateZ(double angle) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double xx = x;
        x = c*xx - s*y;
        y = s*xx + c*y;
    }

    /**
     * Constructs a new {@code Point3D} at the geometric mean of the given
     * points.
     * @param points the points to average
     * @return the geometric mean of the points
     */
    public static Point3D average(Point3D... points) {
        return average(Arrays.asList(points));
    }

    /**
     * Constructs a new {@code Point3D} at the geometric mean of the given
     * points.
     * @param points the points to average
     * @return the geometric mean of the points
     */
    public static Point3D average(Collection<Point3D> points) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (Point3D point : points) {
            x += point.x;
            y += point.y;
            z += point.z;
        }
        return new Point3D(x/points.size(), y/points.size(), z/points.size());
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
        return String.format("Point3D:\t%12.5f %12.5f %12.5f", x, y, z);
    }

    public String toStringBrief(int ndigits) {
        return new String("(" +
            String.format(new String("%."+ndigits+"f"),x)+", "+
            String.format(new String("%."+ndigits+"f"),y)+", "+
            String.format(new String("%."+ndigits+"f"),z)+")");
    }
    public String toStringBrief() {
        return this.toStringBrief(5);
    }
}
