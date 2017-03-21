package org.jlab.rec.tof.banks;

public class BaseHit implements Comparable<BaseHit>, IBaseHit{

	private int _Id;
	private int _Sector;
	private int _Layer;
	private int _Component;
	public int ADC1 = -1;
	public int ADC2 = -1 ;
	public int TDC1 = -1;
	public int TDC2 = -1;
	public double ADCTime1 = -1;
	public int ADCpedestal1= -1;
	public double ADCTime2 = -1;
	public int ADCpedestal2= -1;
	
	public int ADCbankHitIdx1 = -1;
	public int ADCbankHitIdx2 = -1;
	public int TDCbankHitIdx1 = -1;
	public int TDCbankHitIdx2 = -1;
	
	public BaseHit(int sector, int layer, int component) {
		_Sector		=	 sector;
		_Layer		=	 layer; 
		_Component	=	 component; 
	}
	
	@Override
    public int hashCode() {
		int hc = this._Sector*10000 + this._Layer*1000 + this._Component;
		return hc;
    }
	@Override
	public boolean equals(Object obj){
        if (obj instanceof BaseHit) {
        	BaseHit hit = (BaseHit) obj;
            return (hit._Sector == this._Sector && hit._Layer == this._Layer && hit._Component == this._Component 
            		&& hit.ADC1 == this.ADC1 && hit.ADC2 == this.ADC2 && hit.TDC1 == this.TDC1 && hit.TDC2 == this.TDC2
            		&& hit.ADCTime1 == this.ADCTime1 && hit.ADCpedestal1 == this.ADCpedestal1
            		&& hit.ADCTime2 == this.ADCTime2 && hit.ADCpedestal2 == this.ADCpedestal2);
        } else {
            return false;
        }
    }
	@Override
	public int compareTo(BaseHit arg) {
		//int return_val = 0 ;
		int CompSec = this._Sector < arg._Sector  ? -1 : this._Sector  == arg._Sector  ? 0 : 1;
		int CompLay = this._Layer  < arg._Layer  ? -1 : this._Layer   == arg._Layer   ? 0 : 1;
		int CompId = this._Component < arg._Component  ? -1 : this._Component  == arg._Component  ? 0 : 1;
		
		int adc_hit =-1;
		int adc_arg =-1;
		
		if(this.ADC1!=-1)
			adc_hit = this.ADC1;
		if(this.ADC2!=-1)
			adc_hit = this.ADC2;
		if(arg.ADC1!=-1)
			adc_arg = arg.ADC1;
		if(arg.ADC2!=-1)
			adc_arg = arg.ADC2;
		
		int tdc_hit =-1;
		int tdc_arg =-1;
		
		if(this.TDC1!=-1)
			tdc_hit = this.TDC1;
		if(this.TDC2!=-1)
			tdc_hit = this.TDC2;
		if(arg.TDC1!=-1)
			tdc_arg = arg.TDC1;
		if(this.TDC2!=-1)
			tdc_arg = arg.TDC2;
		
		int CompADC = adc_hit < adc_arg  ? -1 : adc_hit  == adc_arg  ? 0 : 1;
		int CompTDC = tdc_hit < tdc_arg  ? -1 : tdc_hit  == tdc_arg  ? 0 : 1;
		
		int return_val1 = ((CompLay ==0) ? CompId : CompLay); 
		int return_val2 = ((CompSec ==0) ? return_val1 : CompSec);
		
		int return_val3 = ((CompADC ==0) ? return_val2 : CompADC);
		int return_val4 = ((CompTDC ==0) ? return_val3 : CompTDC);
		
		return return_val4;
		
		/*
		int CompADC1 = this.ADC1 < arg.ADC1  ? -1 : this.ADC1  == arg.ADC1  ? 0 : 1;
		int CompADC2 = this.ADC2 < arg.ADC2  ? -1 : this.ADC2  == arg.ADC2  ? 0 : 1;
		int CompTDC1 = this.TDC1 < arg.TDC1  ? -1 : this.TDC1  == arg.TDC1  ? 0 : 1;
		int CompTDC2 = this.TDC2 < arg.TDC2  ? -1 : this.TDC2  == arg.TDC2  ? 0 : 1;
		
		int return_val1 = ((CompLay ==0) ? CompId : CompLay); 
		int return_val2 = ((CompSec ==0) ? return_val1 : CompSec);
		
		int return_val3 = ((CompADC1 ==0) ? return_val2 : CompADC1);
		int return_val4 = ((CompADC2 ==0) ? return_val3 : CompADC2);
		int return_val5 = ((CompTDC1 ==0) ? return_val4 : CompTDC1);
		int return_val6 = ((CompTDC2 ==0) ? return_val5 : CompTDC2);
		
		return return_val6; */
		
	}

	

	@Override
	public int get_Sector() {
		return this._Sector;
	}

	@Override
	public int get_Layer() {
		return this._Layer;
	}

	@Override
	public int get_Component() {
		return this._Component;
	}

	@Override
	public int get_ADC1() {
		return this.ADC1;
	}

	@Override
	public int get_ADC2() {
		return this.ADC2;
	}

	@Override
	public int get_TDC1() {
		return this.TDC1;
	}

	@Override
	public int get_TDC2() {
		return this.TDC2;
	}

	@Override
	public double get_ADCTime1() {
		return this.ADCTime1;
	}

	@Override
	public int get_ADCpedestal1() {
		return this.ADCpedestal1;
	}

	@Override
	public double get_ADCTime2() {
		return this.ADCTime2;
	}

	@Override
	public int get_ADCpedestal2() {
		return this.ADCpedestal2;
	}
	public int get_Id() {
		return _Id;
	}

	public void set_Id(int _Id) {
		this._Id = _Id;
	}


	
}
