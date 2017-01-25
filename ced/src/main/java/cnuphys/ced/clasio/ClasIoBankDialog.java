package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.table.TableColumn;

import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEvent;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Bits;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.bCNU.util.Fonts;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.datatable.BankDataTable;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.properties.PropertiesManager;

public class ClasIoBankDialog extends JDialog implements ItemListener {

	// the event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// visibility hashtable
	//private static Hashtable<String, JCheckBox[]> _visHash = new Hashtable<String, JCheckBox[]>();

	// bank name
	private String _bankName;

	// the panel from Gagik
//	private DataBankPanel _dataBankPanel;
	
	//table to hold the data
	private BankDataTable _table;

	//check boxes
	private JPanel _checkboxPanel;
	private JCheckBox _cbarray[];

	// counter
	private static int count = 0;

	public ClasIoBankDialog(String bankName) {
		super(Ced.getFrame(), bankName, false);
		_bankName = bankName;
		setLayout(new BorderLayout(4, 4));
		setup();

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);

		int y = 40 + (count % 5) * 30;
		int x = 40 + (count % 5) * 10 + (count / 5) * 40;
		setLocation(x, y);
		count++;

	//	BankEntryMasks mask = getMask(_bankName);
		
		readVisibility();
		pack();
	}

	/**
	 * Set the list to the columns of the given bank
	 * 
	 * @param bankName
	 *            the name of the bank
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
	
	//persist the visibility selections
	private void writeVisibility() {
		long val = 1;
		for (int i = 0; i < Math.min(_cbarray.length, 63); i++) {
			int bit = i+1;  //because of index column
			if (_cbarray[i].isSelected()) {
				val = Bits.setBitAtLocation(val, bit);
			}
		}
		
	//	System.out.println("bankName = [" + _bankName + "] val out = " + Long.toBinaryString(val));

		PropertiesManager.getInstance().putAndWrite(_bankName, ""+val);
	}
	
	//get visibility from properties
	private void readVisibility() {
		String vs = PropertiesManager.getInstance().get(_bankName);
		if (vs != null) {
			try {
				long val = Long.parseLong(vs);
		//		System.out.println("bankName = [" + _bankName + "] val in  = " + Long.toBinaryString(val));

				for (int i = 0; i < Math.min(_cbarray.length, 63); i++) {
					int bit = i+1;  //because of index column
					if (!Bits.checkBitAtLocation(val, bit)) {
						_cbarray[i].setSelected(false);
						TableColumn column = _table.getColumnModel().getColumn(bit);
						column.setMinWidth(0);
						column.setMaxWidth(0);
						column.setResizable(false);
//						column.setWidth(0);
						column.setPreferredWidth(0);
					}
					else {
						_cbarray[i].setSelected(true);
					}
				}
			}
			catch (Exception e) {
				Log.getInstance().warning("ClasIoBankDialog could not parse: [" + vs + "]");
			}
		}
	}

	public void update() {
		DataEvent event = _eventManager.getCurrentEvent();
		if (event == null) {
			return;
		}
		
		_table.setEvent(event);
	}

	private void setup() {

		//add the table
		_table = new BankDataTable(_bankName);
		add(_table.getScrollPane(), BorderLayout.CENTER);

		// add the visibility checkbox panel
		_checkboxPanel = new JPanel();
		_checkboxPanel.setBorder(new CommonBorder("Visibility"));
		_checkboxPanel.setLayout(new GridLayout(5, 10, 4, 4));

		// now the checkboxes
		String columns[] = colNames(_bankName);

		if ((columns != null) && (columns.length > 0)) {

			_cbarray = new JCheckBox[columns.length];
			
			//get the mask (if there is one) from the user preferences (persistance)
			String maskName = PropertiesManager.getInstance().get(_bankName);
			String tokens[] = null;
			if (maskName != null) {
				tokens = FileUtilities.tokens(maskName, ":");
			}

			for (int i = 0; i < columns.length; i++) {
				
				//the checkbox is selected if there is no mask or, if there is a mask,
				//if the column name is one of the tokens generated from the mask
				boolean selected  = false;
				if ((tokens == null) || (tokens.length < 1)) {
					selected = true;
				}
				else {
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
			
			//cache the checbox array
			//_visHash.put(_bankName, cbarray);
		}

		add(_checkboxPanel, BorderLayout.SOUTH);

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox cb = (JCheckBox)(e.getSource());
		int index = -1;
		
		for (int i = 0; i < _cbarray.length; i++) {
			if (cb == _cbarray[i]) {
				index = i;
				break;
			}
		}
		
		if (index >= 0) {
			//plus 1 for row column
			TableColumn column = _table.getColumnModel().getColumn(index+1);
			if (cb.isSelected()) {
				column.setMinWidth(20);
				column.setMaxWidth(500);
//				column.setWidth(BankDataTable.COLWIDTH);
				column.setPreferredWidth(BankDataTable.COLWIDTH);
				column.setResizable(true);
			}
			else {
				column.setMinWidth(0);
				column.setMaxWidth(0);
//				column.setWidth(0);
				column.setPreferredWidth(0);
				column.setResizable(false);
			}
			_table.revalidate();
			writeVisibility();
		}
	}


}
