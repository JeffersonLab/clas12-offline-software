package org.jlab.geom.detector.bst;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.SiStrip;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Shape3D;


/**
 * A Barrel Silicon Tracker (BST) {@link org.jlab.geom.base.Layer Layer}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.bst.BSTFactory BSTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.bst.BSTDetector BSTDetector} → 
 * {@link org.jlab.geom.detector.bst.BSTSector BSTSector} → 
 * {@link org.jlab.geom.detector.bst.BSTSuperlayer BSTSuperlayer} → 
 * <b>{@link org.jlab.geom.detector.bst.BSTLayer BSTLayer}</b> → 
 * {@link org.jlab.geom.component.SiStrip SiStrip}
 * </code>
 * 
 * @author jnhankins
 */
public class BSTLayer extends AbstractLayer<SiStrip> {
    
    protected BSTLayer(int sectorId, int superlayerId, int layerId) {
        super(DetectorId.BST, sectorId, superlayerId, layerId, true);
    }
    
    @Override
    public List<DetectorHit> getHits(Path3D path) {
        List<DetectorHit> hits = new ArrayList();
        Point3D hitPosition = new Point3D();
        
        // For each line in the path
        for(int i=0; i<path.size()-1; i++) {
            Line3D line = path.getLine(i);
            
            // Check to see if the boundary was hit
            Shape3D boundary = getBoundary();
            if (boundary.hasIntersectionSegment(line)) {
                int closestComponentId = -1;
                double closestDist = Double.POSITIVE_INFINITY;
                List<SiStrip> strips = getAllComponents();
                for (int componentId=0; componentId<strips.size(); componentId++) {
                    Line3D hitL = strips.get(componentId).getLine().distance(line);
                    double dist = strips.get(componentId).getLine().distance(line).length();
                    hitPosition.set(hitL.midpoint().x(), 
                            hitL.midpoint().y(),
                            hitL.midpoint().z());
                    if(closestDist > dist) {
                        closestDist = dist;
                        closestComponentId = componentId;
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

    /**
     * Returns "BST Layer".
     * @return "BST Layer"
     */
    @Override
    public String getType() {
        return "BST Layer";
    }
}
