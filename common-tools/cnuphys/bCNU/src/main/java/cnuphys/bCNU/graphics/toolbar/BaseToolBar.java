package cnuphys.bCNU.graphics.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import cnuphys.bCNU.component.MagnifyWindow;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.graphics.toolbar.lasso.FunnelLassoRectButton;
import cnuphys.bCNU.graphics.toolbar.lasso.ILassoListener;
import cnuphys.bCNU.graphics.toolbar.lasso.LassoRectButton;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.Fonts;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class BaseToolBar extends CommonToolBar
		implements MouseListener, MouseMotionListener {

	public static final int ELLIPSEBUTTON = 01;
	public static final int TEXTBUTTON = 02;
	public static final int RECTANGLEBUTTON = 04;
	public static final int POLYGONBUTTON = 010;
	public static final int LINEBUTTON = 020;
	public static final int RANGEBUTTON = 040;
	public static final int DELETEBUTTON = 0100;
	public static final int TEXTFIELD = 0200;
	public static final int USERCOMPONENT = 0400; // user (app) provides drawing
	public static final int CONTROLPANELBUTTON = 01000; // toggle control panel
	public static final int RADARCBUTTON = 02000;
	public static final int POLYLINEBUTTON = 04000;
	public static final int MAGNIFYBUTTON = 010000;
	
	public static final int NOZOOM = 020000;
	public static final int CLONEBUTTON  = 040000;

	public static final int PANBUTTON = 0100000;
	public static final int UNDOZOOMBUTTON = 0100000;

	public static final int RECTGRIDBUTTON = 01000000;

	// used to eliminate some basic buttons
	public static final int NOPRINTERBUTTON = 010000000;
	public static final int NOCAMERABUTTON = 020000000;
	public static final int CENTERBUTTON = 040000000;

	public static final int DRAWING = ELLIPSEBUTTON + TEXTBUTTON
			+ RECTANGLEBUTTON + POLYGONBUTTON + LINEBUTTON + RADARCBUTTON
			+ POLYLINEBUTTON;

	public static final int EVERYTHING = 07777777777 & ~NOCAMERABUTTON
			& ~NOPRINTERBUTTON & ~NOZOOM ;
	public static final int STANDARD = EVERYTHING & ~CONTROLPANELBUTTON
			& ~USERCOMPONENT & ~CLONEBUTTON;

	public static final int NODRAWING = EVERYTHING & ~DRAWING;

	// only the text button
	public static final int TEXTDRAWING = STANDARD & ~DRAWING + TEXTBUTTON
			& ~RANGEBUTTON;

	// nobuttons!
	public static final int NOTHING = 017777777777;

	/**
	 * Text field used for messages
	 */
	private JTextField _textField;
	
	//the clone button
	private CloneButton _cloneButton;

	// Zoom int by a fixed percentage
	private ZoomInButton _zoomInButton;

	// Zoom out by a fixed percentage
	private ZoomOutButton _zoomOutButton;

	// undo last zoom
	private UndoZoomButton _undoZoomButton;

	// refresh the container
	private RefreshButton _refreshButton;

	// zoom to whole world
	private WorldButton _worldButton;

	// default pointer tool
	private PointerButton _pointerButton;

	// optional lasso rectangle button
	private LassoRectButton _lassoRectButton;

	// optional funnellasso rectangle button
	private FunnelLassoRectButton _funnellassoRectButton;

	// rubber-band zoom
	private BoxZoomButton _boxZoomButton;

	// magnifying glass
	private MagnifyButton _magnifyButton;

	// center the view
	private CenterButton _centerButton;

	// create an ellipse
	private EllipseButton _ellipseButton;

	// add text to the view
	private TextButton _textButton;

	// pan the view
	private PanButton _panButton;

	// create a polygon
	private PolygonButton _polygonButton;

	// create a polygon
	private PolylineButton _polylineButton;

	// range (u r here) button
	private RangeButton _rangeButton;

	// draw a world rectangle
	private RectangleButton _rectangleButton;

	// draw a world rectangle grid
	private RectGridButton _rectgridButton;

	// draw a world rad arc
	private RadArcButton _radarcButton;

	// draw a world line
	private LineButton _lineButton;

	// delete selected items
	private DeleteButton _deleteButton;

	// toggle control panel
	private ControlPanelButton _cpButton;

	// creates a file image
	private CameraButton _cameraButton;

	// prints the container
	private PrinterButton _printerButton;

	// the owner container
	private IContainer _container;

	// user component
	private UserToolBarComponent _userComponent;

	// are there ANY bits set
	private boolean notNothing;

	/**
	 * Create a toolbar with all the buttons.
	 * 
	 * @param container the container this toolbar controls.
	 */
	public BaseToolBar(IContainer container) {
		this(container, EVERYTHING);
	}

	/**
	 * Create a tool bar.
	 * 
	 * @param container the container this toolbar controls.
	 * @param bits controls which tools are added.
	 */
	public BaseToolBar(IContainer container, int bits) {
		// box layout needed for user component to work
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		_container = container;
		_container.setToolBar(this);
		notNothing = bits != NOTHING;
		makeButtons(bits);

		Component c = _container.getComponent();
		if (c != null) {
			c.addMouseListener(this);
			c.addMouseMotionListener(this);
		}
		setBorder(BorderFactory.createEtchedBorder());
		setFloatable(false);
	}

	/**
	 * Makes all the buttons.
	 * 
	 * @param bits Bitwise test of which annotation buttons to add.
	 */
	protected void makeButtons(int bits) {
		if (notNothing && !Bits.checkBit(bits, NOCAMERABUTTON)) {
			_cameraButton = new CameraButton(_container);
		}

		if (notNothing && !Bits.checkBit(bits, NOPRINTERBUTTON)) {
			_printerButton = new PrinterButton(_container);
		}

		if (notNothing) {
			if (!Bits.checkBit(bits, NOZOOM)) {
				_zoomInButton = new ZoomInButton(_container);
				_zoomOutButton = new ZoomOutButton(_container);

				if (Bits.checkBit(bits, UNDOZOOMBUTTON)) {
					_undoZoomButton = new UndoZoomButton(_container);
				}
				_worldButton = new WorldButton(_container);
				_boxZoomButton = new BoxZoomButton(_container);
			}
			_refreshButton = new RefreshButton(_container);
			if (Bits.checkBit(bits, CENTERBUTTON)) {
				_centerButton = new CenterButton(_container);
			}

			if (Bits.checkBit(bits, PANBUTTON)) {
				_panButton = new PanButton(_container);
			}
			
			if (Bits.checkBit(bits, CLONEBUTTON)) {
				_cloneButton = new CloneButton(_container);
			}
		}

		if (notNothing && Bits.checkBit(bits, RANGEBUTTON)) {
			_rangeButton = new RangeButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, MAGNIFYBUTTON)) {
			_magnifyButton = new MagnifyButton(_container);
		}

		// if (Bits.checkBit(bits, POINTERBUTTON)) {
		_pointerButton = new PointerButton(_container);
		// }

		if (notNothing && Bits.checkBit(bits, DELETEBUTTON)) {
			_deleteButton = new DeleteButton(_container);
			_deleteButton.setEnabled(false);
		}

		if (notNothing && Bits.checkBit(bits, CONTROLPANELBUTTON)) {
			_cpButton = new ControlPanelButton(_container);
		}

		// check if drawing tools are requested
		if (notNothing && Bits.checkBit(bits, TEXTBUTTON)) {
			_textButton = new TextButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, ELLIPSEBUTTON)) {
			_ellipseButton = new EllipseButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, RECTANGLEBUTTON)) {
			_rectangleButton = new RectangleButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, RECTGRIDBUTTON)) {
			_rectgridButton = new RectGridButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, RADARCBUTTON)) {
			_radarcButton = new RadArcButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, LINEBUTTON)) {
			_lineButton = new LineButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, POLYGONBUTTON)) {
			_polygonButton = new PolygonButton(_container);
		}

		if (notNothing && Bits.checkBit(bits, POLYLINEBUTTON)) {
			_polylineButton = new PolylineButton(_container);
		}

		// add the pointer button and make it the default
		add(_pointerButton);

		if (_pointerButton != null) {
			setDefaultToggleButton(_pointerButton);
		}

		add(_printerButton);
		add(_cameraButton);
		add(_cpButton, false); // false to prevent it joining button group
		add(_boxZoomButton);
		add(_zoomInButton);
		add(_zoomOutButton);
		add(_undoZoomButton);
		add(_panButton);
		add(_magnifyButton);
		add(_centerButton);
		add(_worldButton);
		add(_rangeButton);
		add(_refreshButton);
		add(_rectangleButton);
		add(_radarcButton);
		add(_lineButton);
		add(_ellipseButton);
		add(_polygonButton);
		add(_polylineButton);
		add(_textButton);
		add(_deleteButton);
		add(_rectgridButton);
		
		if (_cloneButton != null) {
			add(Box.createHorizontalStrut(8));
		}
		add(_cloneButton);


		// add the text field?

		if (notNothing && Bits.checkBit(bits, TEXTFIELD)) {

			_textField = new JTextField(" ");

			_textField.setFont(Fonts.commonFont(Font.PLAIN, 11));
			_textField.setEditable(false);
			_textField.setBackground(Color.black);
			_textField.setForeground(Color.cyan);

			FontMetrics fm = getFontMetrics(_textField.getFont());
			Dimension d = _textField.getPreferredSize();
			d.width = fm
					.stringWidth(" ( 9999.99999 , 9999.99999 ) XXXXXXXXXXX");
			_textField.setPreferredSize(d);
			_textField.setMaximumSize(d);

			add(_textField);
		}

		// if user component, add last

		if (notNothing && Bits.checkBit(bits, USERCOMPONENT)) {
			addSeparator();
			_userComponent = new UserToolBarComponent(_container);
			add(_userComponent);
		}

		enableDrawingButtons(true);

		// set the default button to on
		if (getDefaultToggleButton() != null) {
			getDefaultToggleButton().setSelected(true);
		}

	}

	@Override
	public Component add(Component c) {
		if (c == null) {
			return null;
		}
		return super.add(c);
	}

	/**
	 * Sets the text in the text field widget.
	 * 
	 * @param text the new text.
	 */
	public void setText(String text) {
		if (_textField == null) {
			return;
		}

		if (text == null) {
			_textField.setText("");
		}
		else {
			_textField.setText(text);
		}
	}

	/**
	 * Enable/disable the drawing buttons
	 * 
	 * @param enabled the desired stated.
	 */
	public void enableDrawingButtons(boolean enabled) {
		if (_ellipseButton != null) {
			_ellipseButton.setEnabled(enabled);
		}
		if (_rectangleButton != null) {
			_rectangleButton.setEnabled(enabled);
		}
		if (_radarcButton != null) {
			_radarcButton.setEnabled(enabled);
		}
		if (_lineButton != null) {
			_lineButton.setEnabled(enabled);
		}
		if (_polygonButton != null) {
			_polygonButton.setEnabled(enabled);
		}
		if (_polylineButton != null) {
			_polylineButton.setEnabled(enabled);
		}
		if (_textButton != null) {
			_textButton.setEnabled(enabled);
		}
	}

	/**
	 * Reset the default toggle button selection
	 */
	@Override
	public void resetDefaultSelection() {
		super.resetDefaultSelection();
		ToolBarToggleButton defaultButton = (ToolBarToggleButton) getDefaultToggleButton();

		if (defaultButton != null) {
			_container.getComponent().setCursor(defaultButton.canvasCursor());
			defaultButton.requestFocus();
		}
	}

	/**
	 * Get the button used for a box (rubberband) zoom.
	 * 
	 * @return the button used for a box (rubberband) zoom.
	 */
	public BoxZoomButton getBoxZoomButton() {
		return _boxZoomButton;
	}

	/**
	 * Get the button used for magnification.
	 * 
	 * @return the button used for magnification.
	 */
	public MagnifyButton getMagnifyButton() {
		return _magnifyButton;
	}

	/**
	 * Get the camera button.
	 * 
	 * @return the camera button.
	 */
	public CameraButton getCameraButton() {
		return _cameraButton;
	}

	/**
	 * Get the button used for recentering.
	 * 
	 * @return the button used for recentering.
	 */
	public CenterButton getCenterButton() {
		return _centerButton;
	}

	/**
	 * Get the toolbar's delete button.
	 * 
	 * @return the toolbar's delete button.
	 */
	public DeleteButton getDeleteButton() {
		return _deleteButton;
	}

	/**
	 * Get the toolbar's control panel button.
	 * 
	 * @return the toolbar's control panel button.
	 */
	public ControlPanelButton getControlPanelButton() {
		return _cpButton;
	}

	/**
	 * Get the toolbar's ellipse button.
	 * 
	 * @return the toolbar's ellipse button.
	 */
	public EllipseButton getEllipseButton() {
		return _ellipseButton;
	}

	/**
	 * Get the toolbar's pan button.
	 * 
	 * @return the toolbar's pan button.
	 */
	public PanButton getPanButton() {
		return _panButton;
	}

	/**
	 * Get the toolbar's point button.
	 * 
	 * @return the toolbar's pointer button.
	 */
	public PointerButton getPointerButton() {
		return _pointerButton;
	}

	/**
	 * Get the toolbar's lasso rectangle button.
	 * 
	 * @return the toolbar's lasso rectangle button.
	 */
	public LassoRectButton getLassoRectButton() {
		return _lassoRectButton;
	}

	/**
	 * Get the toolbar's funnellasso rectangle button.
	 * 
	 * @return the toolbar's funnellasso rectangle button.
	 */
	public FunnelLassoRectButton getFunnelLassoRectButton() {
		return _funnellassoRectButton;
	}

	/**
	 * Get the toolbar's polygon button.
	 * 
	 * @return the toolbar's polygon button.
	 */
	public PolygonButton getPolygonButton() {
		return _polygonButton;
	}

	/**
	 * Get the toolbar's polyline button.
	 * 
	 * @return the toolbar's polyline button.
	 */
	public PolylineButton getPolylineButton() {
		return _polylineButton;
	}

	/**
	 * Get the toolbar's range button.
	 * 
	 * @return the toolbar's range button.
	 */
	public RangeButton getRangeButton() {
		return _rangeButton;
	}

	/**
	 * Get the toolbar's rectangle button.
	 * 
	 * @return the toolbar's rectangle button.
	 */
	public RectangleButton getRectangleButton() {
		return _rectangleButton;
	}

	/**
	 * Get the toolbar's rectangle grid button.
	 * 
	 * @return the toolbar's rectangle grid button.
	 */
	public RectGridButton getRectGridButton() {
		return _rectgridButton;
	}

	/**
	 * Get the toolbar's radarc button.
	 * 
	 * @return the toolbar's radarc button.
	 */
	public RadArcButton getRadArcButton() {
		return _radarcButton;
	}

	/**
	 * Get the toolbar's line button.
	 * 
	 * @return the toolbar's line button.
	 */
	public LineButton getLineButton() {
		return _lineButton;
	}

	/**
	 * Get the toolbar's refresh button.
	 * 
	 * @return the toolbar's refresh button.
	 */
	public RefreshButton getRefreshButton() {
		return _refreshButton;
	}

	/**
	 * Get the toolbar's text button.
	 * 
	 * @return the toolbar's text button.
	 */
	public TextButton getTextButton() {
		return _textButton;
	}

	/**
	 * Get the toolbar's undozoom button.
	 * 
	 * @return the toolbar's undozoom button.
	 */
	public UndoZoomButton getUndoZoomButton() {
		return _undoZoomButton;
	}

	/**
	 * Get the toolbar's world (default world zoom) button.
	 * 
	 * @return the toolbar's world button.
	 */
	public WorldButton getWorldButton() {
		return _worldButton;
	}
	
	/**
	 * Get the toolbar's clone button.
	 * 
	 * @return the toolbar's zoom-in button.
	 */
	public CloneButton getCloneButton() {
		return _cloneButton;
	}


	/**
	 * Get the toolbar's zoom-in button.
	 * 
	 * @return the toolbar's zoom-in button.
	 */
	public ZoomInButton getZoomInButton() {
		return _zoomInButton;
	}

	/**
	 * Get the toolbar's zoom-out button.
	 * 
	 * @return the toolbar's zoom-out button.
	 */
	public ZoomOutButton getZoomOutButton() {
		return _zoomOutButton;
	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

		if (!_container.getComponent().isEnabled()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();
		if (mtb == null) {
			return;
		}

		boolean mb1 = (mouseEvent.getButton() == MouseEvent.BUTTON1)
				&& !mouseEvent.isControlDown();
		boolean mb3 = (mouseEvent.getButton() == MouseEvent.BUTTON3)
				|| ((mouseEvent.getButton() == MouseEvent.BUTTON1)
						&& mouseEvent.isControlDown());

		if (mb1) {
			if (mouseEvent.getClickCount() == 1) { // single click
				mtb.mouseClicked(mouseEvent);
			}
			else { // double (or more) clicks
				mtb.mouseDoubleClicked(mouseEvent);
			}
		}
		else if (mb3) {
			// mtb.mouseButton3Click(mouseEvent);
		}

	}

	/**
	 * The mouse has entered the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseEntered(MouseEvent mouseEvent) {

		ToolBarToggleButton mtb = getActiveButton();
		if (mtb != null) {
			_container.getComponent().setCursor(mtb.canvasCursor());
			mtb.mouseEntered(mouseEvent);
		}
	}

	/**
	 * The mouse has exited the container.
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseExited(MouseEvent mouseEvent) {
		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseExited(mouseEvent);
	}

	/**
	 * The mouse was pressed. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release.
	 * 
	 * @param me the causal event.
	 */
	@Override
	public void mousePressed(MouseEvent me) {
		
		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		switch (me.getClickCount()) {
		case 1:

			// hack, if mouse button 2
			if (mtb == _pointerButton) {
				if ((_boxZoomButton != null)
						&& (me.getButton() == MouseEvent.BUTTON2)) {
					mtb = _boxZoomButton;
				}
			}

			mtb.mousePressed(me);
			break;
		}

	}

	/**
	 * The mouse was clicked. Note that the order the events will come is
	 * PRESSED, RELEASED, CLICKED. And a CLICKED will happen only if the mouse
	 * was not moved between press and release. Also, the RELEASED will come
	 * even if the mouse was dragged off the container.
	 * 
	 * @param me the causal event.
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		// hack, if mouse button 2 treat as box zoom
		if (mtb == _pointerButton) {
			if (me.getButton() == MouseEvent.BUTTON2) {
				mtb = _boxZoomButton;
			}
		}

		mtb.mouseReleased(me);

	}

	/**
	 * 
	 * @param mouseEvent the causal event.
	 */
	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		if (!_container.getComponent().isEnabled()) {
			return;
		}

		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseDragged(mouseEvent);
	}

	/**
	 * The mouse has moved. Note will not come here if mouse button pressed,
	 * will go to DRAG instead.
	 * 
	 * @param me the causal event.
	 */
	@Override
	public void mouseMoved(MouseEvent me) {
		ToolBarToggleButton mtb = getActiveButton();

		if (mtb == null) {
			return;
		}

		mtb.mouseMoved(me);
	}

	/**
	 * Convenience routine to get the active button.
	 * 
	 * @return the active toggle button.
	 */
	@Override
	public ToolBarToggleButton getActiveButton() {
		return (ToolBarToggleButton) super.getActiveButton();
	}

	/**
	 * Called after each item event to give the toolbar a chance to reflect the
	 * correct state.
	 */
	public void checkButtonState() {
		if (_deleteButton != null) {
			_deleteButton.setEnabled(_container.anySelectedItems());
		}
	}

	/**
	 * Get the user component, on which the app might draw stuff
	 * 
	 * @return the userComponent
	 */
	public UserToolBarComponent getUserComponent() {
		return _userComponent;
	}

	/**
	 * Set the drawable for the user component (if there is a user component).
	 * 
	 * @param drawable the drawable to use.
	 */
	public void setUserComponentDrawable(IDrawable drawable) {
		if (_userComponent != null) {
			_userComponent.setUserDraw(drawable);
		}
	}

	/**
	 * Add the lasso button
	 * 
	 * @param lassoListener will respond to the lasso selections
	 */
	public void addLassoButton(ILassoListener lassoListener) {
		addLassoButton(lassoListener, false);
	}

	/**
	 * Add the funnellasso button
	 * 
	 * @param lassoListener will respond to the funnellasso selections
	 */
	public void addFunnelLassoButton(ILassoListener lassoListener) {
		addFunnelLassoButton(lassoListener, false);
	}

	/**
	 * Add the lasso button
	 * 
	 * @param lassoListener will respond to the lasso selections
	 * @param xormode will cause the lasso to rubberband in simpler xormode.
	 *            This works better for 3D containers.
	 */
	public void addLassoButton(ILassoListener lassoListener, boolean xormode) {
		_lassoRectButton = new LassoRectButton(_container, lassoListener,
				xormode);
		add(_lassoRectButton);
	}

	/**
	 * Add the funnellasso button
	 * 
	 * @param lassoListener will respond to the funnel lasso selections
	 * @param xormode will cause the funnel lasso to rubberband in simpler
	 *            xormode. This works better for 3D containers.
	 */
	public void addFunnelLassoButton(ILassoListener lassoListener,
			boolean xormode) {
		_funnellassoRectButton = new FunnelLassoRectButton(_container,
				lassoListener, xormode);
		add(_funnellassoRectButton);
	}

	/**
	 * @return the _textField
	 */
	public JTextField getTextField() {
		return _textField;
	}

	/**
	 * The active toggle button has changed
	 */
	@Override
	protected void activeToggleButtonChanged() {
		if (getActiveButton() != _magnifyButton) {
			MagnifyWindow.closeMagnifyWindow();
		}
		if (_container != null) {
			_container.activeToolBarButtonChanged(getActiveButton());
		}
	}

}
