package adjudication;

import exceptions.BadOrderException;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Singleton class responsible for adjudication logic.
 */
public class Adjudicator implements Runnable {

    private List<Order> ordersList;

    public List<Order> getOrders() {
        return ordersList;
    }

    public void setOrders(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    public Adjudicator(List<Order> ordersList) {
        this.ordersList = ordersList;
    }

    public Adjudicator(){ this.ordersList = new ArrayList<>(); }

    private static final int INPUT_MODE = 2;
    private static FileWriter currentFileWriter = null;

    private static int tabsCounter = 0;

    public static void main(String[] args) throws IOException, BadOrderException {

        List<Order> orders = new ArrayList<>();

        if (INPUT_MODE == 0) {
            orders.add(new Order(new Unit(NATION.ENGLAND, PROVINCE.Bel, 1), ORDER_TYPE.SUPPORT, PROVINCE.Ruh, PROVINCE.Hol));
            orders.add(new Order(new Unit(NATION.FRANCE, PROVINCE.Ruh, 0), ORDER_TYPE.MOVE, PROVINCE.Hol, PROVINCE.Hol));
            orders.add(new Order(new Unit(NATION.GERMANY, PROVINCE.Hol, 0), ORDER_TYPE.SUPPORT, PROVINCE.Ruh, PROVINCE.Bel));
            orders.add(new Order(new Unit(NATION.ITALY, PROVINCE.ION, 1), ORDER_TYPE.MOVE, PROVINCE.Tun, PROVINCE.Tun));
            orders.add(new Order(new Unit(NATION.ENGLAND, PROVINCE.Lon, 0), ORDER_TYPE.MOVE, PROVINCE.Den, PROVINCE.Den));
            orders.add(new Order(new Unit(NATION.ENGLAND, PROVINCE.NTH, 1), ORDER_TYPE.CONVOY, PROVINCE.Lon, PROVINCE.Den));
            orders.add(new Order(new Unit(NATION.RUSSIA, PROVINCE.Nwy, 1), ORDER_TYPE.MOVE, PROVINCE.NTH, PROVINCE.NTH));
            orders.add(new Order(new Unit(NATION.RUSSIA, PROVINCE.NWG, 1), ORDER_TYPE.SUPPORT, PROVINCE.Nwy, PROVINCE.NTH));
            for (Order order : orders)
                System.out.println(order.toString());
            new Adjudicator(orders).resolve();
        } else if (INPUT_MODE == 1) {
            System.out.println("Enter an orders list delimited by newlines. Type the String \"=DONE=\" on a newline when finished.");
            Scanner sc = new Scanner(System.in);
            while (true) {
                String in = sc.nextLine();  // Override :: Replace this line w/ Scanner nextLine()
                if (in.equals("=DONE="))
                    break;
                Order order = Order.parseUnit(in);
                orders.add(order);
                //System.out.println(order.toString());
            }
            sc.close();
            new Adjudicator(orders).resolve();
        } else if (INPUT_MODE == 2) {  // Test cases
            File testCaseFolder = new File("src/testcases/testgames/");
            File[] testCaseFiles = testCaseFolder.listFiles();
            if (testCaseFiles == null) throw new IOException();
            if (testCaseFiles.length == 0) return;
            Map<String, String> namesToAbbreviations = new HashMap<>();
            for (PROVINCE province : PROVINCE.values()) {
                if (province.name().contains("(") || province.name().equals("Switzerland")) continue;
                namesToAbbreviations.put(province.getName(), province.name());
            }
            Scanner sc;
            for (File testCaseFile : testCaseFiles) {
                if (!testCaseFile.isFile()) continue;
                File file = new File("src/testcases/testgames/results/OUT_" + testCaseFile.getName());
                file.createNewFile();
                currentFileWriter = new FileWriter(file.getAbsolutePath());
                tabsCounter = 0;
                //if (!testCaseFile.getName().contains("BACKSTABBR NEXUS")) continue;
                System.out.println(testCaseFile.getName() + "\n");
                currentFileWriter.write(testCaseFile.getName() + "\n\n");
                orders = new ArrayList<>();
                sc = new Scanner(testCaseFile);
                tabsCounter = 1;
                boolean abbreviated = !testCaseFile.getName().startsWith("pythongen_");
                //if (!abbreviated) continue;
                while (sc.hasNextLine()) {
                    String orderText = sc.nextLine();
                    if (orderText.isBlank()) continue;
                    if (orderText.contains(".ec") || orderText.contains(".nc") || orderText.contains(".sc")) break;  // TODO: Ignore coasts for now
                    if (!abbreviated) {
                        Map<String, String> fullNamesToAbbrsMap = PROVINCE.generateFullNamesToAbbreviationsMap();
                        for (String fullName : fullNamesToAbbrsMap.keySet()) {
                            if (orderText.contains(fullName)) {
                                orderText = orderText.replaceAll(fullName, fullNamesToAbbrsMap.get(fullName));
                            }
                        }
                    }
                    Order order = Order.parseUnit(orderText);
                    orders.add(order);
                    //System.out.println(order.toString());
                }
                sc.close();
                if (orders.size() == 0) continue;
                new Adjudicator(orders).resolve();
                currentFileWriter.close();
            }
        }
    }

    Map<Order, Integer> supportCounts = new HashMap<>();
    Map<Order, Integer> originalSupportCounts = new HashMap<>();

    Map<Order, Order> supportMap = new HashMap<>();

    List<Order> convoyingArmies = new ArrayList<>();
    Map<Order, List<Order>> convoyPaths = new HashMap<>();

    List<Order> successfulConvoyingArmies = new ArrayList<>();

    List<Order> contestedOrders = new ArrayList<>();
    List<Order> correspondingSupports = new ArrayList<>();

    Map<PROVINCE, List<Order>> battleList = new HashMap<>();

    Map<PROVINCE, Integer> preventStrength = new HashMap<>();

    /**
     * The resolve function - all the Diplomacy adjudication logic is in here.
     */
    public void resolve() {

        // Reinitialize all fields besides `ordersList`
        supportCounts = new HashMap<>();
        supportMap = new HashMap<>();
        convoyingArmies = new ArrayList<>();
        successfulConvoyingArmies = new ArrayList<>();
        contestedOrders = new ArrayList<>();
        battleList = new HashMap<>();
        preventStrength = new HashMap<>();

        List<Order> orders = new ArrayList<>(ordersList);

        for (Order order : orders) {
            if (order.invincible) {
                // This field is only set after second passes thru resolve()
                order.orderType = ORDER_TYPE.HOLD;
                supportCounts.put(order, 9999);
            } else {
                supportCounts.put(order, 0);
            }
        }

        for (PROVINCE province : PROVINCE.values())
            preventStrength.put(province, -1);

        convoyingArmies = findConvoyingArmies(orders);

        Set<Order> voidedOrders = new HashSet<>();
        for (Order convoyingArmy : convoyingArmies) {
            List<Order> convoyPath = drawConvoyPath(orders, convoyingArmy);
            if (convoyPath.size() == 0) {
                voidedOrders.add(convoyingArmy);
            } else {
                convoyingArmy.viaConvoy = true;
                convoyPaths.put(convoyingArmy, convoyPath);
            }
        }

        for (Order order : voidedOrders)
            convoyingArmies.remove(order);

        contestedOrders = findContestedOrders(orders);
        correspondingSupports = findCorrespondingSupports(orders, contestedOrders);

        printOrders(orders, "ALL ORDERS:");
        //printOrders(contestedOrders, "CONTESTED ORDERS:");
        //printOrders(correspondingSupports, "CORRESPONDING SUPPORTS:");
        //System.out.println("======\n");

        // Replace invalid supports with holds
        List<Order> invalidSupports = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderType == ORDER_TYPE.SUPPORT) {
                List<Order> orders2 = orders;
                if (!order.pr1.isAdjacentTo(order.pr2) && order.pr1 != order.pr2)  // The only reason e.g. Bel S Lon - Hol would work is if e.g. NTH C Lon - Hol
                    orders2 = convoyingArmies;
                boolean found = false;
                for (Order order2 : orders2) {
                    if (order.equals(order2)) continue;
                    if (order.pr1 == order2.parentUnit.getPosition()) {
                        found = true;
                        if (order2.orderType == ORDER_TYPE.MOVE && order.pr2 != order2.pr2) {  // Support-to-move does not match an actual move order
                            invalidSupports.add(order);  // Could be replaced with `found = false;`
                        } else if (order2.orderType == ORDER_TYPE.MOVE && order.pr1 == order.pr2) {  // Support-to-hold on a MOVE order
                            invalidSupports.add(order);
                        } else if (order2.orderType != ORDER_TYPE.MOVE && order.pr1 != order.pr2) { // Support-to-move on a stationary order
                            invalidSupports.add(order);
                        } else {
                            supportMap.put(order, order2);
                        }
                        break;
                    }
                }
                if (!found)  // No corresponding order taking the support
                    invalidSupports.add(order);
            }
        }

