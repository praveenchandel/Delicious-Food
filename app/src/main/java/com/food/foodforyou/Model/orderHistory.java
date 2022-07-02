package com.food.foodforyou.Model;

public class orderHistory {

    private String year,month,date,nodeID,name,amount;

    public orderHistory() {
    }

    public orderHistory(String year, String month, String date, String nodeID, String name, String amount) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.nodeID = nodeID;
        this.name = name;
        this.amount = amount;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
