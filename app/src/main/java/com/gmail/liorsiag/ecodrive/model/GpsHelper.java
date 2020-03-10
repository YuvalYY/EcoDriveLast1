package com.gmail.liorsiag.ecodrive.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.gmail.liorsiag.ecodrive.R;

public class GpsHelper {
    private final static String TAG="GpsHelper";
    private DataManager mDataManager;
    private Context mContext;
    private LocationListener mLocationListener;
    private boolean mIsActive=false;

    GpsHelper(Context context){
        mDataManager=DataManager.instance();
        mContext=context;
        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mDataManager.updateGps(new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())});
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    public boolean isActive(){
        return mIsActive;
    }

    public String getGpsStatus(){
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return mContext.getResources().getString(R.string.searching);
        else
            return mContext.getResources().getString(R.string.off);
    }

    void registerGpsListener(){
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null){
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                mIsActive=true;
            }
        }
    }

    void unregisterGpsListener(){
        Log.d(TAG, "unregisterGpsListener: called");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null){
                lm.removeUpdates(mLocationListener);
                mIsActive=false;
            }
        }
    }
}
