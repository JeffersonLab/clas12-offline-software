package org.jlab.geom.prim;

import static java.lang.Math.*;

import org.jlab.geom.Showable;

/**
 * A 3D vector represented by three Cartesian coordinates (x, y, z).
 * <p>
 * Though internally represented in Cartesian coordinates, the position of a
 * point can be set using spherical coordinates via
 * {@link #setMagThetaPhi(double, double, double)}, and the spherical
 * coordinates of any vector can be obtained via {@link #mag()},
 * {@link #theta()}, and {@link #phi()}.
 * <p>
 * In addition to containing functions for performing normal mathematical
 * procedures involving vectors such as {@link #negative() negate/inverse},
 * {@link #add(org.jlab.geom.prim.Vector3D) addition},
 * {@link #sub(org.jlab.geom.prim.Vector3D) subtraction},
 * {@link #scale(double) scalar products},
 * {@link #dot(org.jlab.geom.prim.Vector3D) dot products},
 * {@link #cross(org.jlab.geom.prim.Vector3D) cross products}, and
 * {@link #unit() normalization}, {@code Vector3D} can be used to rotate a
 * point or vector around an arbitrary axes that passes through the origin via
 * {@link #rotate(org.jlab.geom.prim.Point3D, double) rotate(Point3D, doubele)} and
 * {@link #rotate(org.jlab.geom.prim.Vector3D, double) rotate(Vector3D, double)}.
 * The inverse of the rotation functions is provided via
 * {@link #angle(Vector3D, Vector3D)}.
 *
 * @author gavalian
 */
public final class Vector3D implements Transformable, Showable {
    private double x; // the x component
    private double y; // the y component
    private double z; // the z component

    /**
     * Constructs a new empty {@code Vector3D} that is a null vector, ie
     * (0,0,0).
     */
    public Vector3D() {
        setXYZ(0, 0, 0);
    }
    /**
     * Constructs a new {@code Vector3D} with the given x, y, and z components.
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public Vector3D(double x, double y, double z) {
        setXYZ(x, y, z);
    }
     /**
     * Constructs a new {@code Vector3D} with the given xyz[0], xyz[1], xyz[2] components
     * @param xyz where x/y/z-components are xyz[0]/[1]/[2]
     */
    public Vector3D(double[] xyz) {
        setXYZ(xyz[0], xyz[1], xyz[2]);
    }
    /**
     * Constructs a new {@code Vector3D} identical to the given vector.
     * @param vector the vector to copy
     */
    public Vector3D(Vector3D vector) {
        copy(vector);
    }

    /**
     * Sets the components of this vector to be equal the components of the
     * given vector.
     * @param vector the vector to copy
     */
    public void copy(Vector3D vector) {
        setXYZ(vector.x, vector.y, vector.z);
    }

    /**
     * Returns a new instance of this vector.
     */
    public Vector3D clone() {
        return new Vector3D(x,y,z);
    }

    /**
     * Creates a new Vector3D instance from spherical coordinates
     * @param r radial distance from the origin
     * @param phi azimuthal angle from the positive x-axis (right-handed)
     * @param theta polar angle from the positive z-axis
     */
    public static Vector3D fromSpherical(double r, double phi, double theta) {
        final double tol = 1e-6;

        if (r < 0)
        {
            r = abs(r);
            phi += PI;
            theta = PI - theta;
        }

        // phi in (-pi,pi)
        phi = IEEEremainder(phi, 2.*PI);
        if (abs(phi) >= PI)
        {
            if (phi < 0) {
                phi -= 2.*PI;
            } else {
                phi += 2.*PI;
            }
        }
        if (abs(phi) < tol)
        {
            phi = 0.;
        }

        // theta in [0,pi)
        theta = abs(theta);
        if (theta > PI)
        {
            theta = IEEEremainder(theta, 2.*PI);
            if (theta >= PI)
            {
                theta = 2.*PI - theta;
            }
        }
        if (theta < tol)
        {
            phi = 0.;
            theta = 0.;
        }

        double rho = r * sin(theta);
        return new Vector3D(rho*cos(phi), rho*sin(phi), r*cos(theta));
    }

    /**
     * Sets the x, y and z components of this vector.
     * @param x the x component
     * @param y the y component
     * @param z the z component
     */
    public void setXYZ(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }
    /**
     * Sets the x component.
     * @param x the x component
     */
    public void setX(double x) {
        this.x = x;
    }
    /**
     * Sets the y component.
     * @param y the y component
     */
    public void setY(double y) {
        this.y = y;
    }
    /**
     * Sets the z component.
     * @param z the z component
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * Returns the x component.
     * @return the x component
     */
    public double x() {
        return x;
    }
    /**
     * Returns the y component.
     * @return the y component
     */
    public double y() {
        return y;
    }
    /**
     * Returns the z component.
     * @return the z component
     */
    public double z() {
        return z;
    }

