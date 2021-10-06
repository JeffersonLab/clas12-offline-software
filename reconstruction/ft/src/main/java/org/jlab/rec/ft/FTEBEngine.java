package org.jlab.rec.ft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import org.jlab.clas.detector.DetectorData;
import org.jlab.clas.detector.DetectorEvent;

import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.geom.prim.Vector3D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;
import org.jlab.rec.ft.cal.FTCALEngine;
import org.jlab.rec.ft.hodo.FTHODOEngine;

public class FTEBEngine extends ReconstructionEngine {


    FTEventBuilder reco;
    int Run = -1;
    double Solenoid;

    public FTEBEngine() {
        super("FTEB", "devita", "3.0");
    }

    @Override
    public boolean init() {
        reco = new FTEventBuilder();
        reco.debugMode = 0;
        String[] tables = new String[]{
            "/calibration/ft/ftcal/cluster",
            "/calibration/ft/ftcal/thetacorr",
            "/calibration/ft/ftcal/phicorr",
            "/geometry/target"
        };
        requireConstants(Arrays.asList(tables));
        this.getConstantsManager().setVariation("default");

        this.registerOutputBank("FT::particles");

        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        List<FTParticle> FTparticles = new ArrayList<FTParticle>();
        List<FTResponse> FTresponses = new ArrayList<FTResponse>();

        int run = this.setRunConditionsParameters(event);
        if (run>=0) {
            reco.init(this.getSolenoid());
            FTresponses = reco.addResponses(event, this.getConstantsManager(), run);
            FTparticles = reco.initFTparticles(FTresponses, this.getConstantsManager(), run);
            reco.matchToHODO(FTresponses, FTparticles);
            reco.correctDirection(FTparticles, this.getConstantsManager(), run);
            reco.writeBanks(event, FTparticles);
        }
        return true;
    }

    public int setRunConditionsParameters(DataEvent event) {
        int run = -1;
        if (event.hasBank("RUN::config") == false) {
            System.err.println("RUN CONDITIONS NOT READ!");
        }
        else {
            double fieldScale = 0;

            boolean isMC = false;
            boolean isCosmics = false;

            if (event instanceof EvioDataEvent) {
                EvioDataBank bank = (EvioDataBank) event.getBank("RUN::config");
                if (bank.getByte("Type",0) == 0) {
                    isMC = true;
                }
                if (bank.getByte("Mode",0)== 1) {
                    isCosmics = true;
                }
                run = bank.getInt("Run",0);
                fieldScale = bank.getFloat("Solenoid")[0];
            } else {
                DataBank bank = event.getBank("RUN::config");
                if (bank.getByte("type",0) == 0) {
                    isMC = true;
                }
                if (bank.getByte("mode",0)== 1) {
                    isCosmics = true;
                }
                run = bank.getInt("run",0);
                fieldScale = bank.getFloat("solenoid",0);
            }
            this.setSolenoid(fieldScale);
        }
        return run;
    }

    public int getRun() {
        return Run;
    }

    public void setRun(int run) {
        Run = run;
    }

    public double getSolenoid() {
        return Solenoid;
    }

    public void setSolenoid(double Solenoid) {
        this.Solenoid = Solenoid;
    }

