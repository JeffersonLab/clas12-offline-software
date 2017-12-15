package cnuphys.bCNU.graphics.component;

/*
 * CommonBorder
 * Description: Border style stolen from Hv_Border
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import cnuphys.bCNU.util.Fonts;

@SuppressWarnings("serial")
public class CommonBorder extends TitledBorder {

	public Border etched = BorderFactory.createEtchedBorder();
	public Font font = Fonts.commonFont(Font.PLAIN, 9);

	public CommonBorder() {
		super(BorderFactory.createEtchedBorder());
		setTitleColor(Color.blue);
		setTitleFont(font);
	}

	public CommonBorder(String title) {
		this();
		setTitle(title);
	}
	
	/**
	 * Create a common border with an empty border around it
	 * @param title the title
	 * @param size the size of the empty border in pixels
	 * @return the compound border
	 */
	public static Border withEmptyBorder(String title, int size) {
		Border emptyBorder = BorderFactory
				.createEmptyBorder(4, 4, 4, 4);
		
		CommonBorder cborder = new CommonBorder(title);
        return BorderFactory.createCompoundBorder(emptyBorder, cborder);
	}
}
