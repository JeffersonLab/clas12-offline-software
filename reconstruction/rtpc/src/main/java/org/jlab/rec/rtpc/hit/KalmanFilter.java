package org.jlab.rec.rtpc.hit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;
import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


public class KalmanFilter {

    boolean debug = false;

    Swim swim = new Swim();
    HashMap<Integer, List<RecoHitVector>> recotrackmap;
    HashMap<Integer, FinalTrackInfo> finaltrackinfomap;
    HashMap<Integer, KalmanFilterTrackInfo> kftrackinfomap = new HashMap<>();

    ArrayList<SimpleMatrix> measVectorList = new ArrayList<>();
    ArrayList<StateVector> stateVectorPredictList = new ArrayList<>();
    ArrayList<SimpleMatrix> stateCovarianceMatrixPredictList = new ArrayList<>();
    ArrayList<StateVector> stateVectorUpdateList = new ArrayList<>();
    ArrayList<SimpleMatrix> stateCovarianceMatrixUpdateList = new ArrayList<>();
    ArrayList<StateVector> backStateVectorPredictList;
    ArrayList<SimpleMatrix> backCovarianceMatrixPredictList;
    ArrayList<StateVector> backStateVectorUpdateList;
    ArrayList<SimpleMatrix> backCovarianceMatrixUpdateList;

    public KalmanFilter(HitParameters params, DataEvent event, int count) throws Exception {
        recotrackmap = params.get_recotrackmap();
        finaltrackinfomap = params.get_finaltrackinfomap();

        for (int TID : finaltrackinfomap.keySet()) {
            if (!passCuts(TID, event))
                continue;

            MeasurementsList(TID);
            removeMultiHits();
            StateVectorsList(TID);

            int ITMAX = 10;

            for (int iter = 0; iter < ITMAX; iter++) {

                forwardPropagationFirstStep();
                forwardUpdate(0);
                for (int k = 1; k < measVectorList.size() - 1; k++) {
                    forwardPropagation(k);
                    forwardUpdate(k);
                }

                fillBackwardLists();

                for (int k = measVectorList.size() - 1; k > 1; k--) {
                    backwardPropagation(k);
                    backwardUpdate(k);
                }
                backwardPropagationFirstStep();
                clearAndFillLists();
            }
            writeData(TID, event);
            SimpleMatrix a_plus = stateVectorUpdateList.get(0).a.copy();
            double phii = stateVectorUpdateList.get(0).phi;
            SimpleMatrix m = measVectorList.get(0);
            double phi0 = a_plus.get(1, 0);
            double kappa = a_plus.get(2, 0);
            double dz = a_plus.get(3, 0);
            double tanL = a_plus.get(4, 0);
            double alpha = computeAlpha(m);
            double vz = m.get(2, 0) + dz - alpha / kappa * tanL * phii;
            double px = -1 / kappa * Math.sin(phi0 + phii);
            double py = 1 / kappa * Math.cos(phi0 + phii);
            double pz = 1 / kappa * tanL;
            double p = Math.sqrt(px * px + py * py + pz * pz);
            double phi = Math.atan2(py, px);
            double theta = Math.acos((pz) / (p));
            KalmanFilterTrackInfo kalmanFilterTrackInfo = new KalmanFilterTrackInfo(px, py, pz, vz, theta, phi);
            kftrackinfomap.put(TID, kalmanFilterTrackInfo);
            clearLists();
        }
        params.set_kftrackinfomap(kftrackinfomap);
    }

