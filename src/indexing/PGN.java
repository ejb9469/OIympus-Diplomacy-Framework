package indexing;

public class PGN {

    public SevenTagRoster sevenTagRoster;
    public Tag[] optionalTags;
    public Movetext moveText;

    public PGN(SevenTagRoster sevenTagRoster, Tag[] optionalTags, Movetext moveText) {
        this.sevenTagRoster = sevenTagRoster;
        this.optionalTags = optionalTags;
        this.moveText = moveText;
    }

    public PGN(SevenTagRoster sevenTagRoster, Tag[] optionalTags, String moveText) {
        this(sevenTagRoster, optionalTags, new Movetext(moveText));
    }

    public PGN(SevenTagRoster sevenTagRoster, Movetext moveText) {
        this(sevenTagRoster, null, moveText);
    }

    public PGN(SevenTagRoster sevenTagRoster, String moveText) {
        this(sevenTagRoster, null, moveText);
    }

}