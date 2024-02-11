package com.example.myapplication;

import static com.example.myapplication.ChatActivity.handler;
import static com.example.myapplication.ChatActivity.messageAdapter;
import static com.example.myapplication.ChatActivity.messages;
import static com.example.myapplication.ChatActivity.readAndDeserializeNextElementSize;
import static com.example.myapplication.ChatActivity.showCustomProgressDialog;
import static com.example.myapplication.CustomData.readAndConvertBytesToString;
import static com.example.myapplication.FileService.input;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The ReceiveFileCommand class is responsible for receiving and processing files sent over a network connection.
 * It implements the Command interface and contains methods
 * for executing the receive file command, receiving the file name,
 * preparing the file for writing, writing the file content, updating the UI with the
 * received file, and reading and deserializing the next element's long size from the
 * input stream.
 */
public class ReceiveFileCommand implements Command {
    private static final int CHUNK_SIZE = 16 * 1024 * 1024; // 16 MB
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void execute() throws IOException {
        String fileName = receiveFileName();
        long fileBytesLength = readAndDeserializeNextElementLongSize();

        File file = prepareFileForWriting(fileName);
        if (file != null) {
            writeFileContent(file, fileBytesLength);
            updateUIWithReceivedFile(file);
        }
    }

    /**
     * Receives the file name from the input stream
     *
     * @return - String representing the file name
     * @throws IOException - if an error occurs while reading the input stream
     */
    private String receiveFileName() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int fileNameSize = readAndDeserializeNextElementSize(byteArrayOutputStream);
        byte[] fileNameBytes = new byte[fileNameSize];
        int line = input.read(fileNameBytes, 0, fileNameSize);
        return readAndConvertBytesToString(byteArrayOutputStream, line, fileNameBytes);
    }

    /**
     * Prepares the file for writing by checking available space and creating a new file
     *
     * @param fileName - name of the file to be written
     * @return - File object ready for writing, or null if there's not enough space
     */
    private File prepareFileForWriting(String fileName) {
        String externalStoragePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + fileName;
        File file = new File(externalStoragePath);

        @SuppressLint("UsableSpace")
        long availableSpace = Environment.getExternalStorageDirectory().getUsableSpace();
        if (availableSpace < file.length()) {
            System.out.println("Not enough space");
            showCustomProgressDialog(ChatActivity.instance,file);
            //return null;
        }
        return file;
    }

    /**
     * Writes the content of the file to the prepared file object
     *
     * @param file - File object where the content will be written
     * @param fileBytesLength - length of the file content in bytes
     * @throws IOException - if an error occurs while writing the file
     */
    private void writeFileContent(File file, long fileBytesLength) throws IOException {
        byte[] buffer = new byte[CHUNK_SIZE];
        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            long bytesRead;
            while (fileBytesLength > 0) {
                bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileBytesLength));
                bufferedOutputStream.write(buffer, 0, (int) bytesRead);
                bufferedOutputStream.flush();
                fileBytesLength -= bytesRead;
            }
        }
    }

    /**
     * Updates the UI with the received file
     *
     * @param file - received File object
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateUIWithReceivedFile(File file) {
        handler.post(() -> {
            Message message = new Message(MessageType.FILE_RECEIVED, file.getAbsolutePath(), file.getName(), file.length());
            messages.add(message);
            messageAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Reads and deserializes the next element's long size from the input stream
     *
     * @return - long representing the size of the next element
     * @throws IOException - if an error occurs while reading the input stream
     */
    private long readAndDeserializeNextElementLongSize() throws IOException {
        byte[] elementSizeBytes = new byte[8];
        input.read(elementSizeBytes);
        return CustomData.deserializeLong(elementSizeBytes);
    }
}

