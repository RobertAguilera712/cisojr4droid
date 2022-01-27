package io.github.robertaguilera712.cisojr4droid;

import android.os.Handler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

interface UpdateProgressCallback {
    void update(double progress);
}

interface FinishCallback {
    void finish();
}

public class Ciso {

    public final static int CISO_MAGIC = 0x4F534943;
    public final static int CISO_HEADER_SIZE = 0x18;
    public final static int CISO_BLOCK_SIZE = 0x800;
    public final static int CISO_PLAIN_BLOCK = 0x80000000;
    private final Executor executor;
    private final Handler handler;

    public Ciso(Executor executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public void compressIso(final FileInputStream in, final FileOutputStream out, int compressionLevel, final UpdateProgressCallback onUpdate, final FinishCallback onFinish) {
        executor.execute(() -> {
            try {
                final Cso cso = new Cso(in.getChannel().size());

                cso.writeHeader(out);
                cso.writeDummyBlockIndex(out);

                final int[] blockIndex = new int[cso.blockIndexLength];
                long writePosition = CISO_HEADER_SIZE + cso.blockIndexSize;
                final int alignB = 1 << cso.align;
                final int alignM = alignB - 1;
                final byte[] alignmentBuffer = new byte[64];
                final Deflater compressor = new Deflater(compressionLevel);

                for (int block = 0; block < cso.totalBlocks; block++) {
                    int align = (int) (writePosition & alignM);

                    if (align != 0) {
                        align = alignB - align;
                        out.write(alignmentBuffer, 0, align);
                        writePosition += align;
                    }

                    blockIndex[block] = (int) (writePosition >> cso.align);

                    byte[] rawData = new byte[CISO_BLOCK_SIZE];
                    in.read(rawData);
                    int rawDataSize = rawData.length;

                    byte[] compressedData = new byte[rawDataSize + 2];
                    compressor.setInput(rawData);
                    compressor.finish();
                    int length = compressor.deflate(compressedData);
                    compressor.reset();
                    compressedData = Arrays.copyOfRange(compressedData, 2, length);
                    int compressedDataSize = compressedData.length;
                    byte[] writableData;

                    if (compressedDataSize >= rawDataSize) {
                        writableData = rawData;
                        // Plain block marker
                        blockIndex[block] |= CISO_PLAIN_BLOCK;
                        // next index
                        writePosition += rawDataSize;
                    } else {
                        writableData = compressedData;
                        // Next index
                        writePosition += compressedDataSize;
                    }
                    double progress = block * 100.0 / cso.totalBlocks;
                    handler.post(() -> onUpdate.update(progress));

                    // Write data
                    out.write(writableData);
                }
                compressor.end();
                blockIndex[blockIndex.length - 1] = (int) (writePosition >> cso.align);
                out.getChannel().position(CISO_HEADER_SIZE);
                cso.writeBlockIndex(out, blockIndex);

                in.close();
                out.close();
                handler.post(onFinish::finish);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void decompressCso(final FileInputStream in, final FileOutputStream out, final UpdateProgressCallback onUpdate, final FinishCallback onFinish) throws IOException, DataFormatException {
        executor.execute(() -> {
            try{
                Cso cso = checkHeader(in);
                final int[] blockIndex = cso.getBlockIndex(in);
                final Inflater decompressor = new Inflater(true);

                for (int block = 0; block < cso.totalBlocks; block++) {
                    int index = blockIndex[block];
                    int index2;
                    int plain = index & CISO_PLAIN_BLOCK;
                    index &= 0x7FFFFFFF;
                    long readPosition = (long) index << cso.align;
                    int readSize;

                    if (plain != 0) {
                        readSize = CISO_BLOCK_SIZE;
                    } else {
                        index2 = blockIndex[block + 1] & 0x7FFFFFFF;
                        readSize = (index2 - index) << (cso.align);
                    }

                    byte[] rawData = seekAndRead(in, readPosition, readSize);
                    byte[] decompressedData;
                    int rawDataSize = rawData.length;

                    if (rawDataSize != readSize) {
                        System.exit(1);
                    }

                    if (plain != 0) {
                        decompressedData = rawData;
                    } else {
                        byte[] buffer = new byte[CISO_BLOCK_SIZE];
//                        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(rawData.length);
                        decompressor.setInput(rawData);
//                        while (!decompressor.finished()) {
                            int count = decompressor.inflate(buffer);
//                            Log.d("DECOMPRESSING", String.format("Block: %d, count: %s", block, count));
//                            outputBuffer.write(buffer, 0, count);
//                        }
//                        outputBuffer.close();
                        decompressor.reset();
//                        decompressedData = outputBuffer.toByteArray();
                        decompressedData = buffer;
                    }
                    double progress = block * 100.0 / cso.totalBlocks;
                    handler.post(() -> onUpdate.update(progress));

                    out.write(decompressedData);
                }
                decompressor.end();

                in.close();
                out.close();
                handler.post(onFinish::finish);
            } catch (DataFormatException | IOException e) {
                e.printStackTrace();
            }

        });
    }

    private static Cso checkHeader(final FileInputStream in) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(CISO_HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        final byte[] headerBytes = new byte[CISO_HEADER_SIZE];
        in.read(headerBytes);
        buffer.put(headerBytes);
        buffer.position(0);
        final int magic = buffer.getInt();
        buffer.position(8);
        final long totalBytes = buffer.getLong();
        final int blockSize = buffer.getInt();

        if (magic != CISO_MAGIC || blockSize == 0 || totalBytes == 0) {
            System.exit(1);
        }

        return new Cso(totalBytes);
    }


    private static byte[] seekAndRead(FileInputStream in, long position, int size) throws IOException {
        in.getChannel().position(position);
        final byte[] bytes = new byte[size];
        in.read(bytes);
        return bytes;
    }

}
