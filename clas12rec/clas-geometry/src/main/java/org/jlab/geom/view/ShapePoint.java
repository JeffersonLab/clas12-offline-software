/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class ShapePoint implements ShapeObject {
    
    private Point3D shapePoint = new Point3D();
    private  Color  outlineColor  = Color.BLACK;
    private  Color  fillColor     = Color.YELLOW;
    private  int    pointSize  = 12;
    
    public ShapePoint(){
        
    }
    
    public ShapePoint(double x, double y, double z){
        shapePoint.set(x, y, z);
    }
    
    @Override
    public void draw(Graphics2D g2d, UniverseCoordinateSystem csystem) {
        g2d.setColor(fillColor);
        int x = (int) csystem.getPointX(shapePoint.x());
        int y = (int) csystem.getPointY(shapePoint.y());
        g2d.fillOval(x-pointSize/2,y-pointSize/2, pointSize, pointSize);
        g2d.setStroke(new BasicStroke(1));
        
        g2d.setColor(outlineColor);
        g2d.drawOval(x-pointSize/2,y-pointSize/2, pointSize, pointSize);
    }
    
}
