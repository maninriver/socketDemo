package com.example.feng.asynctaskdemo;

import java.io.Closeable;
import java.io.IOException;

class MyUtils {
    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException error) {
            System.out.println(error.getMessage());
        }
    }
}
