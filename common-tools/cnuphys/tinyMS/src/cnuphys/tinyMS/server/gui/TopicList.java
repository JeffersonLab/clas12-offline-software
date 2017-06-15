package cnuphys.tinyMS.server.gui;

import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import cnuphys.tinyMS.graphics.CommonBorder;
import cnuphys.tinyMS.graphics.Fonts;
import cnuphys.tinyMS.server.TinyMessageServer;

public class TopicList extends JList<String> {
	
	public static final int WIDTH = 150;

	//server owner
	private TinyMessageServer _server;
	
	//for scrolling the topics
	private JScrollPane _scrollPane;
	
	/*
	 * For the list of topics
	 * @param 
	 */
	public TopicList(TinyMessageServer server) {
		super(new DefaultListModel());
		_server = server;
		setFont(Fonts.mediumFont);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width = WIDTH;
		return d;
	}
	
	/**
	 * Get the scroll pane
	 * @return the scroll pane
	 */
	public JScrollPane getScrollPane() {
		if (_scrollPane == null) {
			_scrollPane = new JScrollPane() {
			};
			_scrollPane.getViewport().add(this);
			_scrollPane.setBorder(new CommonBorder("Topics"));
		}
		return _scrollPane;
	}
	
	public void addTopic(String topic) {
		if (topic == null) {
			return;
		}
		
		DefaultListModel model = (DefaultListModel)getModel();
		if (!model.contains(topic)) {
			model.addElement(topic);
		}
	}

}
