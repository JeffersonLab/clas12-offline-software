package cnuphys.ced.event.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.jlab.io.base.DataEvent;

public class BST extends DetectorData {

	AdcHitList _adcHits = new AdcHitList("BST::adc");

	private static BST _instance;

	/**
	 * Public access to the singleton
	 * @return the BST singleton
	 */
	public static BST getInstance() {
		if (_instance == null) {
			_instance = new BST();
		}
		return _instance;
	}
	
	@Override
	public void newClasIoEvent(DataEvent event) {
		_adcHits = new AdcHitList("BST::adc");
	}
	
	/**
	 * Update the list. This is probably needed only during accumulation
	 * @return the updated list
	 */
	public AdcHitList updateAdcList() {
		_adcHits = new AdcHitList("BST::adc");
		return _adcHits;
	}

	/**
	 * Get the adc hit list
	 * @return the adc hit list
	 */
	public AdcHitList getHits() {
		return _adcHits;
	}
	
	
	/**
	 * Get a collection of all strip, adc doublets for a given sector and layer
	 * 
	 * @param sector the 1-based sector
	 * @param layer the 1-based layer
	 * @return a collection of all strip, adc doublets for a given sector and
	 *         layer. It is a collection of integer arrays. For each array, the
	 *         0 entry is the 1-based strip and the 1 entry is the adc.
	 */
	public Vector<int[]> allStripsForSectorAndLayer(int sector,
			int layer) {
		Vector<int[]> strips = new Vector<int[]>();
		
		AdcHitList hits = getHits();
		if ((hits != null) && !hits.isEmpty()) {
			for (AdcHit hit : hits) {
				if ((hit.sector == sector) && (hit.layer == layer)) {
					int data[] = { hit.component, hit.averageADC() };
					strips.add(data);
				}
			}
		}

		// sort based on strips
		if (strips.size() > 1) {
			Comparator<int[]> c = new Comparator<int[]>() {

				@Override
				public int compare(int[] o1, int[] o2) {
					return Integer.compare(o1[0], o2[0]);
				}
			};

			Collections.sort(strips, c);
		}

		return strips;
	}
}