package org.jlab.geom.detector.dc;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.component.DriftChamberWire;


/**
 * A Drift Chamber (DC) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.dc.DCFactory DCFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.dc.DCDetector DCDetector} → 
 * {@link org.jlab.geom.detector.dc.DCSector DCSector} → 
 * {@link org.jlab.geom.detector.dc.DCSuperlayer DCSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.dc.DCLayer DCLayer}</b> → 
 * {@link org.jlab.geom.component.DriftChamberWire DriftChamberWire}
 * </code>
 * 
 * @author jnhankins
 */
public class DCLayer extends AbstractLayer<DriftChamberWire> {
    private final Plane3D midplane = new Plane3D();
    
    protected DCLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.DC, sectorId, superlayerId, layerId, false);
    }
    
    public DCLayer(DetectorId id, int sectorId, int superlayerId, int layerId) {
        super(id, sectorId, superlayerId, layerId, false);
    }
    
    /**
     * Returns the plane that bisects the the region containing the layer.
     * In nominal alignments, for sectors 0 and 3 this plane is in the xz-plane.
     * @return the plane that bisects the the region containing the layer
     */
    public Plane3D getMidplane() {
        return midplane;
    }
    
    @Override
    public void onSetTransformation(Transformation3D transform) {
        transform.apply(midplane);
    }
    
    /**
     * Returns "DC Layer".
     * @return "DC Layer"
     */
    @Override
    public String getType() {
        return "DC Layer";
    }

    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        Point3D hitPosition = new Point3D();
        
        // For each line in the path
        for(int i=0; i<path.size()-1; i++) {
            Line3D line = path.getLine(i);
            ArrayList<Point3D>  intersections = new ArrayList<Point3D>();
            // Check to see if the boundary was hit
            Shape3D boundary = getBoundary();
            if (boundary.hasIntersectionSegment(line)) {
                int closestComponentId = -1;
                int icount = boundary.intersection(line, intersections);                
                //System.err.println("-------> intersections size = " + icount
                //+ " " + intersections.size());
                if(icount>0) hitPosition = intersections.get(0);
                double closestDist = 1000.0;
                List<DriftChamberWire> wires = getAllComponents();
                for (int componentId=0; componentId<wires.size(); componentId++) {
                    double dist = wires.get(componentId).getLine().distance(line).length();
                    //System.err.println("-----> component " + componentId
                    //+ "  dist = " + dist);
                    if(closestDist > dist) {
                        closestDist = dist;
                        closestComponentId = componentId;
                        //System.err.println("====>>> Component changed " + closestComponentId
                        //+ "  dist = " + closestDist);
                    }
                }
                hits.add(
                    new DetectorHit(
                        getDetectorId(),
                        getSectorId(),
                        getSuperlayerId(),
                        getLayerId(),
                        closestComponentId,
                        hitPosition));
                return hits;
            }
        }
        
        return hits;
    }
}
