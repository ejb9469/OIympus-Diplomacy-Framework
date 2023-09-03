package indexing;

import adjudication.END_CONDITION;

import java.util.Arrays;

public class SevenTagRoster {

    private Event event;
    private int round;
    private PlayerID[] players;
    private PlayerID[] winners;
    private END_CONDITION endCondition;

    public SevenTagRoster(Event event, int round, PlayerID[] players, PlayerID[] winners, END_CONDITION endCondition) {
        this.event = event;
        this.round = round;
        this.players = players;
        this.winners = winners;
        this.endCondition = endCondition;
    }

    @Override
    public String toString() {
        return this.event.toString() + this.round + Arrays.toString(this.players) + Arrays.toString(this.winners) + endCondition.toString();
    }

}