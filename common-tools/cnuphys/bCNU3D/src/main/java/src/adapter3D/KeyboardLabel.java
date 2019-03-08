package adapter3D;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bCNU3D.Panel3D;

/**
 * This is for making a label to show what a key stroke does, e.g., R for move
 * right
 * 
 * @author heddle
 *
 */
@SuppressWarnings("serial")
public class KeyboardLabel extends JPanel {

	// the font
	private static Font _font = new Font("SansSerif", Font.PLAIN, 10);
	private static final Font _bfont = new Font(Font.MONOSPACED, Font.PLAIN, 10);

	private Panel3D _panel3D;

	public KeyboardLabel(Panel3D panel, String explanation, String keys[], int vkeys[], boolean shifted[]) {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		_panel3D = panel;

		for (int i = 0; i < keys.length; i++) {
			KLButton button = new KLButton(keys[i], vkeys[i], shifted[i]) {

				@Override
				public Dimension getPreferredSize() {
					Dimension d = super.getPreferredSize();
					FontMetrics fm = getFontMetrics(_bfont);
					d.width = fm.stringWidth(" X ");
					d.height = fm.getHeight() + 4;
					return d;
				}

			};

			button.setFont(_bfont);

			add(button);
			add(Box.createHorizontalStrut(6));
		}

		JLabel lab = new JLabel(explanation);
		lab.setFont(_font);

		add(lab);

		add(Box.createHorizontalGlue());
	}

	class KLButton extends JButton {

		KLButton(final String label, final int vk, final boolean shifted) {
			super(label.trim());

			ActionListener al = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					KeyAdapter3D.handleVK(_panel3D, vk, shifted);
				}

			};

			addActionListener(al);
		}

	}
}
