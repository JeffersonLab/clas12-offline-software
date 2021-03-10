package cnuphys.bCNU.component;

import java.awt.event.MouseEvent;
import java.util.EventListener;

import javax.swing.JLabel;

public interface IRollOverListener extends EventListener {

	/**
	 * A roll over label has been entered
	 * @param label the label 
	 * @param e the triggering mouse event
	 */
	public void  RollOverMouseEnter(JLabel label, MouseEvent e);
	
	/**
	 * A roll over label has been exited
	 * @param label the label 
	 * @param e the triggering mouse event
	 */
	public void  RollOverMouseExit(JLabel label, MouseEvent e);
}
