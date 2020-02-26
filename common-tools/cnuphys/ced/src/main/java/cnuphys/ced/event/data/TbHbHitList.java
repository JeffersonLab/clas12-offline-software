package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class TbHbHitList extends Vector<TbHbHit> {

	private String _error;

	public TbHbHitList(String bankName) {

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;
		
		byte[] superlayer = ColumnData.getByteArray(bankName + ".superlayer");;
		byte[] layer6 = ColumnData.getByteArray(bankName + ".layer");;
        short[] wire = ColumnData.getShortArray(bankName + ".wire");
        short[] id = ColumnData.getShortArray(bankName + ".id");
        short[] status = ColumnData.getShortArray(bankName + ".status");
        int[] TDC = ColumnData.getIntArray(bankName + ".TDC");
        
        //complication.. for HB doca will be null
        float[] doca = ColumnData.getFloatArray(bankName + ".doca");
        float[] trkDoca = ColumnData.getFloatArray(bankName + ".trkDoca");

		length = checkArrays(sector, superlayer, layer6, wire, id, status, doca, trkDoca);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			return;
		}

        
        for (int i = 0; i < length; i++) {
        	float fdoca = DataSupport.safeValue(doca, i, -1f);
           	float trkdoca = DataSupport.safeValue(trkDoca, i, -1f);
			int tdc = DataSupport.safeValue(TDC, i, -1);

        	add(new TbHbHit(sector[i], superlayer[i], layer6[i], wire[i], id[i], status[i], tdc, fdoca, trkdoca));
        }
        
		if (size() > 1) {
			Collections.sort(this);
		}

	}
	
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] superlayer, byte[] layer6, short[] wire, short[] id,
			short[] status, float[] doca, float[] trkDoca) {
		
		//docak might be null so exclude
		if ((sector == null) || (superlayer == null) || (layer6 == null) ||
				(wire == null) || (id == null) || (status == null)|| 
				(trkDoca == null)) {
				
			_error = "Unexpected null array when creating DCHitList: " + "sector = null: " + (sector == null)
					+ " superlayer == null: " + (superlayer == null) + " layer == null: " + (layer6 == null) +
					" wire == null: " + (wire == null) + " id == null: " + (id == null) + " status == null: " + (status == null);
					
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating DCHitList";
			return -1;
		}
		
		if (lengthMismatch(sector, superlayer, "superlayer")) {
			return -1;
		}
		if (lengthMismatch(sector, layer6, "layer6")) {
			return -1;
		}
		if (lengthMismatch(sector, wire, "wire")) {
			return -1;
		}
		if (lengthMismatch(sector, id, "id")) {
			return -1;
		}
		if (lengthMismatch(sector, status, "status")) {
			return -1;
		}
//		if (lengthMismatch(sector, TDC, "TDC")) {
//			return -1;
//		}
		
		if (doca != null) {  //null for hit based
			if (lengthMismatch(sector, doca, "doca")) {
				return -1;
			}
		}
		
		if (lengthMismatch(sector, trkDoca, "trkDoca")) {
			return -1;
		}
	

		return sector.length;
	}	

	/**
	 * Find the index of a hit
	 * @param sector the 1-based sector
	 * @param superlayer the superlayer
	 * @param layer6 the 1-based layer 1..6
	 * @param wire the 1-based wire
	 * @return the index, or -1 if not found
	 */
	public int getIndex(byte sector, byte superlayer, byte layer6, short wire) {
		if (isEmpty()) {
			return -1;
		}
		
//		public DCHit(byte sector, byte superlayer, byte layer6, short wire, short id, short status, float time, float doca, float trkDoca) {

		TbHbHit hit = new TbHbHit(sector, superlayer, layer6, wire);
		int index = Collections.binarySearch(this, hit);
		if (index >= 0) {
			return index;
		} 
		else { //not found
			return -1;
		}
	}
	
	/**
	 * Find the hit
	 * @param sector the 1-based sector
	 * @param superlayer the superlayer
	 * @param layer6 the 1-based layer 1..6
	 * @param wire the 1-based wire
	 * @return the index, or -1 if not found
	 */
	public TbHbHit get(byte sector, byte superlayer, byte layer6, short wire) {
		int index = getIndex(sector, superlayer, layer6, wire);
		return (index < 0) ? null : elementAt(index);
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, byte[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating TbHbHitList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating TbHbHitList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, short[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating TbHbHitList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating TbHbHitList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	private boolean lengthMismatch(byte[] sector, float[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating TbHbHitList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating TbHbHitList";
			return true;
		}
		return false;
	}
	
//	//check for length mismatch
//	private boolean lengthMismatch(byte[] sector, int[] array, String name) {
//		if (array == null) {
//			_error = "null " + name + " array when creating TbHbHitList";
//			return true;
//		}
//
//		if (sector.length != array.length) {
//			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating TbHbHitList";
//			return true;
//		}
//		return false;
//	}

	
}
