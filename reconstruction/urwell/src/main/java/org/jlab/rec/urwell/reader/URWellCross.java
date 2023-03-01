package org.jlab.rec.urwell.reader;

import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.service.urwell.URWellConstants;
/**
 *
 * @author Tongtong Cao
 */

public class URWellCross {

    private DetectorDescriptor desc = new DetectorDescriptor(DetectorType.URWELL);
    private Point3D global;
    private Point3D local;
    private double energy = 0;
    private double time = 0;
    private int id = -1;
    private int cluster1 = -1;
    private int cluster2 = -1;
    private int status = -1;
    private int tid = -1; // Track id;

    public URWellCross(int id, int sector, double x, double y, double z, double energy, double time, int cluster1, int cluster2, int status) {
        this.id = id;
        this.desc.setSectorLayerComponent(sector, 0, 0);
        this.global = new Point3D(x, y, z);
        this.local = new Point3D(x, y, z);
        local.rotateZ(Math.toRadians(-60 * (sector - 1)));
        local.rotateY(Math.toRadians(-25));
        this.energy = energy;
        this.time = time;
        this.cluster1 = cluster1;
        this.cluster2 = cluster2;
        this.status =  status;
    }
    
    public URWellCross(int id, int tid, int sector, double x_local, double y_local, double z_local, double energy, double time, int cluster1, int cluster2, int status) {
        this.id = id;
        this.tid = tid;
        this.desc.setSectorLayerComponent(sector, 0, 0);
        this.local = new Point3D(x_local, y_local, z_local);
        this.energy = energy;
        this.time = time;
        this.cluster1 = cluster1;
        this.cluster2 = cluster2;
        this.status =  status;
    }

    /**
    * return track id
    */
    
    public int get_tid(){
        return tid;
    }
    
    /**
    * @param tid track id
    */
    
    public void set_tid(int tid){
        this.tid = tid;
    }

    public int id() {
        return this.id;
    }
    
    public int sector() {
        return this.desc.getSector();
    }

    public Point3D position() {
        return this.global;
    }

    public Point3D local() {
        return this.local;
    }

    public double energy() {
        return energy;
    }

    public double time() {
        return time;
    }

    public void setClusterIndex1(int cluster) {
        this.cluster1 = cluster;
    }

    public void setClusterIndex2(int cluster) {
        this.cluster2 = cluster;
    }
    
    public int cluster1() {
        return this.cluster1;
    }
    
    public int cluster2() {
        return this.cluster2;
    }
    
    public int status() {
        return this.status;
    }

    public URWellCluster getCluster1(List<URWellCluster> urClusters) {
        if (cluster1 > 0 && cluster1 <= urClusters.size()) {
            return urClusters.get(cluster1 - 1);
        } else {
            return null;
        }
    }

    public URWellCluster getCluster2(List<URWellCluster> urClusters) {
        if (cluster2 > 0 && cluster2 <= urClusters.size()) {
            return urClusters.get(cluster2 - 1);
        } else {
            return null;
        }
    }

    public boolean isGood(List<URWellCluster> urClusters) {
        if(this.getCluster1(urClusters) != null && this.getCluster2(urClusters)!= null)
            return Math.abs(this.getCluster1(urClusters).energy() - this.getCluster2(urClusters).energy()) < URWellConstants.deltaE
                    && Math.abs(this.getCluster1(urClusters).time() - this.getCluster2(urClusters).time()) < URWellConstants.deltaT;
        else return false;
    }

    public boolean isInTime() {
        return Math.abs(this.time() - URWellConstants.meanT) < 1.2 * URWellConstants.deltaT;
    }
}
