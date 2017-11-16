package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.track.Track;

public class MeasVecs {

    public List<MeasVec> measurements = new ArrayList<MeasVec>();

    public class MeasVec {

        final int k;
        public double z;
        public double x;
        public double unc;
        public int tilt;
        public double error;

        MeasVec(int k) {
            this.k = k;
        }

    }

    /**
     * The state projector - it projects the state onto the measurement
     *
     * @param stateV the state vector
     * @param s the superlayer of the measurement (0..1)
     * @return a double array corresponding to the 2 entries of the projector
     * matrix
     */
    public double[] H(int s) {
        double[] hMatrix = new double[2];

        hMatrix[0] = 1;
        hMatrix[1] = -Math.tan((Math.toRadians(s * 6.)));

        return hMatrix;
    }

    /**
     * The projected measurement derived from the stateVector at the measurement
     * site
     *
     * @param stateV the state vector
     * @param s the superlayer (0..1)
     * @return projected measurement
     */
    public double h(double[] stateV, int s) {

        double val = stateV[0] - Math.tan((Math.toRadians(s * 6.))) * stateV[1];
        return val;
    }

    public void setMeasVecs(Track trkcand, DCGeant4Factory DcDetector) {
    	
        List<HitOnTrack> hOTS = new ArrayList<HitOnTrack>(); // the list of hits on track		
        FittedHit hitOnTrk;
        // loops over the regions (1 to 3) and the superlayers in a region (1 to 2) and obtains the hits on track
        for (int c = 0; c < 3; c++) {
            for (int s = 0; s < 2; s++) {
                for (int h = 0; h < trkcand.get(c).get(s).size(); h++) {
                    if (trkcand.get(c).get(s).get(h).get_Id() == -1) {
                        continue;
                    }
                    trkcand.get(c).get(s).get(h).set_CellSize(DcDetector);
                    hitOnTrk = trkcand.get(c).get(s).get(h); 
                    int slayr = trkcand.get(c).get(s).get(h).get_Superlayer();
                    double sl1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlope();
                    double it1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitIntercept();

                    double Z = hitOnTrk.get_Z();
                    double X = sl1 * Z + it1;

                    //exclude hits that have poor segment
                    if ((trkcand.get(c).get(s).get(h).get_X() - X) / (trkcand.get(c).get(s).get(h).get_CellSize() / Math.cos(Math.toRadians(6.))) > 1.5) {
                        continue;
                    }
                    
                    HitOnTrack hot = new HitOnTrack(slayr, X, Z);
                    double err_sl1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlopeErr();

                    double err_it1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitInterceptErr();
                    double err_cov1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlIntCov();

                    hot._Unc = Math.sqrt(err_sl1 * err_sl1 * Z * Z + err_it1 * err_it1);
                    hot._hitError = err_sl1 * err_sl1 * Z * Z + err_it1 * err_it1 + 2 * Z * err_cov1 + trkcand.get(c).get(s).get(h).get_DocaErr()*trkcand.get(c).get(s).get(h).get_DocaErr();
                    //if(trkcand.get(c).get(s).get(h).get_Time()/CCDBConstants.getTMAXSUPERLAYER()[trkcand.get(c).get(s).get(h).get_Sector()-1][trkcand.get(c).get(s).get(h).get_Superlayer()-1]<1.1)
                    //	hot._hitError = 100000; //exclude outoftimers from fit
                    hOTS.add(hot);

                }
            }
        }
        Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)

        // identify double hits and take the average position		
        for (int i = 0; i < hOTS.size(); i++) {
            if (i > 0) {
                if (Math.abs(hOTS.get(i - 1)._Z - hOTS.get(i)._Z)<0.01) {
                    hOTS.get(i - 1)._X = (hOTS.get(i - 1)._X / (hOTS.get(i - 1)._Unc * hOTS.get(i - 1)._Unc) + hOTS.get(i)._X / (hOTS.get(i)._Unc * hOTS.get(i)._Unc)) / (1. / (hOTS.get(i - 1)._Unc * hOTS.get(i - 1)._Unc) + 1. / (hOTS.get(i)._Unc * hOTS.get(i)._Unc));
                    //hOTS.get(i-1)._hitError  = 1./Math.sqrt(1./(hOTS.get(i-1)._hitError*hOTS.get(i-1)._hitError) + 1./(hOTS.get(i)._hitError*hOTS.get(i)._hitError) );
                    hOTS.remove(i);
                }
            }
        }

        measurements = new ArrayList<MeasVec>(hOTS.size());

        for (int i = 0; i < hOTS.size(); i++) {
            MeasVec meas = new MeasVec(i);
            meas.x = hOTS.get(i)._X;
            meas.z = hOTS.get(i)._Z;

            meas.error = hOTS.get(i)._hitError;
            meas.unc = hOTS.get(i)._Unc; //uncertainty used in KF fit
            meas.tilt = hOTS.get(i)._tilt;
            this.measurements.add(i, meas);
        }
    }

    private class HitOnTrack implements Comparable<HitOnTrack> {

        public double _hitError;
        private double _X;
        private double _Z;
        private double _Unc;
        private int _tilt;

        public HitOnTrack(int superlayer, double X, double Z) {
            _X = X;
            _Z = Z;
            int s = (int) (superlayer) % 2;
            int tilt = 1;
            if (s == 0) {
                tilt = -1;
            }
            _tilt = tilt;
        }

        @Override
        public int compareTo(HitOnTrack o) {
            if (this._Z == o._Z) {
                return 0;
            }
            if (this._Z > o._Z) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
