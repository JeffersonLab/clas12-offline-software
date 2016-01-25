package cnuphys.ced.event.data;

import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import cnuphys.bCNU.util.FileUtilities;
import net.oh.exp4j.Expression;
import net.oh.exp4j.ExpressionBuilder;
import net.oh.exp4j.ValidationResult;

public class NamedExpression implements Comparable<NamedExpression> {

	/** The expression name */
	public String expName;

	/** The expression definition string */
	public String expString;

	private Expression _expression;

	public NamedExpression(String eName, String eString) {
		expName = eName;
		expString = eString;
	}

	@Override
	public int compareTo(NamedExpression o) {
		String lcv = expName.toLowerCase();
		String lco = o.expName.toLowerCase();
		return lcv.compareTo(lco);
	}

	/**
	 * Get the Expression for this NamedExpression
	 * 
	 * @return the Expression for this NamedExpression
	 */
	public Expression getExpression() {
		if (_expression == null) {
			_expression = getExpression(expString);
		}
		return _expression;
	}

	/**
	 * Obtain a valid expression from an expression string
	 * 
	 * @param expStr the expression string
	 * @return an Expression, or null;
	 */
	public static Expression getExpression(String expStr) {
		return getExpression(expStr, null);
	}

	/**
	 * Obtain a valid expression from an expression string
	 * 
	 * @param expStr the expression string
	 * @param tf an optional textfield for messages
	 * @return an Expression, or null;
	 */
	public static Expression getExpression(String expStr, JTextComponent tf) {
		if ((expStr == null) || expStr.isEmpty()) {
			return null;
		}

		// step 1, remove all white space
		expStr = expStr.replaceAll("\\s+", "");
		if (expStr.isEmpty()) {
			return null;
		}

		// step 2, tokenize
		String tokens[] = FileUtilities.tokens(expStr, ",)(+-*/%^");
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

		Expression expression = null;
		// it is technically ok if there are no vars--it means a constant expression

		try {
			if ((vars == null) || (vars.length < 1)) {
				expression = new ExpressionBuilder(expStr).variables().build();
			}
			else {
				expression = new ExpressionBuilder(expStr).variables(vars).build();
			}
		} catch (IllegalArgumentException ex) {
			expression = null;
			if (tf != null) {
				tf.setText("(IAE) Invalid expression. " + ex.getMessage());
			}
		}

		// valid?
		if (expression != null) {
			// don't check for set variables here
			ValidationResult vres = expression.validate(false);
			if (vres == ValidationResult.SUCCESS) {
				if (tf != null) {
					tf.setText("Valid expression");
				}
			}
			else {
				expression = null;
				String estr = "Invalid expression.";
				for (String es : vres.getErrors()) {
					estr += " [" + es + "]";
				}
				if (tf != null) {
					tf.setText(estr);
				}
			}
		}
		return expression;
	}

}
