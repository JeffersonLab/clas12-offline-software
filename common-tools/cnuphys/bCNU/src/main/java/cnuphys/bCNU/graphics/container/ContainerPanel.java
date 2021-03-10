package cnuphys.bCNU.graphics.container;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.CommonToolBar;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.bCNU.util.X11Colors;
import cnuphys.splot.plot.VerticalLabelUI;

/**
 * Place a container on a panel. It will be a viewless container.
 * This will allow us to make a grid of containers on a single view.
 * @author heddle
 *
 */

public class ContainerPanel extends JPanel  {
	// the underlying container
	private BaseContainer _container;

	// title related
	private JLabel _title;
	
	// status label
	private JLabel _status;

	// axes labels
	private JLabel _xLabel;
	private JLabel _yLabel;
	
	//the compass panels
	private JPanel _northPanel;
	private JPanel _southPanel;


	// how adorned
	public static int VERYBARE = -2;
	public static int BARE = 1;
	public static int STANDARD = 0;

	// toolbar
	protected CommonToolBar _toolbar;
	protected int _tbarBits;

	protected int _decorations;
	
	//work point
	private Point2D.Double _wp = new Point2D.Double();
	
	/**
	 * Create a container panel with a [0, 0, 1, 1] world system.
	 * 
	 * @param dataSet   the data set
	 * @param plotTitle the title of the plot
	 */
	public ContainerPanel(int toolbarBits) {
		this(toolbarBits, new Rectangle2D.Double(0, 0, 1, 1));
	}


	/**
	 * Create a container panel
	 * 
	 * @param worldSystem  the world system
	 */
	public ContainerPanel(int toolbarBits, Rectangle2D.Double worldSystem) {
		this(toolbarBits, worldSystem, STANDARD);
	}


	/**
	 * Create a container panel
	 * 
	 * @param worldSystem  the world system
	 * @param decorations
	 */
	public ContainerPanel(int toolbarBits, Rectangle2D.Double worldSystem, int decorations) {
		createContainer(worldSystem);
		_tbarBits = toolbarBits;
		_decorations = decorations;

		setLayout(new BorderLayout(0, 0));

		add(_container, BorderLayout.CENTER);

		addSouth();
		addNorth();
		
		setBackground(Color.white);
		setOpaque(true);
		setBorder(new CommonBorder());

	}
	
	/**
	 * Refresh the container.
	 */
	public void refresh() {
		_container.refresh();
	}
	
	/**
	 * Get the title label
	 * @return the title label object
	 */
	public JLabel getTitle() {
		return _title;
	}
	
	/**
	 * Set all three labels
	 * @param title the title
	 * @param xlabel the x label
	 * @param yLabel the y label
	 */
	public void setLabels(String title, String xlabel, String ylabel) {
		setTitle(title);
		setXLabel(xlabel);
		setYLabel(ylabel);
	}
	
	/**
	 * Set the panel's x axis label
	 * @param xlabel the new title
	 */
	public void setXLabel(String xlabel) {
		if (xlabel == null) {
			if (_xLabel != null) {
				_southPanel.remove(_xLabel);
				_xLabel = null;
				return;
			}
		}

		if (_xLabel == null) {
			JPanel tp = new JPanel();
			tp.setLayout(new FlowLayout(FlowLayout.CENTER));

			tp.setBackground(Color.white);
			tp.setOpaque(true);

			_xLabel = makeLabel(tp, Fonts.defaultFont, Color.white, Color.black);
			_southPanel.add(tp, BorderLayout.NORTH);

		}
		_xLabel.setText(xlabel);
	}

