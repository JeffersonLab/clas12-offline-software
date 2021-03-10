package cnuphys.ced.clasio;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.datatable.BankDataTable;
import cnuphys.ced.clasio.table.NamedLabel;
import cnuphys.ced.properties.PropertiesManager;

public class ClasIoBankView extends BaseView implements ItemListener, ActionListener {
	
	// the event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// counter
	private static int count = 0;
	
	// bank name
	private String _bankName;
	
	// table to hold the data
	private BankDataTable _table;

	// check boxes
	private JPanel _checkboxPanel;
	private JCheckBox _cbarray[];
	
	// set true when constructor finished
	private boolean _isReady;

	/**
	 * A label for displaying the ordinal number of the event from an event file.
	 */
	private NamedLabel seqEventNumberLabel;
	
	/**
	 * A label for displaying the true number of the event from the RUN::config bank.
	 */
	private NamedLabel trueEventNumberLabel;


	/** A button for selecting "next" event. */
	protected JButton nextButton;

	/** A button for selecting "previous" event. */
	protected JButton prevButton;


	
	public ClasIoBankView(String bankName) {
		
		super(PropertySupport.TITLE, bankName, 
				PropertySupport.ICONIFIABLE, true, 
				PropertySupport.MAXIMIZABLE, true, 
				PropertySupport.CLOSABLE, true, 
				PropertySupport.RESIZABLE, true, 
				PropertySupport.LEFT, 40 + (count % 5) * 10 + (count / 5) * 40,
				PropertySupport.TOP, 40 + (count % 5) * 30,
				PropertySupport.VISIBLE, true, 
				PropertySupport.TOOLBAR, false);
		
		count++;

		_bankName = bankName;
		
		this.getContentPane().removeAll();
		setLayout(new BorderLayout(2, 2));

		addNorth();
		addCenter();
		addSouth();
		
		readVisibility();

		pack();

		_isReady = true;
	}

	private void addNorth() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
		
		
		nextButton = new JButton("next");
		nextButton.setFont(Fonts.smallFont);
		nextButton.addActionListener(this);

		prevButton = new JButton("prev");
		prevButton.setFont(Fonts.smallFont);
		prevButton.addActionListener(this);
		
		seqEventNumberLabel = new NamedLabel("seq #", "true #", 65);
		trueEventNumberLabel = new NamedLabel("true #", "true #", 65);

		panel.add(nextButton);
		panel.add(prevButton);
		panel.add(seqEventNumberLabel);
		panel.add(trueEventNumberLabel);
		
