package org.jlab.rec.rtpc.HitStrategy;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;
import org.jlab.rec.rtpc.hit.ReducedTrackMap;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

public class NewHitStrategy {

  public NewHitStrategy(HitParameters params, DataEvent event, HashMap<Integer, List<RecoHitVector>> reconTrackMap) {

    ReducedTrackMap RTIDMap = new ReducedTrackMap();
    new HitVectorCreation(params, RTIDMap);

    ReducedTrackMap AvgRTIDMap = new ReducedTrackMap();
    new TimeAverage(params, RTIDMap, AvgRTIDMap, true);

    new HitReconstruction(AvgRTIDMap, params, reconTrackMap);

    HashMap<Integer, List<RecoHitVector>> reconAllTrackMap = new HashMap<>();
    new HitReconstruction(RTIDMap, params, reconAllTrackMap);

    // saveHits(params, event, reconTrackMap, reconAllTrackMap);
  }

  void saveHits(
      HitParameters params,
      DataEvent event,
      HashMap<Integer, List<RecoHitVector>> reconTrackMap,
      HashMap<Integer, List<RecoHitVector>> reconAllTrackMap) {

    HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();
    try {
      FileWriter fWriter = new FileWriter("Hits.dat", true);
      fWriter.write("New Track : " + '\n');
      for (int TID : recotrackmap.keySet()) {
        for (int i = 0; i < recotrackmap.get(TID).size(); i++) {
          double x_rec = recotrackmap.get(TID).get(i).x();
          double y_rec = recotrackmap.get(TID).get(i).y();
          double z_rec = recotrackmap.get(TID).get(i).z();
          int padID = recotrackmap.get(TID).get(i).pad();
          double time = recotrackmap.get(TID).get(i).time();

          fWriter.write("" + x_rec + ' ' + y_rec + ' ' + z_rec + ' ' + padID + ' ' + time + '\n');
        }
      }
      fWriter.close();
    } catch (Exception ignored) {
    }

    try {
      FileWriter fWriter = new FileWriter("TrueHits.dat", true);
      fWriter.write("New Track : " + '\n');
      DataBank particle = event.getBank("MC::Particle");
      double vx_mc = particle.getFloat("vx", 0);
      double vy_mc = particle.getFloat("vy", 0);
      double vz_mc = particle.getFloat("vz", 0);
      fWriter.write("" + vx_mc + ' ' + vy_mc + ' ' + vz_mc + '\n');

      DataBank mc = event.getBank("MC::True");
      int true_rows = mc.rows();
      for (int i = 0; i < true_rows; i++) {
        int PID = mc.getInt("pid", i);
        if (PID == 2212) {
          double mc_x = mc.getFloat("avgX", i);
          double mc_y = mc.getFloat("avgY", i);
          double mc_z = mc.getFloat("avgZ", i);
          fWriter.write("" + mc_x + ' ' + mc_y + ' ' + mc_z + '\n');
        }
      }
      fWriter.close();
    } catch (Exception ignored) {
    }

    try {
      FileWriter fWriter = new FileWriter("RecoAvgHits.dat", true);
      fWriter.write("New Track : " + '\n');
      for (int TID : reconTrackMap.keySet()) {
        for (RecoHitVector hit : reconTrackMap.get(TID)) {
          double x_rec = hit.x();
          double y_rec = hit.y();
          double z_rec = hit.z();
          int padID = hit.pad();
          double time = hit.time();
          fWriter.write("" + x_rec + ' ' + y_rec + ' ' + z_rec + ' ' + padID + ' ' + time + '\n');
        }
      }
      fWriter.close();
    } catch (Exception ignored) {
    }

    try {
      FileWriter fWriter = new FileWriter("RecoAllHits.dat", true);
      fWriter.write("New Track : " + '\n');
      for (int TID : reconAllTrackMap.keySet()) {
        for (RecoHitVector hit : reconAllTrackMap.get(TID)) {
          double x_rec = hit.x();
          double y_rec = hit.y();
          double z_rec = hit.z();
          int padID = hit.pad();
          double time = hit.time();
          fWriter.write("" + x_rec + ' ' + y_rec + ' ' + z_rec + ' ' + padID + ' ' + time + '\n');
        }
      }
      fWriter.close();
    } catch (Exception ignored) {
    }
  }
}
