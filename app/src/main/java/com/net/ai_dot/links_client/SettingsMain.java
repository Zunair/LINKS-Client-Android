package com.net.ai_dot.links_client;

/**
 * Created by zfayaz on 5/5/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;


public class SettingsMain {

    Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    private static final String PREFER_NAME = "settingsMain";
    public static final String KEY_IP = "IP";
    public static final String KEY_LKEY = "LKEY";
    public static final String KEY_COMMAND = "COMMAND";
    public static final String KEY_PORT = "PORT";

    public SettingsMain(Context context) {
        this.context = context;
        setPreferences();
    }

    private void setPreferences(){
        preferences = context.getSharedPreferences(PREFER_NAME, context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void save() {
        editor.commit();
    }

    public void cleanPreferences(){
        editor.clear();
        editor.commit();
    }

    public void setIP(String IP){
        editor.putString(KEY_IP, IP);
        save();
    }

    public String getIP(){
        return preferences.getString(KEY_IP, "71.225.62.19");
    }

    public void setKey(String Key){
        editor.putString(KEY_LKEY, Key);
        save();
    }

    public String getKey(){

        return preferences.getString(KEY_LKEY, "ABC1234");
    }

    public void setCommand(String command) {
        editor.putString(KEY_COMMAND, command);
        save();
    }

    public String getCommand(){
        return preferences.getString(KEY_COMMAND, "[Speak(\"Client Test\")]");
    }

    public void setPort(String port) {
        editor.putString(KEY_PORT, port);
        save();
    }

    public String getPort() {
        return preferences.getString(KEY_PORT, "54657");
    }
}