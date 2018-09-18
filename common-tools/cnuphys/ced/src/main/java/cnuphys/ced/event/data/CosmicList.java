package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class CosmicList extends Vector<Cosmic> {
	
	private String _error;

	public CosmicList(String bankName) {

		short id[] = ColumnData.getShortArray(bankName + ".ID");
		if ((id == null) || (id.length < 1)) {
	//		System.err.println("NO ID ARRAY in [" + bankName + "]");
			return;
		}
		
		int length = 0;

        float[] chi2 = ColumnData.getFloatArray(bankName + ".chi2");
        float[] phi = ColumnData.getFloatArray(bankName + ".phi");
        float[] theta = ColumnData.getFloatArray(bankName + ".theta");
        float[] trkline_yx_interc = ColumnData.getFloatArray(bankName + ".trkline_yx_interc");
        float[] trkline_yx_slope = ColumnData.getFloatArray(bankName + ".trkline_yx_slope");
        float[] trkline_yz_interc = ColumnData.getFloatArray(bankName + ".trkline_yz_interc");
        float[] trkline_yz_slope = ColumnData.getFloatArray(bankName + ".trkline_yz_slope");

               
		length = checkArrays(id, chi2, phi, theta, 
				trkline_yx_interc, trkline_yx_slope,
				trkline_yz_interc, trkline_yz_slope);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			return;
		}

//		public Cosmic(int id, float chi2, float phi, float theta, float trkline_yx_interc, float trkline_yx_slope,
//				float trkline_yz_interc, float trkline_yz_slope) {

		for (int i = 0; i < length; i++) {
			add(new Cosmic(id[i], chi2[i], phi[i], theta[i],
					trkline_yx_interc[i], trkline_yx_slope[i],
					trkline_yz_interc[i], trkline_yz_slope[i]));
		}

	}
	
	//check arrays are not null and have same length
	private int checkArrays(short[] id,
			float[] chi2, float[] phi, float[] theta, 
			float[] trkline_yx_interc, float[] trkline_yx_slope, 
			float[] trkline_yz_interc, float[] trkline_yz_slope) {
		
		if ((id == null) || (chi2 == null) || (phi == null) ||
				(theta == null) || 
				(trkline_yx_interc == null) || 
				(trkline_yx_slope == null) || 
				(trkline_yz_interc == null) ||
				(trkline_yz_slope == null)) {
				
			_error = "Unexpected null array when creating CosmicList: " + "id = null: " + (id == null)
					+ " chi2 == null: " + (chi2 == null) + " theta == null: " + (theta == null) +
					" phi == null: " + (phi == null) +
					" trkline_yx_interc == null: " + (trkline_yx_interc == null) + 
					" trkline_yx_slope == null: " + (trkline_yx_slope == null) +
					" trkline_yz_interc == null: " + (trkline_yz_interc == null) +
					" trkline_yz_slope == null: " + (trkline_yz_slope == null);
					
			return -1;
		}
		
		if (id.length < 1) {
			_error = "Id array has 0 length when creating CosmicList";
			return -1;
		}
		
		if (lengthMismatch(id, chi2, "chi2")) {
			return -1;
		}
		if (lengthMismatch(id, phi, "phi")) {
			return -1;
		}
		if (lengthMismatch(id, theta, "theta")) {
			return -1;
		}
		if (lengthMismatch(id, trkline_yx_interc, "trkline_yx_interc")) {
			return -1;
		}
		if (lengthMismatch(id, trkline_yx_slope, "trkline_yx_slope")) {
			return -1;
		}
		if (lengthMismatch(id, trkline_yz_interc, "trkline_yz_interc")) {
			return -1;
		}
		if (lengthMismatch(id, trkline_yz_slope, "trkline_yz_slope")) {
			return -1;
		}
	

		return id.length;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(short[] id, float[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating CosmicList";
			return true;
		}

		if (id.length != array.length) {
			_error = "ID length: " + id.length + " does not match " + name + " length: " + array.length + " when creating CosmicList";
			return true;
		}
		return false;
	}
}
