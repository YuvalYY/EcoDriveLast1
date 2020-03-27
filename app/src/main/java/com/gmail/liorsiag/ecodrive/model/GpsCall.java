package com.gmail.liorsiag.ecodrive.model;

public class GpsCall{
    public long time;
    public double lat;
    public double lon;

    public GpsCall(long time, double lat, double lon) {
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    public String getLatLonString(){
        return lat+"\n"+lon;
    }

    public String[] toStringArray(){
        return new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(this.lat), String.valueOf(this.lon)};
    }
}
