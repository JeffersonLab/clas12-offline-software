package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.table.NodePanel;

public class ClasIoEventView extends BaseView {

	// holds the panel that has the table
	protected NodePanel _nodePanel;

	// singleton
	private static ClasIoEventView instance;

	/**
	 * Constructor for the Event view, which manages events from an event file.
	 */
	private ClasIoEventView() {

		super(AttributeType.TITLE, "Evio Event", AttributeType.ICONIFIABLE,
				true, AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE,
				true, AttributeType.RESIZABLE, true, AttributeType.WIDTH, 950,
				AttributeType.HEIGHT, 600, AttributeType.VISIBLE, true);

		JPanel sPanel = new JPanel();
		sPanel.setLayout(new BorderLayout(2, 2));
		_nodePanel = new NodePanel();

		sPanel.add(_nodePanel, BorderLayout.CENTER);
		add(sPanel);
		pack();
		fixSize();
	}

	// a fixed fraction of the screen
	private void fixSize() {
		Dimension d = GraphicsUtilities.screenFraction(0.80);
		Dimension size = getSize();
		size.height = Math.min(size.height, d.height);
		setSize(size);
	}

	/**
	 * Create the event view, or return the already created singleton.
	 * 
	 * @return the event view singleton.
	 */
	public static ClasIoEventView createEventView() {

		if (instance == null) {
			instance = new ClasIoEventView();
		}
		return instance;
	}

	/**
	 * Access to the singleton (might be <code>null</code>
	 * 
	 * @return the event view singleton.
	 */
	public static ClasIoEventView getInstance() {
		return instance;
	}

}