	/**
	 * Set the panel's y axis label
	 * 
	 * @param ylabel the new title
	 */
	public void setYLabel(String ylabel) {
		if (ylabel == null) {
			if (_yLabel != null) {
				remove(_yLabel);
				_yLabel = null;
				return;
			}
		}

		if (_yLabel == null) {
			JPanel wp = new JPanel();
			wp.setOpaque(false);
			wp.setLayout(new BorderLayout());
			_yLabel = makeRotatedLabel(Fonts.defaultFont, Color.white, Color.black);
			
			wp.add(_yLabel, BorderLayout.CENTER);
			wp.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);
			wp.add(Box.createHorizontalStrut(6), BorderLayout.EAST);
			add(wp, BorderLayout.WEST);
		

		}
		_yLabel.setText(ylabel);
	}
	
	/**
	 * Set the panel's title
	 * @param title the new title
	 */
	public void setTitle(String title) {
		
		if (title == null) {
			if (_title != null) {
				_northPanel.remove(_title);
				_title = null;
				return;
			}
		}
		
		if (_title == null) {
			JPanel tp = new JPanel();
			tp.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			tp.setBackground(Color.white);
			tp.setOpaque(true);

			_title = makeLabel(tp, Fonts.defaultLargeFont, Color.white, Color.black);			
			_northPanel.add(tp, BorderLayout.SOUTH);
		}
		_title.setText(title);
	}
	
	//convenience method to make a label
	private JLabel makeLabel(JPanel p, Font font, Color bg, Color fg) {
		JLabel label = new JLabel();
		label.setFont(font);
		
		label.setOpaque(true);
		label.setBackground(bg);
		label.setForeground(fg);
		p.add(label);
		return label;
	}
	
	// convenience function for making a rotated label for y axis label
	private JLabel makeRotatedLabel(Font font, Color bg, Color fg) {
		JLabel lab = new JLabel();
		lab.setLayout(new BorderLayout());
		lab.setFont(font);
		lab.setOpaque(true);
		
		lab.setUI(new VerticalLabelUI());

		if (bg != null) {
			lab.setBackground(bg);
		}
		if (fg != null) {
			lab.setForeground(fg);
		}
		
		return lab;
	}

	
	//create the container
	private void createContainer(Rectangle2D.Double worldSystem) {
		_container = new BaseContainer(null, worldSystem);
		_container.setBorder(new CommonBorder());
		
		_container.setBackground(X11Colors.getX11Color("alice blue"));
		
		MouseInputAdapter mia = new MouseInputAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (_status != null) {
					_status.setText("");
				}
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				if (_status != null) {
					Point pp = e.getPoint();
					_container.localToWorld(pp, _wp);
					
					String s = String.format("local: [%d, %d]  world: [%-5.2f, %-5.2f]", pp.x, pp.y, _wp.x, _wp.y);
					_status.setText(s);
				}
				
			}	
			
			@Override
			public void mouseDragged(MouseEvent e) {
			}			



		};
		
		_container.addMouseListener(mia);
		_container.addMouseMotionListener(mia);

	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
	}

	//add the south component
	private void addSouth() {
		// south panel for x axis and status
		_southPanel = new JPanel();

		_southPanel.setLayout(new BorderLayout());
		_southPanel.setOpaque(true);
		_southPanel.setBackground(Color.white);


		// status label
		if (_decorations == STANDARD) {

			_status = makeStatusLabel(1);

			_status.setAlignmentX(Component.CENTER_ALIGNMENT);
			_southPanel.add(_status, BorderLayout.SOUTH);
		}

		add(_southPanel, BorderLayout.SOUTH);
	}

	// add the north component
	private void addNorth() {
		

		// toolbar
		_toolbar = new BaseToolBar(_container, _tbarBits) {
			@Override
			public void paint(Graphics g) {
				// exclude from print
				if (!PrintUtilities.isPrinting()) {
					super.paint(g);
				}
			}
		};

		_northPanel = new JPanel();
		_northPanel.setOpaque(true);
		_northPanel.setBackground(Color.white);
		Environment.getInstance().commonize(_northPanel, null);
		_northPanel.setLayout(new BorderLayout());

		// if verybare, tool bar is hidden
		if (_decorations != VERYBARE) {
			_northPanel.add(_toolbar, BorderLayout.NORTH);
		}
		add(_northPanel, BorderLayout.NORTH);
	}

	//create the status label
	private JLabel makeStatusLabel(int numLines) {
		Font font = Fonts.mediumFont;
		FontMetrics fm = getFontMetrics(font);
		
		//allow for number of lines
		final int height = 4 + numLines * (fm.getHeight() + 2);
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

		label.setBorder(new CommonBorder());

		return label;
	}
	


	/**
	 * Get the underlying container
	 * 
	 * @return the container
	 */
	public BaseContainer getBaseContainer() {
		return _container;
	}


}