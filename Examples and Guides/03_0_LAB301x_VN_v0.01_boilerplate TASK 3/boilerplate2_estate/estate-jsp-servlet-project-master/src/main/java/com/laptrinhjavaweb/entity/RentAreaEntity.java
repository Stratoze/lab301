package com.laptrinhjavaweb.entity;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Entity;
import com.laptrinhjavaweb.orm.annotation.Id;
import com.laptrinhjavaweb.orm.annotation.IdField;

@Entity(tableName = "rent_area")
@IdField(name = "id")
public class RentAreaEntity {
    @Id(autoIncrement = true)
    @Column(name = "id")
    private Integer id;

    @Column(name = "area")
    private Integer area;

    @Column(name = "building_id")
    private Integer buildingId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }
}
