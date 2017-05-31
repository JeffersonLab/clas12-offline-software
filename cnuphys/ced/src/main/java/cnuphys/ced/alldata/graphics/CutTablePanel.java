package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.splot.plot.PlotCanvas;
import cnuphys.splot.plot.PlotParameters;

public class CutTablePanel extends JPanel implements ActionListener, ListSelectionListener, TableModelListener {

	//parent plot
	private PlotDialog _plotDialog;
	
	//buttons
	private JButton _plus;
	private JButton _minus;
	
	private JTextArea _textArea;
	private JTextArea _warningText;
	
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
		JPanel sp = new JPanel();
		sp.setLayout(new GridLayout(2, 1, 4, 0));
		
		_textArea = new JTextArea(" ", 4, 4);
		_textArea.setLineWrap(true);

		_textArea.setFont(Fonts.commonFont(Font.PLAIN, 11));
		_textArea.setEditable(false);
		_textArea.setBackground(Color.black);
		_textArea.setForeground(Color.cyan);
		
		_warningText = new JTextArea(" ", 4, 4);
		_warningText.setLineWrap(true);

		_warningText.setFont(Fonts.commonFont(Font.BOLD, 11));
		_warningText.setEditable(false);
		_warningText.setBackground(new Color(240, 240, 240));
		_warningText.setForeground(Color.red);
		_warningText.setText("Note: adding, removing,\nactivating, or deactivating\ncuts will result in all\ndata being cleared.");
		_warningText.setBorder(new CommonBorder("Warning"));
		
		sp.add(_textArea);
		sp.add(_warningText);
		
		p.add(sp, BorderLayout.SOUTH);
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
		getTable().getModel().addTableModelListener(this);
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
			addCut(rangeCut);
		}
	}

	/**
	 * Add a cut
	 * @param cut the cut to add
	 */
	public void addCut(ICut cut) {
		if (cut != null) {
			getModel().add(cut);
			getModel().fireTableDataChanged();
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
				_textArea.setText(cut.toString());
			}
		}
	}
	
	/**
	 * Get all the defined cuts, active or not
	 * @return all the cuts
	 */
	protected Vector<ICut> getCuts() {
		return getModel()._data;
	}
	
	/**
	 * Clear the data from the underlying plot
	 */
	public void clearPlotData() {
		PlotCanvas canvas = _plotDialog.getCanvas();
		if ((canvas != null) && (canvas.getDataSet() != null)) {
			canvas.getDataSet().clear();
		}
	}

	/**
	 * Fix the plot to reflect the active plot strings
	 */
	public void fixStrings() {
		PlotParameters params = _plotDialog.getParameters(); 
		String cs[] = null;
		
		Vector<ICut> cuts = getCuts();
		if (cuts != null) {
			int activeCount = 0;
			for (ICut cut : cuts) {
				if (cut.isActive()) {
					activeCount++;
				}
			}
			
			System.err.println("FIX STR Active Count: " + activeCount);
			
			if (activeCount > 0) {
				cs = new String[activeCount];
				int idx = 0;
				for (ICut cut : cuts) {
					if (cut.isActive()) {
						cs[idx] = cut.plotText();
						idx++;
					}
				}
				
			}
		}
		
		params.setExtraStrings(cs);
		PlotCanvas canvas = _plotDialog.getCanvas();
		if ((canvas != null) && (canvas.getDataSet() != null)) {
			canvas.needsRedraw(false);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		System.err.println("Table changed");
		clearPlotData();
		
		fixStrings();
	}
}
