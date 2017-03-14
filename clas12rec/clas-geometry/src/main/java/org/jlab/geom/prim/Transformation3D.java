package org.jlab.geom.prim;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.Showable;

/**
 * A sequence of rotations and translations that can be performed on a objects
 * implementing the {@link org.jlab.geom.prim.Transformable} interface.
 * <p>
 * To use {@code Transformation3D} object to rotate and translate a 
 * {@code Transformable} object, use the {@link #apply(org.jlab.geom.prim.Transformable)} 
 * method.
 * <p>
 * If a {@code Transformation3D} object is constructed with no arguments, then
 * it will contain no transformations and will in essence represent an identity
 * transformation aka do-nothing transformation.
 * <p>
 * The {@link #translateXYZ(double, double, double)}, {@link #rotateX(double)},
 * {@link #rotateY(double)}, {@link #rotateZ(double)}, and
 * {@link #append(Transformation3D)} methods all modify the object from which
 * they are invoked, but these functions also return a copy of the object so
 * that rotations and translations can be chained together and written on a 
 * single line.
 * <p>
 * Ex.<br>
 * <code>
 * &nbsp;&nbsp;Transformation3D() xform = new Transformation3D(); // identity<br>
 * &nbsp;&nbsp;xform.translateXYZ(100, 0, 20).rotateZ(Math.PI);
 * </code>
 * <p>
 * In this example, after the code has executed {@code xform} will be a
 * transformation that first translates an object by x+100 and z+20, and then
 * rotates the object by 180 degrees clockwise around the z-axis.
 * <p>
 * The inverse of a transformation can be obtained via {@link #inverse()}.
 * <p>
 * Ex.<br>
 * <code>
 * &nbsp;&nbsp;Transformation3D inv = xform.inverse();<br>
 * &nbsp;&nbsp;Point3D pt = new Point3D(3, 5, 7);<br>
 * &nbsp;&nbsp;xform.apply(pt);<br>
 * &nbsp;&nbsp;inv.apply(pt);
 * </code>
 * <p>
 * At the end of this example, {@code pt} should equal to (3, 5, 7), however 
 * extremely small rounding errors may be introduced due to the nature of 
 * floating point arithmetic (but no more than 1 ulp per coordinate per 
 * transformation).
 * 
 * @author jnhankins
 */
public class Transformation3D implements Showable {
    private final ArrayList<Transform> transforms;
    
    /**
     * Constructs a new empty {@code Transformation3D} that is equivalent to an
     * identity transformation.
     */
    public Transformation3D() {
        transforms = new ArrayList();
    }
    
    /**
     * Constructs a new {@code Transformation3D} identical the the given 
     * transformation.
     * @param transform the apply to copy
     */
    public Transformation3D(Transformation3D transform) {
        transforms = new ArrayList(transform.transforms);
    }
    
    /**
     * Sets this transformation to be equal to the given transformation.
     * @param transform the transformation to copy
     * @return a reference to this object
     */
    public Transformation3D copy(Transformation3D transform) {
        transforms.clear();
        transforms.addAll(transform.transforms);
        return this;
    }
    
    /**
     * Appends a translation to this transformation.
     * @param dx amount to translate along the x axis
     * @param dy amount to translate along the y axis
     * @param dz amount to translate along the z axis
     * @return a reference to this object
     */
    public Transformation3D translateXYZ(double dx, double dy, double dz) {
        transforms.add(new TranslationXYZ(dx, dy, dz));
        return this;
    }
    
    /**
     * Appends a clockwise rotation around the x axis to this transformation.
     * @param angle rotation angle in radians
     * @return a reference to this object
     */
    public Transformation3D rotateX(double angle) {
        transforms.add(new RotationX(angle));
        return this;
    }
    
    /**
     * Appends a clockwise rotation around the y axis to this transformation.
     * @param angle rotation angle in radians
     * @return a reference to this object
     */
    public Transformation3D rotateY(double angle) {
        transforms.add(new RotationY(angle));
        return this;
    }

    /**
     * Appends a clockwise rotation around the z axis to this transformation.
     * @param angle rotation angle in radians
     * @return a reference to this object
     */
    public Transformation3D rotateZ(double angle) {
        transforms.add(new RotationZ(angle));
        return this;
    }
    
    /**
     * Appends the given apply to this transformation.
     * @param trans the apply to append
     * @return a reference to this object
     */
    public Transformation3D append(Transform trans) {
        transforms.add(trans);
        return this;
    }
    
    /**
     * Appends a copy of the given transformation to this transformation.
     * @param transformation the transformation append
     * @return a reference to this object
     */
    public Transformation3D append(Transformation3D transformation) {
        for (Transform trans : transformation.transforms)
            transforms.add(trans);
        return this;
    }
    
