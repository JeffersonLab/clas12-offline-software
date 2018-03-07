package cnuphys.swim.test;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Used for testing the swimmer
 * @author heddle
 *
 */
public class SwimTester extends JFrame {

	private GenericList<? extends ASwimTest> _jList;
	
	public SwimTester() {
		super("Swim Tester");
		
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.out.println("Exiting.");
				System.exit(0);
			}
		};
		addWindowListener(wa);
		
		addContent();
		pack();
	}
	
	//add the content to the frame
	private void addContent() {
		setLayout(new BorderLayout(4, 4));
		_jList = new GenericList();
		
		JScrollPane spane = _jList.getScrollPane();
		add(spane, BorderLayout.CENTER);
	}
	
	/**
	 * Main program
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		
		SwimTester tester = new SwimTester();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// testFrame.pack();
				tester.setVisible(true);
				tester.setLocationRelativeTo(null);
			}
		});

	}
}
