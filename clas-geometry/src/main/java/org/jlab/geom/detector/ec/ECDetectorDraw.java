/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.ec;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.prim.Shape3D;

/**
 *
 * @author gavalian
 */
public class ECDetectorDraw {
    private Color   outlineColor = new Color(   0,   0,   0);
    private Color      fillColor = new Color( 254, 161, 127);
    private ECSector    detector = null;
    
    public ECDetectorDraw(ConstantProvider cp){
        this.init(cp);
    }
    
    public final void init(ConstantProvider cp){
        this.detector = (new ECFactory()).createSector(cp, 0);
    }
    
    
    public void draw(Graphics2D g2d, int superlayer, int layer){
        ECLayer ecLayer = (ECLayer) detector.getSuperlayer(superlayer).getLayer(layer);
        Rectangle region = g2d.getClipBounds();
        if(region==null){
            System.err.println("[DetectorDraw] error -> no clip rectange has been set.");
            return;
        }
        
        Point origin = new Point();
        Point end    = new Point();
        
        Shape3D boundary = ecLayer.getBoundary();
        
    }
}
