package cnuphys.fastMCed.eventio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.DetectorId;
import org.jlab.geom.prim.Point3D;

import cnuphys.bCNU.component.LabeledTextField;
import cnuphys.fastMCed.fastmc.HitHolder;
import cnuphys.fastMCed.fastmc.ParticleHits;

public class RandomNoiseGenerator implements ActionListener {

	// singleton
	private static RandomNoiseGenerator _instance;

	// do we generate noise?
	private JCheckBoxMenuItem _generateNoiseCB;

	// set the parameters
	private LabeledTextField _dcRandomOcc;

	// for dc random hit list
	private UniqueRandomNumbers dcUniqueNums = new UniqueRandomNumbers(0, 111);

	private static Point3D zeroPoint = new Point3D();

	// private constructor for singleton
	private RandomNoiseGenerator() {
	}

	/**
	 * Accessor for the RandomNoiseGenerator singleton
	 * 
	 * @return the RandomNoiseGenerator singleton
	 */
	public static RandomNoiseGenerator getInstance() {
		if (_instance == null) {
			_instance = new RandomNoiseGenerator();
		}

		return _instance;
	}

	/**
	 * Add the noise generation items to a menu
	 * 
	 * @param menu the parent menu
	 */
	public void addToMenu(JMenu menu) {
		_generateNoiseCB = new JCheckBoxMenuItem("Generate Random Noise", false);

		_dcRandomOcc = new LabeledTextField("Random DC Occupancy %", 8);
		_dcRandomOcc.setText("3.0");

		menu.add(_generateNoiseCB);
		menu.add(_dcRandomOcc);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
	}

	// set the noise generation parameters
	private void setParameters() {

	}

	/**
	 * Check whether we are to generate noise
	 * 
	 * @return <Code>true</code> if we are to generate noise
	 */
	public boolean isGenerateNoise() {
		return _generateNoiseCB.isSelected();
	}

	public void generateNoise(Vector<ParticleHits> hits) {
		ParticleHits randomHits = new ParticleHits();

		// for now 3% dc noise

		HitHolder dcHits = randomHits.getHitHolder(DetectorId.DC);

		// get the random occupance level
		double randOcc = 0;

		try {
			randOcc = Double.parseDouble(_dcRandomOcc.getText()) / 100.;
		} catch (Exception e) {
			randOcc = 0;
		}

		if (randOcc < 1.0e-5) {
			return;
		}

		for (int sect = 0; sect < 6; sect++) {
			for (int supl = 0; supl < 6; supl++) {
				for (int lay = 0; lay < 6; lay++) {
					int numHits = (int) (randOcc * 112);

					ArrayList<Integer> dcList = dcUniqueNums.getRandomList();

					for (int hitId = 0; hitId < numHits; hitId++) {
						int wire = dcList.get(hitId);

						if (hasHit(hits, DetectorId.DC, sect, supl, lay, wire)) {

							// System.err.println("Skip adding random hit on real hit");
						} else {
							DetectorHit dHit = new DetectorHit(DetectorId.DC, sect, supl, lay, wire, zeroPoint);
							dcHits.add(dHit);
						}
					}
				}
			}
		}

		hits.addElement(randomHits);
	}

	// do not add a random hit on top of a real hit
	private boolean hasHit(Vector<ParticleHits> hits, DetectorId id, int sect0, int supl0, int layer0, int comp0) {
		if (hits != null) {
			for (ParticleHits ph : hits) {
				if (ph.hasHit(id, sect0, supl0, layer0, comp0)) {
					return true;
				}
			}
		}

		return false;
	}

}
