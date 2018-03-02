package cnuphys.ced.event.data;


import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

@SuppressWarnings("serial")
public class BaseHit2List extends Vector<BaseHit2> {

	//for reporting read errors
	protected String _error;
		
	protected String bankName;
	
	//the 1-based sector array
	protected byte[] sector;
	
	//number of hits
	protected int _count;


	public BaseHit2List(String bankName, String componentName)  {
		
		this.bankName = bankName;
		sector = ColumnData.getByteArray(bankName + ".sector");
		byte layer[] = ColumnData.getByteArray(bankName + ".layer");
		int component[] = ColumnData.getIntArray(bankName + "." + componentName);
		
		_count = checkArrays(sector, layer, component);
		if (_count < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			_count = 0;
//			throw new EventDataException("[" + bankName + "] " + _error);
		}


		for (int i = 0; i < _count; i++) {
			BaseHit2 baseHit2 = new BaseHit2(sector[i], layer[i], component[i]);
			add(baseHit2);
		}

	}
	
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] layer, int[] component) {
		
		if ((sector == null) ||
				(layer == null) || (component == null)) {
				
			_error = "BANK [" + bankName + "] Unexpected null array when creating BaseHitList2: " + "sector = null: " + (sector == null) + 
					"layer = null: " + (layer == null) + "component = null: " + (component == null);
					
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
		return sector.length;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, byte[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating list";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating list";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, int[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating CrossList";
			return true;
		}
		
		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating list";
			return true;
		}
		return false;
	}
	
	//check for length mismatch
	protected boolean lengthMismatch(byte[] sector, float[] array, String name) {
		if (array == null) {
			_error = "null " + name + " array when creating list";
			return true;
		}

		if (sector.length != array.length) {
			_error = "Sector length: " + sector.length + " does not match " + name + " length: " + array.length + " when creating list";
			return true;
		}
		return false;
	}
	
	/**
	 * Get the bank name backing this list
	 * @return the bank name backing this list
	 */
	public String getBankName() {
		return bankName;
	}
	
	public int count() {
		return _count;
	}

}