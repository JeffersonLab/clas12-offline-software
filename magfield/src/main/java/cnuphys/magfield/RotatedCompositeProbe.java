package cnuphys.magfield;

import java.util.ArrayList;

public class RotatedCompositeProbe extends FieldProbe {

	// the angle in degrees
	private float _angle = -25.0f;
	private float _sin = (float) Math.sin(Math.toRadians(_angle));
	private float _cos = (float) Math.cos(Math.toRadians(_angle));

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
	 *
	 * @param xs
	 *            the x coordinate in cm
	 * @param ys
	 *            the y coordinate in cm
	 * @param zs
	 *            the z coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(float xs, float ys, float zs, float[] result) {
		
		float x = xs * _cos - zs * _sin;
		float y = ys;
		float z = zs * _cos + xs * _sin;

		float bx = 0, by = 0, bz = 0;
		for (FieldProbe probe : probes) {
			probe.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx * _cos + bz * _sin;
		result[1] = by;
		result[2] = bz * _cos - bx * _sin;
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
}
