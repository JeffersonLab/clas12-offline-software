package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class NameVariablePanel extends JPanel implements PropertyChangeListener{
	
	private SelectPanel _selectPanel;
	
	private JTextField _textField;
	
	private BoundVariablePanel _bvPanel;
	
	public NameVariablePanel() {
		setLayout(new BorderLayout(4, 4));
		addWest();
		addEast();
	}
	
	private void addWest() {
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout(4, 4));
		_selectPanel = new SelectPanel("Select a Variable");
		_selectPanel.addPropertyChangeListener(this);
		westPanel.add(_selectPanel, BorderLayout.CENTER);
		
		
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
		
		JLabel label = new JLabel("Bind to name:");
		
		createTextField();
		
		sp.add(label);
		sp.add(_textField);
		
		sp.setBorder(new CommonBorder("Bind the Variable"));
		westPanel.add(sp, BorderLayout.SOUTH);
		
		add(westPanel, BorderLayout.WEST);
	}

	private void addEast() {
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BorderLayout(4, 4));
		
		_bvPanel = new BoundVariablePanel();
		eastPanel.add(_bvPanel, BorderLayout.CENTER);
	
		add(eastPanel, BorderLayout.EAST);
	}
	
	private void createTextField() {
		_textField = new JTextField("", 20);
		_textField.setEnabled(false);
		
		KeyAdapter kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {

				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					bindVariable();
				}
			}
		};
		
		_textField.addKeyListener(kl);
	}
	
	//do the actual variable binding
	private void bindVariable() {
		// get the variable name
		String vname = _textField.getText();
		if ((vname == null) || (vname.length() < 1)) {
			return;
		}
		
		
		//get the bank and column name
		String fn = _selectPanel.getFullName();
		boolean valid = ((fn != null) && (fn.length() > 4) && fn.contains(":") && fn.contains("."));
		if (!valid) {
			return;
		}
		
	//	System.err.println("Binding [" + vname + "] to [" + fn + "]");
		if (DefinitionManager.getInstance().addBinding(vname, fn)) {
			_bvPanel.getBoundVariableModel().fireTableDataChanged();
		}

	}
	
	public static void main(String arg[]) {
		final JFrame frame = new JFrame();

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

		frame.addWindowListener(windowAdapter);

		frame.setLayout(new BorderLayout());
		
//		HistoPanel hp = new HistoPanel();
		NameVariablePanel hp = new NameVariablePanel();
		frame.add(hp);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.pack();
				frame.setVisible(true);
				frame.setLocationRelativeTo(null);
			}
		});
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		String prop = evt.getPropertyName();
		if ((o == _selectPanel) && prop.equals("newname")) {
			String fn = (String)(evt.getNewValue());
			boolean valid = ((fn != null) && (fn.length() > 4) && fn.contains(":") && fn.contains("."));
			_textField.setEnabled(valid);
		}	

	}

}
