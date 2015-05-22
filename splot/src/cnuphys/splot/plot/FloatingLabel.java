package cnuphys.splot.plot;

import javax.swing.JLabel;

import cnuphys.splot.xml.XmlPrintStreamWritable;
import cnuphys.splot.xml.XmlPrintStreamWriter;

public class FloatingLabel extends JLabel implements XmlPrintStreamWritable {

    // the owner plot panel
    private PlotCanvas _canvas;

    // the plot parameters
    private PlotParameters _params;

    public FloatingLabel(PlotCanvas canvas) {
	_canvas = canvas;
	_params = canvas.getParameters();
	setOpaque(true);
    }

    @Override
    public void writeXml(XmlPrintStreamWriter xmlPrintStreamWriter) {
    }

}
