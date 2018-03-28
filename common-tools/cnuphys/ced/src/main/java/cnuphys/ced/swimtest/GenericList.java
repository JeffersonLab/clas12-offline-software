package cnuphys.ced.swimtest;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Simple dialog for selection from a list
 * @author heddle
 *
 */
public class GenericList<T> extends JList {
	
	private static final long serialVersionUID = 1L;
	private DefaultListModel<T> _listModel;
	    
    //preferred size of dialog
    private Dimension _preferredSize;

    //the scroll pane
    private JScrollPane _scrollPane;
    
    public GenericList() {
    	this(ListSelectionModel.SINGLE_SELECTION, null, null);
    }
    
    /**
     * Create a selection dialog for a list of items
     * 
     * @param selectionMode the mode should be one of the relevant constants from the ListSelectionModel class
     * @param preferredSize the preferred size selection list. If null, 300x400 used.
     * @param items the list of items
     */
	public GenericList(int selectionMode, Dimension preferredSize, List<T> items) {
		
		_preferredSize = preferredSize;
		if (_preferredSize == null) {
			_preferredSize = new Dimension(300, 400);
		}
		
		//init the model
		_listModel = new DefaultListModel<>();
		if (items != null) {
			for (T item : items) {
				_listModel.addElement(item);
			}
		}
	
		setModel(_listModel);
		setSelectionMode(selectionMode);
	}
			
	/**
	 * Get the underlying list model
	 * @return the list data model
	 */
	@Override
	public DefaultListModel<T> getModel() {
		return _listModel;
	}

    /**
	 * Override to create the component that goes in the center. Usually this is
	 * the "main" component.
	 *
	 * @return the component that is placed in the center
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane(this) {
				@Override
				public Dimension getPreferredSize() {
					return (_preferredSize == null) ? super.getPreferredSize()
							: _preferredSize;
				}

			};
		}

		return _scrollPane;
	}
    
    /**
     * Create a selection dialog for a list of items
     * @param selectionMode the mode should be one of the relevant constants from the ListSelectionModel class
     * @param preferredSize the preferred size selection list. If null, 300x400 used.
     * @param items the items as a String array
     */
    public static GenericList<String> fromStringArray(int selectionMode, Dimension preferredSize, String... items) {
    	
    	ArrayList<String> list = new ArrayList<>(items.length);
    	for (String s:items) {
    		list.add(s);
    	}
    	return new GenericList<String>(selectionMode, preferredSize, list);
    }
    
 
}
