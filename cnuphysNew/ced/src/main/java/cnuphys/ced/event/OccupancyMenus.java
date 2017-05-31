package cnuphys.ced.event;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class OccupancyMenus {

	private static JMenu _dcMenu;

	public static JMenu getDCMenu() {
		if (_dcMenu != null) {
			return _dcMenu;
		}

		_dcMenu = new JMenu("DC Occupancy");
		JMenu sectorMenus[] = addMenus("Sector", _dcMenu, 6);

		for (JMenu sectMenu : sectorMenus) {
			JMenu suplMenus[] = addMenus("Super Layer", sectMenu, 6);
			for (JMenu suplMenu : suplMenus) {
				JMenuItem layItems[] = addMenuItems("Layer", suplMenu, 6);
			}
		}

		return _dcMenu;
	}

	private static JMenu[] addMenus(String name, JMenu parent, int num) {
		JMenu menus[] = new JMenu[num];
		for (int i = 1; i <= num; i++) {
			menus[i - 1] = new JMenu(name + " " + i);
			parent.add(menus[i - 1]);
		}
		return menus;
	}

	private static JMenuItem[] addMenuItems(String name, JMenu parent, int num) {
		JMenuItem items[] = new JMenuItem[num];
		for (int i = 1; i <= num; i++) {
			items[i - 1] = new JMenuItem(name + " " + i);
			parent.add(items[i - 1]);
		}
		return items;
	}

}
