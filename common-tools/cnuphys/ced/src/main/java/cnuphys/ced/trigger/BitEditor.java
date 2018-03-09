package cnuphys.ced.trigger;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cnuphys.bCNU.graphics.component.CommonBorder;

public class BitEditor extends JPanel {

	private TriggerPanel _trigPanel;
	
	public BitEditor() {
		setLayout(new BorderLayout(0, 6));
		
		_trigPanel = new TriggerPanel(false, true);
		
		add(new JLabel("Click to create a filter pattern"), BorderLayout.NORTH);
		add(_trigPanel, BorderLayout.CENTER);
		
		setBorder(new CommonBorder("Filter applies to the first trigger word"));
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 1, def.left+4, def.bottom+4,
				def.right+4);
	}

}
