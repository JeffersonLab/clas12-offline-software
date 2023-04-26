package org.jlab.rec.ahdc.Hit;


public class Hit implements Comparable<Hit> {

	private final int    id;
	private final int    superLayerId;
	private final int    layerId;
	private final int    wireId;
	private final double doca;

	private double  phi;
	private double  radius;
	private int     nbOfWires;
	private boolean use = false;
	private double  x;
	private double  y;

	public Hit(int _Id, int _Super_layer, int _Layer, int _Wire, double _Doca) {
		this.id           = _Id;
		this.superLayerId = _Super_layer;
		this.layerId      = _Layer;
		this.wireId       = _Wire;
		this.doca         = _Doca;
		wirePosition();
	}

	private void wirePosition() {
		final double DR_layer = 4.0;
		final double round    = 360.0;

		double numWires = 32.0;
		double R_layer  = 47.0;

		switch (this.superLayerId) {
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

		R_layer = R_layer + DR_layer * this.layerId;
		double alphaW_layer = Math.toRadians(round / (numWires));
		double wx           = -R_layer * Math.sin(alphaW_layer * this.wireId);
		double wy           = -R_layer * Math.cos(alphaW_layer * this.wireId);

		this.nbOfWires = (int) numWires;
		this.phi       = Math.atan2(wy, wx);
		this.radius    = R_layer;
		this.x         = wx;
		this.y         = wy;
	}

	@Override
	public String toString() {
		return "Hit{" + "_Super_layer=" + superLayerId + ", _Layer=" + layerId + ", _Wire=" + wireId + ", _Doca=" + doca + ", _Phi=" + phi + '}';
	}

	@Override
	public int compareTo(Hit arg0) {
		if (this.superLayerId == arg0.superLayerId && this.layerId == arg0.layerId && this.wireId == arg0.wireId) {
			return 0;
		} else {
			return 1;
		}
	}

	public int getId() {
		return id;
	}

	public int getSuperLayerId() {
		return superLayerId;
	}

	public int getLayerId() {
		return layerId;
	}

	public int getWireId() {
		return wireId;
	}

	public double getDoca() {
		return doca;
	}

	public double getRadius() {
		return radius;
	}

	public int getNbOfWires() {
		return nbOfWires;
	}

	public boolean is_NoUsed() {
		return !use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
