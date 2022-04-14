package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.*;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.*;
import org.jlab.rec.rtpc.KalmanFilter.Integrator.DormandPrinceRK78;
import org.jlab.rec.rtpc.KalmanFilter.Integrator.Integrator;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material.NTP_Temperature;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.kStateGas;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.*;

public class KalmanFilter {

    public KalmanFilter(HitParameters params, DataEvent event) {

        int k = 0;
        HashMap<Integer, Hit> hitMap = new HashMap<>();

        HashMap<Integer, List<RecoHitVector>> recotrackmap = params.get_recotrackmap();

        try {
            FileWriter fWriter = new FileWriter("Hits.dat");
            for (int TID : recotrackmap.keySet()) {
                for (int i = 0; i < recotrackmap.get(TID).size(); i++) {
                    double x_rec = recotrackmap.get(TID).get(i).x();
                    double y_rec = recotrackmap.get(TID).get(i).y();
                    double z_rec = recotrackmap.get(TID).get(i).z();

                    hitMap.put(k, new Hit(Math.hypot(x_rec, y_rec), Math.atan2(y_rec, x_rec), z_rec));
                    k++;

                    fWriter.write("" + x_rec + ' ' + y_rec + ' ' + z_rec + '\n');


                }
            }
            fWriter.close();
        } catch (Exception ignored) {
        }


        try {
            FileWriter fWriter = new FileWriter("TrueHits.dat");
            DataBank particle = event.getBank("MC::Particle");
            double vx_mc = particle.getFloat("vx", 0);
            double vy_mc = particle.getFloat("vy", 0);
            double vz_mc = particle.getFloat("vz", 0);
            fWriter.write("" + vx_mc + ' ' + vy_mc + ' ' + vz_mc + '\n');


            DataBank mc = event.getBank("MC::True");
            int true_rows = mc.rows();
            for (int i = 0; i < true_rows; i++) {
                int PID = mc.getInt("pid", i);
                if (PID == 2212) {
                    double mc_x = mc.getFloat("avgX", i);
                    double mc_y = mc.getFloat("avgY", i);
                    double mc_z = mc.getFloat("avgZ", i);
                    fWriter.write("" + mc_x + ' ' + mc_y + ' ' + mc_z + '\n');
                }
            }
            fWriter.close();
        } catch (Exception ignored) {
        }

        // ----------------------------------------------------------------
        // ----------------------------------------------------------------
        // ----------------------------------------------------------------
        // ----------------------------------------------------------------

        double vz = 0;
        HashMap<Integer, FinalTrackInfo> finaltrackinfomap = params.get_finaltrackinfomap();
        for (int TID : finaltrackinfomap.keySet()) {
            FinalTrackInfo track = finaltrackinfomap.get(TID);
            vz = track.get_vz();
        }


        NistManager manager = NistManager.Instance();
        Material CO2 = manager.FindOrBuildMaterial("G4_CARBON_DIOXIDE", false, false);
        Material He = manager.FindOrBuildMaterial("G4_He", false, false);
        Material Kapton = manager.FindOrBuildMaterial("G4_KAPTON", false, false);
        Material Mylar = manager.FindOrBuildMaterial("G4_MYLAR", false, false);

        double He_prop = 0.8;
        double CO2_prop = 0.2;
        double He_dens = 0.0001664;
        double CO2_dens = 0.0018233;
        double He_fractionMass = (He_prop * He_dens) / (He_prop * He_dens + CO2_prop * CO2_dens);
        double CO2_fractionMass = (CO2_prop * CO2_dens) / (He_prop * He_dens + CO2_prop * CO2_dens);
        double bonusGas_Density = He_prop * He_dens + CO2_prop * CO2_dens;
        double density_BONuSGas_GEANT4 = bonusGas_Density * g / cm3;
        Material BONuSGas = new Material("BONuSGas", density_BONuSGas_GEANT4, 2, kStateGas, NTP_Temperature, PhysicalConstants.STP_Pressure);
        BONuSGas.AddMaterial(CO2, CO2_fractionMass);
        BONuSGas.AddMaterial(He, He_fractionMass);

        Isotope deuteron = new Isotope("deuteron", 1, 2, 2.0141018 * g / SystemOfUnits.mole, 0);
        Element deuterium = new Element("deuterium", "deuterium", 1);
        deuterium.AddIsotope(deuteron, 1);
        Material deuteriumGas = new Material("deuteriumGas", 0.000937 * g / cm3, 1, kStateGas, NTP_Temperature, 5.6 * SystemOfUnits.atmosphere);
        deuteriumGas.AddElement(deuterium, 1);


        BetheBlochModel model = new BetheBlochModel();
        Particle proton = new Proton();

        int NumberOfVariables = 6;

        double x0 = 0 * mm, y0 = 0 * mm, z0 = vz * mm, px0 = 100 * MeV, py0 = 10 * MeV, pz0 = -5 * MeV;
        double Bz = -0.745 * 5 * tesla;
        double[] B = {0.0, 0.0, Bz};


        Integrator integrator = new DormandPrinceRK78(proton, model, NumberOfVariables);
        double h = 0.2, hSmall = 1e-3, hBig = 1;


        Double[] temp = new Double[]{3.0, 3.063, 20.0, 20.006, 30.0, 30.004};
        ArrayList<Double> rArrayList = new ArrayList<>();
        Collections.addAll(rArrayList, temp);
        for (int i = 0; i < hitMap.size(); i++) {
            rArrayList.add(hitMap.get(i).get_r());
        }

        Material[] temp2 = {deuteriumGas, Kapton, BONuSGas, Mylar, BONuSGas, Mylar};
        ArrayList<Material> MaterialList = new ArrayList<>();
        Collections.addAll(MaterialList, temp2);
        for (int i = 0; i < hitMap.size(); i++) {
            MaterialList.add(BONuSGas);
        }

        Double[] temp3 = new Double[]{h, 0.01, hBig, hSmall, hBig, hSmall};
        ArrayList<Double> hArrayList = new ArrayList<>();
        Collections.addAll(hArrayList, temp3);
        for (int i = 0; i < hitMap.size(); i++) {
            hArrayList.add(h);
        }

        Hit[] temp4 = new Hit[]{null, null, null, null, null, null};
        ArrayList<Hit> hitArrayList = new ArrayList<>();
        Collections.addAll(hitArrayList, temp4);
        for (int i = 0; i < hitMap.size(); i++) {
            hitArrayList.add(hitMap.get(i));
        }


        // plotForwardBackwardPropagation(yIn, rArrayList, hArrayList, MaterialList, hitArrayList, integrator, B);

        RealMatrix processNoiseTarget = MatrixUtils.createRealMatrix(new double[][]{
                {10.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 10.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 10.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 100.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 100.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 100.0}
        }).scalarMultiply(1);

        RealMatrix processNoise = MatrixUtils.createRealMatrix(new double[][]{
                {0.1, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.1, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.1, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 4.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 4.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 4.0}
        }).scalarMultiply(0.2);


        RealVector stateEstimation = MatrixUtils.createRealVector(new double[]
                {x0, y0, z0, px0, py0, pz0}
        );

        RealMatrix errorCovariance = MatrixUtils.createRealMatrix(new double[][]{
                {4.0, 0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 4.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 100.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 400.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 400.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0, 400.0}
        });


        // System.out.println("hitMap = " + hitMap);

        saveForwardPropagation(stateEstimation, rArrayList, hArrayList, MaterialList, integrator, B, "first.dat");


        Kalman kalman = new Kalman(new ProcessModel(processNoise, processNoiseTarget, B), integrator, stateEstimation, errorCovariance);

        for (k = 0; k < 10; k++) {

            boolean targetNoise = true;

            for (int i = 0; i < rArrayList.size(); i++) {

                if (i > 1) targetNoise = false;

                kalman.newpredict(rArrayList.get(i), hArrayList.get(i), MaterialList.get(i), true, targetNoise);
                print(kalman.getStateEstimationVector(), "Predict");


                if (hitArrayList.get(i) != null) {
                    kalman.correct(hitArrayList.get(i).get_Vector(), hitArrayList.get(i).get_MeasurementNoise());
                    print(kalman.getStateEstimationVector(), "Correct");
                }
            }


            // System.out.println(" ------------------------------------- ");
            // System.out.println("           BackPropagation             ");
            // System.out.println(" ------------------------------------- ");


            for (int i = rArrayList.size() - 1; i > 0; i--) {

                if (hitArrayList.get(i) != null) {

                    kalman.newpredict(rArrayList.get(i - 1), hArrayList.get(i), MaterialList.get(i), false, targetNoise);
                    print(kalman.getStateEstimationVector(), "Predict");

                    kalman.correct(hitArrayList.get(i).get_Vector(), hitArrayList.get(i).get_MeasurementNoise());
                    print(kalman.getStateEstimationVector(), "Correct");

                } else {

                    if (i < 2) targetNoise = true;

                    kalman.newpredict(rArrayList.get(i - 1), hArrayList.get(i), MaterialList.get(i), false, targetNoise);
                    print(kalman.getStateEstimationVector(), "Predict");

                    if (i == 1) {


                        kalman.newpredict(0, hArrayList.get(0), deuteriumGas, false, targetNoise);
                        print(kalman.getStateEstimationVector(), "Predict");

                        RealVector z = new ArrayRealVector(new double[]{0, 0, vz});
                        RealMatrix measurementNoise = new Array2DRowRealMatrix(new double[][]{{4, 0, 0}, {0, 400, 0}, {0, 0, 400}});

                        kalman.correct(z, measurementNoise);
                        print(kalman.getStateEstimationVector(), "Correct");

                    }
                }


            }

            System.out.println("stateEstimation = " + kalman.getStateEstimationVector());

        }

        saveForwardPropagation(kalman.getStateEstimationVector(), rArrayList, hArrayList, MaterialList, integrator, B, "last.dat");

        // saveOutput(params, event, kalman.getStateEstimationVector());



        // saveForwardBackward(stateEstimation,rArrayList,hArrayList,MaterialList,hitArrayList,deuteriumGas,integrator,B,"forward.dat", "backward.dat");




    }

