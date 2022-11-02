package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractDetector;

public class AlertTOFDetector extends AbstractDetector<AlertTOFSector> {

	protected AlertTOFDetector() {
		super(DetectorId.ATOF);
	}

	/**
	 * Returns "Alert TOF Detector".
	 *
	 * @return "Alert TOF Detector"
	 */
	@Override
	public String getType() {
		return "Alert TOF Detector";
	}
}
