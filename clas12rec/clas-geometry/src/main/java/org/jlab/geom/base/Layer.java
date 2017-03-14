package org.jlab.geom.base;

import java.util.List;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.Showable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;


/**
 * One layer of a detector.
 * <p>
 * The primary purpose of a {@code Layer} object is to provide convenient
 * methods for accessing to the {@code Component} objects it contains.
 * <p>
 * Factory: {@link org.jlab.geom.base.Factory Factory}<br>
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.base.Detector Detector} → 
 * {@link org.jlab.geom.base.Sector Sector} → 
 * {@link org.jlab.geom.base.Superlayer Superlayer} → 
 * <b>{@link org.jlab.geom.base.Layer Layer}</b> → 
 * {@link org.jlab.geom.base.Component Component}
 * </code>
 * <p>
 * {@code Layer} contains a {@code Plane3D} called {@link #getPlane() plane} on
 * its up-beam surface such that in the local coordinate system the reference
 * point is at x=0 y=0, the z-coordinate is equal to the the minimum of the
 * z-coordinates of the points in the surface boundary, and the normal is
 * pointing down-beam.
 * <p>
 * {@code Layer} also contains a {@code Shape3D} called 
 * {@link #getBoundary() boundary} that approximately defines the target-side 
 * surface using a set of triangular faces.
 * <p>
 * {@code Layer} handles coordinate systems and alignment calibration for
 * detectors using the methods {@link #getTransformation()} and 
 * {@link #setTransformation(org.jlab.geom.prim.Transformation3D)}.
 * <p>
 * To learn how to create a {@code Layer} and find much more information see
 * {@link org.jlab.geom.base.Factory}.
 * 
 * @author jnhankins
 * @param <ComponentType> the specific type of {@code Component} contained by
 * the {@code Layer}
 */
public interface Layer<ComponentType extends Component> extends Showable {
    /**
     * Returns the id of the detector that this layer is contained in.
     * @return the id of this layer's detector
     */
    DetectorId getDetectorId();
    
    /**
     * Returns the id of the sector that this layer is contained in.
     * @return the id of this layer's sector
     */
    int getSectorId();
    
    /**
     * Returns the id of the superlayer that this layer is contained in.
     * @return the id of this layer's superlayer
     */
    int getSuperlayerId();
    
    /**
     * Returns the id of this layer.
     * @return the id of this layer.
     */
    int getLayerId();
    
    /**
     * Returns the number of components contained in in this layer.
     * @return the number of components
     */
    int getNumComponents();
    
    /**
     * Returns the component associated with the given component id.
     * @param componentId the component id
     * @return the component with the specified id
     */
    ComponentType getComponent(int componentId);
    
    /**
     * Returns an unmodifiable list of all components contained in this layer.
     * @return an unmodifiable list of components
     */
    List<ComponentType> getAllComponents();
    
    /**
     * Returns a Shape3D object that approximately defines the target-side 
     * surface of this layer using a set of triangular faces.
     * @return the shape of boundary of the beam-side surface
     */
    Shape3D getBoundary();
    
    /**
     * Returns a plane on the up-beam surface of this layer such that in the 
     * local coordinate system the reference point is at x = 0 y = 0, the 
     * z-coordinate is equal to the the minimum of the z-coordinates of the 
     * points in the surface boundary, and the normal is pointing down-beam.
     * @return the up-beam surface plane
     */
    Plane3D getPlane();
    
    /** 
     * Returns a list of lists of lines such that each of the inner lists 
     * contains the cross section lines for a single component.
     * <p>
     * This method takes a Transformation3D object as an argument, then
     * constructs a {@code Plane3D} object in the xy-plane and applies the
     * transformation to the plane. The intersection of the layer's component's
     * shapes with the transformed plane is then calculated and stored in a list
     * of lists of lines of lines. The inverse of the transformation is then
     * applied to each line ensuring that each returned line is in the xy-plane
     *
     * @param transformation the transformation
     * @return a list of list of lines representing the cross section
     * @see org.jlab.geom.base.Component#getVolumeIntersection(org.jlab.geom.prim.Line3D, org.jlab.geom.prim.Point3D, org.jlab.geom.prim.Point3D) 
     */
    List<List<Line3D>> getCrossSections(Transformation3D transformation);
    
    /**
     * Returns a list of DetecorHits which store information about intersections
     * with layer surface boundaries in this layer. The component ids stored in the
     * detector hits will all be set to -1 to indicate that detector hit does
     * not specify a specific component.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getLayerHits(Path3D path);
    
    /**
     * Returns a list of DetecorHits which store information about intersections
     * with components in this superlayer.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getHits(Path3D path);
    
    /**
     * Returns a copy of the transformation that was used to rotate and 
     * translate this layer from its initial position in local coordinates to
     * its current position and orientation.
     * <p>
     * Note: The following paragraph uses the term "coordinate system" loosely.
     * The layer's current "coordinate system" is ment to mean the true
     * coordinate system (i.e. local, tilted, sector, or CLAS) with, optionally,
     * additional translations and rotations applied to account for
     * misalignments, etc.
     * <p>
     * Applying the returned translation to a point will transform that point
     * from the local coordinate system to this layer's coordinate system.
     * To translate a point from this layer's current coordinate system.
     * <p>
     * Example: Translating a point back to local coordinates<br>
     * <code>
     * Layer layer = ...<br>
     * Transformation3D inverse = layer.getTransformation().inverse();<br>
     * Point3D point = layer.getComponent(0).getMidpoint();<br>
     * Point3D local = new Point3D(point);<br>
     * inverse.apply(local);<br>
     * System.out.println("Current Coords:"+point+" Local Coords: "+local);
     * </code>
     * Example: Applying alignment calibrations<br>
     * <code>
     * Transformation3D nominal = layer.getTransformation();
     * Transformation3D calibrated = new Transformation3D(nominal);
     * ... // add rotations and translations to calibrate the alighnment
     * layer.setTransformation(calibrated);
     * <br>
     * </code>
     * @return a copy of the current transformation
     */
    Transformation3D getTransformation();
    
    /**
     * Sets the transformation for this layer.
     * <p>
     * Internally, to apply the new transformation, this method will use the
     * inverse of its current transformation to move itself back into local
     * coordinates, it will then move itself to its new coordinates by applying 
     * the new the transformation before storing it.
     * <p>
     * For much more information and example codesee 
     * {@link #getTransformation()}.
     * @param transform the new transform
     * @see #getTransformation() 
     */
    void setTransformation(Transformation3D transform);
    
    /**
     * Returns a string that identifies the specific subtype of this layer.
     * @return a string naming this layer's type
     */
    String getType();
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}
