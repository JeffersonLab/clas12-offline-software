package org.jlab.geom.prim;

import org.jlab.geom.Showable;

/**
 * An infinite unbounded 3D plane represented by a vector that is normal to the
 * plane and a reference point on the plane.
 * <p>
 * {@code Plane3D} contains intersection finding functions for 
 * {@link #intersection(org.jlab.geom.prim.Line3D, org.jlab.geom.prim.Point3D) infinite lines},
 * {@link #intersectionRay(org.jlab.geom.prim.Line3D, org.jlab.geom.prim.Point3D) rays}, 
 * {@link #intersectionSegment(org.jlab.geom.prim.Line3D, org.jlab.geom.prim.Point3D) line segments},
 * {@link #intersection(org.jlab.geom.prim.Plane3D, org.jlab.geom.prim.Line3D) one other plane}, and 
 * {@link #intersection(org.jlab.geom.prim.Plane3D, org.jlab.geom.prim.Plane3D, org.jlab.geom.prim.Point3D) two other planes}.
 * 
 * @author gavalian
 */
public final class Plane3D implements Transformable, Showable {

    private final Point3D point = new Point3D();
    private final Vector3D normal = new Vector3D();

    /**
     * Constructs a new {@code Plane3D} such that the reference point is at the
     * origin and the normal vector is a null vector.
     */
    public Plane3D() {
        // nothing to do
    }

    /**
     * Constructs a new {@code Plane3D} such that the new plane passes through
     * the given point and the normal of the new plane is parallel to the given
     * vector.
     *
     * @param point the reference point
     * @param normal the normal of the surface of the plane
     */
    public Plane3D(Point3D point, Vector3D normal) {
        set(point, normal);
    }

    /**
     * Constructs a new {@code Plane3D} such that the new plane passes through
     * the point specified by the first the given values and the normal of the
     * new plane is parallel to the the vector specified by the last three given
     * values.
     *
     * @param px the x coordinate of the point
     * @param py the y coordinate of the point
     * @param pz the z coordinate of the point
     * @param nx the x component of the normal
     * @param ny the y component of the normal
     * @param nz the z component of the normal
     */
    public Plane3D(double px, double py, double pz, double nx, double ny, double nz) {
        set(px, py, pz, nx, ny, nz);
    }

    /**
     * Constructs a new {@code Plane3D} that is equivalent to the given plane.
     *
     * @param plane the plane to copy
     */
    public Plane3D(Plane3D plane) {
        copy(plane);
    }

    /**
     * Sets the reference point and normal of this plane to coincide with the
     * reference point and normal of the given plane.
     *
     * @param plane the line to copy
     */
    public void copy(Plane3D plane) {
        set(plane.point, plane.normal);
    }

    /**
     * Sets the reference point and normal of this plane to coincide with the
     * given point and vector.
     *
     * @param point the reference point
     * @param normal the normal
     */
    public void set(Point3D point, Vector3D normal) {
        setPoint(point);
        setNormal(normal);
    }

    /**
     * Sets the normal of this plane to parallel the given vector.
     *
     * @param normal the normal
     */
    public void setNormal(Vector3D normal) {
        this.normal.copy(normal);
    }

    /**
     * Sets the reference point of this plane.
     *
     * @param point the reference point
     */
    public void setPoint(Point3D point) {
        this.point.copy(point);
    }

    /**
     * Sets the coordinates of the reference point and components of the normal
     * vector of this plane using the given values.
     *
     * @param px the x coordinate of the point
     * @param py the y coordinate of the point
     * @param pz the z coordinate of the point
     * @param nx the x component of the normal
     * @param ny the y component of the normal
     * @param nz the z component of the normal
     */
    public void set(double px, double py, double pz, double nx, double ny, double nz) {
        setPoint(px, py, pz);
        setNormal(nx, ny, nz);
    }

    /**
     * Sets the coordinates of the reference point of this plane.
     *
     * @param px the x coordinate of the point
     * @param py the y coordinate of the point
     * @param pz the z coordinate of the point
     */
    public void setPoint(double px, double py, double pz) {
        point.set(px, py, pz);
    }

    /**
     * Sets the components of the normal vector of this plane.
     *
     * @param nx the x component of the normal
     * @param ny the y component of the normal
     * @param nz the z component of the normal
     */
    public void setNormal(double nx, double ny, double nz) {
        normal.setXYZ(nx, ny, nz);
    }

