package adjudication;

public class AdjustmentOrder extends Order {

    public AdjustmentOrder(Unit parentUnit) {
        super(parentUnit);
    }

    public AdjustmentOrder(Unit parentUnit, boolean piff) {
        this(parentUnit);
        if (piff)
            this.orderType = ORDER_TYPE.REMOVE;
        else
            this.orderType = ORDER_TYPE.BUILD;
    }

}