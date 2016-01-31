package com.cambridge.hack2016.cnt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileDownloader {

	private InputStream is;
	private BufferedReader br;
	private PrintWriter pw;

	public static interface ProgressListener {
		void fileInfo(FileInfo fi);

		void receivingProgress(double percentage);

		void error(String errMsg);
	}

	public FileDownloader(String id, ProgressListener progress) {
		try (Socket socket = new Socket("localhost", 8080)) {
			pw = new PrintWriter(socket.getOutputStream(), true);
			is = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			FileInfo fi = getFileInfo(id);
			progress.fileInfo(fi);

			try (FileOutputStream fileWriter = new FileOutputStream(new File(fi.fileName)); ChunkReader r = new ChunkReader(is)) {
				long currSize = 0;
				byte[] chunk;
				while ((chunk = r.readChunk()) != null) {
					if (currSize == 0) {
						progress.receivingProgress(0);
					}
					fileWriter.write(chunk);
					currSize += chunk.length;
					progress.receivingProgress(computeProgress(fi.fileSize, currSize));
				}
				progress.receivingProgress(100);
			}
		} catch (IOException | FileNotFoundServerException e) {
			e.printStackTrace();
			progress.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			progress.error("Server error");
		}
	}

	private FileInfo getFileInfo(String id) throws IOException, FileNotFoundServerException {
		sendDownloadRequest(id);
		return receiveFileInfo();
	}

	private void sendDownloadRequest(String id) {
		pw.println("{\"id\": \"" + id + "\", \"role\":\"down\"}");
	}

	private FileInfo receiveFileInfo() throws IOException, FileNotFoundServerException {
		String line = br.readLine();
		JsonObject g = new JsonParser().parse(line).getAsJsonObject();
		JsonElement error = g.get("error");
		if (error != null) {
			throw new FileNotFoundServerException(error.getAsString());
		}
		FileInfo fi = new FileInfo();
		fi.fileName = g.get("name").getAsString();
		fi.fileSize = g.get("size").getAsLong();
		return fi;
	}

	public static class FileInfo {
		public String fileName;
		public long fileSize;
	}

	private static class FileNotFoundServerException extends Exception {
		public FileNotFoundServerException(String msg) {
			super(msg);
		}
	}

	private double computeProgress(long max, long current) {
		return (current / (double) (max)) * 100;
	}
}
