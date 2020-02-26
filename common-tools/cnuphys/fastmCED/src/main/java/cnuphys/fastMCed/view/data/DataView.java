package cnuphys.fastMCed.view.data;


import java.util.List;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.DetectorId;

import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.fastMCed.eventgen.AEventGenerator;
import cnuphys.fastMCed.eventio.IPhysicsEventListener;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.fastmc.ParticleHits;

public class DataView extends BaseView implements
		IPhysicsEventListener {

	protected DataTable _dataTable;

	public DataView(String title, DetectorId detector) {
		super(PropertySupport.TITLE, title, PropertySupport.ICONIFIABLE, true,
				PropertySupport.TOOLBAR, false,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE, true,
				PropertySupport.RESIZABLE, true, PropertySupport.WIDTH, DataTableModel.getPreferredWidth(),
				PropertySupport.HEIGHT, 700, PropertySupport.LEFT, 700,
				PropertySupport.TOP, 100, PropertySupport.VISIBLE, true);

		_dataTable = new DataTable(detector);
		add(_dataTable.getScrollPane());
		// need to listen for events
		PhysicsEventManager.getInstance().addPhysicsListener(this, 1);
	}

	/**
	 * A new event generator is active
	 * @param generator the now active generator
	 */
	@Override
	public void newEventGenerator(final AEventGenerator generator) {
	}

	@Override
	public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
		_dataTable.getDataModel().setData(particleHits);
	}

}