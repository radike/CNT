package com.cambridge.hack2016.cnt.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ClientView extends JFrame {
	public ClientView() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {

		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(640, 480));
		centerFrame();
		setTitle("Awesome Peer-to-peer File Transfer Manager");

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Upload", null, new UploadPanel(), "Upload a file");
		tabbedPane.addTab("Download", null, new DownloadPanel(), "Download a file");
		add(tabbedPane);

		setVisible(true);
	}

	private void centerFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - this.getSize().width / 2, screenSize.height / 2 - this.getSize().height / 2);
	}
}
