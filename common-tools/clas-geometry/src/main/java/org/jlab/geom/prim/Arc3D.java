package org.jlab.geom.prim;

import org.jlab.geom.Showable;

/**
 * An arc represented by a point at the center of the circle subtended by the
 * arc, a point at the origin/beginning of the arc, the normal of the circle of
 * the arc, and an angle theta which is the angle subtended by the arc clockwise
 * around the normal. 
 *
 * @author jnhankins
 */
public final class Arc3D implements Transformable, Showable {
    private final Point3D  origin = new Point3D();
    private final Point3D  center = new Point3D();
    private final Vector3D normal = new Vector3D();
    private double theta;
    
    /**
     * Constructs a new {@code Arc3D} from point centered around the origin with
     * a unit radius in the xy plane such that the origin point is at (1, 0, 0)
     * and the end point is at (0, 1, 0).
     */
    public Arc3D() {
        origin.set(1, 0, 0);
        center.set(0, 0, 0);
        normal.setXYZ(0, 0, 1);
        theta = Math.PI*0.5;
    }
    
    /**
     * Constructs a new {@code Arc3D} from the given parameters.
     * @param origin the origin point on the arc
     * @param center the center point of the circle the arc
     * @param normal the normal vector of the circle of the arc
     * @param theta the angle subtended by the arc
     */
    public Arc3D(Point3D origin, Point3D center, Vector3D normal, double theta) {
        set(origin, center, normal, theta);
    }
    
    /**
     * Constructs a new {@code Arc3D} that is identical to the given arc.
     * @param arc the arc to copy
     */
    public Arc3D(Arc3D arc) {
        copy(arc);
    }
    
    /**
     * Sets the core parameters of this {@code Arc3D} to be equal to those of
     * the given arc such that this arc will coincide with the given arc.
     * @param arc the arc to copy
     */
    public void copy(Arc3D arc) {
        set(arc.origin, arc.center, arc.normal, arc.theta);
    }
    
    /**
     * Sets the core parameters of this arc to equal the given parameters.
     * @param origin the origin point on the arc
     * @param center the center point of the circle the arc
     * @param normal the normal vector of the circle of the arc
     * @param theta the angle subtended by the arc
     */
    public void set(Point3D origin, Point3D center, Vector3D normal, double theta) {
        setOrigin(origin);
        setCenter(center);
        setNormal(normal);
        setTheta(theta);
    }
    
    /**
     * Sets the origin point at the beginning of the arc.
     * @param origin the origin point
     */
    public void setOrigin(Point3D origin) {
        this.origin.copy(origin);
    }
    
    /**
     * Sets the center point of the circle of the arc.
     * @param center the center point of the circle
     */
    public void setCenter(Point3D center) {
        this.center.copy(center);
    }
    
    /**
     * Sets the normal of the circle of the arc.
     * @param normal the normal of the circle of the arc.
     */
    public void setNormal(Vector3D normal) {
        if (normal.isNull()) {
            System.err.println("Arc3D: setNormal(Vector3D normal): the normal cannot be a null vector");
            return;
        }
        this.normal.copy(normal);
    }
    
    /**
     * Sets the angle subtended by the arc. This value will be modulated to be
     * within the range (0, 2*PI].
     * @param theta the angle subtended by the arc
     */
    public void setTheta(double theta) {
        theta = theta%(2*Math.PI);
        this.theta = theta<=0 ? theta+2*Math.PI : theta;
    }
    
    /**
     * Sets the radius of the arc.
     * @param radius the radius
     */
    public void setRadius(double radius) {
        Vector3D vec = center.vectorTo(origin);
        vec.setMag(radius);
        origin.set(center, vec);
    }
    
    /**
     * Returns the origin point of this arc.
     * @return the origin point
     */
    public Point3D origin() {
        return origin;
    }
    
    /**
     * Constructs a new {@code Point3D} at the end of this arc.
     * @return the end point
     */
    public Point3D end() {
        return point(theta);
    }
    
    /**
     * Constructs a new {@code Vector3D} from the center point of the cirlce of
     * the arc to the origin point on the arc.
     * @return a vector from the center to the arc's origin point
     */
    public Vector3D originVector() {
        return center.vectorTo(origin);
    }
    
    /**
     * Constructs a new {@code Vector3D} from the center point of the circle of
     * the arc to the end point on the arc.
     * @return a vector from the center to the arc's end point
     */
    public Vector3D endVector() {
        Vector3D endVector = originVector();
        normal.rotate(endVector, theta);
        return endVector;
    }
    
    /**
     * Returns the center point of the circle of the arc.
     * @return the center point of the circle
     */
    public Point3D center() {
        return center;
    }
    
    /**
     * Returns the vector normal to the circle of the arc.
     * @return the normal vector
     */
    public Vector3D normal() {
        return normal;
    }
    
    /**
     * Returns the radius of the arc.
     * @return the radius
     */
    public double radius() {
        return center.distance(origin);
    }
    
    /**
     * Returns the angle subtended by the arc.
     * @return the angle subtended
     */
    public double theta() {
        return theta;
    }
    
    /**
     * Constructs a new {@code Point3D} object by using the given value as the
     * parameter for the parametric equation describing this arc. If the value
     * is zero, then the returned point coincides with the origin point. If the
     * value is equal to theta, then the returned point coincides with the end
     * point.
     * @param t the parametric parameter
     * @return the point
     */
    public Point3D point(double t) {
        if (t<0 || theta<t) {
            System.err.println("Arc3D: Point3D point(double t): Warning t is out of range [0,theta]. The returned point is not on the arc.");
        }
        Vector3D v = new Vector3D(originVector());
        normal.rotate(v, t);
        return new Point3D(center, v);
    }
    
    public Vector3D bisect() {
        Point3D  midpoint = this.point(theta/2);
        Vector3D bisect   = center.vectorTo(midpoint).asUnit();
        return bisect;
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
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    @Override
    public void show() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Arc3D:")
                .append("\n\torigin:\t").append(origin)
                .append("\n\tend:\t").append(end())
                .append("\n\tcenter:\t").append(center)
                .append("\n\tnormal:").append(normal)
                .append("\n\tradius:\t").append(radius())
                .append("\n\ttheta:\t").append(theta)
                .toString();
    }
}
