package cnuphys.magfield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class TorusMenu extends JMenu implements ActionListener {
	
	private static final String baseTitle = "Torus Map: ";
	
	//singleton
	private static TorusMenu _instance;
	
	private Hashtable<JMenuItem, TorusMap> m_map = new Hashtable<JMenuItem, TorusMap>();

	private TorusMenu() {
		super(baseTitle);
		
		for (TorusMap tmap : TorusMap.values()) {
			if (tmap.foundField()) {
				addMenuItem(tmap);
			}
		}
		
	}
	
	private void addMenuItem(TorusMap tmap) {
		JMenuItem mitem = new JMenuItem(tmap.getName());
		m_map.put(mitem, tmap);
		add(mitem);
		mitem.addActionListener(this);
	}
	
	public static TorusMenu getInstance() {
		if (_instance == null) {
			_instance = new TorusMenu();
		}
		return _instance;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		TorusMap tmap = m_map.get(source);
		MagneticFields.getInstance().setTorus(tmap);
		System.out.println("set torus map to " + tmap.getName());
		fixTitle(tmap);
	}
	
	public void fixTitle(TorusMap map) {
		if (map == null) {
			setText(baseTitle + "null");
		}
		else {
			setText(baseTitle + map.getName());
		}
	}
	
}
