package cnuphys.ced.micromegas;


public class Geometry {

    public Geometry() {

    }

    // Comments on the Geometry of the BMT
    // ------------------------------------
    // The BMT geometry consists of 3 cylindical double layers of MicroMegas.
    // The inner (i.e. closest to beam in double layer) layer contain
    // longitudinal strips oriented in the Z direction
    // and placed at constant pitch. This is called the Z layer.
    // The outer layer contain arched strips at a non-constant pitch in Z. This
    // is called the C layer.
    // The cylinder phi profile in divided into 3 sectors, A, B, C.
    //

    /**
     * 
     * @param sector the sector in CLAS12 1...3
     * @param layer the layer 1...6
     * @param strip the strip number (starts at 1)
     * @return the angle to localize the center of strip
     */
    private double CRZ_GetAngleStrip(int sector, int layer, int strip) {
	// Sector = num_detector + 1;
	// num_detector = 0 (region A), 1 (region B), 2, (region C)
	// For CRZ, this function returns the angle to localize the center of
	// strip "num_strip" for the "num_detector"
	int num_detector = sector - 1; // index of the detector (0...2)
	int num_strip = strip - 1; // index of the strip (starts at 0)
	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6

	// double
	// angle=Constants.CR6Z_edge[num_detector]+(Constants.CR6Z_Xpos+(Constants.CR6Z_width/2.+num_strip*(Constants.CR6Z_width+Constants.CR6Z_spacing)))/Constants.CR6Z_radius;
	double angle = Constants.CRZEDGE1[num_region][num_detector]
		+ (Constants.CRZXPOS[num_region]
			+ (Constants.CRZWIDTH[num_region] / 2.
				+ num_strip * (Constants.CRZWIDTH[num_region]
					+ Constants.CRZSPACING[num_region])))
			/ Constants.CRZRADIUS[num_region];
	if (angle > 2 * Math.PI)
	    angle -= 2 * Math.PI;
	return angle; // in rad
    }

    /**
     * 
     * @param layer the layer 1...6
     * @param angle the position angle of the hit in the Z detector
     * @return the Z strip as a function of azimuthal angle
     */
    public int getZStrip(int layer, double angle) { // the angle is the Lorentz
						    // uncorrected angle

	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6
	int num_detector = -1;

	for (int i = 0; i < 3; i++) {
	    if (angle >= Constants.CRZEDGE1[num_region][i]
		    && ((angle - Constants.CRZEDGE1[num_region][i]) < Math
			    .abs(Constants.CRZEDGE2[num_region][i]
				    - Constants.CRZEDGE1[num_region][i]))) {
		num_detector = i;
		break;
	    }
	}

	int strip_num = (int) Math
		.floor(((angle - Constants.CRZEDGE1[num_region][num_detector])
			* Constants.CRZRADIUS[num_region]
			- Constants.CRZXPOS[num_region]
			- Constants.CRZWIDTH[num_region] / 2.)
			/ (Constants.CRZWIDTH[num_region]
				+ Constants.CRZSPACING[num_region]));

	return strip_num + 1;
    }

    /**
     * 
     * @param layer the layer 1...6
     * @return the Z position of the strip center
     */
    private double CRZ_GetZStrip(int layer) {
	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6
	// For CR6Z, this function returns the Z position of the strip center
	double zc = Constants.CRZZMIN[num_region]
		+ Constants.CRZOFFSET[num_region]
		+ Constants.CRZLENGTH[num_region] / 2.;
	return zc; // in mm
    }

    /**
     * 
     * @param sector the sector in CLAS12 1...3
     * @param layer the layer 1...6
     * @return the angle to localize the beginning of the strips
     */
    private double CRC_GetBeginStrip(int sector, int layer) {
	// Sector = num_detector + 1;
	// num_detector = 0 (region A), 1 (region B), 2, (region C)

	int num_detector = sector - 1; // index of the detector (0...2)
	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6

	// For CRC, this function returns the angle to localize the beginning of
	// the strips
	double angle = Constants.CRCEDGE1[num_region][num_detector]
		+ Constants.CRCXPOS[num_region]
			/ Constants.CRCRADIUS[num_region];
	if (angle > 2 * Math.PI)
	    angle -= 2 * Math.PI;
	return angle; // in rad
    }

