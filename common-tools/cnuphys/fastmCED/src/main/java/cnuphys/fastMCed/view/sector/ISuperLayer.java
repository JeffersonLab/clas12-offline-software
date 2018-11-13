package cnuphys.fastMCed.view.sector;

import org.jlab.geom.prim.Plane3D;
import cnuphys.bCNU.item.PolygonItem;

public interface ISuperLayer {

	/**
	 * Get the projection plane. For SectorView this is the constant
	 * phi plane and is the same for all superlayers. For ProjectedDCView it is the
	 * @return the plane perpendicular to the wires and depends on superlayer.
	 */
	public Plane3D projectionPlane();
	
	/**
	 * Check whether this is in a "lower" sector 4,5,6. Only relevant
	 * for SectorView. For superlayers on the DCProjectedView, should return 
	 * false;
	 * @return for SectorViews return <code>true</code> for sectors 4,5,6. Otherwise false.
	 */
	public boolean isLowerSector();
	
	/**
	 * Get the 1-based sector
	 * @return the 1-based sector
	 */
	public int sector();
	
	/**
	 * Get the one based superlayer
	 * @return the one based superlayyer
	 */
	public int superlayer();
	
	/**
	 * return the underlying polygon item
	 * @return the underlying polygon item
	 */
	public PolygonItem item();
	
}
