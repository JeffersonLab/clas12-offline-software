package cnuphys.splot.example;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.Histo2DData;

public class Histo2D extends AExample {
	@Override
	protected DataSet createDataSet() throws DataSetException {
		Histo2DData h1 = new Histo2DData("Histo 2D", 0.0, 100.0, 50, 0.0, 50.0, 40);
		return new DataSet(h1);
	}

	@Override
	protected String[] getColumnNames() {
		return null;
	}

	@Override
	protected String getXAxisLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getYAxisLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getPlotTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreferences() {
		// TODO Auto-generated method stub
		
	}

}
