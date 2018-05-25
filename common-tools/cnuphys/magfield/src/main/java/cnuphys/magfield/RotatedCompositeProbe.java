package cnuphys.magfield;

import java.util.ArrayList;



public class RotatedCompositeProbe extends FieldProbe {
	
	
	// the angle in degrees
	private double _angle = -25.0;
	private double _sin25 = Math.sin(Math.toRadians(_angle));
	private double _cos25 = Math.cos(Math.toRadians(_angle));

	private ArrayList<FieldProbe> probes = new ArrayList<FieldProbe>();


	public RotatedCompositeProbe(RotatedCompositeField field) {
		super(field);
		for (IField f : field) {
			probes.add(FieldProbe.factory(f));
		}
	}
	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * @param sector the sector [1..6]
	 * @param xs
	 *            the tilted x coordinate in cm
	 * @param ys
	 *            the tilted y coordinate in cm
	 * @param zs
	 *            the tilted z coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */	
	public void field(int sector, float xs, float ys, float zs, float[] result) {
		
		//first rotate location to get to the sector coordinate system
		float x = (float)(xs * _cos25 - zs * _sin25);
		float y = (float)(ys);
		float z = (float)(zs * _cos25 + xs * _sin25);
		
//		if (x < 0) {
//			System.err.println("NEGATIVE X " + x);
//			result[0] = 0;
//			result[1] = 0;
//			result[2] = 0;
//			return;
//		}
		
		
		//now rotate to the correct sector to get the lab coordinates. We can use the result array!
		MagneticFields.sectorToLab(sector, result, x, y, z);
		
		//test 
//		int testSect = MagneticFields.getSector(result[0], result[1]);
//		if (testSect!= sector) {
//			testSect = MagneticFields.getSector(result[0], result[1]);
//			System.err.println("Sectors don't match sector: " + sector + "  testSect:   " + testSect);
//			System.err.println("xs = " + xs + "   ys = " + ys);
//			System.err.println("PHI = " + Math.toDegrees(Math.atan2(result[1], result[0])));
//			System.exit(1);
//		}
	//	System.err.println("Sectors match");


		float bx = 0, by = 0, bz = 0;
		for (FieldProbe probe : probes) {
			probe.field((float) x, (float) y, (float) z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		
		MagneticFields.labToSector(sector, result, bx, by, bz);
		bx = result[0];
		by = result[1];
		bz = result[2];


		//now rotate the field in the opposite sense
		result[0] = (float) (bx * _cos25 + bz * _sin25);
		result[1] = (by);
		result[2] = (float) (bz * _cos25 - bx * _sin25);
	}



	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * THIS ASSUMES COORDINATES ARE IN A SECTOR 1 SECTOR SYSTEM 
	 *
	 * @param xs
	 *            the x coordinate in cm in sector 1 sector system
	 * @param ys
	 *            the y coordinate in cm in sector 1 sector system
	 * @param zs
	 *            the z coordinate in cm in sector 1 sector system
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(float xs, float ys, float zs, float[] result) {
		field(1, xs, ys, zs, result);

//		
//		//first rotate location
//		double x = xs * _cos - zs * _sin;
//		double y = ys;
//		double z = zs * _cos + xs * _sin;
//
////		System.out.println("NEW R: [" + x + ", " + y + ", " + z + "]");
//
//		float bx = 0, by = 0, bz = 0;
//		for (FieldProbe probe : probes) {
//			probe.field((float) x, (float) y, (float) z, result);
//			bx += result[0];
//			by += result[1];
//			bz += result[2];
//		}
//		
//
//		//now rotate the field in the opposite sense
//		result[0] = (float) (bx * _cos + bz * _sin);
//		result[1] = (by);
//		result[2] = (float) (bz * _cos - bx * _sin);
//		
	}


	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		
		float bphi = 0;
		float brho = 0;
		float bz = 0;
		
		for (FieldProbe probe : probes) {
			probe.fieldCylindrical(phi, rho, z, result);
			bphi += result[0];
			brho += result[1];
			bz += result[2];
		}
		
		result[0] = bphi;
		result[1] = brho;
		result[2] = bz;
	}
	

    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in cylindrical
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
     *
     * @param phi
     *            azimuthal angle in degrees.
     * @param rho
     *            the cylindrical rho coordinate in cm.
     * @param z
     *            coordinate in cm
     * @param result
     *            the result
     * @result a Cartesian vector holding the calculated field in kiloGauss.
     */
	@Override
    public void gradientCylindrical(double phi, double rho, double z,
    	    float result[]) {
		
		float bx = 0, by = 0, bz = 0;
		for (FieldProbe probe : probes) {
			probe.gradientCylindrical(phi, rho, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
   	
    }
	

    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in Cartesian
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
     *
     * @param x
     *            the x coordinate in cm
     * @param y
     *            the y coordinate in cm
     * @param z
     *            the z coordinate in cm
     * @param result
     *            a float array holding the retrieved field in kiloGauss. The
     *            0,1 and 2 indices correspond to x, y, and z components.
     */
     @Override
	public void gradient(float xs, float ys, float zs, float result[]) {
 		double x = xs * _cos25 - zs * _sin25;
 		double y = ys;
 		double z = zs * _cos25 + xs * _sin25;
 		float bx = 0, by = 0, bz = 0;
 		for (FieldProbe probe : probes) {
 			probe.gradient((float)x, (float)y, (float)z, result);
 			bx += result[0];
 			by += result[1];
 			bz += result[2];
 		}
		result[0] = (float) (bx * _cos25 + bz * _sin25);
		result[1] = (by);
		result[2] = (float) (bz * _cos25 - bx * _sin25);
     }

	
}
