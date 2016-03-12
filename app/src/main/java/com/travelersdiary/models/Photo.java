package com.travelersdiary.models;

import java.io.Serializable;

public class Photo implements Serializable {

    private String localUri;
    private String picasaUri;

    public Photo() {
    }

    public Photo(String localUri) {
        this.localUri = localUri;
    }

    public Photo(String localUri, String picasaUri) {
        this.localUri = localUri;
        this.picasaUri = picasaUri;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getPicasaUri() {
        return picasaUri;
    }

    public void setPicasaUri(String picasaUri) {
        this.picasaUri = picasaUri;
    }
}
