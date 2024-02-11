package com.example.myapplication;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * A utility class for serializing and deserializing integer and long values, and reading data from input streams.
 */
public class CustomData {

    /**
     * Serializes an integer value into a byte array.
     *
     * @param num  - the integer value to be serialized.
     * @return - A byte array containing the serialized integer value.
     */
    public static byte[] serialize(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    /**
     * Deserializes a byte array into an integer value.
     *
     * @param bytes - the byte array containing the serialized integer value.
     * @return - The deserialized integer value.
     */
    public static int deserialize(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    /**
     * Serializes a long value into a byte array.
     *
     * @param value - the long value to be serialized.
     * @return - A byte array containing the serialized long value.
     */
    public static byte[] serializeLong(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    /**
     * Deserializes a byte array into a long value.
     *
     * @param bytes - the byte array containing the serialized long value.
     * @return - The deserialized long value.
     */
    public static long deserializeLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * Reads data from an input stream, deserializes the message length, and returns it.
     *
     * @param byteArrayOutputStream - the ByteArrayOutputStream used for writing the data.
     * @param read - the number of bytes read from the input stream.
     * @param messageLenBytes - the byte array containing the serialized message length.
     * @return - The deserialized message length as an integer.
     */
    public static int readAndDeserializeMessageLength(ByteArrayOutputStream byteArrayOutputStream, int read, byte[] messageLenBytes) {
        try {
            byteArrayOutputStream.write(messageLenBytes, 0, read);
            byteArrayOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //deserializing bytes to int value
        return CustomData.deserialize(messageLenBytes);
    }

    /**
     * Reads data from an input stream, converts the data into a string, and returns it.
     *
     * @param byteArrayOutputStream - the ByteArrayOutputStream used for writing the data.
     * @param read - the number of bytes read from the input stream.
     * @param messageLengthBytes - the byte array containing the serialized message length.
     * @return - The data converted into a string.
     */
    public static String readAndConvertBytesToString(ByteArrayOutputStream byteArrayOutputStream, int read, byte[] messageLengthBytes) {
        try {
            byteArrayOutputStream.write(messageLengthBytes, 0, read);
            byteArrayOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(messageLengthBytes);
    }
}


