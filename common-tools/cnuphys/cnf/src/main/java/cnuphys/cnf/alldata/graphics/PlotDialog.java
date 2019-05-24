package cnuphys.cnf.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.log.Log;
import cnuphys.cnf.alldata.ColumnData;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.PlotParameters;

public abstract class PlotDialog extends JDialog
		implements ActionListener, IEventListener {

	// plot types
	protected static final String HISTOGRAM = "histogram";
	protected static final String HISTOGRAM2D = "histogram2d";
	protected static final String SCATTERPLOT = "scatterplot";
	
	
	// is the plot accepting new data
	private boolean _acceptingData = true;

	// the name
	protected String _name;

	private static final int width = 650;
	private static final int height = 500;

	// menus
	protected JMenu _fileMenu;
	protected JMenuItem _closeItem;
	protected JMenuItem _deleteItem;
	protected JMenuItem _clearItem;

	// don't print a gazillion error messages
	protected int _errorCount = 0;

	// the plot panel
	protected PlotPanel _plotPanel;

	// cut table
	protected CutTablePanel _cutPanel;

	/**
	 * Create a Plot Dialog
	 * 
	 * @param name the name of the plot
	 */
	public PlotDialog(String name) {
		_name = name;
		setTitle(name);
		setModal(false);
		setSize(width, height);
		setIconImage(ImageManager.cnuIcon.getImage());

		addMenus();

		EventManager.getInstance().addEventListener(this, 2);

		_cutPanel = new CutTablePanel(this);
		add(_cutPanel, BorderLayout.WEST);
	}

	// add the menu
	private void addMenus() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);

		_fileMenu = new JMenu("File");
		_closeItem = addItem(_fileMenu, "Close");
		_clearItem = addItem(_fileMenu, "Clear Data");

		_fileMenu.addSeparator();
		_deleteItem = addItem(_fileMenu, "Delete Plot");

		mbar.add(_fileMenu);
	}

	// add a menu item
	private JMenuItem addItem(JMenu menu, String label) {
		JMenuItem item = new JMenuItem(label);
		menu.add(item);
		item.addActionListener(this);
		return item;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _closeItem) {
			setVisible(false);
		} else if (o == _deleteItem) {
			EventManager.getInstance().removeEventListener(this);

			DefinitionManager.getInstance().remove(_name);
		} else if (o == _clearItem) {
			clear();
		}
	}

	/** Clear all the data */
	protected abstract void clear();

	
	/** refresh the plot */
	public void refresh() {
		_plotPanel.getCanvas().needsRedraw(true);
	}
	
	/**
	 * Opened a new event file
	 * 
	 * @param file the new file
	 */
	@Override
	public void openedNewEventFile(File file) {
		_errorCount = 0;
	}
	
	@Override
	public void newEvent(final DataEvent event, boolean isStreaming) {
		if (_acceptingData) {
			processEvent(event, isStreaming);
		}
	}
	
	public abstract void processEvent(final DataEvent event, boolean isStreaming);
	
	/**
	 * Rewound the current file
	 * @param file the file
	 */
	@Override
	public void rewoundFile(File file) {
		_errorCount = 0;
	}
	
	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
		
	}
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
		refresh();
	}


	protected void warning(String s) {
		_errorCount++;

		if (_errorCount < 10) {
			Log.getInstance().warning(s);
		}
	}

	/**
	 * Add a cut
	 * 
	 * @param cut the cut to add
	 */
	public void addCut(ICut cut) {
		_cutPanel.addCut(cut);
	}

	/**
	 * Get all the defined cuts, active or not
	 * 
	 * @return all the cuts
	 */
	protected Vector<ICut> getCuts() {
		return _cutPanel.getModel()._data;
	}

	/**
	 * Get a string representing the type
	 * 
	 * @return a string representing the type
	 */
	public abstract String getPlotType();

	/**
	 * Get the plot parameters for the underlying plot.
	 * 
	 * @return the plot parameters for the underlying plot.
	 */
	public PlotParameters getParameters() {
		PlotCanvas canvas = getCanvas();
		if (canvas != null) {
			return canvas.getParameters();
		}

		return null;
	}

	/**
	 * Get the plot canvas for the underlying plot.
	 * 
	 * @return the plot canvas for the underlying plot.
	 */
	public PlotCanvas getCanvas() {
		if (_plotPanel != null) {
			return _plotPanel.getCanvas();
		}

		return null;
	}


	/**
	 * Get the effective length of the data
	 * 
	 * @param event the event
	 * @param cd    the column data
	 * @param ne    the named expression
	 * @return the effective length of the data
	 */
	public int getMinLength(DataEvent event, ColumnData cd, NamedExpression ne) {
		int len = 0;
		if (cd != null) {
			double vals[] = cd.getAsDoubleArray(event);
			len = (vals == null) ? 0 : vals.length;
		} // colData != null
		else if (ne != null) {
			len = ne.minLength(event);
		}

		return len;
	}

	/**
	 * Get a value for either the column data or the named expression
	 * 
	 * @param index the index
	 * @param cd    the column data
	 * @param ne    the named expression
	 * @return the value at the index or Double.NaN on error
	 */
	public double getValue(DataEvent event, int index, ColumnData cd, NamedExpression ne) {
		if (index < 0) {
			return Double.NaN;
		}
		if ((cd == null) && (ne == null)) {
			return Double.NaN;
		}

		double val = Double.NaN;

		if (cd != null) {
			double vals[] = cd.getAsDoubleArray(event);
			if ((vals != null) && (index < vals.length)) {
				val = vals[index];
			}
		} else { // expression
			if (ne.readyToCompute()) {
				int len = ne.minLength(event);
				if (index < len) {
					val = ne.value(event, index);
				}
			}
		}

		// cut?
		Vector<ICut> cuts = getCuts();
		if (cuts != null) {
			for (ICut cut : cuts) {
				if (!cut.pass(index)) {
					return Double.NaN;
				}
			}
		}

		return val;
	}

}
