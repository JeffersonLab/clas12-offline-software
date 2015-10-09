package cnuphys.bCNU.graphics.toolbar.lasso;

import java.awt.geom.Rectangle2D;

public interface ILassoListener {

	public void lassoStarting();

	public void lassoEnding();

	/**
	 * Have lassoed a rectangle
	 * 
	 * @param wr
	 *            the world rectangle of the lasso
	 */
	public void rectangleLasso(Rectangle2D.Double wr, boolean funnel);
}
