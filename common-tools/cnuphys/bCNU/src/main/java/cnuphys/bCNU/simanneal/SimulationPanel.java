package cnuphys.bCNU.simanneal;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import cnuphys.bCNU.attributes.AttributePanel;

public class SimulationPanel extends JPanel {
	
	//the underlying simulation
	private Simulation _simulation;
	
	//the content (display_ component
	private JComponent _content;
	
	//the attribute panel
	private AttributePanel _attributePanel;
	
	public SimulationPanel(Simulation simulation, JComponent content) {
		setLayout(new BorderLayout(4, 4));
		_simulation = simulation;
		_content = content;
		add(_content, BorderLayout.CENTER);
		addEast();
	}

	private void addEast() {
		JPanel panel = new JPanel() {
			@Override
			public Insets getInsets() {
				Insets def = super.getInsets();
				return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
						def.right + 2);
			}
			
		};
		
		panel.setLayout(new BorderLayout(4, 4));
		_attributePanel = new AttributePanel(_simulation.getAttributes());
	    panel.add(_attributePanel, BorderLayout.NORTH);
		
	    
	    add(panel, BorderLayout.EAST);
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 2, def.left + 2, def.bottom + 2,
				def.right + 2);
	}

}
