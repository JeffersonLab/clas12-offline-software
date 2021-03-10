package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

public class RTPCHitList extends Vector<RTPCHit> {

	// used to log erors
	private String _error;

	// for color scaling
	private int _maxADC;
	
	public RTPCHitList(String adcBankName) {
		byte[] layer = ColumnData.getByteArray(adcBankName + ".layer");
		if ((layer == null) || (layer.length < 1)) {
			return;
		}

		
		short[] component = ColumnData.getShortArray(adcBankName + ".component");
		int[] adc = ColumnData.getIntArray(adcBankName + ".ADC");
		short[] ped = ColumnData.getShortArray(adcBankName + ".ped");
		float[] time = ColumnData.getFloatArray(adcBankName + ".time");

		try {
			for (int i = 0; i < layer.length; i++) {
				
				RTPCHit hit = new RTPCHit(layer[i], component[i], adc[i], ped[i], time[i]);
                add(hit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		_maxADC = -1;
		for (RTPCHit hit : this) {
			_maxADC = Math.max(_maxADC, hit.adc);
		}

	}

}