    void print(RealVector x, String name) {
        double r = Math.hypot(x.getEntry(0), x.getEntry(1));
        double p = Math.sqrt(x.getEntry(3) * x.getEntry(3)
                + x.getEntry(4) * x.getEntry(4) + x.getEntry(5) * x.getEntry(5));
        // System.out.print("stateEstimation_" + name + " : " + x);
        // System.out.println(" r = " + r + " p = " + p);

    }


    /******************************************************
     ____  _       _   _   _
     |  _ \| | ___ | |_| |_(_)_ __   __ _
     | |_) | |/ _ \| __| __| | '_ \ / _` |
     |  __/| | (_) | |_| |_| | | | | (_| |
     |_|___|_|\___/ \__|\__|_|_| |_|\__, |
     |  ___|   _ _ __   ___| |_(_) |___/_ __
     | |_ | | | | '_ \ / __| __| |/ _ \| '_ \
     |  _|| |_| | | | | (__| |_| | (_) | | | |
     |_|   \__,_|_| |_|\___|\__|_|\___/|_| |_|
     **********************************************************/

    RealVector f(RealVector x, double rMax, double h, double[] B, Integrator dormandPrinceRK78, Material material, boolean dir, FileWriter writer) throws IOException {
        double TotNbOfStep = 1e8;
        double s = 0, sEnd = 40;
        double[] yIn = x.toArray();


        writer.write("" + yIn[0] + ' ' + yIn[1] + ' ' + yIn[2] + '\n');

        for (int nStep = 0; nStep < TotNbOfStep; ++nStep) {

            double previous_r = Math.hypot(yIn[0], yIn[1]);

            if (dir) {
                dormandPrinceRK78.ForwardStepper(yIn, h, B, material);
            } else {
                dormandPrinceRK78.BackwardStepper(yIn, h, B, material);
            }


            s += h;

            double r = Math.hypot(yIn[0], yIn[1]);
            // System.out.println("r = " + r + " h = " + h + " Material = " + material.GetName() + " s = " + s);

            writer.write("" + yIn[0] + ' ' + yIn[1] + ' ' + yIn[2] + '\n');

            if (dir) {
                if (r >= rMax - 1.11 * h) h = h / 10;
                if (h < 10e-4) h = 10e-4;
                if (s >= sEnd || r >= rMax) break;

            } else {
                if (rMax == 0) {
                    if (r <= rMax + 1.11 * h) h = h / 10;
                    if (h < 10e-4) h = 10e-4;
                    if ((previous_r - r) < 0) break;
                } else {
                    if (r <= rMax + 1.11 * h) h = h / 10;
                    if (h < 10e-4) h = 10e-4;
                    if (s >= sEnd || r <= rMax) break;
                    if ((previous_r - r) < 0) break;
                }
            }
        }
        return new ArrayRealVector(yIn);


    }

