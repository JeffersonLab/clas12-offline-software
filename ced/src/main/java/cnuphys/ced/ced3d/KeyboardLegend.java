package cnuphys.ced.ced3d;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import cnuphys.bCNU.component.KeyboardLabel;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.util.Fonts;
import cnuphys.bCNU.util.UnicodeSupport;


public class KeyboardLegend extends JPanel {

    public KeyboardLegend() {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	add("Move in/out", UnicodeSupport.UARROW, UnicodeSupport.DARROW);
	add("Move left or right", "L", "R");
	add("Move up or down", "U", "D");
	add("Make [x,y,z] out", "1", "2", "3");
	add("Rotate " + UnicodeSupport.PLUSMINUS + " (original) x", "x", "X");
	add("Rotate " + UnicodeSupport.PLUSMINUS + " (original) y", "y", "Y");
	add("Rotate " + UnicodeSupport.PLUSMINUS + " (original) z", "z", "Z");
	add("Rotate " + UnicodeSupport.PLUSMINUS + " (original) z", UnicodeSupport.LARROW, UnicodeSupport.RARROW);
	add("Drag: free rotation");
	add("Alter free rotation", "Shift", "Ctl");
	add("Accelerates most actions", "Shift");
	
	setBorder(new CommonBorder("Keyboard Actions"));
	validate();
    }
    

 //   KeyboardLabel(String explanation, Font font, Color bg, Color fg, String ...keys)    
    private void add(String explantion, String... keys) {
	KeyboardLabel kbl = new KeyboardLabel(explantion, Fonts.tweenFont, Color.black, keys);
	kbl.setAlignmentX(Component.LEFT_ALIGNMENT);
	add(kbl);
    }
}
