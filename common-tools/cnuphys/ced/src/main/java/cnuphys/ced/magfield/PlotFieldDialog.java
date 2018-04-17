package cnuphys.ced.magfield;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import bCNU3D.DoubleFormat;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.magfield.IField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.splot.example.APlotDialog;
import cnuphys.splot.fit.FitType;
import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.DataSet;
import cnuphys.splot.pdata.DataSetException;
import cnuphys.splot.pdata.DataSetType;
import cnuphys.splot.plot.PlotParameters;
import cnuphys.splot.style.SymbolType;

public class PlotFieldDialog extends APlotDialog implements ActionListener {

	private static int _numPlotPoints = 50000;

	private static final int Z = 0;
	private static final int RHO = 1;
	private static final int PHI = 2;

	// which is the variable (other two are fixed)
	private static int _whichVaries = Z;

	// the default fixed values and ranges
	private static double _fixedValues[] = { 375, 50, 0 };
	private static double _minValues[] = { 200, 0, -20 };
	private static double _maxValues[] = { 500, 250, 20 };

	private static String sPHI = UnicodeSupport.SMALL_PHI;
	private static String sRHO = UnicodeSupport.SMALL_RHO;
	private static String sDEG = UnicodeSupport.DEGREE;

	// the x axis labels
	private static String _xLabels[] = { "z (cm) ", sRHO + " (cm) ", sPHI + " (deg)" };

	// the toggle button labels
	private static String tbLabels[] = { " z ", " " + sRHO + " ", " " + sPHI + " " };

	// generate the plot
	private JButton _plotButton;

	private VariablePanel _varPanels[];

	// the variable toggle buttons
	private JRadioButton _vButtons[];

	// plot parameters
	private PlotParameters _parameters;

	public PlotFieldDialog(JFrame parent, boolean modal) {
		super(parent, "Magnetic Field Plotter", modal);
		_canvas.setPreferredSize(new Dimension(600, 600));
		pack();
	}

	@Override
	protected DataSet createDataSet() throws DataSetException {

		DataSet ds = new DataSet(DataSetType.XYXY, getColumnNames());

		DataColumn curve = ds.getCurve(0);
		if (curve != null) {
			curve.getFit().setFitType(FitType.CONNECT);
			curve.getStyle().setSymbolType(SymbolType.NOSYMBOL);
		}

		return ds;
	}

	@Override
	protected String[] getColumnNames() {
		String labels[] = { "Component", "B" };
		return labels;
	}

	@Override
	protected String getXAxisLabel() {
		return "z (cm)";
	}

	@Override
	protected String getYAxisLabel() {
		return "|B| (T)";
	}

	@Override
	protected String getPlotTitle() {
		return "Magnetic Field";
	}

	@Override
	public void fillData() {
	}

	@Override
	public void setPreferences() {
		_parameters = _canvas.getParameters();
		_parameters.setExtraDrawing(true);
		_parameters.mustIncludeYZero(true);
		_parameters.setMinExponentX(3);
	}

	/**
	 * Add a north component
	 */
	@Override
	protected void addNorth() {
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout(2, 2));
		panel.add(makeVariablePanel(), BorderLayout.NORTH);
		panel.add(makeButtonPanel(), BorderLayout.SOUTH);

		panel.setBorder(new CommonBorder("Variable Selection"));

