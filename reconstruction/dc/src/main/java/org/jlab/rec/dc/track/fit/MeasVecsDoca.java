package org.jlab.rec.dc.track.fit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.jlab.detector.geant4.v2.DCGeant4Factory;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.track.Track;

public class MeasVecsDoca {

    private static final Logger LOGGER = Logger.getLogger(MeasVecsDoca.class.getName());
    
    public List<MeasVec> measurements = new ArrayList<MeasVec>();

    public int ndf=0;
    
    public double[] H(double[] stateV, double Z, Line3D wireLine) {
        double[] hMatrix = new double[2];
        double[] stateVec = new double[2];
        double Err = 0.025;
        double[][] Result = new double[2][2];
        for(int i = 0; i < 2; i++) {
            stateVec[0] = stateV[0] + (double)Math.pow(-1, i) * Err;
            stateVec[1] = stateV[1];
            Result[i][0] = h(stateVec, Z, wireLine);
        }
        for(int i = 0; i < 2; i++) {
            stateVec[0] = stateV[0];
            stateVec[1] = stateV[1] + (double)Math.pow(-1, i) * Err;
            Result[i][1] = h(stateVec, Z, wireLine);
        }
        hMatrix[0] = (Result[0][0]-Result[1][0])/(2.*Err);
        hMatrix[1] = (Result[0][1]-Result[1][1])/(2.*Err);
        
        return hMatrix;
    }

    public double h(double[] stateV, double Z, Line3D wireLine) {
       
        Line3D WL = new Line3D();
        WL.copy(wireLine);
        WL.copy(WL.distance(new Point3D(stateV[0], stateV[1], Z)));
        
        //LOGGER.log(Level.FINE, Math.signum(-WL.direction().x())+
        //        wireLine.origin().toString()+WL.toString()+" "+stateV[0]+" ,"+stateV[1]);
        return WL.length()*Math.signum(-WL.direction().x());
    }

