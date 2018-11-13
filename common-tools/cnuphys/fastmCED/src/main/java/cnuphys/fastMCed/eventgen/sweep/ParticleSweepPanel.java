package cnuphys.fastMCed.eventgen.sweep;


import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jlab.clas.physics.Particle;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.fastMCed.eventgen.GeneratorManager;
import cnuphys.lund.LundComboBox;

public class ParticleSweepPanel extends JPanel implements ItemListener {

	// chose a particle
	private LundComboBox _lundComboBox;
	
	//vertex
	private VariableSweepPanel _xoPanel;
	private VariableSweepPanel _yoPanel;
	private VariableSweepPanel _zoPanel;
	
	//momentum
	private VariableSweepPanel _pPanel;
	private VariableSweepPanel _thetaPanel;
	private VariableSweepPanel _phiPanel;
	
	//owner
	private SweepEvGenDialog _dialog;

	public ParticleSweepPanel(final SweepEvGenDialog dialog, boolean use, int lundIntId) {
		_dialog = dialog;
		
		setLayout(new BorderLayout(20, 4));

		add(addWestPanel(use, lundIntId), BorderLayout.WEST);
		add(addCenterPanel(), BorderLayout.CENTER);
		add(addEastPanel(), BorderLayout.EAST);
		setBorder(BorderFactory.createEtchedBorder());
		
		fixState();
	}

	public JPanel addWestPanel(boolean use, int lundIntId) {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());
		_lundComboBox = new LundComboBox(false, 950.0, lundIntId);
		
		panel.add(_lundComboBox);
		return panel;
	}

	public JPanel addCenterPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());
		
		_xoPanel = new VariableSweepPanel(_dialog, "Xo", 0, 0, 0, "cm");
		_yoPanel = new VariableSweepPanel(_dialog, "Yo", 0, 0, 0, "cm");
		_zoPanel = new VariableSweepPanel(_dialog, "Zo", 0, 0, 0, "cm");

		panel.add(_xoPanel);
		panel.add(_yoPanel);
		panel.add(_zoPanel);

		return panel;
	}
	
	/**
	 * Get the steps for each variable and the total
	 * @param steps will hold steps in the order [x, y, z, p, theta, phi]
	 * @return the total number of steps
	 */
	public long getSteps(int steps[]) {
		steps[0] = _xoPanel.numSteps();
		steps[1] = _yoPanel.numSteps();
		steps[2] = _zoPanel.numSteps();
		steps[3] = _pPanel.numSteps();
		steps[4] = _thetaPanel.numSteps();
		steps[5] = _phiPanel.numSteps();
		
		//get the sum
		long sum = 1;
		for (int i : steps) {
			sum *= i;
		}
		return sum;
	}
	

	public JPanel addEastPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());
		_pPanel = new VariableSweepPanel(_dialog, "P", GeneratorManager.getPMin(), GeneratorManager.getPMax(), 0.1,
				"GeV/c");
		_thetaPanel = new VariableSweepPanel(_dialog, UnicodeSupport.SMALL_THETA, GeneratorManager.getThetaMin(),
				GeneratorManager.getThetaMax(), 0.25, "deg");
		_phiPanel = new VariableSweepPanel(_dialog, UnicodeSupport.SMALL_PHI, GeneratorManager.getPhiMin(),
				GeneratorManager.getPhiMax(), 0.25, "deg");

		panel.add(_pPanel);
		panel.add(_thetaPanel);
		panel.add(_phiPanel);

		return panel;
	}
	
	//fix selectability
	private void fixState() {
		boolean active = true;
		_lundComboBox.setEnabled(active);
		_xoPanel.setEnabled(active);
		_yoPanel.setEnabled(active);
		_zoPanel.setEnabled(active);
		_pPanel.setEnabled(active);
		_thetaPanel.setEnabled(active);
		_phiPanel.setEnabled(active);
	}
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		fixState();
	}
	
	/**
	 * Create a particle to add to an event
	 * @return a particle to add to an event
	 */
	public Particle  createParticle(int xstep, int ystep, int zstep,
			int pstep, int thetastep, int phistep) {
		int pid = _lundComboBox.getSelectedId().getId();
		double p = _pPanel.getValue(pstep);
		double theta = Math.toRadians(_thetaPanel.getValue(thetastep));
		double phi = Math.toRadians(_phiPanel.getValue(phistep));
		double pperp = p*Math.sin(theta);
		double px = pperp*Math.cos(phi);
		double py = pperp*Math.sin(phi);
		double pz = p*Math.cos(theta);
		double vx = _xoPanel.getValue(zstep);
		double vy = _yoPanel.getValue(ystep);
		double vz = _zoPanel.getValue(zstep);
		Particle part = new Particle(pid, px, py, pz, vx, vy, vz);
		
		return part;
	}
	
	public double getMomentum(int pstep) {
		return _pPanel.getValue(pstep);
	}
	
	public double getTheta(int thetastep) {
		return _thetaPanel.getValue(thetastep);
	}
	
}