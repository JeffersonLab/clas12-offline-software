package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.bCNU.attributes.AttributeType;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;
import cnuphys.lund.SwimTrajectoryListener;
import cnuphys.swim.Swimming;

public abstract class CedView3D extends BaseView implements
		IClasIoEventListener, SwimTrajectoryListener, IAccumulationListener,
		ActionListener {

	// the menu bar
	private final JMenuBar _menuBar;

	// the event manager
	private final ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	// the 3D panel
	private final CedPanel3D _panel3D;

	// menu
	private JMenuItem _printMenuItem;
	private JMenuItem _pngMenuItem;
	private JMenuItem _refreshItem;

	public CedView3D(String title, float angleX, float angleY, float angleZ,
			float xDist, float yDist, float zDist) {
		super(AttributeType.TITLE, title, AttributeType.ICONIFIABLE, true,
				AttributeType.MAXIMIZABLE, true, AttributeType.CLOSABLE, true,
				AttributeType.RESIZABLE, true, AttributeType.VISIBLE, true);

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

	protected abstract CedPanel3D make3DPanel(float angleX, float angleY,
			float angleZ, float xDist, float yDist, float zDist);

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
	public void newClasIoEvent(EvioDataEvent event) {
		if (!_eventManager.isAccumulating()) {
			// setData(event);
			// setEventNumber(_eventManager.getEventNumber());
			// fixButtons();
			_panel3D.refresh();
		}
	}

	@Override
	public void openedNewEventFile(String path) {
		_panel3D.refresh();
	}

	@Override
	public void accumulationEvent(int reason) {
		switch (reason) {
		case AccumulationManager.ACCUMULATION_STARTED:
			break;

		case AccumulationManager.ACCUMULATION_CANCELLED:
			_panel3D.refresh();
			break;

		case AccumulationManager.ACCUMULATION_FINISHED:
			_panel3D.refresh();
			break;
		}
	}

	@Override
	public void trajectoriesChanged() {
		if (!_eventManager.isAccumulating()) {
			_panel3D.refresh();
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

}
