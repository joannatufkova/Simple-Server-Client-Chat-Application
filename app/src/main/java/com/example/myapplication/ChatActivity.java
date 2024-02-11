package com.example.myapplication;

import static com.example.myapplication.CustomData.readAndConvertBytesToString;
import static com.example.myapplication.CustomData.readAndDeserializeMessageLength;
import static com.example.myapplication.FileService.input;
import static com.example.myapplication.FileService.outputStream;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements ProgressListener {
    private static final int PICK_FILE_REQUEST = 1;
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 112;
    private static final String FILE_COMMAND = "File";
    private static final String MESSAGE_COMMAND = "Message";

    @SuppressLint("StaticFieldLeak")
    public static ChatActivity instance;
    private EditText messageEditText;
    @SuppressLint("StaticFieldLeak")
    protected static MessageAdapter messageAdapter;
    static final List<Message> messages = new ArrayList<>();
    private static RecyclerView recyclerView;
    private TextView progressText;
    public static final Handler handler = new Handler(Looper.getMainLooper());
    @SuppressLint("StaticFieldLeak")
    static Context context;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkStoragePermission();
        }

        initializeViews();

        setupMessageAdapter();

        setupSendButton();

        setupAttachButton();

        setupMessageAdapterClickListener();
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void checkStoragePermission() {
        if (checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_MANAGE_EXTERNAL_STORAGE);
        }
    }
    private void initializeViews() {
        instance = this;
        context = getApplicationContext();
        recyclerView = findViewById(R.id.recycler_view);
        messageEditText = findViewById(R.id.chat_input);
        progressText = findViewById(R.id.progressText);
    }
    private void setupMessageAdapter() {
        messageAdapter = new MessageAdapter(ChatActivity.this, messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        recyclerView.smoothScrollToPosition(messages.size());
    }
    private void setupSendButton() {
        ImageButton sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(v -> {
            String messageContent = messageEditText.getText().toString().trim();
            messageEditText.setText("");

            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
            }
        });
    }
    private void sendMessage(String messageContent) {
        handler.post(() -> {
            SendMessageCommand sendMessageCommand = new SendMessageCommand(outputStream, messageContent);
            sendMessageCommand.execute();
        });

        Message message = new Message(FileService.username, messageContent, System.currentTimeMillis(), MessageType.MESSAGE_SENT);
        messages.add(message);
        messageAdapter.notifyItemInserted(messages.size() - 1);
    }
    static void displayMessage(Message message) {
        messages.add(message);
        recyclerView.smoothScrollToPosition(messages.size());
        messageAdapter.notifyItemInserted(messages.size() - 1);
    }
    private void setupAttachButton() {
        ImageButton attachButton = findViewById(R.id.attach_button);

        attachButton.setOnClickListener(v -> {
            launchFilePicker();
        });
    }
    private void launchFilePicker() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST);
    }
    private void setupMessageAdapterClickListener() {
        messageAdapter.setOnItemClickListener((view, position) -> {
            Message message = messages.get(position);
            if (message.getMessageType() == MessageType.FILE_RECEIVED || message.getMessageType() == MessageType.FILE_SENT) {
                launchFileOpenActivity(message.getFilePath());
            }
        });
    }
    private void launchFileOpenActivity(String filePath) {
        String mimeType = getMimeType(filePath);
        File file = new File(filePath);
        Intent intent = new Intent(ChatActivity.this, FileOpenActivity.class);
        intent.putExtra("FILE_PATH", file.getAbsolutePath());
        intent.putExtra("MIME_TYPE", mimeType);
        startActivity(intent);
    }
    private static String getMimeType(String file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(file)).toString());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    /**
     *
     * @return
     */
    public static Thread readCommand() {
        return new Thread(() -> {
            while (true) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (input.available() > 0) {
                        while (true) {
                            int read;
                            try {
                                if (!(input.available() > 0)) {
                                    break;
                                }
                                //converting command length byte[] to int
                                int cmdLength = readAndDeserializeNextElementSize(byteArrayOutputStream);

                                Log.d("ChatActivity", "Command length: " + cmdLength);
                                byte[] bytesOfCommand = new byte[cmdLength];

                                read = input.read(bytesOfCommand, 0, cmdLength);

                                //convert command to string
                                String commandName = readAndConvertBytesToString(byteArrayOutputStream, read, bytesOfCommand);

                                final Command[] command = {null};

                                switch (commandName) {
                                    case MESSAGE_COMMAND:
                                        command[0] = new ReceiveMessageCommand();
                                        break;
                                    case FILE_COMMAND:
                                        command[0] = new ReceiveFileCommand();
                                        break;
                                }

                                if (command[0] != null) {
                                    command[0].execute();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     * @param bos
     * @return
     * @throws IOException
     */
    static int readAndDeserializeNextElementSize(ByteArrayOutputStream bos) throws IOException {
        byte[] fileBytesArray = new byte[4];
        int bytesRead = 0;

        while(bytesRead < 4){
            int readResult = input.read(fileBytesArray,bytesRead, 4 - bytesRead);
            if(readResult == -1){
                break;
            }
            bytesRead += readResult;
        }
        if(bytesRead != 4){
            throw new IOException("Failed to read 4 bytes for the command length");
        }
        return readAndDeserializeMessageLength(bos,bytesRead,fileBytesArray);
    }

    static void showCustomProgressDialog(ChatActivity activity, File file) {
        activity.runOnUiThread(() -> {
            // Check if the activity is not finishing or destroyed
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                // Inflate the custom progress dialog layout
                LayoutInflater inflater = activity.getLayoutInflater();
                View view = inflater.inflate(R.layout.custom_progress_dialog, null);

                // Create a dialog and set the custom view
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setView(view);
                builder.setCancelable(false);

                AlertDialog progressDialog = builder.create();

                // Show the dialog only when the activity is running
                progressDialog.show();

                // Set the retry button listener
                Button retryButton = view.findViewById(R.id.retry_button);
                retryButton.setOnClickListener(v -> {
                    // Retry by calling the execute method again
                    ReceiveFileCommand receiveFileCommand = new ReceiveFileCommand();
                    try {
                        receiveFileCommand.execute();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    progressDialog.dismiss();
                });

                // Set the cancel button listener
                Button cancelButton = view.findViewById(R.id.cancel_button);
                cancelButton.setOnClickListener(v -> {
                    progressDialog.dismiss();
                    try{
                        input.close();
                    } catch (IOException e){
                        throw new RuntimeException();
                    }
                    if(file.exists()){
                        file.delete();
                    }
                });
            }
        });
    }
    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String fileName = "";
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    // This method will be called when the file picker dialog is closed
    @SuppressLint({"Recycle", "NotifyDataSetChanged"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_FILE_REQUEST && data != null)) {
            if (resultCode == RESULT_OK) {
                //get file details
                Uri file = data.getData();
                String fileName = getFileName(file);
                System.out.println("File name: " + fileName);
                String filePath = file.getPath();
                int fileSize = fileName.length();

                // Create and display the progress bar
                ProgressBar progressBar = findViewById(R.id.progressBar1);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);

                // Create a progress listener
                ProgressListener progressListener = (bytesTransferred, totalBytes, speed) -> {
                    // Update UI with the progress
                    runOnUiThread(() -> {
                        // Update progress bar
                        int progress = (int) (bytesTransferred * 100 / totalBytes);
                        progressBar.setProgress(progress);

                        // Update progress text
                        String progressTextStr = String.format(Locale.getDefault(),
                                "Transferred: %d/%d MB\nSpeed: %.2f MB/s",
                                bytesTransferred / (1024 * 1024),
                                totalBytes / (1024 * 1024),
                                speed);
                        progressText.setText(progressTextStr);
                    });
                };

                handler.postDelayed(() -> {
                    // Update the progress bar while the file is being sent
                    for (int progress = 0; progress <= 100; progress += 10) {
                        try {
                            Thread.sleep(100); // Simulate a delay in sending the file
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        final int currentProgress = progress;
                        runOnUiThread(() -> progressBar.setProgress(currentProgress));
                    }

                    ContentResolver contentResolver = getContentResolver();

                    SendFileCommand sendFileCommand = new SendFileCommand(FILE_COMMAND,contentResolver, outputStream, data,progressListener);
                    sendFileCommand.execute();

                    Message message = new Message(MessageType.FILE_SENT, filePath,fileName, fileSize);
                    messages.add(message);

                    messageAdapter.notifyDataSetChanged();

                    // Hide the progress bar after the file is sent
                    progressBar.setVisibility(View.GONE);
                },1000);
            }
        }
    }
    @Override
    public void onProgressUpdate(long bytesTransferred, long totalBytes, double speed) {
        // This method is already being implemented within the anonymous class inside onActivityResult.
    }
}



