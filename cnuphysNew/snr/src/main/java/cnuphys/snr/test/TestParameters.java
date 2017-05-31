package cnuphys.snr.test;

import java.awt.Color;

public class TestParameters {

    /** used to "show clead data */
    public static boolean noiseOff;

    /**
     * Color for generic hits--i.e. raw data.
     */
    private static Color genericHitColor = Color.blue;

    /**
     * Color for generated noise hits. Used when "reality" is displayed.
     */
    private static Color realityNoiseColor = Color.green.darker();

    /**
     * Color for generated track hits. Used when "reality" is displayed.
     */
    private static Color realityTrackColor = Color.red;

    /**
     * Color for analyzed noise hits. Used when "analyzed" is displayed. These
     * will be the hits identified as noise.
     */
    private static Color analyzedNoiseColor = Color.black;

    /**
     * Color for analyzed track hits. Used when "analyzed" is displayed. These
     * will be the hits not identified as noise.
     */
    private static Color analyzedTrackColor = Color.red;

    /**
     * These are for noise that are associated with tracks--i.e., they are
     * saved.
     */
    private static Color savedNoiseColor = Color.cyan;

    /**
     * Used for track color
     */
    private static Color trackColor = Color.black;

    /**
     * How many events in the next accumulation
     */
    private static int accumulationIncrement = 10000;

    /**
     * The maximum tilt angle of a track (degrees).
     */
    private static double thetaMax = 20.0;

    /**
     * Probability of tracks one, two, etc. The numbers should descend
     * reflecting decreasing probability of additional tracks.
     */
    private static double probTracks[] = { 0.98, 0.40, 0.02 };

    /**
     * Rate for random, uncorrelated noise
     */
    private static double noiseRate = 0.03;

    /** Probability that a given wire, hit by a track, does not fire */
    private static double probBadWire = 0.05;

    /** Probability of a blob in a superlayer (chamber) */
    private static double probBlob = 0.2;

    /** size of a blob */
    private static int blobSize = 2;

    /**
     * Get the maximum track tilt angle in degrees.
     * 
     * @return the maximum track tilt angle in degrees.
     */
    public static double getThetaMax() {
	return thetaMax;
    }

    public static String paramString() {
	String s1 = String.format("Noise: %-3.1f%%  ", noiseRate * 100);
	String s2 = String.format("TrackProb: [%-4.2f, %-4.2f, %-4.2f]  ",
		probTracks[0], probTracks[1], probTracks[2]);
	String s3 = String
		.format("DeadWireProb: %-3.1f%%  ", probBadWire * 100);
	String s4 = String.format("BlobProb: %-4.2f  ", probBlob * 100);
	// String s5 = String.format("MaxMissLay: %-4d  ",
	// numMissingLayersAllowed);

	return s1 + s2 + s3 + s4;
    }

    /**
     * Set the maximum track tilt angle.
     * 
     * @param thetaMax
     *            the maximum track tilt angle in degrees.
     */
    public static void setThetaMax(double thetaMax) {
	TestParameters.thetaMax = Math.toRadians(thetaMax);
    }

    /**
     * Get the probability of increasing numbers of tracks.
     * 
     * @return an array that should be probabilities in descending order.
     */
    public static double[] getProbTracks() {
	return probTracks;
    }

    /**
     * Set the probability of increasing numbers of tracks.
     * 
     * @param probTracks
     *            an array that should be probabilities in descending order.
     */
    public static void setProbTracks(double[] probTracks) {
	TestParameters.probTracks = probTracks;
    }

    public static Color getGenericHitColor() {
	return genericHitColor;
    }

    public static void setGenericHitColor(Color genericHitColor) {
	TestParameters.genericHitColor = genericHitColor;
    }

    public static Color getRealityNoiseColor() {
	return realityNoiseColor;
    }

    public static Color getSavedNoiseColor() {
	return savedNoiseColor;
    }

    public static void setRealityNoiseColor(Color realityNoiseColor) {
	TestParameters.realityNoiseColor = realityNoiseColor;
    }

    public static Color getRealityTrackColor() {
	return realityTrackColor;
    }

    public static void setRealityTrackColor(Color realityTrackColor) {
	TestParameters.realityTrackColor = realityTrackColor;
    }

    public static Color getAnalyzedNoiseColor() {
	return analyzedNoiseColor;
    }

    public static void setAnalyzedNoiseColor(Color analyzedNoiseColor) {
	TestParameters.analyzedNoiseColor = analyzedNoiseColor;
    }

    public static Color getAnalyzedTrackColor() {
	return analyzedTrackColor;
    }

    public static void setAnalyzedTrackColor(Color analyzedTrackColor) {
	TestParameters.analyzedTrackColor = analyzedTrackColor;
    }

    public static Color getTrackColor() {
	return trackColor;
    }

    public static void setTrackColor(Color trackColor) {
	TestParameters.trackColor = trackColor;
    }

    public static double getNoiseRate() {
	return noiseRate;
    }

    public static double getProbBlob() {
	return probBlob;
    }

    public static int getBlobSize() {
	return blobSize;
    }

    public static void setNoiseRate(double noiseRate) {
	TestParameters.noiseRate = noiseRate;
    }

    public static double getProbBadWire() {
	return probBadWire;
    }

    public static void setProbBadWire(double probBadWire) {
	TestParameters.probBadWire = probBadWire;
    }

    public static int getAccumulationIncrement() {
	return accumulationIncrement;
    }

    public static void setAccumulationIncrement(int accumulationIncrement) {
	TestParameters.accumulationIncrement = accumulationIncrement;
    }

    // public static int getNumWire() {
    // return numWire;
    // }
    //
    // public static void setNumWire(int numWire) {
    // TestParameters.numWire = numWire;
    // }
    //
    // public static int getNumLayer() {
    // return numLayer;
    // }
    //
    // public static void setNumLayer(int numLayer) {
    // TestParameters.numLayer = numLayer;
    // }

}
