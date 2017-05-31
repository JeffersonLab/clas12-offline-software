package randomChoice;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class Team extends JPanel {
	
	public Student student1;
	public Student student2;
	
	
	public Team(Student s1, Student s2) {
		student1 = s1;
		student2 = s2;
		System.err.println("Made team: " + student1.name + " and " + student2.name);
		
		setLayout(new GridLayout(1, 2, 50, 10));
		add(student1);
		add(student2);
		
//		setOpaque(true);
//		
//		setBackground(Color.red);
	}
	


}
