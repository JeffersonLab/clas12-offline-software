package org.jlab.clas.tracking.kalmanfilter.zReference;

import java.util.ArrayList;
import java.util.List;

import org.jlab.clas.clas.math.FastMath;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AKFitter;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.kalmanfilter.zReference.MeasVecs;
import org.jlab.clas.tracking.kalmanfilter.zReference.StateVecs;
import org.jlab.clas.tracking.utilities.RungeKuttaDoca;
import org.jlab.clas.tracking.utilities.MatrixOps.Libr;
import org.jlab.geom.prim.Point3D;
import org.jlab.jnp.matrix.*;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author Tongtong Cao
 */
public class KFitter extends AKFitter {

    private StateVecs sv = new StateVecs();
    private MeasVecs mv = new MeasVecs();
    private StateVec finalSmoothedStateVec = null;
    private StateVec finalTransportedStateVec = null;

    public StateVec finalStateVec = null;
    public StateVec initialStateVec = null;
    public List<StateVec> kfStateVecsAlongTrajectory;

    private int iterNum;

    private double chi2kf = 0;
    private double KFScale = 4;

    private int svzLength;

    public int ConvStatus = 1;

    private double Z[];

    private boolean stopIteration = false;

    private boolean TBT = false;

    private Map<Integer, Boolean> setFitFailedMap = new HashMap<>();
    private Map<Integer, Boolean> stopIterationMap = new HashMap<>();
    private Map<Integer, StateVecs> svMap = new HashMap<>();
    private Map<Integer, Double> chi2KFMap = new HashMap<>();

    Matrix first_inverse = new Matrix();
    Matrix addition = new Matrix();
    Matrix result = new Matrix();
    Matrix result_inv = new Matrix();
    Matrix adj = new Matrix();

    public KFitter(boolean filter, int iterations, int dir, Swim swim, double Z[], Libr mo) {
        super(filter, iterations, dir, swim, mo);
        this.Z = Z;
    }

    public final void init(List<Surface> measSurfaces, StateVec initSV) {
        finalSmoothedStateVec = null;
        finalTransportedStateVec = null;
        this.NDF0 = -5;
        this.NDF = -5;
        this.chi2 = Double.POSITIVE_INFINITY;
        this.numIter = 0;
        this.setFitFailed = false;
        mv.setMeasVecs(measSurfaces);
        for (int i = 0; i < mv.measurements.size(); i++) {
            if (mv.measurements.get(i).skip == false) {
                this.NDF += mv.measurements.get(i).surface.getNMeas();
            }
        }

        sv.init(initSV);
        sv.Z = Z;
    }

    public final void initFromHB(List<Surface> measSurfaces, StateVec initSV, double beta) {
        finalSmoothedStateVec = null;
        finalTransportedStateVec = null;
        this.NDF0 = -5;
        this.NDF = -5;
        this.chi2 = Double.POSITIVE_INFINITY;
        this.numIter = 0;
        this.setFitFailed = false;
        mv.setMeasVecs(measSurfaces);
        for (int i = 0; i < mv.measurements.size(); i++) {
            if (mv.measurements.get(i).skip == false) {
                this.NDF += mv.measurements.get(i).surface.getNMeas();
            }
        }

        sv.initFromHB(initSV, beta);
        sv.Z = Z;
        TBT = true;
    }

