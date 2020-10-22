/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.services;
import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.cluster.Cluster;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.rec.cvt.cross.CrossList;
import org.jlab.rec.cvt.cross.StraightTrackCrossListFinder;
import org.jlab.rec.cvt.hit.FittedHit;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.TrackCandListFinder;
import org.jlab.rec.cvt.trajectory.TrajectoryFinder;
/**
 *
 * @author ziegler
 */

public class CosmicTracksRec {
    
    private StraightTrackCrossListFinder crossLister = new StraightTrackCrossListFinder();
     
    private RecUtilities recUtil = new RecUtilities();
    
    public boolean processEvent(DataEvent event,  
            List<FittedHit> SVThits, List<FittedHit> BMThits, 
            List<Cluster> SVTclusters, List<Cluster> BMTclusters, 
            List<ArrayList<Cross>> crosses,
            org.jlab.rec.cvt.svt.Geometry SVTGeom, org.jlab.rec.cvt.bmt.Geometry BMTGeom,
            RecoBankWriter rbc,
            double zShift) {
        // make list of crosses consistent with a track candidate
        
        CrossList crosslist = crossLister.findCosmicsCandidateCrossLists(crosses, SVTGeom,
                BMTGeom, 3);
        if (crosslist == null || crosslist.size() == 0) {
            // create the clusters and fitted hits banks
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, zShift);

            return true;
        }
        TrackCandListFinder trkcandFinder = new TrackCandListFinder();
        List<StraightTrack> cosmics = trkcandFinder.getStraightTracks(crosslist, crosses.get(1), SVTGeom, BMTGeom);

        //REMOVE THIS
        //crosses.get(0).addAll(crosses.get(1));
        //------------------------
        if (cosmics.size() == 0) {
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, null, zShift);

            return true;
        }
        
        if (cosmics.size() > 0) {
            for (int k1 = 0; k1 < cosmics.size(); k1++) {
                cosmics.get(k1).set_Id(k1 + 1);
                for (int k2 = 0; k2 < cosmics.get(k1).size(); k2++) {
                    cosmics.get(k1).get(k2).set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate crosses
                    if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
                        cosmics.get(k1).get(k2).get_Cluster1().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster1 in cross
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
                        cosmics.get(k1).get(k2).get_Cluster2().set_AssociatedTrackID(cosmics.get(k1).get_Id()); // associate cluster2 in cross	
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster1() != null) {
                        for (int k3 = 0; k3 < cosmics.get(k1).get(k2).get_Cluster1().size(); k3++) { //associate hits
                            cosmics.get(k1).get(k2).get_Cluster1().get(k3).set_AssociatedTrackID(cosmics.get(k1).get_Id());
                        }
                    }
                    if (cosmics.get(k1).get(k2).get_Cluster2() != null) {
                        for (int k4 = 0; k4 < cosmics.get(k1).get(k2).get_Cluster2().size(); k4++) { //associate hits
                            cosmics.get(k1).get(k2).get_Cluster2().get(k4).set_AssociatedTrackID(cosmics.get(k1).get_Id());
                        }
                    }
                }
                trkcandFinder.matchClusters(SVTclusters, new TrajectoryFinder(), SVTGeom, BMTGeom, true,
                        cosmics.get(k1).get_Trajectory(), k1 + 1);
            }
            recUtil.CleanupSpuriousCrosses(crosses, null, SVTGeom) ;
            //4)  ---  write out the banks
            List<Cross> bmtCrosses = new ArrayList<Cross>();
            List<Cross> bmtCrossesRm = new ArrayList<Cross>();
            for(int k0 = 0; k0<crosses.get(1).size(); k0++){
                for (int k1 = 0; k1 < cosmics.size(); k1++) { 
                    cosmics.get(k1).set_Id(k1 + 1);
                    for (int k2 = 0; k2 < cosmics.get(k1).size(); k2++) {
                        if(cosmics.get(k1).get(k2).get_Detector().equalsIgnoreCase("SVT")==true)
                            continue;
                        if(cosmics.get(k1).get(k2).get_Id()==crosses.get(1).get(k0).get_Id()){
                            if(crosses.get(1).get(k0).get_AssociatedTrackID()==-1) {
                                bmtCrossesRm.add(crosses.get(1).get(k0));
                            }
                        }
                        bmtCrosses.add(cosmics.get(k1).get(k2));
                    }
                }
            }
            crosses.get(1).removeAll(bmtCrossesRm);
            crosses.get(1).addAll(bmtCrosses);
            rbc.appendCVTCosmicsBanks(event, SVThits, BMThits, SVTclusters, BMTclusters, crosses, cosmics, zShift);
        }
        return true;
    }
    
}
