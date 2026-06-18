package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.*;

import java.util.List;

public class BuildingCommand extends AbstractCommand<BuildingDto> {
    private List<DistrictDto> districtDtoList;
    private List<WardDto> wardDtoList;
    private List<UserDto> staffDtoList;
    private BuildingType[] buildingTypeArray = BuildingType.values();
    private BuildingSearchingDto search;

    public BuildingCommand() {
        this.pojo = new BuildingDto();
        search = new BuildingSearchingDto();
    }

    public List<DistrictDto> getDistrictDtoList() {
        return districtDtoList;
    }

    public void setDistrictDtoList(List<DistrictDto> districtDtoList) {
        this.districtDtoList = districtDtoList;
    }

    public List<WardDto> getWardDtoList() {
        return wardDtoList;
    }

    public void setWardDtoList(List<WardDto> wardDtoList) {
        this.wardDtoList = wardDtoList;
    }

    public BuildingType[] getBuildingTypeArray() {
        return buildingTypeArray;
    }

    public void setBuildingTypeArray(BuildingType[] buildingTypeArray) {
        this.buildingTypeArray = buildingTypeArray;
    }

    public List<UserDto> getStaffDtoList() {
        return staffDtoList;
    }

    public void setStaffDtoList(List<UserDto> staffDtoList) {
        this.staffDtoList = staffDtoList;
    }

    public BuildingSearchingDto getSearch() {
        return search;
    }

    public void setSearch(BuildingSearchingDto search) {
        this.search = search;
    }

    public enum BuildingType {
        GROUND_FLOOR("Tầng trệt"),
        APARTMENT("Nguyên căn"),
        FURNITURE("Nội thất");

        private String typeName;

        BuildingType(String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }
}
