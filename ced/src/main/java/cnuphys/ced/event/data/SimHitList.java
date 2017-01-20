package cnuphys.ced.event.data;


import java.util.Collections;
import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;

public class SimHitList extends Vector<SimHit> {
	
	private String _error;

	public SimHitList(String tdcBankName, String adcBankName) {
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
		byte[] layer = null;
		short[] component = null;
		byte[] order = null;
		int[] TDC = null;
		int[] ADC = null;
		int length = 0;
		
		sector = ColumnData.getByteArray(tdcBankName + ".sector");
		if (sector != null) {
			layer = ColumnData.getByteArray(tdcBankName + ".layer");
			component = ColumnData.getShortArray(tdcBankName + ".component");
			order = ColumnData.getByteArray(tdcBankName + ".order");
			TDC = ColumnData.getIntArray(tdcBankName + ".TDC");

			length = checkArrays(sector, layer, component, order, TDC);
			if (length < 0) {
				Log.getInstance().warning("[" + tdcBankName + "] " + _error);
				return;
			}
						
			//Step 1 build basic list
			for (int index = 0; index < length; index++) {
				if (order[index] != 3) { //left tdc
					SimHit hit = new SimHit(sector[index], layer[index], component[index]);
					hit.tdcL = TDC[index];
					add(hit);
				}
			}
		}
		
		//step 2: sort
		if (size() > 1) {
			Collections.sort(this);
		}
		
		System.err.println("SIZE AFTER 1st SORT: " + size());
		
		//step 3 merge in right tdc
		for (int index = 0; index < length; index++) {
			if (order[index] == 3) { //right
				modifyInsert(sector[index], layer[index], component[index], -1, TDC[index], -1, -1);
			}
		}
		
		//on to the adcs
		sector = ColumnData.getByteArray(adcBankName + ".sector");
		if (sector != null) {
			layer = ColumnData.getByteArray(adcBankName + ".layer");
			component = ColumnData.getShortArray(adcBankName + ".component");
			order = ColumnData.getByteArray(adcBankName + ".order");
			ADC = ColumnData.getIntArray(adcBankName + ".ADC");

			length = checkArrays(sector, layer, component, order, ADC);
			if (length < 0) {
				Log.getInstance().warning("[" + adcBankName + "] " + _error);
				return;
			}
						
			//Step 4 merge left adc
			for (int index = 0; index < length; index++) {
				if (order[index] == 0) { //left adc
					modifyInsert(sector[index], layer[index], component[index], -1, -1, ADC[index], -1);
				}
			}
		}
				
		//step 5 merge in right adc
		for (int index = 0; index < length; index++) {
			if (order[index] == 1) { //right
				modifyInsert(sector[index], layer[index], component[index], -1, -1, -1, ADC[index]);
			}
		}
		

	}
	
	public void modifyInsert(byte sector, byte layer, short component, int tdcL, int tdcR, int adcL, int adcR) {
		SimHit hit = new SimHit(sector, layer, component);
		int index = Collections.binarySearch(this, hit);
		if (index >= 0) {
			hit = this.elementAt(index);
		} 
		else {
			System.err.println("INSERT INDEX: " + index + "   SIZE: " + size());
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
	
	private int checkArrays(byte[] sector, byte[] layer, short[] component, byte[] order, int[] data) {
		if ((sector == null) || (layer == null) || (component == null)) {
			_error = "Unexpected null array when creating SimHitList: " + "sector = null: " + (sector == null)
					+ " layer == null: " + (layer == null) + " component == null: " + (component == null);
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating SimHitList";
			return -1;
		}
		
		if (layer.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match layer length: " + layer.length + " when creating SimHitList";
			return -1;
		}
		
		if (component.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match component length: " + component.length + " when creating SimHitList";
			return -1;
		}
		
		if (order.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match order length: " + order.length + " when creating SimHitList";
			return -1;
		}

		if (data.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match data (tdc or adc) length: " + data.length + " when creating SimHitList";
			return -1;
		}

		return sector.length;
	}
}
