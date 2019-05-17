package cnuphys.cnf.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.ImageManager;
import cnuphys.cnf.frame.Def;
import cnuphys.splot.plot.GraphicsUtilities;

public class ColumnsDialog extends JDialog {
	
	private SelectColumnsPanel _columnsPanel;

	public ColumnsDialog(String title) {
		super(Def.getInstance(), title, true);
		
		setLayout(new BorderLayout(4, 4));
		setup();
		setIconImage(ImageManager.cnuIcon.getImage());

		// close is like a cancel
		WindowAdapter wa = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				setVisible(false);
			}
		};
		addWindowListener(wa);

		pack();
		GraphicsUtilities.centerComponent(this);
	}
	
	
	private void setup() {
		_columnsPanel = new SelectColumnsPanel("Choose a single bank, then multiple columns");
		add(_columnsPanel, BorderLayout.CENTER);
		
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
		
		JButton cbutton = new JButton("Close");
		//use lambda for action
		cbutton.addActionListener(e -> setVisible(false));

		sp.add(cbutton);
		add(sp, BorderLayout.SOUTH);
		
	}

}
