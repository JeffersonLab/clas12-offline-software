package org.jlab.rec.rtpc.HitStrategy;

import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.HitVector;
import org.jlab.rec.rtpc.hit.ReducedTrack;
import org.jlab.rec.rtpc.hit.ReducedTrackMap;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TimeAverage {

  private final double[] a_t;
  private double at = 0;
  private final double[] b_t;
  private double bt = 0;
  private final double tl;
  private final double tp;
  private final double tr;

  public TimeAverage(
      HitParameters params,
      ReducedTrackMap AllHitsRTIDMap,
      ReducedTrackMap RTIDMap,
      boolean average) {

    double TFTotalTrackTimeFlag = params.get_TFtotaltracktimeflag();

    a_t = params.get_atparms();
    b_t = params.get_btparms();
    tl = params.get_tl();
    tp = params.get_tp();
    tr = params.get_tr();

    int NumberOfPads = 17280;

    List<Integer> tids = AllHitsRTIDMap.getAllTrackIDs();
    for (int TID : tids) {
      ReducedTrack AvgHitsTrack = new ReducedTrack();
      ReducedTrack allHitsTrack = AllHitsRTIDMap.getTrack(TID);
      List<HitVector> allHits = allHitsTrack.getAllHits();
      allHitsTrack.sortHits();

      TreeMap<Double, List<HitVector>> timeTreeMap = new TreeMap<>();

      for (HitVector hit : allHits) {

        double time = hit.time();
        double r_rec = get_r_rec(hit.z(), time);
        if (r_rec < 30) continue;
        if (r_rec > 70) continue;
        if (!timeTreeMap.containsKey(time)) {
          timeTreeMap.put(time, new ArrayList<>());
        }
        timeTreeMap.get(time).add(hit);
      }

      for (HitVector hit : allHits) {
        double time = hit.time();
        double r_rec = get_r_rec(hit.z(), time);
        if (r_rec < 30) {
          time = (int) get_t_cor(hit.z(), 30.01);
          Double nextLower = timeTreeMap.floorKey(time);
          timeTreeMap.get(nextLower).add(hit);
        }
        if (r_rec > 70) {
          time = (int) get_t_cor(hit.z(), 69.99);
          Double nextHigher = timeTreeMap.ceilingKey(time);
          timeTreeMap.get(nextHigher).add(hit);
        }
      }

      if (!average) {
        for (double t : timeTreeMap.keySet()) {

          List<HitVector> hitVectorList = timeTreeMap.get(t);
          for (HitVector hit : hitVectorList) {
            System.out.println("hit.pad() = " + hit.pad());
            HitVector v = new HitVector(hit.pad(), hit.z(), hit.phi(), t, hit.adc());
            AvgHitsTrack.addHit(v);
          }
        }
      } else {

        for (double t : timeTreeMap.keySet()) {
          List<HitVector> hitVectorList = timeTreeMap.get(t);
          double avgPadID = 0;
          double avgADC = 0;
          double avgZ = 0;
          double den = 0;

          double x = 0.0;
          double y = 0.0;

          for (int i = 0; i < hitVectorList.size(); i++) {
            HitVector hit = hitVectorList.get(i);

            HitVector nextHit;
            if (i < hitVectorList.size() - 1) nextHit = hitVectorList.get(i + 1);
            else nextHit = hitVectorList.get(i);

            if (nextHit.pad() - hit.pad() > 5000) {
              nextHit.setpad(nextHit.pad() - NumberOfPads);
            }
            if (nextHit.pad() - hit.pad() < -5000) {
              nextHit.setpad(nextHit.pad() + NumberOfPads);
            }

            double adc = hit.adc();
            avgPadID += hit.pad() * adc;

            x += Math.cos(hit.phi()) * adc;
            y += Math.sin(hit.phi()) * adc;

            avgADC += hit.adc() * adc;
            avgZ += hit.z() * adc;
            den += adc;
          }

          avgPadID /= den;
          avgADC /= den;
          avgZ /= den;

          double avgPhi = Math.atan2(y / den, x / den);

          if (avgPadID > NumberOfPads) avgPadID -= NumberOfPads;
          if (avgPadID < 0) avgPadID += NumberOfPads;

          if (!Double.isNaN(avgPadID)
              || !Double.isNaN(avgPhi)
              || !Double.isNaN(avgADC)
              || !Double.isNaN(avgZ)) {
            HitVector v = new HitVector((int) avgPadID, avgZ, avgPhi, t, avgADC);
            AvgHitsTrack.addHit(v);
          }
        }
      }

      AvgHitsTrack.sortHits();
      if (Math.abs(AvgHitsTrack.getLargeT() - AvgHitsTrack.getSmallT()) < TFTotalTrackTimeFlag)
        AvgHitsTrack.flagTrack();
      RTIDMap.addTrack(AvgHitsTrack);
    }
  }

  private double get_rec_coef(double[] params, double z2) {
    double z = z2 / 10;
    return params[4] * z * z * z * z
        + params[3] * z * z * z
        + params[2] * z * z
        + params[1] * z
        + params[0];
  }

  private double get_r_rec(double z, double t) {
    at = get_rec_coef(a_t, z) + tl + tp + tr;
    bt = get_rec_coef(b_t, z);
    double x = (t - at) / bt;
    double rmax = 70;
    double rmin = 30;
    return Math.sqrt(rmax * rmax * (1 - x) + rmin * rmin * x);
  }

  private double get_t_cor(double z, double r) {
    at = get_rec_coef(a_t, z) + tl + tp + tr;
    bt = get_rec_coef(b_t, z);
    double rmax = 70.0;
    double rmin = 30.0;

    double x = (r * r - rmax * rmax) / (rmin * rmin - rmax * rmax);
    return x * bt + at;
  }
}
