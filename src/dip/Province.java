package dip;

import java.util.HashMap;
import java.util.Map;

public enum Province {

    // TODO: Coast support
    // TODO: Supply center border chart

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
    Stp("St. Petersburg", true, true),
    Ukr("Ukraine", false, false),
    War("Warsaw", false, true),
    Ank("Ankara", true, true),
    Con("Constantinople", true, true),
    Smy("Smyrna", true, true),
    Syr("Syria", true, false),
    Alb("Albania", true, false),
    Bel("Belgium", true, true),
    Bul("Bulgaria", true, true),
    Fin("Finland", true, false),
    Gre("Greece", true, true),
    Hol("Holland", true, true),
    Nwy("Norway", true, true),
    Naf("North Africa", true, false),
    Por("Portugal", true, true),
    Rum("Rumania", true, true),
    Ser("Serbia", false, true),
    Spa("Spain", true, true),
    Swe("Sweden", true, true),
    Tun("Tunis", true, true),
    Den("Denmark", true, true),
    ADR("Adriatic Sea", true),
    AEG("Aegean Sea", true),
    BAL("Baltic Sea", true),
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
    SKA("Skagerrak", true),  // TODO - Spelling
    TYR("Tyrrhenian Sea", true),
    WES("Western Mediterranean", true);

    private final String name;
    private final short waterAccess;
    private final boolean supplyCenter;
    private static final Map<String, Province> validNames = new HashMap<>();  //{"Bohemia", "Budapest"};  // TODO

    private Province(String name, boolean water, boolean coastal, boolean supplyCenter) {
        this.name = name;
        this.supplyCenter = supplyCenter;
        if (!coastal && !water) {
            this.waterAccess = 0;
        } else if (coastal && !water) {
            this.waterAccess = 1;
        } else {
            this.waterAccess = 2;
        }
    }

    private Province(String name, boolean coastal, boolean supplyCenter) {  // Assumed land province
        this.name = name;
        this.supplyCenter = supplyCenter;
        if (!coastal)
            this.waterAccess = 0;
        else
            this.waterAccess = 1;
    }

    private Province(String name, boolean water) {  // Assumes no supply centers in water, DETERMINISTIC
        this.name = name;
        this.waterAccess = 2;
        this.supplyCenter = false;
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

    static Province convertToEnum(String n) {
        return validNames.get(n);
    }

    // No setter tbd

}