package cnuphys.bCNU.attributes;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AttributePanel extends JPanel {

	private AttributeTable _attributeTable;
	
	public AttributePanel(Attributes attributes) {
		_attributeTable = new AttributeTable();
//		_attributeTable.setData(attributes.clone());
		_attributeTable.setData(attributes);
		setLayout(new BorderLayout(4, 4));
		
		add(_attributeTable.getScrollPane(), BorderLayout.CENTER);
		setBorder(BorderFactory.createEtchedBorder());
	}
	
	public AttributePanel(AttributeTable attributeTable) {
		_attributeTable = attributeTable;
		
		setLayout(new BorderLayout(4, 4));
		
		add(_attributeTable.getScrollPane(), BorderLayout.CENTER);
		setBorder(BorderFactory.createEtchedBorder());
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}
	
	/**
	 * Get the underlying table
	 * @return the underlying table
	 */
	public AttributeTable getAttributeTable() {
		return _attributeTable;
	}

}
