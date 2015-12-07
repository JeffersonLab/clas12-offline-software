package cnuphys.bCNU.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

import javax.swing.JTextArea;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.plugin.Plugin;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;

public class PluginView extends BaseView {

    // default properties
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;
    private static final Color DEFAULT_BACKGROUND = Color.white;

    // reserved view type for drawing view
    public static final int PLUGINVIEWTYPE = -59132;

    // all items on the same layer
    private LogicalLayer _shapeLayer;

    // the corresponding plugin
    private Plugin _plugin;
    
    //status text field
    private JTextArea _textField;

    //used for vv placement
    private int _vvPanel = -1;
    private int _vvLocation = Plugin.CENTER;

    /**
     * Create a plugin view
     * 
     * @param title the title of the view
     * @param xmin minimum x value
     * @param ymin minimum y value
     * @param xmax maximum x value
     * @param ymax maximum y value
     * @param keyvals optional key value pairs
     */
    public PluginView(String title, double xmin, double ymin, double xmax,
	    double ymax, Properties props) {
	super(PropertySupport.TITLE, title, PropertySupport.WORLDSYSTEM,
		new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin),
		PropertySupport.WIDTH, DEFAULT_WIDTH, PropertySupport.HEIGHT,
		DEFAULT_HEIGHT, PropertySupport.TOOLBAR, true,
		PropertySupport.TOOLBARBITS,
		// BaseToolBar.EVERYTHING,
		BaseToolBar.NODRAWING & ~BaseToolBar.RANGEBUTTON
		// & ~BaseToolBar.TEXTFIELD
			& ~BaseToolBar.CONTROLPANELBUTTON
			& ~BaseToolBar.DELETEBUTTON,
			PropertySupport.VISIBLE, true, PropertySupport.HEADSUP, false,
		PropertySupport.BACKGROUND, DEFAULT_BACKGROUND,
		PropertySupport.VIEWTYPE, PLUGINVIEWTYPE,
		PropertySupport.STANDARDVIEWDECORATIONS, true);

	// add the shape layer
	_shapeLayer = new LogicalLayer(getContainer(), "Shapes");
	getContainer().addLogicalLayer(_shapeLayer);

	// process the optional arguments
	processProperties(props);
	
	//add the status line
	_textField = new JTextArea(" ", 2, 80);
	_textField.setLineWrap(true);

	_textField.setFont(Fonts.commonFont(Font.PLAIN, 12));
	_textField.setEditable(false);
	_textField.setBackground(Color.black);
	_textField.setForeground(Color.cyan);

	add(_textField, BorderLayout.SOUTH);
	VirtualView vv = VirtualView.getInstance();
	if (vv != null) {
	    vv.reconfigure();
	    if (_vvPanel > 0) {
		int col = _vvPanel - 1;
		int numCol = vv.getNumCol();
		col = Math.max(0, Math.min((numCol - 1), col));
		vv.moveTo(this, col, _vvLocation);
	    }
	}

    }
    
    /**
     * Update the status line with a new message
     * @param str the new message
     */
    public void updateStatus(String str) {
	_textField.setText((str != null) ? str : "");
    }
    
    /**
     * Append text to the status line
     * @param str the test to append
     */
    public void appendStatus(String str) {
	if (str != null) {
	    _textField.append(str);
	}
    }
    
    /**
     * Clear the status text
     */
    public void clearStatus() {
	_textField.setText("");
    }
    
    // process properties
    private void processProperties(Properties props) {
	if ((props == null) || props.isEmpty()) {
	    return;
	}

	Color background = PropertySupport.getBackground(props);
	if (background != null) {
	    getContainer().getComponent().setBackground(background);
	}

	// height and width?
	int height = PropertySupport.getHeight(props);
	int width = PropertySupport.getWidth(props);

	if (height == Integer.MIN_VALUE) {
	    height = DEFAULT_HEIGHT;
	}
	if (width == Integer.MIN_VALUE) {
	    width = DEFAULT_WIDTH;
	}

	setSize(width, height);
	
	//vv setting
	_vvPanel = PropertySupport.getVVPanel(props);
	_vvLocation = PropertySupport.getVVLocation(props);

    }

    /**
     * Get the shape layer
     * 
     * @return the shape layer
     */
    public LogicalLayer getShapeLayer() {
	return _shapeLayer;
    }

    /**
     * Get the plugin for this plugin view
     * 
     * @return this view's plugin
     */
    public Plugin getPlugin() {
	return _plugin;
    }

    /**
     * Set the plugin for this plugin view
     * 
     * @param plugin the plugin
     */
    public void setPlugin(Plugin plugin) {
	_plugin = plugin;
    }

}
