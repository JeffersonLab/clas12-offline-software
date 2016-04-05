package cnuphys.ced.geometry;

import java.awt.Color;
import java.awt.Dimension;
import java.util.EnumMap;

import javax.swing.text.SimpleAttributeSet;

import cnuphys.bCNU.graphics.component.TextPaneScrollPane;
import cnuphys.splot.plot.X11Colors;

public class GeometryReportPane extends TextPaneScrollPane {
	
	private static final int FONTSIZE = 10;

	// reduce from seven levels
	public static enum MCOLOR {
		M_BLACK, M_BLACK2, M_BLUE, M_ORANGERED, M_GREEN, M_RED, M_CORAL
	}

	private static EnumMap<MCOLOR, SimpleAttributeSet> styles;
	
	static {
		styles = new EnumMap<MCOLOR, SimpleAttributeSet>(MCOLOR.class);
		styles.put(
				MCOLOR.M_BLACK,
				createStyle(Color.black, "monospaced", FONTSIZE, false,
						false));
		styles.put(
				MCOLOR.M_BLACK2,
				createStyle(Color.black, new Color(240, 240, 240), "monospaced", FONTSIZE, false,
						false));
		styles.put(
				MCOLOR.M_BLUE,
				createStyle(Color.blue, "sansserif", FONTSIZE+4, false,
						true));
		styles.put(
				MCOLOR.M_ORANGERED,
				createStyle(X11Colors.getX11Color("orange red"), "sansserif", FONTSIZE+3,
						true, true));
		styles.put(
				MCOLOR.M_GREEN,
				createStyle(X11Colors.getX11Color("dark Green"), "serif", FONTSIZE+2,
						true, false));
		styles.put(
				MCOLOR.M_RED,
				createStyle(Color.red, "monospaced", FONTSIZE, false,
						false));
		styles.put(
				MCOLOR.M_CORAL,
				createStyle(X11Colors.getX11Color("coral"), "serif", FONTSIZE+1,
						false, true));
	}
	
	public GeometryReportPane() {
		setPreferredSize(new Dimension(800, 400));
	}
	
	public void print(MCOLOR mcolor, String message) {
		super.append(message, styles.get(mcolor));
	}
	
	public void println(MCOLOR mcolor, String message) {
		super.append(message+"\n", styles.get(mcolor));
	}

}
