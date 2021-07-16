package org.jlab.rec.fmt.track.fit;

import java.util.ArrayList;
import java.util.List;
import org.jlab.rec.fmt.Geometry;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.track.fit.StateVecs.StateVec;

/**
 * @author ziegler
 */

public class MeasVecs {

    public List<MeasVec> measurements ;

    public class MeasVec implements Comparable<MeasVec> {
        public double z = Double.NaN;
        public double centroid;
        public double seed;
        public double error;
        public int layer;
        public int k;
        public int size;

        MeasVec() {}

        @Override
        public int compareTo(MeasVec arg) {
            int CompLay = this.layer < arg.layer ? -1 : this.layer == arg.layer ? 0 : 1;
            return CompLay;
        }
    }

    public void setMeasVecs(List<Cluster> clusters) {
        measurements = new ArrayList<MeasVec>();

        for (int i = 0; i < clusters.size(); i++) {
            int l = clusters.get(i).get_Layer()-1;
            double cent = clusters.get(i).get_Centroid();
            int seed = clusters.get(i).get_SeedStrip();
            int size = clusters.get(i).size();
            MeasVec meas = new MeasVec();
            meas = this.setMeasVec(l, cent, seed, size);
            measurements.add(meas);
        }
    }

    public MeasVec setMeasVec(int l, double cent, int seed, int size) {

        MeasVec meas     = new MeasVec();
        double err       = (double) Geometry.stripSigma;
        meas.error       = err*err*size;
        meas.layer       = l+1;
        if (l>-1) meas.z = Geometry.getZ(l+1);
        meas.centroid    = cent;
        meas.seed        = seed;

        return meas;
    }

    public double h(StateVec stateVec) {
        if (stateVec == null) return 0;
        if (this.measurements.get(stateVec.k) == null) return 0;

        int layer = this.measurements.get(stateVec.k).layer;

        return Geometry.globalToLocal(stateVec.x, stateVec.y, stateVec.z, layer).y();
    }

    public double[] H(StateVec stateVec, StateVecs sv) {
        int layer = this.measurements.get(stateVec.k).layer;
        double[] H = new double[]{Geometry.getDx(layer), Geometry.getDy(layer), 0, 0, 0};
        return H;
    }

    private StateVec reset(StateVec SVplus, StateVec stateVec, StateVecs sv) {
        SVplus    = sv.new StateVec(stateVec.k);
        SVplus.x  = stateVec.x;
        SVplus.y  = stateVec.y;
        SVplus.z  = stateVec.z;
        SVplus.tx = stateVec.tx;
        SVplus.ty = stateVec.ty;
        SVplus.Q  = stateVec.Q;

        return SVplus;
    }
}
