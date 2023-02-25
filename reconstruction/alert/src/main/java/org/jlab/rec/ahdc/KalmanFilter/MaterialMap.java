package org.jlab.rec.ahdc.KalmanFilter;

import org.jlab.clas.tracking.kalmanfilter.Material;
import org.jlab.clas.tracking.kalmanfilter.Units;

import java.util.HashMap;

public class MaterialMap {

	public static HashMap<String, Material> generateMaterials() {
		Units units = Units.CM;

		String name_De      = "deuteriumGas";
		double thickness_De = 1;
		double density_De   = 9.37E-4;
		double ZoverA_De    = 0.496499;
		double X0_De        = 0;
		double IeV_De       = 19.2;

		org.jlab.clas.tracking.kalmanfilter.Material deuteriumGas = new org.jlab.clas.tracking.kalmanfilter.Material(name_De, thickness_De, density_De, ZoverA_De, X0_De, IeV_De, units);

		String name_Bo      = "BONuS12Gas";
		double thickness_Bo = 1;
		double density_Bo   = 4.9778E-4;
		double ZoverA_Bo    = 0.49989;
		double X0_Bo        = 0;
		double IeV_Bo       = 73.8871;

		org.jlab.clas.tracking.kalmanfilter.Material BONuS12 = new org.jlab.clas.tracking.kalmanfilter.Material(name_Bo, thickness_Bo, density_Bo, ZoverA_Bo, X0_Bo, IeV_Bo, units);

		String name_My      = "Mylar";
		double thickness_My = 1;
		double density_My   = 1.4;
		double ZoverA_My    = 0.501363;
		double X0_My        = 0;
		double IeV_My       = 78.7;

		org.jlab.clas.tracking.kalmanfilter.Material Mylar = new org.jlab.clas.tracking.kalmanfilter.Material(name_My, thickness_My, density_My, ZoverA_My, X0_My, IeV_My, units);

		String name_Ka      = "Kapton";
		double thickness_Ka = 1;
		double density_Ka   = 1.42;
		double ZoverA_Ka    = 0.500722;
		double X0_Ka        = 0;
		double IeV_Ka       = 79.6;

		org.jlab.clas.tracking.kalmanfilter.Material Kapton = new org.jlab.clas.tracking.kalmanfilter.Material(name_Ka, thickness_Ka, density_Ka, ZoverA_Ka, X0_Ka, IeV_Ka, units);

		return new HashMap<String, Material>() {
			{
				put("deuteriumGas", deuteriumGas);
				put("Kapton", Kapton);
				put("Mylar", Mylar);
				put("BONuS12Gas", BONuS12);
			}
		};
	}

}
