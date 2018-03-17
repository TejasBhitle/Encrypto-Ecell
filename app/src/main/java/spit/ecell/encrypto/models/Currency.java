package spit.ecell.encrypto.models;

/**
 * Created by Samriddha on 17-03-2018.
 */

public class Currency {
    String id, symbol, name, desc;
    double currentValue, variation, factor, circulation;
    int owned;

    public Currency(String id, String symbol, String name, String desc, double currentValue, double variation, int owned, double factor, double circulation) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.desc = desc;
        this.currentValue = currentValue;
        this.variation = variation;
        this.owned = owned;
        this.factor = factor;
        this.circulation = circulation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getVariation() {
        return variation;
    }

    public void setVariation(double variation) {
        this.variation = variation;
    }

    public int getOwned() {
        return owned;
    }

    public void setOwned(int owned) {
        this.owned = owned;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getCirculation() {
        return circulation;
    }

    public void setCirculation(double circulation) {
        this.circulation = circulation;
    }
}
