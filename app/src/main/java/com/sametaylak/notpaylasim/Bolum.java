package com.sametaylak.notpaylasim;

import java.io.Serializable;

public class Bolum implements Serializable {

    private int mBolumID;
    private int mUniversiteID;
    private String mBolumAdi;

    public Bolum(int bolumID, int universiteID, String bolumAdi) {
        this.mBolumID = bolumID;
        this.mUniversiteID = universiteID;
        this.mBolumAdi = bolumAdi;
    }

    public String getAd() {
        return this.mBolumAdi;
    }

    public int getID() {
        return this.mBolumID;
    }

    public int getUniversiteID() {
        return this.mUniversiteID;
    }

}
