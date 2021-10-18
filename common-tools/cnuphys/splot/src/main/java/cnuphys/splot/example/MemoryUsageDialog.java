package cnuphys.splot.example;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;

import cnuphys.splot.fit.IValueGetter;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.StripData;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.LimitsMethod;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.style.SymbolType;

public class MemoryUsageDialog extends APlotDialog implements IValueGetter {

	private static Font _titleFont = Environment.getInstance().getCommonFont(12);
	private static Font _statusFont = Environment.getInstance().getCommonFont(9);
	private static Font _axesFont = Environment.getInstance().getCommonFont(10);
	private static Font _legendFont = Environment.getInstance().getCommonFont(10);

	public MemoryUsageDialog(JFrame owner) {
		super(owner, "Memory Usage", false, null);
		setSize(500, 400);
	}

	@Override
	protected DataSet createDataSet() throws DataSetException {
		StripData sd = new StripData("Memory", 25, this, 2000);
		return new DataSet(sd, "time", "Memory Usage (MB)");
	}

	@Override
	protected String[] getColumnNames() {
		return null;
	}

	@Override
	protected String getXAxisLabel() {
		return "Time (s)";
	}

	@Override
	protected String getYAxisLabel() {
		return "Memory (MB)";
	}

	@Override
	protected String getPlotTitle() {
		return "Memory Usage (MB)";
	}

	@Override
	public void fillData() {
	}

	@Override
	public void setPreferences() {
		DataSet ds = _canvas.getDataSet();
		ds.getCurveStyle(0).setBorderColor(Color.red);
		ds.getCurveStyle(0).setFillColor(new Color(128, 0, 0, 48));
		ds.getCurveStyle(0).setSymbolType(SymbolType.NOSYMBOL);
		PlotParameters params = _canvas.getParameters();
		params.setTitleFont(_titleFont);
		params.setAxesFont(_axesFont);
		params.setStatusFont(_statusFont);
		params.setStatusFont(_legendFont);
		params.setMinExponentY(3);
		params.setNumDecimalY(0);
		params.setXLimitsMethod(LimitsMethod.USEDATALIMITS);
		params.mustIncludeYZero(true);
	}

	@Override
	public double value(double x) {
		// memory in mb
		return (Runtime.getRuntime().totalMemory()) / 1048576.;
	}

}