        // Replace invalid convoys with holds
        for (Order order : orders) {
            if (order.orderType == ORDER_TYPE.CONVOY) {
                boolean found = false;
                for (Order order2 : orders) {
                    if (order.equals(order2)) continue;
                    if (order2.orderType != ORDER_TYPE.MOVE) continue;
                    if (order.pr1 == order.parentUnit.getPosition() && order.pr2 == order2.pr2) {
                        found = true;
                        if (order.pr1.isAdjacentTo(order.pr2) && !order2.viaConvoy)
                            invalidSupports.add(order);
                        break;
                    }
                }
                if (!found)
                    invalidSupports.add(order);
            }
        }

        // Replace invalid supports and convoys with holds
        for (Order invalidSupport : invalidSupports) {
            orders.remove(invalidSupport);
            orders.add(new Order(invalidSupport.parentUnit, ORDER_TYPE.HOLD, invalidSupport.parentUnit.getPosition(), invalidSupport.parentUnit.getPosition()));
        }

        // Increment the support count for all [implicitly] valid supports
        for (Order order : orders) {
            if (order.orderType != ORDER_TYPE.SUPPORT) continue;
            supportCounts.put(supportMap.get(order), supportCounts.get(supportMap.get(order)) + 1);
        }
        originalSupportCounts = new HashMap<>(supportCounts);

