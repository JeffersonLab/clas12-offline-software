/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class DetectorShape3DStore {
    
    private String   storeName = "SomeDetector";
    private ArrayList<DetectorShape3D>  detectorShapes = new ArrayList<DetectorShape3D>();
    public  Rectangle  drawRegion = new Rectangle();
    
    
    public  Color   shapeColorEven = new Color(77,176,221);
    public  Color   shapeColorOdd  = new Color(137,216,68);
    public  Color   shapeColorSelected = new Color(210,79,68);
    public  IDetectorShapeIntensity   colorIntensityMap = null;
    
    
    public DetectorShape3DStore(){
        
    }
    
    
    public String getName(){ return this.storeName;}
    public void setName(String name){ this.storeName = name;}
    
    public void addShape(DetectorShape3D shape){
        this.detectorShapes.add(shape);
    }
    
    public List<DetectorShape3D> getShapes(){
        return this.detectorShapes;
    }
    
    public void setIntensityMap(IDetectorShapeIntensity intens){
        this.colorIntensityMap = intens;
    }
    
    public IDetectorShapeIntensity getIntensityMap(){
        return this.colorIntensityMap;
    }
    
    public void updateDrawRegion(){
        
        if(this.detectorShapes.size()>0){
            int xmin = this.detectorShapes.get(0).getMinX();
            int xmax = this.detectorShapes.get(0).getMaxX();
            int ymin = this.detectorShapes.get(0).getMinY();
            int ymax = this.detectorShapes.get(0).getMaxY();
            //System.out.println("Updating draw region with Shapes = " + this.detectorShapes.size());
            
            for(DetectorShape3D shape : this.detectorShapes){
                if(shape.getMinX()<xmin) xmin = shape.getMinX();
                if(shape.getMinY()<ymin) ymin = shape.getMinY();
                if(shape.getMaxX()>xmax) xmax = shape.getMaxX();
                if(shape.getMaxY()>ymax) ymax = shape.getMaxY();
            }
            this.drawRegion.x = xmin;
            this.drawRegion.y = ymin;
            this.drawRegion.width  = xmax - xmin;
            this.drawRegion.height = ymax - ymin;
            
            double xfraction = this.drawRegion.width*0.1;
            double yfraction = this.drawRegion.height*0.1;
        
            this.drawRegion.x = (int) (this.drawRegion.x - xfraction);
            this.drawRegion.y = (int) (this.drawRegion.y - yfraction);
            this.drawRegion.width = this.drawRegion.width + (int) (2*xfraction);
            this.drawRegion.height = this.drawRegion.height + (int) (2*yfraction);
        }
        
    }
    
    public int getX(float x, int w){        
        double relX = (x - this.drawRegion.x)/this.drawRegion.width;
        return (int) (relX*w);
    }
    
    public int getY(float y, int h){
        double relY = (y - this.drawRegion.y)/this.drawRegion.height;
        return (int) (relY*h);
    }
    
    public int getCoordinateX(int x, int w){
        double relX = ((double) x) /w;
        return (int) (this.drawRegion.x + relX*this.drawRegion.width);
    }
    
    public int getCoordinateY(int y, int h){
        double relX = ((double)y)/h;
        return (int) (this.drawRegion.y + relX*this.drawRegion.height);
    }
    
    public DetectorShape3D  getSelectedShape(int x, int y, int width, int height){
        int xc = this.getCoordinateX(x,width);
        int yc = this.getCoordinateY(y,height);
        for(DetectorShape3D shape : this.detectorShapes){
            //System.out.println("COMPONENT " + counter);
            //comp.show();
            if(shape.shapePolygon.contains(xc, yc)==true){
                shape.isActive = true;
                return shape;
            }
        }
        return null;
    }
    
    public void reaset(){
        for(DetectorShape3D shape : this.detectorShapes){
            shape.isActive = false;
        }
    }
    
    public void draw2D(Graphics2D g2d, int xoff, int yoff, int width, int height){
        
        this.updateDrawRegion();
        RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_TEXT_ANTIALIASING,
             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        //Color activeColor = new Color(210,79,68);
        
        g2d.setColor(new Color(165,155,155));
        g2d.fillRect(xoff, yoff, width, height);
        //System.out.println("Drawing Detector Layer SIZE = " + this.components.size());
        float[] value = new float[6];
        for(DetectorShape3D shape : this.detectorShapes){
            int[] x = shape.shapePolygon.xpoints;
            int[] y = shape.shapePolygon.ypoints;
            //System.out.println("POLYGON SIZE = " + x.length);

            GeneralPath path = new GeneralPath();
            path.moveTo(getX(x[0],width), getY(y[0],height));
            for(int loop = 1; loop < shape.shapePolygon.npoints; loop++){
                //if(getX(x[loop],width)!=0&&getY(y[loop],height)!=0)
                path.lineTo(getX(x[loop],width), getY(y[loop],height));
                /*System.out.println("PLOTTING : " + loop + "  " + x[loop]
                + " " + y[loop]);*/
            }
            
            path.closePath();
            //g2d.draw(path);
            if(this.colorIntensityMap==null){
                g2d.setColor(this.shapeColorOdd);
                if(shape.COMPONENT%2==0) g2d.setColor(this.shapeColorEven);
            } else {
                Color col = this.colorIntensityMap.getColor(shape.SECTOR, shape.LAYER, shape.COMPONENT);
                //System.out.println("Setting intensity color");
                g2d.setColor(col);
            }
                        
            if(shape.isActive) g2d.setColor(this.shapeColorSelected);
            
            g2d.fill(path);
            g2d.setColor(Color.black);
            g2d.draw(path);
        }
    }
    
}
