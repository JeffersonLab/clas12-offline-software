package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.banks.RecoBankReader;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.Seed.Key;
import org.jlab.rec.cvt.track.StraightTrackSeeder;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.TrackSeederCA;
import org.jlab.rec.cvt.track.TrackSeederSVTLinker;
//import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author ziegler
 */
public class TracksFromTargetRec {

    private final RecUtilities recUtil = new RecUtilities();
    
    private List<Hit> SVThits;
    private List<Hit> BMThits;
    private Map<Integer, Cluster> SVTclustersHM;
    private Map<Integer, Cluster> BMTclustersHM;
    private Map<Integer, Cross> SVTcrossesHM;
    private Map<Integer, Cross> BMTcrossesHM;
    private Map<Integer, Seed> CVTseedsHM ;
    private Map<Integer, Seed> CVTseedsHMOK ;
    private List<Cluster> SVTclusters;
    private List<Cluster> BMTclusters;
    private List<Cross> SVTcrosses;
    private List<Cross> BMTcrosses;
    private List<Seed>    CVTseeds ;
    private Swim swimmer;
    private double xb; 
    private double yb;
    public int totTruthHits;
    
    
    public TracksFromTargetRec(Swim swimmer, double[] beamPos) {
        this.swimmer = swimmer;
        this.xb = beamPos[0];
        this.yb = beamPos[1];
    }
    
       
    public List<Seed> getSeeds(List<ArrayList<Cluster>> clusters, List<ArrayList<Cross>> crosses) {
        this.init();
        this.SVTclusters = clusters.get(0);
        this.BMTclusters = clusters.get(1);
        this.SVTcrosses = crosses.get(0);   
        this.BMTcrosses = crosses.get(1);  
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        List<Seed> seeds = new ArrayList<>();
        // make list of crosses consistent with a track candidate
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder(xb, yb);
            seeds = trseed.findSeed(this.SVTcrosses, this.BMTcrosses, Constants.getInstance().svtOnly);
            // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//            if(Constants.getInstance().EXCLUDELAYERS==true) {
//                seeds = recUtil.reFit(seeds, swimmer, trseed); // RDV can we juts refit?
//            }
        } else {
            if(Constants.getInstance().svtOnly) {
                TrackSeeder trseed = new TrackSeeder(swimmer, xb, yb);
                trseed.unUsedHitsOnly = true;
                seeds = trseed.findSeed(this.SVTcrosses, null);
            } else {
                if(Constants.getInstance().svtLinkerSeeding) {
                    TrackSeederSVTLinker trseed = new TrackSeederSVTLinker(swimmer, xb, yb);  // new seeder
                    seeds.addAll(trseed.findSeed(this.SVTcrosses, this.BMTcrosses));
                } else {
                    TrackSeederCA trseedca = new TrackSeederCA(swimmer, xb, yb);  // cellular automaton seeder
                    seeds.addAll(trseedca.findSeed(this.SVTcrosses, this.BMTcrosses));
                }
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                if(Constants.getInstance().svtSeeding) {
                    TrackSeeder trseed2 = new TrackSeeder(swimmer, xb, yb);
                    trseed2.unUsedHitsOnly = true;
                    seeds.addAll( trseed2.findSeed(this.SVTcrosses, this.BMTcrosses));
                    // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//                    if(Constants.getInstance().EXCLUDELAYERS==true) {
//                        seeds = recUtil.reFit(seeds, swimmer, trseed, trseed2);
//                    }
                }
                List<Seed> failed = new ArrayList<>();
                for(Seed s : seeds) { 
                    if(Constants.getInstance().seedingDebugMode) {
                        System.out.println("Before chi2 cut");
                        System.out.println(s.toString());
                    }  
                    if(s.getChi2()>Constants.CHI2CUT*s.getCrosses().size())
                        failed.add(s);
                    if(s.getHelix()==null)
                        failed.add(s);
                }
                seeds.removeAll(failed);
                if(!Constants.getInstance().seedBeamSpotConstraint()) {
                    failed = new ArrayList<>();
                    for(Seed s : seeds) {
                        if(!recUtil.reFitCircle(s, Constants.getInstance().SEEDFITITERATIONS, xb, yb))
                            failed.add(s);
                    }
                    seeds.removeAll(failed);
                }
            }
        }
        for(Seed s : seeds) { 
            if(Constants.getInstance().seedingDebugMode) {
                System.out.println("Before overlap remover");
                System.out.println(s.toString());
            }       
            s.setKey(s.new Key(s));
        }
        if(Constants.getInstance().removeOverlappingSeeds) 
            Seed.removeOverlappingSeeds(seeds);
        
        if(Constants.getInstance().seedingDebugMode) {
            for(Seed s : seeds) { 
            
                System.out.println("After overlap remover");
                System.out.println(s.toString());
            }
        }
        
