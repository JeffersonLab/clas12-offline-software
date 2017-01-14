package org.jlab.rec.tof.banks;

public class BaseHit implements Comparable<BaseHit>, IBaseHit{

	private int _Sector;
	private int _Layer;
	private int _Component;
	private int _Id;
	public int ADC1 = -1;
	public int ADC2 = -1 ;
	public int TDC1 = -1;
	public int TDC2 = -1;
	public double ADCTime1 = -1;
	public int ADCpedestal1= -1;
	public double ADCTime2 = -1;
	public int ADCpedestal2= -1;
	
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
		int return_val = 0 ;
		int CompSec = this._Sector < arg._Sector  ? -1 : this._Sector  == arg._Sector  ? 0 : 1;
		int CompLay = this._Layer  < arg._Layer  ? -1 : this._Layer   == arg._Layer   ? 0 : 1;
		int CompId = this._Component < arg._Component  ? -1 : this._Component  == arg._Component  ? 0 : 1;
		
		int return_val1 = ((CompLay ==0) ? CompId : CompLay); 
		return_val = ((CompSec ==0) ? return_val1 : CompSec);
		
		return return_val;
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
