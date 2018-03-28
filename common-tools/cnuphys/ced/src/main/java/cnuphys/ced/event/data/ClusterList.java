package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

@SuppressWarnings("serial")
public class ClusterList extends Vector<Cluster> {

	//for reporting read errors
	private String _error;

	public ClusterList(String bankName) {
		

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		
		if ((sector == null) || (sector.length < 1)) {
			return;
		}

		int length = 0;

		
		byte layer[] = ColumnData.getByteArray(bankName + ".layer");
		float energy[] = ColumnData.getFloatArray(bankName + ".energy");
		float time[] = ColumnData.getFloatArray(bankName + ".time");
		float x[] = ColumnData.getFloatArray(bankName + ".x");
		float y[] = ColumnData.getFloatArray(bankName + ".y");
		float z[] = ColumnData.getFloatArray(bankName + ".z");
		
		length = checkArrays(sector, layer, energy, time, x, y, z);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			return;
		}
		
		for (int i = 0; i < length; i++) {
			add(new Cluster(sector[i], layer[i], energy[i], time[i], x[i], y[i], z[i]));
		}
		
	}
	

	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] layer, float[] energy,
			float time[], float[] x, float[] y, float[] z) {
		
		if ((sector == null) ||
				(x == null) || (y == null) || (y == null)) {
				
			_error = "Unexpected null array when creating HitList: " + "sector = null: " + (sector == null) + 
					"layer = null: " + (layer == null) + "time = null: " + (time == null) + 
					"energy = null: " + (energy == null) + 
					" x == null: " + (x == null) + " y == null: " + (y == null) + " z == null: " + (z == null);
					
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating CrossList";
			return -1;
		}
		
		if (lengthMismatch(sector, layer, "layer")) {
			return -1;
		}
		if (lengthMismatch(sector, time, "time")) {
			return -1;
		}
		if (lengthMismatch(sector, energy, "energy")) {
			return -1;
		}
		if (lengthMismatch(sector, x, "x")) {
			return -1;
		}
		if (lengthMismatch(sector, y, "y")) {
			return -1;
		}
		if (lengthMismatch(sector, z, "z")) {
			return -1;
		}

	

		return sector.length;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, byte[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating ClusterList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating ClusterList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, short[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating ClusterList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating ClusterList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, float[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating ClusterList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating ClusterList";
			return true;
		}
		return false;
	}
}
