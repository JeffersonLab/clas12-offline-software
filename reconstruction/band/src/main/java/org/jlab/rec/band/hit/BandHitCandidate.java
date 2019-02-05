package org.jlab.rec.band.hit;

import org.jlab.rec.band.constants.Parameters;

public class BandHitCandidate {

	public BandHitCandidate(int sector, int layer, int component, int order, double triggerphase, int adc, int tdc) 
	{
		this._sector = sector;
		this._layer = layer;
		this._component = component;	
		this._side = order;
		
		this._AttCorr = (double) adc * Parameters.adcConv[sector-1][layer-1][component-1] //conversion of ADC to MeVee
										+ Parameters.attL[sector-1][layer-1][component-1]; //correct for Attenuation
		this._tCorr = ((double)tdc * Parameters.tdcConv[sector-1][layer-1][component-1])
                                        - Parameters.tdcOffsetLR[sector-1][layer-1][component-1] 
                                        - Parameters.tdcOffsetLayer[layer-1]
                                        - Parameters.tdcOffset
                                        - triggerphase;
                
		

	}

	private double _AttCorr;      // Attenuated corrected MeVee on the PMT
	private double _tCorr;     // Time (ns) corrected for offset 
	
	private int _sector;       // sector of BAND  in which signal is registered 				
	private int _layer;        // layer in which the signal is registered
	private int _component;    // component in which the signal is registered

	private int _side;    // side of Hit. Side 0 for left PMT on a bar or side 1 for right PMT on a bar
	
	
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

	public double GetAttCorr() {
		return _AttCorr;
	}

	public double GetTimeCorr() {
		return _tCorr;
	}


}
