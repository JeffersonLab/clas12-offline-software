package cnuphys.ced.event.data;

import cnuphys.ced.alldata.ColumnData;

public class CVT {
	
	/** the columns */
	public short[] id;
	public byte[] detector;
	public byte[] sector;
	public byte[] layer;
	public float[] x; //cm
	public float[] y;
	public float[] z;
	
	public float[] phi;
	public float[] theta;
	public float[] langle;
	public float[] centroid;
	public float[] path;


	private static CVT _instance;

	/**
	 * Public access to the singleton
	 * 
	 * @return the CTOF singleton
	 */
	public static CVT getInstance() {
		if (_instance == null) {
			_instance = new CVT();
		}
		return _instance;
	}

	public void fillData() {
		id = ColumnData.getShortArray("CVTRec::Trajectory.id");
		detector = ColumnData.getByteArray("CVTRec::Trajectory.detector");
		sector = ColumnData.getByteArray("CVTRec::Trajectory.sector");
		layer = ColumnData.getByteArray("CVTRec::Trajectory.layer");
		x = ColumnData.getFloatArray("CVTRec::Trajectory.x");
		y = ColumnData.getFloatArray("CVTRec::Trajectory.y");
		z = ColumnData.getFloatArray("CVTRec::Trajectory.z");
		
		phi = ColumnData.getFloatArray("CVTRec::Trajectory.phi");
		theta = ColumnData.getFloatArray("CVTRec::Trajectory.theta");
		langle = ColumnData.getFloatArray("CVTRec::Trajectory.langle");
		centroid = ColumnData.getFloatArray("CVTRec::Trajectory.centroid");
		path = ColumnData.getFloatArray("CVTRec::Trajectory.path");
	}
	

}