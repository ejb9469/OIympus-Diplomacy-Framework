package indexing;

import java.util.Date;

public class Event extends Tag {

    private String name;
    private Site site;
    private Date date;

    public Event(String name, Site site, Date date) {
        this.name = name;
        this.site = site;
        this.date = date;
    }

    @Override
    public String toString() {
        return "[" + this.name + "]\n[" + this.site + "]\n[" + this.date + "]";
    }

}