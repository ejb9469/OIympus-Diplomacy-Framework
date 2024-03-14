package testcases;

import adjudication.*;
import exceptions.BadOrderException;
import exceptions.DiplomacyException;
import org.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class BackstabbrNetTestCaseBuilder extends TestCaseBuilder {

    public static final String[] VALID_HOSTS = new String[]{"https://www.backstabbr.com/game/", "http://www.backstabbr.com/game/"};

    @Override
    public void build(String source) throws BadOrderException {

        boolean valid = false;
        for (String host : VALID_HOSTS) {
            if (source.startsWith(host)) {
                valid = true;
                break;
            }
        }
        if (!valid)
            throw new IllegalArgumentException("Invalid URL specified.");

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
            Map<String, String> unitTypeMapStr = (HashMap<String, String>) obj;
            for (String unitStr : unitTypeMapStr.keySet())
                unitTypeMap.put(unitStr, unitTypeMapStr.get(unitStr));
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
        String name = "Backstabbr-Autobuilder " + urlContent.split("/")[2] + "_" + urlContent.split("/")[3];
        TestCase testCase = new TestCase(name, orders, expected, expectedRetreats);

        testCase.go();
        System.out.println(testCase);

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

    public static void main(String[] args) throws DiplomacyException {
        //new BackstabbrNetTestCaseBuilder().build("https://www.backstabbr.com/game/PL-137---Secret-End-Date/5195530129506304/1904/spring");
        new BackstabbrNetTestCaseBuilder().build("https://www.backstabbr.com/game/Hera/6332766085578752/1903/spring");
    }

}