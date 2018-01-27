package cnuphys.ced.event.data;

import java.awt.Color;
import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class AdcHitList extends Vector<AdcHit> {
	
	//used to log erors
	private String _error;
	
	//for color scaling
	private int _maxADC;

	public AdcHitList(String adcBankName) {
		super();
		
		byte[] sector = ColumnData.getByteArray(adcBankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;

		byte[] layer = ColumnData.getByteArray(adcBankName + ".layer");
		short[] component = ColumnData.getShortArray(adcBankName + ".component");
		byte[] order = ColumnData.getByteArray(adcBankName + ".order");
		int[] ADC = ColumnData.getIntArray(adcBankName + ".ADC");
		short[] ped = ColumnData.getShortArray(adcBankName + ".ped");
		float[] time = ColumnData.getFloatArray(adcBankName + ".time");
		
		
		String tstamp = adcBankName + ".timestamp";
		long[] timestamp = ColumnData.getLongArray(tstamp);
		
		length = checkArrays(sector, layer, component, order, ADC);
		if (length < 0) {
			Log.getInstance().warning("[" + adcBankName + "] " + _error);
			return;
		}
		//check more
		int clen = checkArrays(length, ped, time);
		if (clen < 0) {
			Log.getInstance().warning("[" + adcBankName + "] " + _error);
			return;
		}

		// Step 1 build basic list
		for (int index = 0; index < length; index++) {
			if (order[index] != 3) { // left tdc
				modifyInsert(sector[index], layer[index], component[index], ADC[index], -1, ped[index], -1, time[index], Float.NaN);
			}
		}
		
		//step 2: sort
		if (size() > 1) {
			Collections.sort(this);
		}
		
		//step 3 merge in right adc, ped, time
		for (int index = 0; index < length; index++) {
			if (order[index] == 3) { //right
				modifyInsert(sector[index], layer[index], component[index], -1, ADC[index], -1, ped[index], Float.NaN, time[index]);
			}
		}
		
		_maxADC = -1;
		for (AdcHit hit : this) {
			_maxADC = Math.max(_maxADC, hit.averageADC());
		}
		
//		if (adcBankName.contains("HTCC")) {
//			for (AdcHit hit : this) {
//				System.out.println(hit);
//			}
//		}

	}
	
	/**
	 * Get the max average adc
	 * @return the max average adc
	 */
	public int maxADC() {
		return _maxADC;
	}
	
	public void modifyInsert(byte sector, byte layer, short component, int adcL, int adcR, int pedL, int pedR, float timeL, float timeR) {
		AdcHit hit = new AdcHit(sector, layer, component);
		int index = Collections.binarySearch(this, hit);
		if (index >= 0) {
			hit = this.elementAt(index);
		} 
		else {
			index = -(index + 1); // now the insertion point.
			add(index, hit);
		}
		
		if (adcL >= 0) {
			hit.adcL = adcL;
		}
		if (adcR >= 0) {
			hit.adcR = adcR;
		}
		if (pedL >= 0) {
			hit.pedL = (short)pedL;
		}
		if (pedR >= 0) {
			hit.pedR = (short)pedR;
		}
		if (!Float.isNaN(timeL)) {
			hit.timeL = timeL;
		}
		if (!Float.isNaN(timeR)) {
			hit.timeR = timeR;
		}

	}
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] layer, short[] component, byte[] order, int[] data) {
		if ((sector == null) || (layer == null) || (component == null) || (order == null) || (data == null)) {
			_error = "Unexpected null array when creating AdcHitList: " + "sector = null: " + (sector == null)
					+ " layer = null: " + (layer == null) + " component = null: " + (component == null) +
					 " order = null: " + (order == null) + " data (tdc or adc) == null: " + (data == null);
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating AdcHitList";
			return -1;
		}
		
		if (layer.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match layer length: " + layer.length + " when creating AdcHitList";
			return -1;
		}
		
		if (component.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match component length: " + component.length + " when creating AdcHitList";
			return -1;
		}
		
		if (order.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match order length: " + order.length + " when creating AdcHitList";
			return -1;
		}

		if (data.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match data (tdc or adc) length: " + data.length + " when creating AdcHitList";
			return -1;
		}

		return sector.length;
	}
	
	//check more arrays
	private int checkArrays(int length, short[] ped, float[] time) {
		if ((ped == null) || (time == null)) {
			_error = "Unexpected null array when creating AdcHitList: " + "ped = null: " + (ped == null)
					+ " time = null: " +  (time == null);
			return -1;
		}
		
		
		if (ped.length != length) {
			_error = "Expected length: " + length + " does not match ped length: " + ped.length + " when creating AdcHitList";
			return -1;
		}
		
		if (time.length != length) {
			_error = "Expected length: " + length + " does not match time length: " + time.length + " when creating AdcHitList";
			return -1;
		}

		return length;
	}
	
	/**
	 * Find the index of a hit
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @param component the 1-based component
	 * @return the index, or -1 if not found
	 */
	public int getIndex(byte sector, byte layer, short component) {
		if (isEmpty()) {
			return -1;
		}
		AdcHit hit = new AdcHit(sector, layer, component);
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
	 * @param layer the 1-based layer 1..36
	 * @param component the 1-based component
	 * @return the hit, or null if not found
	 */
	public AdcHit get(byte sector, byte layer, short component) {
		int index = getIndex(sector, layer, component);
		return (index < 0) ? null : elementAt(index);
	}
	
	/**
	 * Find the hit
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer 1..36
	 * @param component the 1-based component
	 * @return the hit, or null if not found
	 */
	public AdcHit get(int sector, int layer, int component) {
		return get((byte)sector, (byte)layer, (short)component);
	}

	
	/**
	 * Get a color with apha based of relative adc 
	 * @param hit the hit
	 * @return a fill color for adc hits 
	 */
	public Color adcColor(AdcHit hit) {
		if (hit == null) {
			return Color.white;
		}
		
		int avgADC = hit.averageADC();
		
		double maxadc = Math.max(1.0, _maxADC);
		
		double fract = (avgADC)/maxadc;
//		fract = Math.max(0.5, Math.min(1.0, fract));
		fract = Math.max(0, Math.min(1.0, fract));
		
		int alpha = 128 + (int)(127*fract);
		alpha = Math.min(255,  alpha);
		
//		System.err.println("AVG ADC: " + avgADC + "   fract: " + fract + "   maxADC: "  + _maxADC);
		return AdcColorScale.getInstance().getAlphaColor(fract, alpha);
//		int alpha = (int)(254*fract);
//		
//		return new Color(255, 0, 0, alpha);
	}
}
