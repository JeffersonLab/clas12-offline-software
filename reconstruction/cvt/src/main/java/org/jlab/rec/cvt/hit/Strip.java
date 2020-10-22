package org.jlab.rec.cvt.hit;

import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.bmt.Constants;

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

    private Point3D _ImplantPoint;						// 	   the end-point of the strip at implant (lab frame)
    private Point3D _MidPoint;							//	   the mid-point of the strip (lab frame)
    private Point3D _EndPoint;							//	   the end-point of the strip (lab frame)
    private Vector3D _StripDir;							// 	   unit direction vector along the strip (lab frame)

    public int get_Strip() {
        return _Strip;
    }

    public void set_Strip(int _Strip) {
        this._Strip = _Strip;
    }

    public Point3D get_ImplantPoint() {
        return _ImplantPoint;
    }

    public void set_ImplantPoint(Point3D _ImplantPoint) {
        this._ImplantPoint = _ImplantPoint;
    }

    public Point3D get_MidPoint() {
        return _MidPoint;
    }

    public void set_MidPoint(Point3D _MidPoint) {
        this._MidPoint = _MidPoint;
    }

    public Point3D get_EndPoint() {
        return _EndPoint;
    }

    public void set_EndPoint(Point3D _EndPoint) {
        this._EndPoint = _EndPoint;
    }
    
    public Vector3D get_StripDir() {
        return _StripDir;
    }

    public void set_StripDir(Vector3D _StripDir) {
        this._StripDir = _StripDir;
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
     * @param geo the BMT geometry class Sets the Lorentz corrected phi and
     * strip number for Z detectors, the z position for C detectors
     */
    public void calc_BMTStripParams(org.jlab.rec.cvt.bmt.Geometry geo, int sector, int layer) {

        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 0) { // C-dtectors
            // set z
            double z = geo.CRCStrip_GetZ(layer, this.get_Strip());
            this.set_Z(z);
            // max z err
            this.set_ZErr(geo.CRCStrip_GetPitch(layer, this.get_Strip()) / Math.sqrt(12.));
        }

        if (org.jlab.rec.cvt.bmt.Geometry.getZorC(layer) == 1) { // Z-detectors
            geo.SetLorentzAngle(layer, sector);
            double theMeasuredPhi = geo.CRZStrip_GetPhi(sector, layer, this.get_Strip());
            double theLorentzCorrectedAngle = geo.LorentzAngleCorr(theMeasuredPhi, layer);
            // set the phi 
            this.set_Phi(theLorentzCorrectedAngle);
            this.set_Phi0(theMeasuredPhi); // uncorrected
            //System.out.println(" sec "+sector+" strip "+this.get_Strip()+" LC strip "+geo.getZStrip(layer, theLorentzCorrectedAngle));
            int theLorentzCorrectedStrip = geo.getZStrip(layer, theLorentzCorrectedAngle);
            // get the strip number after correcting for Lorentz angle
            this.set_LCStrip(theLorentzCorrectedStrip);

            double sigma = org.jlab.rec.cvt.bmt.Constants.SigmaDrift / Math.cos(org.jlab.rec.cvt.bmt.Constants.getThetaL()); // max sigma for drift distance  (hDrift) = total gap from top to mesh

            int num_region = (int) (layer + 1) / 2 - 1; // region index (0...2) 0=layers 1&2, 1=layers 3&4, 2=layers 5&6double Z0=0;
            //max phi err
            double phiErrL = sigma / org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region];

            double phiErr = org.jlab.rec.cvt.bmt.Constants.getCRZWIDTH()[num_region] / org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region] / Math.sqrt(12.);
            this.set_PhiErr(Math.sqrt(phiErr * phiErr + phiErrL * phiErrL));
            //System.out.println("arcerr "+org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]+" * "+Math.toDegrees(sigma/org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]));
            this.set_PhiErr0(phiErr);
        }

    }

}