    /**
     * Returns the reference point of this plane.
     *
     * @return a point on this plane
     */
    public Point3D point() {
        return point;
    }

    /**
     * Returns the normal of this plane.
     *
     * @return the normal of this plane
     */
    public Vector3D normal() {
        return normal;
    }
    /**
     * Calculates a Line3D object with origin at the given
     * point and the end at the plane, length is the distance
     * to the plane.
     * @param p point in space
     * @param distance line with end point on the plane
     */
    public void distance(Point3D p, Line3D distance){
        double sn,sd,sb;
        Vector3D diff = new Vector3D(p.x()-point.x(),p.y()-point.y(), p.z()-point.z());
        sn = -normal.dot(diff);        
        sd =  normal.dot(normal);
        sb = sn / sd;
        distance.origin().set(p.x(), p.y(), p.z());
        distance.end().set(p.x()+sb*normal.x(),
                p.y() + sb*normal.y(),p.z() + sb*normal.z());        
    }
    /**
     * Finds the intersection of the given infinite with this plane. If an
     * intersection point is found it will be stored in the given point. Return
     * codes: 0 = disjoint, 1 = intersecting, 2 = coinciding.
     *
     * @param line the infinite line
     * @param intersect the intersection point
     * @return 0 = disjoint, 1 = intersecting, 2 = coinciding
     */
    public int intersection(Line3D line, Point3D intersect) {
        if (!normal.unit()) {
            System.err.println("Warning: Plane3D: intersection(Line3D line, Point3D intersectPoint): the plane's normal is a null vector");
            return 0;
        }
        Vector3D u = line.toVector();
        Vector3D w = line.origin().vectorFrom(point);
        double D = normal.dot(u);
        double N = -normal.dot(w);
        final double SMALL_NUM = 0.00000001;
        if (Math.abs(D) < SMALL_NUM) {
            return N == 0 ? 2 : 0;
        }
        intersect.copy(line.lerpPoint(N / D));
        return 1;
    }

    /**
     * Finds the intersection of the given ray with this plane. If an
     * intersection point is found it will be stored in the given point. Return
     * codes: 0 = disjoint, 1 = intersecting, 2 = coinciding.
     *
     * @param line the ray segment
     * @param intersect the intersection point
     * @return 0 = disjoint, 1 = intersecting, 2 = coinciding
     */
    public int intersectionRay(Line3D line, Point3D intersect) {
        if (!normal.unit()) {
            System.err.println("Warning: Plane3D: intersection(Line3D line, Point3D intersectPoint): the plane's normal is a null vector");
            return 0;
        }
        Vector3D u = line.toVector();
        Vector3D w = line.origin().vectorFrom(point);
        double D = normal.dot(u);
        double N = -normal.dot(w);
        final double SMALL_NUM = 0.00000001;
        if (Math.abs(D) < SMALL_NUM) {
            return N == 0 ? 2 : 0;
        }
        double sI = N / D;
        if (sI < 0) {
            return 0;
        }
        intersect.copy(line.lerpPoint(sI));
        return 1;
    }

    /**
     * Finds the intersection of the given line segment with this plane. If an
     * intersection point is found it will be stored in the given point. Return
     * codes: 0 = disjoint, 1 = intersecting, 2 = coinciding.
     *
     * @param line the line segment
     * @param intersect the intersection point
     * @return 0 = disjoint, 1 = intersecting, 2 = coinciding
     */
    public int intersectionSegment(Line3D line, Point3D intersect) {
        if (!normal.unit()) {
            System.err.println("Warning: Plane3D: intersection(Line3D line, Point3D intersectPoint): the plane's normal is a null vector");
            return 0;
        }
        Vector3D u = line.toVector();
        Vector3D w = line.origin().vectorFrom(point);
        double D = normal.dot(u);
        double N = -normal.dot(w);
        final double SMALL_NUM = 0.00000001;
        if (Math.abs(D) < SMALL_NUM) {
            return N == 0 ? 2 : 0;
        }
        double sI = N / D;
        if (sI < 0 || sI > 1) {
            return 0;
        }
        intersect.copy(line.lerpPoint(sI));
        return 1;
    }

