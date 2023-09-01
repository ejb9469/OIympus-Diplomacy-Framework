public class PGN {

    public SevenTagRoster sevenTagRoster;
    public Tag[] optionalTags;
    public MoveText moveText;

    public PGN(SevenTagRoster sevenTagRoster, Tag[] optionalTags, MoveText moveText) {
        this.sevenTagRoster = sevenTagRoster;
        this.optionalTags = optionalTags;
        this.moveText = moveText;
    }

    public PGN(SevenTagRoster sevenTagRoster, Tag[] optionalTags, String moveText) {
        this(sevenTagRoster, optionalTags, new MoveText(moveText));
    }

    public PGN(SevenTagRoster sevenTagRoster, MoveText moveText) {
        this(sevenTagRoster, null, moveText);
    }

    public PGN(SevenTagRoster sevenTagRoster, String moveText) {
        this(sevenTagRoster, null, moveText);
    }

}