        if(Constants.getInstance().flagSeeds)
            Seed.flagMCSeeds(seeds, this.totTruthHits);
        if(seeds ==null || seeds.isEmpty()) {
            recUtil.CleanupSpuriousSVTCrosses(this.SVTcrosses, null) ;
//            RecoBankWriter.appendCVTBanks(event, SVThits, BMThits, SVTclustersHM, BMTclustersHM, crosses, null, null, 1);
            return null;
        }   
        //
        // create track candidate list, set seed IDs and do a final update to crosses
        for(int i=0; i<seeds.size(); i++) { 
            int id = i+1;
            seeds.get(i).setId(id);
        //    Track track = new Track(seeds.get(i));
        //    track.update_Crosses(id);
        }
        if(Constants.getInstance().flagSeeds && Constants.getInstance().removeOverlappingSeeds==false) {
            List<Seed> ovlrm = Seed.getOverlapRemovedSeeds(seeds);//seeds flagged for removal have negative ids.
            for(Seed s : seeds) {
                s.setId(s.getId()*-1);
            }
            for(Seed s : ovlrm) {
                s.setId(s.getId()*-1);
            }
        }
        
        this.CVTseeds = seeds;
        // Got seeds;
        return seeds;
    }
    
    public List<Track> getTracks(DataEvent event, boolean initFromMc, boolean kfFilterOn, int kfIterations, 
                                 boolean searchMissingCls, int elossPid) {
        if(this.CVTseeds==null) return null;
        if(this.CVTseeds.isEmpty()) return null;
        
        double solenoidScale = Constants.getInstance().getSolenoidScale();
        double solenoidValue = Constants.getInstance().getSolenoidMagnitude(); // already the absolute value
        
        List<Track> tracks = new ArrayList<>();
        KFitter kf = new KFitter(kfFilterOn, kfIterations, Constants.KFDIR, swimmer, Constants.getInstance().KFMatrixLibrary);
        kf.polarity = (int) Math.signum(Constants.getSolenoidScale());
        KFitter kf2 = new KFitter(kfFilterOn, kfIterations, Constants.KFDIR, swimmer, Constants.getInstance().KFMatrixLibrary);
        kf2.polarity = (int) Math.signum(Constants.getSolenoidScale());
        kf2.filterOn = false;
        kf2.numIter=1;
        Measurements measure = new Measurements(xb, yb, Constants.getInstance().kfBeamSpotConstraint());
        for (Seed seed : this.CVTseeds) { 
            if(seed.getId()<0) continue;
            int pid = elossPid;
            //seed.update_Crosses();
            //System.out.println("Seed"+seed.toString());
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

            //if(solenoidValue>0.001 && Constants.LIGHTVEL * seed.getHelix().radius() *solenoidValue<Constants.getInstance().getPTCUT())
            if(solenoidValue>0.001 && seed.getHelix().radius() <Constants.getInstance().getRCUT())    
                continue;
            
            //System.out.println("initializing fitter...");
            kf.init(hlx, cov, xb, yb, 0, surfaces, PDGDatabase.getParticleMass(pid));
            kf.runFitter();
            
            
            if (kf.setFitFailed == false && kf.NDF>0 && kf.getHelix()!=null) { 
                Track fittedTrack = new Track(seed, kf, pid);
                fittedTrack.update_Crosses(seed.getId());
                for(Cross c : fittedTrack) { 
                    if(c.getDetector()==DetectorType.BST) {
                        c.getCluster1().setAssociatedTrackID(0);
                        c.getCluster2().setAssociatedTrackID(0);
                    }
                }
                if (searchMissingCls) { 
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.findClustersOnTrk(this.SVTclusters, seed.getClusters(), fittedTrack, swimmer); //VZ: finds missing clusters; RDV fix 0 error

                    List<Cross> crsOnTrack = recUtil.findCrossesFromClustersOnTrk(this.SVTcrosses, clsOnTrack, fittedTrack);

                    List<Cluster> bmtclsOnTrack = recUtil.findBMTClustersOnTrk(this.BMTclusters, seed.getCrosses(), fittedTrack.getHelix(),
                            fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                    List<Cross> bmtcrsOnTrack = recUtil.findCrossesOnBMTTrack(this.BMTcrosses, bmtclsOnTrack);

                    //VZ check for additional clusters, and only then re-run KF adding new clusters
                    if((clsOnTrack.size()>0 || bmtcrsOnTrack.size()>0) ) { 
                        if(clsOnTrack.size()>0) {
                            seed.getClusters().addAll(clsOnTrack);
                        }
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
                        charge = (int) fittedTrack.getHelix().getCharge();
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
                //System.out.println("Track "+fittedTrack.toString());
                if(this.missingSVTCrosses(fittedTrack) == false)
                    tracks.add(fittedTrack);
            } else {
                if(Constants.getInstance().KFfailRecovery) {
                    kf2.init(hlx, cov, xb, yb, 0, surfaces, PDGDatabase.getParticleMass(pid));
                    kf2.runFitter();
                    if(kf2.getHelix()!=null) {
                        Track fittedTrack = new Track(seed, kf2, pid);
                        for(Cross c : fittedTrack) { 
                            if(c.getDetector()==DetectorType.BST) {
                                c.getCluster1().setAssociatedTrackID(0);
                                c.getCluster2().setAssociatedTrackID(0);
                            }
                        }
                        if(this.missingSVTCrosses(fittedTrack) == false)
                            tracks.add(fittedTrack);
                    }
                }
            }
            
        }
    

        // reset cross and cluster IDs
        if(SVTcrosses!=null) {
            for(Cross c : this.SVTcrosses) {
                c.setAssociatedTrackID(-1);
            }
        }
        if(BMTcrosses!=null) {
            for(Cross c : this.BMTcrosses) {
                c.setAssociatedTrackID(-1);
            }
        }
        if(SVTclusters!=null) {
            for(Cluster c : SVTclusters) {
                c.setAssociatedTrackID(-1);
            }
        }
        if(BMTclustersHM!=null) {
            for(Cluster c : BMTclusters) {
                c.setAssociatedTrackID(-1);
            }
        }
        if(!tracks.isEmpty()) {
            // do a final cleanup
            //Track.removeOverlappingTracks(tracks); 
            if(tracks.isEmpty()) System.out.println("Error: no tracks left after overlap remover");
            
            // update crosses and clusters on track
            for(int it = 0; it < tracks.size(); it++) {
                int id = it + 1;
                tracks.get(it).setId(id); 
                tracks.get(it).findTrajectory(swimmer, Geometry.getInstance().geOuterSurfaces());
                tracks.get(it).update_Crosses(id);
                tracks.get(it).update_Clusters(id);
                tracks.get(it).setTrackCovMat(recUtil.getCovMatInTrackRep(tracks.get(it)));
            //    System.out.println("Fit " + tracks.get(it).toString());
            }
        }
        if(SVTcrosses!=null) {
            for(Cross c : this.SVTcrosses) { 
                if(c.getAssociatedTrackID()==-1) {
                        c.reset();
                }
            } 
        }
        if(BMTcrosses!=null) {
            for(Cross c : this.BMTcrosses) {
                if(c.getAssociatedTrackID()==-1) {
                        c.reset();
                }
                if(c.getId()>2000) { //if matched cross failed tracking resol requirements, reset its id
                    c.setId(c.getId()-1000);
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

    private boolean missingSVTCrosses(Track s) {
                
        boolean nosvt = true;
        for(Cross c : s) {
            if(c.getDetector() == DetectorType.BST)
                nosvt = false;
        }
        
        return nosvt;
    }
     
    public List<Seed> getSeedsFromBanks(DataEvent event) {
        
        this.init();
        
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

        
        SVTclustersHM = RecoBankReader.readBSTClusterBank(event, SVThits);
        BMTclustersHM = RecoBankReader.readBMTClusterBank(event, BMThits);
        
        
        SVTcrossesHM = RecoBankReader.readBSTCrossBank(event, SVTclustersHM);
        BMTcrossesHM = RecoBankReader.readBMTCrossBank(event, BMTclustersHM);
        
                       
        CVTseedsHM = RecoBankReader.readCVTSeedsBank(event, xb, yb, SVTcrossesHM, BMTcrossesHM);
        if(CVTseedsHM == null) {
            return null;
        } else {
            CVTseedsHMOK = RecoBankReader.readCVTTracksBank(event, xb, yb, CVTseedsHM, SVTcrossesHM, BMTcrossesHM);
            if(CVTseedsHMOK == null) {
                return null;
            } else {
                Collection<Seed> values = CVTseedsHMOK.values();
                CVTseeds = new ArrayList<>(values);
            }
        }
        if(SVTclustersHM!=null)
            SVTclusters = new ArrayList<>(SVTclustersHM.values());
        if(BMTclustersHM!=null) 
            BMTclusters = new ArrayList<>(BMTclustersHM.values());
        if(SVTcrossesHM!=null)
            SVTcrosses = new ArrayList<>(SVTcrossesHM.values());
        if(BMTcrossesHM!=null)
            BMTcrosses = new ArrayList<>(BMTcrossesHM.values());
        
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
        if(SVTcrosses==null || SVTcrosses.isEmpty())
            return null;
        else
            return SVTcrosses;
    }
    
    public List<Cross> getBMTcrosses() {
        if(BMTcrosses==null || BMTcrosses.isEmpty())
            return null;
        else
            return BMTcrosses;
    }

    private void init() {
        //clear the lists
        if(SVThits!=null)
            SVThits.clear();
        if(BMThits!=null)
            BMThits.clear();
        if(SVTclustersHM!=null)
            SVTclustersHM.clear();
        if(BMTclustersHM!=null)
            BMTclustersHM.clear();
        if(SVTcrossesHM!=null)
            SVTcrossesHM.clear();
        if(BMTcrossesHM!=null)
            BMTcrossesHM.clear();
        if(CVTseedsHM!=null)
            CVTseedsHM.clear();
        if(SVTclusters!=null)
            SVTclusters.clear();
        if(BMTclusters!=null)
            BMTclusters.clear();
        if(SVTcrosses!=null)
            SVTcrosses.clear();
        if(BMTcrosses!=null)
            BMTcrosses.clear();
        if(CVTseeds!=null)
            CVTseeds.clear();
    }

}
