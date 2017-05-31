package cnuphys.magfield;

import java.util.ArrayList;

public class CompositeProbe extends FieldProbe {
	
	private ArrayList<FieldProbe> probes = new ArrayList<FieldProbe>();

	public CompositeProbe(CompositeField field) {
		super(field);
		for (IField f : field) {
			probes.add(FieldProbe.factory(f));
		}
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
