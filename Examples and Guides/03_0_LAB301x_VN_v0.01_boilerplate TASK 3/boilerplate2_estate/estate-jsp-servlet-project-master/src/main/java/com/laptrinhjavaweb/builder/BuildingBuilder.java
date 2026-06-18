package com.laptrinhjavaweb.builder;

public class BuildingBuilder {
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

    public BuildingBuilder(Builder builder) {
        this.name = builder.name;
        this.buildingAreaFrom = builder.buildingAreaFrom;
        this.buildingAreaTo = builder.buildingAreaTo;
        this.districtId = builder.districtId;
        this.wardId = builder.wardId;
        this.street = builder.street;
        this.managerName = builder.managerName;
        this.managerPhone = builder.managerPhone;
        this.staffArray = builder.staffArray;
        this.rentAreaFrom = builder.rentAreaFrom;
        this.rentAreaTo = builder.rentAreaTo;
        this.rentalCostFrom = builder.rentalCostFrom;
        this.rentalCostTo = builder.rentalCostTo;
        this.numberOfBasement = builder.numberOfBasement;
        this.direction = builder.direction;
        this.level = builder.level;
        this.typeArray = builder.typeArray;
    }

    public static class Builder {
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

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setBuildingAreaFrom(Integer buildingAreaFrom) {
            this.buildingAreaFrom = buildingAreaFrom;
            return this;
        }

        public Builder setBuildingAreaTo(Integer buildingAreaTo) {
            this.buildingAreaTo = buildingAreaTo;
            return this;
        }

        public Builder setDistrictId(Integer districtId) {
            this.districtId = districtId;
            return this;
        }

        public Builder setWardId(Integer wardId) {
            this.wardId = wardId;
            return this;
        }

        public Builder setStreet(String street) {
            this.street = street;
            return this;
        }

        public Builder setManagerName(String managerName) {
            this.managerName = managerName;
            return this;
        }

        public Builder setManagerPhone(String managerPhone) {
            this.managerPhone = managerPhone;
            return this;
        }

        public Builder setStaffArray(Integer[] staffArray) {
            this.staffArray = staffArray;
            return this;
        }

        public Builder setRentAreaFrom(Integer rentAreaFrom) {
            this.rentAreaFrom = rentAreaFrom;
            return this;
        }

        public Builder setRentAreaTo(Integer rentAreaTo) {
            this.rentAreaTo = rentAreaTo;
            return this;
        }

        public Builder setRentalCostFrom(Integer rentalCostFrom) {
            this.rentalCostFrom = rentalCostFrom;
            return this;
        }

        public Builder setRentalCostTo(Integer rentalCostTo) {
            this.rentalCostTo = rentalCostTo;
            return this;
        }

        public Builder setNumberOfBasement(Integer numberOfBasement) {
            this.numberOfBasement = numberOfBasement;
            return this;
        }

        public Builder setDirection(String direction) {
            this.direction = direction;
            return this;
        }

        public Builder setLevel(String level) {
            this.level = level;
            return this;
        }

        public Builder setTypeArray(String[] typeArray) {
            this.typeArray = typeArray;
            return this;
        }

        public BuildingBuilder build() {
            return new BuildingBuilder(this);
        }
    }

    public String getName() {
        return name;
    }

    public Integer getBuildingAreaFrom() {
        return buildingAreaFrom;
    }

    public Integer getBuildingAreaTo() {
        return buildingAreaTo;
    }

    public Integer getDistrictId() {
        return districtId;
    }

    public Integer getWardId() {
        return wardId;
    }

    public String getStreet() {
        return street;
    }

    public String getManagerName() {
        return managerName;
    }

    public String getManagerPhone() {
        return managerPhone;
    }

    public Integer[] getStaffArray() {
        return staffArray;
    }

    public Integer getRentAreaFrom() {
        return rentAreaFrom;
    }

    public Integer getRentAreaTo() {
        return rentAreaTo;
    }

    public Integer getRentalCostFrom() {
        return rentalCostFrom;
    }

    public Integer getRentalCostTo() {
        return rentalCostTo;
    }

    public Integer getNumberOfBasement() {
        return numberOfBasement;
    }

    public String getDirection() {
        return direction;
    }

    public String getLevel() {
        return level;
    }

    public String[] getTypeArray() {
        return typeArray;
    }
}
