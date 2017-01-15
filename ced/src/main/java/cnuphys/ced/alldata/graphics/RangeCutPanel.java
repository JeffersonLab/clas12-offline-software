package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Environment;

public class RangeCutPanel extends JPanel implements PropertyChangeListener {

	private JFormattedTextField _minValTF;

	private JFormattedTextField _maxValTF;
	
	private SelectPanel _sp;

	private double _minVal = 0;
	private double _maxVal = 1;

	public RangeCutPanel() {
		setLayout(new BorderLayout(2, 2));

		_sp = new SelectPanel("Select Variable", true);
		add(_sp, BorderLayout.CENTER);
		addEast();
	}
	
	/**
	 * Get the select panel
	 * 
	 * @return the select panel
	 */
	public SelectPanel getSelectPanel() {
		return _sp;
	}

	private void addEast() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 0));
		
		JPanel sp = new JPanel();
		sp.setLayout(new GridLayout(2, 1, 4, 4));
		_minValTF = decimalField(sp, "Min Value", _minVal);
		_maxValTF = decimalField(sp, "Max Value", _maxVal);
		
		_minValTF.addPropertyChangeListener("value", this);
		_maxValTF.addPropertyChangeListener("value", this);
		
		p.add(sp, BorderLayout.NORTH);
		add(p, BorderLayout.EAST);
	}

	private JFormattedTextField decimalField(JPanel p, String title,
			double defValue) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(4);
		numberFormat.setGroupingUsed(false);

		JPanel panel = titledPanel(title);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JFormattedTextField tf = new JFormattedTextField(numberFormat);
		tf.setValue(defValue);
		tf.setColumns(10);

		panel.add(tf);
		p.add(panel);
		return tf;
	}

	// get a titled panel
	protected JPanel titledPanel(String title) {
		JPanel pan = new JPanel();
		pan.setBorder(new CommonBorder(title));
		Environment.getInstance().commonize(pan, null);
		return pan;
	}
	
	/**
	 * Return a RangeCut if the user hit ok
	 * @return a Range or <code>null</code>.
	 */
	public RangeCut getRangeCut() {
		String name = _sp.getResolvedName();
		_minVal = ((Number) _minValTF.getValue()).doubleValue();
		_maxVal = ((Number) _maxValTF.getValue()).doubleValue();

		return new RangeCut(name, _minVal, _maxVal);

	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		Object source = e.getSource();
		if (source == _minValTF) {
			_minVal = ((Number) _minValTF.getValue()).doubleValue();
		}
		else if (source == _maxValTF) {
			_maxVal = ((Number) _maxValTF.getValue()).doubleValue();
		}
	}

}
