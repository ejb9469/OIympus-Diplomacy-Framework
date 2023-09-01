package indexing;

import adjudication.Order;

import java.util.List;

public class Movetext {

    public List<Order[]> ordersList;
    private MovetextTranslator moveTextTranslator;

    public Movetext(String moveText, MovetextTranslator moveTextTranslator) {
        this.moveTextTranslator = moveTextTranslator;
        this.ordersList = moveTextTranslator.parseStringToOrders(moveText);
    }

    public Movetext(String moveText) {
        this(moveText, new PGNDipTranslation());
    }

    public Movetext(Movetext moveText) {
        this.moveTextTranslator = moveText.moveTextTranslator;
        this.ordersList = moveText.ordersList;
    }

    public void append(String moveText) {

    }

    @Override
    public String toString() {
        return moveTextTranslator.parseOrdersToString(this.ordersList);
    }

}