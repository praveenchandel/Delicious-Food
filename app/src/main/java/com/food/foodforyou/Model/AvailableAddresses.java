package com.food.foodforyou.Model;

public class AvailableAddresses {
   private String address_name,last_time_of_ordering;
   private String standard_cost,standard_delivery_details;
   private String instant_delivery_available,instant_extra_cost,instant_delivery_details;

    public AvailableAddresses() {
    }

    public AvailableAddresses(String address_name, String last_time_of_ordering, String standard_cost, String standard_delivery_details, String instant_delivery_available, String instant_extra_cost, String instant_delivery_details) {
        this.address_name = address_name;
        this.last_time_of_ordering = last_time_of_ordering;
        this.standard_cost = standard_cost;
        this.standard_delivery_details = standard_delivery_details;
        this.instant_delivery_available = instant_delivery_available;
        this.instant_extra_cost = instant_extra_cost;
        this.instant_delivery_details = instant_delivery_details;
    }


    public String getAddress_name() {
        return address_name;
    }

    public void setAddress_name(String address_name) {
        this.address_name = address_name;
    }

    public String getLast_time_of_ordering() {
        return last_time_of_ordering;
    }

    public void setLast_time_of_ordering(String last_time_of_ordering) {
        this.last_time_of_ordering = last_time_of_ordering;
    }

    public String getStandard_cost() {
        return standard_cost;
    }

    public void setStandard_cost(String standard_cost) {
        this.standard_cost = standard_cost;
    }

    public String getStandard_delivery_details() {
        return standard_delivery_details;
    }

    public void setStandard_delivery_details(String standard_delivery_details) {
        this.standard_delivery_details = standard_delivery_details;
    }

    public String getInstant_delivery_available() {
        return instant_delivery_available;
    }

    public void setInstant_delivery_available(String instant_delivery_available) {
        this.instant_delivery_available = instant_delivery_available;
    }

    public String getInstant_extra_cost() {
        return instant_extra_cost;
    }

    public void setInstant_extra_cost(String instant_extra_cost) {
        this.instant_extra_cost = instant_extra_cost;
    }

    public String getInstant_delivery_details() {
        return instant_delivery_details;
    }

    public void setInstant_delivery_details(String instant_delivery_details) {
        this.instant_delivery_details = instant_delivery_details;
    }
}
