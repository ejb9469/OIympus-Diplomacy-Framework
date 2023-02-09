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

        List<Order> contestedOrders = findContestedOrders(orders);
        List<Order> correspondingSupports = findCorrespondingSupports(orders, contestedOrders);

        printOrders(orders, "ALL ORDERS:");
        printOrders(contestedOrders, "CONTESTED ORDERS:");
        printOrders(correspondingSupports, "CORRESPONDING SUPPORTS:");
        System.out.println("======\n");

        List<Order> supportsToCut = new ArrayList<>();
        for (Order order : contestedOrders) {
            if (order.orderType == OrderType.SUPPORT) {
                List<Order> attackers = findAttackers(contestedOrders, order);
                boolean cut = false;
                for (Order attacker : attackers) {
                    if (!attacker.pr1.equals(order.pr2)) {  // Support cannot be cut by unit being attacked by supportee
                        cut = true;
                        break;
                    }
                }
                if (cut) {
                    orders.remove(order);
                    supportsToCut.add(order);
                }
                // Note: We cannot be sure the support order succeeds if it simply isn't cut: it can be cut if dislodged also
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

        strengthMap = calculateStrengths(contestedOrders, orderResolutions);

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