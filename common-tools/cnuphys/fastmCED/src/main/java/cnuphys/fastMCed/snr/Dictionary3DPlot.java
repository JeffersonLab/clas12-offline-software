package cnuphys.fastMCed.snr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bCNU3D.GrowablePointSet;
import bCNU3D.PointSetPanel3D;
import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.fastMCed.frame.FastMCed;
import cnuphys.lund.GeneratedParticleRecord;
import item3D.Axes3D;

public class Dictionary3DPlot extends JDialog implements ActionListener {
	
	//there will be two point sets, points already in the dictionary and 
	//appended points
	
	public static final String MAIN = "InDict";
	public static final String APPEND = "Append";
	
	private GrowablePointSet _dictSet;
	private GrowablePointSet _appendSet;
	
	//the dictionary
	private SNRDictionary _snrDictionary;
	
	//the singleton
	private static Dictionary3DPlot instance;
	
	//the 3D panel
	private PointSetPanel3D p3d;

	/**
	 * Create the dictionary plot
	 * @param parent probably FastMCed
	 */
	private Dictionary3DPlot(JFrame parent) {
		super(parent, "P, " + UnicodeSupport.SMALL_THETA + ", " + UnicodeSupport.SMALL_PHI, false);

		// close is like a close
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);
		setLayout(new BorderLayout(8, 8));
		

		setIconImage(ImageManager.cnuIcon.getImage());
		// add components
		createSouthComponent("Close");
		createCenterComponent();

		pack();

		// center the dialog
		DialogUtilities.centerDialog(this);
	}
	
	//the 3D plot is in the center
	private void createCenterComponent() {
		
		final float pMax = 8f;
		final float thetaMax = 50f;
		final float phiMax = 25f;
		final float xdist = -12f;
		final float ydist = -20f;
		final float zdist = -95f;

		final float angX = 0f;
		final float angY = -5f;
		final float angZ = 0f;

		p3d = new PointSetPanel3D(angX, angY, angZ, xdist, ydist, zdist) {
			@Override
			public void createInitialItems() {
				
				String labels[] = {"P (Gev/c)", " " + UnicodeSupport.SMALL_THETA, "      " + UnicodeSupport.SMALL_PHI};
				Axes3D axes = new Axes3D(this, 0, pMax, 0, thetaMax,
						-phiMax, phiMax, labels, Color.darkGray, 2f, 3, 6, 6, Color.black,
						Color.black, new Font("SansSerif", Font.PLAIN, 20), 0);
				addItem(axes);

			}
			
			/**
			 * This gets the z step used by the mouse and key adapters, to see
			 * how fast we move in or in in response to mouse wheel or up/down
			 * arrows. It should be overridden to give something sensible. like
			 * the scale/100;
			 * 
			 * @return the z step (changes to zDist) for moving in and out
			 */
			@Override
			public float getZStep() {
				return 1f;
			}
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(600, 600);
			}

		};
		
		p3d.setScale(4f, 50f/45f, 0.5f);
		
		ArrayList<GrowablePointSet> pointSets = p3d.getPointSets();
		
		_dictSet = new GrowablePointSet(MAIN, new Color(255, 0, 0, 64), 1.5f, true);
		_appendSet = new GrowablePointSet(APPEND, Color.blue, 4f, true);
		
		pointSets.add(_dictSet);
		pointSets.add(_appendSet);
		
		add(p3d, BorderLayout.CENTER);
	}
	
	/**
	 * Plot the given dictionary
	 * @param dictionary the dictionary
	 */
	public static Dictionary3DPlot plotDictionary(SNRDictionary dictionary) {
		if (instance == null) {
			instance = new Dictionary3DPlot(FastMCed.getFastMCed());
		}
		
		instance.setDictionary(dictionary);
		instance.setVisible(true);
		return instance;
	}
	
	//read the data from the dictionary
	private void setDictionary(SNRDictionary dictionary) {
		_snrDictionary = dictionary;
		
		p3d.clear();
		
		if (_snrDictionary != null) {
			for (String gprHash : _snrDictionary.values()) {
				GeneratedParticleRecord gpr = GeneratedParticleRecord.fromHash(gprHash);
				_dictSet.add(gpr.getMomentum(), gpr.getTheta(), gpr.getPhi());
			}
		}
		
		p3d.refresh();
	}
	
	/**
	 * Append a point to the plot
	 * @param psetName either MAIN or APPEN constant string
	 * @param gprHash the "value" i.e. a GeneratedParticleRecord hash
	 */
	public void append(String psetName, String gprHash) {
		GeneratedParticleRecord gpr = GeneratedParticleRecord.fromHash(gprHash);
		if (APPEND.equals(psetName)) {
			_appendSet.add(gpr.getMomentum(), gpr.getTheta(), gpr.getPhi());
		}
		else if (MAIN.equals(psetName)) {
			_dictSet.add(gpr.getMomentum(), gpr.getTheta(), gpr.getPhi());
		}
		else {
			System.err.println("Unklnown pset name in Dictionary3DPlot.append");
		}
	}
	
	/**
	 * Append a point to the plot in the APPEND set
	 * @param gprHash the "value" i.e. a GeneratedParticleRecord hash
	 */
	public void append(String gprHash) {
		append(APPEND, gprHash);
	}

	
	
	/**
	 * Override to create the component that goes in the south.
	 *
	 * @return the component that is placed in the south. The default
	 *         implementation creates a row of closeout buttons.
	 */
	protected void createSouthComponent(String... closeout) {
		JPanel buttonPanel = new JPanel();
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

	@Override
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
	}
	
}
