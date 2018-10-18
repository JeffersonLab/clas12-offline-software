/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.Constants;
import org.jlab.rec.cvt.banks.RecoBankWriter;
import org.jlab.rec.cvt.track.StraightTrack;
import org.jlab.rec.cvt.track.Track;

/**
 *
 * @author ziegler
 */
public class CVTReconstruction extends CVTEngine {
    
    public CVTReconstruction() {
        super("CVT");
    }
    private AtomicInteger Run = new AtomicInteger(0);
    private int newRun = 0;
    CVTRecHandler recHandler ;
    @Override
    public boolean init() {
        SVTGeom = new org.jlab.rec.cvt.svt.Geometry();
        BMTGeom = new org.jlab.rec.cvt.bmt.Geometry();
        super.LoadTables(11);
        SVTGeom.setSvtStripFactory(svtIdealStripFactory);
        recHandler = new CVTRecHandler();
        return true;
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return true;
        }

       DataBank bank = event.getBank("RUN::config");
        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
       if (newRun == 0)
           return true;

       if (Run.get() == 0 || (Run.get() != 0 && Run.get() != newRun)) {
           
           Constants.Load((Math.abs(bank.getFloat("solenoid", 0))<0.0001), false, (double) bank.getFloat("solenoid", 0));
           Run.set(newRun);
           
       }
       Swim swimmer = new Swim();

        RecoBankWriter rbc = new RecoBankWriter();

        if (recHandler.loadClusters(event, SVTGeom,BMTGeom) == false) {
            return true;
        };

        recHandler.loadCrosses(SVTGeom);

        if (Constants.isCosmicsData()==true) {
            List<StraightTrack> cosmics = recHandler.cosmicsTracking(SVTGeom, BMTGeom);
            rbc.appendCVTCosmicsBanks(event, recHandler.getSVThits(), recHandler.getBMThits(),
                    recHandler.getSVTclusters(), recHandler.getBMTclusters(),
                    recHandler.getCrosses(), cosmics);
        } else { 
            List<Track> trks = recHandler.beamTracking(swimmer,SVTGeom, BMTGeom);
            rbc.appendCVTBanks(event, recHandler.getSVThits(), recHandler.getBMThits(),
                    recHandler.getSVTclusters(), recHandler.getBMTclusters(),
                    recHandler.getCrosses(), trks);
        } 
        return true;
    }
}
