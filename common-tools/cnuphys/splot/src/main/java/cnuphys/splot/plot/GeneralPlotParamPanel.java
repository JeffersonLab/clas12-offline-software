package cnuphys.splot.plot;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class GeneralPlotParamPanel extends APreferencePanel {

	// title text field
	private JTextField _plotTitle;

	// x label text field
	private JTextField _xLabel;

	// ylabel text field
	private JTextField _yLabel;

	/**
	 * For editing basic parameters
	 * 
	 * @param canvas the plot canvas
	 */
	public GeneralPlotParamPanel(PlotCanvas canvas) {
		super(canvas, "Labels");
		setLayout(new BorderLayout(4, 4));
		addNorth();
	}

	// add the north panel
	private void addNorth() {

		JPanel panel = new JPanel();
		Environment.getInstance().commonize(panel, null);
		panel.setLayout(new GridLayout(3, 1));

		// plot title
		JPanel nPanel1 = titledPanel("plot title");
		nPanel1.setLayout(new FlowLayout(FlowLayout.CENTER));
		_plotTitle = new JTextField(_canvas.getParameters().getPlotTitle(), 40);
		nPanel1.add(_plotTitle);

		// x label
		JPanel nPanel2 = titledPanel("x axis label");
		nPanel2.setLayout(new FlowLayout(FlowLayout.CENTER));
		_xLabel = new JTextField(_canvas.getParameters().getXLabel(), 40);
		nPanel2.add(_xLabel);

		// y label
		JPanel nPanel3 = titledPanel("y axis label");
		nPanel3.setLayout(new FlowLayout(FlowLayout.CENTER));
		_yLabel = new JTextField(_canvas.getParameters().getYLabel(), 40);
		nPanel3.add(_yLabel);

		_plotTitle.addKeyListener(this);
		_xLabel.addKeyListener(this);
		_yLabel.addKeyListener(this);

		panel.add(nPanel1);
		panel.add(nPanel2);
		panel.add(nPanel3);
		add(panel, BorderLayout.NORTH);
	}

	@Override
	public void apply() {
		_canvas.getParameters().setPlotTitle(_plotTitle.getText());
		_canvas.getParameters().setXLabel(_xLabel.getText());
		_canvas.getParameters().setYLabel(_yLabel.getText());
	}

}
