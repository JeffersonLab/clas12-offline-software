/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.geom.detector.alert.AHDC;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.DetectorTransformation;
import org.jlab.geom.base.Factory;
import org.jlab.geom.prim.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A Low Energy Recoil Tracker (ALERT)
 * This is to implement ALERT Drift Chamber detector (AHDC).
 * There are 1 {@code MYSector_AHDC}
 * and 5 {@code MYSuperlayer_AHDC} per AHDC Detector.
 * There are 2 {@code MYLayer_AHDC}s for Superlayers 1,2,3
 * and 1 {@code MYLayer_AHDC} for Superlayers 0,4.
 * Each layer contains N {@code DriftChamberWire}s. Number of wires is different from one Superlayer to another.
 * However, real AHDC cell shape is different from regular hexagon given by {@code DriftChamberWire}.
 * Implemented following the DCDetector example
 *
 * @author sergeyeva
 */
public class AlertDCFactory implements Factory<AlertDCDetector, AlertDCSector, AlertDCSuperlayer, AlertDCLayer> {

	private final int nsuperl = 5; // 5 for AHDC
	private final int nlayers = 2; // 1 if superlayerId = 0 OR 4, 2 if superlayerId = 1 OR 2 OR 3 for AHDC

	@Override
	public AlertDCDetector createDetectorCLAS(ConstantProvider cp) {
		return createDetectorSector(cp);
	}

	@Override
	public AlertDCDetector createDetectorSector(ConstantProvider cp) {
		return createDetectorTilted(cp);
	}

	@Override
	public AlertDCDetector createDetectorTilted(ConstantProvider cp) {
		return createDetectorLocal(cp);
	}

	int nsectors = 1;

	@Override
	public AlertDCDetector createDetectorLocal(ConstantProvider cp) {
		AlertDCDetector detector = new AlertDCDetector();
		for (int sectorId = 0; sectorId < nsectors; sectorId++)
		     detector.addSector(createSector(cp, sectorId));
		return detector;
	}


	@Override
	public AlertDCSector createSector(ConstantProvider cp, int sectorId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		AlertDCSector sector = new AlertDCSector(sectorId);
		for (int superlayerId = 0; superlayerId < nsuperl; superlayerId++)
		     sector.addSuperlayer(createSuperlayer(cp, sectorId, superlayerId));
		return sector;
	}


	@Override
	public AlertDCSuperlayer createSuperlayer(ConstantProvider cp, int sectorId, int superlayerId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		if (!(0 <= superlayerId && superlayerId < nsuperl)) throw new IllegalArgumentException("Error: invalid superlayer=" + superlayerId);
		AlertDCSuperlayer superlayer = new AlertDCSuperlayer(sectorId, superlayerId);

		for (int layerId = 0; layerId < nlayers; layerId++)
		     superlayer.addLayer(createLayer(cp, sectorId, superlayerId, layerId));

		return superlayer;
	}

