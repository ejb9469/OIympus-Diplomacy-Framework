package indexing;

import adjudication.*;

import java.util.ArrayList;
import java.util.List;

public class PGNDipTranslation implements MovetextTranslator {

    @Override
    public List<Order[]> parseStringToOrders(String movetext) throws NullPointerException, IndexOutOfBoundsException {

        List<Order[]> allOrders = new ArrayList<>();

        String[] ordersForTurnsStrings = movetext.split("---");  // Splits on triple dashes

        for (String orderForTurnStr : ordersForTurnsStrings) {

            String[] orderStrings = orderForTurnStr.split("\\r?\\n|\\r");  // Splits on all newlines
            Order[] orders = new Order[orderStrings.length];

            for (int i = 0; i < orderStrings.length; i++) {

                String orderStr = orderStrings[i];

                String[] orderDetails = orderStr.split("\\s+");  // Splits on whitespace
                NATION nation = NATION.valueOf(orderDetails[0]);
                int unitType;
                if (orderDetails[1].equalsIgnoreCase("F"))
                    unitType = 1;
                else
                    unitType = 0;
                PROVINCE pr1 = PROVINCE.valueOf(orderDetails[2]);
                ORDER_TYPE orderType = ORDER_TYPE.fromAbbr(orderDetails[3].toUpperCase());

                Unit unit = new Unit(nation, pr1, unitType);
                Order order;
                if (orderType == ORDER_TYPE.SUPPORT || orderType == ORDER_TYPE.CONVOY) {
                    boolean viaConvoy = orderDetails[orderDetails.length - 2].equals("VIA") && orderDetails[orderDetails.length - 1].equals("CONVOY");
                    PROVINCE pr2 = PROVINCE.valueOf(orderDetails[4]);
                    order = new Order(unit, orderType, pr1, pr2, viaConvoy);
                } else {
                    order = new Order(unit, orderType, pr1, pr1);
                }

                orders[i] = order;

            }

            allOrders.add(orders);

        }

        return allOrders;

    }

    @Override
    public String parseOrdersToString(List<Order[]> ordersList) {
        StringBuilder finalStr = new StringBuilder();
        for (Order[] orders : ordersList) {
            StringBuilder turnStr = new StringBuilder();
            for (Order order : orders)
                turnStr.append(order.toString()).append("\n");
            turnStr.append("---\n");
            finalStr.append(turnStr);
        }
        return finalStr.toString();
    }

    @Override
    public String getTranslatorName() {
        return this.getClass().getName();
    }

}