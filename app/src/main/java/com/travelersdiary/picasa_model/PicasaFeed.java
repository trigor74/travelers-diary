package com.travelersdiary.picasa_model;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "feed", strict = false)
public class PicasaFeed {
    @ElementList(name ="entry", inline = true)
    private List<PicasaAlbum> picasaAlbumList;

    public List<PicasaAlbum> getPicasaAlbumList() {
        return picasaAlbumList;
    }

    public void setPicasaAlbumList(List<PicasaAlbum> picasaAlbumList) {
        this.picasaAlbumList = picasaAlbumList;
    }
}



