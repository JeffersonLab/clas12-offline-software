package org.jlab.rec.fmt.track.fit;

import java.util.ArrayList;
import java.util.List;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.fmt.Constants;
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
        measurements = new ArrayList<>();

        for (int i = 0; i < clusters.size(); i++) {
            int l = clusters.get(i).getLayer()-1;
            double cent  = clusters.get(i).getCentroid();
            double error = clusters.get(i).getCentroidError();
            double z     = clusters.get(i).getGlobalSegment().origin().z();
            int seed = clusters.get(i).getSeedStrip();
            MeasVec meas = this.setMeasVec(l, cent, error, z, seed);
            measurements.add(meas);
        }
    }

    public MeasVec setMeasVec(int l, double cent, double error, double z, int seed) {

        MeasVec meas     = new MeasVec();
        meas.layer       = l+1;
        meas.centroid    = cent;
        meas.error       = error;
        meas.z           = z; 
        meas.seed        = seed;

        return meas;
    }

    public double h(StateVec stateVec) {
        if (stateVec == null) return 0;
        if (this.measurements.get(stateVec.k) == null) return 0;

        int layer = this.measurements.get(stateVec.k).layer;

        return Constants.toLocal(layer, stateVec.x, stateVec.y, stateVec.z).y();
    }

    public double[] H(StateVec stateVec, StateVecs sv) {
        int layer = this.measurements.get(stateVec.k).layer;
        Vector3D derivatives = Constants.getDerivatives(layer, stateVec.x, stateVec.y, stateVec.z);
        double[] H = new double[]{derivatives.x(), derivatives.y(), 0, 0, 0};
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
