package io.github.robertaguilera712.cisojr4droid;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Cso {

    final int MAGIC = 0x4F534943;
    final int HEADER_SIZE = 0x18;
    final int BLOCK_SIZE = 0x800;
    final int PLAIN_BLOCK = 0x80000000;
    final byte ALIGN = 0;
    final byte VERSION = 0;
    final long TOTAL_BYTES;
    final int TOTAL_BLOCKS;
    final int BLOCK_INDEX_LENGTH;
    final int BLOCK_INDEX_SIZE;

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
        int bytes = in.read(blockIndexBytes);
        buffer.put(blockIndexBytes);
        buffer.position(0);
        final int[] blockIndex = new int[BLOCK_INDEX_LENGTH];

        for (int i = 0; i < BLOCK_INDEX_LENGTH; i++) {
            blockIndex[i] = buffer.getInt();
        }

        return blockIndex;
    }

//    private boolean checkHeader(final FileInputStream in) throws IOException {
//        final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        final byte[] headerBytes = new byte[HEADER_SIZE];
//        in.read(headerBytes);
//        buffer.put(headerBytes);
//        buffer.position(0);
//        final int magic = buffer.getInt();
//        buffer.position(8);
//        final long totalBytes = buffer.getLong();
//        final int blockSize = buffer.getInt();
//
//        return  (magic == MAGIC || blockSize != 0 || totalBytes != 0);
//    }

}
