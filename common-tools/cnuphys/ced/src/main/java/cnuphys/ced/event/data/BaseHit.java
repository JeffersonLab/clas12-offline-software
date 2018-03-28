package cnuphys.ced.event.data;

import java.awt.Point;

public class BaseHit {
	
	//used for mouse over feedback
	private Point _screenLocation = new Point();


	/** The 1-based sector */
	public byte sector;
	
	/** The 1-based layer */
	public byte layer;
	
	/** The 1-based component */
	public short component;
	
	/**
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @param component the 1-based component
	 */
	public BaseHit(byte sector, byte layer, short component) {
		super();
		this.sector = sector;
		this.layer = layer;
		this.component = component;
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