    public static void main(String arg[]){
        FTCALEngine cal = new FTCALEngine();
        cal.init();
        FTHODOEngine hodo = new FTHODOEngine();
        hodo.init();
        FTEBEngine en = new FTEBEngine();
        en.init();
//		String input = "/Users/devita/Work/clas12/simulations/tests/detectors/clas12/ft/elec_nofield_header.evio";
//		EvioSource  reader = new EvioSource();
        String input = "/Users/devita/Work/clas12/simulations/clas12Tags/4.4.0/out.hipo";
        HipoDataSource reader = new HipoDataSource();
        reader.open(input);

        // initialize histos
        H1F h1 = new H1F("Cluster Energy", 100, 0., 8.);
        h1.setOptStat(Integer.parseInt("1111"));
        h1.setTitleX("Cluster Energy (GeV)");
        H1F h2 = new H1F("Energy Resolution", 100, -1, 1);
        h2.setOptStat(Integer.parseInt("1111"));
        h2.setTitleX("Energy Resolution(GeV)");
        H2F h3 = new H2F("Theta Resolution", 100, 0, 5, 100, -2, 2);
        h3.setTitleX("Energy (GeV)");
        h3.setTitleY("Theta Resolution(deg)");
        H2F h4 = new H2F("Phi Resolution", 100, 0, 5, 100, -10, 10);
        h4.setTitleX("Energy (GeV)");
        h4.setTitleY("Phi Resolution(deg)");
        H1F h5 = new H1F("Time Resolution", 100, -2, 2);
        h5.setOptStat(Integer.parseInt("1111"));
        h5.setTitleX("Time Resolution(ns)");
        H2F h6 = new H2F("Cluster Energy", 24, -180., 180., 24, -180., 180.);
        h6.setTitleX("x (cm)");
        h6.setTitleY("y (cm)");
        H2F h7 = new H2F("N. Clusters", 24, -180., 180., 24, -180., 180.);
        h7.setTitleX("x (cm)");
        h7.setTitleY("y (cm)");
        H2F h8 = new H2F("Cluster Energy", 100, 0., 9., 100, -0.5, 0.5);
        h8.setTitleX("E (GeV)");
        h8.setTitleY("Energy Resolution(GeV)");
        H2F h9 = new H2F("Cluster Energy", 100, 2., 5., 100, -0.5, 0.5);
        h9.setTitleX("#theta");
        h9.setTitleY("Energy Resolution(GeV)");
        H2F h10 = new H2F("Cluster Energy", 100, -180., 180., 100, -0.5, 0.5);
        h10.setTitleX("#phi");
        h10.setTitleY("Energy Resolution(GeV)");
        
        while (reader.hasEvent()) {
            DataEvent event = (DataEvent) reader.getNextEvent();
            cal.processDataEvent(event);
            hodo.processDataEvent(event);
            en.processDataEvent(event);

            if (event instanceof EvioDataEvent) {
                GenericKinematicFitter fitter = new GenericKinematicFitter(11);
                PhysicsEvent gen = fitter.getGeneratedEvent((EvioDataEvent) event);
                if (event.hasBank("FTRec::tracks")) {
                    DataBank bank = event.getBank("FTRec::tracks");
                    int nrows = bank.rows();
                    for (int i = 0; i < nrows; i++) {
                        h1.fill(bank.getDouble("Energy", i));
                        h2.fill(bank.getDouble("Energy", i) - gen.getParticle("[11]").vector().p());
                        Vector3D part = new Vector3D(bank.getDouble("Cx", i), bank.getDouble("Cy", i), bank.getDouble("Cz", i));
                        h3.fill(gen.getParticle("[11]").vector().p(),Math.toDegrees(part.theta() - gen.getParticle("[11]").theta()));
                        h4.fill(gen.getParticle("[11]").vector().p(),Math.toDegrees(part.phi() - gen.getParticle("[11]").phi()));
                        h5.fill(bank.getDouble("Time", i));
                        h6.fill(bank.getDouble("Energy", i), bank.getDouble("Energy", i) - gen.getParticle("[11]").vector().p());
                    }
                }
            } else {
                DetectorEvent detectorEvent = DetectorData.readDetectorEvent(event);
                PhysicsEvent gen = detectorEvent.getGeneratedEvent();
                     if (event.hasBank("FT::particles")) {
                    DataBank bank = event.getBank("FT::particles");
                    int nrows = bank.rows();
                    for (int i = 0; i < nrows; i++) {
                        if(bank.getByte("charge", i)==-1) {
                            h1.fill(bank.getFloat("energy", i));
                            h2.fill(bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                            Vector3D part = new Vector3D(bank.getFloat("cx", i), bank.getFloat("cy", i), bank.getFloat("cz", i));
                            h3.fill(gen.getGeneratedParticle(0).vector().p(),Math.toDegrees(part.theta() - gen.getGeneratedParticle(0).theta()));
                            h4.fill(gen.getGeneratedParticle(0).vector().p(),Math.toDegrees(part.phi() - gen.getGeneratedParticle(0).phi()));
                            h5.fill(bank.getFloat("time", i) - 124.25);
                            h6.fill(bank.getFloat("cx", i) * FTCALConstantsLoader.CRYS_ZPOS, bank.getFloat("cy", i) * FTCALConstantsLoader.CRYS_ZPOS, bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                            h7.fill(bank.getFloat("cx", i) * FTCALConstantsLoader.CRYS_ZPOS, bank.getFloat("cy", i) * FTCALConstantsLoader.CRYS_ZPOS);
                            h8.fill(gen.getGeneratedParticle(0).vector().p(), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                            h9.fill(Math.toDegrees(gen.getGeneratedParticle(0).theta()), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                            h10.fill(Math.toDegrees(gen.getGeneratedParticle(0).phi()), bank.getFloat("energy", i) - gen.getGeneratedParticle(0).vector().p());
                        }
                    }
                }

            }
        }
        JFrame frame = new JFrame("FT Reconstruction");
        frame.setSize(1200, 800);
        EmbeddedCanvas canvas = new EmbeddedCanvas();
        canvas.divide(3, 3);
        canvas.cd(0);
        canvas.draw(h1);
        canvas.cd(1);
        canvas.draw(h2);
        canvas.cd(2);
        canvas.draw(h3);
        canvas.cd(3);
        canvas.draw(h4);
        canvas.cd(4);
        canvas.draw(h5);
        for (int i = 0; i < h6.getDataBufferSize(); i++) {
            float meanE = h6.getDataBufferBin(i);
            float nE = h7.getDataBufferBin(i);
            if (nE > 0) {
                h6.setDataBufferBin(i, meanE / nE);
            }
        }
        canvas.cd(5);
        canvas.draw(h6);
        canvas.cd(6);
        canvas.draw(h8);
        canvas.cd(7);
        canvas.draw(h9);
        canvas.cd(8);
        canvas.draw(h10);
        frame.add(canvas);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
