package cnuphys.ced.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.cedview.sectorview.SectorView;
import cnuphys.ced.event.FeedbackRect;
import cnuphys.ced.event.data.DataDrawSupport;

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
	    if (_view instanceof SectorView) {
		Dimension d = paintSectorViewLegend(g, x, yc);
	    }
	}

    }

    private Dimension paintSectorViewLegend(Graphics g, int x, int yc) {

	int xo = x;
	int yo = yc;

	// hit based dc
	FeedbackRect fbr = new FeedbackRect(x - 4, yc - 4, 8, 8, 0, null, 0);
	DataDrawSupport.drawCircleCross(g, fbr, Color.gray,
		DataDrawSupport.DC_HB_COLOR);
	x += quickString(g, x + 6, yc, "DC Hit Based ") + 20;

	fbr = new FeedbackRect(x - 4, yc - 4, 8, 8, 0, null, 0);
	DataDrawSupport.drawCircleCross(g, fbr, Color.gray,
		DataDrawSupport.DC_TB_COLOR);
	x += quickString(g, x + 6, yc, "DC Time Based ") + 20;

	return new Dimension(x - xo, yc - yo);
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
