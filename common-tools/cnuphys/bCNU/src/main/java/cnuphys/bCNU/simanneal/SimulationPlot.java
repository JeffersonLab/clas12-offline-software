package cnuphys.bCNU.simanneal;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.management.modelmbean.InvalidTargetObjectTypeException;

import cnuphys.bCNU.attributes.Attributes;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotPanel;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.plot.PlotTicks;
import cnuphys.splot.style.SymbolType;

public class SimulationPlot extends PlotPanel implements IUpdateListener {

	//the simulation
	private Simulation _simulation;
	
	//the plot canvas
	private PlotCanvas _plotCanvas;
	
	//the data set
	private DataSet _dataSet;
	
	//the plot parameters
	private PlotParameters _plotParameters;
	
	//use the log of the temperature?
	private boolean _useLogT;
	
	// a panel for the plot
	public SimulationPlot(Simulation simulation) {
		super(makePlotCanvas(simulation.getAttributes()));
		_simulation = simulation;
		_plotCanvas = getCanvas();
		_dataSet = _plotCanvas.getDataSet();
		_plotParameters = _plotCanvas.getParameters();
		setPreferences();
		_simulation.addUpdateListener(this);
	}
	
	/**
	 * Get the plot parameters
	 * @return the plot parameters
	 */
	public PlotParameters getPlotParameters() {
		return _plotParameters;
	}
	
	/**
	 * Get the plot canvas
	 * @return the plot canvas
	 */
	public PlotCanvas getPlotCanvas() {
		return _plotCanvas;
	}


	//set preferences
	private void setPreferences() {
		try {
			_useLogT = _simulation.getAttributes().getAttribute(Simulation.USELOGTEMP).getBoolean();
		} catch (InvalidTargetObjectTypeException e) {
			e.printStackTrace();
		}
		
		Vector<DataColumn> ycols = (Vector<DataColumn>) (_dataSet.getAllColumnsByType(DataColumnType.Y));
		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.NOLINE);
			dc.getStyle().setSymbolType(SymbolType.CIRCLE);
			dc.getStyle().setSymbolSize(3);
			dc.getStyle().setLineWidth(1.5f);
		}
		ycols.get(0).getStyle().setLineColor(Color.red);

		PlotTicks ticks = _plotCanvas.getPlotTicks();
		ticks.setNumMajorTickY(5);
		ticks.setNumMajorTickX(5);

		_plotParameters.setNumDecimalY(2);
		_plotParameters.setMinExponentX(4);
		_plotParameters.setMinExponentY(4);
	
	}
	
	private static PlotCanvas makePlotCanvas(Attributes attributes) {
		String plotTitle = "?";
		String xLabel = "?";
		String yLabel = "?";
		try {
			plotTitle = attributes.getAttribute(Simulation.PLOTTITLE).getString();
			xLabel = attributes.getAttribute(Simulation.XAXISLABEL).getString();
			yLabel = attributes.getAttribute(Simulation.YAXISLABEL).getString();
		} catch (InvalidTargetObjectTypeException e) {
			e.printStackTrace();
		}

		DataSet dataSet = null;
		
		try {
			dataSet = new DataSet(DataSetType.XYY, xLabel, yLabel);
		} catch (DataSetException e) {
			e.printStackTrace();
		}
		PlotCanvas plotCanvas = new PlotCanvas(dataSet, plotTitle, xLabel, yLabel) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width = 400;
				return d;
			}
		};

		return plotCanvas;
	}
	
	@Override
	public void updateSolution(Simulation simulation, Solution newSolution, Solution oldSolution) {

		double t = _simulation.getTemperature();
		double e = newSolution.getPlotY();
//		System.out.println("T = " + t + "  logT = " + Math.log10(t) + "  E = " + e);
		
		try {
			if (_useLogT) {
				t = Math.log10(t);
			}
			_dataSet.add(t, e);
		} catch (DataSetException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void reset(Simulation simulation) {
		_dataSet.clear();
	}

	@Override
	public void stateChange(Simulation simulation, SimulationState oldState, SimulationState newState) {
		if ((oldState == SimulationState.STOPPED) && (newState == SimulationState.RUNNING)) {
			_dataSet.clear();
		}
	}
}
