package com.myapps.mobilesecurityapp.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MSPV {
    private static final String SP_FILE_NAME = "SP_FILE_NAME";
    private static final String SP_ID_COUNTER = "SP_ID_COUNTER";
    private static final String SP_PRIVACY = "SP_PRIVACY";
    private SharedPreferences prefs = null;
    private static MSPV me;
    private String RECORDS = "RECORDS";

    private MSPV(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }
    public static void init(Context context) {
        if (me == null) {
            me = new MSPV(context);
        }
    }
    public static MSPV getMe() {
        return me;
    }

    public void saveCounter(int value) { //new
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SP_ID_COUNTER, value);
        editor.apply();
    }
    public int getNextID() {
        int counter = readInt(SP_ID_COUNTER, 0);
        int nextID = counter + 1;
        saveCounter(nextID);
        return nextID;
    }

    public int readInt(String key, int def) {
        int value = prefs.getInt(key, def);
        return value;
    }
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public String readString(String key, String def) {
        String value = prefs.getString(key, def);
        return value;
    }
    public void saveAudios(AudioList audioList){
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(audioList);
        this.getMe().saveString(RECORDS,json);
        editor.apply();
    }
    public AudioList readAudios(){
        String json = this.getMe().readString(RECORDS,null);
        if(json == null) {
            saveAudios(new AudioList().setAudios(new ArrayList<Audio>()));
            return readAudios();
        }
        return new Gson().fromJson(json, AudioList.class);
    }

    public void savePrivacyPolicy(Boolean acceptPolicy){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SP_PRIVACY, acceptPolicy);
        editor.apply();
    }

    public Boolean readPrivacyPolicy(){
        Boolean acceptPolicy = prefs.getBoolean("SP_PRIVACY",false);
        return acceptPolicy;
    }




}


