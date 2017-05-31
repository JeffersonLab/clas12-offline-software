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
	
	public int[] sectorCounts = {-1, 0, 0, 0, 0, 0, 0}; //use 7 no off by one

	public DCTdcHitList() {
		
		
		int length = 0;
				
		byte[] sector = ColumnData.getByteArray(DCBank + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}

		byte[] layer = ColumnData.getByteArray(DCBank + ".layer");
		short[] wire = ColumnData.getShortArray(DCBank + ".component");
		//byte[] order = ColumnData.getByteArray(DCBank + ".order");
		int[] TDC = ColumnData.getIntArray(DCBank + ".TDC");

		length = checkArrays(sector, layer, wire, TDC);
		if (length < 0) {
			Log.getInstance().warning("[" + DCBank + "] " + _error);
			return;
		}
		
		//see if there are doca arrays of the same length
		byte[] lr = ColumnData.getByteArray(DocaBank + ".LR");
		float[] doca = null;
		float[] time = null;
		float[] sdoca = null;
		float[] stime = null;

		int docalen = -1;
		if ((lr != null) && (lr.length != 0)) {
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
			
			if ((sector[i] > 0) && (sector[i] < 7)) {
				sectorCounts[sector[i]] += 1;
			}
		}
		
		if (size() > 1) {
			Collections.sort(this);
		}


//		for (DCTdcHit hit : this) {
//			System.out.println(hit.toString());
//		}
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
	
	
	/**
	 * Find the hit
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer 1..36
	 * @param component the 1-based component
	 * @return the hit, or null if not found
	 */
	public DCTdcHit getHit(int sector, int layer, int wire) {
		return get((byte)sector, (byte)layer, (short)wire);
	}

	/**
	 * Find the hit
	 * @param sector the 1-based sector
	 * @param superlayer the 1-based superlayer 1..6
	 * @param layer6 the 1-based layer 1..36
	 * @param component the 1-based component
	 * @return the hit, or null if not found
	 */
	public DCTdcHit getHit(int sector, int superlayer, int layer6, int wire) {
		int layer = (superlayer-1)*6 + layer6;
		return get((byte)sector, (byte)layer, (short)wire);
	}


	
	/**
	 * Extract the sectors as an array. Used by the Noise package
	 * @return the sectors as an array.
	 */
	public int[] sectorArray() {
		if (isEmpty()) {
			return null;
		}
		int array[] = new int[size()];
		int index = 0;
		for (DCTdcHit hit : this) {
			array[index] = hit.sector;
			index++;
		}
		return array;
	}
	
	/**
	 * Extract the superlayers as an array. Used by the Noise package
	 * @return the superlayers as an array.
	 */
	public int[] superlayerArray() {
		if (isEmpty()) {
			return null;
		}
		int array[] = new int[size()];
		int index = 0;
		for (DCTdcHit hit : this) {
			array[index] = hit.superlayer;
			index++;
		}
		return array;
	}
	
	/**
	 * Extract the layer6's as an array. Used by the Noise package
	 * @return the layer6's [1..6] as an array.
	 */
	public int[] layer6Array() {
		if (isEmpty()) {
			return null;
		}
		int array[] = new int[size()];
		int index = 0;
		for (DCTdcHit hit : this) {
			array[index] = hit.layer6;
			index++;
		}
		return array;
	}

	/**
	 * Extract the wires as an array. Used by the Noise package
	 * @return the wires as an array.
	 */
	public int[] wireArray() {
		if (isEmpty()) {
			return null;
		}
		int array[] = new int[size()];
		int index = 0;
		for (DCTdcHit hit : this) {
			array[index] = hit.wire;
			index++;
		}
		return array;
	}

}
