package cnuphys.bCNU.util;

import java.awt.*;
import javax.swing.*;

import java.awt.print.*;

public class PrintUtilities implements Printable {
	private Component _componentToBePrinted;

	public static void printComponent(Component c) {
		new PrintUtilities(c).print();
	}

	public PrintUtilities(Component componentToBePrinted) {
		_componentToBePrinted = componentToBePrinted;
	}

	public void print() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		if (printJob.printDialog())
			try {
				printJob.print();
			} catch (PrinterException pe) {
				System.out.println("Error printing: " + pe);
			}
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return (NO_SUCH_PAGE);
		} else {
			Graphics2D g2d = (Graphics2D) g;
			// int dtype = g2d.getDeviceConfiguration().getDevice().getType();
			// if (dtype == GraphicsDevice.TYPE_PRINTER) {
			// System.err.println("A Printer");
			// }
			//
			//

			// AffineTransform nt =
			// g2d.getDeviceConfiguration().getNormalizingTransform();
			// nt.concatenate(g2d.getTransform());
			// g2d.setTransform(nt);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);

			g2d.translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			// TODO CACULATE CORRECT SCALE FACTOR
			g2d.scale(0.75, 0.75);

			disableDoubleBuffering(_componentToBePrinted);
			_componentToBePrinted.paint(g2d);
			enableDoubleBuffering(_componentToBePrinted);
			return (PAGE_EXISTS);
		}
	}

	private static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	private static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}
