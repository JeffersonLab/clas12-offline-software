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
import org.jlab.rec.cvt.bmt.BMTType;
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
        
        // get field value and scale
        double solenoidScale = Constants.getSolenoidScale();
        double solenoidValue = Constants.getSolenoidMagnitude(); // already the absolute value
        
        // make list of crosses consistent with a track candidate
        List<Seed> seeds = null;
        if(solenoidValue<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder();
            seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, isSVTonly);
            if(exLayrs==true) {
                seeds = recUtil.reFit(seeds, SVTGeom, BMTGeom, swimmer, trseed);
            }
            //TrackSeederCA trseed = new TrackSeederCA();  // cellular automaton seeder
            //seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer);
        } else {
            if(isSVTonly) {
                TrackSeeder trseed = new TrackSeeder();
                trseed.unUsedHitsOnly = true;
                seeds = trseed.findSeed(crosses.get(0), null, SVTGeom, BMTGeom, swimmer);
            } else {
                TrackSeederCA trseed = new TrackSeederCA();  // cellular automaton seeder
                seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer);
                //second seeding algorithm to search for SVT only tracks, and/or tracks missed by the CA
                
                TrackSeeder trseed2 = new TrackSeeder();
                trseed2.unUsedHitsOnly = true;
                seeds.addAll( trseed2.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer)); 
                if(exLayrs==true) {
                    seeds = recUtil.reFit(seeds, SVTGeom, BMTGeom, swimmer, trseed,trseed2);
                }
            }
        }
        if(seeds ==null || seeds.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, shift);
            return true;
        }   
        
        KFitter kf = null;
        List<Track> trkcands = new ArrayList<Track>();
        
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
                kf.matLib = Constants.kfMatLib;
                //Uncomment to let track be fitted
                //kf.filterOn=false;
                kf.runFitter(swimmer);
                if (kf.setFitFailed == false && kf.NDF>0 && kf.KFHelix!=null) { 
                    Track fittedTrack = recUtil.OutputTrack(seed, kf, SVTGeom, BMTGeom);
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
                        kf.matLib = Constants.kfMatLib;
                        //Uncomment to let track be fitted
                        //kf.filterOn = false;
                        kf.runFitter(swimmer);
                        
                        if(kf.KFHelix!=null) {
                            fittedTrack = recUtil.OutputTrack(seed, kf, SVTGeom, BMTGeom);
                        }
                    }    
                    
                    trkcands.add(fittedTrack);
                    trkcands.get(trkcands.size() - 1).set_TrackingStatus(seed.trkStatus);
                } else {
                    //trkcands.add(recUtil.OutputTrack(seed));
                    //trkcands.get(trkcands.size() - 1).set_TrackingStatus(1);
            }
        }
    

        if (trkcands.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, shift);
            return true;
        }
        
        //This last part does ELoss C
        TrackListFinder trkFinder = new TrackListFinder();
        trkFinder.removeOverlappingTracks(trkcands); 
        List<Track> tracks = trkFinder.getTracks(trkcands, SVTGeom, BMTGeom, CTOFGeom, CNDGeom, swimmer);

        for( int i=0;i<tracks.size();i++) { 
            tracks.get(i).set_Id(i+1);         
        }

        //System.out.println( " *** *** trkcands " + trkcands.size() + " * trks " + tracks.size());
        // RDV: can it be done before doing trajectories??
        //trkFinder.removeOverlappingTracks(tracks); //turn off until debugged
        // reset cross IDs
        for(int a = 0; a<2; a++) {
            for(Cross c : crosses.get(a))
                c.set_AssociatedTrackID(-1);
        }
        for (int c = 0; c < tracks.size(); c++) {
            tracks.get(c).set_Id(c + 1);
            for (int ci = 0; ci < tracks.get(c).size(); ci++) {

                if (crosses.get(0) != null && crosses.get(0).size() > 0) {
    //                    for (Cross crsSVT : crosses.get(0)) {
                        for (int jj=0 ; jj < crosses.get(0).size(); jj++) {
                                Cross crsSVT = crosses.get(0).get(jj);
                        if (crsSVT.get_Sector() == tracks.get(c).get(ci).get_Sector() && crsSVT.get_Cluster1()!=null && crsSVT.get_Cluster2()!=null 
                                && tracks.get(c).get(ci).get_Cluster1()!=null && tracks.get(c).get(ci).get_Cluster2()!=null
                                && crsSVT.get_Cluster1().get_Id() == tracks.get(c).get(ci).get_Cluster1().get_Id()
                                && crsSVT.get_Cluster2().get_Id() == tracks.get(c).get(ci).get_Cluster2().get_Id()) {  
                            crsSVT.set_Point(tracks.get(c).get(ci).get_Point());
                            tracks.get(c).get(ci).set_Id(crsSVT.get_Id());
                            crsSVT.set_PointErr(tracks.get(c).get(ci).get_PointErr());
                            crsSVT.set_Dir(tracks.get(c).get(ci).get_Dir());
                            crsSVT.set_DirErr(tracks.get(c).get(ci).get_DirErr());
                            crsSVT.set_AssociatedTrackID(c + 1);
                            crsSVT.get_Cluster1().set_AssociatedTrackID(c + 1);
                            for (FittedHit h : crsSVT.get_Cluster1()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                            for (FittedHit h : crsSVT.get_Cluster2()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                            crsSVT.get_Cluster2().set_AssociatedTrackID(c + 1);

                        }
                    }
                }
                if (crosses.get(1) != null && crosses.get(1).size() > 0) {
    //                    for (Cross crsBMT : crosses.get(1)) {
                        for (int jj=0 ; jj < crosses.get(1).size(); jj++) {
                                Cross crsBMT = crosses.get(1).get(jj);
                        if (crsBMT.get_Id() == tracks.get(c).get(ci).get_Id()) {
                            crsBMT.set_Point(tracks.get(c).get(ci).get_Point());
                            crsBMT.set_PointErr(tracks.get(c).get(ci).get_PointErr());
                            crsBMT.set_Dir(tracks.get(c).get(ci).get_Dir());
                            crsBMT.set_DirErr(tracks.get(c).get(ci).get_DirErr());
                            crsBMT.set_AssociatedTrackID(c + 1);
                            crsBMT.get_Cluster1().set_AssociatedTrackID(c + 1);
                            for (FittedHit h : crsBMT.get_Cluster1()) {
                                h.set_AssociatedTrackID(c + 1);
                            }
                        }
                    }
                }
            }
        }

        /// remove direction information from crosses that were part of duplicates, now removed. TODO: Should I put it in the clone removal?  
        for( Cross c : crosses.get(1) ) {
                if( c.get_AssociatedTrackID() < 0 ) {
                        c.set_Dir( new Vector3D(0,0,0));
                        c.set_DirErr( new Vector3D(0,0,0));
                        if( c.get_Type()==BMTType.C) {
                            c.set_Point(new Point3D(Double.NaN,Double.NaN,c.get_Point().z()));
                        }
                        else {
                            c.set_Point(new Point3D(c.get_Point().x(),c.get_Point().y(),Double.NaN));
                        }
                }
        }
        for( Cross c : crosses.get(0) ) {
                if( c.get_AssociatedTrackID() < 0 ) {
                        c.set_Dir( new Vector3D(0,0,0));
                        c.set_DirErr( new Vector3D(0,0,0));
                }
        }


        //------------------------
        // set index associations
        if (tracks.size() > 0) {
            recUtil.CleanupSpuriousCrosses(crosses, tracks, SVTGeom) ; 
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, tracks, shift);
        }

        return true;

        }
    
}