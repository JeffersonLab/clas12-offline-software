package org.jlab.rec.ft.cal;

import org.jlab.utils.groups.IndexedTable;



public class FTCALHit implements Comparable<FTCALHit>{
	// class implements Comparable interface to allow for sorting a collection of hits by Edep values
	
	// constructor 
	public FTCALHit(int i, int ICOMPONENT, int ADC, int TDC, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable timeWalk, IndexedTable cluster) {
		this._COMPONENT = ICOMPONENT;
		this._IDY = ((int) ICOMPONENT/22) + 1;
		this._IDX = ICOMPONENT + 1 - (this._IDY-1)*22;
		this._ADC = ADC;
		this._TDC = TDC;
		
                this._Charge = ((double) this._ADC)*charge2Energy.getDoubleValue("fadc_to_charge", 1,1,ICOMPONENT);
		this.set_Edep(this._Charge*charge2Energy.getDoubleValue("mips_energy", 1,1,ICOMPONENT)
				          /charge2Energy.getDoubleValue("mips_charge", 1,1,ICOMPONENT)/1000.);
                double twCorr=0;
                if(this._Charge>0) {
                    twCorr = timeWalk.getDoubleValue("amplitude", 1,1,ICOMPONENT)*Math.exp(-this._Charge*timeWalk.getDoubleValue("lambda", 1,1,ICOMPONENT));
                }
		this.set_Time(((double) this._TDC)/FTCALConstantsLoader.TIMECONVFAC
                                                 -(FTCALConstantsLoader.CRYS_LENGTH-cluster.getDoubleValue("depth_z", 1,1,0))/FTCALConstantsLoader.VEFF
						 -timeOffsets.getDoubleValue("time_offset", 1,1,ICOMPONENT)-twCorr);
//		if(this.get_Edep()>0.1) System.out.println(ICOMPONENT + " " + this._TDC + " " + 
//				FTCALConstantsLoader.TIMECONVFAC + " " + FTCALConstantsLoader.time_offset[0][0][ICOMPONENT-1] + " " +
//				this.get_Time());
		this.set_Dx( (this._IDX-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_Dy( (this._IDY-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_Dz(FTCALConstantsLoader.CRYS_ZPOS+cluster.getDoubleValue("depth_z", 1,1,0));
		this.set_DGTZIndex(i);
		this.set_ClusID(0);
	}

	public FTCALHit(int i, int ICOMPONENT, int ADC, float time, IndexedTable charge2Energy, IndexedTable timeOffsets, IndexedTable timeWalk, IndexedTable cluster) {
		this._COMPONENT = ICOMPONENT;
		this._IDY = ((int) ICOMPONENT/22) + 1;
		this._IDX = ICOMPONENT + 1 - (this._IDY-1)*22;
		this._ADC = ADC;
		
                this._Charge = ((double) this._ADC)*charge2Energy.getDoubleValue("fadc_to_charge", 1,1,ICOMPONENT);
		this.set_Edep(this._Charge*charge2Energy.getDoubleValue("mips_energy", 1,1,ICOMPONENT)
				          /charge2Energy.getDoubleValue("mips_charge", 1,1,ICOMPONENT)/1000.);
                
                double twCorr=0;
                if(this._Charge>0) {
                    twCorr = timeWalk.getDoubleValue("amplitude", 1,1,ICOMPONENT)*Math.exp(-this._Charge*timeWalk.getDoubleValue("lambda", 1,1,ICOMPONENT));
                }
		
                this.set_Time(time -(FTCALConstantsLoader.CRYS_LENGTH-cluster.getDoubleValue("depth_z", 1,1,0))/FTCALConstantsLoader.VEFF
				   -timeOffsets.getDoubleValue("time_offset", 1,1,ICOMPONENT)-twCorr); 
//		if(this.get_Edep()>0.1) System.out.println(ICOMPONENT + " " + this._TDC + " " + 
//				FTCALConstantsLoader.TIMECONVFAC + " " + FTCALConstantsLoader.time_offset[0][0][ICOMPONENT-1] + " " +
//				this.get_Time());
		this.set_Dx( (this._IDX-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_Dy( (this._IDY-FTCALConstantsLoader.CRYS_DELTA )* FTCALConstantsLoader.CRYS_WIDTH);
		this.set_Dz(FTCALConstantsLoader.CRYS_ZPOS+cluster.getDoubleValue("depth_z", 1,1,0));
		this.set_DGTZIndex(i);
		this.set_ClusID(0);
	}

	private int _COMPONENT;		         	//	   Component number
	private int _IDX;    	 				//	   Crystal ID: X
	private int _IDY;    	 				//	   Crystal ID: Y
	private int _ADC;    	 				//	   ADC
	private int _TDC;    	 				//	   TDC 
		
	private double _Charge;      				//	   Reconstructed energy deposited by the hit in the crystal 
	private double _Edep;      				//	   Reconstructed energy deposited by the hit in the crystal 
	private double _Time;      				//	   Reconstructed time, for now it is the gemc time
	private double _Dx;
	private double _Dy;
	private double _Dz;
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
	
	
	public int get_ClusID() {
		return _ClusIndex;
	}


	public void set_ClusID(int _ClusIndex) {
		this._ClusIndex = _ClusIndex;
	}
	
	public static boolean passHitSelection(FTCALHit hit, IndexedTable thresholds) {
		// a selection cut to pass the hit. 
		if(hit.get_Edep() > thresholds.getDoubleValue("thresholdHit", 1,1,hit.get_COMPONENT())) {
			return true;
		} else {
			return false;
		}		
	}

        @Override
	public int compareTo(FTCALHit arg0) {
		if(this.get_Edep()<arg0.get_Edep()) {
			return 1;
		} else {
			return -1;
		}
	}
        
        public void show() {
            System.out.println(+ this.get_COMPONENT() + "\t" 
                        + this.get_IDX()       + "\t " 
                        + this.get_IDY()       + "\t"
                        + this.get_Edep()      + "\t"
                        + this.get_Time()      + "\t"
                        + this.get_DGTZIndex() + "\t"
                        + this.get_ClusID());
        }
		
}
