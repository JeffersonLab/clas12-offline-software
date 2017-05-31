package cnuphys.bCNU.component;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;

public class EnumCheckBoxPanel extends JPanel {

	// reverse hash which takes an enum and gives the checkbox
	protected Hashtable<Enum<?>, JCheckBox> forwardHash;

	// reverse hash which takes a checkbox and gives the enum
	protected Hashtable<JCheckBox, Enum<?>> reverseHash;

	/**
	 * Create a panel of JCheckBox objects from an enum map.
	 * 
	 * @param enumMap
	 *            an EnumMap<enum, String>. The enum is the key, and the string,
	 *            which will become the label, is the value.
	 */
	public EnumCheckBoxPanel(EnumMap<?, String> enumMap,
			ItemListener itemListener, Font font) {
		Set<?> keySet = enumMap.keySet();

		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		forwardHash = new Hashtable<Enum<?>, JCheckBox>(2 * keySet.size() + 1);
		reverseHash = new Hashtable<JCheckBox, Enum<?>>(2 * keySet.size() + 1);

		// in this enumMap the enum is the key, and a string is the value
		for (Object key : keySet) {
			String niceName = enumMap.get(key);
			JCheckBox cb = new JCheckBox(niceName, true);
			GraphicsUtilities.setSizeSmall(cb);
			cb.setFont(font);
			cb.addItemListener(itemListener);
			forwardHash.put((Enum<?>) key, cb);
			reverseHash.put(cb, (Enum<?>) key);
			add(cb);
		}
	}

	/**
	 * Get the button state of the corresponding enum
	 * 
	 * @param key
	 * @return true if the checkbox is selected
	 */
	public boolean isSelected(Enum<?> key) {
		return getCheckBox(key).isSelected();
	}

	/**
	 * Set the button state of the corresponding enum
	 * 
	 * @param key
	 * @param state
	 */
	public void setSelected(Enum<?> key, boolean state) {
		getCheckBox(key).setSelected(state);
	}

	/**
	 * Obtain the enum from the checkbox
	 * 
	 * @param cb
	 *            the check box in question.
	 * @return the corresponding enum.
	 */
	public Enum<?> getEnum(JCheckBox cb) {
		return reverseHash.get(cb);
	}

	public JCheckBox getCheckBox(Enum<?> key) {
		return forwardHash.get(key);
	}

	public int getVisibilityIndex(Enum<?> key, Enum<?> values[]) {
		JCheckBox cb = getCheckBox(key);
		if (cb.isSelected()) {

			int visIndex = 0;
			for (int i = 0; i < key.ordinal(); i++) {
				cb = getCheckBox(values[i]);
				if (cb.isSelected()) {
					visIndex++;
				}
			}
			return visIndex;
		}
		return -1;
	}
}
