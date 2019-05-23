/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.tof.track;

import org.jlab.detector.hits.DetHit;
import org.jlab.geometry.prim.Line3d;

/**
 *
 * @author devita
 */
public class Track {
    
    private int    Id;
    private Line3d Line;
    private double Path;
    private DetHit Hit;

    public Track() {
    }

    public Track(int Id, Line3d Line, double Path) {
        this.Line = Line;
        this.Path = Path;
        this.Id = Id;
    }

    public Line3d getLine() {
        return Line;
    }

    public void setLine(Line3d Line) {
        this.Line = Line;
    }

    public double getPath() {
        return Path;
    }

    public void setPath(double Path) {
        this.Path = Path;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public DetHit getHit() {
        return Hit;
    }

    public void setHit(DetHit Hit) {
        this.Hit = Hit;
    }

    public String toString() {
        String string = " id = " + this.Id + ", line = " + Line.origin().toString() + "-" + Line.end().toString() + ", path = " + this.Path;
        return string;
    }
    
}
