/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.dc.nn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.cluster.Cluster;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.cross.Cross;
import org.jlab.rec.dc.cross.CrossList;
import org.jlab.rec.dc.cross.CrossMaker;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.segment.Segment;
import org.jlab.rec.dc.trajectory.Road;
import org.jlab.rec.dc.trajectory.RoadFinder;
/**
 *
 * @author ziegler
 */
public class PatternRec {
    
    private ClusterFinder clf = new ClusterFinder();
    private ClusterCleanerUtilities ct = new ClusterCleanerUtilities();
    private ClusterFitter cf = new ClusterFitter();
    private CrossMaker crf = new CrossMaker();
    private RoadFinder rf = new RoadFinder();      
    
    public CrossList RecomposeCrossList(List<Segment> clusters,
            DCGeant4Factory DcDetector) {
        CrossList crossList = new CrossList();
        Map<Integer, ArrayList<Cross>> grpCrs = new HashMap<Integer, ArrayList<Cross>>();
        Map<Integer, ArrayList<Segment>> grpCls = new HashMap<Integer, ArrayList<Segment>>();
        
        for(Segment cls : clusters) {
            int index = cls.get(0).get_AssociatedHBTrackID();
            if(grpCls.get(index)==null) { // if the list not yet created make it
                    grpCls.put(index, new ArrayList<Segment>()); 
                    grpCls.get(index).add(cls); // append cluster
            } else {
                grpCls.get(index).add(cls); // append 
            }
        }
        // using iterators 
        Iterator<Map.Entry<Integer, ArrayList<Segment>>> itr = grpCls.entrySet().iterator(); 
        while(itr.hasNext()) {
            Map.Entry<Integer, ArrayList<Segment>> entry = itr.next(); 
            List<Cross> crosses = crf.find_Crosses(entry.getValue(), DcDetector);  
            Collections.sort(crosses);
            
            if(crosses.size()<3) { // find pseudocross
                List<Road> allRoads = rf.findRoads(entry.getValue(), DcDetector);
                List<Segment> Segs2Road = new ArrayList<>();
                for (Road r : allRoads) { 
                    Segs2Road.clear();
                    int missingSL = -1;
                    for (int ri = 0; ri < 3; ri++) {
                        if (r.get(ri).associatedCrossId == -1) {
                            if (r.get(ri).get_Superlayer() % 2 == 1) {
                                missingSL = r.get(ri).get_Superlayer() + 1;
                            } else {
                                missingSL = r.get(ri).get_Superlayer() - 1;
                            }
                        } 
                    }
                    for (int ri = 0; ri < 3; ri++) {
                        for (Segment s : entry.getValue()) {
                            if (s.get_Sector() == r.get(ri).get_Sector() &&
                                    s.get_Region() == r.get(ri).get_Region() &&
                                    s.associatedCrossId == r.get(ri).associatedCrossId &&
                                    r.get(ri).associatedCrossId != -1) {
                                if (s.get_Superlayer() % 2 == missingSL % 2)
                                    Segs2Road.add(s);
                            }
                        }
                    }
                    if (Segs2Road.size() == 2) {
                        Segment pSegment = rf.findRoadMissingSegment(Segs2Road,
                                DcDetector,
                                r.a);
                        if (pSegment != null)
                            entry.getValue().add(pSegment);
                    }
                }
            crosses = crf.find_Crosses(entry.getValue(), DcDetector);  
            Collections.sort(crosses);   
            grpCrs.put(entry.getKey(), (ArrayList<Cross>) crosses);
            } else {
                grpCrs.put(entry.getKey(), (ArrayList<Cross>) crosses);
            }
        }
        // using iterators 
        Iterator<Map.Entry<Integer, ArrayList<Cross>>> citr = grpCrs.entrySet().iterator(); 
        while(citr.hasNext()) {
            Map.Entry<Integer, ArrayList<Cross>> entry = citr.next(); 
            crossList.add(entry.getValue());
        }
        return crossList;
    }
    
    public List<Segment> RecomposeSegments(List<Hit> fhits, 
            DCGeant4Factory DcDetector) {
            
        List<Segment> fclusters = new ArrayList<Segment>();
        Map<Integer, ArrayList<Hit>> grpHits = new HashMap<Integer, ArrayList<Hit>>();
        
        for (Hit hit : fhits) {
            if (hit.NNTrkId > 0) {
                int index = hit.NNTrkId;
                if(grpHits.get(index)==null) { // if the list not yet created make it
                    grpHits.put(index, new ArrayList<Hit>()); 
                    grpHits.get(index).add(hit); // append hit
                } else {
                    grpHits.get(index).add(hit); // append hit
                }
            }
        }
        
        // using iterators 
        Iterator<Map.Entry<Integer, ArrayList<Hit>>> itr = grpHits.entrySet().iterator(); 
        
        while(itr.hasNext()) {
            Map.Entry<Integer, ArrayList<Hit>> entry = itr.next(); 
             
            if(entry.getValue().size()>=20) {// 4 layers per superlayer, 5 out of six superlayer tracking
                // find clusters
                 //fill array of hit
                clf.fillHitArray(entry.getValue(), 0);        //find clumps of hits init
                List<Cluster> clusters = clf.findClumps(entry.getValue(), ct);
                for (Cluster clus : clusters) {
                    FittedCluster fclus = new FittedCluster(clus);
                    if (clus != null && clus.size() >= 4 ) { //4 layers per superlayer
                        // update the hits
                        for (FittedHit fhit : fclus) {
                            fhit.set_TrkgStatus(0);
                            fhit.updateHitPosition(DcDetector); 
                        }
                
                        cf.SetFitArray(fclus, "TSC"); 
                        cf.Fit(fclus, true); 
                        cf.SetResidualDerivedParams(fclus, false, false, DcDetector); //calcTimeResidual=false, resetLRAmbig=false, local= false

                        fclus = ct.ClusterCleaner(fclus, cf, DcDetector);
                        // update the hits
                        for (FittedHit fhit : fclus) {
                            fhit.set_AssociatedClusterID(clus.get_Id());
                            fhit.set_AssociatedHBTrackID(entry.getKey());
                        }
                        cf.SetFitArray(fclus, "TSC");
                        cf.Fit(fclus, false);
                        cf.SetSegmentLineParameters(fclus.get(0).get_Z(), fclus);
                        
                        Segment seg = new Segment(fclus);
                        seg.set_fitPlane(DcDetector);

                        double sumRes=0;
                        double sumTime=0;

                        for(FittedHit h : seg) {
                            sumRes+=h.get_TimeResidual();
                            sumTime+=h.get_Time();
                        }
                        seg.set_ResiSum(sumRes);
                        seg.set_TimeSum(sumTime);
            
                        fclusters.add(seg);
                    }
                }
            }
        }
        return fclusters;
    }

}
