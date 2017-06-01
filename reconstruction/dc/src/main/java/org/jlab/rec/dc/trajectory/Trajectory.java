package org.jlab.rec.dc.trajectory;

import java.util.ArrayList;
import java.util.List;

import org.jlab.rec.dc.cross.Cross;

/**
 * The trajectory is a set of state vectors at DC wire planes along the particle
 * path. * A StateVec describes a cross measurement in the DC. It is
 * characterized by a point in the DC tilted coordinate system at each wire
 * plane (i.e. constant z) and by unit tangent vectors in the x and y directions
 * in that coordinate system.
 *
 * @author ziegler
 *
 */
public class Trajectory extends ArrayList<Cross> {

    public Trajectory() {

    }

    private int _Sector;      							//	    sector[1...6]
    //		cluster Id

    private List<StateVec> _Trajectory;
    private double _IntegralBdl;
    private double _pathLength;

    public int get_Sector() {
        return _Sector;
    }

    public void set_Sector(int _Sector) {
        this._Sector = _Sector;
    }

    public List<StateVec> get_Trajectory() {
        return _Trajectory;
    }

    public void set_Trajectory(List<StateVec> _Trajectory) {
        this._Trajectory = _Trajectory;
    }

    public double get_IntegralBdl() {
        return _IntegralBdl;
    }

    public void set_IntegralBdl(double _IntegralBdl) {
        this._IntegralBdl = _IntegralBdl;
    }

    public double get_PathLength() {
        return _pathLength;
    }

    public void set_PathLength(double _PathLength) {
        this._pathLength = _PathLength;
    }
    /**
     *
     */
    private static final long serialVersionUID = 358913937206455870L;

}
