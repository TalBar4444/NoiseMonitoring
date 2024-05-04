package com.myapps.mobilesecurityapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AudioList implements Serializable {

    private ArrayList<Audio> audios; // Use ArrayList or a subclass of List

    public AudioList() {
        this.audios = new ArrayList<>();
    }

    public AudioList setAudios(ArrayList<Audio> audios){
        this.audios = audios;
        return this;
    }

    public void addAudio(Audio audio) {
        audios.add(audio);

    }

    public int getSize() {
        return audios.size();
    }

    public ArrayList<Audio> getAudios() {
        return audios;
    }

    public static ArrayList<Audio> getAllAudios() {
        AudioList audioList = MSPV.getMe().readAudios();
        return audioList.getAudios();
    }
}
