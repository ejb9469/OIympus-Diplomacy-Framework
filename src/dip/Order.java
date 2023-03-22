package dip;

import dip.exceptions.BadOrderException;

import java.util.ArrayList;
import java.util.List;

public class Order {

    OrderState state;  //  Unresolved (0), Guessing (1), Resolved (2)
    OrderResolution orderResolution;  // Succeeds (0), fails (1), or unresolved (2)

    Unit parentUnit;
    OrderType orderType;  // -1 = NMR, 0 = hold, 1 = move, 2 = support, 3 = convoy
    Province pr1;
    Province pr2;
    boolean viaConvoy = false;

    Province prInitial;

    // Retreat flags
    boolean dislodged = false;
    List<Province> possibleRetreats = new ArrayList<>();

    // Move flags
    boolean bounce = false;

    // Convoy flags
    boolean convoyEndangered = false;
    boolean convoyAttacked = false;
    boolean noConvoy = false;

    // Support flags
    boolean cut = false;
    List<Order> noHelpList = new ArrayList<>();  // List of Orders that this order cannot receive support from under certain circumstances, most notably same-power support

    public static Order parseUnit(String lingo) throws BadOrderException {

        String[] lingoArray = lingo.split(" ");

        if (lingoArray.length < 4)
            throw new BadOrderException();

        boolean viaConvoy = lingoArray[lingoArray.length-2].equals("VIA") && lingoArray[lingoArray.length-1].equals("CONVOY");

        if (lingoArray.length > 7 && !viaConvoy)
            throw new BadOrderException();

        // e.g. Russian A Bud S Rum - Ser
        String unitNationString = lingoArray[0];
        String unitTypeString = lingoArray[1];
        String unitPositionString = lingoArray[2];
        int unitTypeInt = 0;
        if (unitTypeString.equals("F"))
            unitTypeInt = 1;

        Unit unit = new Unit(Nation.valueOf(unitNationString), Province.valueOf(unitPositionString), unitTypeInt);
        String orderTypeString = lingoArray[3];

        OrderType orderType;

        Province province1;
        Province province2;

        if (orderTypeString.equals("-")) {
            orderType = OrderType.MOVE;
            province1 = Province.valueOf(lingoArray[4]);
            province2 = province1;
        } else if (orderTypeString.equals("H")) {
            orderType = OrderType.HOLD;
            province1 = Province.valueOf(unitPositionString);
            province2 = province1;
        } else if (orderTypeString.equals("S")) {
            orderType = OrderType.SUPPORT;
            province1 = Province.valueOf(lingoArray[4]);
            if (lingoArray[lingoArray.length-1].equals("H"))
                province2 = province1;
            else
                province2 = Province.valueOf(lingoArray[6]);
        } else if (orderTypeString.equals("C")) {
            orderType = OrderType.CONVOY;
            province1 = Province.valueOf(lingoArray[4]);
            province2 = Province.valueOf(lingoArray[6]);
        } else {
            throw new BadOrderException();
        }

        return new Order(unit, orderType, province1, province2, viaConvoy);

    }

    private void setPrInitial() {
        prInitial = parentUnit.getPosition();
    }

    private boolean testValidity() {
        return true;  // TODO - .borders(), etc.
    }

    public Order(Unit parentUnit, OrderType orderType, Province provinceSlot1, Province provinceSlot2, boolean viaConvoy) {
        this(parentUnit, orderType, provinceSlot1, provinceSlot2);
        this.viaConvoy = viaConvoy;
    }

    public Order(Unit parentUnit, OrderType orderType, Province provinceSlot1, Province provinceSlot2) {
        this.parentUnit = parentUnit;
        this.orderType = orderType;
        this.pr1 = provinceSlot1;
        this.pr2 = provinceSlot2;
        this.dislodged = false;
        setPrInitial();
    }

    public Order(Unit parentUnit) {  // NMR
        this.parentUnit = parentUnit;
        this.orderType = OrderType.NONE;
        this.pr1 = parentUnit.getPosition();
        this.pr2 = parentUnit.getPosition();
        this.dislodged = false;
        setPrInitial();
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
            output += " C " + pr1.name() + " -> " + pr2.name();
        } else if (orderType == OrderType.VOID) {
            output += " VOID";
        } else {
            output += " NMR";
        }
        return output;
    }

    public boolean equals(Object order) {
        if (!(order instanceof Order)) {
            return super.equals(order);
        } else {
            return (this.toString().equals(order.toString()));
        }
    }

}