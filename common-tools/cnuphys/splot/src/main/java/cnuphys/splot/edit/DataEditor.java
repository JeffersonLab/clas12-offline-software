package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataTable;
import cnuphys.splot.plot.DoubleFormat;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.PlotCanvas;

public class DataEditor extends SimpleDialog implements TableModelListener {

	// underlying plot canvas
	protected PlotCanvas _plotCanvas;

	// button labels
	protected static final String APPLY = "Apply";
	protected static final String CLOSE = "Close";

	// the table
	protected DataTable _table;

	// num row and col label
	protected JLabel _rowColLabel;

	// range label
	protected JLabel _rangeLabel;

	private static Font _font = Environment.getInstance().getCommonFont(12);

	/**
	 * Edit the plot preferences
	 * 
	 * @param plotCanvas the plot being edited
	 */
	public DataEditor(PlotCanvas plotCanvas) {
		super("Plot Data", plotCanvas, true, APPLY, CLOSE);

		// note components already created by super constructor
		_plotCanvas = plotCanvas;

		addNorth();
		pack();
		setLabels();
	}

	/**
	 * Get the table
	 * 
	 * @return the underlying table
	 */
	public DataTable getTable() {
		return _table;
	}

	/**
	 * can do preparation--for example a component might be added on
	 * "createCenterComponent" but a reference needed in "addNorthComponent"
	 */
	@Override
	protected void prepare() {
		_plotCanvas = (PlotCanvas) _userObject;
		_table = new DataTable(_plotCanvas.getDataSet());
		_table.getModel().addTableModelListener(this);
	}

	/**
	 * Override to create the component that goes in the center. Usually this is the
	 * "main" component.
	 * 
	 * @return the component that is placed in the center
	 */
	@Override
	protected Component createCenterComponent() {
		return _table.getScrollPane();
	}

	/**
	 * A button was hit. The default behavior is to shutdown the dialog.
	 * 
	 * @param command the label on the button that was hit.
	 */
	@Override
	protected void handleCommand(String command) {
		if (CLOSE.equals(command)) {
			setVisible(false);
		}
		else if (APPLY.equals(command)) {
		}
	}

	protected Component addNorth() {
		JPanel npanel = new JPanel();
		npanel.setLayout(new VerticalFlowLayout());

		JPanel npanel1 = new JPanel();
		npanel1.setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel label1 = new JLabel(_plotCanvas.getParameters().getPlotTitle());
		npanel1.add(label1);

		JPanel npanel2 = new JPanel();
		npanel2.setLayout(new FlowLayout(FlowLayout.CENTER));

		String str2 = "Number of rows: 000" + "  Number of columns: 000";
		_rowColLabel = new JLabel(str2);
		_rowColLabel.setFont(_font);
		npanel2.add(_rowColLabel);

		JPanel npanel3 = new JPanel();
		npanel3.setLayout(new FlowLayout(FlowLayout.CENTER));

		String str3 = "X Range: [XXXXX.XX, XXXXX.XX]" + "  Y Range: [XXXXX.XX, XXXXX.XX]";
		_rangeLabel = new JLabel(str3);
		_rangeLabel.setFont(_font);
		npanel3.add(_rangeLabel);

		npanel.add(npanel1);
		npanel.add(npanel2);
		npanel.add(npanel3);
		add(npanel, BorderLayout.NORTH);
		return npanel;
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		setLabels();
	}

	private void setLabels() {
		String str = "Number of rows: " + _table.getModel().getRowCount() + "  Number of columns: "
				+ _table.getModel().getColumnCount();
		_rowColLabel.setText(str);

		DataSet ds = _plotCanvas.getDataSet();
		String xminString = DoubleFormat.doubleFormat(ds.getXmin(), 4, 3);
		String xmaxString = DoubleFormat.doubleFormat(ds.getXmax(), 4, 3);
		String yminString = DoubleFormat.doubleFormat(ds.getYmin(), 4, 3);
		String ymaxString = DoubleFormat.doubleFormat(ds.getYmax(), 4, 3);

		String str2 = String.format("<html><b>X Range:</b> [%s, %s]" + "&nbsp;&nbsp;&nbsp;<b>Y Range:</b> [%s, %s]",
				xminString, xmaxString, yminString, ymaxString);

		_rangeLabel.setText(str2);
	}

}
