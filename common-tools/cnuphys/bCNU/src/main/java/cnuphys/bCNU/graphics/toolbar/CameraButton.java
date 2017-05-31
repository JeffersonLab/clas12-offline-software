package cnuphys.bCNU.graphics.toolbar;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.graphics.container.IContainer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.FileUtilities;

/**
 * A button used to capture the container into a jpeg image.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class CameraButton extends ToolBarButton {

	private String dirname = null;

	public CameraButton(IContainer container) {
		super(container, "images/camera.gif", "Capture to an image file");
	}

	/**
	 * This is what I do if I am pressed
	 * 
	 * @param actionEvent
	 *            the causal event.
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		
		if (container == null) {
			return;
		}
		
		if (container.handledCamera()) {
			return;
		}
		
		Toolkit.getDefaultToolkit().beep();

		try {

			File file = null;

			// try making a png
			if (Environment.getInstance().getPngWriter() != null) {

				file = FileUtilities.saveFile(dirname, "screencapture.png",
						"PNG ImageFile", "png", "PNG");

				if (file != null) {

					// Buffered image object to be written to depending on the
					// view type
					BufferedImage bi;

					ImageOutputStream ios = ImageIO
							.createImageOutputStream(file);
					Environment.getInstance().getPngWriter().setOutput(ios);

					bi = GraphicsUtilities.getComponentImage(container
							.getComponent());

					Environment.getInstance().getPngWriter().write(bi);
					ios.close();
				}
			}
		} catch (Exception e) {
			Log.getInstance().exception(e);
		}
		container.getToolBar().resetDefaultSelection();
	}

}
