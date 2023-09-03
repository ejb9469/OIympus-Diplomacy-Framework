package adjudication;

public class RetreatOrder extends Order {

    public RetreatOrder(Unit parentUnit) {
        super(parentUnit);
        this.dislodged = true;
    }

    public RetreatOrder(Unit parentUnit, PROVINCE pr1) {
        this(parentUnit);
        if (pr1 == null)
            this.orderType = ORDER_TYPE.REMOVE;
        else
            this.orderType = ORDER_TYPE.RETREAT;
        this.pr1 = pr1;
    }

    public String toString() {
        String output = parentUnit.toString();
        if (orderType == ORDER_TYPE.REMOVE)
            return output + " REMOVE";
        else if (orderType == ORDER_TYPE.RETREAT)
            return output + " -> " + pr1.name();
        return super.toString();
    }

}