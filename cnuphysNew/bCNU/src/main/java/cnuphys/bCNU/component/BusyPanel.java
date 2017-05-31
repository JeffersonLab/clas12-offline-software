package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.util.Fonts;

public class BusyPanel extends JPanel {

	JLabel prompt;
	
	public BusyPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
		setBackground(Color.white);
		
		ImageIcon busyIcon = ImageManager.getInstance().loadImageIcon("images/activity.gif");
		
		if (busyIcon != null) {
			JLabel ilab = new JLabel(busyIcon);
			ilab.setOpaque(true);
			ilab.setBackground(Color.white);
			ilab.setForeground(Color.red);

			add(ilab);
		}
		
		prompt = new JLabel("");
		prompt.setOpaque(true);
		prompt.setBackground(Color.white);
		prompt.setForeground(Color.red);
		prompt.setFont(Fonts.mediumFont);
		add(prompt);
	}

	public void setText(String s) {
		prompt.setText(s);
	}
}