        // Set the 'no help' flags for supports on move orders attacking units of the same NATION
        for (Order supportOrder : supportMap.keySet()) {
            Order supportedOrder = supportMap.get(supportOrder);
            if (supportedOrder.orderType != ORDER_TYPE.MOVE) continue;
            Order attackedUnit = findUnitAtPosition(supportedOrder.pr1, orders);
            if (attackedUnit != null) {
                if (attackedUnit.parentUnit.getParentNation() == supportOrder.parentUnit.getParentNation())
                    supportedOrder.noHelpList.add(supportOrder);
            }
        }

        List<Order> contestedOrdersNoConvoys = new ArrayList<>(contestedOrders);
        contestedOrdersNoConvoys.removeAll(convoyingArmies);

        for (Order contestedOrder : contestedOrders/*NoConvoys*/) {
            if (contestedOrder.orderType != ORDER_TYPE.MOVE) continue;
            cutSupport(contestedOrder);
        }

        battleList = populateBattleList(contestedOrders/*NoConvoys*/);

        // CONVOYING ARMIES PROCEDURE \\
        int convoyingArmiesSuccessesOuter = -1;
        while (successfulConvoyingArmies.size() > convoyingArmiesSuccessesOuter) {
            convoyingArmiesSuccessesOuter = successfulConvoyingArmies.size();

            int convoyingArmiesSuccessesInner = -1;
            while (successfulConvoyingArmies.size() > convoyingArmiesSuccessesInner) {
                convoyingArmiesSuccessesInner = successfulConvoyingArmies.size();
                for (Order convoyingArmy : convoyingArmies) {
                    checkDisruptions(convoyingArmy);
                    if (convoyingArmy.convoyEndangered) {
                        convoyingArmy.convoyAttacked = true;
                    } else {
                        cutSupport(convoyingArmy);
                        if (!successfulConvoyingArmies.contains(convoyingArmy))
                            successfulConvoyingArmies.add(convoyingArmy);
                    }
                }
            }

            for (Order convoyingArmy : convoyingArmies) {
                checkDisruptions(convoyingArmy);
                if (convoyingArmy.convoyEndangered) {
                    convoyingArmy.noConvoy = true;
                    supportCounts.replace(convoyingArmy, 0);
                    for (Order supportOrder : supportMap.keySet()) {
                        if (supportMap.get(supportOrder).equals(convoyingArmy))
                            supportMap.get(supportOrder).noConvoy = true;
                    }
                } else if (convoyingArmy.convoyAttacked) {
                    convoyingArmy.convoyAttacked = false;
                    cutSupport(convoyingArmy);
                    if (!successfulConvoyingArmies.contains(convoyingArmy))
                        successfulConvoyingArmies.add(convoyingArmy);
                }
            }

        }

