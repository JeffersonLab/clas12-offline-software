package cnuphys.ced.clasio.filter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;


import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.util.FileUtilities;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.alldata.graphics.AllBanksList;
import cnuphys.ced.properties.PropertiesManager;

/**
 * This class is to filter on the number of rows in a set banks
 * 
 * @author heddle
 *
 */
public class BankSizeFilter extends AEventFilter {
	
	//the key for the preferences file
	private static final String PREFKEY = "BANKSIZEFILTER";
	
	//for writing out preferences
	protected static final String SEP = "|";
	protected static final String RECSEP = "&";

	
	// list for all known banks
	private AllBanksList _blist;
	
	//for entering the range
	private BankSizeRangePanel _bsPanel;
	
	
	private Hashtable<String, BankRangeRecord> _records;

	public BankSizeFilter() {
		super(true);
		setName("Bank Size Filter");
		setActive(false);
		_records = new Hashtable<String, BankRangeRecord>();
		readPreferences();
		report();
	}

	@Override
	public boolean pass(DataEvent event) {
		Collection<BankRangeRecord> recs = _records.values();

		if ((recs != null) && !recs.isEmpty()) {
			for (BankRangeRecord brec : recs) {
				if (brec.active) {
					int rowCount = DataManager.getInstance().getRowCount(event, brec.bankName);
					if ((rowCount <= brec.minCount) || (rowCount >= brec.maxCount)) {
						return false;
					}
				}
			}
		}

		return true;
	}


	/**
	 * Get a record from the hash
	 * @param bname the name of the record
	 * @return the record, or null
	 */
	public BankRangeRecord getRecord(String bname) {
		if (bname == null) {
			return null;
		}
		
		return _records.get(bname);
	}
	
	/**
	 * Add a record to the hash
	 * @param bname the name of the record
	 * @param minCount the min count
	 * @param maxCount the max count
	 * @param active whether the record is active
	 * @return the record
	 */
	public BankRangeRecord addRecord(String bname, int minCount, int maxCount, boolean active) {
		
		if (bname == null) {
			return null;
		}

		
		_records.remove(bname);
		
		BankRangeRecord rec = new BankRangeRecord(bname, minCount, maxCount, active);
		_records.put(bname, rec);
		
		report();
		return rec;
	}
	
	//set the text in the comment area
	private void report() {
		if (_editor != null) {
			_editor.setCommentText("");
			if (!_records.isEmpty()) {
				Collection<BankRangeRecord> recs = _records.values();
				
				StringBuffer sb = new StringBuffer(1024);
				
				for (BankRangeRecord rec : recs) {
					sb.append(rec.toString() + System.lineSeparator());
				}
				
				_editor.setCommentText(sb.toString());
			}
		}
	}
	
	/**
	 * Create the filter editor 
	 * @return the filter editor
	 */
	@Override
	public AFilterDialog createEditor() {
		
		final BankSizeFilter ffilter = this;
		
		AFilterDialog editor = new AFilterDialog("Bank Size Filter Settings", this) {
			
			@Override
			protected void handleCommand(String command) {
				_bsPanel.setName(null);
				super.handleCommand(command);
			}

			
			/**
			 * Create the main component
			 * 
			 * @return the main component of the editor
			 */
			@Override
			public JComponent createMainComponent() {
				JPanel cp = new JPanel();
				_blist = new AllBanksList();
				
				
				
				ListSelectionListener lsl = new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						String bname = _blist.getSelectedValue();
						selectedBank(bname);
						report();
					}
					
				};
				
				_blist.addListSelectionListener(lsl);
				
				cp.add(_blist.getScrollPane(), BorderLayout.CENTER);
				
				
				
				_bsPanel = new BankSizeRangePanel(ffilter);
				cp.add(_bsPanel, BorderLayout.EAST);
				return cp;
			}

			
		};
		
		return editor;
	}
	
	//a bank has been selected from the list
	private void selectedBank(String bankname) {
		_bsPanel.setName(bankname);
	}

	
	/**
	 * Edit the filter
	 */
	@Override
	public void edit() {
		if (_editor == null) {
			_editor = createEditor();
		}
		
		if (_editor != null) {
			report();

			_editor.setVisible(true);
		}
	}
	
	/**
	 * Save the preferences to user pref
	 */
	@Override
	public void savePreferences() {	
		_bsPanel.setName(null);

		cull();
		
		Collection<BankRangeRecord> recs = _records.values();
		if (recs.isEmpty()) {
			PropertiesManager.getInstance().putAndWrite(PREFKEY, "NO");
			return;
		}
		
		StringBuffer sb = new StringBuffer(1024);
		
		for (BankRangeRecord rec : recs) {
			sb.append(rec.hash() + RECSEP);
		}
		
		PropertiesManager.getInstance().putAndWrite(PREFKEY, sb.toString());
	}
	
	/**
	 * Read the preferences from the user pref
	 */
	@Override
	public void readPreferences() {
		String val = PropertiesManager.getInstance().get(PREFKEY);
		if ((val == null) || (val.length() < 5)) {
			return;
		}
		
		String[] recTokens = FileUtilities.tokens(val, RECSEP);
		
		if ((recTokens != null) && (recTokens.length > 0)) {
			for (String recTok : recTokens) {

				String[] valToks = FileUtilities.tokens(recTok, SEP);
				if ((valToks != null) && (valToks.length == 4)) {
					try {
						String bname = valToks[0];
						int minCount = Integer.parseInt(valToks[1]);
						
						String maxStr = valToks[2];
						int maxCount;
						if (maxStr.toLowerCase().contains("inf")) {
							maxCount = Integer.MAX_VALUE;
						}
						else {
							maxCount = Integer.parseInt(valToks[2]);
						}
						
						boolean active = valToks[3].toLowerCase().contains("t");
						
						addRecord(bname, minCount, maxCount, active);
					}
					catch (Exception e) {
						
					}
				}
			}
		}
	}
	
	//remove records that are inactive and a 0 to inf range
	private void cull() {
		
		ArrayList<String> badKeys = new ArrayList<String>();
		
		for (String key : _records.keySet()) {
			BankRangeRecord brec = _records.get(key);
			
			if ((brec.minCount == 0) && (brec.maxCount == Integer.MAX_VALUE)) {
				badKeys.add(key);
			}
		}
		
		for (String badkey : badKeys) {
			_records.remove(badkey);
		}
	}

	
	/**
	 * A builder for a Trigger Filter
	 */
	public static class BankRangeRecord {
		
		public int minCount;
		public int maxCount;
		public String bankName;
		public boolean active;
		
		public BankRangeRecord(String bankName, int minCount, int maxCount, boolean active) {
			this.bankName = bankName;
			this.minCount = minCount;
			this.maxCount = maxCount;
			this.active = active;
		}
		
		@Override
		public String toString() {
			String maxStr = (maxCount == Integer.MAX_VALUE ? "Inf" : "" + String.format("%-4d", maxCount));
			return String.format("%-20s min: %-3d max: %-4s %s", bankName, minCount, maxStr, (active ? "active" : "not active"));
		}
		
		public String hash() {
			String maxStr = (maxCount == Integer.MAX_VALUE ? "Inf" : "" + String.format("%d", maxCount));
			return bankName + SEP + minCount + SEP + maxStr + SEP + (active ? "true" : "false");
		}

	}
	

}
