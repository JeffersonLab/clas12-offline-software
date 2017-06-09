package cnuphys.tinyMS.streamCapture;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class StreamCaptureTest {

	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame();

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);

		testFrame.setLayout(new BorderLayout());
		testFrame.add(new StreamCapturePane(), BorderLayout.CENTER);

		testFrame.setSize(600, 600);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

		System.out.println("Hey man (out)");
		System.err.println("Hey man (err)");
		System.out.print("out followed by ");
		System.err.println("err");
	}

}
