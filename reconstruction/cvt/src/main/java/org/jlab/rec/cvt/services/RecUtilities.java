package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.objects.Strip;
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

import java.util.Collections;
import java.util.Comparator;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Arc3D;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.trajectory.Ray;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
/**
 * Service to return reconstructed TRACKS
 * format
 *
 * @author ziegler
 *
 */
public class RecUtilities {

    public void CleanupSpuriousCrosses(List<ArrayList<Cross>> crosses, List<Track> trks,
            SVTGeometry SVTGeom) {
        List<Cross> rmCrosses = new ArrayList<Cross>();
        
        for(Cross c : crosses.get(0)) {
            double z = SVTGeom.toLocal(c.get_Region()*2-1,
                                       c.get_Sector(),
                                       c.get_Point()).z();
        
            if(z<-0.1 || z>SVTGeometry.getModuleLength()) {
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
    
    public List<Surface> setMeasVecs(Seed trkcand, Swim swim) {
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
        
        // SVT measurements
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { 
            if(trkcand.get_Clusters().get(i).get_Detector()==DetectorType.BST) {
                int mlayer = trkcand.get_Clusters().get(i).get_Layer();
                Surface meas = trkcand.get_Clusters().get(i).measurement(mlayer);
                if((int)Constants.getLayersUsed().get(meas.getLayer())<1)
                    meas.notUsedInFit=true;
                if(i>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                    continue;
                KFSites.add(meas);
            }
        }
       
        // adding the BMT
        double hemisp = Math.signum(trkcand.get_Helix().getPointAtRadius(300).y());
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector()==DetectorType.BMT) {                
                int mlayer=trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6;
                Surface meas = trkcand.get_Crosses().get(c).get_Cluster1().measurement(mlayer);
                meas.hemisphere = hemisp;
                if((int)Constants.getLayersUsed().get(meas.getLayer())<1) {
                    //System.out.println("Exluding layer "+meas.getLayer()+trkcand.get_Crosses().get(c).printInfo());
                    meas.notUsedInFit=true;
                }
                if(c>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer())
                    continue;
                KFSites.add(meas);
            }
        }
        return KFSites;
    }
    private TrajectoryFinder tf = new TrajectoryFinder();
    
    
    
    // RDV: switch to cluster.mesurement()
    public List<Surface> setMeasVecs(StraightTrack trkcand, 
            SVTGeometry sgeo, BMTGeometry bgeo, Swim swim) {
        //Collections.sort(trkcand.get_Crosses());
        List<Surface> KFSites = new ArrayList<Surface>();
        Plane3D pln0 = new Plane3D(new Point3D(Constants.getXb(),Constants.getYb(),Constants.getZoffset()),
                                    new Vector3D(0,0,1));
        Surface meas0 = new Surface(pln0,new Point3D(0,0,0),
        new Point3D(-300,0,0), new Point3D(300,0,0));
        meas0.setSector(0);
        meas0.setLayer(0);
        meas0.setError(1);
        meas0.hemisphere = 1;
        KFSites.add(meas0); 
        Map<Integer, Cluster> clsMap = new HashMap<Integer, Cluster>();
        trkcand.sort(Comparator.comparing(Cross::getY).reversed());
        for (int i = 0; i < trkcand.size(); i++) { //SVT
            if(trkcand.get(i).get_Detector()==DetectorType.BST) {
                List<Cluster> cls = new ArrayList<Cluster>();
                int s = trkcand.get(i).get_Cluster1().get_Sector()-1;
                int lt = trkcand.get(i).get_Cluster1().get_Layer()-1;
                int lb = trkcand.get(i).get_Cluster2().get_Layer()-1;
                Ray ray = trkcand.get_ray();
                double yt= tf.getIntersectionTrackWithSVTModule(s, lt, 
                        ray.get_yxinterc(), ray.get_yxslope(), ray.get_yzinterc(), ray.get_yzslope(), sgeo)[1];
                double yb= tf.getIntersectionTrackWithSVTModule(s, lb, 
                        ray.get_yxinterc(), ray.get_yxslope(), ray.get_yzinterc(), ray.get_yzslope(), sgeo)[1];
               
                if(yt>yb) {
                    cls.add(trkcand.get(i).get_Cluster1());
                    cls.add(trkcand.get(i).get_Cluster2());
                } else {
                    cls.add(trkcand.get(i).get_Cluster2());
                    cls.add(trkcand.get(i).get_Cluster1());
                }
                for (int j = 0; j < cls.size(); j++) { 
                    int id = cls.get(j).get_Id();
                    double ce = cls.get(j).get_Centroid();
                    Point3D endPt1 = cls.get(j).getLine().origin();
                    Point3D endPt2 = cls.get(j).getLine().end();
                    Strip strp = new Strip(id, ce, endPt1.x(), endPt1.y(), endPt1.z(),
                                            endPt2.x(), endPt2.y(), endPt2.z());
//                    Plane3D pln = new Plane3D(endPt1,sgeo.findBSTPlaneNormal(cls.get(j).get_Sector(), 
//                            cls.get(j).get_Layer()));
                    Plane3D pln = new Plane3D(endPt1,cls.get(j).getN());
//                    Point3D Or = sgeo.getPlaneModuleOrigin(cls.get(j).get_Sector(), cls.get(j).get_Layer());
//                    Point3D En = sgeo.getPlaneModuleEnd(cls.get(j).get_Sector(), cls.get(j).get_Layer());
                    Surface meas = new Surface(pln, strp, cls.get(j).origin(), cls.get(j).end());
                    meas.hemisphere = Math.signum(trkcand.get(i).get_Point().y());
                    meas.setSector(cls.get(j).get_Sector());
                    double err = cls.get(j).get_Resolution();
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    //the thickness for multiple scattering.  MS is outward the thickness is set for the 1st layer in the superlayer
                    // air gap ignored
                    double thickn_ov_X0 = 0;
                    if(cls.get(j).get_Layer()%2==1)
                        thickn_ov_X0 = cls.get(j).get(0).get_Strip().getToverX0();
                    meas.setl_over_X0(thickn_ov_X0);
                    //if((int)Constants.getLayersUsed().get(meas.getLayer())<1)
                    //    meas.notUsedInFit=true;
                    
                    KFSites.add(meas);
                    clsMap.put(KFSites.size()-1, cls.get(j));
                    trkcand.clsMap = clsMap;
                }
        }
       
        // adding the BMT
        
        if (trkcand.get(i).get_Detector()==DetectorType.BMT) {
            int layer  = trkcand.get(i).get_Cluster1().get_Layer();
            int sector = trkcand.get(i).get_Cluster1().get_Sector();

            int id = trkcand.get(i).get_Cluster1().get_Id();
            double ce = trkcand.get(i).get_Cluster1().get_Centroid();
            
                if (trkcand.get(i).get_Type()==BMTType.Z) {
                    Point3D point = trkcand.get(i).get_Cluster1().getLine().origin();
                    double phi = bgeo.getPhi(layer, sector, point);
                    double err = trkcand.get(i).get_Cluster1().get_PhiErr();
                    bgeo.toLocal(point, layer, sector);
                    Strip strp = new Strip(id, ce, point.x(), point.y(), phi);  
                    
                    Surface meas = new Surface(bgeo.getTileSurface(layer, sector), strp);
                    meas.hemisphere = Math.signum(trkcand.get(i).get_Point().y());
                    meas.setTransformation(bgeo.toGlobal(layer,sector));                     
                    meas.setSector(trkcand.get(i).get_Sector());
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    //for multiple scattering
                    meas.setl_over_X0(bgeo.getToverX0(layer));

                    KFSites.add(meas);
                    clsMap.put(KFSites.size()-1, trkcand.get(i).get_Cluster1());
                }
                else if (trkcand.get(i).get_Type()==BMTType.C) {
                    double z   = trkcand.get(i).get_Point().z();
                    double err = trkcand.get(i).get_Cluster1().get_ZErr();
                    Arc3D arc  = trkcand.get(i).get_Cluster1().get_Arc();
                    Strip strp = new Strip(id, ce, arc);
         
                    Surface meas = new Surface(bgeo.getTileSurface(layer, sector), strp);
                    meas.hemisphere = Math.signum(trkcand.get(i).get_Point().y());
                    meas.setTransformation(bgeo.toGlobal(layer,sector)); 
                    
                    meas.setSector(sector);
                    meas.setError(err*err); // CHECK THIS .... DOES KF take e or e^2?
                    //for multiple scattering
                    meas.setl_over_X0(bgeo.getToverX0(layer));

                    KFSites.add(meas);
                    clsMap.put(KFSites.size()-1, trkcand.get(i).get_Cluster1());
                }
            }
        }
        for(int i = 0; i< KFSites.size(); i++) {
            KFSites.get(i).setLayer(i);
        }
        return KFSites;
    }
    
    public List<Cluster> FindClustersOnTrkNew (List<Cluster> allClusters, Helix helix, double P, int Q,
            SVTGeometry sgeo, Swim swimmer) { 

        int[] Sectors = new int[SVTGeometry.NLAYERS];
        // RDV it is not correct for tiltes/shifted geometry
        for (int ilayer = 0; ilayer < SVTGeometry.NLAYERS; ilayer++) {
            Point3D traj = helix.getPointAtRadius(sgeo.getLayerRadius(ilayer+1));
            int sec = sgeo.getSector(ilayer+1, traj);   
            Sectors[ilayer] = sec;
        }
        // initialize swimmer starting from the track vertex
        double maxPathLength = 1; 
        swimmer.SetSwimParameters((helix.xdca()+org.jlab.rec.cvt.Constants.getXb()) / 10, (helix.ydca()+org.jlab.rec.cvt.Constants.getYb()) / 10, helix.get_Z0() / 10, 
                     Math.toDegrees(helix.get_phi_at_dca()), Math.toDegrees(Math.acos(helix.costheta())),
                     P, Q, maxPathLength) ;
        double[] inters = null;
        double     path = 0;
        // SVT
        List<Cluster> clustersOnTrack = new ArrayList<Cluster>();
        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
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
            
            double  doca    = Double.MAX_VALUE;
            Point3D traj    = null;
            Cluster cluster = null;
            for(Cluster cls : allClusters) {
                if(cls.get_AssociatedTrackID()==-1 && cls.get_Sector()==sector && cls.get_Layer()==layer) {
                    // for first relevant cluster, swim to the plane to get the trajectory
                    if(traj==null) {
                        Vector3D n = cls.get(0).get_Strip().get_Normal();
                        Point3D p  = cls.get(0).get_Strip().get_Module().origin();
                        double d = n.dot(p.toVector3D());
                        inters = swimmer.SwimToPlaneBoundary(d/10.0, n, 1);
                        if(inters!=null)
                            traj = new Point3D(inters[0]*10, inters[1]*10, inters[2]*10);
                    }
                    if(traj!=null) {
                        double clsDoca = cls.residual(traj);
                        if(Math.abs(clsDoca)<Math.abs(doca) && Math.abs(clsDoca)<cls.get_CentroidError()*5) {
                            cluster = cls;
                            doca    = clsDoca;
                        }                           
                    }
                }
            }
            if(cluster!=null) clustersOnTrack.add(cluster);
        }
        return clustersOnTrack;        
    }
    
    public List<Cluster> FindClustersOnTrk (List<Cluster> allClusters, Helix helix, double P, int Q,
            SVTGeometry sgeo, Swim swimmer) { 
        Map<Integer, Cluster> clusMap = new HashMap<Integer, Cluster>();
        //Map<Integer, Double> stripMap = new HashMap<Integer, Double>();
        Map<Integer, Double> docaMap = new HashMap<Integer, Double>();
        Map<Integer, Point3D> trajMap = new HashMap<Integer, Point3D>();
        int[] Sectors = new int[SVTGeometry.NLAYERS];
        // RDV it is not correct for tilte/shifted geometry
        for (int a = 0; a < Sectors.length; a++) {
            Point3D I = helix.getPointAtRadius(sgeo.getLayerRadius(a+1));
           int sec = sgeo.getSector(a+1, I);   
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
        for (int l = 0; l < SVTGeometry.NLAYERS; l++) {
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
            
            Vector3D n = sgeo.getNormal(layer, sector);
            Point3D  p = sgeo.getModule(layer, sector).origin();
            double d = n.dot(p.toVector3D());
            inters = swimmer.SwimToPlaneBoundary(d/10.0, n, 1);
            if(inters!=null) {
                Point3D trp = new Point3D(inters[0]*10, inters[1]*10, inters[2]*10);
                double nearstp = sgeo.calcNearestStrip(inters[0]*10, inters[1]*10, inters[2]*10, layer, sector);
                //stripMap.put((sector*1000+layer), nearstp);
                docaMap.put((sector*1000+layer), 10.0);//sgeo.getDOCAToStrip(sector, layer, nearstp, trp)); 
                trajMap.put((sector*1000+layer), trp); 
            }
        }
        for(Cluster cls : allClusters) {
            int clsKey = cls.get_Sector()*1000+cls.get_Layer();
            if(cls.get_AssociatedTrackID()==-1 && trajMap!=null && trajMap.get(clsKey)!=null) {
                //double trjCent = stripMap.get(clsKey);
                double clsDoca = cls.residual(trajMap.get(clsKey));
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
            SVTGeometry sgeo, BMTGeometry bgeo) {
        
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { //SVT
            if(trkcand.get_Clusters().get(i).get_Detector()==DetectorType.BST) {
                Cluster cluster = trkcand.get_Clusters().get(i);
                int layer = trkcand.get_Clusters().get(i).get_Layer();
                int sector = trkcand.get_Clusters().get(i).get_Sector();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
//                double doca2Cls = sgeo.getDOCAToStrip(sector, layer, cluster.get_Centroid(), p);
//                double doca2Seed = sgeo.getDOCAToStrip(sector, layer, (double) cluster.get_SeedStrip().get_Strip(), p);
//                cluster.set_SeedResidual(doca2Seed); 
                cluster.set_CentroidResidual(traj.get(layer).resi);
                cluster.set_SeedResidual(p); 
            
                for (FittedHit hit : cluster) {
                    double doca1 = hit.residual(p);
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
            if (trkcand.get_Crosses().get(c).get_Detector()==DetectorType.BST) {
                int layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
                Point3D p = new Point3D(trkcand.get_Crosses().get(c).get_Point().x(), 
                        trkcand.get_Crosses().get(c).get_Point().y(), 
                        traj.get(layer).z);
                Vector3D d = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).set_Point(p);
                trkcand.get_Crosses().get(c).set_Dir(d);
            }
            if (trkcand.get_Crosses().get(c).get_Detector()==DetectorType.BMT) {
                
                double ce = trkcand.get_Crosses().get(c).get_Cluster1().get_Centroid();
                int layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6;
                Cluster cluster = trkcand.get_Crosses().get(c).get_Cluster1();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                Vector3D v = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).set_Dir(v); 
                if (trkcand.get_Crosses().get(c).get_Type()==BMTType.Z) {
                    trkcand.get_Crosses().get(c).set_Point(new Point3D(trkcand.get_Crosses().get(c).get_Point().x(),trkcand.get_Crosses().get(c).get_Point().y(),p.z()));
                    cluster.set_CentroidResidual(traj.get(layer).resi*cluster.getTile().baseArc().radius());
                    //double xc = trkcand.get_Crosses().get(c).get_Point().x();
                    //double yc = trkcand.get_Crosses().get(c).get_Point().y();
                    int bsector = trkcand.get_Crosses().get(c).get_Sector();
                    int blayer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
//                    double cxh = Math.cos(cluster.get_Phi())*bgeo.getRadiusMidDrift(blayer);
//                    double cyh = Math.sin(cluster.get_Phi())*bgeo.getRadiusMidDrift(blayer);
//                    double phic = bgeo.getPhi(blayer, bsector, new Point3D(cxh,cyh,0));
//                    double phit = bgeo.getPhi(blayer, bsector, p);
//                    double doca2Cls = (phic-phit)*bgeo.getRadiusMidDrift(blayer);
                    // RDV switch to use methogds from fitted hit
                    for (FittedHit hit : cluster) {
                        double xh = Math.cos(hit.get_Strip().get_Phi())*bgeo.getRadiusMidDrift(blayer);
                        double yh = Math.sin(hit.get_Strip().get_Phi())*bgeo.getRadiusMidDrift(blayer);
                        double hphic = bgeo.getPhi(blayer, bsector, new Point3D(xh,yh,0));
                        double hphit = bgeo.getPhi(blayer, bsector, p);
                        double doca1 = (hphic-hphit)*bgeo.getRadiusMidDrift(blayer);
                        
                        if(hit.get_Strip().get_Strip()==cluster.get_SeedStrip().get_Strip())
                            cluster.set_SeedResidual(doca1); 
                        if(traj.get(layer).isMeasUsed)
                            hit.set_TrkgStatus(1);
                        hit.set_docaToTrk(doca1);  

                    }
                }
                if (trkcand.get_Crosses().get(c).get_Type()==BMTType.C) {
                    double z = trkcand.get_Crosses().get(c).get_Point().z();
                    double err = trkcand.get_Crosses().get(c).get_Cluster1().get_ZErr();
                    trkcand.get_Crosses().get(c).set_Point(new Point3D(p.x(),p.y(),
                            trkcand.get_Crosses().get(c).get_Cluster1().center().z()));
                    cluster.set_CentroidResidual(traj.get(layer).resi);
                    cluster.set_SeedResidual(p); 
                    for (FittedHit hit : cluster) {
                        if(traj.get(layer).isMeasUsed)
                            hit.set_TrkgStatus(1);
                        hit.set_docaToTrk(p);  

                    }
                }
            }
        }
    }
    
    public Track OutputTrack(Seed seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf,
            SVTGeometry SVTGeom, BMTGeometry BMTGeom) {
        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
                kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
                kf.KFHelix.getZ0(), kf.KFHelix.getTanL());
        helix.B = kf.KFHelix.getB();
        Track cand = new Track(helix);
        cand.setNDF(kf.NDF);
        cand.setChi2(kf.chi2);
        
        for (Cross c : seed.get_Crosses()) {
            if (c.get_Detector()==DetectorType.BST) {
                continue;
            }
        }
        
        this.MatchTrack2Traj(seed, kf.TrjPoints, SVTGeom, BMTGeom);
        cand.addAll(seed.get_Crosses());
        for(Cluster cl : seed.get_Clusters()) {
            
            int layer = cl.get_Layer();
            int sector = cl.get_Sector();
            
            if(cl.get_Detector()==DetectorType.BMT) {
                
                layer = layer + 6;
                
               if(cl.get_Type() == BMTType.Z) {
                   
                Line3D cln = cl.getAxis();
                cl.setN(cln.distance(new Point3D(kf.TrjPoints.get(layer).x,kf.TrjPoints.get(layer).y,kf.TrjPoints.get(layer).z)).direction().asUnit());
                cl.setS(cl.getL().cross(cl.getN()).asUnit());
                 
               }
                
            }
            //double x = kf.TrjPoints.get(layer).x;
            //double y = kf.TrjPoints.get(layer).y;
            //double z = kf.TrjPoints.get(layer).z;
            //double px = kf.TrjPoints.get(layer).px;
            //double py = kf.TrjPoints.get(layer).py;
            //double pz = kf.TrjPoints.get(layer).pz;
            cl.setTrakInters(new Point3D(kf.TrjPoints.get(layer).x,kf.TrjPoints.get(layer).y,kf.TrjPoints.get(layer).z));
        }
        
        return cand;
        
    }
    public Track OutputTrack(StraightTrack seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf,
            SVTGeometry SVTGeom, BMTGeometry BMTGeom) {
        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
                kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
                kf.KFHelix.getZ0(), kf.KFHelix.getTanL());
        helix.B = kf.KFHelix.getB();
        Track cand = new Track(helix);
        cand.setNDF(kf.NDF);
        cand.setChi2(kf.chi2); 
        cand.addAll(seed); 
        
        return cand;
        
    }
    public Track OutputTrack(Seed seed) {
        
        Track cand = new Track(seed.get_Helix());
        
        for (Cross c : seed.get_Crosses()) {
            if (c.get_Detector()==DetectorType.BST) {
                continue;
            }
        }
        cand.addAll(seed.get_Crosses());
        return cand;
        
    }
    
    public List<Seed> reFit(List<Seed> seedlist,
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
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
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
            StraightTrackSeeder trseed) {
        
        List<Seed> seedlist = new ArrayList<Seed>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : bseed.get_Crosses()) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector()==DetectorType.BMT) {
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
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
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
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
            Swim swimmer,  TrackSeederCA trseed,  TrackSeeder trseed2) {
        boolean pass = true;

        List<Seed> seedlist = new ArrayList<Seed>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : bseed.get_Crosses()) {
            int layr = 0;
            int layr2 = 0;
            c.set_AssociatedTrackID(-1);
            if(c.get_Detector()==DetectorType.BMT) {
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
            SVTGeometry SVTGeom, CosmicFitter fitTrk,  TrackCandListFinder trkfindr) {
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
            SVTGeometry SVTGeom, CosmicFitter fitTrk,TrackCandListFinder trkfindr) {
        boolean pass = true;

        List<StraightTrack> seedlist = new ArrayList<StraightTrack>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : cand) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector()==DetectorType.BMT) {
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
    
    public double[] MCtrackPars(DataEvent event) {
        double[] value = new double[6];
        if (event.hasBank("MC::Particle") == false) {
            return value;
        }
        DataBank bank = event.getBank("MC::Particle");
        
        // fills the arrays corresponding to the variables
        if(bank!=null) {
            value[0] = (double) bank.getFloat("vx", 0)*10;
            value[1] = (double) bank.getFloat("vy", 0)*10;
            value[2] = (double) bank.getFloat("vz", 0)*10;
            value[3] = (double) bank.getFloat("px", 0);
            value[4] = (double) bank.getFloat("py", 0);
            value[5] = (double) bank.getFloat("pz", 0);
        }
        return value;
    }

    
}