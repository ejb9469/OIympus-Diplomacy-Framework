package dip;

import java.util.ArrayList;
import java.util.List;

public class Order {

    OrderState state;  //  Unresolved (0), Guessing (1), Resolved (2)
    OrderResolution orderResolution;  // Succeeds (0), fails (1), or unresolved (2)

    Unit parentUnit;
    OrderType orderType;  // -1 = NMR, 0 = hold, 1 = move, 2 = support, 3 = convoy
    Province pr1;
    Province pr2;

    boolean dislodged = false;

    // Move flags
    boolean bounce = false;

    // Convoy flags
    boolean convoyEndangered = false;
    boolean convoyAttacked = false;
    boolean noConvoy = false;

    // Support flags
    boolean cut = false;
    List<Order> noHelpList = new ArrayList<>();  // List of Orders that this order cannot receive support from under certain circumstances, most notably same-power support

    private boolean testValidity() {
        return true;  // TODO - .borders(), etc.
    }

    public Order(Unit parentUnit, OrderType orderType, Province provinceSlot1, Province provinceSlot2) {
        this.parentUnit = parentUnit;
        this.orderType = orderType;
        this.pr1 = provinceSlot1;
        this.pr2 = provinceSlot2;
        this.dislodged = false;
    }

    public Order(Unit parentUnit) {  // NMR
        this.parentUnit = parentUnit;
        this.orderType = OrderType.NONE;
        this.pr1 = null;  // Keep these null assignments in mind
        this.pr2 = null;
        this.dislodged = false;
    }

    public String toString() {
        String output = parentUnit.toString();
        if (orderType == OrderType.MOVE) {
            output += " -> " + pr1.name();  // .getName() would return the Province's full name
        } else if (orderType == OrderType.HOLD) {
            output += " H ";
        } else if (orderType == OrderType.SUPPORT) {
            output += " S " + pr1.name() + " ";
            if (pr1.equals(pr2))
                output += "H";
            else
                output += "-> " + pr2.name();
        } else if (orderType == OrderType.CONVOY) {
            output += " C " + pr1.name() + "-> " + pr2.name();
        } else {
            output += " NMR";
        }
        return output;
    }

}