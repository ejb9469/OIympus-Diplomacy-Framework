package adjudication;

import adjudication.exceptions.BadOrderException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestCaseBuilder {

    private static List<TestCase> testCases = new ArrayList<>();
    private static final Scanner prompt = new Scanner(System.in);

    public static void main(String[] args) throws IOException, BadOrderException, IllegalArgumentException {
        // Grab list of test case files
        File testCasesFolder = new File("src/dip/testcases/run");
        File[] testCaseFiles = testCasesFolder.listFiles();
        if (testCaseFiles == null) throw new IOException();
        if (testCaseFiles.length == 0) return;
        // For each test case file,
        for (File testCaseFile : testCaseFiles) {
            Collection<Order> orders = new HashSet<>();
            Collection<Unit> expected = new HashSet<>();
            Collection<Unit> expectedUnitsWithRetreats = new HashSet<>();
            boolean abbreviated = !testCaseFile.getName().startsWith("pythongen_");
            Scanner sc = new Scanner(testCaseFile);
            Scanner sc2 = new Scanner(testCaseFile);
            StringBuilder fullContents = new StringBuilder();
            while (sc2.hasNextLine())
                fullContents.append(sc2.nextLine()).append("\n");
            System.out.println("\n" + fullContents + "\n");
            // For each order
            while (sc.hasNextLine()) {
                String orderText = sc.nextLine();
                if (orderText.isBlank()) continue;
                if (orderText.contains(".ec") || orderText.contains(".nc") || orderText.contains(".sc")) break;  // TODO: Ignore coasts for now
                if (!abbreviated) {
                    Map<String, String> fullNamesToAbbrsMap = Province.generateFullNamesToAbbreviationsMap();
                    for (String fullName : fullNamesToAbbrsMap.keySet()) {
                        if (orderText.contains(fullName)) {
                            orderText = orderText.replaceAll(fullName, fullNamesToAbbrsMap.get(fullName));
                        }
                    }
                }
                // Create a new order via Order.parseUnit() and add it to Order set
                Order order = Order.parseUnit(orderText);
                if (orders.contains(order)) continue;  // Do not allow duplicates
                orders.add(order);
                // Prompt user for expected unit placement + retreat y/n
                System.out.print("Where should " + order.parentUnit.toString() + " be?: ");
                String provinceStr = prompt.next();
                System.out.print("Should it have a retreat? (y/n): ");
                String retreatStr = prompt.next();
                boolean retreat = retreatStr.strip().equalsIgnoreCase("y");
                order.parentUnit.testCaseRetreat = retreat;
                if (retreat) expectedUnitsWithRetreats.add(order.parentUnit);
                Province province = Province.valueOf(provinceStr.strip());
                Unit expectedUnit = new Unit(order.parentUnit.getParentNation(), province, order.parentUnit.getUnitType());
                expected.add(expectedUnit);
            }
            sc.close();
            // Pipe Order set and 'expected' into new TestCase, and add it to `testCases`.
            TestCase testCase = new TestCase(testCaseFile.getName(), orders, expected, expectedUnitsWithRetreats);
            testCases.add(testCase);
        }
        prompt.close();
        for (TestCase testCase : testCases) {
            testCase.go();
            System.out.println(testCase);
        }
    }

}