package core.gameobjects;

import adjudication.Adjudicator;
import adjudication.NATION;
import adjudication.Order;
import core.playerobjects.Communicator;
import core.playerobjects.Player;
import indexing.PlayerID;

import java.util.*;

public class Game {

    public int maxTurns;

    private final static int MAX_ORDERS = 50;

    private static int ID_INCREMENT = 0;
    private final int id = ID_INCREMENT++;

    private final Adjudicator adjudicator;
    private final Player[] players;
    private final UUID[] playerUUIDs;

    private Order[][] ordersBox;
    private Order[][] ordersPile;

    public Game(PlayerID[] tags, Communicator communicator) {

        this.adjudicator = new Adjudicator();

        // if (communicator == null)
            // communicator = new DebugPipe(this);

        this.playerUUIDs = new UUID[tags.length];
        Set<UUID> seenUUIDs = new HashSet<>();
        for (int i = 0; i < tags.length; i++) {
            UUID randUUID = UUID.randomUUID();
            if (seenUUIDs.contains(randUUID)) {
                i--;
                continue;
            }
            this.playerUUIDs[i] = randUUID;
            seenUUIDs.add(randUUID);
        }

        this.players = new Player[tags.length];
        for (int i = 0; i < tags.length; i++)
            this.players[i] = new Player(communicator, tags[i], this.playerUUIDs[i]);

        for (int i = 0; i < tags.length; i++)
            this.ordersPile[i] = new Order[MAX_ORDERS];

        this.ordersBox = this.ordersPile;

    }

}