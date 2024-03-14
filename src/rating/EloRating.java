package rating;

public class EloRating {

    public static final int MINIMUM = 0;
    public static final int MEDIAN = 900;
    public static final int MAXIMUM = 2000;

    private int rating;

    public EloRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

}
