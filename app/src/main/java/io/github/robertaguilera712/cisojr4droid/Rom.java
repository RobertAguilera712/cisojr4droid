package io.github.robertaguilera712.cisojr4droid;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.documentfile.provider.DocumentFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Rom {

    private static ContentResolver resolver;
    private static DocumentFile romsFolder;
    private static String outputMimeType;
    private static String outputFileExtension;
    private static String inputMimeType;
    private static String inputFileExtension;

    private final Uri inputUri;
    private final String inputFilename;
    private Uri outputUri;
    private String outputFilename;
    private int CompressionLevel;
    private boolean delete;
    private String status;
    private double progress;
    private ParcelFileDescriptor pfdIn;
    private ParcelFileDescriptor pfdOut;
    private FileInputStream in;
    private FileOutputStream out;

    public Rom(Uri inputUri, String inputFilename, String outputFilename, int compressionLevel, boolean delete, String status, double progress) {
        this.inputUri = inputUri;
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
        CompressionLevel = compressionLevel;
        this.delete = delete;
        this.status = status;
        this.progress = progress;
    }

    public void initStreams() throws FileNotFoundException {
        setOutputUri();
        pfdIn = Rom.resolver.openFileDescriptor(inputUri, "r");
        pfdOut = Rom.resolver.openFileDescriptor(outputUri, "rw");
        in = new FileInputStream(pfdIn.getFileDescriptor());
        out = new FileOutputStream(pfdOut.getFileDescriptor());
    }

    private void setOutputUri() {
        DocumentFile outputFile = romsFolder.findFile(outputFilename);
        if (outputFile != null) {
            outputFile.delete();
        }
        outputUri = romsFolder.createFile(outputMimeType, outputFilename).getUri();
    }

    public void closeStreams() {
        try {
            pfdIn.close();
            pfdOut.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInputFilename() {
        return inputFilename;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public String getOutputBaseFilename() {
        return outputFilename.replaceFirst(Rom.outputFileExtension + "$", "");
    }

    public FileInputStream getIn() {
        return in;
    }

    public FileOutputStream getOut() {
        return out;
    }

    public Uri getInputUri() {
        return inputUri;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public static void setResolver(ContentResolver resolver) {
        Rom.resolver = resolver;
    }

    public int getCompressionLevel() {
        return CompressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        CompressionLevel = compressionLevel;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public static String getOutputFileExtension() {
        return outputFileExtension;
    }

    public static void setOutputFileExtension(String outputFileExtension) {
        Rom.outputFileExtension = outputFileExtension;
    }

    public static String getInputMimeType() {
        return inputMimeType;
    }

    public static void setInputMimeType(String inputMimeType) {
        Rom.inputMimeType = inputMimeType;
    }

    public static String getInputFileExtension() {
        return inputFileExtension;
    }

    public static void setInputFileExtension(String inputFileExtension) {
        Rom.inputFileExtension = inputFileExtension;
    }

    public double getProgress() {
        return progress;
    }

    public int getIntProgress() {
        return (int) progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public static void setRomsFolder(DocumentFile romsFolder) {
        Rom.romsFolder = romsFolder;
    }

    public static void setOutputMimeType(String outputMimeType) {
        Rom.outputMimeType = outputMimeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
