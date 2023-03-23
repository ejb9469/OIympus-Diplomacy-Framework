package dip;

public class RetreatOrder extends Order {

    public RetreatOrder(Unit parentUnit) {
        super(parentUnit);
        this.dislodged = true;
    }

    public RetreatOrder(Unit parentUnit, Province pr1) {
        this(parentUnit);
        if (pr1 == null)
            this.orderType = OrderType.REMOVE;
        else
            this.orderType = OrderType.RETREAT;
        this.pr1 = pr1;
    }

    public String toString() {
        String output = parentUnit.toString();
        if (orderType == OrderType.REMOVE)
            return output + " REMOVE";
        else if (orderType == OrderType.RETREAT)
            return output + " -> " + pr1.name();
        return super.toString();
    }

}