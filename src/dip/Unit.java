package dip;

import java.util.ArrayList;
import java.util.List;

public class Unit {

    private Nation parentNation;
    private Province position;
    private int unitType;  // 0 for army, 1 for fleet

    // The fun properties - track where units have been throughout the game
    private final int birthYear;
    private final int uniqueID;
    private static int uniqueIDIncrement = 0;
    private List<Order> orderHistory;

    private Order actingOrder;

    public void updateActingOrder(Order newOrder) {
        this.actingOrder = newOrder;
    }

    public void resetOrder() {
        this.orderHistory.add(actingOrder);
        updateActingOrder(new Order(this));  // Reset order to NMR
    }

    public int getBirthYear() {
        return birthYear;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public int getUnitType() {
        return unitType;
    }

    public Province getPosition() {
        return position;
    }

    public Order getActingOrder() {
        return actingOrder;
    }

    public Nation getParentNation() {
        return parentNation;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public Unit(Nation parentNation, Province position, int unitType) {
        this.parentNation = parentNation;
        this.position = position;
        this.unitType = unitType;
        this.birthYear = GameState.gameYear;
        this.uniqueID = uniqueIDIncrement++;
        this.orderHistory = new ArrayList<>();
    }

    public String toString() {
        String output = parentNation.getAdjective() + " ";
        if (unitType == 0)
            output += "A ";
        else
            output += "F ";
        output += position.name();
        return output;
    }

}