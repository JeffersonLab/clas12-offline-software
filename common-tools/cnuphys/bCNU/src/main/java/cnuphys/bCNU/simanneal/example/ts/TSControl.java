package cnuphys.bCNU.simanneal.example.ts;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import cnuphys.splot.plot.PlotPanel;

public class TSControl extends JPanel implements ActionListener {
	
	//for the plot
	private PlotPanel _plotPanel;
	
	//run button
	private JButton _runButton;
	
	public TSControl() {
		setLayout(new BorderLayout(6, 6));
		addNorth();
		addCenter();
		addSouth();
	}
	
	private void addNorth() {
		
	}
	
	private void addCenter() {
		
	}

	//add the component in the south
	private void addSouth() {
		JPanel panel = new JPanel();
		
		_runButton = new JButton(" Run ");
		_runButton.addActionListener(this);
		
		add(panel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == _runButton) {
			handleRun();
		}
		
	}

	//run button selected
	private void handleRun() {
		
	}

}
