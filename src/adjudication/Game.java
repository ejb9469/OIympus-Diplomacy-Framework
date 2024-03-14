package adjudication;

import java.util.*;

public class Game implements Runnable {

    public static final int TIME_CONTROL = 5 * 60;  // Measured in seconds per phase
    public static final int YEAR_CONTROL = 1908;  // Measured in game years

    public static final int RETREATS_TIME_CONTROL = 2 * 60;
    public static final int BUILDS_TIME_CONTROL = 3 * 60;

    public static final boolean SHOW_NUM_DRAW_REJECTORS = true;

    static boolean gameOver = false;
    static END_CONDITION endCondition = END_CONDITION.ACTIVE;
    static List<NationState> survivingParties = null;

    static int gameYear = 1900;
    static SEASON season = SEASON.WINTER;
    static boolean retreats = false;

    static Set<NationState> nationStates = new HashSet<>();
    // Note: "dip.NATION State" refers to the state of each country, not the political term

    static Map<Unit, PROVINCE> boardState = new HashMap<>();
    static Map<NATION, Integer> supplyCounts = new HashMap<>();

    static Set<Set<NationState>> drawHistory = new HashSet<>();

    private Game() {}  // Empty constructor for Singleton use

    public void run() {
        // Check & clear game status (are we ending?)
        boolean firstRun = true;
        while (true) {
            //////////////////////////////
            //// == PRE-TURN PHASE == ////
            //////////////////////////////
                if (firstRun) {  // Skip on first run
                    firstRun = false;
                } else {
                    // WIN & END CONDITIONS \\
                    if (checkEndStates()) {  // Check if the game is over
                        gameOver = true;
                        break;
                    }
                }
                System.out.println();
            //////////////////////////////
            //// == IN-TURN PHASE == /////
            //////////////////////////////
                // TIMER \\
                try {
                    Thread.sleep(TIME_CONTROL * 1000);  // TODO: Change this for optimization
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.out.println("GAME FORCE-ENDED!!");
                }
                // ADJUDICATION \\
                boolean retreatsNeeded = process();
                incrementSeason(retreatsNeeded);

                // RETREATS \\
                if (retreatsNeeded) {
                    try {
                        Thread.sleep(RETREATS_TIME_CONTROL * 1000);  // TODO: Ditto
                    } catch (InterruptedException ex) {
                        System.out.println("GAME FORCE-ENDED!!");
                        ex.printStackTrace();
                    }
                    processRetreats();
                    incrementSeason(false);
                }

        }
    }

    public static void main(String[] args) {
        //////////////////////////////
        //// === CONFIG OPS === //////
        //////////////////////////////
        setup();
        //////////////////////////////
        //// == GAMEPLAY LOOP == /////
        //////////////////////////////
        (new Thread(new Game())).start();  // Auto-runs dip.Game.run();
        //////////////////////////////
        //// === GAME END OPS === ////
        //////////////////////////////
        cleanup();
        end();
    }

    /**
     * dip.Game.getNationStates() should always be used in all cases, for posterity's sake
     *
     * @return A Set of all dip.NationState objects
     */
    public static Set<NationState> getNationStates() {
        return nationStates;
    }

    /**
     * dip.Game.nations() should be always be used in all cases, for posterity's sake
     *
     * @return A HashSet of all dip.NationState objects' dip.NATION Enums
     */
    public static HashSet<NATION> nations() {
        HashSet<NATION> nations = new HashSet<>();
        for (NationState nationState : nationStates)
            nations.add(nationState.getNation());
        return nations;
    }

    /**
     * Sub-method of checkEndStates()
     * @return True if the size of the dip.NATION's states is 0, else false
     */
    private static boolean abandonedProcedure() {
        if (nationStates.size() == 0) {
            endCondition = END_CONDITION.ABANDONMENT;
            System.out.println("Game ABANDONED.");
            return true;
        }
        return false;
    }

