package org.jlab.rec.ahdc.Hit;

public class TrueHit {
    private double _X;
    private double _Y;
    private double _Z;
    private int _Pid;

    public double get_TrackE() {
        return _TrackE;
    }

    public void set_TrackE(double _TrackE) {
        this._TrackE = _TrackE;
    }

    private double _TrackE;

    public int get_Pid() {
        return _Pid;
    }

    public void set_Pid(int _Pid) {
        this._Pid = _Pid;
    }

    public double get_X() {
        return _X;
    }

    public void set_X(double _X) {
        this._X = _X;
    }

    public double get_Y() {
        return _Y;
    }

    public void set_Y(double _Y) {
        this._Y = _Y;
    }

    public double get_Z() {
        return _Z;
    }

    public void set_Z(double _Z) {
        this._Z = _Z;
    }

    public TrueHit(int pid, double _X, double _Y, double _Z, double _TrackE){
        this._Pid = pid;
        this._X = _X;
        this._Y = _Y;
        this._Z = _Z;
        this._TrackE = _TrackE;
    }

    @Override
    public String toString() {
        return "TrueHit{" +
                "_X=" + _X +
                ", _Y=" + _Y +
                ", _Z=" + _Z +
                '}';
    }
}
