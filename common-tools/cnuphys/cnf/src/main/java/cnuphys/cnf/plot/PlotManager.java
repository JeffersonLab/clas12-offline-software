package cnuphys.cnf.plot;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.drawable.DrawableAdapter;
import cnuphys.bCNU.graphics.colorscale.ColorScaleModel;
import cnuphys.bCNU.graphics.container.BaseContainer;
import cnuphys.bCNU.graphics.container.ContainerPanel;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;
import cnuphys.cnf.stream.DataRanges;
import cnuphys.cnf.stream.StreamManager;

public class PlotManager implements IEventListener {

	// the singleton
	private static PlotManager _instance;

	// plot view
	private DefGridView _view;

	private static final float TINY = (float) 1.0e-5;
	// menu related
	private JMenuBar _menuBar;
	private JMenu _plotMenu;
	private JMenuItem _clearItem;

	// color scales
	private ColorScaleModel _redModel = ColorScaleModel.createColorModel(0, 1, 100, new Color(255, 250, 250),
			Color.red);
	private ColorScaleModel _blueModel = ColorScaleModel.createColorModel(0, 1, 100, new Color(250, 250, 255),
			Color.blue);

	// private constructor
	private PlotManager() {
		EventManager.getInstance().addEventListener(this, 2);
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the PlotManager
	 */
	public static PlotManager getInstance() {
		if (_instance == null) {
			_instance = new PlotManager();
		}

		return _instance;
	}

	// inside or outside test
	public boolean inside(float[] a) {

		float dot = 0;
		for (int i = 0; i < 3; i++) {
			dot += a[i] * a[i + 3];
		}
		return (dot > 0); // outside
	}


	// set the plot limits
	private void setPlotLimits() {
		BaseContainer container;
		double pad = 1.05;
		double w, h, w2, h2;

		// plot (0, 0) x vs y
		container = _view.getContainer(0, 0);
		
		DataRanges dataRanges = StreamManager.getInstance().getDataRanges();
		double xc = (dataRanges.xmin + dataRanges.xmax) / 2;
		double yc = (dataRanges.ymin + dataRanges.ymax) / 2;
		w = pad * (dataRanges.xmax - dataRanges.xmin);
		h = pad * (dataRanges.ymax - dataRanges.ymin);
		w2 = w / 2;
		h2 = h / 2;
		container.reworld(xc - w2, xc + w2, yc - h2, yc + h2);

		// plot (0, 1) r vs theta (deg)
		container = _view.getContainer(0, 1);
		container.reworld(0, 180, 0, dataRanges.rmax);

		// plot (1, 0) r vs phi (deg)
		container = _view.getContainer(1, 0);
		container.reworld(-180, 180, 0, dataRanges.rmax);

	}

	/**
	 * Get the grid view with a grid of plots
	 * 
	 * @return the grid view
	 */
	public DefGridView getDefGridView() {
		if (_view == null) {

			int nrow = 2;
			int ncol = 4;

			_view = DefGridView.createDefGridView("Plots", nrow, ncol, 0.7);

			for (int row = 0; row < nrow; row++) {
				for (int col = 0; col < ncol; col++) {
					ContainerPanel panel = _view.getContainerPanel(row, col);
					new CellDrawer(panel, row, col);
				}

			}

			_menuBar = new JMenuBar();
			_view.setJMenuBar(_menuBar);
			addMenu();
			setupPlots();
		}

		return _view;
	}

	// setup the plots
	private void setupPlots() {
		_view.setLabels(0, 0, "Force Magnitude (z = 0)", "x (fm)", "y (fm)");
		_view.setLabels(0, 1, "Force Magnitude (phi = 0)", "theta (deg)", "r (fm)");
		_view.setLabels(1, 0, "Force Magnitude (z = 0)", "phi (deg)", "r (fm)");

	}

	// add the plots menu
	private void addMenu() {
		_plotMenu = new JMenu("Plots");

		_clearItem = new JMenuItem("Clear all plots");
		_clearItem.addActionListener(event -> clear());
		_plotMenu.add(_clearItem);

		_view.getJMenuBar().add(_plotMenu);
	}
	
	private void clear() {
		StreamManager.getInstance().clear();
		_view.refresh();
	}

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
	}

	@Override
	public void openedNewEventFile(File file) {
	}

	@Override
	public void rewoundFile(File file) {
	}

	@Override
	public void streamingStarted(File file, int numToStream) {		System.err.println("Streaming Started [" + file.getPath() + "] num: " + numToStream);
	}

	@Override
	public void streamingEnded(File file, int reason) {
		setPlotLimits();
		_view.refresh();
	}

	// are two values essentially the same
	private boolean sameValue(float v1, float v2) {
		return Math.abs(v1 - v2) < TINY;
	}

