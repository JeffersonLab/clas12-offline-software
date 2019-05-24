package cnuphys.lund;

import java.awt.Color;
import java.util.Hashtable;

public class X11Colors {

	// hash of the X11 colors, filled as requested.
	private static Hashtable<String, Color> x11colors = new Hashtable<String, Color>(521);

	/**
	 * Gets an X11 color and sets the alpha
	 * 
	 * @param name  the name of the color
	 * @param alpha [0..255] (255 is opaque)
	 * @return the corresponding x11 color (not from cache)
	 */
	public static Color getX11Color(String name, int alpha) {
		Color color = getX11Color(name);
		if (color != null) {
			int r = color.getRed();
			int g = color.getGreen();
			int b = color.getBlue();
			color = new Color(r, g, b, alpha);
		}
		return color;
	}

	/**
	 * Gets the named color from the X11 color list.
	 * 
	 * @param name the name of the X11 color.
	 * @return the named color from the X11 color list, or <code>null</code>.
	 */
	public static Color getX11Color(String name) {
		if (name == null) {
			return null;
		}
		name = name.toLowerCase();

		Color color = x11colors.get(name);
		if (color == null) {
			if (name.equalsIgnoreCase("Alice Blue")) {
				color = new Color(240, 248, 255);
			} else if (name.equalsIgnoreCase("Antique White")) {
				color = new Color(250, 235, 215);
			} else if (name.equalsIgnoreCase("Aqua")) {
				color = new Color(0, 255, 255);
			} else if (name.equalsIgnoreCase("Aquamarine")) {
				color = new Color(127, 255, 212);
			} else if (name.equalsIgnoreCase("Azure")) {
				color = new Color(240, 255, 255);
			} else if (name.equalsIgnoreCase("Beige")) {
				color = new Color(245, 245, 220);
			} else if (name.equalsIgnoreCase("Bisque")) {
				color = new Color(255, 228, 196);
			} else if (name.equalsIgnoreCase("Black")) {
				color = new Color(0, 0, 0);
			} else if (name.equalsIgnoreCase("Blanched Almond")) {
				color = new Color(255, 235, 205);
			} else if (name.equalsIgnoreCase("Blue")) {
				color = new Color(0, 0, 255);
			} else if (name.equalsIgnoreCase("Blue Violet")) {
				color = new Color(138, 43, 226);
			} else if (name.equalsIgnoreCase("Brown")) {
				color = new Color(165, 42, 42);
			} else if (name.equalsIgnoreCase("BurlyWood")) {
				color = new Color(222, 184, 135);
			} else if (name.equalsIgnoreCase("Cadet Blue")) {
				color = new Color(95, 158, 160);
			} else if (name.equalsIgnoreCase("Chartreuse")) {
				color = new Color(127, 255, 0);
			} else if (name.equalsIgnoreCase("Chocolate")) {
				color = new Color(210, 105, 30);
			} else if (name.equalsIgnoreCase("Coral")) {
				color = new Color(255, 127, 80);
			} else if (name.equalsIgnoreCase("Cornflower")) {
				color = new Color(100, 149, 237);
			} else if (name.equalsIgnoreCase("Cornsilk")) {
				color = new Color(255, 248, 220);
			} else if (name.equalsIgnoreCase("Crimson")) {
				color = new Color(220, 20, 60);
			} else if (name.equalsIgnoreCase("Cyan")) {
				color = new Color(0, 255, 255);
			} else if (name.equalsIgnoreCase("Dark Blue")) {
				color = new Color(0, 0, 139);
			} else if (name.equalsIgnoreCase("Dark Cyan")) {
				color = new Color(0, 139, 139);
			} else if (name.equalsIgnoreCase("Dark Goldenrod")) {
				color = new Color(184, 134, 11);
			} else if (name.equalsIgnoreCase("Dark Gray")) {
				color = new Color(169, 169, 169);
			} else if (name.equalsIgnoreCase("Dark Green")) {
				color = new Color(0, 100, 0);
			} else if (name.equalsIgnoreCase("Dark Khaki")) {
				color = new Color(189, 183, 107);
			} else if (name.equalsIgnoreCase("Dark Magenta")) {
				color = new Color(139, 0, 139);
			} else if (name.equalsIgnoreCase("Dark Olive Green")) {
				color = new Color(85, 107, 47);
			} else if (name.equalsIgnoreCase("Dark Orange")) {
				color = new Color(255, 140, 0);
			} else if (name.equalsIgnoreCase("Dark Orchid")) {
				color = new Color(153, 50, 204);
			} else if (name.equalsIgnoreCase("Dark Red")) {
				color = new Color(139, 0, 0);
			} else if (name.equalsIgnoreCase("Dark Salmon")) {
				color = new Color(233, 150, 122);
			} else if (name.equalsIgnoreCase("Dark Sea Green")) {
				color = new Color(143, 188, 143);
			} else if (name.equalsIgnoreCase("Dark Slate Blue")) {
				color = new Color(72, 61, 139);
			} else if (name.equalsIgnoreCase("Dark Slate Gray")) {
				color = new Color(47, 79, 79);
			} else if (name.equalsIgnoreCase("Dark Turquoise")) {
				color = new Color(0, 206, 209);
			} else if (name.equalsIgnoreCase("Dark Violet")) {
				color = new Color(148, 0, 211);
			} else if (name.equalsIgnoreCase("Deep Pink")) {
				color = new Color(255, 20, 147);
			} else if (name.equalsIgnoreCase("Deep Sky Blue")) {
				color = new Color(0, 191, 255);
			} else if (name.equalsIgnoreCase("Dim Gray")) {
				color = new Color(105, 105, 105);
			} else if (name.equalsIgnoreCase("Dodger Blue")) {
				color = new Color(30, 144, 255);
			} else if (name.equalsIgnoreCase("Firebrick")) {
				color = new Color(178, 34, 34);
			} else if (name.equalsIgnoreCase("Floral White")) {
				color = new Color(255, 250, 240);
			} else if (name.equalsIgnoreCase("Forest Green")) {
				color = new Color(34, 139, 34);
			} else if (name.equalsIgnoreCase("Fuchsia")) {
				color = new Color(255, 0, 255);
			} else if (name.equalsIgnoreCase("Gainsboro")) {
				color = new Color(220, 220, 220);
			} else if (name.equalsIgnoreCase("Ghost White")) {
				color = new Color(248, 248, 255);
			} else if (name.equalsIgnoreCase("Gold")) {
				color = new Color(255, 215, 0);
			} else if (name.equalsIgnoreCase("Goldenrod")) {
				color = new Color(218, 165, 32);
			} else if (name.equalsIgnoreCase("Gray")) {
				color = new Color(127, 127, 127);
			} else if (name.equalsIgnoreCase("Green")) {
				color = new Color(0, 127, 0);
			} else if (name.equalsIgnoreCase("Green Yellow")) {
				color = new Color(173, 255, 47);
			} else if (name.equalsIgnoreCase("Honeydew")) {
				color = new Color(240, 255, 240);
			} else if (name.equalsIgnoreCase("Hot Pink")) {
				color = new Color(255, 105, 180);
			} else if (name.equalsIgnoreCase("Indian Red")) {
				color = new Color(205, 92, 92);
			} else if (name.equalsIgnoreCase("Indigo")) {
				color = new Color(75, 0, 130);
			} else if (name.equalsIgnoreCase("Ivory")) {
				color = new Color(255, 255, 240);
			} else if (name.equalsIgnoreCase("Khaki")) {
				color = new Color(240, 230, 140);
			} else if (name.equalsIgnoreCase("Lavender")) {
				color = new Color(230, 230, 250);
			} else if (name.equalsIgnoreCase("Lavender Blush")) {
				color = new Color(255, 240, 245);
			} else if (name.equalsIgnoreCase("Lawn Green")) {
				color = new Color(124, 252, 0);
			} else if (name.equalsIgnoreCase("Lemon Chiffon")) {
				color = new Color(255, 250, 205);
			} else if (name.equalsIgnoreCase("Light Blue")) {
				color = new Color(173, 216, 230);
			} else if (name.equalsIgnoreCase("Light Coral")) {
				color = new Color(240, 128, 128);
			} else if (name.equalsIgnoreCase("Light Cyan")) {
				color = new Color(224, 255, 255);
			} else if (name.equalsIgnoreCase("Light Goldenrod")) {
				color = new Color(250, 250, 210);
			} else if (name.equalsIgnoreCase("Light Green")) {
				color = new Color(144, 238, 144);
			} else if (name.equalsIgnoreCase("Light Grey")) {
				color = new Color(211, 211, 211);
			} else if (name.equalsIgnoreCase("Light Pink")) {
				color = new Color(255, 182, 193);
			} else if (name.equalsIgnoreCase("Light Salmon")) {
				color = new Color(255, 160, 122);
			} else if (name.equalsIgnoreCase("Light Sea Green")) {
				color = new Color(32, 178, 170);
			} else if (name.equalsIgnoreCase("Light Sky Blue")) {
				color = new Color(135, 206, 250);
			} else if (name.equalsIgnoreCase("Light Slate Gray")) {
				color = new Color(119, 136, 153);
			} else if (name.equalsIgnoreCase("Light Steel Blue")) {
				color = new Color(176, 196, 222);
			} else if (name.equalsIgnoreCase("Light Yellow")) {
				color = new Color(255, 255, 224);
			} else if (name.equalsIgnoreCase("Lime")) {
				color = new Color(0, 255, 0);
			} else if (name.equalsIgnoreCase("Lime Green")) {
				color = new Color(50, 205, 50);
			} else if (name.equalsIgnoreCase("Linen")) {
				color = new Color(250, 240, 230);
			} else if (name.equalsIgnoreCase("Magenta")) {
				color = new Color(255, 0, 255);
			} else if (name.equalsIgnoreCase("Maroon")) {
				color = new Color(127, 0, 0);
			} else if (name.equalsIgnoreCase("Medium Aquamarine")) {
				color = new Color(102, 205, 170);
			} else if (name.equalsIgnoreCase("Medium Blue")) {
				color = new Color(0, 0, 205);
			} else if (name.equalsIgnoreCase("Medium Orchid")) {
				color = new Color(186, 85, 211);
			} else if (name.equalsIgnoreCase("Medium Purple")) {
				color = new Color(147, 112, 219);
			} else if (name.equalsIgnoreCase("Medium Sea Green")) {
				color = new Color(60, 179, 113);
			} else if (name.equalsIgnoreCase("Medium Slate Blue")) {
				color = new Color(123, 104, 238);
			} else if (name.equalsIgnoreCase("Medium Spring Green")) {
				color = new Color(0, 250, 154);
			} else if (name.equalsIgnoreCase("Medium Turquoise")) {
				color = new Color(72, 209, 204);
			} else if (name.equalsIgnoreCase("Medium Violet Red")) {
				color = new Color(199, 21, 133);
			} else if (name.equalsIgnoreCase("Midnight Blue")) {
				color = new Color(25, 25, 112);
			} else if (name.equalsIgnoreCase("Mint Cream")) {
				color = new Color(245, 255, 250);
			} else if (name.equalsIgnoreCase("Misty Rose")) {
				color = new Color(255, 228, 225);
			} else if (name.equalsIgnoreCase("Moccasin")) {
				color = new Color(255, 228, 181);
			} else if (name.equalsIgnoreCase("Navajo White")) {
				color = new Color(255, 222, 173);
			} else if (name.equalsIgnoreCase("Navy")) {
				color = new Color(0, 0, 128);
			} else if (name.equalsIgnoreCase("Old Lace")) {
				color = new Color(253, 245, 230);
			} else if (name.equalsIgnoreCase("Olive")) {
				color = new Color(128, 128, 0);
			} else if (name.equalsIgnoreCase("Olive Drab")) {
				color = new Color(107, 142, 35);
			} else if (name.equalsIgnoreCase("Orange")) {
				color = new Color(255, 165, 0);
			} else if (name.equalsIgnoreCase("Orange Red")) {
				color = new Color(255, 69, 0);
			} else if (name.equalsIgnoreCase("Orchid")) {
				color = new Color(218, 112, 214);
			} else if (name.equalsIgnoreCase("Pale Goldenrod")) {
				color = new Color(238, 232, 170);
			} else if (name.equalsIgnoreCase("Pale Green")) {
				color = new Color(152, 251, 152);
			} else if (name.equalsIgnoreCase("Pale Turquoise")) {
				color = new Color(175, 238, 238);
			} else if (name.equalsIgnoreCase("Pale Violet Red")) {
				color = new Color(219, 112, 147);
			} else if (name.equalsIgnoreCase("Papaya Whip")) {
				color = new Color(255, 239, 213);
			} else if (name.equalsIgnoreCase("Peach Puff")) {
				color = new Color(255, 218, 185);
			} else if (name.equalsIgnoreCase("Peru")) {
				color = new Color(205, 133, 63);
			} else if (name.equalsIgnoreCase("Pink")) {
				color = new Color(255, 192, 203);
			} else if (name.equalsIgnoreCase("Plum")) {
				color = new Color(221, 160, 221);
			} else if (name.equalsIgnoreCase("Powder Blue")) {
				color = new Color(176, 224, 230);
			} else if (name.equalsIgnoreCase("Purple")) {
				color = new Color(127, 0, 127);
			} else if (name.equalsIgnoreCase("Red")) {
				color = new Color(255, 0, 0);
			} else if (name.equalsIgnoreCase("Rosy Brown")) {
				color = new Color(188, 143, 143);
			} else if (name.equalsIgnoreCase("Royal Blue")) {
				color = new Color(65, 105, 225);
			} else if (name.equalsIgnoreCase("Saddle Brown")) {
				color = new Color(139, 69, 19);
			} else if (name.equalsIgnoreCase("Salmon")) {
				color = new Color(250, 128, 114);
			} else if (name.equalsIgnoreCase("Sandy Brown")) {
				color = new Color(244, 164, 96);
			} else if (name.equalsIgnoreCase("Sea Green")) {
				color = new Color(46, 139, 87);
			} else if (name.equalsIgnoreCase("Seashell")) {
				color = new Color(255, 245, 238);
			} else if (name.equalsIgnoreCase("Sienna")) {
				color = new Color(160, 82, 45);
			} else if (name.equalsIgnoreCase("Silver")) {
				color = new Color(192, 192, 192);
			} else if (name.equalsIgnoreCase("Sky Blue")) {
				color = new Color(135, 206, 235);
			} else if (name.equalsIgnoreCase("Slate Blue")) {
				color = new Color(106, 90, 205);
			} else if (name.equalsIgnoreCase("Slate Gray")) {
				color = new Color(112, 128, 144);
			} else if (name.equalsIgnoreCase("Snow")) {
				color = new Color(255, 250, 250);
			} else if (name.equalsIgnoreCase("Spring Green")) {
				color = new Color(0, 255, 127);
			} else if (name.equalsIgnoreCase("Steel Blue")) {
				color = new Color(70, 130, 180);
			} else if (name.equalsIgnoreCase("Tan")) {
				color = new Color(210, 180, 140);
			} else if (name.equalsIgnoreCase("Teal")) {
				color = new Color(0, 128, 128);
			} else if (name.equalsIgnoreCase("Thistle")) {
				color = new Color(216, 191, 216);
			} else if (name.equalsIgnoreCase("Tomato")) {
				color = new Color(255, 99, 71);
			} else if (name.equalsIgnoreCase("Turquoise")) {
				color = new Color(64, 224, 208);
			} else if (name.equalsIgnoreCase("Violet")) {
				color = new Color(238, 130, 238);
			} else if (name.equalsIgnoreCase("Wheat")) {
				color = new Color(245, 222, 179);
			} else if (name.equalsIgnoreCase("White")) {
				color = new Color(255, 255, 255);
			} else if (name.equalsIgnoreCase("White Smoke")) {
				color = new Color(245, 245, 245);
			} else if (name.equalsIgnoreCase("Yellow")) {
				color = new Color(255, 255, 0);
			} else if (name.equalsIgnoreCase("Yellow Green")) {
				color = new Color(154, 205, 50);
			}

			if (color != null) {
				x11colors.put(name, color);
			}
		} // end color == null
		return color;
	}
}