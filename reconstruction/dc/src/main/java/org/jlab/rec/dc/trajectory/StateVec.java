package org.jlab.rec.dc.trajectory;

import Jama.*;
/**
 * A StateVec describes a cross measurement in the DC.  It is characterized by a point in the DC
 * tilted coordinate system at each wire plane (i.e. constant z) and by unit tangent vectors in the x and y 
 * directions in that coordinate system.  The state vector parameters are the plane index, (x, y, tanTheta_x, tanTheta_y).
 * [The tilted coordinate system is the sector coordinate system rotated by 25 degrees so that the z axis perperdicular to the wire planes]
 * @author ziegler
 *
 */
public class StateVec extends Matrix {
	
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1874984192960130771L;


    /**
     * Instantiates a new  vec.
     */
    public StateVec() {
            super(4,1);
    }

    private double _PathLength;

    public double getPathLength() {
        return _PathLength;
    }

    public void setPathLength(double _PathLength) {
        this._PathLength = _PathLength;
    }

    /**
     * Sets the.
     *
     * @param V the v
     */
    public void set(StateVec V) {
            set(0,0,V.x());
            set(1,0,V.y());
            set(2,0,V.tanThetaX());
            set(3,0,V.tanThetaY());
    }

    private int _planeIdx;
    /**
     * 
     * @return the wire plane index in the series of planes used in the trajectory
     */  
    public int getPlaneIdx() {
        return _planeIdx;
    }

    
    /**
     * Sets the wire plane index in the series of planes used in the trajectory
     * @param _planeIdx wire plane index in the series of planes used in the trajectory
     */
    public void setPlaneIdx(int _planeIdx) {
        this._planeIdx = _planeIdx;
    }
    
    private double Q;

    public double getQ() {
        return Q;
    }

    public void setQ(double Q) {
        this.Q = Q;
    }
    
    private double z;

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
    
    private double b;

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }
    // KF projector --> get Wire midPoint match
    private double hw;

    public double getProjector() {
        return hw;
    }

    public void setProjector(double h) {
        this.hw = h;
    }
    // KF projector --> get fit doca
    private double h;

    public double getProjectorDoca() {
        return h;
    }

    public void setProjectorDoca(double h) {
        this.h = h;
    }
    /**
     * Sets the stateVec 
     *
     * @param y the _y
     * @param x the _x
     * @param tanThX the _tanThetaX
     * @param tanThY the _tanThetaY
     */
    public void set(double x, double y, double tanThX, double tanThY) {
            set(0,0,x);
            set(1,0,y);
            set(2,0,tanThX);  
            set(3,0,tanThY); 
    }

    /**
     * Instantiates a new stateVec
     *
     * @param y the _y
     * @param x the _x
     * @param tanThX the _tanThetaX
     * @param tanThY the _tanThetaY
     */
    public StateVec(double x, double y, double tanThX, double tanThY) {
            super(4,1);
            set(0,0,x);
            set(1,0,y);
            set(2,0,tanThX);  
            set(3,0,tanThY); 
    }

    /**
     * Instantiates a new stateVec
     *
     * @param v the v
     */
    public StateVec(StateVec v) {
            super(4,1);
            set(0,0,v.x());
            set(1,0,v.y());
            set(2,0,v.tanThetaX());
            set(3,0,v.tanThetaY());
    }


    /**
     * Instantiates a new  StateVec.
     *
     * @param m the m
     */
    private StateVec(Matrix m) { //needed since Jama.Matrix cannot be casted into StateVec		
            super(4,1);
            set(0,0,m.get(0, 0));
            set(1,0,m.get(1, 0));
            set(2,0,m.get(2, 0));
            set(3,0,m.get(3, 0));
    }


    /**
     * Description of x().
     *
     * @return the x component
     */
    public double x() {
            return(get(0,0));
    }

    /**
     * Description of y().
     *
     * @return the y component
     */ 

    public double y() {
            return(get(1,0));
    }

    /**
     * Description of tanThetaX().
     *
     * @return the tanThetaX component
     */ 
    public double tanThetaX() {
            return(get(2,0));
    }

    /**
     * Description of tanThetaY().
     *
     * @return the tanThetaY component
     */ 
    public double tanThetaY() {
            return(get(3,0));
    }



    public void printInfo() {
            System.out.println("StateVec [ "+this.x()+", "+this.y()+", "+this.tanThetaX()+", "+this.tanThetaY()+" ] ");
    }

}