	// are two values essentially the same
	private boolean sameValue(double v1, double v2) {
		return Math.abs(v1 - v2) < TINY;
	}

	class CellDrawer extends DrawableAdapter {

		/** 0-based row */
		public int row;

		/** 0-based column */
		public int column;

		private ContainerPanel _panel;

		public CellDrawer(ContainerPanel panel, int row, int column) {
			this.row = row;
			this.column = column;
			_panel = panel;

			_panel.getBaseContainer().noModel(this);
		}

		@Override
		public void draw(Graphics g, IContainer container) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			BaseContainer bc = (BaseContainer) container;

			Rectangle bounds = bc.getBounds();
			g.setColor(X11Colors.getX11Color("alice blue"));

			g.fillRect(0, 0, bounds.width, bounds.height);

			int count = StreamManager.getInstance().count();

			if (count == 0) {
				return;
			}

			Point2D.Double wp = new Point2D.Double();
			Point pp = new Point();
			
			DataRanges dataRanges = StreamManager.getInstance().getDataRanges();

			for (int index = 0; index < count; index++) {
				
				
				float data[] = StreamManager.getInstance().getDataRow(index);


				if ((row == 0) && (column == 0)) {
					if (sameValue(data[StreamManager.Z], 0)) {
						wp.setLocation(data[StreamManager.X], data[StreamManager.Y]);
						bc.worldToLocal(pp, wp);
						double mag = data[StreamManager.BMAG];
						double fract = mag / dataRanges.bmax;

						Color color;
						if (inside(data)) {
							color = _redModel.getColor(fract);
						} else {
							color = _blueModel.getColor(fract);
						}

						g.setColor(color);

						g.fillRect(pp.x - 2, pp.y - 2, 4, 4);
					}
				}

				else if ((row == 0) && (column == 1)) {
					double phi = Math.toDegrees(Math.atan2(data[StreamManager.Y], data[StreamManager.X]));
					if (sameValue(phi, 0) || sameValue(phi, 360)) {
						double theta = Math.toDegrees(Math.acos(data[StreamManager.Z] / data[StreamManager.R]));

						wp.setLocation(theta, data[StreamManager.R]);
						bc.worldToLocal(pp, wp);
						double mag = data[StreamManager.BMAG];
						double fract = mag / dataRanges.bmax;

						Color color;
						if (inside(data)) {
							color = _redModel.getColor(fract);
						} else {
							color = _blueModel.getColor(fract);
						}

						g.setColor(color);

						g.fillRect(pp.x - 2, pp.y - 2, 4, 4);

					}
				}

				else if ((row == 1) && (column == 0)) {
					if (sameValue(data[StreamManager.Z], 0)) {
						double phi = Math.toDegrees(Math.atan2(data[StreamManager.Y], data[StreamManager.X]));

						wp.setLocation(phi, data[StreamManager.R]);
						bc.worldToLocal(pp, wp);
						double mag = data[StreamManager.BMAG];
						double fract = mag / dataRanges.bmax;

						Color color;
						if (inside(data)) {
							color = _redModel.getColor(fract);
						} else {
							color = _blueModel.getColor(fract);
						}

						g.setColor(color);

						g.fillRect(pp.x - 2, pp.y - 2, 4, 4);

					}

				}

			}

			// draw axis values
			g.setColor(Color.black);
			g.setFont(Fonts.smallMono);
			int numtick = 5;
			Rectangle2D.Double wr = bc.getWorldSystem();
			double dx = wr.width / (numtick + 1);
			double dy = wr.height / (numtick + 1);

			FontMetrics fm = bc.getFontMetrics(Fonts.smallMono);

			for (int i = 0; i < numtick; i++) {
				double yt = wr.y + (i + 1) * dy;
				wp.setLocation(wr.x, yt);
				bc.worldToLocal(pp, wp);
				g.drawLine(pp.x, pp.y, pp.x + 4, pp.y);

				String vstr = String.format("%-6.2f", yt);
				g.setColor(Color.white);
				g.drawString(vstr, pp.x + 5, pp.y + fm.getHeight() / 2 - 1);
				g.setColor(Color.black);
				g.drawString(vstr, pp.x + 6, pp.y + fm.getHeight() / 2);
			}

			for (int i = 0; i < numtick; i++) {
				double xt = wr.x + (i + 1) * dx;
				wp.setLocation(xt, wr.y);
				bc.worldToLocal(pp, wp);
				g.drawLine(pp.x, pp.y, pp.x, pp.y - 4);

				String vstr = String.format("%-6.2f", xt);
				g.setColor(Color.white);
				g.drawString(vstr, pp.x - fm.stringWidth(vstr) / 2 - 1, pp.y - 7);
				g.setColor(Color.black);
				g.drawString(vstr, pp.x - fm.stringWidth(vstr) / 2, pp.y - 6);

			}

		}

	}

}
