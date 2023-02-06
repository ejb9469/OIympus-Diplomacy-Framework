package dip;

public class Order {

    OrderState state;  //  Unresolved (0), Guessing (1), Resolved (2)
    OrderResolution orderResolution;  // Succeeds (0), fails (1), or unresolved (2)

    private Unit parentUnit;
    private int orderType;  // -1 = NMR, 0 = hold, 1 = move, 2 = support, 3 = convoy
    private Province provinceSlot1;
    private Province provinceSlot2;

    private boolean testValidity() {
        return true;  // TODO - .borders(), etc.
    }

    public Order(Unit parentUnit, int orderType, Province provinceSlot1, Province provinceSlot2) {
        this.parentUnit = parentUnit;
        this.orderType = orderType;
        this.provinceSlot1 = provinceSlot1;
        this.provinceSlot2 = provinceSlot2;
    }

    public Order(Unit parentUnit) {  // NMR
        this.parentUnit = parentUnit;
        this.orderType = -1;
        this.provinceSlot1 = null;  // Keep these null assignments in mind
        this.provinceSlot2 = null;
    }

}