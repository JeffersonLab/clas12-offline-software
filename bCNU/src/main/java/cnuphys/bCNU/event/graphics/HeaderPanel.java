package cnuphys.bCNU.event.graphics;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jlab.coda.jevio.BaseStructure;
import org.jlab.coda.jevio.BaseStructureHeader;

public class HeaderPanel extends JPanel {

	/** Number of bytes in selected structure. */
	private NamedLabel lengthLabel;

	/** Type of selected structure: BANK, SEGMENT, or TAGSEGMENT. */
	private NamedLabel structureLabel;

	/** Type of data in selected structure. */
	private NamedLabel dataTypeLabel;

	/** Tag of selected structure. */
	private NamedLabel tagLabel;

	/** Number (num) of selected structure. */
	private NamedLabel numberLabel;

	/** Description of selected structure from dictionary. */
	private NamedLabel descriptionLabel;

	/**
	 * Constructor.
	 */
	public HeaderPanel() {
		setLayout(new GridLayout(2, 1, 0, 3)); // rows, cols, hgap, vgap
		setBorder(new EmptyBorder(5, 5, 2, 0)); // top, left, bot, right

		structureLabel = new NamedLabel("structure", "description", 135);
		dataTypeLabel = new NamedLabel("data type", "description", 135);

		tagLabel = new NamedLabel("tag", "number", 80);
		numberLabel = new NamedLabel("number", "number", 80);

		lengthLabel = new NamedLabel("length", "description", 140);
		descriptionLabel = new NamedLabel("description", "description", 140);

		// limit size of labels
		Dimension d1 = structureLabel.getPreferredSize();
		Dimension d2 = descriptionLabel.getPreferredSize();

		structureLabel.setMaximumSize(d1);
		dataTypeLabel.setMaximumSize(d1);
		tagLabel.setMaximumSize(d1);
		numberLabel.setMaximumSize(d1);
		lengthLabel.setMaximumSize(d2);
		descriptionLabel.setMaximumSize(d2);

		JPanel p0 = createLayoutPanel();
		JPanel p1 = createLayoutPanel();

		p0.add(structureLabel);
		p0.add(Box.createRigidArea(new Dimension(5, 0)));
		p0.add(tagLabel);
		p0.add(Box.createRigidArea(new Dimension(5, 0)));
		p0.add(lengthLabel);

		p1.add(dataTypeLabel);
		p1.add(Box.createRigidArea(new Dimension(5, 0)));
		p1.add(numberLabel);
		p1.add(Box.createRigidArea(new Dimension(5, 0)));
		p1.add(descriptionLabel);

		add(p0);
		add(p1);
	}

	private JPanel createLayoutPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		return p;
	}

	/**
	 * Set the fields in the panel based on the data in the header.
	 * 
	 * @param structure
	 *            the structure to use to set the fields, mostly from its
	 *            header.
	 */
	public void setHeader(BaseStructure structure) {

		if ((structure == null) || (structure.getHeader() == null)) {
			structureLabel.setText("   ");
			lengthLabel.setText("   ");
			tagLabel.setText("   ");
			dataTypeLabel.setText("   ");
			numberLabel.setText("   ");
			descriptionLabel.setText("   ");
		} else {
			BaseStructureHeader header = structure.getHeader();
			structureLabel.setText("" + structure.getStructureType());
			lengthLabel.setText(4 * header.getLength() + " bytes");
			tagLabel.setText("" + header.getTag());
			dataTypeLabel.setText("" + header.getDataType());
			numberLabel.setText("" + header.getNumber());
			descriptionLabel.setText(structure.getDescription());
		}
	}

	/**
	 * Set the dictionary description in header panel.
	 * 
	 * @param structure
	 *            event being described
	 */
	public void setDescription(BaseStructure structure) {
		if (structure == null) {
			descriptionLabel.setText("   ");
			return;
		}
		descriptionLabel.setText(structure.getDescription());
	}

}