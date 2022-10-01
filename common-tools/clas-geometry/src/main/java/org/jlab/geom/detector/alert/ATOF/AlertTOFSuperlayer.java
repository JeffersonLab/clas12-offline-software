package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractSuperlayer;

public class AlertTOFSuperlayer extends AbstractSuperlayer<AlertTOFLayer> {

	protected AlertTOFSuperlayer(int sectorId, int superlayerId) {
		super(DetectorId.ATOF, sectorId, superlayerId);
	}

	/**
	 * Returns "Alert TOF Superlayer".
	 *
	 * @return "Alert TOF Superlayer"
	 */
	@Override
	public String getType() {
		return "Alert TOF Superlayer";
	}
}
