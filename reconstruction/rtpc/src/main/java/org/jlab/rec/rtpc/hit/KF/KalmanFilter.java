package org.jlab.rec.rtpc.KF;

import org.ejml.simple.SimpleMatrix;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.PadVector;
import org.jlab.rec.rtpc.hit.RecoHitVector;

import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class KalmanFilter {

    boolean print = false;

    HashMap<Integer, List<RecoHitVector>> recotrackmap;
    HashMap<Integer, FinalTrackInfo> finaltrackinfomap;
    // HashMap<Integer, KalmanFilterTrackInfo> kftrackinfomap = new HashMap<>();

    HashMap<Integer, Site> sitemap;
    HashMap<Integer, State> statemap;
    HashMap<Integer, SimpleMatrix> matrixmap;
    HashMap<Integer, State> bstatemap;
    HashMap<Integer, SimpleMatrix> bmatrixmap;

    public KalmanFilter(HitParameters params, DataEvent event, double[] t) throws Exception {
        recotrackmap = params.get_recotrackmap();
        finaltrackinfomap = params.get_finaltrackinfomap();

        for (int TID : finaltrackinfomap.keySet()) {

            List<RecoHitVector> listhits = recotrackmap.get(TID);
            RecoHitVector hitvec = recotrackmap.get(TID).get(0);
            double tdiff = hitvec.dt();


            int num_hits = finaltrackinfomap.get(TID).get_numhits();
            double chi2 = finaltrackinfomap.get(TID).get_chi2();
            double vz = finaltrackinfomap.get(TID).get_vz();
            double max_radius = listhits.get(listhits.size()-1).r();

            // cuts chi2 : chi2 < 10
            // cuts num_hits : 15 < num_hits
            // cuts vz -18 < vz < 18
            // cuts max radius max_radius > 65
            // cuts tdiff -500 < tdiff < 500

            if (chi2 < 10 && num_hits > 15 && (vz < 18 && -18 < vz) && max_radius > 65 && (-500 < tdiff && tdiff < 500)) {

                try {
                    Writer w = null;
                    // System.out.println("p: " + p_pad + ',' + p_time + ',' + p_mc);
                    w = new FileWriter("output_test.dat", true);
                    w.write("" + num_hits + ' ' + chi2 + ' ' + vz + ' ' + max_radius + ' ' + tdiff + '\n');
                    w.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                t[0]++;

                try {
                    fillHitMap(TID);
                    initializationStateMap(TID, event);
                    generateSites();


                    for (int i = 0; i < 3; i++) {
                        test3();
                    }

                    t[1]++;

                    // Save data :
                    State state = statemap.get(0);
                    Site from = sitemap.get(0);
                    double drho = state.drho();
                    double phi0 = state.phi0();
                    double kappa = state.kappa();
                    double dz = state.dz();
                    double tanL = state.tanL();

                    double alpha = -895.6040867490954;
                    double phi = 0.0;

                    double x = from.get_x() + drho * Math.cos(phi0)
                            + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
                    double y = from.get_y() + drho * Math.sin(phi0)
                            + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
                    double z = from.get_z() + dz - alpha / kappa * tanL * phi;
                    double px = -1. / kappa * Math.sin(phi0 + phi);
                    double py = 1. / kappa * Math.cos(phi0 + phi);
                    double pz = 1. / kappa * tanL;
                    double pt = Math.hypot(px, py);

                    double p = Math.sqrt(px * px + py * py + pz * pz);
                    double phii = Math.atan2(py, px);
                    double theta = Math.atan2(pt, pz);


                    DataBank mc = event.getBank("MC::Particle");
                    int rows = mc.rows();
                    double mc_p = 0;
                    double mc_phi = 0;
                    double mc_theta = 0;
                    double mc_vz = 0;
                    for (int i = 0; i < rows; i++){
                        int PID = mc.getInt("pid",i);
                        if (PID == 2212) {
                            double mc_px = mc.getFloat("px", i);
                            double mc_py = mc.getFloat("py", i);
                            double mc_pz = mc.getFloat("pz", i);
                            mc_vz = mc.getFloat("vz", 0);
                            double mc_pt = Math.sqrt(mc_px * mc_px + mc_py * mc_py);
                            mc_p = Math.sqrt(mc_px * mc_px + mc_py * mc_py + mc_pz * mc_pz);
                            mc_phi = Math.atan2(mc_py, mc_px);
                            mc_theta = Math.atan2(mc_pt, mc_pz);
                        }
                    }

                    double px_gf = finaltrackinfomap.get(TID).get_px();
                    double py_gf = finaltrackinfomap.get(TID).get_py();
                    double pz_gf = finaltrackinfomap.get(TID).get_pz();
                    double phi_gf = Math.toRadians(finaltrackinfomap.get(TID).get_phi());
                    double theta_gf = Math.toRadians(finaltrackinfomap.get(TID).get_theta());
                    double z_gf = finaltrackinfomap.get(TID).get_vz();
                    double p_gf = Math.sqrt(px_gf * px_gf + py_gf * py_gf + pz_gf * pz_gf);

                    // System.out.println("test : " + p + ',' + phii + ','+ theta + ','+ z);

                    KalmanFilterTrackInfo kalmanFilterTrackInfo = new KalmanFilterTrackInfo(px, py, pz, vz, theta, phi);
                    kftrackinfomap.put(TID, kalmanFilterTrackInfo);

                    // Write them into a file :
                    Writer w = null;
                    try {
                        // System.out.println("p: " + p_pad + ',' + p_time + ',' + p_mc);
                        w = new FileWriter("output" + ".dat", true);
                        w.write("" + p + ' ' + phii + ' ' + theta + ' ' + z + ' ' + mc_p + ' ' + mc_phi + ' ' + mc_theta
                                + ' ' + mc_vz + ' ' + p_gf + ' ' + phi_gf + ' ' + theta_gf + ' ' + z_gf + '\n');
                        w.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("ratio = " + (t[1] / t[0]));

                sitemap.clear();
                statemap.clear();
                matrixmap.clear();
                bstatemap.clear();
                bmatrixmap.clear();
            }
        }

        params.set_kftrackinfomap(kftrackinfomap);
    }



    private void test() throws Exception {

        System.out.println("Starting test for forward propagation !!! ");

        State state = statemap.get(0);
        state.debug("start", sitemap.get(0));

        State state_target = state.moveToTest3(sitemap.get(0), sitemap.get(1), new Material("Deuterium"), true, true);
        State state_wall = state_target.moveToTest3(sitemap.get(1), sitemap.get(2), new Material("Kapton"), true, true);
        State state_buffer = state_wall.moveToTest3(sitemap.get(2), sitemap.get(3), new Material("HeCO2"), true, true);
        State state_foil = state_buffer.moveToTest3(sitemap.get(3), sitemap.get(4), new Material("Mylar"), true, true);
        State state_flow = state_foil.moveToTest3(sitemap.get(4), sitemap.get(5), new Material("HeCO2"), true, true);
        State state_cath = state_flow.moveToTest3(sitemap.get(5), sitemap.get(6), new Material("Mylar"), true, true);
        State state_meas = state_cath.moveToTest3(sitemap.get(6), sitemap.get(7), new Material("HeCO2"), true, true);

        statemap.put(7, state_meas);
        for (int k = 7; k < sitemap.size() - 1; k++) {
            state = statemap.get(k);
            Site from = sitemap.get(k);
            Site to = sitemap.get(k + 1);

            state.debug("start", from);

            Material material = new Material("HeCO2");

            State state_to = state.moveToTest3(from, to, material, true, true);

            statemap.put(k + 1, state_to);
        }

        fillbMap();

        for (int k = sitemap.size() - 1; k > 7; k--) {
            state = statemap.get(k);
            Site from = sitemap.get(k);
            Site to = sitemap.get(k - 1);

            state.debug("start", from);

            Material material = new Material("HeCO2");

            State state_to = state.moveToTest3(from, to, material, false, true);

            statemap.put(k - 1, state_to);
        }

        state_meas = state_cath.moveToTest3(sitemap.get(7), sitemap.get(6), new Material("HeCO2"), false, true);
        state_cath = state_flow.moveToTest3(sitemap.get(6), sitemap.get(5), new Material("Mylar"), false, true);
        state_flow = state_foil.moveToTest3(sitemap.get(5), sitemap.get(4), new Material("HeCO2"), false, true);
        state_foil = state_buffer.moveToTest3(sitemap.get(4), sitemap.get(3), new Material("Mylar"), false, true);
        state_buffer = state_wall.moveToTest3(sitemap.get(3), sitemap.get(2), new Material("HeCO2"), false, true);
        state_wall = state_target.moveToTest3(sitemap.get(2), sitemap.get(1), new Material("Kapton"), false, true);


    }


    private void test2() throws Exception {

        boolean debug = false;

        State state = statemap.get(0);
        state.debug("start", sitemap.get(0));

        State state_target = state.moveToBeam4(sitemap.get(0), sitemap.get(1), new Material("Deuterium"), true, debug);
        state_target.debug("target", sitemap.get(1));

        State state_wall = state_target.moveToBeam4(sitemap.get(1), sitemap.get(2), new Material("Kapton"), true, debug);
        state_wall.debug("wall", sitemap.get(2));

        State state_buffer = state_wall.moveToBeam4(sitemap.get(2), sitemap.get(3), new Material("HeCO2"), true, debug);
        state_buffer.debug("buffer", sitemap.get(3));

        State state_foil = state_buffer.moveToBeam4(sitemap.get(3), sitemap.get(4), new Material("Mylar"), true, debug);
        state_foil.debug("foil", sitemap.get(4));

        State state_flow = state_foil.moveToBeam4(sitemap.get(4), sitemap.get(5), new Material("HeCO2"), true, debug);
        state_flow.debug("flow", sitemap.get(5));

        State state_cath = state_flow.moveToBeam4(sitemap.get(5), sitemap.get(6), new Material("Mylar"), true, debug);
        state_cath.debug("cath", sitemap.get(6));

        State state_meas = state_cath.moveToBeam4(sitemap.get(6), sitemap.get(7), new Material("HeCO2"), true, debug);
        state_meas.debug("meas", sitemap.get(7));


        State meas = state_meas;

        for (int k = 7; k < sitemap.size() - 1; k++) {
            meas = meas.moveToBeam4(sitemap.get(k), sitemap.get(k+1), new Material("HeCO2"), true, debug);
            meas.debug("meas", sitemap.get(k+1));
        }

        // Back Propagation !!!!

        for (int k = sitemap.size() - 1; k > 7; k--) {
            meas = meas.moveToBeam4(sitemap.get(k), sitemap.get(k-1), new Material("HeCO2"), false, debug);
            meas.debug("back meas", sitemap.get(k-1));
        }



        State back_state_meas = meas.moveToBeam4(sitemap.get(7), sitemap.get(6), new Material("HeCO2"), false, debug);
        back_state_meas.debug("back meas", sitemap.get(6));

        State back_state_cath = back_state_meas.moveToBeam4(sitemap.get(6), sitemap.get(5), new Material("Mylar"), false, debug);
        back_state_cath.debug("back cath", sitemap.get(5));


        State back_state_flow = back_state_cath.moveToBeam4(sitemap.get(5), sitemap.get(4), new Material("HeCO2"), false, debug);
        back_state_flow.debug("back flow", sitemap.get(4));

        State back_state_foil = back_state_flow.moveToBeam4(sitemap.get(4), sitemap.get(3), new Material("Mylar"), false, debug);
        back_state_foil.debug("back foil", sitemap.get(3));

        State back_state_buffer = back_state_foil.moveToBeam4(sitemap.get(3), sitemap.get(2), new Material("HeCO2"), false, debug);
        back_state_buffer.debug("back buffer", sitemap.get(2));

        State back_state_wall = back_state_buffer.moveToBeam4(sitemap.get(2), sitemap.get(1), new Material("Kapton"), false, debug);
        back_state_wall.debug("back wall", sitemap.get(1));

        State back_start = back_state_wall.moveToBeam4(sitemap.get(1), sitemap.get(0), new Material("Deuterium"), false, debug);
        back_start.debug("back start", sitemap.get(0));

    }


    private void test3() throws Exception{
        boolean debug = false;

        State state = statemap.get(0);
        SimpleMatrix P = matrixmap.get(0);
        if (print) state.debug("start", sitemap.get(0));

        Group target = truc(state, sitemap.get(0), sitemap.get(1), new Material("Deuterium"), true, debug);
        Group wall = truc(target.state, sitemap.get(1), sitemap.get(2), new Material("Kapton"), true, debug);
        Group buffer = truc(wall.state, sitemap.get(2), sitemap.get(3), new Material("HeCO2"), true, debug);
        Group foil = truc(buffer.state, sitemap.get(3), sitemap.get(4), new Material("Mylar"), true, debug);
        Group flow = truc(foil.state, sitemap.get(4), sitemap.get(5), new Material("HeCO2"), true, debug);
        Group cath = truc(flow.state, sitemap.get(5), sitemap.get(6), new Material("Mylar"), true, debug);
        Group meas = truc(cath.state, sitemap.get(6), sitemap.get(7), new Material("HeCO2"), true, debug);

        State state_minus = meas.state;
        SimpleMatrix F = target.F.mult(wall.F).mult(buffer.F).mult(foil.F).mult(flow.F).mult(cath.F).mult(meas.F);
        SimpleMatrix Q = target.Q.plus(wall.Q).plus(buffer.Q).plus(foil.Q).plus(flow.Q).plus(cath.Q).plus(meas.Q);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);

        Site to = sitemap.get(7);
        SimpleMatrix h = state_minus.h(to);
        SimpleMatrix m = to.get_m();
        SimpleMatrix y = m.minus(h);
        y.set(1, 0, modPI(y.get(1, 0)));

        SimpleMatrix H = state_minus.H(to);
        SimpleMatrix R = to.R();
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        if (print) sv_plus.debug("update", to);

        statemap.put(7, sv_plus);
        matrixmap.put(7, P_plus);

        for (int k = 7; k < sitemap.size() - 1; k++) {
            truc2(k, debug);
        }

        fillbMap();

        // Back Propagation !!!!
        for (int k = sitemap.size() - 1; k > 7; k--) {
            truc3(k, debug);
        }
        lasttruc(debug);


    }

    private void truc2(int k, boolean debug) throws Exception {
        State state1 = statemap.get(k);
        SimpleMatrix P = matrixmap.get(k);
        Site from = sitemap.get(k);
        Site to = sitemap.get(k+1);

        State state_minus = state1.moveToBeam4(sitemap.get(k), sitemap.get(k+1), new Material("HeCO2"), true, debug);
        SimpleMatrix F = state1.FBeam4(from, to, new Material("HeCO2"), true);
        SimpleMatrix Q = F.mult(state1.Qms(state_minus, new Material("HeCO2"))).mult(F.transpose());


        if (print) state1.debug("meas", sitemap.get(k+1));
        // if (print) System.out.println("F = " + F);
        // if (print) System.out.println("Q = " + Q);
        // if (print) System.out.println("P = " + P);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);
        // if (print) System.out.println("P_minus = " + P_minus);

        SimpleMatrix h = state_minus.h(to);
        SimpleMatrix m = to.get_m();
        SimpleMatrix y = m.minus(h);
        y.set(1, 0, modPI(y.get(1, 0)));

        SimpleMatrix H = state_minus.H(to);
        SimpleMatrix R = to.R();
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        if (print) sv_plus.debug("update", to);

        statemap.put(k+1, sv_plus);
        matrixmap.put(k+1, P_plus);
    }

    private void truc3(int k, boolean debug) throws Exception {
        State state1 = statemap.get(k);
        SimpleMatrix P = matrixmap.get(k);
        Site from = sitemap.get(k);
        Site to = sitemap.get(k-1);

        // if (print) System.out.println("from = " + from);
        // if (print) System.out.println("to = " + to);

        State state_minus = state1.moveToBeam4(from, to, new Material("HeCO2"), false, debug);
        SimpleMatrix F = state1.FBeam4(from, to, new Material("HeCO2"), false);
        SimpleMatrix Q = F.mult(state1.Qms(state_minus, new Material("HeCO2"))).mult(F.transpose());


        if (print) state1.debug("meas", sitemap.get(k-1));
        // if (print) System.out.println("F = " + F);
        // if (print) System.out.println("Q = " + Q);
        // if (print) System.out.println("P = " + P);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);
        // if (print) System.out.println("P_minus = " + P_minus);

        SimpleMatrix h = state_minus.h(to);
        SimpleMatrix m = to.get_m();
        SimpleMatrix y = m.minus(h);
        y.set(1, 0, modPI(y.get(1, 0)));

        SimpleMatrix H = state_minus.H(to);
        SimpleMatrix R = to.R();
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));


        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        if (print) sv_plus.debug("update", to);

        bstatemap.put(k-1, sv_plus);
        bmatrixmap.put(k-1, P_plus);
    }

    private void lasttruc(boolean debug) throws Exception {
        State state = statemap.get(7);
        SimpleMatrix P = matrixmap.get(7);
        if (print) state.debug("start", sitemap.get(7));

        Group meas = truc(state, sitemap.get(7), sitemap.get(6), new Material("HeCO2"), false, debug);

        Group cath = truc(meas.state, sitemap.get(6), sitemap.get(5), new Material("Mylar"), false, debug);
        Group flow = truc(cath.state, sitemap.get(5), sitemap.get(4), new Material("HeCO2"), false, debug);
        Group foil = truc(flow.state, sitemap.get(4), sitemap.get(3), new Material("Mylar"), false, debug);
        Group buffer = truc(foil.state, sitemap.get(3), sitemap.get(2), new Material("HeCO2"), false, debug);
        Group wall = truc(buffer.state, sitemap.get(2), sitemap.get(1), new Material("Kapton"), false, debug);
        Group start = truc(wall.state, sitemap.get(1), sitemap.get(0), new Material("Deuterium"), false, debug);


        // Update :
        State state_minus = start.state;
        SimpleMatrix F = start.F.mult(wall.F).mult(buffer.F).mult(foil.F).mult(flow.F).mult(cath.F).mult(meas.F);
        SimpleMatrix Q = start.Q.plus(wall.Q).plus(buffer.Q).plus(foil.Q).plus(flow.Q).plus(cath.Q).plus(meas.Q);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);

        Site to = sitemap.get(0);
        SimpleMatrix h = state_minus.h(to);
        SimpleMatrix m = to.get_m();
        SimpleMatrix y = m.minus(h);
        y.set(1, 0, modPI(y.get(1, 0)));

        SimpleMatrix H = state_minus.H(to);
        SimpleMatrix R = to.R_beam();
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        if (print) sv_plus.debug("update", to);

        statemap.put(0, sv_plus);
        matrixmap.put(0, P_plus);

    }

    private Group truc(State state, Site from, Site to, Material material, boolean dir, boolean debug) throws Exception {
        State state_to = state.moveToBeam4(from, to, material, dir, debug);
        SimpleMatrix F = state.FBeam4(from, to, material, dir);
        SimpleMatrix Q = F.mult(state_to.Qms(state_to, material)).mult(F.transpose());

        if (print) state_to.debug("end", to);


        return new Group(state_to, F, Q);
    }







    private void forwardFirstPropagation() throws Exception {

        System.out.println("Starting test for forward propagation !!! ");

        State state = statemap.get(0);
        SimpleMatrix P = matrixmap.get(0);
        state.debug("start", sitemap.get(0));

        Group target = grouping(state, sitemap.get(0), sitemap.get(1), new Material("Deuterium"), true);
        Group wall = grouping(target.state, sitemap.get(1), sitemap.get(2), new Material("Kapton"), true);
        Group buffer = grouping(wall.state, sitemap.get(2), sitemap.get(3), new Material("HeCO2"), true);
        Group foil = grouping(buffer.state, sitemap.get(3), sitemap.get(4), new Material("Mylar"), true);
        Group flow = grouping(foil.state, sitemap.get(4), sitemap.get(5), new Material("HeCO2"), true);
        Group cath = grouping(flow.state, sitemap.get(5), sitemap.get(6), new Material("Mylar"), true);
        Group meas = grouping(cath.state, sitemap.get(6), sitemap.get(7), new Material("HeCO2"), true);

        SimpleMatrix F = target.F.mult(wall.F).mult(buffer.F).mult(foil.F).mult(flow.F).mult(cath.F).mult(meas.F);
        SimpleMatrix Q = target.Q.plus(wall.Q).plus(buffer.Q).plus(foil.Q).plus(flow.Q).plus(cath.Q).plus(meas.Q);



        // System.out.println("F = " + F);
        // System.out.println("test_F = " + testF());

        F = testF();

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);
        /* -------------|
        |               |
        |    UPDATE     |
        |               |
        | ------------- */

        /*
        State state_minus = meas.state;
        Site to = sitemap.get(7);
        SimpleMatrix h = state_minus.h(to);
        SimpleMatrix y = h.minus(to.get_m());
        // System.out.println("y = " + y);
        y.set(1, 0, modPI(y.get(1, 0)));

        // System.out.println("h = " + h + " m = " + to.get_m());

        SimpleMatrix H = state_minus.H(to);
        SimpleMatrix R = to.R().scale(10);
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        sv_plus.debug("update", to);


         */
        statemap.put(7, meas.state);
        matrixmap.put(7, P_minus);
    }

    private State prop_test(State state) throws Exception {

        State state_target = state.moveToTest2(sitemap.get(0), sitemap.get(1), new Material("Deuterium"), true);
        State state_wall = state_target.moveToTest2(sitemap.get(1), sitemap.get(2), new Material("Kapton"), true);
        State state_buffer = state_wall.moveToTest2(sitemap.get(2), sitemap.get(3), new Material("HeCO2"), true);
        State state_foil = state_buffer.moveToTest2(sitemap.get(3), sitemap.get(4), new Material("Mylar"), true);
        State state_flow = state_foil.moveToTest2(sitemap.get(4), sitemap.get(5), new Material("HeCO2"), true);
        State state_cath = state_flow.moveToTest2(sitemap.get(5), sitemap.get(6), new Material("Mylar"), true);

        return state_cath.moveToTest2(sitemap.get(6), sitemap.get(7), new Material("HeCO2"), true);
    }

    private SimpleMatrix testF() throws Exception {

        State state = statemap.get(0);

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = state.get_sv().copy();
        double phi = state.get_phi();

        for (int i = 0; i < 5; i++) {

            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = prop_test(state_p);
            SimpleMatrix sv_p_m = state_p_m.get_sv().copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = prop_test(state_m);
            SimpleMatrix sv_m_m = state_m_m.get_sv().copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = state.drho();
        double phi0 = state.phi0();
        double kappa = state.kappa();
        double tanL = state.tanL();

        State a_prim = prop_test(state);

        double alpha = sitemap.get(0).alpha();

        double drho_prim = a_prim.drho();
        double phi0_prim = a_prim.phi0();

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

        return new SimpleMatrix(new double[][]{
                {drho_prm_del_drho, drho_prm_del_phi0, drho_prm_del_kappa, drho_prm_del_dz, drho_prm_del_tanL},
                {dphi0_prm_del_drho, dphi0_prm_del_phi0, dphi0_prm_del_kappa, dphi0_prm_del_dz, dphi0_prm_del_tanL},
                dkappadx, {dz_prm_del_drho, dz_prm_del_phi0, dz_prm_del_kappa, dz_prm_del_dz, dz_prm_del_tanL},
                {0, 0, 0, 0, 1}});
    }

    private void testT(int k) throws Exception {
        State state = statemap.get(k);
        SimpleMatrix P = matrixmap.get(k);
        Site from = sitemap.get(k);
        Site to = sitemap.get(k + 1);

        state.debug("start", from);

        Material material = new Material("HeCO2");

        State state_to = state.moveToTest2(from, to, material, true);
        SimpleMatrix F = state.FTest2(from, to, material, true);
        SimpleMatrix Q = F.mult(state.Qms(state_to, material)).mult(F.transpose());

        state_to.debug("end", to);
        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);

        /*
        // Update :
        SimpleMatrix h = state_to.h(to);
        SimpleMatrix y = to.get_m().minus(h);
        // System.out.println("y = " + y);
        y.set(1, 0, modPI(y.get(1, 0)));

        // System.out.println("h = " + h + " m = " + to.get_m());

        SimpleMatrix H = state_to.H(to);
        SimpleMatrix R = to.R().scale(10);
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_to.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        sv_plus.debug("update", to);
         */

        statemap.put(k + 1, state_to);
        matrixmap.put(k + 1, P_minus);
    }

    private Group grouping(State state, Site from, Site to, Material material, boolean dir) throws Exception {
        State state_to = state.moveToTest2(from, to, material, dir);
        SimpleMatrix F = state.FTest2(from, to, material, dir);
        SimpleMatrix Q = F.mult(state.Qms(state_to, material)).mult(F.transpose());

        state_to.debug("end", to);
        System.out.println("F = " + F);
        System.out.println("Q = " + Q);

        return new Group(state_to, F, Q);
    }

    private void backPropagation(int k) throws Exception {


        State state = bstatemap.get(k);
        SimpleMatrix P = bmatrixmap.get(k);
        Site from = sitemap.get(k);
        Site to = sitemap.get(k - 1);

        state.debug("start", from);

        Material material = new Material("HeCO2");

        State state_to = state.moveToTest2(from, to, material, false);
        SimpleMatrix F = state.FTest2(from, to, material, false);
        SimpleMatrix Q = F.mult(state.Qms(state_to, material)).mult(F.transpose());

        state_to.debug("end", to);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);

        /*
        // Update :
        SimpleMatrix h = state_to.h(to);
        SimpleMatrix y = to.get_m().minus(h);
        // System.out.println("y = " + y);
        y.set(1, 0, modPI(y.get(1, 0)));

        // System.out.println("h = " + h + " m = " + to.get_m());

        SimpleMatrix H = state_to.H(to);
        SimpleMatrix R = to.R().scale(10);
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        SimpleMatrix a_plus = state_to.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.updatePhi(to);

        sv_plus.debug("update", to);

         */

        bstatemap.put(k - 1, state_to);
        bmatrixmap.put(k - 1, P_minus);

    }

    private void backFirstPropagation() throws Exception {
        State state = bstatemap.get(7);
        SimpleMatrix P = bmatrixmap.get(7);

        state.debug("start", sitemap.get(7));

        Group meas = grouping(state, sitemap.get(7), sitemap.get(6), new Material("HeCO2"), false);
        Group cath = grouping(meas.state, sitemap.get(6), sitemap.get(5), new Material("Mylar"), false);
        Group flow = grouping(cath.state, sitemap.get(5), sitemap.get(4), new Material("HeCO2"), false);
        Group foil = grouping(flow.state, sitemap.get(4), sitemap.get(3), new Material("Mylar"), false);
        Group buffer = grouping(foil.state, sitemap.get(3), sitemap.get(2), new Material("HeCO2"), false);
        Group wall = grouping(buffer.state, sitemap.get(2), sitemap.get(1), new Material("Kapton"), false);
        Group target = test2beam(state, sitemap.get(1), sitemap.get(0), new Material("Deuterium"));

        SimpleMatrix F = target.F.mult(wall.F).mult(buffer.F).mult(foil.F).mult(flow.F).mult(cath.F).mult(meas.F);
        SimpleMatrix Q = target.Q.plus(wall.Q).plus(buffer.Q).plus(foil.Q).plus(flow.Q).plus(cath.Q).plus(meas.Q);

        SimpleMatrix P_minus = (F.mult(P).mult(F.transpose())).plus(Q);

        Site to = sitemap.get(0);
        SimpleMatrix h = meas.state.h(to);
        SimpleMatrix y = to.get_m().minus(h);
        // System.out.println("y = " + y);
        y.set(1, 0, modPI(y.get(1, 0)));

        // System.out.println("h = " + h + " m = " + to.get_m());

        State state_minus = target.state;
        // state_minus.debug("Test !!!!", to);
        /*
        SimpleMatrix H = state_minus.H(to);
        double deltaR = 0.2;
        double deltaPhi = 2 * Math.PI;
        double deltaZ = 200.;

        SimpleMatrix R = new SimpleMatrix(
                new double[][]{{deltaR * deltaR, 0, 0}, {0, deltaPhi * deltaPhi, 0}, {0, 0, deltaZ * deltaZ}});
        SimpleMatrix S = H.mult(P_minus).mult(H.transpose()).plus(R);

        SimpleMatrix K = P_minus.mult(H.transpose().mult(S.invert()));

        // System.out.println("Ky = " + K.mult(y));

        SimpleMatrix a_plus = state_minus.get_sv().plus(K.mult(y));
        SimpleMatrix P_plus = P_minus.minus(K.mult(H).mult(P_minus));

        State sv_plus = new State(a_plus, 0);

        sv_plus.debug("update", to);

         */

        bstatemap.put(0, state_minus);
        bmatrixmap.put(0, P_minus);
    }

    private Group test2beam(State state, Site from, Site to, Material material) throws Exception {

        State state_to = state.moveToTestBeam(from, to, material, false, true);
        SimpleMatrix F = state.Fbeam(from, to, material);
        SimpleMatrix Q = F.mult(state.Qms(state_to, material)).mult(F.transpose());

        state_to.debug("end", to);

        return new Group(state_to, F, Q);

    }

    private void fillbMap() {
        int k = sitemap.size() - 1;
        State sv_last = statemap.get(k);
        SimpleMatrix P_last = matrixmap.get(k);

        bstatemap = new HashMap<>();
        bmatrixmap = new HashMap<>();

        bstatemap.put(k, sv_last);
        bmatrixmap.put(k, P_last);
    }

    /**
     * Create a HashMap with the reco hits.
     *
     * @param TID Track ID
     */
    private void fillHitMap(int TID) {
        sitemap = new HashMap<>();
        sitemap.put(0, new Site(0, 0, finaltrackinfomap.get(TID).get_vz()));
        int k = 7;
        for (RecoHitVector hit : recotrackmap.get(TID)) {
            sitemap.put(k, new Site(hit.r(), hit.phi(), hit.z()));
            k++;
        }
    }

    public void generateSites() throws Exception {
        State sv = statemap.get(0);
        Site from = sitemap.get(0);

        sitemap.put(1, sv.generateSite(from, 3.0000, new Material("Deuterium")));


        sitemap.put(2, sv.generateSite(from, 3.0550, new Material("Kapton")));
        sitemap.put(3, sv.generateSite(from, 19.994, new Material("HeCO2")));


        sitemap.put(4, sv.generateSite(from, 20.000, new Material("Mylar")));
        sitemap.put(5, sv.generateSite(from, 29.994, new Material("HeCO2")));
        sitemap.put(6, sv.generateSite(from, 30.000, new Material("Mylar")));

        sitemap = sortByValue(sitemap, true);

        sv.set_phi(0.0);



        /*
         */

        if (print) for (int s : sitemap.keySet()) System.out.println("s = " + sitemap.get(s));

        removeMultiHits();
        if (print) System.out.println();
        if (print) for (int s : sitemap.keySet()) System.out.println("s = " + sitemap.get(s));


    }

    private void removeMultiHits() {

        HashMap<Integer, Site> temp = new HashMap<>();

        temp.put(0, sitemap.get(0));

        for (int s : sitemap.keySet()){
            int len = temp.size();
            Site site = sitemap.get(s);
            Site tem = temp.get(len-1);


            if (site.get_r() != tem.get_r() && site.get_phi() != tem.get_phi() && site.get_z() != tem.get_z()) {
                temp.put(len,site);
            }

        }


        sitemap = temp;
    }

    /**
     * Create the first state vector base on the global fit and the first covariance matrix.
     *
     * @param TID   Track ID
     * @param event event (for mc only)
     */
    private void initializationStateMap(int TID, DataEvent event) {
        statemap = new HashMap<>();
        matrixmap = new HashMap<>();

        double R = finaltrackinfomap.get(TID).get_R();
        double drho = finaltrackinfomap.get(TID).get_d0();
        double phi0 = mod2PI(Math.toRadians(finaltrackinfomap.get(TID).get_phi()) - Math.PI / 2);
        double dz = 0.0001;
        double tanL = finaltrackinfomap.get(TID).get_tanL();
        double alpha = -895.6040867490954;
        double kappa = alpha / R;

        DataBank mc = event.getBank("MC::Particle");
        double mc_px = mc.getFloat("px", 0);
        double mc_py = mc.getFloat("py", 0);
        double mc_pz = mc.getFloat("pz", 0);
        double mc_vz = mc.getFloat("vz", 0);
        double mc_vx = 0;
        double mc_vy = 0;

        double mc_pt = Math.hypot(mc_px, mc_py);
        double mc_kappa = 1. / mc_pt;
        double mc_drho = 0.001;
        double mc_phi0 = mod2PI(Math.atan2(mc_py, mc_px) - Math.PI / 2); // - Math.PI / 2
        double mc_dz = 0.001;
        double mc_tanL = mc_pz * mc_kappa;

        // SimpleMatrix P = new SimpleMatrix(new double[][]{{0.1, 0, 0, 0, 0}, {0, 0.02, 0, 0, 0},
        //        {0, 0, 0.1, 0, 0}, {0, 0, 0, 0.1, 0}, {0, 0, 0, 0, 0.005}});

        SimpleMatrix P = new SimpleMatrix(new double[][]{{0.1, 0, 0, 0, 0}, {0, 0.02, 0, 0, 0},
                {0, 0, 0.1, 0, 0}, {0, 0, 0, 0.1, 0}, {0, 0, 0, 0, 0.005}}).scale(1);

        // statemap.put(0, new State(mc_drho, mc_phi0, mc_kappa, mc_dz, mc_tanL, 0, 0));
        statemap.put(0, new State(drho, phi0, kappa, dz, tanL, 0, 0));
        matrixmap.put(0, P);
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
     * Sort the sites list by value of their radius
     *
     * @param unsortMap HashMap of unsorted site
     * @param order     true = asc, false = dsc
     * @return sort HashMap
     */
    private static HashMap<Integer, Site> sortByValue(HashMap<Integer, Site> unsortMap, final boolean order) {
        List<Map.Entry<Integer, Site>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }

    private static class Group {
        public State state;
        public SimpleMatrix F;
        public SimpleMatrix Q;

        public Group(State state, SimpleMatrix f, SimpleMatrix q) {
            this.state = state;
            F = f;
            Q = q;
        }
    }

}
