package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class DCHitList extends Vector<DCHit> {

	//for reporting read errors
	private String _error;
	
	private String _bankName;

	public DCHitList(String bankName) throws EventDataException {
		
		_bankName = bankName;

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		
		if ((sector == null) || (sector.length < 1)) {
			return;
		}

		int length = 0;

		
		byte layer[] = ColumnData.getByteArray(bankName + ".layer");
		byte superlayer[] = ColumnData.getByteArray(bankName + ".superlayer");
		short wire[] = ColumnData.getShortArray(bankName + ".wire");
		short id[] = ColumnData.getShortArray(bankName + ".id");
		short status[] = ColumnData.getShortArray(bankName + ".status");
		byte lr[] = ColumnData.getByteArray(bankName + ".LR");
		int TDC[] = ColumnData.getIntArray(bankName + ".TDC");
				
		float doca[] = ColumnData.getFloatArray(bankName + ".trkDoca");
		
		//for backwards compatibility do not include TDC in array check
		
		length = checkArrays(sector, superlayer, layer, wire, id, status, lr, doca);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			throw new EventDataException("[" + bankName + "] " + _error);
		}
		
		for (int i = 0; i < length; i++) {
			
			int tdc = DataSupport.safeValue(TDC, i, -1);
			
			add(new DCHit(sector[i], superlayer[i], layer[i], wire[i], id[i], status[i], lr[i], tdc, doca[i]));
		}


	}
	
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] superlayer, byte[] layer, short[] wire,
			short id[], short status[], byte lr[], float[] doca) {
		
		if ((sector == null) ||
				(superlayer == null) || (layer == null) || (wire == null)) {
				
			_error = "Unexpected null array when creating HitList: " + "sector = null: " + (sector == null) + 
					"superlayer = null: " + (superlayer == null) +
					"layer = null: " + (layer == null) + "wire = null: " + (wire == null) + 
					"id = null: " + (id == null) + 
					" status == null: " + (status == null) + " lr == null: " + (lr == null) + 
					" doca == null: " + (doca == null);
					
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating CrossList";
			return -1;
		}
		
		if (lengthMismatch(sector, superlayer, "superlayer")) {
			return -1;
		}
		if (lengthMismatch(sector, layer, "layer")) {
			return -1;
		}
		if (lengthMismatch(sector, wire, "component")) {
			return -1;
		}
		if (lengthMismatch(sector, id, "id")) {
			return -1;
		}
		if (lengthMismatch(sector, status, "status")) {
			return -1;
		}
		if (lengthMismatch(sector, lr, "lr")) {
			return -1;
		}
		if (lengthMismatch(sector, doca, "doca")) {
			return -1;
		}

	

		return sector.length;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, byte[] array, String name) {
		
		if (array == null) {
			_error = "null " + name + " array when creating DCHitList";
			return true;
		}
		
		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating DCHitList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, short[] array, String name) {
		
		if (array == null) {
			_error = "null " + name + " array when creating DCHitList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating DCHitList";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, float[] array, String name) {
		
		if (array == null) {
			_error = "null " + name + " array when creating DCHitList";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating DCHitList";
			return true;
		}
		return false;
	}
	
//	//check for length mismatch
//	protected boolean lengthMismatch(byte[] sector, int[] array, String name) {
//		
//		if (array == null) {
//			_error = "null " + name + " array when creating DCHitList";
//			return true;
//		}
//
//		if (sector.length != array.length) {
//			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating DCHitList";
//			return true;
//		}
//		return false;
//	}
//
	
	
	/**
	 * Get the bank name backing this list
	 * @return the bank name backing this list
	 */
	public String getBankName() {
		return _bankName;
	}


}
