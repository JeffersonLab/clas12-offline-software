package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.ced.alldata.ColumnData;

public class DCHitList extends Vector<DCHit> {

	private String _error;

	public DCHitList(String bankName) {

		byte sector[] = ColumnData.getByteArray(bankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;
		
		byte[] superlayer = ColumnData.getByteArray(bankName + ".superlayer");;
		byte[] layer6 = ColumnData.getByteArray(bankName + ".layer");;
        short[] status = ColumnData.getShortArray(bankName + ".status");
        float[] z1 = ColumnData.getFloatArray(bankName + ".SegEndPoint1Z");
        float[] x2 = ColumnData.getFloatArray(bankName + ".SegEndPoint2X");
        float[] z2 = ColumnData.getFloatArray(bankName + ".SegEndPoint2Z");

	}
	
}
