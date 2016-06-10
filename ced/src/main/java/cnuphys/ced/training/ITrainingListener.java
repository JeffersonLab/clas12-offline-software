package cnuphys.ced.training;

import java.util.EventListener;

public interface ITrainingListener extends EventListener {

	/**
	 * Data for training
	 * @param data detector data
	 * @param p the momentum in GeV/c
	 * @param theta the polar angle in degrees
	 * @param phi the azimuthal angle in degrees
	 */
	public void train(long data[], double p, double theta, double phi);
}
