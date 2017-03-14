package org.jlab.geom.prim;

/**
 * An object that can be translated and rotated in 3D.
 * <p>
 * Objects implementing this interface can be manipulated using 
 * {@link org.jlab.geom.prim.Transformation3D Transformation3D} which allows
 * sequences of rotations and translations to be grouped together and performed
 * simultaneously.
 * 
 * @author jnhankins
 */
public interface Transformable {
    
    /**
     * Translates this object linearly by the amounts specified.
     * @param dx amount to translate along the x axis
     * @param dy amount to translate along the y axis
     * @param dz amount to translate along the z axis
     */
    void translateXYZ(double dx, double dy, double dz);
    
    /**
     * Rotates this object clockwise around the x axis.
     * @param angle rotation angle in radians
     */
    void rotateX(double angle);
    
    /**
     * Rotates this object clockwise around the y axis.
     * @param angle rotation angle in radians
     */
    void rotateY(double angle);
    
    /**
     * Rotates this object clockwise around the z axis.
     * @param angle rotation angle in radians
     */
    void rotateZ(double angle);
}
