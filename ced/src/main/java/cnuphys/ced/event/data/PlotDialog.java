package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.xml.stream.XMLStreamException;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.xml.XmlPrintStreamWritable;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;
import cnuphys.bCNU.xml.XmlSupport;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.PlotParameters;

public abstract class PlotDialog extends JDialog implements ActionListener, IAccumulationListener, IClasIoEventListener, XmlPrintStreamWritable {
	
	//XML elements (upper case)
	public static final String XmlBounds = "BOUNDS";
	public static final String XmlHistoData = "HISTODATA";
	public static final String XmlPlot = "PLOT";
	public static final String XmlRangeCut = "RANGECUT";
	
	
	//XML attributes (lower case)
	public static final String XmlActive = "active";
	public static final String XmlCount = "count";
	public static final String XmlMin = "min";
	public static final String XmlMax = "max";
	public static final String XmlName = "name";
	public static final String XmlType = "type";
		
	//plot types
	protected static final String HISTOGRAM = "histogram";
	protected static final String HISTOGRAM2D = "histogram2d";
	protected static final String SCATTERPLOT = "scatterplot";
	
	//the name
	protected String _name;
	
	private static final int width = 650;
	private static final int height = 500;
		
	//menus
	protected JMenu _fileMenu;	
	protected JMenuItem _saveItem;
	protected JMenuItem _closeItem;
	protected JMenuItem _deleteItem;
	protected JMenuItem _clearItem;
	
	//don't print a gazillion error messages
	protected int _errorCount = 0;
	
	// the plot panel
	protected PlotPanel _plotPanel;
	
	//cut table
	protected CutTablePanel _cutPanel;

	/**
	 * Create a Plot Dialog
	 * @param name the name of the plot
	 */
	public PlotDialog(String name) {
		_name = name;
		setTitle(name);
		setModal(false);
		setSize(width, height);
		
		addMenus();
		
		AccumulationManager.getInstance().addAccumulationListener(this);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);

		_cutPanel = new CutTablePanel(this);
		add(_cutPanel, BorderLayout.WEST);
	}
	
	//add the menu
	private void addMenus() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);
		
		_fileMenu = new JMenu("File");
		_saveItem = addItem(_fileMenu, "Save Definition...");
		_closeItem = addItem(_fileMenu, "Close");
		_clearItem = addItem(_fileMenu, "Clear Data");
		
		_fileMenu.addSeparator();
		_deleteItem = addItem(_fileMenu, "Delete Plot");
		
		mbar.add(_fileMenu);
	}
	
	//add a menu item
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
		}
		else if (o == _deleteItem) {
			AccumulationManager.getInstance().removeAccumulationListener(this);
			ClasIoEventManager.getInstance().removeClasIoEventListener(this);

			DefinitionManager.getInstance().remove(_name);
		}
		else if (o == _clearItem) {
			clear();
		}
		else if (o == _saveItem) { //save the plot definition?
			XmlSupport.save(this);
		}

	}
	
	/** Clear all the data */
	protected abstract void clear();	
	
	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			_errorCount = 0;
			break;

		case AccumulationManager.ACCUMULATION_CLEAR:
			clear();
			break;


		case AccumulationManager.ACCUMULATION_CANCELLED:
		case AccumulationManager.ACCUMULATION_FINISHED:
			_plotPanel.getCanvas().needsRedraw(true);
			break;
		}

	}

	@Override
	public void openedNewEventFile(String path) {
		_errorCount = 0;
	}

	protected void warning(String s) {
		_errorCount++;
		
		if (_errorCount < 10) {
			Log.getInstance().warning(s);
		}
	}
	
	/**
	 * Add a cut
	 * @param cut the cut to add
	 */
	public void addCut(ICut cut) {
		_cutPanel.addCut(cut);
	}
	
	/**
	 * Get all the defined cuts, active or not
	 * @return all the cuts
	 */
	protected Vector<ICut> getCuts() {
		return _cutPanel.getModel()._data;
	}
	
	/**
	 * Get a string representing the type
	 * @return a string representing the type
	 */
	public abstract String getPlotType();
	    
    
    /**
     * Get the plot parameters for the underlying plot.
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
     * @return the plot canvas for the underlying plot.
     */
    public PlotCanvas getCanvas() {
    	if (_plotPanel != null) {
    		return _plotPanel.getCanvas();
    	}
    	
    	return null;
    }
    
    /**
     * Write out the cuts of the plot dialog
     * @param xmlPrintStreamWriter the writer
     */
   protected void writeCuts(XmlPrintStreamWriter xmlPrintStreamWriter) {
		Vector<ICut> cuts = getCuts();
		if ((cuts != null) && !cuts.isEmpty()) {
			for (ICut cut:cuts) {
				cut.writeXml(xmlPrintStreamWriter);
			}
		}
  }
    
    /**
     * Write out the bounds of the plot dialog
     * @param xmlPrintStreamWriter the writer
     */
    protected void writeBounds(XmlPrintStreamWriter xmlPrintStreamWriter) {
		Properties props = new Properties();
		XmlSupport.addRectangleAttribute(props, getBounds());
		try {
			xmlPrintStreamWriter.writeElementWithProps(XmlBounds, props);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Write out histo data
     * @param xmlPrintStreamWriter
     * @param hd the histo data
     */
    protected void writeHistoData(XmlPrintStreamWriter xmlPrintStreamWriter,
    		HistoData hd) {
		Properties props = new Properties();
		props.put(XmlMin, hd.getMinX());
		props.put(XmlMax, hd.getMaxX());
		props.put(XmlCount, hd.getNumberBins());
		try {
			xmlPrintStreamWriter.writeElementWithProps(XmlHistoData, props);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
    }
    
	@Override
	public void writeXml(XmlPrintStreamWriter writer) {
		

		try {
			writer.writeStartElement(XmlPlot);
			writer.writeAttribute(XmlType, getPlotType());
			writer.closeBracket();
			
			writeBounds(writer);
			writeCuts(writer);
			customXml(writer);
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write out plot specific xml
	 * @param writer the writer
	 */
	public abstract void customXml(XmlPrintStreamWriter writer);

	
}
