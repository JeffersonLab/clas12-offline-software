package cnuphys.ced.event.data;

import java.awt.Point;
import java.util.List;

public class DCHit {
	
	//used for mouse over feedback
	private Point _screenLocation = new Point();


	/** The 1-based sector */
	public byte sector;
	
	/** The 1-based superlayer 1..6 */
	public byte superlayer;

	/** The 1-based sector 1..6 */
	public byte layer;
	
	/** The 1-based sector */
	public short wire;
	
	/** id of the hit */
	public short id;
	
	/** status of the hit */
	public short status;
	
	/** left right ambiguity */
	public byte lr; 
	
	/** Time */
	public int tdc;
	
	/** Track DOCA in cm */
	public float trkDoca;

	
	/**
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer 1..6
	 * @param wire the 1-based wire
	 */
	public DCHit(byte sector, byte superlayer, byte layer, short wire, short id, short status, byte lr, int tdc, float doca) {
		this.sector = sector;
		this.superlayer = superlayer;
		this.layer = layer;
		this.wire = wire;
		this.id = id;
		this.status = status;
		this.lr = lr;
		this.tdc = tdc;
		this.trkDoca = doca;
	}
	
	/**
	 * Add to a list of feedback strings, no doubt
	 * because the mouse is ovwer this hit
	 * @param v the list to add to
	 */
	public void getFeedbackStrings(String prefix, List<String> v) {
		if (v == null) {
			return;
		}
		
		String hitStr1 = String.format(prefix + " DC hit sect %d supl %d  layer %d  wire %d", sector,
				superlayer, layer, wire);
		v.add("$red$" + hitStr1);
		
		String hitStr2;
		if (trkDoca < 0.) {
			hitStr2 = String.format("status %d  lr %d  tdc %d  (no doca)", status, lr, tdc);
		}
		else {
			hitStr2 = String.format("status %d  lr %d  tdc %d  doca %6.2f mm", status, lr, tdc, 10*trkDoca);
		}
		v.add("$red$" + hitStr2);
	}
	
	/**
	 * Get the hit location where it was last drawn
	 * @return the screen location
	 */
	public Point getLocation() {
		return _screenLocation;
	}

	/**
	 * For feedback
	 * @param pp
	 */
	public void setLocation(Point pp) {
		_screenLocation.x = pp.x;
		_screenLocation.y = pp.y;
	}
	
	public boolean contains(Point pp) {
		return ((Math.abs(_screenLocation.x - pp.x) <= DataDrawSupport.HITHALF) &&
				(Math.abs(_screenLocation.y - pp.y) <= DataDrawSupport.HITHALF));
	}


}