    /**
     * 
     * @param sector the sector in CLAS12 1...3
     * @param layer the layer 1...6
     * @return the angle to localize the end of the strips
     */
    private double CRC_GetEndStrip(int sector, int layer) {
	// Sector = num_detector + 1;
	// num_detector = 0 (region A), 1 (region B), 2, (region C)

	int num_detector = sector - 1; // index of the detector (0...2)
	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6

	// For CRC, this function returns the angle to localize the end of the
	// strips
	double angle = Constants.CRCEDGE1[num_region][num_detector]
		+ (Constants.CRCXPOS[num_region]
			+ Constants.CRCLENGTH[num_region])
			/ Constants.CRCRADIUS[num_region];
	if (angle > 2 * Math.PI)
	    angle -= 2 * Math.PI;
	return angle; // in rad
    }

    private double CRC_GetZStrip(int sector, int layer, int strip) {

	int num_strip = strip - 1; // index of the strip (starts at 0)
	int num_region = (layer + 1) / 2 - 1; // region index (0...2)
						    // 0=layers 1&2, 1=layers
						    // 3&4, 2=layers 5&6

	// For CR6C, this function returns the Z position of the strip center
	int group = 0;
	int limit = Constants.CRCGROUP[num_region][group];
	double zc = Constants.CRCZMIN[num_region]
		+ Constants.CRCOFFSET[num_region]
		+ Constants.CRCWIDTH[num_region][group] / 2.;

	if (num_strip > 0) {
	    for (int j = 1; j < num_strip + 1; j++) {
		zc += Constants.CRCWIDTH[num_region][group] / 2.;
		if (j >= limit) { // test if we change the width
		    group++;
		    limit += Constants.CRCGROUP[num_region][group];
		}
		zc += Constants.CRCWIDTH[num_region][group] / 2.
			+ Constants.CRCSPACING[num_region];
	    }
	}

	return zc; // in mm
    }

    public static void main(String arg[]) {

	Constants.Load();

	Geometry geo = new Geometry();

	int sector = 3;
	int layer = 5;

	for (int nZstrpIdx = 0; nZstrpIdx < 10; nZstrpIdx++) {
	    for (int nCstrpIdx = 0; nCstrpIdx < 1; nCstrpIdx++) {
		double xn = Math.cos(
			geo.CRZ_GetAngleStrip(sector, layer, nZstrpIdx + 1));
		double yn = Math.sin(
			geo.CRZ_GetAngleStrip(sector, layer, nZstrpIdx + 1));

		double z = geo.CRC_GetZStrip(sector, layer, nCstrpIdx + 1); // c
									    // strip

		double sigmaC = Constants.SigmaDrift * Math.sqrt(
			(Constants.CRCRADIUS[2] * Math.sqrt(xn * xn + yn * yn)
				- Constants.CRCRADIUS[2] + Constants.hStrip2Det)
				/ Constants.hDrift);
		double sigmaZ = Constants.SigmaDrift * Math.sqrt(
			(Constants.CRCRADIUS[2] * Math.sqrt(xn * xn + yn * yn)
				- Constants.CRCRADIUS[2] + Constants.hStrip2Det)
				/ Constants.hDrift / Constants.ThetaL);

		double angle = Math.atan2(yn, xn);
		if (angle > 2 * Math.PI)
		    angle -= 2 * Math.PI;

		int calc_strip = geo.getZStrip(layer, angle);

		System.out.println("x " + (Constants.CRCRADIUS[2] * xn) + " y "
			+ (Constants.CRCRADIUS[2] * yn) + " z " + z + " stp "
			+ calc_strip + " sigmaC " + sigmaC + " sigmaZ "
			+ sigmaZ);
	    }

	}

    }
}
