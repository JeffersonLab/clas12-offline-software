package cnuphys.ced.ced3d;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import adapter3D.KeyboardLabel;
import bCNU3D.Panel3D;
import cnuphys.bCNU.graphics.component.CommonBorder;

@SuppressWarnings("serial")
public class KeyboardLegend extends JPanel {

	private static boolean noshift[] = { false, false };
	private static boolean shift[] = { false, true };

	private Panel3D _panel3D;

	/**
	 * Legend on 3D views
	 */
	public KeyboardLegend(Panel3D panel) {
		_panel3D = panel;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add("Rotate +/-" + " (original) x", kArray("x", "X"), vArray(KeyEvent.VK_X, KeyEvent.VK_X), shift);
		add("Move left/right", kArray("L", "R"), vArray(KeyEvent.VK_L, KeyEvent.VK_R), noshift);
		add("Rotate +/-" + " (original) y", kArray("y", "Y"), vArray(KeyEvent.VK_Y, KeyEvent.VK_Y), shift);
		add("Rotate +/-" + " (original) z", kArray("z", "Z"), vArray(KeyEvent.VK_Z, KeyEvent.VK_Z), shift);

		add("Move in/out", kArray("J", "K"), vArray(KeyEvent.VK_J, KeyEvent.VK_K), noshift);
		add("Move up/down", kArray("U", "D"), vArray(KeyEvent.VK_U, KeyEvent.VK_D), noshift);

		add("Make [x,y] out", kArray("1", "2"), vArray(KeyEvent.VK_1, KeyEvent.VK_2), noshift);
		add("Make z axis out/in", kArray("3", "4"), vArray(KeyEvent.VK_3, KeyEvent.VK_4), noshift);

		setBorder(new CommonBorder("Keyboard Actions"));
//		validate();
	}

	private static String[] kArray(String... keys) {
		return keys;
	}

	private static int[] vArray(int... vkeys) {
		return vkeys;
	}

	// KeyboardLabel(String explanation, Font font, Color bg, Color fg, String
	// ...keys)
	private void add(String explantion, String[] keys, int[] vkeys, boolean[] shifted) {
		KeyboardLabel kbl = new KeyboardLabel(_panel3D, explantion, keys, vkeys, shifted);
		kbl.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(kbl);
		add(Box.createVerticalStrut(3));
	}
}
