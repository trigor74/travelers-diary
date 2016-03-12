package com.travelersdiary.models.picasa;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "entry", strict = false)
@NamespaceList({
        @Namespace(reference = "http://www.w3.org/2005/Atom"),
        @Namespace(reference = "http://schemas.google.com/photos/2007", prefix = "gphoto"),
        @Namespace(reference = "http://search.yahoo.com/mrss/", prefix = "media")})
public class PicasaAlbum {
    @Element(name = "title")
    private String title;

    @Path("id[1]")
    @Text(required = false)
    private String albumPath;

    @Element(name = "category", required = false)
    private final static Category CATEGORY = new Category();

    @Path("gphoto:id[2]")
    @Text(required = false)
    private String albumId;

    public PicasaAlbum() {
    }

    public PicasaAlbum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }
}

class Category {
    @Attribute(name = "scheme")
    private final static String SCHEME = "http://schemas.google.com/g/2005#kind";

    @Attribute(name = "term")
    private final static String TERM = "http://schemas.google.com/photos/2007#album";
}
