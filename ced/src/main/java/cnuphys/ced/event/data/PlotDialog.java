package cnuphys.ced.event.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jlab.evio.clas12.EvioDataEvent;

import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.ced.clasio.IClasIoEventListener;
import cnuphys.ced.event.AccumulationManager;
import cnuphys.ced.event.IAccumulationListener;

public abstract class PlotDialog extends JDialog implements ActionListener, IAccumulationListener, IClasIoEventListener {

	//close the dialog
	protected JButton _closeButton;
	
	//remove the dialog
	protected JButton _removeButton;
	
	//the name
	protected String _name;
	
	public PlotDialog(String name) {
		_name = name;
		addSouth();
		setTitle(name);
		setModal(false);
		setSize(400, 400);
	}
	
	private void addSouth(){
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 10));
		
		_closeButton = new JButton(" Close ");
		_closeButton.addActionListener(this);
		
		_removeButton = new JButton(" Remove ");
		_removeButton.addActionListener(this);
		
		sp.add(_closeButton);
		sp.add(_removeButton);
		add(sp, BorderLayout.SOUTH);
		
		AccumulationManager.getInstance().addAccumulationListener(this);
		ClasIoEventManager.getInstance().addClasIoEventListener(this, 2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _closeButton) {
			setVisible(false);
		}
		else if (o == _removeButton) {
			AccumulationManager.getInstance().removeAccumulationListener(this);
			ClasIoEventManager.getInstance().removeClasIoEventListener(this);

			DefinitionManager.getInstance().remove(_name);
		}
	}

}
