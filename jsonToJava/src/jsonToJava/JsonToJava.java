package jsonToJava;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;

public class JsonToJava extends JFrame implements ActionListener {

	//where output is written
	private JTextArea _textArea;
	
	private JMenuItem _openItem;
	private JMenuItem _clearItem;
	private JMenuItem _quitItem;
	
	private String _defDir = "../coatjava/etc/bankdefs/hipo";

	
	public JsonToJava() {
		super("Json To Java");
		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				System.exit(1);
			}
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(1);
			}
		};
		addWindowListener(windowAdapter);
		setLayout(new BorderLayout());

		_textArea = new JTextArea();
		_textArea.setFont(Fonts.smallMono);
		JScrollPane scrollPane = new JScrollPane(_textArea);
		add(scrollPane, BorderLayout.CENTER);
		
		makeMenus();
		
		setSize(GraphicsUtilities.screenFraction(0.9));
	}
	

	// make the menus
	private void makeMenus() {
		JMenuBar menubar = new JMenuBar();
		setJMenuBar(menubar);
		JMenu menu = new JMenu("File");
		_openItem = makeItem(menu, "Open...");
		_clearItem = makeItem(menu, "Clear");
		menu.addSeparator();
		_quitItem = makeItem(menu, "Quit");
     	menubar.add(menu);
	}

	private JMenuItem makeItem(JMenu menu, String label) {
		JMenuItem item = new JMenuItem(label);
		menu.add(item);
		item.addActionListener(this);
		return item;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		
		if (source == _openItem) {
			handleOpen();
		}
		else if (source == _clearItem) {
			_textArea.setText("");
		}
		else if (source == _quitItem) {
			System.exit(0);
		}
	}
	
	private void handleOpen() {
		File file = FileUtilities.openFile(_defDir, "Json File", "json");
		System.out.println("Opening " + file.getPath());
		if (file != null) {
			JsonArray root = JsonParser.parseJsonFile(file.getAbsolutePath());
			if (root != null) {
				System.out.println("found root of type " + root.getValueType());
				System.out.println("size of array: " + root.size());
				
				JsonBank banks[] = new JsonBank[root.size()];
				for (int i = 0; i < root.size(); i++) {
					JsonObject jobj = root.getJsonObject(i);
					banks[i] = JsonBank.fromJsonObject(jobj);
				}
				
				//declarations
				_textArea.setText("//Array declarations\n");
				for (JsonBank bank : banks) {
					for (JsonColumn column : bank.columns) {
						_textArea.append(column.declaration());
					}
				}
				
				_textArea.append("\n\n");
				
				//getters
				for (JsonBank bank : banks) {
					for (JsonColumn column : bank.columns) {
						_textArea.append(column.getter());
					}
				}
				
				_textArea.append("\n\n");
				
				//clear
				_textArea.append("//Nullify the arrays\n");
				_textArea.append("  public void clear() {\n");
				for (JsonBank bank : banks) {
					for (JsonColumn column : bank.columns) {
						_textArea.append(column.nullify());
					}
				}
				_textArea.append("  }\n");
				_textArea.append("\n\n");
			
				//read
				_textArea.append("//Read the arrays\n");
				_textArea.append("  public void read() {\n");
				for (JsonBank bank : banks) {
					for (JsonColumn column : bank.columns) {
						_textArea.append(column.read());
					}
				}
				_textArea.append("  }\n");

			}
		}
	}
	
	public static void main(String arg[]) {
		
		String defDir = "../coatjava/etc/bankdefs/hipo";
		File file = new File(defDir);
		System.out.println("File [" + file.getAbsolutePath() + "] exists: " + file.exists());

		final JsonToJava testFrame = new JsonToJava();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// testFrame.pack();
				testFrame.setVisible(true);
				testFrame.setLocationRelativeTo(null);
				testFrame.handleOpen();
			}
		});
	}


}
