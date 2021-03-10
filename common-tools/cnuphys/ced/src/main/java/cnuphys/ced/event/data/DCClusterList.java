package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

public class DCClusterList extends Vector<DCCluster> {

	// the bank name
	private String _bankName;

	public DCClusterList(String bankName) throws EventDataException {
		
		_bankName = bankName;

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		int length = (sector == null) ? 0 : sector.length;

		if (length < 1) {
			return;
		}
		
//		public DCCluster(byte sector, byte superlayer, short id, byte size, short status, 
//				float avgWire, float fitChisqProb,
//				float fitInterc, float fitIntercErr, float fitSlope, float fitSlopeErr,
//				short... hitID) {

		byte superlayer[] = ColumnData.getByteArray(bankName + ".superlayer");
		short id[] = ColumnData.getShortArray(bankName + ".id");
		byte size[] = ColumnData.getByteArray(bankName + ".size");
		short status[] = ColumnData.getShortArray(bankName + ".status");
		float avgWire[] = ColumnData.getFloatArray(bankName + ".avgWire");
		float fitChisqProb[] = ColumnData.getFloatArray(bankName + ".fitChisqProb");
		float fitInterc[] = ColumnData.getFloatArray(bankName + ".fitInterc");
		float fitIntercErr[] = ColumnData.getFloatArray(bankName + ".fitIntercErr");
		float fitSlope[] = ColumnData.getFloatArray(bankName + ".fitSlope");
		float fitSlopeErr[] = ColumnData.getFloatArray(bankName + ".fitSlopeErr");
		
		short hit1_ID[] = ColumnData.getShortArray(bankName + ".Hit1_ID");
		short hit2_ID[] = ColumnData.getShortArray(bankName + ".Hit2_ID");
		short hit3_ID[] = ColumnData.getShortArray(bankName + ".Hit3_ID");
		short hit4_ID[] = ColumnData.getShortArray(bankName + ".Hit4_ID");
		short hit5_ID[] = ColumnData.getShortArray(bankName + ".Hit5_ID");
		short hit6_ID[] = ColumnData.getShortArray(bankName + ".Hit6_ID");
		short hit7_ID[] = ColumnData.getShortArray(bankName + ".Hit7_ID");
		short hit8_ID[] = ColumnData.getShortArray(bankName + ".Hit8_ID");
		short hit9_ID[] = ColumnData.getShortArray(bankName + ".Hit9_ID");
		short hit10_ID[] = ColumnData.getShortArray(bankName + ".Hit10_ID");
		short hit11_ID[] = ColumnData.getShortArray(bankName + ".Hit11_ID");
		short hit12_ID[] = ColumnData.getShortArray(bankName + ".Hit12_ID");
		
		for (int i = 0; i < length; i++) {
			DCCluster cluster = new DCCluster(sector[i], superlayer[i],
					id[i], size[i], status[i], 
					avgWire[i], fitChisqProb[i], fitInterc[i], fitIntercErr[i], fitSlope[i],
					fitSlopeErr[i],
					hit1_ID[i], hit2_ID[i], hit3_ID[i], hit4_ID[i], hit5_ID[i], hit6_ID[i], 
					hit7_ID[i], hit8_ID[i], hit9_ID[i], hit10_ID[i], hit11_ID[i], hit12_ID[i]);
			
			add(cluster);
		}
		

	}
	

	/**
	 * Obtain the cluster from the id
	 * @param id the id of the cluster
	 * @return the cluster, or <code>null</code>
	 */
	public DCCluster fromClusterId(short id) {
		if (id < 1 ) {
			return null;
		}
		
		if (!isEmpty()) {
			for (DCCluster cluster : this) {
				if (cluster.id == id) {
					return cluster;
				}
			}
		}
		
		return null;
	}

}
