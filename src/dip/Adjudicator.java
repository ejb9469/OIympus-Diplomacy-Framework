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
        new Adjudicator(orders).resolve();
    }

    void resolve() {

        List<Order> orders = new ArrayList<>(ordersList);
        Map<Order, OrderResolution> orderResolutions = new HashMap<>(this.orderResolutions);

        List<Order> contestedOrders = findContestedOrders(orders);
        List<Order> correspondingSupports = findCorrespondingSupports(orders, contestedOrders);

        printOrders(orders, "ALL ORDERS:");
        printOrders(contestedOrders, "CONTESTED ORDERS:");
        printOrders(correspondingSupports, "CORRESPONDING SUPPORTS:");
        System.out.println("\nDONE!");

    }

    private List<Order> findCorrespondingSupports(List<Order> orders, List<Order> matching) {

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

    private List<Order> findContestedOrders(List<Order> orders) {

        List<Order> contestedOrders = new ArrayList<>();

        for (Order order : orders) {
            for (Order order2 : orders) {
                // A unit cannot contest itself
                if (order.equals(order2)) continue;
                // Two holds cannot contest each other, in any respect
                if (order.orderType != OrderType.MOVE && order2.orderType != OrderType.MOVE) continue;
                if (order.orderType == OrderType.MOVE && (order.pr1.equals(order2.parentUnit.getPosition())) || order.pr1.equals(order2.pr1)) {
                    contestedOrders.add(order);
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