package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.rec.cvt.banks.RecoBankReader;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrackSeeder;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.TrackSeederCA;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler
 */
public class TracksFromTargetRec {

    private final RecUtilities recUtil = new RecUtilities();
    
    private List<Hit> SVThits;
    private List<Hit> BMThits;
    private List<Cluster> SVTclusters;
    private List<Cluster> BMTclusters;
    private List<ArrayList<Cross>> CVTcrosses = new ArrayList<>();
    private List<Seed>    CVTseeds = new ArrayList<>();
    private Swim swimmer;
    private double xb; 
    private double yb;
    
    
    public TracksFromTargetRec(Swim swimmer, IndexedTable beamPos) {
        this.swimmer = swimmer;
        this.xb = beamPos.getDoubleValue("x_offset", 0, 0, 0)*10;
        this.yb = beamPos.getDoubleValue("y_offset", 0, 0, 0)*10;
    }
    
       
    public List<Seed> getSeeds(List<ArrayList<Cluster>> clusters, List<ArrayList<Cross>> crosses) {
        this.SVTclusters = clusters.get(0);
        this.BMTclusters = clusters.get(1);
        this.CVTcrosses = crosses;   
        
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        
        // make list of crosses consistent with a track candidate
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder(xb, yb);
            CVTseeds = trseed.findSeed(this.CVTcrosses.get(0), this.CVTcrosses.get(1), Constants.getInstance().svtOnly);
            // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//            if(Constants.getInstance().EXCLUDELAYERS==true) {
//                seeds = recUtil.reFit(seeds, swimmer, trseed); // RDV can we juts refit?
//            }
        } else {
            if(Constants.getInstance().svtOnly) {
                TrackSeeder trseed = new TrackSeeder(swimmer, xb, yb);
                trseed.unUsedHitsOnly = true;
                CVTseeds = trseed.findSeed(this.CVTcrosses.get(0), null);
            } else {
                TrackSeederCA trseed = new TrackSeederCA(swimmer, xb, yb);  // cellular automaton seeder
                CVTseeds = trseed.findSeed(this.CVTcrosses.get(0), this.CVTcrosses.get(1));
                
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                if(Constants.getInstance().svtSeeding) {
                    TrackSeeder trseed2 = new TrackSeeder(swimmer, xb, yb);
                    trseed2.unUsedHitsOnly = true;
                    CVTseeds.addAll( trseed2.findSeed(this.CVTcrosses.get(0), this.CVTcrosses.get(1)));
                    // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//                    if(Constants.getInstance().EXCLUDELAYERS==true) {
//                        seeds = recUtil.reFit(seeds, swimmer, trseed, trseed2);
//                    }
                }
                if(!Constants.getInstance().seedBeamSpotConstraint()) {
                    List<Seed> failed = new ArrayList<>();
                    for(Seed s : CVTseeds) {
                        if(!recUtil.reFitCircle(s, Constants.getInstance().SEEDFITITERATIONS, xb, yb))
                            failed.add(s);
                    }
                    CVTseeds.removeAll(failed);
                }
            }
        }
        if(CVTseeds ==null || CVTseeds.isEmpty()) {
            recUtil.CleanupSpuriousCrosses(this.CVTcrosses, null) ;
//            RecoBankWriter.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, null, 1);
            return null;
        }   
        
