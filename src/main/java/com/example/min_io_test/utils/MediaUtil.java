package com.example.min_io_test.utils;

public class MediaUtil {

    public static String extractExtension(String mediaName) {
        int lastDotIndex = mediaName.lastIndexOf(".");
        return mediaName.substring(lastDotIndex + 1);
    }

}
