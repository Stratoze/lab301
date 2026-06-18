package com.laptrinhjavaweb.service.impl;

import com.laptrinhjavaweb.converter.DistrictConverter;
import com.laptrinhjavaweb.dao.util.SingletonDaoUtil;
import com.laptrinhjavaweb.dto.DistrictDto;
import com.laptrinhjavaweb.entity.DistrictEntity;
import com.laptrinhjavaweb.service.DistrictService;

public class DistrictServiceImpl extends AbstractService<Integer, DistrictDto, DistrictEntity> implements DistrictService {
    public DistrictServiceImpl() {
        super.genericDao = SingletonDaoUtil.getDistrictDaoInstance();
        super.converter = new DistrictConverter();
    }
}
