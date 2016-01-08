package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SelectPanel extends JPanel implements ListSelectionListener {
	
	private BankList _blist;
	
	private ColumnList _clist;
	
	private JLabel _fullName;

	public SelectPanel(String label) {
		setLayout(new BorderLayout(2,4));
		addCenter();
		addNorth(label);
		_fullName = new JLabel("");
		_fullName.setOpaque(true);
		_fullName.setBackground(Color.black);
		_fullName.setForeground(Color.cyan);
		add(_fullName, BorderLayout.SOUTH);
	}
	
	protected void addNorth(String label) {
		if (label != null) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
			JLabel jlab = new JLabel(label);
			p.add(jlab);
			add(p, BorderLayout.NORTH);
		}
	}
	
	public String getSelection() {
		return _fullName.getText();
	}
	
	public void addSelectionListener(ListSelectionListener lsl) {
		_blist.addListSelectionListener(lsl);
		_clist.addListSelectionListener(lsl);
	}
	
	//add the center component
	private void addCenter() {
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 8, 8));
		_blist = new BankList();
		_clist = new ColumnList();
		
		addSelectionListener(this);
		
		p.add(_blist.getScrollPane());
		p.add(_clist.getScrollPane());
		add(p, BorderLayout.CENTER);
	}
	

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		
		if (e.getSource() == _blist) {
			_clist.setList(_blist.getSelectedValue());
		}

		String bname = _blist.getSelectedValue();
		String cname = _clist.getSelectedValue();
		
		if (cname == null) {
			_fullName.setText(null);
		}
		else {
			_fullName.setText(bname + "." + cname);	
		}
		firePropertyChange("newname", "", _fullName.getText());
	}
	
	/**
	 * Get the full name
	 * @return the full name
	 */
	public String getFullName() {
		String fn = _fullName.getText();
		return fn;
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
		ScatterPanel hp = new ScatterPanel();
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
