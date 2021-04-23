package cnuphys.splot.plot;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class PrintUtilities implements Printable {

	private static boolean _isPrinting;
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
			}
			catch (PrinterException pe) {
				System.out.println("Error printing: " + pe);
			}
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex > 0) {
			return (NO_SUCH_PAGE);
		}
		else {
			Graphics2D g2d = (Graphics2D) g;

			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

			// TODO CACULATE CORRECT SCALE FACTOR
			g2d.scale(0.75, 0.75);

			_isPrinting = true;
			_componentToBePrinted.printAll(g2d);
			_isPrinting = false;
			return (PAGE_EXISTS);
		}
	}

	public static boolean isPrinting() {
		return _isPrinting;
	}
}