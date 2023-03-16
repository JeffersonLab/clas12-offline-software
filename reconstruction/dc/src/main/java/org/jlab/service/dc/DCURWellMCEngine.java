package org.jlab.service.dc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.jlab.geom.prim.Point3D;
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
public class DCURWellMCEngine extends DCEngine {

    private double[] pars = new double[6];
    private int charge = -999;
    private int sector = -1;
    private Map<Integer, Double> ZMap = new HashMap<Integer, Double>();    

    public DCURWellMCEngine() {
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
        List<MCHit> uRWellHits = new ArrayList<>();
        int uRWellHits_id = 0;
        List<MCCross> uRWellCrosses = new ArrayList<>();
        List<MCCross> dcCrosses = new ArrayList<>();
        
        if (event.hasBank("MC::True")) {
            DataBank bank = event.getBank("MC::True");
            for (int row = 0; row < event.getBank("MC::True").rows(); row++) {
                // Get information of MC particle in the lab frame
                int detector = bank.getInt("detector", row);
                if (detector == 6 || detector == 23) {
                    pars[0] = (double) bank.getFloat("avgX", row) / 10.;
                    pars[1] = (double) bank.getFloat("avgY", row) / 10.;
                    pars[2] = (double) bank.getFloat("avgZ", row) / 10.;
                    pars[3] = (double) bank.getFloat("px", row) / 1000.;
                    pars[4] = (double) bank.getFloat("py", row) / 1000.;
                    pars[5] = (double) bank.getFloat("pz", row) / 1000.;
                    charge = getCharge(bank.getInt("pid", row));
                    sector = getSector(pars);
                    double [] xparas = {pars[0], pars[1], pars[2]};
                    double [] pparas = {pars[3], pars[4], pars[5]};
                    double[] xpars = TransformToTiltSectorFrame(sector, xparas);
                    double[] ppars = TransformToTiltSectorFrame(sector, pparas);
                    if (sector != -1) {
                        //DC hits
                        if (detector == 6) {
                            MCHit mcHit = new MCHit(dcHits_id++, charge, sector, xpars, ppars, ZMap, 1);
                            dcHits.add(mcHit);
                        }

                        if (detector == 23) {
                            MCHit mcHit = new MCHit(uRWellHits_id++, charge, sector, xpars, ppars, 1);
                            uRWellHits.add(mcHit);
                        }
                    }
                }
            }

            // For uRWELL cross, transport from the first uRWELL hit        
            if (uRWellHits.size() > 0) {
                double z = Constants.URWELLLOCALZ;
                MCHit hit = uRWellHits.get(0);
                double p = hit.getP();
                Point3D dir = hit.getDir();
                Point3D point = hit.getPoint();
                if (point.z() < z) {
                    swim.SetSwimParameters(point.x(), point.y(), point.z(), p * dir.x(), p * dir.y(), p * dir.z(), charge);
                    double spars[] = swim.SwimToPlaneTiltSecSys(hit.getSector(), z);
                    if (spars != null) {
                        double xpars[] = {spars[0], spars[1], spars[2]};
                        double ppars[] = {spars[3], spars[4], spars[5]};
                        MCCross mcCross = new MCCross(1, charge, hit.getSector(), 0, xpars, ppars, -1);
                        uRWellCrosses.add(mcCross);
                    }
                } else {
                    swim.SetSwimParameters(point.x(), point.y(), point.z(), -p * dir.x(), -p * dir.y(), -p * dir.z(), charge);
                    double spars[] = swim.SwimToPlaneTiltSecSys(hit.getSector(), z);
                    if (spars != null) {
                        double xpars[] = {spars[0], spars[1], spars[2]};
                        double ppars[] = {-spars[3], -spars[4], -spars[5]};
                        MCCross mcCross = new MCCross(1, charge, hit.getSector(), 0, xpars, ppars, -1);
                        uRWellCrosses.add(mcCross);
                    }
                }


            }

            // For each DC cross, firstly get a hit closest to and before the cross, and then propagate to the cross
            if (!dcHits.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    double z = Constants.getInstance().dcDetector.getRegionMidpoint(i).z;
                    int hit_index = 0;
                    for (int j = 0; j < dcHits.size(); j++) {
                        if (z - dcHits.get(j).getPoint().z() < 0) {
                            hit_index = j - 1;
                            break;
                        }
                    }
                    if (hit_index > 0) {
                        MCHit hit = dcHits.get(hit_index);
                        double p = hit.getP();
                        Point3D dir = hit.getDir();
                        Point3D point = hit.getPoint();
                        swim.SetSwimParameters(point.x(), point.y(), point.z(), p * dir.x(), p * dir.y(), p * dir.z(), charge);
                        double spars[] = swim.SwimToPlaneTiltSecSys(hit.getSector(), z);
                        if (spars != null) {
                            double xpars[] = {spars[0], spars[1], spars[2]};
                            double ppars[] = {spars[3], spars[4], spars[5]};
                            int status = 1;
                            if (hit_index == dcHits.size() - 1) {
                                status = -1;
                            }
                            MCCross mcCross = new MCCross(i + 1, charge, hit.getSector(), i + 1, xpars, ppars, status);
                            dcCrosses.add(mcCross);
                        }
                    } else { // For very special case, no hit is ahead of the cross, then the first hit behind the cross is picked
                        MCHit hit = dcHits.get(0);
                        double p = hit.getP();
                        Point3D dir = hit.getDir();
                        Point3D point = hit.getPoint();
                        swim.SetSwimParameters(point.x(), point.y(), point.z(), -p * dir.x(), -p * dir.y(), -p * dir.z(), charge);
                        double spars[] = swim.SwimToPlaneTiltSecSys(hit.getSector(), z);
                        if (spars != null) {
                            double xpars[] = {spars[0], spars[1], spars[2]};
                            double ppars[] = {-spars[3], -spars[4], -spars[5]};
                            MCCross mcCross = new MCCross(i + 1, charge, hit.getSector(), i + 1, xpars, ppars, -1);
                            dcCrosses.add(mcCross);
                        }
                    }
                }
            }

            event.appendBank(writer.fillMCURWellHitsBank(event, uRWellHits));
            event.appendBank(writer.fillMCDCHitsBank(event, dcHits));
            event.appendBank(writer.fillMCURWellCrossesBank(event, uRWellCrosses));
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

    private int getSector(double[] pars) {
        double phi = Math.toDegrees(Math.atan2(pars[1], pars[0]));
        if (phi < 0) {
            phi += 360;
        }
        phi += 30;
        int sec = (int) phi / 60 + 1;
        if (sec == 7) {
            sec = 1;
        }
        return sec;
    }
    
    private double[] TransformToTiltSectorFrame(int sector, double[] pos) {
        double x = pos[0];
        double y = pos[1];
        double Z = pos[2];

        double X = x * Constants.COSSECTORNEG60[sector - 1] - y * Constants.SINSECTORNEG60[sector - 1];
        double ry = x * Constants.SINSECTORNEG60[sector - 1] + y * Constants.COSSECTORNEG60[sector - 1];        

        double rz = X * Constants.SIN25 + Z * Constants.COS25 ;
        double rx = X * Constants.COS25 - Z * Constants.SIN25 ;

        double[] r = {rx, ry, rz};       
        return r;
    }
}
