package cnuphys.splot.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorPanel extends JPanel implements ItemListener, ChangeListener {

	/**
	 * "No color" checkbox--used to select no color (if enabled)
	 */

	protected JCheckBox nocolorcb;

	/**
	 * Transparency slider
	 */

	protected JSlider transparencySlider;

	/**
	 * Label showing the old color
	 */

	/**
	 * Label showing the new color
	 */

	protected JLabel oldcolorlabel;

	protected JLabel newcolorlabel;

	protected static int minw = 440;

	protected static int minh = 340;

	protected JPanel previewPanel;

	protected JColorChooser colorChooser;

	public ColorPanel() {
		setLayout(new BorderLayout());
		colorChooser = new JColorChooser();
		colorChooser.getSelectionModel().addChangeListener(this);

		add("Center", colorChooser);

		previewPanel = createPreviewPanel();
		colorChooser.setPreviewPanel(previewPanel);
	}

	@SuppressWarnings("unchecked")
	public JPanel createPreviewPanel() {

		JPanel previewPanel = new JPanel() {

			/**
			 * Override insets for nicer appearance
			 */
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2, def.right + 2);
			}

			/**
			 * Override getMinimumSize for nicer appearance
			 * 
			 * @return Minimum size for panel
			 */
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(minw - 20, 80);
			}

			/**
			 * Override getPreferredSize for nicer appearance
			 * 
			 * @return Preferred size for panel
			 */
			@Override
			public Dimension getPreferredSize() {
				return getMinimumSize();
			}
		};

		previewPanel.setLayout(new BorderLayout(20, 4));

		nocolorcb = new JCheckBox("No Color");
		nocolorcb.addItemListener(this);
		// nocolorcb.setEnabled(nocolor);

		previewPanel.add("West", nocolorcb);

		JPanel sp = new JPanel();
		sp.setLayout(new GridLayout(2, 2, 10, 2));

		newcolorlabel = new JLabel("   ");
		oldcolorlabel = new JLabel("   ");

		sp.add(new JLabel("New Color:"));
		sp.add(newcolorlabel);
		sp.add(new JLabel("Old Color:"));
		sp.add(oldcolorlabel);

		// make the transparency slider

		JPanel tpanel = new JPanel(new BorderLayout(2, 0));
		// tpanel.add("North", new JLabel("Transparency"));

		transparencySlider = new JSlider(0, 100, 100);
		transparencySlider.setMajorTickSpacing(25);
		transparencySlider.setPaintTicks(true);

		Hashtable ht = transparencySlider.createStandardLabels(25);
		transparencySlider.setLabelTable(ht);
		transparencySlider.setPaintLabels(true);

		tpanel.add("East", new JLabel("Opaque"));
		tpanel.add("West", new JLabel("Transparent"));
		tpanel.add("Center", transparencySlider);

		previewPanel.add("South", tpanel);

		newcolorlabel.setOpaque(true);
		oldcolorlabel.setOpaque(true);

		previewPanel.add("East", sp);
		return previewPanel;
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(minw, minh);
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 6, def.left + 6, def.bottom + 6, def.right + 6);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (nocolorcb != null) {
			colorChooser.setEnabled(!noColor());
		}
	}

	/**
	 * Check whether "no color" was selected
	 * 
	 * @return true if no color was selected
	 */

	public boolean noColor() {
		return nocolorcb.isSelected();
	}

	/**
	 * Set whether user is allowed to select "no color"
	 * 
	 * @param anc
	 */

	public void enableNoColor(boolean anc) {
		nocolorcb.setEnabled(anc);
	}

	/**
	 * Set whether user is allowed to set gtransparency level
	 * 
	 * @param anc If true, use can select transparency
	 */

	public void enableTransparency(boolean anc) {
		transparencySlider.setEnabled(anc);
	}

	public void setColor(Color c) {
		colorChooser.setColor(c);
		oldcolorlabel.setBackground(c);

		if (c != null) {
			int alpha = c.getAlpha();
			alpha = (int) (alpha / 2.5);
			alpha = Math.max(0, Math.min(100, alpha));
			transparencySlider.setValue(alpha);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setNoColor(false);
		Color newColor = colorChooser.getColor();
		newcolorlabel.setBackground(newColor);
	}

	public boolean isNoColorSelected() {
		return nocolorcb.isSelected();
	}

	public void setNoColor(boolean nocol) {
		nocolorcb.setSelected(nocol);
		colorChooser.setEnabled(!nocol);
	}

	/**
	 * Get the selected color, modified for transparency if necessary
	 * 
	 * @return the color
	 */

	public Color getColor() {
		if (nocolorcb.isEnabled() && nocolorcb.isSelected()) {
			return null;
		}

		Color bcolor = colorChooser.getColor();
		if (transparencySlider.isEnabled()) {
			int alpha = (int) (2.5 * (transparencySlider.getValue()));
			alpha = Math.max(0, Math.min(255, alpha));
			return new Color(bcolor.getRed(), bcolor.getGreen(), bcolor.getBlue(), alpha);

		}
		else {
			return bcolor;
		}
	}

}