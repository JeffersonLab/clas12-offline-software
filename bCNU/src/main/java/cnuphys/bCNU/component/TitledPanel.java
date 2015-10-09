package cnuphys.bCNU.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class TitledPanel extends JPanel {

	public static final int NORTH = 1;
	public static final int SOUTH = 2;
	public static final int EAST = 4;
	public static final int WEST = 8;

	private TitledPanel(String aTitle, Component aContent, int aBorderFlag,
			Insets aInsets) {
		super(new BorderLayout());

		JPanel north_content_south_panel = new JPanel(new BorderLayout(0, 3));
		north_content_south_panel.add(aContent, BorderLayout.CENTER);
		if ((aBorderFlag & NORTH) != 0) {
			north_content_south_panel.add(new TitledSeparator(aTitle),
					BorderLayout.NORTH);
		}
		if ((aBorderFlag & SOUTH) != 0) {
			north_content_south_panel.add(new JSeparator(
					SwingConstants.HORIZONTAL), BorderLayout.SOUTH);
		}
		if (aInsets != null) {
			north_content_south_panel.setBorder(BorderFactory
					.createEmptyBorder(0, aInsets.left, 0, aInsets.right));
		}

		add(north_content_south_panel, BorderLayout.CENTER);
		if ((aBorderFlag & WEST) != 0) {
			add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
		}
		if ((aBorderFlag & EAST) != 0) {
			add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);
		}
		if (aInsets != null) {
			setBorder(BorderFactory.createEmptyBorder(aInsets.top, 0,
					aInsets.bottom, 0));
		}
	}

	public static TitledPanel createTitledPanel(String aTitle,
			Component aComponent) {
		return createTitledPanel(aTitle, aComponent, NORTH);
	}

	public static TitledPanel createTitledPanel(String aTitle,
			Component aComponent, int aBorderFlag) {
		return new TitledPanel(aTitle, aComponent, aBorderFlag, new Insets(2,
				3, 2, 4));
	}

}
