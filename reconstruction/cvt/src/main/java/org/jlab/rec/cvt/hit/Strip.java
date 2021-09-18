package org.jlab.rec.cvt.hit;

import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.Constants;

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
    
    private Arc3D _Arc;
    private Line3D _Line;
    private Line3D _Axis;
    private Transformation3D toLocal;
    private Transformation3D toGlobal;
    
    public int get_Strip() {
        return _Strip;
    }

    public void set_Strip(int _Strip) {
        this._Strip = _Strip;
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

    public Line3D get_Axis() {
        return _Axis;
    }

    public void set_Axis(Line3D _Axis) {
        this._Axis = _Axis;
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
        
        if (BMTGeometry.getDetectorType(layer) == BMTType.C) { // C-detectors
            // set z
            //double z = geo.CRCStrip_GetZ(layer, this.get_Strip());
            Arc3D arcLine = geo.getCstrip(region, sector, this.get_Strip());
            this.set_Arc(arcLine);
            this.set_Axis(geo.getAxis(layer, sector));
            // max z err
            this.set_Z(geo.getCstripZ(geo.getRegion(layer),this.get_Strip()));
            this.set_ZErr(geo.getPitch(layer, this.get_Strip()) / Math.sqrt(12.));

        }

        if (BMTGeometry.getDetectorType(layer) == BMTType.Z) { // Z-detectors
            //Line3D L0 = geo.getZstrip(geo.getRegion(layer), sector, this.get_Strip());
            Line3D line = geo.getLCZstrip(geo.getRegion(layer), sector, this.get_Strip(), swim);
            this.set_Line(line);
            this.set_Axis(geo.getAxis(layer, sector));
            
            Line3D cln = geo.getAxis(layer, sector);
            cln.set(cln.origin().x(), cln.origin().y(), line.origin().z(), 
                        cln.end().x(), cln.end().y(), line.end().z());
               
            double v = (line.origin().z()-cln.origin().z())/cln.direction().z();
            double x = cln.origin().x()+v*cln.direction().x();
            double y = cln.origin().y()+v*cln.direction().y();
            Vector3D n = new Point3D(x, y, line.origin().z()).
                    vectorTo(new Point3D(line.origin().x(),line.origin().y(),line.origin().z())).asUnit();

            double theMeasuredPhi = geo.getZstripPhi(geo.getRegion(layer), sector, this.get_Strip());
            //double theLorentzCorrectedAngle = line.midpoint().toVector3D().phi(); 
            double theLorentzCorrectedAngle = n.phi(); 
            // set the phi 
            this.set_Phi(theLorentzCorrectedAngle);
            this.set_Phi0(theMeasuredPhi); // uncorrected
            //System.out.println(" sec "+sector+" strip "+this.get_Strip()+" LC strip "+geo.getZStrip(layer, theLorentzCorrectedAngle));
            //int theLorentzCorrectedStrip = geo.getZStrip(layer, theLorentzCorrectedAngle);
            //            double xl = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]*
//                    Math.cos(theLorentzCorrectedAngle);
//            double yl = org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]*
//                    Math.sin(theLorentzCorrectedAngle);
//            int theLorentzCorrectedStrip = geo.getStrip( layer,  sector, 
//                    new Point3D(xl,yl,0));
            int theLorentzCorrectedStrip = geo.getStrip(layer,  sector, line.midpoint());
            // get the strip number after correcting for Lorentz angle
            this.set_LCStrip(theLorentzCorrectedStrip);
            
            double sigma = Constants.SigmaDrift / Math.cos(geo.getThetaLorentz(layer, sector)); // max sigma for drift distance  (hDrift) = total gap from top to mesh

            //max phi err
            double phiErrL = sigma / geo.getRadius(layer);

            double phiErr = geo.getPitch(layer, this.get_Strip()) / geo.getRadius(layer) / Math.sqrt(12.);
            this.set_PhiErr(Math.sqrt(phiErr * phiErr + phiErrL * phiErrL));
            //System.out.println("arcerr "+org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]+" * "+Math.toDegrees(sigma/org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[num_region]));
            this.set_PhiErr0(phiErr);
            
            
        }

    }

}
