package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import org.jlab.rec.cvt.trajectory.Helix;
import org.jlab.clas.tracking.kalmanfilter.*;

import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
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
import java.util.Map.Entry;
import org.jlab.detector.base.DetectorType;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.fit.CircleFitPars;
import org.jlab.rec.cvt.fit.CircleFitter;
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

    public void CleanupSpuriousCrosses(List<ArrayList<Cross>> crosses, List<Track> trks) {
        List<Cross> rmCrosses = new ArrayList<Cross>();
        
        for(Cross c : crosses.get(0)) {
            if(!Constants.SVTGEOMETRY.isInFiducial(c.get_Cluster1().get_Layer(), c.get_Sector(), c.get_Point()))
                rmCrosses.add(c);
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
            // RDV why removing the whole track?
            trks.removeAll(rmTrks);
        }
    }
    
    public List<Surface> setMeasVecs(Seed trkcand, double xb, double yb, Swim swim) {
        //Collections.sort(trkcand.get_Crosses());
        List<Surface> KFSites = new ArrayList<>();
        Vector3D u = new Vector3D(0,0,1);
        Point3D  p = new Point3D(xb, yb, 0);
        Line3D   l = new Line3D(p, u);
        Surface meas0 = new Surface(l.origin(), l.end(), Constants.DEFAULTSWIMACC);
        meas0.setSector(0);
        meas0.setLayer(0);
        meas0.setError(Constants.getRbErr());
        KFSites.add(meas0); 
        // SVT measurements
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { 
            if(trkcand.get_Clusters().get(i).get_Detector()==DetectorType.BST) {
                int layer = trkcand.get_Clusters().get(i).get_Layer();
                Surface meas = trkcand.get_Clusters().get(i).measurement();
                meas.setIndex(layer);
                if((int)Constants.getUsedLayers().get(meas.getLayer())<1)
                    meas.notUsedInFit=true;
                if(i>0 && KFSites.get(KFSites.size()-1).getIndex()==meas.getIndex())
                    continue;
                KFSites.add(meas);
            }
        }
        
        // adding the BMT
        double hemisp = Math.signum(trkcand.get_Helix().getPointAtRadius(300).y());
        for (int c = 0; c < trkcand.get_Crosses().size(); c++) {
            if (trkcand.get_Crosses().get(c).get_Detector()==DetectorType.BMT) { 
                int layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
                Surface meas = trkcand.get_Crosses().get(c).get_Cluster1().measurement();
                meas.setIndex(layer+SVTGeometry.NLAYERS);
                meas.setLayer(layer+SVTGeometry.NLAYERS);
                meas.hemisphere = hemisp;
                if((int)Constants.getUsedLayers().get(layer+SVTGeometry.NLAYERS)<1) {
                    //System.out.println("Exluding layer "+meas.getLayer()+trkcand.get_Crosses().get(c).printInfo());
                    meas.notUsedInFit=true;
                }
                if(c>0 && KFSites.get(KFSites.size()-1).getIndex()==meas.getIndex())
                    continue;
                KFSites.add(meas);
            }
        }
        for(Surface s : KFSites) System.out.println(s.toString());
        return KFSites;
    }
    private TrajectoryFinder tf = new TrajectoryFinder();
    
    // Not used, replaced by Measurements class
    public List<Surface> setMeasVecs(StraightTrack trkcand, Swim swim) {
        if(trkcand.clsMap!=null) trkcand.clsMap.clear(); //VZ: reset cluster map for second pass tracking with isolated SVT clusters
        //Collections.sort(trkcand.get_Crosses());
        List<Surface> KFSites = new ArrayList<>();
        Vector3D u = trkcand.get_ray().get_dirVec();
        Plane3D pln0 = new Plane3D(new Point3D(0, 0, 0), u);
        Surface meas0 = new Surface(pln0,new Point3D(0,0,0),
        new Point3D(-300,0,0), new Point3D(300,0,0),Constants.DEFAULTSWIMACC);
        meas0.setSector(0);
        meas0.setLayer(0);
        meas0.setError(1);
        meas0.hemisphere = 1;
        KFSites.add(meas0); 
        
        Map<Integer, Cluster> clsMap = new HashMap<>();
        trkcand.sort(Comparator.comparing(Cross::getY).reversed());
        for (int i = 0; i < trkcand.size(); i++) { //SVT
            if(trkcand.get(i).get_Detector()==DetectorType.BST) {
                List<Cluster> cls = new ArrayList<>();
                
                if(trkcand.get(i).get_Cluster1()!=null && 
                        trkcand.get(i).get_Cluster2()!=null) { //VZ: modification for pseudocrosses that contain only one cluster
                    int sector   = trkcand.get(i).get_Cluster1().get_Sector();
                    int layertop = trkcand.get(i).get_Cluster1().get_Layer();
                    int layerbot = trkcand.get(i).get_Cluster2().get_Layer();
                    Ray ray = trkcand.get_ray();
                    Point3D top    = new Point3D();
                    Point3D bottom = new Point3D();
                    Constants.SVTGEOMETRY.getPlane(layertop, sector).intersection(ray.toLine(), top);
                    Constants.SVTGEOMETRY.getPlane(layerbot, sector).intersection(ray.toLine(), bottom);

                    if(top.y()>bottom.y()) {
                        cls.add(trkcand.get(i).get_Cluster1());
                        cls.add(trkcand.get(i).get_Cluster2());
                    } else {
                        cls.add(trkcand.get(i).get_Cluster2());
                        cls.add(trkcand.get(i).get_Cluster1());
                    }
                } else {
                    if(trkcand.get(i).get_Cluster1()!=null) cls.add(trkcand.get(i).get_Cluster1());
                    if(trkcand.get(i).get_Cluster2()!=null) cls.add(trkcand.get(i).get_Cluster2());
                }
                for (int j = 0; j < cls.size(); j++) { 
                    int mlayer = cls.get(j).get_Layer();
                    Surface meas = cls.get(j).measurement();
                    meas.setLayer(mlayer);
                    meas.hemisphere = Math.signum(trkcand.get(i).get_Point().y());;
                    // set SVT material budget according to track direction
                    if(j==0) meas.setl_over_X0(SVTGeometry.getToverX0());
                    else     meas.setl_over_X0(0);
                    // RDV to be tested
//                    if((int) Constants.getUsedLayers().get(meas.getLayer())<1)
//                        meas.notUsedInFit=true; //VZ: commenting this out prevents the layer exclusion to be employed in tracking
                    if(i>0 && KFSites.get(KFSites.size()-1).getLayer()==mlayer
                           && KFSites.get(KFSites.size()-1).hemisphere==meas.hemisphere)
                        continue;
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
                Surface meas = trkcand.get(i).get_Cluster1().measurement();
                meas.setLayer(layer);
                meas.hemisphere = Math.signum(trkcand.get(i).get_Point().y());;
                if((int)Constants.getUsedLayers().get(meas.getLayer())<1) {
                    meas.notUsedInFit=true;
                }
                if(i>0 && KFSites.get(KFSites.size()-1).getLayer()==meas.getLayer()
                       && KFSites.get(KFSites.size()-1).hemisphere==meas.hemisphere)
                    continue;
                KFSites.add(meas);
                clsMap.put(KFSites.size()-1, trkcand.get(i).get_Cluster1());
            }
        }
        for(int i = 0; i< KFSites.size(); i++) {
            KFSites.get(i).setLayer(i);
        }
        return KFSites;
    }
    
    public List<Cluster> FindClustersOnTrack(List<Cluster> allClusters, StraightTrack trkcand) {
        List<Cluster> clustersOnTrack = new ArrayList<>();
        Map<Integer, Cluster> clusterMap = new HashMap<Integer, Cluster>();
        trkcand.sort(Comparator.comparing(Cross::getY).reversed());
        List<Cluster> clsList = new ArrayList<Cluster>();
        int crsCnt = 0;        
        for (int i = 0; i < trkcand.size(); i++) { //SVT cluster sorting
            if(trkcand.get(i).get_Detector()==DetectorType.BST) {
                crsCnt++;
                int sector   = trkcand.get(i).get_Cluster1().get_Sector();
                int layertop = trkcand.get(i).get_Cluster1().get_Layer();
                int layerbot = trkcand.get(i).get_Cluster2().get_Layer();
                Ray ray = trkcand.get_ray();
                Point3D top    = new Point3D();
                Point3D bottom = new Point3D();
                Constants.SVTGEOMETRY.getPlane(layertop, sector).intersection(ray.toLine(), top);
                Constants.SVTGEOMETRY.getPlane(layerbot, sector).intersection(ray.toLine(), bottom);
                
                if(top.y()>bottom.y()) {
                    clsList.add(trkcand.get(i).get_Cluster1());
                    clsList.add(trkcand.get(i).get_Cluster2());
                } else {
                    clsList.add(trkcand.get(i).get_Cluster2());
                    clsList.add(trkcand.get(i).get_Cluster1());
                }
            }
        } 
        
        
        for(Cluster cluster : clsList) {
            clusterMap.put(SVTGeometry.getModuleId(cluster.get_Layer(), cluster.get_Sector()), cluster);
        }  
        
        // for each layer
        for (int ilayer = 0; ilayer < SVTGeometry.NLAYERS; ilayer++) {
            int layer = ilayer + 1;
            
            for(int isector=0; isector<SVTGeometry.NSECTORS[ilayer]; isector++) {
                int sector = isector+1;
                
                Ray ray = trkcand.get_ray();
                Point3D traj = new Point3D();
                Constants.SVTGEOMETRY.getPlane(layer, sector).intersection(ray.toLine(), traj);
                
                int key = SVTGeometry.getModuleId(layer, sector);
                
                if(traj!=null && Constants.SVTGEOMETRY.isInFiducial(layer, sector, traj)) {
                    double  doca    = Double.POSITIVE_INFINITY;
                    // loop over all clusters in the same sector and layer that are not associated to s track
                    for(Cluster cls : allClusters) {
                        if(cls.get_AssociatedTrackID()==-1 && cls.get_Sector()==sector && cls.get_Layer()==layer) {
                            double clsDoca = cls.residual(traj);
                            cls.setTrakInters(traj);
                            // save the ones that have better doca
                            if(Math.abs(clsDoca)<Math.abs(doca) && Math.abs(clsDoca)<10*cls.size()*cls.get_SeedStrip().get_Pitch()/Math.sqrt(12)) {
                                if(clusterMap.containsKey(key) && clusterMap.get(key).get_AssociatedTrackID()==-1) {
                                    clusterMap.replace(key, cls);
                                } else {
                                    if(!clusterMap.containsKey(key)) {
                                        clusterMap.put(key, cls); 
                                    }
                                }
                                doca = clsDoca;
                            }                           
                        }
                    }
                }
            }
        }
        // if any lost cluster with doca better than the seed is found, save it
        
        for(Entry<Integer,Cluster> entry : clusterMap.entrySet()) {
            if(entry.getValue().get_AssociatedTrackID()==-1) clustersOnTrack.add(entry.getValue());
        }
        return clustersOnTrack;
        
    }
    
    public List<Cluster> FindClustersOnTrk(List<Cluster> allClusters, List<Cluster> seedCluster, Helix helix, double P, int Q, Swim swimmer) { 
        // initialize swimmer starting from the track vertex
        double maxPathLength = 1; 
        Point3D vertex = helix.getVertex();
        swimmer.SetSwimParameters(vertex.x()/10, vertex.y()/10, vertex.z()/10, 
                     Math.toDegrees(helix.get_phi_at_dca()), Math.toDegrees(Math.acos(helix.costheta())),
                     P, Q, maxPathLength) ;
        double[] inters = null;

        // load SVT clusters that are in the seed
        Map<Integer,Cluster> clusterMap = new HashMap<>();
        for(Cluster cluster : seedCluster) {
            if(cluster.get_Detector() == DetectorType.BMT)
                continue;
            clusterMap.put(SVTGeometry.getModuleId(cluster.get_Layer(), cluster.get_Sector()), cluster);
        }   
        
        // for each layer
        for (int ilayer = 0; ilayer < SVTGeometry.NLAYERS; ilayer++) {
            int layer = ilayer + 1;

            // identify the sector the track may be going through (this doesn't account for misalignments
            Point3D helixPoint = helix.getPointAtRadius(Constants.SVTGEOMETRY.getLayerRadius(layer));
            
            // reinitilize swimmer from last surface
            if(inters!=null) {
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], inters[3], inters[4], inters[5], Q);
            }
            
            for(int isector=0; isector<SVTGeometry.NSECTORS[ilayer]; isector++) {
                int sector = isector+1;
                
                // check the angle between the trajectory point and the sector 
                // and skip sectors that are too far (more than the sector angular coverage)
                Vector3D n = Constants.SVTGEOMETRY.getNormal(layer, sector);
                double deltaPhi = Math.acos(helixPoint.toVector3D().asUnit().dot(n));
                double buffer = Math.toRadians(1.);
                if(Math.abs(deltaPhi)>2*Math.PI/SVTGeometry.NSECTORS[ilayer]+buffer) continue;
                
                int key = SVTGeometry.getModuleId(layer, sector);
                
                // calculate trajectory
                Point3D traj = null;
                Point3D  p = Constants.SVTGEOMETRY.getModule(layer, sector).origin();
                Point3D pm = new Point3D(p.x()/10, p.y()/10, p.z()/10);
                inters = swimmer.SwimPlane(n, pm, Constants.DEFAULTSWIMACC/10);
                if(inters!=null) {
                    traj = new Point3D(inters[0]*10, inters[1]*10, inters[2]*10);
                } 
                // if trajectory is valid, look for missing clusters
                if(traj!=null && Constants.SVTGEOMETRY.isInFiducial(layer, sector, traj)) {
                    double  doca    = Double.POSITIVE_INFINITY; 
                    //if(clusterMap.containsKey(key)) {
                    //    Cluster cluster = clusterMap.get(key);
                    //    doca = cluster.residual(traj); 
                    //}
                    // loop over all clusters in the same sector and layer that are noy associated to s track
                    for(Cluster cls : allClusters) {
                        if(cls.get_AssociatedTrackID()==-1 && cls.get_Sector()==sector && cls.get_Layer()==layer) {
                            double clsDoca = cls.residual(traj); 
                            // save the ones that have better doca
                            if(Math.abs(clsDoca)<Math.abs(doca) && Math.abs(clsDoca)<10*cls.size()*cls.get_SeedStrip().get_Pitch()/Math.sqrt(12)) {
                                if(clusterMap.containsKey(key) && clusterMap.get(key).get_AssociatedTrackID()==-1) {
                                    clusterMap.replace(key, cls); 
                                } else {
                                    if(!clusterMap.containsKey(key)) {
                                        clusterMap.put(key, cls); 
                                    }
                                }
                                doca = clsDoca;
                            }                           
                        }
                    }
                }
            }
        }
        // if any lost cluster with doca better than the seed is found, save it
        List<Cluster> clustersOnTrack = new ArrayList<>();
        for(Entry<Integer,Cluster> entry : clusterMap.entrySet()) {
            if(entry.getValue().get_AssociatedTrackID()==-1) clustersOnTrack.add(entry.getValue());
        }
        return clustersOnTrack;
    }
    public List<Cluster> findBMTClustersOnTrk(List<Cluster> allClusters, List<Cross> seedCrosses, Helix helix, double P, int Q, Swim swimmer) { 
        // initialize swimmer starting from the track vertex
        double maxPathLength = 1; 
        Point3D vertex = helix.getVertex();
        swimmer.SetSwimParameters(vertex.x()/10, vertex.y()/10, vertex.z()/10, 
                     Math.toDegrees(helix.get_phi_at_dca()), Math.toDegrees(Math.acos(helix.costheta())),
                     P, Q, maxPathLength) ;
        double[] inters = null;
        // load SVT clusters that are in the seed
        Map<Integer,Cluster> clusterMap = new HashMap<>(); 
        for(Cross cross : seedCrosses) {
            if(cross.get_Detector() != DetectorType.BMT)
                continue;
            Cluster cluster = cross.get_Cluster1(); 
            cluster.set_AssociatedTrackID(0);
            clusterMap.put(Constants.BMTGEOMETRY.getModuleId(cluster.get_Layer(), cluster.get_Sector()), cluster);
        }   
        
        // for each layer
        for (int ilayer = 0; ilayer < BMTGeometry.NLAYERS; ilayer++) {
            int layer = ilayer + 1;
            double radius  = Constants.BMTGEOMETRY.getRadiusMidDrift(layer);
            // identify the sector the track may be going through (this doesn't account for misalignments
            Point3D helixPoint = helix.getPointAtRadius(radius);
            // reinitilize swimmer from last surface
            if(inters!=null) {
                swimmer.SetSwimParameters(inters[0], inters[1], inters[2], inters[3], inters[4], inters[5], Q);
            }
            
            for(int isector=0; isector<BMTGeometry.NSECTORS; isector++) {
                int sector = isector+1;
                
                // check the angle between the trajectory point and the sector 
                // and skip sectors that are too far (more than the sector angular coverage)
                if(Constants.BMTGEOMETRY.inDetector(layer, sector, helixPoint)==false)
                    continue;
                 
                // calculate trajectory
                Point3D traj = null;
                inters = swimmer.SwimRho(radius/10, Constants.SWIMACCURACYBMT/10);
                
                if(inters!=null) {
                    traj = new Point3D(inters[0]*10, inters[1]*10, inters[2]*10);
                } 
                
                int key = BMTGeometry.getModuleId(layer, sector);
                
               
                // if trajectory is valid, look for missing clusters
                if(traj!=null && Constants.BMTGEOMETRY.inDetector(layer, sector, traj)) {
                    double  doca    = Double.POSITIVE_INFINITY; 
                    // loop over all clusters in the same sector and layer that are not associated to s track
                    for(Cluster cls : allClusters) {
                        if(cls.get_AssociatedTrackID()==-1 && cls.get_Sector()==sector && cls.get_Layer()==layer) {
                            double clsDoca = cls.residual(traj); 
                            // save the ones that have better doca
                            if(Math.abs(clsDoca)<Math.abs(doca) && Math.abs(clsDoca)<10*cls.size()*cls.get_SeedStrip().get_Pitch()/Math.sqrt(12)) {
                                if(clusterMap.containsKey(key) && clusterMap.get(key).get_AssociatedTrackID()==-1) {
                                    clusterMap.replace(key, cls); 
                                } else {
                                    if(!clusterMap.containsKey(key)) {
                                        clusterMap.put(key, cls); 
                                    }
                                }
                                doca = clsDoca;
                            }                           
                        }
                    }
                }
            }
        }
        // if any lost cluster with doca better than the seed is found, save it
        List<Cluster> clustersOnTrack = new ArrayList<>();
        for(Entry<Integer,Cluster> entry : clusterMap.entrySet()) {
            if(entry.getValue().get_AssociatedTrackID()==-1 && entry.getValue().flagForExclusion) clustersOnTrack.add(entry.getValue());
        }
        return clustersOnTrack;
    }
    
    List<Cross> findCrossesOnBMTTrack(List<Cluster> bmtclsOnTrack, CrossMaker cm, int idx) {
         // fill the sorted list
        ArrayList<ArrayList<Cluster>> sortedClusters = cm.sortClusterByDetectorAndIO(bmtclsOnTrack);
        ArrayList<Cluster> bmt_Clayrclus = sortedClusters.get(2);
        ArrayList<Cluster> bmt_Zlayrclus = sortedClusters.get(3);
        ArrayList<Cross> BMTCrosses = cm.findBMTCrosses(bmt_Clayrclus, bmt_Zlayrclus, idx);
        
        return BMTCrosses;
    }
    public void MatchTrack2Traj(Seed trkcand, Map<Integer, 
            org.jlab.clas.tracking.kalmanfilter.helical.KFitter.HitOnTrack> traj) {
        
        for (int i = 0; i < trkcand.get_Clusters().size(); i++) { //SVT
            if(trkcand.get_Clusters().get(i).get_Detector()==DetectorType.BST) {
                Cluster cluster = trkcand.get_Clusters().get(i);
                int layer  = trkcand.get_Clusters().get(i).get_Layer();
                int sector = trkcand.get_Clusters().get(i).get_Sector();
                Point3D p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                cluster.set_CentroidResidual(traj.get(layer).resi);
                cluster.set_SeedResidual(p);             
                for (Hit hit : cluster) {
                    double doca1 = hit.residual(p);
                    double sigma1 = Constants.SVTGEOMETRY.getSingleStripResolution(layer, hit.get_Strip().get_Strip(), traj.get(layer).z);
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
                int  layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer();
                Vector3D d = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).updateSVTCross(d);
            }
            if (trkcand.get_Crosses().get(c).get_Detector()==DetectorType.BMT) {
                // update cross position
                int layer = trkcand.get_Crosses().get(c).get_Cluster1().get_Layer()+6;
                Point3D  p = new Point3D(traj.get(layer).x, traj.get(layer).y, traj.get(layer).z);
                Vector3D v = new Vector3D(traj.get(layer).px, traj.get(layer).py, traj.get(layer).pz).asUnit();
                trkcand.get_Crosses().get(c).updateBMTCross(p, v);
                trkcand.get_Crosses().get(c).set_Dir(v); 
                Cluster cluster = trkcand.get_Crosses().get(c).get_Cluster1();
                if (trkcand.get_Crosses().get(c).get_Type()==BMTType.Z) {
                    cluster.set_CentroidResidual(traj.get(layer).resi*cluster.getTile().baseArc().radius());
                }
                else if (trkcand.get_Crosses().get(c).get_Type()==BMTType.C) {
                    cluster.set_CentroidResidual(traj.get(layer).resi);
                    cluster.set_SeedResidual(p); 
                }
                for (Hit hit : cluster) {
                    hit.set_docaToTrk(hit.residual(p));
                    if(traj.get(layer).isMeasUsed) hit.set_TrkgStatus(1);
                }
            }
        }
    }
    
