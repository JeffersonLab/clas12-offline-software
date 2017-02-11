package org.jlab.rec.ft;







public class FTTrack {

	
	private int _trackID;			   			 // track ID
	private int _trackCharge;		         	 // 0/1 for photon/electron
	private int _trackSize;						 // number of crystals in the cluster associated to the track
	private double _trackX, _trackY, _trackZ;    // end point of the track on the FT (determined from the FTTRK for electrons and from FTCAL for photons)
	private double _trackTime;      			 // time of impact on the FT 
	private double _trackEnergy;			     // total energy of the cluster including correction
	private double _trackTheta, _trackPhi;		 // track Polar and Azimuth angles in the lab
	private double _trackCX, _trackCY, _trackCZ; // track unit vector
	private int _trackCluster;					 // track pointer to cluster information in FTCALRec::cluster bank
	private int _trackSignal;					 // track pointer to signal information in FTHODORec::cluster bank
	private int _trackCross;					 // track pointer to cross information in FTTRKRec::cluster bank

	
	// constructor
	public FTTrack(int cid) {
		this.set_trackID(cid);
	}


	public int get_trackID() {
		return _trackID;
	}


	public void set_trackID(int _trackID) {
		this._trackID = _trackID;
	}


	public int get_trackCharge() {
		return _trackCharge;
	}


	public void set_trackCharge(int _trackCharge) {
		this._trackCharge = _trackCharge;
	}


	public int get_trackSize() {
		return _trackSize;
	}


	public void set_trackSize(int _trackSize) {
		this._trackSize = _trackSize;
	}


	public double get_trackX() {
		return _trackX;
	}


	public void set_trackX(double _trackX) {
		this._trackX = _trackX;
	}


	public double get_trackY() {
		return _trackY;
	}


	public void set_trackY(double _trackY) {
		this._trackY = _trackY;
	}


	public double get_trackZ() {
		return _trackZ;
	}


	public void set_trackZ(double _trackZ) {
		this._trackZ = _trackZ;
	}


	public double get_trackTime() {
		return _trackTime;
	}


	public void set_trackTime(double _trackTime) {
		this._trackTime = _trackTime;
	}


	public double get_trackEnergy() {
		return _trackEnergy;
	}


	public void set_trackEnergy(double _trackEnergy) {
		this._trackEnergy = _trackEnergy;
	}


	public double get_trackTheta() {
		return _trackTheta;
	}


	public void set_trackTheta(double _trackTheta) {
		this._trackTheta = _trackTheta;
	}


	public double get_trackPhi() {
		return _trackPhi;
	}


	public void set_trackPhi(double _trackPhi) {
		this._trackPhi = _trackPhi;
	}


	public double get_trackCX() {
		_trackCX = Math.sin(this.get_trackTheta()/180.*Math.PI)*Math.cos(this.get_trackPhi()/180.*Math.PI);
		return _trackCX;
	}

	
	public double get_trackCY() {
		_trackCY=Math.sin(this.get_trackTheta()/180.*Math.PI)*Math.sin(this.get_trackPhi()/180.*Math.PI);
		return _trackCY;
	}


	public double get_trackCZ() {
		_trackCZ=Math.cos(this.get_trackTheta()/180.*Math.PI);
		return _trackCZ;
	}


	public void set_trackDir(double _trackCX,double _trackCY,double _trackCZ) {
		this._trackCX = _trackCX;
		this._trackCY = _trackCY;
		this._trackCZ = _trackCZ;
		this._trackTheta = Math.acos(this._trackCZ)*180/Math.PI;
		this._trackPhi   = Math.atan2(this._trackCY,this._trackCX)*180/Math.PI;
	}


	public int get_trackCluster() {
		return _trackCluster;
	}


	public void set_trackCluster(int _trackCluster) {
		this._trackCluster = _trackCluster;
	}


	public int get_trackSignal() {
		return _trackSignal;
	}


	public void set_trackSignal(int _trackSignal) {
		this._trackSignal = _trackSignal;
	}


	public int get_trackCross() {
		return _trackCross;
	}


	public void set_trackCross(int _trackCross) {
		this._trackCross = _trackCross;
	}

	
}
