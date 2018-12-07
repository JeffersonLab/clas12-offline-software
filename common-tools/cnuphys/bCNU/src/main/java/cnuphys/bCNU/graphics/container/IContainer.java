package cnuphys.bCNU.graphics.container;

import java.awt.Component;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import cnuphys.bCNU.drawable.DrawableList;
import cnuphys.bCNU.drawable.IDrawable;
import cnuphys.bCNU.feedback.FeedbackControl;
import cnuphys.bCNU.feedback.FeedbackPane;
import cnuphys.bCNU.graphics.toolbar.BaseToolBar;
import cnuphys.bCNU.graphics.toolbar.ToolBarToggleButton;
import cnuphys.bCNU.graphics.world.WorldPolygon;
import cnuphys.bCNU.item.AItem;
import cnuphys.bCNU.item.YouAreHereItem;
import cnuphys.bCNU.layer.LogicalLayer;
import cnuphys.bCNU.view.BaseView;
import cnuphys.bCNU.visible.VisibilityTableScrollPane;

public interface IContainer {

	/**
	 * The factor used for fixed zooms.
	 */
	public static final double FIXED_ZOOM_FACTOR = 0.85;

	/**
	 * Add a layer for containing items rendered on this container..
	 * 
	 * @param name
	 *            the name of the logical layer. If one with that name already
	 *            exists, it is returned.
	 */
	public LogicalLayer addLogicalLayer(String name);

	/**
	 * Add a layer to this container.
	 * 
	 * @param layer
	 *            the logical layer to add.
	 */
	public void addLogicalLayer(LogicalLayer layer);

	/**
	 * Get the annotation layer for this obtainer.
	 * 
	 * @return the annotation layer for this obtainer. All drawing tools draw on
	 *         the annotation layer, which is kept on top.
	 */
	public LogicalLayer getAnnotationLayer();

	/**
	 * Gets a user layer by name. Do not use for the annotation layer-- for that
	 * use getAnnotationLayer().
	 * 
	 * @param name
	 *            the name of the logical layer.
	 * @return the layer, or <code>null</code>.
	 */
	public LogicalLayer getLogicalLayer(String name);

	/**
	 * Remove a layer for containing items rendered on this container.
	 * 
	 * @param layer
	 *            the layer to remove
	 */
	public void removeLogicalLayer(LogicalLayer layer);

	/**
	 * This converts a screen or pixel point to a world point.
	 * 
	 * @param pp
	 *            contains the local (screen-pixel) point.
	 * @param wp
	 *            will hold the resultant world point.
	 */
	public void localToWorld(Point pp, Point2D.Double wp);

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param pp
	 *            will hold the resultant local (screen-pixel) point.
	 * @param wp
	 *            contains world point.
	 */
	public void worldToLocal(Point pp, Point2D.Double wp);

	/**
	 * This converts a world rectangle to a screen or pixel rectangle.
	 * 
	 * @param r
	 *            will hold the resultant local (screen-pixel) rectangle.
	 * @param wr
	 *            contains the world rectangle.
	 */
	public void worldToLocal(Rectangle r, Rectangle.Double wr);

	/**
	 * This converts a screen or local rectangle to a world rectangle.
	 * 
	 * @param r
	 *            contains the local (screen-pixel) rectangle.
	 * @param wr
	 *            will hold the resultant world rectangle.
	 */
	public void localToWorld(Rectangle r, Rectangle.Double wr);

	/**
	 * This converts a world polygon to a screen or pixel polygon.
	 * 
	 * @param polygon
	 *            will hold the resultant local (screen-pixel) polygon.
	 * @param worldPolygon
	 *            contains the world polygon.
	 */
	public void worldToLocal(Polygon polygon, WorldPolygon worldPolygon);

	/**
	 * This converts a screen or local polygon to a world polygon.
	 * 
	 * @param polygon
	 *            contains the local (screen-pixel) polygon.
	 * @param worldPolygon
	 *            will hold the resultant world polygon.
	 */
	public void localToWorld(Polygon polygon, WorldPolygon worldPolygon);

	/**
	 * This converts a world point to a screen or pixel point.
	 * 
	 * @param pp
	 *            will hold the resultant local (screen-pixel) point.
	 * @param wx
	 *            the world x coordinate.
	 * @param wy
	 *            the world y coordinate.
	 */
	public void worldToLocal(Point pp, double wx, double wy);

	/**
	 * Pan the container.
	 * 
	 * @param dh
	 *            the horizontal step in pixels.
	 * @param dv
	 *            the vertical step in pixels.
	 */
	public void pan(int dh, int dv);

	/**
	 * Recenter the container at the point of a click.
	 * 
	 * @param pp
	 *            the point in question. It will be the new center.
	 */
	public void recenter(Point pp);

	/**
	 * Begin preparations for a zoom.
	 */
	public void prepareToZoom();

	/**
	 * Restore the default world. This gets us back to the original zoom level.
	 */
	public void restoreDefaultWorld();

	/**
	 * Convenience routine to scale the container.
	 * 
	 * @param scaleFactor
	 *            the scale factor.
	 */
	public void scale(double scaleFactor);

