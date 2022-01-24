package org.jlab.rec.rtpc.KF;

import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;
import org.jlab.clas.swimtools.Swim;

public class Site implements Comparable<Site> {

    private SimpleMatrix _m;
    private double _r,_phi,_z;
    private float[] b = new float[]{0,0,0};

    public Site(SimpleMatrix m) {
        this._m = m;
        this._r = m.get(0,0);
        this._phi = m.get(1,0);
        this._z = m.get(2,0);
        Swim swim = new Swim();
        swim.BfieldLab( get_x() / 10, get_y() / 10, _z / 10, this.b);
    }

    public Site(double r, double phi, double z){
        this._r = r;
        this._phi = phi;
        this._z = z;
        this._m = new SimpleMatrix(new double[][]{{r},{phi},{z}});
        Swim swim = new Swim();
        swim.BfieldLab( get_x() / 10, get_y() / 10, _z / 10, this.b);
    }

    public double alpha(){
        double c = 0.000299792458;
        double B = -Math.sqrt(b[0] * b[0] + b[1] * b[1] + b[2] * b[2]);
        return  1. / (c * B);
    }

    public SimpleMatrix R() {
        double deltaR = 70. / this._r;
        double deltaPhi = Math.toRadians(1);
        double deltaZ = 2;

        return new SimpleMatrix(
                new double[][] { { deltaR * deltaR, 0, 0 }, { 0, deltaPhi * deltaPhi, 0 }, { 0, 0, deltaZ * deltaZ } }).scale(10);
    }

    public SimpleMatrix R_beam() {
        double deltaR = 2;
        double deltaPhi = 2*Math.PI;
        double deltaZ = 200.;

        return new SimpleMatrix(
                new double[][] { { deltaR * deltaR, 0, 0 }, { 0, deltaPhi * deltaPhi, 0 }, { 0, 0, deltaZ * deltaZ } });
    }

    public SimpleMatrix get_m() {
        return _m;
    }

    public void set_m(SimpleMatrix _m) {
        this._m = _m;
    }

    public double get_r() {
        return _r;
    }

    public void set_r(double _r) {
        this._r = _r;
    }

    public double get_phi() {
        return _phi;
    }

    public void set_phi(double _phi) {
        this._phi = _phi;
    }

    public double get_z() {
        return _z;
    }

    public void set_z(double _z) {
        this._z = _z;
    }

    public double get_x(){
        return _r * Math.cos(_phi);
    }

    public double get_y(){
        return _r * Math.sin(_phi);
    }

    public float[] getB() {
        return b;
    }

    public void setB(float[] b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return "Site{" +
                "r=" + _r +
                ", phi=" + _phi +
                ", z=" + _z +
                " m = " + _m.get(0,0) + ',' + _m.get(1,0) + ','+ _m.get(2,0) +
        '}';
    }

    @Override
    public int compareTo(@NotNull Site o) {
        return Double.compare(_r, o.get_r());
    }
}
