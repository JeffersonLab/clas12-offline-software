package org.jlab.rec.fmt;

import org.apache.commons.math3.special.Erf;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Line3D;

public class Geometry {

	public Geometry() {}

	public static Point3D getStripsIntersection(
			double x0_inner, double x1_inner, double x0_outer, double x1_outer,
			double y0_inner, double y1_inner, double y0_outer, double y1_outer,
			double z0_inner, double z0_outer) {

		Line3D l_in  = new Line3D(x0_inner, y0_inner, z0_inner, x1_inner, y1_inner, z0_inner);
        Line3D l_out = new Line3D(x0_outer, y0_outer, z0_outer, x1_outer, y1_outer, z0_outer);

        return l_in.distance(l_out).midpoint();
	}

    public int getClosestStrip(double x, double y, int layer) {
        int closestStrip = 0;
        if (Math.sqrt(x*x+y*y) < Constants.FVT_Rmax && Math.sqrt(x*x+y*y) > Constants.FVT_Beamhole) {
            Point3D locPos = GeometryMethods.globalToLocal(new Point3D(x, y, 0), layer-1);

            if (locPos.y() > -(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && locPos.y() < (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)) {
            	if (locPos.x() >= 0)
					closestStrip = (int) (Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.)-locPos.y())/Constants.FVT_Pitch) + 1);
            	if (locPos.x() < 0)
					closestStrip = (int) ((Math.floor((locPos.y()+(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch) + 1)
							+ Constants.FVT_Halfstrips +0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips));
            }
            else if (locPos.y() <= -(Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && locPos.y() > -Constants.FVT_Rmax) {
            	closestStrip = (int) (Math.floor(((Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) - locPos.y())/Constants.FVT_Pitch) + 1);
            }
            else if (locPos.y() >= (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) && locPos.y() < Constants.FVT_Rmax) {
            	closestStrip = (int) (Math.floor((locPos.y() + (Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.))/Constants.FVT_Pitch)
						+ 1 + Constants.FVT_Halfstrips+0.5*( Constants.FVT_Nstrips-2.*Constants.FVT_Halfstrips));
            }
        }
        return closestStrip;
    }

    public double getWeightEstimate(int strip, int layer, double x, double y) {
        double sigmaDrift = 0.01;
        double strip_y = Constants.FVT_stripsYlocref[strip-1];
        double strip_x = Constants.FVT_stripsXlocref[strip-1];

        double strip_length = Constants.FVT_stripslength[strip-1];
        double sigma = sigmaDrift*Constants.hDrift;
        double wght = (Erf.erf((strip_y+Constants.FVT_Pitch/2. - y)/sigma/Math.sqrt(2)) - Erf.erf((strip_y-Constants.FVT_Pitch/2.-y)/sigma/Math.sqrt(2)))
				* (Erf.erf((strip_x+strip_length/2.-x)/sigma/Math.sqrt(2))-Erf.erf((strip_x-strip_length/2.-x)/sigma/Math.sqrt(2)))/2./2.;
        if (wght < 0) wght = -wght;

        return wght;
    }

    public double getCentroidEstimate(int layer, double x, double y, int clust_size) {
        Point3D locPos = GeometryMethods.globalToLocal(new Point3D(x, y, 0), layer-1);

        double cent = 0;
        double norm = 0;
        int closestStrip = this.getClosestStrip(x, y, layer);

        if (closestStrip >= 1 && closestStrip <= Constants.FVT_Nstrips) {
            for (int i = 0; i < clust_size; i++) {
                int strip_num = closestStrip + i;

				// Check if the strip exists or if it makes sense to look at it
                if ((strip_num < Constants.FVT_Halfstrips+Constants.FVT_Sidestrips+1)
						|| (closestStrip >= (Constants.FVT_Halfstrips + Constants.FVT_Sidestrips + 1)
						&& strip_num >= (Constants.FVT_Halfstrips+Constants.FVT_Sidestrips+1)
						&& strip_num<Constants.FVT_Nstrips+1)) {

					double normCoeff = getWeightEstimate(strip_num, layer, locPos.x(), locPos.y());
                    cent += normCoeff * (double) strip_num;
                    norm += normCoeff;

					// If in central part, then check the top/bottom strip systematically
                    if (Math.abs(Constants.FVT_stripsYlocref[strip_num-1]) < Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) {
                        strip_num = 2*Constants.FVT_Halfstrips + Constants.FVT_Sidestrips + 1 - strip_num;
                        normCoeff = getWeightEstimate(strip_num, layer, locPos.x(), locPos.y());
                        cent += normCoeff * (double) strip_num;
                        norm += normCoeff;
                    }
                }

                strip_num = closestStrip - i;

				// Deal with the discontinuity between strip 1 and 833, or 321-513.
				if (strip_num < 1 || (strip_num <= Constants.FVT_Halfstrips + Constants.FVT_Sidestrips
						&& closestStrip > Constants.FVT_Halfstrips + Constants.FVT_Sidestrips))
					strip_num = 2*Constants.FVT_Halfstrips + Constants.FVT_Sidestrips + 1 - strip_num;
				double normCoeff = getWeightEstimate(strip_num, layer, locPos.x(), locPos.y());
                cent += normCoeff * (double) strip_num;
                norm += normCoeff;

				// If in central part, then check the top/bottom strip systematically
                if (Math.abs(Constants.FVT_stripsYlocref[strip_num-1]) < Constants.FVT_Halfstrips*Constants.FVT_Pitch/2.) {
                    strip_num = 2*Constants.FVT_Halfstrips + Constants.FVT_Sidestrips + 1 - strip_num;
                    normCoeff = getWeightEstimate(strip_num, layer, locPos.x(), locPos.y());
                    cent += normCoeff*(double)strip_num;
                    norm += normCoeff;
                }
            }
        	if (norm > 0) cent/=norm;
        }
        return cent;
    }
}
