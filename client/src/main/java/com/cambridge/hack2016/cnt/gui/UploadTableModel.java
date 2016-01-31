package com.cambridge.hack2016.cnt.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.cambridge.hack2016.cnt.FileUploader.ProgressListener;

public class UploadTableModel extends AbstractTableModel {

	private List<UploadFileData> files = new ArrayList<UploadFileData>();
	private static final String[] columnNames = new String[] { "File Name", "Code", "Upload status" };
	private static final DecimalFormat formatter = new DecimalFormat("#0.000");

	public UploadFileData addFile(String fileName) {
		UploadFileData uf = new UploadFileData(fileName);
		files.add(0, uf);
		fireTableRowsInserted(0, 1);
		return uf;
	}

	@Override
	public int getRowCount() {
		return files.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		UploadFileData uf = files.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return uf.fileName;
		case 1:
			return uf.code == null ? "" : uf.code;
		case 2:
			if (uf.errorMsg != null) {
				return uf.errorMsg;
			}
			if (uf.code == null) {
				return "Establishing connection with the server";
			} else if (uf.uploadPercentage < 0) {
				return "Ready";
			} else if (uf.uploadPercentage < 100) {
				return "Uploading: " + formatter.format(uf.uploadPercentage) + " %";
			} else if (uf.uploadPercentage >= 100) {
				return "Done - uploaded";
			}
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		if (column == 1) {
			return true;
		}
		return false;
	}

	public class UploadFileData implements ProgressListener {
		private String fileName = null;
		private String code = null;
		private double uploadPercentage = -1;
		private String errorMsg;

		public UploadFileData(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public void fileAccepted(String code) {
			this.code = code;
			UploadTableModel.this.fireTableDataChanged();
		}

		@Override
		public void sendingProgress(double percentage) {
			this.uploadPercentage = percentage;
			UploadTableModel.this.fireTableDataChanged();
		}

		@Override
		public void error(String msg) {
			this.errorMsg = msg;
			UploadTableModel.this.fireTableDataChanged();
		}
	}
}
