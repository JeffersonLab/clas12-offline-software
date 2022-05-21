package org.jlab.rec.rtpc.HitStrategy;

import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;
import org.jlab.rec.rtpc.hit.ReducedTrackMap;

import java.util.HashMap;
import java.util.List;

public class NewHitStrategy {

  public NewHitStrategy(HitParameters params, HashMap<Integer, List<RecoHitVector>> reconTrackMap) {

    ReducedTrackMap RTIDMap = new ReducedTrackMap();
    new HitVectorCreation(params, RTIDMap);

    ReducedTrackMap AvgRTIDMap = new ReducedTrackMap();
    new TimeAverage(params, RTIDMap, AvgRTIDMap, true);

    new HitReconstruction(AvgRTIDMap, params, reconTrackMap);

    HashMap<Integer, List<RecoHitVector>> reconAllTrackMap = new HashMap<>();
    new HitReconstruction(RTIDMap, params, reconAllTrackMap);
  }
}
