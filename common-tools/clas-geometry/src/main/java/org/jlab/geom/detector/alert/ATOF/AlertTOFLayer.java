package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.component.ScintillatorPaddle;

public class AlertTOFLayer extends AbstractLayer<ScintillatorPaddle> {

	protected AlertTOFLayer(int sectorId, int superlayerId, int layerId) {
		super(DetectorId.ATOF, sectorId, superlayerId, layerId, false);
	}

	/**
	 * Returns "Alert TOF Layer".
	 *
	 * @return "Alert TOF Layer"
	 */
	@Override
	public String getType() {
		return "Alert TOF Layer";
	}
}