        boolean anyUnitBouncedOuter = true;
        while (anyUnitBouncedOuter) {
            anyUnitBouncedOuter = false;

            // Mark bounces caused by the inability to swap places
            Set<Order> noSwapDuplicates = new HashSet<>();
            boolean anyUnitBounced = true;
            while (anyUnitBounced) {
                anyUnitBounced = false;
                for (Order moveOrder : contestedOrders) {
                    if (moveOrder.orderType != ORDER_TYPE.MOVE) continue;
                    Order attackedUnit = findUnitAtPosition(moveOrder.pr1, contestedOrders);
                    if (attackedUnit == null) continue;
                    if (attackedUnit.orderType != ORDER_TYPE.MOVE) continue;
                    if (attackedUnit.pr1 != moveOrder.parentUnit.getPosition()) continue;
                    if (convoyingArmies.contains(attackedUnit)) {
                        System.out.println("Debug: Convoy swap detected for " + moveOrder.parentUnit + " and " + attackedUnit.parentUnit);
                    } else {
                        int oldMoveOrderSupportCount = supportCounts.get(moveOrder);  // This is necessary for later comparison because `bounce()` sets `moveOrder`'s support count to 0.
                        if (moveOrder.parentUnit.getParentNation() == attackedUnit.parentUnit.getParentNation()
                                || supportCounts.get(moveOrder) - moveOrder.noHelpList.size() <= supportCounts.get(attackedUnit)) {
                            if (noSwapDuplicates.contains(moveOrder)) continue;
                            System.out.println("Debug: No-swap Type A bounce() called for " + moveOrder.parentUnit + " and " + attackedUnit.parentUnit);
                            noSwapDuplicates.add(moveOrder);
                            bounce(moveOrder);
                            anyUnitBounced = true;
                        }
                        if (moveOrder.parentUnit.getParentNation() == attackedUnit.parentUnit.getParentNation()
                                || supportCounts.get(attackedUnit) - attackedUnit.noHelpList.size() <= oldMoveOrderSupportCount) {
                            if (noSwapDuplicates.contains(attackedUnit)) continue;
                            System.out.println("Debug: No-swap Type B bounce() called for " + moveOrder.parentUnit + " and " + attackedUnit.parentUnit);
                            noSwapDuplicates.add(attackedUnit);
                            bounce(attackedUnit);
                            anyUnitBounced = true;
                        }
                    }
                }
            }

            // Handle 'border battles'
            for (Order moveOrder : contestedOrders) {
                if (moveOrder.orderType != ORDER_TYPE.MOVE) continue;
                for (Order moveOrder2 : contestedOrders) {
                    if (moveOrder2.orderType != ORDER_TYPE.MOVE) continue;
                    if (moveOrder.equals(moveOrder2)) continue;
                    if (moveOrder.pr1 == moveOrder2.parentUnit.getPosition() && moveOrder2.pr1 == moveOrder.parentUnit.getPosition()) {
                        // We're dealing with a 'border battle'
                        if (supportCounts.get(moveOrder).equals(supportCounts.get(moveOrder2))) {
                            preventStrength.put(moveOrder.pr1, supportCounts.get(moveOrder) + 1);
                            preventStrength.put(moveOrder2.pr1, supportCounts.get(moveOrder2) + 1);
                        }
                    }
                }
            }

            List<Order> battlersToBounce = new ArrayList<>();

            // Mark bounces suffered by understrength attackers
            for (PROVINCE province : PROVINCE.values()) {
                for (Order battler : battleList.get(province)) {
                    int battlerSupportCount = supportCounts.get(battler);
                    boolean isStrongest = battlerSupportCount > preventStrength.get(battler.pr1);
                    if (battlerSupportCount <= preventStrength.get(battler.pr1))
                        System.out.println("Debug: Beleaguered garrison detected at " + battler.pr1 + " for unit " + battler);
                    for (Order battler2 : battleList.get(province)) {
                        if (battler.equals(battler2)) continue;
                        if (supportCounts.get(battler2) >= battlerSupportCount) {
                            isStrongest = false;
                            break;
                        }
                    }
                    if (!isStrongest && battler.orderType == ORDER_TYPE.MOVE && !battler.bounce) {
                        System.out.println("Debug: Understrength attackers bounce() called for " + battler.parentUnit);
                        battlersToBounce.add(battler);
                        anyUnitBouncedOuter = true;
                    }
                }
            }

            // Bounce understrength attackers
            for (Order battler : battlersToBounce)
                bounce(battler);

            if (anyUnitBouncedOuter) continue;

            // Mark bounces caused by inability to self-dislodge
            for (PROVINCE province : PROVINCE.values()) {
                int strongestBattlerSupportCount = 0;
                Order strongestBattler = null;
                for (Order battler : battleList.get(province)) {
                    if (supportCounts.get(battler) > strongestBattlerSupportCount) {
                        strongestBattlerSupportCount = supportCounts.get(battler);
                        strongestBattler = battler;
                    } else if (supportCounts.get(battler) == strongestBattlerSupportCount) {
                        strongestBattler = null;
                    }
                }
                if (strongestBattler == null) continue;
                if (strongestBattler.bounce || strongestBattler.orderType != ORDER_TYPE.MOVE) continue;

                Order victim = findUnitAtPosition(province, contestedOrders);
                if (victim == null) continue;
                if (victim.orderType == ORDER_TYPE.MOVE && !victim.bounce) continue;
                if (victim.parentUnit.getParentNation() == strongestBattler.parentUnit.getParentNation()) {
                    System.out.println("Debug: No self-dislodge Type A bounce() called for " + strongestBattler.parentUnit);
                    bounce(strongestBattler);
                    anyUnitBouncedOuter = true;
                    continue;
                }
                supportCounts.replace(victim, supportCounts.get(victim) - victim.noHelpList.size());
                for (Order battler : battleList.get(province)) {
                    if (battler.equals(strongestBattler)) continue;
                    if (supportCounts.get(battler) >= strongestBattlerSupportCount) {
                        System.out.println("Debug: No self-dislodge Type B bounce() called for " + strongestBattler.parentUnit);
                        bounce(strongestBattler);
                        anyUnitBouncedOuter = true;
                        break;
                    }
                }

                int sameNationSupportCount = 0;
                for (Order supportOrder : contestedOrders) {
                    if (supportOrder.orderType != ORDER_TYPE.SUPPORT) continue;
                    if (supportMap.get(supportOrder) != strongestBattler) continue;
                    if (supportOrder.parentUnit.getParentNation() == victim.parentUnit.getParentNation()
                            && strongestBattler.parentUnit.getParentNation() != supportOrder.parentUnit.getParentNation()) {
                        sameNationSupportCount++;
                    }
                }

                if (sameNationSupportCount == 0) continue;

                for (Order battler : battleList.get(province)) {
                    if (battler.equals(strongestBattler)) continue;
                    if (originalSupportCounts.get(battler) >= strongestBattlerSupportCount - sameNationSupportCount) {
                        // The above doesn't work without using `originalSupportCounts` because Austrian A Bud - Rum has already bounced, depleting its support count
                        // This could possibly break, but I'm not sure how. It doesn't seem possible right now. (03/19/24)
                        System.out.println("Debug: No self-dislodge Type C bounce() called for " + strongestBattler.parentUnit);
                        bounce(strongestBattler);
                        anyUnitBouncedOuter = true;
                        break;
                    }
                }

            }

            if (anyUnitBouncedOuter) continue;

            // Mark supports cut by dislodgements
            for (Order moveOrder : contestedOrders) {
                if (moveOrder.orderType != ORDER_TYPE.MOVE) continue;
                if (!moveOrder.bounce) {
                    if (cutSupport(moveOrder))
                        anyUnitBouncedOuter = true;  // Misnomer but functionally the same
                }
            }

            if (anyUnitBouncedOuter) continue;

        }

