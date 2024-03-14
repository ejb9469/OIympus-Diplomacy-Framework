package indexing;

import indexing.details.PlayerDetails;

public class PlayerID implements Tag {

    private final String firstName;
    private final String middleInitial;
    private final String lastName;
    private final PlayerDetails playerDetails = PlayerDetails.NONE;

    public PlayerID(String firstName, String middleInitial, String lastName) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
    }

    public PlayerID(String firstName, String lastName) {
        this(firstName, "", lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public TagDetails getDetails() {
        return this.playerDetails;
    }

    @Override
    public String toString() {
        if (this.getMiddleInitial().isEmpty())
            return this.getFirstName() + " " + this.getLastName();
        else
            return this.getFirstName() + " " + this.getMiddleInitial() + " " + this.getLastName();
    }

}