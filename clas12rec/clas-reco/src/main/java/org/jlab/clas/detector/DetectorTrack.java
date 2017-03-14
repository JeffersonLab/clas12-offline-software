/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author gavalian
 */
public class DetectorTrack {

    private int     trackCharge = 0;
    private double     trackMom = 0.0;
    private double    trackPath = 0.0;
    private Vector3D   trackEnd = new Vector3D();
    
    private Vector3      trackP = new Vector3();
    private Vector3 trackVertex = new Vector3();
    
    
    private List<Line3D> trackCrosses = new ArrayList<Line3D>();
    private double    MAX_LINE_LENGTH = 1500.0;
    
     public DetectorTrack(int charge){
        this.trackCharge = charge;
    }
    
    public DetectorTrack(int charge, double mom){
        this.trackMom = mom;
        this.trackCharge = charge;
    }

    public DetectorTrack(int charge, double px, double py, double pz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
    }
    
    public DetectorTrack(int charge, double px, double py, double pz,
            double vx, double vy, double vz){
        this.trackCharge = charge;
        this.trackP.setXYZ(px, py, pz);
        this.trackVertex.setXYZ(vx, vy, vz);
    }
    
    public DetectorTrack setCharge(int charge){
        this.trackCharge = charge;
        return this;
    }
    
    public DetectorTrack setP(double p){
        this.trackMom = p;
        return this;
    }
    
    public DetectorTrack setVector(double px, double py, double pz){
        this.trackP.setXYZ(px, py, pz);
        return this;
    }
    
    public DetectorTrack setVertex(double vx, double vy, double vz){
        this.trackVertex.setXYZ(vx, vy, vz);
        return this;
    }
    
    public DetectorTrack setPath(double path){
        this.trackPath = path;
        return this;
    }
    
    public DetectorTrack setTrackEnd(double x, double y, double z){
        this.trackEnd.setXYZ(x, y, z);
        return this;
    }
    
    public int      getCharge()   { return trackCharge;}
    public double   getP()        { return trackP.mag();}    
    public double   getPath()     { return trackPath;}
    public Vector3  getVector()   { return this.trackP;}
    public Vector3  getVertex()   { return this.trackVertex;}
    public Vector3D getTrackEnd() { return trackEnd;}
    
    
    public void addCross(double x, double y, double z,
            double ux, double uy, double uz){
        Line3D line = new Line3D(x,y,z,
                x + ux*this.MAX_LINE_LENGTH,
                y + uy*this.MAX_LINE_LENGTH,
                z + uz*this.MAX_LINE_LENGTH
        );
        this.trackCrosses.add(line);
    }
    
    public int getCrossCount(){ return this.trackCrosses.size();}
    
    public Line3D getCross(int index){
        return this.trackCrosses.get(index);
    }
    
    public Line3D getFirstCross(){
        return trackCrosses.get(0);
    }
    
    public Line3D getLastCross(){
        return this.trackCrosses.get(this.trackCrosses.size()-1);
    }
    
    @Override
    public String toString(){
       StringBuilder  str = new StringBuilder();
       str.append(String.format("[Track] >>>> c = %2d, p = %7.3f,", 
               getCharge(),getP()));
       str.append(String.format(" path = %7.2f", getPath()));
       return str.toString();
    }
}
