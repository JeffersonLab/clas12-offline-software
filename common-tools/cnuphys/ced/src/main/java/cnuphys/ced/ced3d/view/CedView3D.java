package cnuphys.ced.ced3d.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.ced3d.CedPanel3D;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

public abstract class CedView3D extends BaseView
		implements IClasIoEventListener, SwimTrajectoryListener, IAccumulationListener, ActionListener {

	// the menu bar
	private final JMenuBar _menuBar;

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// the 3D panel
	protected final CedPanel3D _panel3D;
	
	//for appending event number to the titile
	private static final String evnumAppend = "  (Seq Event# ";


	// menu
	private JMenuItem _printMenuItem;
	private JMenuItem _pngMenuItem;
	private JMenuItem _refreshItem;

	/**
	 * Create a 3D view
	 * 
	 * @param title
	 * @param angleX
	 * @param angleY
	 * @param angleZ
	 * @param xDist
	 * @param yDist
	 * @param zDist
	 */
	public CedView3D(String title, float angleX, float angleY, float angleZ, float xDist, float yDist, float zDist) {
		super(PropertySupport.TITLE, title, PropertySupport.ICONIFIABLE, true, PropertySupport.MAXIMIZABLE, true,
				PropertySupport.CLOSABLE, true, PropertySupport.RESIZABLE, true, PropertySupport.VISIBLE, true);

		_eventManager.addClasIoEventListener(this, 2);

		// listen for trajectory changes
		Swimming.addSwimTrajectoryListener(this);

		_menuBar = new JMenuBar();
		setJMenuBar(_menuBar);
		addMenus();

		setLayout(new BorderLayout(1, 1));
		_panel3D = make3DPanel(angleX, angleY, angleZ, xDist, yDist, zDist);

		add(_panel3D, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(1), BorderLayout.WEST);
		pack();
		AccumulationManager.getInstance().addAccumulationListener(this);
	}

	// make the 3d panel
	protected abstract CedPanel3D make3DPanel(float angleX, float angleY, float angleZ, float xDist, float yDist,
			float zDist);

	// add the menus
	private void addMenus() {
		JMenu actionMenu = new JMenu("ced3D");
		_printMenuItem = new JMenuItem("Print...");
		_printMenuItem.addActionListener(this);
		actionMenu.add(_printMenuItem);

		_pngMenuItem = new JMenuItem("Save as PNG...");
		_pngMenuItem.addActionListener(this);
		actionMenu.add(_pngMenuItem);

		_refreshItem = new JMenuItem("Refresh");
		_refreshItem.addActionListener(this);
		actionMenu.add(_refreshItem);

		_menuBar.add(actionMenu);
	}

	@Override
	public void newClasIoEvent(DataEvent event) {
		if (!_eventManager.isAccumulating()) {
			fixTitle(event);
			_panel3D.refreshQueued();
		}
	}

	@Override
	public void openedNewEventFile(String path) {
		_panel3D.refreshQueued();
	}

	/**
	 * Change the event source type
	 * 
	 * @param source the new source: File, ET
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			fixTitle(_eventManager.getCurrentEvent());
			_panel3D.refresh();
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			fixTitle(_eventManager.getCurrentEvent());
			_panel3D.refresh();
			break;
		}
	}

	@Override
	public void trajectoriesChanged() {
		if (!_eventManager.isAccumulating()) {
			_panel3D.refreshQueued();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();
		if (source == _printMenuItem) {
			PrintUtilities.printComponent(_panel3D);
		} else if (source == _pngMenuItem) {
			GraphicsUtilities.saveAsPng(_panel3D);
		} else if (source == _refreshItem) {
			_panel3D.refresh();
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (_panel3D != null) {
			_panel3D.requestFocus();
		}
	}

	@Override
	public void refresh() {
		if (_panel3D != null) {
			_panel3D.refresh();
		}
	}
	
	/**
	 * Fix the title of the view after an event arrives. The default is to append
	 * the event number.
	 * 
	 * @param event the new event
	 */
	protected void fixTitle(DataEvent event) {
		String title = getTitle();
		int index = title.indexOf(evnumAppend);
		if (index > 0) {
			title = title.substring(0, index);
		}

		int seqNum = _eventManager.getSequentialEventNumber();
		int trueNum = _eventManager.getTrueEventNumber();
		if (seqNum > 0) {
			if (trueNum > 0) {
				setTitle(title + evnumAppend + seqNum + "  True event# " + trueNum + ")");
			}
			else {
			    setTitle(title + evnumAppend + seqNum + ")");
			}
		}
	}


	/**
	 * Tests whether this listener is interested in events while accumulating
	 * 
	 * @return <code>true</code> if this listener is NOT interested in events while
	 *         accumulating
	 */
	@Override
	public boolean ignoreIfAccumulating() {
		return true;
	}

}
