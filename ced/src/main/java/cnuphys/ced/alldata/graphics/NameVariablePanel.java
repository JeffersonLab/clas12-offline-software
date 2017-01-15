package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.ced.alldata.DataManager;

public class NameVariablePanel extends JPanel implements PropertyChangeListener {
	
	private SelectPanel _selectPanel;
	
	private JTextField _VariableNameTextField;
	
	//bound variables
	private BoundVariablePanel _bvPanel;
	
	//expressions
	private ExpressionPanel _expPanel;
	
	//expression entry
	private EntryPanel _entryPanel;
	
	/**
	 * The full panel for binding variables and creating expressions.
	 */
	public NameVariablePanel() {
		setLayout(new BorderLayout(4, 4));
		addWest();
		addEast();
		addSouth();
	}
	
	//add the panel on the south
	private void addWest() {
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout(4, 4));
		_selectPanel = new SelectPanel("Select a Variable", false);
		_selectPanel.addPropertyChangeListener(this);
		westPanel.add(_selectPanel, BorderLayout.CENTER);
		
		
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 2));
		
		JLabel label = new JLabel("Bind to name:");
		
		createVarNameTF();
		
		sp.add(label);
		sp.add(_VariableNameTextField);
		
		sp.setBorder(new CommonBorder("Bind the Variable"));
		westPanel.add(sp, BorderLayout.SOUTH);
		
		add(westPanel, BorderLayout.WEST);
	}

	//add the panel on the east
	private void addEast() {
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BorderLayout(4, 4));
		
		_bvPanel = new BoundVariablePanel();
		eastPanel.add(_bvPanel, BorderLayout.CENTER);
	
		add(eastPanel, BorderLayout.EAST);
	}
	
	//add the panel on the south
	private void addSouth() {
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout(4, 4));
		_expPanel = new ExpressionPanel(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		_entryPanel = new EntryPanel(_expPanel);
		
		southPanel.add(_entryPanel, BorderLayout.CENTER);
		southPanel.add(_expPanel, BorderLayout.EAST);
		
		add(southPanel, BorderLayout.SOUTH);
		_expPanel.getTable().addMouseListener(new MouseAdapter() {
		    @Override
			public void mousePressed(MouseEvent me) {
		        int row = _expPanel.getTable().rowAtPoint(me.getPoint());
		        if (me.getClickCount() == 2) {
		        	_entryPanel.editRow(row);
		        }
		    }
		});		
		
	}
	
	//create the variable name text field
	private void createVarNameTF() {
		_VariableNameTextField = new JTextField("", 20);
		_VariableNameTextField.setEnabled(false);
		
		KeyAdapter kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent kev) {

				if (kev.getKeyCode() == KeyEvent.VK_ENTER) {
					bindVariable();
				}
			}
		};
		
		_VariableNameTextField.addKeyListener(kl);
	}
	
	//do the actual variable binding
	private void bindVariable() {
		// get the variable name
		String vname = _VariableNameTextField.getText();
		if (vname == null) {
			return;
		}

		vname = vname.trim();
		if (vname.length() < 1) {
			return;
		}

		if (!Character.isLetter(vname.charAt(0))) {
			JOptionPane.showMessageDialog(null,
					"A valid name must start with a character.", "Invalid Name", 
					JOptionPane.INFORMATION_MESSAGE, ImageManager.cnuIcon);
			return;
		}
		
		
		//get the bank and column name
		String fn = _selectPanel.getFullColumnName();
		boolean valid = DataManager.getInstance().validColumnName(fn);
		if (!valid) {
			return;
		}
		
	//	System.err.println("Binding [" + vname + "] to [" + fn + "]");
		if (DefinitionManager.getInstance().addBinding(vname, fn)) {
			_bvPanel.getBoundVariableModel().fireTableDataChanged();
		}

	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		String prop = evt.getPropertyName();
		if ((o == _selectPanel) && prop.equals("newname")) {
			String fn = (String)(evt.getNewValue());
			boolean valid = DataManager.getInstance().validColumnName(fn);
			_VariableNameTextField.setEnabled(valid);
		}	

	}

	
	/**
	 * Main program for testing
	 * @param arg command arguments (ignored)
	 */
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

}
