package cnuphys.bCNU.graphics.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.IDN;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A simple browser panel
 * 
 * @author heddle
 *
 */
public class BrowserPanel extends JPanel implements HyperlinkListener  {

	// for entering the url
	private JTextField txtURL = new JTextField("");
	private JEditorPane ep = new JEditorPane("text/html",
	        "<H1>A!</H1><P><FONT COLOR=blue>blue</FONT></P>");
	private JLabel lblStatus = new JLabel(" ");

	public BrowserPanel() {
		setLayout(new BorderLayout(4, 4));
		addNorth();
		addCenter();
		addSouth();
		
		ActionListener al = new ActionListener() {       
	          @Override
			public void actionPerformed(ActionEvent ae) {         
	               try {           
	                    String url = ae.getActionCommand().toLowerCase();           
	                    if (url.startsWith("http://"))             
	                         url = url.substring(7);           
	                    ep.setPage("http://" + IDN.toASCII(url));         
	               } catch (Exception e) {           
	                    e.printStackTrace();
	                    JOptionPane.showMessageDialog(BrowserPanel.this, "Browser problem: " + e.getMessage());         
	               }       
	          }     
	     };  
	     
	     txtURL.addActionListener(al);      

	}

	// add the north component
	private void addNorth() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(4, 4));

		panel.add(new JLabel("URL: "), BorderLayout.WEST);
		panel.add(txtURL, BorderLayout.CENTER);

		add(panel, BorderLayout.NORTH);
	}
	
	//add the center component
	public void addCenter() {
		ep.addHyperlinkListener(this);
		add(ep, BorderLayout.CENTER);
	}
	
	//add the south component
	public void addSouth() {
		add(lblStatus, BorderLayout.SOUTH);
	}

	/**
	 * main program for testing
	 * @param arg ignored
	 */
	public static void main(String[] arg) {
		BrowserPanel bp = new BrowserPanel();
		
		// now make the frame to display
		JFrame testFrame = new JFrame("Browser");

		testFrame.setLayout(new BorderLayout(8, 8));
		testFrame.add(bp, BorderLayout.CENTER);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent hle) {
	     HyperlinkEvent.EventType evtype = hle.getEventType();     
	     if (evtype == HyperlinkEvent.EventType.ENTERED)                 
	          lblStatus.setText(hle.getURL().toString());     
	     else if (evtype == HyperlinkEvent.EventType.EXITED)                
	          lblStatus.setText(" ");   
	}
}
