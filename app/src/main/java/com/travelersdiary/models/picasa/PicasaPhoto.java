package com.travelersdiary.models.picasa;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "entry", strict = false)
@NamespaceList({
        @Namespace(reference = "http://www.w3.org/2005/Atom"),
        @Namespace(reference = "http://schemas.google.com/photos/2007", prefix = "gphoto"),
        @Namespace(reference = "http://schemas.google.com/photos/exif/2007", prefix = "exif"),
        @Namespace(reference = "http://search.yahoo.com/mrss/", prefix = "media")})
public class PicasaPhoto {

    private static final String LINK_REL = "http://schemas.google.com/photos/2007#canonical";

    @Element(name = "category", required = false)
    private final static CategoryPhoto CATEGORY = new CategoryPhoto();

    @Element(name = "content", required = false)
    private Content content;

    @ElementList(name = "link", inline = true)
    private List<Link> links;

    public Content getContent() {
        return content;
    }

    public String getSrc() {
        return content.getSrc();
    }

    public String getImageUrl() {
        ArrayList<Link> imageLinks = (ArrayList<Link>) links;

        for (int i = 0; i < imageLinks.size(); i++) {
            if (imageLinks.get(i).getRel().equals(LINK_REL)) {
                return imageLinks.get(i).getHref();
            }
        }

        return null;
    }
}

class CategoryPhoto {
    @Attribute(name = "scheme")
    private final static String SCHEME = "http://schemas.google.com/g/2005#kind";

    @Attribute(name = "term")
    private final static String TERM = "http://schemas.google.com/photos/2007#photo";
}

class Content {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "src")
    private String src;

    public String getSrc() {
        return src;
    }
}

class Link {

    @Attribute(name = "rel")
    private String rel;

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "href")
    private String href;

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }
}