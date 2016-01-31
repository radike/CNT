package com.cambridge.hack2016.cnt;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ChunkReader implements Closeable {
	private final int chunkSize = 1024;
	private byte[] bytes = new byte[chunkSize];
	private InputStream in;

	public ChunkReader(InputStream in) {
		this.in = in;
	}

	public byte[] readChunk() throws IOException {
		int read = in.read(bytes);
		if (read < 0)
			return null;
		if (read < bytes.length) {
			bytes = Arrays.copyOf(bytes, read);
		}
		return bytes;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
