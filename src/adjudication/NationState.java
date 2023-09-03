package adjudication;

import exceptions.UnitMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NationState {  // This is an unintentional pun

    private NATION nation;

    private boolean dead;

    private List<PROVINCE> supplyCenters;  // Is a list (not a set) to show relative order of acquisition
    private List<List<PROVINCE>> supplyHistory;
    private final List<PROVINCE> homeSupplyCenters;

    private List<Unit> units;
    private List<List<Unit>> unitHistory;

    private boolean drawFlag = false;
    private boolean civilDisorder = false;
    private int drawNumberCondition = 1;  // By default, all players accept a solo only
    private Set<NATION> drawPartyCondition = null;  // Keep the null assignments in mind
    private Map<NATION, int[]> drawSCCondition = null;  // int[] in the format: { (less than / equal to / greater than), count) }

    public void updateSupplyCenters(List<PROVINCE> swappedCenters) {
        for (PROVINCE center : swappedCenters) {
            if (supplyCenters.contains(center)) {
                supplyCenters.remove(center);
            } else {
                supplyCenters.add(center);
            }
        }
        supplyHistory.add(supplyCenters);
        if (supplyCenters.size() == 0)
            kill();
    }

    public void updateDrawFlag() {  // Updates draw flag to reflect player's draw conditions
        if (drawNumberCondition == 1) {
            return;
        } else if (GameState.supplyCounts.size() <= drawNumberCondition && GameState.supplyCounts.size() > 1) {
            drawFlag = true;
            return;
        }
        if (drawPartyCondition != null) {
            if (drawPartyCondition.equals(GameState.supplyCounts.keySet())) {
                drawFlag = true;
            } else if (drawSCCondition != null) {
                boolean accept = true;
                for (NATION nation : drawSCCondition.keySet()) {
                    int[] nationSCCondition = drawSCCondition.get(nation);
                    if (nationSCCondition[0] == -1) {  // Less than or equal to
                        if (GameState.supplyCounts.get(nation) > nationSCCondition[1])
                            accept = false;
                    } else if (nationSCCondition[0] == 0) {  // Equal to
                        if (GameState.supplyCounts.get(nation) != nationSCCondition[1])
                            accept = false;
                    } else if (nationSCCondition[0] == 1) {  // Greater than or equal to
                        if (GameState.supplyCounts.get(nation) < nationSCCondition[1])
                            accept = false;
                    }
                }
                drawFlag = accept;
            }
        }
    }

    public boolean isDrawing() {
        return drawFlag;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isSurrendered() {
        return civilDisorder;
    }

    public NATION getNation() {
        return nation;
    }

    public int getSupplyCount() {
        return supplyCenters.size();
    }

    public List<List<PROVINCE>> getSupplyHistory() {
        return supplyHistory;
    }

    public List<PROVINCE> getSupplyCenters() {
        return supplyCenters;
    }

    public List<PROVINCE> getHomeSupplyCenters() {
        return homeSupplyCenters;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.unitHistory.add(units);
        this.units = units;
    }

    public List<List<Unit>> getUnitHistory() {
        return unitHistory;
    }

    public int getDrawNumberCondition() {
        return drawNumberCondition;
    }

    public Set<NATION> getDrawPartyCondition() {
        return drawPartyCondition;
    }

    public Map<NATION, int[]> getDrawSCCondition() {
        return drawSCCondition;
    }


    private void kill() {
        this.dead = true;
    }

    private void surrender() {
        this.civilDisorder = true;
        this.drawFlag = true;
    }

    public void setDrawFlag(boolean drawFlag) {
        this.drawFlag = drawFlag;
    }

    public void setDrawNumberCondition(int drawNumberCondition) {
        this.drawNumberCondition = drawNumberCondition;
    }

    public void setDrawPartyCondition(Set<NATION> drawPartyCondition) {
        this.drawPartyCondition = drawPartyCondition;
    }

    public void setDrawSCCondition(Map<NATION, int[]> drawSCCondition) {
        this.drawSCCondition = drawSCCondition;
    }

    public void updateOrder(Unit unit, Order order) throws UnitMismatchException {
        for (Unit u : units) {
            if (u.equals(unit))
                unit.updateActingOrder(order);
        }
    }

    public NationState(NATION nation) {
        this.nation = nation;
        units = new ArrayList<>();
        unitHistory = new ArrayList<>();
        homeSupplyCenters = new ArrayList<>();
        supplyCenters = new ArrayList<>();
        supplyHistory = new ArrayList<>();
        if (nation == NATION.ENGLAND) {
            homeSupplyCenters.add(PROVINCE.Lon);
            homeSupplyCenters.add(PROVINCE.Lvp);
            homeSupplyCenters.add(PROVINCE.Edi);
            units.add(new Unit(nation, PROVINCE.Lon, 1));
            units.add(new Unit(nation, PROVINCE.Lvp, 0));
            units.add(new Unit(nation, PROVINCE.Edi, 1));
        } else if (nation == NATION.FRANCE) {
            homeSupplyCenters.add(PROVINCE.Par);
            homeSupplyCenters.add(PROVINCE.Bre);
            homeSupplyCenters.add(PROVINCE.Mar);
            units.add(new Unit(nation, PROVINCE.Par, 0));
            units.add(new Unit(nation, PROVINCE.Bre, 1));
            units.add(new Unit(nation, PROVINCE.Mar, 0));
        } else if (nation == NATION.GERMANY) {
            homeSupplyCenters.add(PROVINCE.Ber);
            homeSupplyCenters.add(PROVINCE.Mun);
            homeSupplyCenters.add(PROVINCE.Kie);
            units.add(new Unit(nation, PROVINCE.Ber, 0));
            units.add(new Unit(nation, PROVINCE.Mun, 0));
            units.add(new Unit(nation, PROVINCE.Kie, 1));
        } else if (nation == NATION.ITALY) {
            homeSupplyCenters.add(PROVINCE.Rom);
            homeSupplyCenters.add(PROVINCE.Ven);
            homeSupplyCenters.add(PROVINCE.Nap);
            units.add(new Unit(nation, PROVINCE.Rom, 0));
            units.add(new Unit(nation, PROVINCE.Ven, 0));
            units.add(new Unit(nation, PROVINCE.Nap, 1));
        } else if (nation == NATION.AUSTRIA) {
            homeSupplyCenters.add(PROVINCE.Vie);
            homeSupplyCenters.add(PROVINCE.Bud);
            homeSupplyCenters.add(PROVINCE.Tri);
            units.add(new Unit(nation, PROVINCE.Vie, 0));
            units.add(new Unit(nation, PROVINCE.Bud, 0));
            units.add(new Unit(nation, PROVINCE.Tri,1));
        } else if (nation == NATION.RUSSIA) {
            homeSupplyCenters.add(PROVINCE.Mos);
            homeSupplyCenters.add(PROVINCE.Stp);
            homeSupplyCenters.add(PROVINCE.War);
            homeSupplyCenters.add(PROVINCE.Sev);
            units.add(new Unit(nation, PROVINCE.Mos, 0));
            units.add(new Unit(nation, PROVINCE.Stp, 1));  // TODO: Update to south coast
            units.add(new Unit(nation, PROVINCE.War, 0));
            units.add(new Unit(nation, PROVINCE.Sev, 0));
        } else if (nation == NATION.TURKEY) {
            homeSupplyCenters.add(PROVINCE.Con);
            homeSupplyCenters.add(PROVINCE.Ank);
            homeSupplyCenters.add(PROVINCE.Smy);
            units.add(new Unit(nation, PROVINCE.Con, 0));
            units.add(new Unit(nation, PROVINCE.Ank, 1));
            units.add(new Unit(nation, PROVINCE.Smy, 0));
        }
        supplyCenters.addAll(homeSupplyCenters);
        supplyHistory.add(supplyCenters);
        unitHistory.add(units);
    }

}