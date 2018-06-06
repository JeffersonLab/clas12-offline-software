package org.jlab.rec.cvt.track;
import java.util.*;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.jlab.rec.cvt.bmt.Geometry;
import org.jlab.rec.cvt.cross.Cross;

/**
 * Cellular automaton maker. 
 * It creates the cells from the crosses, finds the neighbors
 * And it makes the system evolve
 *
 * @author fbossu
 *
 */

public class MakerCA {

	private List<Cell> nodes;
	private double _aCvsR;  // max angle in degrees between the radius to the first cross and cell segment
	private double _abCrs;  // max angle in degrees where to look for cross pairs 
	private double _cosBtwCells; // minimal cosine between cells
	private String _plane;  // plane, XY (default) or RZ 
	private boolean _debug;

	public MakerCA( boolean debug ) {
		this();
		this._debug = debug;
	}
	
	public MakerCA(){
		this._abCrs = 11.;
		this._aCvsR = 15;
		this._cosBtwCells = 0.995;
		this._plane = "XY"; 
		this._debug = false;
	}
	
	
	private double getCrossRadius( Cross c ) {  // TODO: can this be moved inside the Cross class?
		if( c.get_Detector().equalsIgnoreCase("SVT") ) 
			return Math.sqrt(c.get_Point().x()*c.get_Point().x()+c.get_Point().y()*c.get_Point().y());
		
		if( c.get_DetectorType().equalsIgnoreCase("Z") )
			return org.jlab.rec.cvt.bmt.Constants.getCRZRADIUS()[c.get_Region()-1 ];
		
		return org.jlab.rec.cvt.bmt.Constants.getCRCRADIUS()[c.get_Region()-1 ];
	}
	
	private boolean checkAngles( Cell cell ) {
		double xa = cell.get_Crs2D(1, this._plane).x;
		double ya = cell.get_Crs2D(1, this._plane).y;
		double xb = cell.get_Crs2D(2, this._plane).x;
		double yb = cell.get_Crs2D(2, this._plane).y;
		Vector2d va = new Vector2d(xa,ya);
		Vector2d vb = new Vector2d(xb,yb);

		if( this._plane.equalsIgnoreCase("XY")) {
			// in XY follow the linear relation obtained from simulations
			// Angle( DR ) = 0.175 * DR + 0.551 
			// where DR is the difference in radius of the crosses
			// The angles are in degrees
			double R1 = this.getCrossRadius( cell.get_c1() );
			double R2 = this.getCrossRadius( cell.get_c2() );
			double angle = 1.75 * (R2 - R1) + 0.551; // in cm; TODO: create setters and getters for the parameters
//			if( Math.toDegrees(va.angle(vb)) > this._abCrs ) return false;
			if( Math.toDegrees(va.angle(vb)) > angle ) return false;

			// check then the cell makes a "nice" angle with the radius: TODO needed?
    		if(  va.angle( cell.get_dir2D(this._plane)) > Math.toRadians( this._aCvsR) ) return false;			
		}
			
		if( this._plane.equalsIgnoreCase("ZR")) {
			// on ZR, make loose selection on SVT
			// and tight selections on BMT
			if( cell.get_c1().get_Detector().equalsIgnoreCase("SVT")) {
				if( cell.get_c2().get_Detector().equalsIgnoreCase("SVT")) {
					if( Math.toDegrees(va.angle(vb)) > 30. ) return false;
				}
				else
					if( Math.toDegrees(va.angle(vb)) > 19. ) return false;				
			}
			else {
				if( Math.toDegrees(va.angle(vb)) > 3.5 ) return false;
			}
			
			// check then the cell makes a "nice" angle with the radius: TODO needed?
    		if(  va.angle( cell.get_dir2D(this._plane)) > Math.toRadians( this._aCvsR) ) return false;
		}
		return true;
	}
	