	/**
	 * Undo that last zoom.
	 */
	public void undoLastZoom();

	/**
	 * Get a scroll pane with a table for controlling logical layer visibility
	 * 
	 * @return a scroll pane with a table for controlling logical layer
	 *         visibility
	 */
	public VisibilityTableScrollPane getVisibilityTableScrollPane();

	/**
	 * This is called when we have completed a rubber banding. pane.
	 * 
	 * @param b
	 *            The rubber band bounds.
	 */

	public void rubberBanded(Rectangle b);

	/**
	 * Find an item, if any, at the point.
	 * 
	 * @param lp
	 *            The pixel point in question.
	 * @return the topmost satisfying item, or null.
	 */
	public AItem getItemAtPoint(Point lp);

	/**
	 * Obtain a collection of all enclosed items across all layers.
	 * 
	 * @param rect
	 *            the rectangle in question.
	 * @return all items on all layers enclosed by the rectangle.
	 */

	public Vector<AItem> getEnclosedItems(Rectangle rect);

	/**
	 * Find all items, if any, at the point.
	 * 
	 * @param lp
	 *            the pixel point in question.
	 * @return all items across all layers that contain the given point. It may
	 *         be an empty vector, but it won't be <code>null</null>.
	 */
	public Vector<AItem> getItemsAtPoint(Point lp);

	/**
	 * Check whether at least one item on any layer is selected.
	 * 
	 * @return <code>true</code> if at least one item on any layer is selected.
	 */
	public boolean anySelectedItems();

	/**
	 * Delete all selected items, across all layers.
	 * 
	 * @param container
	 *            the container they lived on.
	 */
	public void deleteSelectedItems(IContainer container);

	/**
	 * Select or deselect all items, across all layers.
	 * 
	 * @param select
	 *            the selection flag.
	 */
	public void selectAllItems(boolean select);

	/**
	 * Zooms to the specified area.
	 * 
	 * @param xmin
	 *            minimum x coordinate.
	 * @param xmax
	 *            maximum x coordinate.
	 * @param ymin
	 *            minimum y coordinate.
	 * @param ymax
	 *            maximum y coordinate.
	 */
	public void zoom(final double xmin, final double xmax, final double ymin,
			final double ymax);

	/**
	 * Reworld to the specified area.
	 * 
	 * @param xmin
	 *            minimum x coordinate.
	 * @param xmax
	 *            maximum x coordinate.
	 * @param ymin
	 *            minimum y coordinate.
	 * @param ymax
	 *            maximum y coordinate.
	 */
	public void reworld(final double xmin, final double xmax,
			final double ymin, final double ymax);

	/**
	 * Get this container's tool bar.
	 * 
	 * @return this container's tool bar, or <code>null</code>.
	 */
	public BaseToolBar getToolBar();

	/**
	 * Set this container's tool bar.
	 * 
	 * @param toolBar
	 *            the new toolbar.
	 */
	public void setToolBar(BaseToolBar toolBar);
	
	/**
	 * The active toolbar button changed.
	 * @param activeButton the new active button.
	 */
	public void activeToolBarButtonChanged(ToolBarToggleButton activeButton);
	
	/**
	 * Have you handled the print button so the default action is ignored.
	 * @return <code>true</code> if the printer button was handled.
	 */
	public boolean handledPrint();
	
	/**
	 * Have you handled the camera button so the default action is ignored.
	 * @return <code>true</code> if the camera button was handled.
	 */
	public boolean handledCamera();

	/**
	 * Get the active button on the toolbar, if there is a toolbar.
	 * 
	 * @return the active toggle button.
	 */
	public ToolBarToggleButton getActiveButton();

	/**
	 * Convenience method to update the location string in the toolbar.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 * @param dragging
	 *            <code>true</code> if we are dragging
	 */
	public void locationUpdate(MouseEvent mouseEvent, boolean dragging);

	/**
	 * Force a redo of the feedback even though the mouse didn't move. This is
	 * useful, for example, when control-N'ing events.
	 */
	public void redoFeedback();

	/**
	 * Get the view (internal frame) that holds this container.
	 * 
	 * @return the view (internal frame) that holds this container.
	 */
	public BaseView getView();

	/**
	 * Set the container's view.
	 * 
	 * @param view
	 *            the view to set.
	 */
	public void setView(BaseView view);

	/**
	 * Sets the feedback pane. This is an optional alternative to a HUD.
	 * 
	 * @param feedbackPane
	 *            the feedback pane.
	 */
	public void setFeedbackPane(FeedbackPane feedbackPane);

	/**
	 * Get the optional feedback pane.
	 * 
	 * @return the feedbackPane
	 */
	public FeedbackPane getFeedbackPane();

	/**
	 * Return the object that controls the container's feedback. You can and and
	 * remove feedback providers using this object.
	 * 
	 * @return the object that controls the container's feedback.
	 */
	public FeedbackControl getFeedbackControl();

	/**
	 * Get the optional YouAreHereItem
	 * 
	 * @return the youAreHereItem
	 */
	public YouAreHereItem getYouAreHereItem();

