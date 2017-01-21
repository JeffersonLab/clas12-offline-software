package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class DCTdcHitList extends Vector<DCTdcHit> {

	//used to log erors
	private String _error;
	
	private String DCBank = "DC::tdc";
	private String DocaBank = "DC::doca";  //only in sim data
	
	
//	bank name: [DC::tdc] column name: [sector] full name: [DC::tdc.sector] data type: byte
//	bank name: [DC::tdc] column name: [layer] full name: [DC::tdc.layer] data type: byte
//	bank name: [DC::tdc] column name: [component] full name: [DC::tdc.component] data type: short
//	bank name: [DC::tdc] column name: [order] full name: [DC::tdc.order] data type: byte
//	bank name: [DC::tdc] column name: [TDC] full name: [DC::tdc.TDC] data type: int
	
	//NOT present in real data
//	bank name: [DC::doca] column name: [LR] full name: [DC::doca.LR] data type: byte
//	bank name: [DC::doca] column name: [doca] full name: [DC::doca.doca] data type: float
//	bank name: [DC::doca] column name: [sdoca] full name: [DC::doca.sdoca] data type: float
//	bank name: [DC::doca] column name: [time] full name: [DC::doca.time] data type: float
//	bank name: [DC::doca] column name: [stime] full name: [DC::doca.stime] data type: float

	public DCTdcHitList() {
		
		byte[] sector = null;
		byte[] layer = null;
		short[] wire = null;
		
//I think order is not relevant for DC
//		byte[] order = null; 
		int[] TDC = null;
		int length = 0;
		
		byte[] lr = null;
		float[] doca = null;
		float[] time = null;
		float[] sdoca = null;
		float[] stime = null;
		
		sector = ColumnData.getByteArray(DCBank + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}

		layer = ColumnData.getByteArray(DCBank + ".layer");
		wire = ColumnData.getShortArray(DCBank + ".component");
		// order = ColumnData.getByteArray(DCBank + ".order");
		TDC = ColumnData.getIntArray(DCBank + ".TDC");

		length = checkArrays(sector, layer, wire, TDC);
		if (length < 0) {
			Log.getInstance().warning("[" + DCBank + "] " + _error);
			return;
		}
		
		//see if there are doca arrays of the same length
		lr = ColumnData.getByteArray(DocaBank + ".LR");
		int docalen = -1;
		if (lr != null) {
			doca = ColumnData.getFloatArray(DocaBank + ".doca");
			time = ColumnData.getFloatArray(DocaBank + ".time");
			sdoca = ColumnData.getFloatArray(DocaBank + ".sdoca");
			stime = ColumnData.getFloatArray(DocaBank + ".stime");
			
			docalen = checkArrays(lr, doca, time, sdoca, stime);
			if (docalen < 0) {
				Log.getInstance().warning("[" + DocaBank + "] " + _error);
			}
			if (docalen != length) {
				Log.getInstance().warning("[" + DocaBank + "] " + 
			"doca length " + docalen + " does not match tdc length: " + length);
				docalen = -1;
			}
		}
		
		//now build the list

		for (int i = 0; i < length; i++) {
			if (docalen == length) {
				add(new DCTdcHit(sector[i], layer[i], wire[i], TDC[i], lr[i], doca[i], sdoca[i], time[i], stime[i]));
			}
			else {
				add(new DCTdcHit(sector[i], layer[i], wire[i], TDC[i]));
			}
		}
		
		if (size() > 1) {
			Collections.sort(this);
		}


		for (DCTdcHit hit : this) {
			System.out.println(hit.toString());
		}
	}
	
	private int checkArrays(byte[] lr, float[] doca, float[] time, float[] sdoca, float[] stime) {
		if ((lr == null) || (doca == null) || (time == null) || (sdoca == null) || (stime == null)) {
			_error = "Unexpected null array when creating DcTdcHitList: " + 
		"lr = null: " + (lr == null) +
		" doca = null: " + (doca == null) +
		" time == null: " + (time == null) + 
		" sdoca = null: " + (sdoca == null) +
		" stime == null: " + (stime == null);
			return -1;
		}
		
		if (lr.length < 1) {
			_error = "LR array has 0 length when creating DcTdcHitList";
			return -1;
		}
		
		if (doca.length != lr.length) {
			_error = "LR length: " + lr.length + " does not match doca length: " + doca.length + " when creating DcTdcHitList";
			return -1;
		}
		
		if (time.length != lr.length) {
			_error = "LR length: " + lr.length + " does not match time length: " + time.length + " when creating DcTdcHitList";
			return -1;
		}
		
		if (sdoca.length != lr.length) {
			_error = "LR length: " + lr.length + " does not match sdoca length: " + sdoca.length + " when creating DcTdcHitList";
			return -1;
		}

		if (stime.length != lr.length) {
			_error = "LR length: " + lr.length + " does not match stime td length: " + stime.length + " when creating DcTdcHitList";
			return -1;
		}

		return lr.length;
	}
	
	private int checkArrays(byte[] sector, byte[] layer, short[] wire, int[] data) {
		if ((sector == null) || (layer == null) || (wire == null)) {
			_error = "Unexpected null array when creating DcTdcHitList: " + "sector = null: " + (sector == null)
					+ " layer == null: " + (layer == null) + " wire == null: " + (wire == null);
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating DcTdcHitList";
			return -1;
		}
		
		if (layer.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match layer length: " + layer.length + " when creating DcTdcHitList";
			return -1;
		}
		
		if (wire.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match component length: " + wire.length + " when creating DcTdcHitList";
			return -1;
		}
		
//		if (order.length != sector.length) {
//			_error = "Sector length: " + sector.length + " does not match order length: " + order.length + " when creating DcTdcHitList";
//			return -1;
//		}

		if (data.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match data td length: " + data.length + " when creating DcTdcHitList";
			return -1;
		}

		return sector.length;
	}
	
	
	/**
	 * Find the index of a hit
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @param wire the 1-based wire
	 * @return the index, or -1 if not found
	 */
	public int getIndex(byte sector, byte layer, short wire) {
		if (isEmpty()) {
			return -1;
		}
		DCTdcHit hit = new DCTdcHit(sector, layer, wire, -1);
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
	 * @param layer the 1-based layer
	 * @param wire the 1-based wire
	 * @return the hit, or null if not found
	 */
	public DCTdcHit get(byte sector, byte layer, short wire) {
		int index = getIndex(sector, layer, wire);
		return (index < 0) ? null : elementAt(index);
	}

}
