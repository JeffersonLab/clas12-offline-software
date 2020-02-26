package cnuphys.ced.component;

import java.awt.BorderLayout;
import java.awt.Color;
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
import cnuphys.ced.cedview.central.CentralXYView;
import cnuphys.ced.cedview.central.CentralZView;
import cnuphys.ced.cedview.dcxy.DCXYView;
import cnuphys.ced.cedview.projecteddc.ProjectedDCView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.event.data.DC;
import cnuphys.ced.event.data.DataDrawSupport;
import cnuphys.ced.frame.CedColors;

public class DrawingLegend extends JComponent {
	

	private static final Font labelFont = new Font("SansSerif", Font.PLAIN, 9);
	
	private static final Color bgColor = new Color(120, 120, 120);

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
		g.setColor(bgColor);
		g.fillRect(0, 0, b.width, b.height);

		Point pp = new Point();
		// gemc hit
		int yc = 8;
		int x = 8;
		pp.setLocation(x, yc);
		DataDrawSupport.drawGemcHit(g, pp);
		x += quickString(g, x + 6, yc, "GEMC Hit ") + 20;

		// reconstructed hit
		pp.setLocation(x, yc);
		DataDrawSupport.drawReconHit(g, pp);
		x = quickString(g, x + 6, yc, "Recon Hit ") + 20;
		pp.setLocation(x, yc);
		DataDrawSupport.drawReconCluster(g, pp);
		x = quickString(g, x + 6, yc, "Recon Cluster ") + 20;

		yc += 18;
		x = 8;

		// view dependent drawing
		if (_view != null) {
			if ((_view instanceof SectorView) ||
					(_view instanceof DCXYView) || (_view instanceof ProjectedDCView)) {
				paintForwardViewLegend(g, x, yc);
			}
			
			else if ((_view instanceof CentralXYView) || (_view instanceof CentralZView)) {
				paintCentralViewLegend(g, x, yc);
			}
		}

	}

	//paint the legent for the central 2D views
	private void paintCentralViewLegend(Graphics g, int x, int yc) {
		int xo = x;
		Graphics2D g2 = (Graphics2D) g;
		x = drawCross(g, x, yc, DataDrawSupport.BST_CROSS);
		x = drawCross(g, x, yc, DataDrawSupport.BMT_CROSS);
		
		yc += 18;
		SymbolDraw.drawUpTriangle(g, xo, yc, 3, 
				X11Colors.getX11Color("Dark Green"), X11Colors.getX11Color("Aquamarine"));

		quickString(g, xo+16, yc, "hit strip midpoint");
		
		//tracks
		yc += 18;
		x = xo;
		x = quickString(g, xo, yc, "Tracks   ");
		x = drawLine(g2, x, yc, DC.HB_COLOR, "HB ");
		x = drawLine(g2, x, yc, DC.TB_COLOR, "TB ");
		x = drawLine(g2, x, yc, CedColors.cvtTrackColor, "CVT ");

	}

	//paint the legend for sector views
	private void paintForwardViewLegend(Graphics g, int x, int yc) {
		
		int xo = x;
		Graphics2D g2 = (Graphics2D) g;
		x = drawCross(g, x, yc, DataDrawSupport.HB_CROSS);
		x = drawCross(g, x, yc, DataDrawSupport.TB_CROSS);
		x = drawCross(g, x, yc, DataDrawSupport.FMT_CROSS);
		x = drawCircle(g, x, yc, CedColors.docaTruthLine, "TB Doca");
		yc += 18;
		
		
		//segment lines
		x = xo;
		x = drawSegLine(g2, x, yc, CedColors.hbSegmentLine, DC.HB_COLOR, "HB Segment");
		x = drawSegLine(g2, x, yc, CedColors.tbSegmentLine, DC.TB_COLOR, "TB Segment");
		
		//tracks
		yc += 18;
		x = xo;
		x = quickString(g, xo, yc, "Tracks   ");
		x = drawLine(g2, x, yc, DC.HB_COLOR, "HB ");
		x = drawLine(g2, x, yc, DC.TB_COLOR, "TB ");
		x = drawLine(g2, x, yc, CedColors.cvtTrackColor, "CVT ");

	}
	
	private int drawLine(Graphics2D g2, int x, int yc, Color lineColor, String str) {
		g2.setColor(CedColors.docaTruthFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g2.drawLine(x, yc, x+26, yc);
		g2.setColor(lineColor);
		g2.setStroke(GraphicsUtilities.getStroke(2f, LineStyle.SOLID));
		g2.drawLine(x, yc, x+26, yc);
		
		x += 36;
		return quickString(g2, x, yc-2, str) + 18;
	}

	
	private int drawSegLine(Graphics2D g2, int x, int yc, Color lineColor, Color endColor, String str) {
		g2.setColor(CedColors.docaTruthFill);
		g2.setStroke(GraphicsUtilities.getStroke(6f, LineStyle.SOLID));
		g2.drawLine(x, yc, x+30, yc);
		g2.setColor(lineColor);
		g2.setStroke(GraphicsUtilities.getStroke(2f, LineStyle.SOLID));
		g2.drawLine(x, yc, x+30, yc);
		
		SymbolDraw.drawOval(g2, x, yc, 2, 2, endColor, endColor);
		SymbolDraw.drawOval(g2, x+30, yc, 2, 2, endColor, endColor);
		x += 40;
		return quickString(g2, x, yc-2, str) + 18;
	}

	private int drawCross(Graphics g, int x, int y, int mode) {
		DataDrawSupport.drawCross(g, x, y, mode);
		
		x += (2*DataDrawSupport.CROSSHALF);
		String s = DataDrawSupport.prefix[mode] + "cross";
		return quickString(g, x, y, s) + 14;
	}
	
	private int drawCircle(Graphics g, int x, int y, Color color, String s) {
		SymbolDraw.drawOval(g, x, y, DataDrawSupport.CROSSHALF, DataDrawSupport.CROSSHALF, color, Color.black);
		x += (2*DataDrawSupport.CROSSHALF);
		return quickString(g, x, y, s) + 16;
	}


	//draw a string, returns the x position plus the string width
	private int quickString(Graphics g, int x, int yc, String s) {
		FontMetrics fm = getFontMetrics(labelFont);
		g.setColor(Color.black);
		g.setFont(labelFont);
		g.setColor(Color.cyan);
		g.drawString(s, x, yc + fm.getAscent() / 2);
		return x + fm.stringWidth(s);
	}

//	@Override
//	public Dimension getPreferredSize() {
//		return new Dimension(width, height);
//	}

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
