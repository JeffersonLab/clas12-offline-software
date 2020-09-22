package org.jlab.rec.tof.cluster.ftof;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.jlab.clas.pdg.PhysicsConstants;

import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.tof.cluster.Cluster;
import org.jlab.rec.tof.hit.AHit;

/**
 *
 * @author ziegler
 *
 */
public class ClusterMatcher {

    /**
     * Matches the clusters in panel 1A and 1B
     */
    public ClusterMatcher() {

    }

    private double[] _deltaPathLen = new double[3]; // pathlength of track between
    // counters for 3 cases(taking
    // earlier hit time, max energy
    // or average hit times)
    public double Beta = 0; // beta of the track to be obtained from the event
    // builder
    private double _xMatch = Double.NaN; // matched x coordinate between panel
    // 1a and panel 1b
    private double _yMatch = Double.NaN; // matched y coordinate between panel
    // 1a and panel 1b
    private double _zMatch = Double.NaN; // matched z coordinate between panel
    // 1a and panel 1b
    private double _tMatch = Double.NaN; // matched time between panel 1a and
    // panel 1b

    public double get_xMatch() {
        return _xMatch;
    }

    public void set_xMatch(double _xMatch) {
        this._xMatch = _xMatch;
    }

    public double get_yMatch() {
        return _yMatch;
    }

    public void set_yMatch(double _yMatch) {
        this._yMatch = _yMatch;
    }

    public double get_zMatch() {
        return _zMatch;
    }

    public void set_zMatch(double _zMatch) {
        this._zMatch = _zMatch;
    }

    public double get_tMatch() {
        return _tMatch;
    }

    public void set_tMatch(double _tMatch) {
        this._tMatch = _tMatch;
    }

    public double getBeta() {
        return Beta;
    }

    public void setBeta(double beta) {
        Beta = beta;
    }

