package com.laptrinhjavaweb.dto;

public class BuildingSearchingDto {
    private String name;
    private Integer buildingAreaFrom;
    private Integer buildingAreaTo;
    private Integer districtId;
    private Integer wardId;
    private String street;
    private String managerName;
    private String managerPhone;
    private Integer[] staffArray;
    private Integer rentAreaFrom;
    private Integer rentAreaTo;
    private Integer rentalCostFrom;
    private Integer rentalCostTo;
    private Integer numberOfBasement;
    private String direction;
    private String level;
    private String[] typeArray;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBuildingAreaFrom() {
        return buildingAreaFrom;
    }

    public void setBuildingAreaFrom(Integer buildingAreaFrom) {
        this.buildingAreaFrom = buildingAreaFrom;
    }

    public Integer getBuildingAreaTo() {
        return buildingAreaTo;
    }

    public void setBuildingAreaTo(Integer buildingAreaTo) {
        this.buildingAreaTo = buildingAreaTo;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    public Integer getWardId() {
        return wardId;
    }

    public void setWardId(Integer wardId) {
        this.wardId = wardId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public void setManagerPhone(String managerPhone) {
        this.managerPhone = managerPhone;
    }

    public Integer[] getStaffArray() {
        return staffArray;
    }

    public void setStaffArray(Integer[] staffArray) {
        this.staffArray = staffArray;
    }

    public Integer getRentAreaFrom() {
        return rentAreaFrom;
    }

    public void setRentAreaFrom(Integer rentAreaFrom) {
        this.rentAreaFrom = rentAreaFrom;
    }

    public Integer getRentAreaTo() {
        return rentAreaTo;
    }

    public void setRentAreaTo(Integer rentAreaTo) {
        this.rentAreaTo = rentAreaTo;
    }

    public Integer getRentalCostFrom() {
        return rentalCostFrom;
    }

    public void setRentalCostFrom(Integer rentalCostFrom) {
        this.rentalCostFrom = rentalCostFrom;
    }

    public Integer getRentalCostTo() {
        return rentalCostTo;
    }

    public void setRentalCostTo(Integer rentalCostTo) {
        this.rentalCostTo = rentalCostTo;
    }

    public Integer getNumberOfBasement() {
        return numberOfBasement;
    }

    public void setNumberOfBasement(Integer numberOfBasement) {
        this.numberOfBasement = numberOfBasement;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String[] getTypeArray() {
        return typeArray;
    }

    public void setTypeArray(String[] typeArray) {
        this.typeArray = typeArray;
    }
}
