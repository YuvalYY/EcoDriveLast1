package com.gmail.liorsiag.ecodrive.model;

import java.util.ArrayList;

public class Latlon {
    final static double R = 6371;

    public double lat;
    public double lon;
    protected ArrayList<Integer> mCurrentWSeeds;
    protected ArrayList<Integer> mNextSpeeds;

    public Latlon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        mCurrentWSeeds = new ArrayList<>();
        mNextSpeeds = new ArrayList<>();
    }

    public static double distance(Latlon l1, Latlon l2) {
        double lon1 = Math.toRadians(l1.lon);
        double lon2 = Math.toRadians(l2.lon);
        double lat1 = Math.toRadians(l1.lat);
        double lat2 = Math.toRadians(l2.lat);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dlon / 2), 2);

        return 2 * Math.asin(Math.sqrt(a)) * R;
    }

    public void addSpeed(int currentSpeed, int nextSpeed) {
        mCurrentWSeeds.add(currentSpeed);
        mNextSpeeds.add(nextSpeed);
    }

    public boolean equals(Latlon l) {
        return this.lat == l.lat && this.lon == l.lon;
    }

    public int getSpeed(int currentSpeed){
        int minDiff=Integer.MAX_VALUE;
        int mini=-1;
        for (int i = 0; i < mCurrentWSeeds.size(); i++) {
            if(Math.abs(currentSpeed-mCurrentWSeeds.get(i))<minDiff){
                minDiff=Math.abs(currentSpeed-mCurrentWSeeds.get(i));
                mini=i;
            }
        }
        return mNextSpeeds.get(mini);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("(" + lat + "," + lon + ") [");
        if (mCurrentWSeeds.size() != 0) {
            for (int i = 0; i < mCurrentWSeeds.size(); i++)
                ret.append(mCurrentWSeeds.get(i)).append(":").append(mNextSpeeds.get(i)).append(",");
            ret.setLength(ret.length()-1);
        }
        return ret.append("]").toString();
    }
}
