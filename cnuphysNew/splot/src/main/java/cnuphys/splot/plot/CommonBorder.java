package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class CommonBorder extends TitledBorder {

	public Font font = Environment.getInstance().getCommonFont(9);

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
