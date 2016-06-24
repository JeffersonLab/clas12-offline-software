/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.fx;

import javafx.scene.paint.Color;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class DetectorMesh2D {
    
    private Path3D  meshPath = new Path3D();
    private Color   meshFillColor    = Color.CORAL;
    private Color   meshStrokeColor  = Color.BLACK;
    private double  meshStrokeLineWidth = 1.0;
    private int     detectorID = 0;
    private int[]   detectorIndex = new int[3];
    private PopupText   meshPopupText = new PopupText();
    
    
    public DetectorMesh2D(){
        
    }
    
    public DetectorMesh2D(int did, int indx1, int indx2, int indx3){
        this.detectorID = did;
        detectorIndex[0] = indx1;
        detectorIndex[1] = indx2;
        detectorIndex[2] = indx3;
    }
    
    
    public void addText(String text){ this.meshPopupText.addText(text);}
    public PopupText  getPopupText(){ return this.meshPopupText;}
    
    public Path3D  getPath(){return this.meshPath;}    
    public Color   getFillColor(){ return this.meshFillColor;}
    public Color   getStrokeColor(){ return this.meshStrokeColor;}
    
    public DetectorMesh2D box(double xd, double yd){
        this.meshPath.clear();
        this.meshPath.addPoint(-xd/2.0, -yd/2.0, 0.0);
        this.meshPath.addPoint( xd/2.0, -yd/2.0, 0.0);
        this.meshPath.addPoint( xd/2.0,  yd/2.0, 0.0);
        this.meshPath.addPoint(-xd/2.0,  yd/2.0, 0.0);
        return this;
    }
    
    public DetectorMesh2D trap(double dx1, double dx2, double yd){
        this.meshPath.clear();
        this.meshPath.addPoint(-dx1/2.0, -yd/2.0, 0.0);
        this.meshPath.addPoint( dx2/2.0, -yd/2.0, 0.0);
        this.meshPath.addPoint( dx2/2.0,  yd/2.0, 0.0);
        this.meshPath.addPoint(-dx1/2.0,  yd/2.0, 0.0);
        return this;
    }
    
    public DetectorMesh2D  setFillColor(int r, int g, int b){
        this.meshFillColor = Color.rgb(r, g, b);
        return this;
    }
    
    public DetectorMesh2D  setFillColor(int r, int g, int b, double alpha){
        this.meshFillColor = Color.rgb(r, g, b,alpha);
        return this;
    }
    
    public DetectorMesh2D  setFillColor(Color col){
        this.meshFillColor = col;
        return this;
    }
    
    public DetectorMesh2D  setStrokeColor(int r, int g, int b){
        this.meshStrokeColor = Color.rgb(r, g, b);
        return this;
    }
    
    public DetectorMesh2D  setStrokeColor(int r, int g, int b,double alpha){
        this.meshStrokeColor = Color.rgb(r, g, b,alpha);
        return this;
    }
    
    public DetectorMesh2D  setLineColor(int r, int g, int b){
        this.meshStrokeColor = Color.rgb(r, g, b);
        return this;
    }
    
    public DetectorMesh2D  setLineColor(int r, int g, int b,double alpha){
        this.meshStrokeColor = Color.rgb(r, g, b,alpha);
        return this;
    }
    
    public DetectorMesh2D  setLineWidth(double lw){ 
        this.meshStrokeLineWidth = lw;
        return this;
    }
    
    public double getLineWidth(){
        return this.meshStrokeLineWidth;
    }
    
    public boolean isContained(double x, double y){
        int i, j;
        boolean c = false;
        int nvert = this.meshPath.size();
        for (i = 0, j = nvert-1; i < nvert; j = i++) {
            if ( (( meshPath.point(i).y()>y) != (meshPath.point(j).y()>y)) &&
                    (x < ( meshPath.point(j).x()-meshPath.point(i).x()) * 
                    (y-meshPath.point(i).y()) / (meshPath.point(j).y()-meshPath.point(i).y()) +
                    meshPath.point(i).x()))
                c = !c;
        }
        return c;
        //return false;
    }
    
    public void show(){
        System.out.println(String.format("UNIT [ %4d %4d %4d %4d] ", 
                this.detectorID,this.detectorIndex[0],this.detectorIndex[1],this.detectorIndex[2]));
    }
}
