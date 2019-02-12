package org.jlab.rec.band.hit;

import java.util.ArrayList;

public class BandHit extends ArrayList<BandHitCandidate> {

	/**
	 * author: Florian Hauenstein
	 * A BandHit consists of a coincidence hit on a bar with no veto hits or other bars fired. Corresponding
	 * hits are found by the BandHitFinder
	 *
	 */
	
	private static final long serialVersionUID = 1L;    // What is this??

	private double _diffTime;       	   // tL - tR
	private double _meanTime;       	   // reconstructed time of hit (corresponds to (tL+tR)/2
	private double _x;       	          // x co-ordinate of hit (wrt target center)
	private double _y;       	          // y co-ordinate of hit (wrt target center)
	private double _z;       	          // z co-ordinate of hit (wrt target center)
	private double _ux;					  // uncertainty in hit x coordinate
	private double _uy;					  // uncertainty in hit y coordinate
	private double _uz;					  // uncertainty in hit z coordinate
	private double _tdcLeft;		 	//Corrected TDC left PMT in ns
	private double _tdcRight;			//Corrected TDC right PMT in ns
	private double _ftdcLeft;		 	//Corrected FADC time left PMT in ns
	private double _ftdcRight;			//Corrected FADC time right PMT in ns	
	private double _adcLeft;			//Corrected ADC left PMT 
	private double _adcRight;			//Corrected ADC left PMT 
	
	private int _sector, _layer, _component; 
	


	// constructor
	public BandHit() {
		_sector = -1;
		_layer = -1;
		_component = -1;
		_x = -1;
		_y = -1;
		_z = -1;
		_ux = -1;
		_uy = -1;
		_uz = -1;
		_meanTime = -2000;
		_diffTime = -2000;
		_tdcLeft = -2000;
		_tdcRight = -2000;
		_ftdcLeft = -2000;
		_ftdcRight = -2000;
		_adcLeft = -2000;
		_adcRight = -2000;
	}

	public double GetDiffTime() {
		return _diffTime;
	}
	
	public void SetDiffTime(double time) {
		this._diffTime = time;
	}
	
	public double GetMeanTime() {
		return _meanTime;
	}
	
	public void SetMeanTime(double time) {
		this._meanTime = time;
	}

	public double GetX() {
		return _x;
	}

	public double GetY() {
		return _y;
	}

	public double GetZ() {
		return _z;
	}

	public void SetX(double xpos) {
		this._x = xpos;
	}

	public void SetY(double ypos) {
		this._y = ypos;
	}

	public void SetZ(double zpos) {
		this._z = zpos;
	}

	public double GetUx() {
		return _ux;
	}

	public double GetUy() {
		return _uy;
	}

	public double GetUz() {
		return _uz;
	}

	public void SetUx(double xunc) {
		this._ux = xunc;
	}

	public void SetUy(double yunc) {
		this._uy = yunc;
	}

	public void SetUz(double zunc) {
		this._uz = zunc;
	}

	public double GetTdcLeft() {
		return _tdcLeft;
	}
	
	public double GetTdcRight() {
		return _tdcRight;
	}
	
	public void SetTdcLeft(double tdc) {
		this._tdcLeft = tdc;
	}
	
	public void SetTdcRight(double tdc) {
		this._tdcRight = tdc;
	}
	
	public double GetFtdcLeft() {
		return _ftdcLeft;
	}
	
	public double GetFtdcRight() {
		return _ftdcRight;
	}
	
	public void SetFtdcLeft(double tdc) {
		this._ftdcLeft = tdc;
	}
	
	public void SetFtdcRight(double tdc) {
		this._ftdcRight = tdc;
	}
	
	public double GetAdcLeft() {
		return _adcLeft;
	}
	
	public double GetAdcRight() {
		return _adcRight;
	}
	
	public void SetAdcLeft(double adc) {
		this._adcLeft = adc;
	}
	
	public void SetAdcRight(double adc) {
		this._adcRight = adc;
	}

	
	public int GetSector() {
		return _sector;
	}

	public void SetSector(int sector) {
		this._sector = sector;
	}

	public int GetLayer() {
		return _layer;
	}

	public void SetLayer(int layer) {
		this._layer = layer;
	}

	public int GetComponent() {
		return _component;
	}

	public void SetComponent(int component) {
		this._component = component;
	}		

	

	
}
