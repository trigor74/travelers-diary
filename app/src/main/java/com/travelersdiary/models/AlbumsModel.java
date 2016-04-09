package com.travelersdiary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.LinkedHashSet;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumsModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public LinkedHashSet<String> folderImages;
    protected String folderName;
    protected String folderImagePath;

    public AlbumsModel() {
        folderImages = new LinkedHashSet<>();
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderImagePath() {
        return folderImagePath;
    }

    public void setFolderImagePath(String folderImagePath) {
        this.folderImagePath = folderImagePath;
    }

}
