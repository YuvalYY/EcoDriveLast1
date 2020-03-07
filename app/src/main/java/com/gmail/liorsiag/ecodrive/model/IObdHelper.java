package com.gmail.liorsiag.ecodrive.model;

public class IObdHelper implements ObdHelper {
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void startRecording() {

    }

    @Override
    public void stopRecording() {

    }
}
