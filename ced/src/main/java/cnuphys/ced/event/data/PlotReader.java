package cnuphys.ced.event.data;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import cnuphys.bCNU.util.AsciiReader;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.pdata.HistoData;

public class PlotReader extends AsciiReader {
	
	//the plot type
	private String plotType;
	
	//for storing cuts
	private Vector<ICut> cuts;
	
	//the bounds
	private Rectangle bounds;
	
	//the plot dialog that should get created
    private PlotDialog plotDialog;
    
    //for histograms
    private HistoData histoData;
    
    //for scatter plots
    private DataSet dataSet;
	
	/**
	 * A PloteReader will also process the file 
	 * @param file the file to be processed.
	 * @throws FileNotFoundException
	 */
	public PlotReader(File file) throws FileNotFoundException {
		super(file);
	}

	@Override
	protected void processLine(String line) {
		
		//get plot type
		String tokens[] = PlotDialog.getTokens(line);
		String key = tokens[0];
		
		
		if (PlotDialog.TYPE.equals(key)) {
			plotType = tokens[1];
		}
		else if (RangeCut.CUT_TYPE.equals(key)) {
			if (cuts == null) {
				cuts = new Vector<ICut>();
			}
			cuts.add(RangeCut.fromString(line));
		}
		else if (PlotDialog.BOUNDS.equals(key)) {
			int x = Integer.parseInt(tokens[1]);
			int y = Integer.parseInt(tokens[2]);
			int w = Integer.parseInt(tokens[3]);
			int h = Integer.parseInt(tokens[4]);
			bounds = new Rectangle(x, y, w, h);
		}
		else if (PlotDialog.HISTODATA.equals(key)) {
			//oder name, numBin, xMin, xMax
			String name = tokens[1];
			int numBin = Integer.parseInt(tokens[2]);
			double xmin = Double.parseDouble(tokens[3]);
			double xmax = Double.parseDouble(tokens[4]);
			histoData = new HistoData(name, xmin, xmax, numBin);
		}
		else if (PlotDialog.DATASET.equals(key)) {
			String xname = tokens[1];
			String yname = tokens[2];
			String colNames[] = {xname, yname};
			try {
				dataSet = new DataSet(DataSetType.XYXY, colNames);
			} catch (DataSetException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void done() {
		
		if (plotType == null) {
			return;
		}
		
		// let's the to create the
		
		if (PlotDialog.HISTOGRAM.equals(plotType)) {
			if (histoData != null) {
				plotDialog = DefinitionManager.getInstance().addHistogram(histoData);
			}
			
		}
		else if(PlotDialog.SCATTERPLOT.equals(plotType)) {
			if (dataSet != null) {
				plotDialog = DefinitionManager.getInstance().addScatterPlot(dataSet);
			}
		}

		if (plotDialog == null) {
			return;
		}

		// add the cuts
		if ((cuts != null) && !(cuts.isEmpty())) {
			for (ICut cut : cuts) {
				plotDialog.addCut(cut);
			}
		}
		
		//locate and size
		if (bounds != null) {
			plotDialog.setBounds(bounds);
		}
	}
	
	/**
	 * Get the plot dialog
	 * @return the plot dialog
	 */
	public PlotDialog getPlotDialog() {
		return plotDialog;
	}

}
