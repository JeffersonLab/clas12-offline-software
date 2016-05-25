package cnuphys.ced.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.SymbolDraw;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.style.LineStyle;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.bst.BSTxyView;
import cnuphys.ced.cedview.bst.BSTzView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.cedview.projecteddc.ProjectedDCView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.frame.CedColors;

public class DrawingLegend extends JComponent {

	private int width = 100;
	private int height = 100;
	

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 9);

	// parent view
	private BaseView _view;

	/**
	 * Set the parent view
	 * 
	 * @param view
	 *            the parent view
	 */
	public void setView(BaseView view) {
		_view = view;
	}

	/**
	 * Get the parent view
	 * 
	 * @return the parent view
	 */
	public BaseView getView() {
		return _view;
	}

	@Override
	public void paintComponent(Graphics g) {
		Rectangle b = getBounds();
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, b.width, b.height);

		Point pp = new Point();
		// gemc hit
		int yc = 8;
		int x = 8;
		pp.setLocation(x, yc);
		DataDrawSupport.drawGemcHit(g, pp);
		x += quickString(g, x + 6, yc, "GEMC hit ") + 20;

		// reconstructed hit
		pp.setLocation(x, yc);
		DataDrawSupport.drawReconHit(g, pp);
		x += quickString(g, x + 6, yc, "Reconstructed hit ") + 20;

		yc += 12;
		x = 8;

		// view dependent drawing
		if (_view != null) {
			if ((_view instanceof SectorView) ||
					(_view instanceof DCXYView) || (_view instanceof ProjectedDCView)) {
				paintSectorViewLegend(g, x, yc);
			}
			
			else if ((_view instanceof BSTxyView) || (_view instanceof BSTzView)) {
				paintCentralViewLegend(g, x, yc);
			}
		}

	}

	//paint the legent for the central 2D views
	private void paintCentralViewLegend(Graphics g, int x, int yc) {
		int xo = x;
		x = drawCross(g, x, yc, DataDrawSupport.BST_CROSS);
		x = drawCross(g, x, yc, DataDrawSupport.BMT_CROSS);
		
		yc += 16;
		SymbolDraw.drawUpTriangle(g, xo, yc, 3, 
				X11Colors.getX11Color("Dark Green"), X11Colors.getX11Color("Aquamarine"));

		quickString(g, xo+16, yc, "hit strip midpoint");
	}

	private void paintSectorViewLegend(Graphics g, int x, int yc) {
		
		int xo = x;
		Graphics2D g2 = (Graphics2D) g;
		x = drawCross(g, x, yc, DataDrawSupport.HB_CROSS);
		x = drawCross(g, x, yc, DataDrawSupport.TB_CROSS);
		x = drawCircle(g, x, yc, CedColors.tbDocaLine, "TB Doca");
		yc += 16;
		
		//segment line
		x = xo;
		g.setColor(CedColors.docaFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g.drawLine(x, yc, x+30, yc);
		g.setColor(CedColors.tbSegmentLine);
		g2.setStroke(GraphicsUtilities.getStroke(1.5f, LineStyle.SOLID));
		g.drawLine(x, yc, x+30, yc);
		x += 40;
		x = quickString(g, x, yc-2, "TB Segment") + 16;
	}

	private int drawCross(Graphics g, int x, int y, int mode) {
		DataDrawSupport.drawCross(g, x, y, mode);
		
		x += (2*DataDrawSupport.CROSSHALF);
		String s = DataDrawSupport.prefix[mode] + "cross";
		return quickString(g, x, y, s) + 16;
	}
	
	private int drawCircle(Graphics g, int x, int y, Color color, String s) {
		SymbolDraw.drawOval(g, x, y, DataDrawSupport.CROSSHALF, DataDrawSupport.CROSSHALF, color, Color.black);
		x += (2*DataDrawSupport.CROSSHALF);
		return quickString(g, x, y, s) + 16;
	}


	private int quickString(Graphics g, int x, int yc, String s) {
		FontMetrics fm = getFontMetrics(labelFont);
		g.setColor(Color.black);
		g.setFont(labelFont);
		g.setColor(Color.cyan);
		g.drawString(s, x, yc + fm.getAscent() / 2);
		return x + fm.stringWidth(s);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	public static JPanel getLegendPanel(BaseView view) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2, 2));
		DrawingLegend dleg = new DrawingLegend();
		dleg.setView(view);
		panel.add(dleg, BorderLayout.CENTER);
		panel.setBorder(new CommonBorder("Symbology"));
		return panel;
	}
}
