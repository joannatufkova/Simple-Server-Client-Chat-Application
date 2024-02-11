package com.example.myapplication;

public interface ProgressListener {
    void onProgressUpdate(long bytesTransferred, long totalBytes, double speed);
}
