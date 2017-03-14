package org.jlab.geom.detector.bst;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;


/**
 * A Barrel Silicon Tracker (BST) {@link org.jlab.geom.base.Sector Sector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.bst.BSTFactory BSTFactory}<br> 
 * Hierarchy: 
 * <code>
 * {@link org.jlab.geom.detector.bst.BSTDetector BSTDetector} → 
 * <b>{@link org.jlab.geom.detector.bst.BSTSector BSTSector}</b> → 
 * {@link org.jlab.geom.detector.bst.BSTSuperlayer BSTSuperlayer} → 
 * {@link org.jlab.geom.detector.bst.BSTLayer BSTLayer} → 
 * {@link org.jlab.geom.component.SiStrip SiStrip}
 * </code>
 * 
 * @author jnhankins
 */
public class BSTSector extends AbstractSector<BSTSuperlayer> {
    
    protected BSTSector(int sectorId) {
        super(DetectorId.BST, sectorId);
    }
    
    /**
     * Returns "BST Sector".
     * @return "BST Sector"
     */
    @Override
    public String getType() {
        return "BST Sector";
    }
}
