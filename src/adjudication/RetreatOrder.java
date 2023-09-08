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

}