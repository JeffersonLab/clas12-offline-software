package org.jlab.rec.ahdc.PreCluster;

import org.jlab.rec.ahdc.Hit.Hit;

import java.util.ArrayList;

public class PreCluster implements Comparable<PreCluster> {

	private int            _Id;
	private int            _Super_layer;
	private int            _Layer;
	private double         _Doca;
	private double         _Radius;
	private double         _Phi;
	private double         _Num_wire;
	private double         _X;
	private double         _Y;
	private boolean        _Used = false;
	private ArrayList<Hit> _hits_list;


	public PreCluster(ArrayList<Hit> hit_list) {
		_hits_list        = hit_list;
		this._Super_layer = hit_list.get(0).getSuperLayerId();
		this._Layer       = hit_list.get(0).getLayerId();
		this._Radius      = hit_list.get(0).getRadius();
		this._Num_wire    = hit_list.get(0).getNbOfWires();
		double pre_x = 0.0;
		double pre_y = 0.0;
		for (Hit hit : hit_list) {
			pre_x += hit.getX();
			pre_y += hit.getY();
		}
		pre_x /= hit_list.size();
		pre_y /= hit_list.size();
		this._Phi = mod(-Math.PI / 2 - Math.atan2(pre_y, pre_x), 2 * Math.PI);
		this._X   = -this._Radius * Math.sin(this._Phi);
		this._Y   = -this._Radius * Math.cos(this._Phi);

	}

	public int compareTo(PreCluster arg0) {
		if ((this.get_Radius() > arg0.get_Radius()) || (this.get_Radius() == arg0.get_Radius() && this.get_Phi() > arg0.get_Phi())) {
			return 1;
		} else {
			return 0;
		}
	}

	double mod(double a, double b) {
		return a - b * Math.floor(a / b);
	}

	public ArrayList<Hit> get_hits_list() {
		return _hits_list;
	}

	@Override
	public String toString() {
		return "PreCluster{" + "_Super_layer=" + _Super_layer + ", _Layer=" + _Layer + ", _Radius=" + _Radius + ", _Phi=" + _Phi + '}';
	}

	public int get_Super_layer() {
		return _Super_layer;
	}

	public void set_Super_layer(int _Super_layer) {
		this._Super_layer = _Super_layer;
	}

	public int get_Layer() {
		return _Layer;
	}

	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}

	public double get_Doca() {
		return _Doca;
	}

	public void set_Doca(double _Doca) {
		this._Doca = _Doca;
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

	public int get_Id() {
		return _Id;
	}

	public void set_Id(int _Id) {
		this._Id = _Id;
	}

	public double get_Num_wire() {
		return _Num_wire;
	}

	public void set_Num_wire(double _Num_wire) {
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

	public boolean is_Used() {
		return _Used;
	}

	public void set_Used(boolean _Used) {
		this._Used = _Used;
	}
}
