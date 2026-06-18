package com.laptrinhjavaweb.service.impl;

import com.laptrinhjavaweb.builder.BuildingBuilder;
import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.converter.BuildingConverter;
import com.laptrinhjavaweb.dao.BuildingDao;
import com.laptrinhjavaweb.dao.util.SingletonDaoUtil;
import com.laptrinhjavaweb.dto.BuildingDto;
import com.laptrinhjavaweb.dto.DistrictDto;
import com.laptrinhjavaweb.dto.RentAreaDto;
import com.laptrinhjavaweb.dto.WardDto;
import com.laptrinhjavaweb.entity.BuildingEntity;
import com.laptrinhjavaweb.paging.Pageable;
import com.laptrinhjavaweb.service.*;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;
import com.laptrinhjavaweb.util.FileUploadUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BuildingServiceImpl extends AbstractService<Integer, BuildingDto, BuildingEntity> implements BuildingService {
    private DistrictService districtService;
    private WardService wardService;
    private RentAreaService rentAreaService;
    private AssignmentService assignmentService;

    public BuildingServiceImpl() {
        super.genericDao = SingletonDaoUtil.getBuildingDaoInstance();
        super.converter = new BuildingConverter();

        districtService = SingletonServiceUtil.getDistrictServiceInstance();
        wardService = SingletonServiceUtil.getWardServiceInstance();
        rentAreaService = SingletonServiceUtil.getRentAreaServiceInstance();
        assignmentService = SingletonServiceUtil.getAssignmentServiceInstance();
    }

    @Override
    public List<BuildingDto> findAll(Pageable pageable, BuildingBuilder builder) {
        List<BuildingEntity> buildingEntityList = ((BuildingDao) genericDao).findAll(pageable, builder);
        List<BuildingDto> buildingDtoList = new ArrayList<>();

//        convert entity to dto and add it to dto list
        for (BuildingEntity buildingEntity : buildingEntityList) {
            BuildingDto buildingDto = converter.entityToDto(buildingEntity);

//            get district and ward name to concatenate to full address
            WardDto wardDto = wardService.findOneById(buildingDto.getWardId());
            DistrictDto districtDto = districtService.findOneById(wardDto.getDistrictId());
            buildingDto.setFullAddress(this.buildAddress(districtDto.getName(), wardDto.getName(), buildingDto.getStreet()));

//        get rent area and set to rent area array in building dto
            List<RentAreaDto> rentAreaDtoList = rentAreaService.findAllByBuildingId(buildingDto.getId());
            List<Integer> rentAreaNumberList = new ArrayList<>();
            rentAreaDtoList.forEach(rentAreaDto -> rentAreaNumberList.add(rentAreaDto.getArea()));
            buildingDto.setRentalAreaArray(rentAreaNumberList.toArray(new Integer[rentAreaNumberList.size()]));

            buildingDtoList.add(buildingDto);
        }

        return buildingDtoList;
    }

    @Override
    public BuildingDto findOneById(Integer id) {
        BuildingEntity buildingEntity = super.genericDao.findOneById(id);
        BuildingDto buildingDto = super.converter.entityToDto(buildingEntity);

//            get district and ward name to concatenate to full address and set district id
        WardDto wardDto = wardService.findOneById(buildingDto.getWardId());
        DistrictDto districtDto = districtService.findOneById(wardDto.getDistrictId());

        buildingDto.setDistrictId(districtDto.getId());
        buildingDto.setFullAddress(this.buildAddress(districtDto.getName(), wardDto.getName(), buildingDto.getStreet()));

//        split structure to array
        if (buildingEntity.getType() != null) {
            buildingDto.setTypeArray(buildingEntity.getType().split(","));
        }

//        get rent area and set to rent area array in building dto
        List<RentAreaDto> rentAreaDtoList = rentAreaService.findAllByBuildingId(id);
        List<Integer> rentAreaNumberList = new ArrayList<>();
        rentAreaDtoList.forEach(rentAreaDto -> rentAreaNumberList.add(rentAreaDto.getArea()));
        buildingDto.setRentalAreaArray(rentAreaNumberList.toArray(new Integer[rentAreaNumberList.size()]));

        return buildingDto;
    }

    @Override
    public BuildingDto save(BuildingDto dto) throws Exception {
        dto.setCreatedDate(new Date());

        BuildingEntity entity = converter.dtoToEntity(dto);

        if (dto.getTypeArray() != null) {
            entity.setType(String.join(",", dto.getTypeArray()));
        }

//        save image
        if (StringUtils.isNotBlank(dto.getImage())) {
            String imageName = FileUploadUtil.getInstance().writeBase64(dto.getImage(),
                    SystemConstant.BUILDING_UPLOAD_PATH, SystemConstant.BUILDING_UPLOAD_PATH + dto.getId(),
                    SystemConstant.JPG);

            entity.setImage(imageName);
        }

        genericDao.save(entity);

//        save all new rent area
        this.saveRentalAreaArray(dto.getRentalAreaArray(), entity.getId());

        return converter.entityToDto(entity);
    }

    @Override
    public BuildingDto update(BuildingDto dto) throws Exception {
        BuildingEntity oldEntity = genericDao.findOneById(dto.getId());
        dto.setCreatedDate(oldEntity.getCreatedDate());
        dto.setCreatedBy(oldEntity.getCreatedBy());

        dto.setModifiedDate(new Date());

        BuildingEntity entity = converter.dtoToEntity(dto);
        if (dto.getTypeArray() != null) {
            entity.setType(String.join(",", dto.getTypeArray()));
        }

//        save image
        if (StringUtils.isNotBlank(dto.getImage())) {
            String imageName = FileUploadUtil.getInstance().writeBase64(dto.getImage(),
                    SystemConstant.BUILDING_UPLOAD_PATH, SystemConstant.BUILDING_UPLOAD_PATH + dto.getId(),
                    SystemConstant.JPG);

            entity.setImage(imageName);
        } else {
            entity.setImage(oldEntity.getImage());
        }

        genericDao.update(entity);

//        delete all old rent area and insert new
        rentAreaService.deleteAllByBuildingId(dto.getId());
        this.saveRentalAreaArray(dto.getRentalAreaArray(), entity.getId());

        return converter.entityToDto(entity);
    }

    @Override
    public void deleteById(Integer... ids) throws Exception {
        for (Integer id : ids) {
            rentAreaService.deleteAllByBuildingId(id);
            assignmentService.deleteAllByBuildingId(id);

            genericDao.deleteById(id);
        }
    }

    private String buildAddress(String district, String ward, String street) {
        String address = street;

        if (ward != null && !ward.isEmpty()) {
            address = address.concat(", ").concat(ward);
        }

        if (district != null && !district.isEmpty()) {
            address = address.concat(", ").concat(district);
        }

        return address;
    }

    private void saveRentalAreaArray(Integer[] areaArray, Integer buildingId) throws Exception {
        if (areaArray != null) {
            for (Integer area : areaArray) {
                RentAreaDto rentAreaDto = new RentAreaDto();
                rentAreaDto.setArea(area);
                rentAreaDto.setBuildingId(buildingId);

                rentAreaService.save(rentAreaDto);
            }
        }
    }
}
