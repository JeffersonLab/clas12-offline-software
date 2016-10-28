package cnuphys.ced.cedview.gemcview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.io.evio.EvioDataEvent;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.data.GEMCMetaDataContainer;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;

public class GEMCView extends BaseView implements IClasIoEventListener {

	// the event manager
	protected ClasIoEventManager _eventManager = ClasIoEventManager
			.getInstance();

	protected String _currentFile;

	// holds the event file
	public JLabel _eventFileName;

	// gemc gcard file
	public JLabel _gcardName;

	// torus
	public JLabel _torus;

	// solenoid
	public JLabel _solenoid;

	// table
	private GEMCMetaDataTable _gemcTable;

	public GEMCView() {
		super(PropertySupport.TITLE, "GEMC Options", PropertySupport.ICONIFIABLE,
				true, PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE,
				true, PropertySupport.RESIZABLE, true, PropertySupport.WIDTH,
				GEMCMetaDataTable.preferredWidth(), PropertySupport.HEIGHT, 650,
				PropertySupport.LEFT, 700, PropertySupport.TOP, 100,
				PropertySupport.VISIBLE, true);

		setLayout(new BorderLayout(4, 4));

		add(getNorthPanel(), BorderLayout.NORTH);

		_gemcTable = new GEMCMetaDataTable();

		add(_gemcTable.getScrollPane(), BorderLayout.CENTER);

		// need to listen for events
		_eventManager.addClasIoEventListener(this, 1);
		validate();
	}

	// get the panel for the north
	private JPanel getNorthPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		_eventFileName = newLabel("   EVIO file: ", "", panel);
		panel.add(Box.createVerticalStrut(4));

		_gcardName = newLabel("  GCard file: ", "  no GEMC metadata found  ",
				panel);
		panel.add(Box.createVerticalStrut(4));

		_torus = newLabel("       Torus: ", "", panel);
		panel.add(Box.createVerticalStrut(4));

		_solenoid = newLabel("    Solenoid: ", "", panel);

		return panel;
	}

	private JLabel newLabel(String prompt, String defText, JPanel parent) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

		JLabel plab = new JLabel(prompt);
		plab.setText(prompt);
		plab.setOpaque(true);
		plab.setBackground(Color.yellow);
		plab.setFont(Fonts.smallMono);
		plab.setForeground(Color.black);

		JLabel lab = new JLabel();
		lab.setOpaque(true);
		lab.setBackground(Color.black);
		lab.setForeground(Color.cyan);
		lab.setFont(Fonts.mediumFont);
		lab.setText(defText);

		p.add(plab);
		p.add(lab);

		p.setBorder(new CommonBorder());

		parent.add(p);
		return lab;
	}

	/**
	 * New fast mc event
	 * @param event the generated physics event
	 */
	public void newFastMCGenEvent(PhysicsEvent event) {
	}
	

	@Override
	public void newClasIoEvent(EvioDataEvent event) {
		GEMCMetaDataContainer gemcdata = _eventManager.getGEMCMetaData();

		if (!gemcdata.resetFields) {
	//		System.err.println("Skipped setting fields.");
			return;
		}

		// getHitCount returns the number of properties. This should
		// awlays be zero except for event#1 in a gemc file
		if (gemcdata.getHitCount(0) > 0) {
			// don't want to resent every event
			gemcdata.resetFields = false;
			String gcard = gemcdata.getGCard();
			gcard = (gcard == null) ? "" : gcard;
			_gcardName.setText("  " + gcard + "  ");

			boolean hasTorus = gemcdata.hasTorus();
			boolean hasSolenoid = gemcdata.hasSolenoid();
			double torusScale = gemcdata.torusScaleFactor();
			double solenoidScale = gemcdata.solenoidScaleFactor();

			// fields
			setFieldLabel(_torus, hasTorus, torusScale);
			setFieldLabel(_solenoid, hasSolenoid, solenoidScale);

			System.err.println("Setting fields from GEMC meta data");
			configureFields(hasTorus, torusScale, hasSolenoid, solenoidScale);

		} else {
			_gcardName.setText("  no GEMC metadata found  ");
		}
	}

	private void configureFields(boolean hasTorus, double torusScale,
			boolean hasSolenoid, double solenoidScale) {

		FieldType newFieldType = FieldType.ZEROFIELD;
		if (hasTorus && hasSolenoid) {
			newFieldType = FieldType.COMPOSITE;
		} else if (hasTorus) {
			newFieldType = FieldType.TORUS;
		} else if (hasSolenoid) {
			newFieldType = FieldType.SOLENOID;
		}
		MagneticFields.getInstance().setActiveField(newFieldType);

		// now the scales
		if (hasTorus) {
			setScale((MagneticField) MagneticFields.getInstance().getIField(FieldType.TORUS),
					torusScale);
		}

		if (hasSolenoid) {
			setScale(
					(MagneticField) MagneticFields
							.getInstance().getIField(FieldType.SOLENOID),
					solenoidScale);
		}

	}

	private void setScale(MagneticField field, double scale) {
		if (field != null) {
			field.setScaleFactor(scale);
		}
	}

	private void setFieldLabel(JLabel label, boolean hasField, double scale) {
		if (hasField) {
			String s = String.format("  on  scale = %5.2f  ", scale);
			label.setText(s);
		} else {
			label.setText("  off  ");
		}
	}

	@Override
	public void openedNewEventFile(String path) {
		clear();
		_currentFile = (path == null) ? "" : path;
		_eventFileName.setText("   " + _currentFile + "    ");
		setData(null);
	}
	
	/**
	 * Change the event source type
	 * @param source the new source: File, ET, FastMC
	 */
	@Override
	public void changedEventSource(ClasIoEventManager.EventSourceType source) {
	}

	/**
	 * Clear all information
	 */
	public void clear() {
		_currentFile = null;
		_eventFileName.setText("   ");
		_gcardName.setText("  no GEMC metadata found  ");
	}

	/**
	 * Get the underlying GEMC table
	 * 
	 * @return the underlying GEMC table
	 */
	public GEMCMetaDataTable getGEMCTable() {
		return _gemcTable;
	}

	public void setData(Vector<String> properties) {
		getGEMCTable().getGEMCMetaDataModel().setData(properties);
	}

}
