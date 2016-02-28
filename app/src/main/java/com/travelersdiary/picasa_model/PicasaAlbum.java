package com.travelersdiary.picasa_model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry", strict = false)
public class PicasaAlbum {
    @Element(name = "title")
    private String title;

    @Element(name = "summary", required = false)
    private String summary;

    @Element(name = "rights")
    private String rights;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }
}
