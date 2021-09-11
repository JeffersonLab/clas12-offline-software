/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.bmt;

import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.trajectory.Ray;
import org.yaml.snakeyaml.scanner.Constant;

/**
 *
 * @author ziegler
 */
public class BMTGeometry extends Geometry {
    
    public Arc3D getCstrip(int region, int sector, Cluster clus) {
        double radius = Constants.getCRCRADIUS()[region-1]+Constants.hStrip2Det;
        double angle  = Constants.getCRCPHI()[region-1][sector-1] - Constants.getCRCDPHI()[region-1][sector-1];
        double theta  = Constants.getCRCDPHI()[region-1][sector-1]*2;
        double z = 0;
        double totEn =0;
       
        for (int i = 0; i < clus.size(); i++) {
            FittedHit thehit = clus.get(i); 
            double strpEn = clus.getBMTStripEnergy(thehit);
               
            if(thehit.newClustering && clus.size()>Constants.MAXCLUSSIZE && i>Constants.MAXCLUSSIZE-1) 
                continue;
           
            totEn+=strpEn;
            z += strpEn *this.getCstripZ(region, thehit.get_Strip().get_Strip());
        }
        z/= totEn; 
         
        Point3D origin  = new Point3D(radius,0,z);
        origin.rotateZ(angle);
        Point3D center  = new Point3D(0,0,z);
        Vector3D normal = new Vector3D(0,1,0);
        normal.rotateZ(angle);
        Arc3D striparc = new Arc3D(origin,center,normal,theta);
        
        int layer = this.getLayer(region, BMTType.C);
        Point3D    offset = this.getOffset(layer, sector);
        Vector3D rotation = this.getRotation(layer, sector);
        this.alignArc(offset, rotation, striparc);
        
        return striparc;
    }
    /**
     * 
     * @param arcC
     * @param p
     * @param offset
     * @param rotation
     * @return 
     */
    public double getBMTCresi(Arc3D arcC, Point3D p, Point3D offset, Vector3D rotation) {
        Arc3D arc = new Arc3D();
        arc.copy(arcC);
        Point3D ioffset = new Point3D(-offset.x(), -offset.y(), -offset.z());
        Vector3D irotation = new Vector3D(-rotation.x(), -rotation.y(), -rotation.z());
        this.antiAlignArc(ioffset, irotation, arc);
       
        Point3D stV = new Point3D(p.x(), p.y(), p.z());
        stV.translateXYZ(-offset.x(), -offset.y(), -offset.z());
        stV.rotateZ(-rotation.z());
        stV.rotateY(-rotation.y());
        stV.rotateX(-rotation.x());
        
        return stV.z()-arc.center().z();
    }
    
    public double getPhi(Point3D stateVec, int sector, int layer) {
        Line3D cln = this.getAxis(layer, sector);
        
        double v = (cln.origin().z()-stateVec.z())/cln.direction().z();
        double xs = stateVec.x()+v*cln.direction().x();
        double ys = stateVec.y()+v*cln.direction().y();
        
        Vector3D n = new Point3D(cln.origin().x(), cln.origin().y(), cln.origin().z()).
                vectorTo(new Point3D(xs,ys,cln.origin().z())).asUnit();
        
        return n.phi();
    }
    
    public Point3D getCylinderIntersRay(Ray ray, int sector, int layer, int hemisphere) { //hemisphere -1-->bottom
        double r = this.getRadius(layer);
        Point3D offset = this.getOffset(layer, sector);
        Vector3D rotation = this.getRotation(layer, sector);
        Point3D ref = new Point3D();
        ref.copy(ray.get_refPoint());
        Vector3D u = new Vector3D();
        u.copy(ray.get_dirVec());
        ref.translateXYZ(-offset.x(), -offset.y(), -offset.z());
        ref.rotateZ(-rotation.z());
        ref.rotateY(-rotation.y());
        ref.rotateX(-rotation.x());
        u.rotateZ(-rotation.z());
        u.rotateY(-rotation.y());
        u.rotateX(-rotation.x());
        double delta = Math.sqrt((ref.x()*u.x()+ref.y()*u.y())*(ref.x()*u.x()+ref.y()*u.y())
                -(-r*r+ref.x()*ref.x()+ref.y()*ref.y())*(u.x()*u.x()+u.y()*u.y()));
        double l = (-(ref.x()*u.x()+ref.y()*u.y())+delta)/(u.x()*u.x()+u.y()*u.y());
        if((int)Math.signum(ref.y()+l*u.y())!=hemisphere) {
            l = (-(ref.x()*u.x()+ref.y()*u.y())-delta)/(u.x()*u.x()+u.y()*u.y()); 
        } 

        Point3D cylInt = new Point3D(ref.x()+l*u.x(),ref.y()+l*u.y(),ref.z()+l*u.z());
        cylInt.translateXYZ(offset.x(), offset.y(), offset.z());
        cylInt.rotateZ(rotation.z());
        cylInt.rotateY(rotation.y());
        
        return cylInt;
    }
    
}
