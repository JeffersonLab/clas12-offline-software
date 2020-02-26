package cnuphys.bCNU.graphics.container;

import java.awt.Component;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D.Double;
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

public class ContainerAdapter implements IContainer {

    @Override
    public LogicalLayer addLogicalLayer(String name) {
	return null;
    }

    @Override
    public void addLogicalLayer(LogicalLayer layer) {
    }

    @Override
    public LogicalLayer getAnnotationLayer() {
	return null;
    }

    @Override
    public LogicalLayer getLogicalLayer(String name) {
	return null;
    }

    @Override
    public void removeLogicalLayer(LogicalLayer layer) {
    }

    @Override
    public void localToWorld(Point pp, Double wp) {
    }

    @Override
    public void worldToLocal(Point pp, Double wp) {
    }

    @Override
    public void worldToLocal(Rectangle r, java.awt.geom.Rectangle2D.Double wr) {
    }

    @Override
    public void localToWorld(Rectangle r, java.awt.geom.Rectangle2D.Double wr) {
    }

    @Override
    public void worldToLocal(Polygon polygon, WorldPolygon worldPolygon) {
    }

    @Override
    public void localToWorld(Polygon polygon, WorldPolygon worldPolygon) {
    }

    @Override
    public void worldToLocal(Point pp, double wx, double wy) {
    }

    @Override
    public void pan(int dh, int dv) {
    }

    @Override
    public void recenter(Point pp) {
    }

    @Override
    public void prepareToZoom() {
    }

    @Override
    public void restoreDefaultWorld() {
    }

    @Override
    public void scale(double scaleFactor) {
    }

    @Override
    public void undoLastZoom() {
    }

    @Override
    public VisibilityTableScrollPane getVisibilityTableScrollPane() {
	return null;
    }

    @Override
    public void rubberBanded(Rectangle b) {
    }

    @Override
    public AItem getItemAtPoint(Point lp) {
	return null;
    }

    @Override
    public Vector<AItem> getEnclosedItems(Rectangle rect) {
	return null;
    }

    @Override
    public Vector<AItem> getItemsAtPoint(Point lp) {
	return null;
    }

    @Override
    public boolean anySelectedItems() {
	return false;
    }

    @Override
    public void deleteSelectedItems(IContainer container) {
    }

    @Override
    public void selectAllItems(boolean select) {
    }

    @Override
    public void zoom(double xmin, double xmax, double ymin, double ymax) {
    }

    @Override
    public void reworld(double xmin, double xmax, double ymin, double ymax) {
    }

    @Override
    public BaseToolBar getToolBar() {
	return null;
    }

    @Override
    public void setToolBar(BaseToolBar toolBar) {
    }

    @Override
    public ToolBarToggleButton getActiveButton() {
	return null;
    }

    @Override
    public void locationUpdate(MouseEvent mouseEvent, boolean dragging) {
    }

    @Override
    public void redoFeedback() {
    }

    @Override
    public BaseView getView() {
	return null;
    }

    @Override
    public void setView(BaseView view) {
    }

    @Override
    public void setFeedbackPane(FeedbackPane feedbackPane) {
    }

    @Override
    public FeedbackPane getFeedbackPane() {
	return null;
    }

    @Override
    public FeedbackControl getFeedbackControl() {
	return null;
    }

    @Override
    public YouAreHereItem getYouAreHereItem() {
	return null;
    }

    @Override
    public void setYouAreHereItem(YouAreHereItem youAreHereItem) {
    }

    @Override
    public LogicalLayer getGlassLayer() {
	return null;
    }

    @Override
    public void handleFile(File file) {
    }

    @Override
    public void setDirty(boolean dirty) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public Component getComponent() {
	return null;
    }

    @Override
    public BufferedImage getImage() {
	return null;
    }

    @Override
    public void setAfterDraw(IDrawable afterDraw) {
    }

    @Override
    public void setBeforeDraw(IDrawable beforeDraw) {
    }

    @Override
    public AItem createEllipseItem(LogicalLayer layer, Rectangle b) {
	return null;
    }

    @Override
    public AItem createRectangleItem(LogicalLayer layer, Rectangle b) {
	return null;
    }

    @Override
    public AItem createLineItem(LogicalLayer layer, Point p0, Point p1) {
	return null;
    }

    @Override
    public AItem createPolygonItem(LogicalLayer layer, Point[] pp) {
	return null;
    }

    @Override
    public AItem createPolylineItem(LogicalLayer layer, Point[] pp) {
	return null;
    }

    @Override
    public AItem createRadArcItem(LogicalLayer layer, Point pc, Point p1,
	    double arcAngle) {
	return null;
    }

    @Override
    public String getLocationString(Double wp) {
	return null;
    }

    @Override
    public DrawableList getLogicalLayers() {
	return null;
    }

    @Override
    public Double getWorldPoint() {
	return null;
    }

    @Override
    public java.awt.geom.Rectangle2D.Double getWorldSystem() {
	return null;
    }

    @Override
    public void setWorldSystem(java.awt.geom.Rectangle2D.Double wr) {
    }

    @Override
    public Rectangle getInsetRectangle() {
	return null;
    }

    @Override
    public void setLeftMargin(int lMargin) {

    }

    @Override
    public void setTopMargin(int tMargin) {
    }

    @Override
    public void setRightMargin(int rMargin) {
    }

    @Override
    public void setBottomMargin(int bMargin) {
    }

    /**
     * The active toolbar button changed.
     * 
     * @param activeButton the new active button.
     */
    @Override
    public void activeToolBarButtonChanged(ToolBarToggleButton activeButton) {
    }
    
	/**
	 * Have you handled the print button so the default action is ignored.
	 * @return <code>true</code> if the printer button was handled.
	 */
	@Override
	public boolean handledPrint() {
		return false;
	}
	
	/**
	 * Have you handled the camera button so the default action is ignored.
	 * @return <code>true</code> if the camera button was handled.
	 */
	@Override
	public boolean handledCamera() {
		return false;
	}


}
