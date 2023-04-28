package org.jlab.rec.cvt.hit;

import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Arc3D;
import org.jlab.geom.prim.Cylindrical3D;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.cvt.Geometry;
import org.jlab.rec.cvt.bmt.BMTGeometry;
import org.jlab.rec.cvt.bmt.BMTType;
import org.jlab.rec.cvt.bmt.BMTConstants;

public class Strip {

    public Strip(int strip, double edep, double time) {
        this._Strip = strip;
        this._Edep = edep;
        this._Time = time;
        mcStat[0]=1;
        mcStat[1]=1;
    }
    

    private int _Strip;    	//     strip read from daq 
    private double _Edep;      	//     for simulation this corresponds to the energy deposited on the strip, in data it should be an ADC converted value
    private double _Time;
    private int _Status;        //     0=good, 1=bad edep, 2=bad tim, 3=dead, 4=inefficiency HV, 5=low efficient HV  Hv1 to HV2, 6=low efficient HV2 to HV3              
    
    private int _LCStrip;	//     strip number taking into account Lorentz angle correction (for MM Z detectors)
    private double _Phi;  	//     for MM Z-detectors, the azimuth angle at the strip midwidth after LC
    private double _PhiErr;
    private double _Phi0;  	//     for MM Z-detectors, the azimuth angle at the strip midwidth before LC
    private double _PhiErr0;
    private double _Z;
    private double _ZErr;
    
    private double _Pitch;
    private Arc3D  _Arc;
    private Line3D _Line;
    private Cylindrical3D _Tile;
    private Line3D _Module;
    private Vector3D _Normal;
    private Transformation3D toLocal;
    private Transformation3D toGlobal;
    
    private int[] mcStat = new int[2];
    private int tLevel =-1; //stage of tracking the hit successfully passed: 0=not in cluster; 
                                                                      // 1=in cluster, failed pattern recognition;
                                                                      // 2=passed pattern recognition, rejected by track fitting;
                                                                      // 3=passed  track fitting.
    
    public int getStrip() {
        return _Strip;
    }


    public double getSVTStripR1() {
        return this.getLine().origin().toVector3D().r();
    }
    
    public double getSVTStripTheta1() {
        return this.getLine().origin().toVector3D().theta();
    }

    public double getSVTStripPhi1() {
        return this.getLine().origin().toVector3D().phi();
    }

    public double getSVTStripR2() {
        return this.getLine().end().toVector3D().r();
    }
    
    public double getSVTStripTheta2() {
        return this.getLine().end().toVector3D().theta();
    }

    public double getSVTStripPhi2() {
        return this.getLine().end().toVector3D().phi();
    }

    public double getBMTStripR1() {
        if(this.getArc()!=null) {
            return this.getArc().origin().toVector3D().mag();
        } else {
            return this.getLine().origin().toVector3D().r();
        }
    }
    
    public double getBMTStripTheta1() {
        if(this.getArc()!=null) {
            return this.getArc().origin().toVector3D().theta();
        } else {
            return this.getLine().origin().toVector3D().theta();
        }
    }

    public double getBMTStripPhi1() {
        if(this.getArc()!=null) {
            return this.getArc().origin().toVector3D().phi();
        } else {
            return this.getLine().origin().toVector3D().phi();
        }
    }

    public double getBMTStripR2() {
        if(this.getArc()!=null) {
            return this.getArc().end().toVector3D().mag();
        } else {
            return this.getLine().end().toVector3D().r();
        }
    }
    
    public double getBMTStripTheta2() {
        if(this.getArc()!=null) {
            return this.getArc().end().toVector3D().theta();
        } else {
            return this.getLine().end().toVector3D().theta();
        }
    }

    public double getBMTStripPhi2() {
        if(this.getArc()!=null) {
            return this.getArc().end().toVector3D().phi();
        } else {
            return this.getLine().end().toVector3D().phi();
        }
    }
    
    /**
     * @return the mcStat
     */
    public int[] getMcStat() {
        return mcStat;
    }

    /**
     * @return the tLevel
     */
    public int gettLevel() {
        return tLevel;
    }

    /**
     * @param tLevel the tLevel to set
     */
    public void settLevel(int tLevel) {
        this.tLevel = tLevel;
    }

    /**
     * @param mcStat the mcStat to set: [0][1]=00, if wrong track, not on track; 01, if wrong track, hit-on-track; 10, if right track, not on track; 11, if right track, hit-on-track
     */
    public void setMcStat(int[] mcStat) {
        this.mcStat = mcStat;
    }

    
    public double getPitch() {
        return _Pitch;
    }

    public void setPitch(double _Pitch) {
        this._Pitch = _Pitch;
    }

    public Arc3D getArc() {
        return _Arc;
    }

    public void setArc(Arc3D _Arc) {
        this._Arc = _Arc;
    }

    public Line3D getLine() {
        return _Line;
    }

