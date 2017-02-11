package org.jlab.rec.ft.hodo;


import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ft.hodo.FTHODOHit;



public class FTHODOSignal extends ArrayList<FTHODOHit> {

	/**
	 * A signal in the hodoscope is a pair of hits above threshold in the two layers. 
	 * The hit pair is define according o the algorithm in HodoSignalFinder
	 */
	private static final long serialVersionUID = 1L;

	private int _signalID;			   			// signal ID
	private int _signalSize;					// number of hits forming the signal
	private double _signalX, _signalY, _signalZ;// <X>, <Y> average of the signal pair
	private double _signalDX, _signalDY;        // distance in X and Y between the two layers
	private double _signalTime;      		    // average Time value
	private double _signalEnergy;			    // total energy of the cluster
	private double _signalTheta, _signalPhi;    // signal Polar and Azimuth angles in the lab
	
	
	
	// constructor
	public FTHODOSignal(int cid) {
		this.set_signalID(cid);
	}

	public int get_signalID() {
		return _signalID;
	}

	public void set_signalID(int _signalID) {
		this._signalID = _signalID;
	}

	public int get_signalSize() {
		return _signalSize;
	}

	public void set_signalSize(int _signalSize) {
		this._signalSize = _signalSize;
	}
	
	public double get_signalX() {
		return _signalX;
	}

	public void set_signalX(double signalX) {
		this._signalX = signalX;
	}

	public double get_signalY() {
		return _signalY;
	}

	public void set_signalY(double signalY) {
		this._signalY = signalY;
	}


	public double get_signalZ() {
		return _signalZ;
	}

	public void set_signalZ(double signalZ) {
		this._signalZ = signalZ;
	}


	public double get_signalDX() {
		return _signalDX;
	}

	public void set_signalDX(double signalDX) {
		this._signalDX = signalDX;
	}

	public double get_signalDY() {
		return _signalDY;
	}

	public void set_signalDY(double signalDY) {
		this._signalDY = signalDY;
	}

	public double get_signalTime() {
		return _signalTime;
	}

	public void set_signalTime(double signalTime) {
		this._signalTime = signalTime;
	}

	public double get_signalEnergy() {
		return _signalEnergy;
	}

	public void set_signalEnergy(double signalEnergy) {
		this._signalEnergy = signalEnergy;
	}

	public double get_signalTheta() {
		return _signalTheta;
	}

	public void set_signalTheta(double signalTheta) {
		this._signalTheta = signalTheta;
	}

	public double get_signalPhi() {
		return _signalPhi;
	}

	public void set_signalPhi(double signalPhi) {
		this._signalPhi = signalPhi;
	}
	
	
	
	public static  ArrayList<FTHODOSignal> getSignals(EvioDataBank bank) {

	     int[] signalID           = bank.getInt("signalID");
	     int[] signalSize         = bank.getInt("signalSize");
	     double[] signalX         = bank.getDouble("signalX");
	     double[] signalY         = bank.getDouble("signalY");
	     double[] signalDX        = bank.getDouble("signalDX");
	     double[] signalDY        = bank.getDouble("signalDY");
	     double[] signalTime      = bank.getDouble("signalTime");
	     double[] signalEnergy    = bank.getDouble("signalEnergy");
	     double[] signalTheta     = bank.getDouble("signalTheta");
	     double[] signalPhi       = bank.getDouble("signalPhi");


	     int size = signalID.length;
	     ArrayList<FTHODOSignal> signals = new ArrayList<FTHODOSignal>();
	      
	     for(int i = 0; i<size; i++){  
	    	 FTHODOSignal signal = new FTHODOSignal(signalID[i]);	
	    	 signal.set_signalSize(signalSize[i]);
	    	 signal.set_signalX(signalX[i]);
	    	 signal.set_signalY(signalY[i]);
	    	 signal.set_signalDX(signalDX[i]);
	    	 signal.set_signalDY(signalDY[i]);
	    	 signal.set_signalTime(signalTime[i]);
	    	 signal.set_signalEnergy(signalEnergy[i]);
	    	 signal.set_signalTheta(signalTheta[i]);
	    	 signal.set_signalPhi(signalPhi[i]);

            signals.add(signal); 
	          
	      }
	      return signals;
	      
	}

	
}
	

	

