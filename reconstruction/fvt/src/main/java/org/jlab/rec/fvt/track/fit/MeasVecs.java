package org.jlab.rec.fvt.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.special.Erf;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.fmt.cluster.Cluster;
import org.jlab.rec.fmt.hit.Hit;
import org.jlab.rec.fmt.GeometryMethods;
import org.jlab.rec.fvt.track.fit.StateVecs.StateVec;

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
        double err       = (double) org.jlab.rec.fmt.Constants.FVT_Pitch/Math.sqrt(12.);
        meas.error       = err*err*size;
        meas.layer       = l+1;
        if (l>-1) meas.z = org.jlab.rec.fmt.Constants.FVT_Zlayer[l]+org.jlab.rec.fmt.Constants.hDrift/2;
        meas.centroid    = cent;
        meas.seed        = seed;

        return meas;
    }

    public int getClosestStrip(double x, double y, int layer) {
        int closestStrip = 0;
        if(Math.sqrt(x*x+y*y)<org.jlab.rec.fmt.Constants.FVT_Rmax && Math.sqrt(x*x+y*y)>org.jlab.rec.fmt.Constants.FVT_Beamhole) {

            Point3D locPos = GeometryMethods.globalToLocal(new Point3D(x,y,0), layer-1);

            if (locPos.y()>-(org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.) && locPos.y() < (org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.)){
                if (locPos.x()>=0) closestStrip = (int) (Math.floor(((org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.)-locPos.y())/org.jlab.rec.fmt.Constants.FVT_Pitch) + 1 );
                if (locPos.x()<0) closestStrip =  (int) ((Math.floor((locPos.y()+(org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.))/org.jlab.rec.fmt.Constants.FVT_Pitch) + 1 ) + org.jlab.rec.fmt.Constants.FVT_Halfstrips +0.5*( org.jlab.rec.fmt.Constants.FVT_Nstrips-2.*org.jlab.rec.fmt.Constants.FVT_Halfstrips));
            }
            else if (locPos.y() <= -(org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.) && locPos.y() > -org.jlab.rec.fmt.Constants.FVT_Rmax){
                closestStrip =  (int) (Math.floor(((org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.)-locPos.y())/org.jlab.rec.fmt.Constants.FVT_Pitch) +1 );
            }
            else if (locPos.y() >= (org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.) && locPos.y() < org.jlab.rec.fmt.Constants.FVT_Rmax){
                closestStrip = (int) (Math.floor((locPos.y()+(org.jlab.rec.fmt.Constants.FVT_Halfstrips*org.jlab.rec.fmt.Constants.FVT_Pitch/2.))/org.jlab.rec.fmt.Constants.FVT_Pitch) + 1 + org.jlab.rec.fmt.Constants.FVT_Halfstrips+0.5*( org.jlab.rec.fmt.Constants.FVT_Nstrips-2.*org.jlab.rec.fmt.Constants.FVT_Halfstrips));
            }
        }
        return closestStrip;
    }

    public double getWeightEstimate(int strip, int layer, double x, double y) {
        double sigmaDrift = 0.01;
        double strip_y = org.jlab.rec.fmt.Constants.FVT_stripsYlocref[strip-1];
        double strip_x = org.jlab.rec.fmt.Constants.FVT_stripsXlocref[strip-1];


        double strip_length = org.jlab.rec.fmt.Constants.FVT_stripslength[strip-1];
        double sigma = sigmaDrift*org.jlab.rec.fmt.Constants.hDrift;
        double wght=(Erf.erf((strip_y+org.jlab.rec.fmt.Constants.FVT_Pitch/2.-y)/sigma/Math.sqrt(2))-Erf.erf((strip_y-org.jlab.rec.fmt.Constants.FVT_Pitch/2.-y)/sigma/Math.sqrt(2)))*(Erf.erf((strip_x+strip_length/2.-x)/sigma/Math.sqrt(2))-Erf.erf((strip_x-strip_length/2.-x)/sigma/Math.sqrt(2)))/2./2.;
        if (wght<0) wght=-wght;
        return wght;
    }

    public double getCentroidEstimate(int layer, double x, double y) {
        if (this.getClosestStrip(x, y, layer)>1) {
            return org.jlab.rec.fmt.Constants.FVT_stripsYlocref[this.getClosestStrip(x, y, layer)-1];
        } else {
            return GeometryMethods.globalToLocal(new Point3D(x,y,0), layer-1).y();
        }
    }

    public double h(StateVec stateVec) {
        if (stateVec == null) return 0;
        if (this.measurements.get(stateVec.k) == null) return 0;

        int layer = this.measurements.get(stateVec.k).layer;

        return stateVec.y*Math.cos(org.jlab.rec.fmt.Constants.FVT_Alpha[layer-1])
                - stateVec.x*Math.sin(org.jlab.rec.fmt.Constants.FVT_Alpha[layer-1]);
    }

    public double[] H(StateVec stateVec, StateVecs sv) {
        int layer = this.measurements.get(stateVec.k).layer;
        double[] H = new double[]{-Math.sin(org.jlab.rec.fmt.Constants.FVT_Alpha[layer-1]), Math.cos(org.jlab.rec.fmt.Constants.FVT_Alpha[layer-1]), 0, 0, 0};
        return H;
    }

    private StateVec reset(StateVec SVplus, StateVec stateVec, StateVecs sv) {
        SVplus = sv.new StateVec(stateVec.k);
        SVplus.x = stateVec.x;
        SVplus.y = stateVec.y;
        SVplus.z = stateVec.z;
        SVplus.tx = stateVec.tx;
        SVplus.ty = stateVec.ty;
        SVplus.Q = stateVec.Q;

        return SVplus;
    }
}
