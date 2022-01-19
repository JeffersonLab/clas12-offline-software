package org.jlab.rec.cvt.trajectory;

import java.util.ArrayList;

import org.jlab.rec.cvt.cross.Cross;

/**
 * The trajectory is a set of state vectors at BST planes along the particle
 * path. * A StateVec describes a cross measurement in the BST. directions in
 * that coordinate system.
 *
 * @author ziegler
 *
 */
public class Trajectory extends ArrayList<Cross> {

    public Trajectory(Helix helix) {
        this._helix = helix;
    }

    public Trajectory(Ray ray) {
        this.setRay(ray);
    }

    public Helix getHelix() {
        return _helix;
    }

    public void setHelix(Helix _helix) {
        this._helix = _helix;
    }

    public Ray getRay() {
        return _ray;
    }

    public void setRay(Ray _ray) {
        this._ray = _ray;
    }

    private Helix _helix;
    private Ray _ray;

    // Sector for each region
    private int[] _Sector = new int[4];
    private int _Id = -1;
    private ArrayList<StateVec> _Trajectory;

    public int[] getSVTSector() {
        return _Sector;
    }

    public void setSVTSector(int[] _Sector) {
        this._Sector = _Sector;
    }

    public int getId() {
        return _Id;
    }

    public void setId(int _Id) {
        this._Id = _Id;
    }

    public ArrayList<StateVec> getTrajectory() {
        return _Trajectory;
    }

    public void setTrajectory(ArrayList<StateVec> _Trajectory) {
        this._Trajectory = _Trajectory;
    }

    private double[][][] _SVTIntersections;
    private double[][][] _BMTIntersections;

    public double[][][] getSVTIntersections() {
        return _SVTIntersections;
    }

    public void setSVTIntersections(double[][][] _SVTIntersections) {
        this._SVTIntersections = _SVTIntersections;
    }

    public double[][][] getBMTIntersections() {
        return _BMTIntersections;
    }

    public void setBMTIntersections(double[][][] _BMTIntersections) {
        this._BMTIntersections = _BMTIntersections;
    }

    /**
     *
     */
    private static final long serialVersionUID = 358913937206455870L;

    public boolean isFinal = false;

   

}
