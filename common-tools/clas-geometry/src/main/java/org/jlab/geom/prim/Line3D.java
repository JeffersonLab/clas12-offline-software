package org.jlab.geom.prim;

import org.jlab.geom.Showable;

/**
 * A 3D line represented by two points, the origin point and the end point. The
 * line can be treated as an infinite line, a ray bounded by only the origin
 * point, or a line segment bounded by both the origin point and the end point.
 * <p>
 * The direction of the line can be obtained via {@link #toVector()}. Points on
 * the line can be obtained via {@link #lerpPoint(double)}. Distances from the a
 * line to a point and from one line to another line can be obtained via
 * {@link #distance(org.jlab.geom.prim.Point3D) distance(Point3D)} and
 * {@link #distance(org.jlab.geom.prim.Line3D) distance(Line3D)}.
 *
 * @author gavalian
 */
public final class Line3D implements Transformable, Showable {
    
    private final Point3D boundPoint0 = new Point3D(); // the origin point
    private final Point3D boundPoint1 = new Point3D(); // the end point

    /**
     * Constructs a new {@code Line3D} such that the new {@code Line3D}'s
     * bounding points both coincide at the origin. This line will have zero
     * length.
     */
    public Line3D() {
        // nothing to do
    }

    /**
     * Constructs a new such {@code Line3D} that the new {@code Line3D}'s
     * bounding points coincide with the points specified by the given
     * coordinates.
     *
     * @param x0 x component of the origin point
     * @param y0 y component of the origin point
     * @param z0 z component of the origin point
     * @param x1 x component of the end point
     * @param y1 y component of the end point
     * @param z1 z component of the end point
     */
    public Line3D(double x0, double y0, double z0, double x1, double y1, double z1) {
        set(x0, y0, z0, x1, y1, z1);
    }

    /**
     * Constructs a new {@code Line3D} such that the new {@code Line3D}'s
     * bounding points coincide with the given points.
     *
     * @param point0 the origin point
     * @param point1 the end point
     */
    public Line3D(Point3D point0, Point3D point1) {
        set(point0, point1);
    }

    /**
     * Constructs a new {@code Line3D} such that the the given point is at the
     * origin of the line and the vector from the origin to the end of the line
     * is equal to the given vector.
     *
     * @param point a point on the line
     * @param direction the direction of the line
     */
    public Line3D(Point3D point, Vector3D direction) {
        set(point, direction);
    }

    /**
     * Constructs a new {@code Line3D} such that the new {@code Line3D}'s
     * bounding points coincide with the given line's bounding points.
     *
     * @param line the line to copy
     */
    public Line3D(Line3D line) {
        copy(line);
    }

    /**
     * Sets the bounding points of this line to coincide with the bounding
     * points of the given line.
     *
     * @param line the line to copy
     */
    public void copy(Line3D line) {
        set(line.boundPoint0, line.boundPoint1);
    }

    /**
     * Sets the line origin at the given point and the end point at the position
     * of the origin point plus the given vector.
     *
     * @param point the origin point
     * @param direction a vector from the origin point to the end point
     */
    public void set(Point3D point, Vector3D direction) {
        set(point.x(), point.y(), point.z(),
                point.x() + direction.x(),
                point.y() + direction.y(),
                point.z() + direction.z());
    }

    /**
     * Sets the line origin and end point to given points.
     *
     * @param point0 the new origin point
     * @param point1 the new end point
     */
    public void set(Point3D point0, Point3D point1) {
        setOrigin(point0);
        setEnd(point1);
    }

    /**
     * Sets the origin point.
     *
     * @param point0 the new origin point
     */
    public void setOrigin(Point3D point0) {
        boundPoint0.copy(point0);
    }

    /**
     * Sets the end point.
     *
     * @param point1 the new end point
     */
    public void setEnd(Point3D point1) {
        boundPoint1.copy(point1);
    }