    /**
     *
     * @param C1a Cluster in panel 1a
     * @param C1b Cluster in panel 1b
     * @return a 2-cluster system corresponding to a match between panel 1a and
     * panel 1b
     */
    public ArrayList<Cluster> ClusterDoublet(Cluster C1a, Cluster C1b, DataEvent event) { 
        if (event.hasBank("RECHB::Particle")==false ||  event.hasBank("RECHB::Track")==false) 
            return null; // do only if there's TB tracking
        
        if (C1b.get_xTrk() == null || C1a.get_xTrk() == null) {
            return null; // no tracking info
        }
        if (C1a.get(0).get_TrkId() != C1b.get(0).get_TrkId()) {
            return null; // not from the stame track
        }
        ArrayList<Cluster> ClsDoublet = new ArrayList<Cluster>(2);
        // computes an array of pathlengths for (cluster size=1) 1 or (cluster
        // size=2) 3 reference points along
        // the trajectory of the track with the counters in the cluster
        //
        double[][] X1 = this.get_ClusterHitCoordinates(C1a);
        double[][] X2 = this.get_ClusterHitCoordinates(C1b);
        double[] deltaR = this.calc_deltaR(X1, X2);
        this._deltaPathLen = deltaR;
        //Read Beta:   
        DataBank bank = event.getBank("RECHB::Track");
        int rows = bank.rows();
        for (int i = 0; i < rows; i++) {
            if (bank.getByte("detector", i) == 6 &&
                    bank.getShort("index", i) == C1a.get(0).get_TrkId() - 1) {
                this.Beta = event.getBank("RECHB::Particle").getFloat("beta",
                        bank.getShort("pindex", i));
            }
        }
        //double beta = C1b.get(0).get_TrkPathLen() / C1b.get_t();
        //this.Beta = beta;
        // extrapolate the point in panel 1a to panel 1b using tracking vector
        double ux_mid = X2[1][0] - X1[1][0];
        double uy_mid = X2[1][1] - X1[1][1];
        double uz_mid = X2[1][2] - X1[1][2];
        double C1a_x_etrapPan1b = C1a.get_x() + ux_mid;
        double C1a_y_etrapPan1b = C1a.get_y() + uy_mid;
        double C1a_z_etrapPan1b = C1a.get_z() + uz_mid;

        // Matching the cluster position to the track position
        if (Math.abs(C1a.get_x() - C1a.get_xTrk()[0]) > Constants.CLS1ATRKMATCHXPAR
                || Math.abs(C1a.get_y() - C1a.get_yTrk()[0]) > Constants.CLS1ATRKMATCHYPAR
                || Math.abs(C1a.get_z() - C1a.get_zTrk()[0]) > Constants.CLS1ATRKMATCHZPAR) {
            return null; // not matched
        }
        if (Math.abs(C1b.get_x() - C1b.get_xTrk()[0]) > Constants.CLS1BTRKMATCHXPAR
                || Math.abs(C1b.get_y() - C1b.get_yTrk()[0]) > Constants.CLS1BTRKMATCHYPAR
                || Math.abs(C1b.get_z() - C1b.get_zTrk()[0]) > Constants.CLS1BTRKMATCHZPAR) {
            return null; // not matched
        }
        
        // Matching between 1A and 1B
        if (Math.abs(C1a_x_etrapPan1b - C1b.get_x()) < Constants.CLSMATCHXPAR) {
            this._xMatch = C1a_x_etrapPan1b;
        }
        if (Math.abs(C1a_y_etrapPan1b - C1b.get_y()) < Constants.CLSMATCHYPAR) {
            this._yMatch = C1a_y_etrapPan1b;
        }
        if (Math.abs(C1a_z_etrapPan1b - C1b.get_z()) < Constants.CLSMATCHZPAR) {
            this._zMatch = C1a_z_etrapPan1b;
        }
        if (Math.abs(C1a.get_t() - C1b.get_t()) < Constants.CLSMATCHTPAR) {
            this._tMatch = C1b.get_t();
        }

        if (Double.isNaN(_xMatch) || Double.isNaN(_yMatch)
                || Double.isNaN(_zMatch) || Double.isNaN(_tMatch)) {
            return null; // not matched
        }
        ClsDoublet.add(C1a);
        ClsDoublet.add(C1b);
        return ClsDoublet;

    }

    private double[] calc_deltaR(double[][] X1, double[][] X2) {

        double[] deltaR = new double[3];

        // x_min = X[0][0];x_mid = X[1][0];x_max = X[2][0];
        // y_min = X[0][1];y_mid = X[1][1];y_max = X[2][1];
        // z_min = X[0][2];z_mid = X[1][2];z_max = X[2][2];
        double ux_min = X2[0][0] - X1[0][0];
        double ux_mid = X2[1][0] - X1[1][0];
        double ux_max = X2[2][0] - X1[2][0];
        double uy_min = X2[0][1] - X1[0][1];
        double uy_mid = X2[1][1] - X1[1][1];
        double uy_max = X2[2][1] - X1[2][1];
        double uz_min = X2[0][2] - X1[0][2];
        double uz_mid = X2[1][2] - X1[1][2];
        double uz_max = X2[2][2] - X1[2][2];

        deltaR[0] = new Vector3D(ux_min, uy_min, uz_min).mag();
        deltaR[1] = new Vector3D(ux_mid, uy_mid, uz_mid).mag();
        deltaR[2] = new Vector3D(ux_max, uy_max, uz_max).mag();

        return deltaR;
    }

