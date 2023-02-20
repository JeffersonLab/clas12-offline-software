package org.jlab.rec.ahdc.Track;

import org.apache.commons.math3.linear.RealVector;
import org.jlab.rec.ahdc.Cluster.Cluster;
import org.jlab.rec.ahdc.HelixFit.HelixFitObject;
import org.jlab.rec.ahdc.Hit.Hit;
import org.jlab.rec.ahdc.PreCluster.PreCluster;

import java.util.ArrayList;
import java.util.List;

public class Track {

	private       double         _Distance;
	private       List<Cluster>  _Clusters = new ArrayList<>();
	private       boolean        _Used     = false;
	private final ArrayList<Hit> hits      = new ArrayList<>();
	;

	private double x0  = 0;
	private double y0  = 0;
	private double z0  = 0;
	private double px0 = 0;
	private double py0 = 0;
	private double pz0 = 0;

	private double x0_kf  = 0;
	private double y0_kf  = 0;
	private double z0_kf  = 0;
	private double px0_kf = 0;
	private double py0_kf = 0;
	private double pz0_kf = 0;

	public Track(List<Cluster> clusters) {
		this._Clusters = clusters;
		this._Distance = 0;
		for (int i = 0; i < clusters.size() - 1; i++) {
			this._Distance += Math.sqrt((clusters.get(i).get_X() - clusters.get(i + 1).get_X()) * (clusters.get(i).get_X() - clusters.get(i + 1).get_X()) + (clusters.get(i).get_Y() - clusters.get(i + 1).get_Y()) * (clusters.get(i).get_Y() - clusters.get(i + 1).get_Y()));
		}
		generateHitList();
	}

	public void setPositionAndMomentum(HelixFitObject helixFitObject) {
		this.x0  = helixFitObject.get_X0();
		this.y0  = helixFitObject.get_Y0();
		this.z0  = helixFitObject.get_Z0();
		this.px0 = helixFitObject.get_px();
		this.py0 = helixFitObject.get_py();
		this.pz0 = helixFitObject.get_pz();
	}

	public void setPositionAndMomentumForKF(RealVector x) {
		this.x0_kf  = x.getEntry(0);
		this.y0_kf  = x.getEntry(1);
		this.z0_kf  = x.getEntry(2);
		this.px0_kf = x.getEntry(3);
		this.py0_kf = x.getEntry(4);
		this.pz0_kf = x.getEntry(5);
	}

	private void generateHitList() {
		for (Cluster cluster : _Clusters) {
			for (PreCluster preCluster : cluster.get_PreClusters_list()) {
				hits.addAll(preCluster.get_hits_list());
			}
		}
	}

	public ArrayList<Hit> getHits() {
		return hits;
	}

	@Override
	public String toString() {
		return "Track{" + "_Clusters=" + _Clusters + '}';
	}

	public double get_Distance() {
		return _Distance;
	}

	public List<Cluster> get_Clusters() {
		return _Clusters;
	}

	public boolean is_Used() {
		return _Used;
	}

	public void set_Used(boolean _Used) {
		this._Used = _Used;
	}

	public double get_X0() {
		return x0;
	}

	public double get_Y0() {
		return y0;
	}

	public double get_Z0() {
		return z0;
	}

	public double get_px() {
		return px0;
	}

	public double get_py() {
		return py0;
	}

	public double get_pz() {
		return pz0;
	}

	public double getX0_kf() {
		return x0_kf;
	}

	public double getY0_kf() {
		return y0_kf;
	}

	public double getZ0_kf() {
		return z0_kf;
	}

	public double getPx0_kf() {
		return px0_kf;
	}

	public double getPy0_kf() {
		return py0_kf;
	}

	public double getPz0_kf() {
		return pz0_kf;
	}
}
