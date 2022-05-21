package org.jlab.rec.rtpc.HitStrategy;

import org.jlab.rec.rtpc.hit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HitReconstruction {

  private final double[] a_t;
  private final double[] b_t;
  private final double[] a_phi;
  private final double[] b_phi;
  private final double[] c_phi;
  private final double tl;
  private final double tp;
  private final double tr;

  public HitReconstruction(
      ReducedTrackMap AvgRTIDMap,
      HitParameters params,
      HashMap<Integer, List<RecoHitVector>> recotrackmap) {

    a_t = params.get_atparms();
    b_t = params.get_btparms();
    a_phi = params.get_aphiparms();
    b_phi = params.get_bphiparms();
    c_phi = params.get_cphiparms();
    tl = params.get_tl();
    tp = params.get_tp();
    tr = params.get_tr();

    double tcathode = params.get_tcathode();
    double tshiftfactorshort = params.get_tshiftfactorshort();
    double tshiftfactorlong = params.get_tshiftfactorlong();

    double minhitcount = params.get_minhitspertrackreco();

    List<Integer> tids = AvgRTIDMap.getAllTrackIDs();
    for (int TID : tids) {
      ReducedTrack track = AvgRTIDMap.getTrack(TID);
      List<HitVector> allHits = track.getAllHits();
      if (allHits.size() < minhitcount) continue;
      track.sortHits();

      HitVector smallTHit = track.getSmallTHit();
      HitVector largeTHit = track.getLargeTHit();
      double smallt = smallTHit.time();
      double larget = largeTHit.time();
      double smallz = smallTHit.z();
      double largez = (largeTHit.z() + smallTHit.z()) / 2;
      double tdiffshort = tcathode + get_rec_coef(a_t, smallz) - a_t[0] - smallt;
      double tdifflong =
          tcathode
              + get_rec_coef(a_t, smallz)
              + get_rec_coef(b_t, largez)
              - a_t[0]
              - b_t[0]
              - larget;
      tdiffshort *= tshiftfactorshort;
      tdifflong *= tshiftfactorlong;
      double tdiff = tdiffshort + tdifflong;

      if (tdiff > 1000) continue; // tdiff is in ns

      recotrackmap.put(TID, new ArrayList<>());

      for (HitVector hit : allHits) {
        int padID = hit.pad();
        double time = hit.time();

        double r_rec = get_r_rec(hit.z(), time); // in mm
        double phi_rec = hit.phi() - get_dphi(hit.z(), r_rec); // in rad

        if (phi_rec < -Math.PI) {
          phi_rec += 2.0 * Math.PI;
        }
        if (phi_rec > Math.PI) {
          phi_rec -= 2.0 * Math.PI;
        }

        double x_rec = r_rec * (Math.cos(phi_rec));
        double y_rec = r_rec * (Math.sin(phi_rec));
        double z_rec = hit.z();

        if (!Double.isNaN(x_rec) && !Double.isNaN(y_rec) && !Double.isNaN(hit.z())) {
          recotrackmap
              .get(TID)
              .add(
                  new RecoHitVector(
                      padID, x_rec, y_rec, z_rec, r_rec, phi_rec, tdiff, time, hit.adc(), smallTHit,
                      largeTHit));
        }
      }
    }
  }

  private double get_rec_coef(double[] parms, double z2) {
    double z = z2 / 10;
    return parms[4] * z * z * z * z
        + parms[3] * z * z * z
        + parms[2] * z * z
        + parms[1] * z
        + parms[0];
  }

  private double get_r_rec(double z, double t) {
    double at = get_rec_coef(a_t, z) + tl + tp + tr;
    double bt = get_rec_coef(b_t, z);
    double x = (t - at) / bt;
    double rmax = 70;
    double rmin = 30;
    return Math.sqrt(rmax * rmax * (1 - x) + rmin * rmin * x);
  }

  private double get_dphi(double z, double r) {
    double aphi = get_rec_coef(a_phi, z);
    double bphi = get_rec_coef(b_phi, z);
    double cphi = get_rec_coef(c_phi, z);
    return aphi + bphi * Math.log(70 / r) + 100 * cphi * ((1 / (r * r)) - (1.0 / (70 * 70)));
  }
}
