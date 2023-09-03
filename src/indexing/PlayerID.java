package indexing;

public class PlayerID implements Tag {

    private final String firstName;
    private final String middleInitial;
    private final String lastName;
    // TODO: Alias

    public PlayerID(String firstName, String middleInitial, String lastName) {
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
    }

    public PlayerID(String firstName, String lastName) {
        this(firstName, "", lastName);
    }

    @Override
    public String toString() {
        if (middleInitial.isEmpty())
            return this.firstName + " " + this.lastName;
        else
            return this.firstName + " " + this.middleInitial + " " + this.lastName;
    }

}