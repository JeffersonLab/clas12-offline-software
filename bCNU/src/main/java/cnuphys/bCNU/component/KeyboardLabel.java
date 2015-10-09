package cnuphys.bCNU.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * This is for making a label to show what a key stroke does, e.g., R for move
 * right
 * 
 * @author heddle
 *
 */
public class KeyboardLabel extends JComponent {

	// the "what is does" string
	private String _what;

	// the font
	private Font _font;

	// keys, actually
	private String _keys[];

	private Dimension prefSize = new Dimension();

	public KeyboardLabel(String explanation, Font font, Color fg,
			String... keys) {
		setForeground(fg);
		setFont(_font);
		_font = font;
		_keys = keys;

		FontMetrics fm = getFontMetrics(_font);
		prefSize.height = fm.getHeight() + 10;
		prefSize.width = 12;

		if (explanation != null) {
			_what = new String(explanation);
			prefSize.width += (6 + fm.stringWidth(explanation));
		}

		if (_keys != null) {
			for (String key : _keys) {
				int sw = fm.stringWidth(key);
				int bw = sw + 8;
				prefSize.width += (bw + 10);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return prefSize;
	}

	@Override
	public void paintComponent(Graphics g) {

		int x = 6;
		x = drawKeys(g, x);

		g.setColor(getForeground());
		FontMetrics fm = getFontMetrics(_font);
		g.setFont(_font);

		if (_what != null) {
			g.drawString(_what, x, (prefSize.height + fm.getAscent()) / 2 - 3);
		}
	}

	private int drawKeys(Graphics g, int x) {
		g.setFont(_font);
		FontMetrics fm = getFontMetrics(_font);

		if (_keys != null) {
			for (String key : _keys) {

				int sw = fm.stringWidth(key);
				int bw = sw + 6;
				int bh = fm.getHeight() + 2;
				bw = Math.max(bw, bh);

				// GraphicsUtilities.drawSimple3DRect(g, x, 2, bw, bh, true);

				g.setColor(Color.white);
				g.fillRoundRect(x, 2, bw, bh, 3, 3);
				g.setColor(Color.black);
				g.drawRoundRect(x, 2, bw, bh, 3, 3);
				g.drawString(key, x + (bw - sw) / 2, bh - 1);

				x += (bw + 4);
			}
		}

		return x;

	}
}