    public RealVector newf(RealVector x, double rMax, double h, double[] B, Integrator dormandPrinceRK78, Material material, boolean dir, FileWriter writer) throws IOException {
        double TotNbOfStep = 1e8;
        double s = 0, sEnd = 500;
        double[] yIn = x.toArray();

        double r = Math.hypot(yIn[0], yIn[1]);
        if (rMax > 30.005 && dir) h = (rMax - r);
        if (rMax > 30.005 && !dir) h = (r - rMax);

        System.out.println("rMax = " + rMax + " r = " + r);

        writer.write("" + yIn[0] + ' ' + yIn[1] + ' ' + yIn[2] + '\n');

        for (int nStep = 0; nStep < TotNbOfStep; ++nStep) {

            double previous_r = Math.hypot(yIn[0], yIn[1]);
            double[] previous_yIn = Arrays.copyOf(yIn, 6);

            if (dir) {
                dormandPrinceRK78.ForwardStepper(yIn, h, B, material);
            } else {
                dormandPrinceRK78.BackwardStepper(yIn, h, B, material);
            }


            s += h;

            r = Math.hypot(yIn[0], yIn[1]);
            System.out.println("r = " + r + " h = " + h + " Material = " + material.GetName() + " s = " + s);

            writer.write("" + yIn[0] + ' ' + yIn[1] + ' ' + yIn[2] + '\n');

            if (dir) {
                if (r >= rMax - h) h = rMax - r;
                if (h < 1e-4) h = 1e-4;
                if (s >= sEnd || r >= rMax) break;

            } else {
                if (rMax == 0) {
                    if (r <= rMax + h) h = h / 10;
                    if (h < 1e-4) h = 1e-4;
                    if ((previous_r - r) < 0) {
                        return new ArrayRealVector(previous_yIn);
                    }
                } else {
                    if (r <= rMax + h) h = r - rMax;
                    if (h < 1e-4) h = 1e-4;
                    if (s >= sEnd || r <= rMax) break;
                    if ((previous_r - r) < 0) break;
                }
            }
        }

        return new ArrayRealVector(yIn);

    }

