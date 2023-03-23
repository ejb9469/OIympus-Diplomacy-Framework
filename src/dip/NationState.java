package dip;

import dip.exceptions.UnitMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NationState {  // This is an unintentional pun

    private Nation nation;

    private boolean dead;

    private List<Province> supplyCenters;  // Is a list (not a set) to show relative order of acquisition
    private List<List<Province>> supplyHistory;
    private final List<Province> homeSupplyCenters;

    private List<Unit> units;
    private List<List<Unit>> unitHistory;

    private boolean drawFlag = false;
    private boolean civilDisorder = false;
    private int drawNumberCondition = 1;  // By default, all players accept a solo only
    private Set<Nation> drawPartyCondition = null;  // Keep the null assignments in mind
    private Map<Nation, int[]> drawSCCondition = null;  // int[] in the format: { (less than / equal to / greater than), count) }

    public void updateSupplyCenters(List<Province> swappedCenters) {
        for (Province center : swappedCenters) {
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
                for (Nation nation : drawSCCondition.keySet()) {
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

    public Nation getNation() {
        return nation;
    }

    public int getSupplyCount() {
        return supplyCenters.size();
    }

    public List<List<Province>> getSupplyHistory() {
        return supplyHistory;
    }

    public List<Province> getSupplyCenters() {
        return supplyCenters;
    }

    public List<Province> getHomeSupplyCenters() {
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

    public Set<Nation> getDrawPartyCondition() {
        return drawPartyCondition;
    }

    public Map<Nation, int[]> getDrawSCCondition() {
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

    public void setDrawPartyCondition(Set<Nation> drawPartyCondition) {
        this.drawPartyCondition = drawPartyCondition;
    }

    public void setDrawSCCondition(Map<Nation, int[]> drawSCCondition) {
        this.drawSCCondition = drawSCCondition;
    }

    public void updateOrder(Unit unit, Order order) throws UnitMismatchException {
        for (Unit u : units) {
            if (u.equals(unit))
                unit.updateActingOrder(order);
        }
    }

    public NationState(Nation nation) {
        this.nation = nation;
        units = new ArrayList<>();
        unitHistory = new ArrayList<>();
        homeSupplyCenters = new ArrayList<>();
        supplyCenters = new ArrayList<>();
        supplyHistory = new ArrayList<>();
        if (nation == Nation.ENGLAND) {
            homeSupplyCenters.add(Province.Lon);
            homeSupplyCenters.add(Province.Lvp);
            homeSupplyCenters.add(Province.Edi);
            units.add(new Unit(nation, Province.Lon, 1));
            units.add(new Unit(nation, Province.Lvp, 0));
            units.add(new Unit(nation, Province.Edi, 1));
        } else if (nation == Nation.FRANCE) {
            homeSupplyCenters.add(Province.Par);
            homeSupplyCenters.add(Province.Bre);
            homeSupplyCenters.add(Province.Mar);
            units.add(new Unit(nation, Province.Par, 0));
            units.add(new Unit(nation, Province.Bre, 1));
            units.add(new Unit(nation, Province.Mar, 0));
        } else if (nation == Nation.GERMANY) {
            homeSupplyCenters.add(Province.Ber);
            homeSupplyCenters.add(Province.Mun);
            homeSupplyCenters.add(Province.Kie);
            units.add(new Unit(nation, Province.Ber, 0));
            units.add(new Unit(nation, Province.Mun, 0));
            units.add(new Unit(nation, Province.Kie, 1));
        } else if (nation == Nation.ITALY) {
            homeSupplyCenters.add(Province.Rom);
            homeSupplyCenters.add(Province.Ven);
            homeSupplyCenters.add(Province.Nap);
            units.add(new Unit(nation, Province.Rom, 0));
            units.add(new Unit(nation, Province.Ven, 0));
            units.add(new Unit(nation, Province.Nap, 1));
        } else if (nation == Nation.AUSTRIA) {
            homeSupplyCenters.add(Province.Vie);
            homeSupplyCenters.add(Province.Bud);
            homeSupplyCenters.add(Province.Tri);
            units.add(new Unit(nation, Province.Vie, 0));
            units.add(new Unit(nation, Province.Bud, 0));
            units.add(new Unit(nation, Province.Tri,1));
        } else if (nation == Nation.RUSSIA) {
            homeSupplyCenters.add(Province.Mos);
            homeSupplyCenters.add(Province.Stp);
            homeSupplyCenters.add(Province.War);
            homeSupplyCenters.add(Province.Sev);
            units.add(new Unit(nation, Province.Mos, 0));
            units.add(new Unit(nation, Province.Stp, 1));  // TODO: Update to south coast
            units.add(new Unit(nation, Province.War, 0));
            units.add(new Unit(nation, Province.Sev, 0));
        } else if (nation == Nation.TURKEY) {
            homeSupplyCenters.add(Province.Con);
            homeSupplyCenters.add(Province.Ank);
            homeSupplyCenters.add(Province.Smy);
            units.add(new Unit(nation, Province.Con, 0));
            units.add(new Unit(nation, Province.Ank, 1));
            units.add(new Unit(nation, Province.Smy, 0));
        }
        supplyCenters.addAll(homeSupplyCenters);
        supplyHistory.add(supplyCenters);
        unitHistory.add(units);
    }

}