    public void setMeasVecs(Track trkcand, DCGeant4Factory DcDetector) {
    	
        List<HitOnTrack> hOTS = new ArrayList<>(); // the list of hits on track		
        FittedHit hitOnTrk;
        // loops over the regions (1 to 3) and the superlayers in a region (1 to 2) and obtains the hits on track
        for (int c = 0; c < 3; c++) {
            for (int s = 0; s < 2; s++) {
                for (int h = 0; h < trkcand.get(c).get(s).size(); h++) {
//                    if (trkcand.get(c).get(s).get(h).get_Id() == -1 /*|| trkcand.get(c).get(s).get(h).get_Id() == 0*/) { //PASS1
                    if (trkcand.get(c).get(s).get(h).get_Id() == -1 || trkcand.get(c).get(s).get(h).get_Id() == 0) { 
                        continue;
                    }
                    trkcand.get(c).get(s).get(h).calc_CellSize(DcDetector);
                    hitOnTrk = trkcand.get(c).get(s).get(h); 
                    int slayr = trkcand.get(c).get(s).get(h).get_Superlayer();
                    
                    double sl1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlope();
                    double it1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitIntercept();

                    double Z = hitOnTrk.get_Z();
                    double X = sl1 * Z + it1;

                    //exclude hits that have poor segment
                    //if ((trkcand.get(c).get(s).get(h).get_X() - X) / (trkcand.get(c).get(s).get(h).get_CellSize() / FastMath.cos(Math.toRadians(6.))) > 1.5) {
                    //if(Math.abs(trkcand.get(c).get(s).get(h).get_Residual())>1) {   
                    //    continue;
                    //}
                    
                    HitOnTrack hot = new HitOnTrack(slayr, X, Z,  
                            trkcand.get(c).get(s).get(h).get_WireMaxSag(),
                            trkcand.get(c).get(s).get(h).get_WireLine()
                    );
                    double err_sl1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlopeErr();

                    double err_it1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitInterceptErr();
                    double err_cov1 = trkcand.get(c).get(s).get_fittedCluster().get_clusterLineFitSlIntCov();

                    hot._Unc[0] = Math.sqrt(err_sl1 * err_sl1 * Z * Z + err_it1 * err_it1);
                    hot._hitError = err_sl1 * err_sl1 * Z * Z + err_it1 * err_it1 + 2 * Z * err_cov1 + trkcand.get(c).get(s).get(h).get_DocaErr()*trkcand.get(c).get(s).get(h).get_DocaErr();
                    //if(trkcand.get(c).get(s).get(h).get_Time()/CCDBConstants.getTMAXSUPERLAYER()[trkcand.get(c).get(s).get(h).get_Sector()-1][trkcand.get(c).get(s).get(h).get_Superlayer()-1]<1.1)
                    //	hot._hitError = 100000; //exclude outoftimers from fit
                    hot.region = trkcand.get(c).get(s).get(h).get_Region();
                    
                    hot._doca[0] = trkcand.get(c).get(s).get(h).get_ClusFitDoca();
                    
                    double LR = Math.signum(trkcand.get(c).get(s).get(h).get_XWire()-trkcand.get(c).get(s).get(h).get_X());
                    hot._doca[0]*=LR;
                    //hot._hitError = err_sl1 * err_sl1 * Z * Z + err_it1 * err_it1 + 2 * Z * err_cov1 + trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr()*trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr();
                    hot._hitError = trkcand.get(c).get(s).get(h).get_DocaErr()*trkcand.get(c).get(s).get(h).get_DocaErr();
                    //LOGGER.log(Level.FINE, " Z "+Z+" ferr "+(float)(hot._Unc /(hot._hitError/4.)));
                    hot._Unc[0] = hot._hitError;
                    hOTS.add(hot);

                }
            }
        }
        Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)
        ndf = hOTS.size()-5;
        // identify double hits and take the average position		
        for (int i = 0; i < hOTS.size(); i++) {
            if (i > 0) {
                if (Math.abs(hOTS.get(i - 1)._Z - hOTS.get(i)._Z)<0.01) {
                    hOTS.get(i - 1)._doca[1] = hOTS.get(i)._doca[0];
                    hOTS.get(i - 1)._Unc[1] = hOTS.get(i)._Unc[0];
                    hOTS.get(i - 1)._wireLine[1] = hOTS.get(i)._wireLine[0];
                    hOTS.remove(i);
                }
            }
        }
        

        measurements = new ArrayList<>(hOTS.size());

