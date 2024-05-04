package com.myapps.mobilesecurityapp.utils;

import com.myapps.mobilesecurityapp.model.MSPV;

public class RecordingUtil {

    private static final String BASE_NAME = "audio";
    //private static final String EXTENSION = ".wav"; // Adjust the extension based on your recording format
    private static int counter; // Keeps track of the sequence number

    public static String generateUniqueFileName() {
        synchronized (RecordingUtil.class) { // Thread-safe counter increment
            counter = MSPV.getMe().getNextID();
        }
        String filename = String.format("%s_%02d", BASE_NAME, counter);
        return filename;
    }
}