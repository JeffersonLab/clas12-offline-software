package cnuphys.ced.event.data;

import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioFactory;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class BankList extends JList<String> {
	
	private static Dimension _size = new Dimension(220, 250);

	private static EvioDataDictionary _dataDict = EvioFactory.getDictionary();

	//the scroll pane
	private JScrollPane _scrollPane;
	
	public BankList() {
		super(sorted());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_scrollPane = new JScrollPane(this);
		_scrollPane.setPreferredSize(_size);
		_scrollPane.setBorder(new CommonBorder("Bank Name"));
	}
		
	private static String[] sorted() {
		String knownBanks[] = _dataDict.getDescriptorList();
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
