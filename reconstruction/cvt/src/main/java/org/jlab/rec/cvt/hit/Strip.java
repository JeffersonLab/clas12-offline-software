package org.jlab.rec.cvt.hit;

public class Strip {

	public Strip(int strip, double edep) {
		this._Strip = strip;
		this._Edep = edep;
	}

	private int _Strip;    	 							//	   strip read from daq 
	private int _LCStrip;								//     strip number taking into account Lorentz angle correction (for MM Z detectors)
	private double _Phi;  								//     for MM Z-detectors, the azimuth angle at the strip midwidth after LC
	private double _PhiErr;
	private double _Phi0;  								//     for MM Z-detectors, the azimuth angle at the strip midwidth before LC
	private double _PhiErr0;
	
	private double _Z;    								//     for MM C-detectors. the z position at the strip midwidth
	private double _ZErr;  
	private double _Edep;      							//     for simulation this corresponds to the energy deposited on the strip, in data it should be an ADC converted value
	
	
	public int get_Strip() {
		return _Strip;
	}
	public void set_Strip(int _Strip) {
		this._Strip = _Strip;
	}
	public int get_LCStrip() {
		return _LCStrip;
	}
	public void set_LCStrip(int _LCStrip) {
		this._LCStrip = _LCStrip;
	}
	public double get_Phi() {
		return _Phi;
	}
	public void set_Phi(double _Phi) {
		this._Phi = _Phi;
	}
	public double get_PhiErr() {
		return _PhiErr;
	}
	public void set_PhiErr(double _PhiErr) {
		this._PhiErr = _PhiErr;
	}
	public double get_Phi0() {
		return _Phi0;
	}
	public void set_Phi0(double _Phi0) {
		this._Phi0 = _Phi0;
	}
	public double get_PhiErr0() {
		return _PhiErr0;
	}
	public void set_PhiErr0(double _PhiErr0) {
		this._PhiErr0 = _PhiErr0;
	}
	public double get_Z() {
		return _Z;
	}
	public void set_Z(double _Z) {
		this._Z = _Z;
	}
	public double get_ZErr() {
		return _ZErr;
	}
	public void set_ZErr(double _ZErr) {
		this._ZErr = _ZErr;
	}
	public double get_Edep() {
		return _Edep;
	}
	public void set_Edep(double _Edep) {
		this._Edep = _Edep;
	}

	/**
	 * 
	 * @param geo the BMT geometry class
	 * Sets the Lorentz corrected phi and strip number for Z detectors, the z position for C detectors
	 */
	public void calc_BMTStripParams(org.jlab.rec.cvt.bmt.Geometry geo, int sector, int layer) {
				
		if(layer%2==0) { // C-dtectors
			// set z
			double z = geo.CRCStrip_GetZ(layer, this.get_Strip());
			this.set_Z(z);
			// max z err
			this.set_ZErr(org.jlab.rec.cvt.bmt.Constants.SigmaMax);
		}
		if(layer%2==1) { // Z-detectors
			double theMeasuredPhi = geo.CRZStrip_GetPhi(sector, layer, this.get_Strip());
			double theLorentzCorrectedAngle = geo.LorentzAngleCorr( theMeasuredPhi, layer);
			// set the phi 
			this.set_Phi(theLorentzCorrectedAngle); 
			this.set_Phi0(theMeasuredPhi); // uncorrected
			int theLorentzCorrectedStrip = geo.getZStrip(layer, theLorentzCorrectedAngle);
			// get the strip number after correcting for Lorentz angle
			this.set_LCStrip(theLorentzCorrectedStrip);
			
			double sigma = org.jlab.rec.cvt.bmt.Constants.SigmaMax/Math.sqrt(Math.cos(org.jlab.rec.cvt.bmt.Constants.ThetaL)); // max sigma for drift distance  (hDrift) = total gap from top to mesh
			
			int num_region = (int) (layer+1)/2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6double Z0=0;
			//max phi err
			double phiErr = (sigma/Math.cos(org.jlab.rec.cvt.bmt.Constants.ThetaL)
					-(org.jlab.rec.cvt.bmt.Constants.hDrift-org.jlab.rec.cvt.bmt.Constants.CRZRADIUS[num_region])*Math.tan(org.jlab.rec.cvt.bmt.Constants.ThetaL))/org.jlab.rec.cvt.bmt.Constants.CRZRADIUS[num_region];
			this.set_PhiErr(phiErr);
			this.set_PhiErr0(sigma);
		}
		
	}
	
	
}