	@Override
	public AlertDCLayer createLayer(ConstantProvider cp, int sectorId, int superlayerId, int layerId) {
		if (!(0 <= sectorId && sectorId < nsectors)) throw new IllegalArgumentException("Error: invalid sector=" + sectorId);
		if (!(0 <= superlayerId && superlayerId < nsuperl)) throw new IllegalArgumentException("Error: invalid superlayer=" + superlayerId);
		if (!(0 <= layerId && layerId < nlayers)) throw new IllegalArgumentException("Error: invalid layer=" + layerId);

		AlertDCLayer layer = new AlertDCLayer(sectorId, superlayerId, layerId);

		// Load constants AHDC
		// Length in Z mm!
		double round    = 360.0d;
		double numWires;
		double R_layer  = 32.0d;
		double DR_layer = 4.0d;

		double   zoff1 = 0.0d;
		double   zoff2 = 300.0d;
		Point3D  p1    = new Point3D(R_layer, 0, zoff1);
		Vector3D n1    = new Vector3D(0, 0, 1);

		Plane3D lPlane = new Plane3D(p1, n1);

		Point3D  p2     = new Point3D(R_layer, 0, zoff2);
		Vector3D n2     = new Vector3D(0, 0, 1);
		Plane3D  rPlane = new Plane3D(p2, n2);

		if (superlayerId == 0 || superlayerId == 4) {
			if (layerId == 1) return layer;

		}

		if (superlayerId == 0) {
			numWires = 47.0d; //47
			R_layer  = 32.0d;
		} else if (superlayerId == 1) {
			numWires = 56.0d; //56
			R_layer  = 38.0d;
		} else if (superlayerId == 2) {
			numWires = 72.0d; //72
			R_layer  = 48.0d;
		} else if (superlayerId == 3) {
			numWires = 87.0d;
			R_layer  = 58.0d;
		} else {
			numWires = 99.0d;
			R_layer  = 68.0d;
		}

		// Calculate the radius for the layers of sense wires
		R_layer = R_layer + DR_layer * layerId;

		double alphaW_layer = Math.toRadians(round / (numWires));

		// shift the wire end point +-20deg in XY plan
		double thster = Math.toRadians(20.0d);
		double zl     = 300.0d;

		// Create AHDC sense wires
		for (int wireId = 0; wireId < numWires; wireId++) {

			// The point given by (wx, wy, wz) is the midpoint of the current wire.
			double wx = -R_layer * Math.sin(alphaW_layer * wireId);
			double wy = -R_layer * Math.cos(alphaW_layer * wireId);

			// Find the interesection of the current wire with the end-plate
			// planes by construciting a long line that passes through the midpoint
			double wx_end = -R_layer * Math.sin(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			double wy_end = -R_layer * Math.cos(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			Line3D line   = new Line3D(wx, wy, 0, wx_end, wy_end, zl);

			Point3D lPoint = new Point3D();
			Point3D rPoint = new Point3D();
			lPlane.intersection(line, lPoint);
			rPlane.intersection(line, rPoint);
			// All wire go from left to right
			Line3D wireLine = new Line3D(lPoint, rPoint);
			// Do not change the code above. It is for signal wires positioning

			// Construct the cell around the signal wires created above top
			double px_0 = -(R_layer + 2) * Math.sin(alphaW_layer * wireId);
			double py_0 = -(R_layer + 2) * Math.cos(alphaW_layer * wireId);
			double px_1 = -(R_layer + 2) * Math.sin(alphaW_layer * wireId + alphaW_layer / 2);
			double py_1 = -(R_layer + 2) * Math.cos(alphaW_layer * wireId + alphaW_layer / 2);
			double px_2 = -(R_layer - 2) * Math.sin(alphaW_layer * wireId + alphaW_layer / 2);
			double py_2 = -(R_layer - 2) * Math.cos(alphaW_layer * wireId + alphaW_layer / 2);
			double px_3 = -(R_layer - 2) * Math.sin(alphaW_layer * wireId);
			double py_3 = -(R_layer - 2) * Math.cos(alphaW_layer * wireId);
			double px_4 = -(R_layer - 2) * Math.sin(alphaW_layer * wireId - alphaW_layer / 2);
			double py_4 = -(R_layer - 2) * Math.cos(alphaW_layer * wireId - alphaW_layer / 2);
			double px_5 = -(R_layer + 2) * Math.sin(alphaW_layer * wireId - alphaW_layer / 2);
			double py_5 = -(R_layer + 2) * Math.cos(alphaW_layer * wireId - alphaW_layer / 2);
			// bottom (do not forget to add the +20 deg. twist respect to the "straight" version)
			double px_6  = -(R_layer + 2) * Math.sin(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			double py_6  = -(R_layer + 2) * Math.cos(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			double px_7  = -(R_layer + 2) * Math.sin(alphaW_layer * wireId + alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double py_7  = -(R_layer + 2) * Math.cos(alphaW_layer * wireId + alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double px_8  = -(R_layer - 2) * Math.sin(alphaW_layer * wireId + alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double py_8  = -(R_layer - 2) * Math.cos(alphaW_layer * wireId + alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double px_9  = -(R_layer - 2) * Math.sin(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			double py_9  = -(R_layer - 2) * Math.cos(alphaW_layer * wireId + thster * (Math.pow(-1, superlayerId)));
			double px_10 = -(R_layer - 2) * Math.sin(alphaW_layer * wireId - alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double py_10 = -(R_layer - 2) * Math.cos(alphaW_layer * wireId - alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double px_11 = -(R_layer + 2) * Math.sin(alphaW_layer * wireId - alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));
			double py_11 = -(R_layer + 2) * Math.cos(alphaW_layer * wireId - alphaW_layer / 2 + thster * (Math.pow(-1, superlayerId)));

			// Group into points with (x,y,z) coordinates
			List<Point3D> firstF  = new ArrayList<>();
			List<Point3D> secondF = new ArrayList<>();
			// first Face
			Point3D p_0 = new Point3D(px_0, py_0, 0.0d);
			Point3D p_1 = new Point3D(px_1, py_1, 0.0d);
			Point3D p_2 = new Point3D(px_2, py_2, 0.0d);
			Point3D p_3 = new Point3D(px_3, py_3, 0.0d);
			Point3D p_4 = new Point3D(px_4, py_4, 0.0d);
			Point3D p_5 = new Point3D(px_5, py_5, 0.0d);
			// second Face
			Point3D p_6  = new Point3D(px_6, py_6, zl);
			Point3D p_7  = new Point3D(px_7, py_7, zl);
			Point3D p_8  = new Point3D(px_8, py_8, zl);
			Point3D p_9  = new Point3D(px_9, py_9, zl);
			Point3D p_10 = new Point3D(px_10, py_10, zl);
			Point3D p_11 = new Point3D(px_11, py_11, zl);
			// defining a cell around a wireLine, must be counter-clockwise!
			firstF.add(p_0);
			firstF.add(p_1);
			firstF.add(p_2);
			firstF.add(p_3);
			firstF.add(p_4);
			firstF.add(p_5);

			secondF.add(p_6);
			secondF.add(p_7);
			secondF.add(p_8);
			secondF.add(p_9);
			secondF.add(p_10);
			secondF.add(p_11);

			// Create the cell and signal wire inside
			// PrismaticComponent(int componentId, List<Point3D> firstFace, List<Point3D> secondFace)
			// not possible to add directly PrismaticComponent class because it is an ABSTRACT
			// a new class should be created: public class NewClassWire extends PrismaticComponent {...}
			// 5 top points & 5 bottom points with convexe shape. Concave shape is not supported.
			AlertDCWire wire = new AlertDCWire(wireId, wireLine, firstF, secondF);
			// Add wire object to the list
			layer.addComponent(wire);
		}

		return layer;
	}

	/**
	 * Returns "DC Factory".
	 *
	 * @return "DC Factory"
	 */
	@Override
	public String getType() {
		return "DC Factory";
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
