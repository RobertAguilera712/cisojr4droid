package io.github.robertaguilera712.cisojr4droid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Cso {
	
	final int magic;
	final byte version;
	final int blockSize;
	final long totalBytes;
	final int totalBlocks;
	final int blockIndexLength;
	final int blockIndexSize;
	final byte align;
	
	public Cso(final long fileSize) {
		magic = Ciso.CISO_MAGIC;
		version = 1;
		blockSize = Ciso.CISO_BLOCK_SIZE;
		totalBytes = fileSize;
		totalBlocks = (int) (fileSize / blockSize);
		blockIndexLength = totalBlocks + 1;
		blockIndexSize = blockIndexLength * 4;
		align = 0;
	}
	
	public void writeHeader(final FileOutputStream out) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(Ciso.CISO_HEADER_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(magic);
		buffer.putInt(Ciso.CISO_HEADER_SIZE);
        buffer.putLong(totalBytes);
        buffer.putInt(blockSize);
        buffer.put(version);
        buffer.put(align);
        out.write(buffer.array());
	}
	
	public void writeDummyBlockIndex(final FileOutputStream out) throws IOException {
		final byte[] dummy = new byte[blockIndexSize];
		out.write(dummy);
	}
	
	public void writeBlockIndex(final FileOutputStream out, final int[] blockIndex) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(blockIndexSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i : blockIndex) {
            buffer.putInt(i);
        }
        out.write(buffer.array());
	}

	public int[] getBlockIndex(final FileInputStream in) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(blockIndexSize);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final byte[] blockIndexBytes = new byte[blockIndexSize];
		in.read(blockIndexBytes);
		buffer.put(blockIndexBytes);
		buffer.position(0);
		final int[] blockIndex = new int[blockIndexLength];

		for (int i = 0; i < blockIndexLength; i++) {
			blockIndex[i] = buffer.getInt();
		}

		return blockIndex;
	}
}
