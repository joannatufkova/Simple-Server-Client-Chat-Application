package com.example.myapplication;

import android.annotation.SuppressLint;

import java.io.IOException;

public interface Command {
    @SuppressLint("NotifyDataSetChanged")
    void execute() throws IOException;
}
