package com.laptrinhjavaweb.entity;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Entity;
import com.laptrinhjavaweb.orm.annotation.IdField;
import com.laptrinhjavaweb.orm.annotation.Id;

import java.sql.Date;

@Entity(tableName = "assignment")
@IdField(name = "id")
public class AssignmentEntity {
    @Id(autoIncrement = true)
    @Column(name = "id")
    private Integer id;

    @Column(name = "building_id")
    private Integer buildingId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "created_by")
    private String createdBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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
}
