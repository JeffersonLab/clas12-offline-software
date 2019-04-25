package org.jlab.rec.band.hit;



public class BandHitCandidate {

	public BandHitCandidate(int sector, int layer, int component, int order, int adc, double tdc, float ftdc, double triggerPhase) 
	{

		this._sector 	= sector;
		this._layer 	= layer;
		this._component = component;	
		this._side 		= order+1;  //Left side is 1 and right side is 2

		this._tCorr 	= tdc - triggerPhase;

		this._adc 		= adc;
		this._ftdc 		= ftdc;
		this._tdc 		= tdc;
                

		//System.out.println("Created BandHitCandidate: adc "+ this._adc +" tdc "+this._tdc+" ftdc "+this._ftdc+ " AttCorr " + this._AttCorr + " tcorr "+ this._tCorr);

                

	}

	private int _sector;       		// sector of BAND  in which signal is registered 				
	private int _layer;        		// layer in which the signal is registered
	private int _component;    		// component in which the signal is registered
	private int _side;    			// side of Hit. Side 0 for left PMT on a bar or side 1 for right PMT on a bar

	private double _tCorr;			// Time (ns) corrected for offset 

	private int _adc;    			// Raw adc
	private float _ftdc;     		// Raw tdc from FADC
	private double _tdc;      		// Raw tdc


	public int GetSector() {
		return _sector;
	}	

	public int GetLayer() {
		return _layer;
	}	

	public int GetComponent() {
		return _component;
	}

	public int GetSide() {
		return _side;
	}

	public double GetTimeCorr() {
		return _tCorr;
	}

	public int GetAdc() {
		return _adc;
	}

	public double GetTdc() {
		return _tdc;
	}

	public float GetFtdc() {
		return _ftdc;
	}


}