    void saveForwardBackward(RealVector stateEstimation, ArrayList<Double> rArrayList, ArrayList<Double> hArrayList, ArrayList<Material> MaterialList,
                             ArrayList<Hit> hitArrayList, Material deuteriumGas, Integrator integrator, double[] B, String ForfileName, String BackfileName) {

        try {
            FileWriter fWriter = new FileWriter(ForfileName);
            FileWriter bWriter = new FileWriter(BackfileName);

            for (int i = 0; i < rArrayList.size(); i++) {
                stateEstimation = newf(stateEstimation, rArrayList.get(i), hArrayList.get(i), B, integrator, MaterialList.get(i), true, fWriter);
            }

            for (int i = rArrayList.size() - 1; i > 0; i--) {

                if (hitArrayList.get(i) != null) {

                    stateEstimation = newf(stateEstimation, rArrayList.get(i - 1), hArrayList.get(i), B, integrator, MaterialList.get(i), false, bWriter);


                } else {


                    stateEstimation = newf(stateEstimation, rArrayList.get(i - 1), hArrayList.get(i), B, integrator, MaterialList.get(i), false, bWriter);

                    if (i == 1) {


                        stateEstimation = newf(stateEstimation, 0, hArrayList.get(0), B, integrator, deuteriumGas, false, bWriter);

                    }
                }


            }


            fWriter.close();
            bWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    void saveForwardPropagation(RealVector stateEstimation, ArrayList<Double> rArrayList, ArrayList<Double> hArrayList, ArrayList<Material> MaterialList, Integrator integrator, double[] B, String fileName) {

        try {
            FileWriter fWriter = new FileWriter(fileName);

            for (int i = 0; i < rArrayList.size(); i++) {
                stateEstimation = f(stateEstimation, rArrayList.get(i), hArrayList.get(i), B, integrator, MaterialList.get(i), true, fWriter);
            }

            fWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void saveOutput(HitParameters params, DataEvent event, RealVector x) {
        HashMap<Integer, FinalTrackInfo> finaltrackinfomap = params.get_finaltrackinfomap();
        double p_cf = 0;
        double phi_cf = 0;
        double theta_cf = 0;
        double vz_cf = 0;
        for (int TID : finaltrackinfomap.keySet()) {
            double px_cf = finaltrackinfomap.get(TID).get_px();
            double py_cf = finaltrackinfomap.get(TID).get_py();
            double pz_cf = finaltrackinfomap.get(TID).get_pz();
            p_cf = Math.sqrt(px_cf * px_cf + py_cf * py_cf + pz_cf * pz_cf);
            phi_cf = Math.toRadians(finaltrackinfomap.get(TID).get_phi());
            theta_cf = Math.toRadians(finaltrackinfomap.get(TID).get_theta());
            vz_cf = finaltrackinfomap.get(TID).get_vz();
        }


        double p_mc = 0;
        double phi_mc = 0;
        double theta_mc = 0;
        double vz_mc = 0;
        DataBank mc = event.getBank("MC::Particle");
        double px_mc = mc.getFloat("px", 0) * 1000;
        double py_mc = mc.getFloat("py", 0) * 1000;
        double pz_mc = mc.getFloat("pz", 0) * 1000;
        p_mc = Math.sqrt(px_mc * px_mc + py_mc * py_mc + pz_mc * pz_mc);
        phi_mc = Math.atan2(py_mc, px_mc);
        theta_mc = Math.atan2(Math.hypot(px_mc, py_mc), pz_mc);
        vz_mc = mc.getFloat("vz", 0);


        double p_kf = 0;
        double phi_kf = 0;
        double theta_kf = 0;
        double vz_kf = 0;
        double px_kf = x.getEntry(3);
        double py_kf = x.getEntry(4);
        double pz_kf = x.getEntry(5);
        p_kf = Math.sqrt(px_kf * px_kf + py_kf * py_kf + pz_kf * pz_kf);
        phi_kf = Math.atan2(py_kf, px_kf);
        theta_kf = Math.atan2(Math.hypot(px_kf, py_kf), pz_kf);
        vz_kf = x.getEntry(2);

        // System.out.println("p_mc = " + p_mc + " p_cf = " + p_cf + " p_kf = " + p_kf);
        // System.out.println("phi_mc = " + phi_mc + " phi_cf = " + phi_cf + " phi_kf = " + phi_kf);
        // System.out.println("theta_mc = " + theta_mc + " theta_cf = " + theta_cf + " theta_kf = " + theta_kf);
        // System.out.println("vz_mc = " + vz_mc + " vz_cf = " + vz_cf + " vz_kf = " + vz_kf);

        try {
            FileWriter fWriter = new FileWriter("output.dat", true);
            fWriter.write("" + p_cf + ' ' + p_kf + ' ' + p_mc + ' ');
            fWriter.write("" + phi_cf + ' ' + phi_kf + ' ' + phi_mc + ' ');
            fWriter.write("" + theta_cf + ' ' + theta_kf + ' ' + theta_mc + ' ');
            fWriter.write("" + vz_cf + ' ' + vz_kf + ' ' + vz_mc + '\n');
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
