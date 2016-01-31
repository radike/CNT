package com.cambridge.hack2016.cnt.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.cambridge.hack2016.cnt.FileUploader;

public class UploadPanel extends JPanel {

	private JButton uploadFile = new JButton("Select a file to transfer");
	private UploadTableModel uploadModel = new UploadTableModel();

	public UploadPanel() {
		setLayout(new BorderLayout());
		addControlls();
		addTable();
	}

	private void addControlls() {
		JPanel uploadPanelControlls = new JPanel();
		add(uploadPanelControlls, BorderLayout.NORTH);
		uploadPanelControlls.setLayout(null);
		uploadPanelControlls.setMinimumSize(new Dimension(100, 30));
		uploadPanelControlls.setPreferredSize(uploadPanelControlls.getMinimumSize());
		uploadPanelControlls.setOpaque(false);

		uploadPanelControlls.add(uploadFile);
		uploadFile.setBounds(10, 0, 180, 20);
		uploadFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(UploadPanel.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = fc.getSelectedFile();
					final UploadTableModel.UploadFileData uf = uploadModel.addFile(file.getName());
					new Thread(new Runnable() {

						@Override
						public void run() {
							FileUploader client = new FileUploader(file.getAbsolutePath(), uf);
						}

					}).start();
				}
			}

		});
	}

	private void addTable() {
		JTable uploadTable = new JTable(uploadModel);
		JScrollPane uploadScrollPane = new JScrollPane(uploadTable);
		add(uploadScrollPane, BorderLayout.CENTER);
	}
}
