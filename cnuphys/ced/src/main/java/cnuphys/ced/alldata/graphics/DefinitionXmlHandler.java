package cnuphys.ced.alldata.graphics;

import java.awt.Rectangle;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import cnuphys.bCNU.xml.XmlSupport;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.pdata.Histo2DData;
import cnuphys.splot.pdata.HistoData;

public class DefinitionXmlHandler implements ContentHandler {
	
	//plot creation variables
	private String _plotType;
    private Rectangle _plotBounds;
    private HistoData _histoData;
    private Histo2DData _histoData2D;
   private DataSet _dataSetXYXY;
    
    Vector<ICut> _cuts = new Vector<ICut>();
 

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		if (localName.equals(XmlUtilities.XmlPlot)) {
			startPlot(atts);
		}
		else if (localName.equals(XmlUtilities.XmlHistoData)) {
			readHistogramData(atts);
		}
		else if (localName.equals(XmlUtilities.XmlHistoData2D)) {
			readHistogramData2D(atts);
		}
		else if (localName.equals(XmlUtilities.XmlRangeCut)) {
			readRangeCut(atts);
		}
		else if (localName.equals(XmlUtilities.XmlBounds)) {
			readBounds(atts);
		}
		else if (localName.equals(XmlUtilities.XmlBinding)) {
			readBinding(atts);
		}
		else if (localName.equals(XmlUtilities.XmlExpression)) {
			readExpression(atts);
		}
		else if (localName.equals(XmlUtilities.XmlDataSetXYXY)) {
			readDataSetXYXY(atts);
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		if (localName.equals(XmlUtilities.XmlPlot)) {
			createPlot();
		}
	}

	//read a double from an attribute collection
	private double getDouble(Attributes atts, String tag) {
		String s = atts.getValue("", tag);
		if (s != null) {
			return Double.parseDouble(s);
		}
		else {
			return Double.NaN;
		}
	}

	//read an int from an attribute collection
	//return -1 on error
	private int getInt(Attributes atts, String tag) {
		String s = atts.getValue("", tag);
		if (s != null) {
			return Integer.parseInt(s);
		}
		else {
			return -1;
		}
	}
	
	//read a boolean from an attribute collection
	private boolean getBoolean(Attributes atts, String tag) {
		String s = atts.getValue("", tag);
		if (s != null) {
			return Boolean.parseBoolean(s);
		}
		else {
			return false;
		}
	}
	
	//read the scatter plot data set
	private void readDataSetXYXY(Attributes atts) {
		String xname = atts.getValue("", XmlUtilities.XmlNameX);
		String yname = atts.getValue("", XmlUtilities.XmlNameY);
		String colNames[]= {xname, yname};
		try {
			_dataSetXYXY = new DataSet(DataSetType.XYXY, colNames);
		} catch (DataSetException e) {
			e.printStackTrace();
		}
	}

	//name binding
	private void readBinding(Attributes atts) {
		String name = atts.getValue("", XmlUtilities.XmlName);
		String column = atts.getValue("", XmlUtilities.XmlBankColumn);
		DefinitionManager.getInstance().addBinding(name, column);
	}
	
	//named expression
	private void readExpression(Attributes atts) {
		String name = atts.getValue("", XmlUtilities.XmlName);
		String definition = atts.getValue("", XmlUtilities.XmlDefinition);
		DefinitionManager.getInstance().addExpression(name, definition);
	}
	
	//plot bounds
	private void readBounds(Attributes atts) {
		int left = getInt(atts, XmlSupport.XmlLeftAttName);
		int top = getInt(atts, XmlSupport.XmlTopAttName);
		int width = getInt(atts, XmlSupport.XmlWidthAttName);
		int height = getInt(atts, XmlSupport.XmlHeightAttName);
		_plotBounds = new Rectangle(left, top, width, height);
	}
	
	//load the 1D histogram data
	private void readHistogramData(Attributes atts) {
		String name = atts.getValue("", XmlUtilities.XmlName);
		double min = getDouble(atts, XmlUtilities.XmlMin);
		double max = getDouble(atts, XmlUtilities.XmlMax);
		int binCount = getInt(atts, XmlUtilities.XmlCount);
		_histoData = new HistoData(name, min, max, binCount);
	}

	
	//load the 2D histogram data
	private void readHistogramData2D(Attributes atts) {
		String name = atts.getValue("", XmlUtilities.XmlName);
		String xname = atts.getValue("", XmlUtilities.XmlNameX);
		String yname = atts.getValue("", XmlUtilities.XmlNameY);
		double xmin = getDouble(atts, XmlUtilities.XmlMinX);
		double xmax = getDouble(atts, XmlUtilities.XmlMaxX);
		int xbinCount = getInt(atts, XmlUtilities.XmlCountX);
		double ymin = getDouble(atts, XmlUtilities.XmlMinY);
		double ymax = getDouble(atts, XmlUtilities.XmlMaxY);
		int ybinCount = getInt(atts, XmlUtilities.XmlCountY);
		_histoData2D = new Histo2DData(name, xname, yname, xmin, xmax, xbinCount, ymin, ymax, ybinCount);
	}
	
	//read a range cut
	private void readRangeCut(Attributes atts) {
		String name = atts.getValue("", XmlUtilities.XmlName);
		double min = getDouble(atts, XmlUtilities.XmlMin);
		double max = getDouble(atts, XmlUtilities.XmlMax);
		boolean active = getBoolean(atts, XmlUtilities.XmlActive);

		RangeCut cut = new RangeCut(name, min, max);
		cut.setActive(active);
		_cuts.add(cut);
	}
        
    /**
     * Signal the creation of a new plot (dialog)
     */
	private void startPlot(Attributes atts) {
		//reset relevant cached elements
		_plotType = null;
		_plotBounds = null;
		_histoData = null;
		_histoData2D = null;
		_dataSetXYXY = null;
		_cuts.clear();
		
		//the one attribute should be yhe plot type
		_plotType = atts.getValue("", XmlUtilities.XmlType);
	}
	
	/**
	 * Should have what we need to create the plot
	 */
	private void createPlot() {
		PlotDialog pdialog = null;
		if (_plotType != null) {
			if (PlotDialog.HISTOGRAM.equals(_plotType)) {
				if (_histoData != null) {
					pdialog = DefinitionManager.getInstance().addHistogram(_histoData);
				}
			}
			else if (PlotDialog.HISTOGRAM2D.equals(_plotType)) {
				if (_histoData2D != null) {
					pdialog = DefinitionManager.getInstance().addHistogram2D(_histoData2D);
				}
			}
			else if (PlotDialog.SCATTERPLOT.equals(_plotType)) {
				if (_dataSetXYXY != null) {
					pdialog = DefinitionManager.getInstance().addScatterPlot(_dataSetXYXY);
				}
			}
		}
		
		//cuts?
		if ((pdialog != null) && (!_cuts.isEmpty())) {
			for (ICut cut : _cuts) {
				pdialog.addCut(cut);
			}
		}
		
		//bounds?
		if ((pdialog != null) && (_plotBounds != null)) {
			pdialog.setBounds(_plotBounds);
		}
	}
	
	

	@Override
	public void setDocumentLocator(Locator locator) {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
	}

}
