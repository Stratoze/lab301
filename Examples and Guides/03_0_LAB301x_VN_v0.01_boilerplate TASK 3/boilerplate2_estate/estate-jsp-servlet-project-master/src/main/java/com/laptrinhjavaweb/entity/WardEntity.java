package com.laptrinhjavaweb.entity;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Entity;
import com.laptrinhjavaweb.orm.annotation.Id;
import com.laptrinhjavaweb.orm.annotation.IdField;

@Entity(tableName = "ward")
@IdField(name = "id")
public class WardEntity {
    @Id(autoIncrement = true)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "district_id")
    private Integer districtId;

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

    public Integer getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }
}
