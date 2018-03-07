package cnuphys.swim.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Used for testing the swimmer
 * @author heddle
 *
 */
public class SwimTester extends JFrame implements ListSelectionListener {

	private GenericList<ASwimTest> _jList;
	
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
		addNorth();
	}
	
	public void addNorth() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 4));
		
		_jList = new GenericList();
		_jList.addListSelectionListener(this);
		
		JScrollPane spane = _jList.getScrollPane();

		JLabel label = new JLabel("    Known Tests");
		label.setForeground(Color.red);
		
		panel.add(label, BorderLayout.NORTH);
		
		panel.add(spane, BorderLayout.CENTER);
		add(panel, BorderLayout.NORTH);
	}
	
	public void addTest(ASwimTest test) {
		_jList.getModel().addElement(test);
	}
	
	/**
	 * Main program
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		
		SwimTester tester = new SwimTester();
		
		//add the tests
		tester.addTest(new SameTrackDifferentMaxStep());
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// testFrame.pack();
				tester.setVisible(true);
				tester.setLocationRelativeTo(null);
			}
		});

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			System.err.println("Selected [" + _jList.getSelectedValue() + "]");
		}
	}
}
