package cnuphys.ced.event.data;


import java.awt.Color;
import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class TdcAdcHitList extends Vector<TdcAdcHit> {
	
	//used to log erors
	private String _error;
	
	//for color scaling
	private int _maxADC;

	public TdcAdcHitList(String tdcBankName, String adcBankName) {
		super();
		
		/*
		1) create basic list from the tdc bank and left tdc
        2) sort
        3) merge in right tdc
        4) merge in left adc
        5) merge in right adc
        */

		//step 1: basic list from left tdc
		
		byte[] sector = null;
		
		sector = ColumnData.getByteArray(tdcBankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;

		byte[] layer = ColumnData.getByteArray(tdcBankName + ".layer");
		short[] component = ColumnData.getShortArray(tdcBankName + ".component");
		byte[] order = ColumnData.getByteArray(tdcBankName + ".order");
		int[] TDC = ColumnData.getIntArray(tdcBankName + ".TDC");

		length = checkArrays(sector, layer, component, order, TDC);
		if (length < 0) {
			Log.getInstance().warning("[" + tdcBankName + "] " + _error);
			return;
		}

		// Step 1 build basic list
		for (int index = 0; index < length; index++) {
			if (order[index] != 3) { // left tdc
				modifyInsert(sector[index], layer[index], component[index], TDC[index], -1, -1, -1);
			}
		}
		
		//step 2: sort
		if (size() > 1) {
			Collections.sort(this);
		}
		
		//step 3 merge in right tdc
		for (int index = 0; index < length; index++) {
			if (order[index] == 3) { //right
				modifyInsert(sector[index], layer[index], component[index], -1, TDC[index], -1, -1);
			}
		}
		
		//on to the adcs
		int[] ADC = null;

		sector = ColumnData.getByteArray(adcBankName + ".sector");
		if (sector == null) {
			return;
		}
		layer = ColumnData.getByteArray(adcBankName + ".layer");
		component = ColumnData.getShortArray(adcBankName + ".component");
		order = ColumnData.getByteArray(adcBankName + ".order");
		ADC = ColumnData.getIntArray(adcBankName + ".ADC");

		length = checkArrays(sector, layer, component, order, ADC);
		if (length < 0) {
			Log.getInstance().warning("[" + adcBankName + "] " + _error);
			return;
		}

		// Step 4 merge left adc
		for (int index = 0; index < length; index++) {
			if (order[index] == 0) { // left adc
				modifyInsert(sector[index], layer[index], component[index], -1, -1, ADC[index], -1);
			}
		}
				
		//step 5 merge in right adc
		for (int index = 0; index < length; index++) {
			if (order[index] == 1) { //right
				modifyInsert(sector[index], layer[index], component[index], -1, -1, -1, ADC[index]);
			}
		}
		
		
		_maxADC = -1;
		for (TdcAdcHit hit : this) {
			_maxADC = Math.max(_maxADC, hit.averageADC());
		}
		
//		if (adcBankName.contains("ECAL")) {
//			for (TdcAdcHit hit : this) {
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
	
	public void modifyInsert(byte sector, byte layer, short component, int tdcL, int tdcR, int adcL, int adcR) {
		TdcAdcHit hit = new TdcAdcHit(sector, layer, component);
		int index = Collections.binarySearch(this, hit);
		if (index >= 0) {
			hit = this.elementAt(index);
		} 
		else {
			index = -(index + 1); // now the insertion point.
			add(index, hit);
		}
		
		if (tdcL >= 0) {
			hit.tdcL = tdcL;
		}
		if (tdcR >= 0) {
			hit.tdcR = tdcR;
		}
		if (adcL >= 0) {
			hit.adcL = adcL;
		}
		if (adcR >= 0) {
			hit.adcR = adcR;
		}
	}
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] layer, short[] component, byte[] order, int[] data) {
		if ((sector == null) || (layer == null) || (component == null)) {
			_error = "Unexpected null array when creating TdcAdcHitList: " + "sector = null: " + (sector == null)
					+ " layer == null: " + (layer == null) + " component == null: " + (component == null);
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating TdcAdcHitList";
			return -1;
		}
		
		if (layer.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match layer length: " + layer.length + " when creating TdcAdcHitList";
			return -1;
		}
		
		if (component.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match component length: " + component.length + " when creating TdcAdcHitList";
			return -1;
		}
		
		if (order.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match order length: " + order.length + " when creating TdcAdcHitList";
			return -1;
		}

		if (data.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match data (tdc or adc) length: " + data.length + " when creating TdcAdcHitList";
			return -1;
		}

		return sector.length;
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
		TdcAdcHit hit = new TdcAdcHit(sector, layer, component);
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
	public TdcAdcHit get(byte sector, byte layer, short component) {
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
	public TdcAdcHit get(int sector, int layer, int component) {
		return get((byte)sector, (byte)layer, (short)component);
	}

	
	/**
	 * Get a color with apha based of relative adc 
	 * @param hit the hit
	 * @return a fill color for adc hits 
	 */
	public Color adcColor(TdcAdcHit hit) {
		if (hit == null) {
			return Color.white;
		}
		
		int avgADC = hit.averageADC();
		if (avgADC < 1) {
			return new Color(255, 0, 0, 30);
		}
		if (_maxADC < 1) {
			return Color.black;  //should not happen
		}
		
		double fract = ((double)avgADC)/((double)(_maxADC));
		fract = Math.max(0.15, Math.min(1.0, fract));
		int alpha = (int)(254*fract);
		
		return new Color(255, 0, 0, alpha);
	}
}
