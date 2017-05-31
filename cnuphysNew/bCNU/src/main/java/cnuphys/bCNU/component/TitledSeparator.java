package cnuphys.bCNU.component;

import javax.swing.*;
import java.awt.*;

/**
 * This custom title panel creates a separators on the left and right side of
 * the given title string. The left separator has a fixed width of 8 pixels, the
 * right separator will fill the remainder of the panel.
 * 
 * For example ( | --- aTitle
 * ---------------------------------------------------------- |
 */
@SuppressWarnings("serial")
class TitledSeparator extends JPanel {
	public TitledSeparator(String aTitle) {
		super(new GridBagLayout());

		JLabel label = new JLabel(aTitle) {
			@Override
			public void updateUI() {
				super.updateUI();
				setForeground(UIManager.getColor("TitledBorder.titleColor"));
				setFont(UIManager.getFont("TitledBorder.font").deriveFont(
						Font.BOLD));
			}
		};

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0.60;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 0, 0);
		this.add(Box.createVerticalGlue(), c);

		c.gridy++;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 0, 4);
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(8, (int) separator
				.getPreferredSize().getHeight()));
		this.add(separator, c);

		c.gridy++;
		c.weighty = 0.40;
		this.add(Box.createVerticalGlue(), c);

		c.gridx++;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 0, 0);
		this.add(label, c);

		c.gridx++;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0.60;
		c.gridheight = 1;
		c.insets = new Insets(0, 0, 0, 0);
		this.add(Box.createVerticalGlue(), c);

		c.gridy++;
		c.weighty = 0;
		c.insets = new Insets(0, 4, 0, 0);
		this.add(new JSeparator(), c);

		c.gridy++;
		c.weighty = 0.40;
		this.add(Box.createVerticalGlue(), c);
	}
}
