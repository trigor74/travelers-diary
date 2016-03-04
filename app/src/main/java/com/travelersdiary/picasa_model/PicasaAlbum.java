package com.travelersdiary.picasa_model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry", strict = false)
public class PicasaAlbum {
    @Element(name = "title")
    private String title;

    @Attribute(name = "xmlns", required = false)
    private final static String XMLNS = "http://www.w3.org/2005/Atom";

    @Attribute(name = "xmlns:media", required = false)
    private final static String XMLNS_MEDIA = "http://search.yahoo.com/mrss/";

    @Attribute(name = "xmlns:gphoto", required = false)
    private final static String XMLNS_GPHOTO = "http://schemas.google.com/photos/2007";

    @Element(name = "category", required = false)
    private final static Category CATEGORY = new Category();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

class Category {
    @Attribute(name = "scheme")
    private final static String SCHEME = "http://schemas.google.com/g/2005#kind";

    @Attribute(name = "term")
    private final static String TERM = "http://schemas.google.com/photos/2007#album";
}

