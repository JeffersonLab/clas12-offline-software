package cnuphys.ced.ced3d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

import com.jogamp.opengl.awt.GLJPanel;

import cnuphys.bCNU.component.checkboxarray.CheckBoxArray;
import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PrintUtilities;
import cnuphys.ced.component.PIDLegend;
import bCNU3D.Panel3D;

public class CedPanel3D extends Panel3D {

	// labels for the check box
	public static final String SHOW_VOLUMES = "Volumes";
	public static final String SHOW_TRUTH = "GEMC Truth";
	public static final String SHOW_DC = "DC";
	public static final String SHOW_EC = "EC";
	public static final String SHOW_PCAL = "PCAL";
	public static final String SHOW_FTOF = "FTOF";
	public static final String SHOW_GEMC_DOCA = "GEMC DOCA";
	public static final String SHOW_TB_DOCA = "TB DOCA";
	public static final String SHOW_RECON_FTOF = "Rec-FTOF";
	public static final String SHOW_RECON_CROSSES = "Crosses";
	public static final String SHOW_COSMICS = "Cosmics";
	public static final String SHOW_SVT = "SVT";
	public static final String SHOW_SVT_LAYER_1 = "SVT Layer 1";
	public static final String SHOW_SVT_LAYER_2 = "SVT Layer 2";
	public static final String SHOW_SVT_LAYER_3 = "SVT Layer 3";
	public static final String SHOW_SVT_LAYER_4 = "SVT Layer 4";
	public static final String SHOW_SVT_LAYER_5 = "SVT Layer 5";
	public static final String SHOW_SVT_LAYER_6 = "SVT Layer 6";
	public static final String SHOW_SVT_LAYER_7 = "SVT Layer 7";
	public static final String SHOW_SVT_LAYER_8 = "SVT Layer 8";
	public static final String SHOW_SVT_HITS = "SVT Hits";

	public static final String SHOW_SECTOR_1 = "Sector 1";
	public static final String SHOW_SECTOR_2 = "Sector 2";
	public static final String SHOW_SECTOR_3 = "Sector 3";
	public static final String SHOW_SECTOR_4 = "Sector 4";
	public static final String SHOW_SECTOR_5 = "Sector 5";
	public static final String SHOW_SECTOR_6 = "Sector 6";

	public static final String SHOW_CND = "CND";
	public static final String SHOW_CND_LAYER_1 = "CND Layer 1";
	public static final String SHOW_CND_LAYER_2 = "CND Layer 2";
	public static final String SHOW_CND_LAYER_3 = "CND Layer 3";

	// alpha value for volumes
	protected int volumeAlpha = 28;

	// Check box array
	protected CheckBoxArray _checkBoxArray;

	// show what particles are present
	private PIDLegend _pidLegend;

	// alpha slider for volume alphas
	private AlphaSlider _volumeAlphaSlider;

	// display array labels
	private String _cbaLabels[];

	/*
	 * The panel that holds the 3D objects
	 * 
	 * @param angleX the initial x rotation angle in degrees
	 * 
	 * @param angleY the initial y rotation angle in degrees
	 * 
	 * @param angleZ the initial z rotation angle in degrees
	 * 
	 * @param xdist move viewpoint left/right
	 * 
	 * @param ydist move viewpoint up/down
	 * 
	 * @param zdist the initial viewer z distance should be negative
	 */
	public CedPanel3D(float angleX, float angleY, float angleZ, float xDist,
			float yDist, float zDist, String... cbaLabels) {
		super(angleX, angleY, angleZ, xDist, yDist, zDist);

		_cbaLabels = cbaLabels;

		gljpanel.setBorder(new CommonBorder());
		final GLJPanel gljp = gljpanel;

		addEast();
		addNorth();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
				gljp.requestFocus();
			}

		};
		for (String s : _cbaLabels) {
			AbstractButton ab = _checkBoxArray.getButton(s);
			ab.setFont(Fonts.smallFont);

			if (!SHOW_GEMC_DOCA.equals(s)) {
				ab.setSelected(true);
			}
			ab.addActionListener(al);
		}
		fixSize();
	}

	// add north panel
	private void addNorth() {
		JPanel np = new JPanel();
		np.setLayout(new BorderLayout(20, 0));

		_pidLegend = new PIDLegend(this);
		_volumeAlphaSlider = new AlphaSlider(this, "Volume alpha");
		np.add(_volumeAlphaSlider, BorderLayout.WEST);
		np.add(_pidLegend, BorderLayout.CENTER);
		add(np, BorderLayout.NORTH);
	}

	// add eastern panel
	private void addEast() {
		JPanel ep = new JPanel();
		ep.setLayout(new VerticalFlowLayout());

//		ep.add(new KeyboardLegend());
		_checkBoxArray = new CheckBoxArray(2, 4, 4, _cbaLabels);
		_checkBoxArray.setBorder(new CommonBorder());
		ep.add(_checkBoxArray);

		add(ep, BorderLayout.EAST);
	}

	@Override
	public void refresh() {
		super.refresh();
		_pidLegend.repaint();
	}

	/**
	 * Print the panel.
	 */
	@Override
	public void print() {
		PrintUtilities.printComponent(this);
	}

	/**
	 * Snapshot of the panel.
	 */
	@Override
	public void snapshot() {
		GraphicsUtilities.saveAsPng(this);
	}

	// a fixed fraction of the screen
	private void fixSize() {
		Dimension d = GraphicsUtilities.screenFraction(0.60);
		d.width = d.height;
		gljpanel.setPreferredSize(d);
	}

	/**
	 * Check if a feature should be drawn
	 * 
	 * @param label
	 *            the label for the check box on the option array
	 * @return <code>true</code> if the feature should be drawn
	 */
	public boolean show(String label) {
		AbstractButton ab = _checkBoxArray.getButton(label);
		return (ab == null) ? false : ab.isSelected();
	}

	/**
	 * Get the alpha for volume drawing
	 * 
	 * @return the alpha for volume drawing
	 */
	public int getVolumeAlpha() {
		return _volumeAlphaSlider.getAlpha();
	}

	/**
	 * Get one of the display buttons
	 * 
	 * @param label
	 *            the button label
	 * @return the button or null on failure
	 */
	public AbstractButton getDisplayButton(String label) {
		if (_checkBoxArray == null) {
			return null;
		}
		return _checkBoxArray.getButton(label);
	}

	/**
	 * Get all the display buttons in an array
	 * 
	 * @return all the display buttons
	 */
	public AbstractButton[] gatAllDisplayButtons() {
		if ((_checkBoxArray == null) || (_cbaLabels == null)) {
			return null;
		}

		if (_cbaLabels.length < 1) {
			return null;
		}

		AbstractButton buttons[] = new AbstractButton[_cbaLabels.length];

		for (int i = 0; i < _cbaLabels.length; i++) {
			buttons[i] = _checkBoxArray.getButton(_cbaLabels[i]);
		}

		return buttons;
	}

}
