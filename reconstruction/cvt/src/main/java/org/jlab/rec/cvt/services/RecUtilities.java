package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Map;

import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.objects.Strip;

import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Track; 

/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class RecUtilities {

    public void CleanupSpuriousCrosses(List<ArrayList<Cross>> crosses, List<Track> trks,
            org.jlab.rec.cvt.svt.Geometry SVTGeom) {
        List<Cross> rmCrosses = new ArrayList<Cross>();
        
        for(Cross c : crosses.get(0)) {
            double z = SVTGeom.transformToFrame(c.get_Sector(), c.get_Region()*2, c.get_Point().x(), c.get_Point().y(),c.get_Point().z(), "local", "").z();
            if(z<-0.1 || z>SVTConstants.MODULELEN) {
                rmCrosses.add(c);
            }
        }
       
        
        for(int j = 0; j<crosses.get(0).size(); j++) {
            for(Cross c : rmCrosses) {
                if(crosses.get(0).get(j).get_Id()==c.get_Id())
                    crosses.get(0).remove(j);
            }
        } 
        
       
        if(trks!=null && rmCrosses!=null) {
            List<Track> rmTrks = new ArrayList<Track>();
            for(Track t:trks) {
                boolean rmFlag=false;
                for(Cross c: rmCrosses) {
                    if(c!=null && t!=null && c.get_AssociatedTrackID()==t.get_Id())
                        rmFlag=true;
                }
                if(rmFlag==true)
                    rmTrks.add(t);
            }
            trks.removeAll(rmTrks);
        }
    }
    
    public List<Surface> setMeasVecs(Seed trkcand, org.jlab.rec.cvt.svt.Geometry sgeo) {
        //Collections.sort(trkcand.get_Crosses());
        List<Surface> KFSites = new ArrayList<Surface>();
        Plane3D pln0 = new Plane3D(new Point3D(Constants.getXb(),Constants.getYb(),Constants.getZoffset()),
        new Vector3D(0,0,1));
        Surface meas0 = new Surface(pln0,new Point3D(Constants.getXb(),Constants.getYb(),0),
        new Point3D(Constants.getXb()-300,Constants.getYb(),0), new Point3D(Constants.getXb()+300,Constants.getYb(),0));
        meas0.setSector(0);
        meas0.setLayer(0);
        meas0.setError(1);
        KFSites.add(meas0); 
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { //SVT
            if(trkcand.get_Clusters().get(i).get_Detector()==0) {
                int id = trkcand.get_Clusters().get(i).get_Id();
                double ce = trkcand.get_Clusters().get(i).get_Centroid();
                Point3D endPt1 = trkcand.get_Clusters().get(i).getEndPoint1();
                Point3D endPt2 = trkcand.get_Clusters().get(i).getEndPoint2();
                Strip strp = new Strip(id, ce, endPt1.x(), endPt1.y(), endPt1.z(),
                                        endPt2.x(), endPt2.y(), endPt2.z());
                Plane3D pln = new Plane3D(endPt1,sgeo.findBSTPlaneNormal(trkcand.get_Clusters().get(i).get_Sector(), 
                        trkcand.get_Clusters().get(i).get_Layer()));
                Point3D Or = sgeo.getPlaneModuleOrigin(trkcand.get_Clusters().get(i).get_Sector(), trkcand.get_Clusters().get(i).get_Layer());
                Point3D En = sgeo.getPlaneModuleEnd(trkcand.get_Clusters().get(i).get_Sector(), trkcand.get_Clusters().get(i).get_Layer());
                Surface meas = new Surface(pln, strp, Or, En);
                meas.setSector(trkcand.get_Clusters().get(i).get_Sector());
                meas.setLayer(trkcand.get_Clusters().get(i).get_Layer());
                double err = trkcand.get_Clusters().get(i).get_Error();
                meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                if(i>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                    continue;
                KFSites.add(meas);
            }
        }
       
        // adding the BMT
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("BMT")) {
                Cylindrical3D cyl = new Cylindrical3D();
                cyl.baseArc().setCenter(new Point3D(0, 0, 0));
                cyl.highArc().setCenter(new Point3D(0, 0, 0));
                cyl.baseArc().setNormal(new Vector3D(0,1,0));
                cyl.highArc().setNormal(new Vector3D(0,1,0));
                
                int id = trkcand.get_Crosses().get(c).get_Cluster1().get_Id();
                double ce = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("Z")) {
                    double x = trkcand.get_Crosses().get(c).get_Point().x();
                    double y = trkcand.get_Crosses().get(c).get_Point().y();
                    double phi = Math.atan2(y,x);
                    double err = trkcand.get_Crosses().get(c).get_Cluster1().get_PhiErr()
                            *(org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    
                    Strip strp = new Strip(id, ce, x, y, phi);
                    //cyl.baseArc().setRadius(Math.sqrt(x*x+y*y));
                    //cyl.highArc().setRadius(Math.sqrt(x*x+y*y));
                    cyl.baseArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    cyl.highArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);                   
                    
                    Surface meas = new Surface(cyl, strp);
                    meas.setSector(trkcand.get_Crosses().get(c).get_Sector());
                    meas.setLayer(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6);
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    if(c>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                        continue;
                    KFSites.add(meas);
                }
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("C")) {
                    double z = trkcand.get_Crosses().get(c).get_Point().z();
                    double err = trkcand.get_Crosses().get(c).get_Cluster1().get_ZErr();
                    
                    Strip strp = new Strip(id, ce, z);
                    cyl.baseArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    cyl.highArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);                   
                    Surface meas = new Surface(cyl, strp);
                    meas.setSector(trkcand.get_Crosses().get(c).get_Sector());
                    meas.setLayer(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6);
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    if(c>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                        continue;
                    KFSites.add(meas);
                }
            }
        }
        return KFSites;
    }

    
    public void MatchTrack2Traj(Seed trkcand, Map<Integer, 
            org.jlab.clas.tracking.kalmanfilter.helical.KFitter.HitOnTrack> traj, 
            org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.Geometry bgeo) {
        
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { //SVT
            if(trkcand.get_Clusters().get(i).get_Detector()==0) {
                Cluster cluster = trkcand.get_Clusters().get(i);
                int layer = trkcand.get_Clusters().get(i).get_Layer();
                int sector = trkcand.get_Clusters().get(i).get_Sector();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                double doca2Cls = sgeo.getDOCAToStrip(sector, layer, cluster.get_Centroid(), p);
                double doca2Seed = sgeo.getDOCAToStrip(sector, layer, (double) cluster.get_SeedStrip(), p);
                cluster.set_SeedResidual(doca2Seed); 
                cluster.set_CentroidResidual(doca2Cls);
            
                for (FittedHit hit : cluster) {
                    double doca1 = sgeo.getDOCAToStrip(sector, layer, (double) hit.get_Strip().get_Strip(), p);
                    double sigma1 = sgeo.getSingleStripResolution(layer, hit.get_Strip().get_Strip(), traj.get(layer).z);
                    hit.set_stripResolutionAtDoca(sigma1);
                    hit.set_docaToTrk(doca1);  
                    if(traj.get(layer).isMeasUsed)
                        hit.set_TrkgStatus(1);
                }
            }
        }

        // adding the BMT
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("BMT")) {
                double ce = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                int layer = trkcand.get_Crosses().get(c).getOrderedRegion()+3;
                Cluster cluster = trkcand.get_Crosses().get(c).get_Cluster1();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                Vector3D v = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).set_Dir(v); 
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("Z")) {
                    trkcand.get_Crosses().get(c).set_Point(new Point3D(trkcand.get_Crosses().get(c).get_Point().x(),trkcand.get_Crosses().get(c).get_Point().y(),p.z()));
                    double xc = trkcand.get_Crosses().get(c).get_Point().x();
                    double yc = trkcand.get_Crosses().get(c).get_Point().y();
                    double doca2Cls = (Math.atan2(p.y(), p.x())-Math.atan2(yc, xc))*
                            (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[cluster.get_Region() - 1] 
                            + org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    cluster.set_CentroidResidual(doca2Cls);

                    for (FittedHit hit : cluster) {
                        double xh = Math.cos(hit.get_Strip().get_Phi());
                        double yh = Math.sin(hit.get_Strip().get_Phi());
                        double doca1 = (Math.atan2(p.y(), p.x())-Math.atan2(yh, xh))*
                            (org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[cluster.get_Region() - 1] 
                            + org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                        
                        if(hit.get_Strip().get_Strip()==cluster.get_SeedStrip())
                            cluster.set_SeedResidual(doca1); 
                        if(traj.get(layer).isMeasUsed)
                            hit.set_TrkgStatus(1);
                        hit.set_docaToTrk(doca1);  

                    }
                }
                if (trkcand.get_Crosses().get(c).get_DetectorType().equalsIgnoreCase("C")) {
                    double z = trkcand.get_Crosses().get(c).get_Point().z();
                    double err = trkcand.get_Crosses().get(c).get_Cluster1().get_ZErr();
                    trkcand.get_Crosses().get(c).set_Point(new Point3D(p.x(),p.y(),trkcand.get_Crosses().get(c).get_Point().z()));
                    double doca2Cls = p.z()-cluster.get_Z();
                    
                    cluster.set_CentroidResidual(doca2Cls);

                    for (FittedHit hit : cluster) {
                        double doca1 = p.z()-hit.get_Strip().get_Z();
                        if(hit.get_Strip().get_Strip()==cluster.get_SeedStrip())
                            cluster.set_SeedResidual(doca1); 
                        if(traj.get(layer).isMeasUsed)
                            hit.set_TrkgStatus(1);
                        hit.set_docaToTrk(doca1);  

                    }
                }
            }
        }
    }
    
    public Track OutputTrack(Seed seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf,
            org.jlab.rec.cvt.svt.Geometry SVTGeom, org.jlab.rec.cvt.bmt.Geometry BMTGeom) {
        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
                kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
                kf.KFHelix.getZ0(), kf.KFHelix.getTanL());
        helix.B = kf.KFHelix.getB();
        Track cand = new Track(helix);
        cand.setNDF(kf.NDF);
        cand.setChi2(kf.chi2);
        
        for (Cross c : seed.get_Crosses()) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) {
                continue;
            }
        }
        
        this.MatchTrack2Traj(seed, kf.TrjPoints, SVTGeom, BMTGeom);
        cand.addAll(seed.get_Crosses());
        return cand;
        
    }
    public Track OutputTrack(Seed seed) {
        
        Track cand = new Track(seed.get_Helix());
        
        for (Cross c : seed.get_Crosses()) {
            if (c.get_Detector().equalsIgnoreCase("SVT")) {
                continue;
            }
        }
        cand.addAll(seed.get_Crosses());
        return cand;
        
    }
    
}