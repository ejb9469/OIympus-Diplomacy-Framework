package dip;

import java.util.*;

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

    public static void main(String[] args) {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(new Unit(Nation.ENGLAND, Province.Bel, 1), OrderType.SUPPORT, Province.Ruh, Province.Hol));
        orders.add(new Order(new Unit(Nation.FRANCE, Province.Ruh, 0), OrderType.MOVE, Province.Hol, Province.Hol));
        orders.add(new Order(new Unit(Nation.GERMANY, Province.Hol, 0), OrderType.SUPPORT, Province.Ruh, Province.Bel));
        orders.add(new Order(new Unit(Nation.ITALY, Province.ION, 1), OrderType.MOVE, Province.Tun, Province.Tun));
        new Adjudicator(orders).resolve();
    }

    void resolve() {

        List<Order> orders = new ArrayList<>(ordersList);
        Map<Order, OrderResolution> orderResolutions = new HashMap<>();

        Map<Order, Integer> strengthMap = new HashMap<>();
        Map<Order, Integer> supportCounts = new HashMap<>();

        for (Order order : orders)
            supportCounts.put(order, 0);

        Map<Order, Order> supportMap = new HashMap<>();

        List<Order> convoyingArmies = findConvoyingArmies(orders);
        Map<Order, List<Order>> convoyPaths = new HashMap<>();

        for (Order convoyingArmy : convoyingArmies) {
            List<Order> convoyPath = drawConvoyPath(orders, convoyingArmy);
            if (convoyPath.size() == 0)
                convoyingArmy.orderType = OrderType.VOID;
            else
                convoyPaths.put(convoyingArmy, convoyPath);
        }

        List<Order> contestedOrders = findContestedOrders(orders);
        List<Order> correspondingSupports = findCorrespondingSupports(orders, contestedOrders);

        printOrders(orders, "ALL ORDERS:");
        printOrders(contestedOrders, "CONTESTED ORDERS:");
        printOrders(correspondingSupports, "CORRESPONDING SUPPORTS:");
        System.out.println("======\n");

        List<Order> invalidSupports = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderType == OrderType.SUPPORT) {
                List<Order> orders2 = orders;
                if (!order.pr1.isAdjacentTo(order.pr2))  // The only reason e.g. Bel S Lon - Hol would work is if e.g. NTH C Lon - Hol
                    orders2 = convoyingArmies;
                boolean found = false;
                for (Order order2 : orders2) {
                    if (order.equals(order2)) continue;
                    if (order.pr1.equals(order2.parentUnit.getPosition())) {
                        found = true;
                        supportMap.put(order, order2);
                        if (order2.orderType == OrderType.MOVE) {  // Support-to-hold on a MOVE order
                            if (order.pr1.equals(order.pr2))
                                invalidSupports.add(order);
                        } else {
                            if (!order.pr1.equals(order.pr2))  // Support-to-move on a stationary order
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
            supportCounts.put(supportMap.get(order), supportCounts.get(supportMap.get(order))+1);
        }

        // Set the 'no help' flags for supports on move orders attacking units of the same Nation
        for (Order supportOrder : supportMap.keySet()) {
            Order supportedOrder = supportMap.get(supportOrder);
            if (supportedOrder.orderType != OrderType.MOVE) continue;
            Unit attackedUnit = findUnitAtPosition(supportedOrder.pr1, orders);
            if (attackedUnit != null) {
                if (attackedUnit.getParentNation() == supportOrder.parentUnit.getParentNation())
                    supportedOrder.noHelpList.add(supportOrder);
            }
        }

        List<Order> supportsToCut = new ArrayList<>();
        List<Order> contestedOrdersNoConvoys = new ArrayList<>(contestedOrders);
        contestedOrdersNoConvoys.removeAll(convoyingArmies);

        for (Order contestedOrder : contestedOrdersNoConvoys) {
            if (contestedOrder.orderType == OrderType.SUPPORT) {
                List<Order> attackers = findAttackers(contestedOrders, contestedOrder);
                boolean cut = false;
                for (Order attacker : attackers) {
                    if (!attacker.pr1.equals(contestedOrder.pr2)) {  // Support cannot be cut by unit being attacked by supportee
                        cut = true;
                        break;
                    }
                }
                if (cut) {
                    orders.remove(contestedOrder);
                    supportsToCut.add(contestedOrder);
                }
                // Note: We cannot be sure the support order succeeds if it simply isn't cut: it can still be cut if dislodged under *all* conditions
            }
        }
        contestedOrders.removeAll(supportsToCut);
        supportsToCut = cutSupports(supportsToCut);
        orders.addAll(supportsToCut);
        contestedOrders.addAll(supportsToCut);

        // All uncontested AKA untouched units automatically resolve to `SUCCEEDS`
        for (Order order : orders) {
            if (!contestedOrders.contains(order))
                orderResolutions.put(order, OrderResolution.SUCCEEDS);
            else
                orderResolutions.put(order, OrderResolution.UNRESOLVED);
        }

        Map<Province, List<Order>> battleList = populateBattleList(contestedOrdersNoConvoys);

        for (Order convoyingArmy : convoyingArmies) {

        }

        /////
        //strengthMap = calculateStrengths(contestedOrders, orderResolutions);

    }

    /*private Order checkDisruptions(Order convoyedArmy, List<Order> convoyPath) {

        for (Order convoyingFleet : convoyPath) {

        }

    }*/ // TODO

    private Unit findUnitAtPosition(Province province, List<Order> orders) {
        for (Order order : orders) {
            if (order.parentUnit.getPosition().equals(province))
                return order.parentUnit;
        }
        return null;
    }

    private Map<Province, List<Order>> populateBattleList(List<Order> orders) {

        Map<Province, List<Order>> battleList = new HashMap<>();

        for (Province province : Province.values()) {
            for (Order order : orders) {
                List<Order> combatants = new ArrayList<>();
                if (order.orderType == OrderType.MOVE) {
                    if (order.pr1.equals(province))
                        combatants.add(order);
                } else {
                    if (order.parentUnit.getPosition().equals(province))
                        combatants.add(order);
                }
                if (combatants.size() > 0)
                    battleList.put(province, combatants);
            }
        }
        return battleList;

    }

    private List<Order> findConvoyingArmies(List<Order> orders) {
        List<Order> convoyingArmies = new ArrayList<>();
        for (Order order : orders) {
            if (order.orderType != OrderType.MOVE) continue;
            if (!order.parentUnit.getPosition().isCoastal()) continue;  // You can't convoy inland
            if (!order.parentUnit.getPosition().isAdjacentTo(order.pr1))
                convoyingArmies.add(order);
        }
        return convoyingArmies;
    }

    private List<Order> drawConvoyPath(List<Order> orders, Order moveOrder) {

        List<Order> convoyPath;
        List<Order> beginningConvoys = new ArrayList<>();
        for (Order order : orders) {
            if (order.equals(moveOrder)) continue;  // Technically unnecessary
            if (order.parentUnit.getUnitType() == 1) continue;  // You can't convoy over an army
            if (order.orderType != OrderType.CONVOY) continue;
            if (order.pr1.equals(moveOrder.parentUnit.getPosition()) && order.pr2.equals(moveOrder.pr1) && order.parentUnit.getPosition().isAdjacentTo(moveOrder.parentUnit.getPosition())) {
                beginningConvoys.add(order);
            }
        }

        if (beginningConvoys.size() == 0)
            return beginningConvoys;

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

    private List<Order> findAttackers(Collection<Order> orders, Order matching) {

        List<Order> attackers = new ArrayList<>();

        for (Order order : orders) {
            if (order.equals(matching)) continue;
            if (order.orderType != OrderType.MOVE) continue;
            if (order.pr1.equals(matching.parentUnit.getPosition()))
                attackers.add(order);
        }

        return attackers;

    }

    private Map<Order, Integer> calculateStrengths(List<Order> orders, Map<Order, OrderResolution> orderResolutions) {

        Map<Order, Integer> strengthsMap = new HashMap<>();

        for (Order order : orders) {
            int strength = 1;
            List<Order> supports = findCorrespondingSupports(orderResolutions.keySet(), order);
            for (Order support : supports) {
                if (orderResolutions.get(support) == OrderResolution.SUCCEEDS)
                    strength++;
            }
            strengthsMap.put(order, strength);
            System.out.println("Setting strength value [" + strength + "] to Order:  [[" + order + "]]");
        }

        return strengthsMap;

    }

    /**
     * Transforms support Orders in `supports` to hold Orders
     * @param supports
     * @return
     */
    private List<Order> cutSupports(List<Order> supports) {
        List<Order> holds = new ArrayList<>();
        for (Order support : supports) {
            holds.add(new Order(support.parentUnit, OrderType.HOLD, support.parentUnit.getPosition(), support.parentUnit.getPosition()));
        }
        return holds;
    }

    private List<Order> findSuccessfulOrders(Map<Order, OrderResolution> orderResolutions) {
        List<Order> successfulOrders = new ArrayList<>();
        for (Order order : orderResolutions.keySet()) {
            if (orderResolutions.get(order) == OrderResolution.SUCCEEDS) {
                successfulOrders.add(order);
            }
        }
        return successfulOrders;
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
                if (order.orderType == OrderType.MOVE && (order.pr1.equals(order2.parentUnit.getPosition())) || order.pr1.equals(order2.pr1)) {
                    if (!contestedOrders.contains(order))
                        contestedOrders.add(order);
                    if (!contestedOrders.contains(order2))
                        contestedOrders.add(order2);
                }
            }
        }

        return contestedOrders;

    }

    private void adjudicate() {

    }

    private static void printOrders(Collection<Order> orders, String preamble) {

        if (!preamble.isBlank())
            System.out.println(preamble + "\n");

        for (Order order : orders) {
            System.out.println(order);
        }

        System.out.println("\n");

    }

    private static void printOrders(Collection<Order> orders) {
        printOrders(orders, "");
    }

}