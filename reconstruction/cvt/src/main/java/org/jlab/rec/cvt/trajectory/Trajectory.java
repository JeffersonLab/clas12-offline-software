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
        this.set_ray(ray);
    }

    public Helix get_helix() {
        return _helix;
    }

    public void set_helix(Helix _helix) {
        this._helix = _helix;
    }

    public Ray get_ray() {
        return _ray;
    }

    public void set_ray(Ray _ray) {
        this._ray = _ray;
    }

    private Helix _helix;
    private Ray _ray;

    // Sector for each region
    private int[] _Sector = new int[4];
    private int _Id = -1;
    private ArrayList<StateVec> _Trajectory;

    public int[] get_SVTSector() {
        return _Sector;
    }

    public void set_SVTSector(int[] _Sector) {
        this._Sector = _Sector;
    }

    public int get_Id() {
        return _Id;
    }

    public void set_Id(int _Id) {
        this._Id = _Id;
    }

    public ArrayList<StateVec> get_Trajectory() {
        return _Trajectory;
    }

    public void set_Trajectory(ArrayList<StateVec> _Trajectory) {
        this._Trajectory = _Trajectory;
    }

    private double[][][] _SVTIntersections;
    private double[][][] _BMTIntersections;

    public double[][][] get_SVTIntersections() {
        return _SVTIntersections;
    }

    public void set_SVTIntersections(double[][][] _SVTIntersections) {
        this._SVTIntersections = _SVTIntersections;
    }

    public double[][][] get_BMTIntersections() {
        return _BMTIntersections;
    }

    public void set_BMTIntersections(double[][][] _BMTIntersections) {
        this._BMTIntersections = _BMTIntersections;
    }

    /**
     *
     */
    private static final long serialVersionUID = 358913937206455870L;

    public boolean isFinal = false;

   

}
