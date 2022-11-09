/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import org.jlab.geom.DetectorId;
import org.jlab.geom.abs.AbstractLayer;
import org.jlab.geom.prim.Plane3D;

/**
 * @author sergeyeva
 */
public class AlertDCLayer extends AbstractLayer<AlertDCWire> {
	private final Plane3D midplane = new Plane3D();

	protected AlertDCLayer(int sectorId, int superlayerId, int layerId) {
		super(DetectorId.AHDC, sectorId, superlayerId, layerId, false);
	}

	/**
	 * Returns "ALERT DC Layer".
	 *
	 * @return "ALERT DC Layer"
	 */
	@Override
	public String getType() {
		return "ALERT DC Layer";
	}

	/**
	 * Returns the plane that bisects the region containing the layer.
	 * In nominal alignments, for sectors 0 and 3 this plane is in the xz-plane.
	 *
	 * @return the plane that bisects the region containing the layer
	 */
	public Plane3D getMidplane() {
		return midplane;
	}
}
