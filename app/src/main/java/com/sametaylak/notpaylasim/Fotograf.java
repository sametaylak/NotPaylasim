package com.sametaylak.notpaylasim;

import java.io.Serializable;
import java.util.List;

public class Fotograf implements Serializable{

    int mID;
    String mTitle;
    String mPhotos;
    int mType;

    public Fotograf (int id, String title, String photos, int type) {
        this.mID = id;
        this.mTitle = title;
        this.mPhotos = photos;
        this.mType = type;
    }

    public int getID() {
        return this.mID;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getPhotos() {
        return this.mPhotos;
    }

    public int getType() {
        return this.mType;
    }

}
