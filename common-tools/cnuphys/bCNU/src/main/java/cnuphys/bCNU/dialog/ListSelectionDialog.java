package cnuphys.bCNU.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cnuphys.bCNU.graphics.ImageManager;

/**
 * Simple dialog for selection from a list
 * @author heddle
 *
 */
public class ListSelectionDialog<T> extends JDialog implements ActionListener {
	
	private static String OKSTR = "OK";
	private static String CANCELSTR = "Cancel";

	private DefaultListModel<T> _listModel;
	private JList<T> _jList;
	
    // the reason the dialog closed.
    protected int reason;
    
    // convenient access to south button panel
    protected JPanel buttonPanel;
    
    //preferred size of dialog
    private Dimension _preferredSize;
	
    /**
     * Create a selection dialog for a list of items
     * 
     * @param parent the JFrame parent. If null, Orion.getInstance() is used
     * @param title the title of the dialog
     * @param modal whether or not the dialog is modal
     * @param selectionMode the mode should be one of the relevant constants from the ListSelectionModel class
     * @param preferredSize the preferred size selection list. If null, 300x400 used.
     * @param items the list of items
     */
	public ListSelectionDialog(JFrame parent, String title, boolean modal, 
			int selectionMode, Dimension preferredSize, List<T> items) {
		
		super(parent, title, modal);
		
		_preferredSize = preferredSize;
		if (_preferredSize == null) {
			_preferredSize = new Dimension(300, 400);
		}
		
	       // close is like a close
        WindowAdapter wa = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                reason = DialogUtilities.CANCEL_RESPONSE;
                setVisible(false);
            }
        };
        addWindowListener(wa);
        setLayout(new BorderLayout(8, 8));
		
		//init the model
		_listModel = new DefaultListModel<>();
		if (items != null) {
			for (T item : items) {
				_listModel.addElement(item);
			}
		}
	
		_jList = new JList<>(_listModel);
		_jList.setSelectionMode(selectionMode);
		
        setIconImage(ImageManager.cnuIcon.getImage());
        
        //add components
        createSouthComponent(OKSTR, CANCELSTR);
        createCenterComponent();
        
       	pack();
        
        //center the dialog
        DialogUtilities.centerDialog(this);
	}
	
	/**
	 * Get the reason the dialog closed, either DialogUtilities.CANCEL_RESPONSE or
	 * DialogUtilities.OK_RESPONSE
	 * @return reason the dialog closed
	 */
	public int getReason() {
		return reason;
	}
	
	/**
	 * Get the JList component so that you can optionally
	 * add a ListSelectionListener
	 * @return the JList component
	 */
	public JList<T> getJList() {
		return _jList;
	}
	
	/**
	 * Get the underlying list model
	 * @return the list data model
	 */
	public DefaultListModel<T> getModel() {
		return _listModel;
	}
	
	
    /**
     * Override to create the component that goes in the center. Usually this is
     * the "main" component.
     *
     * @return the component that is placed in the center
     */
    protected void createCenterComponent() {
        JScrollPane spane = new JScrollPane(_jList) {
        	@Override
        	public Dimension getPreferredSize() {
        		return (_preferredSize == null) ? super.getPreferredSize() : _preferredSize;
        	}

        };
        add(spane, BorderLayout.CENTER);
    }

	
    /**
     * Override to create the component that goes in the south.
     *
     * @return the component that is placed in the south. The default
     * implementation creates a row of closeout buttons.
     */
    protected void createSouthComponent(String... closeout) {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());

        int lenm1 = closeout.length - 1;
        for (int index = 0; index <= lenm1; index++) {
            JButton button = new JButton(closeout[index]);
            button.setName("simpleDialog" + closeout[index]);
            button.setActionCommand(closeout[index]);
            button.addActionListener(this);
            buttonPanel.add(button);
            if (index != lenm1) {
                buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            }
        }
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        reason = e.getActionCommand().equals(CANCELSTR) ? DialogUtilities.CANCEL_RESPONSE : DialogUtilities.OK_RESPONSE;
        setVisible(false);
    }
    
    /**
     * Get the single selected value for a single selection list
     * or the smallest indexed selected value for a multiple selection
     * list
     * @return the selected value
     */
    public T getSelectedValue() {
    	return _jList.getSelectedValue();
    }
    
    /**
     * Get a list of  selected values, or
     * an empty list
     * @return a list of  selected values
     */
    public List<T> getSelectedListValues() {
    	return _jList.getSelectedValuesList();
    }
    
    /**
     * Create a selection dialog for a list of items
     * @param parent the JFrame parent. If null, Orion.getInstance() is used
     * @param title the title of the dialog
     * @param modal whether or not the dialog is modal
     * @param selectionMode the mode should be one of the relevant constants from the ListSelectionModel class
     * @param preferredSize the preferred size selection list. If null, 300x400 used.
     * @param items the items as a String array
     */
    public static ListSelectionDialog<String> fromStringArray(JFrame parent, String title, boolean modal, 
			int selectionMode, Dimension preferredSize, String... items) {
    	
    	ArrayList<String> list = new ArrayList<>(items.length);
    	for (String s:items) {
    		list.add(s);
    	}
    	return new ListSelectionDialog<String>(parent, title, modal, selectionMode, preferredSize, list);
    }
    
    
    public static void main(String arg[]) {
    	
    	String names[] = {"Item 1", "Item 2","Item 3","Item 4","Item 5","Item 6","Item 7","Item 8","Item 9","Item 10","Item 11",
    			"Item 12","Item 13","Item 14","Item 15","Item 16","Item 17","Item 18","Item 19","Item 20","Item 21","Item 22",
    			"Item 23","Item 24","Item 25", "abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWZYZ"};
    	
    	ArrayList<TestObject> objects = new ArrayList<>();
    	for (int i = 0; i < names.length; i++) {
    		objects.add(new TestObject(names[i]));
    	}
    	
//    	public ListSelectionDialog(JFrame parent, String title, boolean modal, 
//    			int selectionMode, Dimension preferredSize, List<T> items) {

    	ListSelectionDialog<TestObject> dialog = new ListSelectionDialog<>(null, "Sample Selection", true, ListSelectionModel.SINGLE_SELECTION,
    			new Dimension(300, 400), objects);
    	
//    	final ListSelectionDialog<TestObject> dialog =  fromStringArray(null, "Sample Selection", true, ListSelectionModel.SINGLE_SELECTION,
//    			new Dimension(300, 400), names);
    	
		// now make the frame visible, in the AWT thread
		try {
			EventQueue.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					dialog.setVisible(true);
					
					if (dialog.getReason() == DialogUtilities.OK_RESPONSE) {
						
						System.err.println("Selected[" + dialog.getSelectedValue() + "]");
					}
					else {
						System.err.println("Cancelled");
					}
				
					System.exit(1);
				}

			});
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		

    }
    
    static class TestObject {
    	String name;
    	
    	public TestObject(String name) {
    		this.name = name;
    	}
    	
    	@Override
    	public String toString() {
    		return name;
    	}
    }
 
}