    /**
     * Finds the intersection of the given plane with this plane. If an
     * intersection line is found it will be stored in the given line. Return
     * codes: 0 = disjoint, 1 = intersecting, 2 = coinciding.
     *
     * @param plane the line
     * @param intersect the intersection line
     * @return 0 = disjoint, 1 = intersecting, 2 = coinciding
     */
    public int intersection(Plane3D plane, Line3D intersect) {
        Point3D p0 = this.point();
        Point3D p1 = plane.point();
        Vector3D n0 = this.normal();
        Vector3D n1 = plane.normal();

        if (!n0.unit() || !n1.unit()) {
            System.err.println("Warning: Plane3D: intersection(Plane3D plane, Line3D intersect): the plane's normal is a null vector");
            return 0;
        }

        if (Math.abs(n0.dot(n1)) == 1) { // Normals are parallel
            // Check if points are coplanar
            return n0.dot(p0.vectorTo(p1)) == 0 ? 2 : 0;
        }

        Vector3D dir = n0.cross(n1);
        dir.unit();

        double x0 = n0.x();
        double y0 = n0.y();
        double z0 = n0.z();

        double x1 = n1.x();
        double y1 = n1.y();
        double z1 = n1.z();

        double m = x0 * y1 - x1 * y0;
        double x = x0 * p0.x() + y0 * p0.y() + z0 * p0.z();
        double y = x1 * p1.x() + y1 * p1.y() + z1 * p1.z();

        double x2 = (x * y1 + y * y0) / m;
        double y2 = (y * x0 - x * x1) / m;
        double z2 = 0;
        //dir.show();
        //System.err.printf("%12.5f %12.5f %12.5f %12.5f\n", m,x2,y2,z2);
        intersect.set(x2, y2, z2, x2 + dir.x(), y2 + dir.y(), z2 + dir.z());

        return 1;
    }

    /**
     * Finds the intersection of the two given planes and this plane. If an
     * intersection point is found it will be stored in the given point. Returns
     * true if an intersection was found.
     *
     * @param plane1 the first plane
     * @param plane2 the second plane
     * @param intersect the intersection point
     * @return true if the intersection was found
     */
    public boolean intersection(Plane3D plane1, Plane3D plane2, Point3D intersect) {
        Point3D p0 = this.point();
        Point3D p1 = plane1.point();
        Point3D p2 = plane2.point();
        Vector3D n0 = this.normal();
        Vector3D n1 = plane1.normal();
        Vector3D n2 = plane2.normal();

        if (!n0.unit() || !n1.unit() || !n2.unit()) {
            System.err.println("Warning: Plane3D: intersection(Plane3D plane, Point3D intersectPoint): the plane's normal is a null vector");
            return false;
        }

        if (n0.dot(n1) == 1 || n1.dot(n2) == 1) {
            return false;
        }

        double x0 = n0.x();
        double y0 = n0.y();
        double z0 = n0.z();
        double x1 = n1.x();
        double y1 = n1.y();
        double z1 = n1.z();
        double x2 = n2.x();
        double y2 = n2.y();
        double z2 = n2.z();
        double m = x2 * y1 * z0 - x1 * y2 * z0 - x2 * y0 * z1 + x0 * y2 * z1 + x1 * y0 * z2 - x0 * y1 * z2;
        double x = x0 * p0.x() + y0 * p0.y() + z0 * p0.z();
        double y = x1 * p1.x() + y1 * p1.y() + z1 * p1.z();
        double z = x2 * p2.x() + y2 * p2.y() + z2 * p2.z();
        intersect.set(
                (x * (y2 * z1 - y1 * z2) + y * (y0 * z2 - y2 * z0) + z * (y1 * z0 - y0 * z1)) / m,
                (x * (x1 * z2 - x2 * z1) + y * (x2 * z0 - x0 * z2) + z * (x0 * z1 - x1 * z0)) / m,
                (x * (x2 * y1 - x1 * y2) + y * (x0 * y2 - x2 * y0) + z * (x1 * y0 - x0 * y1)) / m);
        return true;
    }

    @Override
    public void translateXYZ(double x, double y, double z) {
        point.translateXYZ(x, y, z);
    }

    @Override
    public void rotateX(double angle) {
        point.rotateX(angle);
        normal.rotateX(angle);
    }

    @Override
    public void rotateY(double angle) {
        point.rotateY(angle);
        normal.rotateY(angle);
    }

    @Override
    public void rotateZ(double angle) {
        point.rotateZ(angle);
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
        return String.format("Plane3D:\n\tpoint:\t%12.5f %12.5f %12.5f\n\tnormal:\t%12.5f %12.5f %12.5f)",
                point.x(), point.y(), point.z(),
                normal.x(), normal.y(), normal.z());
    }
}
