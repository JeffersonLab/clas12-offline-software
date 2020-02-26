package cnuphys.fastMCed.consumers;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Properties;

import javax.swing.JFrame;

import cnuphys.splot.example.APlotDialog;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.PlotParameters;

public class ResolutionHisto extends APlotDialog {
	
	public static int DEL = -40;
	
	public static final String TITLE = "title";
	public static final String XLABEL = "xlabel";
	public static final String YLABEL = "ylabel";
	public static final String DATAMIN = "datamin";
	public static final String DATAMAX = "datamax";
	public static final String NUMBIN = "numbin";
	
	public ResolutionHisto(JFrame parent, Properties properties) {
		super(parent, "", false, properties);
		setSize(500, 500);
		Rectangle b = getBounds();
		b.x += DEL;
		b.y += DEL;
		setBounds(b);
		
		DEL += 40;
	}

	@Override
	protected DataSet createDataSet() throws DataSetException {
		double vmin = Double.parseDouble(_properties.getProperty(DATAMIN));
		double vmax = Double.parseDouble(_properties.getProperty(DATAMAX));
		int nbin = Integer.parseInt(_properties.getProperty(NUMBIN));
		HistoData h1 = new HistoData("Histo 1", vmin, vmax, nbin);
		return new DataSet(h1);
	}

	@Override
	protected String[] getColumnNames() {
		return null;
	}

	@Override
	protected String getXAxisLabel() {
		return _properties.getProperty(XLABEL);
	}

	@Override
	protected String getYAxisLabel() {
		return _properties.getProperty(YLABEL);
	}

	@Override
	protected String getPlotTitle() {
		return _properties.getProperty(TITLE);
	}

	@Override
	public void fillData() {
	}
	
	public DataSet getDataSet() {
		DataSet ds = _canvas.getDataSet();
		return ds;
	}
	
	public void clearData() {
		DataSet ds = getDataSet();
		ds.clear();
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setFillColor(new Color(196, 196, 196, 64));
		ds.getCurveStyle(0).setLineColor(Color.black);
		ds.getCurveStyle(0).setLineWidth(2f);
		ds.getCurve(0).getFit().setFitType(FitType.GAUSSIANS);
		PlotParameters params = _canvas.getParameters();
		params.setMinExponentY(6);
		params.setNumDecimalY(0);
		
	}

}
