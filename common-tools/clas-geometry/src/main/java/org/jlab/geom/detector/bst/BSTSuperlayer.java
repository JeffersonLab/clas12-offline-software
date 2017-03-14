package org.jlab.geom.detector.bst;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;

/**
 * A Barrel Silicon Tracker (BST) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.bst.BSTFactory BSTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.bst.BSTDetector BSTDetector} → 
 * {@link org.jlab.geom.detector.bst.BSTSector BSTSector} → 
 * <b>{@link org.jlab.geom.detector.bst.BSTSuperlayer BSTSuperlayer}</b> → 
 * {@link org.jlab.geom.detector.bst.BSTLayer BSTLayer} → 
 * {@link org.jlab.geom.component.SiStrip SiStrip}
 * </code>
 * 
 * @author jnhankins
 */
public class BSTSuperlayer extends AbstractSuperlayer<BSTLayer> {

    protected BSTSuperlayer(int sectorId, int superlayerId) {
        super(DetectorId.BST, sectorId, superlayerId);
    }
    
    /**
     * Returns "BST Superlayer".
     * @return "BST Superlayer"
     */
    @Override
    public String getType() {
        return "BST Superlayer";
    }
}