	public void createCells( List<Cross> crs, Geometry bgeom ){
		// this function loops over the crosses and looks for pairs that pass the cuts
		//
		Collections.sort(crs);
		nodes = new ArrayList<Cell>();
        for( int ic=0;ic<crs.size();ic++){
      	  Cross a = crs.get(ic);
      	  int aReg = a.get_Region();
      	  if( a.get_Detector().equalsIgnoreCase("BMT")) {
      		  aReg = 3 + bgeom.getLayer( aReg , a.get_DetectorType() );
      	  }
      	  
      	  if( this._debug ) {
      		  System.out.println( "\n cross a " + a.get_Id() + " " + a.get_Detector() +a.get_DetectorType() + " sect:" + a.get_Sector() + " reg:" 
      				  + aReg + " phi:" + a.get_Point().toVector3D().phi() + " in BMT sector:" + 
      				  bgeom.isInSector(1, a.get_Point().toVector3D().phi(), 0 ));
      	  }
      	  
      	  
//      	  for(int jc=0;jc<crs.size();jc++){
      	  for(int jc=ic+1;jc<crs.size();jc++){
          	  Cross b = crs.get(jc);
          	  
          	  // we skip same region crosses
          	  int bReg = b.get_Region();
          	  if( b.get_Detector().equalsIgnoreCase("BMT")) {
          		  bReg = 3 + bgeom.getLayer( bReg , b.get_DetectorType() );
          	  }
          	  
          	  if( this._debug ) {
          		  System.out.println( " cross b " + b.get_Id() + " " + b.get_Detector() +b.get_DetectorType() + " sect:" + b.get_Sector() + " reg:" 
          				  + bReg + " phi:" + b.get_Point().toVector3D().phi() + " in BMT sector:" + 
          				  bgeom.isInSector(1, b.get_Point().toVector3D().phi(), 0 ));
          	  }
          	  
          	  if( bReg <= aReg  ) continue; // crosses should be ordered. skip in case they are not
          	  
          	  // we allow skipping one region maximum
          	  if( bReg <=4 ) {
          		  if( Math.abs( bReg-aReg) > 2) continue;
          		  if( this._debug) System.out.println(" bReg <=4       passed Delta region 2 ");
          	  }
          	  if( bReg > 4 && bReg < 7  ) {
          		  if( Math.abs( bReg-aReg) > 3) continue;
          		  if( this._debug) System.out.println(" 4 < bReg < 7       passed Delta region 3 " ); 
          	  }
          	  if( bReg >= 7 ) {
          		  if( Math.abs( bReg-aReg) > 4) continue;
          		  if( this._debug) System.out.println(" 7 >= bReg        passed Delta region 4 " );
          	  }

          	  // stay in the same BMT sector
          	  if( b.get_Detector().equalsIgnoreCase("BMT") ){
          		  if( a.get_Detector().equalsIgnoreCase("BMT")){
          			  if(b.get_Sector() != a.get_Sector() ) continue;
          		  }
          		  else{
          			  double aphi = a.get_Point().toVector3D().phi() ;
          			  if( ! bgeom.checkIsInSector( aphi, b.get_Sector(), 1, Math.toRadians(10) )  ) {
          				  if( this._debug) System.out.println("cross b and a are not in the same sector"); 
          				  continue;
      				  }
          			  
          		  }
          	  }
          	  
          	  // create the cell
      		  Cell scell = new Cell( a,b, this._plane);
      		  if( this._debug) System.out.println( " ... create a cell: " + scell);
      		  
      		  // check angular position of the crosses and the cell
      		  if( this.checkAngles(scell) == false ) {
          		  if( this._debug) System.out.println("    +++ angle check not passed  +++ ");
      			  continue;
      		  }
      		  	

      		  // when running on ZR
      		  // if both cells are SVT, check that they are "ok" in xy
      		  if( this._plane.equalsIgnoreCase("ZR")){
      			  if( b.get_Detector().equalsIgnoreCase("SVT") ){
              		  double xaxy = scell.get_Crs2D(1, "XY").x;
              		  double yaxy = scell.get_Crs2D(1, "XY").y;
              		  Vector2d vaxy = new Vector2d(xaxy,yaxy);
              		  if(  vaxy.angle( scell.get_dir2D("XY")) > Math.toRadians( this._aCvsR) ) continue;
      			  }
      		  }
      		  
      		  // here a good cell if found. Adding it to the list of cells
      		  nodes.add(scell);
      		  if( this._debug) System.out.println( "adding the cell to the node list\n");
          	  
      	  }
        }
	}
	

	public void findNeigbors(){
		// from the list of cells, it looks for neigbours from "inside-out" 
		if( nodes == null ){ return; }
        for(int ic=0;ic<nodes.size();ic++){
      	  Cell c = nodes.get(ic);
      	  for(int jc=ic+1;jc<nodes.size();jc++){
      		  Cell n = nodes.get(jc);
      		  if( c.get_neighbors().contains(n))continue;
      		  if( n.get_c1().equals(c.get_c2()) ){     			  
      			  if( n.get_dir2D(this._plane).dot(c.get_dir2D(this._plane)) > this._cosBtwCells){
      				  n.addNeighbour(c);
      			  }
      		  }
      	  }
        }
	}
	
	public void evolve(int N){
        int[] states = new int[nodes.size()];
        // evolve
        for( int i=0; i<N; i++){ // epochs 
      	  
      	  // find the state for each cell
      	  int j=0;
      	  for( Cell c : nodes ){
      		  int max = 0;
      		  for( Cell n : c.get_neighbors() ){
      			  if( n.get_state() > max ) max = n.get_state(); 
      		  }
      		  states[j] =  1 + max ;
      		  j++;
      	  }
      	  // update all cell states at once
      	  for( j=0;j<nodes.size();j++) nodes.get(j).set_state(states[j]);

        }
	}

	public double get_aCvsR() {
		return _aCvsR;
	}

	public void set_aCvsR(double _aCvsR) {
		this._aCvsR = _aCvsR;
	}

	public double get_abCrs() {
		return _abCrs;
	}

	public void set_abCrs(double _abCrs) {
		this._abCrs = _abCrs;
	}

	public double get_cosBtwCells() {
		return _cosBtwCells;
	}

	public void set_cosBtwCells(double _cosBtwCells) {
		this._cosBtwCells = _cosBtwCells;
	}

	public String get_plane() {
		return _plane;
	}

	public void set_plane(String _plane) {
		this._plane = _plane;
	}
	
	public List<Cell> getNodes() {
		return nodes;
	}

	public void setNodes(List<Cell> nodes) {
		this.nodes = nodes;
	}
}
