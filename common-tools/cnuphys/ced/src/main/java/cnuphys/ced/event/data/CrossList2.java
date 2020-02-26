package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class CrossList2 extends Vector<Cross2> {
	
	private String _error;

	public CrossList2(String bankName) {

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;

		byte[] region = ColumnData.getByteArray(bankName + ".region");;
		short[] id = ColumnData.getShortArray(bankName + ".ID");;
        float[] x = ColumnData.getFloatArray(bankName + ".x");
        float[] y = ColumnData.getFloatArray(bankName + ".y");
        float[] z = ColumnData.getFloatArray(bankName + ".z");
        float[] ux = ColumnData.getFloatArray(bankName + ".ux");
        float[] uy = ColumnData.getFloatArray(bankName + ".uy");
        float[] uz = ColumnData.getFloatArray(bankName + ".uz");
        float[] err_x = ColumnData.getFloatArray(bankName + ".err_x");
        float[] err_y = ColumnData.getFloatArray(bankName + ".err_y");
        float[] err_z = ColumnData.getFloatArray(bankName + ".err_z");

               
		length = checkArrays(sector, region, id, x, y, z, ux, uy, uz, 
				err_x, err_y, err_z);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			return;
		}

//		public Cross(byte sector, byte region, short id, float x, float y, float z, float ux, float uy, float uz,
//				float err_x, float err_y, float err_z, float err_ux, float err_uy, float err_uz) {

		for (int i = 0; i < length; i++) {
			add(new Cross2(sector[i], region[i], id[i],
					x[i], y[i], z[i],
					ux[i], uy[i], uz[i],
					err_x[i], err_y[i], err_z[i]));
		}

	}
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] region, short[] id,
			float[] x, float[] y, float[] z, 
			float[] ux, float[] uy, float[] uz, 
			float[] err_x, float[] err_y, float[] err_z) {
		
		if ((sector == null) || (region == null) || (id == null) ||
				(x == null) || (y == null) || (y == null)|| 
				(ux == null) || (uy == null) || (uy == null)|| 
				(err_x == null) || (err_y == null) || (err_y == null)) {
				
			_error = "Unexpected null array when creating CrossList2: " + "sector = null: " + (sector == null)
					+ " region == null: " + (region == null) + " id == null: " + (id == null) +
					" x == null: " + (x == null) + " y == null: " + (y == null) + " z == null: " + (z == null) +
					" ux == null: " + (ux == null) + " uy == null: " + (uy == null) + " uz == null: " + (uz == null) +
					" err_x == null: " + (err_x == null) + " err_y == null: " + (err_y == null) + " err_z == null: " + (err_z == null);
					
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating CrossList2";
			return -1;
		}
		
		if (lengthMismatch(sector, region, "region")) {
			return -1;
		}
		if (lengthMismatch(sector, id, "id")) {
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
		if (lengthMismatch(sector, ux, "ux")) {
			return -1;
		}
		if (lengthMismatch(sector, uy, "uy")) {
			return -1;
		}
		if (lengthMismatch(sector, uz, "uz")) {
			return -1;
		}
		if (lengthMismatch(sector, err_x, "err_x")) {
			return -1;
		}
		if (lengthMismatch(sector, err_y, "err_y")) {
			return -1;
		}
		if (lengthMismatch(sector, err_z, "err_z")) {
			return -1;
		}

		return sector.length;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, byte[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating CrossList2";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating CrossList2";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, short[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating CrossList2";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating CrossList2";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, float[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating CrossList2";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating CrossList2";
			return true;
		}
		return false;
	}
}
