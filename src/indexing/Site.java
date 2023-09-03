package indexing;

public class Site implements Tag {

    private final String address;
    private final String city;
    private final String region;
    private final String country;

    public Site(String address, String city, String region, String country) {
        this.address = address;
        this.city = city;
        this.region = region;
        this.country = country;
    }

    @Override
    public String toString() {
        return this.address + ", " + this.city + " " + this.region + ", " + this.country;
    }

}