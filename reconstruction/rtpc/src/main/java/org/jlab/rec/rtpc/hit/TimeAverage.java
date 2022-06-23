// Author: David Payette

/* This code takes the tracks from the Track Finder, and reduces the signals in the track to single
 * values in time by taking a weighted average of the signal using the ADC value as the weight
 * The end result is the same tracks but with hits which now have non-discritized times (not in
 * 120 ns slices) This is useful for the disentangler to split merged tracks
 */

package org.jlab.rec.rtpc.hit;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TimeAverage {

  public TimeAverage(ConstantsManager manager, HitParameters params, int runNo) {
    /*
     *Initializations
     */
    TrackMap TIDMap = params.get_trackmap();
    org.jlab.rec.rtpc.hit.ADCMap ADCMap = params.get_ADCMap();
    List<Integer> tids = TIDMap.getAllTrackIDs();
    IndexedTable gains = manager.getConstants(runNo, "/calibration/rtpc/gain_balance");
    double TFtotaltracktimeflag = params.get_TFtotaltracktimeflag();

    /*
     * Main Algorithm
     */

    ReducedTrackMap RTIDMap = new ReducedTrackMap();
    for (int tid : tids) { // Loop over all tracks
      Track track = TIDMap.getTrack(tid);
      boolean trackflag = track.isTrackFlagged();
      ReducedTrack rtrack = new ReducedTrack();
      if (trackflag) rtrack.flagTrack();
      Set<Integer> l = track.uniquePadList();
      Set<Integer> timesbypad;
      for (int pad : l) {
        double adcmax = 0;
        double sumnum = 0;
        double sumden = 0;
        timesbypad = track.PadTimeList(pad);
        double adc;
        for (int time : timesbypad) { // Loop to calculate maximum adc value
          adc = ADCMap.getADC(pad, time);
          if (adc > adcmax) {
            adcmax = adc;
          }
        }

        ArrayList<HitVector> allHitVector = new ArrayList<>();
        double adcthresh = adcmax / 4;
        double gain;
        for (int time :
            timesbypad) { // Loop to calculate weighted average time using ADC values which are
          // above half of the maximum
          adc = ADCMap.getADC(pad, time);
          if (adc > adcthresh) {
            sumnum += adc * time;
            sumden += adc;

            PadVector p = params.get_padvector(pad);
            gain = gains.getDoubleValue("gain", 1, (int) p.row(), (int) p.col());
            HitVector hit = new HitVector(pad, p.z(), p.phi(), time, adc / gain);
            allHitVector.add(hit);
          }
        }
        double averagetime = sumnum / sumden;
        PadVector p = params.get_padvector(pad);

        gain = gains.getDoubleValue("gain", 1, (int) p.row(), (int) p.col());
        if (gain == 0) gain = 1;
        HitVector v = new HitVector(pad, p.z(), p.phi(), averagetime, sumden / gain);
        v.setListRawHit(allHitVector);
        rtrack.addHit(v);
      }
      rtrack.sortHits();
      if (Math.abs(rtrack.getLargeT() - rtrack.getSmallT()) < TFtotaltracktimeflag)
        rtrack.flagTrack();
      RTIDMap.addTrack(rtrack);
    }

    /*
     * Output
     */

    params.set_rtrackmap(RTIDMap);
  }
}
