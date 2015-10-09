/**
 * 
 */
package cnuphys.bCNU.attributes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;

@SuppressWarnings("serial")
public class AttributeDoubleEditor extends JFormattedTextField implements
		PropertyChangeListener {

	/**
	 * The attribute name.
	 */
	protected String attributeName = null;

	/**
	 * The owner table.
	 */
	protected AttributeTable attributeTable = null;

	protected static NumberFormat numberFormat = NumberFormat
			.getNumberInstance();
	static {
		numberFormat.setMinimumFractionDigits(8);
	}

	/**
	 * Create an editor for Doubles.
	 * 
	 * @param attributeTable
	 * @param attributeName
	 * @param startValue
	 */

	public AttributeDoubleEditor(AttributeTable attributeTable,
			String attributeName, Double startValue) {

		super(numberFormat);
		this.attributeTable = attributeTable;
		this.attributeName = attributeName;

		if ((attributeTable == null) || (attributeName == null)) {
			return;
		}

		setValue(startValue);
		setColumns(12);
		addPropertyChangeListener("value", this);

		setBorder(null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			double newValue = ((Number) getValue()).doubleValue();
			attributeTable.setAttribute(attributeName, newValue);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
