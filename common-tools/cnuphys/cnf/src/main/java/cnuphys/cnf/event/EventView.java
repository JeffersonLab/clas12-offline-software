package cnuphys.cnf.event;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.cnf.event.table.NodePanel;

public class EventView extends BaseView {

	// holds the panel that has the table
	protected NodePanel _nodePanel;

	// singleton
	private static EventView instance;

	/**
	 * Constructor for the Event view, which manages events from an event file.
	 */
	private EventView() {

		super(PropertySupport.TITLE, "Current Event", PropertySupport.ICONIFIABLE, true, PropertySupport.MAXIMIZABLE,
				true, PropertySupport.CLOSABLE, true, PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, 1100,
				PropertySupport.HEIGHT, 650, PropertySupport.VISIBLE, true, PropertySupport.TOOLBAR, false);

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
	public static EventView createEventView() {

		if (instance == null) {
			instance = new EventView();
		}
		return instance;
	}

	/**
	 * Access to the singleton (might be <code>null</code>
	 * 
	 * @return the event view singleton.
	 */
	public static EventView getInstance() {
		return instance;
	}

}