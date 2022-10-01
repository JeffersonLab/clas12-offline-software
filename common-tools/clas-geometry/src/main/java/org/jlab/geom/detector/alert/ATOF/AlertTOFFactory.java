/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//package clas12vis;

package org.jlab.geom.detector.alert.ATOF;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.component.ScintillatorPaddle;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Transformation3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author viktoriya
 * this is the latest ATOF geometry class to be used in reco. and in GEMC simulations!
 * commit on July 02, 2020
 */
public class AlertTOFFactory implements Factory<AlertTOFDetector, AlertTOFSector, AlertTOFSuperlayer, AlertTOFLayer> {

	private final int nsectors = 15;
	private final int nsuperl  = 2;
	private final int nlayers1 = 10;
	private final int npaddles = 4;

	private final double openAng_pad_deg    = 6.0;
	private final double openAng_pad_rad    = Math.toRadians(openAng_pad_deg);
	private final double openAng_sector_rad = npaddles * openAng_pad_rad;

	@Override
	public AlertTOFDetector createDetectorCLAS(ConstantProvider cp) {
		return createDetectorSector(cp);
	}

	@Override
	public AlertTOFDetector createDetectorSector(ConstantProvider cp) {
		return createDetectorTilted(cp);
	}

	@Override
	public AlertTOFDetector createDetectorTilted(ConstantProvider cp) {
		return createDetectorLocal(cp);
	}

	@Override
	public AlertTOFDetector createDetectorLocal(ConstantProvider cp) {
		AlertTOFDetector detector = new AlertTOFDetector();
		for (int sectorId = 0; sectorId < nsectors; sectorId++)
		     detector.addSector(createSector(cp, sectorId));
		return detector;
	}

	@Override
	public AlertTOFSector createSector(ConstantProvider cp, int sectorId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		AlertTOFSector sector = new AlertTOFSector(sectorId);
		for (int superlayerId = 0; superlayerId < nsuperl; superlayerId++)
		     sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
		return sector;
	}

	@Override
	public AlertTOFSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		if (!(0 <= superlayerId && superlayerId < nsuperl)) throw new IllegalArgumentException("Error: invalid superlayer=" + superlayerId);
		AlertTOFSuperlayer superlayer = new AlertTOFSuperlayer(sectorId, superlayerId);

		if (superlayerId == 0) {
			int nlayers0 = 1;
			for (int layerId = 0; layerId < nlayers0; layerId++)
			     superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
		} else {
			for (int layerId = 0; layerId < nlayers1; layerId++)
			     superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));
		}
		return superlayer;
	}

	@Override
	public AlertTOFLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		if (!(0 <= superlayerId && superlayerId < nsuperl)) throw new IllegalArgumentException("Error: invalid superlayer=" + superlayerId);
		if (!(0 <= layerId && layerId < nlayers1)) throw new IllegalArgumentException("Error: invalid layer=" + layerId);

		double R0  = 77.0d;
		double R1  = 80.0d;
		double dR0 = 3.0d;
		double dR1 = 20.0d;

		// trapezoide dimensions for a bigger paddle (external)
		double pad_b1 = 8.17369; // mm
		double pad_b2 = 10.27; // mm

		double pad_z = 279.7; // mm
		if (superlayerId == 1) pad_z = 27.7; // mm

		// trapezoide dimensions for a smaller paddle (internal)
		double small_pad_b1 = 7.85924; // mm
		double small_pad_b2 = 8.17369; // mm

		double gap_pad_z = 0.3d; // mm, gap between paddles in z

		AlertTOFLayer layer = new AlertTOFLayer(sectorId, superlayerId, layerId);

		List<Plane3D> planes = new ArrayList<>();

		double len_b   = layerId * pad_z + layerId * gap_pad_z; // back paddle plan
		double len_f   = len_b + pad_z; // front paddle plan
		double Rl      = R0;
		double dR      = dR0;
		double widthTl = small_pad_b2;
		double widthBl = small_pad_b1;

		if (superlayerId == 1) {
			Rl      = R1;
			dR      = dR1;
			widthTl = pad_b2;
			widthBl = pad_b1;
		}

		for (int padId = 0; padId < npaddles; padId++) {
			Point3D p0 = new Point3D(-dR / 2, -widthBl / 2, len_f);
			Point3D p1 = new Point3D(dR / 2, -widthTl / 2, len_f);
			Point3D p2 = new Point3D(dR / 2, widthTl / 2, len_f);
			Point3D p3 = new Point3D(-dR / 2, widthBl / 2, len_f);

			Point3D            p4     = new Point3D(-dR / 2, -widthBl / 2, len_b);
			Point3D            p5     = new Point3D(dR / 2, -widthTl / 2, len_b);
			Point3D            p6     = new Point3D(dR / 2, widthTl / 2, len_b);
			Point3D            p7     = new Point3D(-dR / 2, widthBl / 2, len_b);
			ScintillatorPaddle Paddle = new ScintillatorPaddle(sectorId * 4 + padId, p0, p1, p2, p3, p4, p5, p6, p7);

			double openAng_sector_deg = npaddles * openAng_pad_deg;
			Paddle.rotateZ(Math.toRadians(padId * openAng_pad_deg + sectorId * openAng_sector_deg));

			double xoffset;
			double yoffset;

			xoffset = (Rl + dR / 2) * Math.cos(padId * openAng_pad_rad + sectorId * openAng_sector_rad);
			yoffset = (Rl + dR / 2) * Math.sin(padId * openAng_pad_rad + sectorId * openAng_sector_rad);

			Paddle.translateXYZ(xoffset, yoffset, 0);

			// Add the paddles to the list
			layer.addComponent(Paddle);
		}

		Plane3D plane = new Plane3D(0, Rl, 0, 0, 1, 0);
		plane.rotateZ(sectorId * openAng_sector_rad - Math.toRadians(90));
		planes.add(plane);

		return layer;
	}

	/**
	 * Returns "Alert TOF Factory".
	 *
	 * @return "Alert TOF Factory"
	 */
	@Override
	public String getType() {
		return "Alert TOF Factory";
	}

	@Override
	public void show() {
		System.out.println(this);
	}

	@Override
	public String toString() {
		return getType();
	}

	@Override
	public Transformation3D getTransformation(ConstantProvider cp, int sector, int superlayer, int layer) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DetectorTransformation getDetectorTransform(ConstantProvider cp) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
