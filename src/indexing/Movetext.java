package indexing;

import adjudication.Order;
import exceptions.BadOrderException;

import java.util.ArrayList;
import java.util.List;

public class Movetext {

    public List<Order[]> ordersList;
    private MovetextTranslator moveTextTranslator;

    public Movetext(String moveText, MovetextTranslator moveTextTranslator) {
        this.moveTextTranslator = moveTextTranslator;
        try {
            this.ordersList = moveTextTranslator.parseStringToOrders(moveText);
        } catch (BadOrderException ex) {
            ex.printStackTrace();
            this.ordersList = new ArrayList<>();
        }
    }

    public Movetext(String moveText) {
        this(moveText, new PGNDipTranslation());
    }

    public Movetext(Movetext moveText) {
        this.moveTextTranslator = moveText.moveTextTranslator;
        this.ordersList = moveText.ordersList;
    }

    public void append(String moveText) throws BadOrderException {
        ordersList.addAll(moveTextTranslator.parseStringToOrders(moveText));
    }

    @Override
    public String toString() {
        return moveTextTranslator.parseOrdersToString(this.ordersList);
    }

}