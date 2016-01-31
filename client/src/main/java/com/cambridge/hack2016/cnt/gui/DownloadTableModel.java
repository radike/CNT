package com.cambridge.hack2016.cnt.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.cambridge.hack2016.cnt.FileDownloader.FileInfo;
import com.cambridge.hack2016.cnt.FileDownloader.ProgressListener;

public class DownloadTableModel extends AbstractTableModel {

	private List<DownloadFileData> files = new ArrayList<DownloadFileData>();
	private static final String[] columnNames = new String[] { "Code", "File Name", "Download status" };
	private static final DecimalFormat formatter = new DecimalFormat("#0.000");

	public DownloadFileData addFile(String code) {
		DownloadFileData df = new DownloadFileData(code);
		files.add(0, df);
		fireTableRowsInserted(0, 1);
		return df;
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
		DownloadFileData df = files.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return df.code;
		case 1:
			return df.fileName == null ? "" : df.fileName;
		case 2:
			if (df.errorMsg != null) {
				return df.errorMsg;
			}
			if (df.fileName == null) {
				return "Establishing connection with the server";
			} else if (df.downloadPercentage < 0) {
				return "Waiting for the uploader";
			} else if (df.downloadPercentage < 100) {
				return "Downloading: " + formatter.format(df.downloadPercentage) + " %";
			} else if (df.downloadPercentage >= 100) {
				return "Done - downloaded";
			}
		}
		return "";
	}

	public class DownloadFileData implements ProgressListener {
		private String fileName = null;
		private String code = null;
		private double downloadPercentage = -1;
		private String errorMsg;

		public DownloadFileData(String code) {
			this.code = code;
		}

		@Override
		public void fileInfo(FileInfo fi) {
			this.fileName = fi.fileName;
			DownloadTableModel.this.fireTableDataChanged();
		}

		@Override
		public void receivingProgress(double percentage) {
			this.downloadPercentage = percentage;
			DownloadTableModel.this.fireTableDataChanged();
		}

		@Override
		public void error(String errMsg) {
			this.errorMsg = errMsg;
			DownloadTableModel.this.fireTableDataChanged();
		}
	}
}
