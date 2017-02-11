package org.jlab.rec.ft.cal;

import java.util.ArrayList;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ft.cal.FTCALConstantsLoader;



public class FTCALHit implements Comparable<FTCALHit>{
	// class implements Comparable interface to allow for sorting a collection of hits by Edep values
	
	// constructor 
	public FTCALHit(int i, int ICOMPONENT, int ADC, int TDC) {
		this._COMPONENT = ICOMPONENT;
		this._IDY = ((int) ICOMPONENT/22) + 1;
		this._IDX = ICOMPONENT + 1 - (this._IDY-1)*22;
		this._ADC = ADC;
		this._TDC = TDC;
		
		this.set_Edep(((double) this._ADC)*FTCALConstantsLoader.fadc_to_charge[0][0][ICOMPONENT-1]
										  *FTCALConstantsLoader.mips_energy[0][0][ICOMPONENT-1]
										  /FTCALConstantsLoader.mips_charge[0][0][ICOMPONENT-1]/1000.);
		this.set_Time(((double) this._TDC)/FTCALConstantsLoader.TIMECONVFAC
										  -(FTCALConstantsLoader.CRYS_LENGTH-FTCALConstantsLoader.depth_z)/FTCALConstantsLoader.VEFF
										  -FTCALConstantsLoader.time_offset[0][0][ICOMPONENT-1]); 
//		if(this.get_Edep()>0.1) System.out.println(ICOMPONENT + " " + this._TDC + " " + 
//				FTCALConstantsLoader.TIMECONVFAC + " " + FTCALConstantsLoader.time_offset[0][0][ICOMPONENT-1] + " " +
//				this.get_Time());
		this.set_Dx( (this._IDX-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_Dy( (this._IDY-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_DGTZIndex(i);
		this.set_ClusIndex(0);
	}


	private int _COMPONENT;		         	//	   Component number
	private int _IDX;    	 				//	   Crystal ID: X
	private int _IDY;    	 				//	   Crystal ID: Y
	private int _ADC;    	 				//	   ADC
	private int _TDC;    	 				//	   TDC 
		
	private double _Edep;      				//	   Reconstructed energy deposited by the hit in the crystal 
	private double _Time;      				//	   Reconstructed time, for now it is the gemc time
	private double _Dx;
	private double _Dy;
	private int    _DGTZIndex;				//		Pointer to cluster
	private int    _ClusIndex;				//		Pointer to cluster
	

	public int get_COMPONENT() {
		return _COMPONENT;
	}



	public void set_COMPONENT(int COMPONENT) {
		this._COMPONENT = COMPONENT;
	}


	public int get_IDX() {
		return _IDX;
	}



	public void set_IDX(int IDX) {
		this._IDX = IDX;
	}



	public int get_IDY() {
		return _IDY;
	}



	public void set_IDY(int IDY) {
		this._IDY = IDY;
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


	public int get_DGTZIndex() {
		return _DGTZIndex;
	}


	public void set_DGTZIndex(int _DGTZIndex) {
		this._DGTZIndex = _DGTZIndex;
	}
	
	
	public int get_ClusIndex() {
		return _ClusIndex;
	}


	public void set_ClusIndex(int _ClusIndex) {
		this._ClusIndex = _ClusIndex;
	}
	
	
	public static  ArrayList<FTCALHit> getRawHits(EvioDataBank bank) {

	     int[] isector     = bank.getInt("sector");
	     int[] ilayer      = bank.getInt("layer");
	     int[] icomponent  = bank.getInt("component");
	     int[] adc = bank.getInt("ADC");
	     int[] tdc = bank.getInt("TDC");
	     
	     int size = icomponent.length;
	      ArrayList<FTCALHit> hits = new ArrayList<FTCALHit>();
	      
	      for(int i = 0; i<size; i++){
	  
	    	  FTCALHit hit = new FTCALHit(i,icomponent[i], adc[i], tdc[i]);
	    	  
	          if(adc[i]!=-1 && tdc[i]!=-1){
	             hits.add(hit); 
	          }
	          
	      }
	      return hits;
	      
	}
	
	
	
	public static boolean passHitSelection(FTCALHit hit) {
		// a selection cut to pass the hit. 
		if(hit.get_Edep() > FTCALConstantsLoader.EN_THRES) {
			return true;
		} else {
			return false;
		}		
	}

	public int compareTo(FTCALHit arg0) {
		if(this.get_Edep()<arg0.get_Edep()) {
			return 1;
		} else {
			return -1;
		}
	}
		
}
