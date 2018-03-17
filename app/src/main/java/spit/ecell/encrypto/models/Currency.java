package spit.ecell.encrypto.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Samriddha on 17-03-2018.
 */

public class Currency implements Parcelable {
    public static final Creator<Currency> CREATOR = new Creator<Currency>() {
        @Override
        public Currency createFromParcel(Parcel in) {
            return new Currency(in);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };
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

    protected Currency(Parcel in) {
        id = in.readString();
        symbol = in.readString();
        name = in.readString();
        desc = in.readString();
        currentValue = in.readDouble();
        variation = in.readDouble();
        factor = in.readDouble();
        circulation = in.readDouble();
        owned = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(symbol);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeDouble(currentValue);
        dest.writeDouble(variation);
        dest.writeInt(owned);
        dest.writeDouble(factor);
        dest.writeDouble(circulation);
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

    @Override
    public int describeContents() {
        return 0;
    }

}
