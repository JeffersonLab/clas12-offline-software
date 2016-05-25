package cnuphys.ced.frame;

import java.awt.Color;
import cnuphys.bCNU.util.X11Colors;

public class CedColors {

	// for gemc doca's
	public static final Color docaLine = Color.lightGray;
	public static final Color docaFill = new Color(255, 255, 255, 60);

	// for gemc smeared doca's
	private static final Color sdocaLine = Color.gray;
	private static final Color sdocaFill = new Color(255, 128, 255, 60);

	// track fit docas
	public static final Color tbDocaLine = Color.green;
	public static final Color tbDocaFill = new Color(255, 255, 120, 60);

	//tb segment color
	public static final Color tbSegmentLine = X11Colors.getX11Color("Navy");
	
	// for hits cells
	public static final Color defaultHitCellFill = Color.red;
	public static final Color defaultHitCellLine = X11Colors.getX11Color("Dark Red");

	//hexagon color
	public static final Color hexColor = new Color(223, 239, 239);

	// pale color used to fill in DC layers to guide the eye
	public static final Color layerFillColors[] = { X11Colors.getX11Color("cornsilk"),
			X11Colors.getX11Color("azure") };

	// color for wires
	public static final Color senseWireColor = X11Colors.getX11Color("Dodger Blue");
	


}
