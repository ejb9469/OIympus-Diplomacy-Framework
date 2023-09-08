package adjudication;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum PROVINCE {

    // TODO: Coast support

    Boh("Bohemia", false, false),
    Bud("Budapest", false, true),
    Gal("Galicia", false, false),
    Tri("Trieste", false, true),
    Tyr("Tyrolia", true, false),
    Vie("Vienna", false, true),
    Cly("Clyde", true, false),
    Edi("Edinburgh", true, true),
    Lvp("Liverpool", true, true),
    Lon("London", true, true),
    Wal("Wales", true, false),
    Yor("Yorkshire", true, false),
    Bre("Brest", true, true),
    Bur("Burgundy", false, false),
    Gas("Gascony", true, false),
    Mar("Marseilles", true, true),
    Par("Paris", false, true),
    Pic("Picardy", true, false),
    Ber("Berlin", true, true),
    Kie("Kiel", true, true),
    Mun("Munich", false, true),
    Pru("Prussia", true, false),
    Ruh("Ruhr", false, false),
    Sil("Silesia", false, false),
    Apu("Apulia", true, false),
    Nap("Naples", true, true),
    Pie("Piedmont", true, false),
    Rom("Rome", true, true),
    Tus("Tuscany", true, false),
    Ven("Venice", true, true),
    Lvn("Livonia", true, false),
    Mos("Moscow", false, true),
    Sev("Sevastopol", true, true),
    Stp("St. Petersburg", true, true, false),
    Ukr("Ukraine", false, false),
    War("Warsaw", false, true),
    Ank("Ankara", true, true),
    Arm("Armenia", true, false),
    Con("Constantinople", true, true),
    Smy("Smyrna", true, true),
    Syr("Syria", true, false),
    Alb("Albania", true, false),
    Bel("Belgium", true, true),
    Bul("Bulgaria", true, true, false),
    Fin("Finland", true, false),
    Gre("Greece", true, true),
    Hol("Holland", true, true),
    Nwy("Norway", true, true),
    Naf("North Africa", true, false),
    Por("Portugal", true, true),
    Rum("Rumania", true, true),
    Ser("Serbia", false, true),
    Spa("Spain", true, true, false),
    Swe("Sweden", true, true),
    Tun("Tunis", true, true),
    Den("Denmark", true, true),
    ADR("Adriatic Sea", true),
    AEG("Aegean Sea", true),
    BAL("Baltic Sea", true),
    BAR("Barents Sea", true),
    BLA("Black Sea", true),
    EAS("Eastern Mediterranean", true),
    ENG("English Channel", true),
    BOT("Gulf of Bothnia", true),
    LYO("Gulf of Lyon", true),
    HEL("Heligoland Bight", true),
    ION("Ionian Sea", true),
    IRI("Irish Sea", true),
    MAO("Mid-Atlantic Ocean", true),
    NAO("North Atlantic Ocean", true),
    NTH("North Sea", true),
    NWG("Norwegian Sea", true),
    SKA("Skagerrak", true),
    TYS("Tyrrhenian Sea", true),
    WES("Western Mediterranean", true),
    StpNC("St. Petersburg (north coast)", true, true, true),
    StpSC("St. Petersburg (south coast)", true, true, true),
    SpaNC("Spain (north coast)", true, true, true),
    SpaSC("Spain (south coast)", true, true, true),
    BulEC("Bulgaria (east coast)", true, true, true),
    BulSC("Bulgaria (south coast)", true, true, true),
    Swi("Switzerland", false, false);

    private final String name;
    private final short waterAccess;
    private final boolean supplyCenter;
    private final boolean splitCoast;

    private static Map<PROVINCE, PROVINCE[]> adjacencyMap;

    private static final Map<String, PROVINCE> validNames = new HashMap<>();  //{"Bohemia", "Budapest"};  // TODO

    public static Map<String, String> generateFullNamesToAbbreviationsMap() {
        Map<String, String> map = new HashMap<>();
        for (PROVINCE province : PROVINCE.values()) {
            map.put(province.getName(), province.name());
        }
        return map;
    }

    private PROVINCE(String name, boolean coastal, boolean supplyCenter, boolean splitCoast) {
        populateAdjacencyMap();
        this.name = name;
        this.supplyCenter = supplyCenter;
        if (!coastal)
            this.waterAccess = 0;
        else
            this.waterAccess = 1;
        this.splitCoast = splitCoast;
    }

    private PROVINCE(String name, boolean coastal, boolean supplyCenter) {  // Assumed land province
        populateAdjacencyMap();
        this.name = name;
        this.supplyCenter = supplyCenter;
        if (!coastal)
            this.waterAccess = 0;
        else
            this.waterAccess = 1;
        splitCoast = false;
    }

    private PROVINCE(String name, boolean water) {  // Assumes no supply centers in water, DETERMINISTIC
        populateAdjacencyMap();
        this.name = name;
        this.waterAccess = 2;
        this.supplyCenter = false;
        splitCoast = false;
    }

    boolean isAdjacentTo(PROVINCE province) {
        if (province == PROVINCE.Swi || this == PROVINCE.Swi) return false;
        return (Arrays.asList(adjacencyMap.get(this)).contains(province));
    }

    boolean isCoastal() {
        return (waterAccess == 1);
    }

    boolean isWater() {return (waterAccess == 2); }

    boolean isSupplyCenter() {
        return supplyCenter;
    }

    public boolean hasSplitCoast() {
        return splitCoast;
    }

    String getName() {
        return name;
    }

    boolean correctName(String n) {
        return name.equals(n);
    }

    static boolean isValidName(String n) {
        return validNames.containsKey(n);
    }

    static PROVINCE convertToEnum(String n) {
        return validNames.get(n);
    }

    // No setter tbd

    private static void populateAdjacencyMap() {

        adjacencyMap = new HashMap<>();

        adjacencyMap.put(PROVINCE.Boh, new PROVINCE[]{PROVINCE.Mun, PROVINCE.Sil, PROVINCE.Gal, PROVINCE.Tyr, PROVINCE.Vie});
        adjacencyMap.put(PROVINCE.Bud, new PROVINCE[]{PROVINCE.Vie, PROVINCE.Gal, PROVINCE.Tri, PROVINCE.Ser, PROVINCE.Rum});
        adjacencyMap.put(PROVINCE.Gal, new PROVINCE[]{PROVINCE.Sil, PROVINCE.Boh, PROVINCE.Vie, PROVINCE.Bud, PROVINCE.Rum, PROVINCE.Ukr, PROVINCE.War});
        adjacencyMap.put(PROVINCE.Tri, new PROVINCE[]{PROVINCE.Tyr, PROVINCE.Ven, PROVINCE.ADR, PROVINCE.Alb, PROVINCE.Ser, PROVINCE.Bud, PROVINCE.Vie});
        adjacencyMap.put(PROVINCE.Tyr, new PROVINCE[]{PROVINCE.Mun, PROVINCE.Boh, PROVINCE.Vie, PROVINCE.Tri, PROVINCE.Ven, PROVINCE.Pie});
        adjacencyMap.put(PROVINCE.Vie, new PROVINCE[]{PROVINCE.Tyr, PROVINCE.Boh, PROVINCE.Gal, PROVINCE.Bud, PROVINCE.Tri});
        adjacencyMap.put(PROVINCE.Cly, new PROVINCE[]{PROVINCE.NAO, PROVINCE.NWG, PROVINCE.Edi, PROVINCE.Lvp});
        adjacencyMap.put(PROVINCE.Edi, new PROVINCE[]{PROVINCE.Cly, PROVINCE.NWG, PROVINCE.NTH, PROVINCE.Yor, PROVINCE.Lvp});
        adjacencyMap.put(PROVINCE.Lvp, new PROVINCE[]{PROVINCE.NAO, PROVINCE.Cly, PROVINCE.Edi, PROVINCE.Yor, PROVINCE.Wal, PROVINCE.IRI});
        adjacencyMap.put(PROVINCE.Lon, new PROVINCE[]{PROVINCE.Wal, PROVINCE.Yor, PROVINCE.NTH, PROVINCE.ENG});
        adjacencyMap.put(PROVINCE.Wal, new PROVINCE[]{PROVINCE.Lvp, PROVINCE.Yor, PROVINCE.Lon, PROVINCE.ENG, PROVINCE.IRI});
        adjacencyMap.put(PROVINCE.Yor, new PROVINCE[]{PROVINCE.Edi, PROVINCE.NTH, PROVINCE.Lon, PROVINCE.Wal, PROVINCE.Lvp});
        adjacencyMap.put(PROVINCE.Bre, new PROVINCE[]{PROVINCE.ENG, PROVINCE.Pic, PROVINCE.Par, PROVINCE.Gas, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.Bur, new PROVINCE[]{PROVINCE.Bel, PROVINCE.Ruh, PROVINCE.Mun, PROVINCE.Mar, PROVINCE.Gas, PROVINCE.Par, PROVINCE.Pic});
        adjacencyMap.put(PROVINCE.Gas, new PROVINCE[]{PROVINCE.Bre, PROVINCE.Par, PROVINCE.Bur, PROVINCE.Mar, PROVINCE.Spa, PROVINCE.SpaNC, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.Mar, new PROVINCE[]{PROVINCE.Bur, PROVINCE.Pie, PROVINCE.LYO, PROVINCE.Spa, PROVINCE.SpaSC, PROVINCE.Gas});
        adjacencyMap.put(PROVINCE.Par, new PROVINCE[]{PROVINCE.Bre, PROVINCE.Pic, PROVINCE.Bur, PROVINCE.Gas});
        adjacencyMap.put(PROVINCE.Pic, new PROVINCE[]{PROVINCE.ENG, PROVINCE.Bel, PROVINCE.Bur, PROVINCE.Par, PROVINCE.Bre});
        adjacencyMap.put(PROVINCE.Ber, new PROVINCE[]{PROVINCE.BAL, PROVINCE.Pru, PROVINCE.Sil, PROVINCE.Mun, PROVINCE.Kie});
        adjacencyMap.put(PROVINCE.Kie, new PROVINCE[]{PROVINCE.Den, PROVINCE.BAL, PROVINCE.Ber, PROVINCE.Mun, PROVINCE.Ruh, PROVINCE.Hol, PROVINCE.HEL});
        adjacencyMap.put(PROVINCE.Mun, new PROVINCE[]{PROVINCE.Kie, PROVINCE.Ber, PROVINCE.Sil, PROVINCE.Boh, PROVINCE.Tyr, PROVINCE.Bur, PROVINCE.Ruh});
        adjacencyMap.put(PROVINCE.Pru, new PROVINCE[]{PROVINCE.BAL, PROVINCE.Lvn, PROVINCE.War, PROVINCE.Sil, PROVINCE.Ber});
        adjacencyMap.put(PROVINCE.Ruh, new PROVINCE[]{PROVINCE.Hol, PROVINCE.Kie, PROVINCE.Mun, PROVINCE.Bur, PROVINCE.Bel});
        adjacencyMap.put(PROVINCE.Sil, new PROVINCE[]{PROVINCE.Ber, PROVINCE.Pru, PROVINCE.War, PROVINCE.Gal, PROVINCE.Boh, PROVINCE.Mun});
        adjacencyMap.put(PROVINCE.Apu, new PROVINCE[]{PROVINCE.ADR, PROVINCE.ION, PROVINCE.Nap, PROVINCE.Rom, PROVINCE.Ven});
        adjacencyMap.put(PROVINCE.Nap, new PROVINCE[]{PROVINCE.Apu, PROVINCE.ION, PROVINCE.TYS, PROVINCE.Rom});
        adjacencyMap.put(PROVINCE.Pie, new PROVINCE[]{PROVINCE.Tyr, PROVINCE.Ven, PROVINCE.Tus, PROVINCE.LYO, PROVINCE.Mar});
        adjacencyMap.put(PROVINCE.Rom, new PROVINCE[]{PROVINCE.Ven, PROVINCE.Apu, PROVINCE.Nap, PROVINCE.TYS, PROVINCE.Tus});
        adjacencyMap.put(PROVINCE.Tus, new PROVINCE[]{PROVINCE.Ven, PROVINCE.Rom, PROVINCE.TYS, PROVINCE.LYO, PROVINCE.Pie});
        adjacencyMap.put(PROVINCE.Ven, new PROVINCE[]{PROVINCE.Tyr, PROVINCE.Tri, PROVINCE.ADR, PROVINCE.Apu, PROVINCE.Rom, PROVINCE.Tus, PROVINCE.Pie});
        adjacencyMap.put(PROVINCE.Lvn, new PROVINCE[]{PROVINCE.BOT, PROVINCE.Stp, PROVINCE.StpSC, PROVINCE.Mos, PROVINCE.War, PROVINCE.Pru, PROVINCE.BAL});
        adjacencyMap.put(PROVINCE.Mos, new PROVINCE[]{PROVINCE.Stp, PROVINCE.Sev, PROVINCE.Ukr, PROVINCE.War, PROVINCE.Lvn});
        adjacencyMap.put(PROVINCE.Sev, new PROVINCE[]{PROVINCE.Mos, PROVINCE.Arm, PROVINCE.BLA, PROVINCE.Rum, PROVINCE.Ukr});
        adjacencyMap.put(PROVINCE.Stp, new PROVINCE[]{PROVINCE.BAR, PROVINCE.Mos, PROVINCE.Lvn, PROVINCE.BOT, PROVINCE.Fin, PROVINCE.Nwy});
        adjacencyMap.put(PROVINCE.Ukr, new PROVINCE[]{PROVINCE.Mos, PROVINCE.Sev, PROVINCE.Rum, PROVINCE.Gal, PROVINCE.War});
        adjacencyMap.put(PROVINCE.War, new PROVINCE[]{PROVINCE.Lvn, PROVINCE.Mos, PROVINCE.Ukr, PROVINCE.Gal, PROVINCE.Sil, PROVINCE.Pru});
        adjacencyMap.put(PROVINCE.Ank, new PROVINCE[]{PROVINCE.BLA, PROVINCE.Arm, PROVINCE.Smy, PROVINCE.Con});
        adjacencyMap.put(PROVINCE.Arm, new PROVINCE[]{PROVINCE.Sev, PROVINCE.Syr, PROVINCE.Smy, PROVINCE.Ank, PROVINCE.BLA});
        adjacencyMap.put(PROVINCE.Con, new PROVINCE[]{PROVINCE.BLA, PROVINCE.Ank, PROVINCE.Smy, PROVINCE.AEG, PROVINCE.Bul, PROVINCE.BulEC, PROVINCE.BulSC});
        adjacencyMap.put(PROVINCE.Smy, new PROVINCE[]{PROVINCE.Ank, PROVINCE.Arm, PROVINCE.Syr, PROVINCE.EAS, PROVINCE.AEG, PROVINCE.Con});
        adjacencyMap.put(PROVINCE.Syr, new PROVINCE[]{PROVINCE.Arm, PROVINCE.EAS, PROVINCE.Smy});
        adjacencyMap.put(PROVINCE.Alb, new PROVINCE[]{PROVINCE.Tri, PROVINCE.Ser, PROVINCE.Gre, PROVINCE.ION, PROVINCE.ADR});
        adjacencyMap.put(PROVINCE.Bel, new PROVINCE[]{PROVINCE.NTH, PROVINCE.Hol, PROVINCE.Ruh, PROVINCE.Bur, PROVINCE.Pic, PROVINCE.ENG});
        adjacencyMap.put(PROVINCE.Bul, new PROVINCE[]{PROVINCE.Rum, PROVINCE.BLA, PROVINCE.Con, PROVINCE.AEG, PROVINCE.Gre, PROVINCE.Ser});
        adjacencyMap.put(PROVINCE.Fin, new PROVINCE[]{PROVINCE.Nwy, PROVINCE.Stp, PROVINCE.StpSC, PROVINCE.BOT, PROVINCE.Swe});
        adjacencyMap.put(PROVINCE.Gre, new PROVINCE[]{PROVINCE.Ser, PROVINCE.Bul, PROVINCE.BulSC, PROVINCE.AEG, PROVINCE.ION, PROVINCE.Alb});
        adjacencyMap.put(PROVINCE.Hol, new PROVINCE[]{PROVINCE.NTH, PROVINCE.HEL, PROVINCE.Kie, PROVINCE.Ruh, PROVINCE.Bel});
        adjacencyMap.put(PROVINCE.Nwy, new PROVINCE[]{PROVINCE.NWG, PROVINCE.BAR, PROVINCE.Stp, PROVINCE.StpNC, PROVINCE.Fin, PROVINCE.Swe, PROVINCE.SKA, PROVINCE.NTH});
        adjacencyMap.put(PROVINCE.Naf, new PROVINCE[]{PROVINCE.WES, PROVINCE.Tun, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.Por, new PROVINCE[]{PROVINCE.Spa, PROVINCE.SpaNC, PROVINCE.SpaSC, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.Rum, new PROVINCE[]{PROVINCE.Ukr, PROVINCE.Sev, PROVINCE.BLA, PROVINCE.Bul, PROVINCE.BulEC, PROVINCE.Ser, PROVINCE.Bud, PROVINCE.Gal});
        adjacencyMap.put(PROVINCE.Ser, new PROVINCE[]{PROVINCE.Bud, PROVINCE.Rum, PROVINCE.Bul, PROVINCE.Gre, PROVINCE.Alb, PROVINCE.Tri});
        adjacencyMap.put(PROVINCE.Spa, new PROVINCE[]{PROVINCE.Gas, PROVINCE.Mar, PROVINCE.LYO, PROVINCE.WES, PROVINCE.MAO, PROVINCE.Por});
        adjacencyMap.put(PROVINCE.Swe, new PROVINCE[]{PROVINCE.Nwy, PROVINCE.Fin, PROVINCE.BOT, PROVINCE.BAL, PROVINCE.Den, PROVINCE.SKA});
        adjacencyMap.put(PROVINCE.Tun, new PROVINCE[]{PROVINCE.TYS, PROVINCE.ION, PROVINCE.Naf, PROVINCE.WES});
        adjacencyMap.put(PROVINCE.Den, new PROVINCE[]{PROVINCE.SKA, PROVINCE.Swe, PROVINCE.BAL, PROVINCE.Kie, PROVINCE.HEL, PROVINCE.NTH});
        adjacencyMap.put(PROVINCE.ADR, new PROVINCE[]{PROVINCE.Tri, PROVINCE.Alb, PROVINCE.ION, PROVINCE.Apu, PROVINCE.Ven});
        adjacencyMap.put(PROVINCE.AEG, new PROVINCE[]{PROVINCE.Bul, PROVINCE.BulSC, PROVINCE.Con, PROVINCE.Smy, PROVINCE.EAS, PROVINCE.ION, PROVINCE.Gre});
        adjacencyMap.put(PROVINCE.BAL, new PROVINCE[]{PROVINCE.Swe, PROVINCE.BOT, PROVINCE.Lvn, PROVINCE.Pru, PROVINCE.Ber, PROVINCE.Kie, PROVINCE.Den});
        adjacencyMap.put(PROVINCE.BAR, new PROVINCE[]{PROVINCE.NWG, PROVINCE.Nwy, PROVINCE.Stp, PROVINCE.StpNC});
        adjacencyMap.put(PROVINCE.BLA, new PROVINCE[]{PROVINCE.Sev, PROVINCE.Arm, PROVINCE.Ank, PROVINCE.Con, PROVINCE.Bul, PROVINCE.BulEC, PROVINCE.Rum});
        adjacencyMap.put(PROVINCE.EAS, new PROVINCE[]{PROVINCE.Smy, PROVINCE.Syr, PROVINCE.ION, PROVINCE.AEG});
        adjacencyMap.put(PROVINCE.ENG, new PROVINCE[]{PROVINCE.Lon, PROVINCE.NTH, PROVINCE.Bel, PROVINCE.Pic, PROVINCE.Bre, PROVINCE.MAO, PROVINCE.IRI, PROVINCE.Wal});
        adjacencyMap.put(PROVINCE.BOT, new PROVINCE[]{PROVINCE.Fin, PROVINCE.Stp, PROVINCE.StpSC, PROVINCE.Lvn, PROVINCE.BAL, PROVINCE.Swe});
        adjacencyMap.put(PROVINCE.LYO, new PROVINCE[]{PROVINCE.Mar, PROVINCE.Pie, PROVINCE.Tus, PROVINCE.TYS, PROVINCE.WES, PROVINCE.Spa, PROVINCE.SpaSC});
        adjacencyMap.put(PROVINCE.HEL, new PROVINCE[]{PROVINCE.NTH, PROVINCE.Den, PROVINCE.Kie, PROVINCE.Hol});
        adjacencyMap.put(PROVINCE.ION, new PROVINCE[]{PROVINCE.ADR, PROVINCE.Alb, PROVINCE.Gre, PROVINCE.AEG, PROVINCE.EAS, PROVINCE.Tun, PROVINCE.TYS, PROVINCE.Nap, PROVINCE.Apu});
        adjacencyMap.put(PROVINCE.IRI, new PROVINCE[]{PROVINCE.NAO, PROVINCE.Lvp, PROVINCE.Wal, PROVINCE.ENG, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.MAO, new PROVINCE[]{PROVINCE.NAO, PROVINCE.IRI, PROVINCE.ENG, PROVINCE.Bre, PROVINCE.Gas, PROVINCE.Spa, PROVINCE.SpaNC, PROVINCE.SpaSC, PROVINCE.Por, PROVINCE.WES, PROVINCE.Naf});
        adjacencyMap.put(PROVINCE.NAO, new PROVINCE[]{PROVINCE.NWG, PROVINCE.Cly, PROVINCE.Lvp, PROVINCE.IRI, PROVINCE.MAO});
        adjacencyMap.put(PROVINCE.NTH, new PROVINCE[]{PROVINCE.NWG, PROVINCE.Nwy, PROVINCE.SKA, PROVINCE.Den, PROVINCE.HEL, PROVINCE.Hol, PROVINCE.Bel, PROVINCE.ENG, PROVINCE.Lon, PROVINCE.Yor, PROVINCE.Edi});
        adjacencyMap.put(PROVINCE.NWG, new PROVINCE[]{PROVINCE.BAR, PROVINCE.Nwy, PROVINCE.NTH, PROVINCE.Edi, PROVINCE.Cly, PROVINCE.NAO});
        adjacencyMap.put(PROVINCE.SKA, new PROVINCE[]{PROVINCE.Nwy, PROVINCE.Swe, PROVINCE.Den, PROVINCE.NTH});
        adjacencyMap.put(PROVINCE.TYS, new PROVINCE[]{PROVINCE.Tus, PROVINCE.Rom, PROVINCE.Nap, PROVINCE.ION, PROVINCE.Tun, PROVINCE.WES, PROVINCE.LYO});
        adjacencyMap.put(PROVINCE.WES, new PROVINCE[]{PROVINCE.LYO, PROVINCE.TYS, PROVINCE.Tun, PROVINCE.Naf, PROVINCE.Spa, PROVINCE.SpaSC});
        adjacencyMap.put(PROVINCE.StpNC, new PROVINCE[]{PROVINCE.BAR, PROVINCE.Nwy});
        adjacencyMap.put(PROVINCE.StpSC, new PROVINCE[]{PROVINCE.Fin, PROVINCE.Lvn, PROVINCE.BOT});
        adjacencyMap.put(PROVINCE.SpaNC, new PROVINCE[]{PROVINCE.MAO, PROVINCE.Gas, PROVINCE.Por});
        adjacencyMap.put(PROVINCE.SpaSC, new PROVINCE[]{PROVINCE.MAO, PROVINCE.Mar, PROVINCE.LYO, PROVINCE.WES, PROVINCE.Por});
        adjacencyMap.put(PROVINCE.BulEC, new PROVINCE[]{PROVINCE.Rum, PROVINCE.BLA, PROVINCE.Con});
        adjacencyMap.put(PROVINCE.BulSC, new PROVINCE[]{PROVINCE.Con, PROVINCE.AEG, PROVINCE.Gre});
        adjacencyMap.put(PROVINCE.Swi, new PROVINCE[]{});  // teehee

    }

}