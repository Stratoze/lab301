package com.laptrinhjavaweb.dao.impl;

import com.laptrinhjavaweb.dao.AssignmentDao;
import com.laptrinhjavaweb.entity.AssignmentEntity;
import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Logical;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQuery;
import com.laptrinhjavaweb.orm.session.Session;
import com.laptrinhjavaweb.orm.session.SessionFactory;
import com.laptrinhjavaweb.orm.transaction.Transaction;

import java.sql.SQLException;

public class AssignmentDaoImpl extends AbstractDao<Integer, AssignmentEntity> implements AssignmentDao {
    @Override
    public void deleteAllByBuildingId(Integer id) throws SQLException {
        Session session = SessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            SQLQuery query = session.createSQLQuery("DELETE FROM assignment WHERE building_id={id}");
            query.setParam("id", id);
            query.executeUpdate();
            transaction.commit();
        } catch (SQLException e) {
            transaction.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public AssignmentEntity findOneByStaffIdAndBuildingId(Integer staffId, Integer buildingId) {
        Session session = SessionFactory.openSession();
        try {
            Criteria criteria = session.createCriteria(AssignmentEntity.class);
            criteria.addCriterion(Logical.and("buildingId").eq(buildingId));
            criteria.addCriterion(Logical.and("userId").eq(staffId));

            return (AssignmentEntity) criteria.uniqueResult();
        } finally {
            session.close();
        }
    }
}
