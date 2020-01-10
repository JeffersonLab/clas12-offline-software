package org.jlab.rec.rtpc.hit;

/**
 * A  hit characterized by Time, cellID, and Edep.  The ADC to Time conversion has been done.
 * @author payette
 */


public class Hit implements Comparable<Hit>{
	// class implements Comparable interface to allow for sorting a collection of hits by wire number values
	
	
	// constructors 

	public Hit(int hitID, int cellID, double ADC, double Time) {
		this._Id = hitID;
		this._cellID = cellID;
		this._Time = Time;
		this._ADC = ADC;	
	}
	
	/*public Hit(int hitID, int cellID, double Time, double ADC) {
		this._Id = hitID;
		this._cellID = cellID;
		this._Time = Time;
		this._ADC = 1;	
	}*/
	
	private int 	_Id;									
	private int 	_cellID;      							
	private double 	_Time;    	 								 							
	private double 	_ADC;      								
	private int 	_Step;
	private double 	_Edep;
	private double 	_phiRad;
	private double 	_PosX;
	private double 	_PosY;
	private double 	_PosZ;
	private double 	_EdepTrue;
	private double 	_PosXTrue;
	private double 	_PosYTrue;
	private double 	_PosZTrue;
	private double  _TShift;
		
	public int get_Id() {
		return _Id;
	}

	public void set_Id(int _Id) {
		this._Id = _Id;
	}

	public int get_cellID() {
		return _cellID;
	}

	public void set_cellID(int _cellID) {
		this._cellID = _cellID;
	}

	public double get_Time() {
		return _Time;
	}

	public void set_Time(double _Time) {
		this._Time = _Time;
	}

	public double get_ADC() {
		return _ADC;
	}

	public void set_ADC(double _ADC) {
		this._ADC = _ADC;
	}

	public int get_Step() {
		return _Step;
	}

	public void set_Step(int _Step) {
		this._Step = _Step;
	}

	public double get_Edep() {
		return _Edep;
	}

	public void set_Edep(double _Edep) {
		this._Edep = _Edep;
	}

	public double get_phiRad() {
		return _phiRad;
	}

	public void set_phiRad(double _phiRad) {
		this._phiRad = _phiRad;
	}

	public double get_PosX() {
		return _PosX;
	}

	public void set_PosX(double _PosX) {
		this._PosX = _PosX;
	}

	public double get_PosY() {
		return _PosY;
	}

	public void set_PosY(double _PosY) {
		this._PosY = _PosY;
	}

	public double get_PosZ() {
		return _PosZ;
	}

	public void set_PosZ(double _PosZ) {
		this._PosZ = _PosZ;
	}

	public double get_EdepTrue() {
		return _EdepTrue;
	}

	public void set_EdepTrue(double _EdepTrue) {
		this._EdepTrue = _EdepTrue;
	}

	public double get_PosXTrue() {
		return _PosXTrue;
	}

	public void set_PosXTrue(double _PosXTrue) {
		this._PosXTrue = _PosXTrue;
	}

	public double get_PosYTrue() {
		return _PosYTrue;
	}

	public void set_PosYTrue(double _PosYTrue) {
		this._PosYTrue = _PosYTrue;
	}

	public double get_PosZTrue() {
		return _PosZTrue;
	}

	public void set_PosZTrue(double _PosZTrue) {
		this._PosZTrue = _PosZTrue;
	}

	public double get_TShift() {
		return _TShift;
	}
	
	public void set_TShift(double _TShift) {
		this._TShift = _TShift;
	}
	/**
	 * 
	 * @return print statement with hit information
	 */
	public String printInfo() {
		String s = "RTPC Hit: ID "+this.get_Id()+" cellID "+this.get_cellID()+" ADC "+this.get_ADC()+" Edep "+this.get_Edep()+" Time "+this.get_Time();
		return s;
	}

	/**
	 * 
	 * @param otherHit
	 * @return a boolean comparing 2 hits based on basic descriptors; 
	 * returns true if the hits are the same
	 */
	public boolean isSameAs(Hit otherHit) {
		Hit thisHit = (Hit) this;
		boolean cmp = false;
		if(thisHit.get_Edep() == otherHit.get_Edep() 
				&& thisHit.get_cellID() == otherHit.get_cellID()
							&& thisHit.get_Time() == otherHit.get_Time()
									&& thisHit.get_ADC() == otherHit.get_ADC()
																				)
			cmp = true;
		return cmp;
	}

	@Override
	public int compareTo(Hit arg0) {
		if(this._cellID>arg0._cellID) {
			return 1;
		} else {
			return 0;
		}
	}
	

}
