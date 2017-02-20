package com.barman.anuran.googlefitapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Anuran on 2/14/2017.
 */

public class SharedPrefManager {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;
    final String SHARED_PRF_FILE_NAME="trackPref";
    final String KEY_FITNESS_TRACKING="fitness_tracking";

    public SharedPrefManager(Context context) {
        this.context=context;
        sharedPreferences=context.getSharedPreferences(SHARED_PRF_FILE_NAME,Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void setTracking(boolean isToTrackOrNot){
        editor.putBoolean(KEY_FITNESS_TRACKING,isToTrackOrNot);
        editor.commit();
    }

    public boolean getTracking(){
        boolean isToTrackOrNot=sharedPreferences.getBoolean(KEY_FITNESS_TRACKING,false);
        return isToTrackOrNot;
    }
}
