package com.example.myapplication;

import static com.example.myapplication.ChatActivity.displayMessage;
import static com.example.myapplication.ChatActivity.handler;
import static com.example.myapplication.ChatActivity.readAndDeserializeNextElementSize;
import static com.example.myapplication.CustomData.readAndConvertBytesToString;
import static com.example.myapplication.FileService.input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The ReceiveMessageCommand class implements the Command interface and is responsible for
 * executing the process of receiving a text message from another user in the chat.
 */
public class ReceiveMessageCommand implements Command {

    /**
     * Executes the command to receive a text message from another user.
     * It reads the incoming message, deserializes it and displays the received message.
     */
    @Override
    public void execute() throws IOException {
        int messageLen = readMessageLength();
        String message = readMessageContent(messageLen);

        // Display received message
        handler.post(() -> {
            Message receivedMessage = new Message(FileService.username, message, System.currentTimeMillis(), MessageType.MESSAGE_RECEIVED);
            displayMessage(receivedMessage);
        });
    }

    /**
     * Reads and deserializes the message length from the input stream.
     *
     * @return - The length of the incoming message.
     * @throws IOException If an I/O error occurs.
     */
    private int readMessageLength() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        return readAndDeserializeNextElementSize(byteArrayOutputStream);
    }

    /**
     * Reads the message content from the input stream.
     *
     * @param messageLen - the length of the message to read.
     * @return - The content of the message as a String.
     *
     * @throws IOException - If an I/O error occurs.
     */
    private String readMessageContent(int messageLen) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytesOfMessage = new byte[messageLen];

        // Reading message bytes
        int read = input.read(bytesOfMessage, 0, (int) messageLen);
        return readAndConvertBytesToString(byteArrayOutputStream, read, bytesOfMessage);
    }
}

