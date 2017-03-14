/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.geom.detector.bst;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;

/**
 *
 * @author gavalian
 */
public class BSTRing extends AbstractDetector<BSTSector> {
    
    protected BSTRing() {
        super(DetectorId.BST);
    }
    
    @Override
    public String getType() {
        return "BST Ring";
    }
    
}