    public void runFitter() {
        this.chi2 = Double.POSITIVE_INFINITY;
        double initChi2 = Double.POSITIVE_INFINITY;
        // this.NDF = mv.ndf;
        this.svzLength = this.mv.measurements.size();

        int sector = this.mv.measurements.get(0).sector;

        if (TBT == true) {
            this.chi2kf = 0;
            // Get the input parameters
            for (int k = 0; k < svzLength - 1; k++) {
                sv.transport(sector, k, k + 1, this.sv.trackTrajT.get(k), mv, this.getSwimmer(), true);
            }
            this.calcFinalChisq(sector, true);
            this.initialStateVec = sv.trackTrajT.get(svzLength - 1);
            this.finalStateVec = sv.trackTrajT.get(svzLength - 1);
            initChi2 = this.chi2;
            if (Double.isNaN(chi2)) {
                this.setFitFailed = true;
                return;
            }
        }

        for (int i = 1; i <= totNumIter; i++) {
            iterNum = i;
            this.chi2kf = 0;

            if (i > 1) {

                for (int k = svzLength - 1; k > 0; k--) {
                    boolean forward = false;
                    if (k >= 2) {

                        // Not backward transport and filter states for the last measurement layer
                        if (k == svzLength - 1) {
                            if (!sv.transport(sector, k, k - 2, this.sv.trackTrajF.get(k), mv, this.getSwimmer(), forward)) {
                                setFitFailedMap.put(i, this.setFitFailed);
                                stopIterationMap.put(i, this.stopIteration);
                                StateVecs statevects = new StateVecs();
                                for (int key : this.sv.trackTrajT.keySet()) {
                                    statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajF.keySet()) {
                                    statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajP.keySet()) {
                                    statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajB.keySet()) {
                                    statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                                }
                                svMap.put(i, statevects);
                                chi2KFMap.put(i, this.chi2kf);
                                this.stopIteration = true;
                                break;
                            }
                        } else {
                            if (!sv.transport(sector, k, k - 2, this.sv.trackTrajB.get(k), mv, this.getSwimmer(), forward)) {
                                this.stopIteration = true;
                                setFitFailedMap.put(i, this.setFitFailed);
                                stopIterationMap.put(i, this.stopIteration);
                                StateVecs statevects = new StateVecs();
                                for (int key : this.sv.trackTrajT.keySet()) {
                                    statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajF.keySet()) {
                                    statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajP.keySet()) {
                                    statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                                }
                                for (int key : this.sv.trackTrajB.keySet()) {
                                    statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                                }
                                svMap.put(i, statevects);
                                chi2KFMap.put(i, this.chi2kf);
                                break;
                            }
                        }

                        if (!this.filter(k - 2, forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }

                        if (!sv.transport(sector, k - 2, k - 1, this.sv.trackTrajB.get(k - 2), mv, this.getSwimmer(), forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }

                        if (!this.filter(k - 1, forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }
                    } else {
                        if (!sv.transport(sector, 1, 0, this.sv.trackTrajB.get(1), mv, this.getSwimmer(), forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }

                        if (!this.filter(0, forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }
                    }
                }
            }

            if (this.stopIteration) {
                setFitFailedMap.put(i, this.setFitFailed);
                stopIterationMap.put(i, this.stopIteration);
                StateVecs statevects = new StateVecs();
                for (int key : this.sv.trackTrajT.keySet()) {
                    statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                }
                for (int key : this.sv.trackTrajF.keySet()) {
                    statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                }
                for (int key : this.sv.trackTrajP.keySet()) {
                    statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                }
                for (int key : this.sv.trackTrajB.keySet()) {
                    statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                }
                svMap.put(i, statevects);
                chi2KFMap.put(i, this.chi2kf);
                break;
            }

            for (int k = 0; k < svzLength - 1; k++) {
                boolean forward = true;

                if (iterNum == 1 && (k == 0)) {
                    if (TBT == true) {
                        this.sv.transported(true).put(0, this.sv.transported(false).get(0)); // For TBT, calcFinalChisq() is called previously.				
                    }
                }

                if (k == 0) {
                    if (i == 1) {
                        if (!this.sv.transport(sector, 0, 1, this.sv.trackTrajT.get(0), mv, this.getSwimmer(), forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }
                    } else {
                        if (!this.sv.transport(sector, 0, 1, this.sv.trackTrajB.get(0), mv, this.getSwimmer(), forward)) {
                            this.stopIteration = true;
                            setFitFailedMap.put(i, this.setFitFailed);
                            stopIterationMap.put(i, this.stopIteration);
                            StateVecs statevects = new StateVecs();
                            for (int key : this.sv.trackTrajT.keySet()) {
                                statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajF.keySet()) {
                                statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajP.keySet()) {
                                statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                            }
                            for (int key : this.sv.trackTrajB.keySet()) {
                                statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                            }
                            svMap.put(i, statevects);
                            chi2KFMap.put(i, this.chi2kf);
                            break;
                        }
                    }
                } else {
                    if (!this.sv.transport(sector, k, k + 1, this.sv.trackTrajF.get(k), mv, this.getSwimmer(), forward)) {
                        this.stopIteration = true;
                        setFitFailedMap.put(i, this.setFitFailed);
                        stopIterationMap.put(i, this.stopIteration);
                        StateVecs statevects = new StateVecs();
                        for (int key : this.sv.trackTrajT.keySet()) {
                            statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                        }
                        for (int key : this.sv.trackTrajF.keySet()) {
                            statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                        }
                        for (int key : this.sv.trackTrajP.keySet()) {
                            statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                        }
                        for (int key : this.sv.trackTrajB.keySet()) {
                            statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                        }
                        svMap.put(i, statevects);
                        chi2KFMap.put(i, this.chi2kf);
                        break;
                    }

                }

                if (!this.filter(k + 1, forward)) {
                    this.stopIteration = true;
                    setFitFailedMap.put(i, this.setFitFailed);
                    stopIterationMap.put(i, this.stopIteration);
                    StateVecs statevects = new StateVecs();
                    for (int key : this.sv.trackTrajT.keySet()) {
                        statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                    }
                    for (int key : this.sv.trackTrajF.keySet()) {
                        statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                    }
                    for (int key : this.sv.trackTrajP.keySet()) {
                        statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                    }
                    for (int key : this.sv.trackTrajB.keySet()) {
                        statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                    }
                    svMap.put(i, statevects);
                    chi2KFMap.put(i, this.chi2kf);
                    break;
                }
            }

            if (this.stopIteration) {
                setFitFailedMap.put(i, this.setFitFailed);
                stopIterationMap.put(i, this.stopIteration);
                StateVecs statevects = new StateVecs();
                for (int key : this.sv.trackTrajT.keySet()) {
                    statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                }
                for (int key : this.sv.trackTrajF.keySet()) {
                    statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                }
                for (int key : this.sv.trackTrajP.keySet()) {
                    statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                }
                for (int key : this.sv.trackTrajB.keySet()) {
                    statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                }
                svMap.put(i, statevects);
                chi2KFMap.put(i, this.chi2kf);
                break;
            } else {
                setFitFailedMap.put(i, this.setFitFailed);
                stopIterationMap.put(i, this.stopIteration);
                StateVecs statevects = new StateVecs();
                for (int key : this.sv.trackTrajT.keySet()) {
                    statevects.trackTrajT.put(key, this.sv.trackTrajT.get(key).clone());
                }
                for (int key : this.sv.trackTrajF.keySet()) {
                    statevects.trackTrajF.put(key, this.sv.trackTrajF.get(key).clone());
                }
                for (int key : this.sv.trackTrajP.keySet()) {
                    statevects.trackTrajP.put(key, this.sv.trackTrajP.get(key).clone());
                }
                for (int key : this.sv.trackTrajB.keySet()) {
                    statevects.trackTrajB.put(key, this.sv.trackTrajB.get(key).clone());
                }
                svMap.put(i, statevects);
                chi2KFMap.put(i, this.chi2kf);
            }

            if (i > 1) {
                if (this.setFitFailed == true) {
                    i = totNumIter;
                }
                if (this.setFitFailed == false) {
                    if (this.finalStateVec != null) {
                        if (Math.abs(sv.trackTrajF.get(svzLength - 1).Q - this.finalStateVec.Q) < 5.e-4
                                && Math.abs(sv.trackTrajF.get(svzLength - 1).x - this.finalStateVec.x) < 1.e-4
                                && Math.abs(sv.trackTrajF.get(svzLength - 1).y - this.finalStateVec.y) < 1.e-4
                                && Math.abs(sv.trackTrajF.get(svzLength - 1).tx - this.finalStateVec.tx) < 1.e-6
                                && Math.abs(sv.trackTrajF.get(svzLength - 1).ty - this.finalStateVec.ty) < 1.e-6) {
                            i = totNumIter;
                        }
                    }
                    this.finalStateVec = sv.trackTrajF.get(svzLength - 1);

                } else {
                    this.ConvStatus = 1; // Should be 0???
                }
            }

        }

        if (totNumIter == 1) {
            if (this.setFitFailed == false && this.stopIteration == false) {
                this.finalStateVec = sv.trackTrajF.get(svzLength - 1);
            }
        }

        this.calcFinalChisq(sector);

        if (Double.isNaN(chi2)) {
            this.setFitFailed = true;
        }

        if (TBT == true) {
            if (chi2 > initChi2) { // fit failed            	
                this.finalStateVec = this.initialStateVec;
                sv.trackTrajT.put(svzLength - 1, this.initialStateVec);
                this.calcFinalChisq(sector, true);
            }
        }

    }

    private boolean filter(int k, boolean forward) {
        StateVec sVec = sv.transported(forward).get(k);
        org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec mVec = mv.measurements.get(k);

        if (Double.isNaN(sVec.x) || Double.isNaN(sVec.y)
                || Double.isNaN(sVec.tx) || Double.isNaN(sVec.ty)
                || Double.isNaN(sVec.Q)) {
            this.setFitFailed = true;
            return false;
        }
        if (sVec != null && sVec.CM != null
                && k < mv.measurements.size() && mVec.skip == false) {

            double[] K = new double[5];
            double V = mVec.surface.unc[0] * KFScale;
            double[] H = mv.H(sVec.x, sVec.y, mVec.surface.z, mVec.surface.wireLine[0]);
            Matrix CaInv = this.filterCovMat(H, sVec.CM, V);
            Matrix cMat = new Matrix();
            if (CaInv != null) {
                Matrix5x5.copy(CaInv, cMat);
            } else {
                return false;
            }

            for (int j = 0; j < 5; j++) {
                // the gain matrix
                K[j] = (H[0] * cMat.get(j, 0)
                        + H[1] * cMat.get(j, 1)) / V;
            }

            Point3D point = new Point3D(sVec.x, sVec.y, mVec.surface.z);
            double h = mv.hDoca(point, mVec.surface.wireLine[0]);

            double signMeas = 1;
            double sign = 1;
            if (mVec.surface.doca[1] != -99
                    || !(Math.abs(mVec.surface.doca[0]) < 0.5
                    && mVec.surface.doca[1] == -99)) { // use LR only for double hits && large
                // enough docas
                signMeas = Math.signum(mVec.surface.doca[0]);
                sign = Math.signum(h);
            } else {
                signMeas = Math.signum(h);
                sign = Math.signum(h);
            }

            double c2 = ((signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h))
                    * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h)) / V);

            double x_filt = sVec.x
                    + K[0] * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h));
            double y_filt = sVec.y
                    + K[1] * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h));
            double tx_filt = sVec.tx
                    + K[2] * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h));
            double ty_filt = sVec.ty
                    + K[3] * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h));
            double Q_filt = sVec.Q
                    + K[4] * (signMeas * Math.abs(mVec.surface.doca[0]) - sign * Math.abs(h));

            // USE THE DOUBLE HIT
            if (mVec.surface.doca[1] != -99) {
                // now filter using the other Hit
                V = mVec.surface.unc[1] * KFScale;
                H = mv.H(x_filt, y_filt, mVec.surface.z,
                        mVec.surface.wireLine[1]);
                CaInv = this.filterCovMat(H, cMat, V);
                if (CaInv != null) {
                    for (int i = 0; i < 5; i++) {
                        Matrix5x5.copy(CaInv, cMat);
                    }
                } else {
                    return false;
                }
                for (int j = 0; j < 5; j++) {
                    // the gain matrix
                    K[j] = (H[0] * cMat.get(j, 0)
                            + H[1] * cMat.get(j, 1)) / V;
                }

                Point3D point2 = new Point3D(x_filt, y_filt, mVec.surface.z);

                h = mv.hDoca(point2, mVec.surface.wireLine[1]);

                signMeas = Math.signum(mVec.surface.doca[1]);
                sign = Math.signum(h);

                x_filt += K[0] * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h));
                y_filt += K[1] * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h));
                tx_filt += K[2] * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h));
                ty_filt += K[3] * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h));
                Q_filt += K[4] * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h));

                c2 += ((signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h))
                        * (signMeas * Math.abs(mVec.surface.doca[1]) - sign * Math.abs(h)) / V);
            }

            chi2kf += c2;
            if (filterOn) {
                StateVec filteredVec = sv.new StateVec(k);
                filteredVec.x = x_filt;
                filteredVec.y = y_filt;
                filteredVec.tx = tx_filt;
                filteredVec.ty = ty_filt;
                filteredVec.Q = Q_filt;
                filteredVec.z = sVec.z;
                filteredVec.B = sVec.B;
                filteredVec.deltaPath = sVec.deltaPath;

                filteredVec.CM = cMat;

                sv.filtered(forward).put(k, filteredVec);
            } else {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public Matrix filterCovMat(double[] H, Matrix Ci, double V) {

        double det = Matrix5x5.inverse(Ci, first_inverse, adj);
        if (Math.abs(det) < 1.e-30) {
            return null;
        }

        addition.set(
                H[0] * H[0] / V, H[0] * H[1] / V, 0, 0, 0,
                H[0] * H[1] / V, H[1] * H[1] / V, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0);

        Matrix5x5.add(first_inverse, addition, result);
        double det2 = Matrix5x5.inverse(result, result_inv, adj);
        if (Math.abs(det2) < 1.e-30) {
            return null;
        }

        return result_inv;
    }

    private void calcFinalChisq(int sector) {
        calcFinalChisq(sector, false);
    }

    private void calcFinalChisq(int sector, boolean nofilter) {
        int k = svzLength - 1;
        this.chi2 = 0;
        double path = 0;
        double[] nRj = new double[3];

        StateVec sVec;

        // To be changed: to match wit the old package, we make the following codes. Could be changed when other codes for application of calcFinalChisq are changed.
        if (nofilter || (sv.trackTrajF.get(k) == null)) {
            sVec = sv.trackTrajT.get(k);
        } else {
            sVec = sv.trackTrajF.get(k);
        }

        kfStateVecsAlongTrajectory = new ArrayList<>();
        if (sVec != null && sVec.CM != null) {

            boolean forward = false;
            sv.transport(sector, k, 0, sVec, mv, this.getSwimmer(), forward);

            StateVec svc = sv.transported(forward).get(0);
            path += svc.deltaPath;
            svc.setPathLength(path);

            double V0 = mv.measurements.get(0).surface.unc[0];

            Point3D point = new Point3D(svc.x, svc.y, mv.measurements.get(0).surface.z);
            double h0 = mv.hDoca(point, mv.measurements.get(0).surface.wireLine[0]);

            svc.setProjector(mv.measurements.get(0).surface.wireLine[0].origin().x());
            svc.setProjectorDoca(h0);
            kfStateVecsAlongTrajectory.add(svc);
            double res = (mv.measurements.get(0).surface.doca[0] - h0);
            chi2 += (mv.measurements.get(0).surface.doca[0] - h0) * (mv.measurements.get(0).surface.doca[0] - h0) / V0;
            nRj[mv.measurements.get(0).region - 1] += res * res / mv.measurements.get(0).error;
            //USE THE DOUBLE HIT
            if (mv.measurements.get(0).surface.doca[1] != -99) {
                V0 = mv.measurements.get(0).surface.unc[1];
                h0 = mv.hDoca(point, mv.measurements.get(0).surface.wireLine[1]);
                res = (mv.measurements.get(0).surface.doca[1] - h0);
                chi2 += (mv.measurements.get(0).surface.doca[1] - h0) * (mv.measurements.get(0).surface.doca[1] - h0) / V0;
                nRj[mv.measurements.get(0).region - 1] += res * res / mv.measurements.get(0).error;
                svc.setProjector(mv.measurements.get(0).surface.wireLine[1].origin().x());
                svc.setProjectorDoca(h0);
                kfStateVecsAlongTrajectory.add(svc);
            }

            forward = true;
            for (int k1 = 0; k1 < k; k1++) {
                if (k1 == 0) {
                    sv.transport(sector, k1, k1 + 1, svc, mv, this.getSwimmer(), forward);
                } else {
                    sv.transport(sector, k1, k1 + 1, sv.transported(forward).get(k1), mv, this.getSwimmer(), forward);
                }

                double V = mv.measurements.get(k1 + 1).surface.unc[0];

                point = new Point3D(sv.transported(forward).get(k1 + 1).x, sv.transported(forward).get(k1 + 1).y, mv.measurements.get(k1 + 1).surface.z);

                double h = mv.hDoca(point, mv.measurements.get(k1 + 1).surface.wireLine[0]);
                svc = sv.transported(forward).get(k1 + 1);
                path += svc.deltaPath;
                svc.setPathLength(path);
                svc.setProjector(mv.measurements.get(k1 + 1).surface.wireLine[0].origin().x());
                svc.setProjectorDoca(h);
                kfStateVecsAlongTrajectory.add(svc);
                res = (mv.measurements.get(k1 + 1).surface.doca[0] - h);
                chi2 += (mv.measurements.get(k1 + 1).surface.doca[0] - h) * (mv.measurements.get(k1 + 1).surface.doca[0] - h) / V;
                nRj[mv.measurements.get(k1 + 1).region - 1] += res * res / V;
                //USE THE DOUBLE HIT
                if (mv.measurements.get(k1 + 1).surface.doca[1] != -99) {
                    V = mv.measurements.get(k1 + 1).surface.unc[1];
                    h = mv.hDoca(point, mv.measurements.get(k1 + 1).surface.wireLine[1]);
                    res = (mv.measurements.get(k1 + 1).surface.doca[1] - h);
                    chi2 += (mv.measurements.get(k1 + 1).surface.doca[1] - h) * (mv.measurements.get(k1 + 1).surface.doca[1] - h) / V;
                    nRj[mv.measurements.get(k1 + 1).region - 1] += res * res / V;
                    svc.setProjector(mv.measurements.get(k1 + 1).surface.wireLine[1].origin().x());
                    svc.setProjectorDoca(h);
                    kfStateVecsAlongTrajectory.add(svc);
                }
            }
        }

    }

    public Matrix propagateToVtx(int sector, double Zf) {
        return sv.transport(sector, 0, Zf, sv.trackTrajP.get(0), mv, this.getSwimmer());
    }

    @Override
    public void runFitter(AStateVecs sv, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StateVec filter(int k, StateVec vec, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StateVec smooth(int k, AStateVecs sv, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StateVec smooth(StateVec v1, StateVec v2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MeasVecs getMeasVecs() {
        return mv;
    }

    public StateVecs getStateVecs() {
        return sv;
    }

    public boolean getStopIteration() {
        return stopIteration;
    }

    public double getKFChi2() {
        return chi2kf;
    }

    public Map<Integer, Boolean> getSetFitFailedMap() {
        return setFitFailedMap;
    }

    public Map<Integer, Boolean> getStopIterationMap() {
        return stopIterationMap;
    }

    public Map<Integer, StateVecs> getSVMap() {
        return svMap;
    }

    public Map<Integer, Double> getChi2KFMap() {
        return chi2KFMap;
    }

    public void printlnMeasVecs() {
        for (int i = 0; i < mv.measurements.size(); i++) {
            org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec measvec = mv.measurements.get(i);
            String s = String.format("k=%d region=%d superlayer=%d layer=%d error=%.4f", measvec.k, measvec.region, measvec.superlayer,
                    measvec.layer, measvec.error);
            s += String.format(" Surface: index=%d x=%.4f z=%.4f tilt=%.4f wireMaxSag=%.4f", measvec.surface.getIndex(),
                    measvec.surface.x, measvec.surface.z, measvec.surface.tilt, measvec.surface.wireMaxSag, measvec.surface.unc[1]);
            s += String.format(
                    " Surface line 0: doca=%.4f unc=%.4f origin_x =%.4f, origin_y =%.4f, origin_z =%.4f, end_x=%.4f, end_y=%.4f, end_z=%.4f",
                    measvec.surface.doca[0], measvec.surface.unc[0], measvec.surface.wireLine[0].origin().x(),
                    measvec.surface.wireLine[0].origin().y(), measvec.surface.wireLine[0].origin().z(),
                    measvec.surface.wireLine[0].end().x(), measvec.surface.wireLine[0].end().y(),
                    measvec.surface.wireLine[0].end().z());
            if (measvec.surface.wireLine[1] != null) {
                s += String.format(
                        " Surface line 1: doca=%.4f unc=%.4f origin_x =%.4f, origin_y =%.4f, origin_z =%.4f, end_x=%.4f, end_y=%.4f, end_z=%.4f",
                        measvec.surface.doca[1], measvec.surface.unc[1], measvec.surface.wireLine[1].origin().x(),
                        measvec.surface.wireLine[1].origin().y(), measvec.surface.wireLine[1].origin().z(),
                        measvec.surface.wireLine[1].end().x(), measvec.surface.wireLine[1].end().y(),
                        measvec.surface.wireLine[1].end().z());
            }

            System.out.println(s);
        }
    }

}
