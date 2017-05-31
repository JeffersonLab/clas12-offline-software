package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;

public class ScatterPanel extends JPanel implements PropertyChangeListener {
	
	//select panel for x and y
	private SelectPanel _spx;
	private SelectPanel _spy;

	public ScatterPanel() {
		setLayout(new BorderLayout(2, 2));
		addCenter();
	}
	
	private void addCenter() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1, 2, 10));
		
		_spx = new SelectPanel("X axis Variable", true);
		_spy = new SelectPanel("Y axis Variable", true);
		
		p.add(_spx);
		p.add(_spy);
		add(p, BorderLayout.CENTER);
	}
	
	/**
	 * Get the title from the data set
	 * @param ds the data set
	 * @return
	 */
	public static String getTitle(DataSet ds) {
		String xs = ds.getColumnName(0);
		String ys = ds.getColumnName(1);
		
		return xs + " and " + ys;
	}
	
	/**
	 * Get the two select panels x,y
	 * @return the select panels
	 */
	public SelectPanel[] getScatterPanels() {
		SelectPanel p[] = {_spx, _spy};
		return p;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}
	
	/**
	 * Create the scatter plot data set
	 * @return the scatter plot data set
	 */
	public DataSet createDataSet() {
		
		String xname = _spx.getResolvedName();
		String yname = _spy.getResolvedName();
		
		String colNames[] = {xname, yname};
		try {
			return new DataSet(DataSetType.XYXY, colNames);
		} catch (DataSetException e) {
			e.printStackTrace();
			return null;
		}
	}

}
