package org.jlab.rec.dc.mc;

import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.trajectory.TrackVec;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;

/**
 *
 * @author Tongtong Cao
 */
public class MCCross {

    private int status;
    private int sector;
    private int region;
    private int id;
    private Point3D point;
    private Point3D dir;
    private double p;
    
    public MCCross(int id, int sector, int region, double[] pars, int status) {
        this.id = id;
        this.status = status;
        this.sector = sector;
        this.region = region;
        point = new Point3D(pars[0], pars[1], pars[2]);
        Vector3D p3D = new Vector3D(pars[3], pars[4], pars[5]);
        p = p3D.mag();
        dir = p3D.asUnit().toPoint3D();    
    }

    public MCCross(int id, int sector, int region, double[] xpars, double[] ppars, int status) {
        this.id = id;
        this.status = status;
        this.sector = sector;
        this.region = region;
        point = new Point3D(xpars[0], xpars[1], xpars[2]);
        Vector3D p3D = new Vector3D(ppars[0], ppars[1], ppars[2]);
        p = p3D.mag();
        dir = p3D.asUnit().toPoint3D();    
    }
    
    public int getStatus(){
        return status;
    }
    
    public int getSector() {
        return sector;
    }
    
    public int getRegion(){
        return region;
    }

    public int getId() {
        return id;
    }

    public Point3D getPoint() {
        return point;
    }

    public Point3D getDir() {
        return dir;
    }
    
    public double getP(){
        return p;
    }
}
