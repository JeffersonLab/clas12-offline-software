/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class ShapePath implements ShapeObject {
    
    private  Path3D shapePath = new Path3D();
    private  Color  pathColor = Color.BLACK;
    private  int    lineWidth = 2;
    
    public ShapePath(){
        
    }

    public Path3D getPath(){return this.shapePath;}
    
    @Override
    public void draw(Graphics2D g2d, UniverseCoordinateSystem csystem) {
        g2d.setColor(pathColor);
        g2d.setStroke(new BasicStroke(lineWidth));
        int nlines = shapePath.getNumLines();
        for(int i = 0; i < nlines; i++){
            g2d.drawLine( 
                    (int) csystem.getPointX(shapePath.getLine(i).origin().x()),
                    (int) csystem.getPointY(shapePath.getLine(i).origin().y()),
                    (int) csystem.getPointX(shapePath.getLine(i).end().x()),
                    (int) csystem.getPointY(shapePath.getLine(i).end().y())
                    );
        }
    }
    
    
}
