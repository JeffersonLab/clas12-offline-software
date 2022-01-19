package org.jlab.rec.cvt.fit;

import java.util.ArrayList;
import java.util.List;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.trajectory.Ray;
import trackfitter.fitter.utilities.ProbChi2perNDF;

/**
 * A fitter which does sequential fit (for x, y coordinates) and then (for r, z
 * coordinates) to a line using LineFitter.
 */
public class CosmicFitter {

    private Ray _ray;  // fit ray
    private LineFitter _linefitRhoZ = new LineFitter();
    private LineFitter _linefitYX = new LineFitter();
    private LineFitter _linefitY_primeZ = new LineFitter();
    private CosmicFitPars _rayfitoutput;
    
    private LineFitPars _linefitparsRhoZ;
    private LineFitPars _linefitparsYX;
    /**
     * Status of the HelicalTrackFit
     */
    public enum FitStatus {

        /**
         * Successful Fit.
         */
        Successful,
        /**
         * line fit failed.
         */
        LineFitFailed,

    };

    /**
     * Creates a new instance of HelicalTrackFitter.
     */
    public CosmicFitter() {
    }

    /**
     * Does fit to 2 sets of inputs : circle fit to array X[i] Y[i] and line fit
     * to array Y_prime[j] Z[j]
     *
     * @param X array i
     * @param Y array i
     * @param Z array j
     * @param Y_prime array j
     * @param errRt error on XY
     * @param errY_prime error on Y_prime
     * @param errZ array j
     * @return
     */
    public FitStatus fit(List<Double> X, List<Double> Y, List<Double> Z, List<Double> Y_prime, List<Double> errRt, List<Double> errY_prime, List<Double> errZ) {

        //  Initialize the various fitter outputs
        _linefitYX = null;
        _linefitY_primeZ = null;
        setrayfitoutput(null);

        for (int j = 0; j < errRt.size(); j++) {
            if (errRt.get(j) == 0) {
                System.err.println("Point errors ill-defined --  fit exiting");
                return FitStatus.LineFitFailed;
            }
        }

        for (int j = 0; j < errZ.size(); j++) {
            if (errZ.get(j) == 0) {
                System.err.println("Point errors ill-defined --  fit exiting");
                return FitStatus.LineFitFailed;
            }
        }

        // fit the points 
        // check the status
        _linefitYX = new LineFitter();
        boolean linefitstatusXYOK = _linefitYX.fitStatus(Y, X, errRt, new ArrayList<>(X.size()), X.size());

        //Line fit
        _linefitY_primeZ = new LineFitter();
        boolean linefitstatusRZOK = _linefitY_primeZ.fitStatus(Y_prime, Z, errY_prime, errZ, Y_prime.size());

        if (!linefitstatusXYOK || !linefitstatusRZOK) {
            return FitStatus.LineFitFailed;
        }
        //  Get the results of the fits
        double yx_slope = _linefitYX.getFit().slope();
        double yz_slope = _linefitY_primeZ.getFit().slope();
        double yx_interc = _linefitYX.getFit().intercept();
        double yz_interc = _linefitY_primeZ.getFit().intercept();
        //errors
        double yx_slope_err = _linefitYX.getFit().slopeErr();
        double yz_slope_err = _linefitY_primeZ.getFit().slopeErr();
        double yx_interc_err = _linefitYX.getFit().interceptErr();
        double yz_interc_err = _linefitY_primeZ.getFit().interceptErr();

        double y_ref = 0; // calc the ref point at the plane y =0
        double x_ref = yx_interc;
        double z_ref = yz_interc;
        Point3D refPoint = new Point3D(x_ref, y_ref, z_ref);
        Vector3D refDir = new Vector3D(yx_slope, 1, yz_slope).asUnit();
        
        Ray the_ray = new Ray(refPoint, refDir);

        the_ray.setYXSlope(yx_slope);
        the_ray.setYXSlopeErr(yx_slope_err);
        the_ray.setYZSlope(yz_slope);
        the_ray.setYZSlopeErr(yz_slope_err);
        the_ray.setYXInterc(yx_interc);
        the_ray.setYXInterErr(yx_interc_err);
        the_ray.setYZInterc(yz_interc);
        the_ray.setYZIntercErr(yz_interc_err);
        
        double[] chisq = new double[]{_linefitYX.getFit().chisq(), _linefitY_primeZ.getFit().chisq()};
        if (the_ray != null) {
            CosmicFitPars the_rayfitoutput = new CosmicFitPars(the_ray, chisq);
            _ray = the_ray;
            the_rayfitoutput.setRay(the_ray);
            the_rayfitoutput.setChi2(chisq);
            setrayfitoutput(the_rayfitoutput);
            _ray.chi2 = ProbChi2perNDF.prob(chisq[0]+chisq[1],errRt.size()+errZ.size());
        }
        return FitStatus.Successful;
    }

