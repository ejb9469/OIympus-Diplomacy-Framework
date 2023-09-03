package adjudication;

import exceptions.BadOrderException;

import java.util.ArrayList;
import java.util.List;

public class Order {

    public Unit parentUnit;
    public ORDER_TYPE orderType;  // -1 = NMR, 0 = hold, 1 = move, 2 = support, 3 = convoy
    public PROVINCE pr1;
    public PROVINCE pr2;
    public boolean viaConvoy = false;

    public boolean deleteMe = false;

    public PROVINCE prInitial;

    // Retreat flags
    public boolean dislodged;

    // Move flags
    public boolean bounce = false;

    // Convoy flags
    public boolean convoyEndangered = false;
    public boolean convoyAttacked = false;
    public boolean noConvoy = false;

    // Support flags
    public boolean cut = false;
    // List of Orders that this order cannot receive support from under certain circumstances, most notably same-power support
    public List<Order> noHelpList = new ArrayList<>();

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

        Unit unit = new Unit(NATION.valueOf(unitNationString), PROVINCE.valueOf(unitPositionString), unitTypeInt);
        String orderTypeString = lingoArray[3];

        ORDER_TYPE orderType;

        PROVINCE province1;
        PROVINCE province2;

        if (orderTypeString.equals("-")) {
            orderType = ORDER_TYPE.MOVE;
            province1 = PROVINCE.valueOf(lingoArray[4]);
            province2 = province1;
        } else if (orderTypeString.equals("H")) {
            orderType = ORDER_TYPE.HOLD;
            province1 = PROVINCE.valueOf(unitPositionString);
            province2 = province1;
        } else if (orderTypeString.equals("S")) {
            orderType = ORDER_TYPE.SUPPORT;
            province1 = PROVINCE.valueOf(lingoArray[4]);
            if (lingoArray[lingoArray.length-1].equals("H"))
                province2 = province1;
            else
                province2 = PROVINCE.valueOf(lingoArray[6]);
        } else if (orderTypeString.equals("C")) {
            orderType = ORDER_TYPE.CONVOY;
            province1 = PROVINCE.valueOf(lingoArray[4]);
            province2 = PROVINCE.valueOf(lingoArray[6]);
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

    public Order(Unit parentUnit, ORDER_TYPE orderType, PROVINCE provinceSlot1, PROVINCE provinceSlot2, boolean viaConvoy) {
        this(parentUnit, orderType, provinceSlot1, provinceSlot2);
        this.viaConvoy = viaConvoy;
    }

    public Order(Unit parentUnit, ORDER_TYPE orderType, PROVINCE provinceSlot1, PROVINCE provinceSlot2) {
        this.parentUnit = parentUnit;
        this.orderType = orderType;
        this.pr1 = provinceSlot1;
        this.pr2 = provinceSlot2;
        this.dislodged = false;
        setPrInitial();
    }

    public Order(Unit parentUnit) {  // NMR
        this.parentUnit = parentUnit;
        this.orderType = ORDER_TYPE.NONE;
        this.pr1 = parentUnit.getPosition();
        this.pr2 = parentUnit.getPosition();
        this.dislodged = false;
        setPrInitial();
    }

    public String toString() {
        String output = parentUnit.toString();
        if (orderType == ORDER_TYPE.MOVE) {
            output += " -> " + pr1.name();  // .getName() would return the PROVINCE's full name
        } else if (orderType == ORDER_TYPE.HOLD) {
            output += " H ";
        } else if (orderType == ORDER_TYPE.SUPPORT) {
            output += " S " + pr1.name() + " ";
            if (pr1.equals(pr2))
                output += "H";
            else
                output += "-> " + pr2.name();
        } else if (orderType == ORDER_TYPE.CONVOY) {
            output += " C " + pr1.name() + " -> " + pr2.name();
        } else if (orderType == ORDER_TYPE.VOID) {
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