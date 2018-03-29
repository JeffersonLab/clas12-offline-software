package org.jlab.rec.cvt.track;
import org.jlab.rec.cvt.cross.Cross;
import org.jlab.geom.prim.Vector3D;
//import org.jlab.geom.prim.Point3D;
import java.util.*;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Base cell for the cellular automaton
 * A cell is defined by two crosses and its state
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
	public Cell(){}
	
	public Cell( Cross a, Cross b ){
		this._c1 = a;
		this._c2 = b;
		this._dir = a.get_Point().vectorTo(b.get_Point());
		this.nb = new ArrayList<Cell>();
		this._state = 1;
		this._used = false;
	}

	public Cell( Cross a, Cross b, String plane ){
		this._c1 = a;
		this._c2 = b;
		this._dir = a.get_Point().vectorTo(b.get_Point());
		this.nb = new ArrayList<Cell>();
		this._state = 1;
		this._plane = plane;
		this._used = false;
	}
	
	@Override 
	public int compareTo(Cell arg0) {
		
		return compare( this, arg0 );
	}
	
  	public int compare( Cell c, Cell k ){
  		if( c.get_state() == k.get_state()) return 0;
  		if( c.get_state() < k.get_state() )  return 1;
  		else return -1;
  	}
	
	public double get_length(){
		return this._c1.get_Point().vectorTo(this._c2.get_Point()).mag();
	}

	public Point2d get_Crs2D( int i ){
		return this.get_Crs2D( i, this._plane );
	}
	public Point2d get_Crs2D( int i, String vw ){
		if( i < 1 || i > 2){
			System.err.println("ERROR, please select 1 or 2 for the first or second cross");
			return null;
		}
		Cross cross;
		if( i == 1) cross = this._c1;
		else cross = this._c2;
		
		Point2d point = new Point2d();
		if( vw.equalsIgnoreCase("XY")){
			point.set( cross.get_Point().x(), cross.get_Point().y() );
		}
		if( vw.equalsIgnoreCase("ZR")){
			double R = 0.;
			if( cross.get_Detector().equalsIgnoreCase("SVT")){
				R = Math.sqrt(cross.get_Point().x()*cross.get_Point().x() + 
						      cross.get_Point().y()*cross.get_Point().y() );
			}
			else {
				R = org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[cross.get_Region()-1];
			}
			point.set( cross.get_Point().z(), R );
		}		
		return point;
	}
	

	public Vector2d get_dir2D(){
		return this.get_dir2D(this._plane);
	}
	public Vector2d get_dir2D(String vw){
		if( vw.equalsIgnoreCase("ZR") ){
			Point2d p1 = get_Crs2D(1, "ZR");
			Vector2d v1 = new Vector2d( p1.x, p1.y);
			Point2d p2 = get_Crs2D(2, "ZR");
			Vector2d v2 = new Vector2d( p2.x, p2.y);
			
			v2.sub(v1);
			return v2 ;
		}
		else if( vw.equalsIgnoreCase("XY") ){
			Vector2d v = new Vector2d(this._dir.x(),this._dir.y());
			v.normalize();
			return v;
		}
		else { return null; }
	}
	
	@Override
	public String toString(){
//		String tmp = (this.nb.size()>0) ?  this.nb.get(0) + " " + this.nb.get(nb.size()-1) : "";
		return "c1_Id "+this._c1.get_Id() + " "+this._c1.get_Detector()
				+", c2_Id "+this._c2.get_Id() + " "+this._c2.get_Detector()
				+", state "+this._state
				+", nb "+this.nb.size()+ ", plane " + this._plane + " " ;
	}
	
	public boolean equals( Cell c ){
		if( c.get_c1() == this._c1 && c.get_c2() == this._c2 ) return true;
		return false;
	}
	
	public boolean contains( Cross x ){
		if( x.equals(this._c1) || x.equals(this._c2) ) return true;
		return false;
	}
	
	public void addNeighbour( Cell b ){
		if( nb == null ){ 
			this.nb = new ArrayList<Cell>();
		}
		this.nb.add(b);
	}
	
	public List<Cell> get_neighbors(){
		return this.nb;
	}
	
	public Cross get_c1() {
		return _c1;
	}

	public void set_c1(Cross _c1) {
		this._c1 = _c1;
	}

	public Cross get_c2() {
		return _c2;
	}

	public void set_c2(Cross _c2) {
		this._c2 = _c2;
	}

	public Vector3D get_dir() {
		return _dir;
	}

	public void set_dir(Vector3D dir) {
		this._dir = dir;
	}
	
	public int get_state() {
		return _state;
	}

	public void set_state(int _state) {
		this._state = _state;
	}

	public String get_plane() {
		return _plane;
	}

	public void set_plane(String _plane) {
		this._plane = _plane;
	}

	public boolean is_used() {
		return _used;
	}

	public void set_used(boolean _used) {
		this._used = _used;
	}



}
