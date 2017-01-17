package cnuphys.ced.clasio;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEvent;
import org.jlab.io.ui.BankEntryMasks;
import org.jlab.io.ui.DataBankPanel;
import org.jlab.io.base.DataBank;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.FileUtilities;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.datatable.BankDataTable;
import cnuphys.ced.frame.Ced;
import cnuphys.ced.properties.PropertiesManager;

public class ClasIoBankDialog extends JDialog implements ItemListener {

	// the event manager
	private static ClasIoEventManager _eventManager = ClasIoEventManager.getInstance();

	// visibility hashtable
	private static Hashtable<String, JCheckBox[]> _visHash = new Hashtable<String, JCheckBox[]>();

	// bank name
	private String _bankName;

	// the panel from Gagik
//	private DataBankPanel _dataBankPanel;
	
	//table to hold the data
	private BankDataTable _table;

	private JPanel _checkboxPanel;

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

	private BankEntryMasks getMask(String bankName) {
		BankEntryMasks mask = null;
		String columns[] = colNames(bankName);

		if ((columns != null) && (columns.length > 0)) {
			mask = new BankEntryMasks();

			// get vis array
			// visibility already hashed?
			JCheckBox cb[] = _visHash.get(bankName);

			String maskString = "";
//			boolean first = true;

			for (int i = 0; i < columns.length; i++) {
				if (cb[i].isSelected()) {
//					if (first) {
//						first = false;
//					} else {
//						maskString += ":";
//					}
					maskString += columns[i];
					maskString += ":";
				}
			}

//			System.err.println(bankName + "," + maskString);
			mask.setMask(bankName, maskString);

			PropertiesManager.getInstance().putAndWrite(bankName, maskString);
		} // have columns

		return mask;
	}

	public void update() {
		DataEvent event = _eventManager.getCurrentEvent();
		if (event == null) {
			return;
		}
		
		_table.setEvent(event);
		
//		// Gagik's data panel
//		DataBank db = _eventManager.getCurrentEvent().getBank(_bankName);
//
//		if (db == null) {
//			setVisible(false);
//			return;
//		}
//
//		BankEntryMasks mask = getMask(_bankName);
//		try {
//			if (mask == null) {
//				_dataBankPanel.setBank(db);
//			} else {
//				_dataBankPanel.setBank(db, mask);
//			}
//		} catch (Exception e) {
//			Log.getInstance().error("Exception in ClasIoBankDialog.update() " + e.getMessage());
//		}
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

			JCheckBox cbarray[] = new JCheckBox[columns.length];
			
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
				
				cbarray[i] = new JCheckBox(columns[i], selected);
				cbarray[i].addItemListener(this);
				_checkboxPanel.add(cbarray[i]);
			}
			
			//cache the checbox array
			_visHash.put(_bankName, cbarray);
		}

		add(_checkboxPanel, BorderLayout.SOUTH);

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		update();
	}


}
