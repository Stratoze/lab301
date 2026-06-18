package com.laptrinhjavaweb.dao.impl;

import com.laptrinhjavaweb.dao.RentAreaDao;
import com.laptrinhjavaweb.entity.RentAreaEntity;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQuery;
import com.laptrinhjavaweb.orm.session.Session;
import com.laptrinhjavaweb.orm.session.SessionFactory;
import com.laptrinhjavaweb.orm.transaction.Transaction;

import java.sql.SQLException;

public class RentAreaDaoImpl extends AbstractDao<Integer, RentAreaEntity> implements RentAreaDao {
    @Override
    public void deleteAllByBuildingId(Integer id) throws SQLException {
        Session session = SessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            SQLQuery query = session.createSQLQuery("DELETE FROM rent_area WHERE building_id={id}");
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
}
