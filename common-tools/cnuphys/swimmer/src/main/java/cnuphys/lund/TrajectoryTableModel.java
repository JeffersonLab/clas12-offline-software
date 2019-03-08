package cnuphys.lund;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class TrajectoryTableModel extends DefaultTableModel {

	private static final String SUBZERO = "\u2080";
	private static final String SMALL_THETA = "\u03B8";
	private static final String SMALL_PHI = "\u03C6";

	// the names of the columns
	protected static final String colNames[] = { "Id", "PID", "name", "m (MeV)", "q", "x" + SUBZERO + " (cm)",
			"y" + SUBZERO + " (cm)", "z" + SUBZERO + " (cm)", "p (MeV)", SMALL_THETA + " (deg)", SMALL_PHI + " (deg)",
			"KE (MeV)", "Et (MeV)", "status", "source" };

	protected static final int columnWidths[] = { 70, // track id
			70, // lund id
			82, // name
			137, // mass
			60, // charge
			122, // xo
			122, // yo
			122, // zo
			142, // p
			122, // theta
			122, // phi
			147, // KE
			147, // Etot
			70, // status
			280 // source
	};

	// the model data
	protected Vector<TrajectoryRowData> data = new Vector<TrajectoryRowData>(25);

	/**
	 * Constructor
	 */
	public TrajectoryTableModel() {
		super(colNames, 2);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return the number of columns
	 */
	@Override
	public int getColumnCount() {
		return colNames.length;
	}

	/**
	 * Get the number of rows
	 * 
	 * @return the number of rows
	 */
	@Override
	public int getRowCount() {
		if (data == null) {
			return 0;
		}
		return data.size();
	}

	/**
	 * Get the value at a given row and column
	 * 
	 * @return the value at a given row and column
	 */
	@Override
	public Object getValueAt(int row, int col) {

		if (row < data.size()) {
			TrajectoryRowData traj = data.get(row);
			if (traj != null) {
				switch (col) {

				case 0:
					return "" + traj.getTrackId();

				case 1:
					// HACK for HB, TB tracks with no pid
					int id = traj.getId();
					if ((id == -99) || (id == -100) || (id == -101) || (id == -199) || (id == -200) || (id == -201)) {
						return "---";
					} else {
						return "" + id;
					}

				case 2:
					return traj.getName();

				case 3:
					return getStr(traj.getMass(), 3);

				case 4:
					return "" + traj.getCharge();

				case 5:
					return getStr(traj.getXo(), 3);

				case 6:
					return getStr(traj.getYo(), 3);

				case 7:
					return getStr(traj.getZo(), 3);

				case 8:
					return getStr(traj.getMomentum(), 3);

				case 9:
					return getStr(traj.getTheta(), 3);

				case 10:
					return getStr(traj.getPhi(), 3);

				case 11:
					return getStr(traj.getKineticEnergy(), 3);

				case 12:
					return getStr(traj.getMass() + traj.getKineticEnergy(), 3);

				case 13:
					return "" + traj.getStatus();

				case 14:
					return traj.getSource();
				}
			}
		}

		return null;
	}

	// convenience method to get string with spec. number of digits after dec.
	private String getStr(double value, int numdec) {
		return DoubleFormat.doubleFormat(value, numdec);
	}

	/**
	 * Clear all the data
	 */
	public void clear() {
		if (data != null) {
			data.clear();
		}
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Vector<TrajectoryRowData> data) {
		this.data = data;
		fireTableDataChanged();
	}

	/**
	 * Forces the cell to not be editable.
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}