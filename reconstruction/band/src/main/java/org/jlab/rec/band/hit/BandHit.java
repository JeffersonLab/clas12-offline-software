package org.jlab.rec.band.hit;

import java.util.ArrayList;

public class BandHit extends ArrayList<BandHitCandidate> {

	/**
	 * author: Efrain Segarra, Florian Hauenstein
	 * A BandHit consists of a coincidence hit on a bar with no veto hits or other bars fired. Corresponding
	 * hits are found by the BandHitFinder
	 *
	 */

	private static final long serialVersionUID = 1L;    // What is this??

	private int _sector, _layer, _component, _status; 
	private int _indexLpmt, _indexRpmt;
	
	private double _meantimeTdc, _meantimeFadc;
	private double _difftimeTdc, _difftimeFadc;

	private double _adcLcorr, _adcRcorr;
	private double _tFadcLcorr, _tFadcRcorr;
	private double _tTdcLcorr, _tTdcRcorr;

	private double _x, _y, _z;
	private double _ux, _uy, _uz;

	// constructor
	public BandHit() {
		_sector = -1; _layer = -1; _component = -1; _status = -1;
		_indexLpmt = -1; _indexRpmt = -1;

		_meantimeTdc = -2000.; _meantimeFadc = -2000.;
		_difftimeTdc = -2000.; _difftimeFadc = -2000.;

		_adcLcorr = -2000.; _adcRcorr  = -2000.;
		_tFadcLcorr = -2000.; _tFadcRcorr = -2000.;
		_tTdcLcorr = -2000.; _tTdcRcorr = -2000.;

		_x = -2000.; _y = -2000.; _z = -2000.;
		_ux = -2000.; _uy = -2000.; _uz = -2000.;

	}

	// Show class for debugging
	public void Print(){
		System.out.println(_sector+" "+_layer+" "+_component);
		System.out.println("\t"+_x+" "+_y+" "+_z);
		System.out.println("\t"+_difftimeFadc+" "+_adcLcorr+" "+_adcRcorr);
		System.out.println("\t"+_status);
	}

	// Grab functions
	public int GetSector() 		{return _sector;}
	public int GetLayer()		{return _layer;}
	public int GetComponent()	{return _component;}
	public int GetStatus()		{return _status;}

	public double GetMeanTime_TDC() 	{return _meantimeTdc;}
	public double GetMeanTime_FADC() 	{return _meantimeFadc;}
	public double GetDiffTime_TDC()		{return _difftimeTdc;}
	public double GetDiffTime_FADC()	{return _difftimeFadc;}

	public int GetIndexLpmt()		{return _indexLpmt;}
	public int GetIndexRpmt()		{return _indexRpmt;}

	public double GetAdcLeft()		{return _adcLcorr;}
	public double GetAdcRight()		{return  _adcRcorr;}
	public double GetTLeft_FADC()	{return _tFadcLcorr;}
	public double GetTRight_FADC()	{return _tFadcRcorr;}
	public double GetTLeft_TDC()	{return _tTdcLcorr;}
	public double GetTRight_TDC()	{return _tTdcRcorr;}

	public double GetX()	{return _x;}
	public double GetY()	{return _y;}
	public double GetZ()	{return _z;}
	public double GetUx()	{return _ux;}
	public double GetUy()	{return _uy;}
	public double GetUz()	{return _uz;}

	// Set functions
	public void SetSector(int sector) 			{this._sector = sector; }
	public void SetLayer(int layer)	 			{this._layer = layer; }
	public void SetComponent(int component)			{this._component = component;}
	public void SetStatus(int status)			{this._status = status;}	

	public void SetMeanTime_TDC(double meanTimeTDC)		{this._meantimeTdc = meanTimeTDC;}
	public void SetMeanTime_FADC(double meanTimeFADC)	{this._meantimeFadc = meanTimeFADC;}
	public void SetDiffTime_TDC(double diffTimeTDC)		{this._difftimeTdc = diffTimeTDC;}
	public void SetDiffTime_FADC(double diffTimeFADC)	{this._difftimeFadc = diffTimeFADC;}

	public void SetIndexLpmt(int indexL)			{this._indexLpmt = indexL;}
	public void SetIndexRpmt(int indexR)			{this._indexRpmt = indexR;}
	
	public void SetAdcLeft(double adcL)			{this._adcLcorr = adcL;}
	public void SetAdcRight(double adcR)		{this._adcRcorr = adcR;}
	public void SetTLeft_FADC(double ftdcL)		{this._tFadcLcorr = ftdcL;}
	public void SetTRight_FADC(double ftdcR)	{this._tFadcRcorr = ftdcR;}
	public void SetTLeft_TDC(double tdcL)		{this._tTdcLcorr = tdcL;}
	public void SetTRight_TDC(double tdcR)		{this._tTdcRcorr = tdcR;}

	public void SetX(double x)		{this._x = x;}
	public void SetY(double y)		{this._y = y;}
	public void SetZ(double z)		{this._z = z;}
	public void SetUx(double ux)	{this._ux = ux;}
	public void SetUy(double uy)	{this._uy = uy;}
	public void SetUz(double uz)	{this._uz = uz;}








}
