package com.laptrinhjavaweb.dao;

import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.paging.Pageable;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

public interface GenericDao<ID extends Serializable, T> {
    List<T> findAll();

    List<T> findAllByProperties(Pageable pageable, List<Criterion> criterionList);

    Long countByProperties(List<Criterion> criterionList);

    T findOneById(ID id);

    T findOneByProperties(List<Criterion> criterionList);

    void save(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void delete(T entity) throws SQLException;

    void deleteById(ID... id) throws SQLException;
}
