package cnuphys.ced.event.data;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class Hit1List extends BaseHitList<Hit1> {
	

	public Hit1List(String bankName) throws EventDataException {
		
		super(bankName);
		
		if ((sector == null) || (sector.length < 1)) {
			return;
		}

		int length = 0;

		
		byte layer[] = ColumnData.getByteArray(bankName + ".layer");
		short component[] = ColumnData.getShortArray(bankName + ".component");
		float energy[] = ColumnData.getFloatArray(bankName + ".energy");
		float x[] = ColumnData.getFloatArray(bankName + ".x");
		float y[] = ColumnData.getFloatArray(bankName + ".y");
		float z[] = ColumnData.getFloatArray(bankName + ".z");
		
		length = checkArrays(sector, layer, component, energy, x, y, z);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			throw new EventDataException("[" + bankName + "] " + _error);
		}
		
		for (int i = 0; i < length; i++) {
			add(new Hit1(sector[i], layer[i], component[i], energy[i], x[i], y[i], z[i]));
		}


	}
	
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] layer, short[] component,
			float energy[], float[] x, float[] y, float[] z) {
		
		if ((sector == null) ||
				(x == null) || (y == null) || (y == null)) {
				
			_error = "Unexpected null array when creating HitList: " + "sector = null: " + (sector == null) + 
					"layer = null: " + (layer == null) + "component = null: " + (component == null) + 
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
		if (lengthMismatch(sector, component, "component")) {
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
	

}
