package cnuphys.ced.training;

import java.util.EventListener;

public interface ITrainingListener extends EventListener {

	/**
	 * Data for training
	 * @param data detector data for a specific sector
	 * @param p the momentum in GeV/c
	 * @param theta the polar angle in degrees
	 * @param phi the azimuthal angle in degrees
	 */
	public void train(TrainingData data, double p, double theta, double phi);
}