    /**
     * Propagate the state vector between the measurement point k to the measurement
     * k-1.
     * 
     * @param k
     * @throws Exception
     */
    private void backwardPropagation(int k) throws Exception {
        StateVector stateVector_start = backStateVectorUpdateList.get(k);
        SimpleMatrix P_plus = backCovarianceMatrixUpdateList.get(k);
        SimpleMatrix m0 = measVectorList.get(k);
        SimpleMatrix mk = measVectorList.get(k - 1);

        if (Math.abs(stateVector_start.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (Math.abs(stateVector_start.a.get(3, 0)) > 20)
            throw new Exception("dz is too big !!");

        if (debug)
            stateVector_start.print("start : ", m0);

        StateVector stateVector_end = backward_f3(stateVector_start, m0, mk, false);

        if (Math.abs(stateVector_end.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (Math.abs(stateVector_start.a.get(3, 0)) > 20)
            throw new Exception("dz is too big !!");

        if (debug)
            stateVector_end.print("end : ", mk);

        SimpleMatrix F = backward_F3(stateVector_start, m0, mk);
        SimpleMatrix Q = Q(stateVector_end, "HeCO2");
        Q = F.mult(Q).mult(F.transpose());

        SimpleMatrix P_minus = (F.mult(P_plus).mult(F.transpose())).plus(Q);

        backStateVectorPredictList.set(k - 1, stateVector_end);
        backCovarianceMatrixPredictList.set(k - 1, P_minus);
    }

    /**
     * Update the state vector with the information of the measurement k-1
     * 
     * @param k
     * @throws Exception
     */
    private void backwardUpdate(int k) throws Exception {
        StateVector stateVector_minus = backStateVectorPredictList.get(k - 1);
        SimpleMatrix P_minus = backCovarianceMatrixPredictList.get(k - 1);
        SimpleMatrix m = measVectorList.get(k - 1);
        SimpleMatrix h = h(stateVector_minus, m);
        SimpleMatrix y = m.minus(h);
        
        y.set(1, 0, modPI(y.get(1, 0)));
        

        

        SimpleMatrix H = H(stateVector_minus, m);
        SimpleMatrix R = R(m);
        SimpleMatrix S = (H.mult(P_minus).mult(H.transpose())).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose()).mult(S.invert());

        if (debug){
            stateVector_minus.print("minus : ", m);
            System.out.println("h: " + h);
            System.out.println("m: " + m);
            System.out.println("y: " + y);
            System.out.println("Ky: " + K.mult(y));
        }

        SimpleMatrix a_plus = stateVector_minus.a.plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        StateVector stateVector_plus = new StateVector(a_plus, stateVector_minus.phi);

        if (Math.abs(stateVector_plus.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (debug)
            stateVector_plus.print("update : ", m);

        backStateVectorUpdateList.set(k - 1, stateVector_plus);
        backCovarianceMatrixUpdateList.set(k - 1, P_plus);
    }

    /**
     * Propagate the state vector between the 1th measurement point to the beam
     * line.
     * 
     * @throws Exception
     */
    private void backwardPropagationFirstStep() throws Exception {
        StateVector stateVector_start = backStateVectorUpdateList.get(1);
        SimpleMatrix P_plus = backCovarianceMatrixUpdateList.get(1);
        SimpleMatrix m0 = measVectorList.get(0);
        SimpleMatrix m1 = measVectorList.get(1);

        double r_cathode = 30.00;   
        String material_cathode = "HeCO2";
        StateVector stateVector_cathode = backward_f2(stateVector_start, m1, r_cathode, material_cathode, debug);
        SimpleMatrix F_cathode = backward_F2(stateVector_start, m1, r_cathode, material_cathode);
        SimpleMatrix Q_cathode = Q(stateVector_cathode, material_cathode);
        Q_cathode = F_cathode.mult(Q_cathode).mult(F_cathode.transpose());

        // Flow
        double r_flow = 29.994;
        String material_flow = "Mylar";
        StateVector stateVector_flow = backward_f2(stateVector_cathode, m1, r_flow, material_flow, debug);
        SimpleMatrix F_flow = backward_F2(stateVector_cathode, m1, r_flow, material_flow);
        SimpleMatrix Q_flow = Q(stateVector_flow, material_flow);
        Q_flow = F_flow.mult(Q_flow).mult(F_flow.transpose());

        // Ground foil
        double r_foil = 20.00;
        String material_foil = "HeCO2";
        StateVector stateVector_foil = backward_f2(stateVector_flow, m1, r_foil, material_foil, debug);
        SimpleMatrix F_foil = backward_F2(stateVector_flow, m1, r_foil, material_foil);
        SimpleMatrix Q_foil = Q(stateVector_foil, material_foil);
        Q_foil = F_foil.mult(Q_foil).mult(F_foil.transpose());

        // Buffer
        double r_buffer = 19.994;
        String material_buffer = "Mylar";
        StateVector stateVector_buffer = backward_f2(stateVector_foil, m1, r_buffer, material_buffer, debug);
        SimpleMatrix F_buffer = backward_F2(stateVector_foil, m1, r_buffer, material_buffer);
        SimpleMatrix Q_buffer = Q(stateVector_buffer, material_buffer);
        Q_buffer = F_buffer.mult(Q_buffer).mult(F_buffer.transpose());

        // Wall
        double r_wall = 3.055;
        String material_wall = "HeCO2";
        StateVector stateVector_wall = backward_f2(stateVector_buffer, m1, r_wall, material_wall, debug);
        SimpleMatrix F_wall = backward_F2(stateVector_buffer, m1, r_wall, material_wall);
        SimpleMatrix Q_wall = Q(stateVector_wall, material_wall);
        Q_wall = F_wall.mult(Q_wall).mult(F_wall.transpose());

        // Target
        double r_target = 3.0;
        String material_target = "Kapton";
        StateVector stateVector_target = backward_f2(stateVector_wall, m1, r_target, material_target, debug);
        SimpleMatrix F_target = backward_F2(stateVector_wall, m1, r_target, material_target);
        SimpleMatrix Q_target = Q(stateVector_target, material_target);
        Q_target = F_target.mult(Q_target).mult(F_target.transpose());

        // BeamLine
        StateVector beam = backward_f1(stateVector_target, m1, m0, debug);
        SimpleMatrix F_beam = backward_F1(stateVector_target, m1, m0);
        SimpleMatrix Q_beam = Q(beam, "Deuterium");
        Q_beam = F_beam.mult(Q_beam).mult(F_beam.transpose());

        if (debug) System.out.println("F_beam: " + F_beam);

        if (Math.abs(beam.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (debug)
            beam.print("start : ", m0);

        // SimpleMatrix F = F_cathode.mult(F_flow).mult(F_foil).mult(F_buffer).mult(F_wall).mult(F_target).mult(F_beam);
        SimpleMatrix Q = Q_cathode.plus(Q_flow).plus(Q_foil).plus(Q_buffer).plus(Q_wall).plus(Q_target).plus(Q_beam);

        SimpleMatrix F = backward_F4(stateVector_start, m0, m1);

        SimpleMatrix P_minus = (F.mult(P_plus).mult(F.transpose())).plus(Q);

        if (debug) System.out.println("P_minus: " + P_minus);

        stateVectorPredictList.add(beam);
        stateCovarianceMatrixPredictList.add(P_minus);

        // Update :
        SimpleMatrix h = h(beam, m0);
        SimpleMatrix y = m0.minus(h);
        y.set(1, 0, 0);
        if (debug) System.out.println("y: " + y);

        SimpleMatrix H = H(beam, m0);
        SimpleMatrix R = R_beam();
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        if (debug) System.out.println("Ky: " + K.mult(y));

        SimpleMatrix a_plus = beam.a.plus(K.mult(y));
        SimpleMatrix P_plus1 = P_minus.minus(K.mult(H).mult(P_minus));

        StateVector stateVector_plus = new StateVector(a_plus, beam.phi);

        if (Math.abs(stateVector_plus.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (debug)
            stateVector_plus.print("end : ", m0);

        backStateVectorUpdateList.set(0, stateVector_plus);
        backCovarianceMatrixUpdateList.set(0, P_plus1);

    }

    private StateVector backward_f4(StateVector sv_plus, SimpleMatrix m0, SimpleMatrix m1, boolean print) throws Exception{
        double r_cathode = 30.00;   
        String material_cathode = "HeCO2";
        StateVector stateVector_cathode = backward_f2(sv_plus, m1, r_cathode, material_cathode, false);

        // Flow
        double r_flow = 29.994;
        String material_flow = "Mylar";
        StateVector stateVector_flow = backward_f2(stateVector_cathode, m1, r_flow, material_flow, false);

        // Ground foil
        double r_foil = 20.00;
        String material_foil = "HeCO2";
        StateVector stateVector_foil = backward_f2(stateVector_flow, m1, r_foil, material_foil, false);

        // Buffer
        double r_buffer = 19.994;
        String material_buffer = "Mylar";
        StateVector stateVector_buffer = backward_f2(stateVector_foil, m1, r_buffer, material_buffer, false);

        // Wall
        double r_wall = 3.055;
        String material_wall = "HeCO2";
        StateVector stateVector_wall = backward_f2(stateVector_buffer, m1, r_wall, material_wall, false);

        // Target
        double r_target = 3.0;
        String material_target = "Kapton";
        StateVector stateVector_target = backward_f2(stateVector_wall, m1, r_target, material_target, false);

        // BeamLine
        StateVector beam = backward_f1(stateVector_target, m1, m0, false);


        return beam;
    }

    private SimpleMatrix backward_F4(StateVector stateVector, SimpleMatrix m0, SimpleMatrix m1) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double alpha = computeAlpha(m0);
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = backward_f4(stateVectorPlus, m0, m1, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = backward_f4(stateVectorMinus, m0, m1, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = a.get(0, 0);
        double phi0 = a.get(1, 0);
        double kappa = a.get(2, 0);
        double tanL = a.get(4, 0);

        StateVector stateVector_minus = backward_f4(stateVector, m0, m1, false);

        double drho_prim = stateVector_minus.a.get(0, 0);
        double phi0_prim = stateVector_minus.a.get(1, 0);

        double dphi0_prm_del_drho = -1. / (drho_prim + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_phi0 = (drho + alpha / kappa) / (drho_prim + alpha / kappa) * Math.cos(phi0_prim - phi0);
        double dphi0_prm_del_kappa = (alpha / (kappa * kappa)) / (drho_prim + alpha / kappa)
                * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(phi0_prim - phi0);
        double drho_prm_del_phi0 = (drho + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double drho_prm_del_kappa = (alpha / (kappa * kappa)) * (1 - Math.cos(phi0_prim - phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dz_prm_del_drho = ((alpha / kappa) / (drho_prim + alpha / kappa)) * tanL * Math.sin(phi0_prim - phi0);
        double dz_prm_del_phi0 = (alpha / kappa) * tanL
                * (1 - Math.cos(phi0_prim - phi0) * (drho + alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_kappa = (alpha / (kappa * kappa)) * tanL
                * (phi0_prim - phi0 - Math.sin(phi0_prim - phi0) * (alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -alpha * (phi0_prim - phi0) / kappa;

        return new SimpleMatrix(new double[][] {
                { drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL },
                { dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL },
                dkappadx, { dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL },
                { 0, 0, 0, 0, 1 } });
    }

    /**
     * Propagate the state vector from the target to the beamline.
     * 
     * @param stateVector
     * @param m
     * @param m0
     * @param print
     * @return
     * @throws Exception
     */
    private StateVector backward_f1(StateVector stateVector, SimpleMatrix m, SimpleMatrix m0, boolean print)
            throws Exception {

        SimpleMatrix a_plus = stateVector.a.copy();
        double phi_start = stateVector.phi;

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);
        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z0 = m.get(2, 0);
        double x0_prim = m0.get(0, 0) * Math.cos(m0.get(1, 0));
        double y0_prim = m0.get(0, 0) * Math.sin(m0.get(1, 0));
        double z0_prim = m0.get(2, 0);
        double alpha = computeAlpha(m);

        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI;
        phi0_prim = mod2PI(phi0_prim);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double pathLength;
        double min_pathLength = 1e-3;
        int ITMAX = 10;

        StateVector stateVector1 = new StateVector(
                new SimpleMatrix(new double[][] { { drho }, { phi0 }, { kappa }, { dz }, { tanL } }), phi_start, 0);

        Function function = (phi, kappa1) -> Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        for (int iter = 0; iter < ITMAX; iter++) {
            double phi_min = minimize(bracket(0, 1, function, kappa), function, kappa);

            pathLength = pathLength(kappa, tanL, alpha, phi_start, phi_min);
            stateVector1.pathLength += pathLength;
            stateVector1.phi = phi_min;

            if (pathLength < min_pathLength)
                break;

            kappa = energyLoss(kappa, tanL, pathLength, "Deuterium", -1);
            stateVector1.a.set(2, 0, kappa);
            phi_start = phi_min;

        }

        // Change pivot point :
        stateVector1.a.set(0, 0, drho_prim);
        stateVector1.a.set(1, 0, phi0_prim);
        stateVector1.a.set(3, 0, dz_prim);
        stateVector1.phi = 0;

        return stateVector1;
    }

    /**
     * Propagate the state vector between the 1th measurement point and a
     * radius @param r.
     * 
     * @param stateVector
     * @param m
     * @param r
     * @param material
     * @param print
     * @return
     * @throws Exception
     */
    private StateVector backward_f2(StateVector stateVector, SimpleMatrix m, double r, String material, boolean print)
            throws Exception {
        SimpleMatrix a_plus = stateVector.a.copy();
        double phi_start = stateVector.phi;

        if (print)
            stateVector.print("start point", m);

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);
        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double alpha = computeAlpha(m);

        StateVector stateVector1 = new StateVector(
                new SimpleMatrix(new double[][] { { drho }, { phi0 }, { kappa }, { dz }, { tanL } }), phi_start, 0);

        Function func = (phi, kappa1) -> -r + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double pathLength;
        double min_pathLength = 1e-3;
        int ITMAX = 100;
        for (int iter = 0; iter < ITMAX; iter++) {

            double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
            if (print)
                System.out.println("phi_min: " + phi_min);
            double phi_minus = root_finding(func, kappa, phi_min, phi_min + Math.PI/2);
            pathLength = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
            stateVector1.pathLength += pathLength;
            stateVector1.phi = phi_minus;

            if (print)
                System.out.println("pathlength iteration: " + pathLength);
            if (pathLength < min_pathLength) {
                break;
            }
            if (print)
                stateVector1.print("mid", m);

            kappa = energyLoss(kappa, tanL, pathLength, material, -1);
            stateVector1.a.set(2, 0, kappa);
            if (print)
                stateVector1.print("after EL", m);
            phi_start = phi_minus;

        }

        return stateVector1;
    }

    /**
     * Propagate the state vector from a measurement point to a other one.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @param print
     * @return
     * @throws Exception
     */
    private StateVector backward_f3(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1, boolean print)
            throws Exception {
        SimpleMatrix a_plus = stateVector.a.copy();

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);

        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z0 = m.get(2, 0);

        double x0_prim = m1.get(0, 0) * Math.cos(m1.get(1, 0));
        double y0_prim = m1.get(0, 0) * Math.sin(m1.get(1, 0));
        double z0_prim = m1.get(2, 0);

        double alpha = computeAlpha(m);

        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = Math.atan2((y0_prim - Yc), (x0_prim - Xc));
        phi0_prim = mod2PI(phi0_prim);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double pathLength = pathLength(kappa, tanL, alpha, phi0, phi0_prim);

        double kappa_prim = energyLoss(kappa, tanL, pathLength, "HeCO2", -1);

        return new StateVector(
                new SimpleMatrix(
                        new double[][] { { drho_prim }, { phi0_prim }, { kappa_prim }, { dz_prim }, { tanL } }),
                0, pathLength);
    }

    /**
     * backward_F1 is the jacobian of backward_f1 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param m0
     * @return
     * @throws Exception
     */
    private SimpleMatrix backward_F1(StateVector stateVector, SimpleMatrix m, SimpleMatrix m0) throws Exception {
        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = backward_f1(stateVectorPlus, m, m0, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = backward_f1(stateVectorMinus, m, m0, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = a.get(0, 0);
        double phi0 = a.get(1, 0);
        double kappa = a.get(2, 0);
        double tanL = a.get(4, 0);

        StateVector a_prim = backward_f1(stateVector, m, m0, false);

        double drho_prim = a_prim.a.get(0, 0);
        double phi0_prim = a_prim.a.get(1, 0);

        double dphi0_prm_del_drho = -1. / (drho_prim + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_phi0 = (drho + alpha / kappa) / (drho_prim + alpha / kappa) * Math.cos(phi0_prim - phi0);
        double dphi0_prm_del_kappa = (alpha / (kappa * kappa)) / (drho_prim + alpha / kappa)
                * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(phi0_prim - phi0);
        double drho_prm_del_phi0 = (drho + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double drho_prm_del_kappa = (alpha / (kappa * kappa)) * (1 - Math.cos(phi0_prim - phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dz_prm_del_drho = ((alpha / kappa) / (drho_prim + alpha / kappa)) * tanL * Math.sin(phi0_prim - phi0);
        double dz_prm_del_phi0 = (alpha / kappa) * tanL
                * (1 - Math.cos(phi0_prim - phi0) * (drho + alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_kappa = (alpha / (kappa * kappa)) * tanL
                * (phi0_prim - phi0 - Math.sin(phi0_prim - phi0) * (alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -alpha * (phi0_prim - phi0) / kappa;

        return new SimpleMatrix(new double[][] {
                { drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL },
                { dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL },
                dkappadx, { dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL },
                { 0, 0, 0, 0, 1 } });
    }

    /**
     * backward_F2 is the jacobian of backward_f2 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param r
     * @param material
     * @return
     * @throws Exception
     */
    private SimpleMatrix backward_F2(StateVector stateVector, SimpleMatrix m, double r, String material)
            throws Exception {
        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = backward_f2(stateVectorPlus, m, r, material, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = backward_f2(stateVectorMinus, m, r, material, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        return new SimpleMatrix(new double[][] { { 1, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0 }, dkappadx, { 0, 0, 0, 1, 0 },
                { 0, 0, 0, 0, 1 } });
    }

    /**
     * backward_F3 is the jacobian of backward_f3 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @param dir
     * @return
     * @throws Exception
     */
    private SimpleMatrix backward_F3(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = backward_f3(stateVectorPlus, m, m1, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = backward_f3(stateVectorMinus, m, m1, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = a.get(0, 0);
        double phi0 = a.get(1, 0);
        double kappa = a.get(2, 0);
        double tanL = a.get(4, 0);

        StateVector stateVector_minus = backward_f3(stateVector, m, m1, false);

        double drho_prim = stateVector_minus.a.get(0, 0);
        double phi0_prim = stateVector_minus.a.get(1, 0);

        double dphi0_prm_del_drho = -1. / (drho_prim + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_phi0 = (drho + alpha / kappa) / (drho_prim + alpha / kappa) * Math.cos(phi0_prim - phi0);
        double dphi0_prm_del_kappa = (alpha / (kappa * kappa)) / (drho_prim + alpha / kappa)
                * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(phi0_prim - phi0);
        double drho_prm_del_phi0 = (drho + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double drho_prm_del_kappa = (alpha / (kappa * kappa)) * (1 - Math.cos(phi0_prim - phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dz_prm_del_drho = ((alpha / kappa) / (drho_prim + alpha / kappa)) * tanL * Math.sin(phi0_prim - phi0);
        double dz_prm_del_phi0 = (alpha / kappa) * tanL
                * (1 - Math.cos(phi0_prim - phi0) * (drho + alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_kappa = (alpha / (kappa * kappa)) * tanL
                * (phi0_prim - phi0 - Math.sin(phi0_prim - phi0) * (alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -alpha * (phi0_prim - phi0) / kappa;

        return new SimpleMatrix(new double[][] {
                { drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL },
                { dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL },
                dkappadx, { dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL },
                { 0, 0, 0, 0, 1 } });
    }

    /**
     * Propagate the state vector between the measurement point k to the measurement
     * k+1.
     * 
     * @param k
     * @throws Exception
     */
    private void forwardPropagation(int k) throws Exception {
        StateVector stateVector_start = stateVectorUpdateList.get(k);
        SimpleMatrix P_plus = stateCovarianceMatrixUpdateList.get(k);
        SimpleMatrix m0 = measVectorList.get(k);
        SimpleMatrix m1 = measVectorList.get(k + 1);

        if (debug)
            stateVector_start.print("start", m0);

        if (debug)
            System.out.println("P_plus = " + P_plus);

        if (Math.abs(stateVector_start.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (Math.abs(stateVector_start.a.get(3, 0)) > 20)
            throw new Exception("dz is too big !!");

        StateVector stateVector1 = forward_f3(stateVector_start, m0, m1, true, 1);

        if (Math.abs(stateVector1.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (Math.abs(stateVector_start.a.get(3, 0)) > 20)
            throw new Exception("dz is too big !!");

        if (debug)
            stateVector1.print("prop", m1);

        SimpleMatrix F = forward_F3(stateVector_start, m0, m1, 1);
        SimpleMatrix Q = Q(stateVector1, "HeCO2");
        if (debug)
            System.out.println("beforer Q: " + Q);
        Q = F.mult(Q).mult(F.transpose());

        if (debug)
            System.out.println("F = " + F);
        if (debug)
            System.out.println(" after Q = " + Q);

        SimpleMatrix P_minus = (F.mult(P_plus).mult(F.transpose())).plus(Q);

        if (debug)
            System.out.println("P_minus = " + P_minus);

        stateVectorPredictList.add(stateVector1);
        stateCovarianceMatrixPredictList.add(P_minus);
    }

    /**
     * Propagate the state vector between the beam line to the 1th measurement
     * point.
     * 
     * @throws Exception
     */
    private void forwardPropagationFirstStep() throws Exception {
        StateVector sv_plus = stateVectorUpdateList.get(0);
        SimpleMatrix P_plus = stateCovarianceMatrixUpdateList.get(0);
        SimpleMatrix m0 = measVectorList.get(0);
        SimpleMatrix m1 = measVectorList.get(1);

        if (debug)
            System.out.println("P_plus = " + P_plus);

        // Target
        double r_target = 3.0;
        String material_target = "Deuterium";
        StateVector sv_target = forward_f1(sv_plus, m0, r_target, material_target, debug);
        SimpleMatrix F_target = forward_F1(sv_plus, m0, r_target, material_target);
        SimpleMatrix Q_target = F_target.mult(Q(sv_target, material_target)).mult(F_target.transpose());

        // Wall
        double r_wall = 3.055;
        String material_wall = "Kapton";
        StateVector sv_wall = forward_f1(sv_target, m0, r_wall, material_wall, debug);
        SimpleMatrix F_wall = forward_F1(sv_target, m0, r_wall, material_wall);
        SimpleMatrix Q_wall = F_wall.mult(Q(sv_wall, material_wall)).mult(F_wall.transpose());

        // Buffer
        double r_buffer = 19.994;
        String material_buffer = "HeCO2";
        StateVector sv_buffer = forward_f1(sv_wall, m0, r_buffer, material_buffer, debug);
        SimpleMatrix F_buffer = forward_F1(sv_wall, m0, r_buffer, material_buffer);
        SimpleMatrix Q_buffer = F_buffer.mult(Q(sv_buffer, material_buffer)).mult(F_buffer.transpose());

        // Ground foil
        double r_foil = 20.00;
        String material_foil = "Mylar";
        StateVector sv_foil = forward_f1(sv_buffer, m0, r_foil, material_foil, debug);
        SimpleMatrix F_foil = forward_F1(sv_buffer, m0, r_foil, material_foil);
        SimpleMatrix Q_foil = F_foil.mult(Q(sv_foil, material_foil)).mult(F_foil.transpose());

        // Flow
        double r_flow = 29.994;
        String material_flow = "HeCO2";
        StateVector sv_flow = forward_f1(sv_foil, m0, r_flow, material_flow, debug);
        SimpleMatrix F_flow = forward_F1(sv_foil, m0, r_flow, material_flow);
        SimpleMatrix Q_flow = F_flow.mult(Q(sv_flow, material_flow)).mult(F_flow.transpose());

        // Cathode
        double r_cathode = 30.00;
        String material_cathode = "Mylar";
        StateVector sv_cathode = forward_f1(sv_flow, m0, r_cathode, material_cathode, debug);
        SimpleMatrix F_cathode = forward_F1(sv_flow, m0, r_cathode, material_cathode);
        SimpleMatrix Q_cathode = F_cathode.mult(Q(sv_cathode, material_cathode)).mult(F_cathode.transpose());

        // Measurement point:
        StateVector sv_minus = forward_f2(sv_cathode, m0, m1, debug);
        SimpleMatrix F_meas = forward_F2(sv_cathode, m0, m1);
        SimpleMatrix Q_meas = F_meas.mult(Q(sv_minus, "HeCO2")).mult(F_meas.transpose());

        // System.out.println("TEST !!!");

        if (debug)
            System.out.println("F_cathode = " + F_cathode);

        if (debug)
            System.out.println("F_meas = " + F_meas);

        if (debug)
            sv_minus.print("end first propagation: ", m1);

        // SimpleMatrix F = F_target.mult(F_wall).mult(F_buffer).mult(F_foil).mult(F_flow).mult(F_cathode).mult(F_meas);
        SimpleMatrix Q = Q_target.plus(Q_wall).plus(Q_buffer).plus(Q_foil).plus(Q_flow).plus(Q_cathode).plus(Q_meas);

        //if (debug)
        //    System.out.println("F = " + F);
        if (debug)
            System.out.println("Q = " + Q);

        SimpleMatrix F = forward_F4(sv_plus, m0, m1);

        if (debug)
            System.out.println("F = " + F);

        SimpleMatrix P_minus = (F.mult(P_plus).mult(F.transpose())).plus(Q);

        stateVectorPredictList.add(sv_minus);
        stateCovarianceMatrixPredictList.add(P_minus);
    }

    /**
     * Update the state vector with the information of the measurement k+1
     * 
     * @param k
     * @throws Exception
     */
    public void forwardUpdate(int k) throws Exception {
        StateVector stateVector_minus = stateVectorPredictList.get(k + 1);
        SimpleMatrix P_minus = stateCovarianceMatrixPredictList.get(k + 1);
        SimpleMatrix m = measVectorList.get(k + 1);

        if (debug)
            stateVector_minus.print("update start", m);

        SimpleMatrix h = h(stateVector_minus, m);
        SimpleMatrix y = m.minus(h);
        y.set(1, 0, modPI(y.get(1, 0)));

        if (debug) System.out.println("y: " + y);

        if (debug)
            System.out.println("P_minus = " + P_minus);

        SimpleMatrix H = H(stateVector_minus, m);
        SimpleMatrix R = R(m);
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        if (debug) System.out.println("K: " + K);
        if (debug) System.out.println("Ky: " + K.mult(y));


        SimpleMatrix a_plus = stateVector_minus.a.plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        StateVector stateVector_plus = new StateVector(a_plus, stateVector_minus.phi);

        if (debug)
            stateVector_plus.print("update end", m);

        if (debug)
            System.out.println("P_plus = " + P_plus);

        if (Math.abs(stateVector_plus.a.get(0, 0)) > 20)
            throw new Exception("drho is too big !!");

        if (Math.abs(stateVector_plus.a.get(2, 0)) > 20)
            throw new Exception("kappa is too big !!");

        stateVectorUpdateList.add(stateVector_plus);
        stateCovarianceMatrixUpdateList.add(P_plus);
    }

    private StateVector forward_f4(StateVector sv_plus, SimpleMatrix m0, SimpleMatrix m1, boolean print) throws Exception{
        
        double r_target = 3.0;
        String material_target = "Deuterium";
        StateVector sv_target = forward_f1(sv_plus, m0, r_target, material_target, false);

        // Wall
        double r_wall = 3.055;
        String material_wall = "Kapton";
        StateVector sv_wall = forward_f1(sv_target, m0, r_wall, material_wall, false);

        // Buffer
        double r_buffer = 19.994;
        String material_buffer = "HeCO2";
        StateVector sv_buffer = forward_f1(sv_wall, m0, r_buffer, material_buffer, false);

        // Ground foil
        double r_foil = 20.00;
        String material_foil = "Mylar";
        StateVector sv_foil = forward_f1(sv_buffer, m0, r_foil, material_foil, false);

        // Flow
        double r_flow = 29.994;
        String material_flow = "HeCO2";
        StateVector sv_flow = forward_f1(sv_foil, m0, r_flow, material_flow, false);

        // Cathode
        double r_cathode = 30.00;
        String material_cathode = "Mylar";
        StateVector sv_cathode = forward_f1(sv_flow, m0, r_cathode, material_cathode, false);

        // Measurement point:
        StateVector sv_minus = forward_f2(sv_cathode, m0, m1, false);

        return sv_minus;
    }

    /**
     * Propagate the state vector to a radius @param r throught the matrial.
     * 
     * @param sv_plus
     * @param m
     * @param r
     * @param material
     * @param print
     * @return
     * @throws Exception
     */
    private StateVector forward_f1(StateVector sv_plus, SimpleMatrix m, double r, String material, boolean print)
            throws Exception {
        double phi_start = sv_plus.phi;
        if (print)
            sv_plus.print("start", m);

        double drho = sv_plus.drho();
        double phi0 = sv_plus.phi0();
        double kappa = sv_plus.kappa();
        double dz = sv_plus.dz();
        double tanL = sv_plus.tanL();
        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double alpha = computeAlpha(m);

        StateVector stateVector1 = new StateVector(
                new SimpleMatrix(new double[][] { { drho }, { phi0 }, { kappa }, { dz }, { tanL } }), phi_start, 0);

        Function func = (phi, kappa1) -> -r + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double pathLength;
        double min_pathLength = 1e-3;
        int ITMAX = 100;
        for (int iter = 0; iter < ITMAX; iter++) {
            if (print)
                System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)));
            double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
            if (print)
                System.out.println("phi_min: " + phi_min);
            double phi_minus = root_finding(func, kappa, phi_min, 3);
            pathLength = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
            if (print)
                System.out.println("phi_start = " + phi_start);
            if (print)
                System.out.println("phi_end = " + phi_minus);
            if (print)
                System.out.println("pathLength = " + pathLength);
            stateVector1.pathLength += pathLength;
            stateVector1.phi = phi_minus;
            if (print)
                stateVector1.print("prop", m);

            if (pathLength < min_pathLength)
                break;

            kappa = energyLoss(kappa, tanL, pathLength, material, 1);
            stateVector1.a.set(2, 0, kappa);
            phi_start = phi_minus;
            if (print)
                stateVector1.print("EL", m);

        }

        return stateVector1;

    }

    /**
     * Propagate the state vector to the 1th measurement point.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @param print
     * @return
     * @throws Exception
     */
    private StateVector forward_f2(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1, boolean print)
            throws Exception {
        SimpleMatrix a_plus = stateVector.a.copy();
        double phi_start = stateVector.phi;

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);
        double alpha = computeAlpha(m);

        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z0 = m.get(2, 0);

        double x0_prim = m1.get(0, 0) * Math.cos(m1.get(1, 0));
        double y0_prim = m1.get(0, 0) * Math.sin(m1.get(1, 0));
        double z0_prim = m1.get(2, 0);

        double pathLength;
        StateVector stateVector1 = new StateVector(
                new SimpleMatrix(new double[][] { { drho }, { phi0 }, { kappa }, { dz }, { tanL } }), 0, 0);

        Function func = (phi, kappa1) -> -m1.get(0, 0) + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
        if (func.calc(phi_min, kappa) > 0)
            throw new Exception("f(phi_min) > 0 !!");
        double phi_minus = root_finding(func, kappa, phi_min, 3);

        pathLength = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
        stateVector1.pathLength += pathLength;

        double kappa_prim = energyLoss(kappa, tanL, pathLength, "HeCO2", 1);
        stateVector1.a.set(2, 0, kappa_prim);

        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI;
        phi0_prim = mod2PI(phi0_prim);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;
        stateVector1.a.set(0, 0, drho_prim);
        stateVector1.a.set(1, 0, phi0_prim);
        stateVector1.a.set(3, 0, dz_prim);
        return stateVector1;
    }

    /**
     * Propagate the state vector from a measurement point to a other one.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @param print
     * @param dir
     * @return
     * @throws Exception
     */
    private StateVector forward_f3(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1, boolean print, int dir)
            throws Exception {
        SimpleMatrix a_plus = stateVector.a.copy();

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);

        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z0 = m.get(2, 0);

        double x0_prim = m1.get(0, 0) * Math.cos(m1.get(1, 0));
        double y0_prim = m1.get(0, 0) * Math.sin(m1.get(1, 0));
        double z0_prim = m1.get(2, 0);

        double alpha = computeAlpha(m);

        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI;
        phi0_prim = mod2PI(phi0_prim);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double pathLength = pathLength(kappa, tanL, alpha, phi0, phi0_prim);

        double kappa_prim = energyLoss(kappa, tanL, pathLength, "HeCO2", dir);

        return new StateVector(
                new SimpleMatrix(
                        new double[][] { { drho_prim }, { phi0_prim }, { kappa_prim }, { dz_prim }, { tanL } }),
                0, pathLength);
    }

    /**
     * forward_F1 is the jacobian of forward_f1 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param r
     * @param material
     * @return
     * @throws Exception
     */
    private SimpleMatrix forward_F1(StateVector stateVector, SimpleMatrix m, double r, String material)
            throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = forward_f1(stateVectorPlus, m, r, material, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = forward_f1(stateVectorMinus, m, r, material, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        return new SimpleMatrix(new double[][] { { 1, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0 }, dkappadx, { 0, 0, 0, 1, 0 },
                { 0, 0, 0, 0, 1 } });

    }

    /**
     * forward_F2 is the jacobian of forward_f2 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @return
     * @throws Exception
     */
    private SimpleMatrix forward_F2(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] ddrhodx = new double[5];
        double[] dphi0dx = new double[5];
        double[] dkappadx = new double[5];
        double[] ddzdx = new double[5];
        double[] dtanLdx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = forward_f2(stateVectorPlus, m, m1, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = forward_f2(stateVectorMinus, m, m1, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double ddrhodi = (aPlus_minus.get(0, 0) - aMinus_minus.get(0, 0)) / (2 * h);
            double dphi0di = (aPlus_minus.get(1, 0) - aMinus_minus.get(1, 0)) / (2 * h);
            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);
            double ddzdi = (aPlus_minus.get(3, 0) - aMinus_minus.get(3, 0)) / (2 * h);
            double dtanLdi = (aPlus_minus.get(4, 0) - aMinus_minus.get(4, 0)) / (2 * h);

            ddrhodx[i] = ddrhodi;
            dphi0dx[i] = dphi0di;
            dkappadx[i] = dkappadi;
            ddzdx[i] = ddzdi;
            dtanLdx[i] = dtanLdi;
        }

        

        return new SimpleMatrix(new double[][] {
                ddrhodx,
                dphi0dx,
                dkappadx,
                ddzdx,
                dtanLdx});

    }

    /**
     * forward_F3 is the jacobian of forward_f3 with respect to the state vector.
     * 
     * @param stateVector
     * @param m
     * @param m1
     * @param dir
     * @return
     * @throws Exception
     */
    private SimpleMatrix forward_F3(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1, int dir)
            throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = forward_f3(stateVectorPlus, m, m1, false, dir);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = forward_f3(stateVectorMinus, m, m1, false, dir);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = a.get(0, 0);
        double phi0 = a.get(1, 0);
        double kappa = a.get(2, 0);
        double tanL = a.get(4, 0);

        StateVector stateVector_minus = forward_f3(stateVector, m, m1, false, dir);

        double drho_prim = stateVector_minus.a.get(0, 0);
        double phi0_prim = stateVector_minus.a.get(1, 0);

        double dphi0_prm_del_drho = -1. / (drho_prim + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_phi0 = (drho + alpha / kappa) / (drho_prim + alpha / kappa) * Math.cos(phi0_prim - phi0);
        double dphi0_prm_del_kappa = (alpha / (kappa * kappa)) / (drho_prim + alpha / kappa)
                * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(phi0_prim - phi0);
        double drho_prm_del_phi0 = (drho + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double drho_prm_del_kappa = (alpha / (kappa * kappa)) * (1 - Math.cos(phi0_prim - phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dz_prm_del_drho = ((alpha / kappa) / (drho_prim + alpha / kappa)) * tanL * Math.sin(phi0_prim - phi0);
        double dz_prm_del_phi0 = (alpha / kappa) * tanL
                * (1 - Math.cos(phi0_prim - phi0) * (drho + alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_kappa = (alpha / (kappa * kappa)) * tanL
                * (phi0_prim - phi0 - Math.sin(phi0_prim - phi0) * (alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -alpha * (phi0_prim - phi0) / kappa;

        return new SimpleMatrix(new double[][] {
                { drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL },
                { dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL },
                dkappadx, { dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL },
                { 0, 0, 0, 0, 1 } });
    }

    private SimpleMatrix forward_F4(StateVector stateVector, SimpleMatrix m, SimpleMatrix m1)
            throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix a = stateVector.a.copy();
        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix aPlus = new SimpleMatrix(a);
            aPlus.set(i, 0, aPlus.get(i, 0) + h);
            StateVector stateVectorPlus = new StateVector(aPlus, phi);
            StateVector stateVectorPlus_minus = forward_f4(stateVectorPlus, m, m1, false);
            SimpleMatrix aPlus_minus = stateVectorPlus_minus.a.copy();

            SimpleMatrix aMinus = new SimpleMatrix(a);
            aMinus.set(i, 0, aMinus.get(i, 0) - h);
            StateVector stateVectorMinus = new StateVector(aMinus, phi);
            StateVector stateVectorMinus_minus = forward_f4(stateVectorMinus, m, m1, false);
            SimpleMatrix aMinus_minus = stateVectorMinus_minus.a.copy();

            double dkappadi = (aPlus_minus.get(2, 0) - aMinus_minus.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = a.get(0, 0);
        double phi0 = a.get(1, 0);
        double kappa = a.get(2, 0);
        double tanL = a.get(4, 0);

        StateVector stateVector_minus = forward_f4(stateVector, m, m1, false);

        double drho_prim = stateVector_minus.a.get(0, 0);
        double phi0_prim = stateVector_minus.a.get(1, 0);

        double dphi0_prm_del_drho = -1. / (drho_prim + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_phi0 = (drho + alpha / kappa) / (drho_prim + alpha / kappa) * Math.cos(phi0_prim - phi0);
        double dphi0_prm_del_kappa = (alpha / (kappa * kappa)) / (drho_prim + alpha / kappa)
                * Math.sin(phi0_prim - phi0);
        double dphi0_prm_del_dz = 0;
        double dphi0_prm_del_tanL = 0;

        double drho_prm_del_drho = Math.cos(phi0_prim - phi0);
        double drho_prm_del_phi0 = (drho + alpha / kappa) * Math.sin(phi0_prim - phi0);
        double drho_prm_del_kappa = (alpha / (kappa * kappa)) * (1 - Math.cos(phi0_prim - phi0));
        double drho_prm_del_dz = 0;
        double drho_prm_del_tanL = 0;

        double dz_prm_del_drho = ((alpha / kappa) / (drho_prim + alpha / kappa)) * tanL * Math.sin(phi0_prim - phi0);
        double dz_prm_del_phi0 = (alpha / kappa) * tanL
                * (1 - Math.cos(phi0_prim - phi0) * (drho + alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_kappa = (alpha / (kappa * kappa)) * tanL
                * (phi0_prim - phi0 - Math.sin(phi0_prim - phi0) * (alpha / kappa) / (drho_prim + alpha / kappa));
        double dz_prm_del_dz = 1;
        double dz_prm_del_tanL = -alpha * (phi0_prim - phi0) / kappa;

        return new SimpleMatrix(new double[][] {
                { drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL },
                { dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL },
                dkappadx, { dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL },
                { 0, 0, 0, 0, 1 } });
    }

    /**
     * Q is the noise process between a site k-1 to a site k. It is due to the
     * multiple scattering.
     * 
     * @param sv
     * @param material
     * @return
     */
    public SimpleMatrix Q(StateVector stateVector, String material) {
        double kappa = stateVector.a.get(2, 0);
        double tanL = stateVector.a.get(4, 0);

        double p = Math.sqrt((tanL * tanL + 1) / (kappa * kappa));
        double M = 0.938;
        double beta = p / Math.sqrt(p * p + M * M);

        double pathLength = stateVector.pathLength / 10; // cm

        double X0 = 0;
        if (Objects.equals(material, "Deuterium")) {
            X0 = 699.9; // cm : https://pdg.lbl.gov/2020/AtomicNuclearProperties/HTML/deuterium_gas.html
                        // = rad_length/rho at 5atm
        } else if (Objects.equals(material, "Kapton")) {
            X0 = 28.57; // cm :
            // https://pdg.lbl.gov/2020/AtomicNuclearProperties/HTML/polyimide_film.html
        } else if (Objects.equals(material, "HeCO2")) {
            double w_He = 4 * 4.0026 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_C = 12.0107 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_O = 2 * 15.999 / (4 * 4.0026 + 12.0107 + 2 * 15.999);
            double w_CO2 = w_C + 2 * w_O;

            double X0_He = 5.671E+05; // cm : https://pdg.lbl.gov/2020/AtomicNuclearProperties/HTML/helium_gas_He.html
            double X0_C02 = 1.965E+04; // cm : //
                                       // https://pdg.lbl.gov/2020/AtomicNuclearProperties/HTML/carbon_dioxide_gas.html

            X0 = 1 / (w_He / X0_He + w_CO2 / X0_C02); // cm

        } else if (Objects.equals(material, "Mylar")) {
            X0 = 28.54;
        }

        double sctRMS = 0.0136 / (beta * p) * Math.sqrt(pathLength / X0)
                * (1 + 0.088 * Math.log10(pathLength / (X0 * beta * beta)));

        return new SimpleMatrix(new double[][] { { 0, 0, 0, 0, 0 }, { 0, 1 + tanL * tanL, 0, 0, 0 },
                { 0, 0, (kappa * tanL) * (kappa * tanL), 0, kappa * tanL * (1 + tanL * tanL) }, { 0, 0, 0, 0, 0 },
                { 0, 0, kappa * tanL * (1 + tanL * tanL), 0, (1 + tanL * tanL) * (1 + tanL * tanL) } })
                        .scale(sctRMS * sctRMS);
    }

    /**
     * The function h transform a state vector (drho,phi0,kappa,dz,tanL), phi and a
     * measurement point (rm,phim,zm) into r,phi,z
     * 
     * @param sv state vector
     * @param m  measurement point
     * @return [r,phi,z]^T
     */
    public SimpleMatrix h(StateVector stateVector, SimpleMatrix m) {
        SimpleMatrix a_plus = stateVector.a.copy();
        double phi = stateVector.phi;

        double drho = a_plus.get(0, 0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);
        double x0 = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y0 = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z0 = m.get(2, 0);
        double alpha = computeAlpha(m);

        double x = x0 + drho * Math.cos(phi0) + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
        double y = y0 + drho * Math.sin(phi0) + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
        double z = z0 + dz - alpha / kappa * tanL * phi;

        return new SimpleMatrix(new double[][] { { Math.sqrt(x * x + y * y) }, { Math.atan2(y, x) }, { z } });

    }

    /**
     * H is the jacobian of the function h with respect to the state vector.
     * 
     * @param sv state vector
     * @param m  measurement point
     * @return H
     */
    public SimpleMatrix H(StateVector stateVector, SimpleMatrix m) {

        double drho = stateVector.a.get(0, 0);
        double phi0 = stateVector.a.get(1, 0);
        double kappa = stateVector.a.get(2, 0);
        double tanl = stateVector.a.get(4, 0);

        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        double xo = m.get(0, 0) * Math.cos(m.get(1, 0));
        double yo = m.get(0, 0) * Math.sin(m.get(1, 0));

        double drddrho = (-alpha * Math.cos(phi) + alpha + drho * kappa + kappa * xo * Math.cos(phi0)
                + kappa * yo * Math.sin(phi0))
                / Math.sqrt(Math
                        .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2));
        double drdphi0 = (-alpha * xo * Math.sin(phi0) + alpha * xo * Math.sin(phi + phi0) + alpha * yo * Math.cos(phi0)
                - alpha * yo * Math.cos(phi + phi0) - drho * kappa * xo * Math.sin(phi0)
                + drho * kappa * yo * Math.cos(phi0))
                / Math.sqrt(Math
                        .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2));
        double drdkappa = alpha
                * (2 * alpha * Math.cos(phi) - 2 * alpha + drho * kappa * Math.cos(phi) - drho * kappa
                        - kappa * xo * Math.cos(phi0) + kappa * xo * Math.cos(phi + phi0) - kappa * yo * Math.sin(phi0)
                        + kappa * yo * Math.sin(phi + phi0))
                / (Math.pow(kappa, 2) * Math.sqrt(Math
                        .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2)));
        double drddz = 0;
        double drdtanL = 0;

        double dphiddrho = kappa * (alpha * Math.sin(phi) + kappa * xo * Math.sin(phi0) - kappa * yo * Math.cos(phi0))
                / (Math.pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2));
        double dphidphi0 = (-2 * Math.pow(alpha, 2) * Math.cos(phi) + 2 * Math.pow(alpha, 2)
                - 2 * alpha * drho * kappa * Math.cos(phi) + 2 * alpha * drho * kappa
                + alpha * kappa * xo * Math.cos(phi0) - alpha * kappa * xo * Math.cos(phi + phi0)
                + alpha * kappa * yo * Math.sin(phi0) - alpha * kappa * yo * Math.sin(phi + phi0)
                + Math.pow(drho, 2) * Math.pow(kappa, 2) + drho * Math.pow(kappa, 2) * xo * Math.cos(phi0)
                + drho * Math.pow(kappa, 2) * yo * Math.sin(phi0))
                / (Math.pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2));
        double dphidkappa = alpha
                * (drho * Math.sin(phi) - xo * Math.sin(phi0) + xo * Math.sin(phi + phi0) + yo * Math.cos(phi0)
                        - yo * Math.cos(phi + phi0))
                / (Math.pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                        + Math.pow(
                                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                                2));
        double dphiddz = 0;
        double dphidtanL = 0;

        double dzddrho = 0;
        double dzdphi0 = 0;
        double dzdkappa = alpha * phi * tanl / Math.pow(kappa, 2);
        double dzddz = 1;
        double dzdtanL = -alpha * phi / kappa;

        return new SimpleMatrix(new double[][] { { drddrho, drdphi0, drdkappa, drddz, drdtanL },
                { dphiddrho, dphidphi0, dphidkappa, dphiddz, dphidtanL },
                { dzddrho, dzdphi0, dzdkappa, dzddz, dzdtanL } });
    }

    /**
     * R is the covariance error for a measurement point.
     * 
     * @param m measurement point
     * @return
     */
    public SimpleMatrix R(SimpleMatrix z) {
        double deltaR = 70. / z.get(0, 0) * 5;
        double deltaPhi = Math.toRadians(1.) * 5;
        double deltaZ = 2 * 5; 

        return new SimpleMatrix(
                new double[][] { { deltaR * deltaR, 0, 0 }, { 0, deltaPhi * deltaPhi, 0 }, { 0, 0, deltaZ * deltaZ } });
    }

    /**
     * R is the covariance error for beamline.
     * 
     * @return
     */
    private SimpleMatrix R_beam() {
        double deltaR = 0.2;
        double deltaPhi = 2*Math.PI;
        double deltaZ = 200.;

        return new SimpleMatrix(
                new double[][] { { deltaR * deltaR, 0, 0 }, { 0, deltaPhi * deltaPhi, 0 }, { 0, 0, deltaZ * deltaZ } });
    }

    /**
     * Given a function f, and given distinct initial points ax and bx, this routine
     * searches in the downhill direction (defined by the function as evaluated at
     * the initial points) and returns new points ax, bx, cx that bracket a minimum
     * of the function. Also returned are the function values at the three points,
     * fa, fb, and fc. Use the implementation found in Numerical Recipes : The art
     * of scientific computing (2007).
     * 
     * @param a
     * @param b
     * @param f
     * @param kappa
     * @return
     */
    private double[] bracket(double a, double b, Function f, double kappa) {
        final double GOLD = 1.618034, GLIMIT = 100.0, TINY = 1.0e-20;
        double ax, bx, cx, fa, fb, fc;
        double fu, tmp;
        ax = a;
        bx = b;
        fa = f.calc(ax, kappa);
        fb = f.calc(bx, kappa);
        if (fb > fa) {
            tmp = ax;
            ax = bx;
            bx = tmp;
            tmp = fb;
            fb = fa;
            fa = tmp;
        }
        cx = bx + GOLD * (bx - ax);
        fc = f.calc(cx, kappa);
        while (fb > fc) {
            double r = (bx - ax) * (fb - fc);
            double q = (bx - cx) * (fb - fa);
            double u = bx - ((bx - cx) * q - (bx - ax) * r) / (2.0 * SIGN(Math.max(Math.abs(q - r), TINY), q - r));
            double ulim = bx + GLIMIT * (cx - bx);
            if ((bx - u) * (u - cx) > 0.0) {
                fu = f.calc(u, kappa);
                if (fu < fc) {
                    ax = bx;
                    bx = u;
                    fa = fb;
                    fb = fu;
                    return new double[] { ax, bx, cx };
                } else if (fu > fb) {
                    cx = u;
                    fc = fu;
                    return new double[] { ax, bx, cx };
                }
                u = cx + GOLD * (cx - bx);
                fu = f.calc(u, kappa);
            } else if ((cx - u) * (u - ulim) > 0.0) {
                fu = f.calc(u, kappa);
                if (fu < fc) {
                    bx = cx;
                    cx = u;
                    u = u + GOLD * (u - cx);
                    fb = fc;
                    fc = fu;
                    fu = f.calc(u, kappa);
                }
            } else if ((u - ulim) * (ulim - cx) >= 0.0) {
                u = ulim;
                fu = f.calc(u, kappa);
            } else {
                u = cx + GOLD * (cx - bx);
                fu = f.calc(u, kappa);
            }
            ax = bx;
            bx = cx;
            cx = u;
            fa = fb;
            fb = fc;
            fc = fu;
        }
        return new double[] { ax, bx, cx };
    }

    /**
     * Given a function f, and given a bracketing triplet of abscissas ax, bx, cx
     * (such that bx is between ax and cx, and f(bx) is less than both f(ax) and
     * f(cx)), this routine isolates the minimum to a fractional precision of about
     * tol using Brent’s method. The abscissa of the minimum is returned as xmin.
     * Use the implementation found in Numerical Recipes : The art of scientific
     * computing (2007).
     * 
     * @param bracket [ax,bx,cx]
     * @param f       function
     * @param kappa   parameter
     * @return minimum of the function
     * @throws Exception
     */
    private double minimize(double[] bracket, Function f, double kappa) throws Exception {
        int ITMAX = 100;
        double ZEPS = Math.ulp(1.0);
        double a, b, d = 0, etemp, fu, fv, fw, fx, p, q, r, tol1, tol2, u, v, w, x, xm;
        double e = 0.0;
        final double CGOLD = 0.3819660;
        double ax, bx, cx;
        ax = bracket[0];
        bx = bracket[1];
        cx = bracket[2];

        a = Math.min(ax, cx);
        b = Math.max(ax, cx);
        x = w = v = bx;
        fw = fv = fx = f.calc(x, kappa);
        for (int iter = 0; iter < ITMAX; iter++) {
            xm = 0.5 * (a + b);
            tol2 = 2.0 * (tol1 = 1.0E-10 * Math.abs(x) + ZEPS);
            if (Math.abs(x - xm) <= (tol2 - 0.5 * (b - a))) {
                return x;
            }
            if (Math.abs(e) > tol1) {
                r = (x - w) * (fx - fv);
                q = (x - v) * (fx - fw);
                p = (x - v) * q - (x - w) * r;
                q = 2.0 * (q - r);
                if (q > 0.0)
                    p = -p;
                q = Math.abs(q);
                etemp = e;
                e = d;
                if (Math.abs(p) >= Math.abs(0.5 * q * etemp) || p <= q * (a - x) || p >= q * (b - x))
                    d = CGOLD * (e = (x >= xm ? a - x : b - x));
                else {
                    d = p / q;
                    u = x + d;
                    if (u - a < tol2 || b - u < tol2)
                        d = SIGN(tol1, xm - x);
                }
            } else {
                d = CGOLD * (e = (x >= xm ? a - x : b - x));
            }
            u = (Math.abs(d) >= tol1 ? x + d : x + SIGN(tol1, d));
            fu = f.calc(u, kappa);
            if (fu <= fx) {
                if (u >= x)
                    a = x;
                else
                    b = x;
                v = w;
                w = x;
                x = u;
                fv = fw;
                fw = fx;
                fx = fu;
            } else {
                if (u < x)
                    a = u;
                else
                    b = u;
                if (fu <= fw || w == x) {
                    v = w;
                    w = u;
                    fv = fw;
                    fw = fu;
                } else if (fu <= fv || v == x || v == w) {
                    v = u;
                    fv = fu;
                }
            }
        }
        throw new Exception("Maximum number of iterations exceeded in brent");
    }

    /**
     * Using Brent’s method, return the root of a function f known to lie between x1
     * and x2. The root will be refined until its accuracy is tol. Use the
     * implementation found in Numerical Recipes : The art of scientific computing
     * (2007).
     * 
     * @param f     function with the interface
     * @param kappa a parameter of the function f
     * @param x1    left hand of the bracket
     * @param x2    right hand of the bracket
     * @return root of f
     * @throws Exception
     */
    private double root_finding(Function f, double kappa, double x1, double x2) throws Exception {
        int ITMAX = 10000;
        double EPS = Math.ulp(1.0);
        double a = x1, b = x2, c = x2, d = Double.MAX_VALUE, e = Double.MAX_VALUE, fa = f.calc(a, kappa),
                fb = f.calc(b, kappa), fc, p, q, r, s, tol1, xm;
        if ((fa > 0.0 && fb > 0.0) || (fa < 0.0 && fb < 0.0))
            throw new Exception("No roots between upper limit and lower limit.");
        fc = fb;
        for (int iter = 0; iter < ITMAX; iter++) {
            if ((fb > 0.0 && fc > 0.0) || (fb < 0.0 && fc < 0.0)) {
                c = a;
                fc = fa;
                e = d = b - a;
            }
            if (Math.abs(fc) < Math.abs(fb)) {
                a = b;
                b = c;
                c = a;
                fa = fb;
                fb = fc;
                fc = fa;
            }
            tol1 = 2.0 * EPS * Math.abs(b) + 0.5 * 1.0E-20;
            xm = 0.5 * (c - b);
            if (Math.abs(xm) <= tol1 || fb == 0.0)
                return b;
            if (Math.abs(e) >= tol1 && Math.abs(fa) > Math.abs(fb)) {
                s = fb / fa;
                if (a == c) {
                    p = 2.0 * xm * s;
                    q = 1.0 - s;
                } else {
                    q = fa / fc;
                    r = fb / fc;
                    p = s * (2.0 * xm * q * (q - r) - (b - a) * (r - 1.0));
                    q = (q - 1.0) * (r - 1.0) * (s - 1.0);
                }
                if (p > 0.0)
                    q = -q;
                p = Math.abs(p);
                double min1 = 3.0 * xm * q - Math.abs(tol1 * q);
                double min2 = Math.abs(e * q);
                if (2.0 * p < (Math.min(min1, min2))) {
                    e = d;
                    d = p / q;
                } else {
                    d = xm;
                    e = d;
                }
            } else {
                d = xm;
                e = d;
            }
            a = b;
            fa = fb;
            if (Math.abs(d) > tol1)
                b += d;
            else
                b += SIGN(tol1, xm);
            fb = f.calc(b, kappa);
        }
        throw new Exception("Maximum number of iterations exceeded in root_finding");
    }

    /**
     * Returns the magnitude of a times the sign of b
     * 
     * @param a
     * @param b
     * @return
     */
    private double SIGN(double a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    /**
     * Returns the pathlength between phi_start and phi_end.
     * 
     * @param kappa
     * @param tanL
     * @param alpha
     * @param phi_start in rad
     * @param phi_end   in rad
     * @return pathlength in mm
     */
    private double pathLength(double kappa, double tanL, double alpha, double phi_start, double phi_end) {

        return Math.sqrt(Math.pow(alpha, 2) * (Math.pow(tanL, 2) + 1) / Math.pow(kappa, 2))
                * Math.abs((phi_end - phi_start));
    }

    /**
     * Compute the changement of kappa due to energy loss.
     * 
     * @param kappa
     * @param tanL
     * @param pathLength in mm
     * @param material   Deuterium, Kapton, HeCO2, Mylar
     * @param dir        Forward, Backward
     * @return correct kappa
     * @throws Exception
     */
    public double energyLoss(double kappa, double tanL, double pathLength, String material, int dir) throws Exception {

        double z = 1; // charge of particle in unit of electron charge
        double M = 938.272; // mass of the particle in MeV
        double me = 0.511; // electron mass in MeV

        double P = Math.sqrt((tanL * tanL + 1) / (kappa * kappa)) * 1000;
        double BeGa = P / M;
        double Gamma = Math.sqrt(BeGa * BeGa + 1);
        double Beta = P / (M * Gamma);

        double K = 0.307075;

        double ZA = 0;
        double rho = 0;
        double I = 0;

        if (Objects.equals(material, "Deuterium")) {
            I = 19.2 * 1e-6; // MeV
            ZA = 0.5; 
            rho = 0.18; // g/cm^-3
        } else if (Objects.equals(material, "Kapton")) {
            ZA = 0.51264;
            I = 79.6 * 1e-6; // MeV
            rho = 1.420; // g/cm^-3
        } else if (Objects.equals(material, "HeCO2")) {
            ZA = 0.49984;
            I = 77.4 * 1e-6; // MeV
            rho = 0.000499; // g/cm^-3
        } else if (Objects.equals(material, "Mylar")) {
            ZA = 0.52037;
            I = 78.7 * 1e-6; // MeV
            rho = 1.400; // g/cm^-3
        } else {
            throw new Exception("Matrial name is wrong !!");
        }

        double Wmax = (2 * me * Math.pow(Beta * Gamma, 2)) / (1 + 2 * Gamma * (me / M) + Math.pow((me / M), 2));
        double x = rho * pathLength / 10;
        double LogTerm = Math.log(2 * me * BeGa * BeGa * Wmax / (I * I));
        double dEdx = K * z * z * ZA * (1. / (Beta * Beta)) * (0.5 * LogTerm - (Beta * Beta));
        double dE = x * dEdx;
    
        // if (debug) System.out.println("dE: " + dE);

        double E = Math.sqrt(P * P + M * M); // in MeV

        double kappa_prim = kappa + dE / E * kappa;
        

        if (dir == -1) {
            kappa_prim = kappa - dE / E * kappa;
            
        }

        // System.out.println("kappa: " + kappa + " kappa_prim: " + kappa_prim + " deltaK: " + (kappa - kappa_prim));


        return kappa_prim;
    }

    /**
     * Compute alpha = 1/(cB) for the position of a measurement point.
     * 
     * @param m measurement point
     * @return alpha
     */
    public double computeAlpha(SimpleMatrix m) {
        double x = m.get(0, 0) * Math.cos(m.get(1, 0));
        double y = m.get(0, 0) * Math.sin(m.get(1, 0));
        double z = m.get(2, 0);

        float[] b = new float[3];
        swim.BfieldLab(x / 10, y / 10, z / 10, b);
        double Bx = b[0];
        double By = b[1];
        double Bz = b[2];

        double c = 0.000299792458;
        double B = -Math.sqrt(Bx * Bx + By * By + Bz * Bz);

        return 1. / (c * B);
    }

    /**
     * Transform a state vector and a measurement point into x,y,z,px,py,pz.
     * 
     * @param sv state vector (drho,phi0,kappa,dz,tanL)
     * @param m  measurement point (r,phi,z)
     * @return [x,y,z,px,py,pz]
     */
    public double[] t2(StateVector stateVector, SimpleMatrix m) {

        double drho = stateVector.a.get(0, 0);
        double phi0 = stateVector.a.get(1, 0);
        double kappa = stateVector.a.get(2, 0);
        double dz = stateVector.a.get(3, 0);
        double tanL = stateVector.a.get(4, 0);

        double r_m = m.get(0, 0);
        double phi_m = m.get(1, 0);
        double z_m = m.get(2, 0);

        double alpha = computeAlpha(m);
        double phi = stateVector.phi;

        double x = r_m * Math.cos(phi_m) + drho * Math.cos(phi0)
                + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
        double y = r_m * Math.sin(phi_m) + drho * Math.sin(phi0)
                + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
        double z = z_m + dz - alpha / kappa * tanL * phi;
        double px = -1. / kappa * Math.sin(phi0 + phi);
        double py = 1. / kappa * Math.cos(phi0 + phi);
        double pz = 1. / kappa * tanL;

        return new double[] { x, y, z, px, py, pz };
    }

    /**
     * Transform the 3D points of the track into a list of SimpleMatrix.
     * 
     * @param TID track ID
     */
    public void MeasurementsList(int TID) {
        for (int i = 0; i < recotrackmap.get(TID).size(); i++) {
            SimpleMatrix measvector = new SimpleMatrix(new double[][] { { recotrackmap.get(TID).get(i).r() },
                    { recotrackmap.get(TID).get(i).phi() }, { recotrackmap.get(TID).get(i).z() } });
            measVectorList.add(measvector);
        }
        measVectorList.add(0,
                new SimpleMatrix(new double[][] { { 0 }, { 0 }, { finaltrackinfomap.get(TID).get_vz() } }));
    }

    /**
     * Fill the first element of update lists with information for the global fit.
     * 
     * @param TID track ID
     */
    public void StateVectorsList(int TID) {
        double R = finaltrackinfomap.get(TID).get_R();
        double drho = finaltrackinfomap.get(TID).get_d0();
        double phi0 = mod2PI(Math.toRadians(finaltrackinfomap.get(TID).get_phi()) - Math.PI / 2);
        double dz = 0.01;
        double tanL = finaltrackinfomap.get(TID).get_tanL();
        double alpha = -1. / (0.0003 * 3.72);
        double kappa = alpha / R;

        SimpleMatrix a_plus = new SimpleMatrix(new double[][] { { drho }, { phi0 }, { kappa }, { dz }, { tanL } });

        SimpleMatrix P_plus = new SimpleMatrix(new double[][] { { 0.1, 0, 0, 0, 0 }, { 0, 0.02, 0, 0, 0 },
                { 0, 0, 0.1, 0, 0 }, { 0, 0, 0, 0.1, 0 }, { 0, 0, 0, 0, 0.005 } });

        stateVectorPredictList.add(null);
        stateCovarianceMatrixPredictList.add(null);

        stateVectorUpdateList.add(new StateVector(a_plus, 0));
        stateCovarianceMatrixUpdateList.add(P_plus);
    }

    /**
     * Fill the backward lists with stateVectorUpdateList.size() elements null. And
     * put last update state vector in the last slot of backward update list.
     * 
     */
    private void fillBackwardLists() {
        int len = stateVectorUpdateList.size();
        backStateVectorPredictList = new ArrayList<>(Collections.nCopies(len, null));
        backCovarianceMatrixPredictList = new ArrayList<>(Collections.nCopies(len, null));
        backStateVectorUpdateList = new ArrayList<>(Collections.nCopies(len, null));
        backCovarianceMatrixUpdateList = new ArrayList<>(Collections.nCopies(len, null));

        backStateVectorUpdateList.set(len - 1, stateVectorUpdateList.get(len - 1));
        backCovarianceMatrixUpdateList.set(len - 1, stateCovarianceMatrixUpdateList.get(len - 1));
    }

    /**
     * Remove from measurement list same hits.
     * 
     */
    private void removeMultiHits() {

        if (debug) {
            for (SimpleMatrix hit : measVectorList){
                System.out.println("r: " + hit.get(0,0) + " phi: " + hit.get(1,0) + " z: " + hit.get(2,0));
            }
        }

        ArrayList<SimpleMatrix> temp = new ArrayList<>();

        temp.add(measVectorList.get(0));

        for (SimpleMatrix hit : measVectorList){
            int len = temp.size();
            SimpleMatrix tem = temp.get(len-1);

            if (hit.get(0, 0) != tem.get(0, 0) && hit.get(1, 0) != tem.get(1, 0) && hit.get(2, 0) != tem.get(2, 0)) {
                temp.add(hit);
            }

        }

        if (debug) {
            for (SimpleMatrix hit : temp){
                System.out.println("r: " + hit.get(0,0) + " phi: " + hit.get(1,0) + " z: " + hit.get(2,0));
            }
        }


        measVectorList = temp;
    }

    private void removeMultiHitsv2(){
        if (debug) {
            for (SimpleMatrix hit : measVectorList){
                System.out.println("r: " + hit.get(0,0) + " phi: " + hit.get(1,0) + " z: " + hit.get(2,0));
            }
        }
        System.out.println("");

        Set<SimpleMatrix> set = new LinkedHashSet<>();
  
        // Add the elements to set
        set.addAll(measVectorList);
  
        // Clear the list
        measVectorList.clear();
  
        // add the elements of set
        // with no duplicates to the list
        measVectorList.addAll(set);

        if (debug) {
            for (SimpleMatrix hit : measVectorList){
                System.out.println("r: " + hit.get(0,0) + " phi: " + hit.get(1,0) + " z: " + hit.get(2,0));
            }
        }
        System.out.println("");
    }

    /**
     * Clear all list for a new iteration of the Kalman Filter. First backward state
     * vector and covariance matrix are put as first in the update lists.
     * 
     */
    private void clearAndFillLists() {
        stateVectorUpdateList.clear();
        stateVectorPredictList.clear();
        stateCovarianceMatrixPredictList.clear();
        stateCovarianceMatrixUpdateList.clear();
        stateVectorPredictList.add(null);
        stateCovarianceMatrixPredictList.add(null);
        StateVector sv_first = new StateVector(backStateVectorUpdateList.get(0));
        SimpleMatrix P_first = backCovarianceMatrixUpdateList.get(0).copy();
        stateVectorUpdateList.add(sv_first);
        stateCovarianceMatrixUpdateList.add(P_first);
        backStateVectorUpdateList.clear();
        backCovarianceMatrixUpdateList.clear();
    }

    /**
     * Write information on the electron, global fit and kalman filter. Write p_kf,
     * theta_kf, phi_kf, vz_kf, p_gf, theta_gf, phi_gf, vz_gf, p_e, theta_e, phi_e.
     * 
     * @param TID   track ID
     * @param event for electron
     * @throws IOException
     */
    private void writeData(int TID, DataEvent event) throws IOException {
        Writer writ = new FileWriter("test.dat", true);
        SimpleMatrix a_plus = stateVectorUpdateList.get(0).a.copy();
        double phii = stateVectorUpdateList.get(0).phi;
        SimpleMatrix m = measVectorList.get(0);
        double phi0 = a_plus.get(1, 0);
        double kappa = a_plus.get(2, 0);
        double dz = a_plus.get(3, 0);
        double tanL = a_plus.get(4, 0);
        double alpha = computeAlpha(m);
        double vz = m.get(2, 0) + dz - alpha / kappa * tanL * phii;
        double px = -1 / kappa * Math.sin(phi0 + phii);
        double py = 1 / kappa * Math.cos(phi0 + phii);
        double pz = 1 / kappa * tanL;
        double p = Math.sqrt(px * px + py * py + pz * pz);
        double phi = Math.atan2(py, px);
        double theta = Math.acos((pz * 1000) / (p * 1000));
        writ.write("" + p + ',' + theta + ',' + phi + ',' + vz + ',');
        double px_gf = finaltrackinfomap.get(TID).get_px();
        double py_gf = finaltrackinfomap.get(TID).get_py();
        double pz_gf = finaltrackinfomap.get(TID).get_pz();
        double theta_gf = Math.toRadians(finaltrackinfomap.get(TID).get_theta());
        double phi_gf = Math.toRadians(finaltrackinfomap.get(TID).get_phi());
        double vz_gf = finaltrackinfomap.get(TID).get_vz();
        writ.write("" + Math.sqrt(px_gf * px_gf + py_gf * py_gf + pz_gf * pz_gf) / 1000 + ',' + theta_gf + ',' + phi_gf
                + ',' + vz_gf + ',');
        double theta_e = 0;
        double phi_e = 0;
        double p_e = 0;
        double vz_e = 0;
        DataBank REC_Particle = event.getBank("REC::Particle");
        boolean electron = false;
        int num_part_rows = REC_Particle.rows();
        for (int i = 0; i < num_part_rows; i++) {
            vz_e = REC_Particle.getFloat("vz", i);
            double px_e = REC_Particle.getFloat("px", i);
            double py_e = REC_Particle.getFloat("py", i);
            double pz_e = REC_Particle.getFloat("pz", i);
            p_e = Math.sqrt(px_e * px_e + py_e * py_e + pz_e * pz_e);
            theta_e = Math.atan2(Math.hypot(px_e, py_e), pz_e);
            phi_e = Math.atan2(py_e, px_e);
            if (REC_Particle.getInt("pid", i) == 11) {
                electron = true;
            }

            if (electron)
                break;
        }
        writ.write("" + p_e + ',' + theta_e + ',' + phi_e + ',' + vz_e + '\n');
        writ.close();
    }

    /**
     * Clear all list for a new track.
     * 
     */
    private void clearLists() {
        stateVectorUpdateList.clear();
        stateVectorPredictList.clear();
        stateCovarianceMatrixPredictList.clear();
        stateCovarianceMatrixUpdateList.clear();
        backStateVectorUpdateList.clear();
        backCovarianceMatrixUpdateList.clear();
        measVectorList.clear();
    }

    /**
     * Return the angle between [0,2*pi]
     * 
     * @param x angle
     * @return angle between [0,2*pi]
     */
    private double mod2PI(double x) {
        while (x < 0)
            x += 2 * Math.PI;
        while (x > 2 * Math.PI)
            x -= 2 * Math.PI;
        return x;
    }

    /**
     * Return the angle between [-pi,pi]
     * 
     * @param x angle
     * @return angle between [-pi,pi]
     */
    private double modPI(double x) {
        while (x < -Math.PI)
            x += 2 * Math.PI;
        while (x > Math.PI)
            x -= 2 * Math.PI;
        return x;
    }

    /**
     * Interface of a function for minimize, root_finding and bracket
     * 
     */
    interface Function {
        double calc(double x, double kappa);
    }

    /**
     * Represents a state vector for the Kalman Filter.
     *
     */
    private class StateVector {

        SimpleMatrix a;
        double phi;
        double pathLength;

        public StateVector(SimpleMatrix a, double phi, double pathLength) {
            this.a = a.copy();
            this.phi = phi;
            this.pathLength = pathLength;

        }

        public StateVector(SimpleMatrix a, double phi) {
            this.a = a.copy();
            this.phi = phi;
            this.pathLength = 0;
        }

        public StateVector(StateVector otheStateVector) {
            this.a = otheStateVector.a.copy();
            this.phi = otheStateVector.phi;
            this.pathLength = otheStateVector.pathLength;
        }

        public double drho() {
            return this.a.get(0, 0);
        }

        public double phi0() {
            return this.a.get(1, 0);
        }

        public double kappa() {
            return this.a.get(2, 0);
        }

        public double dz() {
            return this.a.get(3, 0);
        }

        public double tanL() {
            return this.a.get(4, 0);
        }

        @Override
        public String toString() {
            return "drho: " + a.get(0, 0) + " phi0: " + a.get(1, 0) + " kappa: " + a.get(2, 0) + " dz: " + a.get(3, 0)
                    + " tanl: " + a.get(4, 0);
        }

        private void print(String name, SimpleMatrix m) {
            double[] start = t2(this, m);
            double x = m.get(0, 0) * Math.cos(m.get(1, 0));
            double y = m.get(0, 0) * Math.sin(m.get(1, 0));
            System.out.println(
                    "r: " + m.get(0, 0) + " phi: " + m.get(1, 0) + "z: " + m.get(2, 0) + " x: " + x + " y: " + y);
            System.out.println(name + ": " + Arrays.toString(start) + " r = " + Math.hypot(start[0], start[1]));
            System.out.println("drho: " + a.get(0, 0) + " phi0: " + a.get(1, 0) + " kappa: " + a.get(2, 0) + " dz: "
                    + a.get(3, 0) + " tanl: " + a.get(4, 0) + " phi: " + phi);
        }
    }

    /**
     * Function to test if the track is an elastic proton at 2GeV.
     * 
     * @param TID   track ID
     * @param event for having information on electron.
     * @return true if pass, false if not
     */
    private boolean passCuts(int TID, DataEvent event) {
        double vz_e = 0;
        double theta_e = 0;
        double phi_e = 0;
        double E3 = 0;
        double E1 = 2.1864;        // Energy beam (GeV)
        double M = 0.93827;        // proton mass (GeV)
        double m = 0.000510998950; // electron mass (GeV)

        DataBank REC_Particle = event.getBank("REC::Particle");
        boolean electron = false;
        int num_part_rows = REC_Particle.rows();
        for (int i = 0; i < num_part_rows; i++) {
            double px_e = REC_Particle.getFloat("px", 0);
            double py_e = REC_Particle.getFloat("py", 0);
            double pz_e = REC_Particle.getFloat("pz", 0);
            vz_e = REC_Particle.getFloat("vz", 0); // (cm)
            theta_e = Math.atan2(Math.hypot(px_e, py_e), pz_e);
            phi_e = Math.atan2(py_e, px_e);
            E3 = Math.sqrt(px_e*px_e + py_e*py_e + pz_e*pz_e);
            if (REC_Particle.getInt("pid", i) == 11) {
                electron = true;
            }

            if (electron)
                break;
        }
        double px_p = finaltrackinfomap.get(TID).get_px() / 1000;
        double py_p = finaltrackinfomap.get(TID).get_py() / 1000;
        double pz_p = finaltrackinfomap.get(TID).get_pz() / 1000;
        double vz_p = finaltrackinfomap.get(TID).get_vz() / 10;
        double chi2_p = finaltrackinfomap.get(TID).get_chi2();
        double num_hits = finaltrackinfomap.get(TID).get_numhits();
        double R = finaltrackinfomap.get(TID).get_R();
        double theta_p = finaltrackinfomap.get(TID).get_theta() * (Math.PI / 180.);
        double phi_p = finaltrackinfomap.get(TID).get_phi() * (Math.PI / 180.);

        double p_p = Math.sqrt(px_p * px_p + py_p * py_p + pz_p * pz_p);

        // Compute the variables :
        double delta_phi = Math.abs(phi_p - phi_e) - Math.PI;
        double delta_vz = vz_e - vz_p;
        // Compute the variables :
        double nu = E1 - E3;
        double Q2 = 4 * E1 * E3 * Math.pow(Math.sin(theta_e / 2), 2);
        double W2 = M * M - Q2 + 2. * M * nu;
        double x = Q2 / (2 * M * nu);

        // Cuts :
        boolean cut_R = R < 0;
        boolean cut_num_hits = num_hits > 15 && num_hits < 50;
        boolean cut_chi2 = chi2_p < 4;
        boolean cut_theta_p = theta_p < 1.6 && theta_p > 1.25;
        boolean cut_delta_vz = delta_vz < 0 && delta_vz > -7;
        boolean cut_vze = vz_e < 12 && vz_e > -18;
        boolean cut_vzp = vz_p < 12 && vz_p > -12;
        boolean cut_theta_e = theta_e > 0.1 && theta_e < 0.25;
        boolean cut_delta_phi = delta_phi > -0.7 && delta_phi < 0.7;
        boolean cut_p = p_p > 0.1;
        
        boolean cut_nu = nu > 0.01 && nu < 1.5;
        boolean cut_x = Math.log10(x) < -1.5;

        if (cut_R && cut_chi2 && cut_num_hits && cut_theta_p && cut_delta_vz && cut_delta_phi 
                && cut_x && cut_vze && cut_vzp && cut_theta_e && cut_p)
        {
            return true;
        }

        return false;
    }    
}
