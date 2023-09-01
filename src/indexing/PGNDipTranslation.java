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
                Nation nation = Nation.valueOf(orderDetails[0]);
                int unitType;
                if (orderDetails[1].equalsIgnoreCase("F"))
                    unitType = 1;
                else
                    unitType = 0;
                Province pr1 = Province.valueOf(orderDetails[2]);
                OrderType orderType = OrderType.fromAbbr(orderDetails[3].toUpperCase());

                Unit unit = new Unit(nation, pr1, unitType);
                Order order;
                if (orderType == OrderType.SUPPORT || orderType == OrderType.CONVOY) {
                    boolean viaConvoy = orderDetails[orderDetails.length - 2].equals("VIA") && orderDetails[orderDetails.length - 1].equals("CONVOY");
                    Province pr2 = Province.valueOf(orderDetails[4]);
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
    }

    @Override
    public String getTranslatorName() {
        return this.getClass().getName();
    }

}