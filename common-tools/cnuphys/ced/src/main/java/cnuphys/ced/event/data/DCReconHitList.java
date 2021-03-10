package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

public class DCReconHitList extends Vector<DCReconHit> {

	//the bank name
	private String _bankName;

	public DCReconHitList(String bankName) throws EventDataException {

		_bankName = bankName;

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		int length = (sector == null) ? 0 : sector.length;

		if (length < 1) {
			return;
		}


		byte layer[] = ColumnData.getByteArray(bankName + ".layer");
		byte superlayer[] = ColumnData.getByteArray(bankName + ".superlayer");
		short wire[] = ColumnData.getShortArray(bankName + ".wire");
		short id[] = ColumnData.getShortArray(bankName + ".id");
		short status[] = ColumnData.getShortArray(bankName + ".status");
		byte lr[] = ColumnData.getByteArray(bankName + ".LR");
		int TDC[] = ColumnData.getIntArray(bankName + ".TDC");
		short clusterID[] = ColumnData.getShortArray(bankName + ".clusterID");

		float trkDoca[] = ColumnData.getFloatArray(bankName + ".trkDoca");
		float doca[] = ColumnData.getFloatArray(bankName + ".doca");

		for (int i = 0; i < length; i++) {

			int tdc = DataSupport.safeValue(TDC, i, -1);

			float docaval = (doca == null) ? -1f : doca[i];

			add(new DCReconHit(sector[i], superlayer[i], layer[i], wire[i], id[i], status[i], lr[i], tdc, trkDoca[i],
					docaval, clusterID[i]));
		}
	}


	/**
	 * Get the bank name backing this list
	 * 
	 * @return the bank name backing this list
	 */
	public String getBankName() {
		return _bankName;
	}
	
	/**
	 * Get the hit from the id. Brute force, because
	 * they are not sorted.
	 * @param id the id to match
	 * @return the hit with the matching ID, or null.
	 */
	public DCReconHit hitFromId(short id) {
		for (DCReconHit hit : this) {
			if (hit.id == id) {
				return hit;
			}
		}
		
		return null;
	}
	

}
