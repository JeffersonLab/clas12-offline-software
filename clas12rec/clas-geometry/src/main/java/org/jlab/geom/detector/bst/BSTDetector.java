package org.jlab.geom.detector.bst;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;

/**
 * A Barrel Silicon Tracker (BST) {@link org.jlab.geom.base.Detector Detector}.
 * <p>
 * Factory: {@link org.jlab.geom.detector.bst.BSTFactory BSTFactory}<br> 
 * Hierarchy: 
 * <code>
 * <b>{@link org.jlab.geom.detector.bst.BSTDetector BSTDetector}</b> → 
 * {@link org.jlab.geom.detector.bst.BSTSector BSTSector} → 
 * {@link org.jlab.geom.detector.bst.BSTSuperlayer BSTSuperlayer} → 
 * {@link org.jlab.geom.detector.bst.BSTLayer BSTLayer} → 
 * {@link org.jlab.geom.component.SiStrip SiStrip}
 * </code>
 * 
 * @author jnhankins
 */
public class BSTDetector extends AbstractDetector<BSTSector> {
    
    protected BSTDetector() {
        super(DetectorId.BST);
    }
    
    /**
     * Returns "BST Detector".
     * @return "BST Detector"
     */
    @Override
    public String getType() {
        return "BST Detector";
    }
}
