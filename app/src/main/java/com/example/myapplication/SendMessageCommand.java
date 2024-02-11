package com.example.myapplication;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The SendMessageCommand class implements the Command interface and is responsible for
 * executing the process of sending a text message to another user in the chat.
 */
public class SendMessageCommand implements Command {
    private static final String MESSAGE_COMMAND = "Message";
    private final String message;
    private final OutputStream outputStream;

    public SendMessageCommand(OutputStream outputStream, String message) {
        this.outputStream = outputStream;
        this.message = message;
    }

    @Override
    public void execute() {
        byte[] commandBytes = prepareCommandBytes();
        sendData(commandBytes);
    }

    /**
     * Prepares a byte array containing the full command and message to send
     *
     * @return - byte[] representing the full command and message
     */
    private byte[] prepareCommandBytes() {
        byte[] cmdLen = serializeCommandLength();
        byte[] cmdBytes = MESSAGE_COMMAND.getBytes();
        byte[] messageLen = serializeMessageLength();
        byte[] messageBytes = message.getBytes();

        int resultLen = cmdLen.length + cmdBytes.length + messageLen.length + messageBytes.length;
        byte[] resultByteArray = new byte[resultLen];

        int index = 0;

        System.arraycopy(cmdLen, 0, resultByteArray, index, cmdLen.length);
        index += cmdLen.length;

        System.arraycopy(cmdBytes, 0, resultByteArray, index, cmdBytes.length);
        index += cmdBytes.length;

        System.arraycopy(messageLen, 0, resultByteArray, index, messageLen.length);
        index += messageLen.length;

        System.arraycopy(messageBytes, 0, resultByteArray, index, messageBytes.length);

        return resultByteArray;
    }

    /**
     * Serializes the length of the command string into a byte array
     *
     * @return - byte[] representing the serialized command length
     */
    private byte[] serializeCommandLength() {
        int commandLength = MESSAGE_COMMAND.length();
        return CustomData.serialize(commandLength);
    }

    /**
     * Serializes the length of the message string into a byte array
     *
     * @return - byte[] representing the serialized message length
     */
    private byte[] serializeMessageLength() {
        int lengthOfMessage = message.length();
        return CustomData.serialize(lengthOfMessage);
    }

    /**
     * Sends the prepared data (byte array) to the output stream
     *
     * @param data - byte array containing the full command and message
     */
    private void sendData(byte[] data) {
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
           // Log.i("SendMessageCommand", "Connection lost. Attempting to reconnect...");
        }
    }
}

