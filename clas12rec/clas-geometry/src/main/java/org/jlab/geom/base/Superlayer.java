package org.jlab.geom.base;

import java.util.List;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.Showable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * One superlayer of a detector.
 * <p>
 * The primary purpose of a {@code Superlayer} object is to provide convenient
 * methods for accessing to the {@code Layer} objects it contains.
 * <p>
 * Factory: {@link org.jlab.geom.base.Factory Factory}<br>
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.base.Detector Detector} → 
 * {@link org.jlab.geom.base.Sector Sector} → 
 * <b>{@link org.jlab.geom.base.Superlayer Superlayer}</b> → 
 * {@link org.jlab.geom.base.Layer Layer} → 
 * {@link org.jlab.geom.base.Component Component}
 * </code>
 * <p>
 * To learn how to create a {@code Superlayer} and find much more information 
 * see {@link org.jlab.geom.base.Factory}.
 * 
 * @author jnhankins
 * @param <LayerType> the specific type of {@code Layer} contained by
 * the {@code Superlayer}
 */
public interface Superlayer<LayerType extends Layer> extends Showable {
    /**
     * Returns the id of the detector that this superlayer is contained in.
     * @return the id of this superlayer's detector
     */
    DetectorId getDetectorId();
    
    /**
     * Returns the id of the sector that this superlayer is contained in.
     * @return the id of this superlayer's sector
     */
    int getSectorId();
    
    /**
     * Returns the id of this superlayer.
     * @return the id of this superlayer
     */
    int getSuperlayerId();
    
    /**
     * Returns the number of layers contained in this sector.
     * @return the number of layers
     */
    int getNumLayers();
    
    /**
     * Returns the layer associated with the given layer id.
     * @param layerId the layer id
     * @return the layer with the specified id
     */
    LayerType getLayer(int layerId);
    
    /**
     * Returns an unmodifiable list of all layers contained in this superlayer.
     * @return an unmodifiable list of layers
     */
    List<LayerType> getAllLayers();
    
    /**
     * Returns a list of lists of lines such that each of the inner lists 
     * contains the cross section lines for a single component.
     * <p>
     * This method takes a Transformation3D object as an argument, then 
     * constructs a {@code Plane3D} object in the xy-plane and applies the
     * transformation to the plane. The intersection of the superlayer's 
     * component's shapes with the transformed plane is then calculated and 
     * stored in a list of lists of lines of lines. The inverse of the 
     * transformation is then applied to each line ensuring that each returned
     * line is in the xy-plane
     * 
     * @param transformation the transformation
     * @return a list of list of lines representing the cross section
     * @see org.jlab.geom.base.Component#getVolumeCrossSection(org.jlab.geom.prim.Transformation3D) 
     */
    List<List<Line3D>> getCrossSections(Transformation3D transformation);
    
    /**
     * Returns a list of DetecorHits which store information about intersections
     * with layer surface boundaries in this superlayer. The component ids
     * stored in the detector hits will all be set to -1 to indicate that
     * detector hit does not specify a specific component.
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
     * Sets the transformation of all layers contained in this superlayer.
     * In most cases layers within a superlayer are "bolted" together and cannot
     * move independently of one another, so this method is intended to provide
     * a mechanism for moving groups of layers together.
     * 
     * @param transform the new transform
     * @see org.jlab.geom.base.Layer#setTransformation(org.jlab.geom.prim.Transformation3D) 
     */
    void setTransformation(Transformation3D transform);
    
    /**
     * Returns a string that identifies the specific subtype of this superlayer.
     * @return a string naming this superlayer's type
     */
    String getType();
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}
