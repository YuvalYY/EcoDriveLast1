package com.gmail.liorsiag.ecodrive.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CModelHelper {
    private final static String TAG = "CModelHelper";
    private final static int SUB_ARR_SIZE = 13;
    private final static Comparator<Latlon> LAT_COMPARATOR = new Comparator<Latlon>() {
        public int compare(Latlon a, Latlon b) {
            return Double.compare(a.lat, b.lat);
        }
    };
    private final static Comparator<Latlon> LON_COMPARATOR = new Comparator<Latlon>() {
        public int compare(Latlon a, Latlon b) {
            return Double.compare(a.lon, b.lon);
        }
    };

    private Context mContext;
    private ArrayList<Latlon> mRouteLatlons;
    private ArrayList<Latlon> mLatSortedLatlons;
    private ArrayList<Latlon> mLonSortedLatlons;
    private boolean mInRoute;
    private DataManager mDataManager;
    private int mCurrPos;

    public CModelHelper(Context c) {
        mContext = c;
        mInRoute = false;
        mDataManager = DataManager.instance();
    }

    public void prepareDrive(String filePath) {
        mInRoute = false;
        loadFile(filePath);
        prepareSortedArrays();
    }

    private void loadFile(String filePath) {
        mRouteLatlons = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException ignored) {
        }
        String line;
        try {
            Latlon last = null;
            if ((line = br.readLine()) != null) {
                String[] sLine = line.split(",");
                last = new Latlon(Double.parseDouble(sLine[0]), Double.parseDouble(sLine[1]));
                last.addSpeed(Integer.parseInt(sLine[2]), Integer.parseInt(sLine[3]));
                mRouteLatlons.add(last);

            }
            while ((line = br.readLine()) != null) {
                String[] sLine = line.split(",");
                if (last.lat != Double.parseDouble(sLine[0]) || last.lon != Double.parseDouble(sLine[1])) {
                    last = new Latlon(Double.parseDouble(sLine[0]), Double.parseDouble(sLine[1]));
                    mRouteLatlons.add(last);
                }
                last.addSpeed(Integer.parseInt(sLine[2]), Integer.parseInt(sLine[3]));
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "Error reading model file", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareSortedArrays() {
        mLatSortedLatlons = new ArrayList<>(mRouteLatlons.size());
        mLonSortedLatlons = new ArrayList<>(mRouteLatlons.size());
        for (Latlon ll : mRouteLatlons) {
            mLatSortedLatlons.add(ll);
            mLonSortedLatlons.add(ll);
        }
        Collections.sort(mLatSortedLatlons, LAT_COMPARATOR);
        Collections.sort(mLonSortedLatlons, LON_COMPARATOR);
    }

    public void speedUpdate(double lat, double lon, int speed) {
        Latlon point = new Latlon(lat, lon);
        if (mInRoute) {
            int nextSpeed = getNextSpeed(point, speed);
            int compare = Integer.compare(nextSpeed, (speed + 2) / 5 * 5);
            if (compare > 0)
                mDataManager.updateDesiredSpeed(String.valueOf(nextSpeed), Color.GREEN);
            else if (compare == 0)
                mDataManager.updateDesiredSpeed(String.valueOf(nextSpeed), Color.BLUE);
            else
                mDataManager.updateDesiredSpeed(String.valueOf(nextSpeed), Color.RED);
        } else {
            locateInRoute(point);
        }
    }

    public int getNextSpeed(Latlon point, int speed) {
        double minDiff = Double.MAX_VALUE;
        int closesti = -1;
        int length = Math.min(mCurrPos + 10, mRouteLatlons.size() - 1);
        for (int i = Math.max(0, mCurrPos - 2); i <= length; i++) {
            if (Latlon.distance(point, mRouteLatlons.get(i)) < minDiff) {
                minDiff = Latlon.distance(point, mRouteLatlons.get(i));
                closesti = i;
            }
        }
        mCurrPos = closesti;
        Log.d(TAG, "getNextSpeed: "+mCurrPos);
        return mRouteLatlons.get(closesti).getSpeed(speed);
    }

    private void locateInRoute(Latlon point) {
        int[] latIndexes = getSortedIndexes(mLatSortedLatlons, point, LAT_COMPARATOR);
        int[] lonIndexes = getSortedIndexes(mLonSortedLatlons, point, LON_COMPARATOR);
        for (int i = latIndexes[0]; i <= latIndexes[1]; i++) {
            for (int j = lonIndexes[0]; j <= lonIndexes[1]; j++) {
                if (mLatSortedLatlons.get(i).equals(mLonSortedLatlons.get(j))) {
//                    Log.d(TAG, "locateInRoute: "+(Latlon.distance(mLatSortedLatlons.get(i), point)<= 0.05));
                    if (Latlon.distance(mLatSortedLatlons.get(i), point) <= 0.05) {
                        Log.d(TAG, "locateInRoute: ");
                        Latlon toFind = mLatSortedLatlons.get(i);
                        for (int k = 0; k < mRouteLatlons.size(); k++) {
                            if (toFind.equals(mRouteLatlons.get(k))) {
                                mCurrPos = k;
                                mDataManager.sayText("Drive initiated");
                                mInRoute = true;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private int[] getSortedIndexes(ArrayList<Latlon> latlons, Latlon point, Comparator<Latlon> comp) {
        int low = 0;
        int high = latlons.size() - 1;
        if (comp.compare(point, latlons.get(low)) < 0) {
            high = SUB_ARR_SIZE - 1;
        }
        else if (comp.compare(point, latlons.get(high)) > 0)
            low = latlons.size() - SUB_ARR_SIZE;
        else
            while (high - low > SUB_ARR_SIZE) {
                int mid = (high + low) / 2;
                if (comp.compare(point, latlons.get(mid)) < 0)
                    high = mid - 1;
                else if (comp.compare(point, latlons.get(mid)) > 0)
                    low = mid + 1;
                else {
                    low = Math.max(mid - 6, 0);
                    high = Math.min(mid + 6, latlons.size() - 1);
                    break;
                }
            }
        return new int[]{low, high};
    }
}
