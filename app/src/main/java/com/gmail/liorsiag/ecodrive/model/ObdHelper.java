package com.gmail.liorsiag.ecodrive.model;

public interface ObdHelper {
    boolean isConnected();

    boolean connect();

    void disconnect();

    void startRecording();

    void stopRecording();
}
