package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A class implementing the Command interface for sending files over a network.
 * It is responsible for constructing and sending the header information and the file data in chunks.
 * The class also calculates the transfer speed and updates the progress of the file transfer.
 */
public class SendFileCommand implements Command {
    private final Intent data;
    private final String FILE_COMMAND;
    private final ContentResolver contentResolver;
    private final OutputStream outputstream;
    private final ProgressListener progressListener;
    private static final int CHUNK_SIZE = 16 * 1024 * 1024; //16MB

    public SendFileCommand(String fileCommand,ContentResolver contentResolver,OutputStream outputStream,Intent data,ProgressListener progressListener) {
        this.FILE_COMMAND = fileCommand;
        this.contentResolver = contentResolver;
        this.outputstream = outputStream;
        this.data = data;
        this.progressListener = progressListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void execute() {
        sendFile(data);
    }

    /**
     * Sends the file in chunks after constructing and sending the header information.
     *
     * @param data Intent containing the file URI to be sent.
     */
    @SuppressLint("SuspiciousIndentation")
    private void sendFile(@Nullable Intent data) {
        assert data != null;
        Uri file = data.getData();
        String fileName = getFileName(file);
        System.out.println("File name: " + fileName);
        long fileSize = getFileSize(file); // use long for large file sizes

        byte[] headerBytes = constructHeader(file, fileName, fileSize);

        try {
            @SuppressLint("Recycle")
            InputStream inputStream = contentResolver.openInputStream(file);
            sendFileData(inputStream, fileSize, headerBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the size header, which includes the lengths and content of the command, file name and file size.
     *
     * @param fileName - the name of the file to be sent.
     *
     * @return - the total size of the header in bytes.
     */
    private int calculateHeaderSize(String fileName) {
        int resultLen = 0;
        resultLen += CustomData.serialize(FILE_COMMAND.length()).length;
        resultLen += FILE_COMMAND.getBytes().length;
        resultLen += CustomData.serialize(fileName.length()).length;
        resultLen += fileName.getBytes().length;
        return resultLen;
    }

    /**
     * Constructs the header byte array with the command, file name and file size information.
     *
     * @param file - the URI of the file to be sent
     * @param fileName - the name of the file to be sent.
     * @param fileSize - the size of the file to be sent in bytes.
     *
     * @return - The constructed header byte array.
     */
    private byte[] constructHeader(Uri file, String fileName, long fileSize) {
        byte[] fileCommandLen = CustomData.serialize(FILE_COMMAND.length());
        byte[] cmdBytes = FILE_COMMAND.getBytes();
        byte[] fileNameLenBytes = CustomData.serialize(fileName.length());
        byte[] fileNameBytes = fileName.getBytes();
        byte[] fileSizeBytes = CustomData.serializeLong(fileSize);

        int headerSize = calculateHeaderSize(fileName) + fileSizeBytes.length;

        return constructFile(headerSize, fileCommandLen, cmdBytes, fileNameLenBytes,
                fileNameBytes, fileSizeBytes, null);
    }

    /**
     * Sends the file data in chunks while updating the progress and calculating the transfer speed.
     *
     * @param inputStream - the Input stream of the file to be sent.
     * @param fileSize - the size of the file to be sent in bytes.
     * @param headerBytes - the header byte array containing the file information.
     *
     * @throws IOException - if there is an issue while reading from the input stream or writing to the output stream
     */
    private void sendFileData(InputStream inputStream, long fileSize, byte[] headerBytes) throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead;
        long remainingFileSize = fileSize;

        outputstream.write(headerBytes);
        outputstream.flush();

        long bytesTransferred = 0;
        long startTime = System.currentTimeMillis();

        while (remainingFileSize > 0) {
            bytesRead = inputStream.read(buffer, 0, (int) Math.min(CHUNK_SIZE, remainingFileSize));
            byte[] chunk = Arrays.copyOf(buffer, bytesRead);
            remainingFileSize -= bytesRead;

            outputstream.write(buffer, 0, bytesRead);
            outputstream.flush();

            bytesTransferred += bytesRead;
            long elapsedTime = System.currentTimeMillis() - startTime;
            double speed = bytesTransferred / (elapsedTime / 1000.0) / (1024.0 * 1024.0); // Calculate speed in MB/s
            progressListener.onProgressUpdate(bytesTransferred, fileSize, speed);
        }
        inputStream.close();
    }


    /**
     * Constructs a byte array containing the header information and file data concatenating the given byte arrays.
     *
     * @param resultLen - the total length of the resulting byte array.
     * @param cmdLen - the byte array representing the length of the command.
     * @param cmdBytes - the byte array representing the command.
     * @param fileNameLen - the byte array representing the length of the file name.
     * @param fileNameBytes - the byte array representing the file name.
     * @param fileLen - the byte array representing the length of the file.
     * @param fileBytes - the byte array representing the file data (optional, can be null)
     *
     * @return - The constructed byte array containing the header information and file data.
     */
    @NonNull
    private byte[] constructFile(int resultLen, byte[] cmdLen, byte[] cmdBytes, byte[] fileNameLen, byte[] fileNameBytes, byte[] fileLen, byte[] fileBytes) {
        byte[] result = new byte[resultLen];
        int index = 0;

        //copy all pieces in the result byte[]
        System.arraycopy(cmdLen, 0, result, index, cmdLen.length);
        index += cmdLen.length;

        System.arraycopy(cmdBytes, 0, result, index, cmdBytes.length);
        index += cmdBytes.length;

        System.arraycopy(fileNameLen, 0, result, index, fileNameLen.length);
        index += fileNameLen.length;

        System.arraycopy(fileNameBytes, 0, result, index, fileNameBytes.length);
        index += fileNameBytes.length;

        System.arraycopy(fileLen, 0, result, index, fileLen.length);
        index += fileLen.length;

        if (fileBytes != null) {
            System.arraycopy(fileBytes, 0, result, index, fileBytes.length);
        }
        return result;
    }

    /**
     * Retrieves the size of a large file using its URI.
     *
     * @param uri - the URI of the file whose size is to be retrieved.
     *
     * @return - The size of the file in bytes, or -1 if the file size could not be determined.
     */
    @SuppressLint("Range")
    private long getFileSize(Uri uri) {
        try (Cursor cursor = contentResolver.query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Retrieves the file name using its URI.
     *
     * @param uri - the URI of the file whose name is to be retrieved.
     *
     * @return - The name of the file, or null if the file name could not be determined.
     */
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}