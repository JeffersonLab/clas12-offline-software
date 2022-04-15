package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MeasurementModel {

    static RealVector h(RealVector x) {

        double xx = x.getEntry(0);
        double yy = x.getEntry(1);
        double zz = x.getEntry(2);

        double r = Math.hypot(xx, yy);
        double phi = Math.atan2(yy, xx);

        return MatrixUtils.createRealVector(new double[]{r, phi, zz});
    }

    static RealMatrix H(RealVector x) {

        double xx = x.getEntry(0);
        double yy = x.getEntry(1);
        double zz = x.getEntry(2);

        double drdx = (xx) / (Math.hypot(xx, yy));
        double drdy = (yy) / (Math.hypot(xx, yy));
        double drdz = 0.0;
        double drdpx = 0.0;
        double drdpy = 0.0;
        double drdpz = 0.0;

        double dphidx = -(yy) / (xx * xx + yy * yy);
        double dphidy = (xx) / (xx * xx + yy * yy);
        double dphidz = 0.0;
        double dphidpx = 0.0;
        double dphidpy = 0.0;
        double dphidpz = 0.0;

        double dzdx = 0.0;
        double dzdy = 0.0;
        double dzdz = 1.0;
        double dzdpx = 0.0;
        double dzdpy = 0.0;
        double dzdpz = 0.0;


        return MatrixUtils.createRealMatrix(new double[][]{
                {drdx, drdy, drdz, drdpx, drdpy, drdpz},
                {dphidx, dphidy, dphidz, dphidpx, dphidpy, dphidpz},
                {dzdx, dzdy, dzdz, dzdpx, dzdpy, dzdpz}});
    }
}
