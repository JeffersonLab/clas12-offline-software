package cnuphys.tinyMS.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JComponent;

import cnuphys.tinyMS.Environment.DoubleFormat;
import cnuphys.tinyMS.server.TinyMessageServer;

public class MemoryStripChart extends JComponent {

	private static int RANGE = 600; // full range in seconds

	private static int INTERVAL = 10; // update interval in seconds

	private static int PREF_WIDTH = 340;

	private static Color BACKGROUND = X11Colors.getX11Color("Alice Blue");

	private static final Font FONT = Fonts.smallFont;

	private int _timerUpdateIndex = 0;

	private static final Color rectColors[] = { new Color(180, 180, 180), new Color(160, 160, 160),
			new Color(140, 140, 140), new Color(160, 160, 160) };

	// maintenance timer
	private Timer _timer;

	// the server
	private TinyMessageServer _server;

	// data
//	private float _maxVal = 1.0f; // MB
	private int _capacity = 1 + (RANGE / INTERVAL);
	private Vector<Float> _data = new Vector<Float>(_capacity);

	public MemoryStripChart(TinyMessageServer server) {
		_server = server;
		// update timer
		setupUpdateTimer();

		setBackground(BACKGROUND);
		setOpaque(true);

		setBorder(new CommonBorder("memory usage (MB) last " + (RANGE / 60) + " minutes"));
	}

	private void setupUpdateTimer() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				update();
			}

		};
		_timer = new Timer();
		_timer.scheduleAtFixedRate(task, 10000, 1000 * INTERVAL);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = PREF_WIDTH;
		return d;
	}

	// get max val of current data
	private float getMaxVal() {
		float mv = 2;

		try {
			synchronized (_data) {
				for (float val : _data) {
					mv = Math.max(mv, val);
				}
			}
		} catch (Exception e) {
		}

		return mv;
	}

	// update the chart
	private void update() {
		_timerUpdateIndex++;

		if (_server.isShutDown()) {
			_timer.cancel();
			return;
		}

		float used = memoryReport();
		
		if (_data.size() >= _capacity) {
			_data.remove(0);
		}
		_data.add(used);

		float maxVal = getMaxVal();
		float delta = used - maxVal;

		// repaint the chart
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle b = getBounds();
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, b.width, b.height);

		// max value of chart
		double maxPlotVal = Math.pow(2, Math.floor(log2(getMaxVal())) + 1);

		// plotrect
		String maxstr = DoubleFormat.doubleFormat(maxPlotVal, 1);
		FontMetrics fm = getFontMetrics(FONT);
		int sw = fm.stringWidth(maxstr);
		int hgap = sw + 8;
		int vgap = 8;

		Rectangle plotRect = new Rectangle(b.x + hgap, b.y + vgap, b.width - hgap - 10, b.height - 2 * vgap - 4);
		g.setColor(Color.white);
		g.fillRect(plotRect.x, plotRect.y, plotRect.width, plotRect.height);

		g.setColor(Color.black);
		g.setFont(FONT);
		g.drawString(maxstr, b.x + 4, plotRect.y + fm.getHeight());

		Rectangle dataRect = new Rectangle();
//		int numData = _data.size();
		double delX = plotRect.getWidth() / _capacity;
		double right = plotRect.getX();
		// now the data
		for (int i = 0; i < _data.size(); i++) {
			int x = (int)right;
			right += delX;
			float val = _data.get(i);
			int h = (int) (plotRect.height * (val / maxPlotVal));
			dataRect.setBounds(x, plotRect.y + plotRect.height - h, (int)(right-x), h);

			g.setColor(rectColors[(i + _timerUpdateIndex) % 4]);
			g.fillRect(dataRect.x, dataRect.y, dataRect.width, dataRect.height);
			g.setColor(Color.blue);
			g.drawRect(dataRect.x, dataRect.y, dataRect.width, dataRect.height);

		}

		g.setColor(Color.red);
		g.drawRect(plotRect.x, plotRect.y, plotRect.width, plotRect.height);

	}

	// for plotting
	private double log2(double x) {
		return Math.log10(x) / Math.log10(2.);
	}

	/**
	 * Get the used memory in MB
	 * 
	 * @param message
	 *            a message to add on
	 */
	public float memoryReport() {
		System.gc();

		StringBuilder sb = new StringBuilder(1024);
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		float used = (float) (total - free);

		return used;
	}

}