        // Define a map of dislodged orders and attack origins for populating retreat lists later
        Map<Order, PROVINCE> attackOriginMap = new HashMap<>();

        // Move units that did not bounce
        List<Order> changedOrders = new ArrayList<>(orders);
        for (Order moveOrder : orders) {
            if (moveOrder.orderType != ORDER_TYPE.MOVE) continue;
            if (moveOrder.bounce) continue;
            Order victim = findUnitAtPosition(moveOrder.pr1, contestedOrders);
            if (victim != null) {
                if (victim.orderType != ORDER_TYPE.MOVE || victim.bounce) {
                    if (contestedOrders/*NoConvoys*/.contains(victim)
                            && victim.orderType == ORDER_TYPE.MOVE
                            && victim.pr1 == moveOrder.parentUnit.getPosition()
                            && contestedOrders/*NoConvoys*/.contains(moveOrder))
                    {
                        List<Order> battlersAtSource = battleList.get(victim.pr1);
                        battlersAtSource.remove(victim);
                        battleList.replace(victim.pr1, battlersAtSource);
                    }
                    changedOrders.remove(victim);
                    victim.dislodged = true;
                    changedOrders.add(victim);
                    PROVINCE attackOrigin = moveOrder.prInitial;
                    if (convoyingArmies.contains(moveOrder)) {
                        List<Order> convoyPath = convoyPaths.get(moveOrder);
                        attackOrigin = convoyPath.get(convoyPath.size()-1).parentUnit.getPosition();
                    }
                    attackOriginMap.put(victim, attackOrigin);
                }
            }
            unbounce(moveOrder.parentUnit.getPosition());
            changedOrders.remove(moveOrder);  // This should work b/c of the .equals() override in dip.Order
            moveOrder.parentUnit.setPosition(moveOrder.pr1);  // The move itself!
            moveOrder.invincible = true;  // Only relevant for second passes
            changedOrders.add(moveOrder);
        }

