package com.bla.models;

public class CollectionOfInterest {
  private String name;
  private double floorPrice;
  private double oneDayChange;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getFloorPrice() {
    return floorPrice;
  }

  public void setFloorPrice(double floorPrice) {
    this.floorPrice = floorPrice;
  }

  public double getOneDayChange() {
    return oneDayChange;
  }

  public void setOneDayChange(double oneDayChange) {
    this.oneDayChange = oneDayChange;
  }

  @Override
  public String toString() {
    return this.name + ": floor: [" + this.floorPrice + "], change: [" + this.oneDayChange + "]";
  }
}