		JPanel cPanel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
			}

		};

		cPanel.setLayout(new GridLayout(3, 1, 0, 6));

		_varPanels = new VariablePanel[3];

		for (int i = 0; i < 3; i++) {
			_varPanels[i] = new VariablePanel(i);
			cPanel.add(_varPanels[i]);
		}

		panel.add(cPanel, BorderLayout.CENTER);

		add(panel, BorderLayout.NORTH);

	}

	// make a button panel
	private JPanel makeButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));

		_plotButton = new JButton("plot");
		_plotButton.addActionListener(this);

		panel.add(_plotButton);
		return panel;
	}

	private JPanel makeVariablePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
		ButtonGroup bg = new ButtonGroup();

		panel.add(new JLabel("Select what varies: "));

		_vButtons = new JRadioButton[3];
		for (int var = 0; var < 3; var++) {
			_vButtons[var] = makeRadioButton(tbLabels[var], var == _whichVaries);
			bg.add(_vButtons[var]);
			panel.add(_vButtons[var]);
		}

		return panel;
	}

	private JRadioButton makeRadioButton(String label, boolean selected) {
		JRadioButton tb = new JRadioButton(label, selected);
		tb.addActionListener(this);
		return tb;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == _plotButton) {
			doPlot();
		} else {
			for (int var = 0; var < 3; var++) {
				if (source == _vButtons[var]) {

					if (var == _whichVaries) {
						return;
					}

					int oldV = _whichVaries;
					_whichVaries = var;
					_varPanels[oldV].setEnabled();
					_varPanels[_whichVaries].setEnabled();

					_parameters.setXLabel(_xLabels[var]);
					fixExtraStrings();
					System.err.println("CHANGED VARIABLE");
					_canvas.getDataSet().clear();
					break;
				}

			}
		}
	}

	private void fixExtraStrings() {

		String s0 = MagneticFields.getInstance().getActiveFieldDescription();
		String s4 = MagneticFields.getInstance().fileBaseNames();
		String s1 = "";
		String s2 = "";
		switch (_whichVaries) {
		case Z:
			String pV = DoubleFormat.doubleFormat(_fixedValues[PHI], 1);
			String rV = DoubleFormat.doubleFormat(_fixedValues[RHO], 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = sRHO + " = " + rV + "cm";
			break;

		case RHO:
			pV = DoubleFormat.doubleFormat(_fixedValues[PHI], 1);
			String zV = DoubleFormat.doubleFormat(_fixedValues[Z], 1);
			s1 = sPHI + " = " + pV + sDEG;
			s2 = "z  = " + zV + "cm";
			break;

		case PHI:
			rV = DoubleFormat.doubleFormat(_fixedValues[RHO], 1);
			zV = DoubleFormat.doubleFormat(_fixedValues[Z], 1);
			s1 = sRHO + " = " + rV + "cm";
			s2 = "z  = " + zV + "cm";
			break;
		}

		_parameters.setExtraStrings(s0, s4, s1, s2);

	}

	// create the plot
	private void doPlot() {
		_canvas.getDataSet().clear();
		fixExtraStrings();

		IField ifield = MagneticFields.getInstance().getActiveField();

		double min = _varPanels[_whichVaries].getMinValue();
		double max = _varPanels[_whichVaries].getMaxValue();
		double del = (max - min) / (_numPlotPoints - 1);

		for (int i = 0; i < _numPlotPoints; i++) {
			double val = min + i * del;
			double mag = 0;
			switch (_whichVaries) {
			case Z:
				mag = ifield.fieldMagnitudeCylindrical(_varPanels[PHI].getFixedValue(), 
						_varPanels[RHO].getFixedValue(), val);
				break;

			case RHO:
				mag = ifield.fieldMagnitudeCylindrical(_varPanels[PHI].getFixedValue(), val,
						_varPanels[Z].getFixedValue());
				break;

			case PHI:
				mag = ifield.fieldMagnitudeCylindrical(val, _varPanels[RHO].getFixedValue(),
						_varPanels[Z].getFixedValue());
				break;
			}

			mag = mag / 10; // to tesla
			try {
				_canvas.getDataSet().add(val, mag);
			} catch (DataSetException e) {
				e.printStackTrace();
				break;
			}
		}

		_canvas.repaint();
	}

	public static void main(String arg[]) {

		// String torusPath =
		// "/Users/heddle/magfield/Jan_clas12TorusFull_2.00.dat";
		String torusPath = "/Users/heddle/magfield/clas12TorusFull_2.00.dat";
		String solenoidPath = "/Users/heddle/magfield/clas12-fieldmap-solenoid.dat";
		try {
			MagneticFields.getInstance().initializeMagneticFields(torusPath, solenoidPath);
			MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not initialize Magnetic Fields");
			System.exit(1);
		}

		PlotFieldDialog pfd = new PlotFieldDialog(null, true);

		JMenuBar mb = pfd.getJMenuBar();
		mb.add(MagneticFields.getInstance().getMagneticFieldMenu());

		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		pfd.addWindowListener(wa);

		pfd.setVisible(true);

	}

	class VariablePanel extends JPanel {

		private JTextField _fixedTF;
		private JTextField _minTF;
		private JTextField _maxTF;

		private int _varIndex;

		public VariablePanel(int var) {

			_varIndex = var;
			setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
			_fixedTF = new JTextField(DoubleFormat.doubleFormat(_fixedValues[var], 2), 8);

			add(new JLabel(fwString(_xLabels[var], "  X (XXX) ")));
			add(_fixedTF);

			add(new JLabel(" min "));
			_minTF = new JTextField(DoubleFormat.doubleFormat(_minValues[var], 2), 8);
			add(_minTF);

			add(new JLabel(" max "));
			_maxTF = new JTextField(DoubleFormat.doubleFormat(_maxValues[var], 2), 8);
			add(_maxTF);

			setEnabled();
		}

		public void setEnabled() {
			// System.err.println("INDEX: " + _varIndex + " VARIES: "+
			// _whichVaries);
			_fixedTF.setEnabled(_varIndex != _whichVaries);
			_minTF.setEnabled(_varIndex == _whichVaries);
			_maxTF.setEnabled(_varIndex == _whichVaries);
		}

		public double getMinValue() {
			try {
				return Double.parseDouble(_minTF.getText());
			} catch (Exception e) {
				return _minValues[_varIndex];
			}
		}

		public double getMaxValue() {
			try {
				return Double.parseDouble(_maxTF.getText());
			} catch (Exception e) {
				return _maxValues[_varIndex];
			}
		}

		public double getFixedValue() {
			try {
				return Double.parseDouble(_fixedTF.getText());
			} catch (Exception e) {
				return _fixedValues[_varIndex];
			}
		}

		private String fwString(String s, String targ) {
			FontMetrics fm = getFontMetrics(getFont());
			int targSW = fm.stringWidth(targ);

			String ss = new String(s);

			while (fm.stringWidth(ss) < targSW) {
				ss = " " + ss;
			}
			return ss;
		}

		@Override
		public Insets getInsets() {
			Insets def = super.getInsets();
			return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
		}

	}

}
