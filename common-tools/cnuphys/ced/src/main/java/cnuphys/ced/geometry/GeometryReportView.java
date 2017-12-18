package cnuphys.ced.geometry;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jlab.geom.prim.Line3D;

import cnuphys.bCNU.graphics.GraphicsUtilities;
import cnuphys.bCNU.util.PropertySupport;
import cnuphys.bCNU.view.BaseView;
import cnuphys.ced.geometry.GeometryReportPane.MCOLOR;

public class GeometryReportView extends BaseView implements ActionListener {
	
	private static MCOLOR blacks[] = {MCOLOR.M_BLACK, MCOLOR.M_BLACK2};


	//the text pane
	private GeometryReportPane _reportPane;
	
	//report buttons
	private JButton _clearButton;
	private JButton _bstButton;
	private JButton _dcButton;

	public GeometryReportView() {
		super(PropertySupport.TITLE, "Geometry Report", PropertySupport.ICONIFIABLE, true,
				PropertySupport.MAXIMIZABLE, true, PropertySupport.CLOSABLE,
				true, PropertySupport.RESIZABLE, true, PropertySupport.WIDTH,
				900, PropertySupport.HEIGHT, 900, PropertySupport.VISIBLE,
				false);
		_reportPane = new GeometryReportPane();
		add(_reportPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
		
		//add the buttons
		_clearButton = addButton(panel, " Clear ");
		_bstButton = addButton(panel, " BST ");
		_dcButton = addButton(panel, " DC ");

		add(panel, BorderLayout.SOUTH);
	
	}
	
	//add a button
	private JButton addButton(JPanel p, String label) {
		JButton b = new JButton(label);
		GraphicsUtilities.setSizeSmall(b);
		b.addActionListener(this);
		p.add(b);
		return b;
	}
	
	public void print(MCOLOR mcolor, String message) {
		_reportPane.print(mcolor, message);
	}
	
	public void println(MCOLOR mcolor, String message) {
		_reportPane.println(mcolor, message);
	}
	
	public void header(String message) {
		_reportPane.println(MCOLOR.M_BLUE, message);
	}

	public void subheader(String message) {
		_reportPane.println(MCOLOR.M_ORANGERED, message);
	}

	
	public void subsubheader(String message) {
		_reportPane.println(MCOLOR.M_GREEN, message);
	}

	public void subsubsubheader(String message) {
		_reportPane.println(MCOLOR.M_CORAL, message);
	}

	public void clear() {
		_reportPane.clear();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == _clearButton) {
			clear();
		}
		else if (o == _bstButton) {
			bstReport();
		}
		else if (o == _dcButton) {
			dcReport();
		}
	}
	
	//layer 1..8
	private int numSect(int layer) {
		int supl = (layer-1) / 2;
		return BSTGeometry.sectorsPerSuperlayer[supl];
	}

	//report dc geometry
	private void dcReport() {
		clear();
		header("DC Geometry Wire Endpoints and Length\n");
		
		for (int sect = 1; sect <= 6; sect++) {
			subheader("\nSector " + sect);
			for (int superlay = 1; superlay <= 6; superlay++) {
				subsubheader("\nSector " + sect + "   Superlayer " + superlay );
				for (int lay = 1; lay <= 6; lay++) {
					subsubsubheader("\nSector " + sect + "   Superlayer " + superlay  + "   Layer " + lay );
					
					println(MCOLOR.M_RED, "  wire      x1         y1          z1          x2          y2          z2          length");

					for (int wire = 1; wire <= 112; wire++) {
						Line3D line3D = DCGeometry.getWire(sect, superlay, lay, wire);
						double x1 = line3D.origin().x();
						double y1 = line3D.origin().y();
						double z1 = line3D.origin().z();
						double x2 = line3D.end().x();
						double y2 = line3D.end().y();
						double z2 = line3D.end().z();
						double len = line3D.length();

						String s = String.format("  %3d  %10.5f  %10.5f  %10.5f  %10.5f  %10.5f  %10.5f  %10.5f", wire, x1, y1, z1, x2, y2, z2, len);
						println(blacks[wire%2], s);
					}
				}
			}
		}
	}
	
	//report bst geometry
	private void bstReport() {
		clear();
		header("BST Geometry\n");
		
		println(MCOLOR.M_BLACK, "layer\t|\t#sectors");
		println(MCOLOR.M_BLACK, "---------------------------------");
		for (int layer = 1; layer <= 8; layer++) {
			println(MCOLOR.M_BLACK, "   " + layer + "\t|\t"+ numSect(layer));
		}
		println(MCOLOR.M_BLACK, "---------------------------------\n");
		header("Strip Endpoints\n");
		
		for (int layer = 1; layer <= 8; layer++) {
			subheader("\nLayer " + layer);
			int numsect = numSect(layer);
			for (int sect = 1; sect <= numsect; sect++) {
				subsubheader("\nLayer " + layer + "  Sector " + sect);
				
				int supl = (layer-1) / 2; //0..3
				int lay0 = (layer-1) % 2;  //0,1

				
				println(MCOLOR.M_RED, " strip      x1         y1          z1          x2          y2          z2");
				
				for (int strip = 1; strip <= 256; strip++) {
					Line3D line3D = BSTGeometry.getStrip(sect-1, supl, lay0, strip-1);
					double x1 = line3D.origin().x();
					double y1 = line3D.origin().y();
					double z1 = line3D.origin().z();
					double x2 = line3D.end().x();
					double y2 = line3D.end().y();
					double z2 = line3D.end().z();
					String s = String.format("  %3d  %10.5f  %10.5f  %10.5f  %10.5f  %10.5f  %10.5f ", strip, x1, y1, z1, x2, y2, z2);
					println(blacks[strip%2], s);
				}
			}
			
		}		
		
	}

}
