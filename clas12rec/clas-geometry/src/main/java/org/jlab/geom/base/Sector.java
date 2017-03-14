package org.jlab.geom.base;

import java.util.List;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.Showable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * One sector of a detector.
 * <p>
 * The primary purpose of a {@code Sector} object is to provide convenient
 * methods for accessing to the {@code Superlayer} objects it contains. If
 * grouping components by sector is inconvenient a {@code Sector} object may
 * contain more than one sector.
 * <p>
 * Factory: {@link org.jlab.geom.base.Factory Factory}<br>
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.base.Detector Detector} → 
 * <b>{@link org.jlab.geom.base.Sector Sector}</b> → 
 * {@link org.jlab.geom.base.Superlayer Superlayer} → 
 * {@link org.jlab.geom.base.Layer Layer} → 
 * {@link org.jlab.geom.base.Component Component}
 * </code>
 * <p>
 * To learn how to create a {@code Sector} and find much more information see
 * {@link org.jlab.geom.base.Factory}.
 * 
 * @author jnhankins
 * @param <SuperlayerType> the specific type of {@code Superlayer} contained by
 * the {@code Sector}
 */
public interface Sector<SuperlayerType extends Superlayer> extends Showable {
    /**
     * Returns the id of the detector that this sector is contained in.
     * @return the id of this sector's detector
     */
    DetectorId getDetectorId();
    
    /**
     * Returns the id of this sector.
     * @return the id of this sector
     */
    int getSectorId();
    
    /**
     * Returns the number of superlayers contained in this sector.
     * @return the number of superlayers
     */
    int getNumSuperlayers();
    
    /**
     * Returns the superlayer associated with the given superlayer id.
     * @param superlayerId the superlayer id
     * @return the superlayer with the specified id
     */
    SuperlayerType getSuperlayer(int superlayerId);
    
    /**
     * Returns an unmodifiable list of all superlayers contained in this sector.
     * @return an unmodifiable list of superlayers
     */
    List<SuperlayerType> getAllSuperlayers();
    
    /**
     * Returns a list of lists of lines such that each of the inner lists 
     * contains the cross section lines for a single component.
     * <p>
     * This method takes a Transformation3D object as an argument, then 
     * constructs a {@code Plane3D} object in the xy-plane and applies the
     * transformation to the plane. The intersection of the sector's 
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
     * with layer surface boundaries in this sector. The component ids stored in the
     * detector hits will all be set to -1 to indicate that detector hit does
     * not specify a specific component.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getLayerHits(Path3D path);
    
    /**
     * Returns a list of DetecorHits which store information about intersections
     * with components in this sector.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getHits(Path3D path);
    
    /**
     * Returns a string that identifies the specific subtype of this sector.
     * @return a string naming this sector's type
     */
    String getType();
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}