		add(panel, BorderLayout.NORTH);
	}
	

	private void addCenter() {
		// add the table
		_table = new BankDataTable(_bankName);
		add(_table.getScrollPane(), BorderLayout.CENTER);

	}
	
	private void addSouth() {
		// add the visibility checkbox panel
		_checkboxPanel = new JPanel();
		_checkboxPanel.setBorder(new CommonBorder("Visibility"));
		_checkboxPanel.setLayout(new GridLayout(5, 10, 4, 4));

		// now the checkboxes
		String columns[] = colNames(_bankName);

		if ((columns != null) && (columns.length > 0)) {

			_cbarray = new JCheckBox[columns.length];

			// get the mask (if there is one) from the user preferences (persistance)
			String maskName = PropertiesManager.getInstance().get(_bankName);
			String tokens[] = null;
			if (maskName != null) {
				tokens = FileUtilities.tokens(maskName, ":");
			}

			for (int i = 0; i < columns.length; i++) {

				// the checkbox is selected if there is no mask or, if there is a mask,
				// if the column name is one of the tokens generated from the mask
				boolean selected = false;
				if ((tokens == null) || (tokens.length < 1)) {
					selected = true;
				} else {
					for (String token : tokens) {
						if (columns[i].equals(token)) {
							selected = true;
							break;
						}
					}
				}

				_cbarray[i] = new JCheckBox(columns[i], selected);
				_cbarray[i].setFont(Fonts.tweenFont);
				_cbarray[i].addItemListener(this);
				_checkboxPanel.add(_cbarray[i]);
			}

		}

		add(_checkboxPanel, BorderLayout.SOUTH);
	}

	/**
	 * Set the list to the columns of the given bank
	 * 
	 * @param bankName the name of the bank
	 */
	public String[] colNames(String bankName) {
		if (bankName != null) {
			DataDescriptor dd = DataManager.getInstance().getDictionary().getDescriptor(bankName);
			if (dd != null) {
				String columns[] = dd.getEntryList();
				// Arrays.sort(columns);
				return columns;
			}
		}
		return null;
	}

	// persist the visibility selections
	private void writeVisibility() {
		long val = 1;
		for (int i = 0; i < Math.min(_cbarray.length, 63); i++) {
			int bit = i + 1; // because of index column
			if (_cbarray[i].isSelected()) {
				val = Bits.setBitAtLocation(val, bit);
			}
		}

		PropertiesManager.getInstance().putAndWrite(_bankName, "" + val);
	}

	// get visibility from properties
	private void readVisibility() {
		String vs = PropertiesManager.getInstance().get(_bankName);
		if (vs != null) {
			try {
				long val = Long.parseLong(vs);
				// System.out.println("bankName = [" + _bankName + "] val in = " +
				// Long.toBinaryString(val));

				for (int i = 0; i < Math.min(_cbarray.length, 63); i++) {
					int bit = i + 1; // because of index column
					if (!Bits.checkBitAtLocation(val, bit)) {
						_cbarray[i].setSelected(false);
						TableColumn column = _table.getColumnModel().getColumn(bit);
						column.setMinWidth(0);
						column.setMaxWidth(0);
						column.setResizable(false);
//						column.setWidth(0);
						column.setPreferredWidth(0);
					} else {
						_cbarray[i].setSelected(true);
					}
				}
			} catch (Exception e) {
				Log.getInstance().warning("ClasIoBankDialog could not parse: [" + vs + "]");
			}
		}
	}  //readvis
	
	
	/**
	 * Update the table with the new event
	 */
	public void update() {
		DataEvent event = _eventManager.getCurrentEvent();
		if (event == null) {
			return;
		}

		_table.setEvent(event);
		revalidate();
		fixButtons();
	}

	/**
	 * Empty table
	 */
	public void clear() {
		_table.setEvent(null);
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox cb = (JCheckBox) (e.getSource());
		int index = -1;

		for (int i = 0; i < _cbarray.length; i++) {
			if (cb == _cbarray[i]) {
				index = i;
				break;
			}
		}

		if (index >= 0) {
			// plus 1 for row column
			TableColumn column = _table.getColumnModel().getColumn(index + 1);
			if (cb.isSelected()) {
				column.setMinWidth(20);
				column.setMaxWidth(500);
				column.setPreferredWidth(BankDataTable.COLWIDTH);
				column.setResizable(true);
			} else {
				column.setMinWidth(0);
				column.setMaxWidth(0);
				column.setPreferredWidth(0);
				column.setResizable(false);
			}
			_table.revalidate();
			writeVisibility();
		}
	}
	
	/**
	 * Set the selectability of the buttons
	 */
	public void fixButtons() {
		if (!_isReady) {
			return;
		}
		nextButton.setEnabled(_eventManager.isNextOK());
		prevButton.setEnabled(_eventManager.isPrevOK());
		setSeqEventNumber();
		setTrueEventNumber();
	}
	

	/**
	 * Set the displayed sequential event number value.
	 * The sequential event number is just the number in the file.
	 * @param seqEventNum event number.
	 */
	public void setSeqEventNumber() {
		int seqEventNum = _eventManager.getSequentialEventNumber();
		if (seqEventNum > -1) {
			seqEventNumberLabel.setText("" + seqEventNum);
		}
	}
	
	/**
	 * Set the displayed true event number value.
	 * The true event number comes from the RUN::config bank.
	 * @param trueEventNum event number.
	 */
	public void setTrueEventNumber() {
		int trueEventNum = _eventManager.getTrueEventNumber();

		if (trueEventNum > -1) {
			trueEventNumberLabel.setText("" + trueEventNum);
		}
		else {
			trueEventNumberLabel.setText("n/a");
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == nextButton) {
			_eventManager.getNextEvent();
		} else if (source == prevButton) {
			_eventManager.getPreviousEvent();
		}
	}

}
