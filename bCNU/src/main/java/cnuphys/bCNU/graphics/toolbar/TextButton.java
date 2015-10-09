package cnuphys.bCNU.graphics.toolbar;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import cnuphys.bCNU.dialog.LabelDialog;
import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.item.TextItem;
import cnuphys.bCNU.util.UnicodeSupport;

/**
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class TextButton extends ToolBarToggleButton {

	/**
	 * Create a button for creating annotated text items.
	 * 
	 * @param container
	 *            the owner container.
	 */
	public TextButton(IContainer container) {
		super(container, "images/text.gif", "Use to annotate");
	}

	/**
	 * The mouse was clicked.
	 * 
	 * @param mouseEvent
	 *            the causal event.
	 */
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {

		LabelDialog labelDialog = new LabelDialog();
		GraphicsUtilities.centerComponent(labelDialog);
		labelDialog.setVisible(true);

		String resultString = UnicodeSupport.specialCharReplace(labelDialog
				.getText());

		if ((resultString != null) && (resultString.length() > 0)) {
			Font font = labelDialog.getSelectedFont();
			if (font != null) {
				Point2D.Double wp = new Point2D.Double();
				container.localToWorld(mouseEvent.getPoint(), wp);
				TextItem item = new TextItem(container.getAnnotationLayer(),
						wp, font, resultString,
						labelDialog.getTextForeground(),
						labelDialog.getTextBackground(), null);
				if (item != null) {
					item.setDraggable(true);
					item.setRotatable(true);
					item.setResizable(true);
					item.setDeletable(true);
					item.setLocked(false);
					item.setRightClickable(true);
				}
				container.refresh();
			}
		}

		container.selectAllItems(false);
		container.getToolBar().resetDefaultSelection();
		container.refresh();
	}

}
