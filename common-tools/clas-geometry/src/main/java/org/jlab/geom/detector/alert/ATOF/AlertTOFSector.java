package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSector;

public class AlertTOFSector extends AbstractSector<AlertTOFSuperlayer> {

	protected AlertTOFSector(int sectorId) {
		super(DetectorId.ATOF, sectorId);
	}

	/**
	 * Returns "Alert TOF Sector".
	 *
	 * @return "Alert TOF Sector"
	 */
	@Override
	public String getType() {
		return "Alert TOF Sector";
	}
}
