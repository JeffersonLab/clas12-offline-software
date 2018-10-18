/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rec.cvt.services;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.detector.geant4.v2.SVT.SVTConstants;
import org.jlab.detector.geant4.v2.SVT.SVTStripFactory;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.bmt.CCDBConstantsLoader;

/**
 *
 * @author ziegler
 */
public class CVTEngine extends ReconstructionEngine {

    org.jlab.rec.cvt.svt.Geometry SVTGeom;
    org.jlab.rec.cvt.bmt.Geometry BMTGeom;
    SVTStripFactory svtIdealStripFactory;
    String clasDictionaryPath ;
    String variationName;
    public CVTEngine(String name) {
        super(name,"ziegler","2.0");
    }

    
    public void LoadTables(int run) {
         System.out.println(" ........................................ trying to connect to db ");
//        CCDBConstantsLoader.Load(new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default"));
        CCDBConstantsLoader.Load(new DatabaseConstantProvider(run, "default"));

        DatabaseConstantProvider cp = new DatabaseConstantProvider(run, "default");
//        DatabaseConstantProvider cp = new DatabaseConstantProvider( "sqlite:///clas12.sqlite", "default");
        
        String ccdbPath = "/geometry/cvt/svt/";
        cp.loadTable( ccdbPath +"svt");
        cp.loadTable( ccdbPath +"region");
        cp.loadTable( ccdbPath +"support");
        cp.loadTable( ccdbPath +"fiducial");
        cp.loadTable( ccdbPath +"material/box");
        cp.loadTable( ccdbPath +"material/tube");
        cp.loadTable( ccdbPath +"alignment");
        //if( loadAlignmentTables ) cp.loadTable( ccdbPath +"alignment/sector"); // possible future tables
        //if( loadAlignmentTables ) cp.loadTable( ccdbPath +"alignment/layer");

        SVTConstants.load( cp );
        
        SVTConstants.loadAlignmentShifts(cp);
        
        svtIdealStripFactory = new SVTStripFactory(cp, false);
        
        cp.disconnect();
    }
    @Override
    public boolean processDataEvent(DataEvent event) {
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

}
