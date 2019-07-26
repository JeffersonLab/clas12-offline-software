/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Region3D;

/**
 *
 * @author gavalian
 */
public class DetectorMesh2DLayer {
    
    private String layerName = "";
    
    private List<DetectorMesh2D>  meshStore  = new ArrayList<DetectorMesh2D>();
    private List<Point3D>         meshPoints     = new ArrayList<Point3D>();
    private List<PopupText>       meshPointsText = new ArrayList<PopupText>();
    private List<Path3D>          meshPaths  = new ArrayList<Path3D>();
    private List<PopupText>       meshPathsText = new ArrayList<PopupText>();
    private double                markerSize = 12.0;
    private double                pathLineWidth = 1;    
    private Region3D              layerRegion  = null;
    private double                layerOpacity = 1.0;
    
    public DetectorMesh2DLayer(String name){
        this.layerName = name;
        this.layerRegion = new Region3D(-0.1,-0.1,-0.1,0.2,0.2,0.2);
    }
    
    public String getName(){return this.layerName;}
    
    public double getOpacity(){return this.layerOpacity;}
    public void setOpacity(double opacity){ this.layerOpacity = opacity; }
    
    public List<DetectorMesh2D> getMeshList(){
        return this.meshStore;
    }
    /**
     * Draw the Meshes on the graphics context.
     * @param gc
     * @param width
     * @param height
     * @param xcenter
     * @param ycenter
     * @param scale 
     */
    public void draw(GraphicsContext gc, double width, double height, 
            double xcenter, double ycenter, 
            double mpX, double mpY, double scale){
        
        for(DetectorMesh2D mesh : this.meshStore){
            
            int npoints = mesh.getPath().size();
            double[] x = new double[npoints];
            double[] y = new double[npoints];
            for(int i = 0; i < npoints; i++){
                x[i] = xcenter + scale*mesh.getPath().point(i).x() + width/2.0;
                y[i] = ycenter + scale*mesh.getPath().point(i).y() + height/2.0;
            }
            
            gc.setFill(
                    Color.rgb(
                            (int) (mesh.getFillColor().getRed()*255),
                            (int) (mesh.getFillColor().getGreen()*255),
                            (int) (mesh.getFillColor().getBlue()*255),
                            this.layerOpacity));
            
            gc.fillPolygon(x, y, npoints);
            gc.setStroke(
                    Color.rgb(
                            (int) (mesh.getStrokeColor().getRed()*255),
                            (int) (mesh.getStrokeColor().getGreen()*255),
                            (int) (mesh.getStrokeColor().getBlue()*255),
                            this.layerOpacity)
            );
            
            gc.setLineWidth(mesh.getLineWidth());
            gc.strokePolygon(x, y, npoints);
        }
        
        PopupText text = new PopupText();
        text.addText("Point X = 0.5");
        text.addText("Point Y = 0.7");
        int hitCounter = 0;
        for(Point3D  hits : this.meshPoints){
            gc.setFill(Color.YELLOW);
            
            double x = xcenter + hits.x()*scale + width/2.0;
            double y = ycenter + hits.y()*scale + height/2.0;
            
            gc.fillOval(x-this.markerSize/2.0, y-this.markerSize/2.0, this.markerSize, this.markerSize);
            
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1.0);
            gc.strokeLine(x - this.markerSize/2.0, y, x + this.markerSize/2.0, y);
            gc.strokeLine(x,y - this.markerSize/2.0, x, y + this.markerSize/2.0);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(x-this.markerSize/2.0, y-this.markerSize/2.0, this.markerSize, this.markerSize);
            if(Math.sqrt((x-mpX)*(x-mpX)+(y-mpY)*(y-mpY))<this.markerSize){
                //text.draw(gc, x, y);
                this.meshPointsText.get(hitCounter).draw(gc, x, y);
            }
            hitCounter++;
        }
        
        gc.setStroke(Color.RED);
        PopupText textTrack = new PopupText();
        textTrack.addText("Momentum = 0.5 GeV");
        textTrack.addText("Theta = 25.67 Deg");
        
        int pathCounter = 0;
        for(Path3D path : this.meshPaths){
            int lines = path.getNumLines();
            boolean  isClose = false;
            Line3D refLine = new Line3D();
            for(int i = 0; i < lines; i++){
                Line3D line = path.getLine(i);
                double x1 = xcenter + line.origin().x()*scale + width/2.0;
                double y1 = ycenter + line.origin().y()*scale + height/2.0;
                double x2 = xcenter + line.end().x()*scale + width/2.0;
                double y2 = ycenter + line.end().y()*scale + height/2.0;
                gc.strokeLine(x1, y1, x2, y2);
                refLine.set(x1, y1, 0.0, x2, y2, 0.0);
                Line3D distance = refLine.distanceSegment(new Point3D(mpX,mpY,0.0));
                if(distance.length()<5.0){
                    isClose = true;
                }
            }
            if(isClose==true){
                //textTrack.draw(gc, mpX, mpY);
                this.meshPathsText.get(pathCounter).draw(gc, mpX, mpY);
            }
            pathCounter++;
        }
                
    }
    
    /*
    public List<Point3D>  getPoints(){
        return this.meshPoints;
    }
    
    public List<Path3D>  getPaths(){
        return this.meshPaths;
    }*/
    
    public void clearHits(){ this.meshPoints.clear();this.meshPointsText.clear();}
    public void clearPaths(){this.meshPaths.clear();this.meshPathsText.clear();}
    
    public void addHit(double x, double y, double z, String... text){
        PopupText ppt = new PopupText();
        for(String t : text){
            ppt.addText(t);
        }
        this.meshPoints.add(new Point3D(x,y,z));
        this.meshPointsText.add(ppt);
    }
    
    public void addPath(Path3D path, String... text){
        PopupText ppt = new PopupText();
        for(String t : text){
            ppt.addText(t);
        }
        this.meshPaths.add(path);
        this.meshPathsText.add(ppt);
    }
    
    public Region3D getRegion(){
        return this.layerRegion;
    }
    
    public void addMesh(DetectorMesh2D mesh){
        meshStore.add(mesh);
        int npoints = mesh.getPath().size();
        for(int i = 0; i < npoints; i++){
            this.layerRegion.addPoint(mesh.getPath().point(i));
        }
    }
}
