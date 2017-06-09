package cnuphys.tinyMS.graphics;

/*
 * CommonBorder
 * Description: Border style stolen from Hv_Border
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

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
}