        for (int i = 0; i < hOTS.size(); i++) {
            MeasVec meas = new MeasVec(i);
            meas.x = hOTS.get(i)._X;
            meas.z = hOTS.get(i)._Z;
            meas.region = hOTS.get(i).region;
            meas.error = hOTS.get(i)._hitError;
            meas.unc = hOTS.get(i)._Unc; //uncertainty used in KF fit
            meas.tilt = hOTS.get(i)._tilt;
            meas.doca = hOTS.get(i)._doca;
            meas.wireMaxSag = hOTS.get(i)._wireMaxSag;
            meas.wireLine = hOTS.get(i)._wireLine;
            this.measurements.add(i, meas);
        }
    }

    void setMeasVecsFromHB(Track trk, DCGeant4Factory DcDetector) { 
        List<HitOnTrack> hOTS = new ArrayList<>(); // the list of hits on track		
        FittedHit hitOnTrk;
        for(int s = 0; s < trk.get_ListOfHBSegments().size(); s++) {
            for(int h = 0; h < trk.get_ListOfHBSegments().get(s).size(); h++) { 
                trk.get_ListOfHBSegments().get(s).get(h).calc_CellSize(DcDetector);
                hitOnTrk = trk.get_ListOfHBSegments().get(s).get(h); 
                int slayr = trk.get_ListOfHBSegments().get(s).get(h).get_Superlayer();

                double sl1 = trk.get_ListOfHBSegments().get(s).get_fittedCluster().get_clusterLineFitSlope();
                double it1 = trk.get_ListOfHBSegments().get(s).get_fittedCluster().get_clusterLineFitIntercept();

                double Z = hitOnTrk.get_Z();
                double X = sl1 * Z + it1;
                HitOnTrack hot = new HitOnTrack(slayr, X, Z, 
                        hitOnTrk.get_WireMaxSag(),
                        hitOnTrk.get_WireLine());
                
                hot._doca[0] = trk.get_ListOfHBSegments().get(s).get(h).get_Doca();
                
                double LR = Math.signum(trk.get_ListOfHBSegments().get(s).get(h).get_XWire()-trk.get_ListOfHBSegments().get(s).get(h).get_X());
                hot._doca[0]*=LR;
                hot._hitError = trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr()*trk.get_ListOfHBSegments().get(s).get(h).get_DocaErr();
                //LOGGER.log(Level.FINE, " Z "+Z+" ferr "+(float)(hot._Unc /(hot._hitError/4.)));
                hot._Unc[0] = hot._hitError;
                hot.region = trk.get_ListOfHBSegments().get(s).get(h).get_Region();
                hOTS.add(hot);
                
            }
        }
        Collections.sort(hOTS); // sort the collection in order of increasing Z value (i.e. going downstream from the target)
        ndf = hOTS.size()-5;
        // identify double hits and take the average position		
        for (int i = 0; i < hOTS.size(); i++) {
            if (i > 0) {
                if (Math.abs(hOTS.get(i - 1)._Z - hOTS.get(i)._Z)<0.01) {
                    hOTS.get(i - 1)._doca[1] = hOTS.get(i)._doca[0];
                    hOTS.get(i - 1)._Unc[1] = hOTS.get(i)._Unc[0];
                    hOTS.get(i - 1)._wireLine[1] = hOTS.get(i)._wireLine[0];
                    hOTS.remove(i);
                }
            }
        }

        measurements = new ArrayList<>(hOTS.size());

        for (int i = 0; i < hOTS.size(); i++) {
            MeasVec meas = new MeasVec(i);
            meas.x = hOTS.get(i)._X;
            meas.z = hOTS.get(i)._Z;
            meas.region = hOTS.get(i).region;
            meas.error = hOTS.get(i)._hitError;
            meas.unc[0] = hOTS.get(i)._Unc[0]; //uncertainty used in KF fit
            meas.unc[1] = hOTS.get(i)._Unc[1]; //uncertainty used in KF fit
            meas.tilt = hOTS.get(i)._tilt;
            meas.doca = hOTS.get(i)._doca;
            meas.wireMaxSag = hOTS.get(i)._wireMaxSag;
            meas.wireLine[0] = hOTS.get(i)._wireLine[0];
            meas.wireLine[1] = hOTS.get(i)._wireLine[1];
            this.measurements.add(i, meas);
            //LOGGER.log(Level.FINE, " measurement "+i+" = "+meas.x+" at "+meas.z);
        }
    }
    
    public class MeasVec {
        
        final int k;
        public double z;
        public double x;
        public double[] unc = new double[2];
        public double tilt;
        public double error;
        public double[] doca = new double[2];
        public double wireMaxSag;
        public Line3D[] wireLine = new Line3D[2];
        public boolean reject = false;
        int region;
        
        
        MeasVec(int k) {
            this.k = k;
        }
        
    }

    private class HitOnTrack implements Comparable<HitOnTrack> {

        public double _hitError;
        private double _X;
        private double _Z;
        private double[] _Unc = new double[2];
        private double _tilt;
        private double[] _doca = new double[2];
        private double _wireMaxSag;
        private Line3D[] _wireLine = new Line3D[2];
        private int region;

        HitOnTrack(int superlayer, double X, double Z, double wiremaxsag, Line3D wireLine) {
            _X = X;
            _Z = Z;
            _wireMaxSag = wiremaxsag;
            _wireLine[0] = wireLine;
            _doca[0] = -99;
            _doca[1] = -99;
            _Unc[0] = 1;
            _Unc[1] = 1;
            
             //use stereo angle in fit based on wire direction
            _tilt = 90-Math.toDegrees(wireLine.direction().asUnit().angle(new Vector3D(1,0,0)));
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
