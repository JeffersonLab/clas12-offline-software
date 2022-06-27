package org.jlab.service.rtpc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.rtpc.banks.HitReader;
import org.jlab.rec.rtpc.banks.RecoBankWriter;
import org.jlab.rec.rtpc.hit.*;
import org.jlab.utils.groups.IndexedTable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class RTPCEngine extends ReconstructionEngine {

  public RTPCEngine() {
    super("RTPC", "davidp", "3.0");
  }

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

    if (kfstatus != null) {
      kfStatus = Boolean.parseBoolean(kfstatus);
    }

    String[] rtpcTables =
        new String[] {
          "/calibration/rtpc/time_offsets",
          "/calibration/rtpc/gain_balance",
          "/calibration/rtpc/time_parms",
          "/calibration/rtpc/recon_parms",
          "/calibration/rtpc/global_parms",
          "/geometry/rtpc/alignment"
        };

    requireConstants(Arrays.asList(rtpcTables));

    return true;
  }

  @Override
  public boolean processDataEvent(DataEvent event) {

    HitParameters params = new HitParameters();

    HitReader hitRead = new HitReader();
    hitRead.fetch_RTPCHits(event, simulation, cosmic); // boolean is for simulation

    List<Hit> hits;

    hits = hitRead.get_RTPCHits();

    if (hits == null || hits.size() == 0) {
      return true;
    }

    int runNo = 10;
    int eventNo = 777;
    double magfield;
    double magfieldfactor = 1;

    if (event.hasBank("RUN::config")) {
      DataBank bank = event.getBank("RUN::config");
      runNo = bank.getInt("run", 0);
      eventNo = bank.getInt("event", 0);
      magfieldfactor = bank.getFloat("solenoid", 0);
      if (runNo <= 0) {
        System.err.println("RTPCEngine:  got run <= 0 in RUN::config, skipping event.");
        return false;
      }
    }

    magfield = 50 * magfieldfactor;
    IndexedTable global_parms =
        this.getConstantsManager().getConstants(runNo, "/calibration/rtpc/global_parms");
    int hitsbound;
    hitsbound = (int) global_parms.getDoubleValue("MaxHitsEvent", 0, 0, 0);
    if (hits.size() > hitsbound) return true;

    // reading the central detectors z shift with respect to the FD
    IndexedTable rtpc_alignment =
        this.getConstantsManager().getConstants(runNo, "/geometry/rtpc/alignment");
    float rtpc_vz_shift;
    rtpc_vz_shift = (float) rtpc_alignment.getDoubleValue("deltaZ", 0, 0, 0);

    if (event.hasBank("RTPC::adc")) {
      params.init(this.getConstantsManager(), runNo);

      new SignalSimulation(hits, params, simulation); // boolean is for simulation
      // Sort Hits into Tracks at the Readout Pads
      new TrackFinder(params, cosmic);
      // Calculate Average Time of Hit Signals
      new TimeAverage(this.getConstantsManager(), params, runNo);
      // Disentangle Crossed Tracks
      new TrackDisentangler(params, disentangle, eventNo);
      // Reconstruct Hits in Drift Region
      new TrackHitReco(params, hits, cosmic, magfield);
      // Helix Fit Tracks to calculate Track Parameters
      new HelixFitTest(params, fitToBeamline, Math.abs(magfield), cosmic, chi2culling);

      RecoBankWriter writer = new RecoBankWriter();
      DataBank recoBank = writer.fillRTPCHitsBank(event, params);
      DataBank trackBank = writer.fillRTPCTrackBank(event, params, rtpc_vz_shift);

      event.appendBank(recoBank);
      event.appendBank(trackBank);

    } else {
      return true;
    }
    return true;
  }

  public static void main(String[] args) {

    double starttime = System.nanoTime();

    // String inputFile = "proton_100k_70-90MeV_fullZ.hipo";
    String inputFile = "proton_p80-200MeV_theta60-120_phi0-360_vz-20-20.hipo";
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
