package cnuphys.fastMCed.view.data;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;

import cnuphys.fastMCed.fastmc.AugmentedDetectorHit;
import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.lund.LundId;

public class DataTableModel extends DefaultTableModel {

	public static final int COL_ROW = 0;
	public static final int COL_ID = 1;
	public static final int COL_NAME = 2;
	public static final int COL_SECTOR = 3;
	public static final int COL_SUPERLAYER = 4;
	public static final int COL_LAYER = 5;
	public static final int COL_COMPONENT = 6;

	// the witdh of the columns
	protected static final int columnWidths[] = { 50, // ROW
			70, // ID
			70, // name
			70, // sector
			70, // superlayer
			70, // layer
			70, // component
	};

	// which detector
	private DetectorId _detectorId;

	// the model data
	protected Vector<HitAndID> _data = new Vector<HitAndID>(25);

	/**
	 * Constructor
	 */
	public DataTableModel(DetectorId detectorId) {
		super(getColumnNames(detectorId), 2);
		_detectorId = detectorId;
	}

	// get the preferred width
	public static int getPreferredWidth() {
		int w = 20;

		for (int cw : columnWidths) {
			w += cw;
		}
		return w;
	}

	//modify the column names based on detector id
	private static String[] getColumnNames(DetectorId detector) {
		String[] cnames = { "Index", "PID", "Name", "Sector", "Superlayer", "Layer", "Component" };

		switch (detector) {
		case DC:
			cnames[COL_COMPONENT] = "Wire";
			break;

		case FTOF:
			cnames[COL_COMPONENT] = "Paddle";
			break;

		default:
		}

		return cnames;
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return columnWidths.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		return (_data == null) ? 0 : _data.size();
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		if (row < _data.size()) {
			HitAndID hitId = _data.get(row);
			if (hitId != null) {

				//everything in DetectorHit is zero based
				DetectorHit hit = hitId.hit;

				switch (col) {

				case COL_ROW:
					return "" + (row + 1);

				case COL_ID:
					return "" + hitId.intId;

				case COL_NAME:
					return "" + hitId.name;

				case COL_SECTOR:
					return "" + (hit.getSectorId() + 1);

				case COL_SUPERLAYER:
					return "" + (hit.getSuperlayerId() + 1);

				case COL_LAYER:
					return "" + (hit.getLayerId() + 1);

				case COL_COMPONENT:
					return "" + (hit.getComponentId() + 1);

				}
			}
		}

		return null;
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		if (_data != null) {
			_data.clear();
		}
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(List<ParticleHits> plist) {
		_data.clear();

		if (plist != null) {
			for (ParticleHits phits : plist) {

				List<AugmentedDetectorHit> hitList = phits.getAllHits(_detectorId);

				if (hitList != null) {
					for (AugmentedDetectorHit aughit : hitList) {
						HitAndID hitID = new HitAndID(phits.getLundId(), aughit);
						_data.add(hitID);
					}
				}
			} // for phits
		}
		fireTableDataChanged();
	}

	/**
	 * Forces the cell to not be editable.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	class HitAndID extends Vector<AugmentedDetectorHit> {
		public final String name;
		public final AugmentedDetectorHit augHit;
		public final DetectorHit hit;
		public final int intId;

		public HitAndID(LundId lid, AugmentedDetectorHit augHit) {
			this.augHit = augHit;
			hit = augHit._hit;
			name = (lid == null) ? "???" : lid.getName();
			intId = (lid == null) ? -999 : lid.getId();
		}
	}
}
