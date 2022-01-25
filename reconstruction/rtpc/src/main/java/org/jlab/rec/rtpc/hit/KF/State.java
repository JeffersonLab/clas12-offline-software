package org.jlab.rec.rtpc.hit.KF;

import org.ejml.simple.SimpleMatrix;
import org.jlab.clas.swimtools.Swim;

public class State {

    private SimpleMatrix _sv;
    private double _phi;
    private double _pl;

    public State(double drho, double phi0, double kappa, double dz, double tanL, double phi, double pl) {
        this._sv = new SimpleMatrix(new double[][]{{drho}, {phi0}, {kappa}, {dz}, {tanL}});
        this._phi = phi;
        this._pl = pl;
    }

    public State(SimpleMatrix sv, double phi, double pl) {
        this._sv = sv.copy();
        this._phi = phi;
        this._pl = pl;
    }

    public State(SimpleMatrix sv, double phi) {
        this._sv = sv.copy();
        this._phi = phi;
        this._pl = 0;
    }

    public State(State otherState) {
        this._sv = otherState.get_sv().copy();
        this._phi = otherState.get_phi();
        this._pl = otherState.get_pl();
    }

    public Site generateSite(Site from, double _r, Material material) throws Exception {

        boolean debug = false;
        boolean dir = true;

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double alpha = -895.6040867490954;

        double phi_minus = this.get_phi();
        double[] r = r(from, phi_minus, kappa, alpha);
        double start = Math.hypot(r[0], r[1]);
        double end = _r;
        double n = 10;

        double pl = 0;
        if (debug) System.out.println("start = " + start);
        if (debug) System.out.println("start + 1n = " + (start + (end - start) / n));
        if (debug) System.out.println("end = " + end);

        boolean stop = false;

        double v = 1. / n * (end - start) / n;
        for (double i = start + (end - start) / n; i < end + v; i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);


            r = r(from, phi_minus, kappa, alpha);

            alpha = -895.6040867490954;

            double fi = i;
            double finalAlpha1 = alpha;
            Function func = (phi, kappa1) -> -fi + Math.sqrt(Math
                    .pow(finalAlpha1 * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(finalAlpha1 * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double phi_start = phi_minus;


            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + fi + " alpha = " + alpha);
                System.out.println("start : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

            if (debug) {
                r = r(from, phi_min, kappa, alpha);
                System.out.println("min : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            phi_minus = root_finding(func, kappa, phi_min, 3);



            double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);

            pl += pl_step;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

            if (debug) System.out.println("pl_step = " + pl_step);

            if (dir) {
                kappa += energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            } else {
                kappa -= energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            }

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

        }

        double finalAlpha = alpha;
        Function func = (phi, kappa1) -> -_r + Math.sqrt(Math
                .pow(finalAlpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(finalAlpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        double phii = root_finding(func, kappa, phi_min, 3);

        double x = from.get_x() + drho * Math.cos(phi0)
                + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phii));
        double y = from.get_y() + drho * Math.sin(phi0)
                + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phii));
        double z = from.get_z() + dz - alpha / kappa * tanL * phii;


        double r_prim = Math.hypot(x, y);
        double phi_prim = Math.atan2(y, x);

        this.set_phi(phii);

        return new Site(r_prim, phi_prim, z);

    }

    public State moveTo(Site from, Site to, Material material) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double phi_start = this.get_phi();

        Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double pl_s = 0;
        double min_pathLength = 1e-3;
        int ITMAX = 100;
        for (int iter = 0; iter < ITMAX; iter++) {
            double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
            double phi_minus = root_finding(func, kappa, phi_min, 3);
            double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
            // System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)) + " phi_min: " + phi_min + " phi_minus = " + phi_minus);
            // System.out.println("pl_step = " + pl_step);
            pl_s += pl_step;


            if (pl_step < min_pathLength)
                break;

            kappa = energyLoss(kappa, tanL, pl_step, material);
            phi_start = phi_minus;
            double x = from.get_x() + drho * Math.cos(phi0)
                    + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
            double y = from.get_y() + drho * Math.sin(phi0)
                    + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
            double z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
            System.out.println("r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);
        }

        double alpha1 = to.alpha();

        double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

        return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, 0, pl_s);

    }

    public State computePhi(Site from) throws Exception {
        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double alpha = from.alpha();
        double phi_start = this.get_phi();

        Function func = (phi, kappa1) -> -from.get_r() + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
        double phi_minus = root_finding(func, kappa, phi_min, 3);

        return new State(drho, phi0, kappa, dz, tanL, phi_minus, this._pl);
    }

    public State moveTob(Site from, Site to, Material material) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double pl = 0;
        double start = from.get_r();
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();
        for (double i = start; i > end; i -= (start - end) / n) {
            // System.out.println("i = " + i);

            if (i == 0) i = (end - start) / n;

            double finalI1 = i;
            Function func = (phi, kappa1) -> -finalI1 + Math.sqrt(Math
                    .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double pl_s = 0;
            double min_pathLength = 1e-3;
            int ITMAX = 100;
            double phi_start = phi_minus;
            // System.out.println("phi_start = " + phi_start);
            for (int iter = 0; iter < ITMAX; iter++) {

                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + finalI1);

                double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
                phi_minus = root_finding(func, kappa, phi_min, 3);
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
                // System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)) + " phi_min: " + phi_min + " phi_minus = " + phi_minus);
                // System.out.println("pl_step = " + pl_step);
                pl_s += pl_step;
                // System.out.println("kappa = " + kappa);
                double x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                double y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                double z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                // System.out.println( "r = " + Math.hypot(x,y) + " x = " + x + " y = " + y  + " z = " + z);

                if (pl_step < min_pathLength)
                    break;

                kappa = energyLossb(kappa, tanL, pl_step, material);
                phi_start = phi_minus;
            }

            pl += pl_s;

        }

        // System.out.println("pl = " + pl);

        double alpha1 = to.alpha();

        double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

        return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, 0, pl);

    }

    public State moveToBeam(Site from, Site to, Material material) throws Exception {
        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double pl = 0;
        double start = from.get_r();
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();
        for (double i = start; i > end; i -= (start - end) / n) {
            System.out.println("i = " + i);

            double finalI1 = i;
            Function func = (phi, kappa1) -> -finalI1 + Math.sqrt(Math
                    .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double pl_s = 0;
            double min_pathLength = 1e-3;
            int ITMAX = 100;
            double phi_start = phi_minus;
            // System.out.println("phi_start = " + phi_start);
            for (int iter = 0; iter < ITMAX; iter++) {

                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + finalI1);

                double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
                phi_minus = root_finding(func, kappa, phi_min, 3);
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
                // System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)) + " phi_min: " + phi_min + " phi_minus = " + phi_minus);
                // System.out.println("pl_step = " + pl_step);
                pl_s += pl_step;
                // System.out.println("kappa = " + kappa);
                double x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                double y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                double z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                // System.out.println( "r = " + Math.hypot(x,y) + " x = " + x + " y = " + y  + " z = " + z);

                if (pl_step < min_pathLength)
                    break;

                kappa = energyLossb(kappa, tanL, pl_step, material);
                phi_start = phi_minus;
            }

            pl += pl_s;

        }

        double alpha1 = to.alpha();

        double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

        Function func1 = (phi, kappa1) -> -end + Math.sqrt(Math
                .pow(alpha1 * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                + Math.pow(alpha1 * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));
        double phi = minimize(bracket(0, 1, func1, kappa), func1, kappa);


        return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi, pl);

    }

    public SimpleMatrix F(Site from, Site to, Material material) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = this._sv.copy();
        double phi = this._phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = state_p.moveTo(from, to, material);
            SimpleMatrix sv_p_m = state_p_m._sv.copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = state_m.moveTo(from, to, material);
            SimpleMatrix sv_m_m = state_m_m._sv.copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanL = this.tanL();

        State a_prim = moveTo(from, to, material);

        double alpha = from.alpha();

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

    public SimpleMatrix Fb(Site from, Site to, Material material) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = this._sv.copy();
        double phi = this._phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = state_p.moveTob(from, to, material);
            SimpleMatrix sv_p_m = state_p_m._sv.copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = state_m.moveTob(from, to, material);
            SimpleMatrix sv_m_m = state_m_m._sv.copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanL = this.tanL();

        State a_prim = moveTob(from, to, material);

        double alpha = from.alpha();

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

    public SimpleMatrix Fbeam(Site from, Site to, Material material) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = this._sv.copy();
        double phi = this._phi;

        for (int i = 0; i < 5; i++) {
            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = state_p.moveToBeam(from, to, material);
            SimpleMatrix sv_p_m = state_p_m._sv.copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = state_m.moveToBeam(from, to, material);
            SimpleMatrix sv_m_m = state_m_m._sv.copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanL = this.tanL();

        State a_prim = moveToBeam(from, to, material);

        double alpha = from.alpha();

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

    public SimpleMatrix Qms(State to, Material material) {

        double kappa = this.kappa();
        double tanL = this.tanL();

        double p = Math.sqrt((tanL * tanL + 1) / (kappa * kappa));
        double M = 0.938;
        double beta = p / Math.sqrt(p * p + M * M);
        double pathLength = to.get_pl() / 10; // cm
        double X0 = material.X0;

        double sctRMS = 0.0136 / (beta * p) * Math.sqrt(pathLength / X0)
                * (1 + 0.088 * Math.log10(pathLength / (X0 * beta * beta)));

        return new SimpleMatrix(new double[][]{{0, 0, 0, 0, 0}, {0, 1 + tanL * tanL, 0, 0, 0},
                {0, 0, (kappa * tanL) * (kappa * tanL), 0, kappa * tanL * (1 + tanL * tanL)}, {0, 0, 0, 0, 0},
                {0, 0, kappa * tanL * (1 + tanL * tanL), 0, (1 + tanL * tanL) * (1 + tanL * tanL)}})
                .scale(sctRMS * sctRMS);
    }

    public SimpleMatrix h(Site to) {
        double phi = this._phi;
        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();
        double x0 = to.get_x();
        double y0 = to.get_y();
        double z0 = to.get_z();
        double alpha = to.alpha();

        double x = x0 + drho * Math.cos(phi0) + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
        double y = y0 + drho * Math.sin(phi0) + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
        double z = z0 + dz - alpha / kappa * tanL * phi;

        return new SimpleMatrix(new double[][]{{Math.sqrt(x * x + y * y)}, {Math.atan2(y, x)}, {z}});
    }

    public SimpleMatrix H(Site to) {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanl = this.tanL();

        double alpha = to.alpha();
        double phi = this._phi;

        double xo = to.get_x();
        double yo = to.get_y();

        double a = Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) + kappa * (drho * Math.sin(phi0) + yo), 2)
                + Math.pow(
                alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) + kappa * (drho * Math.cos(phi0) + xo),
                2);
        double drddrho = (-alpha * Math.cos(phi) + alpha + drho * kappa + kappa * xo * Math.cos(phi0)
                + kappa * yo * Math.sin(phi0))
                / Math.sqrt(a);
        double drdphi0 = (-alpha * xo * Math.sin(phi0) + alpha * xo * Math.sin(phi + phi0) + alpha * yo * Math.cos(phi0)
                - alpha * yo * Math.cos(phi + phi0) - drho * kappa * xo * Math.sin(phi0)
                + drho * kappa * yo * Math.cos(phi0))
                / Math.sqrt(a);
        double drdkappa = alpha
                * (2 * alpha * Math.cos(phi) - 2 * alpha + drho * kappa * Math.cos(phi) - drho * kappa
                - kappa * xo * Math.cos(phi0) + kappa * xo * Math.cos(phi + phi0) - kappa * yo * Math.sin(phi0)
                + kappa * yo * Math.sin(phi + phi0))
                / (Math.pow(kappa, 2) * Math.sqrt(a));
        double drddz = 0;
        double drdtanL = 0;

        double dphiddrho = kappa * (alpha * Math.sin(phi) + kappa * xo * Math.sin(phi0) - kappa * yo * Math.cos(phi0))
                / (a);
        double dphidphi0 = (-2 * Math.pow(alpha, 2) * Math.cos(phi) + 2 * Math.pow(alpha, 2)
                - 2 * alpha * drho * kappa * Math.cos(phi) + 2 * alpha * drho * kappa
                + alpha * kappa * xo * Math.cos(phi0) - alpha * kappa * xo * Math.cos(phi + phi0)
                + alpha * kappa * yo * Math.sin(phi0) - alpha * kappa * yo * Math.sin(phi + phi0)
                + Math.pow(drho, 2) * Math.pow(kappa, 2) + drho * Math.pow(kappa, 2) * xo * Math.cos(phi0)
                + drho * Math.pow(kappa, 2) * yo * Math.sin(phi0))
                / (a);
        double dphidkappa = alpha
                * (drho * Math.sin(phi) - xo * Math.sin(phi0) + xo * Math.sin(phi + phi0) + yo * Math.cos(phi0)
                - yo * Math.cos(phi + phi0))
                / (a);
        double dphiddz = 0;
        double dphidtanL = 0;

        double dzddrho = 0;
        double dzdphi0 = 0;
        double dzdkappa = alpha * phi * tanl / Math.pow(kappa, 2);
        double dzddz = 1;
        double dzdtanL = -alpha * phi / kappa;

        return new SimpleMatrix(new double[][]{{drddrho, drdphi0, drdkappa, drddz, drdtanL},
                {dphiddrho, dphidphi0, dphidkappa, dphiddz, dphidtanL},
                {dzddrho, dzdphi0, dzdkappa, dzddz, dzdtanL}});
    }

    private double energyLoss(double kappa, double tanL, double d, Material material) {

        double z = 1; // charge of particle in unit of electron charge
        double M = 938.272; // mass of the particle in MeV
        double me = 0.511; // electron mass in MeV
        double P = Math.sqrt((tanL * tanL + 1) / (kappa * kappa)) * 1000;
        double BeGa = P / M;
        double Gamma = Math.sqrt(BeGa * BeGa + 1);
        double Beta = P / (M * Gamma);

        double K = 0.307075;

        double ZA = material.ZA;
        double rho = material.rho;
        double I = material.I;

        double Wmax = (2. * me * Math.pow(Beta * Gamma, 2)) / (1. + 2. * Gamma * (me / M)
                + Math.pow((me / M), 2));
        double x = rho * d / 10.;
        double LogTerm = Math.log(2. * me * BeGa * BeGa * Wmax / (I * I));
        double dEdx = K * z * z * ZA * (1. / (Beta * Beta)) * (0.5 * LogTerm - (Beta * Beta));
        double dE = x * dEdx;

        double E = Math.sqrt(P * P + M * M); // in MeV

        return kappa + dE / E * kappa;
    }

    private double energyLossb(double kappa, double tanL, double d, Material material) {

        double z = 1; // charge of particle in unit of electron charge
        double M = 938.272; // mass of the particle in MeV
        double me = 0.511; // electron mass in MeV
        double P = Math.sqrt((tanL * tanL + 1) / (kappa * kappa)) * 1000;
        double BeGa = P / M;
        double Gamma = Math.sqrt(BeGa * BeGa + 1);
        double Beta = P / (M * Gamma);

        double K = 0.307075;

        double ZA = material.ZA;
        double rho = material.rho;
        double I = material.I;

        double Wmax = (2. * me * Math.pow(Beta * Gamma, 2)) / (1. + 2. * Gamma * (me / M)
                + Math.pow((me / M), 2));
        double x = rho * d / 10.;
        double LogTerm = Math.log(2. * me * BeGa * BeGa * Wmax / (I * I));
        double dEdx = K * z * z * ZA * (1. / (Beta * Beta)) * (0.5 * LogTerm - (Beta * Beta));
        double dE = x * dEdx;

        double E = Math.sqrt(P * P + M * M); // in MeV

        return kappa - dE / E * kappa;
    }


    public State moveToTest(Site from, Site to, Material material, boolean dir) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double pl = 0;
        double start = from.get_r();
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();
        start += (end - start) / n;

        System.out.println("start = " + start);
        System.out.println("end = " + end);
        System.out.println("(end-start)/n = " + ((end - start) / n));

        double v = 1. / n * (end - start) / n;
        for (double i = start; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            System.out.println("i = " + i);

            double finalI1 = i;
            Function func = (phi, kappa1) -> -finalI1 + Math.sqrt(Math
                    .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double pl_s = 0;
            double min_pathLength = 1e-3;
            int ITMAX = 100;
            double phi_start = phi_minus;
            // System.out.println("phi_start = " + phi_start);
            for (int iter = 0; iter < ITMAX; iter++) {

                // System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + finalI1);
                double x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                double y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                double z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                System.out.println("start : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);

                double phi_min = minimize(bracket(0, 1, func, kappa), func, kappa);
                phi_minus = root_finding(func, kappa, phi_min, 3);
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
                // System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)) + " phi_min: " + phi_min + " phi_minus = " + phi_minus);
                // System.out.println("pl_step = " + pl_step);
                pl_s += pl_step;
                // System.out.println("kappa = " + kappa);

                phi_start = phi_minus;
                if (pl_step < min_pathLength) {
                    x = from.get_x() + drho * Math.cos(phi0)
                            + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                    y = from.get_y() + drho * Math.sin(phi0)
                            + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                    z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                    System.out.println("break : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);
                    break;
                }

                kappa += energyLossTest(kappa, tanL, pl_step, material);

                x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                System.out.println("end : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);
            }

            pl += pl_s;

        }


        double alpha1 = to.alpha();

        double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

        double pl2 = pathLength(kappa, tanL, alpha, phi0, phi0_prim);

        System.out.println("pl = " + pl + " pl2 = " + pl2);

        return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, 0, pl2);
    }

    public State moveToTest2(Site from, Site to, Material material, boolean dir) throws Exception {

        boolean debug = false;

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double x = from.get_x() + drho * Math.cos(phi0)
                + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + this._phi));
        double y = from.get_y() + drho * Math.sin(phi0)
                + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + this._phi));

        double pl = 0;
        double start = Math.sqrt(x * x + y * y);
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();
        start += (end - start) / n;

        if (debug) System.out.println("start = " + start);
        if (debug) System.out.println("end = " + end);
        if (debug) System.out.println("(end-start)/n = " + ((end - start) / n));

        double v = 1. / n * (end - start) / n;
        for (double i = start; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);

            double finalI1 = i;
            Function func = (phi, kappa1) -> -finalI1 + Math.sqrt(Math
                    .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double pl_s = 0;
            double min_pathLength = 1e-3;
            int ITMAX = 100;
            double phi_start = phi_minus;
            // System.out.println("phi_start = " + phi_start);
            for (int iter = 0; iter < ITMAX; iter++) {

                // System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + finalI1);
                x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                double z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                if (debug) System.out.println("start : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);

                double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

                x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_min));
                y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_min));
                z = from.get_z() + dz - alpha / kappa * tanL * phi_min;
                if (debug) System.out.println("min : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);

                if (Math.hypot(x, y) > i) {
                    double alpha1 = to.alpha();
                    double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
                    double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
                    double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
                    double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
                    double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

                    Function func1 = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                            .pow(alpha1 * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                            + Math.pow(alpha1 * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

                    double phi_min1 = minimize(bracket(0, 0.5, func, kappa), func, kappa);
                    double pl2 = pathLength(kappa, tanL, alpha, phi0, phi0_prim);

                    // System.out.println("pl = " + pl + " pl2 = " + pl2);

                    return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi_min1, pl);
                }

                phi_minus = root_finding(func, kappa, phi_min, 3);
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);
                // System.out.println("bracket: " + Arrays.toString(bracket(0, 1, func, kappa)) + " phi_min: " + phi_min + " phi_minus = " + phi_minus);
                // System.out.println("pl_step = " + pl_step);
                pl_s += pl_step;
                // System.out.println("kappa = " + kappa);

                phi_start = phi_minus;
                if (pl_step < min_pathLength) {
                    x = from.get_x() + drho * Math.cos(phi0)
                            + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                    y = from.get_y() + drho * Math.sin(phi0)
                            + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                    z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                    if (debug) System.out.println("break : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);
                    break;
                }

                if (dir) {
                    kappa += energyLossTest(kappa, tanL, pl_step, material);
                } else {
                    kappa -= energyLossTest(kappa, tanL, pl_step, material);
                }


                x = from.get_x() + drho * Math.cos(phi0)
                        + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi_minus));
                y = from.get_y() + drho * Math.sin(phi0)
                        + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi_minus));
                z = from.get_z() + dz - alpha / kappa * tanL * phi_minus;
                if (debug) System.out.println("end : r = " + Math.hypot(x, y) + " x = " + x + " y = " + y + " z = " + z);
            }

            pl += pl_s;

        }


        double alpha1 = to.alpha();

        double Xc = x0 + (drho + alpha1 / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha1 / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha1 / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha1 / kappa) * (phi0_prim - phi0) * tanL;

        Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                .pow(alpha1 * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                + Math.pow(alpha1 * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        phi_minus = root_finding(func, kappa, phi_min, 3);

        double pl2 = pathLength(kappa, tanL, alpha, phi0, phi0_prim);

        // System.out.println("pl = " + pl + " pl2 = " + pl2);

        return new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi_minus, pl);
    }

    public SimpleMatrix FTest2(Site from, Site to, Material material, boolean dir) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = this._sv.copy();
        double phi = this._phi;

        for (int i = 0; i < 5; i++) {

            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = state_p.moveToTest2(from, to, material, dir);
            SimpleMatrix sv_p_m = state_p_m._sv.copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = state_m.moveToTest2(from, to, material, dir);
            SimpleMatrix sv_m_m = state_m_m._sv.copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanL = this.tanL();

        State a_prim = moveToTest2(from, to, material, dir);

        double alpha = from.alpha();

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

    private double energyLossTest(double kappa, double tanL, double d, Material material) {

        double z = 1; // charge of particle in unit of electron charge
        double M = 0.938272; // mass of the particle in GeV
        double me = 0.511; // electron mass in MeV
        double P = Math.sqrt((tanL * tanL + 1) / (kappa * kappa));
        double BeGa = P / M;
        double Gamma = Math.sqrt(BeGa * BeGa + 1);
        double Beta = P / (M * Gamma);

        double K = 0.307075e-3;

        double ZA = material.ZA;
        double rho = material.rho; // g/cm3
        double I = material.I * 1e-3; // MeV

        double hwp = 28.816 * Math.sqrt(rho * ZA) * 1.e-9;
        double bg2 = (P * P) / (M * M);
        double gm2 = 1. + bg2;
        double meM = me / M;
        double x = Math.log10(Math.sqrt(bg2));
        double C0 = -(2. * Math.log(I / hwp) + 1.);
        double a = -C0 / 27.;
        double del;
        if (x >= 3.) del = 4.606 * x + C0;
        else if (0. <= x) del = 4.606 * x + C0 + a * Math.pow(3. - x, 3.);
        else del = 0.;

        double Wmax = (2. * me * BeGa * BeGa) / (1. + 2. * Gamma * (meM)
                + Math.pow((meM), 2));
        double LogTerm = Math.log(2. * me * BeGa * BeGa * Wmax / (I * I));
        double dEdx = K * z * z * ZA * (1. / (Beta * Beta)) * (0.5 * LogTerm - (Beta * Beta) - del);
        double dE = dEdx * rho * d / 10.;

        double E = Math.sqrt(P * P + M * M);

        return dE / E * kappa;
    }

    public void updatePhi(Site from) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double alpha = -895.6040867490954;

        Function func = (phi, kappa1) -> -from.get_r() + Math.sqrt(Math
                .pow(alpha * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                + Math.pow(alpha * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        double phi_minus = root_finding(func, kappa, phi_min, 3);

        this.set_phi(phi_minus);
    }


    public State moveToTest3(Site from, Site to, Material material, boolean dir, boolean debug) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double[] r = r(from, this.get_phi(), kappa, alpha);
        double start = Math.hypot(r[0], r[1]);
        double end = to.get_r();
        double n = 100;
        double phi_minus = this.get_phi();

        double pl = 0;

        if (debug) System.out.println("start = " + (start + (end - start) / n));
        if (debug) System.out.println("end = " + end);

        double v = 1. / n * (end - start) / n;
        for (double i = start + (end - start) / n; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);

            r = r(from, phi_minus, kappa, alpha);

            float[] b = new float[]{0,0,0};
            Swim swim = new Swim();
            swim.BfieldLab( r[0] / 10, r[1] / 10, r[2] / 10, b);
            double c = 0.000299792458;
            double B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
            // B = -3.725;
            B = 0.745 * B;
            alpha = 1. / (c * B);

            double fi = i;
            double finalAlpha1 = alpha;
            Function func = (phi, kappa1) -> -fi + Math.sqrt(Math
                    .pow(finalAlpha1 * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(finalAlpha1 * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double phi_start = phi_minus;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + fi + " alpha = " + alpha);
                System.out.println("start : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

            if (debug) {
                r = r(from, phi_min, kappa, alpha);
                System.out.println("min : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            phi_minus = root_finding(func, kappa, phi_min, 3);
            double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);

            pl += pl_step;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

            if (dir) {
                // kappa += 0.8 * energyLossTest(kappa, tanL, pl_step, material);
            } else {
                // kappa -= 0.8 * energyLossTest(kappa, tanL, pl_step, material);
            }

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }
        }

        alpha = to.alpha();
        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double finalAlpha = alpha;
        Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                .pow(finalAlpha * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                + Math.pow(finalAlpha * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        double phi = root_finding(func, kappa, phi_min, 3);

        State s = new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi, pl);
        if (debug) {
            r = s.r(to,phi,kappa,alpha);
            System.out.println("new pivot : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

        }


        return s;
    }

    public State moveToTestBeam(Site from, Site to, Material material, boolean dir, boolean debug) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        double alpha = from.alpha();

        double[] r = r(from, this.get_phi(), kappa, alpha);
        double start = Math.hypot(r[0], r[1]);
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();

        double pl = 0;

        if (debug) System.out.println("start = " + (start + (end - start) / n));
        if (debug) System.out.println("end = " + end);

        double v = 1. / n * (end - start) / n;
        for (double i = start + (end - start) / n; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);

            double fi = i;
            double finalAlpha1 = alpha;
            Function func = (phi, kappa1) -> -fi + Math.sqrt(Math
                    .pow(finalAlpha1 * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(finalAlpha1 * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double phi_start = phi_minus;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + fi);
                System.out.println("start : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

            if (debug) {
                r = r(from, phi_min, kappa, alpha);
                System.out.println("min : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }


            try {
                phi_minus = root_finding(func, kappa, phi_min, 3);
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);

                pl += pl_step;

                if (debug) {
                    r = r(from, phi_minus, kappa, alpha);
                    System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

                }

                if (dir) {
                    kappa += energyLossTest(kappa, tanL, pl_step, material);
                } else {
                    kappa -= energyLossTest(kappa, tanL, pl_step, material);
                }

                if (debug) {
                    r = r(from, phi_minus, kappa, alpha);
                    System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

                }
            }
            catch (Exception e){
                double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_min);

                pl += pl_step;

                if (debug) {
                    r = r(from, phi_minus, kappa, alpha);
                    System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

                }

                if (dir) {
                    kappa += energyLossTest(kappa, tanL, pl_step, material);
                } else {
                    kappa -= energyLossTest(kappa, tanL, pl_step, material);
                }

                if (debug) {
                    r = r(from, phi_minus, kappa, alpha);
                    System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

                }
                break;
            }

        }

        alpha = to.alpha();
        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double finalAlpha = alpha;
        Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                .pow(finalAlpha * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                + Math.pow(finalAlpha * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        double phi = root_finding(func, kappa, phi_min, 3);

        State s = new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi, pl);
        if (debug) {
            r = s.r(to,phi,kappa, alpha);
            System.out.println("new pivot : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

        }


        return s;
    }

    private double[] r(Site from, double phi, double kappa, double alpha) {

        double drho = this.drho();
        double phi0 = this.phi0();
        double dz = this.dz();
        double tanL = this.tanL();

        double x = from.get_x() + drho * Math.cos(phi0) + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
        double y = from.get_y() + drho * Math.sin(phi0) + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
        double z = from.get_z() + dz - alpha / kappa * tanL * phi;

        return new double[]{x, y, z};
    }



    public State moveToTest4(Site from, Site to, Material material, boolean dir, boolean debug) throws Exception {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        float[] b = new float[]{0,0,0};
        Swim swim = new Swim();
        swim.BfieldLab( x0 / 10, y0 / 10, z0 / 10, b);
        double c = 0.000299792458;
        double B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
        B = 0.745 * B;
        double alpha = 1. / (c * B);

        alpha = -895.6040867490954;

        double[] r = r(from, this.get_phi(), kappa, alpha);
        double start = Math.hypot(r[0], r[1]);
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();

        double pl = 0;

        if (debug) System.out.println("start = " + (start + (end - start) / n));
        if (debug) System.out.println("end = " + end);

        double v = 1. / n * (end - start) / n;
        for (double i = start + (end - start) / n; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);

            r = r(from, phi_minus, kappa, alpha);

            b = new float[]{0,0,0};
            swim.BfieldLab( r[0] / 10, r[1] / 10, r[2] / 10, b);
            B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
            B = 0.745 * B;
            alpha = 1. / (c * B);
            alpha = -895.6040867490954;

            double fi = i;
            double finalAlpha1 = alpha;
            Function func = (phi, kappa1) -> -fi + Math.sqrt(Math
                    .pow(finalAlpha1 * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(finalAlpha1 * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double phi_start = phi_minus;


            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + fi + " alpha = " + alpha);
                System.out.println("start : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

            if (debug) {
                r = r(from, phi_min, kappa, alpha);
                System.out.println("min : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            phi_minus = root_finding(func, kappa, phi_min, 3);
            double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);

            pl += pl_step;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

            if (debug) System.out.println("pl_step = " + pl_step);

            if (dir) {
                kappa += energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            } else {
                kappa -= energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            }

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }
        }

        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;

        double finalAlpha = alpha;
        Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                .pow(finalAlpha * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                + Math.pow(finalAlpha * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

        double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
        double phi = root_finding(func, kappa, phi_min, 3);

        State s = new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phi, pl);
        if (debug) {
            r = s.r(to,phi,kappa,alpha);
            System.out.println("new pivot : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

        }


        return s;
    }


    public State moveToBeam4(Site from, Site to, Material material, boolean dir, boolean debug) throws Exception {


        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double x0 = from.get_x();
        double y0 = from.get_y();
        double z0 = from.get_z();

        double x0_prim = to.get_x();
        double y0_prim = to.get_y();
        double z0_prim = to.get_z();

        float[] b = new float[]{0,0,0};
        Swim swim = new Swim();
        swim.BfieldLab( x0 / 10, y0 / 10, z0 / 10, b);
        double c = 0.000299792458;
        double B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
        B = 0.745 * B;
        double alpha = 1. / (c * B);

        alpha = -895.6040867490954;

        double[] r = r(from, this.get_phi(), kappa, alpha);
        double start = Math.hypot(r[0], r[1]);
        double end = to.get_r();
        double n = 10;
        double phi_minus = this.get_phi();

        double pl = 0;
        if (debug) System.out.println("start = " + start);
        if (debug) System.out.println("start + 1n = " + (start + (end - start) / n));
        if (debug) System.out.println("end = " + end);

        boolean stop = false;

        double v = 1. / n * (end - start) / n;
        for (double i = start + (end - start) / n; (i < (end + v) && dir) || (i > (end + v) && !dir); i += (end - start) / n) {
            if (debug) System.out.println("i = " + i);


            r = r(from, phi_minus, kappa, alpha);

            b = new float[]{0,0,0};
            swim.BfieldLab( r[0] / 10, r[1] / 10, r[2] / 10, b);
            B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
            B = 0.745 * B;
            alpha = 1. / (c * B);
            alpha = -895.6040867490954;

            double fi = i;
            double finalAlpha1 = alpha;
            Function func = (phi, kappa1) -> -fi + Math.sqrt(Math
                    .pow(finalAlpha1 * (Math.sin(phi0) - Math.sin(phi + phi0)) / kappa1 + drho * Math.sin(phi0) + y0, 2)
                    + Math.pow(finalAlpha1 * (Math.cos(phi0) - Math.cos(phi + phi0)) / kappa1 + drho * Math.cos(phi0) + x0, 2));

            double phi_start = phi_minus;


            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("drho = " + drho + " phi0 = " + phi0 + " kappa = " + kappa + " y0 = " + y0 + " x0 = " + x0 + " r = " + fi + " alpha = " + alpha);
                System.out.println("start : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            }

            try {
                double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);

                if (debug) {
                    r = r(from, phi_min, kappa, alpha);
                    System.out.println("min : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
                }

                phi_minus = root_finding(func, kappa, phi_min, 3);
            } catch (Exception e){
                phi_minus = minimize(bracket(0, 0.5, func, kappa), func, kappa);
                stop = true;
            }




            double pl_step = pathLength(kappa, tanL, alpha, phi_start, phi_minus);

            pl += pl_step;

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("middle : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

            if (debug) System.out.println("pl_step = " + pl_step);

            if (dir) {
                kappa += energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            } else {
                kappa -= energyLossTest(kappa, tanL, pl_step, material);
                if (debug) System.out.println("dE = " + energyLossTest(kappa, tanL, pl_step, material));
            }

            if (debug) {
                r = r(from, phi_minus, kappa, alpha);
                System.out.println("end : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);

            }

            if (stop) break;

        }





        double Xc = x0 + (drho + alpha / kappa) * Math.cos(phi0);
        double Yc = y0 + (drho + alpha / kappa) * Math.sin(phi0);
        double phi0_prim = mod2PI(Math.atan2((Yc - y0_prim), (Xc - x0_prim)) - Math.PI);
        double drho_prim = (Xc - x0_prim) * Math.cos(phi0_prim) + (Yc - y0_prim) * Math.sin(phi0_prim) - alpha / kappa;
        double dz_prim = z0 - z0_prim + dz - (alpha / kappa) * (phi0_prim - phi0) * tanL;
        double phii;
        if (stop){phii = 0;}
        else {
            double finalAlpha = alpha;
            Function func = (phi, kappa1) -> -to.get_r() + Math.sqrt(Math
                    .pow(finalAlpha * (Math.sin(phi0_prim) - Math.sin(phi + phi0_prim)) / kappa1 + drho_prim * Math.sin(phi0_prim) + y0_prim, 2)
                    + Math.pow(finalAlpha * (Math.cos(phi0_prim) - Math.cos(phi + phi0_prim)) / kappa1 + drho_prim * Math.cos(phi0_prim) + x0_prim, 2));

            double phi_min = minimize(bracket(0, 0.5, func, kappa), func, kappa);
            phii = root_finding(func, kappa, phi_min, 3);
        }
        State s = new State(drho_prim, phi0_prim, kappa, dz_prim, tanL, phii, pl);
        if (debug) {
            r = s.r(to,phii,kappa,alpha);
            System.out.println("new pivot : r = " + Math.hypot(r[0], r[1]) + " x = " + r[0] + " y = " + r[1] + " z = " + r[2]);
            System.out.println("pl = " + pl);
        }



        return s;
    }


    public SimpleMatrix FBeam4(Site from, Site to, Material material, boolean dir) throws Exception {

        double h = Math.sqrt(Math.ulp(1.0));
        double[] dkappadx = new double[5];
        SimpleMatrix sv = this._sv.copy();
        double phi = this._phi;

        for (int i = 0; i < 5; i++) {

            SimpleMatrix sv_p = new SimpleMatrix(sv);
            sv_p.set(i, 0, sv_p.get(i, 0) + h);
            State state_p = new State(sv_p, phi);
            State state_p_m = state_p.moveToBeam4(from, to, material, dir, false);
            SimpleMatrix sv_p_m = state_p_m._sv.copy();

            SimpleMatrix sv_m = new SimpleMatrix(sv);
            sv_m.set(i, 0, sv_m.get(i, 0) - h);
            State state_m = new State(sv_m, phi);
            State state_m_m = state_m.moveToBeam4(from, to, material, dir, false);
            SimpleMatrix sv_m_m = state_m_m._sv.copy();

            double dkappadi = (sv_p_m.get(2, 0) - sv_m_m.get(2, 0)) / (2 * h);

            dkappadx[i] = dkappadi;
        }

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double tanL = this.tanL();

        State a_prim = moveToBeam4(from, to, material, dir, false);

        double alpha = from.alpha();

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







    /**
     * Given a function f, and given distinct initial points ax and bx, this routine
     * searches in the downhill direction (defined by the function as evaluated at
     * the initial points) and returns new points ax, bx, cx that bracket a minimum
     * of the function. Also returned are the function values at the three points,
     * fa, fb, and fc. Use the implementation found in Numerical Recipes : The art
     * of scientific computing (2007).
     *
     * @param a     a
     * @param b     b
     * @param f     f
     * @param kappa kappa
     * @return [a, b, c]
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
                    return new double[]{ax, bx, cx};
                } else if (fu > fb) {
                    cx = u;
                    fc = fu;
                    return new double[]{ax, bx, cx};
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
        return new double[]{ax, bx, cx};
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
     * @throws Exception e
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
            double v1 = x >= xm ? a - x : b - x;
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
                    d = CGOLD * (e = v1);
                else {
                    d = p / q;
                    u = x + d;
                    if (u - a < tol2 || b - u < tol2)
                        d = SIGN(tol1, xm - x);
                }
            } else {
                d = CGOLD * (e = v1);
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
     * @throws Exception e
     */
    private double root_finding(Function f, double kappa, double x1, double x2) throws Exception {
        int ITMAX = 10000;
        // double EPS = Math.ulp(1.0);
        double EPS = 1.0E-10;
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
     * @param a a
     * @param b b
     * @return sign
     */
    private double SIGN(double a, double b) {
        return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    /**
     * Returns the pathlength between phi_start and phi_end.
     *
     * @param kappa     kappa = 1/pT
     * @param tanL      tanL = pz*kappa
     * @param alpha     alpha = 1/(cB)
     * @param phi_start in rad
     * @param phi_end   in rad
     * @return pathlength in mm
     */
    private double pathLength(double kappa, double tanL, double alpha, double phi_start, double phi_end) {

        return Math.sqrt(Math.pow(alpha, 2) * (Math.pow(tanL, 2) + 1) / Math.pow(kappa, 2))
                * Math.abs((phi_end - phi_start));
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

    public SimpleMatrix get_sv() {
        return _sv;
    }

    public void set_sv(SimpleMatrix _sv) {
        this._sv = _sv;
    }

    public double get_phi() {
        return _phi;
    }

    public void set_phi(double _phi) {
        this._phi = _phi;
    }

    public double get_pl() {
        return _pl;
    }

    public void set_pl(double _pl) {
        this._pl = _pl;
    }

    public double drho() {
        return this._sv.get(0, 0);
    }

    public double phi0() {
        return this._sv.get(1, 0);
    }

    public double kappa() {
        return this._sv.get(2, 0);
    }

    public double dz() {
        return this._sv.get(3, 0);
    }

    public double tanL() {
        return this._sv.get(4, 0);
    }

    public void set_i(int i, double value) {
        this._sv.set(i, 0, value);
    }

    public double get_i(int i) {
        return this._sv.get(i, 0);
    }

    private double[] change(Site from) {

        double drho = this.drho();
        double phi0 = this.phi0();
        double kappa = this.kappa();
        double dz = this.dz();
        double tanL = this.tanL();

        double alpha = -895.6040867490954;
        alpha = from.alpha();
        double phi = this._phi;

        double x = from.get_x() + drho * Math.cos(phi0)
                + alpha / kappa * (Math.cos(phi0) - Math.cos(phi0 + phi));
        double y = from.get_y() + drho * Math.sin(phi0)
                + alpha / kappa * (Math.sin(phi0) - Math.sin(phi0 + phi));
        double z = from.get_z() + dz - alpha / kappa * tanL * phi;
        double px = -1. / kappa * Math.sin(phi0 + phi);
        double py = 1. / kappa * Math.cos(phi0 + phi);
        double pz = 1. / kappa * tanL;

        return new double[]{x, y, z, px, py, pz};
    }

    public void debug(String name, Site from) {
        double[] start = change(from);
        double x = from.get_x();
        double y = from.get_y();
        double z = from.get_z();
        double distance = Math.sqrt((start[0] - x)*(start[0] - x) + (start[1] - y)*(start[1] - y) + (start[2] - z)*(start[2] - z));

        System.out.printf(name + " r = %.5f, phi = %.5f, z = %.5f,x = %.5f,y = %.5f\n", from.get_r(), from.get_phi(), from.get_z(), x, y);
        System.out.printf("x = %.5f,y = %.5f,z = %.5f,px = %.5f,py = %.5f,pz = %.5f r = %.5f p = %.8f\n", start[0], start[1], start[2], start[3],
                start[4], start[5], Math.hypot(start[0], start[1]), Math.sqrt(start[3] * start[3] + start[4] * start[4] + start[5] * start[5]));
        System.out.printf("drho =  %.5f, phi0 = %.5f, kappa = %.5f, dz = %.5f, tanL = %.5f, phi = %.5f\n", this.drho(), this.phi0(), this.kappa(), this.dz(), this.tanL(), this._phi);
        System.out.printf("distance =  %.5f, pl = %.8f\n\n",distance, this._pl);
        // System.out.println("r: " + from.get_r() + " phi: " + from.get_phi() + "z: " + from.get_z() + " x: " + x + " y: " + y);
        // System.out.println(name + ": " + Arrays.toString(start) + " r = " + Math.hypot(start[0], start[1]) + " p = " + Math.sqrt(start[3] * start[3] + start[4] * start[4] + start[5] * start[5]));
        // System.out.println("drho: " + this.drho() + " phi0: " + this.phi0() + " kappa: " + this.kappa() + " dz: " + this.dz() + " tanl: " + this.tanL() + " phi: " + this._phi);
    }

    /**
     * Interface of a function for minimize, root_finding and bracket
     */
    interface Function {
        double calc(double x, double kappa);
    }

}
