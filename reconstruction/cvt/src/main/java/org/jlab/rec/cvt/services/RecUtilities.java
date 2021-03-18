package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import org.jlab.rec.cvt.trajectory.Helix;
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
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Line3D;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.fit.CosmicFitter;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.StraightTrackSeeder;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.TrackSeederCA;

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
    
    public List<Surface> setMeasVecs(Seed trkcand, 
            org.jlab.rec.cvt.svt.Geometry sgeo,
            org.jlab.rec.cvt.bmt.BMTGeometry bgeo) {
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
                //the thickness for multiple scattering.  MS is outward the thickness is set for the 1st layer in the superlayer
                // air gap ignored
                double thickn_ov_X0 = 0;
                if(trkcand.get_Clusters().get(i).get_Layer()%2==1)
                    thickn_ov_X0 = org.jlab.rec.cvt.svt.Constants.SILICONTHICK / org.jlab.rec.cvt.svt.Constants.SILICONRADLEN;
                meas.setl_over_X0(thickn_ov_X0);
                if((int)Constants.getLayersUsed().get(meas.getLayer())<1)
                    meas.notUsedInFit=true;
                if(i>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                    continue;
                KFSites.add(meas);
            }
        }
       
        // adding the BMT
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("BMT")) {
//                Cylindrical3D cyl = new Cylindrical3D();
//                cyl.baseArc().setCenter(new Point3D(0, 0, 0));
//                cyl.highArc().setCenter(new Point3D(0, 0, 0));
//                cyl.baseArc().setNormal(new Vector3D(0,1,0));
//                cyl.highArc().setNormal(new Vector3D(0,1,0));
                int lyer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
                int sec = trkcand.get_Crosses().get(c).get_Cluster1().get_Sector();
                
                Cylindrical3D cyl = bgeo.getCylinder(lyer, sec);
                
                int id = trkcand.get_Crosses().get(c).get_Cluster1().get_Id();
                double ce = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                if (trkcand.get_Crosses().get(c).get_DetectorType()==BMTType.Z) {
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
                    //for multiple scattering
                    double thickn_ov_X0 = org.jlab.rec.cvt.bmt.Constants.get_T_OVER_X0()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1];
                    meas.setl_over_X0(thickn_ov_X0);
                    if((int)Constants.getLayersUsed().get(meas.getLayer())<1) {
                        //System.out.println("Exluding layer "+meas.getLayer()+trkcand.get_Crosses().get(c).printInfo());
                        meas.notUsedInFit=true;
                    }
                    if(c>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                        continue;
                    KFSites.add(meas);
                }
                if (trkcand.get_Crosses().get(c).get_DetectorType()==BMTType.C) {
                    double z = trkcand.get_Crosses().get(c).get_Point().z();
                    double err = trkcand.get_Crosses().get(c).get_Cluster1().get_ZErr();
                    
                    Strip strp = new Strip(id, ce, z);
                    cyl.baseArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);
                    cyl.highArc().setRadius(org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1]+org.jlab.rec.cvt.bmt.Constants.hStrip2Det);                   
                    Surface meas = new Surface(cyl, strp);
                    meas.setSector(trkcand.get_Crosses().get(c).get_Sector());
                    meas.setLayer(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6);
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    //for multiple scattering
                    double thickn_ov_X0 = org.jlab.rec.cvt.bmt.Constants.get_T_OVER_X0()[(trkcand.get_Crosses().get(c).get_Cluster1().get_Layer() + 1) / 2 - 1];
                    meas.setl_over_X0(thickn_ov_X0);
                    if((int)Constants.getLayersUsed().get(meas.getLayer())<1) {
                        //System.out.println("Exluding layer "+meas.getLayer()+trkcand.get_Crosses().get(c).printInfo());
                        meas.notUsedInFit=true;
                    }
                    if(c>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                        continue;
                    KFSites.add(meas);
                }
            }
        }
        return KFSites;
    }
    public List<Cluster> FindClustersOnTrk (List<Cluster> allClusters, Helix helix, double P, int Q,
            org.jlab.rec.cvt.svt.Geometry sgeo, 
            Swim swimmer) { 
        Map<Integer, Cluster> clusMap = new HashMap<Integer, Cluster>();
        //Map<Integer, Double> stripMap = new HashMap<Integer, Double>();
        Map<Integer, Double> docaMap = new HashMap<Integer, Double>();
        Map<Integer, Point3D> trajMap = new HashMap<Integer, Point3D>();
        int[] Sectors = new int[org.jlab.rec.cvt.svt.Constants.NLAYR];
        for (int a = 0; a < Sectors.length; a++) {
            Point3D I = helix.getPointAtRadius(org.jlab.rec.cvt.svt.Constants.MODULERADIUS[a][0]);
            int sec = sgeo.findSectorFromAngle(a + 1, I);                
            Sectors[a] = sec;
        }
        // initialize swimmer starting from the track vertex
        double maxPathLength = 1; 
        swimmer.SetSwimParameters((helix.xdca()+org.jlab.rec.cvt.Constants.getXb()) / 10, (helix.ydca()+org.jlab.rec.cvt.Constants.getYb()) / 10, helix.get_Z0() / 10, 
                     Math.toDegrees(helix.get_phi_at_dca()), Math.toDegrees(Math.acos(helix.costheta())),
                     P, Q, maxPathLength) ;
        double[] inters = null;
        double     path = 0;
        // SVT
        for (int l = 0; l < org.jlab.rec.cvt.svt.Constants.NLAYR; l++) {
            // reinitilize swimmer from last surface
            if(inters!=null) {
                double intersPhi   = Math.atan2(inters[4], inters[3]);
                double intersTheta = Math.acos(inters[5]/Math.sqrt(inters[3]*inters[3]+inters[4]*inters[4]+inters[5]*inters[5]));
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], Math.toDegrees(intersPhi), Math.toDegrees(intersTheta), 
                        P, Q, maxPathLength) ;
            }
            int layer = l + 1;
            int sector = Sectors[l];
            if(sector == -1)
                continue;
            
            Vector3D n = sgeo.findBSTPlaneNormal(sector, layer);
            Point3D p = sgeo.getPlaneModuleOrigin(sector, layer);
            double d = n.dot(p.toVector3D());
            inters = swimmer.SwimToPlaneBoundary(d/10.0, n, 1);
            if(inters!=null) {
                Point3D trp = new Point3D(inters[0]*10, inters[1]*10, inters[2]*10);
                double nearstp = sgeo.calcNearestStrip(inters[0]*10, inters[1]*10, inters[2]*10, layer, sector);
                //stripMap.put((sector*1000+layer), nearstp);
                docaMap.put((sector*1000+layer), sgeo.getDOCAToStrip(sector, layer, nearstp, trp)); 
                trajMap.put((sector*1000+layer), trp); 
            }
        }
        for(Cluster cls : allClusters) {
            int clsKey = cls.get_Sector()*1000+cls.get_Layer();
            if(cls.get_AssociatedTrackID()==-1 && trajMap!=null && trajMap.get(clsKey)!=null) {
                //double trjCent = stripMap.get(clsKey);
                double clsDoca = sgeo.getDOCAToStrip(cls.get_Sector(), cls.get_Layer(), 
                        cls.get_Centroid(), trajMap.get(clsKey));
                if(clusMap.containsKey(clsKey)) {
                    //double filldCent = clusMap.get(clsKey).get_Centroid();
                    double filldDoca = docaMap.get(clsKey);
                    if(Math.abs(clsDoca)<Math.abs(filldDoca)) {//closer doca
                        clusMap.put(clsKey, cls); //fill it
                    }
                }
                if(Math.abs(clsDoca)<cls.get_CentroidError()*5){ //5sigma cut
                    clusMap.put(clsKey, cls);
                }
            }
        }
        List<Cluster> clustersOnTrack = new ArrayList<Cluster>();
        for(Cluster cl : clusMap.values()) {
            clustersOnTrack.add(cl);
        }
        return clustersOnTrack;
    }
    
    public void MatchTrack2Traj(Seed trkcand, Map<Integer, 
            org.jlab.clas.tracking.kalmanfilter.helical.KFitter.HitOnTrack> traj, 
            org.jlab.rec.cvt.svt.Geometry sgeo, org.jlab.rec.cvt.bmt.BMTGeometry bgeo) {
        
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

        // adding the cross infos
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("SVT")) {
                int layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
                Point3D p = new Point3D(trkcand.get_Crosses().get(c).get_Point().x(), 
                        trkcand.get_Crosses().get(c).get_Point().y(), 
                        traj.get(layer).z);
                Vector3D d = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).set_Point(p);
                trkcand.get_Crosses().get(c).set_Dir(d);
            }
            if (trkcand.get_Crosses().get(c).get_Detector().equalsIgnoreCase("BMT")) {
                double ce = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                int layer = trkcand.get_Crosses().get(c).getOrderedRegion()+3;
                Cluster cluster = trkcand.get_Crosses().get(c).get_Cluster1();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                Vector3D v = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).set_Dir(v); 
                if (trkcand.get_Crosses().get(c).get_DetectorType()==BMTType.Z) {
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
                if (trkcand.get_Crosses().get(c).get_DetectorType()==BMTType.C) {
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
            org.jlab.rec.cvt.svt.Geometry SVTGeom, org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom) {
        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
                kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
                kf.KFHelix.getZ0(), kf.KFHelix.getTanL());
        helix.B = kf.KFHelix.getB();
        Track cand = new Track(helix);
        cand.setNDF(kf.NDF);
        cand.setChi2(kf.chi2);
        
        //for (Cross c : seed.get_Crosses()) {
        //    if (c.get_Detector().equalsIgnoreCase("SVT")) {
        //        continue;
        //    }
        //}
        
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
    
    public List<Seed> reFit(List<Seed> seedlist,
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom,
            Swim swimmer,  StraightTrackSeeder trseed) {
        List<Seed> filtlist = new ArrayList<Seed>();
        if(seedlist==null)
            return filtlist;
        for (Seed bseed : seedlist) {
            if(bseed == null)
                continue;
            List<Seed>  fseeds = this.reFitSeed(bseed, SVTGeom, BMTGeom, trseed);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }        
    
    public List<Seed> reFitSeed(Seed bseed, 
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom,
            StraightTrackSeeder trseed) {
        
        List<Seed> seedlist = new ArrayList<Seed>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : bseed.get_Crosses()) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector().equalsIgnoreCase("BMT")) {
                layr = c.getOrderedRegion()+3;
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0) {
                    c.isInSeed = false;
                    //System.out.println("refit "+c.printInfo());
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0 
                        && (int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr2)>0) {
                    c.set_CrossParamsSVT(null, SVTGeom); 
                    c.isInSeed = false;
                    refi.add(c); 
                }
            }
        }
        Collections.sort(refi);
        seedlist.addAll(trseed.findSeed(refi, refib, SVTGeom, BMTGeom, false));
        return seedlist;
    }
    
    public List<Seed> reFit(List<Seed> seedlist,
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom,
            Swim swimmer,  TrackSeederCA trseed,  TrackSeeder trseed2) {
        trseed = new TrackSeederCA();
        trseed2 = new TrackSeeder();
        List<Seed> filtlist = new ArrayList<Seed>();
        if(seedlist==null)
            return filtlist;
        for (Seed bseed : seedlist) {
            if(bseed == null)
                continue;
            List<Seed>  fseeds = this.reFitSeed(bseed, SVTGeom, BMTGeom, swimmer, trseed, trseed2);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }
    public List<Seed> reFitSeed(Seed bseed, 
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            org.jlab.rec.cvt.bmt.BMTGeometry BMTGeom,
            Swim swimmer,  TrackSeederCA trseed,  TrackSeeder trseed2) {
        boolean pass = true;

        List<Seed> seedlist = new ArrayList<Seed>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : bseed.get_Crosses()) {
            int layr = 0;
            int layr2 = 0;
            c.set_AssociatedTrackID(-1);
            if(c.get_Detector().equalsIgnoreCase("BMT")) {
                layr = c.getOrderedRegion()+3;
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0) {
                    c.isInSeed = false;
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0 
                        && (int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr2)>0) {
                    c.set_CrossParamsSVT(null, SVTGeom);
                    c.isInSeed = false;
                   // System.out.println("refit "+c.printInfo());
                    refi.add(c); 
                }
            }
        }
        Collections.sort(refi);
        seedlist = trseed.findSeed(refi, refib, SVTGeom, BMTGeom, swimmer);
        
        trseed2.unUsedHitsOnly = true;
        seedlist.addAll( trseed2.findSeed(refi, refib, SVTGeom, BMTGeom, swimmer)); 
        
        return seedlist;
    }
    
    public List<StraightTrack> reFit(List<StraightTrack> seedlist, 
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            CosmicFitter fitTrk,  TrackCandListFinder trkfindr) {
        fitTrk = new CosmicFitter();
        trkfindr = new TrackCandListFinder();
        List<StraightTrack> filtlist = new ArrayList<StraightTrack>();
        if(seedlist==null)
            return filtlist;
        for (StraightTrack bseed : seedlist) {
            if(bseed == null)
                continue;
            List<StraightTrack> fseeds = this.reFitSeed(bseed, SVTGeom, fitTrk, trkfindr);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }
    
    
    public List<StraightTrack> reFitSeed(StraightTrack cand, 
            org.jlab.rec.cvt.svt.Geometry SVTGeom,
            CosmicFitter fitTrk,  TrackCandListFinder trkfindr) {
        boolean pass = true;

        List<StraightTrack> seedlist = new ArrayList<StraightTrack>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : cand) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector().equalsIgnoreCase("BMT")) {
                layr = c.getOrderedRegion()+3;
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0) {
                    c.isInSeed = false;
                //    System.out.println("refit "+c.printInfo());
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr)>0 
                        && (int)org.jlab.rec.cvt.Constants.getLayersUsed().get(layr2)>0) {
                    c.set_CrossParamsSVT(null, SVTGeom);
                    c.isInSeed = false;
                   // System.out.println("refit "+c.printInfo());
                    refi.add(c); 
                }
            }
        }
        if(refi.size()>=3) {
            TrackCandListFinder.RayMeasurements NewMeasArrays = trkfindr.
                get_RayMeasurementsArrays((ArrayList<Cross>) refi, false, false, true);
            fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z,
                    NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, 
                    NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
            if(fitTrk.get_ray()!=null) {
                cand = new StraightTrack(fitTrk.get_ray());
                cand.addAll(refi);
                //refit with the SVT included to determine the z profile
                NewMeasArrays = trkfindr.
                get_RayMeasurementsArrays((ArrayList<Cross>) refi, false, false, false);
                fitTrk.fit(NewMeasArrays._X, NewMeasArrays._Y, NewMeasArrays._Z, 
                        NewMeasArrays._Y_prime, NewMeasArrays._ErrRt, NewMeasArrays._ErrY_prime, NewMeasArrays._ErrZ);
                cand = new StraightTrack(fitTrk.get_ray()); 
                cand.addAll(refi);
                seedlist.add(cand);
            }
        }
        return seedlist;
    }
}