package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.X11Colors;

public class EntryPanel extends JPanel {

	// entering the name of the expression
	private JTextField _nameTF;

	// the expression text
	private JTextArea _expressionText;

	// validation error
	private JTextArea _validationTF;

	private ExpressionPanel _expressionPanel;

	public EntryPanel(ExpressionPanel ePanel) {
		_expressionPanel = ePanel;
		setLayout(new BorderLayout(4, 4));
		addNorth();
		addSouth();
		addCenter();
	}

	//add the center panel
	private void addCenter() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(4, 4));

		createExpressionArea();
		centerPanel.add(_expressionText, BorderLayout.CENTER);

		_validationTF = new JTextArea("", 2, 40);
		_validationTF.setOpaque(true);
		_validationTF.setEditable(false);
		_validationTF.setLineWrap(true);
		_validationTF.setBackground(X11Colors.getX11Color("alice blue"));
		_validationTF.setFont(Fonts.mediumFont);
		_validationTF.setForeground(Color.red);
		centerPanel.add(_validationTF, BorderLayout.SOUTH);
		_validationTF.setBorder(new CommonBorder("Validation"));

		add(centerPanel, BorderLayout.CENTER);

	}

	//add the north panel
	private void addNorth() {
		// the functions
		JTextArea fdef = new JTextArea("", 5, 40);
		fdef.setOpaque(true);
		fdef.setEditable(false);
		// fdef.setFont(Fonts.mediumFont);
		fdef.setBackground(X11Colors.getX11Color("alice blue"));
		// fdef.setForeground(Color.cyan);
		fdef.setText("abs, acos, asin, atan, atan2, cbrt, ceil, cos, cosh, exp, expm1, floor, log,\n"
				+ "log, log1p, log10, pow, sin, sinh, sqrt, tan, tanh, toDegrees, toRadians\n\n"
				+ "OPERATORS: +, -, *, /, %, ^ (power, e.g., 2^x or x^y)\n\n"
				+ "NOTE: Prepend named variables with an underscore, e.g., atan2(_x, _y)");

		fdef.setBorder(new CommonBorder(
				"Allowed functions (see Java Math class for use) and operators"));
		add(fdef, BorderLayout.NORTH);
	}

	private void addSouth() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));

		JLabel label = new JLabel("Expression name:");

		createExpNameTF();

		sp.add(label);
		sp.add(_nameTF);

		sp.setBorder(new CommonBorder("Name the Expression"));
		add(sp, BorderLayout.SOUTH);
	}

	//create the expression entry area
	private void createExpressionArea() {
		_expressionText = new JTextArea();
		_expressionText.setLineWrap(true);
		_expressionText.setOpaque(true);
		_expressionText.setEditable(true);
		_expressionText.setBackground(X11Colors.getX11Color("wheat"));
		_expressionText.setBorder(new CommonBorder("Enter the expression"));
	}

	//the expression name text field
	private void createExpNameTF() {
		_nameTF = new JTextField("", 20);
		// _nameTF.setEnabled(false);

		KeyAdapter kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {

				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					nameExpression();
				}
			}
		};

		_nameTF.addKeyListener(kl);
	}

	//add the named expression
	private void nameExpression() {
		// get the variable name
		String ename = _nameTF.getText();
		if (ename == null) {
			return;
		}

		ename = ename.trim();
		if (ename.length() < 1) {
			return;
		}

		if (!Character.isLetter(ename.charAt(0))) {
			JOptionPane.showMessageDialog(null,
					"A valid name must start with a character.");
			return;
		}

		// see if it makes a valid expression
		Expression e = getExpression();

		// get the expression descriptor
	//	String fn = _expressionText.getText();
		Expression expression = this.getExpression();
		boolean valid = (expression != null);
		if (!valid) {
			return;
		}

		// System.err.println("Binding [" + vname + "] to [" + fn + "]");
		if (DefinitionManager.getInstance().addExpression(ename, _expressionText.getText().trim())) {
			_expressionPanel.getExpressionModel().fireTableDataChanged();
		}
	}

	/**
	 * Try to get the expression from the text area
	 * 
	 * @return the expression or <code>null</code>
	 */
	private Expression getExpression() {

		String s = _expressionText.getText();
		if ((s == null) || (s.length() < 1)) {
			return null;
		}

		// step 1, remove all white space
		s = s.replaceAll("\\s+", "");
		if (s.length() < 1) {
			return null;
		}

		// step 2, tokenize
		String tokens[] = FileUtilities.tokens(s, ",)(+-*/%^");
		if ((tokens == null) || (tokens.length < 1)) {
			return null;
		}

		for (String token : tokens) {
			System.err.println("TOKEN [" + token + "]");
		}

		// Step 3 get the variables
		Vector<String> vv = new Vector<String>();
		for (String token : tokens) {
			if (token.startsWith("_")) {
				vv.remove(token);
				vv.add(token);
			}
		}

		String vars[] = (vv.size() == 0) ? null : new String[vv.size()];

		if (vars != null) {
			vv.toArray(vars);
			for (String v : vars) {
				System.err.println("VARIABLE [" + v + "]");
			}
		}

		Expression e = null;
		// it is technically ok if there are no vars--it means a c expression

		try {
			if ((vars == null) || (vars.length < 1)) {
				e = new ExpressionBuilder(s).variables().build();
			} else {
				e = new ExpressionBuilder(s).variables(vars).build();
			}
		} catch (IllegalArgumentException ex) {
			e = null;
			_validationTF.setText("Invalid expression. " + ex.getMessage());
		}

		// valid?
		if (e != null) {
			// don't check for set variables here
			ValidationResult vres = e.validate(false);
			if (vres == ValidationResult.SUCCESS) {
				_validationTF.setText("Valid expression");
			} else {
				String estr = "Invalid expression.";
				for (String es : vres.getErrors()) {
					estr += " [" + es + "]";
				}
				_validationTF.setText(estr);
			}
		}
		return e;
	}

}
