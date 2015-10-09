package cnuphys.ced.clasio.tojava;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.xml.sax.SAXException;

public class ToJava extends JFrame {

	private JFileChooser chooser;
	String dataPath = System.getProperty("user.home")
			+ "/coatJava/etc/bankdefs/clas12";

	private JTextArea textArea;

	public ToJava() {
		super("clas-io XML to Java");

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};

		addWindowListener(windowAdapter);

		setLayout(new BorderLayout());

		textArea = new JTextArea();
		JScrollPane sp = new JScrollPane(textArea);

		add(sp, BorderLayout.CENTER);

		addMenus();
	}

	private void addMenus() {
		JMenuBar mbar = new JMenuBar();
		setJMenuBar(mbar);
		JMenu fileMenu = new JMenu("File");

		final JMenuItem qItem = new JMenuItem("Quit");
		final JMenuItem oItem = new JMenuItem("Open...");

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = e.getSource();

				if (o == qItem) {
					System.exit(0);
				} else if (o == oItem) {
					parse();
				}

			}

		};

		fileMenu.add(oItem);
		fileMenu.addSeparator();
		fileMenu.add(qItem);
		oItem.addActionListener(al);
		qItem.addActionListener(al);

		mbar.add(fileMenu);
	}

	private void parse() {

		textArea.setText("");
		if (chooser == null) {
			chooser = new JFileChooser(dataPath);
		}

		chooser.setSelectedFile(null);
		FileNameExtensionFilter filter;
		filter = new FileNameExtensionFilter("Clas-IO XML Files", "xml", "XML");
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				try {
					Parser.parse(textArea, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String arg[]) {
		final ToJava frame = new ToJava();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setSize(800, 800);
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});

	}
}
