package com.example.myapplication;

import static com.example.myapplication.ChatActivity.readCommand;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A service class for handling file-related operations over a socket connection.
 */
public class FileService extends Service {
    protected static String username;
    private static final int PORT = 7777;
    private static ServerSocket serverSocket;
    private static Socket server;
    protected static Socket client;
    protected static DataOutputStream outputStream;
    protected static DataInputStream input;

    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            serverSocket = new ServerSocket(PORT);

            System.out.println(serverSocket);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return START_STICKY;
    }

    /**
     * Waits for a connection from a client.
     *
     * @param name - the username for the connection.
     * @return - A new thread for handling the connection.
     */
    public static Thread waitForConnection(String name) {
        //socket connection server side
        Log.i("Server", "Listening for client connection.");

        return new Thread(() -> {
            try {
                server = serverSocket.accept();

                // Get the client's IP address
                String ipAddress = server.getInetAddress().getHostAddress();

                Log.i("Server", "Client connected from: " + ipAddress);

                input = new DataInputStream(server.getInputStream());

                outputStream = new DataOutputStream(server.getOutputStream());

                FileService.username = name;

                readCommand().setDaemon(true);
                readCommand().start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Connects to the specified IP address and port.
     *
     * @param ip - the IP address of the server to connect to.
     * @param port - the port number of the server to connect to.
     * @param username - the username for the connection.
     * @throws IOException - if an I/O error occurs when connecting.
     */
    public static void connect(String ip, int port, String username) throws IOException {
        serverSocket.close();

        try {
            //socket connection client side
            FileService.username = username;
            client = new Socket(ip, port);

            String serverIPAddress = client.getLocalAddress().getHostAddress();
            Log.i("Client", "Connected to server at: " + serverIPAddress);

            input = new DataInputStream(client.getInputStream());
            outputStream = new DataOutputStream(client.getOutputStream());

            readCommand().setDaemon(true);
            readCommand().start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            stopConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.onDestroy();
    }

    /**
     * Stops the connection and closes associated resources.
     *
     * @throws IOException - if an I/O error occurs when closing the connection.
     */
    public static void stopConnection() throws IOException {
        if (client != null)
            client.close();
        if (input != null)
            input.close();
        if (server != null)
            server.close();
    }
}