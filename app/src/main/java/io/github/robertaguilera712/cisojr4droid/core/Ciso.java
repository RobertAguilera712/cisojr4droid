package io.github.robertaguilera712.cisojr4droid.core;

import android.os.Handler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import io.github.robertaguilera712.cisojr4droid.model.Cso;
import io.github.robertaguilera712.cisojr4droid.model.Rom;
import io.github.robertaguilera712.cisojr4droid.utils.Callback;

public class Ciso {

    public static void compressIso(
            final Rom rom,
            final Callback.UpdateProgress update,
            final Callback.Finish finish,
            final Handler handler
    ) throws IOException {
        rom.initStreams();
        final FileInputStream in = rom.getIn();
        final FileOutputStream out = rom.getOut();
        final Cso cso = new Cso(in.getChannel().size());

        cso.writeHeader(out);
        cso.writeDummyBlockIndex(out);

        final int[] blockIndex = new int[cso.BLOCK_INDEX_LENGTH];
        long writePosition = cso.HEADER_SIZE + cso.BLOCK_INDEX_SIZE;
        final Deflater compressor = new Deflater(rom.getCompressionLevel());

        for (int block = 0; block < cso.TOTAL_BLOCKS; block++) {

            blockIndex[block] = (int) (writePosition >> cso.ALIGN);

            byte[] rawData = new byte[cso.BLOCK_SIZE];
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
                blockIndex[block] |= cso.PLAIN_BLOCK;
                // next index
                writePosition += rawDataSize;
            } else {
                writableData = compressedData;
                // Next index
                writePosition += compressedDataSize;
            }
            double progress = block * 100.0 / cso.TOTAL_BLOCKS;
            rom.setProgress(progress);
            handler.post(() -> update.update(rom));

            // Write data
            out.write(writableData);
        }
        compressor.end();
        blockIndex[blockIndex.length - 1] = (int) (writePosition >> cso.ALIGN);
        out.getChannel().position(cso.HEADER_SIZE);
        cso.writeBlockIndex(out, blockIndex);

        rom.closeStreams();
        handler.post(() -> finish.finish(rom));
    }

    public static void decompressCso(
            final Rom rom,
            final Callback.UpdateProgress update,
            final Callback.Finish finish,
            final Handler handler
    ) throws IOException, DataFormatException {
        rom.initStreams();
        final FileInputStream in = rom.getIn();
        final FileOutputStream out = rom.getOut();
        final Cso cso = new Cso(getTotalBytes(in));
        final int[] blockIndex = cso.getBlockIndex(in);
        final Inflater decompressor = new Inflater(true);

        for (int block = 0; block < cso.TOTAL_BLOCKS; block++) {
            int index = blockIndex[block];
            int index2;
            int plain = index & cso.PLAIN_BLOCK;
            index &= 0x7FFFFFFF;
            long readPosition = (long) index << cso.ALIGN;
            int readSize;

            if (plain != 0) {
                readSize = cso.BLOCK_SIZE;
            } else {
                index2 = blockIndex[block + 1] & 0x7FFFFFFF;
                readSize = (index2 - index) << (cso.ALIGN);
            }

            byte[] rawData = seekAndRead(in, readPosition, readSize);
            byte[] decompressedData;

            if (plain != 0) {
                decompressedData = rawData;
            } else {
                byte[] buffer = new byte[cso.BLOCK_SIZE];
                decompressor.setInput(rawData);
                decompressor.inflate(buffer);
                decompressor.reset();
                decompressedData = buffer;
            }
            double progress = block * 100.0 / cso.TOTAL_BLOCKS;
            rom.setProgress(progress);
            handler.post(() -> update.update(rom));

            out.write(decompressedData);
        }
        decompressor.end();

        rom.closeStreams();
        handler.post(() -> finish.finish(rom));
    }

    private static byte[] seekAndRead(FileInputStream in, long position, int size) throws IOException {
        in.getChannel().position(position);
        final byte[] bytes = new byte[size];
        in.read(bytes);
        return bytes;
    }

    private static long getTotalBytes(final FileInputStream in) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        final byte[] headerBytes = new byte[24];
        in.read(headerBytes);
        buffer.put(headerBytes);

        return buffer.getLong(8);
    }


}