//    public Track OutputTrack(Seed seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf) {
//        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
//                                                  kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), kf.KFHelix.getZ0(), 
//                                                  kf.KFHelix.getTanL(), kf.KFHelix.getXb(), kf.KFHelix.getYb());
//        helix.B = kf.KFHelix.getB();
//        Track cand = new Track(helix);
//        cand.setNDF(kf.NDF);
//        cand.setChi2(kf.chi2);
//        
//        for (Cross c : seed.get_Crosses()) {
//            if (c.get_Detector()==DetectorType.BST) {
//                continue;
//            }
//        }
//        
//        this.MatchTrack2Traj(seed, kf.TrjPoints);
//        cand.addAll(seed.get_Crosses());
//        for(Cluster cl : seed.get_Clusters()) {
//            
//            int layer = cl.get_Layer();
//            int sector = cl.get_Sector();
//            
//            if(cl.get_Detector()==DetectorType.BMT) {
//                
//                layer = layer + 6;
//                
//               if(cl.get_Type() == BMTType.C) {
//                   
//                Line3D cln = cl.getAxis();
//                cl.setN(cln.distance(new Point3D(kf.TrjPoints.get(layer).x,kf.TrjPoints.get(layer).y,kf.TrjPoints.get(layer).z)).direction().asUnit());
//                cl.setL(cl.getS().cross(cl.getN()).asUnit());
//                 
//               }
//                
//            }
//            //double x = kf.TrjPoints.get(layer).x;
//            //double y = kf.TrjPoints.get(layer).y;
//            //double z = kf.TrjPoints.get(layer).z;
//            //double px = kf.TrjPoints.get(layer).px;
//            //double py = kf.TrjPoints.get(layer).py;
//            //double pz = kf.TrjPoints.get(layer).pz;
//            cl.setTrakInters(new Point3D(kf.TrjPoints.get(layer).x,kf.TrjPoints.get(layer).y,kf.TrjPoints.get(layer).z));
//        }
//        
//        return cand;
//        
//    }
//    public Track OutputTrack(StraightTrack seed, org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf) {
//        org.jlab.rec.cvt.trajectory.Helix helix = new org.jlab.rec.cvt.trajectory.Helix(kf.KFHelix.getD0(), 
//                kf.KFHelix.getPhi0(), kf.KFHelix.getOmega(), 
//                kf.KFHelix.getZ0(), kf.KFHelix.getTanL());
//        helix.B = kf.KFHelix.getB();
//        Track cand = new Track(helix);
//        cand.setNDF(kf.NDF);
//        cand.setChi2(kf.chi2); 
//        cand.addAll(seed); 
//        
//        return cand;
//        
//    }
//    public Track OutputTrack(Seed seed) {
//        
//        Track cand = new Track(seed.get_Helix());
//        for (Cross c : seed.get_Crosses()) {
//            if (c.get_Detector()==DetectorType.BST) {
//                continue;
//            }
//        }
//        cand.addAll(seed.get_Crosses());
//        return cand;
//        
//    }
    
    public List<Seed> reFit(List<Seed> seedlist, Swim swimmer,  StraightTrackSeeder trseed) {
        List<Seed> filtlist = new ArrayList<Seed>();
        if(seedlist==null)
            return filtlist;
        for (Seed bseed : seedlist) {
            if(bseed == null)
                continue;
            List<Seed>  fseeds = this.reFitSeed(bseed, trseed);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }        
    
    public List<Seed> reFitSeed(Seed bseed, StraightTrackSeeder trseed) {
        
        List<Seed> seedlist = new ArrayList<Seed>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : bseed.get_Crosses()) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector()==DetectorType.BMT) {
                layr = c.getOrderedRegion()+3;
                if((int)Constants.getUsedLayers().get(layr)>0) {
                    c.isInSeed = false;
                    //System.out.println("refit "+c.printInfo());
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)Constants.getUsedLayers().get(layr)>0 
                        && (int)Constants.getUsedLayers().get(layr2)>0) {
                    c.updateSVTCross(null); 
                    c.isInSeed = false;
                    refi.add(c); 
                }
            }
        }
        Collections.sort(refi);
        seedlist.addAll(trseed.findSeed(refi, refib, false));
        return seedlist;
    }
    public boolean reFitCircle(Seed seed, int iter, double xb, double yb) {
        boolean fitStatus = false;
        
        List<Double> Xs = new ArrayList<>() ;
        List<Double> Ys = new ArrayList<>() ;
        List<Double> Ws = new ArrayList<>() ;
        
        CircleFitter circlefit = new CircleFitter(xb, yb);
        for(int i = 0; i< iter; i++) {
            Xs.clear();
            Ys.clear();
            Ws.clear();
            List<Cross> seedCrosses = seed.get_Crosses();
            
            for (int j = 0; j < seedCrosses.size(); j++) {
                if (seedCrosses.get(j).get_Type() == BMTType.C)
                    continue;
                
                Xs.add(seedCrosses.get(j).get_Point().x());
                Ys.add(seedCrosses.get(j).get_Point().y());
                Ws.add(1. / (seedCrosses.get(j).get_PointErr().x()*seedCrosses.get(j).get_PointErr().x()
                            +seedCrosses.get(j).get_PointErr().y()*seedCrosses.get(j).get_PointErr().y()));

            }

            fitStatus = circlefit.fitStatus(Xs, Ys, Ws, Xs.size());

            if(fitStatus) {
                CircleFitPars pars = circlefit.getFit();
                seed.get_Helix().set_curvature(pars.rho());           
                seed.get_Helix().set_dca(-pars.doca());
                seed.get_Helix().set_phi_at_dca(pars.phi());
                seed.update_Crosses();
            }
        }
        return fitStatus;
    }
    
    public List<Seed> reFit(List<Seed> seedlist, Swim swimmer,  TrackSeederCA trseed,  TrackSeeder trseed2, double xb, double yb) {
        trseed = new TrackSeederCA(swimmer, xb, yb);
        trseed2 = new TrackSeeder(swimmer, xb, yb);
        List<Seed> filtlist = new ArrayList<Seed>();
        if(seedlist==null)
            return filtlist;
        for (Seed bseed : seedlist) {
            if(bseed == null)
                continue;
            List<Seed>  fseeds = this.reFitSeed(bseed, swimmer, trseed, trseed2);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }
    public List<Seed> reFitSeed(Seed bseed, Swim swimmer,  TrackSeederCA trseed,  TrackSeeder trseed2) {
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
                if((int)Constants.getUsedLayers().get(layr)>0) {
                    c.isInSeed = false;
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)Constants.getUsedLayers().get(layr)>0 
                        && (int)Constants.getUsedLayers().get(layr2)>0) {
                    c.updateSVTCross(null);
                    c.isInSeed = false;
                   // System.out.println("refit "+c.printInfo());
                    refi.add(c); 
                }
            }
        }
        Collections.sort(refi);
        seedlist = trseed.findSeed(refi, refib);
        
        trseed2.unUsedHitsOnly = true;
        seedlist.addAll( trseed2.findSeed(refi, refib)); 
        
        return seedlist;
    }
    
    public List<StraightTrack> reFit(List<StraightTrack> seedlist, CosmicFitter fitTrk,  TrackCandListFinder trkfindr) {
        fitTrk = new CosmicFitter();
        trkfindr = new TrackCandListFinder();
        List<StraightTrack> filtlist = new ArrayList<StraightTrack>();
        if(seedlist==null)
            return filtlist;
        for (StraightTrack bseed : seedlist) {
            if(bseed == null)
                continue;
            List<StraightTrack> fseeds = this.reFitSeed(bseed, fitTrk, trkfindr);
            if(fseeds!=null) {
                filtlist.addAll(fseeds);
            }
        }
        return filtlist;
    }
    
    
    public List<StraightTrack> reFitSeed(StraightTrack cand, CosmicFitter fitTrk,TrackCandListFinder trkfindr) {
        boolean pass = true;

        List<StraightTrack> seedlist = new ArrayList<StraightTrack>();
        List<Cross> refib = new ArrayList<Cross>();
        List<Cross> refi = new ArrayList<Cross>();
        for(Cross c : cand) {
            int layr = 0;
            int layr2 = 0;
            if(c.get_Detector()==DetectorType.BMT) {
                layr = c.getOrderedRegion()+3;
                if((int)Constants.getUsedLayers().get(layr)>0) {
                    c.isInSeed = false;
                //    System.out.println("refit "+c.printInfo());
                    refib.add(c);
                }
            } else {
                layr = c.get_Cluster1().get_Layer();
                layr2 = c.get_Cluster2().get_Layer();
                if((int)Constants.getUsedLayers().get(layr)>0 
                        && (int)Constants.getUsedLayers().get(layr2)>0) {
                    c.updateSVTCross(null);
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

    public double[][] getCovMatInTrackRep(Track trk) {
        double[][] tCov = new double[6][6];
        double [][] hCov = trk.get_helix().get_covmatrix();
        
    //error matrix (assuming that the circle fit and line fit parameters are uncorrelated)
    // | d_dca*d_dca                   d_dca*d_phi_at_dca            d_dca*d_curvature        0            0             |
    // | d_phi_at_dca*d_dca     d_phi_at_dca*d_phi_at_dca     d_phi_at_dca*d_curvature        0            0             |
    // | d_curvature*d_dca	    d_curvature*d_phi_at_dca      d_curvature*d_curvature     0            0             |
    // | 0                              0                             0                    d_Z0*d_Z0                     |
    // | 0                              0                             0                       0        d_tandip*d_tandip |
    // 
    
    
        double pt = trk.get_Pt();
        double rho = trk.get_helix().get_curvature();
        double c = Constants.LIGHTVEL;
        double Bz = pt/(c*trk.get_helix().radius());
        double d0 = trk.get_helix().get_dca();
        double phi0 = trk.get_helix().get_phi_at_dca();
        double tandip = trk.get_helix().get_tandip();

        double delxdeld0 = -Math.sin(phi0);
        double delxdelphi0 = -d0*Math.cos(phi0);
        double delydeld0 = Math.cos(phi0);
        double delydelphi0 = -d0*Math.sin(phi0);
        
        double delzdelz0 = 1;
        
        double delpxdelphi0 = -pt*Math.sin(phi0);   
        double delpxdelrho = -pt*Math.cos(phi0)/rho;    
        double delpydelphi0 = pt*Math.cos(phi0);
        double delpydelrho = -pt*Math.sin(phi0)/rho;    
        
        double delpzdelrho = -pt*tandip/rho;    
        
        double delpzdeltandip = pt;
        
        tCov[0][0] = (hCov[0][0]*delxdeld0+hCov[1][0]*delxdelphi0)*delxdeld0
                    +(hCov[0][1]*delxdeld0+hCov[1][1]*delxdelphi0)*delxdelphi0;
        tCov[0][1] = (hCov[0][0]*delxdeld0+hCov[1][0]*delxdelphi0)*delydeld0
                    +(hCov[0][1]*delxdeld0+hCov[1][1]*delxdelphi0)*delydelphi0;
        tCov[0][2] = (hCov[0][3]*delxdeld0+hCov[1][3]*delxdelphi0);
        tCov[0][3] = (hCov[0][1]*delxdeld0+hCov[1][1]*delxdelphi0)*delpxdelphi0
                    +(hCov[0][2]*delxdeld0+hCov[1][2]*delxdelphi0)*delpxdelrho;
        tCov[0][4] = (hCov[0][1]*delxdeld0+hCov[1][1]*delxdelphi0)*delpydelphi0
                    +(hCov[0][2]*delxdeld0+hCov[1][2]*delxdelphi0)*delpydelrho;
        tCov[0][5] = (hCov[0][2]*delxdeld0+hCov[1][2]*delxdelphi0)*delpzdelrho
                    +(hCov[0][4]*delxdeld0+hCov[1][4]*delxdelphi0)*delpzdeltandip;
        
        
        tCov[1][0] = (hCov[0][0]*delydeld0+hCov[1][0]*delydelphi0)*delxdeld0
                    +(hCov[0][1]*delydeld0+hCov[1][1]*delydelphi0)*delxdelphi0;
        tCov[1][1] = (hCov[0][0]*delydeld0+hCov[1][0]*delydelphi0)*delydeld0
                    +(hCov[0][1]*delydeld0+hCov[1][1]*delydelphi0)*delydelphi0;
        tCov[1][2] = (hCov[0][3]*delydeld0+hCov[1][3]*delydelphi0);
        tCov[1][3] = (hCov[0][1]*delydeld0+hCov[1][1]*delydelphi0)*delpxdelphi0
                    +(hCov[0][2]*delydeld0+hCov[1][2]*delydelphi0)*delpxdelrho;
        tCov[1][4] = (hCov[0][1]*delydeld0+hCov[1][1]*delydelphi0)*delpydelphi0
                    +(hCov[0][2]*delydeld0+hCov[1][2]*delydelphi0)*delpydelrho;
        tCov[1][5] = (hCov[0][2]*delydeld0+hCov[1][2]*delydelphi0)*delpzdelrho
                    +(hCov[0][4]*delydeld0+hCov[1][4]*delydelphi0)*delpzdeltandip;
        
        tCov[2][0] = hCov[2][0]*delxdeld0+hCov[2][1]*delxdelphi0;
        tCov[2][1] = hCov[2][0]*delydeld0+hCov[2][1]*delydelphi0;
        tCov[2][2] = hCov[2][3];
        tCov[2][3] = hCov[2][1]*delpxdelphi0+hCov[2][2]*delpxdelrho;
        tCov[2][4] = hCov[2][1]*delpydelphi0+hCov[2][2]*delpydelrho;
        tCov[2][5] = hCov[2][2]*delpzdelrho+hCov[2][4]*delpzdeltandip;
        
        tCov[3][0] = (hCov[1][0]*delpxdelphi0+hCov[2][0]*delpxdelrho)*delxdeld0
                    +(hCov[1][1]*delpxdelphi0+hCov[2][1]*delpxdelrho)*delxdelphi0;
        tCov[3][1] = (hCov[1][0]*delpxdelphi0+hCov[2][0]*delpxdelrho)*delydeld0
                    +(hCov[1][1]*delpxdelphi0+hCov[2][1]*delpxdelrho)*delydelphi0;
        tCov[3][2] = (hCov[1][3]*delpxdelphi0+hCov[2][3]*delpxdelrho);
        tCov[3][3] = (hCov[1][1]*delpxdelphi0+hCov[2][1]*delpxdelrho)*delpxdelphi0
                    +(hCov[1][2]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpxdelrho;
        tCov[3][4] = (hCov[1][1]*delpxdelphi0+hCov[2][1]*delpxdelrho)*delpydelphi0
                    +(hCov[1][2]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpydelrho;
        tCov[3][5] = (hCov[1][2]*delpxdelphi0+hCov[2][2]*delpxdelrho)*delpzdelrho
                    +(hCov[1][4]*delpxdelphi0+hCov[2][4]*delpxdelrho)*delpzdeltandip;
        
        tCov[4][0] = (hCov[1][0]*delpydelphi0+hCov[2][0]*delpydelrho)*delxdeld0
                    +(hCov[1][1]*delpydelphi0+hCov[2][1]*delpydelrho)*delxdelphi0;
        tCov[4][1] = (hCov[1][0]*delpydelphi0+hCov[2][0]*delpydelrho)*delydeld0
                    +(hCov[1][1]*delpydelphi0+hCov[2][1]*delpydelrho)*delydelphi0;
        tCov[4][2] = (hCov[1][3]*delpydelphi0+hCov[2][3]*delpydelrho);
        tCov[4][3] = (hCov[1][1]*delpydelphi0+hCov[2][1]*delpydelrho)*delpxdelphi0
                    +(hCov[1][2]*delpydelphi0+hCov[2][2]*delpydelrho)*delpxdelrho;
        tCov[4][4] = (hCov[1][1]*delpydelphi0+hCov[2][1]*delpydelrho)*delpydelphi0
                    +(hCov[1][2]*delpydelphi0+hCov[2][2]*delpydelrho)*delpydelrho;
        tCov[4][5] = (hCov[1][2]*delpydelphi0+hCov[2][2]*delpydelrho)*delpzdelrho
                    +(hCov[1][4]*delpydelphi0+hCov[2][4]*delpydelrho)*delpzdeltandip;
      
        tCov[5][0] = (hCov[2][0]*delpzdelrho+hCov[4][0]*delpzdeltandip)*delxdeld0
                    +(hCov[2][1]*delpzdelrho+hCov[4][1]*delpzdeltandip)*delxdelphi0;
        tCov[5][1] = (hCov[2][0]*delpzdelrho+hCov[4][0]*delpzdeltandip)*delydeld0
                    +(hCov[2][1]*delpzdelrho+hCov[4][1]*delpzdeltandip)*delydelphi0;
        tCov[5][2] = (hCov[2][3]*delpzdelrho+hCov[4][3]*delpzdeltandip);
        tCov[5][3] = (hCov[2][1]*delpzdelrho+hCov[4][1]*delpzdeltandip)*delpxdelphi0
                    +(hCov[2][2]*delpzdelrho+hCov[4][2]*delpzdeltandip)*delpxdelrho;
        tCov[5][4] = (hCov[2][1]*delpzdelrho+hCov[4][1]*delpzdeltandip)*delpydelphi0
                    +(hCov[2][2]*delpzdelrho+hCov[4][2]*delpzdeltandip)*delpydelrho;
        tCov[5][5] = (hCov[2][2]*delpzdelrho+hCov[4][2]*delpzdeltandip)*delpzdelrho
                    +(hCov[2][4]*delpzdelrho+hCov[4][4]*delpzdeltandip)*delpzdeltandip;
        
        //for (int k = 0; k < 6; k++) {
        //    System.out.println(tCov[k][0]+"	"+tCov[k][1]+"	"+tCov[k][2]+"	"+tCov[k][3]+"	"+tCov[k][4]+"	"+tCov[k][5]);
        //}
        //System.out.println("    ");
        
        return tCov;
    }
    
    
    
}
