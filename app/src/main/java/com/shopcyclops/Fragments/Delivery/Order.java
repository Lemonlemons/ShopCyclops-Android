package com.shopcyclops.Fragments.Delivery;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Andrew on 10/10/2015.
 */
public class Order implements Comparable<Order> {
    private int id;
    private float lat;
    private float lng;
    private int viewer_id;
    private int stream_id;
    private float taxrate;
    private int pricebeforetax;
    private int pricebeforefees;
    private int totalprice;
    private String cardcode;
    private boolean is_delivered;
    private LatLng waypoint;
    private List<LatLng> leg;
    private String stream_title;
    private int totalQuantity;

    public Order(LatLng waypoint, List<LatLng> leg, int viewer_id, boolean is_delivered)
    {
        this.waypoint = waypoint;
        this.leg = leg;
        this.viewer_id = viewer_id;
        this.is_delivered = is_delivered;
    }

    public Order()
    {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LatLng getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(LatLng waypoint) {
        this.waypoint = waypoint;
    }

    public List<LatLng> getLeg() {
        return leg;
    }

    public void setLeg(List<LatLng> leg) {
        this.leg = leg;
    }

    public boolean isIs_delivered() {
        return is_delivered;
    }

    public void setIs_delivered(boolean is_delivered) {
        this.is_delivered = is_delivered;
    }

    public String getCardcode() {
        return cardcode;
    }

    public void setCardcode(String cardcode) {
        this.cardcode = cardcode;
    }

    public int getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(int totalprice) {
        this.totalprice = totalprice;
    }

    public int getPricebeforefees() {
        return pricebeforefees;
    }

    public void setPricebeforefees(int pricebeforefees) {
        this.pricebeforefees = pricebeforefees;
    }

    public int getPricebeforetax() {
        return pricebeforetax;
    }

    public void setPricebeforetax(int pricebeforetax) {
        this.pricebeforetax = pricebeforetax;
    }

    public float getTaxrate() {
        return taxrate;
    }

    public void setTaxrate(float taxrate) {
        this.taxrate = taxrate;
    }

    public int getViewer_id() {
        return viewer_id;
    }

    public void setViewer_id(int viewer_id) {
        this.viewer_id = viewer_id;
    }

    public int getStream_id() {
        return stream_id;
    }

    public void setStream_id(int stream_id) {
        this.stream_id = stream_id;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public String getStream_title() {
        return stream_title;
    }

    public void setStream_title(String stream_title) {
        this.stream_title = stream_title;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @Override
    public int compareTo(Order order) {
        return 0;
    }
}
