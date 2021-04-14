package cnuphys.magfield;

class GEMCCompare {
	public float x;
	public float y;
	public float z;
	
	public int gn1;
	public int gn2;
	public int gn3;

	//fields in clas system
	public float bX;
	public float bY;
	public float bZ;
	
	private float[] bced = new float[3];
	private int[] nced = new int[3];
	
	private boolean verbose = false;
	private FieldProbe _probe;
	
	private  double rho;
 
	private double phi;
	
	public GEMCCompare(FieldProbe probe, float x, float y, float z, int gn1, int gn2, int gn3, float bx, float by, float bz) {
		super();
		_probe = probe;
		this.x = x;
		this.y = y;
		this.z = z;
		this.gn1 = gn1;
		this.gn2 = gn2;
		this.gn3 = gn3;
		this.bX = bx;
		this.bY = by;
		this.bZ = bz;
		
		//get "ced" nearest neighbor indices
		
		rho = FastMath.sqrt(x * x + y * y);
		phi = FastMath.atan2Deg(y, x);

        // 12-fold symmmetric
		// relativePhi (-30, 30) phi relative to middle of sector
		phi = Math.abs(relativePhi(phi));
		
		cedIndices(phi, rho, z, nced);
		probe.field(x, y, z, bced);

	}

	@Override
	public String toString() {
		String s1 = String.format("---------------\nxyz = (%7.3f, %7.3f, %7.3f) cm \n",
				x, y, z);
		
//		s1 += String.format("GEMC grid indices [%2d, %4d, %3d]\n",  gn1, gn2, gn3);
//		s1 += String.format(" CED grid indices [%2d, %4d, %3d]\n",  nced[0], nced[1], nced[2]);

		s1 += String.format("GEMC field (%11.4e, %11.4e, %11.4e) kG\n",   10*bX, 10*bY, 10*bZ);
		s1 += String.format(" CED field (%11.4e, %11.4e, %11.4e) kG\n",   bced[0], bced[1], bced[2]);

		
		String s2 = "";

		if (verbose) {
			GridCoordinate q1Coord = _probe.q1Coordinate;
			GridCoordinate q2Coord = _probe.q2Coordinate;
			GridCoordinate q3Coord = _probe.q3Coordinate;
			
			int n1 = q1Coord.getIndex(phi);
			int n2 = q2Coord.getIndex(rho);
			int n3 = q3Coord.getIndex(z);
			
			
			
			
			s2 = String.format("\nced enclosed indices [%2d, %4d, %3d]\n", n1, n2, n3);
			s2 += String.format("local coordinates phi = %8.5f deg   rho = %7.3f cm   z = %7.3f cm \n", phi, rho, z);
			s2 += String.format("ced cell phi limits: %-7.3f to %-7.3f \n", q1Coord.getValue(n1), q1Coord.getValue(n1+1));
			s2 += String.format("ced cell rho limits: %-7.3f to %-7.3f \n", q2Coord.getValue(n2), q2Coord.getValue(n2+1));
			s2 += String.format("ced cell   z limits: %-7.3f to %-7.3f \n", q3Coord.getValue(n3), q3Coord.getValue(n3+1));

//			for (int i = 0; i < 2; i++ ) {
//				int nn1 = n1 + i;
//				for (int j = 0; j < 2; j++ ) {
//					int nn2 = n2 + j;
//					for (int k = 0; k < 2; k++ ) {
//						int nn3 = n3 + k;
//						int index = _probe.getCompositeIndex(nn1, nn2, nn3);
//						float bx = _probe.getB1(index);
//						float by = _probe.getB2(index);
//						float bz = _probe.getB3(index);
//						
//						s2 += String.format("corner[%1d, %1d, %1d] field: (%12.5e, %12.5e, %12.5e) kG\n", i, j, k, bx, by, bz);
//					}
//				}
//			}
			
			
		}
		
		
		return s1 + " " + s2;
	}
	
	
	// nearest neighbor algorithm
	private void cedIndices(double phi, double rho, double z, int indices[]) {
		
		GridCoordinate q1Coord = _probe.q1Coordinate;
		GridCoordinate q2Coord = _probe.q2Coordinate;
		GridCoordinate q3Coord = _probe.q3Coordinate;

		
		int n1 = q1Coord.getRoundedIndex(phi);
		int n2 = q2Coord.getRoundedIndex(rho);
		int n3 = q3Coord.getRoundedIndex(z);
		
		indices[0] = n1;
		indices[1] = n2;
		indices[2] = n3;
		
	}
	
	/**
	 * Must deal with the fact that we only have the field between 0 and 30 degrees.
	 *
	 * @param absolutePhi the absolute phi
	 * @return the relative phi (-30, 30) from the nearest middle of a sector in
	 *         degrees.
	 */
	private double relativePhi(double absolutePhi) {
		if (absolutePhi < 0.0) {
			absolutePhi += 360.0;
		}

		// make relative phi between 0 -30 and 30
		double relativePhi = absolutePhi;
		while (Math.abs(relativePhi) > 30.0) {
			relativePhi -= 60.0;
		}
		return relativePhi;
	}

	
}