    public static boolean checkEndStates() {

        // ABANDONMENT
        if (abandonedProcedure()) {
            return true;
        }

        // TIMEOUT
        if (gameYear > YEAR_CONTROL) {
            endCondition = END_CONDITION.TIMEOUT;
            System.out.println("The game has completed its final year of " + YEAR_CONTROL + ", ending the game in TIMEOUT.");
            return true;
        }

        // SOLO
        for (NATION nation : nations()) {
            if (supplyCounts.get(nation) >= 18) {
                endCondition = END_CONDITION.SOLO;
                System.out.println(nation.name() + " has achieved a SOLO!");
                return true;
            }
        }

        // CONCESSION aka All-But-One Surrender
        boolean massSurrender = true;
        NationState potentialVictor = null;
        for (NationState nationState : nationStates) {
            if (!nationState.isSurrendered()) {
                if (potentialVictor != null) {
                    massSurrender = false;
                    break;
                } else {
                    potentialVictor = nationState;
                }
            }
        }
        if (massSurrender && potentialVictor == null) {  // Marks the game for abandonment
            nationStates.clear();
            return abandonedProcedure();  // alternative to goto
        } else if (massSurrender) {
            endCondition = END_CONDITION.CONCESSION;
            System.out.println("All remaining players have CONCEDED to " + potentialVictor.getNation().name());
            return true;
        }

        // DRAW
        Set<NationState> drawRejectors = peaceConference();
        drawHistory.add(drawRejectors);
        if (drawRejectors.size() == 0) {
            endCondition = END_CONDITION.DRAW;
            System.out.println("DRAW accepted.");
            return true;
        } else {
            System.out.print("Draw rejected.");
            String next = "";
            if (SHOW_NUM_DRAW_REJECTORS)
                next = " " + drawRejectors.size() + " players rejected the draw.";
            System.out.println(next);
        }

        return false;

    }

    private static void setup() {
        nationStates.add(new NationState(NATION.ENGLAND));
        nationStates.add(new NationState(NATION.FRANCE));
        nationStates.add(new NationState(NATION.GERMANY));
        nationStates.add(new NationState(NATION.ITALY));
        nationStates.add(new NationState(NATION.AUSTRIA));
        nationStates.add(new NationState(NATION.RUSSIA));
        nationStates.add(new NationState(NATION.TURKEY));

        survivingParties.addAll(nationStates);

        // Establish board state fields for quick & easy reference during adjudication later
        for (NationState nationState : nationStates) {
            for (Unit unit : nationState.getUnits()) {
                boardState.put(unit, unit.getPosition());
            }
            supplyCounts.put(nationState.getNation(), nationState.getSupplyCount());
        }

        ////
        incrementSeason(false);  // This will update the "setup phase" of Winter 1900 to S01
    }

    private static void cleanup() {
        survivingParties = new ArrayList<>();
        if (endCondition == END_CONDITION.SOLO) {
            // TODO
        }
    }

    private static void end() {
        // TODO
    }

    /**
     * Attempts to negotiate a draw given all players' draw flags.
     * Handling the draw flags themselves happens BEFORE this method is called.
     * Civil disorder countries are not consulted in the peace conference.
     * @return Set of players who rejected the draw, 0 meaning the game ends in a draw
     */
    private static Set<NationState> peaceConference() {
        Set<NationState> rejectors = new HashSet<>();
        for (NationState nationState : nationStates) {
            if (nationState.isSurrendered())
                continue;
            if (!nationState.isDrawing()) {
                rejectors.add(nationState);
            }
        }
        return rejectors;
    }

    private static void incrementSeason(boolean retreatsNeeded) {
        if (retreatsNeeded) {
            retreats = true;
            return;
        } else if (retreats) {
            retreats = false;
        }
        if (season == SEASON.SPRING) {
            season = SEASON.FALL;
        } else if (season == SEASON.FALL) {
            season = SEASON.WINTER;
        } else /*if (season == dip.SEASON.WINTER)*/ {
            season = SEASON.SPRING;
            gameYear++;
        }
    }

    /**
     * Adjudicates all orders and updates state accordingly
     * @return True if retreats phase needed
     */
    private static boolean process() {

        List<Order> allOrders = new ArrayList<>();
        for (NationState nationState : nationStates) {
            for (Unit unit : nationState.getUnits()) {
                allOrders.add(unit.getActingOrder());
            }
        }

        Adjudicator adjudicator = new Adjudicator(allOrders);
        adjudicator.resolve();

        boolean retreatsNeeded = false;

        // Update units; shed orders
        List<Order> updatedOrders = adjudicator.getOrders();
        Map<NATION, List<Unit>> nationsUnits = new HashMap<>();
        for (NATION nation : nations())
            nationsUnits.put(nation, new ArrayList<>());
        for (Order order : updatedOrders) {
            retreatsNeeded = retreatsNeeded || order.dislodged;
            order.parentUnit.resetOrder();
            nationsUnits.get(order.parentUnit.getParentNation()).add(order.parentUnit);
        }
        for (NationState nationState : nationStates)
            nationState.setUnits(nationsUnits.get(nationState.getNation()));

        return retreatsNeeded;

    }

    private static void processRetreats() {
        
    }

    /**
     * @return The dip.SEASON + Year abbreviation - e.g. "F01"
     */
    public static String tag() {
        String tag = "";
        if (season == SEASON.SPRING)
            tag += "S";
        else if (season == SEASON.FALL)
            tag += "F";
        else /*if (season == season.WINTER)*/
            tag += "W";
        if (gameYear < 1910)
            tag += "0";
        tag += (gameYear - 1900);
        if (retreats)
            tag += "R";
        return tag;
    }

}