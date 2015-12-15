package cnuphys.ced.cedview.sectorview;

import java.awt.geom.Point2D;

import bCNU3D.DoubleFormat;

public class CoordinateTransform {

	private SectorView _view;
	public CoordinateTransform(SectorView view) {
		_view = view;
	}
	
	public void labToWorld(double x, double y, double z, Point2D.Double wp) {
		labToWorld(x, y, z, wp, _view.getPhiRotate());
	}
	
	public void labToWorld(double x, double y, double z, Point2D.Double wp, double phi) {
		wp.x = z;
		phi = Math.toRadians(phi);
		int sector = getSector(x, y);
		
		double tanphi = Math.tan(phi);
		double xp = (x + tanphi*y)/(1 + tanphi*tanphi);
		double yp = xp*tanphi;
		
		double d = Math.hypot(xp, yp);
		
		if (sector >3) {
			d = -d;
		}
		wp.y = d;
	}

	public void worldToLab(double world[], Point2D.Double wp, double phi) {
		double z = wp.x;
		
		double rho = Math.abs(wp.y);
		phi = Math.toRadians(phi);
		
		double x = rho*Math.cos(phi);
		double y = rho*Math.sin(phi);
		
		world[0] = x;
		world[1] = y;
		world[2] = z;
	}
	
	public int getSector(double x, double y) {
		double phi = Math.toDegrees(Math.atan2(y, x));

		if ((-30 < phi) && (phi <= 30)) {
			return 1;
		}
		else if ((30 < phi) && (phi <= 90)) {
			return 2;
		}
		else if ((90 < phi) && (phi <= 150)) {
			return 3;
		}
		else if ((-90 < phi) && (phi <= -30)) {
			return 6;
		}
		else if ((-150 < phi) && (phi <= -90)) {
			return 5;
		}
		else {
			return 4;
		}
	}
	
	
	public static void main(String arg[]) {
		double z = 100;
		double phiRotate[] = {5, 65, 125, 5, 65, 125};
		double phi[] = {20, 80, 140, -160, -100, -40};
		
		Point2D.Double wp = new Point2D.Double();
		double xyz[] = new double[3];
		
		CoordinateTransform ct = new CoordinateTransform(null);
		
		for (int i = 0; i < phi.length; i++) {
			double x = 120*Math.cos(Math.toRadians(phi[i]));
			double y = 120*Math.sin(Math.toRadians(phi[i]));
			
			int sector = ct.getSector(x, y);
			ct.labToWorld(x, y, z, wp, phiRotate[i]);
			ct.worldToLab(xyz, wp, phi[i]);
			
			String s = "(" + DoubleFormat.doubleFormat(x, 1) + ", " + DoubleFormat.doubleFormat(y, 1) + 
					", " + DoubleFormat.doubleFormat(z, 1)+ ") ";
			String s2 = " (" + DoubleFormat.doubleFormat(xyz[0], 1) + ", " + DoubleFormat.doubleFormat(xyz[1], 1) + 
					", " + DoubleFormat.doubleFormat(xyz[2], 1)+ ") ";
			
			System.out.println(s + "sector: " + sector + " wp: " + wp + s2);
		}
		
		
		
	}

}
