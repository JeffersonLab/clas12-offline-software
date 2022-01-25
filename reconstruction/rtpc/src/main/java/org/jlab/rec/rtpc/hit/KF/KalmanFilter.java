package org.jlab.rec.rtpc.hit.KF;

import org.ejml.simple.SimpleMatrix;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.hit.*;

import java.util.*;
import java.util.stream.Collectors;

public class KalmanFilter {

    boolean print = false;

    HashMap<Integer, List<RecoHitVector>> recotrackmap;
    HashMap<Integer, FinalTrackInfo> finaltrackinfomap;
    HashMap<Integer, KalmanFilterTrackInfo> kftrackinfomap = new HashMap<>();

    HashMap<Integer, Site> sitemap;
    HashMap<Integer, State> statemap;
    HashMap<Integer, SimpleMatrix> matrixmap;
    HashMap<Integer, State> bstatemap;
    HashMap<Integer, SimpleMatrix> bmatrixmap;

    public KalmanFilter(HitParameters params, DataEvent event) {
        recotrackmap = params.get_recotrackmap();
        finaltrackinfomap = params.get_finaltrackinfomap();

        for (int TID : finaltrackinfomap.keySet()) {

                try {
                    fillHitMap(TID);
                    initializationStateMap(TID, event);
                    generateSites();


                    for (int i = 0; i < 3; i++) {
                        test3();
                    }

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

                    double vz = from.get_z() + dz - alpha / kappa * tanL * phi;
                    double px = -1. / kappa * Math.sin(phi0 + phi);
                    double py = 1. / kappa * Math.cos(phi0 + phi);
                    double pz = 1. / kappa * tanL;
                    double pt = Math.hypot(px, py);

                    double phii = Math.atan2(py, px);
                    double theta = Math.atan2(pt, pz);


                    KalmanFilterTrackInfo kalmanFilterTrackInfo = new KalmanFilterTrackInfo(px, py, pz, vz, theta, phii);
                    kftrackinfomap.put(TID, kalmanFilterTrackInfo);

                } catch (Exception ignored) {}

            try {
                sitemap.clear();
                statemap.clear();
                matrixmap.clear();
                bstatemap.clear();
                bmatrixmap.clear();
            } catch (Exception ignored) {}
        }

        params.set_kftrackinfomap(kftrackinfomap);
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
