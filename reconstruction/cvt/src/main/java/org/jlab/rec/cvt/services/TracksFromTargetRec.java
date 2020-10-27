/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.services;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.swimtools.Swim;
import org.jlab.detector.geant4.v2.CTOFGeant4Factory;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
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
            org.jlab.rec.cvt.svt.Geometry SVTGeom, org.jlab.rec.cvt.bmt.Geometry BMTGeom,
            CTOFGeant4Factory CTOFGeom, Detector CNDGeom,
            RecoBankWriter rbc,
            double shift, 
            Swim swimmer,
            boolean isSVTonly) {
        // make list of crosses consistent with a track candidate
        
        List<Seed> seeds = null;
        if(Math.abs(Constants.getSolenoidVal())<0.001) {
            StraightTrackSeeder trseed = new StraightTrackSeeder();
            seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, isSVTonly);
            //TrackSeederCA trseed = new TrackSeederCA();  // cellular automaton seeder
            //seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer);
        } else {
            if(isSVTonly) {
                TrackSeeder trseed = new TrackSeeder();
                seeds = trseed.findSeed(crosses.get(0), null, SVTGeom, BMTGeom, swimmer);
            
            } else {
                TrackSeederCA trseed = new TrackSeederCA();  // cellular automaton seeder
                seeds = trseed.findSeed(crosses.get(0), crosses.get(1), SVTGeom, BMTGeom, swimmer);
            }
        }
        if(seeds ==null || seeds.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, shift);
            return true;
        }   
        
        org.jlab.clas.tracking.kalmanfilter.helical.KFitter kf = null;
        List<Track> trkcands = new ArrayList<Track>();

        for (Seed seed : seeds) { 
            org.jlab.clas.tracking.trackrep.Helix hlx = null ;

            double xr =  -seed.get_Helix().get_dca()*Math.sin(seed.get_Helix().get_phi_at_dca());
            double yr =  seed.get_Helix().get_dca()*Math.cos(seed.get_Helix().get_phi_at_dca());
            double zr =  seed.get_Helix().get_Z0();
            double pt = Constants.LIGHTVEL * seed.get_Helix().radius() * Constants.getSolenoidVal();
            if(Math.abs(Constants.getSolenoidVal())<0.001)
                pt = 100;
            double pz = pt*seed.get_Helix().get_tandip();
            double px = pt*Math.cos(seed.get_Helix().get_phi_at_dca());
            double py = pt*Math.sin(seed.get_Helix().get_phi_at_dca());
            
            int charge = (int) (Math.signum(Constants.getSolenoidscale())*seed.get_Helix().get_charge());
            if(Math.abs(Constants.getSolenoidVal())<0.001)
                charge = 1;
            
            hlx = new org.jlab.clas.tracking.trackrep.Helix(xr,yr,zr,px,py,pz, 
                    charge, Constants.getSolenoidVal(), org.jlab.clas.tracking.trackrep.Helix.Units.MM);

            Matrix cov = seed.get_Helix().get_covmatrix();
            if(Math.abs(Constants.getSolenoidVal())>0.001 &&
                    Constants.LIGHTVEL * seed.get_Helix().radius() *Constants.getSolenoidVal()<Constants.PTCUT)
                continue;
                kf = new org.jlab.clas.tracking.kalmanfilter.helical.KFitter( hlx, cov, event,  swimmer, 
                    org.jlab.rec.cvt.Constants.getXb(), 
                    org.jlab.rec.cvt.Constants.getYb(),
                    org.jlab.rec.cvt.Constants.getZoffset(), 
                    recUtil.setMeasVecs(seed, SVTGeom)) ;

                kf.runFitter(swimmer);
               
                if (kf.setFitFailed == false && kf.NDF>0) {
                    trkcands.add(recUtil.OutputTrack(seed, kf, SVTGeom, BMTGeom));
                    trkcands.get(trkcands.size() - 1).set_TrackingStatus(2);
                }
            //} else {
                //trkcands.add(recUtil.OutputTrack(seed));
                //trkcands.get(trkcands.size() - 1).set_TrackingStatus(1);
            //}
        }
    

        if (trkcands.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, shift);
            return true;
        }

        //This last part does ELoss C
        TrackListFinder trkFinder = new TrackListFinder();
        List<Track> tracks = trkFinder.getTracks(trkcands, SVTGeom, BMTGeom, CTOFGeom, CNDGeom, swimmer);
        for( int i=0;i<tracks.size();i++) { 
            tracks.get(i).set_Id(i+1);         }

        //System.out.println( " *** *** trkcands " + trkcands.size() + " * trks " + trks.size());
        trkFinder.removeOverlappingTracks(tracks); //turn off until debugged

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
                        if( c.get_DetectorType().equalsIgnoreCase("C")) {
    //        			System.out.println(c + " " + c.get_AssociatedTrackID());
                                c.set_Point(new Point3D(Double.NaN,Double.NaN,c.get_Point().z()));
    //        			System.out.println(c.get_Point());
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