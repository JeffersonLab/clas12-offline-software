package org.jlab.clas.tracking.kalmanfilter.zReference;

import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs;
import org.jlab.clas.tracking.kalmanfilter.AMeasVecs.MeasVec;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs.StateVec;
import org.jlab.clas.tracking.kalmanfilter.AStateVecs;
import org.jlab.clas.tracking.kalmanfilter.Surface;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.clas.tracking.utilities.MatrixOps;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.jnp.matrix.Matrix5x5;
import org.jlab.clas.tracking.utilities.RungeKuttaDoca;
import org.jlab.jnp.matrix.Matrix;

/**
 *
 * @author Tongtong Cao
 */
public class StateVecs extends AStateVecs {

    private RungeKuttaDoca rk = new RungeKuttaDoca();
    private final Matrix fMS = new Matrix();
    private final Matrix copyMatrix = new Matrix();

    final double ARGONRADLEN = 14;  // radiation length in Argon is 14 cm

    final double AIRRADLEN = 30400; // radiation length in cm

    public double Z[];

    private double beta = 1.0; // beta depends on mass hypothesis

    // For hit-based tracking
    public void init(StateVec initSV) {
        this.initSV = initSV;
        this.trackTrajT.clear();
        this.trackTrajF.clear();
        this.trackTrajP.clear();
        this.trackTrajB.clear();
        this.trackTrajS.clear();
        this.trackTrajT.put(0, new StateVec(initSV));
    }

    // For time-based tracking
    public void initFromHB(StateVec initSV, double beta) {
        this.beta = beta;
        this.initSV = initSV;
        this.trackTrajT.clear();
        this.trackTrajF.clear();
        this.trackTrajP.clear();
        this.trackTrajB.clear();
        this.trackTrajS.clear();
        this.trackTrajT.put(0, new StateVec(initSV));
    }

    /**
     *
     * @param sector
     * @param i initial state vector index
     * @param Zf
     * @param iVec state vector at the initial index
     * @param mv measurements
     */
    public Matrix transport(int sector, int i, double Zf, StateVec iVec, AMeasVecs mv, Swim swimmer) { // s = signed step-size

        double stepSize = 1.0;
        StateVec fVec = new StateVec(0);
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.ty = iVec.ty;
        fVec.Q = iVec.Q;
        fVec.B = iVec.B;
        Matrix5x5.copy(iVec.CM, fVec.CM);

        double s = 0;
        double zInit = mv.measurements.get(i).surface.z;
        double BatMeas = iVec.B;

        double z = zInit;

        while (Math.signum(Zf - zInit) * z < Math.signum(Zf - zInit) * Zf) {
            z = fVec.z;
            if (z == Zf) {
                break;
            }

            double x = fVec.x;
            double y = fVec.y;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q = fVec.Q;
            double dPath = fVec.deltaPath;
            Matrix cMat = new Matrix();
            Matrix5x5.copy(fVec.CM, cMat);
            s = Math.signum(Zf - zInit) * stepSize;

            // LOGGER.log(Level.FINE, " from "+(float)Z[i]+" to "+(float)Z[f]+" at "+(float)z+" By is "+bf[1]+" B is "+Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2])/Bmax+" stepSize is "+s);
            if (Math.signum(Zf - zInit) * (z + s) > Math.signum(Zf - zInit) * Zf) {
                s = Math.signum(Zf - zInit) * Math.abs(Zf - z);
            }

            //rk.RK4transport(sector, Q, x, y, z, tx, ty, s, swimmer, cMat, fVec, dPath);
            rk.RK4transport(sector, s, swimmer, cMat, fVec);

            // Q  process noise matrix estimate            
            double p = Math.abs(1. / iVec.Q);

            double X0 = this.getX0(z, Z);
            double t_ov_X0 = Math.abs(s) / X0;//path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            double beta = this.beta;
            if (beta > 1.0 || beta <= 0) {
                beta = 1.0;
            }

            double sctRMS = 0;

            if (Math.abs(s) > 0) {
                sctRMS = ((0.0136) / (beta * PhysicsConstants.speedOfLight() * p)) * Math.sqrt(t_ov_X0)
                        * (1 + 0.038 * Math.log(t_ov_X0));
            }

            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;

            fMS.set(
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, cov_txtx, cov_txty, 0,
                    0, 0, cov_txty, cov_tyty, 0,
                    0, 0, 0, 0, 0
            );

            Matrix5x5.copy(fVec.CM, copyMatrix);
            Matrix5x5.add(copyMatrix, fMS, fVec.CM);

            if (Math.abs(fVec.B - BatMeas) < 0.0001) {
                stepSize *= 2;
            }

            BatMeas = fVec.B;
        }

        return fVec.CM;

    }

