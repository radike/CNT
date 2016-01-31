package com.cambridge.hack2016.cnt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class FileUploader {

	private File file;
	private OutputStream os;
	private BufferedReader br;
	private PrintWriter pw;

	public static interface ProgressListener {
		void fileAccepted(String id);

		void sendingProgress(double percentage);

		void error(String msg);
	}

	private ProgressListener progress;

	public FileUploader(String filePath, ProgressListener progress) {
		file = new File(filePath);
		this.progress = progress;

		try (Socket socket = new Socket("localhost", 8080)) {
			os = socket.getOutputStream();
			pw = new PrintWriter(os, true);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			offerFile();
			waitToRequest();
			sendFile();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			progress.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			progress.error("Server error");
		}
	}

	private void offerFile() throws IOException, NoSuchAlgorithmException {
		sendOffer();
		String id = receiveOfferId();
		progress.fileAccepted(id);
	}

	private void sendOffer() throws NoSuchAlgorithmException, IOException {
		String hash = computeFileHash();
		long fileSize = file.length();
		pw.println("{\"hash\": \"" + hash + "\", \"size\":" + fileSize + ", \"name\":\"" + file.getName() + "\", \"role\":\"up\"}");
	}

	private String computeFileHash() throws IOException, NoSuchAlgorithmException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();
			return Hex.encodeHexString(hashedBytes);
		}
	}

	private String receiveOfferId() throws IOException {
		String answer = br.readLine();
		JsonObject g = new JsonParser().parse(answer).getAsJsonObject();
		return g.get("id").getAsString();
	}

	private void waitToRequest() throws IOException {
		br.readLine();
	}

	private void sendFile() throws JsonSyntaxException, IOException {
		try (ChunkReader r = new ChunkReader(new FileInputStream(file))) {
			long size = file.length();
			long send = 0;
			byte[] chunk;
			progress.sendingProgress(0);
			while ((chunk = r.readChunk()) != null) {
				send += chunk.length;
				os.write(chunk);
				progress.sendingProgress(computeProgress(size, send));
			}
			progress.sendingProgress(100);
		}
	}

	private double computeProgress(long max, long current) {
		return (current / (double) (max)) * 100;
	}
}