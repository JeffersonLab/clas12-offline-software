package org.jlab.rec.cvt.services;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.helical.KFitter;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.svt.SVTGeometry;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.FittedHit;
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
    private StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
    private RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<FittedHit> SVThits, List<FittedHit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            SVTGeometry SVTGeom, BMTGeometry BMTGeom,
            CTOFGeant4Factory CTOFGeom, Detector CNDGeom,
            RecoBankWriter rbc,
            double shift, 
            Swim swimmer,
            boolean isSVTonly, boolean exLayrs) {
        
        // get field intensity and scale
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        
        // make list of crosses consistent with a track candidate
        List<Seed> seeds = null;
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder();
            seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, isSVTonly);
            if(exLayrs==true) {
                seeds = recUtil.reFit(seeds, SVTGeom, BMTGeom, swimmer, trseed); // RDV can we juts refit?
            }
        } else {
            if(isSVTonly) {
                TrackSeeder trseed = new TrackSeeder(SVTGeom, BMTGeom, swimmer);
                trseed.unUsedHitsOnly = true;
                seeds = trseed.findSeed(crosses.get(0), null);
            } else {
                TrackSeederCA trseed = new TrackSeederCA(SVTGeom, BMTGeom, swimmer);  // cellular automaton seeder
                seeds = trseed.findSeed(crosses.get(0), crosses.get(1));
                
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                TrackSeeder trseed2 = new TrackSeeder(SVTGeom, BMTGeom, swimmer);
                trseed2.unUsedHitsOnly = true;
                seeds.addAll( trseed2.findSeed(crosses.get(0), crosses.get(1))); // RDV check for overlaps
                if(exLayrs==true) {
                    seeds = recUtil.reFit(seeds, SVTGeom, BMTGeom, swimmer, trseed,trseed2);
                }
            }
        }
        if(seeds ==null || seeds.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, null, shift);
            return true;
        }   
        
        // create track candidate list, set seed IDs and do a final update to crosses
        List<Track> trkcands = new ArrayList<>();        
        for(Seed seed : seeds) {
            int id = trkcands.size()+1;
            seed.setId(id);
            Track track = new Track(seed);
            track.update_Crosses(id, SVTGeom, BMTGeom);
            trkcands.add(track);                
        }
//        if(true) {
//            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, seeds, trkcands, shift);
//            return true;
//        }
        
        trkcands.clear();
        KFitter kf = null;
        for (Seed seed : seeds) { 
            Point3D  v = seed.get_Helix().getVertex();
            Vector3D p = seed.get_Helix().getPXYZ(solenoidValue);
            int charge = (int) (Math.signum(solenoidScale)*seed.get_Helix().get_charge());
            if(solenoidValue<0.001)
                charge = 1;
            v.translateXYZ(Constants.getXb(),
                           Constants.getYb(),
                           0);
            //Uncomment to force to MC truth:
            //double[] pars = recUtil.MCtrackPars(event);
            //v = new Point3D(pars[0],pars[1],pars[2]);
            //p = new Vector3D(pars[3],pars[4],pars[5]);
            Helix hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                            solenoidValue, Constants.getXb(), Constants.getYb(), Helix.Units.MM);
            double[][] cov = seed.get_Helix().get_covmatrix();
            
            if(solenoidValue>0.001 &&
                    Constants.LIGHTVEL * seed.get_Helix().radius() *solenoidValue<Constants.PTCUT)
                continue;
                
                kf = new KFitter( hlx, cov, event,  swimmer, 
                    Constants.getXb(), 
                    Constants.getYb(),
                    shift, 
                    recUtil.setMeasVecs(seed, swimmer)) ;
                kf.setMatrixLibrary(Constants.kfMatLib);
                //Uncomment to let track be fitted
                //kf.filterOn=false;
                kf.runFitter(swimmer);
                if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null) { 
                    Track fittedTrack = new Track(seed, kf);
                    for(Cross c : fittedTrack) { 
                        if(c.get_Detector()==DetectorType.BST) {
                            c.get_Cluster1().set_AssociatedTrackID(0);
                            c.get_Cluster2().set_AssociatedTrackID(0);
                        }
                    }
                    //refit adding missing clusters
                    List<Cluster> clsOnTrack = recUtil.FindClustersOnTrk(SVTclusters, seed.get_Clusters(), fittedTrack.get_helix(), 
                            fittedTrack.get_P(), fittedTrack.get_Q(), SVTGeom, swimmer);
                    if(clsOnTrack.size()>0) {
                        seed.get_Clusters().addAll(clsOnTrack);
                    
                        //reset pars
                        v = fittedTrack.get_helix().getVertex();
                        p = fittedTrack.get_helix().getPXYZ(solenoidValue);
                        charge = (int) (Math.signum(solenoidScale)*fittedTrack.get_helix().get_charge());
                        if(solenoidValue<0.001)
                            charge = 1;
                        v.translateXYZ(Constants.getXb(),Constants.getYb(), 0);
                        hlx = new Helix(v.x(),v.y(),v.z(),p.x(),p.y(),p.z(), charge,
                                        solenoidValue, Constants.getXb(), Constants.getYb(), Helix.Units.MM);

                        kf = new KFitter( hlx, cov, event,  swimmer, 
                        Constants.getXb(), 
                        Constants.getYb(),
                        shift, 
                        recUtil.setMeasVecs(seed, swimmer)) ;
                        kf.setMatrixLibrary(Constants.kfMatLib);
                        //Uncomment to let track be fitted
                        //kf.filterOn = false;
                        kf.runFitter(swimmer);
                        
                        // RDV get rid of added clusters if not true
                        if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null)
                            fittedTrack = new Track(seed, kf);
                    }
                    fittedTrack.set_SeedingMethod(seed.get_Status());
                    trkcands.add(fittedTrack);
            }
        }
    

        // reset cross and cluster IDs
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
                c.set_AssociatedTrackID(-1);
            }
        }
        for(Cluster c : SVTclusters) {
            c.set_AssociatedTrackID(-1);
        }
//        if (trkcands.isEmpty()) {
//            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;            
//            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, seeds, null, shift);
//            return true;
//        }
        
        List<Track> tracks = null;
        if(!trkcands.isEmpty()) {
            // do a final cleanup
            TrackListFinder.removeOverlappingTracks(trkcands); 
    //        TrackListFinder.checkForOverlaps(trkcands, "KF");
            tracks = TrackListFinder.getTracks(trkcands, SVTGeom, BMTGeom, CTOFGeom, CNDGeom, swimmer);
            // update crosses and clusters on track
            for(int it = 0; it < tracks.size(); it++) {
                int id = it + 1;
                tracks.get(it).set_Id(id); 
                tracks.get(it).update_Crosses(id, SVTGeom, BMTGeom);
                tracks.get(it).update_Clusters(id, SVTGeom);
            }
        }
        for(int det = 0; det<2; det++) {
            for(Cross c : crosses.get(det)) {
                if(c.get_AssociatedTrackID()==-1) c.reset(SVTGeom);
            }
        }


//        //------------------------ RDV check with Veronique
//        // set index associations
//        if (tracks.size() > 0) {
//            recUtil.CleanupSpuriousCrosses(crosses, tracks, SVTGeom) ;
//        }
        rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, seeds, tracks, shift);

        return true;

        }
    
}