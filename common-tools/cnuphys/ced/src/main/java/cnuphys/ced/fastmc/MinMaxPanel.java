package cnuphys.ced.fastmc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MinMaxPanel extends JPanel {
	
	private double _absMin;
	private double _absMax;
	
	
	private JTextField _minTF;
	private JTextField _maxTF;

	public MinMaxPanel(String varName, String units, double absMin, double absMax) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		_absMin = absMin;
		_absMax = absMax;
		
		JLabel nameP = nameLabel(varName);
		JLabel unitLabel = new JLabel(units);
		
		JLabel minLabel = new JLabel("min:");
		JLabel maxLabel = new JLabel("max:");
		
		
		_minTF = new JTextField(5);
		_maxTF = new JTextField(5);
		
		_minTF.setText(""+absMin);
		_maxTF.setText(""+absMax);
		
		add(nameP);
		add(Box.createHorizontalStrut(12));
		add(minLabel);
		add(Box.createHorizontalStrut(6));
		add(_minTF);
		add(Box.createHorizontalStrut(12));
		add(maxLabel);
		add(Box.createHorizontalStrut(6));
		add(_maxTF);
		add(Box.createHorizontalStrut(6));
		add(unitLabel);
	}
	
	public JLabel nameLabel(String name) {
		JLabel p = new JLabel(name) {
			
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width = 100;
				return d;
			}
		};
		
		p.setForeground(Color.blue);
		p.setHorizontalAlignment(SwingConstants.RIGHT);
		return p;
	}
		
	public double getMinimum() {
		return getTFVal(_minTF);
	}
	
	public double getMaximum() {
		return getTFVal(_maxTF);
	}
	
	public double getTFVal(JTextField tf) {
		double val = Double.NaN;
		
		try {
			val = Double.parseDouble(tf.getText());
			val = inRange(val);
		}
		catch (Exception e) {
			
		}
		
		return val;
	}

	private double inRange(double val) {
		return Math.max(_absMin, Math.min(_absMax, val));
	}
	
	private double random(Random rand) {
		double min = getMinimum();
		double max = getMaximum();
		
		if (Double.isNaN(min) || Double.isNaN(max)) {
			return Double.NaN;
		}
		
		double range = max - min;
		return min + range*rand.nextDouble();
		
	}
}
