package testcases;

import adjudication.*;
import exceptions.BadOrderException;
import exceptions.BadURLException;
import exceptions.DiplomacyException;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BackstabbrNetTestCaseBuilder extends TestCaseBuilder {

    public static final boolean PROCESS_ENTIRE_GAME = true;

    public static final boolean MULTIPLE_URLS = false;

    public static final String[] URLS = new String[]{
            append1901Spring("https://www.backstabbr.com/game/PL-185---Anon--SecretEnd/5104611115794432"),
            append1901Spring("https://www.backstabbr.com/game/2024-SC-R3B1-Hakkinen/5068731283013632"),
            append1901Spring("https://www.backstabbr.com/game/Speedboat-Septet/5190055435304960"),
            append1901Spring("https://www.backstabbr.com/game/Diplostrats-Speedboat/5147499523604480"),
            append1901Spring("https://www.backstabbr.com/game/Nexus-Game-21/4681649772560384"),
    };

    public static final String URL = "https://www.backstabbr.com/game/Nexus-Game-21/4681649772560384/1908/fall";  // Used when MULTIPLE_URLS is false

    public static final String[] VALID_HOSTS = new String[]{
            "https://www.backstabbr.com/game/",
            "http://www.backstabbr.com/game/",
            "https://www.backstabbr.com/sandbox/",
            "http://www.backstabbr.com/sandbox/"
    };

    private static TestCase currentTestCase = null;

    @Override
    public void build(String source) throws BadOrderException, BadURLException {

        boolean valid = false;
        for (String host : VALID_HOSTS) {
            if (source.startsWith(host)) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new BadURLException();

        URL url;
        InputStream iStream;
        BufferedReader bReader;
        String line;

        StringBuilder jsonSubsection = new StringBuilder();
        try {

            url = new URL(source);
            iStream = url.openStream();  // throws IOException
            bReader = new BufferedReader(new InputStreamReader(iStream));

            boolean listening = false;
            while ((line = bReader.readLine()) != null) {
                if (line.strip().startsWith("// NEW JAVSCRIPT!") || line.strip().startsWith("// NEW JAVASCRIPT!")) {  // lol typo in the source
                    listening = true;
                    continue;
                }
                if (listening) {
                    if (!line.strip().startsWith("var"))
                        listening = false;
                    else
                        jsonSubsection.append(line.strip()).append("\n");
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println(jsonSubsection);

        String jsonOrdersSubsection = jsonSubsection.toString().split("var orders = ")[1].split("\n")[0].strip();
        JSONObject jsonOrdersMap = new JSONObject(jsonOrdersSubsection);
        Map<String, Object> ordersMap = jsonOrdersMap.toMap();

        Set<PROVINCE> provincesWithSuccessfulUnits = new HashSet<>();
        Set<PROVINCE> provincesWithRetreatingUnits = new HashSet<>();

        Map<NATION, Set<String[]>> nationOrdersData = new HashMap<>();
        for (String nationStr : ordersMap.keySet()) {

            NATION parentNation = NATION.valueOf(nationStr.toUpperCase());
            Map<String, HashMap<String, String>> orderMap = (Map<String, HashMap<String, String>>) ordersMap.get(nationStr);
            nationOrdersData.put(parentNation, new HashSet<>());
            for (String orderStr : orderMap.keySet()) {

                HashMap<String, String> orderInfo = orderMap.get(orderStr);
                //System.out.println(orderStr);
                //System.out.println(orderInfo);
                ORDER_TYPE orderType = null;
                PROVINCE prInitial = PROVINCE.valueOf(orderStr);
                PROVINCE pr1 = null;
                PROVINCE pr2 = null;

                boolean containsTo = false;
                for (String datumKey : orderInfo.keySet()) {

                    if (datumKey.equalsIgnoreCase("retreat")) {
                        provincesWithRetreatingUnits.add(prInitial);
                        continue;
                    }

                    String datumVal = orderInfo.get(datumKey);
                    if (datumKey.equalsIgnoreCase("type")) {
                        orderType = ORDER_TYPE.valueOf(datumVal);
                        if (orderType == ORDER_TYPE.HOLD) {
                            pr1 = prInitial;
                            pr2 = prInitial;
                        }
                    } else if (datumKey.equalsIgnoreCase("result")) {
                        if (datumVal.equalsIgnoreCase("succeeds"))
                            provincesWithSuccessfulUnits.add(prInitial);
                    } else if (datumKey.equalsIgnoreCase("from")) {
                        if (!containsTo) {
                            for (String datumKey2 : orderInfo.keySet()) {
                                if (!datumKey2.equalsIgnoreCase("to")) continue;
                                containsTo = true;
                                String provinceStr = fixCoastFormatting(orderInfo.get(datumKey).strip());
                                pr1 = PROVINCE.valueOf(provinceStr);
                                break;
                            }
                        }
                        String provinceStr = fixCoastFormatting(orderInfo.get(datumKey).strip());
                        pr2 = PROVINCE.valueOf(provinceStr);
                        if (!containsTo)
                            pr1 = PROVINCE.valueOf(provinceStr);
                    } else if (datumKey.equalsIgnoreCase("to")) {
                        String provinceStr = fixCoastFormatting(orderInfo.get(datumKey).strip());
                        pr2 = PROVINCE.valueOf(provinceStr);
                        boolean containsFrom = false;
                        for (String datumKey2 : orderInfo.keySet()) {
                            if (!datumKey2.equalsIgnoreCase("from")) continue;
                            containsFrom = true;
                            break;
                        }
                        if (!containsFrom)
                            pr1 = PROVINCE.valueOf(provinceStr);
                    }

                }

                if (orderType == null || pr1 == null || pr2 == null) {
                    System.err.println(orderType.toString() + pr1.toString() + pr2.toString());
                    throw new BadOrderException();
                }

                nationOrdersData.get(parentNation).add(new String[]{orderType.name(), prInitial.name(), pr1.name(), pr2.name()});

            }

        }

        String jsonUnitsSubsection = jsonSubsection.toString().split("var unitsByPlayer = ")[1].split("\n")[0].strip();
        JSONObject jsonUnitsMap = new JSONObject(jsonUnitsSubsection);
        Map<String, Object> unitsMap = jsonUnitsMap.toMap();
        Map<String, String> unitTypeMap = new HashMap<>();

        for (Object obj : unitsMap.values()) {
            Map<String, Object> unitTypeMapStr = (HashMap<String, Object>) obj;
            for (String unitStr : unitTypeMapStr.keySet()) {
                try {
                    unitTypeMap.put(unitStr, (String)unitTypeMapStr.get(unitStr));
                } catch (ClassCastException ex) {
                    // Coastal fleets contain a Map where the unit type String is for every other unit.
                    // This Map contains the fleet's coast and unit type.
                    // For some reason, the casting above combined with using the Java JSON Library means sometimes Strings are actually HashMaps.
                    // This disgusting mess of casting and exception handling is the best I could do. At least it works.
                    Map<String, String> coastTypeMojoMap = (HashMap<String, String>)unitTypeMapStr.get(unitStr);
                    String coastedUnitStr = fixCoastFormatting(unitStr + "." + coastTypeMojoMap.get("coast"));
                    // TODO: Utilize coasts
                    unitTypeMap.put(unitStr, coastTypeMojoMap.get("type"));
                }
            }
        }

        Set<Order> orders = new HashSet<>();
        for (NATION parentNation : nationOrdersData.keySet()) {
            Set<String[]> ordersInfo = nationOrdersData.get(parentNation);
            for (String[] orderInfo : ordersInfo) {
                ORDER_TYPE orderType = ORDER_TYPE.valueOf(orderInfo[0]);
                PROVINCE prInitial = PROVINCE.valueOf(orderInfo[1]);
                PROVINCE pr1 = PROVINCE.valueOf(orderInfo[2]);
                PROVINCE pr2 = PROVINCE.valueOf(orderInfo[3]);
                boolean unitTypeBool = unitTypeMap.get(orderInfo[1]).equalsIgnoreCase("A");
                int unitType;
                if (unitTypeBool) unitType = 0; else unitType = 1;
                Unit unit = new Unit(parentNation, prInitial, unitType);
                unit.testCaseRetreat = true;  // This field is not used in this class, but to avoid errors, set it to true. `FileTestCaseBuilder` uses it.
                orders.add(new Order(unit, orderType, pr1, pr2));
            }
        }

        Set<Unit> expected = new HashSet<>();

        for (Order order : orders)
            expected.add(order.parentUnit);

        for (PROVINCE province : provincesWithSuccessfulUnits) {
            Order successfulOrder = null;
            for (Order order : orders) {
                if (province == order.prInitial) {
                    successfulOrder = order;
                    break;
                }
            }
            if (successfulOrder.orderType == ORDER_TYPE.MOVE) {
                expected.remove(successfulOrder.parentUnit);
                Unit cloneUnit = new Unit(successfulOrder.parentUnit);
                cloneUnit.setPosition(successfulOrder.pr1);
                expected.add(cloneUnit);
            }
        }

        Set<Unit> expectedRetreats = new HashSet<>();

        for (PROVINCE province : provincesWithRetreatingUnits) {
            Order orderWithRetreat = null;
            for (Order order : orders) {
                if (province == order.prInitial) {
                    orderWithRetreat = order;
                    break;
                }
            }
            expectedRetreats.add(orderWithRetreat.parentUnit);
        }

        String urlContent = source.strip().split("//")[1];
        String name = "Backstabbr-Autobuilder__" + urlContent.split("/")[2] + "_" + urlContent.split("/")[3];
        TestCase testCase = new TestCase(name, orders, expected, expectedRetreats);

        testCase.go();
        System.out.println(testCase);
        currentTestCase = testCase;

    }

    /**
     * If a period is present, translates e.g. "Bul.sc" to "BulSC" for referencing the PROVINCE enum.
     * @param provinceStr Province name value.
     * @return Province name value in local PROVINCE enum format.
     */
    public static String fixCoastFormatting(String provinceStr) {
        if (provinceStr.contains(".")) {
            String[] provinceStrArr = provinceStr.split("\\.");
            provinceStr = provinceStrArr[0] + provinceStrArr[1].toUpperCase();
        }
        return provinceStr;
    }

    public static String getNextURL(String prevURL) throws BadURLException {

        // https://www.backstabbr.com/game/PL-185---Anon--SecretEnd/5104611115794432/1908/fall

        boolean valid = false;
        for (String host : VALID_HOSTS) {
            if (prevURL.startsWith(host)) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new BadURLException();

        String nextURLAttempt = null;
        if (prevURL.endsWith("spring"))
            nextURLAttempt = prevURL.split("spring")[0] + "fall";
        else if (prevURL.endsWith("fall")) {
            String base = prevURL.substring(0, prevURL.length() - 10);  // "winter", "/", and year (e.g. 1908)
            int year = Integer.parseInt(prevURL.substring(prevURL.length() - 9, prevURL.length() - 5));
            nextURLAttempt = String.format("%s/%d/spring", base, year+1);
        }

        if (nextURLAttempt == null)
            throw new BadURLException();

        try {
            URL url = new URL(nextURLAttempt);
            url.openConnection();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            System.out.println("Malformed URL");
            throw new BadURLException();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Bad URL");
            return null;
        }

        return nextURLAttempt;

    }

    public static String append1901Spring(String url) {
        if (url.endsWith("/"))
            return url + "1901/spring";
        else
            return url + "/1901/spring";
    }

    public static void main(String[] args) throws DiplomacyException {

        if (MULTIPLE_URLS) {

            Map<String, String> allResults = new TreeMap<>();

            for (String url : URLS) {

                if (PROCESS_ENTIRE_GAME) {

                    Map<String, String> results = new TreeMap<>();

                    String nextURL = url;
                    new BackstabbrNetTestCaseBuilder().build(nextURL);
                    results.put(nextURL, currentTestCase.toString());
                    while (true) {
                        try {
                            nextURL = getNextURL(nextURL);
                            if (nextURL == null)
                                break;
                            new BackstabbrNetTestCaseBuilder().build(nextURL);
                            if (currentTestCase.toString().endsWith("(0/0)"))
                                break;
                            results.put(nextURL, currentTestCase.toString());
                        } catch (Exception ex) {
                            break;
                        }
                    }

                    int numerSum = 0;
                    int denomSum = 0;
                    for (String resultsKey : results.keySet()) {
                        System.out.printf("%s :: %s\n", resultsKey, results.get(resultsKey));
                        numerSum += Integer.parseInt(results.get(resultsKey).split("\\(")[1].split("/")[0]);
                        denomSum += Integer.parseInt(results.get(resultsKey).split("\\(")[1].split("/")[1].split("\\)")[0]);
                    }
                    System.out.printf("\nTOTAL: %d/%d\n", numerSum, denomSum);
                    allResults.put(url, String.format("%d/%d", numerSum, denomSum));

                } else {
                    new BackstabbrNetTestCaseBuilder().build(url);
                }

            }

            int numerSum = 0;
            int denomSum = 0;
            for (String resultsKey : allResults.keySet()) {
                System.out.printf("%s :: %s\n", resultsKey, allResults.get(resultsKey));
                numerSum += Integer.parseInt(allResults.get(resultsKey).split("/")[0]);
                denomSum += Integer.parseInt(allResults.get(resultsKey).split("/")[1]);
            }
            System.out.println("\n=================================");
            System.out.printf("TOTAL: %d/%d\n", numerSum, denomSum);
            System.out.println("=================================");

        } else {
            new BackstabbrNetTestCaseBuilder().build(URL);
        }

    }

}