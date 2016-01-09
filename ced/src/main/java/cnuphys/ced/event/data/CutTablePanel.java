package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;

public class CutTablePanel extends JPanel implements ActionListener, ListSelectionListener {

	//parent plot
	private PlotDialog _plotDialog;
	
	//buttons
	private JButton _plus;
	private JButton _minus;
	
	private JTextArea _textArea;
	
	private CutTableScrollPane _cutPane;
	
	public CutTablePanel(PlotDialog plotDialog) {
		_plotDialog = plotDialog;
		
		setLayout(new BorderLayout(4,4));
		addNorth();
	}
	
	private void addNorth(JPanel p) {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		_plus = new JButton(" add ");
		_minus = new JButton("remove");
		_plus.addActionListener(this);
		_minus.addActionListener(this);
		
		_plus.setFont(Fonts.mediumFont);
		_minus.setFont(Fonts.mediumFont);
		_minus.setEnabled(false);
		sp.add(_minus);
		sp.add(_plus);
		sp.setBorder(BorderFactory.createEtchedBorder());
		p.add(sp, BorderLayout.NORTH);
	}
	
	private void addSouth(JPanel p) {
		_textArea = new JTextArea(" ", 4, 4);
		_textArea.setLineWrap(true);

		_textArea.setFont(Fonts.commonFont(Font.PLAIN, 12));
		_textArea.setEditable(false);
		_textArea.setBackground(Color.black);
		_textArea.setForeground(Color.cyan);
		p.add(_textArea, BorderLayout.SOUTH);
	}
	
	private void addNorth() {
		
		JPanel sp = new JPanel();
		sp.setLayout(new BorderLayout(0,0));
		
		_cutPane = new CutTableScrollPane(null, "Cuts");
		_cutPane.setBorder(new CommonBorder("cuts"));
		sp.add(_cutPane, BorderLayout.CENTER);
		addNorth(sp);
		addSouth(sp);
		
		add(sp, BorderLayout.NORTH);
		
		getTable().getSelectionModel().addListSelectionListener(this);
	}
	
	public CutTable getTable() {
		return _cutPane.getCutTable();
	}
	
	public CutTableModel getModel() {
		return _cutPane.getCutTableModel();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _plus) {
			addCut();
		}
		else if (o == _minus) {
			removeCut();
		}
	}
	
	private void addCut() {
		DefineRangeCutDialog dialog = new DefineRangeCutDialog();
		dialog.setVisible(true);
		int reason = dialog.getReason();
		if (reason == DialogUtilities.OK_RESPONSE) {
			RangeCut rangeCut = dialog.getRangeCut();
			if (rangeCut != null) {
				System.err.println("ADD ICIT");
				getModel().add(rangeCut);
				getModel().fireTableDataChanged();
			}
		}
	}
	
	private void removeCut() {
		int row = getTable().getSelectedRow();
		if (row >= 0) {
			getModel().removeRow(row);
			getModel().fireTableDataChanged();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int row = getTable().getSelectedRow();
			_minus.setEnabled(row >= 0);
			
			if (row < 0) {
				_textArea.setText("");
			}
			else {
				ICut cut = getModel().getCutAtRow(row);
				System.err.println("Selected row " + cut.getName());
				_textArea.setText(cut.toString());
			}
		}
	}
}
