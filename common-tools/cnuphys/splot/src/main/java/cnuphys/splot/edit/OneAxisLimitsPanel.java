package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.plot.LimitsMethod;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.style.EnumComboBox;

public class OneAxisLimitsPanel extends JPanel implements ActionListener {
	
	public enum Axis {X, Y}
	

	//the plot canvas
	private PlotCanvas _canvas;
	
	//which axis
	private Axis _axis;
		
	//current method
	private LimitsMethod _method1;
	
	//current manual limits
	private double _min1;
	private double _max1;
	
	//the data set
	private DataSet _dataSet;
	
	//current data limits
	private double _dataMin;
	private double _dataMax;
	
	//text fields for limits
	private JFormattedTextField _minTF;
	private JFormattedTextField _maxTF;
	
	
	//the method combo box
	private EnumComboBox _methodSelector;
	
	//plot parameters
	private PlotParameters _params;
	

	/**
	 * Edit the axes limits
	 * @param canvas the plot canvas
	 * @param axis which axis
	 */
	public OneAxisLimitsPanel(PlotCanvas canvas, Axis axis) {
		_canvas = canvas;
		_params = _canvas.getParameters();
		_dataSet = _canvas.getDataSet();
		_axis = axis;
		setLayout(new BorderLayout(4, 4));
		
		addNorth();
		addCenter();
		fix();
	}
	
	private void addNorth() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		JLabel label = null;
		
		switch (_axis) {
		case X:
			_method1 = _params.getXLimitsMethod();
			_min1 = _params.getManualXMin();
			_max1 = _params.getManualXMax();
			
			_dataMin = _dataSet.getXmin();
			_dataMax = _dataSet.getXmax();
			label = new JLabel("X axis method");
			break;
			
		case Y:
			_method1 = _params.getYLimitsMethod();
			_min1 = _params.getManualYMin();
			_max1 = _params.getManualYMax();
			
			_dataMin = _dataSet.getYmin();
			_dataMax = _dataSet.getYmax();
			label = new JLabel("Y axis method");
		break;
		}
		
		double dataDel = 0.05 * (_dataMax - _dataMin);
		if (Double.isNaN(_min1)) {
			_min1 = _dataMin - dataDel;
		}
		if (Double.isNaN(_max1)) {
			_max1 = _dataMax + dataDel;
		}
		
		addCombo(p);
		p.add(label);
		add(p, BorderLayout.NORTH);
	}
	
	private void addCenter() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1, 4, 4));
		
		JPanel spMin = new JPanel();
		spMin.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		_minTF = createTF(_min1);
		spMin.add(_minTF);
		spMin.add(new JLabel(_axis.name() + "min (manual)"));
	//	spMin.add(Box.createHorizontalStrut(8));

		
		JPanel spMax = new JPanel();
		spMax.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		_maxTF = createTF(_max1);
		spMax.add(_maxTF);
		spMax.add(new JLabel(_axis.name() + "max (manual)"));


		p.add(spMin);
		p.add(spMax);
		add(p, BorderLayout.CENTER);
		
	}
	
	private void fix() {
		
		_minTF.setEnabled(_method1 == LimitsMethod.MANUALLIMITS);
		_maxTF.setEnabled(_method1 == LimitsMethod.MANUALLIMITS);
		
	}
	
	//add the method combo box
	private void addCombo(JPanel p) {
		_methodSelector = LimitsMethod.getComboBox(_method1);
		_methodSelector.addActionListener(this);
		p.add(_methodSelector);
	}
	
	private JFormattedTextField createTF(double defValue) {
			NumberFormat numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMaximumFractionDigits(4);
			numberFormat.setGroupingUsed(false);

			JFormattedTextField tf = new JFormattedTextField(numberFormat);
			tf.setValue(defValue);
			tf.setColumns(8);

			return tf;
		
	}

	/** apply changes */
	public void apply() {
		switch (_axis) {
		case X:
			_params.setXLimitsMethod(_method1);

			if (_method1 == LimitsMethod.MANUALLIMITS) {
				_min1 = getValue(_minTF, _min1);
				_max1 = getValue(_maxTF, _max1);
				_params.setXRange(_min1, _max1);
			}
			break;

		case Y:
			_params.setYLimitsMethod(_method1);
			if (_method1 == LimitsMethod.MANUALLIMITS) {
				_min1 = getValue(_minTF, _min1);
				_max1 = getValue(_maxTF, _max1);
				_params.setYRange(_min1, _max1);
			}
			break;
		}
		
	}
	
	private double getValue(JFormattedTextField tf, double v) {
		
		try {
			return Double.parseDouble(tf.getText());
		}
		catch (Exception e) {
			tf.setText(""+v);
			return v;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		LimitsMethod method = (LimitsMethod) _methodSelector.getSelectedEnum();
		if (method == _method1) {
			return;
		}
		
		_method1 = method;
		fix();
	}

	
}
