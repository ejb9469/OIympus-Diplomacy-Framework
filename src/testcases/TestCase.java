package testcases;

import adjudication.Adjudicator;
import adjudication.Order;
import adjudication.Unit;

import java.util.*;

/**
 * A collection of Order objects as input to test.
 * Contains expected output in the form of Unit positions + retreats
 */
public class TestCase {

    private final Collection<Order> orders;
    private final Collection<Unit> expected;
    private final Collection<Unit> expectedUnitsWithRetreats;
    private final String name;

    private Map<Unit, Boolean> actual = null;

    private boolean hasRun = false;

    // -2 => Not run, -1 => Runtime error, 0+ => # of successful orders
    private int successFlag = -2;

    public TestCase(String name, Collection<Order> orders, Collection<Unit> expected, Collection<Unit> retreats) {
        this.name = name;
        this.orders = orders;
        this.expected = expected;
        this.expectedUnitsWithRetreats = retreats;
    }

    // Only adjudicates once!
    public int go() {

        if (this.hasRun) return this.successFlag;

        Adjudicator adjudicator = new Adjudicator(new ArrayList<>(orders));
        Thread thread = new Thread(adjudicator);
        long then = new Date().getTime();
        thread.start();
        long now = new Date().getTime();
        if (now - then >= 5000) {
            thread.stop();
            this.successFlag = -1;  // runtime error
            this.hasRun = true;
            return this.successFlag;
        }

        // IF NO RUNTIME ERRORS...
        List<Order> resultOrders = adjudicator.getOrders();
        Map<Unit, Boolean> actual = new HashMap<>();
        int successFlag = 0;
        List<Unit> parsedUnits = new ArrayList<>();
        for (Order order : resultOrders) {
            boolean unique = !parsedUnits.contains(order.parentUnit);  // Consider a duplicate a failure due to 1UPT rule
            boolean badRetreat = expectedUnitsWithRetreats.contains(order.parentUnit) && !order.parentUnit.testCaseRetreat;
            boolean success = (expected.contains(order.parentUnit) && unique && !badRetreat);
            if (success)
                successFlag++;
            if (unique)
                actual.put(order.parentUnit, success);
            parsedUnits.add(order.parentUnit);
        }
        this.actual = actual;
        this.successFlag = successFlag;
        this.hasRun = true;
        return successFlag;

    }

    public boolean hasRun() {
        return hasRun;
    }

    public int getSuccessFlag() {
        return successFlag;
    }

    public Map<Unit, Boolean> getActual() {
        return actual;
    }

    @Override
    public String toString() {
        String statusStr;
        if (successFlag == -2) statusStr = "NOT RUN";
        else if (successFlag == -1) statusStr = "RUNTIME ERROR";
        else statusStr = "(" + successFlag + "/" + orders.size() + ")";
        return name + " :: " + statusStr;
    }

}