package cnuphys.fastMCed.consumers;

import java.io.File;

import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.bCNU.util.Environment;
import cnuphys.fastMCed.eventio.PhysicsEventManager;
import cnuphys.fastMCed.snr.SNRDictionary;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;

public abstract class ASNRConsumer extends PhysicsEventConsumer {

	protected String errStr = "???";

	// the dictionaries
	protected SNRDictionary _outDictionary;
	protected SNRDictionary _inDictionary;

	protected SNRManager snr = SNRManager.getInstance();

	@Override
	public String flagExplanation() {
		return errStr;
	}


	/**
	 * Load a dictionary (looking in a dictionaries folder in the home dir,
	 * clearly that is only temporary). If not found, create an empty dictionary
	 * @param bendDirection either IN_BENDER or OIUT_BENDER constant from SNRDictionary class.
	 */
	protected void loadOrCreateDictionary(int bendDirection) {
		double torusScale = 0;
		double solenoidScale = 0;
		boolean useTorus = MagneticFields.getInstance().hasActiveTorus();
		boolean useSolenoid = MagneticFields.getInstance().hasActiveSolenoid();
		if (useTorus) {
			Torus torus = MagneticFields.getInstance().getTorus();
			torusScale = (torus == null) ? 0 : torus.getScaleFactor();
		}
		if (useSolenoid) {
			Solenoid solenoid = MagneticFields.getInstance().getSolenoid();
			solenoidScale = (solenoid == null) ? 0 : solenoid.getScaleFactor();
		}
		String fileName = SNRDictionary.getFileName(bendDirection, useTorus, torusScale, useSolenoid, solenoidScale);

		String dirPath = Environment.getInstance().getHomeDirectory() + "/dictionaries";
		
		
		SNRDictionary dictionary = null;

		File file = new File(dirPath, fileName);
		System.err.println("Dictionary file: [" + file.getPath() + "]");
		if (file.exists()) {
			System.err.println("Found dictionary file");
			dictionary = SNRDictionary.read(dirPath, fileName);

			System.err.println("Number of keys: " + dictionary.size());
		}

		if (dictionary == null) {
			dictionary = new SNRDictionary(bendDirection, useTorus, torusScale, useSolenoid, solenoidScale);
		}
		
		if (bendDirection == SNRDictionary.OUT_BENDER) {
			_outDictionary = dictionary;
		}
		else {
			_inDictionary = dictionary;
		}
	}
	
	//get the truth information
	//first entry with matching charge
	protected TrajectoryRowData getTruth(int charge) {
		ISwimAll allSwimmer = PhysicsEventManager.getInstance().getAllSwimmer();
		
		boolean found = false;
		
		for (TrajectoryRowData trajData : allSwimmer.getRowData()) {
			if (trajData.getCharge() == charge) {
				return trajData;
			}
		}
		return null;
	}

}
