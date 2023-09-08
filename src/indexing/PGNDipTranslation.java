package indexing;

import adjudication.*;
import exceptions.BadOrderException;

import java.util.ArrayList;
import java.util.List;

public class PGNDipTranslation implements MovetextTranslator {

    @Override
    public List<Order[]> parseStringToOrders(String movetext) throws NullPointerException, IndexOutOfBoundsException, BadOrderException {

        List<Order[]> allOrders = new ArrayList<>();
        String[] ordersForTurnsStrings = movetext.split("(\\r?\\n|\\r)\\d*(\\r?\\n|\\r)");  // Splits on multiple newlines

        for (String orderForTurnStr : ordersForTurnsStrings) {

            String[] orderStrings = orderForTurnStr.split("\\r?\\n|\\r");  // Splits on all single newlines
            Order[] orders = new Order[orderStrings.length];

            for (int i = 0; i < orderStrings.length; i++)
                    orders[i] = Order.parseUnit(orderStrings[i]);

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