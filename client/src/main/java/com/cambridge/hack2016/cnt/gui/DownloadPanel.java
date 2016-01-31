package com.cambridge.hack2016.cnt.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.cambridge.hack2016.cnt.FileDownloader;

public class DownloadPanel extends JPanel {
	private JLabel enterCode = new JLabel("Enter code:");
	private JTextField codeField = new JTextField();
	private JButton searchHash = new JButton("Download");
	private DownloadTableModel downloadModel = new DownloadTableModel();

	public DownloadPanel() {
		setLayout(new BorderLayout());
		addControlls();
		addTable();
	}

	private void addControlls() {
		JPanel downloadPanelControlls = new JPanel();
		add(downloadPanelControlls, BorderLayout.NORTH);
		downloadPanelControlls.setLayout(null);
		downloadPanelControlls.setMinimumSize(new Dimension(100, 30));
		downloadPanelControlls.setPreferredSize(downloadPanelControlls.getMinimumSize());
		downloadPanelControlls.setOpaque(false);

		downloadPanelControlls.add(enterCode);
		enterCode.setBounds(10, 0, 70, 20);

		downloadPanelControlls.add(codeField);
		codeField.setBounds(80, 0, 160, 22);

		downloadPanelControlls.add(searchHash);
		searchHash.setBounds(250, 0, 100, 20);
		searchHash.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final String code = codeField.getText();
				final DownloadTableModel.DownloadFileData df = downloadModel.addFile(code);
				new Thread(new Runnable() {

					@Override
					public void run() {
						FileDownloader client = new FileDownloader(code, df);
					}

				}).start();
			}

		});
	}

	private void addTable() {
		JTable downloadTable = new JTable(downloadModel);
		JScrollPane downloadScrollPane = new JScrollPane(downloadTable);
		add(downloadScrollPane, BorderLayout.CENTER);
	}
}
