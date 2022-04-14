package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossMaker;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.Hit;
import org.jlab.rec.cvt.measurement.Measurements;
import org.jlab.rec.cvt.track.Seed;
import org.jlab.rec.cvt.track.StraightTrackSeeder;
import org.jlab.rec.cvt.track.Track;
import org.jlab.rec.cvt.track.TrackListFinder;
import org.jlab.rec.cvt.track.TrackSeeder;
import org.jlab.rec.cvt.track.TrackSeederCA;

/**
 *
 * @author ziegler
 */
public class TracksFromTargetRec {
    private final StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
    private final RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<Hit> SVThits, List<Hit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            double xb, double yb,
            RecoBankWriter rbc,
            Swim swimmer) {
        
        // get field intensity and scale
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        
        // make list of crosses consistent with a track candidate
        List<Seed> seeds = null;
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder(xb, yb);
            seeds = trseed.findSeed(crosses.get(0), crosses.get(1), Constants.SVTONLY);
            // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//            if(Constants.EXCLUDELAYERS==true) {
//                seeds = recUtil.reFit(seeds, swimmer, trseed); // RDV can we juts refit?
//            }
        } else {
            if(Constants.SVTONLY) {
                TrackSeeder trseed = new TrackSeeder(swimmer, xb, yb);
                trseed.unUsedHitsOnly = true;
                seeds = trseed.findSeed(crosses.get(0), null);
            } else {
                TrackSeederCA trseed = new TrackSeederCA(swimmer, xb, yb);  // cellular automaton seeder
                seeds = trseed.findSeed(crosses.get(0), crosses.get(1));
                
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                if(Constants.SVTSEEDING) {
                    TrackSeeder trseed2 = new TrackSeeder(swimmer, xb, yb);
                    trseed2.unUsedHitsOnly = true;
                    seeds.addAll( trseed2.findSeed(crosses.get(0), crosses.get(1)));
                    // RDV, disabled because it seems to create fake tracks, skipping measurement in KF
//                    if(Constants.EXCLUDELAYERS==true) {
//                        seeds = recUtil.reFit(seeds, swimmer, trseed, trseed2);
//                    }
                }
                if(!Constants.seedBeamSpotConstraint()) {
                    List<Seed> failed = new ArrayList<>();
                    for(Seed s : seeds) {
                        if(!recUtil.reFitCircle(s, Constants.SEEDFITITERATIONS, xb, yb))
                            failed.add(s);
                    }
                    seeds.removeAll(failed);
                }
            }
        }
        if(seeds ==null || seeds.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, null);
            return true;
        }   
        
        // create track candidate list, set seed IDs and do a final update to crosses
        List<Track> trkcands = new ArrayList<>();        
        for(Seed seed : seeds) {
            int id = trkcands.size()+1;
            seed.setId(id);
            Track track = new Track(seed);
            track.update_Crosses(id);
            trkcands.add(track);   
        }
        
        trkcands.clear();
        KFitter kf = new KFitter(Constants.KFFILTERON, Constants.KFITERATIONS, Constants.kfBeamSpotConstraint(), swimmer, Constants.KFMATLIB);
        Measurements surfaces = new Measurements(false, xb, yb);
        for (Seed seed : seeds) { 
            Point3D  v = seed.getHelix().getVertex();
            Vector3D p = seed.getHelix().getPXYZ(solenoidValue);
            int charge = (int) (Math.signum(solenoidScale)*seed.getHelix().getCharge());
            if(solenoidValue<0.001)
                charge = 1;
            double[] pars = recUtil.mcTrackPars(event);
            if(Constants.INITFROMMC) {
                v = new Point3D(pars[0],pars[1],pars[2]);
                p = new Vector3D(pars[3],pars[4],pars[5]);
                if(solenoidValue<0.001) p.scale(100/p.mag());
            }
            Helix hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                            solenoidValue, xb , yb, Helix.Units.MM);
            double[][] cov = seed.getHelix().getCovMatrix();

            if(solenoidValue>0.001 && Constants.LIGHTVEL * seed.getHelix().radius() *solenoidValue<Constants.PTCUT)
                continue;
            kf.init(hlx, cov, xb, yb, 0, surfaces.getMeasurements(seed)) ;
            kf.runFitter();
            if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null) { 
                Track fittedTrack = new Track(seed, kf);
                for(Cross c : fittedTrack) { 
                    if(c.getDetector()==DetectorType.BST) {
                        c.getCluster1().setAssociatedTrackID(0);
                        c.getCluster2().setAssociatedTrackID(0);
                    }
                }
                //refit adding missing clusters
                List<Cluster> clsOnTrack = recUtil.findClustersOnTrk(SVTclusters, seed.getClusters(), fittedTrack.getHelix(), 
                        fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                List<Cluster> bmtclsOnTrack = recUtil.findBMTClustersOnTrk(BMTclusters, seed.getCrosses(), fittedTrack.getHelix(), 
                        fittedTrack.getP(), fittedTrack.getQ(), swimmer); //VZ: finds missing clusters
                CrossMaker cm = new CrossMaker();
                List<Cross> bmtcrsOnTrack = recUtil.findCrossesOnBMTTrack(bmtclsOnTrack, cm, crosses.get(1).size()+2000);

                if(clsOnTrack.size()>0 || bmtcrsOnTrack.size()>0) { 
                    if(clsOnTrack.size()>0) 
                        seed.getClusters().addAll(clsOnTrack); //VZ check for additional clusters, and only then re-run KF adding new clusters                    
                    if(bmtcrsOnTrack.size()>0) {
                        seed.getCrosses().addAll(bmtcrsOnTrack); //VZ check for additional crosses, and only then re-run KF adding new clusters                    
                        crosses.get(1).addAll(bmtcrsOnTrack);
                        for(Cross c : bmtcrsOnTrack) {
                            seed.getClusters().add(c.getCluster1());
                        }
                    }
                    //reset pars
                    v = fittedTrack.getHelix().getVertex();
                    p = fittedTrack.getHelix().getPXYZ(solenoidValue);
                    charge = (int) (Math.signum(solenoidScale)*fittedTrack.getHelix().getCharge());
                    if(solenoidValue<0.001)
                        charge = 1;
                    hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                                    solenoidValue, xb, yb, Helix.Units.MM);

                    kf.init(hlx, cov, xb, yb, 0, surfaces.getMeasurements(seed)) ;
                    kf.runFitter();

                    // RDV get rid of added clusters if not true
                    if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null)
                        fittedTrack = new Track(seed, kf);
                }
                trkcands.add(fittedTrack);
            }
        }
    

        // reset cross and cluster IDs
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
                c.setAssociatedTrackID(-1);
            }
        }
        for(Cluster c : SVTclusters) {
            c.setAssociatedTrackID(-1);
        }
        for(Cluster c : BMTclusters) {
            c.setAssociatedTrackID(-1);
        }
        
        List<Track> tracks = null;
        if(!trkcands.isEmpty()) {
            // do a final cleanup
            TrackListFinder.removeOverlappingTracks(trkcands); 
            if(trkcands.isEmpty()) System.out.println("Error: no tracks left after overlap remover");
            
            tracks = TrackListFinder.getTracks(trkcands, swimmer);
            // update crosses and clusters on track
            for(int it = 0; it < tracks.size(); it++) {
                int id = it + 1;
                tracks.get(it).setId(id); 
                tracks.get(it).update_Crosses(id);
                tracks.get(it).update_Clusters(id);
                tracks.get(it).setTrackCovMat(recUtil.getCovMatInTrackRep(tracks.get(it)));
            }
        }
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
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
        rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, seeds, tracks);

        return true;

        }
    
}