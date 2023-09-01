package indexing;

import adjudication.Order;

import java.util.List;

public interface MovetextTranslator {

    public List<Order[]> parseStringToOrders(String movetext);
    public String parseOrdersToString(List<Order[]> ordersList);
    public String getTranslatorName();

}