        for (Order order : changedOrders) {
            order.resetFlags();
            if (order.dislodged)
                order.orderType = ORDER_TYPE.VOID;
        }

        ordersList = changedOrders;

        // Populate retreats list for dislodged units
        List<Unit> unitsList = new ArrayList<>();
        for (Order order : orders)
            unitsList.add(order.parentUnit);
        for (Order victim : attackOriginMap.keySet()) {
            victim.parentUnit.populateRetreatsList(attackOriginMap.get(victim), unitsList);
        }

        printUnits(ordersList, "\nFINAL STATE: ");
        System.out.println();
        if (currentFileWriter != null) {
            try {
                currentFileWriter.write("\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void unbounce(PROVINCE province) {
        int strongestBattlerSupportCount = 0;
        Order strongestBattler = null;
        for (Order battler : battleList.get(province)) {
            if (supportCounts.get(battler) > strongestBattlerSupportCount) {
                strongestBattlerSupportCount = supportCounts.get(battler);
                strongestBattler = battler;
            } else if (supportCounts.get(battler) == strongestBattlerSupportCount) {
                strongestBattler = null;
            }
        }
        if (strongestBattler != null) {
            if (!strongestBattler.bounce) return;
            strongestBattler.bounce = false;
            if (strongestBattler.dislodged) {
                strongestBattler.dislodged = false;
            } else {
                List<Order> battlersAtSource = battleList.get(strongestBattler.prInitial);
                battlersAtSource.remove(strongestBattler);
                battleList.replace(province, battlersAtSource);
                unbounce(strongestBattler.prInitial);
            }
        }
    }

    private void bounce(Order moveOrder) {
        supportCounts.remove(moveOrder);
        moveOrder.bounce = true;
        List<Order> battlers = battleList.get(moveOrder.parentUnit.getPosition());
        moveOrder.noHelpList = new ArrayList<>();
        supportCounts.put(moveOrder, 0);
        battlers.add(moveOrder);
        battleList.put(moveOrder.parentUnit.getPosition(), battlers);
    }

    /**
     * Cut support procedure
     * @param moveOrder Move order to cut supports with
     * @return Whether *any* support was cut
     */
    private boolean cutSupport(Order moveOrder) {
        Order defender = findUnitAtPosition(moveOrder.pr1, contestedOrders);
        if (defender == null) return false;
        if (defender.orderType != ORDER_TYPE.SUPPORT) return false;
        if (defender.cut) return false;
        if (moveOrder.parentUnit.getParentNation() == defender.parentUnit.getParentNation()) return false;
        if (defender.pr2 == moveOrder.parentUnit.getPosition()) return false;
        if (convoyingArmies.contains(moveOrder)) {
            System.out.println("Debug: Adjudicator.cutSupport() is handling a convoying army...");  // Debug
        }
        defender.cut = true;
        Order supported = supportMap.get(defender);
        if (supported == null) return false;  // Should this return false or true? Should this line even be here?
        supportCounts.replace(supported, supportCounts.get(supported) - 1);
        supported.noHelpList.remove(defender);
        return true;
    }

    private void checkDisruptions(Order convoyingArmy) {

        List<Order> convoyPath = convoyPaths.get(convoyingArmy);
        for (Order convoyingFleet : convoyPath) {
            List<Order> battlers = battleList.get(convoyingFleet.parentUnit.getPosition());
            boolean beleaguered = false;
            int maxStrength = 0;
            if (battlers.size() == 0) continue;
            Order strongestBattler = null;
            for (Order battler : battlers) {
                int battlerSupports = supportCounts.get(battler);
                if (battlerSupports > maxStrength) {
                    strongestBattler = battler;
                    maxStrength = battlerSupports;
                    beleaguered = false;
                } else if (battlerSupports == maxStrength) {
                    beleaguered = true;
                }
            }
            if (strongestBattler == null) continue;
            if (beleaguered) continue;
            if (convoyingFleet.parentUnit.getParentNation() == strongestBattler.parentUnit.getParentNation())
                continue;
            convoyingArmy.convoyEndangered = true;
            return;
        }

    }

    private Order findUnitAtPosition(PROVINCE province, Collection<Order> orders) {
        for (Order order : orders) {
            if (order.parentUnit.getPosition().equals(province))
                return order;
        }
        return null;
    }

    private Map<PROVINCE, List<Order>> populateBattleList(Collection<Order> orders) {

        Map<PROVINCE, List<Order>> battleList = new HashMap<>();

        for (PROVINCE province : PROVINCE.values()) {
            List<Order> combatants = new ArrayList<>();
            for (Order order : orders) {
                if (order.orderType == ORDER_TYPE.MOVE) {
                    if (order.pr1 == province)
                        combatants.add(order);
                } else {
                    if (order.parentUnit.getPosition() == province)
                        combatants.add(order);
                }
            }
            battleList.put(province, combatants);
        }
        return battleList;

    }

    private List<Order> findConvoyingArmies(List<Order> orders) {
        List<Order> convoyingArmies = new ArrayList<>();
        for (Order order : orders) {
            if (order.parentUnit.getUnitType() != 0) continue;
            if (order.orderType != ORDER_TYPE.MOVE) continue;
            if (!order.parentUnit.getPosition().isCoastal()) continue;  // You can't convoy from or to inland provinces
            if (!order.pr1.isCoastal()) continue;
            //if (!order.parentUnit.getPosition().isAdjacentTo(order.pr1) || order.viaConvoy)
            convoyingArmies.add(order);
        }
        return convoyingArmies;
    }

    private List<Order> drawConvoyPath(List<Order> orders, Order moveOrder) {

        List<Order> convoyPath;
        List<Order> beginningConvoys = new ArrayList<>();
        for (Order order : orders) {
            if (order.equals(moveOrder)) continue;  // Technically unnecessary
            if (order.parentUnit.getUnitType() == 0) continue;  // You can't convoy over an army
            if (order.orderType != ORDER_TYPE.CONVOY) continue;
            if (order.pr1.equals(moveOrder.parentUnit.getPosition()) && order.pr2.equals(moveOrder.pr1) && order.parentUnit.getPosition().isAdjacentTo(moveOrder.parentUnit.getPosition())) {
                beginningConvoys.add(order);
            }
        }

        if (beginningConvoys.size() == 0)
            return beginningConvoys;  // empty list

        Order firstConvoy = beginningConvoys.get(0);
        List<Order> initPath = new ArrayList<>();
        initPath.add(firstConvoy);
        convoyPath = drawOneConvoyPath(orders, firstConvoy, initPath, new ArrayList<>());

        return convoyPath;

    }

    private List<Order> drawOneConvoyPath(List<Order> orders, Order firstConvoy, List<Order> convoyPath, List<Order> excludedOrders) {
        List<Order> adjacentConvoys = findAdjacentConvoys(orders, firstConvoy, convoyPath);

        adjacentConvoys.removeIf(excludedOrders::contains);

        if (adjacentConvoys.size() == 0)
            return convoyPath;

        convoyPath.add(adjacentConvoys.get(0));
        excludedOrders.add(adjacentConvoys.get(0));
        return drawOneConvoyPath(orders, adjacentConvoys.get(0), convoyPath, excludedOrders);
    }

    /**
     * Locate convoy orders identical to `convoyOrder` and adjacent to its unit
     * @param orders List of orders
     * @param convoyOrder Matching convoy order
     * @return List of adjacent fleets convoying the same army
     */
    private List<Order> findAdjacentConvoys(Collection<Order> orders, Order convoyOrder, Collection<Order> excludedOrders) {
        List<Order> adjacentConvoys = new ArrayList<>();
        for (Order order : orders) {
            if (excludedOrders.contains(order)) continue;
            if (order.equals(convoyOrder)) continue;  // Technically unnecessary
            if (order.orderType != ORDER_TYPE.CONVOY) continue;
            if (order.pr1.equals(convoyOrder.pr1) && order.pr2.equals(convoyOrder.pr2) && order.parentUnit.getPosition().isAdjacentTo(convoyOrder.parentUnit.getPosition())) {
                adjacentConvoys.add(order);
            }
        }
        return adjacentConvoys;
    }

    /**
     * @param orders Collection of orders from which to grab potential corresponding supports
     * @param matching Collection of orders for supports to correspond *to*
     * @return List of support orders that correspond to the orders in `matching`
     */
    private List<Order> findCorrespondingSupports(Collection<Order> orders, Collection<Order> matching) {

        List<Order> correspondingSupports = new ArrayList<>();

        for (Order order : matching) {
            for (Order supportOrder : orders) {
                if (order.equals(supportOrder)) continue;
                if (supportOrder.orderType != ORDER_TYPE.SUPPORT) continue;
                if (order.orderType != ORDER_TYPE.MOVE && supportOrder.pr1.equals(order.parentUnit.getPosition()) && supportOrder.pr2.equals(order.parentUnit.getPosition())) {
                    correspondingSupports.add(supportOrder);
                    continue;
                }
                if (order.orderType == ORDER_TYPE.MOVE && supportOrder.pr1.equals(order.parentUnit.getPosition()) && supportOrder.pr2.equals(order.pr1)) {
                    correspondingSupports.add(supportOrder);
                }
            }
        }

        return correspondingSupports;

    }

    private List<Order> findCorrespondingSupports(Collection<Order> orders, Order matching) {
        List<Order> singular = new ArrayList<>();
        singular.add(matching);
        return findCorrespondingSupports(orders, singular);
    }

    private List<Order> findContestedOrders(List<Order> orders) {

        List<Order> contestedOrders = new ArrayList<>();

        for (Order order : orders) {
            for (Order order2 : orders) {
                // A unit cannot contest itself
                if (order.equals(order2)) continue;
                // Two holds cannot contest each other, in any respect
                if (order.orderType != ORDER_TYPE.MOVE && order2.orderType != ORDER_TYPE.MOVE) continue;
                if ((order.orderType == ORDER_TYPE.MOVE && (order.pr1 == order2.parentUnit.getPosition()) || order.pr1 == order2.pr1)) {
                    if (!contestedOrders.contains(order))
                        contestedOrders.add(order);
                    if (!contestedOrders.contains(order2))
                        contestedOrders.add(order2);
                }
            }
        }

        return contestedOrders;

    }

    public void printUnits(Collection<Order> orders, String preamble) {
        if (preamble == null) preamble = "";
        if (!preamble.isBlank()) {
            System.out.println("\t".repeat(tabsCounter) + preamble);
            if (currentFileWriter != null) {
                try {
                    currentFileWriter.write("\t".repeat(tabsCounter) + preamble + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        for (Order order : orders) {
            String unitText = "\t".repeat(tabsCounter) + order.parentUnit.toString();
            if (order.dislodged) {
                unitText += " :: DISLODGED";
                unitText += "\n" + "\t".repeat(tabsCounter+1) + "POSSIBLE RETREATS: " + order.parentUnit.getPossibleRetreats();
            }
            System.out.println(unitText);
            if (currentFileWriter != null) {
                try {
                    currentFileWriter.write(unitText + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println();
        if (currentFileWriter != null) {
            try {
                currentFileWriter.write("\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void printOrders(Collection<Order> orders, String preamble) {

        if (preamble == null) preamble = "";
        if (!preamble.isBlank())
            System.out.println("\t".repeat(tabsCounter) + preamble);

        for (Order order : orders) {
            String orderText = "\t".repeat(tabsCounter) + order.toString();
            System.out.println(orderText);
        }

        System.out.println();

    }

    public void printOrders(Collection<Order> orders) {
        printOrders(orders, "");
    }

    public void printUnits(Collection<Order> orders) {
        printUnits(orders, "");
    }

    /**
     * Adjudicator.run() is for thread context ONLY!!
     * This method is only used by TEST CASES as a direct hook into the Adjudicator class.
     */
    @Override
    public void run() {
        resolve();
    }

}