    public FitStatus fit2(List<Double> X, List<Double> Y, List<Double> Z, List<Double> Rho, List<Double> errRt, List<Double> errRho, List<Double> ErrZ) {
        //  Initialize the various fitter outputs
        
        _linefitRhoZ = null;
        _linefitYX = null;
        _linefitY_primeZ = null;
        setrayfitoutput(null);

        for (int j = 0; j < errRt.size(); j++) {
            if (errRt.get(j) == 0) {
                System.err.println("Point errors ill-defined --  fit exiting");
                return FitStatus.LineFitFailed;
            }
        }

        for (int j = 0; j < ErrZ.size(); j++) {
            if (ErrZ.get(j) == 0) {
                System.err.println("Point errors ill-defined --  fit exiting");
                return FitStatus.LineFitFailed;
            }
        }
       
        for (int j = 0; j < X.size(); j++) {
            
            if (errRt.get(j) == 0) {
                System.err.println("Point errors ill-defined -- helical fit exiting");
                return FitStatus.LineFitFailed;
            }
        }

        

        //Line fits
        _linefitYX = new LineFitter();
        boolean linefitstatusXYOK = _linefitYX.fitStatus(Y, X, errRt, new ArrayList<>(X.size()), X.size());

        _linefitRhoZ = new LineFitter();
        boolean linefitstatusOK = _linefitRhoZ.fitStatus(Rho, Z, errRho, ErrZ, Z.size());

        if (!linefitstatusOK) {
            return FitStatus.LineFitFailed; 
        }
        //  Get the results of the fits
        _linefitparsRhoZ = _linefitRhoZ.getFit();
        _linefitparsYX = _linefitYX.getFit();

        double fit_tandip = _linefitparsRhoZ.slope();
        double fit_Z0 = _linefitparsRhoZ.intercept();
        
        double yx_slope = _linefitYX.getFit().slope();
        double yz_slope = fit_tandip*Math.sqrt(1+yx_slope*yx_slope);
        double yx_interc = _linefitYX.getFit().intercept();
        double yz_interc = fit_Z0 + fit_tandip*Math.abs(yx_interc);
        
        double y_ref = 0; // calc the ref point at the plane y =0
        double x_ref = yx_slope*y_ref + yx_interc;
        double z_ref = yz_slope*y_ref + yz_interc ;
        
        Point3D refPoint = new Point3D(x_ref, y_ref, z_ref);
        Vector3D refDir = new Vector3D(yx_slope, 1, yz_slope).asUnit();
        Ray the_ray = new Ray(refPoint, refDir);

        the_ray.setYXSlope(yx_slope);
        the_ray.setYZSlope(yz_slope);
        the_ray.setYXInterc(yx_interc);
        the_ray.setYZInterc(yz_interc);

        double[] chisq = new double[]{_linefitYX.getFit().chisq(), _linefitRhoZ.getFit().chisq()};
        if (the_ray != null) {
            CosmicFitPars the_rayfitoutput = new CosmicFitPars(the_ray, chisq);
            _ray = the_ray;
            the_rayfitoutput.setRay(the_ray);
            the_rayfitoutput.setChi2(chisq);
            setrayfitoutput(the_rayfitoutput);
            _ray.chi2 = ProbChi2perNDF.prob(chisq[0]+chisq[1],errRt.size()+ErrZ.size());
        }
        System.out.println(refPoint.toString()+"yzslope "+yz_slope+" yz_interc "+yz_interc+" c2 "+_linefitRhoZ.getFit().chisq());
        return FitStatus.Successful;
    }

    public Ray getray() {
        return _ray;
    }

    public void setray(Ray _ray) {
        this._ray = _ray;
    }

    public CosmicFitPars getrayfitoutput() {
        return _rayfitoutput;
    }

    public void setrayfitoutput(CosmicFitPars _rayfitoutput) {
        this._rayfitoutput = _rayfitoutput;
    }

}