    public void setLine(Line3D _Line) {
        this._Line = _Line;
    }

    public Cylindrical3D getTile() {
        return _Tile;
    }

    public void setTile(Cylindrical3D _Tile) {
        this._Tile = _Tile;
    }

    public Line3D getModule() {
        return _Module;
    }

    public void setModule(Line3D _Module) {
        this._Module = _Module;
    }

    public Vector3D getNormal() {
        return _Normal;
    }

    public void setNormal(Vector3D _Normal) {
        this._Normal = _Normal;
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

    public int getLCStrip() {
        return _LCStrip;
    }

    public void setLCStrip(int _LCStrip) {
        this._LCStrip = _LCStrip;
    }

    public double getPhi() {
        return _Phi;
    }

    public void setPhi(double _Phi) {
        this._Phi = _Phi;
    }

    public double getPhiErr() {
        return _PhiErr;
    }

    public void setPhiErr(double _PhiErr) {
        this._PhiErr = _PhiErr;
    }

    public double getPhi0() {
        return _Phi0;
    }

    public void setPhi0(double _Phi0) {
        this._Phi0 = _Phi0;
    }

    public double getPhiErr0() {
        return _PhiErr0;
    }

    public void setPhiErr0(double _PhiErr0) {
        this._PhiErr0 = _PhiErr0;
    }

    public double getZ() {
        return _Z;
    }

    public void setZ(double _Z) {
        this._Z = _Z;
    }

    public double getZErr() {
        return _ZErr;
    }

    public void setZErr(double _ZErr) {
        this._ZErr = _ZErr;
    }

    public double getEdep() {
        return _Edep;
    }

    public void setEdep(double _Edep) {
        this._Edep = _Edep;
    }

    /**
     * @return the _Time
     */
    public double getTime() {
        return _Time;
    }

    /**
     * @param _Time the _Time to set
     */
    public void setTime(double _Time) {
        this._Time = _Time;
    }

    public int getStatus() {
        return _Status;
    }

    public void setStatus(int _Status) {
        this._Status = _Status;
    }

    
    /**
     * 
     *
     * @param sector
     * @param layer
     * @param swim
     */
    public void calcBMTStripParams(int sector, int layer, Swim swim) {
        BMTGeometry geo = Geometry.getInstance().getBMT();
        
        int region = geo.getRegion(layer); // region index (1...3) 1=layers 1&2, 2=layers 3&4, 3=layers 5&6
        this.setToGlobal(geo.toGlobal(layer, sector));
        this.setToLocal(geo.toLocal(layer, sector));
        this.setTile(geo.getTileSurface(layer, sector));
        this.setPitch(geo.getPitch(layer, this.getStrip()));
        
        if (BMTGeometry.getDetectorType(layer) == BMTType.C) { // C-detectors
            // set z
            //double z = geo.CRCStrip_GetZ(layer, this.getStrip());
            Arc3D arcLine = geo.getCstrip(region, sector, this.getStrip());
            this.setArc(arcLine);
            this.setNormal(arcLine.bisect());
            // max z err
            this.setZ(geo.getCstripZ(geo.getRegion(layer),this.getStrip()));
            this.setZErr(geo.getPitch(layer, this.getStrip()) / Math.sqrt(12.));

        }

        if (BMTGeometry.getDetectorType(layer) == BMTType.Z) { // Z-detectors
            Line3D line = geo.getLCZstrip(geo.getRegion(layer), sector, this.getStrip(), swim);
            this.setLine(line);
            this.setNormal(this.getTile().getAxis().distance(line.midpoint()).direction().asUnit());            
            // set the phi 
            Point3D local = geo.getIdealLCZstrip(region, sector, this.getStrip(), swim).midpoint();
            double theMeasuredPhi = geo.getZstripPhi(geo.getRegion(layer), sector, this.getStrip());
            double theLorentzCorrectedAngle = local.toVector3D().phi();
            this.setPhi(theLorentzCorrectedAngle);
            this.setPhi0(theMeasuredPhi); // uncorrected, can be outside of -pi,pi

            // get the strip number after correcting for Lorentz angle
            int theLorentzCorrectedStrip = geo.getStrip(layer,  sector, line.midpoint());
            this.setLCStrip(theLorentzCorrectedStrip);

            double sigma = BMTConstants.SIGMADRIFT / Math.cos(geo.getThetaLorentz(layer, sector, line.midpoint(), swim)); // max sigma for drift distance  (HDRIFT) = total gap from top to mesh

            //max phi err
            double phiErrL = sigma / geo.getRadius(layer);

            double phiErr = geo.getPitch(layer, this.getStrip()) / geo.getRadius(layer) / Math.sqrt(12.);
            this.setPhiErr(Math.sqrt(phiErr * phiErr + phiErrL * phiErrL));
            this.setPhiErr0(phiErr);
            
            
        }

    }

}
