package io.github.robertaguilera712.cisojr4droid.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Cso {

    public final int MAGIC = 0x4F534943;
    public final int HEADER_SIZE = 0x18;
    public final int BLOCK_SIZE = 0x800;
    public final int PLAIN_BLOCK = 0x80000000;
    public final byte ALIGN = 0;
    public final byte VERSION = 0;
    public final long TOTAL_BYTES;
    public final int TOTAL_BLOCKS;
    public final int BLOCK_INDEX_LENGTH;
    public final int BLOCK_INDEX_SIZE;

    public Cso(final long totalBytes) {
        TOTAL_BYTES = totalBytes;
        TOTAL_BLOCKS = (int) TOTAL_BYTES / BLOCK_SIZE;
        BLOCK_INDEX_LENGTH = TOTAL_BLOCKS + 1;
        BLOCK_INDEX_SIZE = BLOCK_INDEX_LENGTH * 4;
    }

    public void writeHeader(final FileOutputStream out) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(MAGIC);
        buffer.putInt(HEADER_SIZE);
        buffer.putLong(TOTAL_BYTES);
        buffer.putInt(BLOCK_SIZE);
        buffer.put(VERSION);
        buffer.put(ALIGN);
        out.write(buffer.array());
    }

    public void writeDummyBlockIndex(final FileOutputStream out) throws IOException {
        final byte[] dummyBlockIndex = new byte[BLOCK_INDEX_SIZE];
        out.write(dummyBlockIndex);
    }

    public void writeBlockIndex(final FileOutputStream out, final int[] blockIndex) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(BLOCK_INDEX_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i : blockIndex) {
            buffer.putInt(i);
        }
        out.write(buffer.array());
    }

    public int[] getBlockIndex(final FileInputStream in) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_INDEX_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        final byte[] blockIndexBytes = new byte[BLOCK_INDEX_SIZE];
        in.read(blockIndexBytes);
        buffer.put(blockIndexBytes);
        buffer.position(0);
        final int[] blockIndex = new int[BLOCK_INDEX_LENGTH];

        for (int i = 0; i < BLOCK_INDEX_LENGTH; i++) {
            blockIndex[i] = buffer.getInt();
        }

        return blockIndex;
    }

}