    public double r2() {
        return x*x + y*y + z*z;
    }
    public double r() {
        return sqrt(this.r2());
    }
    /**
     * Returns the angle between the x axis and the orthogonal projection of
     * this vector onto the x-y plane.
     * @return the phi component of this vector's spherical coordinates
     */
    public double phi() {
        return atan2(y,x);
    }
    public double costheta() {
        final double tol = 1e-6;
        double l = this.r();
        double ret;
        if (l < (2.*tol)) {
            ret = 1.0;
        } else {
            ret = z / l;
        }
        return ret;
    }
    /**
     * Returns the angle between this vector and the z axis.
     * @return the theta component of this vector's spherical coordinates
     */
    public double theta() {
        return acos(this.costheta());
    }

    public double rho2() {
        return x*x + y*y;
    }
    /**
     * Returns the length of the orthogonal projection of this vector onto the
     * x-y plane.
     * @return sqrt(x*x + y*y)
     */
    public double rho() {
        return sqrt(this.rho2());
    }


    /**
     * Returns true if this vector is a null vector.
     * @return true if this vector is a null vector
     */
    public boolean isNull() {
        return x==0 && y==0 && z==0;
    }

    /**
     * Returns the square of the magnitude of this vector.
     * @return the square of the magnitude of this vector
     */
    public double mag2() {
        return x*x + y*y + z*z;
    }
    /**
     * Returns the magnitude of this vector.
     * @return the magnitude of this vector
     */
    public double mag() {
        return Math.sqrt(mag2());
    }

    /**
     * Sets this vector based on the given spherical coordinates.
     * @param mag   the magnitude
     * @param theta the angle between the vector and the z axis
     * @param phi   the angle between the x axis and the orthogonal projection
     *              of the vector onto the x-y plane
     */
    public void setMagThetaPhi(double mag, double theta , double phi) {
        double st = Math.sin(theta);
        double ct = Math.cos(theta);
        double sp = Math.sin(phi);
        double cp = Math.cos(phi);
        x = mag * st * cp;
        y = mag * st * sp;
        z = mag * ct;
    }

    /**
     * Negates this vector's components.
     */
    public void negative() {
        x = -x;
        y = -y;
        z = -z;
    }
    /**
     * Adds the given vector to this vector.
     * @param vector the vector to add to this vector
     */
    public Vector3D add(Vector3D vector) {
        x += vector.x;
        y += vector.y;
        z += vector.z;
        return this;
    }
    /**
     * Subtracts the given vector from this vector
     * @param vector the vector to subtract from this vector
     */
    public Vector3D sub(Vector3D vector) {
        x -= vector.x;
        y -= vector.y;
        z -= vector.z;
        return this;
    }
    /**
     * Scales each of this vectors components by the given scale factor.
     * @param factor the amount to scale the components of this vector
     */
    public void scale(double factor) {
        x = factor*x;
        y = factor*y;
        z = factor*z;
    }
    /**
     * Sets the magnitude of this vector to equal one unless this vector is a
     * null vector.
     * @return returns false if this vector is a null vector
     */
    public boolean unit() {
        double m = mag();
        if (m == 0)
            return false;
        scale(1/m);
        return true;
    }
    /**
     * Create unit vector from this
     * @return unit vector
     **/
    public Vector3D asUnit() {
        Vector3D u = this.clone();
        u.unit();
        return u;
    }

    /**
     * Sets the magnitude of this vector to equal the given magnitude unless
     * vector is a null vector.
     * @param mag the desired magnitude
     * @return returns false if this vector is a null vector
     */
    public boolean setMag(double mag) {
        double m = mag();
        if (m == 0)
            return false;
        scale(mag/m);
        return true;
    }

    public Vector3D add(double a) {
        return new Vector3D(this.x+a, this.y+a, this.z+a);
    }
    public Vector3D subtract(double a) {
        return new Vector3D(this.x-a, this.y-a, this.z-a);
    }
    public Vector3D multiply(double a) {
        return new Vector3D(this.x*a, this.y*a, this.z*a);
    }
    public Vector3D divide(double a) {
        return new Vector3D(this.x/a, this.y/a, this.z/a);
    }

    /**
     * Returns the dot product of this vector and the given vector.
     * @param vector the vector to dot product with this vector
     * @return the dot product
     */
    public double dot(Vector3D vector) {
        return x*vector.x + y*vector.y + z*vector.z;
    }
    /**
     * Constructs a new {@code Vector3D} containing the cross product of this vector and
     * the given vector.
     * @param vector the vector to cross product with this vector
     * @return the cross product
     */
    public Vector3D cross(Vector3D vector) {
        return new Vector3D(
                y*vector.z-z*vector.y,
                z*vector.x-x*vector.z,
                x*vector.y-y*vector.x);
    }

