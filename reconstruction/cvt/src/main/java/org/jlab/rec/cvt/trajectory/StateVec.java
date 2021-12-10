package org.jlab.rec.cvt.trajectory;


import org.jlab.rec.cvt.Constants;

/**
 * A StateVec describes a cross measurement in the BST. It is characterized by a
 * point in the lab coordinate system at each module plane and by unit tangent
 * vectors to the track at the state-vec point.
 *
 * @author ziegler
 *
 */
public class StateVec implements Comparable<StateVec> {

    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1874984192960130771L;

    private int _ID;
    private int _SurfaceDetector;
    private int _SurfaceLayer;
    private int _SurfaceSector;
    private int _SurfaceComponent;
    private double _TrkPhiAtSurface;
    private double _TrkThetaAtSurface;
    private double _TrkToModuleAngle;
    private double _CalcCentroidStrip;
    private double _Path;
    private double _x;
    private double _y;
    private double _z;
    private double _ux;
    private double _uy;
    private double _uz;
    
    public int get_ID() {
        return _ID;
    }

    public void set_ID(int _ID) {
        this._ID = _ID;
    }

    public double get_Path() {
        return _Path;
    }

    public void set_Path(double _Path) {
        this._Path = _Path;
    }

    public int get_SurfaceDetector() {
        return _SurfaceDetector;
    }

    public void set_SurfaceDetector(int _SurfaceDetector) {
        this._SurfaceDetector = _SurfaceDetector;
    }

    public int get_SurfaceLayer() {
        return _SurfaceLayer;
    }

    public void set_SurfaceLayer(int _SurfaceLayer) {
        this._SurfaceLayer = _SurfaceLayer;
    }

    public int get_SurfaceSector() {
        return _SurfaceSector;
    }

    public void set_SurfaceSector(int _SurfaceSector) {
        this._SurfaceSector = _SurfaceSector;
    }

    public double get_TrkPhiAtSurface() {
        return _TrkPhiAtSurface;
    }
    /**
     * @return the _SurfaceComponent
     */
    public int get_SurfaceComponent() {
        return _SurfaceComponent;
    }

    /**
     * @param _SurfaceComponent the _SurfaceComponent to set
     */
    public void set_SurfaceComponent(int _SurfaceComponent) {
        this._SurfaceComponent = _SurfaceComponent;
    }
    
    public void set_TrkPhiAtSurface(double _TrkPhiAtSurface) {
        this._TrkPhiAtSurface = _TrkPhiAtSurface;
    }

    public double get_TrkThetaAtSurface() {
        return _TrkThetaAtSurface;
    }

    public void set_TrkThetaAtSurface(double _TrkThetaAtSurface) {
        this._TrkThetaAtSurface = _TrkThetaAtSurface;
    }

    public double get_TrkToModuleAngle() {
        return _TrkToModuleAngle;
    }

    public void set_TrkToModuleAngle(double _TrkToModuleAngle) {
        this._TrkToModuleAngle = _TrkToModuleAngle;
    }

    public double get_CalcCentroidStrip() {
        return _CalcCentroidStrip;
    }

    public void set_CalcCentroidStrip(double _CalcCentroidStrip) {
        this._CalcCentroidStrip = _CalcCentroidStrip;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Instantiates a new vec.
     */
    public StateVec() {
        set(0,0,0,0,0,0);
    }

    /**
     * Sets the.
     *
     * @param V the v
     */
    public void set(StateVec V) {
        set(V.x(), V.y(), V.z(), V.ux(), V.uy(), V.uz());
    }

    private int _planeIdx;

    /**
     *
     * @return the wire plane index in the series of planes used in the
     * trajectory
     */
    public int get_planeIdx() {
        return _planeIdx;
    }

    /**
     * Sets the wire plane index in the series of planes used in the trajectory
     *
     * @param _planeIdx wire plane index in the series of planes used in the
     * trajectory
     */
    public void set_planeIdx(int _planeIdx) {
        this._planeIdx = _planeIdx;
    }

    /**
     * Sets the stateVec
     *
     * @param y the _y
     * @param x the _x
     * @param z the _z
     * @param ux the x-component of the tangent to the helix at point (x,y,z)
     * @param uy the y-component of the tangent to the helix at point (x,y,z)
     * @param uz the z-component of the tangent to the helix at point (x,y,z)
     */
    public void set(double x, double y, double z, double ux, double uy, double uz) {
        _x=x;
        _y=y;
        _z=z;
        _ux=ux;
        _uy=uy;
        _uz=uz;
    }

    /**
     * Instantiates a new stateVec
     *
     * @param y the _y
     * @param x the _x
     * @param z the _z
     * @param ux the x-component of the tangent to the helix at point (x,y,z)
     * @param uy the y-component of the tangent to the helix at point (x,y,z)
     * @param uz the z-component of the tangent to the helix at point (x,y,z)
     */
    public StateVec(double x, double y, double z, double ux, double uy, double uz) {
        set(x, y, z, ux, uy, uz);
    }

    /**
     * Instantiates a new stateVec
     *
     * @param v the v
     */
    public StateVec(StateVec v) {
        set(v._x, v._y, v._z, v._ux, v._uy, v._uz); 
    }

    

    /**
     * Description of x().
     *
     * @return the x component
     */
    public double x() {
        return _x;
    }

    /**
     * Description of y().
     *
     * @return the y component
     */
    public double y() {
        return _y;
    }

    /**
     * Description of z().
     *
     * @return the z component
     */
    public double z() {
        return _z;
    }

    /**
     * Description of ux().
     *
     * @return the ux component
     */
    public double ux() {
        return _ux;
    }

    /**
     * Description of uy().
     *
     * @return the uy component
     */
    public double uy() {
        return _uy;
    }

    /**
     * Description of uz().
     *
     * @return the uz component
     */
    public double uz() {
        return _uz;
    }

    @Override
    public int compareTo(StateVec arg) {

        int return_val = 0;
        if (Constants.isCosmicsData == false) {
            int RegComp = this.get_SurfaceLayer() < arg.get_SurfaceLayer() ? -1 : this.get_SurfaceLayer() == arg.get_SurfaceLayer() ? 0 : 1;
            int IDComp = this.get_ID() < arg.get_ID() ? -1 : this.get_ID() == arg.get_ID() ? 0 : 1;

            return_val = ((RegComp == 0) ? IDComp : RegComp);
        }
        if (Constants.isCosmicsData == true) {
            int RegComp = this.y() < arg.y() ? -1 : this.y() == arg.y() ? 0 : 1;
            int IDComp = this.get_ID() < arg.get_ID() ? -1 : this.get_ID() == arg.get_ID() ? 0 : 1;

            return_val = RegComp;//((RegComp == 0) ? IDComp : RegComp);
        }

        return return_val;
    }

}
