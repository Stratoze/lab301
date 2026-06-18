package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.paging.Pageable;

import java.io.Serializable;
import java.util.List;

public interface GenericService<ID extends Serializable, D> {
    List<D> findAll();

    List<D> findAllByProperties(Pageable pageable, List<Criterion> criterionList);

    D findOneById(ID id);

    D save(D dto) throws Exception;

    D update(D dto) throws Exception;

    void deleteById(ID... ids) throws Exception;
}
