package org.jlab.rec.vtx;

public class Vertex {

    public Vertex(double r, Particle p0, Particle p1, Particle p2) {
        this._r = r;
        this._p0 = p0;
        this._p1 = p1;
        this._p2 = p2;
    }
    /**
     * @return the _r
     */
    public double getR() {
        return _r;
    }

    /**
     * @param _r the _r to set
     */
    public void setR(double _r) {
        this._r = _r;
    }

    /**
     * @return the _p1
     */
    public Particle getP1() {
        return _p1;
    }

    /**
     * @param _p1 the _p1 to set
     */
    public void setP1(Particle _p1) {
        this._p1 = _p1;
    }

    /**
     * @return the _p2
     */
    public Particle getP2() {
        return _p2;
    }

    /**
     * @param _p2 the _p2 to set
     */
    public void setP2(Particle _p2) {
        this._p2 = _p2;
    }

    /**
     * @return the _p0
     */
    public Particle getP0() {
        return _p0;
    }

    /**
     * @param _p0 the _p0 to set
     */
    public void setP0(Particle _p0) {
        this._p0 = _p0;
    }
    private double _r;
    private Particle _p1;
    private Particle _p2;
    private Particle _p0;
    
    
	
} // end class
