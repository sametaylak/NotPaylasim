package com.sametaylak.notpaylasim;

import java.io.Serializable;

public class Universite implements Serializable {

    private int mUniversiteID;
    private String mUniversiteAdi;

    public Universite(int universiteID, String universiteAdi) {
        this.mUniversiteAdi = universiteAdi;
        this.mUniversiteID = universiteID;
    }

    public String getAd() {
        return this.mUniversiteAdi;
    }

    public int getID() {
        return this.mUniversiteID;
    }

}
