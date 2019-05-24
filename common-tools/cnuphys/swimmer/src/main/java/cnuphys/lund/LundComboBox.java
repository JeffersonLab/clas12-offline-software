package cnuphys.lund;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class LundComboBox extends JComboBox {

	private DefaultComboBoxModel _model;

	class LundComboBoxItem {
		public LundId lundId;

		public LundComboBoxItem(LundId lundId) {
			this.lundId = lundId;
		}

		@Override
		public String toString() {
			double massInMeV = 1000 * lundId.getMass();
			String s = String.format(" (%-7.3f MeV)", massInMeV);
			return lundId.getName() + s;
		}

	};

	/**
	 * Constructor for a particle combobox
	 * 
	 * @param chargedOnly if <> only add charged particles
	 * @param maxMass     the max mass added to the combobox, in MeV
	 * @param defaultId   e.g., 11 for electron
	 */
	public LundComboBox(boolean chargedOnly, double maxMass, int defaultId) {
		setEditable(false);
		LundComboBoxItem defaultItem = null;

		// convert maxMass to GeV
		maxMass /= 1000.;

		Vector<LundComboBoxItem> v = new Vector<LundComboBoxItem>();
		for (LundId lid : LundSupport.getInstance().getLundIds()) {
			if (lid.getMass() < maxMass) {
				if (!chargedOnly || (lid.getCharge() != 0)) {
					LundComboBoxItem item = new LundComboBoxItem(lid);
					v.add(item);
					if (lid.getId() == defaultId) {
						defaultItem = item;
					}
				}
			}
		}

		_model = new DefaultComboBoxModel(v);
		if (defaultItem != null) {
			_model.setSelectedItem(defaultItem);
		}

		setModel(_model);
	}

	// get the currently selected particle
	public LundId getSelectedId() {
		return ((LundComboBoxItem) (_model.getSelectedItem())).lundId;
	}

	public static void main(String args[]) {
		javax.swing.JFrame testFrame = new javax.swing.JFrame("test frame");
		java.awt.Container cp = testFrame.getContentPane();
		testFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

		cp.setLayout(new BorderLayout(4, 0));

		LundComboBox lcb = new LundComboBox(true, 950.0, 11);
		cp.add(lcb, BorderLayout.NORTH);

		testFrame.pack();
		testFrame.setVisible(true);
	}
}
