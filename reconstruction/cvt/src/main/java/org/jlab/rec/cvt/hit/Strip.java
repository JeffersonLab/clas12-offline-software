package org.jlab.rec.cvt.hit;

import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.BMTConstants;

public class Strip {

    public Strip(int strip, double edep, double time) {
        this._Strip = strip;
        this._Edep = edep;
        this._Time = time;
    }
    

    private int _Strip;    	 							//     strip read from daq 
    private double _Edep;      							//     for simulation this corresponds to the energy deposited on the strip, in data it should be an ADC converted value
    private double _Time;
    
    private int _LCStrip;								//     strip number taking into account Lorentz angle correction (for MM Z detectors)
    private double _Phi;  								//     for MM Z-detectors, the azimuth angle at the strip midwidth after LC
    private double _PhiErr;
    private double _Phi0;  								//     for MM Z-detectors, the azimuth angle at the strip midwidth before LC
    private double _PhiErr0;
    private double _Z;
    private double _ZErr;
    
    private double _Pitch;
    private Arc3D  _Arc;
    private Line3D _Line;
    private Cylindrical3D _Tile;
    private Line3D _Module;
    private Vector3D _Normal;
    private double _ToverX0;
    private Transformation3D toLocal;
    private Transformation3D toGlobal;
    
    public int get_Strip() {
        return _Strip;
    }

    public void set_Strip(int _Strip) {
        this._Strip = _Strip;
    }

    public double get_Pitch() {
        return _Pitch;
    }

    public void set_Pitch(double _Pitch) {
        this._Pitch = _Pitch;
    }

    public Arc3D get_Arc() {
        return _Arc;
    }

    public void set_Arc(Arc3D _Arc) {
        this._Arc = _Arc;
    }

    public Line3D get_Line() {
        return _Line;
    }

    public void set_Line(Line3D _Line) {
        this._Line = _Line;
    }

    public Cylindrical3D get_Tile() {
        return _Tile;
    }

    public void set_Tile(Cylindrical3D _Tile) {
        this._Tile = _Tile;
    }

    public Line3D get_Module() {
        return _Module;
    }

    public void set_Module(Line3D _Module) {
        this._Module = _Module;
    }

    public Vector3D get_Normal() {
        return _Normal;
    }

    public void set_Normal(Vector3D _Normal) {
        this._Normal = _Normal;
    }

    public double getToverX0() {
        return _ToverX0;
    }

    public void setToverX0(double _ToverX0) {
        this._ToverX0 = _ToverX0;
    }

    public Transformation3D toLocal() {
        return toLocal;
    }

    public void setToLocal(Transformation3D toLocal) {
        this.toLocal = toLocal;
    }

    public Transformation3D toGlobal() {
        return toGlobal;
    }

    public void setToGlobal(Transformation3D toGlobal) {
        this.toGlobal = toGlobal;
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
     * @return the _Time
     */
    public double get_Time() {
        return _Time;
    }

    /**
     * @param _Time the _Time to set
     */
    public void set_Time(double _Time) {
        this._Time = _Time;
    }

    /**
     *
     * @param geo the BMT geometry class Sets the Lorentz corrected phi and
     * strip number for Z detectors, the z position for C detectors
     */
    public void calc_BMTStripParams(BMTGeometry geo, int sector, int layer, Swim swim) {

        int region = geo.getRegion(layer); // region index (1...3) 1=layers 1&2, 2=layers 3&4, 3=layers 5&6
        this.setToGlobal(geo.toGlobal(layer, sector));
        this.setToLocal(geo.toLocal(layer, sector));
        this.set_Tile(geo.getTileSurface(layer, sector));
        this.set_Pitch(geo.getPitch(layer, this.get_Strip()));
        this.setToverX0(geo.getToverX0(layer));
        
        if (BMTGeometry.getDetectorType(layer) == BMTType.C) { // C-detectors
            // set z
            //double z = geo.CRCStrip_GetZ(layer, this.get_Strip());
            Arc3D arcLine = geo.getCstrip(region, sector, this.get_Strip());
            this.set_Arc(arcLine);
            this.set_Normal(arcLine.bisect());
            // max z err
            this.set_Z(geo.getCstripZ(geo.getRegion(layer),this.get_Strip()));
            this.set_ZErr(geo.getPitch(layer, this.get_Strip()) / Math.sqrt(12.));

        }

        if (BMTGeometry.getDetectorType(layer) == BMTType.Z) { // Z-detectors
            Line3D line = geo.getLCZstrip(geo.getRegion(layer), sector, this.get_Strip(), swim);
            this.set_Line(line);
            this.set_Normal(this.get_Tile().getAxis().distance(line.midpoint()).direction().asUnit());            
            // set the phi 
            Point3D local = geo.getIdealLCZstrip(region, sector, this.get_Strip(), swim).midpoint();
            double theMeasuredPhi = geo.getZstripPhi(geo.getRegion(layer), sector, this.get_Strip());
            double theLorentzCorrectedAngle = local.toVector3D().phi();
            this.set_Phi(theLorentzCorrectedAngle);
            this.set_Phi0(theMeasuredPhi); // uncorrected, can be outside of -pi,pi

            // get the strip number after correcting for Lorentz angle
            int theLorentzCorrectedStrip = geo.getStrip(layer,  sector, line.midpoint());
            this.set_LCStrip(theLorentzCorrectedStrip);
            
            double sigma = BMTConstants.SigmaDrift / Math.cos(geo.getThetaLorentz(layer, sector)); // max sigma for drift distance  (hDrift) = total gap from top to mesh

            //max phi err
            double phiErrL = sigma / geo.getRadius(layer);

            double phiErr = geo.getPitch(layer, this.get_Strip()) / geo.getRadius(layer) / Math.sqrt(12.);
            this.set_PhiErr(Math.sqrt(phiErr * phiErr + phiErrL * phiErrL));
            this.set_PhiErr0(phiErr);
            
            
        }

    }

}
