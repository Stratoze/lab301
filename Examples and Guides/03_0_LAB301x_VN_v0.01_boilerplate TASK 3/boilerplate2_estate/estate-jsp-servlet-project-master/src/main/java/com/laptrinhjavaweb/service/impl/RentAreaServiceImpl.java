package com.laptrinhjavaweb.service.impl;

import com.laptrinhjavaweb.converter.RentAreaConverter;
import com.laptrinhjavaweb.dao.impl.RentAreaDaoImpl;
import com.laptrinhjavaweb.dao.util.SingletonDaoUtil;
import com.laptrinhjavaweb.dto.RentAreaDto;
import com.laptrinhjavaweb.entity.RentAreaEntity;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Logical;
import com.laptrinhjavaweb.service.RentAreaService;

import java.util.Arrays;
import java.util.List;

public class RentAreaServiceImpl extends AbstractService<Integer, RentAreaDto, RentAreaEntity> implements RentAreaService {
    public RentAreaServiceImpl() {
        super.genericDao = SingletonDaoUtil.getRentAreaDaoInstance();
        super.converter = new RentAreaConverter();
    }

    @Override
    public List<RentAreaDto> findAllByBuildingId(Integer buildingId) {
        Criterion criterion = Logical.and("buildingId").eq(buildingId);
        return super.findAllByProperties(null, Arrays.asList(criterion));
    }

    @Override
    public void deleteAllByBuildingId(Integer id) throws Exception {
        ((RentAreaDaoImpl) genericDao).deleteAllByBuildingId(id);
    }
}
