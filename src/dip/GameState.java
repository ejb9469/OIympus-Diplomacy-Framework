package dip;

import java.util.*;

public class GameState implements Runnable {

    public static final int TIME_CONTROL = 5 * 60;  // Measured in seconds per phase
    public static final int YEAR_CONTROL = 1908;  // Measured in game years

    public static final boolean SHOW_NUM_DRAW_REJECTORS = true;

    static boolean gameOver = false;
    static EndCondition endCondition = EndCondition.ACTIVE;
    static List<NationState> survivingParties = null;

    static int gameYear = 1900;
    static Season season = Season.WINTER;
    static boolean retreats = false;

    static Set<NationState> nationStates = new HashSet<>();
    // Note: "dip.Nation State" refers to the state of each country, not the political term

    static Map<Unit, Province> boardState = new HashMap<>();
    static Map<Nation, Integer> supplyCounts = new HashMap<>();

    static Set<Set<NationState>> drawHistory = new HashSet<>();

    private static OrderResolution[] orderResolutions = new OrderResolution[34];
    private static OrderState[] orderStates = new OrderState[34];
    private static int[] orderDependencies = new int[34];
    private static int numDependencies = 0;

    private GameState() {}  // Empty constructor for Singleton use

    public void run() {
        // Check & clear game status (are we ending?)
        boolean firstRun = true;
        while (true) {
            //////////////////////////////
            //// == PRE-TURN PHASE == ////
            //////////////////////////////
                if (firstRun) {  // Skip on first run
                    firstRun = false;
                    continue;
                }
                // WIN & END CONDITIONS \\
                if (checkEndStates()) {  // Check if the game is over
                    gameOver = true;
                    break;
                }
                System.out.println();
            //////////////////////////////
            //// == IN-TURN PHASE == /////
            //////////////////////////////
                // TIMER \\
                try {
                    Thread.sleep(TIME_CONTROL * 1000);  // TODO: Change this for optimization
                } catch (InterruptedException ex) {
                    System.out.println("GAME FORCE-ENDED!!");
                    ex.printStackTrace();
                }
                // ADJUDICATION \\

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
        (new Thread(new GameState())).start();  // Auto-runs dip.GameState.run();
        //////////////////////////////
        //// === GAME END OPS === ////
        //////////////////////////////
        cleanup();
        end();
    }

    /**
     * dip.GameState.getNationStates() should always be used in all cases, for posterity's sake
     *
     * @return A Set of all dip.NationState objects
     */
    public static Set<NationState> getNationStates() {
        return nationStates;
    }

    /**
     * dip.GameState.nations() should be always be used in all cases, for posterity's sake
     *
     * @return A HashSet of all dip.NationState objects' dip.Nation Enums
     */
    public static HashSet<Nation> nations() {
        HashSet<Nation> nations = new HashSet<>();
        for (NationState nationState : nationStates)
            nations.add(nationState.getNation());
        return nations;
    }

    /**
     * @return The dip.Season + Year abbreviation - e.g. "F01"
     */
    public static String tag() {
        String tag = "";
        if (season == Season.SPRING)
            tag += "S";
        else if (season == Season.FALL)
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

    /**
     * Sub-method of checkEndStates()
     * @return True if the size of the dip.Nation's states is 0, else false
     */
    static boolean abandonedProcedure() {
        if (nationStates.size() == 0) {
            endCondition = EndCondition.ABANDONMENT;
            System.out.println("Game ABANDONED.");
            return true;
        }
        return false;
    }

    static boolean checkEndStates() {

        // ABANDONMENT
        if (abandonedProcedure()) {
            return true;
        }

        // TIMEOUT
        if (gameYear > YEAR_CONTROL) {
            endCondition = EndCondition.TIMEOUT;
            System.out.println("The game has completed its final year of " + YEAR_CONTROL + ", ending the game in TIMEOUT.");
            return true;
        }

        // SOLO
        for (Nation nation : nations()) {
            if (supplyCounts.get(nation) >= 18) {
                endCondition = EndCondition.SOLO;
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
            endCondition = EndCondition.CONCESSION;
            System.out.println("All remaining players have CONCEDED to " + potentialVictor.getNation().name());
            return true;
        }

        // DRAW
        Set<NationState> drawRejectors = peaceConference();
        drawHistory.add(drawRejectors);
        if (drawRejectors.size() == 0) {
            endCondition = EndCondition.DRAW;
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
        nationStates.add(new NationState(Nation.ENGLAND));
        nationStates.add(new NationState(Nation.FRANCE));
        nationStates.add(new NationState(Nation.GERMANY));
        nationStates.add(new NationState(Nation.ITALY));
        nationStates.add(new NationState(Nation.AUSTRIA));
        nationStates.add(new NationState(Nation.RUSSIA));
        nationStates.add(new NationState(Nation.TURKEY));

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
        if (endCondition == EndCondition.SOLO) {
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
        if (season == Season.SPRING) {
            season = Season.FALL;
        } else if (season == Season.FALL) {
            season = Season.WINTER;
        } else /*if (season == dip.Season.WINTER)*/ {
            season = Season.SPRING;
            gameYear++;
        }
    }

    private static void process() {

        List<Order> allOrders = new ArrayList<>();
        for (NationState nationState : nationStates) {
            for (Unit unit : nationState.getUnits()) {
                allOrders.add(unit.getActingOrder());
            }
        }

        for (Order order : allOrders) {
            resolve(0);
        }

    }

    private static OrderResolution resolve(int nr) {

        int i, oldNumDependencies;
        OrderResolution firstResult, secondResult;

        // BASE CASE :: If the order is resolved, return
        if (orderStates[nr] == OrderState.RESOLVED)
            return orderResolutions[nr];

        // If the order is UNRESOLVED, look if the resolution of the order is dependent on the resolution of other orders.
        if (orderStates[nr] == OrderState.GUESSING) {
            i = 0;
            while (i < numDependencies) {
                if (orderDependencies[i++] == nr) {
                    return orderResolutions[nr];
                }
            }
        }

        oldNumDependencies = numDependencies;

        orderResolutions[nr] = OrderResolution.FAILS;
        orderStates[nr] = OrderState.GUESSING;

        firstResult = adjudicate(nr);

        if (numDependencies == oldNumDependencies) {
            if (orderStates[nr] != OrderState.RESOLVED) {
                orderResolutions[nr] = firstResult;
                orderStates[nr] = OrderState.RESOLVED;
            }
            return firstResult;
        }

        if (orderDependencies[oldNumDependencies] != nr) {
            orderDependencies[numDependencies++] = nr;
            orderResolutions[nr] = firstResult;
            return firstResult;
        }

        while (numDependencies > oldNumDependencies) {
            orderStates[orderDependencies[--numDependencies]] = OrderState.UNRESOLVED;
        }

        orderResolutions[nr] = OrderResolution.SUCCEEDS;
        orderStates[nr] = OrderState.GUESSING;

        secondResult = adjudicate(nr);

        if (firstResult == secondResult) {
            while (numDependencies > oldNumDependencies) {
                orderStates[orderDependencies[--numDependencies]] = OrderState.UNRESOLVED;
            }
            orderResolutions[nr] = firstResult;
            orderStates[nr] = OrderState.RESOLVED;
            return firstResult;
        }

        backupRule(oldNumDependencies);

        return resolve(nr);

    }

    private static OrderResolution adjudicate(int nr) {  // TODO
        return OrderResolution.UNRESOLVED;  // placeholder
    }

    private static void backupRule(int nr) {

    }

}