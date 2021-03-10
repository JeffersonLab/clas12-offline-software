package cnuphys.ced.alldata.graphics;

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.ced.alldata.DataManager;

/**
 * Puts all known banks into a scrollable list
 * 
 * @author heddle
 *
 */
public class AllBanksList extends JList<String> {

	private static Dimension _size = new Dimension(220, 250);

	// the scroll pane
	private JScrollPane _scrollPane;

	/**
	 * Create a list that has all the known banks
	 */
	public AllBanksList() {
		super(sorted(DataManager.getInstance().getKnownBanks()));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_scrollPane = new JScrollPane(this);
		_scrollPane.setPreferredSize(_size);
		_scrollPane.setBorder(new CommonBorder("Bank Name"));
	}

	// sort the known banks
	private static String[] sorted(String knownBanks[]) {
		Arrays.sort(knownBanks);
		return knownBanks;
	}

	/**
	 * Get the scroll pane
	 * 
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

}