    /**
     * Constructs a new {@code Point3D} using this vector's x, y, and z
     * components.
     * @return a point representation of this vector
     */
    public Point3D toPoint3D() {
        return new Point3D(x, y, z);
    }

    /**
     * Returns a value greater than or equal to zero proportional to the amount
     * that the two vectors differ. If the two vectors are equal then the
     * returned value is 0.
     * @param vector the vector to compare to
     * @return the comparison result
     */
    public double compare(Vector3D vector) {
        double quality = 0.0;
        quality += Math.abs(x-vector.x)/Math.abs(x);
        quality += Math.abs(y-vector.y)/Math.abs(y);
        quality += Math.abs(z-vector.z)/Math.abs(z);
        return quality;
    }

    /**
     * Rotates the given point clockwise around the axis produced by this
     * vector by the given angle
     * @param point the point to rotate
     * @param angle the angle of rotation
     */
    public void rotate(Point3D point, double angle) {
        Vector3D vector = point.toVector3D();
        rotate(vector, angle);
        point.set(vector.x, vector.y, vector.z);
    }

    /**
     * Rotates the given vector clockwise  around the axis produced by this
     * vector by the given angle.
     * @param vector the vector to rotate
     * @param angle the angle of rotation
     */
    public void rotate(Vector3D vector, double angle) {
        double m = vector.mag();
        Vector3D N = new Vector3D(this);
        N.unit();
        Vector3D A = N.cross(vector);
        Vector3D B = A.cross(N);
        A.scale(Math.sin(angle));
        B.scale(Math.cos(angle));
        double n = N.dot(vector);
        vector.copy(N);
        vector.setMag(n);
        vector.add(A);
        vector.add(B);
        vector.setMag(m);
        System.out.println(Math.toDegrees(angle));
        System.out.println(A);
        System.out.println(B);
        System.out.println(vector);
    }

    /**
     * Calculates the clockwise angle of rotation from the image of the first
     * given vector to the image of the second given vector projected onto a
     * plane who's normal is this vector and using this vector as an axis.
     * @param vector0 the first vector
     * @param vector1 the second vector
     * @return the angle between the two vectors
     */
    public double angle(Vector3D vector0, Vector3D vector1) {
        Vector3D N = new Vector3D(this);
        N.unit();
        Vector3D A = N.cross(vector0);
        Vector3D B = N.cross(vector1);
        double angle = Math.atan2(N.dot(A.cross(B)), A.dot(B));
        return (angle+Math.PI*2)%(Math.PI*2);
    }

    /**
     * Projection of this vector onto another vector
     * @param v another vector
     * @return projection of this vector onto v
     */
    public Vector3D projection(Vector3D v) {
        final double tol = 1e-6;
        double r2 = v.r2();
        if (r2 > tol) {
            return v.multiply(this.dot(v) / v.r2());
        } else {
            return this.clone();
        }
    }

    /**
     * Angle between this and another vector
     * @param v another vector
     * @return the angle between the two vectors
     */
    public double angle(Vector3D v) {
        final double tol = 1e-9;
        double a = sqrt(this.r2() * v.r2());
        if (a > tol) {
            double dotprod = this.dot(v);
            double cosangle = dotprod / a;
            if(cosangle<0){
                if (abs(cosangle) > (1-tol)) {
                    return Math.PI;
                } else {
                    return acos(cosangle);
                }
            } else {
                if (abs(cosangle) > (1-tol)) {
                    return 0.;
                } else {
                    return acos(cosangle);
                }
            }            
        } else {
            return 0.;
        }
    }

    /**
     * Does nothing; vectors cannot be translated.
     * @param x ignored
     * @param y ignored
     * @param z ignored
     */
    @Override
    public void translateXYZ(double x, double y, double z) {
        // do nothing; vectors cannot be translated
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
     * Invokes {@code System.out.println(this)}.
     */
    @Override
    public void show() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("Vector3D:\t%12.5f %12.5f %12.5f", x, y, z);
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
    
    public static void main(String[] args){
        Vector3D  vec = new Vector3D(1.0,0.0,0.0);
        Transformation3D transform = new Transformation3D();
        
        transform.rotateZ(Math.toRadians(25.0));
        transform.rotateX(Math.toRadians(30.0));
        
        transform.show();
        
        transform.apply(vec);
        
        System.out.println(vec);
        System.out.println(vec.mag());
        
        Vector3D vec1 = new Vector3D(0.0,0.0,1.0);
        Vector3D vec2 = new Vector3D(0.0,0.0,1.0);
        System.out.println("angle = " + vec1.angle(vec2));
    }
}
