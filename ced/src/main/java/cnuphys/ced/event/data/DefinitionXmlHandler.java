package cnuphys.ced.event.data;

import java.awt.Rectangle;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import cnuphys.splot.pdata.HistoData;

public class DefinitionXmlHandler implements ContentHandler {
	
	//plot creation variables
	private String _plotType;
    private Rectangle _plotBounds;
    private HistoData _histoData;
    Vector<ICut> _cuts = new Vector<ICut>();
 

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
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
	//	System.err.println("XML Start Element [" + localName + "]");
		
		if (localName.equals(PlotDialog.XmlPlot)) {
			startPlot(atts);
		}
		
//		if (atts != null) {
//			int len = atts.getLength();
//			for (int i = 0; i < len; i++) {
//				String aname = atts.getLocalName(i);
//				String val = atts.getValue(i);
//				System.err.println("  " + aname + "=" + val);
//			}
//		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
//		System.err.println("XML End Element [" + localName + "]");
		
		if (localName.equals(PlotDialog.XmlPlot)) {
			createPlot();
		}

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
	
	
        
    /**
     * Signal the creation of a new plot (dialog)
     */
	private void startPlot(Attributes atts) {
		//reset relevant cached elements
		_plotType = null;
		_plotBounds = null;
		_histoData = null;
		_cuts.clear();
		
		//the one attribute should be yhe plot type
		_plotType = atts.getValue("", PlotDialog.XmlType);
		System.err.println("PLOT TYPE: [" + _plotType + "]");
	}
	
	/**
	 * Should have what we need to create the plot
	 */
	private void createPlot() {
		PlotDialog pdialog = null;
		if (_plotType != null) {
			if (PlotDialog.HISTOGRAM.equals(_plotType)) {
				if (_histoData != null) {
					
				}
			}
			else if (PlotDialog.HISTOGRAM2D.equals(_plotType)) {
				
			}
			else if (PlotDialog.SCATTERPLOT.equals(_plotType)) {
				
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

}
