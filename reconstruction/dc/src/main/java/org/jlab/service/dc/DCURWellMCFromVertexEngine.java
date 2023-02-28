package org.jlab.service.dc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.mc.MCHit;
import org.jlab.rec.dc.mc.MCCross;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.RecoBankWriter;

/**
 * @author Tongtong Cao
 *
 */
public class DCURWellMCFromVertexEngine extends DCEngine {

    final private double[] zDCRegions = {210, 330, 460};
    private double[] pars = new double[6];
    private int charge = -999;
    private int sector = -1;
    private Map<Integer, Double> ZMap = new HashMap<Integer, Double>();

    public DCURWellMCFromVertexEngine() {
        super("MC");
        this.getBanks().init("MC", "", "MC");

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                ZMap.put(i * 6 + j, Constants.getInstance().dcDetector.getLayerMidpoint(i, j).z);
            }
        }
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        int run = this.getRun(event);
        if (run == 0) {
            return true;
        }

        RecoBankWriter writer = new RecoBankWriter(this.getBanks());
        Swim swim = new Swim();

        List<MCHit> dcHits = new ArrayList<>();
        int dcHits_id = 0;
        List<MCCross> uRWELLCrosses = new ArrayList<>();
        List<MCCross> dcCrosses = new ArrayList<>();

        if (event.hasBank("MC::Particle")) {

            DataBank bank = event.getBank("MC::Particle");
            
                // Get information of MC particle in the lab frame
                pars[0] = (double) bank.getFloat("vx", 0);
                pars[1] = (double) bank.getFloat("vy", 0);
                pars[2] = (double) bank.getFloat("vz", 0);
                pars[3] = (double) bank.getFloat("px", 0);
                pars[4] = (double) bank.getFloat("py", 0);
                pars[5] = (double) bank.getFloat("pz", 0);
                charge = getCharge(bank.getInt("pid", 0));

                sector = getSector(pars, swim, charge);
                if (sector != -1) {                    
                    // DC hits
                    double [] xparas = {pars[0], pars[1], pars[2]};
                    double [] pparas = {pars[3], pars[4], pars[5]};
                    double[] xpars = TransformToTiltSectorFrame(sector, xparas);
                    double[] ppars = TransformToTiltSectorFrame(sector, pparas);
                    swim.SetSwimParameters(xpars[0], xpars[1], xpars[2], ppars[0], ppars[1], ppars[2], charge);
                    for (int key : ZMap.keySet()) {
                        double paras[] = swim.SwimToPlaneTiltSecSys(sector, ZMap.get(key));
                        MCHit dcHit = new MCHit(key + 1, sector, key, paras, 1);
                        if (key != ZMap.keySet().size() - 1) {
                            swim.SetSwimParameters(paras[0], paras[1], paras[2], paras[3], paras[4], paras[5], charge);
                        }
                        dcHits.add(dcHit);
                    }
                    
                    // uRWell cross
                    swim.SetSwimParameters(xpars[0], xpars[1], xpars[2], ppars[0], ppars[1], ppars[2], charge);
                    double paras[] = swim.SwimToPlaneTiltSecSys(sector, Constants.URWELLLOCALZ);
                    MCCross uRWELLCross = new MCCross(1, sector, 0, paras, 1);
                    uRWELLCrosses.add(uRWELLCross);
                    
                    // DC crosses
                    for(int i = 0; i < 3; i++){
                        swim.SetSwimParameters(paras[0], paras[1], paras[2], paras[3], paras[4], paras[5], charge);
                        paras = swim.SwimToPlaneTiltSecSys(sector, Constants.getInstance().dcDetector.getRegionMidpoint(i).z);
                        MCCross dcCross = new MCCross(i + 1, sector, i + 1, paras, 1);
                        dcCrosses.add(dcCross);
                    }

                }
            
            event.appendBank(writer.fillMCDCHitsBank(event, dcHits));
            event.appendBank(writer.fillMCURWellCrossesBank(event, uRWELLCrosses));
            event.appendBank(writer.fillMCDCCrossesBank(event, dcCrosses));
        }

        return true;
    }
    // Get charge of MC particle through PID

    private int getCharge(int pid) {
        if ((int) (pid / 100) == 0) {
            return -(int) Math.signum(pid);
        } else {
            return (int) Math.signum(pid);
        }
    }

    private int getSector(double[] pars, Swim swim, int charge) {
        swim.SetSwimParameters(pars[0], pars[1], pars[2], pars[3], pars[4], pars[5], charge);
        double paras[] = new double[8];
        double phi;
        int sec[] = new int[3];
        for (int i = 0; i < 3; i++) {
            paras = swim.SwimToPlaneLab(zDCRegions[i]);
            phi = Math.toDegrees(Math.atan2(paras[1], paras[0]));
            if (phi < 0) {
                phi += 360;
            }
            phi += 30;
            sec[i] = (int) phi / 60 + 1;
            if (sec[i] == 7) {
                sec[i] = 1;
            }
            if (i != 2) {
                swim.SetSwimParameters(paras[0], paras[1], paras[2], paras[3], paras[4], paras[5], charge);
            }
        }

        // In the forward tracking regions, tracks are required to be in the same sector.
        if (sec[0] == sec[1] && sec[0] == sec[2]) {
            return sec[0];
        } else {
            return -1;
        }
    }
    
    private double[] TransformToTiltSectorFrame(int sector, double[] pos) {

        double cos_tilt = Math.cos(Math.toRadians(25.));
        double sin_tilt = Math.sin(Math.toRadians(25.));
        double rad60 = Math.toRadians(60.);

        double x = pos[0];
        double y = pos[1];
        double Z = pos[2];

        int t = -1;
        double X = x * Math.cos((sector - 1) * t * rad60) - y * Math.sin((sector - 1) * t * rad60);
        double ry = x * Math.sin((sector - 1) * t * rad60) + y * Math.cos((sector - 1) * t * rad60);

        t = 1;
        double rz = (double) t * X * sin_tilt + Z * cos_tilt;
        double rx = X * cos_tilt - (double) t * Z * sin_tilt;

        double[] r = {rx, ry, rz};
        return r;
    }
}
