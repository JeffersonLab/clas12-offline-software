package cnuphys.ced.alldata.graphics;

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jlab.io.base.DataDictionary;

import cnuphys.bCNU.graphics.component.CommonBorder;

/**
 * Puts all known banks into a scrollable list
 * @author heddle
 *
 */
public class AllBanksList extends JList<String> {
	
	private static Dimension _size = new Dimension(220, 250);

	//the scroll pane
	private JScrollPane _scrollPane;
	
	public AllBanksList(DataDictionary dict) {
		super(sorted(dict));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_scrollPane = new JScrollPane(this);
		_scrollPane.setPreferredSize(_size);
		_scrollPane.setBorder(new CommonBorder("Bank Name"));
	}
		
	//sort the known banks
	private static String[] sorted(DataDictionary dict) {
		String knownBanks[] = dict.getDescriptorList();
		Arrays.sort(knownBanks);
		return knownBanks;
	}
		
	/**
	 * Get the scroll pane
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		return _scrollPane;
	}
	
}
