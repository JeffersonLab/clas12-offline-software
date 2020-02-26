package cnuphys.bCNU.fortune;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cnuphys.bCNU.dialog.SimpleDialog;

public class FortuneDialog extends SimpleDialog {
	
	private JButton anotherButton;
	
	private JTextArea _textArea;
	
	
	public FortuneDialog() {
		super("Unix Fortune", false, "Close");
	}
	
	@Override
	public Insets getInsets() {
		Insets def = super.getInsets();
		return new Insets(def.top + 4, def.left + 4, def.bottom + 4,
				def.right + 4);
	}
	
	@Override
	protected Component createNorthComponent() {
		JLabel jlab = new JLabel("<html> <i>ced</i> bears <b>no responsibility</b> for any politically incorrect fortunes. ");
		jlab.setOpaque(true);
		jlab.setBackground(Color.darkGray);
		jlab.setForeground(Color.cyan);
		jlab.setBorder(BorderFactory.createEtchedBorder());
		return jlab;
	}

	
	@Override
	protected Component createSouthComponent() {
		super.createSouthComponent();
		
		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				anotherFortune();
			}
			
		};
		
		anotherButton = new JButton("New Fortune");
		anotherButton.addActionListener(al);
		
		buttonPanel.add(anotherButton,  0);
		return buttonPanel;
	}
	
	@Override
	protected Component createCenterComponent() {
		_textArea = new JTextArea(8, 45);
		_textArea.setEditable(false);
		_textArea.setWrapStyleWord(true);
		
		JScrollPane pane = new JScrollPane(_textArea);
		return pane;
	}
	
	private void anotherFortune() {
		_textArea.setText(FortuneManager.getInstance().getFortune());
	}
	
	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		if (vis) {
			anotherFortune();
		}
	}

}
