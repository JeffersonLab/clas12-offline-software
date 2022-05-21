package org.jlab.rec.cvt.track;

import org.jlab.rec.cvt.cross.Cross;
import org.jlab.geom.prim.Vector3D;
import java.util.*;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import org.jlab.rec.cvt.Constants;

/**
 * Base cell for the cellular automaton A cell is defined by two crosses and its
 * state
 *
 * @author fbossu
 *
 */
public class Cell implements Comparable<Cell> {

    private Cross _c1;     // first terminal of the cell
    private Cross _c2;     // last terminal of the cell
    private Vector3D _dir; // direction of the cell
    private int _state;    // state of the cell
    private String _plane; // plane can be XY or ZR
    private ArrayList<Cell> nb; // list of neighbor cells 
    private boolean _used; // has it been used in candidates?

    public Cell() {
    }

    public Cell(Cross a, Cross b) {
        this._c1 = a;
        this._c2 = b;
        this._dir = a.getPoint().vectorTo(b.getPoint());
        this.nb = new ArrayList<>();
        this._state = 1;
        this._used = false;
    }

    public Cell(Cross a, Cross b, String plane) {
        this._c1 = a;
        this._c2 = b;
        this._dir = a.getPoint().vectorTo(b.getPoint());
        this.nb = new ArrayList<>();
        this._state = 1;
        this._plane = plane;
        this._used = false;
    }

    @Override
    public int compareTo(Cell arg0) {

        return compare(this, arg0);
    }

    public int compare(Cell c, Cell k) {
        if (c.getState() == k.getState()) {
            return 0;
        }
        if (c.getState() < k.getState()) {
            return 1;
        } else {
            return -1;
        }
    }

    public double getLength() {
        return this._c1.getPoint().vectorTo(this._c2.getPoint()).mag();
    }

    public Point2d getCrs2D(int i) {
        return this.getCrs2D(i, this._plane);
    }

    public Point2d getCrs2D(int i, String vw) {
        if (i < 1 || i > 2) {
            System.err.println("ERROR, please select 1 or 2 for the first or second cross");
            return null;
        }
        Cross cross;
        if (i == 1) {
            cross = this._c1;
        } else {
            cross = this._c2;
        }

        Point2d point = new Point2d();
        if (vw.equalsIgnoreCase("XY")) {
            point.set(cross.getPoint().x(), cross.getPoint().y());
        }
        if (vw.equalsIgnoreCase("ZR")) {
            point.set(cross.getPoint().z() - Constants.getInstance().getZoffset(), cross.getRadius());
        }
        return point;
    }

    public Vector2d getDir2D() {
        return this.getDir2D(this._plane);
    }

    public Vector2d getDir2D(String vw) {
        if (vw.equalsIgnoreCase("ZR")) {
            Point2d p1 = getCrs2D(1, "ZR");
            Vector2d v1 = new Vector2d(p1.x, p1.y);
            Point2d p2 = getCrs2D(2, "ZR");
            Vector2d v2 = new Vector2d(p2.x, p2.y);

            v2.sub(v1);
            return v2;
        } else if (vw.equalsIgnoreCase("XY")) {
            Vector2d v = new Vector2d(this._dir.x(), this._dir.y());
            v.normalize();
            return v;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
//		String tmp = (this.nb.size()>0) ?  this.nb.get(0) + " " + this.nb.get(nb.size()-1) : "";
        return "c1_Id " + this._c1.getId() + " " + this._c1.getDetector()
                + ", c2_Id " + this._c2.getId() + " " + this._c2.getDetector()
                + ", state " + this._state
                + ", nb " + this.nb.size() + ", plane " + this._plane + " ";
    }

    public boolean equals(Cell c) {
        return c.getC1() == this._c1 && c.getC2() == this._c2;
    }

    public boolean contains(Cross x) {
        return x.equals(this._c1) || x.equals(this._c2);
    }

    public void addNeighbour(Cell b) {
        if (nb == null) {
            this.nb = new ArrayList<>();
        }
        this.nb.add(b);
    }

    public List<Cell> getNeighbors() {
        return this.nb;
    }

    public Cross getC1() {
        return _c1;
    }

    public void setC1(Cross _c1) {
        this._c1 = _c1;
    }

    public Cross getC2() {
        return _c2;
    }

    public void setC2(Cross _c2) {
        this._c2 = _c2;
    }

    public Vector3D getDir() {
        return _dir;
    }

    public void setDir(Vector3D dir) {
        this._dir = dir;
    }

    public int getState() {
        return _state;
    }

    public void getState(int _state) {
        this._state = _state;
    }

    public String getPlane() {
        return _plane;
    }

    public void setPlane(String _plane) {
        this._plane = _plane;
    }

    public boolean isUsed() {
        return _used;
    }

    public void setUsed(boolean _used) {
        this._used = _used;
    }

}
