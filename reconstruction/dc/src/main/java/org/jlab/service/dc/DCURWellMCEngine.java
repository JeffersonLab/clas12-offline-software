package org.jlab.service.dc;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import org.jlab.geom.prim.Point3D;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.mc.MCHit;
import org.jlab.rec.dc.mc.MCCross;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.trajectory.TrackVec;
import static org.jlab.service.dc.DCEngine.LOGGER;

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
        List<MCCross> uRWELLCrosses = new ArrayList<>();
        List<MCCross> dcCrosses = new ArrayList<>();

        List<Double> x_list_uRWELL = new ArrayList<>();
        List<Double> y_list_uRWELL = new ArrayList<>();
        List<Double> z_list_uRWELL = new ArrayList<>();
        List<Double> px_list_uRWELL = new ArrayList<>();
        List<Double> py_list_uRWELL = new ArrayList<>();
        List<Double> pz_list_uRWELL = new ArrayList<>();

        if (event.hasBank("MC::True")) {

            DataBank bank = event.getBank("MC::True");
            for (int row = 0; row < event.getBank("MC::True").rows(); row++) {
                // Get information of MC particle in the lab frame
                pars[0] = (double) bank.getFloat("avgX", row) / 10.;
                pars[1] = (double) bank.getFloat("avgY", row) / 10.;
                pars[2] = (double) bank.getFloat("avgZ", row) / 10.;
                pars[3] = (double) bank.getFloat("px", row) / 1000.;
                pars[4] = (double) bank.getFloat("py", row) / 1000.;
                pars[5] = (double) bank.getFloat("pz", row) / 1000.;
                int pid = bank.getInt("pid", row);
                int detector = bank.getInt("detector", row);

                if (detector == 6) {
                    sector = getSector(pars);

                    if(sector != -1){
                        TrackVec tv = new TrackVec();
                        double[] xpars = tv.TransformToTiltSectorFrame(sector, pars[0], pars[1], pars[2]);
                        double[] ppars = tv.TransformToTiltSectorFrame(sector, pars[3], pars[4], pars[5]);

                        //DC hits
                        if (detector == 6) {
                            if (charge == -999) {
                                charge = getCharge(pid);
                            }
                            MCHit mcHit = new MCHit(dcHits_id++, sector, xpars, ppars, ZMap, 1);
                            dcHits.add(mcHit);
                        }
                    }
                }
                
                if (detector == 23) {
                        x_list_uRWELL.add(pars[0]);
                        y_list_uRWELL.add(pars[1]);
                        z_list_uRWELL.add(pars[2]);
                        px_list_uRWELL.add(pars[3]);
                        py_list_uRWELL.add(pars[4]);
                        pz_list_uRWELL.add(pars[5]);
                 }
            }

            // uRWELL cross is set as average of all uRWELL hits
            int list_size_uRWELL = x_list_uRWELL.size();
            if (list_size_uRWELL != 0) {
                double x_sum_uRWELL = 0, y_sum_uRWELL = 0, z_sum_uRWELL = 0;
                double px_sum_uRWELL = 0, py_sum_uRWELL = 0, pz_sum_uRWELL = 0;
                for (int i = 0; i < x_list_uRWELL.size(); i++) {
                    x_sum_uRWELL += x_list_uRWELL.get(i);
                    y_sum_uRWELL += y_list_uRWELL.get(i);
                    z_sum_uRWELL += z_list_uRWELL.get(i);
                    px_sum_uRWELL += px_list_uRWELL.get(i);
                    py_sum_uRWELL += py_list_uRWELL.get(i);
                    pz_sum_uRWELL += pz_list_uRWELL.get(i);
                }

                double x_aver[] = {x_sum_uRWELL / list_size_uRWELL, y_sum_uRWELL / list_size_uRWELL, z_sum_uRWELL / list_size_uRWELL};
                double p_aver[] = {px_sum_uRWELL / list_size_uRWELL, py_sum_uRWELL / list_size_uRWELL, pz_sum_uRWELL / list_size_uRWELL};
                sector = getSector(x_aver);
                
                if(sector != -1){
                    TrackVec tv = new TrackVec();
                    double[] xpars = tv.TransformToTiltSectorFrame(sector, x_aver[0], x_aver[1], x_aver[2]);
                    double[] ppars = tv.TransformToTiltSectorFrame(sector, p_aver[0], p_aver[1], p_aver[2]);

                    MCCross mcCross = new MCCross(1, sector, 0, xpars, ppars, 1);
                    uRWELLCrosses.add(mcCross);
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
                            MCCross mcCross = new MCCross(i + 1, hit.getSector(), i + 1, xpars, ppars, status);
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
                            double ppars[] = {spars[3], spars[4], spars[5]};
                            MCCross mcCross = new MCCross(i + 1, hit.getSector(), i + 1, xpars, ppars, -1);
                            dcCrosses.add(mcCross);
                        }
                    }
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

    private int getSector(double[] pars) {
        double phi = Math.toDegrees(Math.atan2(pars[1], pars[0]));
        if(phi < 0) phi += 360;
        phi += 30;
        int sec = (int) phi/60 + 1;
        if(sec == 7) sec = 1;
        return sec;
    }    
}
