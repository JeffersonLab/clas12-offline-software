package cnuphys.ced.cedview.alltof;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.PolygonItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.ced.cedview.projecteddc.ISector;
import cnuphys.ced.event.data.CTOF;
import cnuphys.ced.event.data.FTOF;
import cnuphys.ced.event.data.TdcAdcHit;
import cnuphys.ced.event.data.TdcAdcHitList;
import cnuphys.lund.X11Colors;

public class TOFShellItem extends PolygonItem {
	
	private static double DX = 2; //x border margin (cm)
	private static double DY = 2; //y border margin (cm)

	private static final Color _fillColor[] = {Color.white, X11Colors.getX11Color("alice blue")};
	private static final String _fbcolor = "$cyan$";
	
	//offsets
	private double _x0;
	private double _y0;
	private double _xc;
		
	//width (cm)
	private double _width;
	
	//lengths (ascending order)
	private double[] _length;
	
	//number of paddles
	private int _numPaddle;
	
	private ISector _sector;
	
	//0,1,2,3 1A, 1B, 2, CTOF
	private int _panel;
	
	public TOFShellItem(LogicalLayer layer, ISector isect, int panel, 
			double x0, double y0, String name, double width, double[] length) {
		super(layer, getPoints(x0, y0, width, length));
		
		_sector = isect;
		_panel = panel;
		_x0 = x0;
		_y0 = y0;
		_name = name;
		_width = width;
		_length = length;
		_numPaddle = length.length;
		_xc = _x0 + DX + _length[_numPaddle-1]/2;
		
		
	}
	
	/**
	 * Custom drawer for the item.
	 * 
	 * @param g
	 *            the graphics context.
	 * @param container
	 *            the graphical container being rendered.
	 */
	@Override
	public void drawItem(Graphics g, IContainer container) {
		super.drawItem(g, container);
		
		//draw the rectangles
		Rectangle rr = new Rectangle();
		
		for (int i = 0; i < _length.length; i++) {
			getStripRectangle(container, i, rr);
			g.setColor(_fillColor[i%2]);
			g.fillRect(rr.x, rr.y, rr.width, rr.height);
			g.setColor(Color.black);
			g.drawRect(rr.x, rr.y, rr.width, rr.height);
		}
		
	}
	
	/**
	 * Get the strip rectangle
	 * @param container the drawing container
	 * @param index the 0 based paddle index
	 * @param rr the pixel rectangle to fill
	 */
	public void getStripRectangle(IContainer container, int index, Rectangle rr) {
		if ((index >= 0) && (index < _numPaddle) && (rr != null)) {
			Rectangle2D.Double wr = new Rectangle2D.Double();
			double y = _y0 + DY + index*_width;
			double len = _length[index];
			wr.setRect(_xc-len/2, y, len, _width);
			container.worldToLocal(rr, wr);
		}
	}
	
	/**
	 * Add any appropriate feedback strings
	 * panel. Default implementation returns the item's name.
	 * 
	 * @param container
	 *            the Base container.
	 * @param pp
	 *            the mouse location.
	 * @param wp
	 *            the corresponding world point.
	 * @param feedbackStrings
	 *            the List of feedback strings to add to.
	 */
	@Override
	public void getFeedbackStrings(IContainer container, Point pp,
			Point2D.Double wp, List<String> feedbackStrings) {
		
		if (!contains(container, pp)) {
			return;
		}
		
		feedbackStrings.add(_fbcolor + getName());
		
		//get the paddle index
		double ymin = _y0 + DY;
		
		Rectangle rr = new Rectangle();
		int paddleId = 1 + (int)((wp.y - ymin)/_width);
		getStripRectangle(container, paddleId-1, rr);
		if (!rr.contains(pp)) {
			return;
		}
		
		//hit?
		
		TdcAdcHit hit = null;
		if (_panel == TOFView.ALL_CTOF) {
  		    TdcAdcHitList hits = CTOF.getInstance().getHits();
			if ((hits != null) && !hits.isEmpty()) {
				hit = hits.get(0, 0, paddleId);
				if (hit != null) {
					hit.tdcAdcFeedback("CTOF paddle", feedbackStrings);
					return;
				}
			}
		}
		else {
		    TdcAdcHitList hits = FTOF.getInstance().getTdcAdcHits();
			byte sect = (byte) _sector.getSector();
			byte layer = (byte) (_panel + 1);
			short paddle = (short) (paddleId);
			hit = hits.get(sect, layer, paddle);
			if (hit != null) {
				hit.tdcAdcFeedback(getName(), "paddle", feedbackStrings);
				return;
			}
		}
		
		feedbackStrings.add(_fbcolor + "paddle " + paddleId);
		
	}


	//get the four points of the shell.
	private static Point2D.Double[] getPoints(double x0, double y0, double width, double[] length) {
		int numPaddle = length.length;
		double ymax = y0 + 2*DY + (numPaddle*width);
		double ymin = y0;
		double xc = x0 + DX + length[numPaddle-1]/2;

		Point2D.Double wp[] = new Point2D.Double[4];
		for (int i = 0; i < 4; i++) {
			wp[i] = new Point2D.Double();
		}
		
		double DEL = 4.2*DX;
		wp[0].setLocation(xc-length[0]/2-DEL, ymin);
		wp[1].setLocation(xc-length[numPaddle-1]/2-DEL, ymax);
		wp[2].setLocation(xc+length[numPaddle-1]/2+DEL, ymax);
		wp[3].setLocation(xc+length[0]/2+DEL, ymin);
		return wp;
	}
}
