package org.jlab.rec.fmt.hit;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.rec.fmt.Constants;

/**
 * A  hit characterized by layer, sector, wire number, and Edep.  The ADC to time conversion has been done.
 * @author ziegler
 *
 */


public class Hit implements Comparable<Hit>{
	// class implements Comparable interface to allow for sorting a collection of hits by wire number values
	
	
	// constructors 
	
	/**
	 * @param sector 
	 * @param layer 
	 * @param strip 
	 * @param Edep (for gemc output without digitization)
	 */
	public Hit(int sector, int layer, int strip, double Edep) {
		this._Sector = sector;
		this._Layer = layer;
		this._Strip = strip;
		this._Edep = Edep;	
                
		double x0 = Constants.FVT_stripsX[layer-1][strip-1][0];
		double x1 = Constants.FVT_stripsX[layer-1][strip-1][1];		
		double y0 = Constants.FVT_stripsY[layer-1][strip-1][0];
		double y1 = Constants.FVT_stripsY[layer-1][strip-1][1];
		double Z  = Constants.FVT_Zlayer[layer-1]; // z
		Line3D seg = new Line3D();
		seg.setOrigin(new Point3D(x0,y0,Z));
		seg.setEnd(new Point3D(x1,y1,Z));
		set_StripSegment(seg);
	}
	
	
	private int _Sector;      							//	   sector[1...24]
	private int _Layer;    	 							//	   layer [1,...6]
	private int _Strip;    	 							//	   strip [1...256]

	private double _Edep;      							//	   Reconstructed time, for now it is the gemc time
	private Line3D _StripSegment;						//     The geometry segment representing the strip position
	private int _Id;									//	   Hit Id
	
	
	/**
	 * 
	 * @return the sector (1...24)
	 */
	public int get_Sector() {
		return _Sector;
	}

	/**
	 * Sets the sector 
	 * @param _Sector
	 */
	public void set_Sector(int _Sector) {
		this._Sector = _Sector;
	}
	

	/**
	 * 
	 * @return the layer (1...8)
	 */
	public int get_Layer() {
		return _Layer;
	}

	/**
	 * Sets the layer
	 * @param _Layer
	 */
	public void set_Layer(int _Layer) {
		this._Layer = _Layer;
	}

	/**
	 * 
	 * @return the wire number (1...256)
	 */
	public int get_Strip() {
		return _Strip;
	}

	/**
	 * Sets the wire number
	 * @param _Wire
	 */
	public void set_Strip(int _Strip) {
		this._Strip = _Strip;
	}

	/**
	 * 
	 * @return the Edep in MeV
	 */
	public double get_Edep() {
		return _Edep;
	}

	/**
	 * Sets the Edep
	 * @param _Edep
	 */
	public void set_Edep(double _Edep) {
		this._Edep = _Edep;
	}


	/**
	 * 
	 * @return the ID
	 */	
	public int get_Id() {
		return _Id;
	}

	/**
	 * Sets the hit ID.  The ID corresponds to the hit index in the EvIO column.
	 * @param _Id
	 */
	public void set_Id(int _Id) {
		this._Id = _Id;
	}


	/**
	 * 
	 * @return region (1...4)
	 */
    public int get_Region() {
     	return (int) (this._Layer+1)/2;
     }
    /**
     * 
     * @return superlayer 1 or 2 in region (1...4)
     */
    public int get_RegionSlayer() {
    	return (this._Layer+1)%2+1;
	}

    public Line3D get_StripSegment() {
		return _StripSegment;
	}

	public void set_StripSegment(Line3D _StripSegment) {
		this._StripSegment = _StripSegment;
	}

	/**
	 * 
	 * @param arg0 the other hit
	 * @return an int used to sort a collection of hits by wire number. Sorting by wire is used in clustering.
	 */
	@Override
	public int compareTo(Hit arg0) {
		if(this._Strip>arg0._Strip) {
			return 1;
		} else {
			return 0;
		}
	}
		
	/**
	 * 
	 * @return print statement with hit information
	 */
	public String printInfo() {
		String s = " Hit: ID "+this.get_Id()+" Sector "+this.get_Sector()+" Layer "+this.get_Layer()+" Strip "+this.get_Strip()+" Edep "+this.get_Edep();
		return s;
	}

	/**
	 * 
	 * @param otherHit
	 * @return a boolean comparing 2 hits based on basic descriptors; 
	 * returns true if the hits are the same
	 */
	public boolean isSameAs(FittedHit otherHit) {
		FittedHit thisHit = (FittedHit) this;
		boolean cmp = false;
		if(thisHit.get_Edep() == otherHit.get_Edep() 
				&& thisHit.get_Sector() == otherHit.get_Sector()
							&& thisHit.get_Layer() == otherHit.get_Layer()
									&& thisHit.get_Strip() == otherHit.get_Strip() )
			cmp = true;
		return cmp;
	}
	

}
