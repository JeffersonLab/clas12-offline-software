package org.jlab.geom.detector.dc;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;


/**
 * A Drift Chamber (DC) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.dc.DCFactory DCFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.dc.DCDetector DCDetector} → 
 * <b>{@link org.jlab.geom.detector.dc.DCSector DCSector}</b> → 
 * {@link org.jlab.geom.detector.dc.DCSuperlayer DCSuperlayer} → 
 * {@link org.jlab.geom.detector.dc.DCLayer DCLayer} → 
 * {@link org.jlab.geom.component.DriftChamberWire DriftChamberWire}
 * </code>
 * 
 * @author jnhankins
 */
public class DCSector extends AbstractSector<DCSuperlayer> {
    
    protected DCSector(int sectorId) {
        super(DetectorId.DC, sectorId);
    }
    
    public DCSector(DetectorId id,int sectorId){
        super(id, sectorId);
    }
    /**
     * Constructs a new {@code Plane3D} half way between the two superlayers in the region
     * at the specified index with the normal of the plane facing away from
     * the target.
     * @param regionId the id of the region (0, 1, or 2)
     * @return the region's middle plane
     */
    public Plane3D getRegionMiddlePlane(int regionId) {
        if (regionId < 0 || 2<regionId) {
            System.err.println("DCSector: getRegionMiddlePlane(int regionId): regionId="+regionId+" is invalid");
            return new Plane3D();
        }
        
        Point3D p0 = getSuperlayer(regionId*2).getLayer(5).getPlane().point();
        Point3D p1 = getSuperlayer(regionId*2+1).getLayer(0).getPlane().point();
        
        Vector3D norm = p0.vectorTo(p1);
        norm.unit();
        return new Plane3D(p0.midpoint(p1), norm);
    }
    
    /**
     * Returns "DC Sector".
     * @return "DC Sector"
     */
    @Override
    public String getType() {
        return "DC Sector";
    }
}