    /**
     *
     * @param sector
     * @param i initial state vector index
     * @param f final state vector index
     * @param iVec state vector at the initial index
     * @param mv measurements
     */
    public boolean transport(int sector, int i, int f, StateVec iVec, AMeasVecs mv, Swim swimmer, boolean forward) { // s = signed step-size
        if (iVec == null) {
            return false;
        }
        double stepSize = 1.0;
        StateVec fVec = new StateVec(f);
        fVec.x = iVec.x;
        fVec.y = iVec.y;
        fVec.z = iVec.z;
        fVec.tx = iVec.tx;
        fVec.ty = iVec.ty;
        fVec.Q = iVec.Q;
        fVec.B = iVec.B;
        Matrix5x5.copy(iVec.CM, fVec.CM);

        double s = 0;
        double zInit = mv.measurements.get(i).surface.z;
        double BatMeas = iVec.B;

        double z = zInit;
        double zFinal = mv.measurements.get(f).surface.z;

        while (Math.signum(zFinal - zInit) * z < Math.signum(zFinal - zInit) * zFinal) {
            z = fVec.z;
            if (z == zFinal) {
                break;
            }

            double x = fVec.x;
            double y = fVec.y;
            double tx = fVec.tx;
            double ty = fVec.ty;
            double Q = fVec.Q;
            double dPath = fVec.deltaPath;
            Matrix cMat = new Matrix();
            Matrix5x5.copy(fVec.CM, cMat);
            s = Math.signum(zFinal - zInit) * stepSize;

            // LOGGER.log(Level.FINE, " from "+(float)Z[i]+" to "+(float)Z[f]+" at "+(float)z+" By is "+bf[1]+" B is "+Math.sqrt(bf[0]*bf[0]+bf[1]*bf[1]+bf[2]*bf[2])/Bmax+" stepSize is "+s);
            if (Math.signum(zFinal - zInit) * (z + s) > Math.signum(zFinal - zInit) * zFinal) {
                s = Math.signum(zFinal - zInit) * Math.abs(zFinal - z);
            }

            //rk.RK4transport(sector, Q, x, y, z, tx, ty, s, swimmer, cMat, fVec, dPath);
            rk.RK4transport(sector, s, swimmer, cMat, fVec);

            // Q  process noise matrix estimate            
            double p = Math.abs(1. / iVec.Q);

            double X0 = this.getX0(z, Z);
            double t_ov_X0 = Math.abs(s) / X0;//path length in radiation length units = t/X0 [true path length/ X0] ; Ar radiation length = 14 cm

            double beta = this.beta;
            if (beta > 1.0 || beta <= 0) {
                beta = 1.0;
            }

            double sctRMS = 0;

            if (Math.abs(s) > 0) {
                sctRMS = ((0.0136) / (beta * PhysicsConstants.speedOfLight() * p)) * Math.sqrt(t_ov_X0)
                        * (1 + 0.038 * Math.log(t_ov_X0));
            }

            double cov_txtx = (1 + tx * tx) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_tyty = (1 + ty * ty) * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;
            double cov_txty = tx * ty * (1 + tx * tx + ty * ty) * sctRMS * sctRMS;

            fMS.set(
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, cov_txtx, cov_txty, 0,
                    0, 0, cov_txty, cov_tyty, 0,
                    0, 0, 0, 0, 0
            );

            Matrix5x5.copy(fVec.CM, copyMatrix);
            Matrix5x5.add(copyMatrix, fMS, fVec.CM);

            if (Math.abs(fVec.B - BatMeas) < 0.0001) {
                stepSize *= 2;
            }

            BatMeas = fVec.B;
        }

        if (forward) {
            this.trackTrajT.put(f, fVec);
        } else {
            this.trackTrajP.put(f, fVec);
        }

        if (Double.isNaN(fVec.x) || Double.isNaN(fVec.y) || Double.isNaN(fVec.tx) || Double.isNaN(fVec.ty) || Double.isNaN(fVec.Q)) {
            return false;
        } else {
            return true;
        }

    }

    public double getX0(double z, double Z[]) {
        double X0 = AIRRADLEN;
        double tolerance = 0.01;

        for (int i = 1; i < Z.length; i++) {
            if (i % 2 == 0) {
                continue;
            }
            if (z >= Z[i] - tolerance && z <= Z[i + 1] + tolerance) {
                return ARGONRADLEN;
            }
        }
        return X0;
    }

    @Override
    public double[][] Q(StateVec vec, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void corrForEloss(int dir, StateVec vec, AMeasVecs mv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getStateVecPosAtMeasSite(StateVec sv, AMeasVecs.MeasVec mv, Swim swim) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean setStateVecPosAtMeasSite(StateVec sv, MeasVec mv, Swim swimmer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printlnStateVec(StateVec S) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double[][] F(StateVec iVec, StateVec fVec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Helix helix, double[][] cov, double xref, double yref, double zref, double mass, Swim swimmer) {
        throw new UnsupportedOperationException("Not for forward tracking.");

    }

    @Override
    public void init(double x0, double z0, double tx, double tz, Units units, double[][] cov) {
        throw new UnsupportedOperationException("Not for forward tracking.");
    }
}
