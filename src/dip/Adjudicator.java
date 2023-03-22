package dip;

import java.util.*;
import java.io.File;
import java.io.IOException;

public class Adjudicator {

    private Map<Order, OrderResolution> orderResolutions;
    private List<Order> ordersList;

    Adjudicator(List<Order> orders) {
        orderResolutions = new HashMap<>();
        for (Order order : orders) {
            orderResolutions.put(order, OrderResolution.UNRESOLVED);
        }
        ordersList = orders;
    }

    private static final int INPUT_MODE = 2;

    private static int tabsCounter = 0;

    public static void main(String[] args) throws IOException {

        List<Order> orders = new ArrayList<>();

        if (INPUT_MODE == 0) {
            orders.add(new Order(new Unit(Nation.ENGLAND, Province.Bel, 1), OrderType.SUPPORT, Province.Ruh, Province.Hol));
            orders.add(new Order(new Unit(Nation.FRANCE, Province.Ruh, 0), OrderType.MOVE, Province.Hol, Province.Hol));
            orders.add(new Order(new Unit(Nation.GERMANY, Province.Hol, 0), OrderType.SUPPORT, Province.Ruh, Province.Bel));
            orders.add(new Order(new Unit(Nation.ITALY, Province.ION, 1), OrderType.MOVE, Province.Tun, Province.Tun));
            orders.add(new Order(new Unit(Nation.ENGLAND, Province.Lon, 0), OrderType.MOVE, Province.Den, Province.Den));
            orders.add(new Order(new Unit(Nation.ENGLAND, Province.NTH, 1), OrderType.CONVOY, Province.Lon, Province.Den));
            orders.add(new Order(new Unit(Nation.RUSSIA, Province.Nwy, 1), OrderType.MOVE, Province.NTH, Province.NTH));
            orders.add(new Order(new Unit(Nation.RUSSIA, Province.NWG, 1), OrderType.SUPPORT, Province.Nwy, Province.NTH));
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
            File testCaseFolder = new File("src/dip/testcases");
            File[] testCaseFiles = testCaseFolder.listFiles();
            //if (testCaseFiles == null) return;
            Scanner sc;
            for (File testCaseFile : testCaseFiles) {
                tabsCounter = 0;
                System.out.println(testCaseFile.getName() + "\n");
                orders = new ArrayList<>();
                sc = new Scanner(testCaseFile);
                tabsCounter = 1;
                while (sc.hasNextLine()) {
                    String orderText = sc.nextLine();
                    if (orderText.isBlank()) continue;
                    Order order = Order.parseUnit(orderText);
                    orders.add(order);
                    //System.out.println(order.toString());
                }
                sc.close();
                new Adjudicator(orders).resolve();
            }
        }
    }

    Map<Order, Integer> supportCounts = new HashMap<>();

    Map<Order, Order> supportMap = new HashMap<>();

    List<Order> convoyingArmies = new ArrayList<>();
    Map<Order, List<Order>> convoyPaths = new HashMap<>();

    List<Order> successfulConvoyingArmies = new ArrayList<>();

    List<Order> contestedOrders = new ArrayList<>();
    List<Order> correspondingSupports = new ArrayList<>();

    Map<Province, List<Order>> battleList = new HashMap<>();

