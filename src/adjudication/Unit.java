package adjudication;

import java.util.*;

public class Unit {

    private NATION parentNation;
    private PROVINCE position;
    private int unitType;  // 0 for army, 1 for fleet

    // The fun properties - track where units have been throughout the game
    private final int birthYear;
    private final int uniqueID;
    private static int uniqueIDIncrement = 0;
    private List<Order> orderHistory;

    private Order actingOrder;
    private List<PROVINCE> possibleRetreats = new ArrayList<>();

    // This field is only for test cases
    public boolean testCaseRetreat = false;

    public void populateRetreatsList(PROVINCE attackOrigin, Collection<Unit> units) {
        Set<PROVINCE> occupiedProvinces = new HashSet<>();  // Sets cannot contain duplicates
        for (Unit unit : units)
            occupiedProvinces.add(unit.getPosition());
        for (PROVINCE province : PROVINCE.values()) {
            if (province != position && province.isAdjacentTo(position) && province != attackOrigin && !occupiedProvinces.contains(province)) {
                if ((unitType == 0 && province.isWater()) || (unitType == 1 && !province.isCoastal() && !province.isWater())) continue;
                possibleRetreats.add(province);
            }
        }
    }

    public void wipeRetreatsList() {
        possibleRetreats = new ArrayList<>();
    }

    public List<PROVINCE> getPossibleRetreats() {
        return possibleRetreats;
    }

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

    public PROVINCE getPosition() {
        return position;
    }

    public void setPosition(PROVINCE position) {
        this.position = position;
    }

    public Order getActingOrder() {
        return actingOrder;
    }

    public NATION getParentNation() {
        return parentNation;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public Unit(NATION parentNation, PROVINCE position, int unitType) {
        this.parentNation = parentNation;
        this.position = position;
        this.unitType = unitType;
        this.birthYear = Game.gameYear;
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

    public boolean equals(Object unit) {
        if (!(unit instanceof Unit)) {
            return super.equals(unit);
        } else {
            return (this.toString().equals(unit.toString()));
        }
    }

}