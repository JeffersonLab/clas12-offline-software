package cnuphys.ced.alldata.graphics;

import java.util.Properties;
import java.util.Vector;

import javax.swing.text.JTextComponent;
import javax.xml.stream.XMLStreamException;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.xml.XmlPrintStreamWritable;
import cnuphys.bCNU.xml.XmlPrintStreamWriter;
import cnuphys.ced.alldata.ColumnData;
import cnuphys.ced.alldata.DataManager;
import net.oh.exp4j.Expression;
import net.oh.exp4j.ExpressionBuilder;
import net.oh.exp4j.ValidationResult;

public class NamedExpression implements Comparable<NamedExpression>, XmlPrintStreamWritable {

	/** The expression name */
	protected String _expName;

	/** The expression definition string */
	protected String _expString;

	/** The actual expression */
	protected Expression _expression;
	
	/** The variables of the expression */
	protected String[] _variables;
	
	/** A matching array of ColumnData objects */
	protected ColumnData[] _columnData;
	
	public NamedExpression(String eName, String eString) {
		_expName = eName;
		_expString = eString;
	}

	@Override
	public int compareTo(NamedExpression o) {
		String lcv = _expName.toLowerCase();
		String lco = o._expName.toLowerCase();
		return lcv.compareTo(lco);
	}

	/**
	 * Get the expression name
	 * @return the expression name
	 */
	public String getExpressionName() {
		return _expName;
	}
	
	/**
	 * Get the Expression for this NamedExpression
	 * 
	 * @return the Expression for this NamedExpression
	 */
	public Expression getExpression() {
		if (_expression == null) {
			_variables = getVariables(_expString);
			if ((_variables != null) && (_variables.length > 0)) {
				_columnData = new ColumnData[_variables.length];
				for (int i = 0; i < _variables.length; i++) {
					_columnData[i] = null;
					NameBinding nb = DefinitionManager.getInstance().getNameBinding(_variables[i]);
					if (nb != null) {
						_columnData[i] = DataManager.getInstance().getColumnData(nb.bankColumnName);
					}
					
					System.err.println("var name: [" + _variables[i] + "]  columnData: " + _columnData[i]);
				} //end for i
			}
			_expression = getExpression(_expString);
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
	 * Get an array of variables from the expression string
	 * @param expStr the expression string
	 * @return an array of variables which should have a leading underscore
	 */
	private static String[] getVariables(String expStr) {
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

//		for (String token : tokens) {
//			System.err.println("TOKEN [" + token + "]");
//		}

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
//			for (String v : vars) {
//				System.err.println("VARIABLE [" + v + "]");
//			}
		}
		
		return vars;
	}
	
	/**
	 * Obtain a valid expression from an expression string
	 * 
	 * @param expStr the expression string
	 * @param tf an optional textfield for messages
	 * @return an Expression, or null;
	 */
	public static Expression getExpression(String expStr, JTextComponent tf) {

        // get the variables
		String vars[] = getVariables(expStr);

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

	public int minLength(DataEvent event) {
		if (!readyToCompute()) {
			return 0;
		}
		
		int len = (_columnData == null) ? 0 : _columnData.length;
		if (len < 1) {
			return 0;
		}
		
		double a[] = _columnData[0].getAsDoubleArray(event);
		int dataLen = (a == null) ? 0 : a.length;
		
		for (int i = 1; i < len; i++) {
			a = _columnData[i].getAsDoubleArray(event);
			int alen = (a == null) ? 0 : a.length;
			dataLen = Math.min(alen, dataLen);
		}
		return dataLen;
	}

	/**
	 * The named expression is ready to compute if it has an expression,
	 * and the length of the variable array is the length of the ColumnData
	 * array, and not of the ColumnData elements are <code>null</code>.
	 * @return <code>true</code> if the named expression is eady to compute
	 */
	public boolean readyToCompute() {
		getExpression();
		if (_expression == null) {
			return false;
		}
		
		int vlen = (_variables == null) ? 0 : _variables.length;
		int clen = (_columnData == null) ? 0 : _columnData.length;
		
		if (vlen != clen) {
			return false;
		}
		
		for (int i = 0; i < clen; i++) {
			if (_columnData[i] == null) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Compute the value of the expression
	 * @param index the index into the data arrays
	 * @return the value
	 */
	public double value(DataEvent event, int index) {
		if (readyToCompute()) {
			int vlen = (_variables == null) ? 0 : _variables.length;
			for (int i = 0; i < vlen; i++) {
				double val[] = _columnData[i].getAsDoubleArray(event);
				if (val == null) {
					return Double.NaN;
				}
	//			System.err.println("EXP INDX: " + i + "  val = " + val[i]);
				_expression.setVariable(_variables[i], val[index]);
			}
			return _expression.evaluate();
		}
		
		return Double.NaN;
	}

	@Override
	public void writeXml(XmlPrintStreamWriter xmlPrintStreamWriter) {
		Properties props = new Properties();
		props.put(XmlUtilities.XmlName, _expName);
		props.put(XmlUtilities.XmlDefinition, _expString);
		try {
			xmlPrintStreamWriter.writeElementWithProps(XmlUtilities.XmlExpression, props);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}
	
}