    /**
     * The resolve function - all the Diplomacy adjudication logic is in here.
     */
    void resolve() {

        List<Order> orders = new ArrayList<>(ordersList);

        for (Order order : orders)
            supportCounts.put(order, 0);

        convoyingArmies = findConvoyingArmies(orders);

        for (Order convoyingArmy : convoyingArmies) {
            List<Order> convoyPath = drawConvoyPath(orders, convoyingArmy);
            if (convoyPath.size() == 0)
                convoyingArmy.orderType = OrderType.VOID;
            else
                convoyPaths.put(convoyingArmy, convoyPath);
        }

        contestedOrders = findContestedOrders(orders);
        correspondingSupports = findCorrespondingSupports(orders, contestedOrders);

        printOrders(orders, "ALL ORDERS:");
        //printOrders(contestedOrders, "CONTESTED ORDERS:");
        //printOrders(correspondingSupports, "CORRESPONDING SUPPORTS:");
        //System.out.println("======\n");

        List<Order> invalidSupports = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderType == OrderType.SUPPORT) {
                List<Order> orders2 = orders;
                if (!order.pr1.isAdjacentTo(order.pr2) && order.pr1 != order.pr2)  // The only reason e.g. Bel S Lon - Hol would work is if e.g. NTH C Lon - Hol
                    orders2 = convoyingArmies;
                boolean found = false;
                for (Order order2 : orders2) {
                    if (order.equals(order2)) continue;
                    if (order.pr1 == order2.parentUnit.getPosition()) {
                        found = true;
                        supportMap.put(order, order2);
                        if (order2.orderType == OrderType.MOVE) {  // Support-to-hold on a MOVE order
                            if (order.pr1 == order.pr2)
                                invalidSupports.add(order);
                        } else {
                            if (order.pr1 != order.pr2)  // Support-to-move on a stationary order
                                invalidSupports.add(order);
                        }
                        break;
                    }
                }
                if (!found)  // No corresponding order taking the support
                    invalidSupports.add(order);
            }
        }

        // Replace invalid supports with holds
        for (Order invalidSupport : invalidSupports) {
            orders.remove(invalidSupport);
            orders.add(new Order(invalidSupport.parentUnit, OrderType.HOLD, invalidSupport.parentUnit.getPosition(), invalidSupport.parentUnit.getPosition()));
        }

        // Increment the support count for all [implicitly] valid supports
        for (Order order : orders) {
            if (order.orderType != OrderType.SUPPORT) continue;
            supportCounts.put(supportMap.get(order), supportCounts.get(supportMap.get(order)) + 1);
        }

        // Set the 'no help' flags for supports on move orders attacking units of the same Nation
        for (Order supportOrder : supportMap.keySet()) {
            Order supportedOrder = supportMap.get(supportOrder);
            if (supportedOrder.orderType != OrderType.MOVE) continue;
            Order attackedUnit = findUnitAtPosition(supportedOrder.pr1, orders);
            if (attackedUnit != null) {
                if (attackedUnit.parentUnit.getParentNation() == supportOrder.parentUnit.getParentNation())
                    supportedOrder.noHelpList.add(supportOrder);
            }
        }

        List<Order> contestedOrdersNoConvoys = new ArrayList<>(contestedOrders);
        contestedOrdersNoConvoys.removeAll(convoyingArmies);

        for (Order contestedOrder : contestedOrdersNoConvoys) {
            if (contestedOrder.orderType != OrderType.MOVE) continue;
            cutSupport(contestedOrder);
        }

        battleList = populateBattleList(contestedOrdersNoConvoys);

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
            boolean anyUnitBounced = true;
            while (anyUnitBounced) {
                anyUnitBounced = false;
                for (Order moveOrder : contestedOrdersNoConvoys) {
                    if (moveOrder.orderType != OrderType.MOVE) continue;
                    Order attackedUnit = findUnitAtPosition(moveOrder.pr1, contestedOrdersNoConvoys);
                    if (attackedUnit == null) continue;
                    if (attackedUnit.orderType != OrderType.MOVE) continue;
                    if (attackedUnit.bounce) continue;
                    if (attackedUnit.pr1 != moveOrder.parentUnit.getPosition()) continue;
                    if (moveOrder.parentUnit.getParentNation() == attackedUnit.parentUnit.getParentNation()
                            || supportCounts.get(moveOrder) - moveOrder.noHelpList.size() <= supportCounts.get(attackedUnit)) {
                        //System.out.println("No-swap Type A bounce() called for " + moveOrder.parentUnit);
                        bounce(moveOrder);
                        anyUnitBounced = true;
                    }
                    if (moveOrder.parentUnit.getParentNation() == attackedUnit.parentUnit.getParentNation()
                            || supportCounts.get(attackedUnit) - attackedUnit.noHelpList.size() <= supportCounts.get(moveOrder)) {
                        //System.out.println("No-swap Type B bounce() called for " + moveOrder.parentUnit);
                        bounce(attackedUnit);
                        anyUnitBounced = true;
                    }
                }
            }

            // Mark bounces suffered by understrength attackers
            for (Province province : Province.values()) {
                for (Order battler : battleList.get(province)) {
                    int battlerSupportCount = supportCounts.get(battler);
                    boolean isStrongest = true;
                    for (Order battler2 : battleList.get(province)) {
                        if (battler.equals(battler2)) continue;
                        if (supportCounts.get(battler2) >= battlerSupportCount) {
                            isStrongest = false;
                            break;
                        }
                    }
                    if (!isStrongest && battler.orderType == OrderType.MOVE && !battler.bounce) {
                        //System.out.println("Understrength attackers bounce() called for " + battler.parentUnit);
                        bounce(battler);
                        anyUnitBouncedOuter = true;
                    }
                }
            }

            if (anyUnitBouncedOuter) continue;

            // Mark bounces caused by inability to self-dislodge
            for (Province province : Province.values()) {
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
                if (strongestBattler.bounce || strongestBattler.orderType != OrderType.MOVE) continue;

                Order victim = findUnitAtPosition(province, contestedOrders);
                if (victim == null) continue;
                if (victim.orderType == OrderType.MOVE && !victim.bounce) continue;
                if (victim.parentUnit.getParentNation() == strongestBattler.parentUnit.getParentNation()) {
                    //System.out.println("No self-dislodge Type A bounce() called for " + strongestBattler.parentUnit);
                    bounce(strongestBattler);
                    anyUnitBouncedOuter = true;
                    continue;
                }
                supportCounts.replace(victim, supportCounts.get(victim) - victim.noHelpList.size());
                for (Order battler : battleList.get(province)) {
                    if (battler.equals(strongestBattler)) continue;
                    if (supportCounts.get(battler) >= strongestBattlerSupportCount) {
                        //System.out.println("No self-dislodge Type B bounce() called for " + strongestBattler.parentUnit);
                        bounce(strongestBattler);
                        anyUnitBouncedOuter = true;
                        break;
                    }
                }
            }

            if (anyUnitBouncedOuter) continue;

            // Mark supports cut by dislodgements
            for (Order moveOrder : contestedOrders) {
                if (moveOrder.orderType != OrderType.MOVE) continue;
                if (!moveOrder.bounce) {
                    if (cutSupport(moveOrder))
                        anyUnitBouncedOuter = true;  // Misnomer but functionally the same
                }
            }

            if (anyUnitBouncedOuter) continue;

        }

        // Move units that did not bounce
        List<Order> changedOrders = new ArrayList<>(orders);
        for (Order moveOrder : orders) {
            if (moveOrder.orderType != OrderType.MOVE) continue;
            if (moveOrder.bounce) continue;
            Order victim = findUnitAtPosition(moveOrder.pr1, contestedOrders);
            if (victim != null) {
                if (victim.orderType != OrderType.MOVE || victim.bounce) {
                    if (contestedOrdersNoConvoys.contains(victim)
                            && victim.orderType == OrderType.MOVE
                            && victim.pr1 == moveOrder.parentUnit.getPosition()
                            && contestedOrdersNoConvoys.contains(moveOrder))
                    {
                        List<Order> battlersAtSource = battleList.get(victim.pr1);
                        battlersAtSource.remove(victim);
                        battleList.replace(victim.pr1, battlersAtSource);
                    }
                    changedOrders.remove(victim);
                    victim.dislodged = true;
                    changedOrders.add(victim);
                    // TODO: Populate retreats list
                }
            }
            unbounce(moveOrder.parentUnit.getPosition());
            changedOrders.remove(moveOrder);  // This should work b/c of the .equals() override in dip.Order
            moveOrder.parentUnit.setPosition(moveOrder.pr1);  // The move itself!
            changedOrders.add(moveOrder);
        }

        ordersList = changedOrders;

        printUnits(ordersList, "FINAL STATE: ");
        System.out.println();

    }

    private void unbounce(Province province) {
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
     * @param moveOrder
     * @return Whether *any* support was cut
     */
    private boolean cutSupport(Order moveOrder) {
        Order defender = findUnitAtPosition(moveOrder.pr1, contestedOrders);
        if (defender == null) return false;
        if (defender.orderType != OrderType.SUPPORT) return false;
        if (defender.cut) return false;
        if (moveOrder.parentUnit.getParentNation() == defender.parentUnit.getParentNation()) return false;
        if (defender.pr2 == moveOrder.parentUnit.getPosition()) return false;
        if (convoyingArmies.contains(moveOrder)) {
            System.out.println("Adjudicator.cutSupport() is handling a convoying army...");  // Debug
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

    private Order findUnitAtPosition(Province province, Collection<Order> orders) {
        for (Order order : orders) {
            if (order.parentUnit.getPosition().equals(province))
                return order;
        }
        return null;
    }

    private Map<Province, List<Order>> populateBattleList(Collection<Order> orders) {

        Map<Province, List<Order>> battleList = new HashMap<>();

        for (Province province : Province.values()) {
            List<Order> combatants = new ArrayList<>();
            for (Order order : orders) {
                if (order.orderType == OrderType.MOVE) {
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
            if (order.orderType != OrderType.MOVE) continue;
            if (!order.parentUnit.getPosition().isCoastal()) continue;  // You can't convoy from or to inland provinces
            if (!order.pr1.isCoastal()) continue;
            if (!order.parentUnit.getPosition().isAdjacentTo(order.pr1) || order.viaConvoy)
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
            if (order.orderType != OrderType.CONVOY) continue;
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
            if (order.orderType != OrderType.CONVOY) continue;
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
                if (supportOrder.orderType != OrderType.SUPPORT) continue;
                if (order.orderType != OrderType.MOVE && supportOrder.pr1.equals(order.parentUnit.getPosition()) && supportOrder.pr2.equals(order.parentUnit.getPosition())) {
                    correspondingSupports.add(supportOrder);
                    continue;
                }
                if (order.orderType == OrderType.MOVE && supportOrder.pr1.equals(order.parentUnit.getPosition()) && supportOrder.pr2.equals(order.pr1)) {
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
                if (order.orderType != OrderType.MOVE && order2.orderType != OrderType.MOVE) continue;
                if ((order.orderType == OrderType.MOVE && (order.pr1 == order2.parentUnit.getPosition()) || order.pr1 == order2.pr1)) {
                    if (!contestedOrders.contains(order))
                        contestedOrders.add(order);
                    if (!contestedOrders.contains(order2))
                        contestedOrders.add(order2);
                }
            }
        }

        return contestedOrders;

    }

    private static void printUnits(Collection<Order> orders, String preamble) {
        if (!preamble.isBlank())
            System.out.println("\t".repeat(tabsCounter) + preamble);
        for (Order order : orders) {
            String unitText = "\t".repeat(tabsCounter) + order.parentUnit.toString();
            if (order.dislodged)
                unitText += " :: DISLODGED";
            System.out.println(unitText);
        }
        System.out.println();
    }

    private static void printOrders(Collection<Order> orders, String preamble) {

        if (!preamble.isBlank())
            System.out.println("\t".repeat(tabsCounter) + preamble);

        for (Order order : orders) {
            String orderText = "\t".repeat(tabsCounter) + order.toString();
            System.out.println(orderText);
        }

        System.out.println();

    }

    private static void printOrders(Collection<Order> orders) {
        printOrders(orders, "");
    }

    private static void printUnits(Collection<Order> orders) {
        printUnits(orders, "");
    }

}