	/**
	 * Set the optional YouAreHereItem.
	 * 
	 * @param youAreHereItem
	 *            the youAreHereItem to set
	 */
	public void setYouAreHereItem(YouAreHereItem youAreHereItem);

	/**
	 * This is sometimes used as needed (i.e., not created until requested).
	 * That will generally make it the topmost view--so it is good for things
	 * like a reference point (YouAreHereItem).
	 * 
	 * @return the glass layer.
	 */
	public LogicalLayer getGlassLayer();

	/**
	 * Handle a file, one that probably result from a drag and drop or a double
	 * click. Treat it like an "open".
	 * 
	 * @param file
	 *            the file to handle.
	 */
	public void handleFile(File file);

	/**
	 * Convenience method for setting the dirty flag for all items on all
	 * layers.
	 * 
	 * @param dirty
	 *            the new value of the dirty flag.
	 */
	public void setDirty(boolean dirty);

	/**
	 * Refresh the container.
	 */
	public void refresh();

	/**
	 * Get the underlying JComponent
	 * 
	 * @return the underlying component
	 */
	public Component getComponent();

	/**
	 * Get the background image.
	 * 
	 * @return the fully painted background image.
	 */
	public BufferedImage getImage();

	/**
	 * Set the after-draw drawable for this container.
	 * 
	 * @param afterDraw
	 *            the new after-draw drawable.
	 */
	public void setAfterDraw(IDrawable afterDraw);

	/**
	 * Set the before-draw drawable.
	 * 
	 * @param beforeDraw
	 *            the new before-draw drawable.
	 */
	public void setBeforeDraw(IDrawable beforeDraw);

	/**
	 * From a given screen rectangle, create an ellipse item.
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param b
	 *            the bounding screen rectangle, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createEllipseItem(LogicalLayer layer, Rectangle b);

	/**
	 * From a given screen rectangle, create a rectangle item.
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param b
	 *            the screen rectangle, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createRectangleItem(LogicalLayer layer, Rectangle b);

	/**
	 * From two given screen points, create a line item
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param p0
	 *            one screen point, probably from rubber banding.
	 * @param p1
	 *            another screen point, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createLineItem(LogicalLayer layer, Point p0, Point p1);

	/**
	 * From a given screen polygon, create a polygon item.
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param pp
	 *            the screen polygon, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createPolygonItem(LogicalLayer layer, Point pp[]);

	/**
	 * From a given screen polyline, create a polyline item.
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param pp
	 *            the screen polyline, probably from rubber banding.
	 * @return the new item
	 */
	public AItem createPolylineItem(LogicalLayer layer, Point pp[]);

	/**
	 * Create a radarc item from the given parameters, probably obtained by
	 * rubberbanding.
	 * 
	 * @param layer
	 *            the layer to put the item on
	 * @param pc
	 *            the center of the arc
	 * @param p1
	 *            the point at the end of the first leg. Thus pc->p1 determing
	 *            the radius.
	 * @param arcAngle
	 *            the angle COUNTERCLOCKWISE in degrees.
	 * @return the new item
	 */
	public AItem createRadArcItem(LogicalLayer layer, Point pc, Point p1,
			double arcAngle);

	/**
	 * Get a location string for a point
	 * 
	 * @param wp
	 *            the world point in question
	 * @return a location string for a point
	 */
	public String getLocationString(Point2D.Double wp);

	/**
	 * Get all the logical layers.
	 * 
	 * @return all logical layers in the container.
	 */
	public DrawableList getLogicalLayers();

	/**
	 * Create a Point2D.Double or subclass thereof that is appropriate for this
	 * container. For example, a map application might return a subclass of
	 * Point2D.Double that deals with latitude and longitude.
	 * 
	 * @return a Point2D.Double or subclass thereof that is appropriate for this
	 *         container.
	 */
	public Point2D.Double getWorldPoint();

	/**
	 * Get the current world system
	 * 
	 * @return the world system
	 */
	public Rectangle2D.Double getWorldSystem();

	/**
	 * Set the world system (does not cause redraw)
	 * 
	 * @param wr
	 *            the new world system
	 */
	public void setWorldSystem(Rectangle2D.Double wr);

	/**
	 * Obtain the inset rectangle. Insets are the inert region around the
	 * container's active area. Often there are no insets. Sometimes they are
	 * used so that text can be written in the inset area, such as for plot
	 * view.
	 * 
	 * @return the inset rectangle.
	 */
	public Rectangle getInsetRectangle();

	/**
	 * Set the left margin for containers with nonzero insets.
	 * 
	 * @param lMargin
	 *            the left margin
	 */
	public void setLeftMargin(int lMargin);

	/**
	 * Set the top margin for containers with nonzero insets.
	 * 
	 * @param tMargin
	 *            the top margin
	 */
	public void setTopMargin(int tMargin);

	/**
	 * Set the right margin for containers with nonzero insets.
	 * 
	 * @param rMargin
	 *            the right margin
	 */
	public void setRightMargin(int rMargin);

	/**
	 * Set the bottom margin for containers with nonzero insets.
	 * 
	 * @param bMargin
	 *            the bottom margin
	 */
	public void setBottomMargin(int bMargin);

}
