package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

public class BaseHitList<T extends BaseHit> extends Vector<T> {

	//for reporting read errors
	protected String _error;
		
	protected String bankName;
	
	//the 1-based sector array
	protected byte[] sector;


	public BaseHitList(String bankName) {
		this.bankName = bankName;
		sector = ColumnData.getByteArray(bankName + ".sector");
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
	protected boolean lengthMismatch(byte[] sector, short[] array, String name) {
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

}
