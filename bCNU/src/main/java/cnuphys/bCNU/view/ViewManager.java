package cnuphys.bCNU.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import cnuphys.bCNU.log.Log;

/**
 * Manages all the views, or internal frames.
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ViewManager extends Vector<BaseView>
	implements InternalFrameListener {

    // singleton instance
    private static ViewManager instance;

    // the view menu
    private JMenu _viewMenu;
    
    // the plugin view menu
    private JMenu _pluginMenu;

    // List of view change listeners
    private EventListenerList _listenerList;

    // Virtual view is present
    private VirtualView _virtualView;

    /**
     * Private constructor used to create singleton.
     */
    private ViewManager() {
	_viewMenu = new JMenu("Views");
	_pluginMenu = new JMenu("Plugins");
    }

    /**
     * Add (register) a view for control by this manager.
     * 
     * @param view the View to add.
     * @return <code>true</code>, per Collection guidelines.
     */
    @Override
    public boolean add(final BaseView view) {
	boolean result = super.add(view);
	notifyListeners(view, true);

	JMenuItem mi = new JMenuItem(view.getTitle());
	ActionListener al = new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (view.isIcon()) {
		    try {
			view.setIcon(false);
		    } catch (PropertyVetoException e1) {
		    }
		}
		view.setVisible(true);
		view.toFront();

		if (!(view instanceof VirtualView)) {
		    makeViewVisibleInVirtualWorld(view);
		}
	    }

	};
	mi.addActionListener(al);
	if (view instanceof PluginView) {
	    _pluginMenu.add(mi);
	}
	else {
	    _viewMenu.add(mi);
	}

	view.addInternalFrameListener(this);
	Log.getInstance().config("ViewManager: added view: " + view.getTitle());

	if (view instanceof VirtualView) {
	    _virtualView = (VirtualView) view;
	}
	return result;
    }

    /**
     * Removes (unregisters) a view.
     * 
     * @param view the View to remove.
     * @return <code>true</code> if this ViewManager contained the specified
     *         view.
     */
    public boolean remove(BaseView view) {
	if (view != null) {
	    view.removeInternalFrameListener(this);
	    Log.getInstance()
		    .config("ViewManager: removed view: " + view.getTitle());

	    notifyListeners(view, false);
	    return super.remove(view);
	}
	return false;
    }

    /**
     * Refresh all the views with containers.
     */
    public void refreshAllContainerViews() {
	for (BaseView view : this) {
	    if (view.getContainer() != null) {
		view.getContainer().refresh();
	    }
	}
    }

    public void makeViewVisibleInVirtualWorld(BaseView view) {
	if ((_virtualView != null) && (_virtualView != view)) {
	    _virtualView.activateViewCell(view);
	}
    }

    /**
     * Obtain the singleton.
     * 
     * @return the singleton ViewManager object.
     */
    public static ViewManager getInstance() {
	if (instance == null) {
	    instance = new ViewManager();
	}
	return instance;
    }

    /**
     * The internal frame has been activated.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameActivated(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame has been closed.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameClosed(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame is closing.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameClosing(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame has been deactivated.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameDeactivated(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame has been deiconified.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameDeiconified(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame has been iconified.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameIconified(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
    }

    /**
     * The internal frame has been opened.
     * 
     * @param ife the causal event.
     */
    @Override
    public void internalFrameOpened(InternalFrameEvent ife) {
	BaseView view = (BaseView) (ife.getSource());
   }

    /**
     * Gets the view menu whose state is maintained by the ViewManager.
     * 
     * @return the view menu.
     */
    public JMenu getViewMenu() {
	return _viewMenu;
    }

    /**
     * Gets the plugin menu whose state is maintained by the ViewManager.
     * 
     * @return the plugin menu.
     */
    public JMenu getPluginMenu() {
	return _pluginMenu;
    }

    // notify listeners of a change in the views
    private void notifyListeners(BaseView view, boolean added) {

	if (_listenerList == null) {
	    return;
	}

	// Guaranteed to return a non-null array
	Object[] listeners = _listenerList.getListenerList();

	// This weird loop is the bullet proof way of notifying all listeners.
	for (int i = 0; i < listeners.length; i += 2) {
	    if (listeners[i] == IViewListener.class) {
		IViewListener listener = (IViewListener) listeners[i + 1];
		if (added) {
		    listener.viewAdded(view);
		}
		else {
		    listener.viewRemoved(view);
		}
	    }

	}
    }

    /**
     * Add a data change listener
     * 
     * @param listener the listener to add
     */
    public void addViewListener(IViewListener listener) {

	if (_listenerList == null) {
	    _listenerList = new EventListenerList();
	}

	// avoid adding duplicates
	_listenerList.remove(IViewListener.class, listener);
	_listenerList.add(IViewListener.class, listener);
    }

    /**
     * Remove a ViewListener.
     * 
     * @param listener the listener to remove.
     */

    public void removeViewListener(IViewListener listener) {

	if ((listener == null) || (_listenerList == null)) {
	    return;
	}

	_listenerList.remove(IViewListener.class, listener);
    }

}
