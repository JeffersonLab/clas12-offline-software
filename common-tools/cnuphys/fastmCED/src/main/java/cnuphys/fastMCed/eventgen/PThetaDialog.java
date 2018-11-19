package cnuphys.fastMCed.eventgen;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JFrame;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.fastMCed.snr.SNRDictionary;
import cnuphys.lund.GeneratedParticleRecord;
import cnuphys.splot.example.APlotDialog;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataColumnType;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.style.SymbolType;

public class PThetaDialog extends APlotDialog {


	private SNRDictionary _dictionary;

	/**
	 * Create the dialog for ploting the field
	 * 
	 * @param parent
	 *            the parent dialog
	 * @param modal
	 *            the usual meaning
	 */
	public PThetaDialog(JFrame parent, boolean modal, SNRDictionary dictionary) {
		super(parent, "P-" + UnicodeSupport.SMALL_THETA + " Scatter Plot", modal, null);
		_dictionary = dictionary;
		setIconImage(ImageManager.cnuIcon.getImage());
		_canvas.setPreferredSize(new Dimension(600, 600));
		
		DataSet ds = _canvas.getDataSet();
		System.err.println("ADDING DATA TO PLOT");
		for (String gprhash : dictionary.values()) {
			GeneratedParticleRecord gpr = GeneratedParticleRecord.fromHash(gprhash);
//			System.err.println(gpr.toString());

			try {
				ds.add(gpr.getTheta(), gpr.getMomentum());
			} catch (DataSetException e) {
				e.printStackTrace();
			}
		}
		
		
		pack();
	}

	@Override
	protected DataSet createDataSet() throws DataSetException {

		DataSet ds = new DataSet(DataSetType.XYXY, getColumnNames());
		return ds;
	}

	@Override
	protected String[] getColumnNames() {
		String labels[] = { UnicodeSupport.SMALL_THETA, "P"};
		return labels;
	}

	@Override
	protected String getXAxisLabel() {
		return UnicodeSupport.SMALL_THETA + " (deg)";
	}

	@Override
	protected String getYAxisLabel() {
		return "P (GeV/c)";
	}

	@Override
	protected String getPlotTitle() {
		return "P-Theta";
	}

	@Override
	public void fillData() {
	}


	@Override
	public void setPreferences() {
		Color fillColor = Color.black;
		DataSet ds = _canvas.getDataSet();
		Collection<DataColumn> ycols = ds.getAllColumnsByType(DataColumnType.Y);

		for (DataColumn dc : ycols) {
			dc.getFit().setFitType(FitType.NOLINE);
			dc.getStyle().setSymbolType(SymbolType.SQUARE);
			dc.getStyle().setSymbolSize(3);
			dc.getStyle().setFillColor(fillColor);
			dc.getStyle().setLineColor(null);
		}

		// many options controlled via plot parameters
		PlotParameters params = _canvas.getParameters();
		params.mustIncludeXZero(true);
		params.mustIncludeYZero(true);
//		params.addPlotLine(new HorizontalLine(_canvas, 0));
//		params.addPlotLine(new VerticalLine(_canvas, 0));
	}


}