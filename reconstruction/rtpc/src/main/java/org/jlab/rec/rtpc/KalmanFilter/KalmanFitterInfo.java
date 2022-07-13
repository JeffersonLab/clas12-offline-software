package org.jlab.rec.rtpc.KalmanFilter;

public class KalmanFitterInfo {
	private double _px;
	private double _py;
	private double _pz;
	private double _vz;
	private double _dEdx;
	private double _p_drift;

	public KalmanFitterInfo(
			double _px, double _py, double _pz, double _vz, double _dEdx, double _p_drift) {
		this._px = _px;
		this._py = _py;
		this._pz = _pz;
		this._vz = _vz;
		this._dEdx = _dEdx;
		this._p_drift = _p_drift;
	}

	public double get_px() {
		return _px;
	}

	public void set_px(double _px) {
		this._px = _px;
	}

	public double get_py() {
		return _py;
	}

	public void set_py(double _py) {
		this._py = _py;
	}

	public double get_pz() {
		return _pz;
	}

	public void set_pz(double _pz) {
		this._pz = _pz;
	}

	public double get_vz() {
		return _vz;
	}

	public void set_vz(double _vz) {
		this._vz = _vz;
	}

	public double get_dEdx() {
		return _dEdx;
	}

	public void set_dEdx(double _dEdx) {
		this._dEdx = _dEdx;
	}

	public double get_p_drift() {
		return _p_drift;
	}

	public void set_p_drift(double _p_drift) {
		this._p_drift = _p_drift;
	}
}
