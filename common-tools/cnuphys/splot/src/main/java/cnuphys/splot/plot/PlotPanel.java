package cnuphys.splot.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import cnuphys.splot.toolbar.CommonToolBar;

public class PlotPanel extends JPanel implements PropertyChangeListener {

	// the underlying plot canvas
	private PlotCanvas _canvas;

	// title related
	private JLabel _titleLabel;

	// status label
	private JLabel _status;

	// axes labels
	private JLabel _xLabel;
	private JLabel _yLabel;

	// how adorned
	public static int VERYBARE = -2;
	public static int BARE = 1;
	public static int STANDARD = 0;

	// toolbar
	protected CommonToolBar _toolbar;

	protected int _decorations;

	/**
	 * Create a plot panel for a single xy dataset and a toolbar
	 * 
	 * @param dataSet   the data set
	 * @param plotTitle the title of the plot
	 */
	public PlotPanel(PlotCanvas canvas) {
		this(canvas, STANDARD);
	}

	/**
	 * Create a plot panel for a single xy dataset
	 * 
	 * @param dataSet     the data set
	 * @param plotTitle   the title of the plot
	 * @param decorations (stripped down panel?)
	 */
	public PlotPanel(PlotCanvas canvas, int decorations) {
		_canvas = canvas;
		_canvas.setParent(this);
		_decorations = decorations;

		Environment.getInstance().commonize(this, null);
		setLayout(new BorderLayout(0, 0));

		_canvas.addPropertyChangeListener(this);
		add(_canvas, BorderLayout.CENTER);

		addSouth();
		addNorth();
		addWest();

		if (_decorations == STANDARD) {
			// update the status line with the mouse plot coordinates of the
			// mouse
			MouseMotionAdapter mma = new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent me) {
					if (_status != null) {
						_status.setText(_canvas.getLocationString());
					}
				}
			};