    /**
     * Modifies the given {@link org.jlab.geom.prim.Transformable Transformable}
     * object by applying the transformation represented by this 
     * {@code Transformation3D} to the given object.
     * @param obj the object to apply this transformation to
     */
    public void apply(Transformable obj) {
        for (Transform transform : transforms) {
            transform.apply(obj);
        }
    }
    
    /**
     * Resets this transformation by removing all of its transforms.
     */
    public void clear() {
        transforms.clear();
    }
    
    /**
     * Constructs a new {@code Transformation3D} that is the inverse of this
     * transformation.
     * @return the inverse of this transformation
     */
    public Transformation3D inverse() {
        Transformation3D inv = new Transformation3D();
        for (int i=transforms.size()-1; i>=0; i--)
            inv.append(transforms.get(i).inverse());
        return inv;
    }

    /**
     * Returns a reference to this {@code Transformation3D}'s 
     * {@link org.jlab.geom.prim.Transformation3D.Transform Transform} sequence.
     * Modifying the returned list will modify this {@code Transformation3D}.
     * @return the transform sequence
     */
    public List<Transform> transformSequence() {
        return transforms;
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
        StringBuilder str = new StringBuilder("Transformation3D::");
        if (transforms.isEmpty())
            str.append(" Identity");
        else for (Transform xform : transforms)
            str.append("\n\t").append(xform);
        return str.toString();
    }
    
    /**
     * A interface for geometric transformations (eg translations and rotations)
     * that can be applied to a {@code Transformable} object.
     */
    public static interface Transform {
        /**
         * Modifies the given {@code Transformable} object by applying the
         * transformation represented by this transform.
         * @param obj the object to apply the transform to
         */
        public abstract void apply(Transformable obj);
        
        /**
         * Constructs a new {@code Transform} that is the inverse of this
         * transform.
         * @return the inverse of this transform
         */
        public abstract Transform inverse();
        public abstract String    getName();
        public abstract double    getValue(int index);
    }
    
    /**
     * A translation transformation.
     */
    public static class TranslationXYZ implements Transform {
        final double dx;
        final double dy;
        final double dz;
        
        TranslationXYZ(double dx, double dy, double dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }
        
        @Override
        public void apply(Transformable obj) {
            obj.translateXYZ(dx, dy, dz);
        }

        @Override
        public Transform inverse() {
            return new TranslationXYZ(-dx, -dy, -dz);
        }
        
        @Override
        public String toString() {
            return "Translate:\t("+dx+", "+dy+", "+dz+")";
        }

        public String getName() {
            return "xyx";
        }
        
        public double getValue(int index){
            switch(index){
                case 0: return this.dx;
                case 1: return this.dy;
                case 2: return this.dz;                    
            }
            return 0;
        }
    }
    
    /**
     * A clockwise rotation around the x-axis.
     */
    public static class RotationX implements Transform {
        final double angle;
        
        RotationX(double angle) {
            this.angle = angle;
        }
        
        @Override
        public void apply(Transformable obj) {
            obj.rotateX(angle);
        }

        @Override
        public Transform inverse() {
            return new RotationX(-angle);
        }
        
        @Override
        public String toString() {
            return "Rotate-X: \t"+angle+" rad";
        }

        public String getName() {
            return "rx";
        }
        
        public double getValue(int index){
             return this.angle;
        }
    }
    
    /**
     * A clockwise rotation around the y-axis.
     */
    public static class RotationY implements Transform {
        final double angle;
        
        RotationY(double angle) {
            this.angle = angle;
        }
        
        @Override
        public void apply(Transformable obj) {
            obj.rotateY(angle);
        }

        @Override
        public Transform inverse() {
            return new RotationY(-angle);
        }
        
        @Override
        public String toString() {
            return "Rotate-Y: \t"+angle+" rad";
        }

        public String getName() {
            return "ry";
        }
        
        public double getValue(int index){
             return this.angle;
        }
    }
    
    /**
     * A clockwise rotation around the z-axis.
     */
    public static class RotationZ implements Transform {
        final double angle;
        
        RotationZ(double angle) {
            this.angle = angle;
        }
        
        @Override
        public void apply(Transformable obj) {
            obj.rotateZ(angle);
        }

        @Override
        public Transform inverse() {
            return new RotationZ(-angle);
        }
        
        @Override
        public String toString() {
            return "Rotate-Z: \t"+angle+" rad";
        }

        public String getName() {
            return "rz";
        }
        
        public double getValue(int index){
             return this.angle;
        }
    }
}
