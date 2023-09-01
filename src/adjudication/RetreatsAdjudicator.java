package adjudication;

import java.util.*;

public class RetreatsAdjudicator {

    private List<RetreatOrder> ordersList;

    public Collection<RetreatOrder> getOrders() {
        return ordersList;
    }

    RetreatsAdjudicator(List<RetreatOrder> ordersList) {
        this.ordersList = ordersList;
    }

    public void resolve() {
        List<RetreatOrder> orders = new ArrayList<>(ordersList);
        Set<Province> clashes = new HashSet<>();
        for (RetreatOrder order : orders) {
            if (order.orderType != OrderType.RETREAT) {
                order.deleteMe = true;
                continue;
            }
            if (!order.parentUnit.getPossibleRetreats().contains(order.pr1)) {
                order.deleteMe = true;
                continue;
            }
            if (clashes.contains(order.pr1)) {
                order.deleteMe = true;
            } else {
                for (RetreatOrder order2 : orders) {
                    if (order2.orderType != OrderType.RETREAT) continue;
                    if (order.pr1 == order2.pr1 && order2.parentUnit.getPossibleRetreats().contains(order2.pr1)) {
                        clashes.add(order.pr1);
                        order.deleteMe = true;
                        order2.deleteMe = true;
                        break;
                    }
                }
            }
        }
        ordersList = orders;
        for (RetreatOrder order : ordersList)
            order.parentUnit.wipeRetreatsList();
    }

    public void printUnits(Collection<Order> orders, String preamble) {

    }

    public void printOrders(Collection<Order> orders, String preamble) {

    }

}
