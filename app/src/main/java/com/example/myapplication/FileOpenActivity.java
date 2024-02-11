package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * A class which is responsible for opening a received file using an appropriate application.
 * The file path and MIME type are passed through the intent to this activity, which then creates an intent to open the file.
 */
public class FileOpenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_open);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("FILE_PATH");
        String mimeType = intent.getStringExtra("MIME_TYPE");

        Uri fileUri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID + ".provider", new File(filePath));

        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
        openFileIntent.setDataAndType(fileUri,mimeType);
        openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openFileIntent);
    }
}
