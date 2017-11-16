package cnuphys.magfield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;

public class TorusMenu extends JMenu implements ActionListener {
	
	private static ArrayList<Torus> tori = new ArrayList<>(10);

	public TorusMenu() {
		super("Torus");
		System.out.println(">>>>>>>>> KNOW ABOUT " + tori.size() + "  TORI");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	public static void addTorus(Torus torus) {
		if (torus != null) {
			tori.add(torus);
		}
	}
	
}