    private double[][] get_ClusterHitCoordinates(Cluster c1a) {
        double[][] X1 = new double[3][3];

        double x1_min = c1a.get(0).get_Position().x();
        double y1_min = c1a.get(0).get_Position().y();
        double z1_min = c1a.get(0).get_Position().z();

        double x1_max = c1a.get(0).get_Position().x();
        double y1_max = c1a.get(0).get_Position().y();
        double z1_max = c1a.get(0).get_Position().z();

        if (c1a.size() > 1) {
            x1_max = c1a.get(1).get_Position().x();
            y1_max = c1a.get(1).get_Position().y();
            z1_max = c1a.get(1).get_Position().z();
        }

        double x1_mid = 0.5 * (x1_min + x1_max);
        double y1_mid = 0.5 * (y1_min + y1_max);
        double z1_mid = 0.5 * (z1_min + z1_max);

        X1[0][0] = x1_min;
        X1[0][1] = y1_min;
        X1[0][2] = z1_min;

        X1[1][0] = x1_mid;
        X1[1][1] = y1_mid;
        X1[1][2] = z1_mid;

        X1[2][0] = x1_max;
        X1[2][1] = y1_max;
        X1[2][2] = z1_max;

        return X1;
    }

    /**
     *
     * @param clusters
     * @return list of matched cluster doublets
     */
    public ArrayList<ArrayList<Cluster>> MatchedClusters(List<Cluster> clusters, DataEvent event) { 
        ArrayList<ArrayList<Cluster>> ClsDoublets = new ArrayList<ArrayList<Cluster>>();
        
        if (event.hasBank("RECHB::Particle")==true ||  event.hasBank("RECHB::Track")==true) { 
         // do only if there's TB tracking
            Collections.sort(clusters);

            for (Cluster C1 : clusters) {
                if (C1.get_Panel() != 1) {
                    continue;
                }
                for (Cluster C2 : clusters) {
                    if (C2.get_Panel() != 2) {
                        continue;
                    }
                    if (C1.get_Sector() != C2.get_Sector()) {
                        continue;
                    }

                    ArrayList<Cluster> ClsDoub = ClusterDoublet(C1, C2, event);
                    if(ClsDoub==null)
                        continue;
                    C2.set_tCorr(this.get_CorrectedHitTime(C1, C2)); // set the
                    // corrected
                    // Time for
                    // 1b
                    ClsDoublets.add(ClsDoub);
                }
            }
        }
        return ClsDoublets;

    }

    /**
     *
     * @param panel
     * @param paddle
     * @return average counter timing resolutions
     */
    private double calc_deltaT(int panel, int paddle) {

        double delta_t = Constants.DELTA_T[panel - 1][0] * paddle
                + Constants.DELTA_T[panel - 1][1];

        return delta_t;
    }

    /**
     *
     * @param clus1A
     * @param clus1B
     * @return corrected hit times for each of the 3 ways of computing deltaR
     */
    private double[] get_CorrectedHitTime(Cluster clus1A, Cluster clus1B) {

        double[] tCorr = {-1, -1, -1};
        double[] term2 = new double[3];
        int CntrInPan1aWithEmax = this.get_CounterWithMaxE(clus1A);
        int CntrInPan1bWithEmax = this.get_CounterWithMaxE(clus1B);

        if (clus1A != null && clus1B != null && Beta > 0) { // clusters and
            // pathlength have
            // to be well
            // defined
            double delta_t1a = this.calc_deltaT(clus1A.get_Panel(),
                    CntrInPan1aWithEmax);
            double delta_t1b = this.calc_deltaT(clus1B.get_Panel(),
                    CntrInPan1bWithEmax);

            double term1 = clus1B.get_t() / delta_t1b;
            double term3 = 1. / delta_t1a + 1. / delta_t1b;
            for (int i = 0; i < 3; i++) {
                if (this._deltaPathLen[i] > 0) {
                    term2[i] = (clus1A.get_t() - this._deltaPathLen[i] / Beta/ PhysicsConstants.speedOfLight()) / delta_t1a;
                    tCorr[i] = (term1 + term2[i]) / term3;
                }
            }
        }
        return tCorr;

    }

    /**
     *
     * @param clus Cluster
     * @return the counter with the max energy
     */
    private int get_CounterWithMaxE(Cluster clus) {
        double Emax = -1;
        int counter = 0;
        for (AHit hit : clus) {
            if (hit.get_Energy() > Emax) {
                Emax = hit.get_Energy();
                counter = hit.get_Paddle();
            }
        }
        return counter;
    }

}
