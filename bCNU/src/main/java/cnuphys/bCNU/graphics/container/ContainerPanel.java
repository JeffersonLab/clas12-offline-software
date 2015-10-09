package cnuphys.bCNU.graphics.container;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * This is for placing a container on a panel instead of a view. The conatiner's
 * component will bein the center of the panel's border layout.
 * 
 * @author heddle
 *
 */

@SuppressWarnings("serial")
public class ContainerPanel extends JPanel {

	// the container
	private IContainer _container;

	public ContainerPanel(IContainer container) {
		_container = container;

		setLayout(new BorderLayout(4, 4));
		add(container.getComponent(), BorderLayout.CENTER);
	}

	/**
	 * Get the underlying container
	 * 
	 * @return the underlying container
	 */
	public IContainer getContainer() {
		return _container;
	}
}
