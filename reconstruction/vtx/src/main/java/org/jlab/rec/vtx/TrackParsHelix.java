package org.jlab.rec.vtx;



import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class TrackParsHelix extends Helix {

    /**
     * @return the _id1
     */
    public int getId() {
        return _id;
    }

    /**
     * @param _id1 the _id1 to set
     */
    public void setId(int _id1) {
        this._id = _id1;
    }


    public TrackParsHelix(int i1, double x0, double y0, double z0,
                                       double px0, double py0, double pz0, 
                                       int q, double Bf, double xb, double yb) {
        _id = i1;
        this.setHelixParams(x0, y0, z0, px0, py0, pz0, q, Bf, xb, yb);
    }

    private int _id;               // id of the track
    private Helix _helix;          // track helix
    private double _phi_dca;       // azimuth at the DOCA
    private double _q;
    private double _pt;
    private double _cosphidca ;
    private double _sinphidca ;

    private double _x0;  // the reference point = the beam spot as default; test secondary vtx using these coords
    private double _y0;  // 
    private double _z0;  // 

    public void setHelixParams(double x0, double y0, double z0,
                                       double px0, double py0, double pz0, 
                                       int q, double Bf, double xb, double yb) { 

        this._helix = new Helix(x0,y0,z0,px0,py0,pz0,q,Bf,
                            xb,yb,Units.CM);
        
        this._x0 = this._helix.getXb();
        this._y0 = this._helix.getYb();
        this._z0 = 0;
        this._pt = Math.sqrt(px0*px0+py0*py0);

        double xC = (1. / this._helix.getOmega()- this._helix.getD0()) * Math.sin(Math.atan2(py0, px0));
        double yC = (-1. / this._helix.getOmega() + this._helix.getD0()) * Math.cos(Math.atan2(py0, px0));
 
        this._phi_dca = Math.atan2(yC, xC);
        if (-q < 0) {
            this._phi_dca = Math.atan2(-yC, -xC);
        }
        _cosphidca = Math.cos(this._phi_dca);
        _sinphidca = Math.sin(this._phi_dca);

    } // end setHelixParams()


    // calculate coordinates of the point of the helix curve from the parameter phi.
    // phi=0 corresponds to the ref. point given by the track
    public Point3D calcPoint(double phi) {

        double x = _x0 + this._helix.getD0()*_cosphidca + (1./this._helix.getOmega())*(_cosphidca - Math.cos(_phi_dca+phi));
        double y = _y0 + this._helix.getD0()*_sinphidca + (1./this._helix.getOmega())*(_sinphidca - Math.sin(_phi_dca+phi));
        double z = _z0 + this._helix.getZ0()            - (1./this._helix.getOmega())*this._helix.getTanL()*phi;

        return new Point3D(x,y,z);

    }
    public Vector3D calcDir(double phi) {
        double px = -this._pt * Math.sin(this._phi_dca + phi);
        double py = this._pt * Math.cos(this._phi_dca + phi);
        double pz = this._pt * this._helix.getTanL();

        return new Vector3D(px,py,pz).asUnit();
    }


}
