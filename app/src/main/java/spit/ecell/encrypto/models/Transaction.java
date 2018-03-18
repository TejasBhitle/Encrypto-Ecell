package spit.ecell.encrypto.models;

import java.util.Date;

/**
 * Created by tejas on 17/3/18.
 */

public class Transaction {
    private String name;
    private Date timeStamp;
    private double value, quantity;
    private boolean isBought;

    public Transaction(String name, double value, double quantity, boolean isBought, Date timeStamp) {
        this.name = name;
        this.value = value;
        this.quantity = quantity;
        this.isBought = isBought;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isBought() {
        return isBought;
    }

    public void setBought(boolean bought) {
        isBought = bought;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
