package org.jlab.rec.ft.hodo;


import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ft.hodo.FTHODOConstantsLoader;
import org.jlab.rec.ft.hodo.FTHODOHit;


public class FTHODOHit implements Comparable<FTHODOHit>{
	// class implements Comparable interface to allow for sorting a collection of hits by Edep values
	
	// constructor 
	public FTHODOHit(int i, int Sector, int Layer, int ID, int ADC, int TDC) {
		this._Sector = Sector;
		this._Layer = Layer;
		this._ID    = ID;
		this._ADC   = ADC;
		this._TDC   = TDC;
				
		this.set_Edep(((double) this._ADC)*FTHODOConstantsLoader.FADC_TO_CHARGE
				                          *FTHODOConstantsLoader.mips_energy[Sector-1][Layer-1][ID-1]
			                         	  /FTHODOConstantsLoader.mips_charge[Sector-1][Layer-1][ID-1]);
		this.set_Time(((double) this._TDC)/FTHODOConstantsLoader.TIMECONVFAC
				                          -FTHODOConstantsLoader.time_offset[Sector-1][Layer-1][ID-1]); 	// Time set to gemc value
		this.set_Dx(FTHODOConstantsLoader.px[Sector-1][Layer-1][ID-1]);
		this.set_Dy(FTHODOConstantsLoader.py[Sector-1][Layer-1][ID-1]);
		this.set_Dz(FTHODOConstantsLoader.pz[Sector-1][Layer-1][ID-1]);
		this.set_DGTZIndex(i);
		this.set_SignalIndex(0);
//		System.out.println(this._Dx + " " + this._Dy);
	}

	

	private int _ID;    	 				//	   ID
	private int _Sector;    	 			//	   Sector
	private int _Layer;    	 				//	   Layer
	private int _ADC;    	 				//	   ADC
	private int _TDC;    	 				//	   TDC 
	
	private double _Edep;      				//	   	Reconstructed energy deposited by the hit in the tile 
	private double _Time;      				//	   	Reconstructed time, for now it is the gemc time
	private double _Dx;						//	   	X position (corresponds to tile centroid) 
	private double _Dy;						//	   	Y position (corresponds to tile centroid) 
	private double _Dz;						//	   	Z position (corresponds to tile centroid) 
	private int    _DGTZIndex;				//		Pointer to cluster
	private int    _SignalIndex;			//		Pointer to cluster


	
	public int get_ID() {
		return _ID;
	}



	public void set_ID(int ID) {
		this._ID = ID;
	}



	public int get_Sector() {
		return _Sector;
	}



	public void set_Sector(int Sector) {
		this._Sector = Sector;
	}



	public int get_Layer() {
		return _Layer;
	}



	public void set_Layer(int Layer) {
		this._Layer = Layer;
	}



	public int get_ADC() {
		return _ADC;
	}



	public void set_ADC(int ADC) {
		this._ADC = ADC;
	}



	public int get_TDC() {
		return _TDC;
	}



	public void set_TDC(int TDC) {
		this._TDC = TDC;
	}
	
	
	public double get_Edep() {
		return _Edep;
	}


	public void set_Edep(double edep) {
		this._Edep = edep;
	}



	public double get_Time() {
		return _Time;
	}


	public void set_Time(double Time) {
		this._Time = Time;
	}


	public double get_Dx() {
		return _Dx;
	}



	public void set_Dx(double Dx) {
		this._Dx = Dx;
	}



	public double get_Dy() {
		return _Dy;
	}



	public void set_Dy(double Dy) {
		this._Dy = Dy;
	}


	public double get_Dz() {
		return _Dz;
	}



	public void set_Dz(double Dz) {
		this._Dz = Dz;
	}


	public int get_DGTZIndex() {
		return _DGTZIndex;
	}



	public void set_DGTZIndex(int _DGTZIndex) {
		this._DGTZIndex = _DGTZIndex;
	}



	public int get_SignalIndex() {
		return _SignalIndex;
	}



	public void set_SignalIndex(int _SignalIndex) {
		this._SignalIndex = _SignalIndex;
	}



	public static  ArrayList<FTHODOHit> getRawHits(EvioDataBank bank) {

	     int[] id     = bank.getInt("component");
	     int[] sector = bank.getInt("sector");
	     int[] layer  = bank.getInt("layer");
	     int[] adc    = bank.getInt("ADC");
	     int[] tdc    = bank.getInt("TDC");
	     
	     int size = id.length;

	     ArrayList<FTHODOHit> hits = new ArrayList<FTHODOHit>();
	      
	      for(int i = 0; i<size; i++){
	  
	          if(adc[i]!=-1 && tdc[i]!=-1 ){
	        	 FTHODOHit hit = new FTHODOHit(i,sector[i], layer[i], id[i], adc[i], tdc[i]);
	             hits.add(hit); 
	          }
	          
	      }
	      return hits;
	      
	}
	
	
	
	public static boolean passHitSelection(FTHODOHit hit) {
		// a selection cut to pass the hit. 
		if(hit.get_Edep() > FTHODOConstantsLoader.EN_THRES) {
			return true;
		} else {
			return false;
		}		
	}
	
	

	@Override
	public int compareTo(FTHODOHit arg0) {
		if(this.get_Edep()<arg0.get_Edep()) {
			return 1;
		} else {
			return -1;
		}
	}

		
}
