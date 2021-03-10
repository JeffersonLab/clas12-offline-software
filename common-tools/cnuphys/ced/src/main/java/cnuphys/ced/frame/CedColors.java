package cnuphys.ced.frame;

import java.awt.Color;
import cnuphys.bCNU.util.X11Colors;

public class CedColors {

	// doca's
	public static final Color docaTruthLine = Color.blue;
	public static final Color docaTruthFill = new Color(0, 0, 0, 76);

//	// track fit docas
//	public static final Color tbDocaLine = Color.green;
//	public static final Color tbDocaFill = new Color(0, 0, 0, 60);


	// hb segment color
	public static final Color hbSegmentLine = X11Colors.getX11Color("brown");

	// tb segment line color
	public static final Color tbSegmentLine = X11Colors.getX11Color("Navy");
	
	// ai hb segment color
	public static final Color aihbSegmentLine = X11Colors.getX11Color("dark khaki");

	// ai tb segment line color
	public static final Color aitbSegmentLine = X11Colors.getX11Color("dark magenta");


	// for hits cells
	public static final Color defaultHitCellFill = Color.red;
	public static final Color defaultHitCellLine = X11Colors.getX11Color("Dark Red");

	// hexagon color
	public static final Color hexColor = new Color(223, 239, 239);

	// pale color used to fill in DC layers to guide the eye
	public static final Color layerFillColors[] = { X11Colors.getX11Color("cornsilk"), X11Colors.getX11Color("azure") };

	// color for wires
	public static final Color senseWireColor = X11Colors.getX11Color("Dodger Blue");

	// cvt tracks
	public static final Color cvtTrackColor = X11Colors.getX11Color("Dark Green");

	// hit based
	public static final Color HB_COLOR = Color.yellow;
	public static final Color HB_TRANS = new Color(255, 255, 0, 240);
	public static final Color HB_DOCAFRAME = HB_COLOR.darker();

	// time based
	public static final Color TB_COLOR = X11Colors.getX11Color("dark orange");
	public static final Color TB_TRANS = X11Colors.getX11Color("dark orange", 240);
	public static final Color TB_DOCAFRAME = TB_COLOR.darker();

	// AI hit based
	public static final Color AIHB_COLOR = X11Colors.getX11Color("spring green");
	public static final Color AIHB_TRANS = X11Colors.getX11Color("spring green", 240);
	public static final Color AIHB_DOCAFRAME = AIHB_COLOR.darker();

	// AI time based
	public static final Color AITB_COLOR = X11Colors.getX11Color("magenta");
	public static final Color AITB_TRANS = X11Colors.getX11Color("magenta", 240);
	public static final Color AITB_DOCAFRAME = AITB_COLOR.darker();

	// neural net based
	public static final Color NN_COLOR = X11Colors.getX11Color("teal");
	public static final Color NN_TRANS = X11Colors.getX11Color("teal", 160);

	// doca fills
	public static final Color DOCA_COLOR = new Color(0, 255, 0, 76);
	public static final Color TRKDOCA_COLOR = new Color(0, 0, 255, 76);

	// fills for clusters
	public static final Color HB_CLUSTER_COLOR = new Color(255, 255, 0, 220);
	public static final Color TB_CLUSTER_COLOR = X11Colors.getX11Color("dark orange", 220);
	public static final Color SNR_CLUSTER_COLOR = new Color(46, 169, 87, 220);
	
	//cvt
	public static final Color CVT_COLOR = X11Colors.getX11Color("dark green");
	
	//REC::Calorimeter
	public static final Color RECEcalFill = new Color(255, 0, 0, 48);
	public static final Color RECPcalFill = new Color(196, 64, 0, 48);


}