			_canvas.addMouseMotionListener(mma);
		}

	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	private void addSouth() {
		// south panel for x axis and status
		JPanel spanel = new JPanel();
		Environment.getInstance().commonize(spanel, null);

		spanel.setLayout(new BorderLayout());
		spanel.setOpaque(true);
		spanel.setBackground(Color.white);
		PlotParameters parameters = _canvas.getParameters();

		// axes labels
		_xLabel = makeJLabel(parameters.getXLabel(), parameters.getAxesFont(), SwingConstants.CENTER, Color.white, null,
				false);
		_xLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		spanel.add(_xLabel, BorderLayout.CENTER);

		// status label
		if (_decorations == STANDARD) {

			_status = makeStatusLabel();

//			_status = makeJLabel("  ", parameters.getStatusFont(),
//					SwingConstants.CENTER, new Color(240, 240, 240),
//					Color.blue, true);

			_status.setAlignmentX(Component.CENTER_ALIGNMENT);
			spanel.add(_status, BorderLayout.SOUTH);
		}

		add(spanel, BorderLayout.SOUTH);
	}

	// add the north component
	private void addNorth() {
		PlotParameters parameters = _canvas.getParameters();

		// toolbar
		_toolbar = new CommonToolBar(SwingConstants.HORIZONTAL) {
			@Override
			public void paint(Graphics g) {
				// exclude from print
				if (!PrintUtilities.isPrinting()) {
					super.paint(g);
				}
			}
		};
		_toolbar.addToolBarListener(_canvas);

		JPanel npanel = new JPanel();
		npanel.setOpaque(true);
		npanel.setBackground(Color.white);
		Environment.getInstance().commonize(npanel, null);
		npanel.setLayout(new BorderLayout());

		// title label
		_titleLabel = makeJLabel(_canvas.getParameters().getPlotTitle(), parameters.getTitleFont(),
				SwingConstants.CENTER, Color.white, null, false);
		_titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		_toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

		// if verybare, tool bar is hidden
		if (_decorations != VERYBARE) {
			npanel.add(_toolbar, BorderLayout.NORTH);
		}
		npanel.add(_titleLabel, BorderLayout.CENTER);
		add(npanel, BorderLayout.NORTH);
	}

	// add the west component
	private void addWest() {
		PlotParameters parameters = _canvas.getParameters();
		_yLabel = makeRotatedLabel(_canvas.getParameters().getYLabel(), parameters.getAxesFont(), Color.white, null);
		add(_yLabel, BorderLayout.WEST);
	}

	private JLabel makeStatusLabel() {
		Font font = _canvas.getParameters().getStatusFont();
		FontMetrics fm = getFontMetrics(font);
		final int height = 4 + 3 * (fm.getHeight() + 2);
		Color bg = X11Colors.getX11Color("alice blue");

		JLabel label = new JLabel() {
			@Override
			public void paint(Graphics g) {
				// exclude from print
				if (!PrintUtilities.isPrinting()) {
					super.paint(g);
				}
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = height;
				return d;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension d = super.getMinimumSize();
				d.height = height;
				return d;
			}

			@Override
			public Dimension getMaximumSize() {
				Dimension d = super.getMaximumSize();
				d.height = height;
				return d;
			}

		};

		label.setFont(font);
		label.setOpaque(true);
		label.setBackground(bg);
		label.setForeground(Color.black);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.CENTER);

		Border commonBorder = new CommonBorder();
		Border emptyBorder = BorderFactory.createLineBorder(bg, 2);

		label.setBorder(BorderFactory.createCompoundBorder(commonBorder, emptyBorder));

		return label;
	}

	// convenience function for making a label
	private JLabel makeJLabel(String text, Font font, int alignment, Color bg, Color fg, boolean excludeFromPrint) {

		JLabel lab = null;
		if (excludeFromPrint) {
			lab = new JLabel(text != null ? text : " ") {
				@Override
				public void paint(Graphics g) {
					// exclude from print
					if (!PrintUtilities.isPrinting()) {
						super.paint(g);
					}
				}
			};
		}
		else {
			lab = new JLabel(text != null ? text : " ");
		}
		lab.setFont(font);
		lab.setOpaque(true);
		if (bg != null) {
			lab.setBackground(bg);
		}
		if (fg != null) {
			lab.setForeground(fg);
		}
		lab.setHorizontalAlignment(alignment);
		lab.setVerticalAlignment(SwingConstants.CENTER);
		return lab;
	}

	// convenience function for making a rotated label for y axis label
	private JLabel makeRotatedLabel(String text, Font font, Color bg, Color fg) {
		JLabel lab = new JLabel(text);
		lab.setFont(font);
		lab.setOpaque(true);
		lab.setUI(new VerticalLabelUI());
		lab.setHorizontalAlignment(SwingConstants.CENTER);

		if (bg != null) {
			lab.setBackground(bg);
		}
		if (fg != null) {
			lab.setForeground(bg);
		}
		return lab;
	}

	/**
	 * Get the underlying plot canvas
	 * 
	 * @return the plot canvas
	 */
	public PlotCanvas getCanvas() {
		return _canvas;
	}

	/**
	 * Get the plot parameters
	 * 
	 * @return the plot parameters
	 */
	public PlotParameters getParameters() {
		return _canvas.getParameters();
	}

	public void setColor(Color bg) {
		super.setBackground(bg);
		_canvas.setBackground(bg);
		_titleLabel.setBackground(bg);
		_xLabel.setBackground(bg);
		_yLabel.setBackground(bg);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (PlotCanvas.TITLECHANGEPROP.equals(evt.getPropertyName())) {
			_titleLabel.setText((String) evt.getNewValue());
			_titleLabel.repaint();
		}
		else if (PlotCanvas.XLABELCHANGEPROP.equals(evt.getPropertyName())) {
			_xLabel.setText((String) evt.getNewValue());
			_xLabel.repaint();
		}
		else if (PlotCanvas.YLABELCHANGEPROP.equals(evt.getPropertyName())) {
			_yLabel.setText((String) evt.getNewValue());
			_yLabel.repaint();
		}

	}

	/**
	 * Set which toggle button is selected
	 */
	public void setSelectedToggle(String s) {
		if (_toolbar != null) {
			_toolbar.setSelectedToggle(s);
		}
	}

}
