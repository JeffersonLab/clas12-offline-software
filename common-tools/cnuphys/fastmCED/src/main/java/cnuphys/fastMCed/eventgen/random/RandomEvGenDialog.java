package cnuphys.fastMCed.eventgen.random;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.fastMCed.eventgen.GeneratorManager;
import cnuphys.fastMCed.eventgen.IEventSource;

/**
 * A dialog for generating random events. The events can have up to four particles
 * 
 * @author heddle
 *
 */
public class RandomEvGenDialog extends JDialog implements ActionListener, IEventSource {

	private static String OKSTR = "OK";
	private static String CANCELSTR = "Cancel";
	
	//electron, proton, gamma
	private static int lundIds[] = {11, 2212, 22, -11};

	// the reason the dialog closed.
	private int reason;

	//random number generator
	private Random _rand;
	
	// convenient access to south button panel
	private JPanel buttonPanel;
	
	//the particle panels
	private ParticlePanel[] ppanels;
	
	//seed and max p perp
	private JTextField _seedTextField;
	private JTextField _pperpTextField;
	private long _defaultSeed = -1;

	/**
	 * Create a random event generator
	 * 
	 * @param parent
	 *            the parent frame
	 * @param maxNum the max number of particles
	 */
	public RandomEvGenDialog(JFrame parent, int maxNum) {
		super(parent, "Random Event Generator", true);

		// close is like a close
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				reason = DialogUtilities.CANCEL_RESPONSE;
				setVisible(false);
			}
		};
		addWindowListener(wa);
		setLayout(new BorderLayout(8, 8));
		

		setIconImage(ImageManager.cnuIcon.getImage());
		// add components
		createSouthComponent(OKSTR, CANCELSTR);
		createCenterComponent(maxNum);
		createNorthComponent();

		pack();

		// center the dialog
		DialogUtilities.centerDialog(this);

	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 4, def.left + 4, def.bottom + 4,
				def.right + 4);
	}


	/**
	 * Get the reason the dialog closed, either DialogUtilities.CANCEL_RESPONSE
	 * or DialogUtilities.OK_RESPONSE
	 * 
	 * @return reason the dialog closed
	 */
	public int getReason() {
		return reason;
	}
	
	protected void createNorthComponent() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		_seedTextField = new JTextField(""+_defaultSeed, 10);
		_pperpTextField = new JTextField(""+GeneratorManager.getPPerpMax(), 6);
		
		panel.add(new JLabel("Seed: "));
		panel.add(_seedTextField);
		panel.add(Box.createHorizontalStrut(30));
		panel.add(new JLabel("<html> Max P&perp; (GeV/C): "));
		panel.add(_pperpTextField);
		
		add(panel, BorderLayout.NORTH);
	}
	
	/**
	 * Get the random number seed
	 * @return  the random number seed
	 */
	public long getSeed() {
		try {
			return Long.parseLong(_seedTextField.getText());
		}
		catch (Exception e) {
			return  _defaultSeed;
		}
	}
	
	/**
	 * Get the max p perp
	 * @return  the max p perp in GeV/c
	 */
	public double getMaxPPerp() {
		try {
			double pperpMax = Double.parseDouble(_pperpTextField.getText());
			GeneratorManager.setPPerpMax(pperpMax);
			return pperpMax;
		}
		catch (Exception e) {
			_pperpTextField.setText("" + GeneratorManager.getPPerpMax());
			return  GeneratorManager.getPPerpMax();
		}
	}


	/**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 *
	 * @return the component that is placed in the center
	 */
	
	protected void createCenterComponent(int maxNum) {
		JPanel panel = new JPanel();
		
		ppanels = new ParticlePanel[maxNum];
		panel.setLayout(new VerticalFlowLayout());
		
		for (int i = 0; i < maxNum; i++) {
			ppanels[i] = new ParticlePanel(this, i == 0, lundIds[i % lundIds.length]);
			panel.add(ppanels[i]);
		}
		add(panel, BorderLayout.CENTER);
	}

	/**
	 * Override to create the component that goes in the south.
	 *
	 * @return the component that is placed in the south. The default
	 *         implementation creates a row of closeout buttons.
	 */
	protected void createSouthComponent(String... closeout) {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());

		int lenm1 = closeout.length - 1;
		for (int index = 0; index <= lenm1; index++) {
			JButton button = new JButton(closeout[index]);
			button.setName("simpleDialog" + closeout[index]);
			button.setActionCommand(closeout[index]);
			button.addActionListener(this);
			buttonPanel.add(button);
			if (index != lenm1) {
				buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			}
		}

		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Get the random number generator
	 * @return the random number generator
	 */
	public Random getRandom() {
		return _rand;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		
		if (e.getActionCommand() == CANCELSTR) {
			reason = DialogUtilities.CANCEL_RESPONSE;
		}
		else { 
			reason = DialogUtilities.OK_RESPONSE;
			long seed = getSeed();
			if (seed < 1) {
				_rand = new Random();
			}
			else {
				_rand = new Random(seed);
			}
		}
		
		setVisible(false);
	}
	@Override
	public PhysicsEvent getEvent() {
		PhysicsEvent  event = new PhysicsEvent();
		for (ParticlePanel panel : ppanels) {
			if (panel.isActive()) {
				Particle p = panel.createParticle();
				event.addParticle(p);
			}
		}
		
		return event;
	}

}
