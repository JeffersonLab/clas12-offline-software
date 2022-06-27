package org.jlab.service.rtpc;

import org.apache.commons.math3.filter.KalmanFilter;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.rtpc.HitStrategy.NewHitStrategy;
import org.jlab.rec.rtpc.KalmanFilter.KalmanFitter;
import org.jlab.rec.rtpc.KalmanFilter.KalmanFitterInfo;
import org.jlab.rec.rtpc.banks.HitReader;
import org.jlab.rec.rtpc.banks.RecoBankWriter;
import org.jlab.rec.rtpc.hit.*;
import org.jlab.utils.groups.IndexedTable;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RTPCEngine extends ReconstructionEngine {

  public RTPCEngine() {
    super("RTPC", "davidp", "3.0");
  }

  double count = 0;
  double success = 0;
  private boolean simulation = false;
  private boolean cosmic = false;
  private int fitToBeamline = 1;
  private boolean disentangle = true;
  private boolean chi2culling = true;
  private boolean kfStatus = true;

  @Override
  public boolean init() {
    String sim = this.getEngineConfigString("rtpcSimulation");
    String cosm = this.getEngineConfigString("rtpcCosmic");
    String beamfit = this.getEngineConfigString("rtpcBeamlineFit");
    String disentangler = this.getEngineConfigString("rtpcDisentangler");
    String chi2cull = this.getEngineConfigString("rtpcChi2Cull");
    String kfstatus = this.getEngineConfigString("rtpcKF");

    if (sim != null) {
      simulation = Boolean.parseBoolean(sim);
    }

    if (cosm != null) {
      cosmic = Boolean.parseBoolean(cosm);
    }

    if (beamfit != null) {
      fitToBeamline = Boolean.parseBoolean(beamfit) ? 1 : 0;
    }

    if (disentangler != null) {
      disentangle = Boolean.parseBoolean(disentangler);
    }

    if (chi2cull != null) {
      chi2culling = Boolean.parseBoolean(chi2cull);
    }

    if(kfstatus != null){
      kfStatus = Boolean.parseBoolean(kfstatus);
    }

    String[] rtpcTables =
        new String[] {
          "/calibration/rtpc/time_offsets",
          "/calibration/rtpc/gain_balance",
          "/calibration/rtpc/time_parms",
          "/calibration/rtpc/recon_parms"
        };

    requireConstants(Arrays.asList(rtpcTables));

    this.registerOutputBank("RTPC::hits", "RTPC::tracks");

    return true;
  }

  @Override
  public boolean processDataEvent(DataEvent event) {

    // disentangle = false;
    // simulation = true;

    HitParameters params = new HitParameters();

    HitReader hitRead = new HitReader();
    hitRead.fetch_RTPCHits(event, simulation, cosmic); // boolean is for simulation

    List<Hit> hits;

    hits = hitRead.get_RTPCHits();

    if (hits == null || hits.size() == 0) {
      return true;
    }

    int runNo = 10;
    double magfield = 50.0;
    double magfieldfactor = 1;

    if (event.hasBank("RUN::config")) {
      DataBank bank = event.getBank("RUN::config");
      runNo = bank.getInt("run", 0);
      magfieldfactor = bank.getFloat("solenoid", 0);
      if (runNo <= 0) {
        System.err.println("RTPCEngine:  got run <= 0 in RUN::config, skipping event.");
        return false;
      }
    }

    runNo = 10;

    magfield = magfield * magfieldfactor;
    IndexedTable time_offsets =
        this.getConstantsManager().getConstants(runNo, "/calibration/rtpc/time_offsets");
    int hitsbound;
    hitsbound = (int) time_offsets.getDoubleValue("tl", 1, 1, 4);
    if (hitsbound < 1) hitsbound = 15000;
    if (hits.size() > hitsbound) return true;

    if (event.hasBank("RTPC::adc")) {

      count++;

      // if (count != 2) return true;
      // if (count < 4 || count > 10) return true;
      // if (count > 10000) return true;

      params.init(this.getConstantsManager(), runNo);

      new SignalSimulation(hits, params, simulation);

      // Sort Hits into Tracks at the Readout Pads
      new TrackFinder(params, cosmic);
      // Calculate Average Time of Hit Signals
      new TimeAverage(this.getConstantsManager(), params, runNo);
      // Disentangle Crossed Tracks
      new TrackDisentangler(params, disentangle);
      // Reconstruct Hits in Drift Region
      new TrackHitReco(params, hits, cosmic, magfield);
      // Helix Fit Tracks to calculate Track Parameters
      new HelixFitTest(params, fitToBeamline, Math.abs(magfield), cosmic, chi2culling);

      RecoBankWriter writer = new RecoBankWriter();
      DataBank recoBank = writer.fillRTPCHitsBank(event, params);
      DataBank trackBank = writer.fillRTPCTrackBank(event, params);

      event.appendBank(recoBank);
      event.appendBank(trackBank);

      if (kfStatus) {
        HashMap<Integer, KalmanFitterInfo> KFTrackMap = new HashMap<>();
        try {
          // New Hit Strategy
          HashMap<Integer, List<RecoHitVector>> reconTrackMap = new HashMap<>();
          new NewHitStrategy(params, event, reconTrackMap);
          new KalmanFitter(params, event, reconTrackMap, KFTrackMap, magfield, simulation);
          success++;
        } catch (Exception e) {
          e.printStackTrace();
        }
        DataBank KFBank = writer.fillRTPCKFBank(event, KFTrackMap);
        event.appendBank(KFBank);
      }


    } else {
      return true;
    }
    return true;
  }

  public static void main(String[] args) {

    double starttime = System.nanoTime();

    String inputFile = "proton_100k_70-90MeV_fullZ.hipo";
    // String inputFile =  "data_elastic_radiative.hipo";
    String outputFile = "output.hipo";

    if (new File(outputFile).delete()) System.out.println("output.hipo is delete.");

    System.err.println(" \n[PROCESSING FILE] : " + inputFile);

    RTPCEngine en = new RTPCEngine();
    en.init();

    HipoDataSource reader = new HipoDataSource();
    HipoDataSync writer = new HipoDataSync();
    reader.open(inputFile);
    writer.open(outputFile);
    while (reader.hasEvent()) {
      DataEvent event = reader.getNextEvent();
      en.processDataEvent(event);
      writer.writeEvent(event);
    }
    writer.close();

    System.out.println("finished " + (System.nanoTime() - starttime) * Math.pow(10, -9));
  }
}
