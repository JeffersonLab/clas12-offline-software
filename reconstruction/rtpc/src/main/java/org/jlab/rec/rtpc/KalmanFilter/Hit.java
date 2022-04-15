package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Hit implements Comparable<Hit> {

    private final double _r, _phi, _z;

    public Hit(double r, double phi, double z) {
        this._r = r;
        this._phi = phi;
        this._z = z;
    }

    public double get_r() {
        return _r;
    }

    public RealVector get_Vector() {
        return new ArrayRealVector(new double[]
                {this._r, this._phi, this._z}
        );
    }

    public RealMatrix get_MeasurementNoise() {
        double deltaR = 70. / this._r * 2;
        double deltaPhi = Math.toRadians(2) * 2;
        double deltaZ = 4;

        return new Array2DRowRealMatrix(new double[][]{
                {deltaR * deltaR, 0, 0},
                {0, deltaPhi * deltaPhi, 0},
                {0, 0, deltaZ * deltaZ}
        });
    }

    @Override
    public int compareTo(Hit o) {
        return Double.compare(_r, o.get_r());
    }

    @Override
    public String toString() {
        return "Hit{" +
                "r=" + this._r +
                ", phi=" + this._phi +
                ", z=" + this._z +
                '}' + '\n';
    }
}
