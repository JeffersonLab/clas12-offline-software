package cnuphys.ced.clasio.table;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioNode;

import cnuphys.bCNU.event.graphics.NamedLabel;
import cnuphys.ced.clasio.EvioNodeSupport;

public class NodeSummaryPanel extends JPanel {

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
    public NodeSummaryPanel() {
	setLayout(new GridLayout(2, 1, 0, 3)); // rows, cols, hgap, vgap
	setBorder(new EmptyBorder(5, 5, 2, 0)); // top, left, bot, right

	structureLabel = new NamedLabel("structure", "structure", 120);
	dataTypeLabel = new NamedLabel("data type", "structure", 120);

	tagLabel = new NamedLabel("tag", "number", 60);
	numberLabel = new NamedLabel("number", "number", 60);

	lengthLabel = new NamedLabel("length", "description", 160);
	descriptionLabel = new NamedLabel("description", "description", 160);

	// limit size of labels
	Dimension d1 = structureLabel.getPreferredSize();
	Dimension d2 = numberLabel.getPreferredSize();
	Dimension d3 = descriptionLabel.getPreferredSize();

	structureLabel.setMaximumSize(d1);
	dataTypeLabel.setMaximumSize(d1);
	tagLabel.setMaximumSize(d2);
	numberLabel.setMaximumSize(d2);
	lengthLabel.setMaximumSize(d3);
	descriptionLabel.setMaximumSize(d3);

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
     * Set the fields in the panel based on the data in the node.
     * 
     * @param node
     *            the node to use
     */
    public void setSummary(EvioNode node) {

	if (node == null) {
	    structureLabel.setText("   ");
	    lengthLabel.setText("   ");
	    tagLabel.setText("   ");
	    dataTypeLabel.setText("   ");
	    numberLabel.setText("   ");
	    descriptionLabel.setText("   ");
	} else {
	    structureLabel.setText(DataType.getName(node.getType()));
	    lengthLabel.setText(4 * node.getDataLength() + " bytes");
	    tagLabel.setText("" + node.getTag());
	    dataTypeLabel.setText(DataType.getName(node.getDataType()));
	    numberLabel.setText("" + node.getNum());
	    descriptionLabel.setText(EvioNodeSupport.getName(node));
	}
    }
}