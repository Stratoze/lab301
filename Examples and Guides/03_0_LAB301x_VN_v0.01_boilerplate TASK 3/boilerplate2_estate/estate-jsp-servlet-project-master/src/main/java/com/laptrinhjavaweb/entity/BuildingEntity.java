package com.laptrinhjavaweb.entity;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Entity;
import com.laptrinhjavaweb.orm.annotation.Id;
import com.laptrinhjavaweb.orm.annotation.IdField;

import java.sql.Date;

@Entity(tableName = "building")
@IdField(name = "id")
public class BuildingEntity {
    @Id(autoIncrement = true)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "ward_id")
    private Integer wardId;

    @Column(name = "street")
    private String street;

    @Column(name = "structure")
    private String structure;

    @Column(name = "number_of_basement")
    private Integer numberOfBasement;

    @Column(name = "building_area")
    private Integer buildingArea;

    @Column(name = "direction")
    private String direction;

    @Column(name = "level")
    private String level;

    @Column(name = "area_description")
    private String areaDescription;

    @Column(name = "rental_cost")
    private Integer rentalCost;

    @Column(name = "cost_description")
    private String costDescription;

    @Column(name = "service_cost")
    private String serviceCost;

    @Column(name = "car_cost")
    private String carCost;

    @Column(name = "motorbike_cost")
    private String motorbikeCost;

    @Column(name = "overtime_cost")
    private String overtimeCost;

    @Column(name = "electricity_cost")
    private String electricityCost;

    @Column(name = "deposit")
    private String deposit;

    @Column(name = "payment")
    private String payment;

    @Column(name = "time_contract")
    private String timeContract;

    @Column(name = "time_decorator")
    private String timeDecorator;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_phone")
    private String managerPhone;

    @Column(name = "commission_cost")
    private String commissionCost;

    @Column(name = "note")
    private String note;

    @Column(name = "link")
    private String link;

    @Column(name = "location")
    private String location;

    @Column(name = "type")
    private String type;

    @Column(name = "image")
    private String image;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public Integer getNumberOfBasement() {
        return numberOfBasement;
    }

    public void setNumberOfBasement(Integer numberOfBasement) {
        this.numberOfBasement = numberOfBasement;
    }

    public Integer getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(Integer buildingArea) {
        this.buildingArea = buildingArea;
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

    public String getAreaDescription() {
        return areaDescription;
    }

    public void setAreaDescription(String areaDescription) {
        this.areaDescription = areaDescription;
    }

    public Integer getRentalCost() {
        return rentalCost;
    }

    public void setRentalCost(Integer rentalCost) {
        this.rentalCost = rentalCost;
    }

    public String getCostDescription() {
        return costDescription;
    }

    public void setCostDescription(String costDescription) {
        this.costDescription = costDescription;
    }

    public String getServiceCost() {
        return serviceCost;
    }

    public void setServiceCost(String serviceCost) {
        this.serviceCost = serviceCost;
    }

    public String getCarCost() {
        return carCost;
    }

    public void setCarCost(String carCost) {
        this.carCost = carCost;
    }

    public String getMotorbikeCost() {
        return motorbikeCost;
    }

    public void setMotorbikeCost(String motorbikeCost) {
        this.motorbikeCost = motorbikeCost;
    }

    public String getOvertimeCost() {
        return overtimeCost;
    }

    public void setOvertimeCost(String overtimeCost) {
        this.overtimeCost = overtimeCost;
    }

    public String getElectricityCost() {
        return electricityCost;
    }

    public void setElectricityCost(String electricityCost) {
        this.electricityCost = electricityCost;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getTimeContract() {
        return timeContract;
    }

    public void setTimeContract(String timeContract) {
        this.timeContract = timeContract;
    }

    public String getTimeDecorator() {
        return timeDecorator;
    }

    public void setTimeDecorator(String timeDecorator) {
        this.timeDecorator = timeDecorator;
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

    public String getCommissionCost() {
        return commissionCost;
    }

    public void setCommissionCost(String commissionCost) {
        this.commissionCost = commissionCost;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
