package org.jlab.rec.ahdc.Cluster;

import org.jlab.rec.ahdc.Hit.Hit;
import org.jlab.rec.ahdc.PreCluster.PreCluster;

import java.util.ArrayList;

/**
 * Cluster are compose by 2 PreCluster on layer with a different stereo angle
 */
public class Cluster {

	private double                _Radius;
	private double                _Phi;
	private double                _Z;
	private boolean               _Used = false;
	private int                   _Num_wire;
	private double                _X;
	private double                _Y;
	private double                _U;
	private double                _V;
	private ArrayList<PreCluster> _PreClusters_list;


	public Cluster(PreCluster precluster, PreCluster other_precluster) {
		this._PreClusters_list = new ArrayList<>();
		_PreClusters_list.add(precluster);
		_PreClusters_list.add(other_precluster);
		this._Radius = (precluster.get_Radius() + other_precluster.get_Radius()) / 2;
		this._Z      = ((other_precluster.get_Phi() - precluster.get_Phi()) / (Math.toRadians(20) * Math.pow(-1, precluster.get_Super_layer()) - Math.toRadians(20) * Math.pow(-1, other_precluster.get_Super_layer()))) * 300 - 150;
		double x1     = -precluster.get_Radius() * Math.sin(precluster.get_Phi());
		double y1     = -precluster.get_Radius() * Math.cos(precluster.get_Phi());
		double x2     = -other_precluster.get_Radius() * Math.sin(other_precluster.get_Phi());
		double y2     = -other_precluster.get_Radius() * Math.cos(other_precluster.get_Phi());
		double x_mean = (x1 + x2) / 2;
		double y_mean = (y1 + y2) / 2;
		this._Phi      = mod(-Math.PI / 2 - Math.atan2(y_mean, x_mean), (2 * Math.PI));
		this._Num_wire = (int) (precluster.get_Num_wire() + other_precluster.get_Num_wire()) / 2;
		this._X        = -this._Radius * Math.sin(this._Phi);
		this._Y        = -this._Radius * Math.cos(this._Phi);
		this._U        = this._X / (this._X * this._X + this._Y * this._Y);
		this._V        = this._Y / (this._X * this._X + this._Y * this._Y);
	}

	public Cluster(double X, double Y, double Z) {
		this._X = X;
		this._Y = Y;
		this._Z = Z;
	}

	@Override
	public String toString() {
		return "Cluster{" + "_X=" + _X + ", _Y=" + _Y + ", _Z=" + _Z + '}';
	}

	public ArrayList<PreCluster> get_PreClusters_list() {
		return _PreClusters_list;
	}

	double mod(double a, double b) {
		return a - b * Math.floor(a / b);
	}

	public double get_Radius() {
		return _Radius;
	}

	public void set_Radius(double _Radius) {
		this._Radius = _Radius;
	}

	public double get_Phi() {
		return _Phi;
	}

	public void set_Phi(double _Phi) {
		this._Phi = _Phi;
	}

	public double get_Z() {
		return _Z;
	}

	public void set_Z(double _Z) {
		this._Z = _Z;
	}

	public boolean is_Used() {
		return _Used;
	}

	public void set_Used(boolean _Used) {
		this._Used = _Used;
	}

	public int get_Num_wire() {
		return _Num_wire;
	}

	public void set_Num_wire(int _Num_wire) {
		this._Num_wire = _Num_wire;
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

	public double get_U() {
		return _U;
	}

	public void set_U(double _U) {
		this._U = _U;
	}

	public double get_V() {
		return _V;
	}

	public void set_V(double _V) {
		this._V = _V;
	}
}
