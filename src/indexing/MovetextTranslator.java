package indexing;

import adjudication.Order;
import exceptions.BadOrderException;

import java.util.List;

public interface MovetextTranslator {

    public List<Order[]> parseStringToOrders(String movetext) throws BadOrderException;
    public String parseOrdersToString(List<Order[]> ordersList);
    public String getTranslatorName();

}