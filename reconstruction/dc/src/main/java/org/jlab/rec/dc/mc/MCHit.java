package org.jlab.rec.dc.mc;

import java.util.Map;
import java.util.HashMap;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.Constants;

public class MCHit {

    private int status;
    private int sector = -1;
    private int superlayer = -1;
    private int layer = -1;
    private int id = -1;
    private Point3D point;
    private Point3D dir;
    private double p;
    
    public MCHit(int id, int sector, int ssl, double[] pars, int status) {
        this.id = id;
        this.status = status;
        this.sector = sector;
        point = new Point3D(pars[0], pars[1], pars[2]);
        Vector3D p3D = new Vector3D(pars[3], pars[4], pars[5]);
        p = p3D.mag();
        dir = p3D.asUnit().toPoint3D();
        getSSL(ssl);

    }        

    public MCHit(int id, int sector, double[] xpars, double[] ppars, Map<Integer, Double> ZMap, int status) {
        this.id = id;
        this.status = status;
        this.sector = sector;
        point = new Point3D(xpars[0], xpars[1], xpars[2]);
        Vector3D p3D = new Vector3D(ppars[0], ppars[1], ppars[2]);
        p = p3D.mag();
        dir = p3D.asUnit().toPoint3D();
        getSSL(point.z(), ZMap);

    }
    
    public MCHit(int id, int sector, double[] xpars, double[] ppars, int status) {
        this.id = id;
        this.status = status;
        this.sector = sector;
        point = new Point3D(xpars[0], xpars[1], xpars[2]);
        Vector3D p3D = new Vector3D(ppars[0], ppars[1], ppars[2]);
        p = p3D.mag();
        dir = p3D.asUnit().toPoint3D();
    }
    
    private void getSSL(int ssl){
        this.superlayer = (int)ssl/6 + 1;
        this.layer = (int)ssl%6 + 1;               
    }
    
    private void getSSL(double pos, Map<Integer, Double> ZMap){
        for(int key: ZMap.keySet()){
            if(Math.abs(ZMap.get(key) - pos) < 0.5){
                this.superlayer = (int)key/6 + 1;
                this.layer = (int)key%6 + 1;  
                break;
            }
        }
    }
    
    public int getStatus(){
        return status;
    }
    
    public int getSector() {
        return sector;
    }

    public int getSuperlayer() {
        return superlayer;
    }

    public int getLayer() {
        return layer;
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
