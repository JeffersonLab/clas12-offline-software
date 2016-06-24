package org.jlab.geom.base;

import java.util.List;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.Showable;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Transformation3D;

/**
 * One complete CLAS12 detector, such as the entire Central Neutron Detector
 * (CND) or the combined set of all Drift Chambers (DC).
 * <p>
 * The primary purpose of a {@code Detector} object is to provide convenient
 * methods for accessing to the {@link org.jlab.geom.base.Sector Sector} objects
 * it contains. If grouping components by sector is inconvenient for a
 * particular type of detector, then the {@code Detector} implementation may
 * have only one sector, in which case requisite sector-related information will
 * be made available through other mechanisms.
 * <p>
 * Factory: {@link org.jlab.geom.base.Factory Factory}<br> Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.base.Detector Detector}</b> → 
 * {@link org.jlab.geom.base.Sector Sector} → 
 * {@link org.jlab.geom.base.Superlayer Superlayer} → 
 * {@link org.jlab.geom.base.Layer Layer} → 
 * {@link org.jlab.geom.base.Component Component}
 * </code>
 * <p>
 * To learn how to create a {@code Detector} and find much more information see
 * {@link org.jlab.geom.base.Factory}.
 * 
 * @author jnhankins
 * @param <SectorType> the specific type of {@code Sector} contained by the
 * {@code Detector}
 */
public interface Detector<SectorType extends Sector> extends Showable {
    /**
     * Returns the id of this detector.
     * This id is a DetectorID which is an enumeration type that uniquely 
     * identifies this detector. To find an integer id associated with this
     * detector, call getDetectorId.getIdNumber().
     * @return the id of this detector
     */
    DetectorId getDetectorId();
    
    /**
     * Returns the number of sectors contained in this detector.
     * @return the number of sectors
     */
    int getNumSectors();
    
    /**
     * Returns the sector associated with the given sector id.
     * @param sectorId the sector id
     * @return the sector with the specified id
     */
    SectorType getSector(int sectorId);
    
    /**
     * Returns an unmodifiable list of all of the sectors contained in this
     * detector.
     * @return an unmodifiable list of all sectors
     */
    List<SectorType> getAllSectors();
    
    /**
     * Returns a list of lists of lines such that each of the inner lists 
     * contains the cross section lines for a single component.
     * <p>
     * This method takes a Transformation3D object as an argument, then 
     * constructs a new {@code Plane3D} object in the xy-plane and applies the
     * transformation to the plane. The intersection of the detector's 
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
     * with layer surface boundaries in this detector. The component ids stored
     * in the detector hits will all be set to -1 to indicate that detector hit
     * does not specify a specific component.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getLayerHits(Path3D path);
    
    /**
     * Returns a list of DetecorHits which store information about intersections
     * with components in this detector.
     * @param path the path
     * @return a list of detector hits
     */
    List<DetectorHit> getHits(Path3D path);
    
    /**
     * Returns a string that identifies the specific subtype of this detector.
     * @return a string naming this detectors's type
     */
    String getType();
    
    /**
     * Invokes {@code System.out.println(this)}.
     */
    void show();
}