package cnuphys.ced.event.data;

import java.util.Vector;

import cnuphys.bCNU.log.Log;
import cnuphys.ced.alldata.ColumnData;


public class SegmentList extends Vector<Segment> {
	
	private String _error;

	public SegmentList(String bankName) {

		byte[] sector = ColumnData.getByteArray(bankName + ".sector");
		if ((sector == null) || (sector.length < 1)) {
			return;
		}
		
		int length = 0;

		byte[] superlayer = ColumnData.getByteArray(bankName + ".superlayer");;
        float[] x1 = ColumnData.getFloatArray(bankName + ".SegEndPoint1X");
        float[] z1 = ColumnData.getFloatArray(bankName + ".SegEndPoint1Z");
        float[] x2 = ColumnData.getFloatArray(bankName + ".SegEndPoint2X");
        float[] z2 = ColumnData.getFloatArray(bankName + ".SegEndPoint2Z");
        
		length = checkArrays(sector, superlayer, x1, z1, x2, z2);
		if (length < 0) {
			Log.getInstance().warning("[" + bankName + "] " + _error);
			return;
		}

		for (int i = 0; i < length; i++) {
			add(new Segment(sector[i], superlayer[i], x1[i], z1[i], x2[i], z2[i]));
		}

	}
	
	//check arrays are not null and have same length
	private int checkArrays(byte[] sector, byte[] superlayer, float[] x1, float[] z1, float[] x2, float[] z2) {
		if ((sector == null) || (superlayer == null) || (x1 == null) || (z1 == null) || (x2 == null)|| (z2 == null)) {
			_error = "Unexpected null array when creating SegmentList: " + "sector = null: " + (sector == null)
					+ " superlayer == null: " + (superlayer == null) + " x1 == null: " + (x1 == null) +
					" z1 == null: " + (z1 == null) + " x2 == null: " + (x2 == null) + " z2 == null: " + (z2 == null);
			return -1;
		}
		
		if (sector.length < 1) {
			_error = "Sector array has 0 length when creating SegmentList";
			return -1;
		}
		
		if (superlayer.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match super length: " + superlayer.length + " when creating SegmentList";
			return -1;
		}
		
		if (x1.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match x1 length: " + x1.length + " when creating SegmentList";
			return -1;
		}
		
		if (z1.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match z1 length: " + z1.length + " when creating SegmentList";
			return -1;
		}

		if (x2.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match x2 length: " + x2.length + " when creating SegmentList";
			return -1;
		}
		
		if (z2.length != sector.length) {
			_error = "Sector length: " + sector.length + " does not match z2 length: " + z2.length + " when creating SegmentList";
			return -1;
		}

		return sector.length;
	}
}
