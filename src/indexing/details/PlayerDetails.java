package indexing.details;

import indexing.*;
import rating.EloRating;

import java.util.List;

public class PlayerDetails implements TagDetails {

    public static final PlayerDetails NONE = new PlayerDetails(null, null, null, false);

    public PLAYER_STYLE playerStyle;
    public List<Game> gamesList;
    public List<Event> eventsList;

    public EloRating eloRating;
    public double topShare;
    public int ranking;
    public double averageCenterCount;

    public PlayerDetails(PLAYER_STYLE playerStyle, List<Game> gamesList, List<Event> eventsList, boolean calculate) {
        this.playerStyle = playerStyle;
        this.gamesList = gamesList;
        this.eventsList = eventsList;
    }


    public void update() {

    }

}
