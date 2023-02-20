package org.jlab.rec.ahdc.KalmanFilter;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Plane3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class Hit implements Comparable<Hit> {

	private final double thster = Math.toRadians(20.0);
	private final int    superLayer;
	private final int    layer;
	private final int    wire;
	private final double r;
	private final double doca;
	private final double numWires;
	private final Line3D line3D;

	public Hit(int superLayer, int layer, int wire, int numWire, double r, double doca) {
		this.superLayer = superLayer;
		this.layer      = layer;
		this.wire       = wire - 1;
		this.r          = r;
		this.doca       = doca;
		this.numWires = numWire;

		final double DR_layer = 4.0;
		final double round    = 360.0;
		final double thster   = Math.toRadians(20.0);
		final double zl       = 300.0;

		double numWires = 32.0;
		double R_layer  = 47.0;

		double zoff1 = 0.0d;
		double zoff2 = 300.0d;
		Point3D  p1 = new Point3D(R_layer, 0, zoff1);
		Vector3D n1 = new Vector3D(0, 0, 1);
		//n1.rotateY(-thopen);
		//n1.rotateZ(thtilt);
		Plane3D lPlane = new Plane3D(p1, n1);

		Point3D  p2 = new Point3D(R_layer, 0, zoff2);
		Vector3D n2 = new Vector3D(0, 0, 1);
		//n2.rotateY(thopen);
		//n2.rotateZ(thtilt);
		Plane3D rPlane = new Plane3D(p2, n2);

		switch (this.superLayer) {
			case 0:
				numWires = 47.0;
				R_layer = 32.0;
				break;
			case 1:
				numWires = 56.0;
				R_layer = 38.0;
				break;
			case 2:
				numWires = 72.0;
				R_layer = 48.0;
				break;
			case 3:
				numWires = 87.0;
				R_layer = 58.0;
				break;
			case 4:
				numWires = 99.0;
				R_layer = 68.0;
				break;
		}

		R_layer = R_layer + DR_layer * this.layer;
		double alphaW_layer = Math.toRadians(round / (numWires));
		double wx           = -R_layer * Math.sin(alphaW_layer * this.wire);
		double wy           = -R_layer * Math.cos(alphaW_layer * this.wire);

		double wx_end = -R_layer * Math.sin(alphaW_layer * this.wire + thster * (Math.pow(-1, this.superLayer)));
		double wy_end = -R_layer * Math.cos(alphaW_layer * this.wire + thster * (Math.pow(-1, this.superLayer)));

		Line3D line = new Line3D(wx, wy, -150, wx_end, wy_end, zl/2);

		Point3D lPoint = new Point3D();
		Point3D rPoint = new Point3D();
		lPlane.intersection(line, lPoint);
		rPlane.intersection(line, rPoint);
		// All wire go from left to right
		Line3D wireLine = new Line3D(lPoint, rPoint);

		this.line3D = wireLine;
	}

	public RealVector get_Vector() {
		return new ArrayRealVector(new double[]{this.doca});
	}

	public RealMatrix get_MeasurementNoise() {
		return new Array2DRowRealMatrix(new double[][]{{10}});
	}

	public double doca() {
		return doca;
	}

	public double r()    {return r;}

	public Line3D line() {return line3D;}

	public double distance(Point3D point3D) {
		return this.line3D.distance(point3D).length();
	}

	@Override
	public int compareTo(Hit o) {
		System.out.println("r = " + r + " other r = " + o.r);
		return Double.compare(r, o.r);
	}

	@Override
	public String toString() {
		return "Hit{" + "superLayer=" + superLayer + ", layer=" + layer + ", wire=" + wire + ", r=" + r + ", doca=" + doca + '}';
	}

	public RealVector get_Vector_beam() {
		return null;
	}

	public double getThster() {
		return thster;
	}

	public int getSuperLayer() {
		return superLayer;
	}

	public int getLayer() {
		return layer;
	}

	public int getWire() {
		return wire;
	}

	public double getR() {
		return r;
	}

	public double getDoca() {
		return doca;
	}

	public Line3D getLine3D() {
		return line3D;
	}

	public double getNumWires() {
		return numWires;
	}
}

