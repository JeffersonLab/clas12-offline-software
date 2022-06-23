package org.jlab.rec.rtpc.HitStrategy;

import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.HitVector;
import org.jlab.rec.rtpc.hit.ReducedTrack;
import org.jlab.rec.rtpc.hit.ReducedTrackMap;

import java.util.ArrayList;
import java.util.List;

public class HitVectorCreation {

  public HitVectorCreation(HitParameters params, ReducedTrackMap AllHitMap) {

    ReducedTrackMap RTIDMap = params.get_rtrackmap();
    List<Integer> tids = RTIDMap.getAllTrackIDs();

    for (int TID : tids) {
      ReducedTrack track = RTIDMap.getTrack(TID);
      List<HitVector> allHits = track.getAllHits();
      ReducedTrack rtrack = new ReducedTrack();
      for (HitVector hit : allHits) {
        ArrayList<HitVector> allNoAvgHits = hit.listRawHit();
        for (HitVector NoAvgHit : allNoAvgHits) {
          rtrack.addHit(NoAvgHit);
        }
      }
      rtrack.sortHits();
      AllHitMap.addTrack(rtrack);
    }
  }
}