        // create track candidate list, set seed IDs and do a final update to crosses
        for(int i=0; i<CVTseeds.size(); i++) {
            int id = i+1;
            CVTseeds.get(i).setId(id);
            Track track = new Track(CVTseeds.get(i));
            track.update_Crosses(id);
        }
        // Got seeds;
        return CVTseeds;
    }
    
    public List<Track> getTracks(DataEvent event, boolean initFromMc, boolean kfFilterOn, int kfIterations, 
                                 boolean searchMissingCls, int pid) {
        if(this.CVTseeds.isEmpty()) return null;
        
        double solenoidScale = Constants.getInstance().getSolenoidScale();
        double solenoidValue = Constants.getInstance().getSolenoidMagnitude(); // already the absolute value
        
        List<Track> tracks = new ArrayList<>();
        KFitter kf = new KFitter(kfFilterOn, kfIterations, Constants.KFDIR, swimmer, Constants.getInstance().KFMatrixLibrary);
        Measurements measure = new Measurements(xb, yb, Constants.getInstance().kfBeamSpotConstraint());
        for (Seed seed : CVTseeds) { 
//            seed.update_Crosses();
//            System.out.println(seed.toString());
            List<Surface> surfaces = measure.getMeasurements(seed);

            if(pid==0) pid = this.getTrackPid(event, seed.getId());
            Point3D  v = seed.getHelix().getVertex();
            Vector3D p = seed.getHelix().getPXYZ(solenoidValue);
            
            if(Constants.getInstance().preElossCorrection && pid!=Constants.DEFAULTPID) {
                double pcorr = measure.getELoss(p.mag(), PDGDatabase.getParticleMass(pid));
                p.scale(pcorr/p.mag());
            }
            
            int charge = (int) (Math.signum(solenoidScale)*seed.getHelix().getCharge());
            if(solenoidValue<0.001)
                charge = 1;

            double[] pars = recUtil.mcTrackPars(event);
            if(initFromMc) {
                v = new Point3D(pars[0],pars[1],pars[2]);
                p = new Vector3D(pars[3],pars[4],pars[5]);
                if(solenoidValue<0.001) p.scale(100/p.mag());
            }
            Helix hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                            solenoidValue, xb , yb, Units.MM);
            double[][] cov = Constants.COVHELIX;

            if(solenoidValue>0.001 && Constants.LIGHTVEL * seed.getHelix().radius() *solenoidValue<Constants.PTCUT)
                continue;
            kf.init(hlx, cov, xb, yb, 0, surfaces, PDGDatabase.getParticleMass(pid));
            kf.runFitter();
            if (kf.setFitFailed == false && kf.NDF>0 && kf.getHelix()!=null) { 
                Track fittedTrack = new Track(seed, kf, pid);
                for(Cross c : fittedTrack) { 
                    if(c.getDetector()==DetectorType.BST) {
                        c.getCluster1().setAssociatedTrackID(0);
                        c.getCluster2().setAssociatedTrackID(0);
                    }
                }
                if (searchMissingCls) {
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.findClustersOnTrk(SVTclusters, seed.getClusters(), fittedTrack.getHelix(),
                            fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters; RDV fix 0 error
                    List<Cross> crsOnTrack = recUtil.findCrossesFromClustersOnTrk(CVTcrosses.get(0), clsOnTrack, fittedTrack);

                    List<Cluster> bmtclsOnTrack = recUtil.findBMTClustersOnTrk(BMTclusters, seed.getCrosses(), fittedTrack.getHelix(),
                            fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                    List<Cross> bmtcrsOnTrack = recUtil.findCrossesOnBMTTrack(CVTcrosses.get(1), bmtclsOnTrack);

                    //VZ check for additional clusters, and only then re-run KF adding new clusters
                    if((clsOnTrack.size()>0 || bmtcrsOnTrack.size()>0) && false) { 
                        if(clsOnTrack.size()>0) 
                            seed.getClusters().addAll(clsOnTrack);
                        if(crsOnTrack.size()>0) {
                            seed.getCrosses().addAll(crsOnTrack);
                        }
                        if(bmtcrsOnTrack.size()>0) {
                            seed.getCrosses().addAll(bmtcrsOnTrack); //VZ check for additional crosses, and only then re-run KF adding new clusters                    
                            for(Cross c : bmtcrsOnTrack) {
                                seed.getClusters().add(c.getCluster1());
                            }
                        }
                        Collections.sort(seed.getClusters());
                        Collections.sort(seed.getCrosses());
                        
                        //reset pars
                        v = fittedTrack.getHelix().getVertex();
                        p = fittedTrack.getHelix().getPXYZ(solenoidValue);
                        charge = (int) (Math.signum(solenoidScale)*fittedTrack.getHelix().getCharge());
                        if(solenoidValue<0.001)
                            charge = 1;
                        hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                                        solenoidValue, xb, yb, Units.MM);

                        kf.init(hlx, cov, xb, yb, 0, surfaces, PDGDatabase.getParticleMass(pid)) ;
                        kf.runFitter();

                        if (kf.setFitFailed == false && kf.NDF>0 && kf.getHelix()!=null) { 
                            fittedTrack = new Track(seed, kf, pid);
                            for(Cross c : fittedTrack) { 
                                if(c.getDetector()==DetectorType.BST) {
                                    c.getCluster1().setAssociatedTrackID(0);
                                    c.getCluster2().setAssociatedTrackID(0);
                                }
                            }
                        }
                    }
                }
                tracks.add(fittedTrack);
            }
        }
    

        // reset cross and cluster IDs
        for(int det = 0; det<2; det++) {
            for(Cross c : CVTcrosses.get(det)) {
                c.setAssociatedTrackID(-1);
            }
        }
        for(Cluster c : SVTclusters) {
            c.setAssociatedTrackID(-1);
        }
        if(BMTclusters!=null) {
            for(Cluster c : BMTclusters) {
                c.setAssociatedTrackID(-1);
            }
        }
        if(!tracks.isEmpty()) {
            // do a final cleanup
            Track.removeOverlappingTracks(tracks); 
            if(tracks.isEmpty()) System.out.println("Error: no tracks left after overlap remover");
            
            // update crosses and clusters on track
            for(int it = 0; it < tracks.size(); it++) {
                int id = it + 1;
                tracks.get(it).setId(id); 
                tracks.get(it).findTrajectory(swimmer, Constants.getInstance().OUTERSURFACES);
                tracks.get(it).update_Crosses(id);
                tracks.get(it).update_Clusters(id);
                tracks.get(it).setTrackCovMat(recUtil.getCovMatInTrackRep(tracks.get(it)));
//                System.out.println("Fit " + tracks.get(it).toString());
            }
        }
        for(int det = 0; det<2; det++) {
            for(Cross c : CVTcrosses.get(det)) {
                if(c.getAssociatedTrackID()==-1) {
                    c.reset();
                    if(det==1 && c.getId()>2000) { //if matched cross failed tracking resol requirements, reset its id
                        c.setId(c.getId()-1000);
                    } 
                }
                
            }
        }


//        //------------------------ RDV check with Veronique
//        // set index associations
//        if (tracks.size() > 0) {
//            recUtil.CleanupSpuriousCrosses(crosses, tracks, SVTGeom) ;
//        }
        return tracks;

    }

        
    public List<Seed> getSeedsFromBanks(DataEvent event) {
        
                
        SVThits = RecoBankReader.readBSTHitBank(event);
        BMThits = RecoBankReader.readBMTHitBank(event);
        if(SVThits!= null) {
            Collections.sort(SVThits);
        }
        if(BMThits!=null) {
            for(Hit hit : BMThits) {
                hit.getStrip().calcBMTStripParams(hit.getSector(), hit.getLayer(), swimmer);
            }
            Collections.sort(BMThits);
        }

        
        SVTclusters = RecoBankReader.readBSTClusterBank(event);
        BMTclusters = RecoBankReader.readBMTClusterBank(event);
        if(SVThits!=null && SVTclusters!=null) {
            for(Hit hit : SVThits) {
                if(hit.getAssociatedClusterID()>0) {
                    SVTclusters.get(hit.getAssociatedClusterID()-1).add(hit);
                    hit.newClustering = true;
                }
            }
            for(Cluster cluster : SVTclusters) {
               Collections.sort(cluster);
               cluster.setSeed(cluster.get(0));
               for(Hit hit: cluster) hit.newClustering=false;
            }
        }
        if(BMThits!=null && BMTclusters!=null) {
            for(Hit hit : BMThits) {
                if(hit.getAssociatedClusterID()>0) {
                    BMTclusters.get(hit.getAssociatedClusterID()-1).add(hit);
                    hit.newClustering = true;
                }
            }
           for(Cluster cluster : BMTclusters) {
               Collections.sort(cluster);
               cluster.setSeed(cluster.get(0));
               for(Hit hit: cluster) hit.newClustering=false;
           } 
        }
        
        List<Cross> SVTcrosses = RecoBankReader.readBSTCrossBank(event);
        List<Cross> BMTcrosses = RecoBankReader.readBMTCrossBank(event);
        if(SVTcrosses!=null) {
            for(Cross cross : SVTcrosses) {
                cross.setCluster1(SVTclusters.get(cross.getCluster1().getId()-1));
                cross.setCluster2(SVTclusters.get(cross.getCluster2().getId()-1)); 
            }
            CVTcrosses.add((ArrayList<Cross>) SVTcrosses);
        }
        else {
            CVTcrosses.add(new ArrayList<>());
        }
        if(BMTcrosses!=null) {
            for(Cross cross : BMTcrosses) {
                cross.setCluster1(BMTclusters.get(cross.getCluster1().getId()-1));
            }
            CVTcrosses.add((ArrayList<Cross>) BMTcrosses);
        }
        else {
            CVTcrosses.add(new ArrayList<>());
        }
                       
        List<Seed> seeds = RecoBankReader.readCVTSeedsBank(event, xb, yb);
        if(seeds == null) 
            return null;
        
        
        List<Track> tracks = RecoBankReader.readCVTTracksBank(event, xb, yb);
        if(tracks == null) 
            return null;
        
        for(Track track : tracks) {
            Seed seed = track.getSeed();
            seed.setHelix(track.getHelix());
            seed.getHelix().setCovMatrix(seeds.get(seed.getId()-1).getHelix().getCovMatrix());
            seed.setChi2(track.getChi2());
            seed.setNDF(track.getNDF());
            seed.setId(track.getId());
            
            List<Cross> crosses = new ArrayList<>();
            for(Cross cross : SVTcrosses) {
                if(cross.getAssociatedTrackID()==seed.getId())
                    crosses.add(cross);
            }
            if(BMTcrosses!=null) {
                for(Cross cross : BMTcrosses) {
                    if(cross.getAssociatedTrackID()==seed.getId())
                        crosses.add(cross);
                }
            }
            seed.setCrosses(crosses);
            for(Cluster cluster : SVTclusters) {
                if(cluster.getAssociatedTrackID()==seed.getId() && !seed.getClusters().contains(cluster)) 
                    seed.getClusters().add(cluster);
            }
            CVTseeds.add(seed);
        }
       
        return CVTseeds;
    }

    private int getTrackPid(DataEvent event, int trkId) {
        int pid = Constants.DEFAULTPID;

        if (event.hasBank("RECHB::Particle")  && event.hasBank("RECHB::Track")) {    
          
            DataBank partBank = event.getBank("RECHB::Particle");
            DataBank trackBank = event.getBank("RECHB::Track");

            int rows = trackBank.rows();
            for (int i = 0; i < rows; i++) {
                if (trackBank.getByte("detector", i) == DetectorType.CVT.getDetectorId() &&
                    trackBank.getShort("index", i) == trkId - 1) {
                    int pindex = trackBank.getShort("pindex", i);
                    if(partBank.getInt("pid", pindex)!=0)
                        pid = partBank.getInt("pid", pindex);
                    break;
                }
            }
        }
        return pid;
    }

    public List<Hit> getSVThits() {
        return SVThits;
    }

    public List<Hit> getBMThits() {
        return BMThits;
    }

    public List<Cluster> getSVTclusters() {
        if(SVTclusters==null || SVTclusters.isEmpty())
            return null;
        else
            return SVTclusters;
    }

    public List<Cluster> getBMTclusters() {
        if(BMTclusters==null || BMTclusters.isEmpty())
            return null;
        else
            return BMTclusters;
    }

    public List<Cross> getSVTcrosses() {
        if(CVTcrosses.get(0)==null || CVTcrosses.get(0).isEmpty())
            return null;
        else
            return CVTcrosses.get(0);
    }
    
    public List<Cross> getBMTcrosses() {
        if(CVTcrosses.get(1)==null || CVTcrosses.get(1).isEmpty())
            return null;
        else
            return CVTcrosses.get(1);
    }
    
}
