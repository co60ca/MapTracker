package ca.uoguelph.pspenler.maptracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Configuration implements Parcelable {
    private String experimentName;
    private String configName;
    private String configFile;
    private String resultsServer;
    private String beaconLabel;
    private String imagePath;
    private ArrayList<Landmark> landmarks;
    private int beaconHeight;
    private int validConfig;
    private int configLoaded;
    private String errorMsg = "";

    Configuration(){
        experimentName = "";
        configFile = "";
        resultsServer = "";
        beaconLabel = "";
        imagePath = "";
        landmarks = null;
        beaconHeight = 0;
        validConfig = 0;
    }

    public void initConfig(String name, String confFile, String server, String label, String height) throws Exception{
        //Checks that experiment name is not empty
        if(name.equals("")) {
            throw new Exception("Experiment name cannot be empty");
        }else {
            experimentName = name;
        }

        //Checks that config file exists


        loadConfig(confFile);
        while(configLoaded == 0);
        if(configLoaded == 2){
            throw new Exception(errorMsg);
        }
        configFile = confFile;


        resultsServer = server;

        //Checks that beacon label is not empty
        if(label.equals("")) {
            throw new Exception("Beacon label cannot be empty");
        }else {
            beaconLabel = label;
        }

        //Checks that beacon height is an integer
        try{
            beaconHeight = Integer.parseInt(height);
        }
        catch(Exception e){
            throw new Exception("Beacon height is not an integer");
        }

        validConfig = 1;
    }

    public int isValid(){
        return validConfig;
    }

    public String getName(){
        return experimentName;
    }

    public String getConfigFile(){
        return configFile;
    }

    public String getResultsServer(){
        return resultsServer;
    }

    public String getBeaconLabel(){
        return beaconLabel;
    }

    public int getBeaconHeight(){
        return beaconHeight;
    }

    public String getImagePath() {
        return imagePath;
    }

    public ArrayList<Landmark> getLandmarks() {
        return landmarks;
    }

    private void setLandmarks(ArrayList<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public void setConfigName(String configName) { this.configName = configName; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public void invalidate(){ this.validConfig = 0; }

    private void loadConfig(String config){
        configLoaded = 0;

        @SuppressLint("HandlerLeak") final Handler mHandler = new Handler(){

            public void handleMessage(Message msg) {
                Log.d("Message", "received");
                Bundle b;
                if(msg.what == 1){
                    Log.e("Message", "TYPE 1");
                    b = msg.getData();

                    configName = b.getString("configName");
                    imagePath = b.getString("imagePath");
                    landmarks = b.getParcelableArrayList("landmarks");
                    configLoaded = 1;
                }
                if(msg.what == 2){
                    Log.e("Message", "TYPE 2");
                    b = msg.getData();

                    errorMsg = b.getString("errorMsg");
                    configLoaded = 2;
                }
                super.handleMessage(msg);
            }
        };

        Thread thread = new WebThread(mHandler, config);
        thread.start();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.experimentName);
        dest.writeString(this.configName);
        dest.writeString(this.configFile);
        dest.writeString(this.resultsServer);
        dest.writeString(this.beaconLabel);
        dest.writeString(this.imagePath);
        dest.writeList(this.landmarks);
        dest.writeInt(this.beaconHeight);
        dest.writeInt(this.validConfig);
    }

    protected Configuration(Parcel in) {
        this.experimentName = in.readString();
        this.configName = in.readString();
        this.configFile = in.readString();
        this.resultsServer = in.readString();
        this.beaconLabel = in.readString();
        this.imagePath = in.readString();
        this.landmarks = new ArrayList<>();
        in.readList(this.landmarks, Landmark.class.getClassLoader());
        this.beaconHeight = in.readInt();
        this.validConfig = in.readInt();
    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration createFromParcel(Parcel source) {
            return new Configuration(source);
        }

        @Override
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
}
