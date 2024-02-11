package com.example.myapplication;

/**
 * The Message class represents a message object that can be sent and received within the chat application.
 * It contains information about the sender, content, timestamp, message type,  file-related attributes,
 * and the progress of a file transfer.
 */

public class Message {
    private String username;
    private String content;
    private long timestamp;
    private final MessageType messageType;
    private String fileName;
    private long fileSize;
    private String filePath;

    private int progress;

    public Message(String username, String content, long timestamp, MessageType messageType) {
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.messageType = messageType;
    }

    public Message(MessageType messageType, String filePath, String fileName, long fileSize) {
        this.messageType = messageType;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public String getFilePath() {
        return filePath;
    }
    public String getFileName() {
        return fileName;
    }
}


