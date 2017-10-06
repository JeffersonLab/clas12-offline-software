package cnuphys.ced.alldata.graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cnuphys.bCNU.dialog.DialogUtilities;
import cnuphys.bCNU.graphics.ImageManager;

public class DefineExpressionDialog extends JDialog implements ActionListener {
	
	//close button
	private JButton _closeButton;
	
	
	private NameVariablePanel _namedVariablePanel;

	public DefineExpressionDialog() {
		setTitle("Define an Expression");
		setModal(true);
		setLayout(new BorderLayout(4, 4));
		setIconImage(ImageManager.cnuIcon.getImage());
		
		_namedVariablePanel = new NameVariablePanel();
		add(_namedVariablePanel, BorderLayout.CENTER);

		addSouth();
		pack();
		DialogUtilities.centerDialog(this);

	}
	
	private void addSouth() {
		JPanel sp = new JPanel();
		sp.setLayout(new FlowLayout(FlowLayout.CENTER, 200, 10));
		
		_closeButton = new JButton(" Close  ");		
		_closeButton.addActionListener(this);
		
		sp.add(_closeButton);
		add(sp, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == _closeButton) {
			setVisible(false);
		}
	}
}