    /**
     * Sets the origin and end points using the given coordinates.
     *
     * @param x0 x component of the origin point
     * @param y0 y component of the origin point
     * @param z0 z component of the origin point
     * @param x1 x component of the end point
     * @param y1 y component of the end point
     * @param z1 z component of the end point
     */
    public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
        setOrigin(x0, y0, z0);
        setEnd(x1, y1, z1);
    }

    /**
     * Sets the origin point.
     *
     * @param x the x coordinate of the origin point
     * @param y the y coordinate of the origin point
     * @param z the z coordinate of the origin point
     */
    public void setOrigin(double x, double y, double z) {
        boundPoint0.set(x, y, z);
    }

    /**
     * Sets the end point.
     *
     * @param x the x coordinate of the end point
     * @param y the y coordinate of the end point
     * @param z the z coordinate of the end point
     */
    public void setEnd(double x, double y, double z) {
        boundPoint1.set(x, y, z);
    }

    /**
     * Returns the origin point.
     *
     * @return the origin point
     */
    public Point3D origin() {
        return boundPoint0;
    }

    public Vector3D originDir(){
        Vector3D vec = new Vector3D();
        vec.setXYZ(boundPoint0.x()-boundPoint1.x(),
                boundPoint0.y()-boundPoint1.y(),
                boundPoint0.z()-boundPoint1.z());
        vec.unit();
        return vec;
    }
    /**
     * Returns the end point.
     *
     * @return the end point
     */
    public Point3D end() {
        return boundPoint1;
    }

    /**
     * Returns the length of this line.
     *
     * @return the distance from one bounding point to the other
     */
    public double length() {
        return boundPoint0.distance(boundPoint1);
    }
    
    /**
     * Constructs a new {@code Point3D} at the midpoint between the two bounding
     * points.
     *
     * @return the midpoint of this line
     */
    public Point3D midpoint() {
        return boundPoint0.midpoint(boundPoint1);
    }

    /**
     * Constructs a new {@code Point3D} on the line by linearly interpolating from this
     * point to the given point by the amount specified. If t==0, then the
     * returned point is equal to the origin point. If t==0.5, then the returned
     * point half way between both points. If t==1, then the returned point is
     * equal to the end point.
     *
     * @param t the interpolation coefficient
     * @return the newly constructed point
     */
    public Point3D lerpPoint(double t) {
        return boundPoint0.lerp(boundPoint1, t);
    }

    /**
     * Returns a vector from the origin point to the end point.
     *
     * @return a vector from the origin point to the end point
     */
    public Vector3D toVector() {
        return boundPoint0.vectorTo(boundPoint1);
    }

    /**
     * Constructs a new {@code Line3D} from this infinite line to the given point
     * such that the origin point of the constructed line coincides with this
     * infinite line, the end point of the constructed line coincides with the
     * given point, and the length of the constructed line is minimal.
     *
     * @param point the given point
     * @return the line of closest approach
     */
    public Line3D distance(Point3D point) {
        // Find the vector from this line's origin point to this line's
        // end point
        Vector3D v = toVector();
        // Find the vector from this line's origin point to the given point
        Vector3D w = boundPoint0.vectorTo(point);

        double c1 = w.dot(v);
        double c2 = v.dot(v);

        // Calculate the fraction distance between this line's two bounding points
        // for the remaining point. Note that this number will be between in the
        // range [0, 1].
        double b = c1 / c2;
        // Calculate the position of the remaining point
        return new Line3D(lerpPoint(b), point);
    }

    /**
     * Constructs a new {@code Line3D} from this ray line to the given point such
     * that the origin point of the constructed line coincides with this ray
     * line, the end point of the constructed line coincides with the given
     * point, and the length of the constructed line is minimal.
     *
     * @param point the given point
     * @return the line of closest approach
     */
    public Line3D distanceRay(Point3D point) {
        // Find the vector from this line's origin point to this line's
        // end point
        Vector3D v = toVector();
        // Find the vector from this line's origin point to the given point
        Vector3D w = boundPoint0.vectorTo(point);
        double c1 = w.dot(v);
        if (c1 <= 0) {
            // The given point lies behind this line's origin point, so the
            // remaining point of the line of closest appproach should be at
            // this line's origin point
            return new Line3D(boundPoint0, point);
        }
        double c2 = v.dot(v);
        // Calculate the fraction distance between this line's two bounding points
        // for the remaining point. Note that this number will be between in the
        // range [0, 1].
        double b = c1 / c2;
        // Calculate the position of the remaining point
        return new Line3D(lerpPoint(b), point);
    }

    /**
     * Constructs a new {@code Line3D} from this line segment to the given point
     * such that the origin point of the constructed line coincides with this
     * line segment, the end point of the constructed line coincides with the
     * given point, and the length of the constructed line is minimal.
     *
     * @param point the given point
     * @return the line of closest approach
     */
    public Line3D distanceSegment(Point3D point) {
        // Find the vector from this line's origin point to this line's
        // end point
        Vector3D v = toVector();
        // Find the vector from this line's origin point to the given point
        Vector3D w = boundPoint0.vectorTo(point);
        double c1 = w.dot(v);
        if (c1 <= 0) {
            // The given point lies behind this line's origin point, so the
            // remaining point of the line of closest appproach should be at
            // this line's origin point
            return new Line3D(boundPoint0, point);
        }
        double c2 = v.dot(v);
        if (c2 <= c1) {
            // The given point lies beyond this line's end point, so the
            // remaining point of the line of closest appproach should be at
            // this line's end point
            return new Line3D(boundPoint1, point);
        }
        // Calculate the fraction distance between this line's two bounding points
        // for the remaining point. Note that this number will be between in the
        // range [0, 1].
        double b = c1 / c2;
        // Calculate the position of the remaining point
        return new Line3D(lerpPoint(b), point);
    }

    /**
     * Constructs a new {@code Line3D} from this line to the given line such
     * that the origin point of the constructed line coincides with this line,
     * the end point of the constructed line coincides with the given line, and
     * the length of the constructed line is minimal.
     *
     * @param line the given infinite line
     * @return the line of closest approach
     */
    public Line3D distance(Line3D line) {
        Vector3D u = this.toVector();
        Vector3D v = line.toVector();
        Vector3D w = this.boundPoint0.vectorFrom(line.boundPoint0);
        double a = u.dot(u);   // always >= 0
        double b = u.dot(v);
        double c = v.dot(v);   // always >= 0
        double d = u.dot(w);
        double e = v.dot(w);
        double D = a * c - b * b;  // always >= 0
        double sc, sN, sD = D; // sc = sN / sD, default sD = D >= 0
        double tc, tN, tD = D; // tc = tN / tD, default tD = D >= 0

        // compute the line parameters of the two closest points
        final double SMALL_NUM = 0.00000001;
        if (D < SMALL_NUM) {
            // the lines are almost parallel
            sN = 0; // force using point P0 on segment S1
            sD = 1; // to prevent possible division by 0 later
            tN = e;
            tD = c;
        } else {
            // the lines are not parallel
            // get the closest points on the infinite lines
            sN = (b * e - c * d);
            tN = (a * e - b * d);
        }

        // finally do the division to get sc and tc
        sc = (Math.abs(sN) < SMALL_NUM ? 0 : sN / sD);
        tc = (Math.abs(tN) < SMALL_NUM ? 0 : tN / tD);

        return new Line3D(this.lerpPoint(sc), line.lerpPoint(tc));
    }

    /**
     * Constructs a new {@code Line3D} from this line segment to the given line
     * segment such that the origin point of the constructed line coincides with
     * this line segment, the end point of the constructed line segment
     * coincides with the given line segment, and the length of the constructed
     * line is minimal.
     *
     * @param line the given line segment
     * @return the line of closest approach
     */
    public Line3D distanceSegments(Line3D line) {
        Vector3D u = this.toVector();
        Vector3D v = line.toVector();
        Vector3D w = this.boundPoint0.vectorFrom(line.boundPoint0);
        double a = u.dot(u);   // always >= 0
        double b = u.dot(v);
        double c = v.dot(v);   // always >= 0
        double d = u.dot(w);
        double e = v.dot(w);
        double D = a * c - b * b;  // always >= 0
        double sc, sN, sD = D; // sc = sN / sD, default sD = D >= 0
        double tc, tN, tD = D; // tc = tN / tD, default tD = D >= 0

        // compute the line parameters of the two closest points
        final double SMALL_NUM = 0.00000001;
        if (D < SMALL_NUM) {
            // the lines are almost parallel
            sN = 0; // force using point P0 on segment S1
            sD = 1; // to prevent possible division by 0 later
            tN = e;
            tD = c;
        } else {
            // the lines are not parallel
            // get the closest points on the infinite lines
            sN = (b * e - c * d);
            tN = (a * e - b * d);
            if (sN < 0) {
                // sc < 0 => the s=0 edge is visible
                sN = 0;
                tN = e;
                tD = c;
            } else if (sN > sD) {
                // sc > 1  => the s=1 edge is visible
                sN = sD;
                tN = e + b;
                tD = c;
            }
        }

        if (tN < 0) {
            // tc < 0 => the t=0 edge is visible
            tN = 0;
            // recompute sc for this edge
            if (-d < 0) {
                sN = 0;
            } else if (-d > a) {
                sN = sD;
            } else {
                sN = -d;
                sD = a;
            }
        } else if (tN > tD) {
            // tc > 1 => the t=1 edge is visible
            tN = tD;
            // recompute sc for this edge
            if ((-d + b) < 0) {
                sN = 0;
            } else if ((-d + b) > a) {
                sN = sD;
            } else {
                sN = (-d + b);
                sD = a;
            }
        }

        // finally do the division to get sc and tc
        sc = (Math.abs(sN) < SMALL_NUM ? 0 : sN / sD);
        tc = (Math.abs(tN) < SMALL_NUM ? 0 : tN / tD);

        return new Line3D(this.lerpPoint(sc), line.lerpPoint(tc));
    }

    @Override
    public void translateXYZ(double x, double y, double z) {
        boundPoint0.translateXYZ(x, y, z);
        boundPoint1.translateXYZ(x, y, z);
    }

    @Override
    public void rotateX(double angle) {
        boundPoint0.rotateX(angle);
        boundPoint1.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        boundPoint0.rotateY(angle);
        boundPoint1.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        boundPoint0.rotateZ(angle);
        boundPoint1.rotateZ(angle);
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
                .append("Line3D:\n\t")
                .append(boundPoint0)
                .append("\n\t")
                .append(boundPoint1)
                .toString();
    }

    /**
     * direction vector from begin to end point
     **/
    public Vector3D direction() {
        return boundPoint1.toVector3D().sub(boundPoint0.toVector3D());
    }

    public Point3D projection(Point3D p) {
        return this.projection(p.toVector3D()).toPoint3D();
    }

    /**
     * projection of a point onto this line
     **/
    public Vector3D projection(Vector3D v)
    {
        // plane perpendicular to line, which contains point v
        Plane3D p = new Plane3D(v.toPoint3D(), this.direction());

        // find intersection of line and plane
        Point3D i = new Point3D();
        p.intersection(this,i);
        return i.toVector3D();
    }

}
